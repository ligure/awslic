package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.application.portal.ViewAWSHtmlModelWeb;
import com.actionsoft.application.schedule.cache.AWSScheduleCache;
import com.actionsoft.application.schedule.dao.AWSScheduleDaoFactory;
import com.actionsoft.application.schedule.model.AWSScheduleModel;
import com.actionsoft.application.schedule.util.ProcessGroup;
import com.actionsoft.application.schedule.util.SQLGroup;
import com.actionsoft.application.schedule.web.ScheduleDesignWeb;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.application.server.conf.web.ConfigCardWeb;
import com.actionsoft.application.server.conf.web.ConfigTabsWeb;
import com.actionsoft.application.server.conf.xmlvisual.model.ComponentModel;
import com.actionsoft.application.server.conf.xmlvisual.model.FormulaModel;
import com.actionsoft.application.server.conf.xmlvisual.model.SMVendorModel;
import com.actionsoft.application.server.conf.xmlvisual.model.SocketModel;
import com.actionsoft.application.server.conf.xmlvisual.web.XMLVisualConfigWeb;
import com.actionsoft.application.system.runtimemanager.web.DBPoolMonitorWeb;
import com.actionsoft.application.system.runtimemanager.web.ThreadMonitorWeb;
import com.actionsoft.apps.portal.console.MobilePortlet;
import com.actionsoft.awf.commons.expression.web.ExpressionEditorLeftWeb;
import com.actionsoft.awf.commons.expression.web.ExpressionEditorMainWeb;
import com.actionsoft.awf.commons.expression.web.ExpressionEditorTopWeb;
import com.actionsoft.awf.commons.expression.web.ExpressionEditorWeb;
import com.actionsoft.awf.form.execute.plugins.component.FormUIComponentRelatedTaskInstanceWeb;
import com.actionsoft.awf.form.execute.plugins.component.gov.SubjectWord;
import com.actionsoft.awf.form.execute.plugins.component.livesearch.UserQuery;
import com.actionsoft.awf.form.execute.plugins.component.web.UIDisplayEditorWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.system.web.DictionaryAWFUIComponetConfigWeb;
import com.actionsoft.awf.form.execute.plugins.onlinefile.CommonOnlineOfficeFile;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.FlexUpFile;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.calendar.web.WorkingCalendarWeb;
import com.actionsoft.eai.shortmessage.SendSMSWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.loadbalancer.cluster.web.SynchrCommandWeb;
import com.actionsoft.thirdparty.LoginRtxWeb;

