package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.repository.ModelRepositoryTreeJsonData;
import com.actionsoft.awf.repository.ModelRepositoryWeb;
import com.actionsoft.awf.repository.standarddict.web.StandardDictWeb;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;

public class AWSBizModelSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("AWS_BM_Portal")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    myOut.write(web.getAWSBizModelPortal());
	    web = null;
	} else if (socketCmd.equals("AWS_BM_Navigation")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    myOut.write(web.getAWSBizModelNavigation());
	    web = null;
	} else if (socketCmd.equals("AWS_BM_RelationView")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.getAWSRelationView(wfId, list));
	    web = null;
	} else if (socketCmd.equals("AWS_BM_RelationView_Data")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String wfId = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.getAWSRelationData(wfId, list));
	    web = null;
	} else if (socketCmd.equals("BM_Tree_JsonData")) {
	    ModelRepositoryTreeJsonData web = new ModelRepositoryTreeJsonData(
		    me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getAWSBizModelTreeJsonData(requestType, param1,
		    param2, param3));
	    web = null;
	} else if (socketCmd.equals("BM_StandardDict_List")) {
	    StandardDictWeb web = new StandardDictWeb(me);
	    String sdType = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getStandardDictListPage(sdType));
	    web = null;
	} else if (socketCmd.equals("BM_StandardDict_List_AjaxSheetXML")) {
	    StandardDictWeb web = new StandardDictWeb(me);
	    String sdType = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getStandardDictListAjaxSheetXML(sdType));
	    web = null;
	} else if (socketCmd.equals("BM_StandardDict_Save")) {
	    StandardDictWeb web = new StandardDictWeb(me);
	    String data = UtilCode.decode(myStr.matchValue("_data[", "]data_"));
	    myOut.write(web.createStandardDictData(data));
	    web = null;
	} else if (socketCmd.equals("BM_StandardDict_Remove")) {
	    StandardDictWeb web = new StandardDictWeb(me);
	    String selectedKeys = UtilCode.decode(myStr.matchValue(
		    "_selectedKeys[", "]selectedKeys_"));
	    myOut.write(web.removeStandardDictData(selectedKeys));
	    web = null;
	} else if (socketCmd.equals("BM_DataView")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    myOut.write(web.getAWSBizModelDataView("root"));
	    web = null;
	} else if (socketCmd.equals("BM_WorkFlowGroup_DataView")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getAWSBizModelWorkFlowGroupDataView(groupName));
	    web = null;
	} else if (socketCmd.equals("BM_Template_Page")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String templateType = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTemplatePage(Integer.parseInt(metaDataId),
		    templateType));
	    web = null;
	} else if (socketCmd.equals("BM_Search_Model")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String findKey = UtilCode.decode(myStr.matchValue("_findKey[",
		    "]findKey_"));
	    String query = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String start = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getSearchModelDataToJSON(query,
		    Integer.parseInt(limit), Integer.parseInt(start)));
	    web = null;
	} else if (socketCmd.equals("BM_Copy_Model_Page")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String modelType = myCmdArray.elementAt(3).toString();
	    String modelId = myCmdArray.elementAt(4).toString();
	    String category = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getCopyModelPage(modelType,
		    Integer.parseInt(modelId), category));
	    web = null;
	} else if (socketCmd.equals("BM_Copy_WorkFlowModel")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String modelType = myCmdArray.elementAt(3).toString();
	    String modelId = myCmdArray.elementAt(4).toString();
	    String workFlowStyle = myCmdArray.elementAt(5).toString();
	    String workFlowName = myCmdArray.elementAt(6).toString();
	    String groupName = myCmdArray.elementAt(7).toString();
	    Hashtable extendParams = new UnsyncHashtable();
	    if (workFlowStyle != null && !workFlowStyle.equals(""))
		extendParams.put("FLOWSTYLE", workFlowStyle);
	    if (workFlowName != null && !workFlowName.equals(""))
		extendParams.put("FLOWNAME", workFlowName);
	    if (groupName != null && !groupName.equals(""))
		extendParams.put("GROUP_NAME", groupName);
	    myOut.write(web.copyModel(modelType, Integer.parseInt(modelId),
		    extendParams));
	    web = null;
	} else if (socketCmd.equals("BM_Copy_DiggerModel")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String modelType = myCmdArray.elementAt(3).toString();
	    String modelId = myCmdArray.elementAt(4).toString();
	    String diggerName = myCmdArray.elementAt(5).toString();
	    String groupName = myCmdArray.elementAt(6).toString();
	    Hashtable extendParams = new UnsyncHashtable();
	    if (diggerName != null && !diggerName.equals(""))
		extendParams.put("DIGGERNAME", diggerName);
	    if (groupName != null && !groupName.equals(""))
		extendParams.put("GROUP_NAME", groupName);
	    myOut.write(web.copyModel(modelType, Integer.parseInt(modelId),
		    extendParams));
	    web = null;
	} else if (socketCmd.equals("BM_Copy_MetaDataModel")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String modelType = myCmdArray.elementAt(3).toString();
	    String modelId = myCmdArray.elementAt(4).toString();
	    String entityName = myCmdArray.elementAt(5).toString();
	    String entityTitle = myCmdArray.elementAt(6).toString();
	    String groupName = myCmdArray.elementAt(7).toString();
	    String boType = myCmdArray.elementAt(8).toString();
	    Hashtable extendParams = new UnsyncHashtable();
	    if (entityName != null && !entityName.equals(""))
		extendParams.put("ENTITY_NAME", entityName);
	    if (entityTitle != null && !entityTitle.equals(""))
		extendParams.put("ENTITY_TITLE", entityTitle);
	    if (groupName != null && !groupName.equals(""))
		extendParams.put("GROUP_NAME", groupName);
	    if (boType != null && !boType.equals(""))
		extendParams.put("BOTYPE", boType);
	    myOut.write(web.copyModel(modelType, Integer.parseInt(modelId),
		    extendParams));
	    web = null;
	} else if (socketCmd.equals("BM_Copy_FormModel")) {
	    ModelRepositoryWeb web = new ModelRepositoryWeb(me);
	    String modelType = myCmdArray.elementAt(3).toString();
	    String modelId = myCmdArray.elementAt(4).toString();
	    String reportTitle = myCmdArray.elementAt(5).toString();
	    String groupName = myCmdArray.elementAt(6).toString();
	    Hashtable extendParams = new UnsyncHashtable();
	    if (reportTitle != null && !reportTitle.equals(""))
		extendParams.put("REPORT_TITLE", reportTitle);
	    if (groupName != null && !groupName.equals(""))
		extendParams.put("GROUP_NAME", groupName);
	    myOut.write(web.copyModel(modelType, Integer.parseInt(modelId),
		    extendParams));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
