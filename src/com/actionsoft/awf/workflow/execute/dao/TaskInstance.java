package com.actionsoft.awf.workflow.execute.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.database.CloudObject;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.calendar.util.CalWorkTimeImp;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.execute.PriorityType;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.engine.BPMEngineFactory;
import com.actionsoft.awf.workflow.execute.engine.TaskManager;
import com.actionsoft.awf.workflow.execute.engine.delegate.UserTaskActivityBehaviorContextInterface;
import com.actionsoft.awf.workflow.execute.engine.helper.RoutePointControl;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceLogModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.coe.bpa.etl.collector.WorkFlowTaskStartCollectorImp;
import com.actionsoft.eai.im.IMNotifyTaskMessage;

public class TaskInstance extends CloudObject {

    private int taskStepNo = 0;

    private static Map routeORGMapping = new ConcurrentHashMap();

    public int insertTask(TaskInstanceModel model) throws WorkflowException {
	return insertTask(model, false);
    }

    public int insertTask(TaskInstanceModel model, boolean isShortMessage)
	    throws WorkflowException {
	UserContext target = null;
	UserContext owner = null;
	ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		.createProcessInstance().getInstance(
			model.getProcessInstanceId());
	WorkFlowStepModel stepModel = null;
	try {
	    target = new UserContext(model.getTarget());
	    owner = new UserContext(model.getOwner());
	    if (model.getTitle() == null) {
		model.setTitle("空");
	    }
	    if (this.taskStepNo == 0) {
		this.taskStepNo = processInstanceModel
			.getActivityDefinitionNo();
	    }
	    stepModel = WorkFlowStepCache.getModelOfStepNo(
		    processInstanceModel.getProcessDefinitionId(),
		    this.taskStepNo);
	    model.setProcessDefinitionId(processInstanceModel
		    .getProcessDefinitionId());
	    model.setActivityDefinitionId(stepModel._id);
	    int[] mappingData = getRouteORGMapping(target.getUID(),
		    model.getProcessInstanceId());
	    if (mappingData == null) {
		DepartmentModel dModel = target.getDepartmentModel();
		mappingData = new int[3];
		mappingData[0] = dModel.getCompanyId();
		mappingData[1] = dModel.getId();
		mappingData[2] = target.getUserModel().getRoleId();
	    }
	    model.setTargetCompanyId(mappingData[0]);
	    model.setTargetDepartmentId(mappingData[1]);
	    model.setTargetRoleId(mappingData[2]);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    throw new WorkflowException(e.toString());
	}

	UserTaskActivityBehaviorContextInterface tContext = BPMEngineFactory
		.getInstance().createUserTaskActivityBehaviorContext();
	tContext.setTaskInstanceModel(model);
	tContext.setCurrentUserContext(target);
	tContext.setProcessInstanceModel(processInstanceModel);
	int taskInstanceId = TaskManager.createUserTask(tContext);
	if (taskInstanceId < 0) {
	    throw new WorkflowException("插入任务记录失败:\n"
		    + processInstanceModel.toString());
	}
	model.setId(taskInstanceId);
	Thread notifyTaskMessage = new Thread(new IMNotifyTaskMessage(owner,
		model, processInstanceModel));
	notifyTaskMessage.setName("AWS Task Notify(*)");
	notifyTaskMessage.start();

	if (model.getStatus() != 9 && isShortMessage
		&& stepModel._isShortMessage) {
	    Thread notifyShortMessage = new Thread(new NotifyShortMessage(
		    owner, model.getProcessInstanceId(), model.getTarget(),
		    processInstanceModel, model.getTitle(), model.getId()));
	    notifyShortMessage.setName("AWS Process Notify(SMS)");
	    notifyShortMessage.start();
	}
	if (stepModel._emailAlertType > 0) {
	    Thread notifyEmail = new Thread(new NotifyEMAIL(owner,
		    model.getTarget(), model.getTitle(), taskInstanceId,
		    stepModel._emailAlertType));
	    notifyEmail.setName("AWS Process Notify(Email)");
	    notifyEmail.start();
	}
	return taskInstanceId;
    }

