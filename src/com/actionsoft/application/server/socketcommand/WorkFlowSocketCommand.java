package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import com.actionsoft.application.portal.navigation.cache.NavigationDirectoryCache;
import com.actionsoft.application.portal.navigation.cache.NavigationFunctionCache;
import com.actionsoft.application.portal.navigation.model.NavigationDirectoryModel;
import com.actionsoft.application.portal.navigation.model.NavigationFunctionModel;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.commons.BaseDataAdmin;
import com.actionsoft.awf.form.design.cache.SheetCache;
import com.actionsoft.awf.form.design.model.FormModel;
import com.actionsoft.awf.form.design.model.SheetModel;
import com.actionsoft.awf.form.design.web.DesignFormBaseDataTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormCardWeb;
import com.actionsoft.awf.form.design.web.DesignFormDisplayTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormExcelTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormMobileTempleteTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormPdfTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormRuleTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormSheetTabWeb;
import com.actionsoft.awf.form.design.web.DesignFormWeb;
import com.actionsoft.awf.form.design.web.DesignFormWorkflowSatisfationWeb;
import com.actionsoft.awf.form.design.web.DesignFormXULTabWeb;
import com.actionsoft.awf.form.execute.FormFileUtil;
import com.actionsoft.awf.form.execute.FormUtil;
import com.actionsoft.awf.form.execute.RefForm;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.SubForm;
import com.actionsoft.awf.form.execute.WorkflowFormUtil;
import com.actionsoft.awf.form.execute.plugins.component.FormUIComponentFileImpl;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.cache.ElectroncachetDefCache;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.model.ElectroncachetDefModel;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.model.ElectroncachetRefModel;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.web.ElectroncachetCardWeb;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.web.ElectroncachetCheckPasswordWeb;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.web.ElectroncachetCreateWeb;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.web.ElectroncachetHistoryInfoWeb;
import com.actionsoft.awf.form.execute.plugins.component.file.web.FormUIComponentFileRefKMFileWeb;
import com.actionsoft.awf.form.execute.plugins.ext.AjaxDataDecode;
import com.actionsoft.awf.form.execute.plugins.ext.TreeGrid;
import com.actionsoft.awf.organization.addresstree.UserTaskExecuteAddParticipantsAddressTreeWeb;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.report.execute.web.DiggerExecuteWeb;
import com.actionsoft.awf.report.execute.web.WorklistConditionWeb;
import com.actionsoft.awf.repository.web.CategoryWeb;
import com.actionsoft.awf.util.DBSequence;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.MD5;
import com.actionsoft.awf.util.SequenceException;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepBindFieldCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.dao.WFDesignDaoFactory;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepButWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignWeb;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepBindFieldModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepBindReportModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepParameterModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepRTClassModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepToolbarModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowVariableModel;
import com.actionsoft.awf.workflow.design.web.WFDesignBaseDataTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignCopyWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignDocumentTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignDocumentWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignEventTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignGroupFindACWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignGroupWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignMetaDataVariableWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignMoveWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignSecurityTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepActivityTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepBaseDataTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepCardWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepCostTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepDocumentTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepHelpTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepImpExpTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepMessageTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepOpinionTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepRTClassTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignStepRuleTabWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignWeb;
import com.actionsoft.awf.workflow.design.web.WFDesignWorklistConfTabWeb;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.helper.UrgeUtil;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.UserTaskAuditMenuModel;
import com.actionsoft.awf.workflow.execute.model.UserTaskHistoryOpinionModel;
import com.actionsoft.awf.workflow.execute.search.HistorySearchWeb;
import com.actionsoft.awf.workflow.execute.track.TrackWebFactory;
import com.actionsoft.awf.workflow.execute.variable.ProcessVariableInstance;
import com.actionsoft.awf.workflow.execute.worklist.web.ProcessAttachFilesWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.ProcessGroupWorklistWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.ProcessStartWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskAuditMenuWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskExecuteWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskFormPrintWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskFormsWeb;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskUrgeWeb;
import com.actionsoft.awf.workflow.monitor.runtime.RuntimeMonitorMainWeb;
import com.actionsoft.awf.workflow.monitor.runtime.util.RuntimeMonitorUtil;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.i18n.I18nRes;
import com.letv.rtx.UpdateTargetThread;
import com.letv.rtx.UpdateWorkMsgThread;

