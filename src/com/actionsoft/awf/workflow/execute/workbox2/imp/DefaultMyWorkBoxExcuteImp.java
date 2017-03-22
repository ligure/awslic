package com.actionsoft.awf.workflow.execute.workbox2.imp;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import net.sf.json.JSONObject;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.report.execute.ViewSQLFactory;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceLogModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.awf.workflow.execute.workbox2.MyWorkBoxExcuteAbs;
import com.actionsoft.awf.workflow.execute.workbox2.cache.MyWorkBoxConfig;
import com.actionsoft.awf.workflow.execute.workbox2.model.MyWorkBoxConfigColumnModel;
import com.actionsoft.awf.workflow.execute.workbox2.model.MyWorkBoxConfigModel;
import com.actionsoft.awf.workflow.execute.workbox2.util.MyWorkBoxUtil;
import com.actionsoft.awf.workflow.execute.workbox2.util.TASK_COMPARATOR_BEGINDATE;
import com.actionsoft.awf.workflow.execute.workbox2.util.TASK_COMPARATOR_BEGINDATE_ForTaskLog;
import com.actionsoft.awf.workflow.execute.workbox2.util.TASK_COMPARATOR_CREATEDATE;
import com.actionsoft.awf.workflow.execute.workbox2.util.TASK_COMPARATOR_ENDDATE_ForTaskLog;
import com.actionsoft.awf.workflow.execute.workbox2.util.TaskDataSource;
import com.actionsoft.coe.team.bpa.analysis.util.CoEInitUtil;
import com.actionsoft.deploy.transfer.uuid.UUID;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.i18n.I18nRes;

public class DefaultMyWorkBoxExcuteImp extends MyWorkBoxExcuteAbs {
    public DefaultMyWorkBoxExcuteImp(UserContext userContext) {
	super(userContext);
    }

    public String getWorkBoxWeb(UserContext me, int worklistType) {
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid", getSIDFlag());
	hashTags.put("src", "./login.wf?sid="
		+ super.getContext().getSessionId()
		+ "&cmd=My_WorkBox2_Card&group=&condition=&worklistType="
		+ worklistType);
	hashTags.put("SID_THREAD", "var threadSID='"
		+ super.getContext().getSessionId() + "';");
	return RepleaseKey
		.replace(HtmlModelFactory.getHtmlModel("my_WorkBox2_Main.htm"),
			hashTags);
    }

