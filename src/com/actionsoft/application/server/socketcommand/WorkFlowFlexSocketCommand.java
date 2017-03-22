package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.portal.navigation.cache.NavigationDirectoryCache;
import com.actionsoft.application.portal.navigation.cache.NavigationFunctionCache;
import com.actionsoft.application.portal.navigation.model.NavigationDirectoryModel;
import com.actionsoft.application.portal.navigation.model.NavigationFunctionModel;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowVariableCache;
import com.actionsoft.awf.workflow.design.dao.WFDesignDaoFactory;
import com.actionsoft.awf.workflow.design.flex.web.AWSFPDWorkFlowExtendInfo;
import com.actionsoft.awf.workflow.design.flex.web.AWSFPDWorkFlowTransact;
import com.actionsoft.awf.workflow.design.flex.web.AWSWorkFlowModelPersistent;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignCoEUserlWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignEmailTemplateWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignGlobalWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStartAndEndWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepActiveAppWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepButWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepCardWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepImpExpWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepMessageWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepOpinionWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepRTClassWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepRuleWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepTimerLimitWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignStepWeb;
import com.actionsoft.awf.workflow.design.flex.web.WFFlexDesignWeb;
import com.actionsoft.awf.workflow.design.model.WorkFlowLaneModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepBindFieldModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepBindReportModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepParameterModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepRTClassModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepToolbarModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowVariableModel;
import com.actionsoft.awf.workflow.design.web.WFFlexDesignVersionWeb;

