package com.actionsoft.awf.workflow.execute.worklist.web;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import com.actionsoft.application.logging.AuditLogger;
import com.actionsoft.application.logging.model.Action;
import com.actionsoft.application.logging.model.AuditModel;
import com.actionsoft.application.logging.model.AuditObj;
import com.actionsoft.application.logging.model.Catalog;
import com.actionsoft.application.logging.model.Channel;
import com.actionsoft.application.logging.model.Level;
import com.actionsoft.application.server.LICENSE;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.AppError;
import com.actionsoft.apps.portal.mobile.config.MobileConfig;
import com.actionsoft.awf.commons.security.ac.util.AccessControlUtil;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.cache.CompanyCache;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.RoleCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.cache.UserMapCache;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.organization.model.UserMapModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSequence;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.EncryptFileUtil;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.SequenceException;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UpFile;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.util.UtilFile;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepOpinionCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowSubModel;
import com.actionsoft.awf.workflow.design.util.WFFlexDesignVersionUtil;
import com.actionsoft.awf.workflow.execute.PriorityType;
import com.actionsoft.awf.workflow.execute.SubWorkflowException;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.dao.TaskInstance;
import com.actionsoft.awf.workflow.execute.engine.SubWorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.WorkflowTaskEngine;
import com.actionsoft.awf.workflow.execute.engine.helper.ProcessInstanceUtil;
import com.actionsoft.awf.workflow.execute.engine.helper.SubProcessInstanceUtil;
import com.actionsoft.awf.workflow.execute.event.FormEventHandler;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.awf.workflow.execute.model.UserTaskAuditMenuModel;
import com.actionsoft.awf.workflow.execute.model.UserTaskHistoryOpinionModel;
import com.actionsoft.awf.workflow.execute.route.impl.RouteAbst;
import com.actionsoft.awf.workflow.execute.route.impl.RouteFactory;
import com.actionsoft.awf.workflow.history.archive.cache.ArchiveVolumeCache;
import com.actionsoft.awf.workflow.history.archive.dao.ArchiveDaoFactory;
import com.actionsoft.awf.workflow.history.archive.model.ArchiveRoomModel;
import com.actionsoft.awf.workflow.history.archive.model.ArchiveVolumeModel;
import com.actionsoft.awf.workflow.history.archive.model.ArchivesModel;
import com.actionsoft.coe.bpa.etl.collector.WorkFlowTaskEndCollectorImp;
import com.actionsoft.coe.bpa.etl.collector.WorkFlowTaskStartCollectorImp;
import com.actionsoft.coe.team.bpa.analysis.design.dao.AnalysisKPIScope;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.plugs.apps.reflect.MobilereflectUtil;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.letv.rtx.UpdateWorkMsgThread;

import net.sf.json.JSONObject;

public class UserTaskExecuteWeb {
    public String getExecDialog(UserContext me, int processInstanceId,
	    int taskInstanceId) {
	return getExecDialog(me, processInstanceId, taskInstanceId, "");
    }

    public String getExecDialog(UserContext me, int processInstanceId,
	    int taskInstanceId, String from) {
	int roleId = me.getRoleModel().getId();
	if (from.indexOf("|") > -1) {
	    String r = from.substring(from.indexOf("|") + 1);
	    roleId = Integer.parseInt(r);
	    from = from.substring(0, from.indexOf("|"));
	}
	ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(processInstanceId);
	if (processInstanceModel == null) {
	    return sendMessageWarnAlert(me,
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!",
		    "parent.window.close();");
	}
	TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		.createTaskInstance().getInstanceOfActive(taskInstanceId);
	if (taskInstanceModel == null && processInstanceModel.isStart()) {
	    return sendMessageWarnAlert(me, "<I18N#该任务已经结束>",
		    "parent.window.close();");
	}
	if (SubWorkflowEngine.getInstance().isWaitSplitSubProcess(
		processInstanceId, taskInstanceModel.getActivityDefinitionId())) {
	    return sendMessageWarnAlert(me, "<I18N#对不起，您需要等待相关子流程结束后再执行>",
		    "parent.window.close();");
	}
	if (taskInstanceId > 0) {
	    UserTaskAuditMenuModel auditModel = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory
		    .createUserTaskAuditMenu()
		    .getInstanceOfTask(taskInstanceId);
	    if (auditModel == null) {
		Hashtable opinionList = WorkFlowStepOpinionCache
			.getListOfWorkFlowOpinion(taskInstanceModel
				.getActivityDefinitionId());
		if (opinionList.size() > 0) {
		    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
			    .getModelOfStepNo(processInstanceModel
				    .getProcessDefinitionId(),
				    processInstanceModel
					    .getActivityDefinitionNo());
		    if (workFlowStepModel._isAudit) {
			return sendMessageWarnAlert(
				me,
				"<I18N#可能由于服务器忙，您的审核意见未保存成功，请关闭窗口，在您的待办事宜中重新打开该任务进行办理>!",
				"parent.window.close();");
		    }
		    auditModel = new UserTaskAuditMenuModel();
		    auditModel.setAuditType(-99);
		} else {
		    auditModel = new UserTaskAuditMenuModel();
		    auditModel.setAuditType(-99);
		}

	    }
	    WorkFlowStepModel workFlowStepModeltmp = WorkFlowStepCache
		    .getModelOfStepNo(
			    processInstanceModel.getProcessDefinitionId(),
			    processInstanceModel.getActivityDefinitionNo());

	    if (Boolean.parseBoolean(AWFConfig._awfServerConf
		    .getChoosePositionCompleteTask())
		    && workFlowStepModeltmp != null
		    && workFlowStepModeltmp._stepNo != 1
		    && from.equals("")
		    && UserMapCache.getMapListOfUser(me.getID()).size() > 0) {
		return getExecDialogIdentityConfirm(me, processInstanceId,
			taskInstanceId);
	    }
	    if (auditModel != null) {
		if (auditModel.getAuditType() == -2
			|| auditModel.getAuditType() == -4)
		    return executeSystemAuditAction(me, processInstanceId,
			    taskInstanceId, auditModel, from);
		if (auditModel.getAuditType() == -3) {
		    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
			    .getModelOfStepNo(processInstanceModel
				    .getProcessDefinitionId(),
				    processInstanceModel
					    .getActivityDefinitionNo());
		    if (workFlowStepModel == null
			    || workFlowStepModel._routePointType != 1) {
			WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache
				.getModel(processInstanceModel
					.getProcessDefinitionId());
			try {
			    WorkflowTaskEngine.getInstance()
				    .appendOpinionHistory(me,
					    processInstanceId, taskInstanceId,
					    auditModel);
			} catch (WorkflowException we) {
			    we.printStackTrace(System.err);
			}
			ProcessInstanceUtil hmc = new ProcessInstanceUtil(
				processInstanceId);
			hmc.flowStepEndCall(me, taskInstanceModel.getId(),
				"通知:" + processInstanceModel.getTitle(),
				workFlowStepModel);

			if (workFlowModel._archivesId <= 0) {
			    int year = 0 - Integer.parseInt(UtilDate
				    .yearFormat(new Date()));
			    return toArchives(me, true, processInstanceId,
				    year, taskInstanceId);
			}
			return toArchives(me, true, processInstanceId,
				workFlowModel._archivesId, taskInstanceId);
		    }
		} else if (auditModel.getAuditType() != 0
			&& auditModel.getAuditType() != -99) {
		    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
			    .getModelOfStepNo(processInstanceModel
				    .getProcessDefinitionId(),
				    processInstanceModel
					    .getActivityDefinitionNo());
		    if (workFlowStepModel != null
			    && workFlowStepModel._routePointType == 1) {
			double jobPercent = workFlowStepModel._jobPercent;
			if (jobPercent == 0.01D) {
			    DBSql.executeUpdate("DELETE FROM wf_messagepoint WHERE PARENT_ID="
				    + Integer.toString(processInstanceId)
				    + " AND wfs_ID="
				    + Integer.toString(workFlowStepModel._id)
				    + " and target!='" + me.getUID() + "'");
			    DBSql.executeUpdate("delete from wf_task where BIND_ID="
				    + processInstanceId
				    + " AND ID!="
				    + taskInstanceId + " and status=1 ");
			    return executeSystemAuditAction(me,
				    processInstanceId, taskInstanceId,
				    auditModel, from);
			}
			int count = DBSql
				.getInt("select count(*) as c from wf_task where bind_id="
					+ processInstanceId
					+ " and (STATUS=1 or status=4) and id<>"
					+ taskInstanceId, "c");
			if (count == 0) {
			    return executeSystemAuditAction(me,
				    processInstanceId, taskInstanceId,
				    auditModel, from);
			}
		    } else {
			return executeSystemAuditAction(me, processInstanceId,
				taskInstanceId, auditModel, from);
		    }
		}
	    }
	}
	setDeptInfoByIdentityConfirm(taskInstanceId, from, roleId);
	try {
	    String assignCode = WorkflowTaskEngine.getInstance()
		    .assignComplexProcessTaskInstance(me, processInstanceId,
			    taskInstanceId);
	    if (assignCode.equals("processEnd")) {
		int jumpStepNo = WorkflowTaskEngine.getInstance()
			.getJumpStepNoOfRules(me, processInstanceId,
				taskInstanceId);
		if (jumpStepNo > 0) {
		    return hashNextActivityPage(me, processInstanceId,
			    taskInstanceId, from);
		}
		WorkFlowModel workFlowModel = (WorkFlowModel) WorkFlowCache
			.getModel(processInstanceModel.getProcessDefinitionId());
		if (workFlowModel._isAutoArchives) {
		    ProcessInstanceUtil hmc = new ProcessInstanceUtil(
			    processInstanceId);
		    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
			    .getModelOfStepNo(processInstanceModel
				    .getProcessDefinitionId(),
				    processInstanceModel
					    .getActivityDefinitionNo());
		    hmc.flowStepEndCall(me, taskInstanceModel.getId(), "通知:"
			    + processInstanceModel.getTitle(),
			    workFlowStepModel);

		    if (workFlowModel._archivesId <= 0) {
			return toArchives(me, processInstanceId,
				-Integer.parseInt(UtilDate
					.yearFormat(new Timestamp(System
						.currentTimeMillis()))),
				taskInstanceId);
		    }
		    return toArchives(me, processInstanceId,
			    workFlowModel._archivesId, taskInstanceId);
		}
		return getArchivesList(me, processInstanceId, taskInstanceId,
			"", 0);
	    }
	    if (assignCode.equals("assign"))
		return hashNextActivityPage(me, processInstanceId,
			taskInstanceId, from);
	    if (assignCode.equals("taskEnd")) {
		if (LICENSE.isBPA()) {
		    boolean isSafSupport = false;
		    try {
			WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache
				.getModel(processInstanceModel
					.getProcessDefinitionId());
			isSafSupport = new AnalysisKPIScope()
				.isSafSupport(flowModel._uuid);
		    } catch (Exception localException) {
			localException.printStackTrace(System.err);
		    }
		    if (isSafSupport) {
			return alertMessageAndSatisfation(me, "<I18N#恭喜>", "",
				"<I18N#当前任务已执行完毕>!");
		    }
		}
		return alertMessage(me, "<I18N#恭喜>", "", "<I18N#当前任务已执行完毕>!");
	    }
	    return "<I18N#引擎说是一个未知的assign状态>=" + assignCode;
	} catch (WorkflowException we) {
	    we.printStackTrace(System.err);
	    return alertMessage(me, "", "", we.getMessage());
	}
    }

    private void setDeptInfoByIdentityConfirm(int taskInstanceId, String from,
	    int roleId) {
	if (!from.equals("")) {
	    String[] ids = from.split("//");
	    if (ids.length == 2) {
		DepartmentModel localDepartmentModel = null;
		try {
		    localDepartmentModel = (DepartmentModel) DepartmentCache
			    .getModel(Integer.parseInt(ids[1]));
		} catch (NumberFormatException localNumberFormatException) {
		    localNumberFormatException.printStackTrace(System.err);
		}
		if (localDepartmentModel != null)
		    DBSql.executeUpdate("update wf_task set dptid="
			    + localDepartmentModel.getId() + ",roleid="
			    + roleId + " where id=" + taskInstanceId);
	    }
	}
    }

