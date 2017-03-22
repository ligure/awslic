package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.form.execute.plugins.component.FormUIComponentBizNoManagerWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryActivationWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryLoader;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryTestForJavaWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryTestForXMLWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.AjaxDictionary;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.DictionaryModel;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend2.TreeDepartmentWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend3.GridDictionary;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend4.GridHelpDictionary;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend5.GridCheckDictionary;
import com.actionsoft.awf.form.execute.plugins.dictionary.system.web.DictionaryEditWeb;
import com.actionsoft.awf.form.execute.plugins.dictionary.system.web.DictionarySystemMainWeb;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;

public class DictionarySocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("Dictionary_Public_Open")) {
	    String RTClass = myCmdArray.elementAt(3).toString();
	    String pageNow = myCmdArray.elementAt(4).toString();
	    String instanceId = myCmdArray.elementAt(5).toString();
	    String taskId = myCmdArray.elementAt(6).toString();
	    String xmlFile = myCmdArray.elementAt(7).toString();
	    String viewType = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    String formType = myCmdArray.elementAt(10).toString();
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (instanceId == null || instanceId.equals(""))
		instanceId = "0";
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    if (dbFilter == null)
		dbFilter = "";
	    if (xmlFile == null)
		xmlFile = "";
	    myOut.write(DictionaryLoader.loaderDictionary(me, RTClass,
		    dbFilter, 1, Integer.parseInt(instanceId),
		    Integer.parseInt(taskId), xmlFile, bindValue, viewType,
		    meId, formType));
	} else if (socketCmd.equals("Dictionary_Public_Open1")) {
	    String RTClass = myCmdArray.elementAt(3).toString();
	    String pageNow = myCmdArray.elementAt(4).toString();
	    String instanceId = myCmdArray.elementAt(5).toString();
	    String taskId = myCmdArray.elementAt(6).toString();
	    String xmlFile = myCmdArray.elementAt(7).toString();
	    String viewType = myCmdArray.elementAt(8).toString();
	    String meId = myCmdArray.elementAt(9).toString();
	    String formType = myCmdArray.elementAt(10).toString();
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    if (instanceId == null || instanceId.equals(""))
		instanceId = "0";
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    if (dbFilter == null)
		dbFilter = "";
	    if (xmlFile == null)
		xmlFile = "";
	    myOut.write(DictionaryLoader.loaderDictionary(me, RTClass,
		    dbFilter, Integer.parseInt(pageNow),
		    Integer.parseInt(instanceId), Integer.parseInt(taskId),
		    xmlFile, bindValue, viewType, meId, formType));
	} else if (socketCmd.equals("Dictionary_Public_Test"))
	    myOut.write(new DictionaryTestForJavaWeb(me).getTestPage());
	else if (socketCmd.equals("Dictionary_Public_XML_Test"))
	    myOut.write(new DictionaryTestForXMLWeb(me).getTestPage());
	else if (socketCmd.equals("Dictionary_Runtime_Acitvation")) {
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String workflowId = myCmdArray.elementAt(4).toString();
	    String titleField = myCmdArray.elementAt(5).toString();
	    String keyField = myCmdArray.elementAt(6).toString();
	    if (metaDataId == null || metaDataId.equals(""))
		metaDataId = "0";
	    if (workflowId == null || workflowId.equals(""))
		workflowId = "0";
	    myOut.write(new DictionaryActivationWeb(me).getPage(
		    Integer.parseInt(metaDataId), Integer.parseInt(workflowId),
		    titleField, keyField));
	} else if (socketCmd.equals("Dictionary_Department_Tree")) {
	    String grid = myCmdArray.elementAt(3).toString();
	    String idField = myCmdArray.elementAt(4).toString();
	    String nameField = myCmdArray.elementAt(5).toString();
	    String entityName = UtilCode.decode(myStr.matchValue(
		    "_entityName[", "]entityName_"));
	    String valueType = UtilCode.decode(myStr.matchValue("_valueType[",
		    "]valueType_"));
	    myOut.write(new TreeDepartmentWeb(me).getTree(valueType,
		    Boolean.valueOf(grid), idField, nameField, entityName));
	} else if (socketCmd.equals("Dictionary_List_Open")) {
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String instanceId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String xmlFile = myCmdArray.elementAt(6).toString();
	    String separatedType = myCmdArray.elementAt(7).toString();
	    if (separatedType.trim().length() == 0)
		separatedType = "0";
	    String viewType = myCmdArray.elementAt(8).toString();
	    String isMuilt = myCmdArray.elementAt(9).toString();
	    if (isMuilt.trim().length() == 0)
		isMuilt = "0";
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    if (Integer.parseInt(isMuilt) == 1)
		dbFilter = "";
	    String rtClass = UtilCode.decode(myStr.matchValue("_rtClass[",
		    "]rtClass_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    myOut.write(new GridDictionary(me).getMainPage(rtClass, dbFilter,
		    Integer.parseInt(pageNow), Integer.parseInt(instanceId),
		    Integer.parseInt(taskId), xmlFile, viewType, bindValue,
		    Integer.parseInt(separatedType)));
	} else if (socketCmd.equals("Dictionary_Action_Open")) {
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String instanceId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String xmlFile = myCmdArray.elementAt(6).toString();
	    String separatedType = myCmdArray.elementAt(7).toString();
	    String viewType = myCmdArray.elementAt(8).toString();
	    String isMuilt = myCmdArray.elementAt(9).toString();
	    if (isMuilt.trim().length() == 0)
		isMuilt = "0";
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String rtClass = UtilCode.decode(myStr.matchValue("_rtClass[",
		    "]rtClass_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    myOut.write(new GridDictionary(me).getUpPage(rtClass, dbFilter,
		    Integer.parseInt(pageNow), Integer.parseInt(instanceId),
		    Integer.parseInt(taskId), xmlFile, viewType, bindValue,
		    Integer.parseInt(isMuilt), separatedType));
	} else if (socketCmd.equals("Dictionary_Target_Open")) {
	    String viewType = myCmdArray.elementAt(3).toString();
	    myOut.write(new GridDictionary(me).getTargetPage(viewType));
	} else if (socketCmd.equals("Dictionary_Help_List_Open")) {
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String instanceId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String xmlFile = myCmdArray.elementAt(6).toString();
	    String xmlType = myCmdArray.elementAt(7).toString();
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String rtClass = UtilCode.decode(myStr.matchValue("_rtClass[",
		    "]rtClass_"));
	    myOut.write(new GridHelpDictionary(me).getMainPage(rtClass,
		    dbFilter, Integer.parseInt(pageNow),
		    Integer.parseInt(instanceId), Integer.parseInt(taskId),
		    xmlFile, xmlType));
	} else if (socketCmd.equals("Dictionary_Help_Action_Open")) {
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String instanceId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String xmlFile = myCmdArray.elementAt(6).toString();
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String rtClass = UtilCode.decode(myStr.matchValue("_rtClass[",
		    "]rtClass_"));
	    myOut.write(new GridHelpDictionary(me).getUpPage(rtClass, dbFilter,
		    Integer.parseInt(pageNow), Integer.parseInt(instanceId),
		    Integer.parseInt(taskId), xmlFile));
	} else if (socketCmd.equals("Dictionary_Help_Target_Open"))
	    myOut.write(new GridHelpDictionary(me).getTargetPage());
	else if (socketCmd.equals("Dictionary_Insert_SubForm")) {
	    String workflowId = myCmdArray.elementAt(3).toString();
	    String workflowStepId = myCmdArray.elementAt(4).toString();
	    String xmlFile = myCmdArray.elementAt(5).toString();
	    String instanceId = myCmdArray.elementAt(6).toString();
	    String taskId = myCmdArray.elementAt(7).toString();
	    String pageNow = myCmdArray.elementAt(8).toString();
	    String refId = myCmdArray.elementAt(9).toString();
	    String formType = myCmdArray.elementAt(10).toString();
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String rtClass = UtilCode.decode(myStr.matchValue("_rtClass[",
		    "]rtClass_"));
	    String id = UtilCode.decode(myStr.matchValue("_id[", "]id_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    GridCheckDictionary gc = new GridCheckDictionary(me);
	    try {
		gc.setRefId(Integer.parseInt(refId));
	    } catch (Exception exception) {
	    }
	    myOut.write(gc.insert2Sheet(id, workflowId, workflowStepId,
		    xmlFile, instanceId, taskId, dbFilter, rtClass, pageNow,
		    formType, bindValue));
	} else if (socketCmd.equals("Dictionary_BizNo_Open")) {
	    String wfBindId = myCmdArray.elementAt(3).toString();
	    String fieldName = myCmdArray.elementAt(4).toString();
	    String groupName = UtilCode.decode(myStr.matchValue("_groupName[",
		    "]groupName_"));
	    myOut.write(new FormUIComponentBizNoManagerWeb(me).getSelectPage(
		    groupName, fieldName, wfBindId));
	} else if (socketCmd.equals("Dictionary_BizNo_Get")) {
	    String wfBindId = myCmdArray.elementAt(3).toString();
	    String fieldName = myCmdArray.elementAt(4).toString();
	    String groupName = UtilCode.decode(myStr.matchValue("_groupName[",
		    "]groupName_"));
	    String nextNo = UtilCode.decode(myStr.matchValue("_nextNo[",
		    "]nextNo_"));
	    myOut.write(new FormUIComponentBizNoManagerWeb(me).getBizNo(
		    groupName, fieldName, wfBindId, nextNo));
	} else if (socketCmd.equals("Dictionary_Public_Ajax_QueryXml")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String xmlFile = myCmdArray.elementAt(5).toString();
	    String sourceFiledName = myCmdArray.elementAt(6).toString();
	    String dbFilter = UtilCode.decode(myStr.matchValue("_dbFilter[",
		    "]dbFilter_"));
	    String bindValue = UtilCode.decode(myStr.matchValue("_bindValue[",
		    "]bindValue_"));
	    if (instanceId == null || instanceId.equals(""))
		instanceId = "0";
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    if (dbFilter == null)
		dbFilter = "";
	    if (xmlFile == null)
		xmlFile = "";
	    myOut.write(new AjaxDictionary(me).getXMLData(sourceFiledName,
		    xmlFile, dbFilter, bindValue, Integer.parseInt(instanceId),
		    Integer.parseInt(taskId)));
	} else if (socketCmd.equals("Dictionary_Public_TreeLoader_JSONDATE")) {
	    TreeDepartmentWeb web = new TreeDepartmentWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getJsonTreeOfORG(requestType, param1, param2,
		    param3));
	    web = null;
	} else if (socketCmd.equals("AWS_System_Dict_List")) {
	    DictionarySystemMainWeb web = new DictionarySystemMainWeb(me);
	    String treegroup = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getDictionaryMainInstance(treegroup));
	} else if (socketCmd.equals("AWS_System_Dict_Open")) {
	    DictionarySystemMainWeb web = new DictionarySystemMainWeb(me);
	    String dictionaryId = myCmdArray.elementAt(3).toString();
	    if (dictionaryId == null || dictionaryId.equals(""))
		dictionaryId = "0";
	    myOut.write(web.getDictionaryBaseInfo(Integer
		    .parseInt(dictionaryId)));
	    web = null;
	} else if (socketCmd.equals("AWS_System_Dict_BaseInfo_list")) {
	    DictionarySystemMainWeb web = new DictionarySystemMainWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    if (id == null || id.equals(""))
		id = "0";
	    String isModify = myCmdArray.elementAt(4).toString();
	    if (isModify == null || isModify.equals(""))
		isModify = "0";
	    myOut.write(web.getDictionaryInfoTab(Integer.parseInt(id),
		    Integer.parseInt(isModify)));
	    web = null;
	} else if (socketCmd.equals("AWS_System_Dict_BaseInfo_Query")) {
	    DictionarySystemMainWeb web = new DictionarySystemMainWeb(me);
	    String name = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getDictionaryQuery(name));
	    web = null;
	} else if (socketCmd.equals("AWS_System_Dict_BaseInfo_delet")) {
	    DictionarySystemMainWeb web = new DictionarySystemMainWeb(me);
	    String treegroup = myCmdArray.elementAt(3).toString();
	    String bindid = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getDictionaryDelete(treegroup,
		    Integer.parseInt(bindid)));
	    web = null;
	} else if (socketCmd.equals("AWS_System_Dict_BaseInfo_Create")) {
	    DictionarySystemMainWeb web = new DictionarySystemMainWeb(me);
	    String treegroup = myCmdArray.elementAt(3).toString();
	    String dictName = myCmdArray.elementAt(4).toString();
	    String dictType = myCmdArray.elementAt(5).toString();
	    if ("on".equals(dictType))
		dictType = "平台";
	    String dictKey = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getDictionaryCreate(treegroup, dictName, dictType,
		    dictKey));
	    web = null;
	} else if (socketCmd.equals("Dictionary_Edit_Web")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    if (metaDataId.trim().length() == 0)
		metaDataId = "0";
	    String type = myCmdArray.elementAt(4).toString();
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    myOut.write(web.getDictionaryEditMainWeb(dictName,
		    Integer.parseInt(metaDataId), type));
	} else if (socketCmd.equals("Dictionary_Edit_fileList")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String sort = myCmdArray.elementAt(3).toString();
	    String sortType = myCmdArray.elementAt(4).toString();
	    if (sortType.trim().length() == 0)
		sortType = "0";
	    String type = myCmdArray.elementAt(5).toString();
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    myOut.write(web.getDictionaryEditAjaxData(sort,
		    Integer.parseInt(sortType), dictName, type));
	} else if (socketCmd.equals("Dictionary_Edit_FileInfo")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    if (metaDataId.trim().length() == 0)
		metaDataId = "0";
	    String type = myCmdArray.elementAt(4).toString();
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    myOut.write(web.getDictionaryEditTabBaseInfo(dictName,
		    Integer.parseInt(metaDataId), type));
	    web = null;
	} else if (socketCmd.equals("Dictionary_Edit_FileExists")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    myOut.write(web.isDictionaryFileExists(dictName));
	} else if (socketCmd.equals("Dictionary_Edit_FileOperate")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String type = myCmdArray.elementAt(3).toString();
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    String dictTile = UtilCode.decode(myStr.matchValue("_DICTTITLE[",
		    "]DICTTITLE_"));
	    String dictLine = UtilCode.decode(myStr.matchValue("_DICTLINE[",
		    "]DICTLINE_"));
	    String dictSql = UtilCode.decode(myStr.matchValue("_DICTSQL[",
		    "]DICTSQL_"));
	    String dictJsBefore = UtilCode.decode(myStr.matchValue(
		    "_DICTJSBEFORE[", "]DICTJSBEFORE_"));
	    String dictJsAfter = UtilCode.decode(myStr.matchValue(
		    "_DICTJSAFTER[", "]DICTJSAFTER_"));
	    String dictMapping = UtilCode.decode(myStr.matchValue(
		    "_DICTMAPPING[", "]DICTMAPPING_"));
	    String dictQuery = UtilCode.decode(myStr.matchValue("_DICTQUERY[",
		    "]DICTQUERY_"));
	    String dictChoiceField = UtilCode.decode(myStr.matchValue(
		    "_DICTCHOICEFIELD[", "]DICTCHOICEFIELD"));
	    String dictChoiceName = UtilCode.decode(myStr.matchValue(
		    "_DICTCHOICENAME[", "]DICTCHOICENAME_"));
	    String dictChoiceSql = UtilCode.decode(myStr.matchValue(
		    "_DICTCHOICESQL[", "]DICTCHOICESQL_"));
	    String dictDataAdapter = UtilCode.decode(myStr.matchValue(
		    "_DICTDATAADAPTER[", "]DICTDATAADAPTER_"));
	    String dictFromTable = UtilCode.decode(myStr.matchValue(
		    "_DICTFROMTABLE[", "]DICTFROMTABLE_"));
	    String dictTargetTable = UtilCode.decode(myStr.matchValue(
		    "_DICTTARGETTABLE[", "]DICTTARGETTABLE_"));
	    String fileInfo = UtilCode.decode(myStr.matchValue("_FILEINFO[",
		    "]FILEINFO_"));
	    DictionaryModel model = new DictionaryModel();
	    model._title = dictTile;
	    model._line = dictLine;
	    model._sql = dictSql;
	    model._insertBeforeJavaScript = dictJsBefore;
	    model._insertAfterJavaScript = dictJsAfter;
	    model._choiceField = dictChoiceField;
	    model._choiceName = dictChoiceName;
	    model._choiceSql = dictChoiceSql;
	    model._fromTable = dictFromTable;
	    model._targetTable = dictTargetTable;
	    model._dictMapping = dictMapping;
	    model._conditionUI = dictQuery;
	    model._dataAdapter = dictDataAdapter;
	    myOut.write(web.operateDictionaryEditFileButton(type, dictName,
		    model));
	    web = null;
	} else if (socketCmd.equals("Dictionary_Edit_AjaxFieldXML")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    myOut.write(web.getAjaxFiledDataXML(dictName));
	    web = null;
	} else if (socketCmd.equals("Dictionary_Edit_AjaxConditionUIXML")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String dictName = UtilCode.decode(myStr.matchValue("_DICTNAME[",
		    "]DICTNAME_"));
	    myOut.write(web.getAjaxConditionUIDataXML(dictName));
	} else if (socketCmd.equals("Dictionary_Edit_FileMerage")) {
	    DictionaryEditWeb web = new DictionaryEditWeb(me);
	    String dictLocalSql = UtilCode.decode(myStr.matchValue("_SQL[",
		    "]SQL_"));
	    myOut.write(web.isFileMerageSql(dictLocalSql));
	} else if (socketCmd.equals("Dictionary_Public_Clear")) {
	    String RTClass = myCmdArray.elementAt(3).toString();
	    String instanceId = myCmdArray.elementAt(4).toString();
	    String taskId = myCmdArray.elementAt(5).toString();
	    String xmlFile = myCmdArray.elementAt(6).toString();
	    if (instanceId == null || instanceId.equals(""))
		instanceId = "0";
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    if (xmlFile == null)
		xmlFile = "";
	    myOut.write(DictionaryLoader.loaderDictionary_clear(me, RTClass,
		    Integer.parseInt(instanceId), Integer.parseInt(taskId),
		    xmlFile));
	} else {
	    return false;
	}
	return true;
    }
}