    public int[] createUserTaskOfCC(UserContext owner, int processInstanceId,
	    int taskInstanceId, String participant, String title)
	    throws WorkflowException {
	Connection conn = DBSql.open();
	try {
	    participant = participant.trim();
	    String success = Function.checkAddress(participant);
	    if (!success.equals("ok")) {
		throw new WorkflowException("参与者含有不能到达的非法地址");
	    }
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(processInstanceId);
	    this.taskStepNo = processInstanceModel.getActivityDefinitionNo();
	    if (taskInstanceId > 0) {
		int wfsid = DBSql.getInt(conn,
			"select wfsid from wf_task where id=" + taskInstanceId,
			"wfsid");
		if (wfsid > 0) {
		    WorkFlowStepModel tmpStepModel = (WorkFlowStepModel) WorkFlowStepCache
			    .getModel(wfsid);
		    if (tmpStepModel != null)
			this.taskStepNo = tmpStepModel._stepNo;
		} else {
		    wfsid = DBSql.getInt(conn,
			    "select wfsid from wf_task_log where id="
				    + taskInstanceId, "wfsid");
		    if (wfsid > 0) {
			WorkFlowStepModel tmpStepModel = (WorkFlowStepModel) WorkFlowStepCache
				.getModel(wfsid);
			if (tmpStepModel != null) {
			    this.taskStepNo = tmpStepModel._stepNo;
			}
		    }
		}
	    }

	    UtilString myStr = new UtilString(participant);
	    Vector taskInstanceIdList = myStr.split(" ");
	    TaskInstanceModel model = new TaskInstanceModel();
	    model.setProcessInstanceId(processInstanceId);
	    model.setOwner(owner.getUID());
	    model.setTitle(title);
	    model.setPriority(2);
	    model.setExpireTime(0);
	    model.setFromPoint(taskInstanceId);
	    model.setStatus(2);
	    int ownerDeptId = DBSql.getInt(conn,
		    "select dptid from wf_task where id=" + taskInstanceId,
		    "dptid");
	    if (ownerDeptId == 0)
		ownerDeptId = DBSql.getInt(conn,
			"select dptid from wf_task_log where id="
				+ taskInstanceId, "dptid");
	    model.setOwnerDepartmentId(ownerDeptId);

	    int[] taskInstanceArray = new int[taskInstanceIdList.size()];
	    for (int i = 0; i < taskInstanceIdList.size(); ++i) {
		String uid = (String) taskInstanceIdList.get(i);
		uid = Function.getUID(uid);
		model.setTarget(uid);
		taskInstanceArray[i] = insertTask(model);

		if (!LICENSE.isBPA() || taskInstanceArray[i] <= 0)
		    continue;
		WorkFlowTaskStartCollectorImp taskStartImp = new WorkFlowTaskStartCollectorImp();
		taskStartImp.setTaskId(taskInstanceArray[i]);
		taskStartImp.collectorData();
	    }
	    return taskInstanceArray;
	} catch (WorkflowException we) {
	} catch (Exception e) {
	    throw new WorkflowException("插入任务数据时发生未知的异常");
	} finally {
	    DBSql.close(conn, null, null);
	}
	return new int[0];
    }