    public String getExecDialogIdentityConfirm(UserContext me,
	    int processInstanceId, int taskInstanceId) {
	String _nextButton = "<input type=button value='<I18N#下一步>' "
		+ (me.isMobileClient() ? " data-inline='true'" : "")
		+ " class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Worklist_BindReport_P_Save_Next_Identity_Confirm_OK');return false;\" name='nx'  border='0'>";

	String sid = "<input type=hidden name=sid value=" + me.getSessionId()
		+ ">\n";

	StringBuilder from = new StringBuilder();
	from.append("<img src='../aws_img/nondynamic.gif' align='absmiddle'><I18N#注意：您有兼任职位><br>");
	from.append("<select id='from' name='from' ")
		.append(me.isMobileClient() ? ""
			: " class ='actionsoftSelect' style='width:80%'")
		.append(">");

	String userMasterLocalName = me.getCompanyModel().getCompanyName()
		+ "//" + me.getDepartmentModel().getDepartmentFullNameOfCache();
	String userMasterLocal = me.getCompanyModel().getId() + "//"
		+ me.getDepartmentModel().getId();
	Hashtable mapList = UserMapCache.getMapListOfUser(me.getID());
	from.append("<option value='")
		.append(userMasterLocal)
		.append("'>")
		.append(userMasterLocalName)
		.append("(" + me.getRoleModel().getGroupName() + "/"
			+ me.getRoleModel().getRoleName() + ")")
		.append("</option>\n");
	if (mapList.size() > 0) {
	    for (int i = 0; i < mapList.size(); ++i) {
		UserMapModel mapModel = (UserMapModel) mapList.get(new Integer(
			i));

		RoleModel roleModel = (RoleModel) RoleCache.getModel(mapModel
			.getRoleId());

		DepartmentModel departmentModel = (DepartmentModel) DepartmentCache
			.getModel(mapModel.getDepartmentId());

		CompanyModel companyModel = (CompanyModel) CompanyCache
			.getModel(departmentModel.getCompanyId());
		String userMapLocal = companyModel.getId() + "//"
			+ departmentModel.getId();
		String userMapLocalName = companyModel.getCompanyName() + "//"
			+ DepartmentCache.getFullName(departmentModel.getId());
		userMapLocalName = userMapLocalName + "("
			+ roleModel.getGroupName() + "/"
			+ roleModel.getRoleName() + ")";
		from.append("<option value='").append(userMapLocal)
			.append("'><I18N#兼>:").append(userMapLocalName)
			.append("</option>\n");
	    }
	    from.append("</select>");
	} else {
	    from.append("</select>");
	}

	Hashtable hashTags = new UnsyncHashtable();

	if (me.isMobileClient()) {
	    String js = "";
	    js = js + "<meta charset=\"utf-8\" />\n";
	    js = js
		    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n";
	    js = js
		    + "<link rel=\"stylesheet\" href=\"../aws_js/jquery/jqm1.3.0/jquery.mobile-1.3.0.min.css\" />\n";
	    js = js
		    + "<script src=\"../aws_js/jquery/1.7.1/jquery-1.7.1.min.js\"></script>\n";
	    js = js
		    + "<script src=\"../aws_js/jquery/jqm1.3.0/jquery.mobile-1.3.0.min.js\"></script>\n";
	    js = js
		    + "<link rel=\"stylesheet\" href=\"../app/com.actionsoft.apps.portal.mobile/css/mobile.css\" />\n";
	    hashTags.put("doctype", "<!DOCTYPE html>\n");
	    hashTags.put("mobilejs", js);
	    hashTags.put("pccss", "");
	    hashTags.put("pctoolbarcss", "");
	    hashTags.put("table1", "");
	    hashTags.put("table2", "");
	    try {
		MobilereflectUtil.setCommonTags(me, hashTags);
	    } catch (Exception localException) {
		localException.printStackTrace(System.err);
	    }
	} else {
	    hashTags.put("doctype", "");
	    hashTags.put("mobilejs", "");
	    hashTags.put("pccss", " class='actionsoftTitle'");
	    hashTags.put("pctoolbarcss", " class='actionsoftToolBar'");
	    hashTags.put("table1",
		    "<table width='80%' border='0' align='center'><tr><td>");
	    hashTags.put("table2", "</td></tr></table>");
	}

	hashTags.put("button2", _nextButton);
	hashTags.put("id", Integer.valueOf(processInstanceId));
	hashTags.put("task_id", Integer.valueOf(taskInstanceId));
	hashTags.put("from", from);
	hashTags.put("sid", sid);
	return RepleaseKey.replace(HtmlModelFactory
		.getHtmlModel("wf_messageSend1_IdentityConfirm.htm"), hashTags);
    }

    private String executeSystemAuditAction(UserContext me,
	    int processInstanceId, int taskInstanceId,
	    UserTaskAuditMenuModel auditModel, String from) {
	if (auditModel.getAuditType() < 0 && auditModel.getAuditType() != -6) {
	    WorkflowTaskEngine.getInstance().executeAuditMenuAction(me,
		    processInstanceId, taskInstanceId, auditModel);
	    if (auditModel.getAuditType() == -2 || auditModel.getAuditType() == -4) {
		String msgId = DBSql.getString("select MSGID from WF_MESSAGE_INTERFACE where ID = " + taskInstanceId, "MSGID");
		if (msgId.length() > 0) {
		    HttpClient client = new HttpClient();
		    MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		    connectionManager.getParams().setConnectionTimeout(3000);
		    connectionManager.getParams().setSoTimeout(3000);
		    client.setHttpConnectionManager(connectionManager);
		    JSONObject content = new JSONObject();
		    content.put("pcurl",
			    AWFConfig._awfServerConf.getPortalHost()
				    + "/services/rs/sso/execute?cmd=WorkFlow_Execute_Worklist_File_Open&id="
				    + processInstanceId + "&task_id="
				    + taskInstanceId + "&openstate=4");
		    content.put("status", "todo");
		    content.put("msgId", msgId);
		    content.put("state", 4);
		    new UpdateWorkMsgThread(client, content, msgId, "").start();
		}
	    }
	    return alertMessage(me, "<I18N#恭喜>", "<I18N#已经发出>！",
		    "<I18N#任务已经发送给办理者>！");
	}
	return hashNextActivityPage(me, processInstanceId, taskInstanceId, from);
    }

    public String getModifySuccessfullyPage(UserContext me,
	    int processInstanceId, int processTaskInstanceId) {
	ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(processInstanceId);
	if (processInstanceModel == null) {
	    return sendMessageWarnAlert(me,
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!",
		    "window.close();");
	}
	TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		.createTaskInstance()
		.getInstanceOfActive(processTaskInstanceId);
	if (taskInstanceModel == null && processInstanceModel.isStart()) {
	    return sendMessageWarnAlert(me, "<I18N#该任务已经结束>!",
		    "window.close();");
	}
	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(
			processInstanceModel.getProcessDefinitionId(), 1);
	if (workFlowStepModel == null) {
	    return sendMessageWarnAlert(me, "<I18N#未发现节点模型>!",
		    "window.close();");
	}
	boolean isOk = FormEventHandler.handleEvent(me, "TRANSACTION_VALIDATE",
		processInstanceId, processTaskInstanceId,
		processInstanceModel.getProcessDefinitionId(),
		workFlowStepModel._id, 0, 0, null, "");
	if (isOk) {
	    try {
		int taskid = DBSql.getInt(
			"select from_point from wf_task where id="
				+ processTaskInstanceId, "from_point");
		WorkflowTaskEngine.getInstance().modifyTaskSuccessfully(
			processTaskInstanceId);
		if (taskid > 0) {
		    DBSql.executeUpdate("update wf_task set readtime = sysdate where id = "
			    + taskid);
		    TaskInstanceModel taskinstancemodel = ProcessRuntimeDaoFactory
			    .createTaskInstance().getInstanceOfActive(taskid);
		    WorkFlowStepModel workflowstepmodel = (WorkFlowStepModel) WorkFlowStepCache
			    .getModel(taskinstancemodel
				    .getActivityDefinitionId());
		    if (workflowstepmodel._emailAlertType > 0) {
			TaskInstance.taskNotifyEMAIL(me,
				taskinstancemodel.getTarget(),
				taskinstancemodel.getTitle(), taskid,
				workflowstepmodel._emailAlertType);
		    }
		    String msgId = DBSql.getString("select MSGID from WF_MESSAGE_INTERFACE where ID = " + taskid, "MSGID");
		    if (msgId.length() > 0) {
			HttpClient client = new HttpClient();
			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			connectionManager.getParams().setConnectionTimeout(3000);
			connectionManager.getParams().setSoTimeout(3000);
			client.setHttpConnectionManager(connectionManager);
			JSONObject content = new JSONObject();
			content.put(
				"pcurl",
				AWFConfig._awfServerConf.getPortalHost()
					+ "/services/rs/sso/execute?cmd=WorkFlow_Execute_Worklist_File_Open&id="
					+ processInstanceId + "&task_id="
					+ taskid + "&openstate=1");
			content.put("status", "todo");
			content.put("msgId", msgId);
			content.put("state", 1);
			content.put("isread", 0);
			new UpdateWorkMsgThread(client, content, msgId, "").start();
		    }
		}
		String msgId = DBSql.getString(
			"select MSGID from WF_MESSAGE_INTERFACE where ID = "
				+ processTaskInstanceId, "MSGID");
		if (msgId.length() > 0) {
		    HttpClient client = new HttpClient();
		    MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		    connectionManager.getParams().setConnectionTimeout(3000);
		    connectionManager.getParams().setSoTimeout(3000);
		    client.setHttpConnectionManager(connectionManager);
		    JSONObject content = new JSONObject();
		    content.put("status", "delete");
		    content.put("msgId", msgId);
		    new UpdateWorkMsgThread(client, content, msgId, "D").start();
		}
		return alertMessage(me, "<I18N#恭喜>", "<I18N#信息已经发出>！",
			"<I18N#已经通知待办人修改完毕>");
	    } catch (WorkflowException we) {
		we.printStackTrace(System.err);
		return alertMessage(me, "", "", we.getMessage());
	    }
	}
	StringBuilder err = new StringBuilder();
	String alt = MessageQueue.getInstance().getMessage(me.getUID());
	alt = new UtilString(alt).replace("\\n", "<br>");
	err.append("<html><head><title><I18N#提示></title><meta http-equiv='' content='text/html; charset=utf-8'/><link media=all href='../aws_css/public.css' type='text/css' rel='Stylesheet'/><script src='../aws_js/effect.js' type='text/javascript'></script></head><body>");
	err.append("<br><br><br><table bgcolor=eeeeee width=70% align=center border=0><tr><td ><img src=../aws_img/wait.gif><I18N#提醒:由于未通过有效性校验,您还不能提交该任务>!<br><br></td></tr>");
	if (alt.length() > 2) {
	    err.append("<tr><td align=left style='font-size:14px;'><hr><l><b><I18N#参考信息>:</b><br>"
		    + alt + "<br></l></td></tr>");
	}
	err.append("<tr><td align=right><I18N#接下来您可以>：<a href='' onclick='window.history.go(-1);return false;'><I18N#返回到上一页面></a>&nbsp;&nbsp;<a href='' onclick='parent.window.close();return false;'><I18N#关闭窗口></a><hr></td></tr>");
	err.append("</table>");
	err.append("</body>");
	return err.toString();
    }

