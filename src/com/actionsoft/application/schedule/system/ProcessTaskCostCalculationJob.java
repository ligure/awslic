package com.actionsoft.application.schedule.system;

import java.lang.reflect.Constructor;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.application.server.LICENSE;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.cache.CompanyCache;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.RoleCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.rule.JumpActivityRuleEngine;
import com.actionsoft.awf.rule.ProcessRuleEngine;
import com.actionsoft.awf.util.ClassReflect;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.workflow.calendar.util.CalWorkTimeImp;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCostCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepCostModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.execute.PriorityType;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.WorkflowTaskEngine;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.awf.workflow.execute.route.impl.RouteAbst;
import com.actionsoft.awf.workflow.execute.route.impl.RouteFactory;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskExecuteWeb;
import com.actionsoft.coe.bpa.etl.collector.WorkFlowTaskTimeoutCollectorImp;
import com.actionsoft.eai.shortmessage.SMSContext;
import com.actionsoft.eai.shortmessage.SendSMSUtil;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.loader.core.TaskTimeOutEventA;
import com.actionsoft.plugs.email.util.AWSMailUtil;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;

public class ProcessTaskCostCalculationJob implements IJob {
    private static boolean isExsitLogTable = false;

    static {
	MetaDataModel metaDataModel = (MetaDataModel) MetaDataCache
		.getModel("BO_AWS_RT_COSTLOG");
	if (metaDataModel != null)
	    isExsitLogTable = true;
    }

    public void execute(JobExecutionContext context)
	    throws JobExecutionException {
	long beginTime = System.currentTimeMillis();
	executeAction(context);
	long endTime = System.currentTimeMillis();
	System.out.println("信息: ["
		+ UtilDate.datetimeFormat(new Date(beginTime))
		+ "]AWS Process cost calculation:[" + (endTime - beginTime)
		/ 1000L + "s]");
    }