    public int[] createUserTaskOfAddParticipants(UserContext owner,
	    int processInstanceId, int taskInstanceId, String participant,
	    String title) throws WorkflowException {
	Connection conn = DBSql.open();
	try {
	    participant = participant.trim();
	    String success = Function.checkAddress(participant);
	    if (!success.equals("ok")) {
		throw new WorkflowException("加签参与者含有不能到达的非法地址");
	    }
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(processInstanceId);
	    this.taskStepNo = processInstanceModel.getActivityDefinitionNo();
	    int status = 0;
	    if (taskInstanceId > 0) {
		int wfsid = DBSql.getInt(conn,
			"select wfsid from wf_task where id=" + taskInstanceId,
			"wfsid");
		status = DBSql
			.getInt(conn, "select status from wf_task where id="
				+ taskInstanceId, "status");
		if (wfsid > 0) {
		    WorkFlowStepModel tmpStepModel = (WorkFlowStepModel) WorkFlowStepCache
			    .getModel(wfsid);
		    if (tmpStepModel != null)
			this.taskStepNo = tmpStepModel._stepNo;
		} else {
		    wfsid = DBSql.getInt(conn,
			    "select wfsid from wf_task_log where id="
				    + taskInstanceId, "wfsid");
		    if (wfsid > 0) {
			WorkFlowStepModel tmpStepModel = (WorkFlowStepModel) WorkFlowStepCache
				.getModel(wfsid);
			if (tmpStepModel != null) {
			    this.taskStepNo = tmpStepModel._stepNo;
			}
		    }
		}
	    }

	    Vector taskInstanceIdList = new UtilString(participant).split(" ");
	    TaskInstanceModel model = new TaskInstanceModel();
	    title = (title.indexOf("'") > -1) ? new UtilString(title).replace(
		    "'", "\"") : title;
	    model.setProcessInstanceId(processInstanceId);
	    model.setOwner(owner.getUID());
	    model.setTitle(title);
	    model.setPriority(2);
	    model.setExpireTime(0);
	    model.setFromPoint(taskInstanceId);
	    model.setStatus(11);
	    model.setOwnerDepartmentId(DBSql.getInt(conn,
		    "select dptid from wf_task where id=" + taskInstanceId,
		    "dptid"));
	    int[] taskInstanceArray = new int[taskInstanceIdList.size()];
	    for (int i = 0; i < taskInstanceIdList.size(); ++i) {
		String uid = (String) taskInstanceIdList.get(i);
		uid = Function.getUID(uid);
		model.setTarget(uid);
		taskInstanceArray[i] = insertTask(model);

		if (!LICENSE.isBPA() || taskInstanceArray[i] <= 0)
		    continue;
		WorkFlowTaskStartCollectorImp taskStartImp = new WorkFlowTaskStartCollectorImp();
		taskStartImp.setTaskId(taskInstanceArray[i]);
		taskStartImp.collectorData();
	    }
	    if (status != 11) {
		changeTaskState(conn, taskInstanceId, 4);
	    }
	    return taskInstanceArray;
	} catch (WorkflowException we) {
	} catch (Exception e) {
	    throw new WorkflowException("插入任务数据时发生未知的异常");
	} finally {
	    DBSql.close(conn, null, null);
	}
	return new int[0];
    }