    public String hashNextActivityPage(UserContext me, int processInstanceId,
	    int taskInstanceId, String from) {
	StringBuilder html = new StringBuilder("");
	String button1 = "<input type=button value='<I18N#放 弃>'  class ='actionsoftButton' onClick='history.go(-1)'   border='0'>";
	String button2 = "<input type=button value='<I18N#下一步>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Worklist_Transaction_SpecifyUser');return false;\"   border='0'>";
	String sid = "<input type=hidden name=sid value=" + me.getSessionId()
		+ ">\n";
	ProcessInstanceModel instanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(processInstanceId);
	if (instanceModel == null) {
	    return sendMessageWarnAlert(me,
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!",
		    "parent.window.close();");
	}
	if (ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfActive(
		taskInstanceId) == null
		&& instanceModel.isStart()) {
	    return sendMessageWarnAlert(me, "<I18N#该任务已经结束>!",
		    "parent.window.close();");
	}
	int jumpStepNo = WorkflowTaskEngine.getInstance().getJumpStepNoOfRules(
		me, processInstanceId, taskInstanceId);
	int hashNextNo = instanceModel.getActivityDefinitionNo();
	if (jumpStepNo == -1 || jumpStepNo == 9999) {
	    UserTaskAuditMenuModel auditModel = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory
		    .createUserTaskAuditMenu()
		    .getInstanceOfTask(taskInstanceId);
	    if (auditModel == null) {
		auditModel = new UserTaskAuditMenuModel();
		auditModel.setAuditType(-99);
	    }
	    try {
		WorkflowTaskEngine.getInstance().appendOpinionHistory(me,
			processInstanceId, taskInstanceId, auditModel);
	    } catch (WorkflowException we) {
		we.printStackTrace(System.err);
		return alertMessage(me, "", "", we.getMessage());
	    }
	    WorkFlowModel workflowModel = (WorkFlowModel) WorkFlowCache
		    .getModel(instanceModel.getProcessDefinitionId());
	    if (workflowModel._isAutoArchives) {
		if (workflowModel._archivesId <= 0) {
		    return toArchives(me, processInstanceId,
			    -Integer.parseInt(UtilDate
				    .yearFormat(new Timestamp(System
					    .currentTimeMillis()))),
			    taskInstanceId);
		}

		return toArchives(me, processInstanceId,
			workflowModel._archivesId, taskInstanceId);
	    }
	    return getArchivesList(me, processInstanceId, taskInstanceId, "", 0);
	}
	hashNextNo = jumpStepNo;
	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(instanceModel.getProcessDefinitionId(),
			hashNextNo);
	if (workFlowStepModel != null && workFlowStepModel._isJumpStep) {
	    if (me.isMobileClient()) {
		html.append("<div data-role=\"fieldcontain\"><select name='select_workflowstep'  data-mini=\"true\">");
		html.append("<option value=0><I18N#请选择跳转到></option>");
		Map h = WorkFlowStepCache.getListOfWorkFlow(instanceModel
			.getProcessDefinitionId());
		if (h != null) {
		    for (int i = 0; i < h.size(); ++i) {
			WorkFlowStepModel stepModel = (WorkFlowStepModel) h
				.get(new Integer(i));
			if (hashNextNo != stepModel._stepNo) {
			    html.append("<option value=")
				    .append(stepModel._stepNo).append(" >")
				    .append(stepModel._stepNo)
				    .append(".<I18N#")
				    .append(stepModel._stepName)
				    .append("></option>");
			}
		    }
		    html.append("</select></div>");
		}
	    } else {
		html.append("<select name='select_workflowstep'  class ='actionsoftSelect' >");
		html.append("<option value=0><I18N#请选择跳转到></option>");
		Map h = WorkFlowStepCache.getListOfWorkFlow(instanceModel
			.getProcessDefinitionId());
		if (h != null) {
		    for (int i = 0; i < h.size(); ++i) {
			WorkFlowStepModel stepModel = (WorkFlowStepModel) h
				.get(new Integer(i));
			if (hashNextNo != stepModel._stepNo) {
			    html.append("<option value=")
				    .append(stepModel._stepNo).append(" >")
				    .append(stepModel._stepNo)
				    .append(".<I18N#")
				    .append(stepModel._stepName)
				    .append("></option>");
			}
		    }
		    html.append("</select>");
		}
	    }
	} else {
	    String page = getParticipantPage(me, processInstanceId, hashNextNo,
		    taskInstanceId, from);
	    return page.replaceAll("<#userName>", me.getUserModel()
		    .getUserName());
	}
	Hashtable hashTags = new UnsyncHashtable(9);
	hashTags.put("button1", button1);
	hashTags.put("button2", button2);
	hashTags.put("text1", html.toString());
	hashTags.put("sid", sid);
	hashTags.put("stepname", "<span><I18N#" + workFlowStepModel._stepName
		+ "></span>");
	hashTags.put("flowDiagram", "");
	hashTags.put("stepno", Integer.toString(hashNextNo));
	hashTags.put("id", Integer.toString(processInstanceId));
	hashTags.put("task_id", Integer.toString(taskInstanceId));
	hashTags.put("userName", me.getUserModel().getUserName());
	if (me.isMobileClient()) {
	    try {
		MobilereflectUtil.setCommonTags(me, hashTags);
		return RepleaseKey
			.replace(
				HtmlModelFactory
					.getHtmlModel("com.actionsoft.apps.portal.mobile_jumpExec.htm"),
				hashTags);
	    } catch (Exception e) {
		e.printStackTrace(System.err);
		return AlertWindow.getInfoWindow("平台未部署手机流程门户套件");
	    }
	}
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("wf_messageSend1.htm"), hashTags);
    }

    public String getParticipantPage(UserContext me, int processInstanceId,
	    int stepNo, int taskInstanceId) {
	return getParticipantPage(me, processInstanceId, stepNo,
		taskInstanceId, "");
    }

    public String getParticipantPage(UserContext me, int processInstanceId,
	    int stepNo, int taskInstanceId, String from) {
	String sid = "<input type=hidden name=sid value=" + me.getSessionId()
		+ ">\n";
	ProcessInstanceModel instanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(processInstanceId);
	if (instanceModel == null) {
	    return sendMessageWarnAlert(me,
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!",
		    "parent.window.close();");
	}
	if (ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfActive(
		taskInstanceId) == null
		&& instanceModel.isStart()) {
	    return sendMessageWarnAlert(me, "<I18N#该任务已经结束>!",
		    "parent.window.close();");
	}

	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(instanceModel.getProcessDefinitionId(),
			stepNo);

	DepartmentModel localDepartmentModel = me.getDepartmentModel();
	int ownerDepartmentId = localDepartmentModel.getId();
	String nextTaskMan = "";
	if (taskInstanceId > 0) {
	    TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		    .createTaskInstance().getInstanceOfActive(taskInstanceId);
	    if (taskInstanceModel != null) {
		if (workFlowStepModel._isHistoryRoute) {
		    nextTaskMan = WorkflowTaskEngine.getInstance()
			    .getParticipantsHistoryList(processInstanceId,
				    workFlowStepModel._flowId,
				    workFlowStepModel._id);
		}
		ownerDepartmentId = taskInstanceModel.getOwnerDepartmentId();
		if (ownerDepartmentId > 0) {
		    if (UserCache.isExistInDepartment(
			    taskInstanceModel.getOwnerDepartmentId(),
			    me.getID())) {
			localDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(taskInstanceModel
					.getOwnerDepartmentId());
		    } else {
			DepartmentModel tmpDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(taskInstanceModel
					.getOwnerDepartmentId());

			if (tmpDepartmentModel != null
				&& UserCache.isExistInDepartment(
					tmpDepartmentModel
						.getParentDepartmentId(), me
						.getID())) {
			    localDepartmentModel = (DepartmentModel) DepartmentCache
				    .getModel(tmpDepartmentModel
					    .getParentDepartmentId());
			}
		    }
		    if (localDepartmentModel == null)
			localDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(((UserModel) UserCache
					.getModel(taskInstanceModel.getOwner()))
					.getDepartmentId());
		} else {
		    ownerDepartmentId = localDepartmentModel.getId();
		}
	    }
	}
	if (!from.equals("")) {
	    String[] ids = from.split("//");
	    if (ids.length == 2) {
		try {
		    localDepartmentModel = (DepartmentModel) DepartmentCache
			    .getModel(Integer.parseInt(ids[1]));
		    if (localDepartmentModel != null) {
			DBSql.executeUpdate("update wf_task set dptid="
				+ localDepartmentModel.getId() + " where id="
				+ taskInstanceId);
		    }
		} catch (NumberFormatException localNumberFormatException) {
		    localNumberFormatException.printStackTrace(System.err);
		}
	    }
	}
	if (workFlowStepModel._isHistoryRoute && nextTaskMan != null
		&& !nextTaskMan.equals("") || !instanceModel.isStart()) {
	    String targetUser = nextTaskMan;
	    if (targetUser.trim().equals("")) {
		WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache
			.getModel(workFlowStepModel._flowId);
		if (flowModel._flowMaster.trim().length() > 0) {
		    alertMail(
			    me.getUID(),
			    flowModel._flowMaster,
			    "<I18N#流程向下执行时未找到办理者>",
			    "我在使用流程["
				    + flowModel._flowName
				    + "]时，在["
				    + workFlowStepModel._stepName
				    + "]节点执行‘办理’动作时，配置的路由方案没有找到人员，请您检查下流程路由方案的配置是否正常，谢谢!");
		}
		return alertMessage2(me, "<I18N#不能送达下个节点>", "",
			"<I18N#请通知系统管理员配置该流程>");
	    }
	    String mailTo = "<table><tr><td><input type='text' name='MAIL_TO'  class ='actionsoftInput' size='60'  value='"
		    + Function.getAddressName(targetUser)
		    + "'></td><td>"
		    + RouteAbst._addresssButton + "</td></tr></table>";
	    if (!instanceModel.isStart()) {
		mailTo = "<table><tr><td><input type='text' name='MAIL_TO'  class ='actionsoftInput' style='display:none' size='60'  value='"
			+ targetUser
			+ "'></td><td>"
			+ targetUser
			+ "</td></tr></table>";
	    }
	    String title = "<input type='text' name='TITLE'  class ='actionsoftInput' size='60' value='("
		    + workFlowStepModel._stepName
		    + ")"
		    + instanceModel.getTitle() + "'>";
	    String rb1 = "<input type=\"radio\"  name=\"rb1\" value=\"1\" checked ><I18N#无><input type=\"radio\" name=\"rb1\" value=\"3\"><I18N#高><input type=\"radio\" name=\"rb1\" value=2><I18N#中><input type=\"radio\" name=\"rb1\" value=\"0\"><I18N#低></td>";
	    String rb2 = "<div style='display=none'><input type=\"radio\"  name=\"rb2\" value=\"0\" "
		    + (workFlowStepModel._routePointType == 0 ? "checked" : "")
		    + " >串签<input type=\"radio\" name=\"rb2\" value=1 "
		    + (workFlowStepModel._routePointType == 1 ? "checked" : "")
		    + " >并签</div>";
	    rb2 = rb2
		    + (workFlowStepModel._routePointType == 1 ? "<font color=red><b><I18N#当多个人参与办理时,并签></b></font>"
			    : "<font color=red><b><I18N#当多个人参与办理时,串签></b></font>");

	    String isShortMessageCheck = "";
	    if (workFlowStepModel._isShortMessage
		    && AWFConfig._awfServerConf.getShortmessageServer()
			    .toLowerCase().equals("on"))
		isShortMessageCheck = "<input name=isShortMessageCheck type=radio value=0 ><I18N#不发送短信><input type=radio name=isShortMessageCheck value=1 checked><I18N#给当前办理人发送短信></td>";
	    else {
		isShortMessageCheck = "<input name=isShortMessageCheck type=radio value=0 checked><I18N#不发送短信><input type=radio name=isShortMessageCheck value=1><I18N#给当前办理人发送短信></td>";
	    }
	    if (!AWFConfig._awfServerConf.getShortmessageServer().toLowerCase()
		    .equals("on")) {
		isShortMessageCheck = "<div style='display:none'>"
			+ isShortMessageCheck + "</div>";
	    }
	    Hashtable hashTags = new UnsyncHashtable();
	    hashTags.put("button1", RouteAbst._cancelButton);
	    hashTags.put("button2", RouteAbst._preButton);
	    hashTags.put("button3", RouteAbst._sendButton);
	    hashTags.put("mailTo", mailTo);
	    hashTags.put("pageTitle", instanceModel.getTitle());
	    hashTags.put("title", title);
	    hashTags.put("rb1", rb1);
	    hashTags.put("rb2", rb2);
	    hashTags.put("task_id", Integer.toString(taskInstanceId));
	    hashTags.put("localDepartmentId",
		    Integer.toString(localDepartmentModel.getId()));
	    hashTags.put("sid", sid);
	    hashTags.put("sessionId", me.getSessionId());
	    hashTags.put("isShortMessageCheck", isShortMessageCheck);
	    hashTags.put("stepname", "<span><I18N#"
		    + workFlowStepModel._stepName + "></span>");
	    hashTags.put("stepLimit",
		    Integer.toString(workFlowStepModel._stepLimitLess));
	    hashTags.put("stepLimitMore",
		    Integer.toString(workFlowStepModel._stepLimitMore));
	    hashTags.put("stepno", Integer.toString(stepNo));
	    hashTags.put("id", Integer.toString(processInstanceId));
	    hashTags.put("userName", me.getUserModel().getUserName());
	    setCommonTags(me, hashTags);
	    return RepleaseKey
		    .replace(HtmlModelFactory
			    .getHtmlModel("wf_messageSend2Dynamic.htm"),
			    hashTags);
	}
	int nextStepNo = stepNo;
	if (stepNo <= instanceModel.getActivityDefinitionNo()) {
	    Object o = RouteFactory.getInstance(me, instanceModel,
		    localDepartmentModel, ownerDepartmentId,
		    workFlowStepModel._routeType);
	    if (o != null) {
		RouteAbst route = (RouteAbst) o;
		return route.getRoutePage(taskInstanceId, nextStepNo);
	    }
	    return "<I18N#非法的路由方案>";
	}
	RouteAbst route;
	while (true) {
	    Object o = RouteFactory.getInstance(me, instanceModel,
		    localDepartmentModel, ownerDepartmentId,
		    workFlowStepModel._routeType);
	    if (o != null) {
		route = (RouteAbst) o;
		String targetAddress = route.getTargetUserAddress(
			workFlowStepModel, taskInstanceId);
		if (workFlowStepModel._isIgnoreSame
			&& Function.getUID(targetAddress).trim()
				.equals(me.getUID())) {
		    ++nextStepNo;
		    if (WorkFlowStepCache.getModelOfStepNo(
			    instanceModel.getProcessDefinitionId(), nextStepNo) == null) {
			workFlowStepModel = WorkFlowStepCache.getModelOfStepNo(
				instanceModel.getProcessDefinitionId(),
				nextStepNo - 1);
			o = RouteFactory.getInstance(me, instanceModel,
				localDepartmentModel, ownerDepartmentId,
				workFlowStepModel._routeType);
			route = (RouteAbst) o;
			return route.getRoutePage(taskInstanceId,
				nextStepNo - 1);
		    }
		    workFlowStepModel = WorkFlowStepCache.getModelOfStepNo(
			    instanceModel.getProcessDefinitionId(), nextStepNo);
		} else {
		    return route.getRoutePage(taskInstanceId, nextStepNo);
		}
	    } else {
		return "<I18N#非法的路由方案>";
	    }
	}
    }

    public String pushNext(UserContext owner, int processInstanceId,
	    int runStyle, int priority, int status, int nextStepNo,
	    String participants, String title, int taskInstanceId,
	    boolean isShortMessage, int localDepartmentId) {
	StringBuffer inputHidden = new StringBuffer();
	inputHidden
		.append("<input type='hidden' name='processInstanceId' id='processInstanceId' value='"
			+ processInstanceId + "'>\n");
	inputHidden
		.append("<input type='hidden' name='taskInstanceId' id='taskInstanceId' value='"
			+ taskInstanceId + "'>\n");
	inputHidden
		.append("<input type='hidden' name='title' id='title' value='"
			+ title + "'>\n");
	inputHidden
		.append("<input type='hidden' name='openstate' id='openstate' value='"
			+ status + "'>\n");
	if (participants == null || participants.trim().length() == 0) {
	    inputHidden
		    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
	    return sendMessageWarnAlert(owner, "<I18N#必须指定下个任务的办理人>!"
		    + inputHidden.toString(), "history.go(-1);");
	}
	if (title == null) {
	    title = "";
	}
	StringBuilder space = new StringBuilder();
	Hashtable spaceList = new UnsyncHashtable();
	for (int ii = 0; ii < 11; ++ii) {
	    space.append(" ");
	    spaceList.put(new Integer(ii), space.toString());
	}
	for (int i = 10; i > 0; --i) {
	    if (participants.indexOf((String) spaceList.get(new Integer(i))) != -1) {
		participants = new UtilString(participants).replace(
			(String) spaceList.get(new Integer(i)), " ");
	    }
	}

	String success = "";
	participants = participants.trim();
	success = Function.checkAddress(participants.trim());
	if (!success.equals("ok") || participants.length() == 0) {
	    if (owner.isMobileClient()) {
		inputHidden
			.append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
		return taskMessageWarnAlert(
			owner,
			AppError.getError("ERROR-0700")
				+ inputHidden.toString());
	    }
	    inputHidden
		    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
	    return AppError.getErrorPage("ERROR-0700" + inputHidden.toString());
	}

	ProcessInstanceModel instanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(processInstanceId);
	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(instanceModel.getProcessDefinitionId(),
			nextStepNo);
	WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache
		.getModel(workFlowStepModel._flowId);
	if (instanceModel == null) {
	    inputHidden
		    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
	    return sendMessageWarnAlert(owner,
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!"
			    + inputHidden.toString(), "parent.window.close();");
	}
	if (workFlowStepModel == null) {
	    inputHidden
		    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
	    return sendMessageWarnAlert(owner, "<I18N#指定的下个节点模型不存在>!"
		    + inputHidden.toString(), "parent.window.close();");
	}
	TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		.createTaskInstance().getInstanceOfActive(taskInstanceId);
	if (taskInstanceModel == null) {
	    inputHidden
		    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
	    return sendMessageWarnAlert(owner,
		    "<I18N#该任务已经结束>!" + inputHidden.toString(),
		    "parent.window.close();");
	}

	if (flowModel._isSecurityLayer) {
	    UtilString myStr = new UtilString(participants);
	    Vector myArray = myStr.split(" ");
	    Map securityMap = new HashMap();
	    int flag = 0;
	    try {
		for (int i = 0; i < myArray.size(); ++i) {
		    String uid = myArray.elementAt(i).toString();
		    String tip = "";

		    if (uid.indexOf("<") > -1) {
			uid = uid.substring(0, uid.indexOf("<"));
		    }
		    if (!uid.equals("*")) {
			UserModel model = (UserModel) UserCache.getModel(uid);
			if (model != null) {
			    if (instanceModel.getSecurityLayer() == 1) {
				if (!SecurityProxy.checkDocumentLayerSecurity(
					model.getUID(),
					"AWFDocumentLayerUnit_机密")
					&& !SecurityProxy
						.checkDocumentLayerSecurity(
							model.getUID(),
							"AWFDocumentLayerUnit_秘密")) {
				    tip = "秘密";
				    flag = 1;
				}
			    } else if (instanceModel.getSecurityLayer() == 2
				    && !SecurityProxy
					    .checkDocumentLayerSecurity(
						    model.getUID(),
						    "AWFDocumentLayerUnit_机密")) {
				tip = "机密";
				flag = 2;
			    } else if (instanceModel.getSecurityLayer() == 3
				    && !SecurityProxy
					    .checkDocumentLayerSecurity(
						    model.getUID(),
						    "AWFDocumentLayerUnit_绝密")) {
				tip = "绝密";
				flag = 3;
			    }
			    if (!"".equals(tip)) {
				String[] userSecurity = { model.getUserName(),
					tip };
				if (securityMap.get(tip) == null) {
				    securityMap.put(tip, new ArrayList());
				}
				((List) securityMap.get(tip)).add(userSecurity);
			    }
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	    StringBuffer sbTip = new StringBuffer();
	    Iterator iterator = securityMap.keySet().iterator();
	    boolean hasValue = false;
	    while (iterator.hasNext()) {
		hasValue = true;
		String key = (String) iterator.next();
		List list = (List) securityMap.get(key);
		sbTip.append(I18nRes.findValue("用户") + "【");
		int count = list.size();
		for (int i = 0; i < count; ++i) {
		    String[] userSecurity = (String[]) list.get(i);
		    sbTip.append(I18nRes.findValue(userSecurity[0]));
		    if (i != count - 1) {
			sbTip.append(",");
		    }
		}
		sbTip.append("】").append(I18nRes.findValue("无处理")).append("【")
			.append(I18nRes.findValue(key)).append("】")
			.append(I18nRes.findValue("级别的权限")).append("，");
	    }
	    if (hasValue) {
		String tip = "";
		if (flag == 1)
		    tip = "秘密";
		else if (flag == 2)
		    tip = "机密";
		else if (flag == 3) {
		    tip = "绝密";
		}
		AuditLogger logger = AuditLogger.getLogger(Channel.SECURITY,
			Catalog.UNAUTHORIZEDACCESS);
		AuditModel auditModel = AuditModel.create();
		auditModel.setTitle(I18nRes.findValue("越权访问")
			+ I18nRes.findValue(tip) + I18nRes.findValue("数据"));
		auditModel.addObjectTrace("ProcessTitle",
			instanceModel.getTitle());
		auditModel
			.addObjectTrace("FlowName", WFFlexDesignVersionUtil
				.getFlowNameOfVersion(flowModel));
		logger.warn(auditModel);
		String mes = I18nRes.findValue("对不起") + "，" + sbTip
			+ I18nRes.findValue("任务不能创建") + "!";
		if (owner.isMobileClient()) {
		    inputHidden
			    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='0'>\n");
		    return taskMessageWarnAlert(owner,
			    mes + inputHidden.toString());
		}
		return AlertWindow.getWarningWindow(I18nRes.findValue("对不起")
			+ "，" + sbTip + I18nRes.findValue("任务不能创建") + "!");
	    }
	}

	WorkFlowStepModel preWorkFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(instanceModel.getProcessDefinitionId(),
			instanceModel.getActivityDefinitionNo());
	if (preWorkFlowStepModel == null) {
	    return sendMessageWarnAlert(owner, "<I18N#上个个节点模型已不存在>!"
		    + inputHidden.toString(), "parent.window.close();");
	}
	if (workFlowStepModel._transfersLimitType != 0
		&& !workFlowStepModel._transfersLimit.equals("")) {
	    String isLimit = "";
	    UtilString myStr = new UtilString(participants.trim());
	    Vector myArray = myStr.split(" ");
	    if (myArray != null && myArray.size() != 0) {
		for (int i = 0; i < myArray.size(); ++i) {
		    UserModel userModel = (UserModel) UserCache
			    .getModel(Function.getUID(myArray.elementAt(i)
				    .toString()));
		    UtilString myStr2 = new UtilString(
			    workFlowStepModel._transfersLimit.trim());
		    Vector myArray2 = myStr2.split(" ");
		    if (myArray2 != null && myArray2.size() != 0) {
			for (int ii = 0; ii < myArray2.size(); ++ii) {
			    if (myArray2
				    .elementAt(ii)
				    .toString()
				    .equals(Integer.toString(userModel
					    .getRoleId()))) {
				isLimit = "";
				break;
			    }
			    Hashtable roleMapList = UserMapCache
				    .getMapListOfUser(userModel.getId());
			    if (roleMapList != null && roleMapList.size() != 0) {
				for (int r = 0; r < roleMapList.size(); ++r) {
				    UserMapModel mapModel = (UserMapModel) roleMapList
					    .get(new Integer(r));
				    RoleModel roleModel = (RoleModel) RoleCache
					    .getModel(mapModel.getRoleId());
				    if (myArray2
					    .elementAt(ii)
					    .toString()
					    .equals(Integer.toString(roleModel
						    .getId()))) {
					isLimit = "";
					break;
				    }
				    isLimit = userModel.getUID();
				}
			    } else {
				isLimit = userModel.getUID();
			    }
			}
		    }
		    if (isLimit.equals("")) {
			break;
		    }
		}
	    }
	    if (!isLimit.equals("")) {
		if (flowModel._flowMaster.trim().length() > 0) {
		    alertMail(owner.getUID(), flowModel._flowMaster,
			    "<I18N#流程向下执行时被系统级过滤器阻止!>", "我在使用流程["
				    + flowModel._flowName + "]时，在["
				    + workFlowStepModel._stepName + "]节点给["
				    + participants + "]发送时被该节点配置的角色过滤策略阻止!");
		}
		return alertMessage(owner, "<I18N#提示>", "<I18N#任务不能被发送>！",
			"<I18N#目标用户>" + isLimit + "<I18N#不属于限制范围内,发送被终止>！");
	    }
	}

	try {
	    Vector subProcessProfiles = WorkflowTaskEngine.getInstance()
		    .getSubProcessProfilesOfRules(owner, processInstanceId,
			    taskInstanceId);
	    WorkFlowSubModel subProcessProfile = null;
	    if (subProcessProfiles.size() > 0)
		subProcessProfile = (WorkFlowSubModel) subProcessProfiles
			.get(0);
	    int[] subProcessInstanceIds = new int[0];
	    if (subProcessProfile != null) {
		try {
		    String breakPointInfo = "";
		    if (subProcessProfile._synType == SynType.synchronous
			    .getValue()) {
			breakPointInfo = "_owner{" + owner.getUID()
				+ "}owner_ _synType{" + runStyle
				+ "}synType_ _priority{" + priority
				+ "}priority_ _nextStepNo{" + nextStepNo
				+ "}nextStepNo_ _participants{" + participants
				+ "}participants_ _title{" + title
				+ "}title_ _isShortMessage{" + isShortMessage
				+ "}isShortMessage_ _localDepartmentId{"
				+ localDepartmentId + "}localDepartmentId_";
		    }
		    for (int i = 0; i < subProcessProfiles.size(); ++i) {
			WorkFlowSubModel subProcessProfileModel = (WorkFlowSubModel) subProcessProfiles
				.get(i);
			int[] ids = SubWorkflowEngine.getInstance()
				.createSubProcessInstance(processInstanceId,
					taskInstanceId, -1,
					subProcessProfileModel, "", "",
					breakPointInfo, null);
			if (ids != null) {
			    int[] tmp = new int[subProcessInstanceIds.length
				    + ids.length];
			    System.arraycopy(subProcessInstanceIds, 0, tmp, 0,
				    subProcessInstanceIds.length);
			    System.arraycopy(ids, 0, tmp,
				    subProcessInstanceIds.length, ids.length);
			    subProcessInstanceIds = tmp;
			}
		    }
		} catch (SubWorkflowException we) {
		    we.printStackTrace(System.err);
		    return alertMessage(owner, "", "", we.getMessage());
		}
	    }
	    boolean isSuccessfully = WorkflowTaskEngine.getInstance()
		    .closeProcessTaskInstance(owner, processInstanceId,
			    taskInstanceId);
	    if (!isSuccessfully) {
		if (subProcessProfile != null) {
		    try {
			WorkflowEngine.getInstance().removeProcessInstances(
				subProcessInstanceIds);
		    } catch (WorkflowException localWorkflowException1) {
			localWorkflowException1.printStackTrace(System.err);
		    }
		    if (subProcessInstanceIds != null
			    && subProcessInstanceIds.length > 0) {
			for (int i = 0; i < subProcessInstanceIds.length; ++i) {
			    int subId = subProcessInstanceIds[i];
			    SubProcessInstanceUtil.getInstance()
				    .clearProcessSharedataMapping(
					    processInstanceId, subId);
			}
		    }
		}
		return alertMessage(owner, "<I18N#提示>", "<I18N#任务不能结束>！",
			"<I18N#数据没有办理完毕>" + inputHidden.toString());
	    }
	    if (subProcessProfile == null
		    || subProcessProfile._synType == SynType.asynchronous
			    .getValue() || subProcessInstanceIds.length == 0) {
		ProcessRuntimeDaoFactory.createTaskInstance()
			.removeOtherTaskByTask(processInstanceId,
				taskInstanceId);
		int newTaskId = 0;
		int[] tid = WorkflowTaskEngine.getInstance()
			.createProcessTaskInstance(owner, processInstanceId,
				new SynType(runStyle),
				new PriorityType(priority), status, nextStepNo,
				participants, title, isShortMessage,
				localDepartmentId);
		newTaskId = tid[0];
		ProcessInstanceUtil hmc = new ProcessInstanceUtil(
			processInstanceId);
		hmc.flowStepEndCall(owner, taskInstanceId, "通知:" + title,
			preWorkFlowStepModel);
		if (LICENSE.isBPA()) {
		    boolean isSafSupport = false;
		    try {
			isSafSupport = new AnalysisKPIScope()
				.isSafSupport(flowModel._uuid);
		    } catch (Exception localException1) {
			localException1.printStackTrace(System.err);
		    }
		    if (isSafSupport) {
			inputHidden
				.append("<input type='hidden' name='executeStatus' id='executeStatus' value='1'>\n");
			return alertMessageAndSatisfation(
				owner,
				"<I18N#恭喜>",
				"<I18N#任务已经发出>！<input type=hidden name=lastTaskId value="
					+ newTaskId + ">"
					+ inputHidden.toString(),
				"<I18N#已经发送给下一个待办者>("
					+ Function.getAddressName(participants)
					+ ")");
		    }
		    inputHidden
			    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='1'>\n");
		    return alertMessage(
			    owner,
			    "<I18N#恭喜>",
			    "<I18N#任务已经发出>！<input type=hidden name=lastTaskId value="
				    + newTaskId + ">" + inputHidden.toString(),
			    "<I18N#已经发送给下一个待办者>("
				    + Function.getAddressName(participants)
				    + ")", "task end");
		}
		inputHidden
			.append("<input type='hidden' name='executeStatus' id='executeStatus' value='1'>\n");
		return alertMessage(
			owner,
			"<I18N#恭喜>",
			"<I18N#任务已经发出>！<input type=hidden name=lastTaskId value="
				+ newTaskId + ">" + inputHidden.toString(),
			"<I18N#已经发送给下一个待办者>("
				+ Function.getAddressName(participants) + ")");
	    }
	    Connection conn = null;
	    StringBuilder input = new StringBuilder();
	    try {
		conn = DBSql.open();
		String sql = "";
		for (int i = 0; i < subProcessInstanceIds.length; ++i) {
		    input.append("\n<input type=hidden name='subProcessInstanceId_"
			    + i + "' value='" + subProcessInstanceIds[i] + "'>");
		    sql = "select id from wf_task where bind_id="
			    + subProcessInstanceIds[i];
		    int taskId = DBSql.getInt(conn, sql, "id");
		    input.append("\n<input type=hidden name='subProcessTaskInstanceId_"
			    + i + "' value='" + taskId + "'>");
		}
	    } catch (Exception e) {
		System.out.println("查询子流程任务实例时出现错误：");
		e.printStackTrace(System.err);
	    } finally {
		DBSql.close(conn, null, null);
	    }
	    inputHidden
		    .append("<input type='hidden' name='executeStatus' id='executeStatus' value='1'>\n");
	    return alertMessage(
		    owner,
		    "<I18N#恭喜>",
		    "<I18N#子流程已经启动>" + input.toString()
			    + inputHidden.toString(),
		    "<I18N#子流程结束后会自动通知下个任务的待办者>(" + participants + ")");
	} catch (WorkflowException we) {
	    flowModel._flowMaster.trim().length();
	    we.printStackTrace(System.err);
	    return alertMessage(owner, "", "", we.getMessage());
	}
    }

    public String getEntrustSendPageOf2(UserContext me, int id, int stepNo,
	    int taskId) {
	ProcessInstanceModel model = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(id);
	if (model == null) {
	    return RepleaseKey.replaceI18NTag(me.getLanguage(),
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!");
	}
	TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		.createTaskInstance().getInstanceOfActive(taskId);
	if (taskInstanceModel == null && model.isStart()) {
	    return RepleaseKey.replaceI18NTag(me.getLanguage(),
		    "<I18N#该任务已经结束>!");
	}
	if (taskInstanceModel != null && taskInstanceModel.getStatus() != 1) {
	    return RepleaseKey.replaceI18NTag(me.getLanguage(),
		    "<I18N#此类型的任务不允许执行该操作>!");
	}

	String cancelButton = "<input type=button value='<I18N#放 弃>'  class ='actionsoftButton' onClick='parent.window.close();return false;'    border='0'>";
	String preButton = "";
	String sendButton = "<input type=button value='<I18N#发 送>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Worklist_Transaction_Entrust_Send');return false;\"   border='0'>";
	String addresssButton = "<input type=button value='<I18N#地址簿>'  class ='actionsoftButton' onClick=\"openmailtree(frmMain,'MAIL_TO','Address_Inner_Open');return false;\"   border='0'>";
	String sid = "<input type=hidden name=sid value=" + me.getSessionId()
		+ ">\n";

	DepartmentModel localDepartmentModel = me.getDepartmentModel();

	if (taskId > 0) {
	    int ownerDepartmentId = taskInstanceModel.getOwnerDepartmentId();
	    if (ownerDepartmentId > 0) {
		if (UserCache.isExistInDepartment(
			taskInstanceModel.getOwnerDepartmentId(), me.getID())) {
		    localDepartmentModel = (DepartmentModel) DepartmentCache
			    .getModel(taskInstanceModel.getOwnerDepartmentId());
		} else {
		    DepartmentModel tmpDepartmentModel = (DepartmentModel) DepartmentCache
			    .getModel(taskInstanceModel.getOwnerDepartmentId());

		    if (tmpDepartmentModel != null
			    && UserCache.isExistInDepartment(
				    tmpDepartmentModel.getParentDepartmentId(),
				    me.getID())) {
			localDepartmentModel = (DepartmentModel) DepartmentCache
				.getModel(tmpDepartmentModel
					.getParentDepartmentId());
		    }
		}

	    }
	    if (localDepartmentModel == null) {
		localDepartmentModel = (DepartmentModel) DepartmentCache
			.getModel(((UserModel) UserCache
				.getModel(taskInstanceModel.getOwner()))
				.getDepartmentId());
	    }
	}

	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(model.getProcessDefinitionId(), stepNo);
	String activeTitle = model.getTitle();
	String activeUser = workFlowStepModel._stepUser;
	WorkFlowModel workflowModel = (WorkFlowModel) WorkFlowCache
		.getModel(workFlowStepModel._flowId);
	if (workflowModel._defaultTitle.length() > 0) {
	    activeTitle = new RuntimeFormManager(me, id, taskId, 0, 0)
		    .convertMacrosValue(workflowModel._defaultTitle);
	    activeUser = new RuntimeFormManager(me, id, taskId, 0, 0)
		    .convertMacrosValue(activeUser);
	}

	String action = workFlowStepModel._stepTransmitName.equals("") ? "<I18N#委托>"
		: workFlowStepModel._stepTransmitName;
	String mailTo = "<input type='text' name='MAIL_TO'  class ='actionsoftInput' size='60'  value='"
		+ activeUser + "'>" + addresssButton;
	String title = "<input type='text' name='TITLE'  class ='actionsoftInput' size='60' value='("
		+ me.getUserModel().getUserName()
		+ I18nRes.findValue(me.getLanguage(), action)
		+ ")"
		+ activeTitle + "'>";
	String rb1 = "<input type=\"radio\"  name=\"rb1\" value=\"1\" checked ><I18N#无><input type=\"radio\" name=\"rb1\" value=\"3\"><I18N#高><input type=\"radio\" name=\"rb1\" value=2><I18N#中><input type=\"radio\" name=\"rb1\" value=\"0\"><I18N#低></td>";
	String rb2 = "<input type=\"radio\"  name=\"rb2\" value=\"0\"  'checked ' ><I18N#串签><input type=\"radio\" name=\"rb2\" value=1 ><I18N#并签></td>";

	String isShortMessageCheck = "";
	if (workFlowStepModel._isShortMessage
		&& AWFConfig._awfServerConf.getShortmessageServer()
			.toLowerCase().equals("on"))
	    isShortMessageCheck = "<input name=isShortMessageCheck type=radio value=0 ><I18N#不发送短信><input type=radio name=isShortMessageCheck value=1 checked><I18N#给当前办理人发送短信></td>";
	else {
	    isShortMessageCheck = "<input name=isShortMessageCheck type=radio value=0 checked><I18N#不发送短信><input type=radio name=isShortMessageCheck value=1><I18N#给当前办理人发送短信></td>";
	}
	if (!AWFConfig._awfServerConf.getShortmessageServer().toLowerCase()
		.equals("on")) {
	    isShortMessageCheck = "<div style='display:none'>"
		    + isShortMessageCheck + "</div>";
	}
	StringBuilder buffer = new StringBuilder();
	buffer.append("<script type=\"text/javascript\">\n");
	buffer.append("var 请输入委托办理者地址='")
		.append(I18nRes.findValue(me.getLanguage(), "请输入委托办理者地址"))
		.append("';\n");
	buffer.append("var 请输入委托办理标题='")
		.append(I18nRes.findValue(me.getLanguage(), "请输入委托办理标题"))
		.append("';\n");
	buffer.append("var 该节点只允许指定='")
		.append(I18nRes.findValue(me.getLanguage(), "该节点只允许指定"))
		.append("';\n");
	buffer.append("var 个人办理='")
		.append(I18nRes.findValue(me.getLanguage(), "个人办理"))
		.append("';\n");
	buffer.append("</script>");

	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("transmitPerson",
		I18nRes.findValue(me.getLanguage(), action));
	hashTags.put("button1", cancelButton);
	hashTags.put("button2", preButton);
	hashTags.put("button3", sendButton);
	hashTags.put("mailTo", mailTo);
	hashTags.put("pageTitle", model.getTitle());
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), title));
	hashTags.put("rb1", rb1);
	hashTags.put("rb2", rb2);
	hashTags.put("task_id", Integer.toString(taskId));
	hashTags.put("localDepartmentId",
		Integer.toString(localDepartmentModel.getId()));
	hashTags.put("localDepartmentName",
		localDepartmentModel.getDepartmentFullNameOfCache());
	hashTags.put("sid", sid);
	hashTags.put("isShortMessageCheck", isShortMessageCheck);
	hashTags.put("stepname", "<span><I18N#" + workFlowStepModel._stepName
		+ "></span>");
	hashTags.put("stepLimit",
		Integer.toString(workFlowStepModel._stepLimitLess));
	hashTags.put("stepLimitMore",
		Integer.toString(workFlowStepModel._stepLimitMore));
	hashTags.put("stepno", Integer.toString(stepNo));
	hashTags.put("id", Integer.toString(id));
	hashTags.put("i18nScript", buffer.toString());
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("wf_messageSendEntrust.htm"),
		hashTags);
    }

    public String sendEntrustMessage(UserContext owner, int bindId,
	    int runStyle, int priority, int status, int stepNo, String mailTo,
	    String title, int expiretime, int taskId, int localDepartmentId) {
	AuditLogger logger = AuditLogger.getLogger(Channel.SYSTEM,
		Catalog.RUNTIME, AuditObj.RT_AssignTask);
	Map trace = new HashMap();
	trace.put(I18nRes.findValue(owner.getLanguage(), "流程实例")
		+ "ID[ProcessInstanceId]", new Integer(bindId));
	trace.put(
		I18nRes.findValue(owner.getLanguage(), "任务实例") + "ID[TaskId]",
		new Integer(taskId));
	trace.put(I18nRes.findValue(owner.getLanguage(), "任务标题")
		+ "[TaskTitle]", title);
	trace.put(I18nRes.findValue(owner.getLanguage(), "移交发起者")
		+ "[TaskFromUID]", owner.getUID());

	RuntimeFormManager ufm = new RuntimeFormManager(owner, bindId, 0, 0, 0);
	String success = "";
	String targetUser = ufm.convertMacrosValue(mailTo);
	trace.put(I18nRes.findValue(owner.getLanguage(), "委托办理人")
		+ "[TaskToUID]", targetUser);
	ProcessInstanceModel model = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(bindId);
	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(model.getProcessDefinitionId(), stepNo);

	if (model == null) {
	    return sendMessageWarnAlert(owner,
		    "<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!",
		    "window.close();");
	}
	if (ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfActive(
		taskId) == null
		&& model.isStart()) {
	    return sendMessageWarnAlert(owner, "<I18N#该任务已经结束>!",
		    "parent.window.close();");
	}

	if (workFlowStepModel._routePointType == 0)
	    runStyle = 0;
	else {
	    runStyle = 1;
	}
	success = Function.checkAddress(targetUser.trim());
	if (!success.equals("ok")) {
	    trace.put("Exception", "账户不合法");
	    logger.log(
		    "任务" + title
			    + I18nRes.findValue(owner.getLanguage(), "委托办理")
			    + I18nRes.findValue(owner.getLanguage(), "移交失败"),
		    Action.AssignTask, trace, Level.ERROR);
	    if (owner.isMobileClient()) {
		return taskMessageWarnAlert(owner,
			AppError.getError("ERROR-0700"));
	    }
	    return AppError.getErrorPage("ERROR-0700");
	}
	try {
	    boolean isSuccessfully = WorkflowTaskEngine.getInstance()
		    .closeProcessTaskInstance(owner, bindId, taskId);
	    int[] tid = WorkflowTaskEngine.getInstance()
		    .createProcessTaskInstance(owner, bindId,
			    new SynType(runStyle), new PriorityType(priority),
			    status, stepNo, mailTo, title, false,
			    localDepartmentId);
	    logger.log(
		    I18nRes.findValue(owner.getLanguage(), "任务") + title
			    + I18nRes.findValue(owner.getLanguage(), "委托办理")
			    + I18nRes.findValue(owner.getLanguage(), "移交成功"),
		    Action.AssignTask, trace, Level.INFO);
	    return alertMessage(owner, "<I18N#恭喜>", "<I18N#已经发出>！",
		    "<I18N#任务已经发送给办理者>(" + Function.getAddressName(mailTo)
			    + ")");
	} catch (WorkflowException we) {
	    we.printStackTrace(System.err);
	    trace.put("Exception", we.getMessage());
	    logger.log(
		    I18nRes.findValue(owner.getLanguage(), "任务") + title
			    + I18nRes.findValue(owner.getLanguage(), "委托办理")
			    + I18nRes.findValue(owner.getLanguage(), "移交失败"),
		    Action.AssignTask, trace, Level.ERROR);
	    return alertMessage(owner, "", "", we.getMessage());
	}

    }

    private String sendMessageWarnAlert(UserContext me, String alert,
	    String actionJs) {
	alert = RepleaseKey.replaceI18NTag(me.getLanguage(), alert);
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, alert);
	}
	return "<script> var flag=false; alert('"
		+ alert
		+ "'); try{   parent.parent.MyWorkBoxView.closeCurActiveTab(); flag=true; }catch(e){} try{   parent.MyWorkBoxView.closeCurActiveTab(); flag=true }catch(e){} if(!flag){ try{  "
		+ actionJs + "   }catch(e){}}</script>";
    }

    public String alertMessage(UserContext me, String msg1, String msg2,
	    String msg3) {
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, msg1 + ":" + msg2 + msg3);
	}
	String button1 = "";
	StringBuilder javascript = new StringBuilder();
	javascript.append("function closeIWORK(){");
	javascript.append("p--;");
	javascript.append("if(p==0){");
	javascript
		.append("if( typeof(parent.BatchTaskData)!='undefined') {parent.win.hide();return;}else{};");
	javascript.append("var flag=false; ");
	javascript
		.append("try{parent.parent.MyWorkBoxView.closeCurActiveTab('task_'+parent.document.frmMain.task_id.value); flag=true;}catch(e){} ");
	javascript
		.append("try{parent.parent.MyWorkBoxView.closeCurActiveTab('trackmessage'); flag=true;}catch(e){}");
	javascript.append("try{parent.parent.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshGridState();}catch(e){}");
	javascript
		.append("try{parent.parent.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.opener.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentTab(); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentDialog(parent.document.frmMain.id.value); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.restoreUserWorkViewInnerContainerHeight();}catch(e){}");
	javascript
		.append("try{parent.parent.refreshCurrentForm(parent.parent.document.frmMain);}catch(e){}");
	if (!msg3.equals("<I18N#数据没有办理完毕>")) {
	    javascript.append("if(!flag){parent.window.close();}");
	}
	javascript.append("}");
	javascript.append("}");
	javascript.append("var p=2;setInterval('closeIWORK()',1500);");
	javascript.append("function refreshMailBox(){");
	javascript
		.append("try{parent.opener.refreshTask(parent.opener.document.frmMain);}catch(e){}");
	javascript.append("try{parent.refreshActiveInstanceGrid();}catch(e){}");
	javascript.append("}");
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type='hidden' name='sid' value='" + me.getSessionId()
			+ "'>");
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), msg1));
	hashTags.put("flag1", msg2);
	if (msg3.contains("无权限启动该流程")) {
	    msg3 = msg3.replace("无权限启动该流程",
		    I18nRes.findValue(me.getLanguage(), "无权限启动该流程"));
	}
	hashTags.put("flag2", msg3);
	hashTags.put("flag3", button1);
	hashTags.put("js", javascript);
	hashTags.put("onload", " onload='refreshMailBox()' ");
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("sys_alert.htm"), hashTags);
    }

    public String alertMessage(UserContext me, String msg1, String msg2,
	    String msg3, String status) {
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, msg1 + ":" + msg2 + msg3, status);
	}
	String button1 = "";
	StringBuilder javascript = new StringBuilder();
	javascript.append("function closeIWORK(){");
	javascript.append("p--;");
	javascript.append("if(p==0){");
	javascript
		.append("if( typeof(parent.BatchTaskData)!='undefined') {parent.win.hide();return;}else{};");
	javascript.append("var flag=false; ");
	javascript
		.append("try{parent.parent.MyWorkBoxView.closeCurActiveTab('task_'+parent.document.frmMain.task_id.value); flag=true;}catch(e){} ");
	javascript
		.append("try{parent.parent.MyWorkBoxView.closeCurActiveTab('trackmessage'); flag=true;}catch(e){}");
	javascript.append("try{parent.parent.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshGridState();}catch(e){}");
	javascript
		.append("try{parent.parent.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.opener.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentTab(); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentDialog(parent.document.frmMain.id.value); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.restoreUserWorkViewInnerContainerHeight();}catch(e){}");
	javascript
		.append("try{parent.parent.refreshCurrentForm(parent.parent.document.frmMain);}catch(e){}");
	if (!msg3.equals("<I18N#数据没有办理完毕>")) {
	    javascript.append("if(!flag){parent.window.close();}");
	}
	javascript.append("}");
	javascript.append("}");
	javascript.append("var p=2;setInterval('closeIWORK()',1500);");
	javascript.append("function refreshMailBox(){");
	javascript
		.append("try{parent.opener.refreshTask(parent.opener.document.frmMain);}catch(e){}");
	javascript.append("try{parent.refreshActiveInstanceGrid();}catch(e){}");
	javascript.append("}");

	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type='hidden' name='sid' value='" + me.getSessionId()
			+ "'>");
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), msg1));
	hashTags.put("flag1", msg2);
	hashTags.put("flag2", msg3);
	hashTags.put("flag3", button1);
	hashTags.put("js", javascript);
	hashTags.put("onload", " onload='refreshMailBox()' ");
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("sys_alert.htm"), hashTags);
    }

    public String alertMessageAndSatisfation(UserContext me, String msg1,
	    String msg2, String msg3) {
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, msg1 + ":" + msg2 + msg3);
	}
	String button1 = "";
	StringBuilder javascript = new StringBuilder();
	javascript.append("function closeIWORK(){");
	javascript.append("p--;");
	javascript.append("if(p==0){");
	javascript
		.append("if( typeof(parent.BatchTaskData)!='undefined') {parent.win.hide();return;}else{};");
	javascript.append("var flag=false; ");
	javascript.append("try{parent.parent.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshGridState();}catch(e){}");
	javascript
		.append("try{parent.parent.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.opener.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentTab(); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentDialog(parent.document.frmMain.id.value); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.restoreUserWorkViewInnerContainerHeight();}catch(e){}");
	javascript
		.append("try{parent.parent.refreshCurrentForm(parent.parent.document.frmMain);}catch(e){}");
	javascript.append("if(!flag){parent.openSatisfationWin();}");
	javascript.append("}");
	javascript.append("}");
	javascript.append("var p=2;setInterval('closeIWORK()',1000);");
	javascript.append("function refreshMailBox(){");
	javascript
		.append("try{parent.opener.refreshTask(parent.opener.document.frmMain);}catch(e){}");
	javascript.append("try{parent.refreshActiveInstanceGrid();}catch(e){}");
	javascript.append("}\n");
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type='hidden' name='sid' value='" + me.getSessionId()
			+ "'>");
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), msg1));
	hashTags.put("flag1", msg2);
	hashTags.put("flag2", msg3);
	hashTags.put("flag3", button1);
	hashTags.put("js", javascript);
	hashTags.put("onload", " onload='refreshMailBox()' ");
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("sys_alert.htm"), hashTags);
    }

    public String alertMessageAndSatisfation(UserContext me, String msg1,
	    String msg2, String msg3, String status) {
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, msg1 + ":" + msg2 + msg3, status);
	}
	String button1 = "";
	StringBuilder javascript = new StringBuilder();
	javascript.append("function closeIWORK(){");
	javascript.append("p--;");
	javascript.append("if(p==0){");
	javascript
		.append("if( typeof(parent.BatchTaskData)!='undefined') {parent.win.hide();return;}else{};");
	javascript.append("var flag=false; ");
	javascript.append("try{parent.parent.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshGridState();}catch(e){}");
	javascript
		.append("try{parent.parent.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.opener.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentTab(); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentDialog(parent.document.frmMain.id.value); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.restoreUserWorkViewInnerContainerHeight();}catch(e){}");
	javascript
		.append("try{parent.parent.refreshCurrentForm(parent.parent.document.frmMain);}catch(e){}");
	javascript.append("if(!flag){parent.openSatisfationWin();}");
	javascript.append("}");
	javascript.append("}");
	javascript.append("var p=2;setInterval('closeIWORK()',1000);");
	javascript.append("function refreshMailBox(){");
	javascript
		.append("try{parent.opener.refreshTask(parent.opener.document.frmMain);}catch(e){}");
	javascript.append("try{parent.refreshActiveInstanceGrid();}catch(e){}");
	javascript.append("}\n");
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type='hidden' name='sid' value='" + me.getSessionId()
			+ "'>");
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), msg1));
	hashTags.put("flag1", msg2);
	hashTags.put("flag2", msg3);
	hashTags.put("flag3", button1);
	hashTags.put("js", javascript);
	hashTags.put("onload", " onload='refreshMailBox()' ");
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("sys_alert.htm"), hashTags);
    }

    public String alertMessage2(UserContext me, String msg1, String msg2,
	    String msg3) {
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, msg1 + ":" + msg2 + msg3);
	}
	String button1 = "";
	StringBuilder javascript = new StringBuilder();
	javascript.append("function closeIWORK(){");
	javascript.append("p--;if(p==0){");
	javascript
		.append("if( typeof(parent.BatchTaskData)!='undefined') {parent.win.hide();return;}else{}; ");
	javascript.append("try{parent.closeActiveDialog();}catch(e){}");
	javascript
		.append("try{parent.refreshActiveInstanceGrid();}catch(e){} ");
	javascript.append("var flag=false;");
	javascript
		.append("try{parent.MyWorkBoxView.closeCurActiveTab('task_'+document.frmMain.task_id.value);  flag=true;}catch(e){} ");
	javascript
		.append("try{parent.parent.MyWorkBoxView.closeCurActiveTab('task_'+parent.document.frmMain.task_id.value);  flag=true;}catch(e){} ");
	javascript
		.append("try{parent.parent.MyWorkBoxView.closeCurActiveTab('trackmessage'); flag=true;}catch(e){}");
	javascript.append("try{parent.parent.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshWorkList();}catch(e){}");
	javascript.append("try{parent.opener.refreshGridState();}catch(e){}");
	javascript
		.append("try{parent.parent.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.opener.reloadUserWorkViewGrid();}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentTab(); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.closeUserWorkViewCurrentDialog(parent.document.frmMain.id.value); flag=true;}catch(e){}");
	javascript
		.append("try{parent.parent.restoreUserWorkViewInnerContainerHeight();}catch(e){}");
	javascript.append("if(!flag){ parent.window.close();}");
	javascript.append("}");
	javascript.append("}");
	javascript.append("var p=3;setInterval('closeIWORK()',1000);");
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type='hidden' name='sid' value='" + me.getSessionId()
			+ "'>");
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), msg1));
	hashTags.put("flag1", msg2);
	hashTags.put("flag2", msg3);
	hashTags.put("flag3", button1);
	hashTags.put("js", javascript);
	hashTags.put("onload", "");
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("sys_alert.htm"), hashTags);
    }

    public String alertMessage3(UserContext me, String msg1, String msg2,
	    String msg3) {
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, msg1 + ":" + msg2 + msg3);
	}
	String button1 = "";
	String javascript = "function closeIWORK(){p--;if(p==0){if( typeof(parent.BatchTaskData)!='undefined') {parent.win.hide();return;}else{}; try{parent.closeActiveDialog();}catch(e){}try{parent.refreshActiveInstanceGrid();}catch(e){} var flag=false;try{parent.MyWorkBoxView.closeCurActiveTab('task_'+document.frmMain.task_id.value);  flag=true;}catch(e){} try{parent.parent.MyWorkBoxView.closeCurActiveTab('task_'+parent.document.frmMain.task_id.value);  flag=true;}catch(e){} try{parent.parent.MyWorkBoxView.closeCurActiveTab('trackmessage'); flag=true;}catch(e){}try{parent.opener.refreshGridState();}catch(e){}try{parent.parent.reloadUserWorkViewGrid();}catch(e){}try{parent.opener.reloadUserWorkViewGrid();}catch(e){}try{parent.parent.closeUserWorkViewCurrentTab(); flag=true;}catch(e){}try{parent.parent.closeUserWorkViewCurrentDialog(parent.document.frmMain.id.value); flag=true;}catch(e){}try{parent.parent.restoreUserWorkViewInnerContainerHeight();}catch(e){}if(!flag){ parent.window.close();}}}var p=3;setInterval('closeIWORK()',50);";

	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type='hidden' name='sid' value='" + me.getSessionId()
			+ "'>");
	hashTags.put("title", I18nRes.findValue(me.getLanguage(), msg1));
	hashTags.put("flag1", msg2);
	hashTags.put("flag2", msg3);
	hashTags.put("flag3", button1);
	hashTags.put("js", javascript);
	hashTags.put("onload", "");
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("sys_alert.htm"), hashTags);
    }

    private String taskMessageWarnAlert(UserContext me, String msg) {
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type=hidden name=sid value=" + me.getSessionId()
			+ ">\n");
	hashTags.put("msg", msg);
	hashTags.put("userName", me.getUserModel().getUserName());
	setCommonTags(me, hashTags);
	return RepleaseKey
		.replace(
			HtmlModelFactory
				.getHtmlModel("com.actionsoft.apps.portal.mobile_Portal_Task_MsgBox.htm"),
			hashTags);
    }

    private String taskMessageWarnAlert(UserContext me, String msg,
	    String status) {
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid",
		"<input type=hidden name=sid value=" + me.getSessionId()
			+ ">\n");
	hashTags.put("msg", msg);
	hashTags.put("userName", me.getUserModel().getUserName());
	hashTags.put("status", status);
	setCommonTags(me, hashTags);
	return RepleaseKey
		.replace(
			HtmlModelFactory
				.getHtmlModel("com.actionsoft.apps.portal.mobile_Portal_Task_MsgBox.htm"),
			hashTags);
    }

    public String getArchivesList(UserContext me, int id, int taskId,
	    String storeRoomName, int volumeId) {
	StringBuilder x1 = new StringBuilder("");
	StringBuilder x2 = new StringBuilder("");
	StringBuilder x3 = new StringBuilder("");
	String sid = "<input type=hidden name=sid value=" + me.getSessionId()
		+ ">\n";
	x1.append("<select name='select_room'  class ='actionsoftSelect' onchange=\"execMyCommand(frmMain,'WorkFlow_Execute_ArchivesWin');return false;\">");
	if (storeRoomName.equals(""))
	    x1.append("<option value=0><I18N#请选择></option>");
	else {
	    x1.append("<option value=").append(storeRoomName).append(">")
		    .append(storeRoomName).append("</option>");
	}
	Hashtable h = ArchiveDaoFactory.createArchiveRoom().getInstance();
	if (h != null) {
	    for (int i = 0; i < h.size(); ++i) {
		ArchiveRoomModel model = (ArchiveRoomModel) h
			.get(new Integer(i));
		if (!storeRoomName.equals(model._roomName)
			&& (AccessControlUtil.accessControlCheck(me,
				"ARCHIVEROOM", Integer.toString(model._id),
				"RW") || me.getUID().equals("admin"))) {
		    x1.append("<option value=").append(model._roomName)
			    .append(">").append(model._roomName)
			    .append("</option>");
		}
	    }
	}
	x1.append("</select>");
	x2.append("<select name='select_volume'  class ='actionsoftSelect'>");
	x2.append("<option value=0><I18N#请选择></option>");
	h = ArchiveDaoFactory.createArchiveVolume().getInstanceOfRoom(
		storeRoomName);
	if (h != null) {
	    for (int i = 0; i < h.size(); ++i) {
		ArchiveVolumeModel model = (ArchiveVolumeModel) h
			.get(new Integer(i));
		x2.append("<option value=")
			.append(model._id)
			.append(">")
			.append(model._volumeNo + "(" + model._volumeName + ")")
			.append("</option>");
	    }
	}
	x2.append("</select>");
	x3.append("<input type=button value='<I18N#确定归档>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Archives');return false;\" name='ok' class='input'  border='0'>");
	Hashtable hashTags = new UnsyncHashtable(10);
	hashTags.put("flag1", x1.toString());
	hashTags.put("flag2", x2.toString());
	hashTags.put("flag3", x3.toString());
	hashTags.put("id", Integer.toString(id));
	hashTags.put("taskId", Integer.toString(taskId));
	hashTags.put("storeRoomName", storeRoomName);
	hashTags.put("page_title", "<I18N#归档>");
	hashTags.put("sid", sid);
	h = null;
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("wf_messageToArchives.htm"),
		hashTags);
    }

    public String toArchives(UserContext me, int id, int volumeId, int taskId) {
	return toArchives(me, false, id, volumeId, taskId);
    }

    public String toArchives(UserContext me, boolean isException,
	    int processInstanceId, int volumeId, int taskInstanceId) {
	ArchiveVolumeModel volumeModel = (ArchiveVolumeModel) ArchiveVolumeCache
		.getModel(volumeId);
	if (volumeModel == null) {
	    volumeModel = new ArchiveVolumeModel();
	    volumeModel._id = volumeId;
	    volumeModel._yearNo = UtilDate.yearFormat(new Timestamp(System
		    .currentTimeMillis()));
	    volumeModel._storeRoom = I18nRes.findValue(me.getLanguage(),
		    "aws.portal_自动归档档案库");
	    volumeModel._volumeNo = UtilDate.yearFormat(new Timestamp(System
		    .currentTimeMillis()));
	    volumeModel._volumeName = (UtilDate.yearFormat(new Timestamp(System
		    .currentTimeMillis())) + "年度档案");
	    volumeModel._startDate = (UtilDate.yearFormat(new Timestamp(System
		    .currentTimeMillis())) + "-01-01");
	    volumeModel._endDate = (UtilDate.yearFormat(new Timestamp(System
		    .currentTimeMillis())) + "-12-31");
	    ArchiveDaoFactory.createArchiveVolume().create(volumeModel);
	}
	ArchivesModel archivesModel = new ArchivesModel();
	ProcessInstanceModel processInstanceModel = new ProcessInstanceModel();
	processInstanceModel = ProcessRuntimeDaoFactory.createProcessInstance()
		.getInstance(processInstanceId);
	archivesModel._title = processInstanceModel.getTitle();
	archivesModel._titleKey = processInstanceModel.getTitleKey();
	archivesModel._volumeId = volumeId;
	archivesModel._fileFrom = processInstanceModel.getCreateUserLocation();
	archivesModel._createUser = processInstanceModel.getCreateUser();
	archivesModel._wfType = processInstanceModel.getProcessGroupName();
	archivesModel._fileId = processInstanceId;
	if (archivesModel._titleKey == null) {
	    archivesModel._titleKey = "";
	}
	int r = ArchiveDaoFactory.createArchives().create(archivesModel);
	if (r > 0) {
	    try {
		WorkflowEngine.getInstance().closeProcessInstance(me,
			processInstanceId, taskInstanceId);
		if (LICENSE.isBPA()) {
		    TaskInstance ti = new TaskInstance();
		    TaskInstanceModel task = ti
			    .getSystemTaskBySQL("select * from wf_task order by id desc");
		    WorkFlowTaskStartCollectorImp taskStartCollector = new WorkFlowTaskStartCollectorImp();
		    taskStartCollector.setTaskId(task.getId());
		    taskStartCollector.collectorData();
		    boolean isSafSupport = false;
		    try {
			WorkFlowModel flowModel = (WorkFlowModel) WorkFlowCache
				.getModel(processInstanceModel
					.getProcessDefinitionId());
			isSafSupport = new AnalysisKPIScope()
				.isSafSupport(flowModel._uuid);
		    } catch (Exception localException1) {
			localException1.printStackTrace(System.err);
		    }
		    if (isSafSupport) {
			return alertMessageAndSatisfation(me, "<I18N#恭喜>",
				"<I18N#流转完毕>！", "<I18N#流转完毕并归档到档案库的案卷中>");
		    }
		}
		Connection conn = null;
		StringBuilder input = new StringBuilder();
		try {
		    conn = DBSql.open();
		    String sql = "";

		    sql = "select subprocessinstanceid from wf_subprocess s where s.parentprocessinstanceid="
			    + processInstanceId;
		    int subprocessinstanceid = DBSql.getInt(conn, sql,
			    "subprocessinstanceid");
		    input.append("\n<input type=hidden name='subProcessInstanceId_0' value='"
			    + subprocessinstanceid + "'>");

		    sql = "select subtaskinstanceid from wf_subprocess s where s.parentprocessinstanceid="
			    + processInstanceId;
		    int subtaskinstanceid = DBSql.getInt(conn, sql,
			    "subtaskinstanceid");
		    input.append("\n<input type=hidden name='subProcessTaskInstanceId_0' value='"
			    + subtaskinstanceid + "'>");
		} catch (Exception e) {
		    System.out.println("查询子流程任务实例时出现错误：");
		    e.printStackTrace(System.err);
		} finally {
		    DBSql.close(conn, null, null);
		}

		input.append("\n<input type=hidden id='taskInstanceId' name='taskInstanceId' value='"
			+ taskInstanceId + "'>\n");
		input.append("\n<input type=hidden id='processInstanceId' name='processInstanceId' value='"
			+ processInstanceId + "'>");
		input.append("\n<input type=hidden id='executeStatus' name='executeStatus' value='1'>");

		return alertMessage(me, "<I18N#恭喜>",
			"<I18N#流转完毕>！" + input.toString(),
			"<I18N#流转完毕并归档到档案库的案卷中>", "task end");
	    } catch (WorkflowException we) {
		we.printStackTrace(System.err);
		return alertMessage(me, "", "", we.getMessage());
	    } catch (Exception e1) {
		e1.printStackTrace(System.err);
		return e1.getMessage();
	    }
	}
	if (r == DBSql.SQL_EXECUTE_STATUS_DBPOOL_ERROR) {
	    if (me.isMobileClient()) {
		return taskMessageWarnAlert(me, AppError.getError("ERROR-0303"));
	    }
	    return AppError.getErrorPage("ERROR-0303");
	}
	if (me.isMobileClient()) {
	    return taskMessageWarnAlert(me, AppError.getError("ERROR-0302"));
	}
	return AppError.getErrorPage("ERROR-0302");
    }

    public void alertMail(String fromUser, String toUser, String title,
	    String content) {
	content = toUser + ",您好!<br><br>" + content
		+ "<br><br><hr>这是一封由系统自动发送给您的提醒邮件，不必回复<br>";
	IMAPI.getInstance().sendMail(fromUser, toUser, "<I18N#提醒>" + title,
		content);
    }

    public String getAddParticipantsPage(UserContext me, int id, int taskId) {
	TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		.createTaskInstance().getInstanceOfActive(taskId);
	StringBuilder addParticipantsType = new StringBuilder();
	String custom = "";
	String titleType = "";
	String requestType = "read";
	String translation = "none";
	StringBuilder loadTranslation = new StringBuilder();
	if (taskInstanceModel != null) {
	    WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache
		    .getModel(taskInstanceModel.getActivityDefinitionId());
	    if (stepModel != null && stepModel._addParticipantsType != null) {
		addParticipantsType
			.append("<td  width=\"10%\" align=\"center\" valign=\"middle\" nowrap style=\"display:")
			.append(Function.isCheckedCheckbox(
				stepModel._addParticipantsType, "0", " ") ? ""
				: "none")
			.append("\" ")
			.append(">")
			.append("<input type=\"radio\" value=\"0\" name=\"activeType\" ")
			.append(stepModel._addParticipantsType.trim().indexOf(
				"0") == 0 ? "checked" : "")
			.append(" onClick=\"changeActiveType();\"><I18N#部门阅办></td>");
		addParticipantsType
			.append("<td width=\"10%\" align=\"center\" valign=\"middle\" nowrap style=\"display:")
			.append(Function.isCheckedCheckbox(
				stepModel._addParticipantsType, "1", " ") ? ""
				: "none")
			.append("\" ")
			.append(">")
			.append("<input type=\"radio\" value=\"1\" name=\"activeType\" ")
			.append(stepModel._addParticipantsType.trim().indexOf(
				"1") == 0 ? "checked" : "")
			.append(" onClick=\"changeActiveType();\"> <I18N#会签></td>");
		addParticipantsType
			.append("<td width=\"10%\" align=\"center\" valign=\"middle\" nowrap style=\"display:")
			.append(stepModel._addParticipantsType.trim().length() == 0
				|| Function.isCheckedCheckbox(
					stepModel._addParticipantsType, "2",
					" ") ? "" : "none")
			.append("\">")
			.append("<input type=\"radio\" value=\"2\" name=\"activeType\" ")
			.append(stepModel._addParticipantsType.trim().length() == 0
				|| stepModel._addParticipantsType.trim()
					.indexOf("2") == 0 ? "checked" : "")
			.append("  onClick=\"changeActiveType();\"><I18N#自由加签></td>");
		addParticipantsType
			.append("<td width=\"10%\" align=\"center\" valign=\"middle\" nowrap style=\"display:")
			.append(Function.isCheckedCheckbox(
				stepModel._addParticipantsType, "3", " ") ? ""
				: "none")
			.append("\" ")
			.append(">")
			.append("<input type=\"radio\" value=\"3\" name=\"activeType\" ")
			.append(stepModel._addParticipantsType.trim().indexOf(
				"3") == 0 ? "checked" : "")
			.append(" onClick=\"changeActiveType();\"> <I18N#自由协同></td>");

		if (stepModel._addParticipantsType.trim().indexOf("0") == 0) {
		    titleType = "(阅办)";
		    requestType = "read";
		    translation = "";
		    custom = "none";
		} else if (stepModel._addParticipantsType.trim().indexOf("1") == 0) {
		    titleType = "(会签)";
		    requestType = "Dept";
		    translation = "";
		    custom = "none";
		    loadTranslation
			    .append("var nodes = OrgView.getNodes(); \n")
			    .append("nodes.setText('<I18N#当前用户同级部门>');\n")
			    .append("requestType = 'Dept';\n")
			    .append("nodes.reload();\n");
		} else if (stepModel._addParticipantsType.trim().length() == 0
			|| stepModel._addParticipantsType.trim().indexOf("2") == 0) {
		    titleType = "(加签)";
		    requestType = "";
		} else if (stepModel._addParticipantsType.trim().indexOf("3") == 0) {
		    titleType = "(协同)";
		    requestType = "";
		}
	    }

	}
	String button1 = "<input type=button value='<I18N#放 弃>' class ='actionsoftButton' onClick='parent.window.close();return false;'    border='0'>";
	String button2 = "<input type=button value='<I18N#发 送>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Worklist_AddParticipants_Send');return false;\"   border='0'>";
	String sid = "<input type=hidden name=\"sid\" id=\"sid\" value="
		+ me.getSessionId() + ">\n";
	String text1 = "";
	String addresss = "";
	if (me.getLanguage().equals("en")) {
	    if (me.getLookAndFeelType().equals("_def51")
		    || me.getLookAndFeelType().equals("_def55"))
		text1 = "<input type='text' name='MAIL_TO'  class ='actionsoftInput' style='width:66px' size=\"50\">";
	    else {
		text1 = "<input type='text' name='MAIL_TO'  class ='actionsoftInput' style='width:120px' size=\"50\">";
	    }
	    addresss = "<input type=button value='<I18N#地址簿>'  class ='actionsoftButton' onClick=\"openmailtree(frmMain,'MAIL_TO','Address_Inner_Open');return false;\"   border='0'>";
	} else {
	    text1 = "<input type='text' name='MAIL_TO'  class ='actionsoftInput' style='width:278px' size=\"50\">";
	    addresss = "<input type=button value='<I18N#地址簿>'  class ='actionsoftButton' onClick=\"openmailtree(frmMain,'MAIL_TO','Address_Inner_Open');return false;\"   border='0'>";
	}
	StringBuilder checkHtml = new StringBuilder();
	Hashtable hashTags = new UnsyncHashtable(8);
	hashTags.put("button1", button1);
	hashTags.put("button2", button2);
	hashTags.put("text1", text1);
	hashTags.put("addresss", addresss);
	hashTags.put("custom", custom);
	hashTags.put("titleType", titleType);
	hashTags.put("requestType", requestType);
	hashTags.put("translation", translation);
	hashTags.put("loadTranslation", loadTranslation.toString());
	hashTags.put("addParticipantsType", addParticipantsType.toString());
	hashTags.put("title", ProcessRuntimeDaoFactory.createProcessInstance()
		.getInstance(id).getTitle());
	hashTags.put("checkList", checkHtml.toString());
	hashTags.put("task_id", Integer.toString(taskId));
	hashTags.put("sid", sid);
	hashTags.put("page_title", "<span><I18N#加签></span>");
	hashTags.put("id", Integer.toString(id));
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("wf_messageAddParticipants.htm"),
		hashTags);
    }

    public String addParticipantsTask(UserContext owner, int processInstanceId,
	    int processTaskInstanceId, String participant, String title,
	    String opinion) {
	try {
	    TaskInstanceModel tm = ProcessRuntimeDaoFactory
		    .createTaskInstance().getInstanceOfActive(
			    processTaskInstanceId);
	    TaskInstanceModel fromTask = ProcessRuntimeDaoFactory
		    .createTaskInstance()
		    .getInstanceOfActive(tm.getFromPoint());
	    WorkflowTaskEngine.getInstance().appendProcessTaskInstance(owner,
		    processInstanceId, processTaskInstanceId, participant,
		    title);
	    UserTaskAuditMenuModel menuModel = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory
		    .createUserTaskAuditMenu().getInstanceOfTask(
			    processTaskInstanceId);
	    if (opinion != null) {
		UserTaskHistoryOpinionModel model = new UserTaskHistoryOpinionModel();

		ProcessInstanceModel _instanceModel = ProcessRuntimeDaoFactory
			.createProcessInstance().getInstance(processInstanceId);
		model.setCreateUser(owner.getUID());
		if (title.trim().length() > 0) {
		    if (title.indexOf("(阅办)") == 0)
			model.setOpinion(opinion
				+ "<br><br><b><font color=red><I18N#阅办></font></b>");
		    else if (title.indexOf("(会签)") == 0)
			model.setOpinion(opinion
				+ "<br><br><b><font color=red><I18N#会签></font></b>");
		    else if (title.indexOf("(协同)") == 0) {
			model.setOpinion(opinion
				+ "<br><br><b><font color=red><I18N#协同></font></b>");
		    } else if (fromTask == null)
			model.setOpinion(opinion
				+ "<br><br><b><font color=red><I18N#发起加签></font></b>");
		    else {
			model.setOpinion(opinion
				+ "<br><br><b><font color=red><I18N#加签></font></b>");
		    }
		} else {
		    model.setOpinion(opinion
			    + "<br><br><b><font color=red><I18N#加签></font></b>");
		}
		model.setAuditMenuName("-");
		if (menuModel != null) {
		    model.setFiles(menuModel.getFiles());
		}
		model.setProcessInstanceId(processInstanceId);
		try {
		    model.setTaskInstanceId(new DBSequence()
			    .getSequence("SYS_WORKFLOWOPINION"));
		} catch (SequenceException e) {
		    e.printStackTrace(System.err);
		}
		String files = "";
		if (menuModel != null) {
		    files = menuModel.getFiles();
		}
		UnsyncVector v = new UnsyncVector();
		if (!files.equals("")) {
		    v = new UtilString(files).split2UnsyncVector("@@@@");
		}
		for (int i = 0; i < v.size(); ++i) {
		    String filename = v.elementAt(i).toString();
		    String filepath = AWFConfig._awfServerConf
			    .getDocumentPath()
			    + "opinion/group"
			    + processInstanceId
			    + "/file"
			    + processTaskInstanceId
			    + "/"
			    + UpFile.encryptFileName(filename);
		    filepath = UtilFile.getDeepPath(filepath);
		    EncryptFileUtil.decryptFile(filepath);
		    UtilFile uf = new UtilFile(filepath + ".dec");
		    String path = AWFConfig._awfServerConf.getDocumentPath()
			    + "opinion/group" + processInstanceId + "/file"
			    + model.getTaskInstanceId() + "/";
		    String newfilepath = path;
		    newfilepath = UtilFile.getDeepPath(newfilepath);
		    File f = new File(newfilepath);
		    if (!f.exists()) {
			f.mkdirs();
		    }
		    newfilepath = newfilepath
			    + UpFile.encryptFileName(filename);
		    uf.saveAs(newfilepath);
		    UpFile.encryptFile(newfilepath);
		    File ff = new File(filepath + ".dec");
		    ff.delete();
		}
		model.setAuditObject("<span style=font-size:14px><b><I18N#"
			+ WorkFlowStepCache.getModelOfStepNo(
				_instanceModel.getProcessDefinitionId(),
				_instanceModel.getActivityDefinitionNo())._stepName
			+ "></b></span>");
		ProcessRuntimeDaoFactory.createUserTaskHistoryOpinion().create(
			model);
	    }
	    String msgId = DBSql.getString("select MSGID from WF_MESSAGE_INTERFACE where ID = " + processTaskInstanceId, "MSGID");
	    if (msgId.length() > 0) {
		HttpClient client = new HttpClient();
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(3000);
		connectionManager.getParams().setSoTimeout(3000);
		client.setHttpConnectionManager(connectionManager);
		JSONObject content = new JSONObject();
		content.put(
			"pcurl",
			AWFConfig._awfServerConf.getPortalHost()
				+ "/services/rs/sso/execute?cmd=WorkFlow_Execute_Worklist_File_Open&id="
				+ processInstanceId + "&task_id="
				+ processTaskInstanceId + "&openstate=4");
		content.put("status", "todo");
		content.put("msgId", msgId);
		content.put("state", 4);
		new UpdateWorkMsgThread(client, content, msgId, "").start();
	    }
	    return alertMessage(owner, "<I18N#恭喜>", "<I18N#已经发出>！",
		    "<I18N#已发送给指定人员>");
	} catch (WorkflowException we) {
	    we.printStackTrace(System.err);
	    return alertMessage(owner, "", "", we.getMessage());
	}
    }

    public String execAddParticipantsTask(UserContext target,
	    int processInstanceId, int processTaskInstanceId) {
	ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(processInstanceId);
	if (processInstanceModel == null) {
	    return "<script>alert('<I18N#流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除>!');window.close();</script>";
	}
	TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory
		.createTaskInstance()
		.getInstanceOfActive(processTaskInstanceId);
	if (taskInstanceModel == null && processInstanceModel.isStart()) {
	    return "<script>alert('<I18N#该任务已经结束>!');window.close();</script>";
	}
	WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		.getModelOfStepNo(
			processInstanceModel.getProcessDefinitionId(), 1);
	if (workFlowStepModel == null)
	    return "<script>alert('<I18N#未发现节点模型>!');window.close();</script>";
	try {
	    int taskid = DBSql.getInt(
		    "select from_point from wf_task where id="
			    + processTaskInstanceId, "from_point");
	    WorkflowTaskEngine.getInstance().closeAppendProcessTaskInstance(
		    target, processInstanceId, processTaskInstanceId);
	    if (taskid > 0) {
		DBSql.executeUpdate("update wf_task set readtime = sysdate where id = " + taskid);
		int c = DBSql.getInt("select count(*) as c from wf_task where bind_id = "+ processInstanceId +" and from_point = " + taskid + " and status = 11", "c");
		if (c == 0) {
    		    String msgId = DBSql.getString("select MSGID from WF_MESSAGE_INTERFACE where ID = " + taskid, "MSGID");
    		    if (msgId.length() > 0) {
    			HttpClient client = new HttpClient();
    			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    			connectionManager.getParams().setConnectionTimeout(3000);
    			connectionManager.getParams().setSoTimeout(3000);
    			client.setHttpConnectionManager(connectionManager);
    			JSONObject content = new JSONObject();
    			content.put("pcurl", AWFConfig._awfServerConf.getPortalHost()
    					+ "/services/rs/sso/execute?cmd=WorkFlow_Execute_Worklist_File_Open&id="
    					+ processInstanceId + "&task_id=" + taskid
    					+ "&openstate=1");
    			content.put("status", "todo");
    			content.put("msgId", msgId);
    			content.put("state", 1);
    			content.put("isread", 0);
    			new UpdateWorkMsgThread(client, content, msgId, "").start();
    		    }
		}
	    }
	    if (LICENSE.isBPA()) {
		WorkFlowTaskEndCollectorImp collector = new WorkFlowTaskEndCollectorImp();
		collector.setTaskId(processTaskInstanceId);
		collector.collectorData();
	    }
	    String msgId = DBSql.getString(
		    "select MSGID from WF_MESSAGE_INTERFACE where ID = "
			    + processTaskInstanceId, "MSGID");
	    if (msgId.length() > 0) {
		HttpClient client = new HttpClient();
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(3000);
		connectionManager.getParams().setSoTimeout(3000);
		client.setHttpConnectionManager(connectionManager);
		JSONObject content = new JSONObject();
		content.put(
			"pcurl",
			AWFConfig._awfServerConf.getPortalHost()
				+ "/services/rs/sso/execute?cmd=WorkFlow_Execute_Worklist_File_Open&id="
				+ processInstanceId + "&task_id="
				+ processTaskInstanceId + "&openstate=2");
		content.put("status", "done");
		content.put("msgId", msgId);
		if (taskInstanceModel.getEndTime() != null) {
		    content.put("endtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(taskInstanceModel.getEndTime()));
		}
		new UpdateWorkMsgThread(client, content, msgId, "Y").start();
	    }
	    return alertMessage3(target, "<I18N#恭喜>", "<I18N#协同任务已结束>！", "");
	} catch (WorkflowException we) {
	    we.printStackTrace(System.err);
	    return alertMessage(target, "", "", we.getMessage());
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    return alertMessage(target, "", "", e.getMessage());
	}

    }

    public String getCCTaskPage(UserContext me, int processInstanceId,
	    int taskInstanceId, String readToType, String client) {
	String button1 = "<input type=button value='<I18N#放 弃>' class ='actionsoftButton' onClick='parent.window.close();return false;'    border='0'>";
	String button2 = "<input type=button value='<I18N#发 送>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Worklist_SendRead_Send');return false;\"   border='0'>";
	String sid = "<input type=hidden name=sid value=" + me.getSessionId()
		+ ">\n";
	String addresss1 = "<input type=button value='<I18N#地址簿>'  class ='actionsoftButton' onClick=\"openmailtree(frmMain,'MAIL_TO','Address_Inner_Open');return false;\"   border='0'>";
	String text1 = "<input type='text' name='MAIL_TO'  class ='actionsoftInput' size='"
		+ (me.isMobileClient() ? "30" : "60") + "' >";
	if (me.isMobileClient()) {
	    addresss1 = "";
	}
	if (readToType.equals("")) {
	    readToType = "传阅";
	}
	String text2 = "<input type='text' name='TITLE'  class ='actionsoftInput' size='"
		+ (me.isMobileClient() ? "30" : "60")
		+ "' value='("
		+ readToType
		+ ")"
		+ ProcessRuntimeDaoFactory.createProcessInstance()
			.getInstance(processInstanceId).getTitle() + "'>";
	Hashtable taskOwnerList = ProcessRuntimeDaoFactory.createTaskInstance()
		.getUserTargetsOfProcessInstance(processInstanceId);
	StringBuilder checkHtml = new StringBuilder();
	int i = 0;
	if (taskOwnerList != null && taskOwnerList.size() != 0) {
	    checkHtml
		    .append("<tr><td width='22%' nowrap> <div align='right'><strong><I18N#可选接收人>：</strong></div></td>");
	    checkHtml
		    .append("<td colspan='3' width='78%' class='lightyellow'>");
	    for (Enumeration e = taskOwnerList.keys(); e.hasMoreElements();) {
		String taskOwner = (String) e.nextElement();
		++i;
		UserModel userModel = (UserModel) UserCache.getModel(taskOwner);
		checkHtml.append("<label><input type='checkbox' name=taskOwner"
			+ i + " value=" + taskOwner + " >"
			+ userModel.getUserNameAlias() + "[" + taskOwner
			+ "]&nbsp;&nbsp;</label>");
	    }
	    checkHtml.append("</td></tr>");
	}
	Hashtable hashTags = new UnsyncHashtable(8);
	hashTags.put("button1", button1);
	hashTags.put("button2", button2);
	hashTags.put("text1", text1);
	hashTags.put("addresss1", addresss1);
	hashTags.put("text2", text2);
	hashTags.put("checkList", checkHtml.toString());
	hashTags.put("task_id", Integer.toString(taskInstanceId));
	hashTags.put("sid", sid);
	hashTags.put("page_title",
		I18nRes.findValue(me.getLanguage(), readToType));
	hashTags.put("id", Integer.toString(processInstanceId));
	if (me.isMobileClient()) {
	    hashTags.put("client", client.equals("browser") ? "" : "none");
	    return RepleaseKey
		    .replace(
			    HtmlModelFactory
				    .getHtmlModel("com.actionsoft.apps.portal.mobile_Portal_PageCC.htm"),
			    hashTags);
	}
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("wf_messageRead.htm"), hashTags);
    }

    public String execCCTask(UserContext owner, int processInstanceId,
	    int processTaskInstanceId, String participant, String title) {
	try {
	    int newTaskId = 0;
	    int[] tid = WorkflowTaskEngine.getInstance()
		    .createCCProcessTaskInstance(owner, processInstanceId,
			    processTaskInstanceId, participant, title);
	    newTaskId = tid[0];
	    return alertMessage2(owner, "<I18N#恭喜>", "<I18N#已经发出>！",
		    "<I18N#已发送给指定接收人>");
	} catch (WorkflowException we) {
	    we.printStackTrace(System.err);
	    return alertMessage(owner, "", "", we.getMessage());
	}
    }

    private void setCommonTags(UserContext me, Hashtable hashTags) {
	String sessionId = me.getSessionId();
	try {
	    sessionId = URLEncoder.encode(sessionId, "UTF-8");
	} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
	    localUnsupportedEncodingException.printStackTrace(System.err);
	}
	hashTags.put("sessionId", sessionId);
	hashTags.put("headerTheme", MobileConfig.getConfModel()
		.getHeaderTheme());
	hashTags.put("theme", MobileConfig.getConfModel().getTheme());
	hashTags.put("footerTheme", MobileConfig.getConfModel()
		.getFooterTheme());
	hashTags.put("transition", MobileConfig.getConfModel().getTransition());
    }
}