    private void executeAction(JobExecutionContext context) {
	Hashtable<Integer, TaskInstanceModel> workingTask = ProcessRuntimeDaoFactory
		.createTaskInstance().getAllActiveTaskListOfStatus(1);
	for (int i = 0; i < workingTask.size(); ++i) {
	    TaskInstanceModel taskInstanceModel = (TaskInstanceModel) workingTask
		    .get(new Integer(i));
	    if (taskInstanceModel.getStatus() != 1)
		continue;
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    taskInstanceModel.getProcessInstanceId());
	    if (processInstanceModel == null) {
		System.out.println("警告：ID为[" + taskInstanceModel.getId()
			+ "]标题为[" + taskInstanceModel.getTitle()
			+ "]的任务已丢失流程实例，超时策略未执行!");
	    } else {
		WorkFlowStepModel stepModel = WorkFlowStepCache
			.getModelOfStepNo(
				processInstanceModel.getProcessDefinitionId(),
				processInstanceModel.getActivityDefinitionNo());
		if (stepModel == null) {
		    System.out.println("警告：ID为[" + taskInstanceModel.getId()
			    + "]标题为[" + taskInstanceModel.getTitle()
			    + "]的任务所在流程节点模型未找到(bindid="
			    + processInstanceModel.getId() + ",wfId="
			    + processInstanceModel.getProcessDefinitionId()
			    + ",wfsNo="
			    + processInstanceModel.getActivityDefinitionNo()
			    + ")，超时策略未执行!");
		} else {
		    UserModel targetUserModel = null;
		    DepartmentModel targetDepartmentModel = null;
		    CompanyModel targetCompanyModel = null;
		    RoleModel targetRoleModel = null;
		    Hashtable<?, ?> costList = WorkFlowStepCostCache
			    .getListOfWorkFlowStep(stepModel._id);
		    for (int ii = 0; ii < costList.size(); ++ii) {
			WorkFlowStepCostModel costModel = (WorkFlowStepCostModel) costList
				.get(new Integer(ii));

			boolean isExcuteCost = false;
			long taskCostNow = 0L;

			if (costModel._calcType == 1
				&& taskInstanceModel.getReadTime() == null) {
			    continue;
			}

			if (costModel._calcType == 3) {
			    Calendar c = Calendar.getInstance();
			    c.setTime(new Date(taskInstanceModel.getBeginTime()
				    .getTime()));
			    taskCostNow = CalWorkTimeImp.getInstanceByUid(
				    taskInstanceModel.getTarget())
				    .calcWorkingTime(c);
			    isExcuteCost = !isExcutedCostTrigger(
				    taskInstanceModel, costModel);
			} else {
			    if (DBSql.getInt(
				    "select count(*) as c from BO_AWS_RT_COSTLOG where TASKID="
					    + taskInstanceModel.getId()
					    + " and COSTID=" + costModel._id,
				    "c") > 0) {
				continue;
			    }
			    double taskCostTimes = costModel._cost;
			    if (taskCostTimes == -100.0D) {
				if (stepModel._duration == 0) {
				    System.err
					    .println("时限监控-[警告][合理期限=0][处理结果=忽略执行该策略][Task="
						    + taskInstanceModel
							    .getTitle() + "]");
				    continue;
				}
				taskCostTimes = Double.parseDouble(Integer
					.toString(stepModel._duration)) / 1000.0D / 60.0D / 60.0D;
				if (taskCostTimes == 0.0D) {
				    System.err
					    .println("时限监控-[警告][合理期限=数值太小][处理结果=忽略执行该策略][Task="
						    + taskInstanceModel
							    .getTitle() + "]");
				    continue;
				}
			    }
			    if (taskCostTimes == -101.0D) {
				if (stepModel._durationWarning == 0) {
				    System.err
					    .println("时限监控-[警告][宽延期限=0][处理结果=忽略执行该策略][Task="
						    + taskInstanceModel
							    .getTitle() + "]");
				    continue;
				}
				taskCostTimes = Double.parseDouble(Integer
					.toString(stepModel._durationWarning)) / 1000.0D / 60.0D / 60.0D;
				if (taskCostTimes == 0.0D) {
				    System.err
					    .println("时限监控-[警告][宽延期限=数值太小][处理结果=忽略执行该策略][Task="
						    + taskInstanceModel
							    .getTitle() + "]");
				    continue;
				}
			    }
			    Calendar c = Calendar.getInstance();
			    if (costModel._calcType == 1) {
				c.setTime(new Date(taskInstanceModel
					.getReadTime().getTime()));
			    } else if (costModel._calcType == 0) {
				c.setTime(new Date(taskInstanceModel
					.getBeginTime().getTime()));
			    } else if (costModel._calcType == 2) {
				String timeValueStr = costModel._bizTime;
				if (timeValueStr.indexOf("@") > -1) {
				    try {
					UserContext targetContext = new UserContext(
						taskInstanceModel.getTarget());
					RuntimeFormManager rfm = new RuntimeFormManager(
						targetContext,
						taskInstanceModel
							.getProcessInstanceId(),
						taskInstanceModel.getId(), 0, 0);
					timeValueStr = rfm
						.convertMacrosValue(costModel._bizTime);
				    } catch (Exception e) {
					e.printStackTrace(System.err);
				    }
				}
				long beginTime = UtilDate.getTimes(
					timeValueStr, "yyyy-MM-dd HH:mm:ss");
				if (beginTime == 0L) {
				    new UtilDate();
				    beginTime = UtilDate.getTimes(timeValueStr,
					    "yyyy-MM-dd");
				    if (beginTime == 0L) {
					System.err
						.println("时限监控-[警告][业务参数="
							+ timeValueStr
							+ "][可能是非合法的yyyy-MM-dd HH:mm:ss格式][处理结果=忽略执行该策略][Task="
							+ taskInstanceModel
								.getTitle()
							+ "]");
					continue;
				    }
				}
				c.setTime(new Date(beginTime));
			    }
			    taskCostNow = CalWorkTimeImp.getInstanceByUid(
				    taskInstanceModel.getTarget())
				    .calcWorkingTime(c);
			    isExcuteCost = taskCostNow > taskCostTimes * 60.0D;
			}
			String sysAutoTitle = "无";
			if (!isExcuteCost)
			    continue;
			String bizRuleStr = costModel._bizRule;
			if (bizRuleStr.indexOf("@") > -1) {
			    try {
				UserContext targetContext = new UserContext(
					taskInstanceModel.getTarget());
				RuntimeFormManager rfm = new RuntimeFormManager(
					targetContext,
					taskInstanceModel
						.getProcessInstanceId(),
					taskInstanceModel.getId(), 0, 0);
				bizRuleStr = rfm
					.convertMacrosValue(costModel._bizRule);
			    } catch (Exception e) {
				e.printStackTrace(System.err);
			    }
			}
			if (bizRuleStr.trim().length() > 0) {
			    bizRuleStr = bizRuleStr.trim().toLowerCase();
			    if (!bizRuleStr.equals("true")
				    && !bizRuleStr.equals("yes")
				    && !bizRuleStr.equals("是")
				    && !bizRuleStr.equals("1")
				    && !bizRuleStr.equals("on")) {
				System.err
					.println("时限监控-[信息][业务条件=FALSE][处理结果=忽略执行该策略][Task="
						+ taskInstanceModel.getTitle()
						+ "]");
			    }
			} else {
			    TaskInstanceModel tmpModel = ProcessRuntimeDaoFactory
				    .createTaskInstance().getInstanceOfActive(
					    taskInstanceModel.getId());
			    if (tmpModel == null) {
				continue;
			    }
			    int timeOutLogBoId = 0;
			    int timeOutLogWorkflowInstanceId = 0;
			    if (isExsitLogTable) {
				if (targetUserModel == null)
				    targetUserModel = (UserModel) UserCache
					    .getModel(taskInstanceModel
						    .getTarget());
				if (targetUserModel == null)
				    continue;
				if (targetDepartmentModel == null)
				    targetDepartmentModel = (DepartmentModel) DepartmentCache
					    .getModel(targetUserModel
						    .getDepartmentId());
				if (targetDepartmentModel == null)
				    continue;
				if (targetCompanyModel == null)
				    targetCompanyModel = (CompanyModel) CompanyCache
					    .getModel(targetDepartmentModel
						    .getCompanyId());
				if (targetCompanyModel == null)
				    continue;
				if (targetRoleModel == null)
				    targetRoleModel = (RoleModel) RoleCache
					    .getModel(targetUserModel
						    .getRoleId());
				if (targetRoleModel == null) {
				    continue;
				}
				if (costModel._costPolicy == -3)
				    sysAutoTitle = "发送内网邮件通知";
				else if (costModel._costPolicy == -2)
				    sysAutoTitle = "发送外网邮件通知";
				else if (costModel._costPolicy == -1)
				    sysAutoTitle = "自动向下执行";
				else if (costModel._costPolicy == -4)
				    sysAutoTitle = "将该任务退回";
				else if (costModel._costPolicy == -5)
				    sysAutoTitle = "给当任务办理者发送了催办短信";
				else if (costModel._costPolicy != -99
					&& costModel._costPolicy == -100) {
				    sysAutoTitle = "自定义的超时动作，未知";
				}
				try {
				    WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache
					    .getModel(WorkflowEngine
						    .getInstance()
						    .getWorkflowDefId(
							    "ff85be74cca5a04c52d9249c0778b446"));
				    if (flowModel._workFlowType == 1) {
					timeOutLogWorkflowInstanceId = WorkflowInstanceAPI
						.getInstance()
						.createBOInstance(
							"ff85be74cca5a04c52d9249c0778b446",
							"admin",
							"["
								+ targetUserModel
									.getUserName()
								+ "]在["
								+ UtilDate
									.datetimeFormat(new Date())
								+ "]"
								+ I18nRes
									.findValue("的流程绩效超时记录"));
				    } else {
					timeOutLogWorkflowInstanceId = WorkflowInstanceAPI
						.getInstance()
						.createProcessInstance(
							"ff85be74cca5a04c52d9249c0778b446",
							"admin",
							"["
								+ targetUserModel
									.getUserName()
								+ "]在["
								+ UtilDate
									.datetimeFormat(new Date())
								+ "]"
								+ I18nRes
									.findValue("的流程绩效超时记录"));
					int j = WorkflowTaskInstanceAPI
						.getInstance()
						.createProcessTaskInstance(
							"admin",
							timeOutLogWorkflowInstanceId,
							0,
							1,
							1,
							"admin",
							"["
								+ targetUserModel
									.getUserName()
								+ "]在["
								+ UtilDate
									.datetimeFormat(new Date())
								+ "]"
								+ I18nRes
									.findValue("的流程绩效超时记录"),
							false, 0)[0];
				    }
				    if (timeOutLogWorkflowInstanceId > 0) {
					Hashtable<String, Object> rowData = new UnsyncHashtable<String, Object>();
					rowData.put("YEAR", Integer
						.toString(UtilDate
							.getYear(new Date())));
					rowData.put("MONTH", Integer
						.toString(UtilDate
							.getMonth(new Date())));
					rowData.put("DAY", Integer
						.toString(UtilDate
							.getDay(new Date())));
					rowData.put("COMPANYNAME",
						targetCompanyModel
							.getCompanyName());
					rowData.put("DEPTID", Integer
						.toString(targetDepartmentModel
							.getId()));
					rowData.put("DEPTNAME",
						targetDepartmentModel
							.getDepartmentName());
					rowData.put("AWSID",
						targetUserModel.getUID());
					rowData.put("USERNAME",
						targetUserModel.getUserName());
					rowData.put("ROLEID", Integer
						.toString(targetRoleModel
							.getId()));
					rowData.put(
						"ROLENAME",
						targetRoleModel.getGroupName()
							+ "/"
							+ targetRoleModel
								.getRoleName());
					rowData.put(
						"INSTANCEID",
						Integer.toString(taskInstanceModel
							.getProcessInstanceId()));
					rowData.put("TASKID", Integer
						.toString(taskInstanceModel
							.getId()));
					rowData.put("TITLE",
						taskInstanceModel.getTitle());
					rowData.put("COSTPOINT", Integer
						.toString(costModel._costPoint));
					rowData.put("COSTID",
						Integer.toString(costModel._id));
					rowData.put("COSTTIME",
						Long.toString(taskCostNow));
					rowData.put("FLOWPOINTER",
						Integer.valueOf(flowModel._id));
					rowData.put("STATUS", "待核定");
					rowData.put("SYSAUTO", sysAutoTitle);
					timeOutLogBoId = BOInstanceAPI
						.getInstance()
						.createBOData(
							"BO_AWS_RT_COSTLOG",
							rowData,
							timeOutLogWorkflowInstanceId,
							"admin");
					if (timeOutLogBoId > 0) {
					    if (LICENSE.isBPA()
						    && timeOutLogBoId > 0) {
						WorkFlowTaskTimeoutCollectorImp taskStartImp = new WorkFlowTaskTimeoutCollectorImp();
						taskStartImp
							.setModel(taskInstanceModel);
						taskStartImp.collectorData();
					    }

					    DBSql.executeUpdate("UPDATE BO_AWS_RT_COSTLOG SET LOGDATE="
						    + DBSql.getDateDefaultValue()
						    + ",BEGINDATE="
						    + DBSql.convertLongDate(UtilDate
							    .datetimeFormat(taskInstanceModel
								    .getBeginTime()))
						    + " WHERE ID="
						    + timeOutLogBoId);
					}
				    }
				} catch (Exception e) {
				    System.err.println("超时策略计算[wf_task.id="
					    + taskInstanceModel.getId() + "]["
					    + taskInstanceModel.getTitle()
					    + "]时发生错误");
				    e.printStackTrace(System.err);
				}
			    } else {
				System.out.println("超时策略执行完，但未记录到COSTLOG日志中!");
			    }

			    if (costModel._costPolicy == -3) {
				// 发送内部邮件提醒
				executeTaskInnerMail(taskInstanceModel,
					costModel);
			    } else if (costModel._costPolicy == -2) {
				// 发送外部邮件提醒
				executeTaskOuterMail(taskInstanceModel,
					costModel);
			    } else if (costModel._costPolicy == -1) {
				// 由系统自动向下执行
				executeTask(taskInstanceModel,
					processInstanceModel);
			    } else if (costModel._costPolicy == -4) {
				// 由系统自动退回给上个执行者
				rollbackTask(taskInstanceModel,
					processInstanceModel);
			    } else if (costModel._costPolicy == -5) {
				// 由系统给当前执行者发送短信催办
				sendShortMessage(taskInstanceModel,
					processInstanceModel);
			    } else if (costModel._costPolicy == -100) {
				// 触发一个Event事件
				boolean isExecute = executeClazz(taskCostNow,
					taskInstanceModel,
					processInstanceModel, stepModel,
					costModel);
				if (!isExecute
					&& timeOutLogWorkflowInstanceId > 0)
				    try {
					WorkflowEngine
						.getInstance()
						.removeProcessInstance(
							timeOutLogWorkflowInstanceId);
				    } catch (Exception e) {
					e.printStackTrace(System.err);
				    }
			    }
			    ProcessRuntimeDaoFactory
				    .createProcessInstance()
				    .setOvertime(
					    taskInstanceModel
						    .getProcessInstanceId(),
					    true);
			}
		    }
		}
	    }
	}
	String days = AWFConfig._awfServerConf.getTaskNoticeDays();
	if (!days.equals("0")) {
	    Hashtable<Integer, TaskInstanceModel> notifyTask = ProcessRuntimeDaoFactory
		    .createTaskInstance().getAllActiveTaskListOfStatus(9);
	    for (int i = 0; i < notifyTask.size(); ++i) {
		TaskInstanceModel taskInstanceModel = (TaskInstanceModel) notifyTask
			.get(new Integer(i));
		if (System.currentTimeMillis() > taskInstanceModel
			.getBeginTime().getTime()
			+ Long.parseLong(days)
			* 24L
			* 60L * 60L * 1000L) {
		    System.out.println("[通知]已超过三天,被自动清除。阅读者["
			    + taskInstanceModel.getTarget() + "]标题["
			    + taskInstanceModel.getTitle() + "]");
		    DBSql.executeUpdate("delete from wf_task where id="
			    + taskInstanceModel.getId());
		}
	    }
	}
    }

    private boolean executeClazz(long taskCost,
	    TaskInstanceModel taskInstanceModel,
	    ProcessInstanceModel processInstanceModel,
	    WorkFlowStepModel stepModel, WorkFlowStepCostModel costModel) {
	try {
	    Constructor cons = null;
	    Class[] parameterTypes = new Class[0];
	    cons = ClassReflect.getConstructor(costModel._bizClazz,
		    parameterTypes);
	    if (cons == null)
		return false;
	    Object[] initargets = new Object[0];
	    TaskTimeOutEventA superClass = (TaskTimeOutEventA) cons
		    .newInstance(initargets);
	    if (superClass == null)
		return false;
	    return superClass.taskTimeOut(
		    taskInstanceModel.getProcessInstanceId(),
		    taskInstanceModel.getId(), taskInstanceModel,
		    processInstanceModel, taskCost, stepModel);
	} catch (Exception e) {
	    System.err.println("节点限时触发的自定义Java类【" + costModel._bizClazz
		    + "】没有在超时计算时正确执行!");
	    e.printStackTrace(System.err);
	}
	return false;
    }

    private boolean isExcutedCostTrigger(TaskInstanceModel taskInstanceModel,
	    WorkFlowStepCostModel wfsCostModel) {
	if (wfsCostModel._costText == null) {
	    return true;
	}
	String[] cost = wfsCostModel._costText.split(":");
	int rule = Integer.parseInt(cost[0]);
	int year = UtilDate.getYear(new Date());
	int month = UtilDate.getMonth(new Date());
	int day = UtilDate.getDay(new Date());
	int hour = UtilDate.getHour(new Date());
	int minutes = UtilDate.getMinute(new Date());
	int week = UtilDate.getDayOfWeek(year, month, day);
	switch (rule) {
	case 0: {
	    String h = cost[1];
	    String m = cost[2];
	    Timestamp taskBeginTime = taskInstanceModel.getBeginTime();
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(11, Integer.parseInt(h));
	    calendar.set(12, Integer.parseInt(m));
	    if (!hasCostExcuteed(hour, h, minutes, m, year, month, day,
		    taskInstanceModel.getId(), wfsCostModel._id,
		    calendar.getTime(), taskBeginTime))
		return false;
	    break;
	}
	case 1: {
	    String h = cost[2];
	    String m = cost[3];
	    String[] weeks = cost[1].split(",");
	    for (int i = 0; i < weeks.length; ++i) {
		if (week == Integer.parseInt(weeks[i])) {
		    Timestamp taskBeginTime = taskInstanceModel.getBeginTime();
		    Calendar calendar = UtilDate
			    .getCalendarByDayOfCurrentWeek(week);
		    calendar.set(11, Integer.parseInt(h));
		    calendar.set(12, Integer.parseInt(m));
		    if (!hasCostExcuteed(hour, h, minutes, m, year, month, day,
			    taskInstanceModel.getId(), wfsCostModel._id,
			    calendar.getTime(), taskBeginTime)) {
			return false;
		    }
		}
	    }
	    break;
	}
	case 2: {
	    String h = cost[2];
	    String m = cost[3];
	    String[] days = cost[1].split(",");
	    for (int i = 0; i < days.length; ++i) {
		if (day == Integer.parseInt(days[i])) {
		    Timestamp taskBeginTime = taskInstanceModel.getBeginTime();
		    Calendar calendar = UtilDate
			    .getCalendarByDayOfCurrentMonth(day);
		    calendar.set(11, Integer.parseInt(h));
		    calendar.set(12, Integer.parseInt(m));
		    if (!hasCostExcuteed(hour, h, minutes, m, year, month, day,
			    taskInstanceModel.getId(), wfsCostModel._id,
			    calendar.getTime(), taskBeginTime)) {
			return false;
		    }
		}
	    }
	    break;
	}
	case 3: {
	    String h = cost[3];
	    String m = cost[4];
	    if (month % 3 + 1 != Integer.parseInt(cost[1])
		    || day != Integer.parseInt(cost[2]))
		break;
	    Timestamp taskBeginTime = taskInstanceModel.getBeginTime();
	    Calendar calendar = UtilDate.getCalendarByCurrentQuarter(
		    month % 3 + 1, day);
	    calendar.set(11, Integer.parseInt(h));
	    calendar.set(12, Integer.parseInt(m));
	    if (!hasCostExcuteed(hour, h, minutes, m, year, month, day,
		    taskInstanceModel.getId(), wfsCostModel._id,
		    calendar.getTime(), taskBeginTime))
		return false;
	    break;
	}
	case 4:
	    String h = cost[3];
	    String m = cost[4];
	    if (month != Integer.parseInt(cost[1])
		    || day != Integer.parseInt(cost[2]))
		break;
	    Timestamp taskBeginTime = taskInstanceModel.getBeginTime();
	    Calendar calendar = UtilDate.getCalendarByCurrentYear(month, day);
	    calendar.set(11, Integer.parseInt(h));
	    calendar.set(12, Integer.parseInt(m));
	    if (!hasCostExcuteed(hour, h, minutes, m, year, month, day,
		    taskInstanceModel.getId(), wfsCostModel._id,
		    calendar.getTime(), taskBeginTime))
		return false;
	    break;
	}
	return true;
    }

    private boolean hasCostExcuteed(int hour, String h, int minutes, String m,
	    int year, int month, int day, int taskId, int costId, Date date,
	    Timestamp taskBeginTime) {
	if (hour > Integer.parseInt(h) && date.after(taskBeginTime)) {
	    if (!isCostExcuteed(year, month, day, taskId, costId))
		return false;
	} else if (hour == Integer.parseInt(h)
		&& minutes >= Integer.parseInt(m) && date.after(taskBeginTime)
		&& !isCostExcuteed(year, month, day, taskId, costId)) {
	    return false;
	}
	return true;
    }

    private boolean isCostExcuteed(int year, int month, int day, int taskid,
	    int costId) {
	String sql = "select count(id) as c from BO_AWS_RT_COSTLOG where YEAR="
		+ year + " and MONTH=" + month + " and DAY=" + day
		+ " and TASKID=" + taskid + " and COSTID=" + costId;
	return DBSql.getInt(sql, "c") > 0;
    }

    private void sendShortMessage(TaskInstanceModel taskInstanceModel,
	    ProcessInstanceModel processInstanceModel) {
	UserContext owner = null;
	try {
	    owner = new UserContext(taskInstanceModel.getTarget());
	} catch (Exception localException) {
	}
	if (owner != null) {
	    SMSContext sms = new SMSContext();
	    sms.setCompanyName(owner.getCompanyModel().getCompanyName());
	    sms.setDepartmentName(owner.getDepartmentModel()
		    .getDepartmentName());
	    sms.setUid(owner.getUID());
	    sms.setUserName(owner.getUserModel().getUserName());

	    UserModel model = (UserModel) UserCache.getModel(taskInstanceModel
		    .getTarget());
	    if (model != null && model.getSMid() != null
		    && !model.getSMid().equals(""))
		sms.setMobileCode(model.getSMid().trim());
	    else if (model != null && model.getMobile() != null
		    && !model.getMobile().equals("")) {
		sms.setMobileCode(model.getMobile().trim());
	    }
	    sms.setMobileID(model.getUID().trim());
	    sms.setMobileUserName(model.getUserName());
	    SendSMSUtil send = new SendSMSUtil(sms);
	    send.send("超时待办提醒:" + taskInstanceModel.getTitle());
	}
    }

    private void rollbackTask(TaskInstanceModel taskInstanceModel,
	    ProcessInstanceModel processInstanceModel) {
	int wfsId = DBSql.getInt("select WFSID from wf_task_log where bind_id="
		+ processInstanceModel.getId()
		+ " and status=1 order by id desc", "WFSID");
	if (wfsId > 0) {
	    WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache
		    .getModel(wfsId);
	    if (stepModel != null) {
		String owner = DBSql.getString(
			"select owner from wf_task_log where bind_id="
				+ processInstanceModel.getId()
				+ " and status=1 order by id desc", "owner");
		String target = DBSql.getString(
			"select target from wf_task_log where bind_id="
				+ processInstanceModel.getId()
				+ " and status=1 order by id desc", "target");
		String title = DBSql.getString(
			"select title from wf_task_log where bind_id="
				+ processInstanceModel.getId()
				+ " and status=1 order by id desc", "title");
		try {
		    int[] taskId = WorkflowTaskEngine.getInstance()
			    .createProcessTaskInstance(owner,
				    processInstanceModel.getId(),
				    SynType.synchronous, PriorityType.normal,
				    1, stepModel._stepNo, target,
				    "(超时未办回退)[" + title, false, 0);
		    if (taskId[0] > 0) {
			DBSql.executeUpdate("delete from wf_task where id="
				+ taskInstanceModel.getId());
			DBSql.executeUpdate("delete from wf_task_log where id="
				+ taskInstanceModel.getId());
		    }
		} catch (WorkflowException we) {
		    we.printStackTrace(System.err);
		}
	    }
	}
    }

    private void executeTask(TaskInstanceModel taskInstanceModel,
	    ProcessInstanceModel processInstanceModel) {
	UserContext uc = null;
	try {
	    uc = new UserContext(taskInstanceModel.getTarget());
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
	String returnCode = "";
	try {
	    returnCode = WorkflowTaskEngine.getInstance()
		    .assignComplexProcessTaskInstance(uc,
			    taskInstanceModel.getProcessInstanceId(),
			    taskInstanceModel.getId());
	    WorkflowTaskInstanceAPI.getInstance().appendOpinionHistory(
		    taskInstanceModel.getProcessInstanceId(),
		    taskInstanceModel.getId(), "超时",
		    "<font color=red>超时系统自动处理</font>");
	    if (returnCode.equals("processEnd")) {
		WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache
			.getModel(processInstanceModel.getProcessDefinitionId());
		if (workFlowModel._isAutoArchives) {
		    new UserTaskExecuteWeb().toArchives(uc, taskInstanceModel
			    .getProcessInstanceId(), -Integer.parseInt(UtilDate
			    .yearFormat(new Timestamp(System
				    .currentTimeMillis()))), taskInstanceModel
			    .getId());
		} else {
		    System.out.println("节点超时策略执行提醒：任务["
			    + taskInstanceModel.getTitle()
			    + "]所依赖的工作流被指定为手工归档，系统无法自动完成，超时策略执行失败！");
		    return;
		}

		WorkflowTaskEngine.getInstance().closeProcessTaskInstance(uc,
			taskInstanceModel.getProcessInstanceId(),
			taskInstanceModel.getId());
		return;
	    }
	    if (returnCode.equals("assign")) {
		JumpActivityRuleEngine jumpEngine = ProcessRuleEngine
			.getInstance().jumpActivityRuleEngine(uc,
				taskInstanceModel.getProcessInstanceId(),
				taskInstanceModel.getId());
		int r = jumpEngine.getNextActivityNo();
		if (r == -1 || r == 9999) {
		    WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache
			    .getModel(processInstanceModel
				    .getProcessDefinitionId());
		    if (workFlowModel._isAutoArchives) {
			new UserTaskExecuteWeb().toArchives(uc,
				taskInstanceModel.getProcessInstanceId(),
				-Integer.parseInt(UtilDate
					.yearFormat(new Timestamp(System
						.currentTimeMillis()))),
				taskInstanceModel.getId());
			return;
		    }
		    System.out.println("节点超时策略执行警告：任务["
			    + taskInstanceModel.getTitle()
			    + "]所依赖的工作流被指定为手工归档，系统无法自动完成，超时策略执行失败！");
		    return;
		}
		WorkFlowStepModel stepModel = WorkFlowStepCache
			.getModelOfStepNo(
				processInstanceModel.getProcessDefinitionId(),
				r);
		DepartmentModel localDepartmentModel = uc.getDepartmentModel();
		int ownerDepartmentId = taskInstanceModel
			.getOwnerDepartmentId();
		if (ownerDepartmentId > 0) {
		    if (UserCache.isExistInDepartment(
			    taskInstanceModel.getOwnerDepartmentId(),
			    uc.getID())) {
			localDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(taskInstanceModel
					.getOwnerDepartmentId());
		    } else {
			DepartmentModel tmpDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(taskInstanceModel
					.getOwnerDepartmentId());

			if (UserCache.isExistInDepartment(
				tmpDepartmentModel.getParentDepartmentId(),
				uc.getID())) {
			    localDepartmentModel = (DepartmentModel) DepartmentCache
				    .getModel(tmpDepartmentModel
					    .getParentDepartmentId());
			}

		    }

		    if (localDepartmentModel == null) {
			localDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(((UserModel) UserCache
					.getModel(taskInstanceModel.getOwner()))
					.getDepartmentId());
		    }
		}
		String workMan = stepModel._stepUser;
		Object o = RouteFactory.getInstance(uc, processInstanceModel,
			localDepartmentModel, ownerDepartmentId,
			stepModel._routeType);

		if (o != null) {
		    workMan = ((RouteAbst) o).getTargetUserAddress(stepModel,
			    taskInstanceModel.getId());
		} else {
		    System.err.println("没有找到相关的路由处理类!");
		    return;
		}
		if (workMan == null || workMan.equals("")) {
		    System.err.println("节点超时策略执行提醒：任务["
			    + taskInstanceModel.getTitle() + "]的下一个节点是["
			    + stepModel._stepName
			    + "]，但是该节点没有指定固定的办理者，系统将此办理任务发送给该流程管理员！");
		    WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache
			    .getModel(processInstanceModel
				    .getProcessDefinitionId());
		    workMan = workFlowModel._flowMaster;
		    if (workMan == null || workMan.equals("")) {
			System.err
				.println("节点超时策略执行提醒：任务["
					+ taskInstanceModel.getTitle()
					+ "]的下一个节点是["
					+ stepModel._stepName
					+ "]，但是该节点没有指定固定的办理者，也没有发现流程管理员，系统无法自动完成，超时策略执行失败！");
			return;
		    }

		}
		DBSql.executeUpdate("update wf_task set title='**超时自动处理:"
			+ taskInstanceModel.getTitle() + "' where id="
			+ taskInstanceModel.getId());
		try {
		    int[] taskId = WorkflowTaskEngine.getInstance()
			    .createProcessTaskInstance(
				    uc,
				    taskInstanceModel.getProcessInstanceId(),
				    new SynType(stepModel._routePointType),
				    PriorityType.normal,
				    1,
				    r,
				    workMan,
				    "(超时自动)(" + stepModel._stepName + ")"
					    + processInstanceModel.getTitle(),
				    false, 0);
		    WorkflowTaskEngine.getInstance().closeProcessTaskInstance(
			    uc, taskInstanceModel.getProcessInstanceId(),
			    taskInstanceModel.getId());
		} catch (WorkflowException we) {
		    we.printStackTrace(System.err);
		}
		return;
	    }
	    if (returnCode.equals("task break")) {
		System.err
			.println("节点超时策略执行警告：任务["
				+ taskInstanceModel.getTitle()
				+ "]所依赖的工作流节点被指定了RTClass,该类在执行时不允许结束此任务，系统无法自动完成，超时策略执行失败！");
		return;
	    }
	    if (returnCode.equals("task end"))
		return;
	    System.err.println("节点超时策略执行警告：任务[" + taskInstanceModel.getTitle()
		    + "]被执行时返回的状态码无法识别：" + returnCode);
	    return;
	} catch (Exception we) {
	    we.printStackTrace(System.err);
	}
    }

    private void executeTaskOuterMail(TaskInstanceModel taskInstanceModel,
	    WorkFlowStepCostModel costModel) {
	String[] emailTemplete = null;
	String mailNo = costModel._mailNo;
	try {
	    if (mailNo == null || mailNo.trim().length() == 0) {
		emailTemplete = IMAPI.getInstance()
			.getMailDefaultTemplateByGroupName("任务超时提醒");
	    } else {
		emailTemplete = IMAPI.getInstance().getMailTemplate(mailNo);
	    }
	} catch (AWSSDKException e) {
	    e.printStackTrace(System.err);
	}
	try {
	    emailTemplete = (emailTemplete == null) ? IMAPI.getInstance()
		    .getMailDefaultTemplateByGroupName("任务超时提醒")
		    : emailTemplete;
	} catch (AWSSDKException e2) {
	    e2.printStackTrace(System.err);
	}
	if (emailTemplete == null) {
	    System.err.println("不存在邮件模板[" + costModel._mailNo + "]发送超时提醒失败");
	} else {
	    UserContext uc = null;
	    try {
		uc = new UserContext(taskInstanceModel.getTarget());
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	    UserContext sender = null;
	    try {
		sender = new UserContext(RuleAPI.getInstance()
			.executeRuleScript(emailTemplete[0], uc,
				taskInstanceModel.getProcessInstanceId(),
				taskInstanceModel.getId()));
	    } catch (Exception e) {
		try {
		    sender = new UserContext("admin");
		} catch (Exception localException1) {
		}
		e.printStackTrace(System.err);
	    }
	    String mailTo = RuleAPI.getInstance().executeRuleScript(
		    costModel._mailTo, uc,
		    taskInstanceModel.getProcessInstanceId(),
		    taskInstanceModel.getId());
	    mailTo = mailTo.replaceAll("%CURRENT_CONTEXT%",
		    taskInstanceModel.getTarget());
	    Hashtable<?, ?> innerAccountTo = new UnsyncHashtable<Object, Object>();
	    Hashtable<?, ?> wwwAccountTo = new UnsyncHashtable<Object, Object>();
	    AWSMailUtil.getInstance().getAddressList(
		    sender.getCompanyModel().getId(), innerAccountTo,
		    wwwAccountTo, mailTo, "TO");
	    mailTo = getOuterMailAddress(innerAccountTo, wwwAccountTo);
	    Hashtable<String, String> param = new UnsyncHashtable<String, String>();
	    param.put("%CURRENT_CONTEXT%", taskInstanceModel.getTarget());
	    try {
		IMAPI.getInstance().sendMailByModel(emailTemplete[4],
			taskInstanceModel.getTarget(), mailTo,
			taskInstanceModel.getProcessInstanceId(),
			taskInstanceModel.getId(), param);
	    } catch (AWSSDKException e) {
		e.printStackTrace(System.err);
	    }
	}
    }

    private String getOuterMailAddress(Hashtable<?, ?> innerAccount,
	    Hashtable<?, ?> wwwAccount) {
	StringBuilder addressList = new StringBuilder();
	for (int i = 0; i < innerAccount.size(); ++i) {
	    String uid = (String) innerAccount.get(new Integer(i));
	    if (uid != null && uid.trim().length() > 0) {
		UserModel userModel = (UserModel) UserCache.getModel(uid);
		if (userModel != null && userModel.getEmail() != null
			&& userModel.getEmail().trim().length() > 0)
		    addressList.append(userModel.getEmail()).append(" ");
		else {
		    addressList.append(uid).append(" ");
		}
	    }
	}
	for (int i = 0; i < wwwAccount.size(); ++i) {
	    String email = (String) wwwAccount.get(new Integer(i));
	    if (email != null && email.indexOf(":") != -1) {
		String[] wwwEMail = email.split(":");
		addressList.append(wwwEMail[1]).append(" ");
	    }
	}
	return addressList.toString().trim();
    }

    private void executeTaskInnerMail(TaskInstanceModel taskInstanceModel,
	    WorkFlowStepCostModel costModel) {
	String[] emailTemplete = null;
	String mailNo = costModel._mailNo;
	try {
	    if (mailNo == null || mailNo.trim().length() == 0) {
		emailTemplete = IMAPI.getInstance()
			.getMailDefaultTemplateByGroupName("任务超时提醒");
	    } else
		emailTemplete = IMAPI.getInstance().getMailTemplate(mailNo);
	} catch (AWSSDKException e) {
	    e.printStackTrace(System.err);
	}
	try {
	    emailTemplete = (emailTemplete == null) ? IMAPI.getInstance()
		    .getMailDefaultTemplateByGroupName("任务超时提醒")
		    : emailTemplete;
	} catch (AWSSDKException e2) {
	    e2.printStackTrace(System.err);
	}
	if (emailTemplete == null) {
	    System.err.println("不存在邮件模板[" + costModel._mailNo + "]发送超时提醒失败");
	} else {
	    UserContext uc = null;
	    try {
		uc = new UserContext(taskInstanceModel.getTarget());
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	    String mailTo = RuleAPI.getInstance().executeRuleScript(
		    costModel._mailTo, uc,
		    taskInstanceModel.getProcessInstanceId(),
		    taskInstanceModel.getId());
	    mailTo = mailTo.replaceAll("%CURRENT_CONTEXT%",
		    taskInstanceModel.getTarget());
	    Hashtable<String, String> param = new UnsyncHashtable<String, String>();
	    param.put("%CURRENT_CONTEXT%", taskInstanceModel.getTarget());
	    try {
		IMAPI.getInstance().sendMailByModel(emailTemplete[4],
			taskInstanceModel.getTarget(), mailTo,
			taskInstanceModel.getProcessInstanceId(),
			taskInstanceModel.getId(), param);
	    } catch (AWSSDKException e) {
		e.printStackTrace(System.err);
	    }
	}
    }
}