    public int[] createUserTask(String ownerId, int processInstanceId,
	    SynType synType, PriorityType priorityType, int status,
	    int activityNo, String participant, String title, int expiretime,
	    int localDepartmentId, boolean isShortMessage)
	    throws WorkflowException {
	Connection conn = DBSql.open();
	try {
	    RoutePointControl rpc = new RoutePointControl(processInstanceId);
	    rpc.setActivityNo(activityNo);

	    rpc.resizeWaitPoint(conn);
	    int r = rpc.insertPoint(ownerId, participant, priorityType,
		    localDepartmentId);
	    if (r == RoutePointControl.INSERT_STATE_MAIL_VALIDATA)
		throw new WorkflowException("任务参与者含有不能到达的非法地址");
	    if (r == RoutePointControl.INSERT_STATE_WORKFLOW_BIND_ERROR)
		throw new WorkflowException("流程实例未发现");
	    if (r == RoutePointControl.INSERT_STATE_WORKFLOW_DEFINE_ERROR)
		throw new WorkflowException("流程定义或模型参考引用时发生错误");
	    if (r == RoutePointControl.INSERT_STATE_POINT_ERROR) {
		throw new WorkflowException("插入任务数据时发生异常");
	    }

	    Vector taskInstanceIdList = new UnsyncVector();
	    UtilString myStr = new UtilString(participant);
	    Vector myArray = myStr.split(" ");
	    title = (title.indexOf("'") > -1) ? new UtilString(title).replace(
		    "'", "\"") : title;
	    TaskInstanceModel model = new TaskInstanceModel();
	    model.setProcessInstanceId(processInstanceId);
	    model.setOwner(ownerId);
	    model.setTitle(title);
	    model.setPriority(priorityType.getValue());
	    model.setExpireTime(expiretime);
	    model.setStatus(status);
	    model.setOwnerDepartmentId(localDepartmentId);

	    this.taskStepNo = activityNo;
	    int pId = 0;
	    if (synType.getValue() == SynType.synchronous.getValue()) {
		pId = rpc.getNextPoint(conn);
		if (pId < 0) {
		    throw new WorkflowException("没有从调度表中得到一个合法的工作流任务指针");
		}
		String uid = (String) myArray.get(0);
		uid = Function.getUID(uid);
		model.setTarget(uid);
		model.setFromPoint(pId);
		try {
		    int taskInstanceId = insertTask(model, isShortMessage);
		    rpc.moveNextPoint(conn);
		    taskInstanceIdList.add(new Integer(taskInstanceId));
		} catch (WorkflowException we) {
		    throw we;
		}
	    } else {
		r = -1;
		for (int i = 0; i < myArray.size(); ++i) {
		    pId = rpc.getNextPoint(conn);
		    if (pId < 0) {
			throw new WorkflowException("没有从调度表中得到一个合法的工作流任务指针");
		    }

		    String uid = (String) myArray.get(i);
		    if (uid.indexOf("<") > -1) {
			uid = uid.substring(0, uid.indexOf("<"));
		    }
		    model.setTarget(uid);
		    model.setFromPoint(pId);
		    try {
			int taskInstanceId = insertTask(model, isShortMessage);
			rpc.moveNextPoint(conn);
			taskInstanceIdList.add(new Integer(taskInstanceId));
		    } catch (WorkflowException we) {
			we.printStackTrace(System.err);
			throw we;
		    }
		}

	    }

	    int[] taskInstanceArray = new int[taskInstanceIdList.size()];
	    for (int i = 0; i < taskInstanceIdList.size(); ++i) {
		int taskInstanceId = ((Integer) taskInstanceIdList.get(i))
			.intValue();
		if (LICENSE.isBPA() && taskInstanceId > 0) {
		    WorkFlowTaskStartCollectorImp taskStartImp = new WorkFlowTaskStartCollectorImp();
		    taskStartImp.setTaskId(taskInstanceId);
		    taskStartImp.collectorData();
		}
		taskInstanceArray[i] = taskInstanceId;
	    }
	    if (taskInstanceArray.length > 0) {
		ProcessRuntimeDaoFactory.createProcessInstance()
			.changeActivityNo(conn, processInstanceId, activityNo);
	    } else
		throw new WorkflowException("没有可向下执行的任务[participant="
			+ participant + "][Next activityNo=" + activityNo + "]");
	    clearRouteORGMapping(processInstanceId);
	    return taskInstanceArray;
	} catch (WorkflowException we) {
	} catch (Exception e) {
	    throw new WorkflowException("插入任务数据时发生未知的异常");
	} finally {
	    DBSql.close(conn, null, null);
	}
	return new int[0];
    }

    public int getUserTaskCountOfWorking(Connection conn, int processInstanceId) {
	try {
	    return DBSql.getInt(conn,
		    "SELECT COUNT(*) C FROM WF_TASK WHERE (STATUS=1 or status=4) AND BIND_ID="
			    + processInstanceId, "C");
	} catch (SQLException e) {
	    e.printStackTrace(System.err);
	}
	return -1;
    }

    public String getUserTargetsOfActivity(int processInstanceId,
	    int activityDefId) {
	String sql = "select * from WF_TASK_LOG WHERE BIND_ID="
		+ processInstanceId + " AND WFSID=" + activityDefId;
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    if (rset.next())
		return rset.getString("TARGET");
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return "";
    }

    public Hashtable getUserTargetsOfProcessInstance(int processInstance) {
	Hashtable list = new UnsyncHashtable();
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	String sql = "SELECT TARGET FROM WF_TASK_LOG WHERE  BIND_ID="
		+ Integer.toString(processInstance);
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    while (rset.next())
		list.put(rset.getString("TARGET"), rset.getString("TARGET"));
	} catch (SQLException e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return list;
    }