    public String getWorkListCard(int worklistType, String group,
	    String condition) {
	int groupColumn = 0;
	StringBuilder AWS_GRID_CM_DIY = new StringBuilder();
	MyWorkBoxConfigModel configMainModel = MyWorkBoxConfig
		.getMainConfigModel("com.actionsoft.awf.workflow.execute.workbox2.imp.DefaultMyWorkBoxExcuteImp");

	if (worklistType != 7) {
	    AWS_GRID_CM_DIY.append(
		    "var AWS_GRID_CM_DIY = new Ext.grid.ColumnModel([").append(
		    "\n");
	    if (worklistType == 0)
		AWS_GRID_CM_DIY.append(
			"new Ext.grid.RowNumberer(),AWS_GRID_CHECK,").append(
			"\n");
	    else
		AWS_GRID_CM_DIY.append("new Ext.grid.RowNumberer(),").append(
			"\n");

	    for (int j = 1; j < 15; ++j) {
		MyWorkBoxConfigColumnModel configModel = MyWorkBoxConfig
			.getColConfigModel(
				"com.actionsoft.awf.workflow.execute.workbox2.imp.DefaultMyWorkBoxExcuteImp",
				j, worklistType);
		groupColumn = configModel != null
			&& configModel.getColID().equals(group) ? j
			: groupColumn;
		String header = "";
		if (configModel == null) {
		    AWS_GRID_CM_DIY
			    .append("{header: '', width: 0, dataIndex: 'COL"
				    + j
				    + "',hidden:true,sortable: true, fixed:false}");
		} else if (worklistType == 0) {
		    String lang = new UtilString(super.getContext()
			    .getSessionId()).matchValue("L{", "}L");

		    if (configModel.getName().trim()
			    .equals("lang:aws.common.worklist.task.title")
			    || configModel
				    .getName()
				    .trim()
				    .equals("lang:aws.common.worklist.task.TimeLimit")
			    || configModel
				    .getName()
				    .trim()
				    .equals("lang:aws.common.worklist.task.from")
			    || configModel
				    .getName()
				    .trim()
				    .equals("lang:aws.common.worklist.task.accept_date"))
			header = MyWorkBoxUtil.getLangName(lang, configModel
				.getName().trim());
		    else {
			header = I18nRes.findValue(super.getContext()
				.getLanguage(), configModel.getName().trim());
		    }
		    String title = I18nRes.findValue(super.getContext()
			    .getLanguage(), "标题");
		    if (header.equals(title) || configModel.isHidden())
			AWS_GRID_CM_DIY.append("{header: '" + header
				+ "',renderer: setColBg, width: "
				+ configModel.getLength() + ", dataIndex: 'COL"
				+ j + "',hidden:" + configModel.isHidden()
				+ ",sortable: true, fixed:false}");
		    else {
			AWS_GRID_CM_DIY.append("{header: '" + header
				+ "',renderer: setColBg, width: "
				+ configModel.getLength() + ", dataIndex: 'COL"
				+ j + "',hidden:" + configModel.isHidden()
				+ ",sortable: true, fixed:true}");
		    }

		} else {
		    String lang = new UtilString(super.getContext()
			    .getSessionId()).matchValue("L{", "}L");
		    if (configModel.getName().trim()
			    .equals("lang:aws.common.worklist.task.title")
			    || configModel
				    .getName()
				    .trim()
				    .equals("lang:aws.common.worklist.task.TimeLimit")
			    || configModel
				    .getName()
				    .trim()
				    .equals("lang:aws.common.worklist.task.from")
			    || configModel
				    .getName()
				    .trim()
				    .equals("lang:aws.common.worklist.task.accept_date"))
			header = MyWorkBoxUtil.getLangName(lang, configModel
				.getName().trim());
		    else {
			header = I18nRes.findValue(super.getContext()
				.getLanguage(), configModel.getName().trim());
		    }
		    String hd = MyWorkBoxUtil.getLangName(lang, configModel
			    .getName().trim());
		    int width = configModel.getLength();
		    if (hd.equals("收回")) {
			width += 10;
		    }

		    if (header.equals("标题") || configModel.isHidden())
			AWS_GRID_CM_DIY.append("{header: '"
				+ I18nRes.findValue(getContext().getLanguage(),
					hd) + "', width: " + width
				+ ", dataIndex: 'COL" + j + "',hidden:"
				+ configModel.isHidden()
				+ ",sortable: true, fixed:false}");
		    else {
			AWS_GRID_CM_DIY.append("{header: '"
				+ I18nRes.findValue(getContext().getLanguage(),
					hd) + "', width: " + width
				+ ", dataIndex: 'COL" + j + "',hidden:"
				+ configModel.isHidden()
				+ ",sortable: true, fixed:true}");
		    }
		}

		AWS_GRID_CM_DIY.append(",").append("\n");
	    }
	    AWS_GRID_CM_DIY
		    .append("{header: '', width: 0, dataIndex: 'COLEXT1',hidden:true,sortable: true, fixed:true}");

	    AWS_GRID_CM_DIY.append(" ]);").append("\n");
	} else {
	    int select_workflow = 0;
	    try {
		select_workflow = WorkflowEngine.getInstance()
			.getWorkflowDefId("84e9f6ef264805cc5efd32705f653f33");
	    } catch (WorkflowException e) {
		e.printStackTrace(System.err);
	    }

	    int stepNo = (configMainModel.isCreateCommisionFlow()) ? 1 : 1;
	    AWS_GRID_CM_DIY.append("var select_workflow=" + select_workflow)
		    .append("; var stepNo=").append(stepNo).append(";");
	}
	String js = "my_WorkBox2_WorkList.js";
	if (worklistType == 7)
	    js = "my_WorkBox2_WorkList_ApplyCommision.js";
	else if (group.trim().length() > 0 && groupColumn > 0) {
	    js = "my_WorkBox2_WorkList_Group.js";
	}
	int myTaskCount = TaskDataSource.getAlltaskCount(super.getContext()
		.getUID(),
		" and (status=1 or status=3 or status=11 or status=4) ");
	int myNoticeCount = TaskDataSource.getAlltaskCount(super.getContext()
		.getUID(), " and (status=2 or status=9) ");
	Hashtable hashTags = new UnsyncHashtable();
	hashTags.put("sid", getSIDFlag());
	hashTags.put("js", js);
	hashTags.put("Group_Column", "var group_Column='COL" + groupColumn
		+ "';");
	hashTags.put("group", group);
	hashTags.put("condition", condition);
	hashTags.put("worklistType", worklistType);
	hashTags.put("AWS_GRID_CM_DIY", AWS_GRID_CM_DIY.toString());
	String isShowCoeBorad = "none";
	if (LICENSE.isBPA() && CoEInitUtil.isCoeInitEnd()) {
	    isShowCoeBorad = "";
	}

	hashTags.put("isShowCoeBorad", isShowCoeBorad);
	hashTags.put("SID_THREAD", "var threadSID='"
		+ super.getContext().getSessionId() + "'; \nvar butHide="
		+ (worklistType != 0) + "; \nvar worklistType='" + worklistType
		+ "'; \nvar pageSize=" + configMainModel.getPageSize()
		+ "; \nvar myTaskCount=" + myTaskCount
		+ "; \nvar myNoticeCount=" + myNoticeCount + ";");
	return RepleaseKey
		.replace(HtmlModelFactory.getHtmlModel("my_WorkBox2_Card.htm"),
			hashTags);
    }