public class WorkFlowSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("WorkFlow_Design_List")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkFlowList(workFlowStyle));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Root")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    String searchKey = UtilCode.decode(myStr.matchValue("_searchKey[",
		    "]searchKey_"));
	    myOut.write(web.getWorkflowRoot(groupName, searchKey));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Find")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkFlowList(workFlowStyle));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_CreatePage")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.createWorkFlowGroupPage(groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_Create")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    String workFlowName = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    WorkFlowModel model = new WorkFlowModel();
	    model._flowMaster = me.getUID();
	    model._flowName = workFlowName;
	    model._flowStyle = workFlowStyle;
	    model._groupName = groupName;
	    myOut.write(web.saveWorkFlowGroup(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_NavigationPage")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String systemId = myCmdArray.elementAt(4).toString();
	    String directoryId = myCmdArray.elementAt(5).toString();
	    String groupName = myCmdArray.elementAt(6).toString();
	    if (workFlowId == null || workFlowId.equals(""))
		workFlowId = "0";
	    if (directoryId == null || directoryId.equals(""))
		directoryId = "0";
	    if (systemId == null || systemId.equals(""))
		systemId = "0";
	    myOut.write(web.navigationWorkFlowGroupPage(groupName,
		    Integer.parseInt(workFlowId), Integer.parseInt(systemId),
		    Integer.parseInt(directoryId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_Navigation")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String directoryId = myCmdArray.elementAt(4).toString();
	    if (workFlowId == null || workFlowId.equals(""))
		workFlowId = "0";
	    if (directoryId == null || directoryId.equals(""))
		directoryId = "0";
	    myOut.write(web.navigationWorkFlowGroup(
		    Integer.parseInt(workFlowId), Integer.parseInt(directoryId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_VerifyNamePage")) {
	    WFDesignGroupWeb web = new WFDesignGroupWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getVerifyWFGroupNamePage(groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_VerifyName")) {
	    WFDesignGroupWeb web = new WFDesignGroupWeb(me);
	    String oldName = myCmdArray.elementAt(3).toString();
	    String newName = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    myOut.write(web.verifyWFGroupName(oldName, newName, groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_RemovePage")) {
	    WFDesignGroupWeb web = new WFDesignGroupWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getRemoveWFGroupPage(groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_Remove")) {
	    WFDesignGroupWeb web = new WFDesignGroupWeb(me);
	    String removeParam = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    if (removeParam == null || removeParam.equals(""))
		removeParam = "1";
	    String wfStyles = UtilCode.decode(myStr.matchValue("_wfStyle[",
		    "]wfStyle_"));
	    myOut.write(web.removeWFGroup(removeParam, wfStyles, groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_DownloadXML")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.getDownloadXMLDialog(list));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_UploadXMLWindow")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    myOut.write(web.getUpFilePage());
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_UploadXMLImport")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    myOut.write(web.installUploadXML(workFlowStyle));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Create")) {
	    WFDesignBaseDataTabWeb web = new WFDesignBaseDataTabWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    String workFlowName = myCmdArray.elementAt(4).toString();
	    WorkFlowModel model = new WorkFlowModel();
	    model._flowMaster = me.getUID();
	    model._flowName = workFlowName;
	    model._flowStyle = workFlowStyle;
	    myOut.write(web.saveWorkFlow(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Worklist_Open")) {
	    WFDesignWorklistConfTabWeb web = new WFDesignWorklistConfTabWeb(me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(flowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Worklist_Save")) {
	    WFDesignWorklistConfTabWeb web = new WFDesignWorklistConfTabWeb(me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    String xmlContent = UtilCode.decode(myStr.matchValue(
		    "_xmlContent[", "]xmlContent_"));
	    myOut.write(web.saveConfig(Integer.parseInt(flowId), xmlContent));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WF_Worklist_GetTableListCheckBox")) {
	    WFDesignWorklistConfTabWeb web = new WFDesignWorklistConfTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String fields = UtilCode.decode(myStr.matchValue("_fields[",
		    "]fields_"));
	    if (metaDataId.equals(""))
		metaDataId = "0";
	    myOut.write(web.getMetaDataMapListCheckBox(
		    Integer.parseInt(metaDataId), false, fields, true));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Event_Open")) {
	    WFDesignEventTabWeb web = new WFDesignEventTabWeb(me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(flowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Event_Create")) {
	    WFDesignEventTabWeb web = new WFDesignEventTabWeb(me);
	    WorkFlowStepRTClassModel model = new WorkFlowStepRTClassModel();
	    model._flowId = Integer
		    .parseInt(myCmdArray.elementAt(3).toString());
	    model._flowStepId = 0;
	    model._rtType = myCmdArray.elementAt(4).toString();
	    model._className = myCmdArray.elementAt(5).toString();
	    if (model._className == null)
		model._className = "";
	    model._fullPath = ".";
	    myOut.write(web.saveRTClass(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_MDVariable_Remove")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeVariable(Integer.parseInt(flowId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_MDVariable_Open")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(flowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_MDVariable_Create")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    String variableName = myCmdArray.elementAt(4).toString();
	    String tableName = myCmdArray.elementAt(5).toString();
	    String fieldName = myCmdArray.elementAt(6).toString();
	    String constantValue = UtilCode.decode(myStr.matchValue(
		    "_constantValue[", "]constantValue_"));
	    WorkFlowVariableModel model = new WorkFlowVariableModel();
	    model._flowId = Integer.parseInt(flowId);
	    model._variableName = variableName.toUpperCase();
	    model._defaultValue = constantValue;
	    myOut.write(web.createVariable(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Remove")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    String workFlowId = UtilCode.decode(myStr.matchValue(
		    "_workFlowId[", "]workFlowId_"));
	    myOut.write(web.removeWorkFlow(workFlowStyle, workFlowId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_TestRuntimeRoute")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTestRuntimeRoute(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_SetClose")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    String workFlowId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.closeWorkFlow(workFlowStyle,
		    Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_SetOpen")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    String workFlowId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.openWorkFlow(workFlowStyle,
		    Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Open")) {
	    WFFlexDesignWeb web = new WFFlexDesignWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWFFlexDesignPortal(Integer.parseInt(workflowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_BaseData_Open")) {
	    WFDesignBaseDataTabWeb web = new WFDesignBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getBaseDataForm(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_BaseData_Save")) {
	    WFDesignBaseDataTabWeb web = new WFDesignBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String flowName = myCmdArray.elementAt(4).toString();
	    String isAutoArchives = myCmdArray.elementAt(5).toString();
	    String isFinger = myCmdArray.elementAt(6).toString();
	    String flowType = myCmdArray.elementAt(7).toString();
	    String isShare = myCmdArray.elementAt(8).toString();
	    String isVersion = myCmdArray.elementAt(9).toString();
	    String isFlowEndCallAll = myCmdArray.elementAt(10).toString();
	    String isQuickIn = myCmdArray.elementAt(11).toString();
	    String archivesId = myCmdArray.elementAt(12).toString();
	    String isDisplayFile = myCmdArray.elementAt(13).toString();
	    String isTrackForm = myCmdArray.elementAt(14).toString();
	    String isEndCallAuth = myCmdArray.elementAt(15).toString();
	    String wfVersion = myCmdArray.elementAt(16).toString();
	    String isMontior = myCmdArray.elementAt(17).toString();
	    String groupName = myCmdArray.elementAt(18).toString();
	    String isShowMini = myCmdArray.elementAt(19).toString();
	    String isCancel = myCmdArray.elementAt(20).toString();
	    String trackDiagramType = myCmdArray.elementAt(21).toString();
	    String isDraftRemove = myCmdArray.elementAt(22).toString();
	    String isWFCancel = myCmdArray.elementAt(23).toString();
	    String unCancel_WFS = UtilCode.decode(myStr.matchValue(
		    "_uncancelwfs[", "]uncancelwfs_"));
	    String shortMessageModel = UtilCode.decode(myStr.matchValue(
		    "_shortMessageModel[", "]shortMessageModel_"));
	    String durationWarningComplex = UtilCode.decode(myStr.matchValue(
		    "_durationWarningComplex[", "]durationWarningComplex_"));
	    String durationComplex = UtilCode.decode(myStr.matchValue(
		    "_durationComplex[", "]durationComplex_"));
	    String flowDesc = "";
	    String defaultTitle = UtilCode.decode(myStr.matchValue(
		    "_defaultTitle[", "]defaultTitle_"));
	    String flowEndCall = UtilCode.decode(myStr.matchValue(
		    "_flowEndCall[", "]flowEndCall_"));
	    String flowMaster = UtilCode.decode(myStr.matchValue(
		    "_flowMaster[", "]flowMaster_"));
	    String appId = UtilCode.decode(myStr.matchValue("_appId[",
		    "]appId_"));
	    WorkFlowModel model = new WorkFlowModel();
	    model._flowDesc = flowDesc;
	    model._defaultTitle = defaultTitle;
	    model._flowEndCall = flowEndCall;
	    model._isFlowEndCallAll = isFlowEndCallAll.equals("1");
	    model._isQuickIn = isQuickIn.equals("1");
	    model._flowMaster = flowMaster;
	    model._flowName = flowName;
	    model._shortMessageModel = shortMessageModel;
	    model._id = Integer.parseInt(workFlowId);
	    model._isAutoArchives = isAutoArchives.equals("1");
	    model._isFinger = isFinger.equals("1");
	    model._isVersion = isVersion.equals("1");
	    model._isShare = isShare.equals("1");
	    model._workFlowType = Integer.parseInt(flowType);
	    model._archivesId = Integer.parseInt(archivesId);
	    model._isDisplayFile = isDisplayFile.equals("1");
	    model._isTrackForm = isTrackForm.equals("1");
	    model._isMontior = isMontior.equals("1");
	    model._durationComplex = durationComplex;
	    model._durationWarningComplex = durationWarningComplex;
	    model._isEndCallAuth = isEndCallAuth.equals("1");
	    model._isShowMini = isShowMini.equals("1");
	    model._isCancel = isCancel.equals("1");
	    model._isDraftRemove = isDraftRemove.equals("1");
	    model._trackDiagramType = Integer.parseInt(trackDiagramType);
	    model._wfVersion = wfVersion;
	    model._groupName = groupName;
	    model._appId = appId;
	    model._isWFCancel = isWFCancel.equals("1");
	    model._uncancel_wfs = unCancel_WFS;
	    myOut.write(web.saveWorkFlow(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Open")) {
	    WFDesignStepCardWeb web = new WFDesignStepCardWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkFlowDesignFrame(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Diagram_Show")) {
	    WFDesignStepCardWeb web = new WFDesignStepCardWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String selectWorkFlowStepId = myCmdArray.elementAt(4).toString();
	    if (selectWorkFlowStepId == null || selectWorkFlowStepId.equals(""))
		selectWorkFlowStepId = "0";
	    myOut.write(web.showDiagram(Integer.parseInt(workFlowId),
		    Integer.parseInt(selectWorkFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Frame_Open")) {
	    WFDesignStepCardWeb web = new WFDesignStepCardWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String pageType = myCmdArray.elementAt(5).toString();
	    if (pageType == null || pageType.equals(""))
		pageType = "0";
	    myOut.write(web.getWorkFlowStepBaseDataPage(
		    Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(pageType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Security_List")) {
	    WFDesignSecurityTabWeb web = new WFDesignSecurityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String securityType = myCmdArray.elementAt(4).toString();
	    if (securityType == null || securityType.equals(""))
		securityType = "0";
	    myOut.write(web.getWorkFlowSecurity(Integer.parseInt(workFlowId),
		    Integer.parseInt(securityType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Security_Save")) {
	    WFDesignSecurityTabWeb web = new WFDesignSecurityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String securityType = myCmdArray.elementAt(4).toString();
	    if (securityType == null || securityType.equals(""))
		securityType = "0";
	    String securityList = UtilCode.decode(myStr.matchValue(
		    "_securityList[", "]securityList_"));
	    WorkFlowModel workflowModel = (WorkFlowModel) WorkFlowCache
		    .getModel(Integer.parseInt(workFlowId));
	    NavigationFunctionModel navigationFunctionModel = (NavigationFunctionModel) NavigationFunctionCache
		    .getModel(workflowModel._flowStyle.lastIndexOf("组") == -1 ? workflowModel._flowStyle
			    : workflowModel._flowStyle.substring(0,
				    workflowModel._flowStyle.lastIndexOf("组")));
	    if (navigationFunctionModel != null)
		try {
		    NavigationDirectoryModel navigationDirectoryModel = (NavigationDirectoryModel) NavigationDirectoryCache
			    .getModel(navigationFunctionModel._directoryId);
		    String secutiryTypeList = navigationDirectoryModel._systemId
			    + " "
			    + navigationFunctionModel._directoryId
			    + " "
			    + navigationFunctionModel._id;
		    web.saveSecurityByGroup(securityList, secutiryTypeList);
		} catch (Exception exception) {
		}
	    myOut.write(web.saveSecurity(Integer.parseInt(workFlowId),
		    securityList, Integer.parseInt(securityType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_BaseData_Open")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    if (workFlowStepId == null || workFlowStepId.equals(""))
		workFlowStepId = "0";
	    myOut.write(web.getBaseDataForm(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Step_Set")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String stepPosition = myCmdArray.elementAt(5).toString();
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    if (workFlowStepId == null || workFlowStepId.equals(""))
		workFlowStepId = "0";
	    if (stepPosition == null || stepPosition.equals(""))
		stepPosition = "0";
	    WorkFlowStepModel stepModel = (WorkFlowStepModel) WorkFlowStepCache
		    .getModel(Integer.parseInt(workFlowStepId));
	    int tmpStepPosition = Integer.parseInt(stepPosition);
	    if (tmpStepPosition < stepModel._stepNo)
		tmpStepPosition++;
	    WFDesignDaoFactory.createWorkFlowStep().updataOfWorkFlowStep(
		    Integer.parseInt(workFlowId), stepModel._stepNo,
		    tmpStepPosition);
	    model._id = Integer.parseInt(workFlowStepId);
	    model._flowId = Integer.parseInt(workFlowId);
	    model._stepNo = tmpStepPosition;
	    myOut.write(web.setStep(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_BaseData_Save")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String stepName = myCmdArray.elementAt(5).toString();
	    String roleId = myCmdArray.elementAt(6).toString();
	    String isJumpStep = myCmdArray.elementAt(7).toString();
	    String isReadTo = myCmdArray.elementAt(8).toString();
	    String isPrint = myCmdArray.elementAt(9).toString();
	    String routeType = myCmdArray.elementAt(10).toString();
	    String routePointType = myCmdArray.elementAt(11).toString();
	    String teamId = myCmdArray.elementAt(12).toString();
	    String jobPercent = myCmdArray.elementAt(13).toString();
	    String isAudit = myCmdArray.elementAt(14).toString();
	    String stepLimit = myCmdArray.elementAt(15).toString();
	    String isUrgePower = myCmdArray.elementAt(16).toString();
	    String refRoleId1 = myCmdArray.elementAt(17).toString();
	    String refStepId = myCmdArray.elementAt(18).toString();
	    String isDisplayAuditLog = myCmdArray.elementAt(19).toString();
	    String printTimes = myCmdArray.elementAt(20).toString();
	    String isShareOpinion = myCmdArray.elementAt(21).toString();
	    String isStepTransmit = myCmdArray.elementAt(22).toString();
	    String isSelfDispose = myCmdArray.elementAt(23).toString();
	    String transfersLimitType = myCmdArray.elementAt(24).toString();
	    String kmDirectoryId = myCmdArray.elementAt(25).toString();
	    String kmOption = myCmdArray.elementAt(26).toString();
	    String isIgnoreSame = myCmdArray.elementAt(27).toString();
	    String isShortMessage = myCmdArray.elementAt(28).toString();
	    String isAddParticipants = myCmdArray.elementAt(29).toString();
	    String isBatchExecTask = myCmdArray.elementAt(30).toString();
	    String emailAlertType = myCmdArray.elementAt(31).toString();
	    String isHistoryRoute = myCmdArray.elementAt(32).toString();
	    String stepLimitMore = myCmdArray.elementAt(33).toString();
	    String stepUser = UtilCode.decode(myStr.matchValue("_stepUser[",
		    "]stepUser_"));
	    String readToType = UtilCode.decode(myStr.matchValue(
		    "_readToType[", "]readToType_"));
	    String stepTogetherUser = UtilCode.decode(myStr.matchValue(
		    "_stepTogetherUser[", "]stepTogetherUser_"));
	    String durationWarningComplex = UtilCode.decode(myStr.matchValue(
		    "_durationWarningComplex[", "]durationWarningComplex_"));
	    String durationComplex = UtilCode.decode(myStr.matchValue(
		    "_durationComplex[", "]durationComplex_"));
	    String stepTransmitName = UtilCode.decode(myStr.matchValue(
		    "_stepTransmitName[", "]stepTransmitName_"));
	    String transactButtonType = UtilCode.decode(myStr.matchValue(
		    "_transactButtonType[", "]transactButtonType_"));
	    String kmTitle = UtilCode.decode(myStr.matchValue("_kmTitle[",
		    "]kmTitle_"));
	    String extendId = UtilCode.decode(myStr.matchValue("_extendId[",
		    "]extendId_"));
	    String diyRoute = UtilCode.decode(myStr.matchValue("_diyRoute[",
		    "]diyRoute_"));
	    if (diyRoute == null || diyRoute.equals(""))
		diyRoute = "";
	    if (kmDirectoryId == null || kmDirectoryId.equals(""))
		kmDirectoryId = "0";
	    if (emailAlertType == null || emailAlertType.equals(""))
		emailAlertType = "0";
	    if (isHistoryRoute == null || isHistoryRoute.equals(""))
		isHistoryRoute = "0";
	    if (kmOption == null || kmOption.equals(""))
		kmOption = "0";
	    if (transfersLimitType == null || transfersLimitType.equals(""))
		transfersLimitType = "0";
	    if (isStepTransmit == null || isStepTransmit.equals(""))
		isStepTransmit = "0";
	    if (isShareOpinion == null || isShareOpinion.equals(""))
		isShareOpinion = "0";
	    if (isUrgePower == null || isUrgePower.equals(""))
		isUrgePower = "0";
	    if (printTimes == null || printTimes.equals(""))
		printTimes = "-1";
	    if (workFlowStepId == null || workFlowStepId.equals(""))
		workFlowStepId = "0";
	    if (roleId == null || roleId.equals(""))
		roleId = "0";
	    if (refRoleId1 == null || refRoleId1.equals(""))
		refRoleId1 = "0";
	    if (refStepId == null || refStepId.equals(""))
		refStepId = "0";
	    if (isJumpStep == null || isJumpStep.equals(""))
		isJumpStep = "0";
	    if (isReadTo == null || isReadTo.equals(""))
		isReadTo = "0";
	    if (isPrint == null || isPrint.equals(""))
		isPrint = "0";
	    if (isAudit == null || isAudit.equals(""))
		isAudit = "0";
	    if (routeType == null || routeType.equals(""))
		routePointType = "0";
	    if (routePointType == null || routePointType.equals(""))
		routePointType = "0";
	    if (teamId == null || teamId.equals(""))
		teamId = "0";
	    if (stepLimit == null || stepLimit.equals(""))
		stepLimit = "0";
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    model._id = Integer.parseInt(workFlowStepId);
	    model._flowId = Integer.parseInt(workFlowId);
	    model._stepLimitLess = Integer.parseInt(stepLimit);
	    model._stepLimitMore = Integer.parseInt(stepLimitMore);
	    model._emailAlertType = Integer.parseInt(emailAlertType);
	    model._isJumpStep = !isJumpStep.equals("0");
	    model._isPrint = !isPrint.equals("0");
	    model._isReadTo = !isReadTo.equals("0");
	    model._isIgnoreSame = !isIgnoreSame.equals("0");
	    model._isShortMessage = !isShortMessage.equals("0");
	    model._isAddParticipants = !isAddParticipants.equals("0");
	    model._isHistoryRoute = !isHistoryRoute.equals("0");
	    model._roleId = Integer.parseInt(roleId);
	    model._refRoleId1 = Integer.parseInt(refRoleId1);
	    model._refStepId = Integer.parseInt(refStepId);
	    model._routePointType = Integer.parseInt(routePointType);
	    model._routeType = Integer.parseInt(routeType);
	    model._teamId = Integer.parseInt(teamId);
	    model._stepName = stepName;
	    model._stepUser = stepUser;
	    model._stepTogetherUser = stepTogetherUser;
	    model._jobPercent = Double.parseDouble(jobPercent);
	    model._isAudit = !isAudit.equals("0");
	    model._isUrgePower = !isUrgePower.equals("0");
	    model._isDisplayAuditLog = !isDisplayAuditLog.equals("0");
	    model._printTimes = Integer.parseInt(printTimes);
	    model._readToType = readToType;
	    model._isStepTransmit = !isStepTransmit.equals("0");
	    model._stepTransmitName = stepTransmitName;
	    model._isShareOpinion = !isShareOpinion.equals("0");
	    model._isSelfDispose = !isSelfDispose.equals("0");
	    model._isBatchExecTask = !isBatchExecTask.equals("0");
	    model._transfersLimitType = Integer.parseInt(transfersLimitType);
	    model._transfersButtonType = transactButtonType;
	    model._kmDirectoryId = Integer.parseInt(kmDirectoryId);
	    model._kmOption = Integer.parseInt(kmOption);
	    model._kmTitle = kmTitle;
	    model._extendId = extendId;
	    model._durationComplex = durationComplex;
	    model._durationWarningComplex = durationWarningComplex;
	    model._diyRoute = diyRoute;
	    myOut.write(web.saveStep(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Change")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowStepId = myCmdArray.elementAt(3).toString();
	    String activityType = myCmdArray.elementAt(4).toString();
	    myOut.write(web.changeActivityType(
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(activityType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_URL_Open")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getBindUrl(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_URL_Save")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    WorkFlowStepParameterModel model = new WorkFlowStepParameterModel();
	    int wfId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    int wfsId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    String url = UtilCode.decode(myStr.matchValue("_bindUrl[",
		    "]bindUrl_"));
	    myOut.write(web.saveUrl(wfId, wfsId, url));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_BindField_Save")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    WorkFlowStepBindFieldModel model = new WorkFlowStepBindFieldModel();
	    model._flowId = Integer
		    .parseInt(myCmdArray.elementAt(3).toString());
	    model._flowStepId = Integer.parseInt(myCmdArray.elementAt(4)
		    .toString());
	    model._formId = Integer
		    .parseInt(myCmdArray.elementAt(5).toString());
	    model._metaDataId = Integer.parseInt(myCmdArray.elementAt(6)
		    .toString());
	    model._fieldName = myCmdArray.elementAt(7).toString();
	    model._isDisplay = !myCmdArray.elementAt(8).toString().equals("0");
	    model._isModify = !myCmdArray.elementAt(9).toString().equals("0");
	    myOut.write(web.saveBindFieldDataAjax(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_BindField_PSaveall")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    WorkFlowStepBindFieldModel model = new WorkFlowStepBindFieldModel();
	    model._flowId = Integer
		    .parseInt(myCmdArray.elementAt(3).toString());
	    model._flowStepId = Integer.parseInt(myCmdArray.elementAt(4)
		    .toString());
	    model._formId = Integer
		    .parseInt(myCmdArray.elementAt(5).toString());
	    String tmpDisplay = UtilCode.decode(myStr.matchValue("_isDisplay[",
		    "]isDisplay_"));
	    String tmpModify = UtilCode.decode(myStr.matchValue("_isRead[",
		    "]isRead_"));
	    String isModify = tmpModify.substring(0, tmpModify.length() - 1);
	    UtilString us = new UtilString(isModify);
	    Vector modifyArray = new UnsyncVector();
	    modifyArray = us.split(" ");
	    for (int p = 0; p < modifyArray.size(); p++) {
		String cIsModify = (String) modifyArray.get(p);
		model._fieldName = cIsModify.substring(0,
			cIsModify.indexOf("*"));
		model._metaDataId = Integer.parseInt(cIsModify.substring(
			cIsModify.indexOf("*") + 1, cIsModify.indexOf("/")));
		model._isModify = !cIsModify.substring(
			cIsModify.indexOf("/") + 1, cIsModify.length()).equals(
			"0");
		WFDesignDaoFactory.createWorkFlowStepBindField().create(model);
	    }

	    WorkFlowStepBindFieldCache.reload();
	    String isDisplay = tmpDisplay.substring(0, tmpDisplay.length() - 1);
	    UtilString us2 = new UtilString(isDisplay);
	    Vector displayArray = new UnsyncVector();
	    displayArray = us2.split(" ");
	    for (int d = 0; d < displayArray.size(); d++) {
		String cIsDisplay = (String) displayArray.get(d);
		model._fieldName = cIsDisplay.substring(0,
			cIsDisplay.indexOf("*"));
		model._metaDataId = Integer.parseInt(cIsDisplay.substring(
			cIsDisplay.indexOf("*") + 1, cIsDisplay.indexOf("/")));
		model._isDisplay = !cIsDisplay.substring(
			cIsDisplay.indexOf("/") + 1, cIsDisplay.length())
			.equals("0");
		WorkFlowStepBindFieldModel sModel = WorkFlowStepBindFieldCache
			.getModelOfIsModify(model._flowStepId,
				model._fieldName, model._metaDataId);
		model._isModify = sModel._isModify;
		WFDesignDaoFactory.createWorkFlowStepBindField().create(model);
	    }

	    WorkFlowStepBindFieldCache.reload();
	    myOut.write(web.getBindReportForm(model._flowId, model._flowStepId,
		    null));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_BindField_SSaveall")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    WorkFlowStepBindFieldModel model = new WorkFlowStepBindFieldModel();
	    model._flowId = Integer
		    .parseInt(myCmdArray.elementAt(3).toString());
	    model._flowStepId = Integer.parseInt(myCmdArray.elementAt(4)
		    .toString());
	    model._formId = Integer
		    .parseInt(myCmdArray.elementAt(5).toString());
	    String tmpDisplay = UtilCode.decode(myStr.matchValue("_isDisplay[",
		    "]isDisplay_"));
	    String tmpModify = UtilCode.decode(myStr.matchValue("_isRead[",
		    "]isRead_"));
	    String isModify = tmpModify.substring(0, tmpModify.length() - 1);
	    UtilString us = new UtilString(isModify);
	    Vector modifyArray = new UnsyncVector();
	    modifyArray = us.split(" ");
	    for (int p = 0; p < modifyArray.size(); p++) {
		String cIsModify = (String) modifyArray.get(p);
		model._fieldName = cIsModify.substring(0,
			cIsModify.indexOf("*"));
		model._metaDataId = Integer.parseInt(cIsModify.substring(
			cIsModify.indexOf("*") + 1, cIsModify.indexOf("/")));
		model._isModify = !cIsModify.substring(
			cIsModify.indexOf("/") + 1, cIsModify.length()).equals(
			"0");
		WFDesignDaoFactory.createWorkFlowStepBindField().create(model);
	    }

	    WorkFlowStepBindFieldCache.reload();
	    String isDisplay = tmpDisplay.substring(0, tmpDisplay.length() - 1);
	    UtilString us2 = new UtilString(isDisplay);
	    Vector displayArray = new UnsyncVector();
	    displayArray = us2.split(" ");
	    for (int d = 0; d < displayArray.size(); d++) {
		String cIsDisplay = (String) displayArray.get(d);
		model._fieldName = cIsDisplay.substring(0,
			cIsDisplay.indexOf("*"));
		model._metaDataId = Integer.parseInt(cIsDisplay.substring(
			cIsDisplay.indexOf("*") + 1, cIsDisplay.indexOf("/")));
		model._isDisplay = !cIsDisplay.substring(
			cIsDisplay.indexOf("/") + 1, cIsDisplay.length())
			.equals("0");
		WorkFlowStepBindFieldModel sModel = WorkFlowStepBindFieldCache
			.getModelOfIsModify(model._flowStepId,
				model._fieldName, model._metaDataId);
		model._isModify = sModel._isModify;
		WFDesignDaoFactory.createWorkFlowStepBindField().create(model);
	    }

	    WorkFlowStepBindFieldCache.reload();
	    myOut.write(web.getBindReportForm(model._flowId, model._flowStepId,
		    null));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Parameter_Create")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    WorkFlowStepParameterModel model = new WorkFlowStepParameterModel();
	    model._flowId = Integer
		    .parseInt(myCmdArray.elementAt(3).toString());
	    model._flowStepId = Integer.parseInt(myCmdArray.elementAt(4)
		    .toString());
	    model._paraName = myCmdArray.elementAt(5).toString();
	    model._paraValue = myCmdArray.elementAt(6).toString();
	    myOut.write(web.saveParameter(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Parameter_Remove")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeParameter(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Form_Open")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getBindReportForm(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), null));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Form_Save")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String formId = myCmdArray.elementAt(5).toString();
	    String reportIsAdd = myCmdArray.elementAt(6).toString();
	    String reportIsModify = myCmdArray.elementAt(7).toString();
	    String reportIsRemove = myCmdArray.elementAt(8).toString();
	    String reportIsShare = myCmdArray.elementAt(9).toString();
	    String reportIsClone = myCmdArray.elementAt(10).toString();
	    String reportIsCopy = myCmdArray.elementAt(11).toString();
	    String reportIsDisplayCopy = myCmdArray.elementAt(12).toString();
	    if (formId == null || formId.equals(""))
		formId = "0";
	    if (reportIsAdd == null || reportIsAdd.equals(""))
		reportIsAdd = "0";
	    if (reportIsModify == null || reportIsModify.equals(""))
		reportIsModify = "0";
	    if (reportIsRemove == null || reportIsRemove.equals(""))
		reportIsRemove = "0";
	    if (reportIsShare == null || reportIsShare.equals(""))
		reportIsShare = "0";
	    if (reportIsClone == null || reportIsClone.equals(""))
		reportIsClone = "0";
	    if (reportIsCopy == null || reportIsCopy.equals(""))
		reportIsCopy = "0";
	    if (reportIsDisplayCopy == null || reportIsDisplayCopy.equals(""))
		reportIsDisplayCopy = "0";
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    model._formId = Integer.parseInt(formId);
	    model._id = Integer.parseInt(workFlowStepId);
	    model._flowId = Integer.parseInt(workFlowId);
	    model._reportIsAdd = !reportIsAdd.equals("0");
	    model._reportIsModify = !reportIsModify.equals("0");
	    model._reportIsRemove = !reportIsRemove.equals("0");
	    model._reportIsShare = !reportIsShare.equals("0");
	    model._reportIsCopy = !reportIsCopy.equals("0");
	    model._reportIsClone = !reportIsClone.equals("0");
	    model._reportIsDisplayCopy = !reportIsDisplayCopy.equals("0");
	    myOut.write(web.saveBindReportData(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Form_BindSub")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String formId = myCmdArray.elementAt(5).toString();
	    String reportIsAdd = myCmdArray.elementAt(6).toString();
	    String reportIsModify = myCmdArray.elementAt(7).toString();
	    String reportIsRemove = myCmdArray.elementAt(8).toString();
	    if (formId == null || formId.equals(""))
		formId = "0";
	    if (reportIsAdd == null || reportIsAdd.equals(""))
		reportIsAdd = "0";
	    if (reportIsModify == null || reportIsModify.equals(""))
		reportIsModify = "0";
	    if (reportIsRemove == null || reportIsRemove.equals(""))
		reportIsRemove = "0";
	    WorkFlowStepBindReportModel model = new WorkFlowStepBindReportModel();
	    model._formId = Integer.parseInt(formId);
	    model._flowStepId = Integer.parseInt(workFlowStepId);
	    model._flowId = Integer.parseInt(workFlowId);
	    model._reportIsAdd = !reportIsAdd.equals("0");
	    model._reportIsModify = !reportIsModify.equals("0");
	    model._reportIsRemove = !reportIsRemove.equals("0");
	    myOut.write(web.saveBindSubReportData(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Activity_Form_SubRemove")) {
	    WFDesignStepActivityTabWeb web = new WFDesignStepActivityTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeBindSubReport(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Opinion_Open")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Cost_Open")) {
	    WFDesignStepCostTabWeb web = new WFDesignStepCostTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Message_Open")) {
	    WFDesignStepMessageTabWeb web = new WFDesignStepMessageTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Rule_Open")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String filterType = myCmdArray.elementAt(5).toString();
	    if (filterType == null || filterType.equals(""))
		filterType = "0";
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(filterType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Rule_SubProcessInfo")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String profileId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getSubProcessInfo(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(profileId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Rule_SubProcessInfoSave")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String profileId = myCmdArray.elementAt(5).toString();
	    String subFlowUUID = myCmdArray.elementAt(6).toString();
	    String subProcessSyn = myCmdArray.elementAt(7).toString();
	    String endStepUUID = myCmdArray.elementAt(8).toString();
	    String muliInstanceLoop = myCmdArray.elementAt(9).toString();
	    String beforeStartEvent = UtilCode.decode(myStr.matchValue(
		    "_beforeStartEvent[", "]beforeStartEvent_"));
	    String afterStartEvent = UtilCode.decode(myStr.matchValue(
		    "_afterStartEvent[", "]afterStartEvent_"));
	    String endCloseEvent = UtilCode.decode(myStr.matchValue(
		    "_endCloseEvent[", "]endCloseEvent_"));
	    String inMapping = UtilCode.decode(myStr.matchValue("_inMapping[",
		    "]inMapping_"));
	    String inEvent = UtilCode.decode(myStr.matchValue("_inEvent[",
		    "]inEvent_"));
	    String outMapping = UtilCode.decode(myStr.matchValue(
		    "_outMapping[", "]outMapping_"));
	    String servicesLocation = UtilCode.decode(myStr.matchValue(
		    "_servicesLocation[", "]servicesLocation_"));
	    String participant = UtilCode.decode(myStr.matchValue(
		    "_participant[", "]participant_"));
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    myOut.write(web.saveSubProcessInfo(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(profileId), subFlowUUID,
		    Integer.parseInt(subProcessSyn), endStepUUID,
		    Integer.parseInt(muliInstanceLoop), participant, title,
		    inMapping, inEvent, outMapping, servicesLocation,
		    beforeStartEvent, afterStartEvent, endCloseEvent));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Rule_ArrorUp")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String workFlowStepRuleId = myCmdArray.elementAt(5).toString();
	    String filterType = myCmdArray.elementAt(6).toString();
	    if (filterType == null || filterType.equals(""))
		filterType = "0";
	    myOut.write(web.upIndex(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(workFlowStepRuleId),
		    Integer.parseInt(filterType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Rule_ArrorDown")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String workFlowStepRuleId = myCmdArray.elementAt(5).toString();
	    String filterType = myCmdArray.elementAt(6).toString();
	    if (filterType == null || filterType.equals(""))
		filterType = "0";
	    myOut.write(web.downIndex(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(workFlowStepRuleId),
		    Integer.parseInt(filterType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Opinion_Create")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String opinionType = myCmdArray.elementAt(5).toString();
	    String opinionName = myCmdArray.elementAt(6).toString();
	    String isCheck = myCmdArray.elementAt(7).toString();
	    String actionSQL = UtilCode.decode(myStr.matchValue("_actionSQL[",
		    "]actionSQL_"));
	    myOut.write(web.createOpinion(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(opinionType), opinionName,
		    Integer.parseInt(isCheck), actionSQL));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Opinion_Update")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String opinionType = myCmdArray.elementAt(5).toString();
	    String opinionName = myCmdArray.elementAt(6).toString();
	    String id = myCmdArray.elementAt(7).toString();
	    String actionSQL = UtilCode.decode(myStr.matchValue("_actionSQL[",
		    "]actionSQL_"));
	    myOut.write(web.updateOpinion(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(opinionType), opinionName,
		    Integer.parseInt(id), actionSQL));
	    web = null;
	} else if (socketCmd.equals("Opinion_Design_Index_View_ArrorUp")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String id = myCmdArray.elementAt(5).toString();
	    myOut.write(web.upIndex(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("Opinion_Design_Index_View_ArrorDown")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String id = myCmdArray.elementAt(5).toString();
	    myOut.write(web.downIndex(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Opinion_Remove")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeOpinion(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_But_Open")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(workFlowId, workFlowStepId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_But_Delete")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeButNormal(workFlowId, workFlowStepId, idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_But_Save")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String butExec = myCmdArray.elementAt(5).toString();
	    String butIsSave = myCmdArray.elementAt(6).toString();
	    String butTarget = myCmdArray.elementAt(7).toString();
	    String butReturn = myCmdArray.elementAt(8).toString();
	    String butRef = myCmdArray.elementAt(9).toString();
	    String butName = UtilCode.decode(myStr.matchValue("_butName[",
		    "]butName_"));
	    String butScript = UtilCode.decode(myStr.matchValue("_butScript[",
		    "]butScript_"));
	    String butAjaxCode = UtilCode.decode(myStr.matchValue(
		    "_butAjaxCode[", "]butAjaxCode_"));
	    WorkFlowStepToolbarModel model = new WorkFlowStepToolbarModel();
	    model.setId(Integer.parseInt(id));
	    model.setFlowStepId(Integer.parseInt(workFlowStepId));
	    model.setButExec(butExec);
	    model.setButIsSave(Boolean.parseBoolean(butIsSave));
	    model.setButTarget(butTarget);
	    model.setButReturn(butReturn);
	    model.setButRef(butRef);
	    model.setButName(butName);
	    model.setButScript(butScript);
	    model.setButAjaxCode(butAjaxCode);
	    myOut.write(web.saveButNormal(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_ImpExp_Open")) {
	    WFDesignStepImpExpTabWeb web = new WFDesignStepImpExpTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_ImpExp_Remove")) {
	    WFDesignStepImpExpTabWeb web = new WFDesignStepImpExpTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeAction(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_ImpExp_Create")) {
	    WFDesignStepImpExpTabWeb web = new WFDesignStepImpExpTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String sourceId = myCmdArray.elementAt(5).toString();
	    String action = myCmdArray.elementAt(6).toString();
	    String dataType = myCmdArray.elementAt(7).toString();
	    myOut.write(web.createAction(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(sourceId), action, dataType));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Help_Open")) {
	    WFDesignStepHelpTabWeb web = new WFDesignStepHelpTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Help_Save")) {
	    WFDesignStepHelpTabWeb web = new WFDesignStepHelpTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String stepHelp = UtilCode.decode(myStr.matchValue("_stepHelp[",
		    "]stepHelp_"));
	    myOut.write(web.saveHelp(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), stepHelp));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Workflow_Help")) {
	    WFDesignStepHelpTabWeb web = new WFDesignStepHelpTabWeb(me);
	    String workFlowStepId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getHelp(Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WF_Design_Document_Open")) {
	    WFDesignDocumentTabWeb web = new WFDesignDocumentTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String wfDocumentACType = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(wfDocumentACType)));
	    web = null;
	} else if (socketCmd.equals("WF_Design_Document_Save")) {
	    WFDesignDocumentTabWeb web = new WFDesignDocumentTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String WFStepDocument = UtilCode.decode(myStr.matchValue(
		    "_WFStepDocument[", "]WFStepDocument_"));
	    myOut.write(web.saveWFDocument(Integer.parseInt(workFlowId),
		    WFStepDocument));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Document_Open")) {
	    WFDesignStepDocumentTabWeb web = new WFDesignStepDocumentTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Document_Save")) {
	    WFDesignStepDocumentTabWeb web = new WFDesignStepDocumentTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String WFStepDocument = UtilCode.decode(myStr.matchValue(
		    "_WFStepDocument[", "]WFStepDocument_"));
	    myOut.write(web.saveWFSDocument(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), WFStepDocument));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Message_Create")) {
	    WFDesignStepMessageTabWeb web = new WFDesignStepMessageTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String messageType = myCmdArray.elementAt(5).toString();
	    String messageUser = UtilCode.decode(myStr.matchValue(
		    "_messageUser[", "]messageUser_"));
	    if (messageUser == null)
		messageUser = "";
	    String stepList = UtilCode.decode(myStr.matchValue("_stepList[",
		    "]stepList_"));
	    if (stepList == null)
		stepList = "";
	    String auditMenuName = UtilCode.decode(myStr.matchValue(
		    "_auditMenuName[", "]auditMenuName_"));
	    if (auditMenuName == null)
		auditMenuName = "";
	    myOut.write(web.createMessagePolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), messageUser, stepList,
		    auditMenuName, messageType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Message_Remove")) {
	    WFDesignStepMessageTabWeb web = new WFDesignStepMessageTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeMessagePolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Cost_Create")) {
	    WFDesignStepCostTabWeb web = new WFDesignStepCostTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String cost = myCmdArray.elementAt(5).toString();
	    String costPolicy = myCmdArray.elementAt(6).toString();
	    String calcType = myCmdArray.elementAt(7).toString();
	    String costPoint = myCmdArray.elementAt(8).toString();
	    if (calcType == null || calcType.length() == 0)
		calcType = "0";
	    if (costPoint == null || costPoint.length() == 0)
		costPoint = "0";
	    myOut.write(web.createCostPolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Double.parseDouble(cost),
		    Integer.parseInt(costPolicy), Integer.parseInt(calcType),
		    Integer.parseInt(costPoint)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Cost_Remove")) {
	    WFDesignStepCostTabWeb web = new WFDesignStepCostTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeCostPolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Rule_Create")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String fieldList = myCmdArray.elementAt(5).toString();
	    String compareType = myCmdArray.elementAt(6).toString();
	    String compareValue = myCmdArray.elementAt(7).toString();
	    String jumpStepId = myCmdArray.elementAt(8).toString();
	    String ruleGroup = myCmdArray.elementAt(9).toString();
	    String subFlowUUID = myCmdArray.elementAt(10).toString();
	    String subProcessSyn = myCmdArray.elementAt(11).toString();
	    String endStepUUID = myCmdArray.elementAt(12).toString();
	    String muliInstanceLoop = myCmdArray.elementAt(13).toString();
	    String filterType = myCmdArray.elementAt(14).toString();
	    if (filterType == null || filterType.equals(""))
		filterType = "0";
	    String participant = UtilCode.decode(myStr.matchValue(
		    "_participant[", "]participant_"));
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    String jumpUser = UtilCode.decode(myStr.matchValue("_jumpUser[",
		    "]jumpUser_"));
	    if (ruleGroup == null)
		ruleGroup = "";
	    if (jumpStepId == null || jumpStepId.equals(""))
		jumpStepId = "0";
	    myOut.write(web.createRule(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), fieldList, compareType,
		    compareValue, Integer.parseInt(jumpStepId), jumpUser,
		    ruleGroup, subFlowUUID, subProcessSyn, endStepUUID,
		    Integer.parseInt(muliInstanceLoop), participant, title,
		    Integer.parseInt(filterType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Rule_Remove")) {
	    WFDesignStepRuleTabWeb web = new WFDesignStepRuleTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String filterType = myCmdArray.elementAt(5).toString();
	    if (filterType == null || filterType.equals(""))
		filterType = "0";
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeRule(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList,
		    Integer.parseInt(filterType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_RuntimeClass_Open")) {
	    WFDesignStepRTClassTabWeb web = new WFDesignStepRTClassTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String rtType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), rtType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_RuntimeClass_Create")) {
	    WFDesignStepRTClassTabWeb web = new WFDesignStepRTClassTabWeb(me);
	    WorkFlowStepRTClassModel model = new WorkFlowStepRTClassModel();
	    model._flowId = Integer
		    .parseInt(myCmdArray.elementAt(3).toString());
	    model._flowStepId = Integer.parseInt(myCmdArray.elementAt(4)
		    .toString());
	    model._rtType = myCmdArray.elementAt(5).toString();
	    model._className = myCmdArray.elementAt(6).toString();
	    if (model._className == null)
		model._className = "";
	    model._fullPath = ".";
	    myOut.write(web.saveRTClass(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Create")) {
	    WFDesignStepCardWeb web = new WFDesignStepCardWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.createWorkFlowStepPage(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Save")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String stepNo = myCmdArray.elementAt(4).toString();
	    String formId = myCmdArray.elementAt(5).toString();
	    String stepName = UtilCode.decode(myStr.matchValue("_stepName[",
		    "]stepName_"));
	    if (formId == null || formId.equals(""))
		formId = "0";
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    model._formId = Integer.parseInt(formId);
	    model._flowId = Integer.parseInt(workFlowId);
	    model._stepName = stepName;
	    model._stepNo = Integer.parseInt(stepNo);
	    myOut.write(web.saveStep(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Remove")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.removeStep(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_List")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String filterName = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getReportList(groupName, filterName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Create")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String reportName = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    FormModel model = new FormModel();
	    model.setDeleted(false);
	    model.setTitle(reportName);
	    model.setGroupName(groupName);
	    model.setMaster(me.getUID());
	    myOut.write(web.saveReport(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_BaseData_Open")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getBaseDataPage(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_EditTest")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String reportName = myCmdArray.elementAt(3).toString();
	    FormModel model = new FormModel();
	    model.setDeleted(false);
	    model.setTitle(reportName);
	    model.setMaster(me.getUID());
	    myOut.write(web.saveForm(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_HtmlEditWeb")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String htmlName = myCmdArray.elementAt(4).toString();
	    String sheetId = myCmdArray.elementAt(5).toString();
	    if (sheetId == null || sheetId.length() == 0)
		sheetId = "0";
	    myOut.write(web.getHtmlEditWeb(Integer.parseInt(formId),
		    Integer.parseInt(sheetId), htmlName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_HtmlEditSave")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String htmlName = myCmdArray.elementAt(4).toString();
	    String sheetId = myCmdArray.elementAt(5).toString();
	    if (sheetId == null || sheetId.equals(""))
		sheetId = "0";
	    String htmlContext = UtilCode.decode(myStr.matchValue(
		    "_htmlContext[", "]htmlContext_"));
	    myOut.write(web.setModelName(Integer.parseInt(formId),
		    Integer.parseInt(sheetId), htmlName, htmlContext));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Remove")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.removeReport(groupName, list));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Open")) {
	    DesignFormCardWeb web = new DesignFormCardWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String pageType = myCmdArray.elementAt(4).toString();
	    if (pageType == null || pageType.equals(""))
		pageType = "1";
	    myOut.write(web.getReportPage(Integer.parseInt(formId),
		    Integer.parseInt(pageType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_BaseData_Save")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String reportTitle = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String master = UtilCode.decode(myStr.matchValue("_master[",
		    "]master_"));
	    FormModel model = new FormModel();
	    model.setDeleted(false);
	    model.setId(Integer.parseInt(formId));
	    model.setTitle(reportTitle);
	    model.setGroupName(groupName);
	    model.setMaster(master);
	    if (groupName == null || groupName.equals(""))
		model.setGroupName("未分类");
	    myOut.write(web.saveReportAjax(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_BaseData_SaveHead")) {
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String reportTitle = myCmdArray.elementAt(4).toString();
	    String isHidden = myCmdArray.elementAt(5).toString();
	    FormModel model = new FormModel();
	    model.setDeleted(false);
	    model.setId(Integer.parseInt(formId));
	    model.setTitle(reportTitle);
	    model.setHeadHidden(!isHidden.equals("0"));
	    myOut.write(web.saveHiddenReport(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Store_Open")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getSheetPage(Integer.parseInt(formId), null));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_XUL_Open")) {
	    DesignFormXULTabWeb web = new DesignFormXULTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_XUL_Save")) {
	    DesignFormXULTabWeb web = new DesignFormXULTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String xulRes = myCmdArray.elementAt(4).toString();
	    myOut.write(web.setXULRes(Integer.parseInt(formId), xulRes));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Store_AppendP")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String metaDataId = myCmdArray.elementAt(4).toString();
	    SheetModel model = new SheetModel();
	    model.setSubSheet(false);
	    model.setMetaDataId(Integer.parseInt(metaDataId));
	    model.setFormId(Integer.parseInt(formId));
	    model.setTitle("[未设定]");
	    model.setPageNum(0);
	    myOut.write(web.saveSheet(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Store_AppendS")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String metaDataId = myCmdArray.elementAt(4).toString();
	    SheetModel model = new SheetModel();
	    model.setSubSheet(true);
	    model.setMetaDataId(Integer.parseInt(metaDataId));
	    model.setFormId(Integer.parseInt(formId));
	    model.setTitle("[未设定]");
	    model.setPageNum(0);
	    myOut.write(web.saveSheet(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Store_Remove")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String sheetId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.removeSheet(Integer.parseInt(formId),
		    Integer.parseInt(sheetId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Store_Save")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String sheetHead = UtilCode.decode(myStr.matchValue("_sheetHead[",
		    "]sheetHead_"));
	    String sheetOrderBy = UtilCode.decode(myStr.matchValue(
		    "_sheetOrderBy[", "]sheetOrderBy_"));
	    String sheetId = myCmdArray.elementAt(3).toString();
	    String formId = myCmdArray.elementAt(4).toString();
	    String pageNum = myCmdArray.elementAt(5).toString();
	    if (pageNum == null || "".equals(pageNum))
		pageNum = "0";
	    String xmlFile = myCmdArray.elementAt(6).toString();
	    myOut.write(web.saveSheetDisplay(Integer.parseInt(sheetId),
		    Integer.parseInt(formId), Integer.parseInt(pageNum),
		    sheetHead, xmlFile, sheetOrderBy));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Store_SaveDM")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String displayInfo = myCmdArray.elementAt(3).toString();
	    myOut.write(web.saveDisplayModel(displayInfo));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_EditSheetHead")) {
	    DesignFormSheetTabWeb web = new DesignFormSheetTabWeb(me);
	    String sheetId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.openSheetHeadEditor(sheetId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Excel_Open")) {
	    DesignFormExcelTabWeb web = new DesignFormExcelTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getExcelPage(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Pdf_Open")) {
	    DesignFormPdfTabWeb web = new DesignFormPdfTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getPdfPage(Integer.parseInt(formId)));
	} else if (socketCmd.equals("WorkFlow_Design_Form_Pdf_Preview")) {
	    DesignFormPdfTabWeb web = new DesignFormPdfTabWeb(me);
	    String fileName = myCmdArray.elementAt(3).toString();
	    myOut.write(DesignFormPdfTabWeb.getPdfPreview(me, fileName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Pdf_SelectModel")) {
	    DesignFormPdfTabWeb web = new DesignFormPdfTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String fileName = myCmdArray.elementAt(4).toString();
	    myOut.write(web.setModelName(Integer.parseInt(formId), fileName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Excel_SelectModel")) {
	    DesignFormExcelTabWeb web = new DesignFormExcelTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String fileName = myCmdArray.elementAt(4).toString();
	    myOut.write(web.setModelName(Integer.parseInt(formId), fileName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Excel_Preview")) {
	    DesignFormExcelTabWeb web = new DesignFormExcelTabWeb(me);
	    String fileName = myCmdArray.elementAt(3).toString();
	    myOut.write(DesignFormExcelTabWeb.getExcelPreview(me, fileName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Display_Open")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String htmlName = myCmdArray.elementAt(4).toString();
	    String sheetId = myCmdArray.elementAt(5).toString();
	    if (sheetId == null || sheetId.equals(""))
		sheetId = "0";
	    myOut.write(web.getDisplayPage(Integer.parseInt(formId), htmlName,
		    Integer.parseInt(sheetId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Display_UpModelPage")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    myOut.write(web.getUpModelPage());
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Display_CopyFormModel")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String sheetId = myCmdArray.elementAt(4).toString();
	    String isRepeat = myCmdArray.elementAt(5).toString();
	    String repeat = "";
	    if (sheetId == null || sheetId.equals(""))
		sheetId = "0";
	    if ("true".equals(isRepeat))
		myOut.write(web.copyFormModel());
	    else if ("false".equals(isRepeat))
		myOut.write(web.clearTempDir());
	    else
		myOut.write(web.upFileIsRepeat());
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Display_DownFormModel")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String fileName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getDownModelPage(fileName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Display_Preview")) {
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(new RuntimeFormManager(me).getFormPreview(Integer
		    .parseInt(formId)));
	} else if (socketCmd.equals("WorkFlow_Design_Form_Display_ViewModel")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getHtmlModelPage(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Display_Verify")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String sheetId = myCmdArray.elementAt(4).toString();
	    if (sheetId == null || sheetId.equals(""))
		sheetId = "0";
	    myOut.write(web.verifyHtmlModelPage(Integer.parseInt(formId),
		    Integer.parseInt(sheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Display_SelectHtmlModel")) {
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String modelName = myCmdArray.elementAt(4).toString();
	    String sheetId = myCmdArray.elementAt(5).toString();
	    if (sheetId == null || sheetId.equals(""))
		sheetId = "0";
	    myOut.write(web.setModelName(Integer.parseInt(formId), modelName,
		    Integer.parseInt(sheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Display_RefreshCache")) {
	    HtmlModelFactory.reload();
	    DesignFormDisplayTabWeb web = new DesignFormDisplayTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String sheetId = myCmdArray.elementAt(4).toString();
	    if (sheetId == null || sheetId.equals(""))
		sheetId = "0";
	    myOut.write(web.getDisplayPage(Integer.parseInt(formId), null,
		    Integer.parseInt(sheetId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_DownloadXML")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.getDownloadXMLDialog(list));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_UploadXMLWindow")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    myOut.write(web.getUpFilePage());
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_UploadXMLImport")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.installUploadXML(groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Rule_Open")) {
	    DesignFormRuleTabWeb web = new DesignFormRuleTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getRulePage(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_MovePage")) {
	    WFDesignMoveWeb web = new WFDesignMoveWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getPage(groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_CopyPage")) {
	    WFDesignCopyWeb web = new WFDesignCopyWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getPage(groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Move")) {
	    WFDesignMoveWeb web = new WFDesignMoveWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    String newWorkflowGroup = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    myOut.write(web.moveWorkFlow(Integer.parseInt(workflowId),
		    newWorkflowGroup, groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WF_Copy")) {
	    WFDesignCopyWeb web = new WFDesignCopyWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    String workflowName = myCmdArray.elementAt(4).toString();
	    String newWorkflowGroup = myCmdArray.elementAt(5).toString();
	    String groupName = myCmdArray.elementAt(6).toString();
	    myOut.write(web.copyWorkFlow(Integer.parseInt(workflowId),
		    newWorkflowGroup, workflowName, groupName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist")) {
	    String wfType = myCmdArray.elementAt(3).toString();
	    String boxType = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    if (pageNow.equals(""))
		pageNow = "1";
	    String isShowTabs = myCmdArray.elementAt(6).toString();
	    if (isShowTabs == null || isShowTabs.equals(""))
		isShowTabs = "true";
	    boolean isShowTab = isShowTabs.equals("true");
	    String tmp = myCmdArray.elementAt(7).toString();
	    if (wfType == null || wfType.equals(""))
		wfType = tmp;
	    if (boxType.equals(""))
		if (WorkFlowCache.getWorkFlowType(wfType) == 1)
		    boxType = I18nRes.findValue(me.getLanguage(),
			    "aws.common.worklist.page.store.default.boxtype");
		else
		    boxType = I18nRes
			    .findValue(me.getLanguage(),
				    "aws.common.worklist.page.workflow.default.boxtype");
	    ProcessGroupWorklistWeb web = new ProcessGroupWorklistWeb(me,
		    wfType, Integer.parseInt(boxType), isShowTab);
	    myOut.write(web.getBoxList(Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_DiggerCondition")) {
	    String workflowstyle = myCmdArray.elementAt(3).toString();
	    String boxType = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String searchDiggerId = myCmdArray.elementAt(6).toString();
	    WorklistConditionWeb web = new WorklistConditionWeb(me);
	    myOut.write(web.getDiggerConditionPageOfWorkflow(
		    Integer.parseInt(searchDiggerId), workflowstyle, boxType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_DiggerCondition_exec")) {
	    String workflowstyle = myCmdArray.elementAt(3).toString();
	    String boxType = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String searchDiggerId = myCmdArray.elementAt(6).toString();
	    String sql = UtilCode.decode(myStr.matchValue("_sql[", "]sql_"));
	    WorklistConditionWeb web = new WorklistConditionWeb(me);
	    myOut.write(web.getDiggerResultPageOfWorkflow(
		    Integer.parseInt(searchDiggerId), sql, workflowstyle,
		    boxType));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Create")) {
	    String wfStyle = myCmdArray.elementAt(3).toString();
	    String select_workflow = myCmdArray.elementAt(4).toString();
	    ProcessStartWeb web = new ProcessStartWeb(me, wfStyle);
	    if (select_workflow == null || select_workflow.equals(""))
		select_workflow = "0";
	    myOut.write(web.startProcessDialog(Integer
		    .parseInt(select_workflow)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_CreateSelectWF")) {
	    String wfStyle = myCmdArray.elementAt(3).toString();
	    String select_workflow = myCmdArray.elementAt(4).toString();
	    String wfYear = myCmdArray.elementAt(5).toString();
	    String from = UtilCode.decode(myStr.matchValue("_from[", "]from_"));
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    ProcessStartWeb web = new ProcessStartWeb(me, wfStyle);
	    myOut.write(web.startProcessAction(
		    Integer.parseInt(select_workflow), title, from,
		    Integer.parseInt(wfYear)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Draft_Remove")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String isShowTabs = myCmdArray.elementAt(5).toString();
	    if (isShowTabs == null || isShowTabs.equals(""))
		isShowTabs = "true";
	    boolean isShowTab = isShowTabs.equals("true");
	    String p3 = UtilCode.decode(myStr
		    .matchValue("_idlist[", "]idlist_"));
	    ProcessGroupWorklistWeb web = new ProcessGroupWorklistWeb(me, p1,
		    Integer.parseInt(p2), isShowTab);
	    myOut.write(web.deleteMessage(p3));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_PressCall")) {
	    String id = myCmdArray.elementAt(3).toString();
	    boolean isOk = WorkflowEngine.getInstance().notifyMessageByMail(me,
		    Integer.parseInt(id));
	    if (isOk)
		myOut.write("<script>alert('"
			+ I18nRes.findValue(me.getLanguage(), "催办信已发出")
			+ "!');window.close();</script>");
	    else
		myOut.write("<script>alert('"
			+ I18nRes.findValue(me.getLanguage(), "催办信未成功发出")
			+ "!');window.close();</script>");
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_PressCall_SMS")) {
	    String id = myCmdArray.elementAt(3).toString();
	    myOut.write(WorkflowEngine.getInstance().notifyMessageBySMS(me,
		    Integer.parseInt(id)));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_UndoTransaction")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(6).toString();
	    String opinion = UtilCode.decode(myStr.matchValue("_opinion[",
		    "]opinion_"));
	    if (p1 != null && p1.trim().length() > 0 && p2 != null
		    && p2.trim().length() > 0) {
		ProcessGroupWorklistWeb web = new ProcessGroupWorklistWeb(me,
			p1, Integer.parseInt(p2));
		myOut.write(web.undoTranaction2(Integer.parseInt(p3),
			Integer.parseInt(p4), opinion));
		web = null;
	    } else {
		ProcessGroupWorklistWeb web = new ProcessGroupWorklistWeb(me);
		myOut.write(web.undoTranaction2(Integer.parseInt(p3),
			Integer.parseInt(p4), opinion));
		web = null;
	    }
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_UndoNextTransaction")) {
	    String workflowstyle = myCmdArray.elementAt(3).toString();
	    String boxType = myCmdArray.elementAt(4).toString();
	    String id = myCmdArray.elementAt(5).toString();
	    String taskId = myCmdArray.elementAt(6).toString();
	    String opinion = UtilCode.decode(myStr.matchValue("_opinion[",
		    "]opinion_"));
	    ProcessGroupWorklistWeb web = new ProcessGroupWorklistWeb(me,
		    workflowstyle, Integer.parseInt(boxType));
	    myOut.write(web.undoNextTranaction2(Integer.parseInt(id),
		    Integer.parseInt(taskId), opinion));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Help_Open")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    if ("".equals(workflowId) || workflowId == null)
		workflowId = "0";
	    myOut.write(web.getWorkflowHelp(Integer.parseInt(workflowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_File_Open")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    if (openState.equals(""))
		openState = "0";
	    String taskId = myCmdArray.elementAt(5).toString();
	    if (taskId.equals(""))
		taskId = "0";
	    String pageType = myCmdArray.elementAt(6).toString();
	    if (pageType.equals(""))
		pageType = "0";
	    String operator = myCmdArray.elementAt(7).toString();
	    if (operator.equals(""))
		operator = me.getUID();
	    myOut.write(web.getFramesPage(Integer.parseInt(id),
		    Integer.parseInt(openState), Integer.parseInt(taskId),
		    Integer.parseInt(pageType), operator));
	    web = null;
	} else if (socketCmd.equals("Archive_File_View")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String pageType = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getFramesPage(Integer.parseInt(id),
		    Integer.parseInt(openState), Integer.parseInt(taskId),
		    Integer.parseInt(pageType), me.getUID()));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Page_Open")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String pageType = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getTabPage(Integer.parseInt(id),
		    Integer.parseInt(openState), Integer.parseInt(taskId),
		    Integer.parseInt(pageType), me.getUID()));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Viewer")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(7).toString();
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(p1), Integer.parseInt(p3),
		    Integer.parseInt(p2), 0);
	    int workflowStepId = Integer.parseInt(p4);
	    reportWeb.setWorkflowStep(workflowStepId);
	    int defaultFormid = WorkFlowStepCache.getAccessDefaultForm(
		    workflowStepId, me);
	    reportWeb.setFormModel(defaultFormid);
	    myOut.write(reportWeb.getFormPage(1));
	    reportWeb = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Open")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(6).toString();
	    String p5 = myCmdArray.elementAt(7).toString();
	    String p6 = myCmdArray.elementAt(8).toString();
	    myOut.write(web.getFormPage(Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3),
		    Integer.parseInt(p4), Integer.parseInt(p5),
		    Integer.parseInt(p6)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Attach_Open")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getProcessAttachFilesPage(Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Attach_Upload")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    ProcessAttachFilesWeb web = new ProcessAttachFilesWeb(me, 0,
		    Integer.parseInt(p1), Integer.parseInt(p2), p3);
	    myOut.write(web.getUpFilePage());
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Attach_Remove")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = UtilCode.decode(myStr.matchValue("_fn[", "]fn_"));
	    ProcessAttachFilesWeb web = new ProcessAttachFilesWeb(me);
	    myOut.write(web.removeFile(Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3), p4));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Urge_Attach_Upload")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(6).toString();
	    String p5 = myCmdArray.elementAt(7).toString();
	    String urgeContent = UtilCode.decode(myStr.matchValue(
		    "_urgeContent[", "]urgeContent_"));
	    UserTaskUrgeWeb web = new UserTaskUrgeWeb(me);
	    UrgeUtil uc = new UrgeUtil();
	    uc.selectUrge(Integer.parseInt(p1), Integer.parseInt(p2), 0,
		    urgeContent, Integer.parseInt(p3));
	    web.printGetMessage3(me, Integer.parseInt(p1),
		    Integer.parseInt(p3), Integer.parseInt(p2),
		    Integer.parseInt(p4));
	    myOut.write(web.getUpFilePage(Integer.parseInt(p1),
		    Integer.parseInt(p2), p5));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Urge_Attach_Open")) {
	    UserTaskUrgeWeb web = new UserTaskUrgeWeb(me);
	    String bindId = myCmdArray.elementAt(3).toString();
	    String stepNo = myCmdArray.elementAt(4).toString();
	    String flowId = myCmdArray.elementAt(5).toString();
	    String taskId = myCmdArray.elementAt(6).toString();
	    String urgeContent = UtilCode.decode(myStr.matchValue(
		    "_urgeContent[", "]urgeContent_"));
	    UrgeUtil uc = new UrgeUtil();
	    uc.selectUrge(Integer.parseInt(bindId), Integer.parseInt(stepNo),
		    0, urgeContent, 0);
	    myOut.write(web.printGetMessage3(me, Integer.parseInt(bindId),
		    Integer.parseInt(flowId), Integer.parseInt(stepNo),
		    Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Urge_Attach_Remove")) {
	    UserTaskUrgeWeb web = new UserTaskUrgeWeb(me);
	    String bindId = myCmdArray.elementAt(3).toString();
	    String stepNo = myCmdArray.elementAt(4).toString();
	    String flowId = myCmdArray.elementAt(5).toString();
	    String taskId = myCmdArray.elementAt(6).toString();
	    String fn = UtilCode.decode(myStr.matchValue("_fn[", "]fn_"));
	    String urgeContent = UtilCode.decode(myStr.matchValue(
		    "_urgeContent[", "]urgeContent_"));
	    UrgeUtil uc = new UrgeUtil();
	    uc.selectUrge(Integer.parseInt(bindId), Integer.parseInt(stepNo),
		    0, urgeContent, 0);
	    myOut.write(web.removeFile(me, Integer.parseInt(bindId),
		    Integer.parseInt(stepNo), Integer.parseInt(flowId),
		    Integer.parseInt(taskId), fn));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_Upload")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(6).toString();
	    String p5 = myCmdArray.elementAt(7).toString();
	    String p6 = myCmdArray.elementAt(8).toString();
	    String p7 = myCmdArray.elementAt(9).toString();
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    UtilString us1 = new UtilString(bindValue);
	    bindValue = us1.replace("'", "’");
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(p3), Integer.parseInt(p7),
		    Integer.parseInt(p6), Integer.parseInt(p1),
		    Integer.parseInt(p5));
	    myOut.write(web.upFile(Integer.parseInt(p1), Integer.parseInt(p2),
		    "FormFile", bindValue, p4, p5, Integer.parseInt(p3),
		    Integer.parseInt(p7), Integer.parseInt(p6)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_Open")) {
	    String bindId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String state = myCmdArray.elementAt(5).toString();
	    String subSheetId = myCmdArray.elementAt(6).toString();
	    String bindReportId = myCmdArray.elementAt(7).toString();
	    String formType = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(bindId), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(bindReportId),
		    Integer.parseInt(subSheetId));
	    if (formType == null || "".equals(formType))
		formType = "0";
	    web.setFormType(Integer.parseInt(formType));
	    myOut.write(web.printGetMessage3());
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_OpenbyUrl")) {
	    String flag1 = myCmdArray.elementAt(3).toString();
	    String flag2 = myCmdArray.elementAt(4).toString();
	    String openstate = myCmdArray.elementAt(5).toString();
	    myOut.write(FormFileUtil.getAttachFromUrl(me,
		    Integer.parseInt(flag1), Integer.parseInt(flag2),
		    Integer.parseInt(openstate)));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_Remove")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String metaDataMapId = myCmdArray.elementAt(4).toString();
	    String bindId = myCmdArray.elementAt(5).toString();
	    String openstate = myCmdArray.elementAt(6).toString();
	    String taskId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String bindReportId = myCmdArray.elementAt(9).toString();
	    String formType = myCmdArray.elementAt(10).toString();
	    if (formType == null || "".equals(formType))
		formType = "0";
	    String meId = myCmdArray.elementAt(11).toString();
	    String fn = UtilCode.decode(myStr.matchValue("_fn[", "]fn_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(bindId), Integer.parseInt(taskId),
		    Integer.parseInt(openstate), Integer.parseInt(meId),
		    Integer.parseInt(bindReportId),
		    Integer.parseInt(subSheetId));
	    web.setFormType(Integer.parseInt(formType));
	    myOut.write(web.removeFile(Integer.parseInt(id),
		    Integer.parseInt(metaDataMapId), fn));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_GridPage")) {
	    String active = myCmdArray.elementAt(3).toString();
	    String flag1 = myCmdArray.elementAt(4).toString();
	    String flag2 = myCmdArray.elementAt(5).toString();
	    if (flag1.trim().length() == 0)
		flag1 = "0";
	    String rootdir = myCmdArray.elementAt(6).toString();
	    FormUIComponentFileImpl impl = new FormUIComponentFileImpl(null, "");
	    myOut.write(impl.getFormArchivesModifyPageGrid(me,
		    Integer.parseInt(active), Integer.parseInt(flag1),
		    Integer.parseInt(flag2), rootdir));
	    impl = null;
	} else if (socketCmd.equals("WorkFlow_Monitor_Track_List")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    myOut.write(TrackWebFactory.getInstance(me).getTrackPage(
		    Integer.parseInt(instanceId)));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Page_SetSecurityLayer")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String securityLayer = myCmdArray.elementAt(4).toString();
	    ProcessRuntimeDaoFactory.createProcessInstance().setSecurityLayer(
		    Integer.parseInt(id), Integer.parseInt(securityLayer));
	    myOut.write("true");
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_AddParticipants_Win")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String id = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getAddParticipantsPage(me, Integer.parseInt(id),
		    Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_AddParticipants_JsonTree")) {
	    UserTaskExecuteAddParticipantsAddressTreeWeb web = new UserTaskExecuteAddParticipantsAddressTreeWeb(
		    me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getAddParticipantsJsonTree(me, requestType, param1,
		    param2, param3));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_AddParticipants_Send")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String id = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String mailTo = UtilCode.decode(myStr.matchValue("_mailTo[",
		    "]mailTo_"));
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    String opinion = UtilCode.decode(myStr.matchValue("_opinion[",
		    "]opinion_"));
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    myOut.write(web.addParticipantsTask(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), mailTo, title, opinion));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_AddParticipants_Ok")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    myOut.write(web.execAddParticipantsTask(me, Integer.parseInt(id),
		    Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_SendRead_Win")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = UtilCode.decode(myStr.matchValue("_readToType[",
		    "]readToType_"));
	    myOut.write(web.getCCTaskPage(me, Integer.parseInt(p1),
		    Integer.parseInt(p2), p3, ""));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_SendRead_Send")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String mailTo = "";
	    String p1 = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String p2 = UtilCode.decode(myStr.matchValue("_p2[", "]p2_"));
	    if (p2 != null)
		p2 = p2.replaceAll(" +", " ");
	    String p3 = UtilCode.decode(myStr.matchValue("_p3[", "]p3_"));
	    String p4 = UtilCode.decode(myStr.matchValue("_p4[", "]p4_"));
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    if (p2 != null && !p2.equals("") && p4 != null && !p4.equals(""))
		mailTo = p2.trim() + p4;
	    else if (p2 != null && !p2.equals("")
		    && (p4 == null || p4.equals("")))
		mailTo = p2;
	    else if (p4 != null && !p4.equals("")
		    && (p2 == null || p2.equals("")))
		mailTo = p4.trim();
	    myOut.write(web.execCCTask(me, Integer.parseInt(p1),
		    Integer.parseInt(taskId), mailTo, p3));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_ArchivesWin")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String id = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String volumeId = myCmdArray.elementAt(5).toString();
	    String storeRoomName = UtilCode.decode(myStr.matchValue(
		    "_storeRoomName[", "]storeRoomName_"));
	    if (volumeId == null || volumeId.equals(""))
		volumeId = "0";
	    if (storeRoomName == null)
		storeRoomName = "";
	    myOut.write(web.getArchivesList(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), storeRoomName,
		    Integer.parseInt(volumeId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Archives")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String id = myCmdArray.elementAt(3).toString();
	    String volumeId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.toArchives(me, Integer.parseInt(id),
		    Integer.parseInt(volumeId), Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Transaction_Check")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getExecDialog(me, Integer.parseInt(p1),
		    Integer.parseInt(p2)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Transaction_Urge")) {
	    UserTaskUrgeWeb web = new UserTaskUrgeWeb(me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    String stepNo = myCmdArray.elementAt(4).toString();
	    String bindId = myCmdArray.elementAt(5).toString();
	    String taskId = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getMainPage(me, Integer.parseInt(bindId),
		    Integer.parseInt(flowId), Integer.parseInt(stepNo),
		    Integer.parseInt(stepNo), Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_View_Urge")) {
	    UserTaskUrgeWeb web = new UserTaskUrgeWeb(me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    String stepNo = myCmdArray.elementAt(4).toString();
	    String nowStepNo = myCmdArray.elementAt(5).toString();
	    String bindId = myCmdArray.elementAt(6).toString();
	    String taskId = myCmdArray.elementAt(7).toString();
	    myOut.write(web.getMainPage(me, Integer.parseInt(bindId),
		    Integer.parseInt(flowId), Integer.parseInt(stepNo),
		    Integer.parseInt(nowStepNo), Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Transaction_SpecifyUser")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getParticipantPage(me, Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Transaction_Entrust_Check")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getEntrustSendPageOf2(me, Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_SelfDispose")) {
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String stepId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String routePointValue = myCmdArray.elementAt(6).toString();
	    String openstate = myCmdArray.elementAt(7).toString();
	    myOut.write(web.changeToSelfDispose(Integer.parseInt(id),
		    Integer.parseInt(openstate), Integer.parseInt(taskId), 0,
		    Integer.parseInt(routePointValue), Integer.parseInt(stepId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Transaction_Send")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(6).toString();
	    String p5 = myCmdArray.elementAt(7).toString();
	    String localDepartmentId = myCmdArray.elementAt(8).toString();
	    if (localDepartmentId == null || localDepartmentId.equals(""))
		localDepartmentId = "0";
	    String p6 = UtilCode.decode(myStr.matchValue("_p6[", "]p6_"));
	    String p7 = UtilCode.decode(myStr.matchValue("_p7[", "]p7_"));
	    String isShortMessageValue = UtilCode.decode(myStr.matchValue(
		    "_isShortMessage[", "]isShortMessage_"));
	    String p9 = UtilCode.decode(myStr.matchValue("_p9[", "]p9_"));
	    String togetherMailTo = UtilCode.decode(myStr.matchValue(
		    "_togetherMailTo[", "]togetherMailTo_"));
	    String mailTo = p6.trim();
	    if (togetherMailTo != null && !togetherMailTo.equals(""))
		mailTo = mailTo + togetherMailTo;
	    boolean isShortMessage = false;
	    if (isShortMessageValue != null && isShortMessageValue.equals("1"))
		isShortMessage = true;
	    myOut.write(web.pushNext(me, Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3),
		    Integer.parseInt(p4), Integer.parseInt(p5), mailTo, p7,
		    Integer.parseInt(p9), isShortMessage,
		    Integer.parseInt(localDepartmentId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Transaction_Entrust_Send")) {
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String p4 = myCmdArray.elementAt(6).toString();
	    String p5 = myCmdArray.elementAt(7).toString();
	    String localDepartmentId = myCmdArray.elementAt(8).toString();
	    if (localDepartmentId == null || localDepartmentId.equals(""))
		localDepartmentId = "0";
	    String p6 = UtilCode.decode(myStr.matchValue("_p6[", "]p6_"));
	    String p7 = UtilCode.decode(myStr.matchValue("_p7[", "]p7_"));
	    String p8 = UtilCode.decode(myStr.matchValue("_p8[", "]p8_"));
	    String p9 = UtilCode.decode(myStr.matchValue("_p9[", "]p9_"));
	    String mailTo = p6;
	    myOut.write(web.sendEntrustMessage(me, Integer.parseInt(p1),
		    Integer.parseInt(p2), Integer.parseInt(p3),
		    Integer.parseInt(p4), Integer.parseInt(p5), mailTo, p7,
		    Integer.parseInt(p8), Integer.parseInt(p9),
		    Integer.parseInt(localDepartmentId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Urge_Save")) {
	    UserTaskUrgeWeb web = new UserTaskUrgeWeb(me);
	    String bindId = myCmdArray.elementAt(3).toString();
	    String stepNo = myCmdArray.elementAt(4).toString();
	    String stepToNo = myCmdArray.elementAt(5).toString();
	    String flowId = myCmdArray.elementAt(6).toString();
	    String taskId = myCmdArray.elementAt(7).toString();
	    String urgeContent = UtilCode.decode(myStr.matchValue(
		    "_urgeContent[", "]urgeContent_"));
	    if (urgeContent == null)
		urgeContent = "";
	    UrgeUtil urgeControl = new UrgeUtil();
	    urgeControl.selectUrge(Integer.parseInt(bindId),
		    Integer.parseInt(stepNo), Integer.parseInt(stepToNo),
		    urgeContent, Integer.parseInt(flowId));
	    if (urgeContent != null) {
		UserTaskHistoryOpinionModel model = new UserTaskHistoryOpinionModel();
		ProcessInstanceModel _instanceModel = ProcessRuntimeDaoFactory
			.createProcessInstance().getInstance(
				Integer.parseInt(bindId));
		model.setCreateUser(me.getUID());
		model.setOpinion(urgeContent
			+ "<br><br><b><font color=red><I18N#特事特办></font></b>");
		model.setAuditMenuName("-");
		model.setProcessInstanceId(Integer.parseInt(bindId));
		try {
		    model.setTaskInstanceId(new DBSequence()
			    .getSequence("SYS_WORKFLOWOPINION"));
		} catch (SequenceException e) {
		    e.printStackTrace(System.err);
		}
		model.setAuditObject("<span style=font-size:14px><b><I18N#"
			+ WorkFlowStepCache.getModelOfStepNo(
				_instanceModel.getProcessDefinitionId(),
				_instanceModel.getActivityDefinitionNo())._stepName
			+ "></b></span>");
		ProcessRuntimeDaoFactory.createUserTaskHistoryOpinion().create(
			model);
	    }
	    myOut.write(web.executeJump(Integer.parseInt(bindId),
		    Integer.parseInt(taskId), Integer.parseInt(stepToNo)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Re_Disaccord")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    myOut.write(web.getModifySuccessfullyPage(me, Integer.parseInt(p1),
		    Integer.parseInt(p2)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Printer_Open")) {
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String activityInstanceId = myCmdArray.elementAt(4).toString();
	    if (activityInstanceId == null || activityInstanceId.equals(""))
		activityInstanceId = "0";
	    String formId = myCmdArray.elementAt(5).toString();
	    if (formId == null || formId.equals(""))
		formId = "0";
	    String nstep = myCmdArray.elementAt(6).toString();
	    if (nstep == null || nstep.equals(""))
		nstep = "0";
	    UserTaskFormPrintWeb web = new UserTaskFormPrintWeb(me);
	    myOut.write(web.getPrintPage(Integer.parseInt(processInstanceId),
		    Integer.parseInt(activityInstanceId),
		    Integer.parseInt(formId), Integer.parseInt(nstep)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_P_Save_Next")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId));
	    myOut.write(web.saveFormDataNext(bindValue));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_P_Save_Next_Identity_Confirm_OK")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String from = UtilCode.decode(myStr.matchValue("_from[", "]from_"));
	    UserTaskExecuteWeb web = new UserTaskExecuteWeb();
	    myOut.write(web.getExecDialog(me, Integer.parseInt(p1),
		    Integer.parseInt(p2), from));
	    web = null;
	} else if (socketCmd.equals("ProcessTaskInstance_EAI_Execute_Next")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId), 1, 0, 0);
	    myOut.write(web.saveFormDataNext(bindValue));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_P_Save")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String pageNow = myCmdArray.elementAt(8).toString();
	    String subSheetId = myCmdArray.elementAt(9).toString();
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (subSheetId == null || subSheetId.equals(""))
		subSheetId = "1";
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    myOut.write(web.saveFormData(bindValue, Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_P_Save_Ajax")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String pageNow = myCmdArray.elementAt(8).toString();
	    String subSheetId = myCmdArray.elementAt(9).toString();
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (subSheetId == null || subSheetId.equals(""))
		subSheetId = "1";
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    myOut.write(web.saveFormDataAjax(bindValue,
		    Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_ExtendButton_Exec")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String pageNow = myCmdArray.elementAt(8).toString();
	    String btid = myCmdArray.elementAt(9).toString();
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager rm = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId));
	    java.util.Hashtable fd = rm.convertBindData2Hashtable(
		    new UtilString(bindValue),
		    SheetCache.getMastSheetModel(Integer.parseInt(formId)));
	    myOut.write(web.butExec(id, taskId, formId, btid, fd));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Refresh")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String pageNow = myCmdArray.elementAt(8).toString();
	    String subSheetId = myCmdArray.elementAt(9).toString();
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (subSheetId == null || subSheetId.equals(""))
		subSheetId = "1";
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    myOut.write(web.getFormPage(Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_SubBindReport_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = "0";
	    String formId = myCmdArray.elementAt(7).toString();
	    int instanceId = Integer.parseInt(id);
	    RuntimeFormManager web = new RuntimeFormManager(me, instanceId,
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(meId), Integer.parseInt(formId));
	    myOut.write(web.getFormPage(1));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Snapshot_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String meId = myCmdArray.elementAt(4).toString();
	    UserTaskFormPrintWeb web = new UserTaskFormPrintWeb(me);
	    myOut.write(web.getSnapshotPrinterPage(Integer.parseInt(id),
		    Integer.parseInt(meId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Audit_Save")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String auditId = myCmdArray.elementAt(7).toString();
	    String tmpAuditType = UtilCode.decode(myStr.matchValue(
		    "_auditType[", "]auditType_"));
	    String auditResult = "";
	    String auditType = "";
	    if (tmpAuditType != null && !tmpAuditType.equals("")) {
		auditResult = tmpAuditType.substring(0,
			tmpAuditType.indexOf("/"));
		auditType = tmpAuditType.substring(
			tmpAuditType.indexOf("/") + 1, tmpAuditType.length());
	    } else {
		auditResult = "";
		auditType = "-99";
	    }
	    String opinion = UtilCode.decode(myStr.matchValue("_opinion[",
		    "]opinion_"));
	    UserTaskAuditMenuModel model = new UserTaskAuditMenuModel();
	    model.setAuditType(Integer.parseInt(auditType));
	    model.setAuditMenuName(auditResult);
	    model.setOpinion(opinion);
	    model.setTaskInstanceId(Integer.parseInt(taskId));
	    model.setId(Integer.parseInt(auditId));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId), 0);
	    myOut.write(web.saveAuditData(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Audit_Save_NoPage")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String auditId = myCmdArray.elementAt(7).toString();
	    String tmpAuditType = UtilCode.decode(myStr.matchValue(
		    "_auditType[", "]auditType_"));
	    String auditResult = "";
	    String auditType = "";
	    tmpAuditType = AjaxDataDecode.getInstance().decode(tmpAuditType);
	    if (tmpAuditType != null && !tmpAuditType.trim().equals("")) {
		auditResult = tmpAuditType.substring(0,
			tmpAuditType.indexOf("/"));
		auditType = tmpAuditType.substring(
			tmpAuditType.indexOf("/") + 1, tmpAuditType.length());
	    } else {
		auditResult = "";
		auditType = "-99";
	    }
	    String opinion = UtilCode.decode(myStr.matchValue("_opinion[",
		    "]opinion_"));
	    UserTaskAuditMenuModel model = new UserTaskAuditMenuModel();
	    StringBuffer sql = new StringBuffer();
	    sql.append("SELECT  id as c FROM ").append("wf_messageaudit")
		    .append(" WHERE TASK_ID=").append(taskId);
	    int auid = DBSql.getInt(sql.toString(), "c");
	    if (auid > 0)
		auditId = String.valueOf(auid);
	    model.setAuditType(Integer.parseInt(auditType));
	    model.setAuditMenuName(auditResult);
	    model.setOpinion(AjaxDataDecode.getInstance().decode(opinion));
	    model.setTaskInstanceId(Integer.parseInt(taskId));
	    model.setId(Integer.parseInt(auditId));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId), 0);
	    myOut.write(web.saveAuditDataNoPage(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Audit_Attachment_List")) {
	    String taskId = myCmdArray.elementAt(3).toString();
	    String processInstanceId = myCmdArray.elementAt(4).toString();
	    UserTaskAuditMenuWeb web = new UserTaskAuditMenuWeb();
	    myOut.write(web.getAuditAttachmentList(me,
		    Integer.parseInt(taskId),
		    Integer.parseInt(processInstanceId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Audit_Attachment_Remove")) {
	    String taskId = myCmdArray.elementAt(3).toString();
	    String processInstanceId = myCmdArray.elementAt(4).toString();
	    String fn = UtilCode.decode(myStr.matchValue("_fn[", "]fn_"));
	    UserTaskAuditMenuWeb web = new UserTaskAuditMenuWeb();
	    myOut.write(web.removeFile(Integer.parseInt(taskId),
		    Integer.parseInt(processInstanceId), fn));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String selectId = myCmdArray.elementAt(6).toString();
	    String meId = myCmdArray.elementAt(7).toString();
	    String formId = myCmdArray.elementAt(8).toString();
	    String subSheetId = myCmdArray.elementAt(9).toString();
	    boolean hideBt = Boolean.parseBoolean(myCmdArray.elementAt(10)
		    .toString());
	    String formType = myCmdArray.elementAt(11).toString();
	    if (formType == null || formType.trim().equals(""))
		formType = "1";
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    UtilString us1 = new UtilString(bindValue);
	    bindValue = us1.replace("'", "’");
	    if (meId == null || meId.length() == 0)
		meId = "0";
	    if (selectId == null || selectId.length() == 0)
		selectId = "0";
	    if (taskId == null || taskId.length() == 0)
		taskId = "0";
	    SubForm web = new SubForm(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(meId), Integer.parseInt(formId),
		    Integer.parseInt(subSheetId));
	    if (bindValue.length() > 0)
		web.saveFormData(bindValue);
	    web.setBusinessObjectId(Integer.parseInt(selectId));
	    web.setFormType(Integer.parseInt(formType));
	    web.setShowPreNextButton(!hideBt);
	    web.setRefId(Integer.parseInt(selectId) == 0 ? Integer
		    .parseInt(meId) : Integer.parseInt(selectId));
	    myOut.write(web.getFormPage(Integer.parseInt(selectId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_SS_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String selectId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String pageNow = myCmdArray.elementAt(9).toString();
	    if (selectId == null || selectId.length() == 0)
		selectId = "0";
	    if (taskId == null || taskId.length() == 0)
		taskId = "0";
	    if (pageNow == null || "".equals(pageNow))
		pageNow = "1";
	    RefForm web = new RefForm(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(selectId), Integer.parseInt(formId),
		    Integer.parseInt(subSheetId));
	    myOut.write(web.getRefPage(Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Open_Pre")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    if (meId == null || meId.length() == 0)
		meId = "0";
	    if (taskId == null || taskId.length() == 0)
		taskId = "0";
	    if (formId == null || "".equals(formId))
		formId = "0";
	    SubForm web = new SubForm(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(meId), Integer.parseInt(formId),
		    Integer.parseInt(subSheetId));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    if (bindValue.length() > 0)
		web.saveHtmlSheetData(bindValue, Integer.parseInt(subSheetId));
	    int pre = FormUtil.nav(Integer.parseInt(meId),
		    Integer.parseInt(subSheetId), true, web);
	    web.setBusinessObjectId(pre);
	    myOut.write(web.getFormPage(pre));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Open_Next")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    if (meId == null || meId.length() == 0)
		meId = "0";
	    if (taskId == null || taskId.length() == 0)
		taskId = "0";
	    if (formId == null || "".equals(formId))
		formId = "0";
	    SubForm web = new SubForm(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(meId), Integer.parseInt(formId),
		    Integer.parseInt(subSheetId));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    if (bindValue.length() > 0)
		web.saveHtmlSheetData(bindValue, Integer.parseInt(subSheetId));
	    int next = FormUtil.nav(Integer.parseInt(meId),
		    Integer.parseInt(subSheetId), false, web);
	    web.setBusinessObjectId(next);
	    myOut.write(web.getFormPage(next));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Remove")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String selectId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    String nowSubSheetId = myCmdArray.elementAt(10).toString();
	    String formType = myCmdArray.elementAt(11).toString();
	    if (nowSubSheetId.equals(""))
		nowSubSheetId = "0";
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(selectId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    try {
		web.setFormType(Integer.parseInt(formType));
	    } catch (Exception exception2) {
	    }
	    myOut.write(web.removeHtmlSheetData(Integer.parseInt(subSheetId),
		    Integer.parseInt(meId), Integer.parseInt(nowSubSheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Remove2")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String selectId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    String nowSubSheetId = myCmdArray.elementAt(10).toString();
	    if (nowSubSheetId.equals(""))
		nowSubSheetId = "0";
	    if (state.equals(""))
		state = "0";
	    taskId = selectId;
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(selectId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    myOut.write(web.removeHtmlSheetData2(Integer.parseInt(subSheetId),
		    Integer.parseInt(meId), Integer.parseInt(nowSubSheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_RemoveAll")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String formId = myCmdArray.elementAt(6).toString();
	    String subSheetId = myCmdArray.elementAt(7).toString();
	    String meId = myCmdArray.elementAt(8).toString();
	    String nowSubSheetId = myCmdArray.elementAt(9).toString();
	    String formType = myCmdArray.elementAt(10).toString();
	    if (nowSubSheetId.equals(""))
		nowSubSheetId = "0";
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(formId));
	    try {
		web.setFormType(Integer.parseInt(formType));
	    } catch (Exception exception1) {
	    }
	    myOut.write(web.removeAllHtmlSheetData(
		    Integer.parseInt(subSheetId), Integer.parseInt(meId),
		    Integer.parseInt(nowSubSheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Copy")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = "0";
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String count = myCmdArray.elementAt(9).toString();
	    if (count.equals(""))
		count = "0";
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    myOut.write(web.copyHtmlSheetData(bindValue,
		    Integer.parseInt(subSheetId), Integer.parseInt(count)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_S_Save")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(state), Integer.parseInt(meId),
		    Integer.parseInt(formId), Integer.parseInt(subSheetId));
	    web.setShowPreNextButton(!Boolean.parseBoolean(myCmdArray
		    .elementAt(9).toString()));
	    myOut.write(web.saveHtmlSheetData(bindValue,
		    Integer.parseInt(subSheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Sub_Refresh")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String pageNow = myCmdArray.elementAt(9).toString();
	    String formType = myCmdArray.elementAt(10).toString();
	    SubForm web = new SubForm(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(meId), Integer.parseInt(formId),
		    Integer.parseInt(subSheetId));
	    web.setPageNow(Integer.parseInt(pageNow));
	    web.setFormType(Integer.parseInt(formType));
	    myOut.write(web.getHtmlSheetForm(Integer.parseInt(subSheetId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_New_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String subSheetId = myCmdArray.elementAt(8).toString();
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    UtilString us1 = new UtilString(bindValue);
	    bindValue = us1.replace("'", "’");
	    SubForm web = new SubForm(me, Integer.parseInt(id),
		    Integer.parseInt(taskId), Integer.parseInt(state),
		    Integer.parseInt(meId), Integer.parseInt(formId),
		    Integer.parseInt(subSheetId));
	    web.saveData(bindValue);
	    web.setShowPreNextButton(!Boolean.parseBoolean(myCmdArray
		    .elementAt(9).toString()));
	    if (FormUtil.isFieldSubSheet(Integer.parseInt(subSheetId))) {
		int refId = FormUtil.getFieldParent(Integer.parseInt(meId),
			Integer.parseInt(subSheetId));
		web.setRefId(refId);
	    }
	    web.setBusinessObjectId(0);
	    myOut.write(web.getFormPage(0));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_HistorySearch_List")) {
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String resultType = myCmdArray.elementAt(4).toString();
	    String sql = UtilCode.decode(myStr.matchValue("_sql[", "]sql_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (resultType == null || resultType.equals(""))
		resultType = "1";
	    myOut.write(new HistorySearchWeb(me).getSearchWeb(sql,
		    Integer.parseInt(pageNow), Integer.parseInt(resultType)));
	} else if (socketCmd.equals("WorkFlow_HistorySearch_Result")) {
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String resultType = myCmdArray.elementAt(4).toString();
	    String sql = UtilCode.decode(myStr.matchValue("_sql[", "]sql_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (resultType == null || resultType.equals(""))
		resultType = "1";
	    myOut.write(new HistorySearchWeb(me).getSearchResult(sql,
		    Integer.parseInt(pageNow), Integer.parseInt(resultType)));
	} else if (socketCmd.equals("Electroncachet_Manager_Open")) {
	    ElectroncachetCardWeb web = new ElectroncachetCardWeb(me);
	    String uid = myCmdArray.elementAt(3).toString();
	    String pageType = myCmdArray.elementAt(4).toString();
	    if (pageType == null || pageType.equals(""))
		pageType = "1";
	    myOut.write(web.getElectroncachetPage(uid,
		    Integer.parseInt(pageType), ""));
	    web = null;
	} else if (socketCmd.equals("Electroncache_Design_Rebuild")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String cachetId = myCmdArray.elementAt(3).toString();
	    ElectroncachetDefModel model = (ElectroncachetDefModel) ElectroncachetDefCache
		    .getModel(Integer.parseInt(cachetId));
	    MD5 md5 = new MD5();
	    if (model != null)
		model._cachetPassword = md5.toDigest("123456");
	    myOut.write(web.saveEletroncachetData(model, me.getUID()));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_CachetCreate_Open")) {
	    ElectroncachetHistoryInfoWeb web = new ElectroncachetHistoryInfoWeb(
		    me);
	    String uid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getHistoryInfoList(uid, ""));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Create_Open")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String uid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getElectroncachetCreatePage(0, uid));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Info_Open")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String cachetId = myCmdArray.elementAt(3).toString();
	    String uid = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getElectroncachetUpdatePage(
		    Integer.parseInt(cachetId), uid));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Search_Open")) {
	    ElectroncachetCardWeb web = new ElectroncachetCardWeb(me);
	    String pageType = myCmdArray.elementAt(3).toString();
	    String cachefilter = UtilCode.decode(myStr.matchValue(
		    "_cachefilter[", "]cachefilter_"));
	    myOut.write(web.getElectroncachetPage(me.getUID(),
		    Integer.parseInt(pageType), cachefilter));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Attach_Upload")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String companyId = myCmdArray.elementAt(3).toString();
	    String cachetId = myCmdArray.elementAt(9).toString();
	    myOut.write(web.upFile(Integer.parseInt(cachetId),
		    Integer.parseInt(companyId)));
	    web = null;
	} else if (socketCmd.equals("Electroncache_Attach_Open")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String oldPassword = myCmdArray.elementAt(3).toString();
	    String cachetId = myCmdArray.elementAt(4).toString();
	    String uid = myCmdArray.elementAt(5).toString();
	    myOut.write(web.printGetMessage3(Integer.parseInt(cachetId), uid,
		    oldPassword));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Create_Del")) {
	    ElectroncachetCardWeb web = new ElectroncachetCardWeb(me);
	    String uid = myCmdArray.elementAt(3).toString();
	    String cCachet = UtilCode.decode(myStr.matchValue("_list[",
		    "]list_"));
	    myOut.write(web.removeCachetData(cCachet, uid));
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Attach_Remove")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String oldPassword = myCmdArray.elementAt(3).toString();
	    String cachetId = myCmdArray.elementAt(4).toString();
	    String companyId = myCmdArray.elementAt(5).toString();
	    String fn = UtilCode.decode(myStr.matchValue("_fn[", "]fn_"));
	    myOut.write(web.removeFile(Integer.parseInt(companyId),
		    Integer.parseInt(cachetId), fn, oldPassword));
	    web = null;
	} else if (socketCmd.equals("Electroncache_Design_Create")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    String companyId = myCmdArray.elementAt(3).toString();
	    String cachetGroup = myCmdArray.elementAt(4).toString();
	    String cachetNo = myCmdArray.elementAt(5).toString();
	    String cachetOwner = myCmdArray.elementAt(6).toString();
	    String confirmPassword = myCmdArray.elementAt(7).toString();
	    String isDisplayDate = myCmdArray.elementAt(8).toString();
	    if (confirmPassword == null)
		confirmPassword = "";
	    String cachetManager = UtilCode.decode(
		    myStr.matchValue("_list[", "]list_")).trim();
	    ElectroncachetDefModel eModel = (ElectroncachetDefModel) ElectroncachetDefCache
		    .getCachetObject(cachetNo);
	    ElectroncachetDefModel model = new ElectroncachetDefModel();
	    model._companyId = Integer.parseInt(companyId);
	    model._cachetGroup = cachetGroup;
	    model._cachetNo = cachetNo;
	    model._cachetOwner = cachetOwner;
	    model._cachetManager = cachetManager;
	    model._isDisplayDate = Integer.parseInt(isDisplayDate) == 1;
	    MD5 md5 = new MD5();
	    model._cachetPassword = md5.toDigest(confirmPassword);
	    if (eModel != null)
		myOut.write(web.alertMessage("提示", "你不能创建印章", "输入的印章编号已经存在"));
	    else
		myOut.write(web.saveEletroncachetData(model, me.getUID()));
	    web = null;
	} else if (socketCmd.equals("Electroncache_Design_Update")) {
	    ElectroncachetCreateWeb web = new ElectroncachetCreateWeb(me);
	    MD5 md5 = new MD5();
	    String companyId = myCmdArray.elementAt(3).toString();
	    String cachetGroup = myCmdArray.elementAt(4).toString();
	    String cachetNo = myCmdArray.elementAt(5).toString();
	    String cachetOwner = myCmdArray.elementAt(6).toString();
	    String oldPassword = myCmdArray.elementAt(7).toString();
	    String confirmPassword = myCmdArray.elementAt(8).toString();
	    String cachetId = myCmdArray.elementAt(9).toString();
	    String isDisplayDate = myCmdArray.elementAt(10).toString();
	    String cachetManager = UtilCode.decode(
		    myStr.matchValue("_list[", "]list_")).trim();
	    ElectroncachetDefModel eModel2 = (ElectroncachetDefModel) ElectroncachetDefCache
		    .getModel(Integer.parseInt(cachetId));
	    ElectroncachetDefModel model = (ElectroncachetDefModel) ElectroncachetDefCache
		    .getModel(Integer.parseInt(cachetId));
	    model._companyId = Integer.parseInt(companyId);
	    model._cachetGroup = cachetGroup;
	    model._cachetNo = cachetNo;
	    model._cachetOwner = cachetOwner;
	    model._cachetManager = cachetManager;
	    model._isDisplayDate = Integer.parseInt(isDisplayDate) == 1;
	    if (eModel2 != null)
		if (!eModel2._cachetPassword.equals(md5.toDigest(oldPassword))) {
		    myOut.write(web.alertMessage("提示", "不能修改印章信息",
			    "输入的印章历史密码不正确"));
		} else {
		    model._cachetPassword = md5.toDigest(confirmPassword);
		    myOut.write(web.saveEletroncachetData(model, me.getUID()));
		}
	    web = null;
	} else if (socketCmd.equals("Electroncachet_Add_Open")) {
	    ElectroncachetCheckPasswordWeb web = new ElectroncachetCheckPasswordWeb(
		    me);
	    String cachetNo = myCmdArray.elementAt(3).toString();
	    String refUserId = myCmdArray.elementAt(4).toString();
	    String refInstanceId = myCmdArray.elementAt(5).toString();
	    String refTaskId = myCmdArray.elementAt(6).toString();
	    String formId = myCmdArray.elementAt(7).toString();
	    String openState = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    String refFullName = UtilCode.decode(myStr.matchValue(
		    "_refFullName[", "]refFullName_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    myOut.write(web.getCheckPasswordWeb(cachetNo, refUserId,
		    refFullName, Integer.parseInt(refInstanceId),
		    Integer.parseInt(refTaskId), Integer.parseInt(formId),
		    Integer.parseInt(openState), Integer.parseInt(meId),
		    bindValue));
	    web = null;
	} else if (socketCmd.equals("Electroncache_Check_Password")) {
	    ElectroncachetCheckPasswordWeb web = new ElectroncachetCheckPasswordWeb(
		    me);
	    String cachetId = myCmdArray.elementAt(3).toString();
	    String cachetNo = myCmdArray.elementAt(4).toString();
	    String cachetName = myCmdArray.elementAt(5).toString();
	    String refUserId = myCmdArray.elementAt(6).toString();
	    String refInstanceId = myCmdArray.elementAt(7).toString();
	    String refTaskId = myCmdArray.elementAt(8).toString();
	    String refFullName = UtilCode.decode(
		    myStr.matchValue("_fullName[", "]fullName_")).trim();
	    String password = UtilCode.decode(
		    myStr.matchValue("_pwd[", "]pwd_")).trim();
	    ElectroncachetRefModel cModel = new ElectroncachetRefModel();
	    cModel._cachetId = Integer.parseInt(cachetId);
	    cModel._cachetNo = cachetNo;
	    cModel._cachetName = cachetName;
	    cModel._refUserId = refUserId;
	    cModel._refFullName = refFullName;
	    cModel._refInstanceId = Integer.parseInt(refInstanceId);
	    cModel._refTaskId = Integer.parseInt(refTaskId);
	    ElectroncachetDefModel model = (ElectroncachetDefModel) ElectroncachetDefCache
		    .getModel(Integer.parseInt(cachetId));
	    MD5 md5 = new MD5();
	    if (md5.toDigest(password).equals(model._cachetPassword)) {
		web.saveElectroncacheRefData(cModel);
		myOut.write(web.alertMessage(I18nRes.findValue(
			me.getLanguage(), "印章密码验证通过")));
	    } else {
		myOut.write(web.alertMessage(I18nRes.findValue(
			me.getLanguage(), "密码错误")));
	    }
	    web = null;
	} else if (socketCmd.equals("ElectroncachetView_Info_Open")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    RuntimeFormManager web = new RuntimeFormManager(me,
		    Integer.parseInt(instanceId), Integer.parseInt(taskId), 5,
		    0);
	    myOut.write(web.getFormPage(1));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_FileLongValue_Open")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String entityName = myCmdArray.elementAt(4).toString();
	    String fieldName = myCmdArray.elementAt(5).toString();
	    DiggerExecuteWeb web = new DiggerExecuteWeb(me);
	    myOut.write(web.getLongValueWeb(Integer.parseInt(instanceId),
		    entityName, fieldName));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_Reconvert_Form")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.getReconvertFormPage(Integer.parseInt(instanceId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_Reconvert_Exec")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String departmentId = myCmdArray.elementAt(4).toString();
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.executeReconvert(Integer.parseInt(instanceId),
		    Integer.parseInt(departmentId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFG_FindACPage")) {
	    WFDesignGroupFindACWeb web = new WFDesignGroupFindACWeb(me);
	    myOut.write(web.getWFGroupFindACPage());
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_CheckIsExistBOData")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    myOut.write(web.checkExistBOData(Integer.parseInt(instanceId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_TempRemove")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    UserTaskFormsWeb web = new UserTaskFormsWeb(me);
	    myOut.write(web.removeTempInstance(Integer.parseInt(instanceId)));
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    String msgId = "";
	    try {
		HttpClient client = new HttpClient();
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(3000);
		connectionManager.getParams().setSoTimeout(3000);
		client.setHttpConnectionManager(connectionManager);
		conn = DBSql.open();
		ps = conn
			.prepareStatement("select MSGID from WF_MESSAGE_INTERFACE where BINDID = ?");
		ps.setInt(1, Integer.parseInt(instanceId));
		rs = ps.executeQuery();
		while (rs.next()) {
		    msgId = rs.getString("MSGID") == null ? "" : rs
			    .getString("MSGID");
		    if (msgId.length() > 0) {
			JSONObject content = new JSONObject();
			content.put("status", "delete");
			content.put("msgId", msgId);
			new UpdateWorkMsgThread(client, content, msgId, "D")
				.start();
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    } finally {
		DBSql.close(conn, ps, rs);
	    }
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Execute_API_OpenForm")) {
	    String boId = myCmdArray.elementAt(3).toString();
	    String workflowUUID = myCmdArray.elementAt(4).toString();
	    String formUUID = myCmdArray.elementAt(5).toString();
	    String workflowAction = myCmdArray.elementAt(6).toString();
	    String boName = myCmdArray.elementAt(7).toString();
	    myOut.write(WorkflowFormUtil.getForm(me, workflowUUID, formUUID,
		    workflowAction, boName, Integer.parseInt(boId)));
	} else if (socketCmd.equals("WorkFlow_Design_WFS_SettingRouteRef")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String routeType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getRouteRefPage(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(routeType)));
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_SettingRouteRef_AjaxRoleGroup")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String param2 = UtilCode.decode(myStr.matchValue("_param2[",
		    "]param2_"));
	    String param3 = UtilCode.decode(myStr.matchValue("_param3[",
		    "]param3_"));
	    myOut.write(web.getJsonOfRoleGroup(requestType, param1, param2,
		    param3));
	} else if (socketCmd.equals("WorkFlow_Design_WFS_SettingRouteRef_Save")) {
	    WFDesignStepBaseDataTabWeb web = new WFDesignStepBaseDataTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String routeType = myCmdArray.elementAt(5).toString();
	    String opt = myCmdArray.elementAt(6).toString();
	    String sourceId = UtilCode.decode(myStr.matchValue("_sourceId[",
		    "]sourceId_"));
	    myOut.write(web.saveRouteRefObj(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(routeType), sourceId,
		    Integer.parseInt(opt)));
	} else if (socketCmd.equals("AWS_Sys_ModelCategory_List")) {
	    CategoryWeb web = new CategoryWeb(me);
	    myOut.write(web.getList());
	} else if (socketCmd.equals("AWS_Sys_ModelCategory_Create")) {
	    CategoryWeb web = new CategoryWeb(me);
	    String categoryName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.createCategory(categoryName));
	    web = null;
	} else if (socketCmd.equals("AWS_Sys_ModelCategory_Modify")) {
	    CategoryWeb web = new CategoryWeb(me);
	    String id = UtilCode.decode(myStr.matchValue("_id[", "]id_"))
		    .trim();
	    String categoryName = UtilCode.decode(myStr.matchValue("_name[",
		    "]name_"));
	    myOut.write(web.modifyCategory(Integer.parseInt(id), categoryName));
	} else if (socketCmd.equals("AWS_Sys_ModelCategory_Remove")) {
	    CategoryWeb web = new CategoryWeb(me);
	    String ids = UtilCode.decode(myStr.matchValue("_ids[", "]ids_"));
	    myOut.write(web.removeCategory(ids));
	} else if (socketCmd.equals("AWS_Sys_ModelCategory_Merge")) {
	    CategoryWeb web = new CategoryWeb(me);
	    String categoryName = myCmdArray.elementAt(3).toString();
	    String ids = UtilCode.decode(myStr.matchValue("_ids[", "]ids_"));
	    myOut.write(web.mergeCategory(ids, categoryName));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String pageNow = myCmdArray.elementAt(6).toString();
	    String subSheetId = myCmdArray.elementAt(7).toString();
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(openState), 55029, 0);
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    myOut.write(reportWeb.getAjaxSheetList(sheetModel,
		    Integer.parseInt(pageNow)));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_ReadTGJson")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String start = myCmdArray.elementAt(6).toString();
	    String subSheetId = myCmdArray.elementAt(7).toString();
	    String anode = myCmdArray.elementAt(8).toString();
	    if (anode == null || "".equals(anode))
		anode = "0";
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(openState), 0);
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    myOut.write(FormUtil.getAjaxSheetTGJSON(reportWeb, sheetModel,
		    Integer.parseInt(start), anode));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_ReadXML")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String pageNow = myCmdArray.elementAt(6).toString();
	    String subSheetId = myCmdArray.elementAt(7).toString();
	    String start = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    try {
		if (start != null && !"".equals(start)
			&& sheetModel.getPageNum() > 0)
		    pageNow = Integer.toString(Integer.parseInt(start)
			    / sheetModel.getPageNum() + 1);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    String filterStr = UtilCode.decode(myStr.matchValue("_filterStr[",
		    "]filterStr_"));
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(openState), Integer.parseInt(meId), 0);
	    myOut.write(FormUtil.getAjaxSheetDataXML(reportWeb, sheetModel,
		    Integer.parseInt(pageNow), filterStr));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_Save")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String pageNow = myCmdArray.elementAt(6).toString();
	    String subSheetId = myCmdArray.elementAt(7).toString();
	    String bindDataStr = UtilCode.decode(myStr.matchValue(
		    "_bindDataStr***[", "]***bindDataStr_"));
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(openState), 0);
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    String s = reportWeb.saveAjaxSheetData(sheetModel, bindDataStr,
		    Integer.parseInt(pageNow));
	    myOut.write(s);
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_Remove")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String pageNow = myCmdArray.elementAt(6).toString();
	    String subSheetId = myCmdArray.elementAt(7).toString();
	    String selectedKeys = UtilCode.decode(myStr.matchValue(
		    "_selectedKeys[", "]selectedKeys_"));
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId),
		    Integer.parseInt(openState), 0);
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    myOut.write(reportWeb.removeAjaxSheetData(sheetModel, selectedKeys,
		    Integer.parseInt(pageNow)));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_CreateDefauleValue")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String subSheetId = myCmdArray.elementAt(5).toString();
	    String meId = myCmdArray.elementAt(6).toString();
	    RuntimeFormManager reportWeb = new RuntimeFormManager(me,
		    Integer.parseInt(id), Integer.parseInt(taskId), 1,
		    Integer.parseInt(meId), 0, Integer.parseInt(subSheetId));
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    myOut.write(FormUtil.getAjaxSheetDataDefaultValue(reportWeb,
		    sheetModel));
	} else if (socketCmd.equals("WorkFlow_Execute_BaseInfoTools_Admin")) {
	    String groupId = myCmdArray.elementAt(3).toString();
	    BaseDataAdmin baseData = new BaseDataAdmin(me);
	    myOut.write(baseData.getMainPage(groupId));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.getMainPage());
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_ModelTree_JSon")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    myOut.write(web.getWFMTreeJson(requestType, param));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_ActiveData_XML")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String start = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String param = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String conditon = UtilCode.decode(myStr.matchValue("_condition[",
		    "]condition_"));
	    myOut.write(web.getActiveTaskData(requestType, param, conditon,
		    start, limit));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_FinishData_XML")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String start = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String param = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String conditon = UtilCode.decode(myStr.matchValue("_condition[",
		    "]condition_"));
	    myOut.write(web.getFinishInstanceData(requestType, param, conditon,
		    start, limit));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_UndoTransaction")) {
	    String instance = UtilCode.decode(myStr.matchValue("_instance[",
		    "]instance_"));
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.undoTransaction(instance));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_WFRemoveSub")) {
	    String instance = UtilCode.decode(myStr.matchValue("_instance[",
		    "]instance_"));
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.removeWFSub(instance));
	    String str1 = RuntimeMonitorUtil.getNewList(instance);
	    String[] arrayOfString = str1.split(" ");
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    String msgId = "";
	    try {
		HttpClient client = new HttpClient();
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(3000);
		connectionManager.getParams().setSoTimeout(3000);
		client.setHttpConnectionManager(connectionManager);
		conn = DBSql.open();
		ps = conn
			.prepareStatement("select MSGID from WF_MESSAGE_INTERFACE where BINDID = ?");
		for (int i = 0; i < arrayOfString.length; ++i) {
		    String str2 = arrayOfString[i];
		    str2 = str2.indexOf("_") == -1 ? str2 : str2.substring(
			    str2.indexOf("_") + 1, str2.length());
		    ps.setInt(1, Integer.parseInt(str2));
		    try {
			rs = ps.executeQuery();
			while (rs.next()) {
			    msgId = rs.getString("MSGID") == null ? "" : rs
				    .getString("MSGID");
			    if (msgId.length() > 0) {
				JSONObject content = new JSONObject();
				content.put("status", "delete");
				content.put("msgId", msgId);
				new UpdateWorkMsgThread(client, content, msgId,
					"D").start();
			    }
			}
		    } catch (Exception e) {
			e.printStackTrace(System.err);
		    } finally {
			DBSql.close(rs);
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    } finally {
		DBSql.close(conn, ps, null);
	    }
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_ReconvertData_XML")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String start = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getReconvertData(start, limit));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_JobNextData_XML")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String start = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String param = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    myOut.write(web.getJobNextData(requestType, param, start, limit));
	} else if (socketCmd.equals("Org_Job_Next_Welcome")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String param1 = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    myOut.write(web.getJobNextPage(type, param1, "", "", ""));
	} else if (socketCmd.equals("Org_Job_Next_Excute")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String fromUID = myCmdArray.elementAt(4).toString();
	    String toUID = myCmdArray.elementAt(5).toString();
	    String param1 = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String s = web.excuteJobNext(type, param1, fromUID, toUID);
	    String flowid = RuntimeMonitorUtil.getNewList2(param1);
	    new UpdateTargetThread(fromUID, toUID, flowid).start();
	    myOut.write(s);
	} else if (socketCmd.equals("WorkFlow_Monitor_Tools_ActiveWF_Welcome")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String instances = UtilCode.decode(myStr.matchValue("_bindid[",
		    "]bindid_"));
	    myOut.write(web.getActiveWFPage(instances, "", "", ""));
	} else if (socketCmd.equals("WorkFlow_Monitor_Tools_ActiveWF_Excute")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String stepNO = myCmdArray.elementAt(3).toString();
	    String type = myCmdArray.elementAt(4).toString();
	    String isDelSrcTask = myCmdArray.elementAt(5).toString();
	    String activeRTClass = UtilCode.decode(myStr.matchValue(
		    "_activeRTClass[", "]activeRTClass_"));
	    String toUID = UtilCode.decode(myStr.matchValue("_toUID[",
		    "]toUID_"));
	    String instances = UtilCode.decode(myStr.matchValue("_bindid[",
		    "]bindid_"));
	    myOut.write(web.excuteActiveWF(Integer.parseInt(stepNO), instances,
		    toUID, activeRTClass, isDelSrcTask.equals("yes"),
		    type.equals("0")));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_WFRemove_XML")) {
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String start = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String param = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    myOut.write(web.getWFRemoveData(requestType, param, start, limit));
	} else if (socketCmd.equals("WorkFlow_Monitor_Main_WFRemove_Excute")) {
	    String instance = UtilCode.decode(myStr.matchValue("_wfid[",
		    "]wfid_"));
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.removeWF(instance));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_RuntimeManager_Overview_Chart_Data")) {
	    String opentype = myCmdArray.elementAt(3).toString();
	    RuntimeMonitorMainWeb web = new RuntimeMonitorMainWeb(me);
	    myOut.write(web.getOverviewChartData(opentype));
	    web = null;
	} else if (socketCmd.equals("WF_Document_Main")) {
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    String wfuuid = myCmdArray.elementAt(3).toString();
	    if (wfuuid == null || "".equals(wfuuid))
		wfuuid = "";
	    myOut.write(web.getMainWeb(wfuuid));
	    web = null;
	} else if (socketCmd.equals("WF_Document_Main_Read")) {
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    String wfuuid = myCmdArray.elementAt(3).toString();
	    if (wfuuid == null || "".equals(wfuuid))
		wfuuid = "";
	    myOut.write(web.getMainWeb(wfuuid));
	    web = null;
	} else if (socketCmd.equals("WF_Document_Main_Read_KM")) {
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    String wfuuid = myCmdArray.elementAt(3).toString();
	    if (wfuuid == null || "".equals(wfuuid))
		wfuuid = "";
	    myOut.write(web.getKm(wfuuid));
	    web = null;
	} else if (socketCmd.equals("WF_Document_ModelTree_JSon")) {
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String param2 = UtilCode.decode(myStr.matchValue("_param2[",
		    "]param2_"));
	    myOut.write(web.getWFMTreeJson(requestType, param1, param2));
	    web = null;
	} else if (socketCmd.equals("WF_Document_LiveSearch_QueryData")) {
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    String query = UtilCode.decode(myStr.matchValue("_query[",
		    "]query_"));
	    myOut.write(web.getWFSearchData(query));
	    web = null;
	} else if (socketCmd.equals("WF_Document_Attach_Upload")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String upFileType = myCmdArray.elementAt(6).toString();
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    myOut.write(web.getUpFilePage(p1, p2, p3, upFileType));
	    web = null;
	} else if (socketCmd.equals("WF_Document_Save")) {
	    String wfuuid = myCmdArray.elementAt(3).toString();
	    String wfdoc = UtilCode.decode(myStr.matchValue("_wfDoc[",
		    "]wfDoc_"));
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    myOut.write(web.saveWFDoc(wfuuid, wfdoc));
	    web = null;
	} else if (socketCmd.equals("WF_Document_Km_Save")) {
	    String wfuuid = myCmdArray.elementAt(3).toString();
	    String kmCardNo = UtilCode.decode(myStr.matchValue("_cardNo[",
		    "]cardNo_"));
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    myOut.write(web.saveWFDocKm(wfuuid, kmCardNo));
	    web = null;
	} else if (socketCmd.equals("WF_Document_GetDoc")) {
	    String wfuuid = myCmdArray.elementAt(3).toString();
	    String docType = myCmdArray.elementAt(4).toString();
	    WFDesignDocumentWeb web = new WFDesignDocumentWeb(me);
	    myOut.write(web.getWFDocData(wfuuid, docType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_AjaxSheet_ReqRid")) {
	    String subSheetId = myCmdArray.elementAt(3).toString();
	    SheetModel sheetModel = (SheetModel) SheetCache.getModel(Integer
		    .parseInt(subSheetId));
	    myOut.write(TreeGrid.reqRid(sheetModel));
	} else if (socketCmd.equals("WorkFlow_Design_Form_CreatePage")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getFormModelCreatePage(groupName));
	    web = null;
	} else if (socketCmd.equals("FormModel_Design_Map_Open")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String fromModelId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getFormModelMapOpen(fromModelId));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Model_Excel_UpModelPage")) {
	    DesignFormExcelTabWeb web = new DesignFormExcelTabWeb(me);
	    myOut.write(web.getUpExcelModelPage());
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Model_Excel_CopyFormModel")) {
	    DesignFormExcelTabWeb web = new DesignFormExcelTabWeb(me);
	    web.copyFormModel();
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getExcelPage(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("FormModel_Design_Create_And_Maps_Save")) {
	    DesignFormWeb web = new DesignFormWeb(me);
	    String formstyle = myCmdArray.elementAt(5).toString();
	    String isCreate = myCmdArray.elementAt(6).toString();
	    String formName = myCmdArray.elementAt(3).toString();
	    String formType = myCmdArray.elementAt(4).toString();
	    String data = UtilCode.decode(myStr.matchValue("_data[", "]data_"));
	    myOut.write(web.formModelCreateAndAjaxSave(formName, formType,
		    isCreate, formstyle, data));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFG_Create_And_Create_WFStep")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowStyle = myCmdArray.elementAt(3).toString();
	    String workFlowName = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String flowType = myCmdArray.elementAt(6).toString();
	    String bindReportId = myCmdArray.elementAt(7).toString();
	    String step = myCmdArray.elementAt(8).toString();
	    String initStepName = UtilCode.decode(myStr.matchValue(
		    "_initStepName[", "]initStepName_"));
	    if (bindReportId == null || bindReportId.equals(""))
		bindReportId = "0";
	    if (step == null || step.equals(""))
		step = "wfg";
	    WorkFlowModel model = new WorkFlowModel();
	    model._flowMaster = me.getUID();
	    model._flowName = workFlowName;
	    model._flowStyle = workFlowStyle;
	    model._groupName = groupName;
	    model._workFlowType = Integer.parseInt(flowType);
	    model._accessSecurityType = 1;
	    myOut.write(web.saveWorkFlowGroupAndCreateWFStep(model,
		    Integer.parseInt(bindReportId), initStepName, step));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Step_Wizard_Page")) {
	    WFDesignWeb web = new WFDesignWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWFStepWizardPage(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Step_Get_Opinion_AjaxSheetXML")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getOpinionListAjaxSheetXML(Integer
		    .parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Opinions_Create")) {
	    WFDesignStepOpinionTabWeb web = new WFDesignStepOpinionTabWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String data = UtilCode.decode(myStr.matchValue("_data[", "]data_"));
	    myOut.write(web.createOpinions(Integer.parseInt(workFlowId), data));
	    web = null;
	} else if (socketCmd.equals("WF_HTMLEDIT_Attach_Upload")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String upFileType = myCmdArray.elementAt(6).toString();
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    myOut.write(web.getUpFilePage(p1, p2, p3, upFileType));
	    web = null;
	} else if (socketCmd.equals("WF_HTMLEDITOR_Attach_Upload")) {
	    String p1 = myCmdArray.elementAt(3).toString();
	    String p2 = myCmdArray.elementAt(4).toString();
	    String p3 = myCmdArray.elementAt(5).toString();
	    String upFileType = myCmdArray.elementAt(6).toString();
	    DesignFormBaseDataTabWeb web = new DesignFormBaseDataTabWeb(me);
	    myOut.write(web.getUpFilePage(p1, p2, p3, upFileType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Search_MetaData_FiledValue")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String bindReportId = myCmdArray.elementAt(3).toString();
	    String query = myCmdArray.elementAt(5).toString();
	    String limit = myCmdArray.elementAt(6).toString();
	    String start = myCmdArray.elementAt(7).toString();
	    String searchFiled = UtilCode.decode(myStr.matchValue(
		    "_searchFiled[", "]searchFiled_"));
	    myOut.write(web.getSearchMetaDataFiledValue(
		    Integer.parseInt(bindReportId), searchFiled, query,
		    Integer.parseInt(limit), Integer.parseInt(start)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_Search_By_Sql")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String recordNum = myCmdArray.elementAt(3).toString();
	    if (recordNum.trim().length() == 0)
		recordNum = "20";
	    String limit = myCmdArray.elementAt(4).toString();
	    String start = myCmdArray.elementAt(5).toString();
	    String source = myCmdArray.elementAt(6).toString();
	    String filedName = UtilCode.decode(myStr.matchValue("_filedName[",
		    "]filedName_"));
	    String sql = UtilCode.decode(myStr.matchValue("_sql[", "]sql_"));
	    String query = UtilCode.decode(myStr.matchValue("_query[",
		    "]query_"));
	    myOut.write(web.getSearchMetaDataFiledValueBySql(source, sql,
		    filedName, query, Integer.parseInt(recordNum)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_BindReport_FieldValue_Casecade")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String bindReportId = myCmdArray.elementAt(3).toString();
	    String sourceField = myCmdArray.elementAt(4).toString();
	    String searchType = myCmdArray.elementAt(5).toString();
	    String fileName = myCmdArray.elementAt(6).toString();
	    String targetField = UtilCode.decode(myStr.matchValue(
		    "_cascadeField[", "]cascadeField_"));
	    String bindData = UtilCode.decode(myStr.matchValue("_bindData[",
		    "]bindData_"));
	    if ("FORM".equals(searchType))
		myOut.write(web.getFormCascadeFieldValue(
			Integer.parseInt(bindReportId), targetField,
			sourceField, bindData));
	    else if ("DIGGER".equals(searchType))
		myOut.write(web.getDiggerCascadeFieldValue(
			Integer.parseInt(bindReportId), targetField,
			sourceField, bindData));
	    else if ("XMLDICT".equals(searchType))
		myOut.write(web.getXMLDictionaryCascadeFieldValue(fileName,
			targetField, sourceField, bindData));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_ProcessVar_Save")) {
	    WFDesignMetaDataVariableWeb web = new WFDesignMetaDataVariableWeb(
		    me);
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String varName = UtilCode.decode(myStr.matchValue(
		    "_processVarName[", "]processVarName_"));
	    String varValue = UtilCode.decode(myStr.matchValue(
		    "_processVarValue[", "]processVarValue_"));
	    int id = ProcessVariableInstance.getInstance().assignVariable(
		    Integer.parseInt(processInstanceId), varName, varValue);
	    myOut.write(Integer.toString(id));
	} else if (socketCmd.equals("WorkFlow_Execute_Worklist_Satisfation")) {
	    DesignFormWorkflowSatisfationWeb web = new DesignFormWorkflowSatisfationWeb(
		    me);
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String processTaskInstanceId = myCmdArray.elementAt(4).toString();
	    if (processInstanceId.trim().length() == 0)
		processInstanceId = "0";
	    if (processTaskInstanceId.trim().length() == 0)
		processTaskInstanceId = "0";
	    myOut.write(web.getWFSatisfationWeb(
		    Integer.parseInt(processInstanceId),
		    Integer.parseInt(processTaskInstanceId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Satisfation_Save")) {
	    DesignFormWorkflowSatisfationWeb web = new DesignFormWorkflowSatisfationWeb(
		    me);
	    String processInstanceId = myCmdArray.elementAt(3).toString();
	    String processTaskInstanceId = myCmdArray.elementAt(4).toString();
	    String workflowDesignSatisfationValue = myCmdArray.elementAt(5)
		    .toString();
	    String workflowFormSatisfationValue = myCmdArray.elementAt(6)
		    .toString();
	    String workflowDesignSatisfationOpinion = UtilCode.decode(myStr
		    .matchValue("_designOpinion[", "]designOpinion_"));
	    String workflowFormSatisfationOpinion = UtilCode.decode(myStr
		    .matchValue("_formOpinion[", "]formOpinion_"));
	    if (processInstanceId.trim().length() == 0)
		processInstanceId = "0";
	    if (processTaskInstanceId.trim().length() == 0)
		processTaskInstanceId = "0";
	    if (workflowDesignSatisfationValue.trim().length() == 0)
		workflowDesignSatisfationValue = "0";
	    if (workflowFormSatisfationValue.trim().length() == 0)
		workflowFormSatisfationValue = "0";
	    myOut.write(web.saveWFSatisfation(
		    Integer.parseInt(processInstanceId),
		    Integer.parseInt(processTaskInstanceId),
		    Integer.parseInt(workflowDesignSatisfationValue),
		    Integer.parseInt(workflowFormSatisfationValue),
		    workflowDesignSatisfationOpinion,
		    workflowFormSatisfationOpinion));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_RefKMFile_knowledge")) {
	    String kmid = UtilCode.decode(myStr.matchValue("_kmid[", "]kmid_"));
	    FormUIComponentFileRefKMFileWeb web = new FormUIComponentFileRefKMFileWeb(
		    me);
	    myOut.write(web.getWeb(kmid));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_RefKMFile_cardList")) {
	    String kmid = UtilCode.decode(myStr.matchValue("_kmid[", "]kmid_"));
	    String fileType = UtilCode.decode(myStr.matchValue("_fileType[",
		    "]fileType_"));
	    FormUIComponentFileRefKMFileWeb web = new FormUIComponentFileRefKMFileWeb(
		    me);
	    myOut.write(web.getCardList(kmid, fileType));
	} else if (socketCmd
		.equals("WorkFlow_Execute_Worklist_Form_Attach_RefKMFile_filecopy")) {
	    String kmid = UtilCode.decode(myStr.matchValue("_kmid[", "]kmid_"));
	    String bindid = UtilCode.decode(myStr.matchValue("_bindid[",
		    "]bindid_"));
	    String metadatamapid = UtilCode.decode(myStr.matchValue(
		    "_metadatamapid[", "]metadatamapid_"));
	    String selectedkmid = UtilCode.decode(myStr.matchValue(
		    "_selectedkmid[", "]selectedkmid_"));
	    String selectedfilename = UtilCode.decode(myStr.matchValue(
		    "_selectedfilename[", "]selectedfilename_"));
	    FormUIComponentFileRefKMFileWeb web = new FormUIComponentFileRefKMFileWeb(
		    me);
	    String meid = UtilCode.decode(myStr.matchValue("_meid[", "]meid_"));
	    String primaryid = UtilCode.decode(myStr.matchValue("_primaryid[",
		    "]primaryid_"));
	    String fileType = UtilCode.decode(myStr.matchValue("_fileType[",
		    "]fileType_"));
	    myOut.write(web.bindForm(kmid, metadatamapid, selectedkmid,
		    selectedfilename, meid, primaryid));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_Form_Mobile_Open")) {
	    DesignFormMobileTempleteTabWeb web = new DesignFormMobileTempleteTabWeb(
		    me);
	    String reportId = UtilCode.decode(myStr.matchValue("_reportId[",
		    "]reportId_"));
	    myOut.write(web.getMobileTempletePage(reportId));
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Support_Mobile_Templete")) {
	    DesignFormMobileTempleteTabWeb web = new DesignFormMobileTempleteTabWeb(
		    me);
	    String formUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.supportMobileTemplete(formUUID));
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Delete_Mobile_Templete")) {
	    DesignFormMobileTempleteTabWeb web = new DesignFormMobileTempleteTabWeb(
		    me);
	    String BO_MOBILE_FORM_ID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.deleteMobileTemplete(BO_MOBILE_FORM_ID));
	} else if (socketCmd
		.equals("WorkFlow_Design_Form_Save_Mobile_Templete")) {
	    DesignFormMobileTempleteTabWeb web = new DesignFormMobileTempleteTabWeb(
		    me);
	    String BO_MOBILE_FORM_ID = myCmdArray.elementAt(3).toString();
	    String masterFields = UtilCode.decode(myStr.matchValue(
		    "_masterFields[", "]masterFields_"));
	    String subTables = UtilCode.decode(myStr.matchValue("_subTables[",
		    "]subTables_"));
	    String subFieldsAll = UtilCode.decode(myStr.matchValue(
		    "_subFieldsAll[", "]subFieldsAll_"));
	    myOut.write(web.saveMobileTemplete(BO_MOBILE_FORM_ID, masterFields,
		    subTables, subFieldsAll));
	} else {
	    return false;
	}
	return true;
    }
}