public class ToolsSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("Expression_Editor_Open")) {
	    ExpressionEditorMainWeb web = new ExpressionEditorMainWeb(me);
	    String target = myCmdArray.elementAt(3).toString();
	    String target_value = UtilCode.decode(myStr.matchValue(
		    "_textValue[", "]textValue_"));
	    myOut.write(web.getMainWeb(target, target_value));
	    web = null;
	} else if (socketCmd.equals("Expression_Left_Tree_Open")) {
	    ExpressionEditorLeftWeb web = new ExpressionEditorLeftWeb(me);
	    myOut.write(web.getWeb());
	    web = null;
	} else if (socketCmd.equals("Expression_Top_Open")) {
	    ExpressionEditorTopWeb web = new ExpressionEditorTopWeb(me);
	    myOut.write(web.getWeb());
	    web = null;
	} else if (socketCmd.equals("Expression_Right_Editor_Open")) {
	    ExpressionEditorWeb web = new ExpressionEditorWeb(me);
	    String target = myCmdArray.elementAt(3).toString();
	    String target_value = UtilCode.decode(myStr.matchValue(
		    "_textValue[", "]textValue_"));
	    myOut.write(web.getWeb(target, target_value));
	    web = null;
	} else if (socketCmd.equals("Display_Editor_Open")) {
	    UIDisplayEditorWeb web = new UIDisplayEditorWeb(me);
	    String target = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    String displayType = myCmdArray.elementAt(5).toString();
	    String metaDataId = myCmdArray.elementAt(6).toString();
	    String target_value = UtilCode.decode(myStr.matchValue(
		    "_textValue[", "]textValue_"));
	    String fieldName = UtilCode.decode(myStr.matchValue("_fieldName[",
		    "]fieldName_"));
	    if (mapId.trim().length() == 0)
		mapId = "0";
	    if (metaDataId.trim().length() == 0)
		metaDataId = "0";
	    myOut.write(web.getWeb(Integer.parseInt(mapId), target,
		    target_value, Integer.parseInt(metaDataId), displayType,
		    fieldName));
	    web = null;
	} else if (socketCmd.equals("Display_Editor_Sql_Analog")) {
	    UIDisplayEditorWeb web = new UIDisplayEditorWeb(me);
	    String dbsource = myCmdArray.elementAt(3).toString();
	    String valueFiled = UtilCode.decode(myStr.matchValue(
		    "_valueFiled[", "]valueFiled_"));
	    String valueDisplayFiled = UtilCode.decode(myStr.matchValue(
		    "_valueDisplayFiled[", "]valueDisplayFiled_"));
	    String valueSql = UtilCode.decode(myStr.matchValue("_valueSql[",
		    "]valueSql_"));
	    String uiType = UtilCode.decode(myStr.matchValue("_uiType[",
		    "]uiType_"));
	    myOut.write(web.getAnalogWeb(dbsource, valueFiled,
		    valueDisplayFiled, valueSql, uiType, me));
	    web = null;
	} else if (socketCmd.equals("Display_Editor_Depart_Search")) {
	    UIDisplayEditorWeb web = new UIDisplayEditorWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String query = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String start = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getDepartSearch(
		    Integer.parseInt(metaDataId.substring(1)), query,
		    Integer.parseInt(limit), Integer.parseInt(start)));
	    web = null;
	} else if (socketCmd.equals("Display_Editor_Dict_Search")) {
	    UIDisplayEditorWeb web = new UIDisplayEditorWeb(me);
	    String limit = myCmdArray.elementAt(3).toString();
	    if (limit.trim().length() == 0)
		limit = "0";
	    String start = myCmdArray.elementAt(4).toString();
	    if (start.trim().length() == 0)
		start = "0";
	    String query = UtilCode.decode(myStr.matchValue("_query[",
		    "]query_"));
	    myOut.write(web.getDictionarySearch(query, Integer.parseInt(limit),
		    Integer.parseInt(start)));
	    web = null;
	} else if (socketCmd.equals("Display_Editor_AC_Value")) {
	    UIDisplayEditorWeb web = new UIDisplayEditorWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getAcValue(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId)));
	    web = null;
	} else if (socketCmd.equals("Display_Editor_SuitIsHad")) {
	    UIDisplayEditorWeb web = new UIDisplayEditorWeb(me);
	    myOut.write(web.getGovSuitIsHad());
	    web = null;
	} else if (socketCmd.equals("AWF_DBP_Page")) {
	    DBPoolMonitorWeb web = new DBPoolMonitorWeb(me);
	    myOut.write(web.getDBPPage());
	    web = null;
	} else if (socketCmd.equals("AWF_ThreadMonitor_Page")) {
	    ThreadMonitorWeb web = new ThreadMonitorWeb(me);
	    myOut.write(web.getThreadPage());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_Card")) {
	    ConfigCardWeb web = new ConfigCardWeb(me);
	    String boxType = myCmdArray.elementAt(3).toString();
	    if (boxType == null || boxType.equals(""))
		boxType = "0";
	    myOut.write(web.getCardPage(Integer.parseInt(boxType)));
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_AWFServer_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getAWFServerTab());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_DataBaseServer_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getDataBaseServerTab());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_WebServer_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getWebServerTab());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_aws_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getiWorkTab());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_Log_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getLogTab());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_Optimize_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getOptimizeTab());
	    web = null;
	} else if (socketCmd.equals("AWF_Configuration_XServer_Open")) {
	    ConfigTabsWeb web = new ConfigTabsWeb(me);
	    myOut.write(web.getXServerTab());
	    web = null;
	} else if (socketCmd.equals("Cluster_Synchr_List")) {
	    SynchrCommandWeb web = new SynchrCommandWeb(me);
	    myOut.write(web.getList());
	    web = null;
	} else if (socketCmd.equals("Cluster_Synchr_Exec")) {
	    SynchrCommandWeb web = new SynchrCommandWeb(me);
	    String list = UtilCode.decode(myStr.matchValue("_synchrList[",
		    "]synchrList_"));
	    myOut.write(web.synchrCommand(list));
	    web = null;
	} else if (socketCmd.equals("AWS_Server_ViewHtmlModel")) {
	    ViewAWSHtmlModelWeb web = new ViewAWSHtmlModelWeb(me);
	    String modelName = UtilCode.decode(myStr.matchValue("_modelName[",
		    "]modelName_"));
	    myOut.write(web.viewHtmlPage(modelName));
	    web = null;
	} else if (socketCmd.equals("SMS_API_SendMessageWindow")) {
	    SendSMSWeb web = new SendSMSWeb(me);
	    String AWS_SM_MOBILE_TMP = UtilCode.decode(myStr.matchValue(
		    "_AWS_SM_MOBILE_TMP[", "]AWS_SM_MOBILE_TMP_"));
	    String AWS_SM_CONTENT_TMP = UtilCode.decode(myStr.matchValue(
		    "_AWS_SM_CONTENT_TMP[", "]AWS_SM_CONTENT_TMP_"));
	    myOut.write(web.getSendSMDialog(AWS_SM_MOBILE_TMP,
		    AWS_SM_CONTENT_TMP));
	    web = null;
	} else if (socketCmd.equals("SMS_API_SendMessage")) {
	    SendSMSWeb web = new SendSMSWeb(me);
	    String AWS_SM_MOBILE = UtilCode.decode(myStr.matchValue(
		    "_AWS_SM_MOBILE[", "]AWS_SM_MOBILE_"));
	    String AWS_SM_CONTENT = UtilCode.decode(myStr.matchValue(
		    "_AWS_SM_CONTENT[", "]AWS_SM_CONTENT_"));
	    myOut.write(web.sendSM(AWS_SM_MOBILE, AWS_SM_CONTENT));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_List")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    myOut.write(web.getList());
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Json")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    myOut.write(web.getSchedulesAsJson());
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_LogJson")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String schedules = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getScheduleLogsAsJson(schedules));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Remove")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String task = myCmdArray.elementAt(3).toString();
	    myOut.write(web.deleteSchedules(task));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Schedule")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String scheduleId = myCmdArray.elementAt(3).toString();
	    String isSchedule = myCmdArray.elementAt(4).toString();
	    myOut.write(web.schedule(scheduleId, isSchedule));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Check_Cron")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String cron = UtilCode.decode(myStr.matchValue("_cron[", "]cron_"));
	    myOut.write(web.checkCronException(cron));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_SetDisable")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String scheduleId = myCmdArray.elementAt(3).toString();
	    String beDisable = myCmdArray.elementAt(4).toString();
	    myOut.write(web.setDisable(scheduleId, beDisable));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_SetSystem")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String scheduleId = myCmdArray.elementAt(3).toString();
	    String beSystem = myCmdArray.elementAt(4).toString();
	    myOut.write(web.setSystem(scheduleId, beSystem));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Pause")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String scheduleId = myCmdArray.elementAt(3).toString();
	    String isSchedule = myCmdArray.elementAt(4).toString();
	    myOut.write(web.pauseSchedule(scheduleId, isSchedule));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Interrupt")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String scheduleId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.interruptSchedule(scheduleId));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Open")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String planId = myCmdArray.elementAt(3).toString();
	    boolean isNew = "".equals(planId) || "0".equals(planId);
	    AWSScheduleModel scheduleModel = null;
	    if (isNew) {
		scheduleModel = new AWSScheduleModel();
		scheduleModel.setId(0);
		String jobType = myCmdArray.elementAt(4).toString();
		scheduleModel.setGroup(jobType);
	    } else {
		scheduleModel = (AWSScheduleModel) AWSScheduleCache
			.getModel(Integer.parseInt(planId));
	    }
	    myOut.write(web.getSchedulePage(scheduleModel));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Open_Process")) {
	    ProcessGroup web = new ProcessGroup();
	    String planId = myCmdArray.elementAt(3).toString();
	    boolean isNew = "".equals(planId) || "0".equals(planId);
	    AWSScheduleModel scheduleModel = null;
	    if (isNew) {
		scheduleModel = new AWSScheduleModel();
		scheduleModel.setId(0);
		scheduleModel.setGroup("PROCESS");
	    } else {
		scheduleModel = (AWSScheduleModel) AWSScheduleCache
			.getModel(Integer.parseInt(planId));
	    }
	    String processUuid = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getSchedulePage(me, scheduleModel, processUuid));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Process_FiledList")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String wfUuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getProcessFiled(wfUuid));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Save")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    AWSScheduleModel model = null;
	    String planId = myCmdArray.elementAt(3).toString();
	    boolean isCreate = "".equals(planId) || "0".equals(planId);
	    if (isCreate)
		model = new AWSScheduleModel();
	    else
		model = (AWSScheduleModel) AWSScheduleCache.getModel(Integer
			.parseInt(planId));
	    model.setSystem("1".equals(myCmdArray.elementAt(4).toString()));
	    model.setDisabled("1".equals(myCmdArray.elementAt(5).toString()));
	    model.setGroup(myCmdArray.elementAt(6).toString());
	    model.setName(UtilCode.decode(myStr.matchValue("_name[", "]name_")));
	    String cz = UtilCode.decode(myStr
		    .matchValue("_classz[", "]classz_"));
	    if (model.getGroup().equals("webservice"))
		model.setClassz(cz.replaceAll("__eol__", "\r\n"));
	    else if ("SQL".equals(model.getGroup()))
		model.setClassz(SQLGroup.json2DB(cz));
	    else
		model.setClassz(cz);
	    model.setUserParam(myStr.matchValue("_userParam[", "]userParam_"));
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    model.setDesc(desc);
	    String triggerRule = UtilCode.decode(myStr.matchValue(
		    "_triggerRule[", "]triggerRule_"));
	    model.setTriggerRule(triggerRule);
	    model.setCreateUser(me.getUID());
	    model.setCreateDate(new Date());
	    if (isCreate) {
		int id = AWSScheduleDaoFactory.creatAWSSchedule().create(model);
		model.setId(id <= 0 ? 0 : id);
	    } else {
		AWSScheduleDaoFactory.creatAWSSchedule().store(model);
	    }
	    MessageQueue.getInstance().putMessage(me.getUID(),
		    I18nRes.findValue(me.getLanguage(), "已保存"), true);
	    myOut.write(web.getSchedulePage(model));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Apply")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    AWSScheduleModel model = null;
	    String planId = myCmdArray.elementAt(3).toString();
	    boolean isCreate = "".equals(planId) || "0".equals(planId);
	    if (isCreate)
		model = new AWSScheduleModel();
	    else
		model = (AWSScheduleModel) AWSScheduleCache.getModel(Integer
			.parseInt(planId));
	    model.setSystem("1".equals(myCmdArray.elementAt(4).toString()));
	    model.setDisabled("1".equals(myCmdArray.elementAt(5).toString()));
	    model.setGroup(myCmdArray.elementAt(6).toString());
	    model.setName(UtilCode.decode(myStr.matchValue("_name[", "]name_")));
	    String cz = UtilCode.decode(myStr
		    .matchValue("_classz[", "]classz_"));
	    if (model.getGroup().equals("webservice"))
		model.setClassz(cz.replaceAll("__eol__", "\r\n"));
	    else if ("SQL".equals(model.getGroup()))
		model.setClassz(SQLGroup.json2DB(cz));
	    else
		model.setClassz(cz);
	    model.setUserParam(myStr.matchValue("_userParam[", "]userParam_"));
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    model.setDesc(desc);
	    String triggerRule = UtilCode.decode(myStr.matchValue(
		    "_triggerRule[", "]triggerRule_"));
	    model.setTriggerRule(triggerRule);
	    model.setCreateUser(me.getUID());
	    model.setCreateDate(new Date());
	    if (isCreate) {
		int id = AWSScheduleDaoFactory.creatAWSSchedule().create(model);
		model.setId(id <= 0 ? 0 : id);
	    } else {
		AWSScheduleDaoFactory.creatAWSSchedule().store(model);
	    }
	    web.applySchedule(model, isCreate);
	    myOut.write(web.getSchedulePage(model));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_Check_Class")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String classzInfo = UtilCode.decode(myStr.matchValue(
		    "_classzInfo[", "]classzInfo_"));
	    myOut.write(web.checkClassz(classzInfo, type));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_Schedule_ViweLog")) {
	    ScheduleDesignWeb web = new ScheduleDesignWeb(me);
	    String ids = myCmdArray.elementAt(3).toString();
	    String result = web.getLogList(ids);
	    myOut.write(result);
	    web = null;
	} else if (socketCmd.equals("RelatedTaskUI_List")) {
	    FormUIComponentRelatedTaskInstanceWeb web = new FormUIComponentRelatedTaskInstanceWeb(
		    me);
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String action = myCmdArray.elementAt(4).toString();
	    String mapUUID = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getList(Integer.parseInt(processInstanceId),
		    action, mapUUID));
	    web = null;
	} else if (socketCmd.equals("RelatedTaskUI_Append")) {
	    FormUIComponentRelatedTaskInstanceWeb web = new FormUIComponentRelatedTaskInstanceWeb(
		    me);
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String mapUUID = myCmdArray.elementAt(5).toString();
	    myOut.write(web.appendTask(Integer.parseInt(processInstanceId),
		    Integer.parseInt(taskId), mapUUID));
	    web = null;
	} else if (socketCmd.equals("RelatedTaskUI_Remove")) {
	    FormUIComponentRelatedTaskInstanceWeb web = new FormUIComponentRelatedTaskInstanceWeb(
		    me);
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String mapUUID = myCmdArray.elementAt(5).toString();
	    myOut.write(web.removeTask(Integer.parseInt(processInstanceId),
		    Integer.parseInt(taskId), mapUUID));
	    web = null;
	} else if (socketCmd.equals("OnlineFile_Modify_Page")) {
	    CommonOnlineOfficeFile web = new CommonOnlineOfficeFile(me);
	    String rootDir = myCmdArray.elementAt(3).toString();
	    String dir1 = myCmdArray.elementAt(4).toString();
	    String dir2 = myCmdArray.elementAt(5).toString();
	    String editType = myCmdArray.elementAt(6).toString();
	    String copyType = myCmdArray.elementAt(7).toString();
	    String printType = myCmdArray.elementAt(8).toString();
	    String fileName = UtilCode.decode(myStr.matchValue("_fileName[",
		    "]fileName_"));
	    String webOfficeImpl = UtilCode.decode(myStr.matchValue(
		    "_webofficeImpl[", "]webofficeImpl_"));
	    myOut.write(web.getModifyWindow(rootDir, dir1, dir2, fileName,
		    editType, copyType, printType, webOfficeImpl));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSListener")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-listener.xml");
	    String bpmHostName = UtilCode.decode(myStr.matchValue(
		    "_BPMHostName[", "]BPMHostName_"));
	    myOut.write(web.getMainPage(bpmHostName, socketCmd));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSListenerGetList")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-listener.xml");
	    myOut.write(web.getModelList());
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSListener_ModifyPage")) {
	    String itemName = myCmdArray.elementAt(3).toString();
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-listener.xml");
	    SocketModel model = (SocketModel) web.getModel(itemName);
	    if (itemName.equals(""))
		model = null;
	    myOut.write(web.getModifyPage(model));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSListener_SaveAndEffective")) {
	    String className = myCmdArray.elementAt(3).toString();
	    String session = myCmdArray.elementAt(4).toString();
	    String originalName = myCmdArray.elementAt(5).toString();
	    String flag = myCmdArray.elementAt(6).toString();
	    String bpmHostName = UtilCode.decode(myStr.matchValue(
		    "_BPMHostName[", "]BPMHostName_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-listener.xml");
	    SocketModel model = new SocketModel();
	    model.setClassName(className);
	    model.setSession(session);
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, false));
	    else
		myOut.write(web.saveModel(model, originalName, 1, false));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSListener_ClusterAndEffective")) {
	    String className = myCmdArray.elementAt(3).toString();
	    String session = myCmdArray.elementAt(4).toString();
	    String originalName = myCmdArray.elementAt(5).toString();
	    String flag = myCmdArray.elementAt(6).toString();
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-listener.xml");
	    SocketModel model = new SocketModel();
	    model.setClassName(className);
	    model.setSession(session);
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, true));
	    else
		myOut.write(web.saveModel(model, originalName, 1, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSListener_Remove")) {
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-listener.xml");
	    myOut.write(web.removeModel(idList, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSRule")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-rule.xml");
	    String bpmHostName = UtilCode.decode(myStr.matchValue(
		    "_BPMHostName[", "]BPMHostName_"));
	    myOut.write(web.getMainPage(bpmHostName, socketCmd));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSRuleGetList")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-rule.xml");
	    myOut.write(web.getModelList());
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSRule_ModifyPage")) {
	    String itemName = myCmdArray.elementAt(3).toString();
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-rule.xml");
	    FormulaModel model = (FormulaModel) web.getModel(itemName);
	    if (itemName.equals(""))
		model = null;
	    myOut.write(web.getModifyPage(model));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSRule_SaveAndEffective")) {
	    String groupName = myCmdArray.elementAt(3).toString();
	    String itemId = myCmdArray.elementAt(4).toString();
	    String title = myCmdArray.elementAt(5).toString();
	    String interfaceClass = myCmdArray.elementAt(6).toString();
	    String implementsClass = myCmdArray.elementAt(7).toString();
	    String syntax = myCmdArray.elementAt(8).toString();
	    String originalName = myCmdArray.elementAt(9).toString();
	    String flag = myCmdArray.elementAt(10).toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-rule.xml");
	    FormulaModel model = new FormulaModel();
	    model.setGroupName(groupName);
	    model.setId(itemId);
	    model.setTitle(title);
	    model.setInterfaceClass(interfaceClass);
	    model.setImplementsClass(implementsClass);
	    model.setSyntax(syntax);
	    model.setDesc(new UtilString(desc).replace("__eol__", ""));
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, false));
	    else
		myOut.write(web.saveModel(model, originalName, 1, false));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSRule_ClusterAndEffective")) {
	    String groupName = myCmdArray.elementAt(3).toString();
	    String itemId = myCmdArray.elementAt(4).toString();
	    String title = myCmdArray.elementAt(5).toString();
	    String interfaceClass = myCmdArray.elementAt(6).toString();
	    String implementsClass = myCmdArray.elementAt(7).toString();
	    String syntax = myCmdArray.elementAt(8).toString();
	    String originalName = myCmdArray.elementAt(9).toString();
	    String flag = myCmdArray.elementAt(10).toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-rule.xml");
	    FormulaModel model = new FormulaModel();
	    model.setGroupName(groupName);
	    model.setId(itemId);
	    model.setTitle(title);
	    model.setInterfaceClass(interfaceClass);
	    model.setImplementsClass(implementsClass);
	    model.setSyntax(syntax);
	    model.setDesc(new UtilString(desc).replace("__eol__", ""));
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, true));
	    else
		myOut.write(web.saveModel(model, originalName, 1, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSRule_Remove")) {
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-rule.xml");
	    myOut.write(web.removeModel(idList, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSComponent")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-eform-component.xml");
	    String bpmHostName = UtilCode.decode(myStr.matchValue(
		    "_BPMHostName[", "]BPMHostName_"));
	    myOut.write(web.getMainPage(bpmHostName, socketCmd));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSComponentGetList")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-eform-component.xml");
	    myOut.write(web.getModelList());
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSComponent_ModifyPage")) {
	    String itemName = myCmdArray.elementAt(3).toString();
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-eform-component.xml");
	    ComponentModel model = (ComponentModel) web.getModel(itemName);
	    if (itemName.equals(""))
		model = null;
	    myOut.write(web.getModifyPage(model));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSComponent_SaveAndEffective")) {
	    String groupName = myCmdArray.elementAt(3).toString();
	    String itemId = myCmdArray.elementAt(4).toString();
	    String title = myCmdArray.elementAt(5).toString();
	    String interfaceClass = myCmdArray.elementAt(6).toString();
	    String implementsClass = myCmdArray.elementAt(7).toString();
	    String originalName = myCmdArray.elementAt(8).toString();
	    String flag = myCmdArray.elementAt(9).toString();
	    String setIcon = myCmdArray.elementAt(10).toString();
	    String setDisplayWidth = myCmdArray.elementAt(11).toString();
	    String setInputWidth = myCmdArray.elementAt(12).toString();
	    String setInputHeight = myCmdArray.elementAt(13).toString();
	    String setHtmlInner = myCmdArray.elementAt(14).toString();
	    String setting = myCmdArray.elementAt(15).toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-eform-component.xml");
	    ComponentModel model = new ComponentModel();
	    model.setGroupName(groupName);
	    model.setId(itemId);
	    model.setTitle(title);
	    model.setInterfaceClass(interfaceClass);
	    model.setImplementsClass(implementsClass);
	    model.setSetDisplayWidth(setDisplayWidth.equals("1"));
	    model.setSetInputHeight(setInputHeight.equals("1"));
	    model.setSetInputWidth(setInputWidth.equals("1"));
	    model.setSetHtmlInner(setHtmlInner.equals("1"));
	    model.setSetting(setting.equals("1"));
	    model.setSeticon(setIcon);
	    model.setDesc(new UtilString(desc).replace("__eol__", ""));
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, false));
	    else
		myOut.write(web.saveModel(model, originalName, 1, false));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSComponent_ClusterAndEffective")) {
	    String groupName = myCmdArray.elementAt(3).toString();
	    String itemId = myCmdArray.elementAt(4).toString();
	    String title = myCmdArray.elementAt(5).toString();
	    String interfaceClass = myCmdArray.elementAt(6).toString();
	    String implementsClass = myCmdArray.elementAt(7).toString();
	    String originalName = myCmdArray.elementAt(8).toString();
	    String flag = myCmdArray.elementAt(9).toString();
	    String setIcon = myCmdArray.elementAt(10).toString();
	    String setDisplayWidth = myCmdArray.elementAt(11).toString();
	    String setInputWidth = myCmdArray.elementAt(12).toString();
	    String setInputHeight = myCmdArray.elementAt(13).toString();
	    String setHtmlInner = myCmdArray.elementAt(14).toString();
	    String setting = myCmdArray.elementAt(15).toString();
	    String setAltText = myCmdArray.elementAt(16).toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-eform-component.xml");
	    ComponentModel model = new ComponentModel();
	    model.setGroupName(groupName);
	    model.setId(itemId);
	    model.setTitle(title);
	    model.setInterfaceClass(interfaceClass);
	    model.setImplementsClass(implementsClass);
	    model.setSetDisplayWidth(setDisplayWidth.equals("1"));
	    model.setSetInputHeight(setInputHeight.equals("1"));
	    model.setSetInputWidth(setInputWidth.equals("1"));
	    model.setSetHtmlInner(setHtmlInner.equals("1"));
	    model.setSetting(setting.equals("1"));
	    model.setSeticon(setIcon);
	    model.setSetAltText(setAltText.equals("1"));
	    model.setDesc(new UtilString(desc).replace("__eol__", ""));
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, true));
	    else
		myOut.write(web.saveModel(model, originalName, 1, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSComponent_Remove")) {
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-eform-component.xml");
	    myOut.write(web.removeModel(idList, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSSMVendor")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-sm-vendor.xml");
	    String bpmHostName = UtilCode.decode(myStr.matchValue(
		    "_BPMHostName[", "]BPMHostName_"));
	    myOut.write(web.getMainPage(bpmHostName, socketCmd));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSSMVendorGetList")) {
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-sm-vendor.xml");
	    myOut.write(web.getModelList());
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSSMVendor_ModifyPage")) {
	    String itemName = myCmdArray.elementAt(3).toString();
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-sm-vendor.xml");
	    SMVendorModel model = (SMVendorModel) web.getModel(itemName);
	    if (itemName.equals(""))
		model = null;
	    myOut.write(web.getModifyPage(model));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSSMVendor_SaveAndEffective")) {
	    String vendorName = myCmdArray.elementAt(3).toString();
	    String productName = myCmdArray.elementAt(4).toString();
	    String productVersion = myCmdArray.elementAt(5).toString();
	    String implementsClass = myCmdArray.elementAt(6).toString();
	    String service = myCmdArray.elementAt(7).toString();
	    String originalName = myCmdArray.elementAt(8).toString();
	    String flag = myCmdArray.elementAt(9).toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-sm-vendor.xml");
	    SMVendorModel model = new SMVendorModel();
	    model.setVendorName(vendorName);
	    model.setProductName(productName);
	    model.setProductVersion(productVersion);
	    model.setImplementsClass(implementsClass);
	    model.setService(service);
	    model.setDesc(new UtilString(desc).replace("__eol__", ""));
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, false));
	    else
		myOut.write(web.saveModel(model, originalName, 1, false));
	    web = null;
	} else if (socketCmd
		.equals("XML_Visual_Config_AWSSMVendor_ClusterAndEffective")) {
	    String vendorName = myCmdArray.elementAt(3).toString();
	    String productName = myCmdArray.elementAt(4).toString();
	    String productVersion = myCmdArray.elementAt(5).toString();
	    String implementsClass = myCmdArray.elementAt(6).toString();
	    String service = myCmdArray.elementAt(7).toString();
	    String originalName = myCmdArray.elementAt(8).toString();
	    String flag = myCmdArray.elementAt(9).toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    String bpmHostName = UtilCode.decode(myStr.matchValue(
		    "_BPMHostName[", "]BPMHostName_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-sm-vendor.xml");
	    SMVendorModel model = new SMVendorModel();
	    model.setVendorName(vendorName);
	    model.setProductName(productName);
	    model.setProductVersion(productVersion);
	    model.setImplementsClass(implementsClass);
	    model.setService(service);
	    model.setDesc(new UtilString(desc).replace("__eol__", ""));
	    if (flag.equals("0"))
		myOut.write(web.addModel(model, 1, true));
	    else
		myOut.write(web.saveModel(model, originalName, 2, true));
	    web = null;
	} else if (socketCmd.equals("XML_Visual_Config_AWSSMVendor_Remove")) {
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    XMLVisualConfigWeb web = new XMLVisualConfigWeb(me,
		    "./aws-sm-vendor.xml");
	    myOut.write(web.removeModel(idList, true));
	    web = null;
	} else if (socketCmd.equals("Ajax_Check_UserCheck")) {
	    String checkData = UtilCode.decode(myStr.matchValue("_checkData[",
		    "]checkData_"));
	    myOut.write(UserQuery.checkUser(checkData));
	} else if (socketCmd.equals("LiveSearch_QueryData")) {
	    String query = UtilCode.decode(myStr.matchValue("_query[",
		    "]query_"));
	    String param1 = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String param2 = UtilCode.decode(myStr.matchValue("_param2[",
		    "]param2_"));
	    String param3 = UtilCode.decode(myStr.matchValue("_param3[",
		    "]param3_"));
	    String opt = UtilCode.decode(myStr.matchValue("_opt[", "]opt_"));
	    UserQuery uq = new UserQuery(me, query, opt);
	    myOut.write(uq.getWeb(param1, param2, param3));
	    uq = null;
	} else if (socketCmd.equals("Working_Calendar_Main")) {
	    String local = myCmdArray.elementAt(3).toString();
	    String status = myCmdArray.elementAt(4).toString();
	    status = status != null && status.trim().length() != 0 ? status
		    : "0";
	    WorkingCalendarWeb web = new WorkingCalendarWeb(me);
	    myOut.write(web.getMain(local, status));
	} else if (socketCmd.equals("WorkingCalendar_List_XML_Data")) {
	    String local = myCmdArray.elementAt(3).toString();
	    WorkingCalendarWeb web = new WorkingCalendarWeb(me);
	    myOut.write(web.getWorkingCalendarXmlData(local));
	} else if (socketCmd.equals("Gov_Get_Subject_Word")) {
	    String source = myStr.matchValue("_source[", "]source_");
	    String targetValue = myStr.matchValue("_targetValue[",
		    "]targetValue_");
	    if (targetValue == null)
		targetValue = "";
	    SubjectWord sw = new SubjectWord();
	    myOut.write(sw.separate(targetValue, source));
	    sw = null;
	} else if (socketCmd.equals("Holiday_List_XML_Data")) {
	    String local = myCmdArray.elementAt(3).toString();
	    WorkingCalendarWeb web = new WorkingCalendarWeb(me);
	    myOut.write(web.getHolidayXmlData(local));
	} else if (socketCmd.equals("Expression_At_Command_JSON")) {
	    ExpressionEditorLeftWeb web = new ExpressionEditorLeftWeb(me);
	    String filter = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getATCommandToJSON(filter));
	} else if (socketCmd.equals("Expression_At_Command_JSON_Field")) {
	    ExpressionEditorLeftWeb web = new ExpressionEditorLeftWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getFieldsOfTable(Integer.parseInt(metaDataId)));
	} else if (socketCmd.equals("AWF_UiComponment_Config")) {
	    DictionaryAWFUIComponetConfigWeb web = new DictionaryAWFUIComponetConfigWeb(
		    me);
	    String type = myCmdArray.elementAt(3).toString();
	    String diggerId = myCmdArray.elementAt(4).toString();
	    String diggerMapId = myCmdArray.elementAt(5).toString();
	    String fileName = UtilCode.decode(myStr.matchValue("_fileName[",
		    "]fileName_"));
	    String displayType = UtilCode.decode(myStr.matchValue(
		    "_displayType[", "]displayType_"));
	    String displaySql = UtilCode.decode(myStr.matchValue(
		    "_displaySql[", "]displaySql_"));
	    if ("".equals(diggerId))
		diggerId = "0";
	    if ("".equals(diggerMapId))
		diggerMapId = "0";
	    myOut.write(web.getWeb(type, Integer.parseInt(diggerId),
		    Integer.parseInt(diggerMapId), fileName, displayType,
		    displaySql));
	} else if (socketCmd.equals("Login_RTX")) {
	    LoginRtxWeb web = new LoginRtxWeb(me);
	    myOut.write(web.loginRtx());
	} else if (socketCmd.equals("Mobile_Portlet")) {
	    MobilePortlet web = new MobilePortlet(me);
	    myOut.write(web.getMobilePortletPage());
	} else if (socketCmd.equals("Flex_File_Upload")) {
	    Hashtable h = new UnsyncHashtable();
	    h.put("sid", me.getSessionId());
	    String ms = myCmdArray.elementAt(3).toString();
	    h.put("maxFileSize", "".equals(ms) ? "''" : ms);
	    int maxUpLength = -1;
	    String maxl = myCmdArray.elementAt(4).toString();
	    try {
		if (maxl != null && !"".equals(maxl))
		    maxUpLength = Integer.parseInt(maxl);
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	    h.put("maxUpLength", maxUpLength);
	    h.put("flag1",
		    UtilCode.decode(myStr.matchValue("_flag1[", "]flag1_")));
	    h.put("flag2",
		    UtilCode.decode(myStr.matchValue("_flag2[", "]flag2_")));
	    h.put("rootDir",
		    UtilCode.decode(myStr.matchValue("_rootDir[", "]rootDir_")));
	    String filter = UtilCode.decode(myStr.matchValue("_filesToFilter[",
		    "]filesToFilter_"));
	    h.put("filesToFilter", "".equals(filter) ? "''" : filter);
	    String httpUrl = UtilCode.decode(myStr.matchValue("_httpUrl[",
		    "]httpUrl_"));
	    h.put("httpUrl", httpUrl);
	    myOut.write(FlexUpFile.buildWebUI(h));
	} else {
	    return false;
	}
	return true;
    }
}