    public String getWorkListXMLData(int worklistType, int start, int limit,
	    String sqlCondition) {
	String sql = getSqlCondition(sqlCondition, worklistType);

	if (worklistType == 0 || worklistType == 5) {
	    return getMyTaskWorkList(worklistType, sql, start, limit);
	}

	if (worklistType == 3 || worklistType == 1) {
	    return getMyFinishTaskWorkList(worklistType, sql, start, limit);
	}

	if (worklistType == 2) {
	    return getMyCreateTaskWorkList(worklistType, sql, start, limit);
	}
	return "";
    }

    public String getSqlCondition(String sqlCondition, int worklistType) {
	if ("".equals(sqlCondition)) {
	    return sqlCondition;
	}

	if (sqlCondition.indexOf("AWS_FuzzyQuery=") == 0) {
	    return sqlCondition;
	}

	String sql = "";

	JSONObject jsonObj = JSONObject.fromObject(sqlCondition);

	if (!"".equals(jsonObj.getString("searchTite"))) {
	    sql = sql + " and title like '%" + jsonObj.getString("searchTite")
		    + "%'";
	}
	if (worklistType != 2 && !"".equals(jsonObj.getString("searchFrom"))) {
	    String tmp = jsonObj.getString("searchFrom");
	    String[] names = tmp.split(" ");
	    String condition = "";
	    for (int i = 0; i < names.length; ++i) {
		String name = names[i];
		if (name.indexOf('<') > 0)
		    condition = condition + "'"
			    + name.substring(0, name.indexOf("<")) + "',";
		else {
		    condition = condition + "'" + name + "',";
		}
	    }
	    condition = "(" + condition.substring(0, condition.length() - 1)
		    + ")";

	    sql = sql + " and owner in " + condition;
	}

	if (!"".equals(jsonObj.getString("searchDateFrom"))) {
	    if (worklistType == 2)
		sql = sql + " and create_date>="
			+ jsonObj.getString("searchDateFrom");
	    else {
		sql = sql + " and begintime>="
			+ jsonObj.getString("searchDateFrom");
	    }
	}

	if (!"".equals(jsonObj.getString("searchDateEnd"))) {
	    if (worklistType == 2)
		sql = sql + " and create_date<="
			+ jsonObj.getString("searchDateEnd");
	    else {
		sql = sql + " and begintime<="
			+ jsonObj.getString("searchDateEnd");
	    }
	}

	if (!"".equals(jsonObj.getString("searchFounderMember"))) {
	    if (worklistType == 2)
		sql = sql + " and CREATE_USER like '%"
			+ jsonObj.getString("searchFounderMember") + "%'";
	    else {
		sql = sql
			+ " and bind_id in(select id from wf_messagedata where CREATE_USER like '%"
			+ jsonObj.getString("searchFounderMember") + "%') ";
	    }
	}
	return sql;
    }

