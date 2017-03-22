package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.commons.bigtext.WorkFlowBigText;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.services.web.WSPublishWeb;
import com.actionsoft.awf.services.web.WebserviceProfileWeb;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.eai.loginBridge.model.LoginBridgeMapModel;
import com.actionsoft.eai.loginBridge.model.LoginBridgeModel;
import com.actionsoft.eai.loginBridge.web.LoginBridgeBaseDataTabWeb;
import com.actionsoft.eai.loginBridge.web.LoginBridgeCardWeb;
import com.actionsoft.eai.loginBridge.web.LoginBridgeDeployTabWeb;
import com.actionsoft.eai.loginBridge.web.LoginBridgeMapTabWeb;
import com.actionsoft.eai.loginBridge.web.LoginBridgeRTWeb;
import com.actionsoft.eai.loginBridge.web.LoginBridgeWeb;
import com.actionsoft.i18n.I18nRes;

public class EAISocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("EAI_LoginBridge_List")) {
	    LoginBridgeWeb web = new LoginBridgeWeb(me);
	    myOut.write(web.getLoginBridgeList());
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Remove")) {
	    LoginBridgeWeb web = new LoginBridgeWeb(me);
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeLoginBridge(idList));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Open")) {
	    LoginBridgeCardWeb web = new LoginBridgeCardWeb(me);
	    int loginBridgeId = Integer.parseInt(myCmdArray.elementAt(3)
		    .toString());
	    String boxType = myCmdArray.elementAt(4).toString();
	    if (boxType == null || boxType.equals(""))
		boxType = "0";
	    int boxTypeI = Integer.parseInt(boxType);
	    myOut.write(web.getLoginBridgePage(loginBridgeId, boxTypeI));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Create")) {
	    LoginBridgeCardWeb web = new LoginBridgeCardWeb(me);
	    String loginBridgeName = UtilCode.decode(myStr.matchValue(
		    "_loginBridgeName[", "]loginBridgeName_"));
	    myOut.write(web.createLoginBridge(loginBridgeName));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_BaseData_Open")) {
	    LoginBridgeBaseDataTabWeb web = new LoginBridgeBaseDataTabWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    myOut.write(web.getBaseDataForm(id));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_BaseData_Save")) {
	    LoginBridgeBaseDataTabWeb web = new LoginBridgeBaseDataTabWeb(me);
	    LoginBridgeModel model = new LoginBridgeModel();
	    model._id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    model._formMethod = myCmdArray.elementAt(4).toString();
	    model._formAction = UtilCode.decode(myStr.matchValue(
		    "_formAction[", "]formAction_"));
	    model._title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    myOut.write(web.saveBaseData(model));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Map_Open")) {
	    LoginBridgeMapTabWeb web = new LoginBridgeMapTabWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    myOut.write(web.getMapForm(id));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Map_Create")) {
	    LoginBridgeMapTabWeb web = new LoginBridgeMapTabWeb(me);
	    LoginBridgeMapModel model = new LoginBridgeMapModel();
	    model._lbId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    model._fieldName = myCmdArray.elementAt(4).toString();
	    model._fieldTitle = myCmdArray.elementAt(5).toString();
	    model._fieldType = myCmdArray.elementAt(6).toString();
	    model._defaultValue = UtilCode.decode(myStr.matchValue(
		    "_defaultValue[", "]defaultValue_"));
	    myOut.write(web.createLoginBridgeMap(model));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Map_Remove")) {
	    LoginBridgeMapTabWeb web = new LoginBridgeMapTabWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeLoginBridgeMap(id, idList));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Runtime1")) {
	    LoginBridgeRTWeb web = new LoginBridgeRTWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    myOut.write(web.getLoginPage(id));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Runtime1_Save")) {
	    LoginBridgeRTWeb web = new LoginBridgeRTWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    String keyValues = UtilCode.decode(myStr.matchValue("_keyValues[",
		    "]keyValues_"));
	    myOut.write(web.savaProfile(id, keyValues));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Runtime2")) {
	    LoginBridgeRTWeb web = new LoginBridgeRTWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    myOut.write(web.getAutoLoginPage(id));
	    web = null;
	} else if (socketCmd.equals("EAI_LoginBridge_Deploy_Open")) {
	    LoginBridgeDeployTabWeb web = new LoginBridgeDeployTabWeb(me);
	    int id = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    myOut.write(web.getDeployForm(id));
	    web = null;
	} else if (socketCmd.equals("EAI_HtmlEditor_Content")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String fieldName = myCmdArray.elementAt(4).toString();
	    ProcessInstanceModel headMessageModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    Integer.parseInt(instanceId));
	    if (headMessageModel == null) {
		myOut.write(I18nRes.findValue(me.getLanguage(), "流程实例不存在！"));
	    } else {
		WorkFlowBigText big = new WorkFlowBigText(
			headMessageModel.getProcessDefinitionId(),
			Integer.parseInt(instanceId), fieldName + ".act");
		String mes = big.getBigText();
		mes = mes.equals("") ? I18nRes.findValue(me.getLanguage(),
			"无数据") : mes;
		myOut.write(mes);
	    }
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_OPEN")) {
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.getProfile());
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_ADD")) {
	    String parentNode = myCmdArray.elementAt(3).toString();
	    String name = UtilCode.decode(myStr.matchValue("_name[", "]name_"));
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.getAddNode(parentNode, name));
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_UPDATE")) {
	    String updateNode = UtilCode.decode(myStr.matchValue(
		    "_updateNode[", "]updateNode_"));
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.getUpdateNode(updateNode));
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_CONTENT")) {
	    String profileId = myCmdArray.elementAt(3).toString();
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.getProfileContent(profileId));
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_DELETE")) {
	    String deleteNode = myCmdArray.elementAt(3).toString();
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.getDeleteNode(deleteNode));
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_SEARCH")) {
	    String profileUUID = myCmdArray.elementAt(3).toString();
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.searchProfile(profileUUID));
	} else if (socketCmd.equals("EAI_WEBSERVICE_PROFILE_LOAD")) {
	    String profileId = myCmdArray.elementAt(3).toString();
	    String pageNow = myCmdArray.elementAt(4).toString();
	    if (pageNow == null || "".equals(pageNow))
		pageNow = "0";
	    int now = 1;
	    try {
		now = Integer.parseInt(pageNow);
	    } catch (Exception exception) {
	    }
	    WebserviceProfileWeb wp = new WebserviceProfileWeb(me);
	    myOut.write(wp.getProfileLoad(profileId, now <= 0 ? 1 : now));
	} else if (socketCmd.equals("AWS_DataflowCenter_Main")) {
	    WSPublishWeb wp = new WSPublishWeb(me);
	    myOut.write(wp.getMainWeb());
	} else if (socketCmd.equals("AWS_DataflowCenter_LoadTree")) {
	    WSPublishWeb wp = new WSPublishWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String path = UtilCode.decode(myStr.matchValue("_path[", "]path_"));
	    myOut.write(wp.getTreeJsonData(type, path));
	} else if (socketCmd.equals("AWS_DataflowCenter_Open")) {
	    WSPublishWeb wp = new WSPublishWeb(me);
	    String xmtfile = UtilCode.decode(myStr.matchValue("_xmtfile[",
		    "]xmtfile_"));
	    myOut.write(wp.getTabDetail(xmtfile));
	} else if (socketCmd.equals("AWS_DataflowCenter_Simulate_Open")) {
	    WSPublishWeb wp = new WSPublishWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String xmtfile = UtilCode.decode(myStr.matchValue("_xmtfile[",
		    "]xmtfile_"));
	    myOut.write(wp.simulateOpen(type, xmtfile));
	} else if (socketCmd.equals("AWS_DataflowCenter_Simulate")) {
	    WSPublishWeb wp = new WSPublishWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String profile = myCmdArray.elementAt(4).toString();
	    String xmtfile = UtilCode.decode(myStr.matchValue("_xmtfile[",
		    "]xmtfile_"));
	    String params = UtilCode.decode(myStr.matchValue("_params[",
		    "]params_"));
	    String endpointAddress = UtilCode.decode(myStr.matchValue(
		    "_endpointAddress[", "]endpointAddress_"));
	    myOut.write(wp.simulate(type, profile, xmtfile, params,
		    endpointAddress));
	} else if (socketCmd.equals("AWS_DataflowCenter_CreateLib")) {
	    WSPublishWeb web = new WSPublishWeb(me);
	    String libName = UtilCode.decode(myStr.matchValue("_libName[",
		    "]libName_"));
	    myOut.write(web.createLib(libName));
	    web = null;
	} else if (socketCmd.equals("AWS_DataflowCenter_DeleteLib")) {
	    WSPublishWeb web = new WSPublishWeb(me);
	    String filepath = UtilCode.decode(myStr.matchValue("_filepath[",
		    "]filepath_"));
	    myOut.write(web.deleteFile(filepath));
	    web = null;
	} else if (socketCmd.equals("AWS_DataflowCenter_Digest")) {
	    WSPublishWeb web = new WSPublishWeb(me);
	    String filepath = UtilCode.decode(myStr.matchValue("_filepath[",
		    "]filepath_"));
	    myOut.write(web.getDigestFilePath(filepath));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