    public Hashtable<Integer, TaskInstanceModel> getAllActiveTaskListOfStatus(
	    int stauts) {
	String sql = "SELECT * FROM WF_TASK WHERE status=" + stauts;
	return getActiveTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getAllEndTaskListOfStatus(
	    int stauts) {
	String sql = "SELECT * FROM WF_TASK_LOG WHERE status=" + stauts;
	return getEndTaskListBySQL(sql);
    }

    public TaskInstanceModel getInstanceOfActive(int taskInstanceId) {
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	String sql = "SELECT * FROM WF_TASK WHERE ID=" + taskInstanceId;
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    if (rset.next())
		return record2TaskModel(rset);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return null;
    }

    public TaskInstanceModel getInstanceOfEnd(int taskInstanceId) {
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	String sql = "select * from wf_task_log WHERE id=" + taskInstanceId;
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    if (rset.next())
		return record2TaskLogModel(rset);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return null;
    }

    public TaskInstanceModel getLastEndInstance(int instanceId) {
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	String sql = "select * from wf_task_log WHERE BIND_ID=" + instanceId
		+ " order by id desc";
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    if (rset.next())
		return record2TaskModel(rset);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return null;
    }

    public Hashtable<Integer, TaskInstanceModel> getEndTaskListByProcessInstance(
	    int processInstanceId) {
	String sql = "select * from wf_task_log WHERE  bind_id="
		+ processInstanceId + " order by id asc";
	return getEndTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getActiveTaskListByProcessInstance(
	    int processInstanceId) {
	String sql = "select * from wf_task where bind_id=" + processInstanceId
		+ " order by id asc";
	return getActiveTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getActiveTaskListByProcessGroupName(
	    String targetUID, String processGroupName) {
	String sql = "select * from wf_task where wf_style='"
		+ processGroupName + "' and target='" + targetUID
		+ "' order by begintime desc";
	return getActiveTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getActiveTaskListByTarget(
	    String targetUID) {
	String sql = "select * from wf_task where target='" + targetUID
		+ "' order by begintime desc";
	return getActiveTaskListBySQL(sql);
    }

    public boolean hasNextActivityExec(int taskInstanceId, int processInstanceId) {
	String sql = "select count(*) as c from wf_task_log where BIND_ID="
		+ processInstanceId + " AND STATUS=1 and ID>" + taskInstanceId;
	int c = DBSql.getInt(sql, "c");
	return c > 0;
    }

    public void changeTaskReadTag(int taskInstanceId) {
	DBSql.executeUpdate("UPDATE WF_TASK SET READ_TASK=1,READTIME="
		+ DBSql.getDateDefaultValue() + " WHERE ID=" + taskInstanceId
		+ " AND READ_TASK=0");
    }

    public void changeTaskState(Connection conn, int taskInstanceId, int state) {
	String sql = "update Wf_Task set status=" + state + " where id="
		+ taskInstanceId;
	try {
	    DBSql.executeUpdate(conn, sql);
	} catch (SQLException e) {
	    e.printStackTrace(System.err);
	}
    }

    public void backupTask2Log(int taskInstanceId) {
	TaskInstanceModel taskInstanceModel = getInstanceOfActive(taskInstanceId);
	StringBuilder sql = new StringBuilder();
	sql.append(
		"INSERT INTO WF_TASK_LOG (ID,OWNER,TARGET,TITLE,BEGINTIME,ENDTIME,EXPIRETIME,WORKFLOWTYPE,PRIORITY,FROM_POINT,BIND_ID,WF_STYLE,READ_TASK,OWNER_DPT_ID,STATUS,WFID,WFSID,READTIME,ORGID,DPTID,ROLEID) SELECT ID,OWNER,TARGET,TITLE,BEGINTIME,")
		.append(DBSql.getDateDefaultValue())
		.append(",EXPIRETIME,WORKFLOWTYPE,PRIORITY,FROM_POINT,BIND_ID,WF_STYLE,READ_TASK,OWNER_DPT_ID,STATUS,WFID,WFSID,READTIME,ORGID,DPTID,ROLEID FROM WF_TASK WHERE ID=")
		.append(taskInstanceId);
	int r = DBSql.executeUpdate(sql.toString());

	Calendar beginCalendar = Calendar.getInstance();
	beginCalendar.setTime(new Date(taskInstanceModel.getBeginTime()
		.getTime()));
	long taskCost = CalWorkTimeImp.getInstanceByUid(
		taskInstanceModel.getTarget()).calcWorkingTime(beginCalendar);
	if (taskCost == 0L)
	    taskCost = 1L;
	taskCost = taskCost * 60L * 1000L;
	if (r >= 0) {
	    sql.setLength(0);
	    sql.append("update WF_TASK_LOG set taskcost=").append(taskCost)
		    .append(" where id=").append(taskInstanceId);
	    DBSql.executeUpdate(sql.toString());
	}
    }

    public int getActiveUserTaskCount(String uid) {
	String sql = "select count(*) as c from wf_task where target='" + uid
		+ "'";
	return DBSql.getInt(sql, "c");
    }

    public void removeTaskByProcessInstance(int processInstanceId) {
	String sql = "delete from wf_task where BIND_ID=" + processInstanceId;
	DBSql.executeUpdate(sql);
	sql = "delete from wf_task_log where BIND_ID=" + processInstanceId;
	DBSql.executeUpdate(sql);
    }

    public void removeOtherTaskByTask(int processInstanceId, int taskInstanceId) {
	String sql = "delete from wf_task where BIND_ID=" + processInstanceId
		+ " AND ID!=" + taskInstanceId
		+ " and STATUS != 9 and STATUS != 2";
	DBSql.executeUpdate(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getUserEndTaskListOfCondition(
	    String uid, String whereCase) {
	String sql = "";
	if (whereCase.length() > 0)
	    whereCase = " and " + whereCase;
	sql = "select * from wf_task_log where target='" + uid + "' "
		+ whereCase + " order by endtime desc";
	return getEndTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getUserEndTaskListOfProcessGroupName(
	    String uid, String processGroupName) {
	String sql = "";
	if (processGroupName == null || processGroupName.equals(""))
	    sql = "select * from wf_task_log where target='" + uid
		    + "' and from_point>0 order by endtime desc";
	else {
	    sql = "select * from wf_task_log where target='" + uid
		    + "' and WF_STYLE='" + processGroupName
		    + "' and from_point>0 order by endtime desc";
	}
	return getEndTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getUserEndCCTaskListOfProcessGroupName(
	    String uid, String processGroupName) {
	String sql = "";
	if (processGroupName == null || processGroupName.equals(""))
	    sql = "select * from wf_task_log where target='" + uid
		    + "' and status=2 order by endtime desc";
	else {
	    sql = "select * from wf_task_log where target='" + uid
		    + "' and WF_STYLE='" + processGroupName
		    + "' and status=2 order by endtime desc";
	}
	return getEndTaskListBySQL(sql);
    }

    public Hashtable<Integer, TaskInstanceModel> getUserEndTaskListOfCondition2(
	    String whereCase, int top) {
	String sql = "select * from wf_task_log where " + whereCase
		+ " order by endtime desc";
	return getEndTaskListBySQL(sql, top);
    }

    public Hashtable<Integer, TaskInstanceModel> getUserActiveTaskListOfCondition(
	    String whereCase, int top) {
	String sql = "select * from wf_task where " + whereCase
		+ " order by id desc";
	return getActiveTaskListBySQL(sql, top);
    }

    public Hashtable getEndTaskListBySQL(String sql) {
	return getEndTaskListBySQL(sql, 0);
    }

    public Hashtable getEndTaskListBySQL(String sql, int top) {
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	Hashtable list = new UnsyncHashtable();
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    int count = 0;
	    while (rset.next()) {
		list.put(new Integer(list.size()), record2TaskLogModel(rset));
		count++;
		if (top > 0 && count == top)
		    break;
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return list;
    }

    public Hashtable getActiveTaskListBySQL(String sql) {
	return getActiveTaskListBySQL(sql, 0);
    }

    public Hashtable getActiveTaskListBySQL(String sql, int top) {
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	Hashtable list = new UnsyncHashtable();
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    int count = 0;
	    while (rset.next()) {
		list.put(new Integer(list.size()), record2TaskModel(rset));
		count++;
		if (top > 0 && count == top)
		    break;
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return list;
    }

    public TaskInstanceModel getSystemTaskBySQL(String sql) {
	Connection conn = DBSql.open();
	Statement stmt = null;
	ResultSet rset = null;
	TaskInstanceModel taskInstance = new TaskInstanceModel();
	try {
	    stmt = conn.createStatement();
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    if (rset.next())
		taskInstance = record2TaskModel(rset);
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    DBSql.close(conn, stmt, rset);
	}
	return taskInstance;
    }

    public TaskInstanceModel record2TaskModel(ResultSet rset) throws Exception {
	TaskInstanceModel model = new TaskInstanceModel();
	model.setTitle(rset.getString("TITLE"));
	model.setBeginTime(rset.getTimestamp("BEGINTIME"));
	model.setProcessInstanceId(rset.getInt("BIND_ID"));
	model.setEndTime(rset.getTimestamp("ENDTIME"));
	model.setExpireTime(rset.getInt("EXPIRETIME"));
	model.setFromPoint(rset.getInt("FROM_POINT"));
	model.setId(rset.getInt("ID"));
	model.setOwner(rset.getString("OWNER"));
	model.setPriority(rset.getInt("PRIORITY"));
	model.setReadTask(rset.getInt("READ_TASK"));
	model.setStatus(rset.getInt("STATUS"));
	model.setTarget(rset.getString("TARGET"));
	model.setProcessGroupName(rset.getString("WF_STYLE"));
	model.setProcessDefinitionId(rset.getInt("WFID"));
	model.setActivityDefinitionId(rset.getInt("WFSID"));
	model.setTargetCompanyId(rset.getInt("ORGID"));
	model.setTargetDepartmentId(rset.getInt("DPTID"));
	model.setTargetRoleId(rset.getInt("ROLEID"));
	model.setOwnerDepartmentId(rset.getInt("OWNER_DPT_ID"));
	model.setReadTime(rset.getTimestamp("READTIME"));
	return model;
    }

    public TaskInstanceLogModel record2TaskLogModel(ResultSet rset)
	    throws Exception {
	TaskInstanceLogModel model = new TaskInstanceLogModel();
	model.setTitle(rset.getString("TITLE"));
	model.setBeginTime(rset.getTimestamp("BEGINTIME"));
	model.setProcessInstanceId(rset.getInt("BIND_ID"));
	model.setEndTime(rset.getTimestamp("ENDTIME"));
	model.setExpireTime(rset.getInt("EXPIRETIME"));
	model.setFromPoint(rset.getInt("FROM_POINT"));
	model.setId(rset.getInt("ID"));
	model.setOwner(rset.getString("OWNER"));
	model.setPriority(rset.getInt("PRIORITY"));
	model.setStatus(rset.getInt("STATUS"));
	model.setTarget(rset.getString("TARGET"));
	model.setProcessGroupName(rset.getString("WF_STYLE"));
	model.setProcessDefinitionId(rset.getInt("WFID"));
	model.setActivityDefinitionId(rset.getInt("WFSID"));
	model.setOwnerDepartmentId(rset.getInt("OWNER_DPT_ID"));
	model.setTargetCompanyId(rset.getInt("ORGID"));
	model.setTargetDepartmentId(rset.getInt("DPTID"));
	model.setTargetRoleId(rset.getInt("ROLEID"));
	model.setReadTime(rset.getTimestamp("READTIME"));
	model.setTaskExecuteCost(rset.getDouble("TASKCOST"));
	return model;
    }

    public static void taskNotifyEMAIL(UserContext owner, String participant,
	    String title, int taskInstanceId, int emailType) {
	Thread notifyEmail = new Thread(new NotifyEMAIL(owner, participant,
		title, taskInstanceId, emailType));
	notifyEmail.setName("AWS Process Notify(Email)");
	notifyEmail.start();
    }

    public static void setRouteORGMapping(String uid, int processInstanceId,
	    int companyId, int departmentId, int roleId) {
	if (companyId == 0) {
	    DepartmentModel dModel = (DepartmentModel) DepartmentCache
		    .getModel(departmentId);
	    if (dModel != null)
		companyId = dModel.getCompanyId();
	}
	String data = companyId + "." + departmentId + "." + roleId;
	routeORGMapping.put(processInstanceId + "." + uid, data);
    }

    public static int[] getRouteORGMapping(String uid, int processInstanceId) {
	String token = (String) routeORGMapping.get(processInstanceId + "."
		+ uid);
	if (token == null || token.equals(""))
	    return null;
	int cId = Integer.parseInt(token.substring(0, token.indexOf(".")));
	int dId = Integer.parseInt(new UtilString(token).matchValue(".", "."));
	int rId = Integer.parseInt(token.substring(token.lastIndexOf(".") + 1));
	int[] data = { cId, dId, rId };
	return data;
    }

    public static void clearRouteORGMapping(int processInstanceId) {
	for (Iterator localIterator = routeORGMapping.keySet().iterator(); localIterator
		.hasNext();) {
	    Object k = localIterator.next();
	    String key = (String) k;
	    if (key.indexOf(processInstanceId + ".") == 0)
		routeORGMapping.remove(key);
	}
    }
}