    private String getMyTaskWorkList(int worklistType, String sqlCondition,
	    int start, int limit) {
	sqlCondition = MyWorkBoxUtil.getMyTaskSQLConditin(sqlCondition,
		worklistType);
	sqlCondition = worklistType != 0 ? " and (status=2 or status=9) "
		+ sqlCondition
		: " and (status=1 or status=3 or status=11 or status=4) "
			+ sqlCondition;
	StringBuilder xml = new StringBuilder();
	xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
	xml.append("<GridResponse>").append("\n");
	xml.append("<OperationRequest>\n");
	xml.append("<RequestId>" + UUID.getDefault().getNextUID()
		+ "</RequestId>\n");
	xml.append("<RequestProcessingTime>" + System.currentTimeMillis()
		+ "</RequestProcessingTime>\n");
	xml.append("<Arguments>\n");
	xml.append("<Argument Name=\"Services\" Value=\"AWS MyTaskList\"></Argument>\n");
	xml.append("<Argument Name=\"Author\" Value=\"Actionsoft Co.,LTD\"></Argument>\n");
	xml.append("<Argument Name=\"Version\" Value=\"5.0\"></Argument>\n");
	xml.append("</Arguments>\n");
	xml.append("</OperationRequest>\n");
	Vector myTaskList = TaskDataSource.getAllTaskList(super.getContext()
		.getUID(), sqlCondition);

	Collections.sort(myTaskList, new TASK_COMPARATOR_BEGINDATE());
	int sumData = myTaskList.size();
	xml.append("  <Results>").append(sumData).append("</Results> ")
		.append("\n");
	xml.append("<Items>").append("\n");

	for (int i = start; myTaskList != null && i < myTaskList.size()
		&& i < start + limit; ++i) {
	    TaskInstanceModel model = (TaskInstanceModel) myTaskList.get(i);
	    xml.append("<Item>").append("\n");
	    String id = !model.getExt3().equals("2")
		    && !model.getExt3().equals("3") ? String.valueOf(model
		    .getId()) : model.getId() + "_" + i;
	    xml.append("<ID>").append(id).append("</ID>").append("\n");
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    model.getProcessInstanceId());
	    xml.append("<ItemAttributes>").append("\n");
	    for (int j = 1; j < 15; ++j) {
		MyWorkBoxConfigColumnModel configModel = MyWorkBoxConfig
			.getColConfigModel(
				"com.actionsoft.awf.workflow.execute.workbox2.imp.DefaultMyWorkBoxExcuteImp",
				j, worklistType);
		xml.append("<COL" + j + "><![CDATA[")
			.append(MyWorkBoxUtil.getMyTaskUI(super.getContext(),
				configModel, model, processInstanceModel))
			.append("]]></COL" + j + ">").append("\n");
	    }
	    String ISBatchTask = "0";
	    if (model.getExt3().equals("1")) {
		WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache
			.getModel(model.getActivityDefinitionId());
		ISBatchTask = stepModel != null && stepModel._isBatchExecTask
			&& model.getStatus() == 1 ? "1" : "0";
	    }
	    xml.append("<COLEXT1><![CDATA[").append(ISBatchTask)
		    .append("]]></COLEXT1>").append("\n");
	    xml.append("</ItemAttributes>").append("\n");
	    xml.append("</Item>").append("\n");
	}
	xml.append("</Items>").append("\n");
	xml.append(" </GridResponse>").append("\n");