public class WorkFlowFlexSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("WorkFlow_Flex_Design_Portal")) {
	    WFFlexDesignWeb web = new WFFlexDesignWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWFFlexDesignPortal(workflowId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Global")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getGlobalPortal(Integer.parseInt(workflowId)));
	    web = null;
	} else if (socketCmd.equals("AWSFlexProcessDesigner_Portal")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    String editable = myCmdArray.elementAt(4).toString();
	    String scheme = myCmdArray.elementAt(5).toString();
	    String debug = myCmdArray.elementAt(6).toString();
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    if (editable == null || editable.equals(""))
		editable = "1";
	    if (scheme == null || scheme.equals(""))
		scheme = "awsdefault";
	    if (debug == null || debug.equals(""))
		debug = "false";
	    try {
		myOut.write(web.getFPDPortal(Integer.parseInt(wfId),
			Integer.parseInt(editable), scheme,
			Boolean.parseBoolean(debug)));
	    } catch (Exception e) {
		myOut.write(web.getFPDPortal(wfId, Integer.parseInt(editable),
			scheme, Boolean.parseBoolean(debug)));
	    }
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Exp_Image")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    String imgData = UtilCode.decode(myStr.matchValue("_imgData[",
		    "]imgData_"));
	    myOut.write(web.downloadImage(uuid, imgData));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Exp_XPDL")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.downloadXPDL(uuid));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Get_WorkFlowModel")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkFlowXMLData(uuid));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Save_WorkFlowModel_XML")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    String isExportImage = myCmdArray.elementAt(4).toString();
	    String xml = UtilCode.decode(myStr.matchValue("_XML[", "]XML_"));
	    myOut.write(web.saveWorkFlowXPDL(uuid, xml, isExportImage));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Get_WorkFlowStepsModel")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkFlowXMLItemData(uuid, true));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Save_WorkFlow")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String wfUUID = myCmdArray.elementAt(3).toString();
	    String poolName = UtilCode.decode(myStr.matchValue("_poolName[",
		    "]poolName_"));
	    myOut.write(web.saveWorkFlowName(wfUUID, poolName));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Create_WorkFlowLane")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    String wfUUID = myCmdArray.elementAt(4).toString();
	    String laneName = UtilCode.decode(myStr.matchValue("_laneName[",
		    "]laneName_"));
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    WorkFlowLaneModel lane = new WorkFlowLaneModel();
	    lane._flowId = Integer.parseInt(wfId);
	    lane._flowUUID = wfUUID;
	    lane._laneName = laneName;
	    myOut.write(web.createWorkFlowLane(lane));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Save_WorkFlowLane")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String laneUUID = myCmdArray.elementAt(3).toString();
	    String laneName = UtilCode.decode(myStr.matchValue("_laneName[",
		    "]laneName_"));
	    myOut.write(web.saveWorkFlowLane(laneUUID, laneName));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Remove_WorkFlowLane")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    String laneUUID = myCmdArray.elementAt(4).toString();
	    myOut.write(web.removeWorkFlowLane(Integer.parseInt(wfId), laneUUID));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Create_WorkFlowStep")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String laneUUID = myCmdArray.elementAt(4).toString();
	    String stepNo = myCmdArray.elementAt(5).toString();
	    String insertTargetNodeId = myCmdArray.elementAt(6).toString();
	    String insertAt = myCmdArray.elementAt(7).toString();
	    String isRedrawLine = myCmdArray.elementAt(8).toString();
	    String stepName = UtilCode.decode(myStr.matchValue("_stepName[",
		    "]stepName_"));
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    model._flowId = Integer.parseInt(workFlowId);
	    model._stepName = stepName;
	    model._stepNo = Integer.parseInt(stepNo);
	    model._laneUUID = laneUUID;
	    myOut.write(web.createWorkFlowStep(model, insertTargetNodeId,
		    insertAt, isRedrawLine));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Save_WorkFlowStep")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    String wfStepUUID = myCmdArray.elementAt(4).toString();
	    String laneUUID = myCmdArray.elementAt(5).toString();
	    String stepName = UtilCode.decode(myStr.matchValue("_stepName[",
		    "]stepName_"));
	    myOut.write(web.saveWorkFlowStep(wfStepUUID, stepName, laneUUID));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Remove_WorkFlowStep")) {
	    AWSWorkFlowModelPersistent web = new AWSWorkFlowModelPersistent(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    String wfStepId = myCmdArray.elementAt(4).toString();
	    String wfStepUUID = myCmdArray.elementAt(5).toString();
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    if (wfStepId == null || wfStepId.equals(""))
		wfStepId = "0";
	    myOut.write(web.removeWorkFlowStep(Integer.parseInt(wfId),
		    Integer.parseInt(wfStepId), wfStepUUID));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Frame_Open")) {
	    WFFlexDesignStepCardWeb web = new WFFlexDesignStepCardWeb(me);
	    String pageType = myCmdArray.elementAt(3).toString();
	    String workflowId = myCmdArray.elementAt(4).toString();
	    String workflowStepId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getWorkFlowNodeDesignTab(
		    Integer.parseInt(pageType), workflowId, workflowStepId));
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_BaseData_Open")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    String workflowStepId = myCmdArray.elementAt(4).toString();
	    if (workflowStepId == null || workflowStepId.equals(""))
		workflowStepId = "0";
	    myOut.write(web.getBaseDataForm(Integer.parseInt(workflowId),
		    Integer.parseInt(workflowStepId)));
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Actor_Open")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String workflowId = myCmdArray.elementAt(3).toString();
	    String workflowStepId = myCmdArray.elementAt(4).toString();
	    if (workflowStepId == null || workflowStepId.equals(""))
		workflowStepId = "0";
	    myOut.write(web.getActorForm(Integer.parseInt(workflowId),
		    Integer.parseInt(workflowStepId)));
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_BaseData_Save")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String isJumpStep = myCmdArray.elementAt(5).toString();
	    String isReadTo = myCmdArray.elementAt(6).toString();
	    String isPrint = myCmdArray.elementAt(7).toString();
	    String routePointType = myCmdArray.elementAt(8).toString();
	    String jobPercent = myCmdArray.elementAt(9).toString();
	    String isAudit = myCmdArray.elementAt(10).toString();
	    String stepLimit = myCmdArray.elementAt(11).toString();
	    String isUrgePower = myCmdArray.elementAt(12).toString();
	    String isDisplayAuditLog = myCmdArray.elementAt(13).toString();
	    String printTimes = myCmdArray.elementAt(14).toString();
	    String isShareOpinion = myCmdArray.elementAt(15).toString();
	    String isStepTransmit = myCmdArray.elementAt(16).toString();
	    String isSelfDispose = myCmdArray.elementAt(17).toString();
	    String transfersLimitType = myCmdArray.elementAt(18).toString();
	    String kmDirectoryId = myCmdArray.elementAt(19).toString();
	    String kmOption = myCmdArray.elementAt(20).toString();
	    String isIgnoreSame = myCmdArray.elementAt(21).toString();
	    String isShortMessage = myCmdArray.elementAt(22).toString();
	    String isAddParticipants = myCmdArray.elementAt(23).toString();
	    String isBatchExecTask = myCmdArray.elementAt(24).toString();
	    String emailAlertType = myCmdArray.elementAt(25).toString();
	    String auditMenuPosition = myCmdArray.elementAt(26).toString();
	    String emaiTemplate = myCmdArray.elementAt(27).toString();
	    String AuditMenuOpinion = myCmdArray.elementAt(28).toString();
	    String stepLimitMore = myCmdArray.elementAt(29).toString();
	    if (stepLimitMore == null || stepLimitMore.equals(""))
		stepLimitMore = "00";
	    String stepName = UtilCode.decode(myStr.matchValue("_stepName[",
		    "]stepName_"));
	    String readToType = UtilCode.decode(myStr.matchValue(
		    "_readToType[", "]readToType_"));
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
	    String addParticipantsType = UtilCode.decode(myStr.matchValue(
		    "_addParticipantsType[", "]addParticipantsType_"));
	    if (kmDirectoryId == null || kmDirectoryId.equals(""))
		kmDirectoryId = "0";
	    if (emailAlertType == null || emailAlertType.equals(""))
		emailAlertType = "0";
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
	    if (isJumpStep == null || isJumpStep.equals(""))
		isJumpStep = "0";
	    if (isReadTo == null || isReadTo.equals(""))
		isReadTo = "0";
	    if (isPrint == null || isPrint.equals(""))
		isPrint = "0";
	    if (isAudit == null || isAudit.equals(""))
		isAudit = "0";
	    if (routePointType == null || routePointType.equals(""))
		routePointType = "0";
	    if (stepLimit == null || stepLimit.equals(""))
		stepLimit = "0";
	    if (auditMenuPosition == null
		    || auditMenuPosition.trim().equals(""))
		auditMenuPosition = "0";
	    if (AuditMenuOpinion == null || AuditMenuOpinion.trim().equals(""))
		AuditMenuOpinion = "0";
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    WorkFlowStepModel oldModel = (WorkFlowStepModel) WorkFlowStepCache
		    .getModel(Integer.parseInt(workFlowStepId));
	    if (oldModel != null)
		model.setModel(oldModel);
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
	    model._routePointType = Integer.parseInt(routePointType);
	    model._stepName = stepName;
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
	    model._auditMenuPosition = Integer.parseInt(auditMenuPosition);
	    model._emailTemplate = emaiTemplate;
	    model._auditMenuOpinion = Integer.parseInt(AuditMenuOpinion);
	    model._addParticipantsType = addParticipantsType.trim();
	    myOut.write(web.saveStepBaseData(model, 0));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Actor_Save")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String roleId = myCmdArray.elementAt(5).toString();
	    String routeType = myCmdArray.elementAt(6).toString();
	    String teamId = myCmdArray.elementAt(7).toString();
	    String refRoleId1 = myCmdArray.elementAt(8).toString();
	    String refStepId = myCmdArray.elementAt(9).toString();
	    String isHistoryRoute = myCmdArray.elementAt(10).toString();
	    String stepUser = UtilCode.decode(myStr.matchValue("_stepUser[",
		    "]stepUser_"));
	    String stepTogetherUser = UtilCode.decode(myStr.matchValue(
		    "_stepTogetherUser[", "]stepTogetherUser_"));
	    String diyRoute = UtilCode.decode(myStr.matchValue("_diyRoute[",
		    "]diyRoute_"));
	    String routeText = UtilCode.decode(myStr.matchValue("_routText[",
		    "]routText_"));
	    if (diyRoute == null || diyRoute.equals(""))
		diyRoute = "";
	    if (isHistoryRoute == null || isHistoryRoute.equals(""))
		isHistoryRoute = "0";
	    if (roleId == null || roleId.equals(""))
		roleId = "0";
	    if (refRoleId1 == null || refRoleId1.equals(""))
		refRoleId1 = "0";
	    if (refStepId == null || refStepId.equals(""))
		refStepId = "0";
	    if (routeType == null || routeType.equals(""))
		routeType = "0";
	    if (teamId == null || teamId.equals(""))
		teamId = "0";
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    model._id = Integer.parseInt(workFlowStepId);
	    model._flowId = Integer.parseInt(workFlowId);
	    model._isHistoryRoute = !isHistoryRoute.equals("0");
	    model._routeType = Integer.parseInt(routeType);
	    if (model._routeType == 20 || model._routeType == 21) {
		model._roleId = Integer.parseInt(refRoleId1);
		model._refRoleId1 = Integer.parseInt(roleId);
	    } else {
		model._roleId = Integer.parseInt(roleId);
		model._refRoleId1 = Integer.parseInt(refRoleId1);
	    }
	    model._routeText = routeText;
	    model._refStepId = Integer.parseInt(refStepId);
	    model._teamId = Integer.parseInt(teamId);
	    model._stepUser = stepUser.trim();
	    model._stepTogetherUser = stepTogetherUser.trim();
	    model._diyRoute = diyRoute;
	    myOut.write(web.saveStepActor(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_WFS_SettingRouteRef")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String routeType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getRouteRefPage(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(routeType)));
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_WFS_SettingRouteRef_AjaxRoleGroup")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    String param2 = UtilCode.decode(myStr.matchValue("_param2[",
		    "]param2_"));
	    String param3 = UtilCode.decode(myStr.matchValue("_param3[",
		    "]param3_"));
	    myOut.write(web.getJsonOfRoleGroup(requestType, param1, param2,
		    param3));
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_WFS_SettingRouteRef_Save")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
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
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_BaseData_Open")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getBaseDataForm(Integer.parseInt(workFlowId)));
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_BaseData_Save")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String flowName = myCmdArray.elementAt(4).toString();
	    String wfVersion = myCmdArray.elementAt(5).toString();
	    String isMontior = myCmdArray.elementAt(6).toString();
	    String flowType = myCmdArray.elementAt(7).toString();
	    String isShare = myCmdArray.elementAt(8).toString();
	    String groupName = myCmdArray.elementAt(9).toString();
	    String isCancel = myCmdArray.elementAt(10).toString();
	    String isDraftRemove = myCmdArray.elementAt(11).toString();
	    String isWFCancel = myCmdArray.elementAt(12).toString();
	    String trackDiagramType = myCmdArray.elementAt(13).toString();
	    String isTrackForm = myCmdArray.elementAt(14).toString();
	    String isShowMini = myCmdArray.elementAt(15).toString();
	    String isDisplayFile = myCmdArray.elementAt(16).toString();
	    String isSecurityLayer = myCmdArray.elementAt(17).toString();
	    String accessSecurityType = myCmdArray.elementAt(18).toString();
	    String processLevel = myCmdArray.elementAt(19).toString();
	    String shortMessageModel = UtilCode.decode(myStr.matchValue(
		    "_shortMessageModel[", "]shortMessageModel_"));
	    String unCancel_WFS = UtilCode.decode(myStr.matchValue(
		    "_uncancelwfs[", "]uncancelwfs_"));
	    String durationWarningComplex = UtilCode.decode(myStr.matchValue(
		    "_durationWarningComplex[", "]durationWarningComplex_"));
	    String durationComplex = UtilCode.decode(myStr.matchValue(
		    "_durationComplex[", "]durationComplex_"));
	    String flowDesc = "";
	    String flowMaster = UtilCode.decode(myStr.matchValue(
		    "_flowMaster[", "]flowMaster_"));
	    String appId = UtilCode.decode(myStr.matchValue("_appId[",
		    "]appId_"));
	    WorkFlowModel model = new WorkFlowModel();
	    model._flowDesc = flowDesc;
	    model._flowMaster = flowMaster;
	    model._flowName = flowName;
	    model._shortMessageModel = shortMessageModel;
	    model._id = Integer.parseInt(workFlowId);
	    model._isShare = isShare.equals("1");
	    model._workFlowType = Integer.parseInt(flowType);
	    model._isDisplayFile = isDisplayFile.equals("1");
	    model._isTrackForm = isTrackForm.equals("1");
	    model._isMontior = isMontior.equals("1");
	    model._durationComplex = durationComplex;
	    model._durationWarningComplex = durationWarningComplex;
	    model._isShowMini = isShowMini.equals("1");
	    model._isCancel = isCancel.equals("1");
	    model._isDraftRemove = isDraftRemove.equals("1");
	    model._trackDiagramType = Integer.parseInt(trackDiagramType);
	    model._wfVersion = wfVersion;
	    model._groupName = groupName;
	    model._appId = appId;
	    model._isWFCancel = isWFCancel.equals("1");
	    model._uncancel_wfs = unCancel_WFS;
	    model._isSecurityLayer = isSecurityLayer.equals("1");
	    model._accessSecurityType = Integer.parseInt(accessSecurityType);
	    model._processLevel = Integer
		    .parseInt("".equals(processLevel) ? "0" : processLevel);
	    myOut.write(web.saveWorkFlow(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_Variable_Open")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getGlobalVariable(Integer.parseInt(workFlowId)));
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_Variable_Create")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String variableName = UtilCode.decode(myStr.matchValue(
		    "_variableName[", "]variableName_"));
	    String variableType = UtilCode.decode(myStr.matchValue(
		    "_variableType[", "]variableType_"));
	    String defaultValue = UtilCode.decode(myStr.matchValue(
		    "_defaultValue[", "]defaultValue_"));
	    WorkFlowVariableModel model = new WorkFlowVariableModel();
	    model._flowId = Integer.parseInt(workFlowId);
	    model._variableName = variableName;
	    model._variableType = variableType;
	    model._defaultValue = defaultValue;
	    WorkFlowVariableModel modelTemp = (WorkFlowVariableModel) WorkFlowVariableCache
		    .getModel(Integer.parseInt(workFlowId), variableName);
	    if (modelTemp != null)
		myOut.write("<script>alert('变量名称：[" + variableName
			+ "]已在存在，如果需要修改请先删除后再添加！');history.go(-1);</script>");
	    else
		myOut.write(web.saveVariable(model));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_Variable_Delete")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String modelId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.removeVariable(Integer.parseInt(workFlowId),
		    Integer.parseInt(modelId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_RuntimeClass_Open")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getEvent(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_RuntimeClass_Create")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
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
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_Worklist_Open")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorklist(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_Worklist_Save")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String flowId = myCmdArray.elementAt(3).toString();
	    String xmlContent = UtilCode.decode(myStr.matchValue(
		    "_xmlContent[", "]xmlContent_"));
	    myOut.write(web.saveConfig(Integer.parseInt(flowId), xmlContent));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Global_Worklist_GetTableListCheckBox")) {
	    WFFlexDesignGlobalWeb web = new WFFlexDesignGlobalWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String fields = UtilCode.decode(myStr.matchValue("_fields[",
		    "]fields_"));
	    if (metaDataId.equals(""))
		metaDataId = "0";
	    myOut.write(web.getMetaDataMapListCheckBox(
		    Integer.parseInt(metaDataId), false, fields, true));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Global_COE_Open")) {
	    WFFlexDesignCoEUserlWeb web = new WFFlexDesignCoEUserlWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getCOEUser(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Global_COE_GetJson")) {
	    WFFlexDesignCoEUserlWeb web = new WFFlexDesignCoEUserlWeb(me);
	    String workFlowUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getCOEUserJson(workFlowUUID));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Global_COE_EditWeb")) {
	    WFFlexDesignCoEUserlWeb web = new WFFlexDesignCoEUserlWeb(me);
	    String coeId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getCOEEditWeb(coeId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Global_COE_Edit")) {
	    WFFlexDesignCoEUserlWeb web = new WFFlexDesignCoEUserlWeb(me);
	    String COEId = myCmdArray.elementAt(3).toString();
	    int COEIdInt = -1;
	    if (COEId != null && !"".equals(COEId))
		COEIdInt = Integer.parseInt(COEId);
	    String COEUserId = myCmdArray.elementAt(4).toString();
	    String COERole = myCmdArray.elementAt(5).toString();
	    String workFlowUUID = myCmdArray.elementAt(6).toString();
	    int COERoleInt = Integer.parseInt(COERole);
	    myOut.write(web.getCOEEdit(COEIdInt, COEUserId, COERoleInt,
		    workFlowUUID));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Global_COE_Dels")) {
	    WFFlexDesignCoEUserlWeb web = new WFFlexDesignCoEUserlWeb(me);
	    String ids = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getCOEDelete(ids));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_ActiveApp_Open")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String activityType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getActiveApp(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(activityType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Activity_Form_ArrorUp")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String activityType = myCmdArray.elementAt(5).toString();
	    String id = myCmdArray.elementAt(6).toString();
	    WFDesignDaoFactory.createWorkFlowStepBindReport().upIndex(
		    Integer.parseInt(id));
	    myOut.write(web.getActiveApp(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(activityType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Activity_Form_ArrorDown")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String activityType = myCmdArray.elementAt(5).toString();
	    String id = myCmdArray.elementAt(6).toString();
	    WFDesignDaoFactory.createWorkFlowStepBindReport().downIndex(
		    Integer.parseInt(id));
	    myOut.write(web.getActiveApp(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(activityType)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_ActiveApp_SelectForm_Open")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    if ("".equals(workFlowId) || workFlowId == null)
		workFlowId = "0";
	    if ("".equals(workFlowStepId) || workFlowStepId == null)
		workFlowStepId = "0";
	    myOut.write(web.getSelectForm(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_ActiveApp_SelectForm_Ajax")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String filter = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getFormsToJSON(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), groupName, filter));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_ActiveApp_Parameter_Create")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
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
		.equals("WorkFlow_Flex_Design_ActiveApp_Parameter_Remove")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeParameter(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_ActiveApp_URL_Save")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    int wfId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    int wfsId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    int isHiddenTaskToolBar = Integer.parseInt(myCmdArray.elementAt(5)
		    .toString());
	    String url = UtilCode.decode(myStr.matchValue("_bindUrl[",
		    "]bindUrl_"));
	    myOut.write(web.saveUrl(wfId, wfsId, url, isHiddenTaskToolBar == 1));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_ActiveApp_URL_Repository")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    int wfId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    int wfsId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    myOut.write(web.getWorkflowDesignStepActiveUrlRepositoryWeb(wfId,
		    wfsId));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_ActiveApp_URL_Repository_Ajax")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String groupUUID = myCmdArray.elementAt(5).toString();
	    String filter = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getUrlRepositoryToJSON(
		    Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), groupUUID, filter));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Activity_Form_Save")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
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
		.equals("WorkFlow_Flex_Design_Activity_SubForm_Save")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
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
		.equals("WorkFlow_Flex_Design_Step_ActiveApp_AccessList")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String formId = myCmdArray.elementAt(5).toString();
	    if ("".equals(formId) || formId == null)
		formId = "0";
	    myOut.write(web.getFormAccessList(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Activity_BindField_Save")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
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
		.equals("WorkFlow_Flex_Design_Step_ActiveApp_BindFormList")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getBindFormList(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Activity_Form_SubRemove")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String isMain = myCmdArray.elementAt(5).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeBindSubReport(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList,
		    isMain.equals("1")));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Form_Display_Preview")) {
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(new RuntimeFormManager(me).getFormPreview(Integer
		    .parseInt(formId)));
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Activity_BindField_PSaveall")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String bindReportId = myCmdArray.elementAt(5).toString();
	    String tmpDisplay = UtilCode.decode(myStr.matchValue("_isDisplay[",
		    "]isDisplay_"));
	    String tmpModify = UtilCode.decode(myStr.matchValue("_isRead[",
		    "]isRead_"));
	    myOut.write(web.updateBindField(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(bindReportId), tmpModify, tmpDisplay));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Activity_BindField_SSaveall")) {
	    WFFlexDesignStepActiveAppWeb web = new WFFlexDesignStepActiveAppWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String formId = myCmdArray.elementAt(5).toString();
	    String tmpDisplay = UtilCode.decode(myStr.matchValue("_isDisplay[",
		    "]isDisplay_"));
	    String tmpModify = UtilCode.decode(myStr.matchValue("_isRead[",
		    "]isRead_"));
	    myOut.write(web.updateBindSubField(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(formId),
		    tmpModify, tmpDisplay));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_ImpExp_Open")) {
	    WFFlexDesignStepImpExpWeb web = new WFFlexDesignStepImpExpWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_ImpExp_Create")) {
	    WFFlexDesignStepImpExpWeb web = new WFFlexDesignStepImpExpWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String sourceId = myCmdArray.elementAt(5).toString();
	    String action = myCmdArray.elementAt(6).toString();
	    String dataType = myCmdArray.elementAt(7).toString();
	    myOut.write(web.createAction(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(sourceId), action, dataType));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_ImpExp_Remove")) {
	    WFFlexDesignStepImpExpWeb web = new WFFlexDesignStepImpExpWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeAction(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_RuntimeClass_Open")) {
	    WFFlexDesignStepRTClassWeb web = new WFFlexDesignStepRTClassWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String rtType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getRTClass(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), rtType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_RuntimeClass_Create")) {
	    WFFlexDesignStepRTClassWeb web = new WFFlexDesignStepRTClassWeb(me);
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
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_TimerLimit_Open")) {
	    WFFlexDesignStepTimerLimitWeb web = new WFFlexDesignStepTimerLimitWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getWeb(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_TimerLimit_Create")) {
	    WFFlexDesignStepTimerLimitWeb web = new WFFlexDesignStepTimerLimitWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String cost = myCmdArray.elementAt(5).toString();
	    String costPolicy = myCmdArray.elementAt(6).toString();
	    String calcType = myCmdArray.elementAt(7).toString();
	    String costPoint = myCmdArray.elementAt(8).toString();
	    String bizTime = UtilCode.decode(myStr.matchValue("_bizTime[",
		    "]bizTime_"));
	    String bizRule = UtilCode.decode(myStr.matchValue("_bizRule[",
		    "]bizRule_"));
	    String bizClazz = UtilCode.decode(myStr.matchValue("_bizClazz[",
		    "]bizClazz_"));
	    String mailTo = UtilCode.decode(myStr.matchValue("_mailTo[",
		    "]mailTo_"));
	    String mailNo = UtilCode.decode(myStr.matchValue("_mailNo[",
		    "]mailNo_"));
	    String costText = UtilCode.decode(myStr.matchValue("_costText[",
		    "]costText_"));
	    if (calcType == null || calcType.length() == 0)
		calcType = "0";
	    if (costPoint == null || costPoint.length() == 0)
		costPoint = "0";
	    myOut.write(web.createCostPolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Double.parseDouble(cost),
		    Integer.parseInt(costPolicy), Integer.parseInt(calcType),
		    Integer.parseInt(costPoint), bizTime, bizRule, bizClazz,
		    mailTo, mailNo, costText));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_TimerLimit_Remove")) {
	    WFFlexDesignStepTimerLimitWeb web = new WFFlexDesignStepTimerLimitWeb(
		    me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeCostPolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Message_Open")) {
	    WFFlexDesignStepMessageWeb web = new WFFlexDesignStepMessageWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Message_Create")) {
	    WFFlexDesignStepMessageWeb web = new WFFlexDesignStepMessageWeb(me);
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
	    String mailNo = UtilCode.decode(myStr.matchValue("_mailNo[",
		    "]mailNo_"));
	    if (mailNo == null)
		mailNo = "";
	    String taskTitle = UtilCode.decode(myStr.matchValue("_taskTitle[",
		    "]taskTitle_"));
	    if (mailNo == null)
		mailNo = "";
	    myOut.write(web.createMessagePolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), messageUser, stepList,
		    auditMenuName, messageType, mailNo, taskTitle));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Message_Remove")) {
	    WFFlexDesignStepMessageWeb web = new WFFlexDesignStepMessageWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeMessagePolicy(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Start_Open")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getStart(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Start_Save")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String isQuickIn = myCmdArray.elementAt(4).toString();
	    String isMobileStart = myCmdArray.elementAt(5).toString();
	    String defaultTitle = UtilCode.decode(myStr.matchValue(
		    "_defaultTitle[", "]defaultTitle_"));
	    WorkFlowModel model = (WorkFlowModel) WorkFlowCache
		    .getModel(Integer.parseInt(workFlowId));
	    model._isQuickIn = "1".equals(isQuickIn);
	    model._isMobileStart = "1".equals(isMobileStart);
	    model._defaultTitle = defaultTitle;
	    myOut.write(web.savStart(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Start_Security")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String securityType = myCmdArray.elementAt(4).toString();
	    if (securityType == null || securityType.equals(""))
		securityType = "0";
	    myOut.write(web.getWorkFlowSecurity(Integer.parseInt(workFlowId),
		    Integer.parseInt(securityType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Start_Schedule_List")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkflowSchedule(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Start_Security_List")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String securityType = myCmdArray.elementAt(4).toString();
	    if (securityType == null || securityType.equals(""))
		securityType = "0";
	    myOut.write(web.getWorkFlowSecurity(Integer.parseInt(workFlowId),
		    Integer.parseInt(securityType)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Start_Security_Save")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String securityType = myCmdArray.elementAt(4).toString();
	    if (securityType == null || securityType.equals(""))
		securityType = "0";
	    String securityList = UtilCode.decode(myStr.matchValue(
		    "_securityList[", "]securityList_"));
	    WorkFlowModel workflowModel = (WorkFlowModel) WorkFlowCache
		    .getModel(Integer.parseInt(workFlowId));
	    NavigationFunctionModel navigationFunctionModel = (NavigationFunctionModel) NavigationFunctionCache
		    .getModel(workflowModel._flowStyle.lastIndexOf("组") != -1 ? workflowModel._flowStyle
			    .substring(0,
				    workflowModel._flowStyle.lastIndexOf("组"))
			    : workflowModel._flowStyle);
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
	} else if (socketCmd.equals("WorkFlow_Flex_Design_End_Open")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getEnd(Integer.parseInt(workFlowId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_End_Save")) {
	    WFFlexDesignStartAndEndWeb web = new WFFlexDesignStartAndEndWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String isAutoArchives = myCmdArray.elementAt(4).toString();
	    String archivesId = myCmdArray.elementAt(5).toString();
	    String isFlowEndCallAll = myCmdArray.elementAt(6).toString();
	    String isEndCallAuth = myCmdArray.elementAt(7).toString();
	    String flowEndCall = UtilCode.decode(myStr.matchValue(
		    "_flowEndCall[", "]flowEndCall_"));
	    WorkFlowModel model = (WorkFlowModel) WorkFlowCache
		    .getModel(Integer.parseInt(workFlowId));
	    model._isAutoArchives = "1".equals(isAutoArchives);
	    model._archivesId = Integer.parseInt(archivesId);
	    model._isFlowEndCallAll = "1".equals(isFlowEndCallAll);
	    model._isEndCallAuth = "1".equals(isEndCallAuth);
	    model._flowEndCall = flowEndCall;
	    myOut.write(web.saveEnd(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Opinion_Open")) {
	    WFFlexDesignStepOpinionWeb web = new WFFlexDesignStepOpinionWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String id = myCmdArray.elementAt(5).toString();
	    String isShowBack = myCmdArray.elementAt(6).toString();
	    String targetId = myCmdArray.elementAt(7).toString();
	    if (isShowBack == null || "".equals(isShowBack))
		isShowBack = "0";
	    try {
		myOut.write(web.getTab(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId), Integer.parseInt(id),
			Integer.parseInt(isShowBack), targetId));
	    } catch (Exception e) {
		myOut.write(web.getTab(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId), id,
			Integer.parseInt(isShowBack), targetId));
	    }
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Opinion_Create")) {
	    WFFlexDesignStepOpinionWeb web = new WFFlexDesignStepOpinionWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String opinionType = myCmdArray.elementAt(5).toString();
	    String isCheck = myCmdArray.elementAt(6).toString();
	    String opinionId = myCmdArray.elementAt(7).toString();
	    if ("".equals(opinionId) || opinionId == null)
		opinionId = "0";
	    String opinionName = UtilCode.decode(myStr.matchValue(
		    "_opinionName[", "]opinionName_"));
	    String actionSQL = UtilCode.decode(myStr.matchValue("_actionSQL[",
		    "]actionSQL_"));
	    myOut.write(web.createOpinion(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(opinionType), opinionName,
		    Integer.parseInt(isCheck), Integer.parseInt(opinionId),
		    actionSQL));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Opinion_Remove")) {
	    WFFlexDesignStepOpinionWeb web = new WFFlexDesignStepOpinionWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String oid = myCmdArray.elementAt(5).toString();
	    if (oid == null || "".equals(oid))
		oid = "0";
	    myOut.write(web.removeOpinion(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(oid)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_OpinionList_Open")) {
	    WFFlexDesignStepOpinionWeb web = new WFFlexDesignStepOpinionWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getList(workFlowId, workFlowStepId));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_OpinionList_Ajax")) {
	    WFFlexDesignStepOpinionWeb web = new WFFlexDesignStepOpinionWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getList2JSON(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Opinion_Sort")) {
	    WFFlexDesignStepOpinionWeb web = new WFFlexDesignStepOpinionWeb(me);
	    String sourceOpinionId = myCmdArray.elementAt(3).toString();
	    String targetOpinionId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.opinionSort(Integer.parseInt(sourceOpinionId),
		    Integer.parseInt(targetOpinionId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Rule_Open")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String typeId = myCmdArray.elementAt(5).toString();
	    String targetId = myCmdArray.elementAt(6).toString();
	    if (typeId == null || "".equals(typeId))
		typeId = "1";
	    myOut.write(web.getTab(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(typeId),
		    targetId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Rule_Save")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String fieldList = myCmdArray.elementAt(5).toString();
	    String compareType = myCmdArray.elementAt(6).toString();
	    String jumpStepId = myCmdArray.elementAt(7).toString();
	    String ruleGroup = myCmdArray.elementAt(8).toString();
	    String subFlowUUID = myCmdArray.elementAt(9).toString();
	    String subProcessSyn = myCmdArray.elementAt(10).toString();
	    String endStepUUID = myCmdArray.elementAt(11).toString();
	    String muliInstanceLoop = myCmdArray.elementAt(12).toString();
	    String filterType = myCmdArray.elementAt(13).toString();
	    String ruleId = myCmdArray.elementAt(14).toString();
	    if (filterType == null || filterType.equals(""))
		filterType = "0";
	    String compareValue = UtilCode.decode(myStr.matchValue(
		    "_compareValue[", "]compareValue_"));
	    String participant = UtilCode.decode(myStr.matchValue(
		    "_participant[", "]participant_"));
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    String jumpUser = UtilCode.decode(myStr.matchValue("_jumpUser[",
		    "]jumpUser_"));
	    String rules = UtilCode.decode(myStr.matchValue("_rules[",
		    "]rules_"));
	    if (ruleGroup == null)
		ruleGroup = "";
	    if (jumpStepId == null || jumpStepId.equals(""))
		jumpStepId = "0";
	    if (ruleId == null || ruleId.equals(""))
		ruleId = "0";
	    if (!"".equals(rules.trim()) && rules.length() > 0)
		myOut.write(web.saveRule(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId), ruleGroup, rules));
	    else
		myOut.write(web.createRule(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId), fieldList,
			compareType, compareValue,
			Integer.parseInt(jumpStepId), jumpUser, ruleGroup,
			subFlowUUID, subProcessSyn, endStepUUID,
			Integer.parseInt(muliInstanceLoop), participant, title,
			Integer.parseInt(filterType), Integer.parseInt(ruleId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_RuleList_Open")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String ruleId = myCmdArray.elementAt(5).toString();
	    String isShowBack = myCmdArray.elementAt(6).toString();
	    if (ruleId == null || "".equals(ruleId.trim()))
		ruleId = "0";
	    if (isShowBack == null || "".equals(isShowBack.trim()))
		isShowBack = "0";
	    try {
		myOut.write(web.getRuleList(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId),
			Integer.parseInt(ruleId), Integer.parseInt(isShowBack)));
	    } catch (Exception e) {
		myOut.write(web.getRuleList(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId), ruleId,
			Integer.parseInt(isShowBack)));
	    }
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_RuleList_Ajax")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String ruleId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getRuleListToAjax(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(ruleId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_RuleList_Remove")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String ruleId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.removeRule(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId), Integer.parseInt(ruleId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Step_Rule_SubProcessInfo")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String profileId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getSubProcessInfo(Integer.parseInt(workFlowId),
		    Integer.parseInt(workFlowStepId),
		    Integer.parseInt(profileId)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_WFS_Rule_SubProcessInfoSave")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
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
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Rule_AllRules")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String typeId = myCmdArray.elementAt(5).toString();
	    if ("0".equals(typeId))
		myOut.write(web.getAllRules(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId)));
	    else
		myOut.write(web.getAllRules2JSON(Integer.parseInt(workFlowId),
			Integer.parseInt(workFlowStepId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_But")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getButMain(workFlowId, workFlowStepId));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_But_Delete")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeBut(workFlowId, workFlowStepId, idList));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_But_Check")) {
	    WFFlexDesignStepButWeb web = new WFFlexDesignStepButWeb(me);
	    String butExec = myCmdArray.elementAt(3).toString();
	    myOut.write(web.checkButExec(butExec));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_But_Save")) {
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
	    myOut.write(web.saveBut(model));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Rule_Sort")) {
	    WFFlexDesignStepRuleWeb web = new WFFlexDesignStepRuleWeb(me);
	    String sourceNodeId = myCmdArray.elementAt(3).toString();
	    String targetNodeId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.ruleSort(Integer.parseInt(sourceNodeId),
		    Integer.parseInt(targetNodeId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Step_Move")) {
	    WFFlexDesignStepWeb web = new WFFlexDesignStepWeb(me);
	    String workFlowId = myCmdArray.elementAt(3).toString();
	    String workFlowStepId = myCmdArray.elementAt(4).toString();
	    String stepPosition = myCmdArray.elementAt(5).toString();
	    WorkFlowStepModel model = new WorkFlowStepModel();
	    if (workFlowStepId == null || workFlowStepId.equals(""))
		workFlowStepId = "0";
	    if (stepPosition == null || stepPosition.equals(""))
		stepPosition = "0";
	    WorkFlowStepModel moveTargetModel = WorkFlowStepCache
		    .getModelOfStepNo(Integer.parseInt(workFlowId),
			    Integer.parseInt(stepPosition));
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
	    myOut.write(web.moveStep(model, moveTargetModel));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Issue_Log")) {
	    WFFlexDesignWeb web = new WFFlexDesignWeb(me);
	    String logContent = UtilCode.decode(myStr.matchValue(
		    "_logContent[", "]logContent_"));
	    myOut.write(web.issueLog(logContent));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Init_Layout")) {
	    AWSFPDWorkFlowTransact web = new AWSFPDWorkFlowTransact(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.initWorkFlowLayout(uuid));
	    web = null;
	} else if (socketCmd.equals("AWSFlexProcessBPA_Portal")) {
	    WFFlexDesignWeb web = new WFFlexDesignWeb(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    String extendConditions = UtilCode.decode(myStr.matchValue(
		    "_extendConditions[", "]extendConditions_"));
	    myOut.write(web.getFPBPAPortal(Integer.parseInt(wfId),
		    extendConditions));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Get_Workflow_BPA_Info")) {
	    AWSFPDWorkFlowExtendInfo web = new AWSFPDWorkFlowExtendInfo(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    if (wfid == null || wfid.equals(""))
		wfid = "0";
	    String extendConditions = UtilCode.decode(myStr.matchValue(
		    "_extendConditions[", "]extendConditions_"));
	    myOut.write(web.getWorkflowExtendInfo(Integer.parseInt(wfid),
		    extendConditions));
	    web = null;
	} else if (socketCmd.equals("AWSFlexProcessTrack_Portal")) {
	    WFFlexDesignWeb web = new WFFlexDesignWeb(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    String extendConditions = UtilCode.decode(myStr.matchValue(
		    "_extendConditions[", "]extendConditions_"));
	    myOut.write(web.getFPTrackPortal(Integer.parseInt(wfId),
		    extendConditions));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Get_Workflow_Track")) {
	    AWSFPDWorkFlowExtendInfo web = new AWSFPDWorkFlowExtendInfo(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    if (wfid == null || wfid.equals(""))
		wfid = "0";
	    String extendConditions = UtilCode.decode(myStr.matchValue(
		    "_extendConditions[", "]extendConditions_"));
	    myOut.write(web.getWorkflowTrackStatus(Integer.parseInt(wfid),
		    extendConditions));
	    web = null;
	} else if (socketCmd.equals("AWSFPD_Get_Workflow_Track_Info")) {
	    AWSFPDWorkFlowExtendInfo web = new AWSFPDWorkFlowExtendInfo(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    if (wfId == null || wfId.equals(""))
		wfId = "0";
	    String wfsId = myCmdArray.elementAt(4).toString();
	    if (wfsId == null || wfsId.equals(""))
		wfsId = "0";
	    myOut.write(web.getWorkflowTrackStepInfo(Integer.parseInt(wfId),
		    Integer.parseInt(wfsId)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Flex_Design_Email_Template")) {
	    WFFlexDesignEmailTemplateWeb web = new WFFlexDesignEmailTemplateWeb(
		    me);
	    String no = myCmdArray.elementAt(3).toString();
	    no = no != null ? no : "";
	    myOut.write(web.getWeb(no));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Email_Template_New_List")) {
	    WFFlexDesignEmailTemplateWeb web = new WFFlexDesignEmailTemplateWeb(
		    me);
	    String no = myCmdArray.elementAt(3).toString();
	    no = no != null ? no : "";
	    myOut.write(web.getNewList(no));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Email_Template_CheckPassword")) {
	    WFFlexDesignEmailTemplateWeb web = new WFFlexDesignEmailTemplateWeb(
		    me);
	    String password = myCmdArray.elementAt(3).toString();
	    String checkType = myCmdArray.elementAt(4).toString();
	    myOut.write(web.checkPassword(password, checkType));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Email_Template_Simulations")) {
	    WFFlexDesignEmailTemplateWeb web = new WFFlexDesignEmailTemplateWeb(
		    me);
	    String no = myCmdArray.elementAt(3).toString();
	    no = no != null ? no : "";
	    myOut.write(web.SimulationsEmailTemplete(no));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Email_Template_OverView")) {
	    WFFlexDesignEmailTemplateWeb web = new WFFlexDesignEmailTemplateWeb(
		    me);
	    String no = myCmdArray.elementAt(3).toString();
	    no = no != null ? no : "";
	    myOut.write(web.getOverViewEmailTemplete(no));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Flex_Design_Email_Template_Check_TemplateNo")) {
	    WFFlexDesignEmailTemplateWeb web = new WFFlexDesignEmailTemplateWeb(
		    me);
	    String id = myCmdArray.elementAt(3).toString();
	    String no = myCmdArray.elementAt(4).toString();
	    no = no != null ? no : "";
	    myOut.write(web.isHasTempleteNo(Integer.parseInt(id), no));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Version")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    myOut.write(web.getWFVersionListWeb(Integer.parseInt(wfid)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Version_XML_DATA")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    myOut.write(web.getWFVersionListXMLDATA(Integer.parseInt(wfid)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Version_Create")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    myOut.write(web.createNewVersionWF(Integer.parseInt(wfid)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Version_Remove")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    myOut.write(web.removeVersionWF(Integer.parseInt(wfid)));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Version_Publish")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    myOut.write(web.publishVersionWF(Integer.parseInt(wfid)));
	    web = null;
	} else if (socketCmd.equals("WorkFlow_Design_WFS_Design_Version_Copy")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    String newGroupName = myCmdArray.elementAt(4).toString();
	    String newFlowName = myCmdArray.elementAt(5).toString();
	    myOut.write(web.copyWorkFlow(Integer.parseInt(wfid), newGroupName,
		    newFlowName));
	    web = null;
	} else if (socketCmd
		.equals("WorkFlow_Design_WFS_Design_Version_GetWFName")) {
	    WFFlexDesignVersionWeb web = new WFFlexDesignVersionWeb(me);
	    String wfid = myCmdArray.elementAt(3).toString();
	    wfid = wfid != null && wfid.trim().length() != 0 ? wfid : "0";
	    myOut.write(web.getWFVersionName(Integer.parseInt(wfid)));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
