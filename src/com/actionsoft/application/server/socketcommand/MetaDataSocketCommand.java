package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.cache.MetaDataMapCache;
import com.actionsoft.awf.bo.cache.MetaDataRelationCache;
import com.actionsoft.awf.bo.model.MetaDataIndexModel;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.bo.model.MetaDataRelationModel;
import com.actionsoft.awf.bo.web.MetaDataBaseDataTabWeb;
import com.actionsoft.awf.bo.web.MetaDataCardWeb;
import com.actionsoft.awf.bo.web.MetaDataDisplayTabWeb;
import com.actionsoft.awf.bo.web.MetaDataIndexTabWeb;
import com.actionsoft.awf.bo.web.MetaDataMapTabWeb;
import com.actionsoft.awf.bo.web.MetaDataRelationTabWeb;
import com.actionsoft.awf.bo.web.MetaDataSetValidateWeb;
import com.actionsoft.awf.bo.web.MetaDataViewDataTabWeb;
import com.actionsoft.awf.bo.web.MetaDataViewMapTabWeb;
import com.actionsoft.awf.bo.web.MetaDataWeb;
import com.actionsoft.awf.form.execute.plugins.component.FormUIComponentMulComboxImpl;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;

public class MetaDataSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("MetaData_Design_List")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String filterName = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getMetaDataList(groupName, filterName));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_DownloadXML")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.getDownloadXMLDialog(list));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_UploadXMLWindow")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    myOut.write(web.getUpFilePage());
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_UploadXMLImport")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.installUploadXML(groupName));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_CreatePage")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getMetaDataCreatePage(groupName));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Create")) {
	    MetaDataBaseDataTabWeb web = new MetaDataBaseDataTabWeb(me);
	    String entityName = myCmdArray.elementAt(3).toString();
	    String entityTitle = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String boType = myCmdArray.elementAt(6).toString();
	    MetaDataModel model = new MetaDataModel();
	    if (entityName.equals("系统默认"))
		entityName = Long.toString(System.currentTimeMillis());
	    model.setEntityName("BO_" + entityName);
	    if (boType.equals("VIEW"))
		model.setEntityName("VIEW_" + entityName);
	    model.setGroupName(groupName);
	    model.setEntityTitle(entityTitle);
	    model.setMaster(me.getUID());
	    model.setBoType(boType);
	    model.setViewSql("");
	    model.setViewType("COMMON");
	    myOut.write(web.saveMetaData(model));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Create_And_Maps_Save")) {
	    MetaDataBaseDataTabWeb web = new MetaDataBaseDataTabWeb(me);
	    String entityName = myCmdArray.elementAt(3).toString();
	    String entityTitle = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String boType = myCmdArray.elementAt(6).toString();
	    String data = UtilCode.decode(myStr.matchValue("_data[", "]data_"));
	    MetaDataModel model = new MetaDataModel();
	    if (entityName.equals("系统默认"))
		entityName = Long.toString(System.currentTimeMillis());
	    model.setEntityName("BO_" + entityName);
	    if (boType.equals("VIEW"))
		model.setEntityName("VIEW_" + entityName);
	    model.setGroupName(groupName);
	    model.setEntityTitle(entityTitle);
	    model.setMaster(me.getUID());
	    model.setBoType(boType);
	    model.setViewSql("");
	    model.setViewType("COMMON");
	    myOut.write(web.createMetaDataAndCreateMapsDataAjax(model, data));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Create_And_ViewData_Save")) {
	    MetaDataBaseDataTabWeb web = new MetaDataBaseDataTabWeb(me);
	    String entityName = myCmdArray.elementAt(3).toString();
	    String entityTitle = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String boType = myCmdArray.elementAt(6).toString();
	    String viewType = myCmdArray.elementAt(7).toString();
	    String viewSql = UtilCode.decode(myStr.matchValue("_viewSQL[",
		    "]viewSQL_"));
	    MetaDataModel model = new MetaDataModel();
	    if (entityName.equals("系统默认"))
		entityName = Long.toString(System.currentTimeMillis());
	    model.setEntityName("BO_" + entityName);
	    if (boType.equals("VIEW"))
		model.setEntityName("VIEW_" + entityName);
	    model.setGroupName(groupName);
	    model.setEntityTitle(entityTitle);
	    model.setMaster(me.getUID());
	    model.setBoType(boType);
	    model.setViewSql("");
	    model.setViewType("COMMON");
	    myOut.write(web.createMetaDataAndCreateViewDataAjax(model,
		    viewType, viewSql));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Remove")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.removeMetaData(groupName, list));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Open")) {
	    MetaDataCardWeb web = new MetaDataCardWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String pageType = myCmdArray.elementAt(4).toString();
	    MetaDataModel model = (MetaDataModel) MetaDataCache
		    .getModel(Integer.parseInt(metaDataId));
	    if ((pageType == null || pageType.equals(""))
		    && model.getBoType().equals("TABLE"))
		pageType = "1";
	    else if ((pageType == null || pageType.equals(""))
		    && model.getBoType().equals("VIEW"))
		pageType = "6";
	    myOut.write(web.getMetaDataPage(Integer.parseInt(metaDataId),
		    Integer.parseInt(pageType)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_BaseData_Open")) {
	    MetaDataBaseDataTabWeb web = new MetaDataBaseDataTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getBaseDataForm(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_BaseData_Save")) {
	    MetaDataBaseDataTabWeb web = new MetaDataBaseDataTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String entityTitle = myCmdArray.elementAt(4).toString();
	    String groupName = myCmdArray.elementAt(5).toString();
	    String boType = myCmdArray.elementAt(6).toString();
	    String master = UtilCode.decode(myStr.matchValue("_master[",
		    "]master_"));
	    if (metaDataId == null)
		metaDataId = "0";
	    if (master == null || master.equals(""))
		master = me.getUID();
	    MetaDataModel model = new MetaDataModel();
	    model.setEntityTitle(entityTitle);
	    model.setId(Integer.parseInt(metaDataId));
	    model.setGroupName(groupName);
	    model.setBoType(boType);
	    model.setMaster(master);
	    if (model.getGroupName().equals(""))
		model.setGroupName("未分类");
	    myOut.write(web.saveMetaData(model));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_View_Open")) {
	    MetaDataViewDataTabWeb web = new MetaDataViewDataTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getViewDataForm(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_ViewData_SqlSyntaxCheck")) {
	    MetaDataViewDataTabWeb web = new MetaDataViewDataTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String viewType = myCmdArray.elementAt(4).toString();
	    String viewSql = UtilCode.decode(myStr.matchValue("_viewSQL[",
		    "]viewSQL_"));
	    myOut.write(String.valueOf(web.sqlSyntaxCheck(
		    Integer.parseInt(metaDataId), viewType, viewSql, true)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_ViewData_Save")) {
	    MetaDataViewDataTabWeb web = new MetaDataViewDataTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String viewType = myCmdArray.elementAt(4).toString();
	    String viewSql = UtilCode.decode(myStr.matchValue("_viewSQL[",
		    "]viewSQL_"));
	    MetaDataModel model = (MetaDataModel) MetaDataCache
		    .getModel(Integer.parseInt(metaDataId));
	    String oldViewSql = model.getViewSql();
	    String oldViewType = model.getViewType();
	    model.setViewSql(new UtilString(viewSql).replace("__eol__", ""));
	    model.setViewType(viewType);
	    myOut.write(web
		    .saveMetaDataViewAjax(model, oldViewSql, oldViewType));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Relation_Open")) {
	    MetaDataRelationTabWeb web = new MetaDataRelationTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getRelationPage(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Relation_Item_Open")) {
	    MetaDataRelationTabWeb web = new MetaDataRelationTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String relationId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getRelationItemPage(Integer.parseInt(metaDataId),
		    Integer.parseInt(relationId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Relation_Save")) {
	    MetaDataRelationTabWeb web = new MetaDataRelationTabWeb(me);
	    int metaDataId = Integer.parseInt(myCmdArray.elementAt(3)
		    .toString());
	    MetaDataModel md = (MetaDataModel) MetaDataCache
		    .getModel(metaDataId);
	    int relationId = Integer.parseInt(myCmdArray.elementAt(4)
		    .toString());
	    String ftable = myCmdArray.elementAt(5).toString();
	    int constrainttype = Integer.parseInt(myCmdArray.elementAt(6)
		    .toString());
	    int deleteaction = Integer.parseInt(myCmdArray.elementAt(7)
		    .toString());
	    int updateaction = Integer.parseInt(myCmdArray.elementAt(8)
		    .toString());
	    String actionType = myCmdArray.elementAt(9).toString();
	    String pkey = UtilCode.decode(myStr.matchValue("_pkey[", "]pkey_"));
	    String fkey = UtilCode.decode(myStr.matchValue("_fkey[", "]fkey_"));
	    String updatefields = UtilCode.decode(myStr.matchValue(
		    "_updatefields[", "]updatefields_"));
	    String deleteEx = UtilCode.decode(myStr.matchValue("_deleteEx[",
		    "]deleteEx_"));
	    String updateEx = UtilCode.decode(myStr.matchValue("_updateEx[",
		    "]updateEx_"));
	    MetaDataRelationModel model = new MetaDataRelationModel();
	    if (relationId > 0)
		model.setModel((MetaDataRelationModel) MetaDataRelationCache
			.getModel(relationId));
	    model.setPtable(md.getEntityName());
	    model.setFtable(ftable);
	    model.setPkey(pkey);
	    model.setFkey(fkey);
	    model.setConstraintType(constrainttype);
	    model.setDeleteAction(deleteaction);
	    model.setUpdateAction(updateaction);
	    model.setUpdateFields(updatefields);
	    model.setDeleteEx(deleteEx);
	    model.setUpdateEx(updateEx);
	    model.setActionType("".equals(actionType) ? 0 : Integer
		    .parseInt(actionType));
	    myOut.write(web.saveRelation(metaDataId, model));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Relation_Item_Delete")) {
	    MetaDataRelationTabWeb web = new MetaDataRelationTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_indexList[",
		    "]indexList_"));
	    myOut.write(web.deleteRelation(Integer.parseInt(metaDataId), list));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Relation_Data")) {
	    MetaDataRelationTabWeb web = new MetaDataRelationTabWeb(me);
	    String dtype = myCmdArray.elementAt(3).toString();
	    String keyId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getData(dtype, keyId));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_Open")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getMapPage(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_View_Map_Open")) {
	    MetaDataViewMapTabWeb web = new MetaDataViewMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getViewMapPage(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_View")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    if (mapId == null || mapId.equals(""))
		mapId = "0";
	    myOut.write(web.getMapForm(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_View_Map_View")) {
	    MetaDataViewMapTabWeb web = new MetaDataViewMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    if (mapId == null || mapId.equals(""))
		mapId = "0";
	    myOut.write(web.getViewMapForm(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_Save")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    MetaDataViewMapTabWeb web2 = new MetaDataViewMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    MetaDataModel mdModel = (MetaDataModel) MetaDataCache
		    .getModel(Integer.parseInt(metaDataId));
	    String mapId = myCmdArray.elementAt(4).toString();
	    String mapType = myCmdArray.elementAt(5).toString();
	    if (mapId == null || mapId.equals(""))
		mapId = "0";
	    String fieldName = UtilCode.decode(myStr.matchValue("_fieldName[",
		    "]fieldName_"));
	    String fieldTitle = UtilCode.decode(myStr.matchValue(
		    "_fieldTitle[", "]fieldTitle_"));
	    String fieldType = UtilCode.decode(myStr.matchValue("_fieldType[",
		    "]fieldType_"));
	    String fieldLength = UtilCode.decode(myStr.matchValue(
		    "_fieldLength[", "]fieldLength_"));
	    String isNotNull = UtilCode.decode(myStr.matchValue("_isNotNull[",
		    "]isNotNull_"));
	    String fieldDefault = UtilCode.decode(myStr.matchValue(
		    "_fieldDefault[", "]fieldDefault_"));
	    MetaDataMapModel model = new MetaDataMapModel();
	    model.setId(Integer.parseInt(mapId));
	    model.setMetaDataId(Integer.parseInt(metaDataId));
	    model.setFieldName(fieldName);
	    model.setFieldTitle(fieldTitle);
	    model.setMapType(mapType);
	    model.setFieldType(fieldType);
	    try {
		model.setFieldLenth(fieldLength);
		if (fieldLength == null)
		    model.setFieldLenth("");
	    } catch (Exception exception3) {
	    }
	    model.setNotNull(isNotNull.equals("1"));
	    model.setFieldDefault(fieldDefault);
	    if (mdModel.getBoType().equals("TABLE"))
		myOut.write(web.saveMap(model));
	    else if (mdModel.getBoType().equals("VIEW"))
		myOut.write(web2.saveViewMap(model));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Maps_Save")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String data = UtilCode.decode(myStr.matchValue("_data[", "]data_"));
	    myOut.write(web.saveMaps(Integer.parseInt(metaDataId), data));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_Remove")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.removeMap(Integer.parseInt(metaDataId), list));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_Set_Is_Copy")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    String iscopy = myCmdArray.elementAt(5).toString();
	    myOut.write(web.setMapFieldIsCopy(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId), Boolean.valueOf(iscopy)
			    .booleanValue()));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Display_Tree_JsonData")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String metaDataId = myCmdArray.elementAt(4).toString();
	    String mapId = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getUITreeJsonData(requestType, metaDataId, mapId));
	    web = null;
	} else if (socketCmd
		.equals("MetaData_Design_Map_Get_Map_List_AjaxSheetXML")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getMapListAjaxSheetXML(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd
		.equals("MetaData_Design_Map_Get_Map_List_AjaxJSON")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String searchType = myCmdArray.elementAt(3).toString();
	    String query = myCmdArray.elementAt(4).toString();
	    if ("name".equals(searchType))
		myOut.write(web.getAllMetaDatasOfNameToAjaxJSON(query
			.toUpperCase()));
	    else if ("title".equals(searchType))
		myOut.write(web.getAllMetaDatasOfTitleToAjaxJSON(query
			.toUpperCase()));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_CreateTemplateValue")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String templateList = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getMapListTemplateValue(
		    Integer.parseInt(metaDataId), templateList));
	    web = null;
	} else if (socketCmd
		.equals("MetaData_Design_Map_Get_MulCombox_AjaxJSON")) {
	    String uuid = myCmdArray.elementAt(3).toString();
	    String bindValue = myStr.matchValue("_bindValue[", "]bindValue_");
	    String displaySQL = myStr
		    .matchValue("_displaySQL[", "]displaySQL_");
	    myOut.write(FormUIComponentMulComboxImpl.getListData(uuid,
		    bindValue, displaySQL, me));
	} else if (socketCmd.equals("MetaData_Design_Display_Open")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    String fieldType = UtilCode.decode(myStr.matchValue("_FIELDTYPE[",
		    "]FIELDTYPE_"));
	    myOut.write(web.getDisplayPage(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId), fieldType));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_SetValidate_Open")) {
	    MetaDataSetValidateWeb web = new MetaDataSetValidateWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getPage(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_SetValidate_Save")) {
	    MetaDataSetValidateWeb web = new MetaDataSetValidateWeb(me);
	    String mapId = myCmdArray.elementAt(3).toString();
	    String validateType = myCmdArray.elementAt(4).toString();
	    String validateErr = myCmdArray.elementAt(5).toString();
	    String validateTip = myStr.matchValue("_validateTip[",
		    "]validateTip_");
	    String validateRule = new UtilString(myStr.matchValue(
		    "_validateRule[", "]validateRule_"))
		    .replace("__eol__", " ");
	    myOut.write(web.saveSetting(Integer.parseInt(mapId), validateType,
		    validateErr, validateTip, validateRule));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Display_TopDown")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    String flag = myCmdArray.elementAt(5).toString();
	    if (flag.trim().length() == 0)
		flag = "0";
	    myOut.write(web.getMetaDataUIComponentTopDown(
		    Integer.parseInt(metaDataId), Integer.parseInt(mapId),
		    Integer.parseInt(flag)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Display_UITree_JsonData")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getMetaDataUIComponentTree(me, requestType, param1,
		    param2, param3));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Display_View")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    String displayType = UtilCode.decode(myStr.matchValue(
		    "_REQUESTTYPE[", "]REQUESTTYPE_"));
	    String fieldType = UtilCode.decode(myStr.matchValue("_FIELDTYPE[",
		    "]FIELDTYPE_"));
	    myOut.write(web.getDisplayForm(Integer.parseInt(metaDataId),
		    Integer.parseInt(mapId), displayType));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_GetMapIndex")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getMapIndex(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Display_Save")) {
	    MetaDataDisplayTabWeb web = new MetaDataDisplayTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    String displayWidth = UtilCode.decode(myStr.matchValue(
		    "_displayWidth[", "]displayWidth_"));
	    String displayType = UtilCode.decode(myStr.matchValue(
		    "_displayType[", "]displayType_"));
	    String displaySQL = UtilCode.decode(myStr.matchValue(
		    "_displaySQL[", "]displaySQL_"));
	    String inputWidth = UtilCode.decode(myStr.matchValue(
		    "_inputWidth[", "]inputWidth_"));
	    String inputHeight = UtilCode.decode(myStr.matchValue(
		    "_inputHeight[", "]inputHeight_"));
	    String htmlInner = UtilCode.decode(myStr.matchValue("_htmlInner[",
		    "]htmlInner_"));
	    String altText = UtilCode.decode(myStr.matchValue("_alttext[",
		    "]alttext_"));
	    MetaDataMapModel model = (MetaDataMapModel) MetaDataMapCache
		    .getModel(Integer.parseInt(mapId));
	    model.setMetaDataId(Integer.parseInt(metaDataId));
	    model.setDisplaySetting(displaySQL);
	    model.setDisplayType(displayType);
	    model.setHtmlInner(htmlInner);
	    model.setAltText(altText);
	    try {
		model.setInputHeight(Integer.parseInt(inputHeight));
	    } catch (Exception exception) {
	    }
	    try {
		model.setDisplayWidth(Integer.parseInt(displayWidth));
	    } catch (Exception exception1) {
	    }
	    try {
		model.setInputWidth(Integer.parseInt(inputWidth));
	    } catch (Exception exception2) {
	    }
	    myOut.write(web.saveDisplay(model));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Index_Open")) {
	    MetaDataIndexTabWeb web = new MetaDataIndexTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getIndexPage(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Index_Create")) {
	    MetaDataIndexTabWeb web = new MetaDataIndexTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getIndexForm(Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Index_Delete")) {
	    MetaDataIndexTabWeb web = new MetaDataIndexTabWeb(me);
	    String indexList = UtilCode.decode(myStr.matchValue("_indexList[",
		    "]indexList_"));
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.dropIndex(Integer.parseInt(metaDataId), indexList));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Index_Save")) {
	    MetaDataIndexTabWeb web = new MetaDataIndexTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String indexType = myCmdArray.elementAt(4).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    MetaDataIndexModel model = new MetaDataIndexModel();
	    model.setFieldName(list);
	    model.setIndexType(indexType);
	    model.setMetaDataId(Integer.parseInt(metaDataId));
	    myOut.write(web.saveIndex(model));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_View_ArrorUp")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.upIndex(Integer.parseInt(mapId),
		    Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_View_ArrorDown")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.downIndex(Integer.parseInt(mapId),
		    Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_View_BatchArrorUp")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.batchUpIndex(list, Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_Map_View_BatchArrorDown")) {
	    MetaDataMapTabWeb web = new MetaDataMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.batchDownIndex(list, Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_View_Map_View_ArrorUp")) {
	    MetaDataViewMapTabWeb web = new MetaDataViewMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.upIndex(Integer.parseInt(mapId),
		    Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_View_Map_View_ArrorDown")) {
	    MetaDataViewMapTabWeb web = new MetaDataViewMapTabWeb(me);
	    String metaDataId = myCmdArray.elementAt(3).toString();
	    String mapId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.downIndex(Integer.parseInt(mapId),
		    Integer.parseInt(metaDataId)));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_CreateMetaDataPage")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getMetaDataCreateMetaDataPage(groupName));
	    web = null;
	} else if (socketCmd.equals("MetaData_Design_CreateMetaData")) {
	    MetaDataWeb web = new MetaDataWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    String tableName = myCmdArray.elementAt(4).toString();
	    String isImpData = myCmdArray.elementAt(5).toString();
	    String datasource = myCmdArray.elementAt(6).toString();
	    myOut.write(web.createMetaData(groupName, tableName.toUpperCase(),
		    Boolean.valueOf(isImpData).booleanValue(), datasource));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