	return xml.toString();
    }

    private String getMyFinishTaskWorkList(int worklistType,
	    String sqlCondition, int start, int limit) {
	sqlCondition = MyWorkBoxUtil.getMyTaskSQLConditin(sqlCondition,
		worklistType);
	sqlCondition = worklistType != 1 ? " and (status=2 or status=9) "
		+ sqlCondition
		: " and (status=1 or status=3 or status=11 or status=4) "
			+ sqlCondition;
	StringBuilder xml = new StringBuilder();
	String sql = "select * from wf_task_log where target='"
		+ super.getContext().getUID() + "' " + sqlCondition
		+ " order by endtime desc,id desc";
	int sumData = DBSql.getInt(
		"select count(id) as c from (select * from wf_task_log where target='"
			+ super.getContext().getUID() + "' " + sqlCondition
			+ ") t", "c");
	sql = ViewSQLFactory.getViewSQL(sql, start, limit);
	xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
	xml.append("<GridResponse>").append("\n");
	xml.append("<OperationRequest>\n");
	xml.append("<RequestId>" + UUID.getDefault().getNextUID()
		+ "</RequestId>\n");
	xml.append("<RequestProcessingTime>" + System.currentTimeMillis()
		+ "</RequestProcessingTime>\n");
	xml.append("<Arguments>\n");
	xml.append("<Argument Name=\"Services\" Value=\"AWS MyTaskList\"></Argument>\n");
	xml.append("<Argument Name=\"Author\" Value=\"Actionsoft Co.,LTD\"></Argument>\n");
	xml.append("<Argument Name=\"Version\" Value=\"5.0\"></Argument>\n");
	xml.append("</Arguments>\n");
	xml.append("</OperationRequest>\n");
	Vector myTaskList = TaskDataSource.getAllFinishTaskList(sql);
	if (worklistType == 1)
	    Collections.sort(myTaskList,
		    new TASK_COMPARATOR_ENDDATE_ForTaskLog());
	else {
	    Collections.sort(myTaskList,
		    new TASK_COMPARATOR_BEGINDATE_ForTaskLog());
	}
	xml.append("  <Results>").append(sumData).append("</Results> ")
		.append("\n");
	xml.append("<Items>").append("\n");

	for (int i = 0; myTaskList != null && i < myTaskList.size(); ++i) {
	    TaskInstanceLogModel model = (TaskInstanceLogModel) myTaskList
		    .get(i);
	    xml.append("<Item>").append("\n");
	    xml.append("<ID>").append(model.getId()).append("</ID>")
		    .append("\n");
	    xml.append("<ItemAttributes>").append("\n");
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    model.getProcessInstanceId());
	    for (int j = 1; j < 15; ++j) {
		MyWorkBoxConfigColumnModel configModel = MyWorkBoxConfig
			.getColConfigModel(
				"com.actionsoft.awf.workflow.execute.workbox2.imp.DefaultMyWorkBoxExcuteImp",
				j, worklistType);

		xml.append("<COL" + j + "><![CDATA[")
			.append(MyWorkBoxUtil.getMyFinishTaskUI(
				super.getContext(), configModel, model,
				processInstanceModel))
			.append("]]></COL" + j + ">").append("\n");
	    }
	    xml.append("<COLEXT1><![CDATA[").append("").append("]]></COLEXT1>")
		    .append("\n");
	    xml.append("</ItemAttributes>").append("\n");
	    xml.append("</Item>").append("\n");
	}
	xml.append("</Items>").append("\n");
	xml.append(" </GridResponse>").append("\n");
	return xml.toString();
    }

    private String getMyCreateTaskWorkList(int worklistType,
	    String sqlCondition, int start, int limit) {
	StringBuilder xml = new StringBuilder();
	sqlCondition = MyWorkBoxUtil.getMyCreateTaskSQLConditin(sqlCondition,
		worklistType);
	String sql = "select * from WF_MESSAGEDATA where CREATE_USER='"
		+ super.getContext().getUID()
		+ "' and wfs_no>0 and wf_start=1 " + sqlCondition;
	int sumData = DBSql.getInt(
		"select count(id) as c from (" + sql + ") t", "c");
	sql = sql + " order by id desc";
	sql = ViewSQLFactory.getViewSQL(sql, start, limit);
	xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
	xml.append("<GridResponse>").append("\n");
	xml.append("<OperationRequest>\n");
	xml.append("<RequestId>" + UUID.getDefault().getNextUID()
		+ "</RequestId>\n");
	xml.append("<RequestProcessingTime>" + System.currentTimeMillis()
		+ "</RequestProcessingTime>\n");
	xml.append("<Arguments>\n");
	xml.append("<Argument Name=\"Services\" Value=\"AWS MyTaskList\"></Argument>\n");
	xml.append("<Argument Name=\"Author\" Value=\"Actionsoft Co.,LTD\"></Argument>\n");
	xml.append("<Argument Name=\"Version\" Value=\"5.0\"></Argument>\n");
	xml.append("</Arguments>\n");
	xml.append("</OperationRequest>\n");
	Vector myTaskList = ProcessRuntimeDaoFactory.createProcessInstance()
		.getInstanceBySQL(sql);
	Collections.sort(myTaskList, new TASK_COMPARATOR_CREATEDATE());
	xml.append("  <Results>").append(sumData).append("</Results> ")
		.append("\n");
	xml.append("<Items>").append("\n");

	for (int i = 0; myTaskList != null && i < myTaskList.size(); ++i) {
	    ProcessInstanceModel model = (ProcessInstanceModel) myTaskList
		    .get(i);
	    xml.append("<Item>").append("\n");
	    xml.append("<ID>").append(model.getId()).append("</ID>")
		    .append("\n");
	    xml.append("<ItemAttributes>").append("\n");
	    for (int j = 1; j < 15; ++j) {
		MyWorkBoxConfigColumnModel configModel = MyWorkBoxConfig
			.getColConfigModel(
				"com.actionsoft.awf.workflow.execute.workbox2.imp.DefaultMyWorkBoxExcuteImp",
				j, worklistType);
		xml.append("<COL" + j + "><![CDATA[")
			.append(MyWorkBoxUtil.getMyCreateTaskUI(
				super.getContext(), configModel, model))
			.append("]]></COL" + j + ">").append("\n");
	    }
	    xml.append("<COLEXT1><![CDATA[").append("").append("]]></COLEXT1>")
		    .append("\n");
	    xml.append("</ItemAttributes>").append("\n");
	    xml.append("</Item>").append("\n");
	}
	xml.append("</Items>").append("\n");
	xml.append(" </GridResponse>").append("\n");
	return xml.toString();
    }
}