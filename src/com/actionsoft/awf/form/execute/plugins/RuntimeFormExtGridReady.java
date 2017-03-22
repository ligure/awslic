package com.actionsoft.awf.form.execute.plugins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.actionsoft.application.portal.navigation.util.NavUtil;
import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.cache.MetaDataMapCache;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.commons.security.ac.util.AccessControlUtil;
import com.actionsoft.awf.form.design.cache.FormCache;
import com.actionsoft.awf.form.design.cache.SheetCache;
import com.actionsoft.awf.form.design.cache.SheetDisplayCache;
import com.actionsoft.awf.form.design.model.FormModel;
import com.actionsoft.awf.form.design.model.SheetDisplayModel;
import com.actionsoft.awf.form.design.model.SheetModel;
import com.actionsoft.awf.form.execute.FormUtil;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.component.file.model.FormUIComponentFilePropertyModel;
import com.actionsoft.awf.form.execute.plugins.component.file.util.FormUIComponentFilePropertyInit;
import com.actionsoft.awf.form.execute.plugins.component.flexaddress.model.AddressUIFlexModel;
import com.actionsoft.awf.form.execute.plugins.component.flexaddress.util.AddressUIFlexUtil;
import com.actionsoft.awf.form.execute.plugins.component.slider.SliderUtil;
import com.actionsoft.awf.form.execute.plugins.component.treesource.ComboboxTree;
import com.actionsoft.awf.form.execute.plugins.component.web.UICascadeUtil;
import com.actionsoft.awf.form.execute.plugins.component.web.UIDBSourceUtil;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.GridDictionary;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend5.GridCheckDictionary;
import com.actionsoft.awf.form.execute.plugins.ext.ExtGridException;
import com.actionsoft.awf.form.execute.plugins.ext.ExtGridReady;
import com.actionsoft.awf.form.execute.plugins.ext.component.AWSButton;
import com.actionsoft.awf.form.execute.plugins.ext.component.ExtComboBox;
import com.actionsoft.awf.form.execute.plugins.ext.component.ExtDateField;
import com.actionsoft.awf.form.execute.plugins.ext.component.ExtNumberField;
import com.actionsoft.awf.form.execute.plugins.ext.component.ExtTextField;
import com.actionsoft.awf.form.execute.plugins.ext.component.ExtToolbarModel;
import com.actionsoft.awf.form.execute.plugins.ext.component.XMLDictionary;
import com.actionsoft.awf.form.execute.plugins.ext.data.ExtRecordConstructionModel;
import com.actionsoft.awf.form.execute.plugins.ext.data.ExtSimpleStore;
import com.actionsoft.awf.form.execute.plugins.ext.data.ExtStoreModel;
import com.actionsoft.awf.form.execute.plugins.ext.data.ExtXmlReaderModel;
import com.actionsoft.awf.form.execute.plugins.ext.grid.ExtColumnModel;
import com.actionsoft.awf.form.execute.plugins.ext.grid.ExtGridPanelModel;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.FlexUpFile;
import com.actionsoft.awf.util.Html;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilFile;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepBindFieldCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepImpExpCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepBindFieldModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.i18n.I18nRes;

public class RuntimeFormExtGridReady {
    protected RuntimeFormManager parent;

    public RuntimeFormExtGridReady(RuntimeFormManager rfm) {
	this.parent = rfm;
    }

    protected ExtGridReady getGridReadyImpl(String entityName,
	    SheetModel sheetModel) {
	return new ExtGridReady(entityName, sheetModel.getTitle());
    }

    protected StringBuilder getPlugins(Hashtable extExtColumnList) {
	StringBuilder plugins = new StringBuilder();
	for (int i = 0; i < extExtColumnList.size(); ++i) {
	    ExtColumnModel extColumnModel = (ExtColumnModel) extExtColumnList
		    .get(new Integer(i));
	    if (extColumnModel.getEditor() != null
		    && extColumnModel.getEditor().equals("check")) {
		plugins.append("AWS_CHECK_" + extColumnModel.getDataIndex())
			.append(",");
	    }
	}
	if (plugins.length() > 0) {
	    plugins.setLength(plugins.length() - 1);
	    String tmp = plugins.toString();
	    plugins = new StringBuilder("[" + tmp + "]");
	}
	return plugins;
    }

    protected StringBuilder getRemoveGrid(UserContext me,
	    ProcessInstanceModel processInstanceModel, int workflowTaskState,
	    int taskId, int pageNow, SheetModel sheetModel) {
	StringBuilder buildGridPrivateCode = new StringBuilder();
	buildGridPrivateCode.append("function RemoveGrid(){\n");
	addRefCheck(buildGridPrivateCode, sheetModel);
	buildGridPrivateCode
		.append("\tvar sels = AWS_GRID_PANEL.selModel.getSelections();\n");
	buildGridPrivateCode.append("\tif(sels.length>0){\n");
	buildGridPrivateCode.append("\t\tvar existDB = false;\n");
	buildGridPrivateCode.append("\t\tExt.each(sels,function(sel){\n");
	buildGridPrivateCode
		.append("\t\t\tif(sel.get(\"ID\")!=\"\" && sel.get(\"ID\") > 0){\n");
	buildGridPrivateCode.append("\t\t\t\texistDB = true;\n");
	buildGridPrivateCode.append("\t\t\t\treturn false;\n");
	buildGridPrivateCode.append("\t\t\t}\n");
	buildGridPrivateCode.append("\t\t});\n");
	buildGridPrivateCode.append("\t\tif(!existDB){\n");
	buildGridPrivateCode.append("\t\t\tExt.each(sels,function(sel){\n");
	buildGridPrivateCode.append("\t\t\t\tsel.commit();\n");
	buildGridPrivateCode
		.append("\t\t\t\tAWS_GRID_PANEL.store.remove(sel);\n");
	buildGridPrivateCode.append("\t\t\t});\n");
	buildGridPrivateCode.append("\t\t}else{\n");
	buildGridPrivateCode.append(" \t\t\tExt.MessageBox.confirm('"
		+ I18nRes.findValue(me.getLanguage(), "提示框") + "', '"
		+ I18nRes.findValue(me.getLanguage(), "是否删除选定的记录？")
		+ "' , doRemoveGrid);\n");
	buildGridPrivateCode.append("\t\t}\n");
	buildGridPrivateCode.append("}else{\n");
	buildGridPrivateCode.append("  var box = Ext.MessageBox.alert('"
		+ I18nRes.findValue(me.getLanguage(), "提示框") + "', '"
		+ I18nRes.findValue(me.getLanguage(), "对不起您没有选择要删除的行")
		+ "');\n");
	buildGridPrivateCode
		.append("  setTimeout(function(){box.hide();},1000);\n");
	buildGridPrivateCode.append("}}\n");
	buildGridPrivateCode.append("function doRemoveGrid(btn){\n");
	buildGridPrivateCode.append("if(btn == 'yes'){\n");
	buildGridPrivateCode.append(" AWS_GRID_PANEL.el.mask('"
		+ I18nRes.findValue(me.getLanguage(), "正在执行删除操作...") + "');\n");
	buildGridPrivateCode
		.append(" var selectedKeys = AWS_GRID_PANEL.selModel.selections.keys;\n");
	buildGridPrivateCode
		.append(" selectedKeys=Ext.encode(selectedKeys);\n");
	buildGridPrivateCode.append(" Ext.Ajax.request({\n");
	buildGridPrivateCode.append("  url: './message.wf',\n");
	buildGridPrivateCode.append("  method: 'POST',\n");
	buildGridPrivateCode.append(" params: {\n");
	buildGridPrivateCode.append("  selectedKeys : selectedKeys,\n");
	buildGridPrivateCode.append("  sid : '").append(me.getSessionId())
		.append("',\n");
	buildGridPrivateCode
		.append("  cmd : 'WorkFlow_Execute_Worklist_BindReport_AjaxSheet_Remove',\n");
	buildGridPrivateCode.append("  id : '")
		.append(processInstanceModel.getId()).append("',\n");
	buildGridPrivateCode.append("  openstate : '")
		.append(workflowTaskState).append("',\n");
	buildGridPrivateCode.append("  taskId : '").append(taskId)
		.append("',\n");
	buildGridPrivateCode.append("  pagenow : '").append(pageNow)
		.append("',\n");
	buildGridPrivateCode.append("  subSheetId : '")
		.append(sheetModel.getId()).append("'\n");
	buildGridPrivateCode.append(" },//end params\n");
	buildGridPrivateCode.append(" failure:function(response,options){\n");
	buildGridPrivateCode.append("  AWS_GRID_PANEL.el.unmask(true);\n");
	buildGridPrivateCode.append("  var box = Ext.MessageBox.alert('"
		+ I18nRes.findValue(me.getLanguage(), "警告框")
		+ "',response.responseText);\n");
	buildGridPrivateCode
		.append("  setTimeout(function(){box.hide();},1000);\n");
	buildGridPrivateCode.append("  AWS_GRID_DS.reload();\n");
	buildGridPrivateCode.append(" },//end failure block\n");
	buildGridPrivateCode.append(" success:function(response,options){\n");
	buildGridPrivateCode.append("  AWS_GRID_PANEL.el.unmask(true);\n");
	buildGridPrivateCode.append("  var box = Ext.MessageBox.alert('"
		+ I18nRes.findValue(me.getLanguage(), "提示框")
		+ "',response.responseText);\n");
	buildGridPrivateCode
		.append("  setTimeout(function(){box.hide();},1000);\n");
	buildGridPrivateCode.append("  AWS_GRID_DS.rejectChanges();\n");
	buildGridPrivateCode.append("  AWS_GRID_DS.reload();\n");
	buildGridPrivateCode.append(" }//end success block\n");
	buildGridPrivateCode.append("});\n");
	buildGridPrivateCode.append(" \n");
	buildGridPrivateCode.append("}\n");
	buildGridPrivateCode.append("}\n");
	return buildGridPrivateCode;
    }

    protected StringBuilder getAddRow(boolean isSaveMasterBo, UserContext me,
	    ProcessInstanceModel processInstanceModel, SheetModel sheetModel,
	    int taskId) {
	StringBuilder buildGridPrivateCode = new StringBuilder();
	buildGridPrivateCode.append("function addRow(){\n");
	addRefCheck(buildGridPrivateCode, sheetModel);
	if (isSaveMasterBo) {
	    buildGridPrivateCode
		    .append("if(getBindValue(frmMain)!='' && frmMain.meId.value=='0'){\n");
	    buildGridPrivateCode.append("  Ext.MessageBox.alert('"
		    + I18nRes.findValue(me.getLanguage(), "提醒框")
		    + "','"
		    + I18nRes.findValue(me.getLanguage(), "请首先点击")
		    + "‘"
		    + I18nRes.findValue(me.getLanguage(),
			    "aws.common.efrom.save") + "’"
		    + I18nRes.findValue(me.getLanguage(), "按钮保存数据") + "!');\n");
	    buildGridPrivateCode.append("  return;\n");
	    buildGridPrivateCode.append("}\n");
	}
	buildGridPrivateCode.append("AWS_GRID_PANEL.el.mask('"
		+ I18nRes.findValue(me.getLanguage(), "从服务器读单元格默认值...")
		+ "');\n");
	buildGridPrivateCode.append("var p ;\n");
	buildGridPrivateCode.append(" Ext.Ajax.request({\n");
	buildGridPrivateCode.append("  url: './xml.wf',\n");
	buildGridPrivateCode.append("  method: 'POST',\n");
	buildGridPrivateCode.append(" params: {\n");
	buildGridPrivateCode.append("  sid : '").append(me.getSessionId())
		.append("',\n");
	buildGridPrivateCode
		.append("  cmd : 'WorkFlow_Execute_Worklist_BindReport_AjaxSheet_CreateDefauleValue',\n");
	buildGridPrivateCode.append("  id : '")
		.append(processInstanceModel.getId()).append("',\n");
	buildGridPrivateCode.append("  meId : '")
		.append(this.parent.get_businessObjectId()).append("',\n");
	buildGridPrivateCode.append("  taskId : '").append(taskId)
		.append("',\n");
	buildGridPrivateCode.append("  subSheetId : '")
		.append(sheetModel.getId()).append("'\n");
	buildGridPrivateCode.append(" },//end params\n");
	buildGridPrivateCode.append(" failure:function(response,options){\n");
	buildGridPrivateCode.append("  AWS_GRID_PANEL.el.unmask(true);\n");
	buildGridPrivateCode.append("  Ext.MessageBox.alert('"
		+ I18nRes.findValue(me.getLanguage(), "警告框")
		+ "',response.responseText);\n");
	buildGridPrivateCode.append("  return;\n");
	buildGridPrivateCode.append(" },//end failure block\n");
	buildGridPrivateCode.append(" success:function(response,options){\n");
	buildGridPrivateCode.append("  AWS_GRID_PANEL.el.unmask(true);\n");

	buildGridPrivateCode
		.append("  p = eval(\"new AWS_GRID_DS_PLANT({})\");\n");
	buildGridPrivateCode.append("  AWS_GRID_PANEL.stopEditing();\n");
	buildGridPrivateCode
		.append("  var insertRowInd=AWS_GRID_DS.data.length;\n");
	buildGridPrivateCode.append("  currentRowInd=insertRowInd;\n");
	buildGridPrivateCode.append("  currentColInd=0;\n");
	buildGridPrivateCode.append("  AWS_GRID_DS.insert(insertRowInd, p);\n");
	buildGridPrivateCode
		.append("  var v = Ext.util.JSON.decode(\"{\" + response.responseText +\"}\");\n");
	buildGridPrivateCode.append("  for(var attr in v){\n");
	buildGridPrivateCode.append("  \tp.set(attr,v[attr]);\n");
	buildGridPrivateCode.append("  }\n");
	buildGridPrivateCode.append("  var rowIndex = 2;\n");
	buildGridPrivateCode.append("  for(var i = 2;i<=rowIndex;i++){\n");
	buildGridPrivateCode.append("       if(AWS_GRID_CM.isHidden(i)){\n");
	buildGridPrivateCode.append("          rowIndex ++;\n");
	buildGridPrivateCode.append("       }else{\n");
	buildGridPrivateCode
		.append("         if(!AWS_GRID_CM.isCellEditable(i,insertRowInd)){\n");
	buildGridPrivateCode.append("            rowIndex ++;\n");
	buildGridPrivateCode.append("         }\n");
	buildGridPrivateCode.append("       }\n");
	buildGridPrivateCode.append("  }\n");
	buildGridPrivateCode
		.append("  AWS_GRID_PANEL.startEditing(insertRowInd, rowIndex);\n");
	buildGridPrivateCode.append(" }//end success block\n");
	buildGridPrivateCode.append("});\n");
	buildGridPrivateCode.append("}\n");
	return buildGridPrivateCode;
    }

    protected StringBuilder getSpecialToolBar(
	    WorkFlowStepModel workflowStepModel) {
	return new StringBuilder();
    }

    protected StringBuilder getDifferentListener(SheetModel sheetModel) {
	StringBuilder buildGridPrivateCode = new StringBuilder();
	buildGridPrivateCode
		.append("AWS_GRID_CHECK.addListener('beforerowselect', handleGridBeforeRowSelect);\n");
	buildGridPrivateCode
		.append("function handleGridBeforeRowSelect(selectionModel, rowIndex, keepExisting, record){\n");
	buildGridPrivateCode
		.append(" var fielter=record.get('AWS_SHEET_FIELTER');\n");
	buildGridPrivateCode
		.append(" var isdelete = getFilterData(fielter, 'isdelete');\n");
	buildGridPrivateCode.append(" if(isdelete=='0'){\n");
	buildGridPrivateCode.append("  return false;\n");
	buildGridPrivateCode.append(" }else{\n");
	buildGridPrivateCode.append("  return true;\n");
	buildGridPrivateCode.append(" }\n");
	buildGridPrivateCode.append("}\n");
	return buildGridPrivateCode;
    }

    protected ExtGridPanelModel getExtGridPanelModel(SheetModel sheetModel) {
	ExtGridPanelModel extGridPanelModel = new ExtGridPanelModel();
	if (sheetModel.getPageNum() > 0) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("new Ext.PagingToolbar({\n");
	    sb.append("  \tstore: AWS_GRID_DS,\n");
	    sb.append("  \tdisplayInfo: true,\n");
	    sb.append("\t\tdisplayMsg:\"" + I18nRes.findValue("共{2}条记录")
		    + "\",\n");
	    sb.append("\t\tbeforePageText :\"" + I18nRes.findValue("第")
		    + "\",\n");
	    sb.append("\t\tafterPageText  :\"" + I18nRes.findValue("页，共{0}页")
		    + "\",\n");
	    sb.append("\t\tfirstText:\"" + I18nRes.findValue("首页") + "\",\n");
	    sb.append("\t\tlastText :\"" + I18nRes.findValue("尾页") + "\",\n");
	    sb.append("\t\tnextText :\"" + I18nRes.findValue("下一页") + "\",\n");
	    sb.append("\t\tprevText :\"" + I18nRes.findValue("上一页") + "\",\n");
	    sb.append("\t\trefreshText :\"" + I18nRes.findValue("刷新") + "\",\n");
	    sb.append("\t\temptyMsg :\"" + I18nRes.findValue("没有数据显示")
		    + "\",\n");
	    sb.append("  \tpageSize: " + sheetModel.getPageNum() + "\n");
	    sb.append("   })\n");
	    extGridPanelModel.bbar = sb.toString();
	}
	return extGridPanelModel;
    }

    protected StringBuilder getColumnModel(Hashtable extExtColumnList) {
	StringBuilder columnModel = new StringBuilder("new ").append(
		"Ext.grid.ColumnModel").append("([");
	String publicColumn = "new Ext.grid.RowNumberer(),";
	columnModel.append(publicColumn);
	columnModel.append("AWS_GRID_CHECK,");
	for (int i = 0; i < extExtColumnList.size(); ++i) {
	    ExtColumnModel extColumnModel = (ExtColumnModel) extExtColumnList
		    .get(new Integer(i));
	    if (extColumnModel.getEditor() != null
		    && extColumnModel.getEditor().equals("check"))
		columnModel
			.append("AWS_CHECK_" + extColumnModel.getDataIndex())
			.append(",");
	    else {
		columnModel.append(extColumnModel.toString()).append(",\n");
	    }
	}
	columnModel.setLength(columnModel.length() - ",\n".length());
	columnModel.append("\n])");
	return columnModel;
    }

    protected StringBuilder getStore(UserContext me,
	    ProcessInstanceModel processInstanceModel, int pageNow,
	    int workflowTaskState, SheetModel sheetModel, String groupingField,
	    String sortInfo, Hashtable extRecordConstructionList, int taskId) {
	StringBuilder store = new StringBuilder("new ").append(
		(groupingField.length() == 0) ? "Ext.data.Store"
			: "Ext.data.GroupingStore").append("(");
	ExtStoreModel extStoreModel = new ExtStoreModel();
	String url = "";
	try {
	    url = "ext_object:'./xml.wf?sid="
		    + URLEncoder.encode(me.getSessionId(), "UTF-8")
		    + "&cmd=WorkFlow_Execute_Worklist_BindReport_AjaxSheet_ReadXML&id="
		    + processInstanceModel.getId() + "&pagenow=" + pageNow
		    + "&openstate=" + workflowTaskState + "&subSheetId="
		    + sheetModel.getId() + "&task_id=" + taskId + "&meId="
		    + this.parent.get_businessObjectId() + "'";
	} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
	}
	extStoreModel.setUrl(url);
	extStoreModel.pruneModifiedRecords = "ext_object:true";
	if (groupingField.length() > 0) {
	    extStoreModel.setGroupField(groupingField);
	}
	if (sortInfo.length() > 0) {
	    String field = sortInfo;
	    String direction = "desc";
	    if (sortInfo.indexOf(",") > -1) {
		field = sortInfo.substring(0, sortInfo.indexOf(","));
		direction = sortInfo.substring(sortInfo.indexOf(",") + 1);
	    }
	    extStoreModel.setSortInfo("{field: '" + field + "', direction: '"
		    + direction + "'}");
	}
	ExtXmlReaderModel extXmlReaderModel = new ExtXmlReaderModel();
	extXmlReaderModel.setId("ID");
	extXmlReaderModel.setRecord("Item");
	extXmlReaderModel.setTotalRecords("TotalRecord");
	extStoreModel.setReader("new " + "Ext.data.XmlReader" + "("
		+ extXmlReaderModel + ",["
		+ getStoreConstruction(extRecordConstructionList).toString()
		+ "])");
	store.append(extStoreModel).append(")");
	return store;
    }

    protected StringBuilder getRecordModel(Hashtable extRecordConstructionList) {
	StringBuilder recordModel = new StringBuilder(
		"Ext.data.Record.create([");
	for (Enumeration e = extRecordConstructionList.elements(); e
		.hasMoreElements();) {
	    ExtRecordConstructionModel construction = (ExtRecordConstructionModel) e
		    .nextElement();
	    recordModel.append(construction).append(",");
	}
	recordModel.setLength(recordModel.length() - 1);
	recordModel.append("])");
	return recordModel;
    }

    protected StringBuilder getStoreConstruction(
	    Hashtable extRecordConstructionList) {
	StringBuilder storeConstruction = new StringBuilder();
	for (Enumeration e = extRecordConstructionList.elements(); e
		.hasMoreElements();) {
	    ExtRecordConstructionModel construction = (ExtRecordConstructionModel) e
		    .nextElement();
	    if (storeConstruction.length() == 0)
		storeConstruction.append("{").append("name:'")
			.append(construction.getName())
			.append("',mapping:'ItemAttributes > ")
			.append(construction.getName()).append("'},");
	    else {
		storeConstruction.append(construction).append(",");
	    }
	}

	if (storeConstruction.length() > 0) {
	    storeConstruction.setLength(storeConstruction.length() - 1);
	}
	return storeConstruction;
    }

    void setSpecialCols(ExtColumnModel extColumnModel,
	    MetaDataMapModel metaDataMapModel) {
	String[] ss = { "ID", "AWS_SHEET_FIELTER", "PARENTSUBID" };
	for (int i = 0; i < ss.length; ++i) {
	    if (metaDataMapModel.getFieldName().equals(ss[i])) {
		extColumnModel.setHidden("true");
		extColumnModel.setHideable("ext_object:false");
	    }
	}
    }

    public String build(SheetModel sheetModel, int height, String width,
	    int pageNow, String groupingField, String sortInfo,
	    HashMap extendParam) {
	ProcessInstanceModel processInstanceModel = this.parent
		.get_headMessageModel();
	WorkFlowModel workflowModel = this.parent.get_workFlowModel();
	WorkFlowStepModel workflowStepModel = this.parent
		.get_workFlowStepModel();
	int workflowTaskState = this.parent.get_workflowTaskState();
	int taskId = this.parent.get_activityInstanceId();
	UserContext me = this.parent.get_me();
	String buildScrip = "";
	HashMap fontColors = new HashMap();
	HashMap colColors = new HashMap();
	if (extendParam.containsKey("fontColors")) {
	    fontColors = (HashMap) extendParam.get("fontColors");
	}
	if (extendParam.containsKey("colColors")) {
	    colColors = (HashMap) extendParam.get("colColors");
	}
	Map metaDataMapList = new HashMap(
		MetaDataMapCache.getListOfMetaData(sheetModel.getMetaDataId()));
	MetaDataModel metaDataModel = (MetaDataModel) MetaDataCache
		.getModel(sheetModel.getMetaDataId());
	MetaDataMapModel idMetaDataMapmodel = new MetaDataMapModel();
	MetaDataMapModel fielderMetaDataMapmodel = new MetaDataMapModel();
	StringBuilder setupDataSourcePrivateCode = new StringBuilder();
	StringBuilder dynamicRes = new StringBuilder();
	StringBuilder buildGridPrivateCode = new StringBuilder();
	StringBuilder validataCellValueJS = new StringBuilder();
	StringBuilder scopCode = new StringBuilder();
	boolean isSaveMasterBo = false;

	idMetaDataMapmodel.setFieldName("ID");
	idMetaDataMapmodel.setFieldTitle("标记");
	idMetaDataMapmodel.setFieldLenth("20");
	idMetaDataMapmodel.setDisplayType("数值");
	idMetaDataMapmodel.setHtmlInner("disabled");
	metaDataMapList.put(new Integer(metaDataMapList.size()),
		idMetaDataMapmodel);

	fielderMetaDataMapmodel.setFieldName("AWS_SHEET_FIELTER");
	fielderMetaDataMapmodel.setFieldTitle("Fielter事件");
	fielderMetaDataMapmodel.setFieldLenth("1000");
	fielderMetaDataMapmodel.setDisplayType("文本");
	fielderMetaDataMapmodel.setHtmlInner("disabled");
	metaDataMapList.put(new Integer(metaDataMapList.size()),
		fielderMetaDataMapmodel);

	ExtGridReady gridReady = getGridReadyImpl(
		metaDataModel.getEntityName(), sheetModel);
	gridReady.setWidth(width);

	String extendCss = getColumnCSS(colColors);
	gridReady.setExtendCss(extendCss);

	FormModel formModel = (FormModel) FormCache.getModel(sheetModel
		.getFormId());
	Map stepBindFieldList = WorkFlowStepBindFieldCache
		.getListOfWorkFlowStep(workflowStepModel._id);
	int tableWidth = 0;
	int columnCount = 0;
	Hashtable extRecordConstructionList = new UnsyncHashtable();
	Hashtable extExtColumnList = new UnsyncHashtable();
	StringBuilder columnsDateTypeJS = new StringBuilder();

	boolean isJQXT_Modify = false;
	if (workflowStepModel._reportIsModify && workflowTaskState == 11) {
	    String taskTitle = DBSql.getString(
		    "select title from wf_task where id=" + taskId, "title");
	    if (taskTitle != null && taskTitle.indexOf("(协同)") == 0)
		isJQXT_Modify = true;
	}

	for (int i = 0; i < metaDataMapList.size(); ++i) {
	    MetaDataMapModel metaDataMapModel = (MetaDataMapModel) metaDataMapList
		    .get(new Integer(i));

	    WorkFlowStepBindFieldModel stepBindModel = WorkFlowStepBindFieldCache
		    .getModelOfMapId(stepBindFieldList,
			    metaDataMapModel.getMetaDataId(),
			    metaDataMapModel.getFieldName());

	    SheetDisplayModel sheetDisplayModel = (SheetDisplayModel) SheetDisplayCache
		    .getSheetDisplay3(sheetModel.getId(),
			    metaDataMapModel.getFieldName());

	    if (metaDataMapModel.getFieldDefault().toLowerCase()
		    .indexOf("@getform(") > -1) {
		isSaveMasterBo = true;
	    }

	    boolean isReadOnly = true;
	    boolean isReadOnly2 = true;
	    if ((isJQXT_Modify || workflowStepModel._reportIsModify || workflowStepModel._reportIsAdd)
		    && stepBindModel._isModify
		    && metaDataMapModel.isModify()
		    && workflowTaskState != 2
		    && workflowTaskState != 8
		    && workflowTaskState != 2) {
		isReadOnly = false;
		isReadOnly2 = false;
	    }

	    if (metaDataMapModel.getHtmlInner().toLowerCase()
		    .indexOf("disabled") > -1
		    || metaDataMapModel.getHtmlInner().toLowerCase()
			    .indexOf("readonly") > -1) {
		isReadOnly = true;
	    }

	    if (AccessControlUtil.accessControlCheck(me,
		    "SYS_WorkFlowStepBindField_AC",
		    String.valueOf(stepBindModel._id), "RW")) {
		isReadOnly = true;
		isReadOnly2 = true;
	    }

	    if (FormUtil.isFnDisplay(me, workflowStepModel, sheetModel,
		    metaDataMapModel)) {
		ExtRecordConstructionModel extRecordConstructionModel = new ExtRecordConstructionModel();
		ExtColumnModel extColumnModel = new ExtColumnModel();
		extRecordConstructionModel.setName(metaDataMapModel
			.getFieldName());
		String title = metaDataMapModel.getFieldTitle();
		if (title.indexOf("**") == 0) {
		    title = title.substring(2);
		}
		extColumnModel
			.setHeader(NavUtil.getLangName("aws-i18n-metadata",
				me.getLanguage(), title)
				+ (metaDataMapModel.isNotNull() ? "(<img src=../aws_img/colNotNull.gif alt=必填项>)"
					: ""));
		extColumnModel.setWidth(Integer.toString(metaDataMapModel
			.getDisplayWidth()));
		extColumnModel.setDataIndex(metaDataMapModel.getFieldName());
		extColumnModel.setSortable("true");
		extColumnModel.setRead(isReadOnly);
		extColumnModel.setAlign(FormUtil.getAlign(metaDataMapModel));

		setSpecialCols(extColumnModel, metaDataMapModel);

		String renderer = getRenderer(metaDataMapModel, colColors,
			fontColors, this.parent.get_formModel());
		extColumnModel.setRenderer(renderer);

		if (metaDataMapModel.isNotNull()
			&& !metaDataMapModel.getDisplayType().equals("附件")) {
		    validataCellValueJS.append("if(f=='")
			    .append(metaDataMapModel.getFieldName())
			    .append("' && v===''){\n");
		    validataCellValueJS
			    .append(" alert('<I18N#数据未通过合法性校验，表格中的字段>[")
			    .append(NavUtil.getLangName("aws-i18n-metadata",
				    me.getLanguage(),
				    metaDataMapModel.getFieldTitle()))
			    .append("]" + I18nRes.findValue("不允许为空") + "');\n");

		    validataCellValueJS.append(" return false;\n");
		    validataCellValueJS.append("}\n");
		}

		if (metaDataMapModel.getDisplayType().equals("日期")) {
		    extRecordConstructionModel.setType("date");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'date';}\n");
		    extRecordConstructionModel.setDateFormat("Y-m-d");
		    ExtDateField field = new ExtDateField();
		    field.setFormat("Y-m-d");
		    field.setRegexText("不是正确的日期格式");

		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, this.parent.get_formModel());

		    extColumnModel.setRenderer(renderer);
		    if (!isReadOnly)
			extColumnModel.setEditor("new AWS_FORM.DateField("
				+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("日期时间")) {
		    extRecordConstructionModel.setType("date");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'dateTime';}\n");
		    extRecordConstructionModel.setDateFormat("Y-m-d H:i:s");
		    ExtDateField field = new ExtDateField();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setFormat("Y-m-d H:i:s");
		    field.setAltFormats("Y-m-d H:i:s");

		    field.setAltFormats("不是正确的日期时间格式");
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, this.parent.get_formModel());

		    extColumnModel.setRenderer(renderer);
		    if (isReadOnly) {
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else
			extColumnModel.setEditor("new AWS_FORM.DateField("
				+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("时间")) {
		    extRecordConstructionModel.setType("string");
		    extRecordConstructionModel.setDateFormat("H:i:s");
		    ExtDateField field = new ExtDateField();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setFormat("H:i:s");
		    field.setAltFormats("H:i:s");

		    field.setAltFormats("不是正确的时间格式");
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, this.parent.get_formModel());

		    extColumnModel.setRenderer(renderer);
		    if (isReadOnly)
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    else
			extColumnModel.setEditor("new AWS_FORM.DateField("
				+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("数值")
			&& metaDataMapModel.getFieldLenth().indexOf(",") == -1) {
		    extRecordConstructionModel.setType("int");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'int';}\n");
		    ExtNumberField field = new ExtNumberField();
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setInvalidText("格式非法，必须是一个有效的数字");
		    extColumnModel.setEditor("new AWS_FORM.NumberField("
			    + field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("数值")
			&& metaDataMapModel.getFieldLenth().indexOf(",") > -1) {
		    extRecordConstructionModel.setType("float");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'float';}\n");
		    ExtNumberField field = new ExtNumberField();
		    field.setInvalidText("格式非法，必须是一个有效的数字");
		    String len = metaDataMapModel.getFieldLenth().substring(0,
			    metaDataMapModel.getFieldLenth().indexOf(","));
		    field.setMaxLength(len);
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().indexOf(",") != -1) {
			field.decimalPrecision = metaDataMapModel
				.getFieldLenth().substring(
					metaDataMapModel.getFieldLenth()
						.indexOf(",") + 1);
		    }
		    field.setMaxLengthText("最大长度不允许超过" + len + "个字符!");
		    extColumnModel.setEditor("new AWS_FORM.NumberField("
			    + field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("复选框")) {
		    String displaySql = metaDataMapModel.getDisplaySetting();
		    UtilString util = new UtilString(displaySql);
		    Vector v = util.split("|");
		    List l = null;
		    String single = "";
		    if (displaySql != null && displaySql.indexOf("SQL>") == 0) {
			String[] fs = v.get(0).toString()
				.substring("SQL>".length()).split(":");
			String dataField = fs[1].toString();
			String displayField = ("".equals(fs[0])) ? dataField
				: fs[0];
			String sql = v.get(1).toString();
			String source = (v.size() > 2) ? v.get(2).toString()
				: "";

			Connection conn = null;
			Statement stmt = null;
			ResultSet rset = null;
			l = new ArrayList();
			try {
			    conn = UIDBSourceUtil.open(source, me);
			    stmt = conn.createStatement();
			    rset = DBSql.executeQuery(conn, stmt,
				    this.parent.convertMacrosValue(sql));
			    while (rset.next()) {
				String dataOfValue = rset.getString(dataField);
				String dataOfDisplay = rset
					.getString(displayField);
				String[] s = {
					dataOfValue == null ? "" : dataOfValue,
					dataOfDisplay == null ? ""
						: dataOfDisplay };
				l.add(s);
				single = single + dataOfValue + ":"
					+ dataOfDisplay + "|";
			    }
			} catch (Exception e) {
			    e.printStackTrace(System.err);
			    l.clear();
			    l.add(new String[] { "", Html.escape(e.toString()) });
			} finally {
			    UIDBSourceUtil.close(source, conn, stmt, rset);
			}
		    }

		    if (l != null && l.size() > 3 || v != null && v.size() > 3) {
			columnsDateTypeJS.append("if(ind=='"
				+ metaDataMapModel.getFieldName()
				+ "'){return 'string';}\n");
			extColumnModel.setWidth(String.valueOf(metaDataMapModel
				.getDisplayWidth() <= 20 ? 100
				: metaDataMapModel.getDisplayWidth()));
			ExtComboBox field = new ExtComboBox();
			if (isReadOnly) {
			    field.setReadOnly("true");
			}
			if (metaDataMapModel.getFieldLenth().length() > 0) {
			    field.setMaxLength(metaDataMapModel.getFieldLenth());
			    field.setMaxLengthText("最大长度不允许超过"
				    + metaDataMapModel.getFieldLenth() + "个字符!");
			}
			field.setTypeAhead("true");
			field.setMode("local");
			field.setTriggerAction("all");

			ExtSimpleStore store = new ExtSimpleStore();
			store.setFields("ext_object:['name','value']");

			String storeData = "";
			if (l != null) {
			    storeData = JSONArray.fromObject(l).toString();
			} else {
			    StringBuilder sb = new StringBuilder();
			    sb.append("[");
			    for (int k = 0; k < v.size(); ++k) {
				String val = v.get(k).toString();
				String key = "";
				String value = "";
				if (val.indexOf(":") > -1) {
				    key = val.substring(0, val.indexOf(":"));
				    value = val.substring(val.indexOf(":") + 1,
					    val.length());
				} else {
				    key = value = val;
				}
				sb.append("['").append(key).append("','")
					.append(value).append("'],");
			    }
			    sb.setLength(sb.length() - 1);
			    sb.append("]");
			    storeData = sb.toString();
			}
			store.setData(storeData);

			field.setForceSelection("true");
			field.setEmptyText("请选择");
			field.setDisplaySeparator(";");
			field.setValueSeparator(",");
			field.setHiddenName(metaDataMapModel.getFieldName());
			field.setSelectedClass("x-combo-selected");
			field.setTriggerClass("x-form-arrow-trigger");
			field.setEntityName(metaDataModel.getEntityName());
			field.setStore("new Ext.data.SimpleStore("
				+ store.toString() + ")");
			field.setDisplayField("value");
			field.setValueField("name");
			if (isReadOnly)
			    extColumnModel.setEditor("new AWS_FORM.TextField("
				    + field + ")");
			else
			    extColumnModel
				    .setEditor("new AWS_FORM.MultiSelectField("
					    + field + ")");
		    } else {
			extRecordConstructionModel.setType("string");
			extColumnModel.setEditor("check");
			scopCode.append("var AWS_CHECK_")
				.append(metaDataMapModel.getFieldName())
				.append("= new Ext.grid.CheckColumn({\n");
			scopCode.append("header: '")
				.append(NavUtil.getLangName(
					"aws-i18n-metadata", me.getLanguage(),
					metaDataMapModel.getFieldTitle())
					+ (metaDataMapModel.isNotNull() ? "(<img src=../aws_img/colNotNull.gif alt=必填项>)"
						: "")).append("',\n");
			scopCode.append("dataIndex: '")
				.append(metaDataMapModel.getFieldName())
				.append("',\n");
			scopCode.append("displaySql_Value:'")
				.append(l != null ? single
					: single.length() > 0 ? single
						.substring(0, single.length())
						: metaDataMapModel
							.getDisplaySetting())
				.append("',\n");
			scopCode.append("entityName:'")
				.append(metaDataModel.getEntityName())
				.append("',\n");
			if (isReadOnly)
			    scopCode.append("disabled: 'disabled',\n");
			else {
			    scopCode.append("disabled: '',\n");
			}
			scopCode.append("width: ")
				.append(metaDataMapModel.getDisplayWidth())
				.append("\n");
			scopCode.append("})\n");
		    }
		} else if (metaDataMapModel.getDisplayType().equals("数据字典")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    XMLDictionary field = new XMLDictionary();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setAutoHeight("false");
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    field.setNameSpace(metaDataModel.getEntityName());
		    field.setRtClass(GridDictionary.class.getName());
		    String xmlFile = metaDataMapModel.getDisplaySetting();
		    String innerEvent = metaDataMapModel.getHtmlInner();
		    String displaySql = metaDataMapModel.getDisplaySetting();
		    String tmpSql = "";
		    int separatedChecked = 0;
		    if (innerEvent != null
			    && innerEvent.toLowerCase().indexOf("onchange") == -1
			    && displaySql.indexOf("|多选") == -1) {
			if (xmlFile.trim().length() > 0
				&& xmlFile.indexOf("|数据效验") != -1) {
			    xmlFile = xmlFile.substring(0,
				    xmlFile.indexOf("|数据效验"));
			}
			if (isGetFormDate(xmlFile))
			    xmlFile = xmlFile + "$";
		    } else if (innerEvent != null
			    && innerEvent.toLowerCase().indexOf("onchange") == -1
			    && displaySql.indexOf("|多选") != -1) {
			String[] disArray = displaySql.split("\\|");
			String separated = disArray.length > 1 ? disArray.length > 2 ? disArray.length > 3 ? disArray.length > 4 ? disArray[4]
				: disArray[3]
				: disArray[2]
				: disArray[1]
				: "";
			if (xmlFile.trim().length() > 0
				&& xmlFile.indexOf("|数据效验") != -1) {
			    xmlFile = xmlFile.substring(0,
				    xmlFile.indexOf("|数据效验"));
			}
			String t = xmlFile;
			xmlFile = xmlFile + "|" + separated;

			if (isGetFormDate(t)) {
			    xmlFile = xmlFile + "$";
			}
		    }
		    field.setXmlFile(xmlFile);
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    if (isReadOnly2)
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    else
			extColumnModel
				.setEditor("new AWS_FORM.XMLDictionaryField("
					+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("地址簿")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, formModel);
		    XMLDictionary field = new XMLDictionary();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    field.setNameSpace(metaDataModel.getEntityName());
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    field.setOnTriggerClick("ext_object:function(){openMailTreeGrid(frmMain,'"
			    + metaDataMapModel.getFieldName()
			    + "','"
			    + metaDataModel.getEntityName()
			    + "');return false;}");
		    if (isReadOnly2)
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    else
			extColumnModel
				.setEditor("new AWS_FORM.XMLDictionaryField("
					+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("增强地址簿")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, formModel);
		    XMLDictionary field = new XMLDictionary();
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    field.setNameSpace(metaDataModel.getEntityName());
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    if (metaDataMapModel.getDisplaySetting() == null
			    || metaDataMapModel.getDisplaySetting().trim()
				    .length() == 0) {
			field.setOnTriggerClick("ext_object:function(){openMailTreeGrid(frmMain,'"
				+ metaDataMapModel.getFieldName()
				+ "','"
				+ metaDataModel.getEntityName()
				+ "');return false;}");
		    } else {
			AddressUIFlexUtil util = AddressUIFlexUtil
				.getInstance();
			AddressUIFlexModel addressUIFlexModel = util
				.getAddressUIFlexModel(
					metaDataMapModel.getUUID(), me);
			UtilString utilString = new UtilString(
				metaDataMapModel.getDisplaySetting());
			String displaySql = utilString.matchValue(
				"<targetField>", "</targetField>");
			field.setOnTriggerClick("ext_object:function(){openFlexAddressTree2Grid(frmMain,'"
				+ displaySql
				+ "','"
				+ metaDataMapModel.getFieldName()
				+ "','"
				+ metaDataModel.getEntityName()
				+ "','"
				+ metaDataMapModel.getUUID()
				+ "','"
				+ addressUIFlexModel.getSelector()
				+ "','"
				+ "OPTION"
				+ "','"
				+ addressUIFlexModel.getDelimiter()
				+ "');return false;}");
		    }
		    if (isReadOnly2) {
			extColumnModel.setRead(false);
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else {
			extColumnModel
				.setEditor("new AWS_FORM.XMLDictionaryField("
					+ field + ")");
		    }
		} else if (metaDataMapModel.getDisplayType().equals("平板分类选择")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, formModel);
		    XMLDictionary field = new XMLDictionary();
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    field.setNameSpace(metaDataModel.getEntityName());
		    String rowData = metaDataMapModel.getDisplaySetting();

		    String sql1 = "";
		    String sql2 = "";
		    if (rowData.toUpperCase().indexOf("WHERE") > 0) {
			sql1 = rowData.substring(0, rowData.toUpperCase()
				.indexOf("WHERE"));
			sql2 = rowData.substring(rowData.toUpperCase().indexOf(
				"WHERE"));
			rowData = sql1 + this.parent.convertMacrosValue(sql2);
		    }
		    rowData = new UtilString(rowData).replace("'", "’");
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    field.setOnTriggerClick("ext_object:function(){openAWSFlatComboxSelectWinGrid('"
			    + metaDataMapModel.getFieldName()
			    + "','"
			    + metaDataModel.getEntityName()
			    + "','"
			    + rowData
			    + "');return false;}");
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (isReadOnly2)
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    else
			extColumnModel
				.setEditor("new AWS_FORM.XMLDictionaryField("
					+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("部门字典")
			|| metaDataMapModel.getDisplayType().equals("部门字典-2")
			|| metaDataMapModel.getDisplayType().equals("部门字典-3")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, formModel);
		    XMLDictionary field = new XMLDictionary();
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    field.setNameSpace(metaDataModel.getEntityName());
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }

		    field.setOnTriggerClick("ext_object:function(){openDepartmentTree(frmMain,'"
			    + metaDataMapModel.getId()
			    + "','','',true,'"
			    + metaDataModel.getEntityName()
			    + "');return false;}");
		    if (isReadOnly) {
			field.setReadOnly("true");
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else {
			extColumnModel
				.setEditor("new AWS_FORM.XMLDictionaryField("
					+ field + ")");
		    }
		} else if (metaDataMapModel.getDisplayType().equals("附件")) {
		    UtilString util = new UtilString(
			    metaDataMapModel.getDisplaySetting());
		    String kmId = util.matchValue("<knowledgeConfig>",
			    "</knowledgeConfig>");
		    String isRef = util.matchValue("<knowledge>",
			    "</knowledge>");
		    extRecordConstructionModel.setType("string");
		    extColumnModel.setEditor("check");
		    scopCode.append("var AWS_CHECK_")
			    .append(metaDataMapModel.getFieldName())
			    .append("= new Ext.grid.UpFile({\n");
		    scopCode.append("header: '")
			    .append(NavUtil.getLangName("aws-i18n-metadata",
				    me.getLanguage(),
				    metaDataMapModel.getFieldTitle())
				    + (metaDataMapModel.isNotNull() ? "(<img src=../aws_img/colNotNull.gif alt=必填项>)"
					    : "")).append("',\n");
		    scopCode.append("dataIndex: '")
			    .append(metaDataMapModel.getFieldName())
			    .append("',\n");
		    scopCode.append("metaDataMapId:'")
			    .append(metaDataMapModel.getId()).append("',\n");
		    scopCode.append("entityName:'")
			    .append(metaDataModel.getEntityName())
			    .append("',\n");
		    scopCode.append("entityTitle:'")
			    .append(metaDataModel.getEntityTitle())
			    .append("',\n");
		    scopCode.append("tooltip:'")
			    .append(this.parent
				    .convertMacrosValue(metaDataMapModel
					    .getAltText())).append("',\n");
		    scopCode.append("isknowledge:'").append(isRef)
			    .append("',\n");
		    scopCode.append("kmId:'").append(kmId).append("',\n");
		    FormUIComponentFilePropertyInit init = new FormUIComponentFilePropertyInit();
		    FormUIComponentFilePropertyModel model = init
			    .init(metaDataMapModel.getDisplaySetting());
		    boolean flag = true;
		    boolean active = editable(workflowTaskState, workflowModel,
			    workflowStepModel, isJQXT_Modify);
		    if (active) {
			if (isReadOnly || isReadOnly2)
			    flag = false;
		    } else {
			flag = false;
		    }
		    long upSize = FlexUpFile
			    .getSize(model != null ? model._upSize : 0);
		    String upExt = "";
		    if (model != null) {
			upExt = init.getUpFileLimit(model);
		    }
		    scopCode.append("active:'").append(String.valueOf(flag))
			    .append("',\n");
		    scopCode.append("upNum:'")
			    .append(model != null ? model._upNum : 0)
			    .append("',\n");
		    scopCode.append("upSize:'").append(upSize).append("',\n");
		    scopCode.append("upExt:'").append(upExt).append("',\n");
		    scopCode.append("width: ")
			    .append(metaDataMapModel.getDisplayWidth() < 300 ? 300
				    : metaDataMapModel.getDisplayWidth())
			    .append("\n");
		    scopCode.append("})\n");
		} else if (metaDataMapModel.getDisplayType().equals("按钮")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    AWSButton field = new AWSButton();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setAutoHeight("false");
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    if (metaDataMapModel.getDisplaySetting().length() > 4
			    && metaDataMapModel.getDisplaySetting()
				    .toLowerCase().substring(0, 4)
				    .equals("url=")) {
			field.setDisplayType("url");
			String urls = this.parent
				.convertUrlMacrosValue(metaDataMapModel
					.getDisplaySetting().substring(4));
			field.setDisplayValue(urls);
		    } else if (metaDataMapModel.getDisplaySetting().length() > 11
			    && metaDataMapModel.getDisplaySetting()
				    .toLowerCase().substring(0, 11)
				    .equals("javascript=")) {
			field.setDisplayType("javascript");
			String js = this.parent
				.convertMacrosValue(metaDataMapModel
					.getDisplaySetting().substring(11));
			field.setDisplayValue(js);
		    } else if (metaDataMapModel.getDisplaySetting().length() == 0) {
			field.setDisplayType("-");
			field.setDisplayValue("-");
		    } else {
			field.setDisplayType("javabean");
			String rtClass = this.parent
				.convertMacrosValue(metaDataMapModel
					.getDisplaySetting());
			field.setDisplayValue(rtClass);
		    }

		    field.setNameSpace(metaDataModel.getEntityName());
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    if (isReadOnly2) {
			extColumnModel.setRead(false);
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else {
			extColumnModel.setEditor("new AWS_FORM.AWSButtonField("
				+ field + ")");
		    }
		} else if (metaDataMapModel.getDisplayType().equals("URL")) {
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    AWSButton field = new AWSButton();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setAutoHeight("false");
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    field.setDisplayType("URL");
		    String urls = this.parent
			    .convertUrlMacrosValue(metaDataMapModel
				    .getDisplaySetting());
		    field.setDisplayValue(urls);
		    field.setNameSpace(metaDataModel.getEntityName());
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    if (isReadOnly2) {
			extColumnModel.setRead(false);
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else {
			extColumnModel.setEditor("new AWS_FORM.AWSButtonField("
				+ field + ")");
		    }
		} else if (metaDataMapModel.getDisplayType().equals("多行")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    ExtTextField field = new ExtTextField();

		    field.setAutoHeight("false");
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    field.setHeight(Integer.toString(metaDataMapModel
			    .getInputHeight()));
		    extColumnModel.setEditor("new Ext.form.TextArea(" + field
			    + ")");
		} else if (metaDataMapModel.getDisplayType().equals("列表")
			|| metaDataMapModel.getDisplayType().equals("单选按纽组")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    ExtComboBox field = new ExtComboBox();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    field.setTypeAhead("true");
		    field.setMode("local");
		    field.setTriggerAction("all");
		    field.setEmptyText("请选择...");
		    field.setSelectOnFocus("true");
		    ExtSimpleStore store = new ExtSimpleStore();
		    store.setFields("ext_object:['SELECT_VALUE','SELECT_TITLE']");
		    StringBuilder storeData = new StringBuilder();
		    storeData.append("[");
		    storeData.append("['','空'],");

		    String rowData = metaDataMapModel.getDisplaySetting();
		    if (rowData.length() > 2
			    && !rowData.substring(0, 3).toLowerCase()
				    .equals("sql")) {
			UtilString us = new UtilString(rowData);
			Vector v = us.split("|");
			for (int rowInd = 0; rowInd < v.size(); ++rowInd) {
			    String data = (String) v.get(rowInd);
			    data = data.trim();

			    if (data.length() > 0) {
				if (data.substring(0, 1).equals(":"))
				    data = data.substring(1);
				String dataOfValue;
				String dataOfDisplay;
				if (data.indexOf(":") == -1) {
				    dataOfValue = data;
				    dataOfDisplay = data;
				} else {
				    dataOfValue = data.substring(0,
					    data.indexOf(":"));
				    dataOfDisplay = data.substring(data
					    .indexOf(":") + 1);
				}
				storeData.append("['").append(dataOfValue)
					.append("','").append(dataOfDisplay)
					.append("'],");
			    }
			}
			storeData.setLength(storeData.length() - 1);
			storeData.append("]");
		    } else {
			UtilString us = new UtilString(rowData.substring(4));
			Vector v = us.split("|");
			String key = (String) v.get(0);
			String source = v.size() > 2 ? (String) v.get(2) : "";
			String displayField = "";
			String dataField = "";

			if (key.indexOf(":") > -1) {
			    dataField = key.substring(0, key.indexOf(":"));
			    displayField = key.substring(key.indexOf(":") + 1);
			} else {
			    dataField = key;
			    displayField = key;
			}
			displayField = "".equals(displayField) ? dataField
				: displayField;
			String sql = (String) v.get(1);
			sql = this.parent.convertMacrosValue(sql);
			Connection conn = null;
			Statement stmt = null;
			ResultSet rset = null;
			try {
			    conn = UIDBSourceUtil.open(source, me);
			    stmt = conn.createStatement();
			    rset = DBSql.executeQuery(conn, stmt, sql);
			    while (rset.next()) {
				String dataOfValue = rset.getString(dataField);
				String dataOfDisplay = rset
					.getString(displayField);
				if (dataOfValue == null)
				    dataOfValue = "";
				if (dataOfDisplay == null)
				    dataOfDisplay = "";
				storeData.append("['").append(dataOfValue)
					.append("','").append(dataOfDisplay)
					.append("'],");
			    }
			} catch (Exception e) {
			    e.printStackTrace(System.err);
			    storeData.setLength(0);
			    storeData.append("[");
			    storeData.append("[\"\",\""
				    + Html.escape(e.toString()) + "\"],");
			} finally {
			    UIDBSourceUtil.close(source, conn, stmt, rset);
			}
			storeData.setLength(storeData.length() - 1);
			storeData.append("]");
		    }

		    store.setData(storeData.toString());
		    field.setStore("new Ext.data.SimpleStore(" + store + ")");
		    field.setStore(UICascadeUtil.getGridCascadeStore(me
			    .getSessionId(), metaDataModel.getEntityName(),
			    metaDataMapModel, this.parent
				    .convertMacrosValue(metaDataMapModel
					    .getDisplaySetting()),
			    metaDataMapModel.getDisplayType().equals("列表")));
		    field.setDisplayField("SELECT_TITLE");
		    field.setValueField("SELECT_VALUE");
		    if (isReadOnly) {
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else {
			if (metaDataMapModel.getDisplayType().equals("列表")
				&& UICascadeUtil.matchCascadeFields(
					metaDataMapModel.getDisplaySetting())
					.size() > 0) {
			    field.setListeners("ext_object:{focus :function(combo){combo.store.reload();}}");
			}

			extColumnModel.setEditor("new AWS_FORM.ComboBox("
				+ field + ")");

			String showTypeRender = getRenderer(metaDataMapModel,
				colColors, fontColors,
				this.parent.get_formModel());
			extColumnModel.setRenderer(showTypeRender);
		    }
		} else if (metaDataMapModel.getDisplayType().equals("树型数据选择")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    if (isReadOnly) {
			ExtTextField field = new ExtTextField();
			if (isReadOnly) {
			    field.setReadOnly("true");
			}
			if (metaDataMapModel.getFieldLenth().length() > 0) {
			    field.setMaxLength(metaDataMapModel.getFieldLenth());
			    field.setMaxLengthText("最大长度不允许超过"
				    + metaDataMapModel.getFieldLenth() + "个字符!");
			}
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    } else {
			Map config = new HashMap();
			config.put("FN", metaDataMapModel.getFieldName());
			config.put("NS", metaDataModel.getEntityName());
			ComboboxTree ed = new ComboboxTree(me,
				metaDataMapModel.getFieldName(),
				metaDataMapModel.getUUID(), "", this.parent);
			extColumnModel.setEditor(ed.getComboBox(config));
		    }
		} else if (metaDataMapModel.getDisplayType().equals("字段子表")) {
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'sub_sheet';}\n");
		    Hashtable iH = SheetCache
			    .getSubSheetList(formModel.getId());

		    SheetModel iModel = FormUtil.getRefSheet(metaDataMapModel,
			    iH);
		    extRecordConstructionModel.setType("string");
		    extColumnModel.setEditor("check");
		    scopCode.append("var AWS_CHECK_")
			    .append(metaDataMapModel.getFieldName())
			    .append("= new Ext.grid.fieldSubReport({\n");
		    scopCode.append("header: '")
			    .append(NavUtil.getLangName("aws-i18n-metadata",
				    me.getLanguage(),
				    metaDataMapModel.getFieldTitle())
				    + (metaDataMapModel.isNotNull() ? "(<img src=../aws_img/colNotNull.gif alt=必填项>)"
					    : "")).append("',\n");
		    scopCode.append("dataIndex: '")
			    .append(metaDataMapModel.getFieldName())
			    .append("',\n");
		    scopCode.append("fieldSheetId: ")
			    .append(iModel == null ? 0 : iModel.getId())
			    .append(",\n");
		    scopCode.append("sheetId: '").append(sheetModel.getId())
			    .append("',\n");
		    scopCode.append("sheetTitle: '")
			    .append(sheetModel.getTitle()).append("',\n");
		    scopCode.append("fieldName: '")
			    .append(metaDataMapModel.getFieldName())
			    .append("',\n");
		    scopCode.append("isReadOnly:").append(isReadOnly)
			    .append(",\n");
		    scopCode.append("entityName:'")
			    .append(metaDataModel.getEntityName())
			    .append("',\n");
		    scopCode.append("width: ")
			    .append(metaDataMapModel.getDisplayWidth())
			    .append("\n");
		    scopCode.append("})\n");
		} else if (metaDataMapModel.getDisplayType().equals("货币")) {
		    extRecordConstructionModel.setType("float");
		    extColumnModel
			    .setHeader(NavUtil.getLangName("aws-i18n-metadata",
				    me.getLanguage(),
				    metaDataMapModel.getFieldTitle())
				    + (metaDataMapModel.isNotNull() ? "(<img src=../aws_img/colNotNull.gif alt=必填项>)"
					    : ""));
		    extColumnModel
			    .setDataIndex(metaDataMapModel.getFieldName());
		    extColumnModel.setWidth(String.valueOf(metaDataMapModel
			    .getDisplayWidth()));
		    renderer = getRenderer(metaDataMapModel, colColors,
			    fontColors, formModel);
		    extColumnModel.setRenderer(renderer);
		    extColumnModel.setSortable("true");
		    ExtTextField field = new ExtTextField();
		    field.setAutoHeight("false");
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    extColumnModel.setEditor("new AWS_FORM.TextField(" + field
			    + ")");
		    extColumnModel.setRead(true);
		} else if ("滑杆".equals(metaDataMapModel.getDisplayType())) {
		    extRecordConstructionModel.setType("int");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'int';}\n");
		    extColumnModel
			    .setHeader(NavUtil.getLangName("aws-i18n-metadata",
				    me.getLanguage(),
				    metaDataMapModel.getFieldTitle())
				    + (metaDataMapModel.isNotNull() ? "(<img src=../aws_img/colNotNull.gif alt=必填项>)"
					    : ""));
		    extColumnModel
			    .setDataIndex(metaDataMapModel.getFieldName());
		    extColumnModel.setWidth(String.valueOf(metaDataMapModel
			    .getDisplayWidth()));
		    extColumnModel.setSortable("true");

		    SliderUtil su = new SliderUtil(metaDataMapModel);
		    dynamicRes.append(su.getResourcejs());

		    StringBuilder html = new StringBuilder();
		    html.append("new AWS.form.Slider({diyStyle:'"
			    + su.getDiyStyle() + "'");
		    html.append(",maximum:" + su.getMaximum());
		    html.append(",orientation:'" + su.getType() + "'");
		    html.append(",minimum:" + su.getMinimum());
		    html.append(",blockIncrement:" + su.getBlockIncrement());
		    html.append(",unitIncrement:" + su.getUnitIncrement());
		    html.append(",showTip:" + su.isShowTip());

		    if (su.isShowTip()) {
			html.append(",tipText:'" + su.getTipText() + "'");
		    }

		    if (isReadOnly) {
			html.append(",disabled:true");
		    }
		    html.append("})");
		    ExtTextField field = new ExtTextField();

		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    field.setAutoHeight("false");
		    if (isReadOnly2)
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    else {
			extColumnModel.setEditor("ext_object:"
				+ html.toString());
		    }
		} else if (metaDataMapModel.getDisplayType().equals("用户扩展授权")) {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    extColumnModel.setRenderer(renderer);
		    XMLDictionary field = new XMLDictionary();
		    field.setGrid("ext_object:" + metaDataModel.getEntityName()
			    + ".Grid");
		    field.setOnTriggerClick("ext_object:function(){openAcGrid(frmMain,'"
			    + metaDataMapModel.getDisplaySetting().trim()
			    + "','"
			    + metaDataModel.getEntityName()
			    + "');return false;}");
		    field.setNameSpace(metaDataModel.getEntityName());
		    extColumnModel.setEditor("1");
		    if (isReadOnly2)
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    else
			extColumnModel
				.setEditor("new AWS_FORM.XMLDictionaryField("
					+ field + ")");
		} else if (metaDataMapModel.getDisplayType().equals("多选列表")) {
		    if (!isReadOnly) {
			JSONObject jo = JSONObject.fromObject(metaDataMapModel
				.getDisplaySetting());
			jo.put("sqlSelect", this.parent.convertMacrosValue(jo
				.getString("sqlSelect")));
			jo.put("sid", me.getSessionId());
			jo.put("uuid", metaDataMapModel.getUUID());
			jo.put("GRIDOBJ", metaDataModel.getEntityName());
			extColumnModel.setEditor("new Ext.form.AWSMulList("
				+ jo.toString() + ")");
		    }
		} else if (metaDataMapModel.getDisplayType().equals("单行")) {
		    String displaySql = metaDataMapModel.getDisplaySetting();
		    if (displaySql == null || displaySql.trim().length() == 0) {
			displaySql = "1";
		    }
		    ExtTextField field = new ExtTextField();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    UtilString util = new UtilString(displaySql);
		    Object[] displaySqlArray = util.split("|").toArray();
		    String type = displaySqlArray[0].toString();
		    if (type.equals("0")) {
			extRecordConstructionModel.setType("string");
			columnsDateTypeJS.append("if(ind=='"
				+ metaDataMapModel.getFieldName()
				+ "'){return 'string';}\n");
			ExtComboBox combox = new ExtComboBox();
			if (isReadOnly) {
			    field.setReadOnly("true");
			}
			if (metaDataMapModel.getFieldLenth().length() > 0) {
			    field.setMaxLength(metaDataMapModel.getFieldLenth());
			    field.setMaxLengthText("最大长度不允许超过"
				    + metaDataMapModel.getFieldLenth() + "个字符!");
			}
			String displayFieldText = displaySqlArray.length > 1 ? displaySqlArray[1]
				.toString() : "";
			displayFieldText = this.parent
				.convertMacrosValue(displayFieldText);
			String sqlText = displaySqlArray.length > 2 ? displaySqlArray[2]
				.toString() : "";
			sqlText = this.parent.convertMacrosValue(sqlText);
			int recordNum = displaySqlArray.length > 3 ? Integer
				.parseInt(displaySqlArray[3].toString()) : 20;
			String source = displaySqlArray.length > 4 ? displaySqlArray[4]
				.toString() : "";
			StringBuilder store = new StringBuilder();
			store.append("proxy : new Ext.data.HttpProxy({method : 'POST',url : \"./login.wf\"}),");
			store.append("reader : new Ext.data.JsonReader({root : 'rows',totalProperty : 'totalCount'}, ['value']),");
			store.append("baseParams : {")
				.append("sid :'")
				.append(me.getSessionId())
				.append("',")
				.append("cmd : 'WorkFlow_Execute_Worklist_BindReport_Search_By_Sql',")
				.append("recordNum :'").append(recordNum)
				.append("',").append("filedName :'")
				.append(displayFieldText).append("',")
				.append("sql :\"").append(sqlText)
				.append("\",").append("source :\"")
				.append(source).append("\",")
				.append("query :''");
			store.append("}");

			combox.setStore("ext_object:new Ext.data.Store({"
				+ store.toString() + "})");
			combox.setSelectedClass("");
			combox.setHideTrigger("true");
			combox.setMinChars("1");
			combox.setTpl("new Ext.XTemplate('<table border=\"0\"  cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" class=\"x-combo-list-talbe-ist\">','<tpl for=\".\">','<tr class=\"x-combo-list-item search-item\"><td width=\"50%\" >{value}</td></tr>','</tpl>', '</table>')");
			combox.setMode("remote");
			combox.setTriggerAction("all");
			combox.setWidth("300");
			combox.setLoadingText("Searching...");
			StringBuilder select = new StringBuilder();
			select.append("function(record, index) {")
				.append("var params=record.get(\"value\");")
				.append("var stroeRowIndex=")
				.append(metaDataModel.getEntityName())
				.append(".Grid.getCurrentRowInd();")
				.append(metaDataModel.getEntityName())
				.append(".Grid.getGridPanel().stopEditing();")
				.append(metaDataModel.getEntityName())
				.append(".Grid.getDataSource().getAt(stroeRowIndex).set('")
				.append(metaDataMapModel.getFieldName())
				.append("',params); ")
				.append("this.collapse();}");
			combox.setOnSelect("ext_object:" + select.toString());
			if (isReadOnly)
			    extColumnModel.setEditor("new AWS_FORM.TextField("
				    + field + ")");
			else
			    extColumnModel.setEditor("new AWS_FORM.ComboBox("
				    + combox + ")");
		    } else {
			extColumnModel.setEditor("new AWS_FORM.TextField("
				+ field + ")");
		    }
		} else {
		    extRecordConstructionModel.setType("string");
		    columnsDateTypeJS.append("if(ind=='"
			    + metaDataMapModel.getFieldName()
			    + "'){return 'string';}\n");
		    ExtTextField field = new ExtTextField();
		    if (isReadOnly) {
			field.setReadOnly("true");
		    }
		    if (metaDataMapModel.getFieldLenth().length() > 0) {
			field.setMaxLength(metaDataMapModel.getFieldLenth());
			field.setMaxLengthText("最大长度不允许超过"
				+ metaDataMapModel.getFieldLenth() + "个字符!");
		    }
		    extColumnModel.setEditor("new AWS_FORM.TextField(" + field
			    + ")");
		}

		if (!isReadOnly) {
		    extColumnModel
			    .setTooltip(metaDataMapModel.getAltText() == null ? ""
				    : this.parent
					    .convertMacrosValue(metaDataMapModel
						    .getAltText()));
		}

		if (!"".equals(metaDataMapModel.getValidateType())) {
		    extColumnModel.setVErr(Integer.toString(metaDataMapModel
			    .getValidateErr()));
		    extColumnModel.setVType(metaDataMapModel.getValidateType());
		    if ("re_rule".equals(metaDataMapModel.getValidateType()))
			extColumnModel.setVRule("ext_object:"
				+ this.parent
					.convertMacrosValue(new UtilString(
						metaDataMapModel
							.getValidateRule())
						.replace("+", "\\+")));
		    else {
			extColumnModel.setVRule(this.parent
				.convertMacrosValue(new UtilString(
					metaDataMapModel.getValidateRule())
					.replace("+", "\\+")));
		    }

		    extColumnModel.setVTip(this.parent.convertMacrosValue(
			    metaDataMapModel.getValidateTip()).replaceAll(
			    "\\{title\\}", metaDataMapModel.getFieldTitle()));
		}

		extRecordConstructionList.put(metaDataMapModel.getFieldName(),
			extRecordConstructionModel);
		extExtColumnList.put(new Integer(extExtColumnList.size()),
			extColumnModel);
		++columnCount;
		tableWidth += metaDataMapModel.getDisplayWidth();
	    }
	}

	gridReady.setDynamicRes(dynamicRes);
	gridReady.setRecordModel(getRecordModel(extRecordConstructionList)
		.toString());
	gridReady.setColumnModel(getColumnModel(extExtColumnList).toString());

	gridReady.setStore(getStore(me, processInstanceModel, pageNow,
		workflowTaskState, sheetModel, groupingField, sortInfo,
		extRecordConstructionList, taskId).toString());

	width.indexOf("%");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.addListener('afteredit',handleGridAfterEdit);\n");
	buildGridPrivateCode
		.append("function handleGridAfterEdit(eventObj){\n");
	buildGridPrivateCode.append("if(typeof("
		+ metaDataModel.getEntityName() + "_afteredit)!='undefined')"
		+ metaDataModel.getEntityName()
		+ "_afteredit(AWS_GRID_DS,AWS_GRID_PANEL,eventObj);\n");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.getView().renderRows(currentRowInd);\n");

	buildGridPrivateCode.append("}\n");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.addListener('celldblclick',handleGridCellDBClick);\n");
	buildGridPrivateCode
		.append("function handleGridCellDBClick(g,r,c,eventObj){\n");
	buildGridPrivateCode.append("if(typeof("
		+ metaDataModel.getEntityName()
		+ "_celldblclick)!='undefined')"
		+ metaDataModel.getEntityName()
		+ "_celldblclick(g,r,c,AWS_GRID_DS,eventObj);\n");
	buildGridPrivateCode.append("}\n");

	buildGridPrivateCode
		.append("window.onresize = function(){\n setTimeout(function(){for(var i=0;i<containerIds.length;i++){\n var c;try{ c=containerIds[i].substring(containerIds[i].indexOf('AWS_GRID_')+9,containerIds[i].length);}catch(e){} \n   var m=AWS_GRID_PANEL; if(c){  m=eval(c).Grid.getGridPanel(); } m.setWidth(document.body.clientWidth-50);} },100); };");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.getView().addListener('rowsinserted',handleGridViewRowsInserted);\n");
	buildGridPrivateCode
		.append("function handleGridViewRowsInserted(view, firstRow, lastRow){\n");
	buildGridPrivateCode.append("if(typeof("
		+ metaDataModel.getEntityName()
		+ "_rowsinserted)!='undefined')"
		+ metaDataModel.getEntityName()
		+ "_rowsinserted(view, firstRow, lastRow, AWS_GRID_DS);\n");
	buildGridPrivateCode.append("}\n");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.getView().addListener('rowremoved',handleGridViewRowRemoved);\n");
	buildGridPrivateCode
		.append("function handleGridViewRowRemoved(view, rowIndex, record){\n");
	buildGridPrivateCode.append("if(typeof("
		+ metaDataModel.getEntityName() + "_rowremoved)!='undefined')"
		+ metaDataModel.getEntityName()
		+ "_rowremoved(view, rowIndex, record, AWS_GRID_DS);\n");
	buildGridPrivateCode.append("}\n");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.getView().addListener('refresh',handleGridViewRefresh);\n");
	buildGridPrivateCode.append("function handleGridViewRefresh(view){\n");
	buildGridPrivateCode.append("if(typeof("
		+ metaDataModel.getEntityName() + "_refresh)!='undefined')"
		+ metaDataModel.getEntityName()
		+ "_refresh(view, AWS_GRID_PANEL, AWS_GRID_DS);\n");
	buildGridPrivateCode.append("}\n");

	buildGridPrivateCode.append("function getFilterData(fielter, flag){\n");

	buildGridPrivateCode.append("try{\n");
	buildGridPrivateCode.append(" var data = fielter.split('|');\n");
	buildGridPrivateCode
		.append(" if(data==null || data==''){return '';}\n");
	buildGridPrivateCode.append(" var rowcss=data[0].substring(7);\n");
	buildGridPrivateCode.append(" var readonly=data[1].substring(9);\n");
	buildGridPrivateCode.append(" var isdelete=data[2].substring(9);\n");
	buildGridPrivateCode.append(" if(flag=='rowcss'){\n");
	buildGridPrivateCode.append("  return rowcss;\n");
	buildGridPrivateCode.append(" } else if(flag=='readonly'){\n");
	buildGridPrivateCode.append("  return readonly;\n");
	buildGridPrivateCode.append(" } else if(flag=='isdelete'){\n");
	buildGridPrivateCode.append("  return isdelete;\n");
	buildGridPrivateCode.append(" }\n");
	buildGridPrivateCode.append("}catch(e){}\n");
	buildGridPrivateCode.append("}\n\n");

	buildGridPrivateCode
		.append("AWS_GRID_PANEL.addListener('beforeedit',handleGridBeforeEdit);\n");
	buildGridPrivateCode.append("function handleGridBeforeEdit(e){\n");
	buildGridPrivateCode
		.append(" var fielter = AWS_GRID_DS.getAt(currentRowInd).get('AWS_SHEET_FIELTER');\n");
	buildGridPrivateCode
		.append(" var readonly = getFilterData(fielter, 'readonly');\n");
	buildGridPrivateCode.append(" if(readonly=='1'){\n");
	buildGridPrivateCode.append("  e.cancel = true;\n");
	buildGridPrivateCode.append(" } else {\n");
	buildGridPrivateCode.append("  e.cancel = false;\n");
	buildGridPrivateCode.append(" }\n");
	buildGridPrivateCode.append("}\n\n");

	buildGridPrivateCode
		.append("AWS_GRID_CHECK.addListener('beforerowselect', handleGridBeforeRowSelect);\n");
	buildGridPrivateCode
		.append("function handleGridBeforeRowSelect(selectionModel, rowIndex, keepExisting, record){\n");
	buildGridPrivateCode
		.append(" var fielter=record.get('AWS_SHEET_FIELTER');\n");
	buildGridPrivateCode
		.append(" var isdelete = getFilterData(fielter, 'isdelete');\n");
	buildGridPrivateCode.append(" if(isdelete=='0'){\n");
	buildGridPrivateCode.append("  return false;\n");
	buildGridPrivateCode.append(" }else{\n");
	buildGridPrivateCode.append("  return true;\n");
	buildGridPrivateCode.append(" }\n");
	buildGridPrivateCode.append("}\n");

	buildGridPrivateCode.append(getDifferentListener(sheetModel));

	ExtGridPanelModel extGridPanelModel = getExtGridPanelModel(sheetModel);
	boolean editable = editable(workflowTaskState, workflowModel,
		workflowStepModel, isJQXT_Modify);
	String gridPanelType = getGridPanelType(editable);
	if (editable) {
	    extGridPanelModel.setClicksToEdit("1");
	}

	StringBuilder gridPanel = new StringBuilder("new ").append(
		gridPanelType).append("(");
	extGridPanelModel.setStore("AWS_GRID_DS");
	extGridPanelModel.setCm("AWS_GRID_CM");
	extGridPanelModel.setSm("AWS_GRID_CHECK");
	if (groupingField.length() > 0) {
	    extGridPanelModel
		    .setView("ext_object:new Ext.grid.GroupingView({\nforceFit:true,\ngroupTextTpl: '{text}({[values.rs.length]}行)'\n})\n");
	}
	extGridPanelModel.setRenderTo("AWS_GRID_"
		+ metaDataModel.getEntityName());
	if (width.indexOf("%") > -1)
	    extGridPanelModel.setWidth("ext_object:Ext.get('AWS_GRID_"
		    + metaDataModel.getEntityName() + "').getWidth()");
	else {
	    extGridPanelModel.setWidth(width);
	}
	extGridPanelModel.setHeight(Integer.toString(height));
	if (!FormUtil.isFieldSubSheet(metaDataModel)) {
	    extGridPanelModel.setTitle("<I18N#" + sheetModel.getTitle() + ">");
	}

	extGridPanelModel
		.setViewConfig("{columnsText:'<I18N#显示的列>',scrollOffset:30,sortAscText:'<I18N#升序>',sortDescText:'<I18N#降序>'}");
	extGridPanelModel.loadMask = "{msg:\"<I18N#正在加载数据>...\"}";
	StringBuilder plugins = getPlugins(extExtColumnList);
	if (plugins.length() > 0)
	    extGridPanelModel
		    .setPlugins("ext_object:[new AWSGridAltText({f:getFilterData}),new Ext.grid.plugins.AutoResize(),"
			    + plugins.substring(1));
	else {
	    extGridPanelModel
		    .setPlugins("ext_object:[new AWSGridAltText({f:getFilterData}),new Ext.grid.plugins.AutoResize()]");
	}

	extGridPanelModel.setIconCls("icon-grid");
	StringBuilder toolbar = new StringBuilder("[");

	String lang = new UtilString(me.getSessionId()).matchValue("L{", "}L");
	lang = lang == null || lang.trim().length() == 0 ? "cn" : lang;

	boolean isEdit = isJQXT_Modify
		|| (workflowStepModel._reportIsAdd
			|| workflowStepModel._reportIsModify || workflowStepModel._reportIsRemove)
		&& (workflowTaskState == 1 || workflowTaskState == 0
			&& workflowModel._workFlowType == 1
			|| workflowTaskState == 3 || workflowTaskState == 1);
	if (isEdit) {

	    ExtToolbarModel toolbarButton;
	    if (workflowStepModel._reportIsAdd) {
		toolbarButton = new ExtToolbarModel();

		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.efrom.sheet.create"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"追加一行新记录"));
		toolbarButton.setIconCls("add");
		toolbarButton.setHandler("ext_object:addRow");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append(getAddRow(isSaveMasterBo, me,
			processInstanceModel, sheetModel, taskId));
	    }

	    if (workflowStepModel._reportIsAdd
		    && workflowTaskState != 2
		    && workflowTaskState != 8
		    && workflowTaskState != 2
		    && WorkFlowStepImpExpCache.check(workflowStepModel._id,
			    sheetModel.getId(), "Copy", "Record")) {
		toolbarButton = new ExtToolbarModel();

		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.efrom.sheet.copy"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"复制子表记录"));
		toolbarButton.setIconCls("copy");
		toolbarButton.setHandler("ext_object:copyRow");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append("function copyRow(){\n");
		addRefCheck(buildGridPrivateCode, sheetModel);
		buildGridPrivateCode
			.append("ajaxRecordCopy(AWS_GRID_PANEL, AWS_GRID_DS, AWS_GRID_DS_PLANT);\n");
		buildGridPrivateCode.append("}\n");
	    }
	    if (sheetModel.getXmlFile() != null
		    && !sheetModel.getXmlFile().equals("")
		    && workflowStepModel._reportIsAdd
		    && (workflowTaskState == 1 || workflowTaskState == 0
			    && workflowModel._workFlowType == 1
			    || workflowTaskState == 3 || workflowTaskState == 1)) {
		toolbarButton = new ExtToolbarModel();
		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.efrom.sheet.bydict"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"参考数据字典批量追加记录"));
		toolbarButton.setIconCls("addRef");
		toolbarButton.setHandler("ext_object:addRef");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append("function addRef(){\n");
		addRefCheck(buildGridPrivateCode, sheetModel);
		buildGridPrivateCode.append("openDictionary3(frmMain,'"
			+ GridCheckDictionary.class.getName() + "','"
			+ sheetModel.getXmlFile() + "|子表录入|"
			+ workflowModel._id + "|" + workflowStepModel._id
			+ "');\n");
		buildGridPrivateCode.append("}\n");
	    }

	    if (workflowStepModel._reportIsRemove) {
		toolbarButton = new ExtToolbarModel();
		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.worklist.process.remove"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"删除选择的行"));
		toolbarButton.setIconCls("remove");
		toolbarButton.setHandler("ext_object:RemoveGrid");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append(getRemoveGrid(me,
			processInstanceModel, workflowTaskState, taskId,
			pageNow, sheetModel));
	    }

	    toolbar.append(getSpecialToolBar(workflowStepModel));

	    toolbar.append("'-',");
	    if (workflowStepModel._reportIsAdd
		    || workflowStepModel._reportIsModify) {
		toolbarButton = new ExtToolbarModel();
		toolbarButton.setText(I18nRes
			.findValue(lang, "aws.common.save"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"保存追加或修改的数据"));
		toolbarButton.setIconCls("save");
		toolbarButton.setHandler("ext_object:saveGrid");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append("function saveGrid(){\n");
		addRefCheck(buildGridPrivateCode, sheetModel);
		buildGridPrivateCode
			.append("var m = AWS_GRID_DS.modified.slice(0);  \n");
		buildGridPrivateCode.append("if(m.length > 0){ \n");
		buildGridPrivateCode
			.append("  Ext.MessageBox.confirm('<I18N#提示框>', '<I18N#您确定您要保存修改过的数据吗？>' , doSaveGrid);\n");
		buildGridPrivateCode.append("}else{\n");
		buildGridPrivateCode
			.append("  var box = Ext.MessageBox.alert('<I18N#提示框>', '<I18N#对不起，您没有增加或修改记录>'); \n");
		buildGridPrivateCode
			.append("  setTimeout(function(){box.hide();},1000);\n");
		buildGridPrivateCode.append("}}\n");
		buildGridPrivateCode.append("function doSaveGrid(btn){\n");
		buildGridPrivateCode.append("  if(btn == 'yes'){\n");
		buildGridPrivateCode.append("    outerDoSaveGrid();\n");
		buildGridPrivateCode.append("  }//end if\n");
		buildGridPrivateCode.append("}\n");

		scopCode.append("function gridCheck(){\n");
		scopCode.append("var m = AWS_GRID_DS.getModifiedRecords();\n");
		scopCode.append("for(var i = 0, len = m.length; i < len; i++) {\n");
		scopCode.append(" var rec=m[i];\n");
		scopCode.append(" for(var colIndex=0;colIndex<AWS_GRID_CM.getColumnCount();colIndex++){\n");
		scopCode.append("   var fieldName=AWS_GRID_CM.getDataIndex(colIndex);\n");
		scopCode.append("   if(fieldName!=''){\n");
		scopCode.append("     var cellValue=rec.get(fieldName);\n");
		scopCode.append("     if(getColumnDateType(fieldName)=='date'){\n");
		scopCode.append("       var v= new Date(cellValue).format('Y-m-d');\n");
		scopCode.append("      if(v=='NaN-NaN-NaN')cellValue= cellValue;else cellValue= v;\n");
		scopCode.append("     }\n");
		scopCode.append("     if(getColumnDateType(fieldName)=='dateTime'){\n");
		scopCode.append("       var v= new Date(cellValue).format('Y-m-d H:i');\n");
		scopCode.append("      if(v=='NaN-NaN-NaN NaN:NaN')cellValue= cellValue;else cellValue= v;\n");
		scopCode.append("     }\n");
		scopCode.append("     if(getColumnDateType(fieldName)=='sub_sheet'){\n");

		scopCode.append("      cellValue= '';\n");
		scopCode.append("     }\n");
		scopCode.append("     if(!validataCellValue(fieldName,cellValue,rec,AWS_GRID_CM,colIndex)){AWS_GRID_PANEL.el.unmask(true);return false;}");
		scopCode.append("   }\n");
		scopCode.append(" }\n");
		scopCode.append("}//end for modify record\n");
		scopCode.append("return true;\n");
		scopCode.append("}\n");

		scopCode.append("function validataCellValue(f,v,r,cm,c){\n");
		scopCode.append(validataCellValueJS);
		scopCode.append("if(!awsRuleGridCheck(v,r,cm,c)){return false};\n");
		scopCode.append("return true;\n");
		scopCode.append("}\n");

		scopCode.append("function outerDoSaveGrid(){\n");
		scopCode.append("AWS_GRID_PANEL.el.mask('<I18N#正在提交数据...>');\n");

		scopCode.append("var m = AWS_GRID_DS.getRange();\n");

		scopCode.append("var bindDataStr='_AWSSHEETMODIFYCOUNT{'+m.length+'}AWSSHEETMODIFYCOUNT_ ';\n");
		scopCode.append("for(var i = 0, len = m.length; i < len; i++) {\n");
		scopCode.append(" var rec=m[i];\n");
		scopCode.append(" bindDataStr=bindDataStr+'_AWSSHEETMODIFYRECORD'+i+'{';\n");
		scopCode.append(" for(var colIndex=0;colIndex<AWS_GRID_CM.getColumnCount();colIndex++){\n");
		scopCode.append("   var fieldName=AWS_GRID_CM.getDataIndex(colIndex);\n");
		scopCode.append("   if(fieldName!=''){\n");
		scopCode.append("     var cellValue=rec.get(fieldName);\n");
		scopCode.append("     if(getColumnDateType(fieldName)=='date'){\n");
		scopCode.append("       var v= new Date(cellValue).format('Y-m-d');\n");
		scopCode.append("      if(v=='NaN-NaN-NaN')cellValue= cellValue;else cellValue= v;\n");
		scopCode.append("     }\n");
		scopCode.append("     if(getColumnDateType(fieldName)=='dateTime'){\n");
		scopCode.append("       var v= new Date(cellValue).format('Y-m-d H:i:m');\n");
		scopCode.append("      if(v=='NaN-NaN-NaN NaN:NaN:NaN')cellValue= cellValue;else cellValue= v;\n");
		scopCode.append("     }\n");

		scopCode.append("     if(!validataCellValue(fieldName,cellValue,rec,AWS_GRID_CM,colIndex)){AWS_GRID_PANEL.el.unmask(true);return false;}");
		scopCode.append("     bindDataStr=bindDataStr+'_'+fieldName+'{'+cellValue+'}'+fieldName+'_ ';\n");
		scopCode.append("   }\n");
		scopCode.append(" }\n");
		scopCode.append(" bindDataStr=bindDataStr+'}AWSSHEETMODIFYRECORD'+i+'_ ';\n");
		scopCode.append("}//end for modify record\n");

		scopCode.append("Ext.Ajax.request({\n");
		scopCode.append(" url: '../ajax',\n");
		scopCode.append(" method: 'POST',\n");
		scopCode.append(" params: {\n");
		scopCode.append("  bindDataStr : bindDataStr,\n");
		scopCode.append("  sid : '").append(me.getSessionId())
			.append("',\n");
		scopCode.append("  cmd : 'WorkFlow_Execute_Worklist_BindReport_AjaxSheet_Save',\n");
		scopCode.append("  id : '")
			.append(processInstanceModel.getId()).append("',\n");
		scopCode.append("  openstate : '").append(workflowTaskState)
			.append("',\n");
		scopCode.append("  taskId : '").append(taskId).append("',\n");
		scopCode.append("  pagenow : '").append(pageNow).append("',\n");
		scopCode.append("  subSheetId : '").append(sheetModel.getId())
			.append("'\n");
		scopCode.append(" },//end params\n");
		scopCode.append(" failure:function(response,options){\n");
		scopCode.append("  AWS_GRID_PANEL.el.unmask(true);\n");
		scopCode.append("  var box = Ext.MessageBox.alert('"
			+ I18nRes.findValue(lang, "警告框")
			+ "',response.responseText);\n");
		scopCode.append("  setTimeout(function(){box.hide();},1000);\n");
		scopCode.append(" },//end failure block\n");
		scopCode.append(" success:function(response,options){\n");
		scopCode.append("  AWS_GRID_PANEL.el.unmask(true);\n");

		scopCode.append("  AWS_GRID_DS.commitChanges();\n");

		scopCode.append(" AWS_GRID_DS.reload();\n");
		scopCode.append(" }//end success block\n");
		scopCode.append("});\n");
		scopCode.append("}\n");
	    }

	    toolbarButton = new ExtToolbarModel();
	    toolbarButton.setText(I18nRes.findValue(lang,
		    "fee70b6d027d87e410037d71471e4a04"));
	    toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
		    "重新提取数据刷新表格"));
	    toolbarButton.setIconCls("refresh");
	    toolbarButton.setHandler("ext_object:RefreshGrid");
	    toolbar.append(toolbarButton).append(",");

	    buildGridPrivateCode.append("function RefreshGrid(){\n");
	    addRefCheck(buildGridPrivateCode, sheetModel);
	    buildGridPrivateCode
		    .append("var m = AWS_GRID_DS.modified.slice(0);\n");
	    buildGridPrivateCode.append("if(m.length > 0){\n");
	    buildGridPrivateCode
		    .append("Ext.MessageBox.confirm('<I18N#提示框>', '<I18N#您已经修改了表格数据刷新将丢失未保存的修改确认要刷新表格数据吗>' , doRefreshGrid);\n");
	    buildGridPrivateCode.append("}else{\n");
	    buildGridPrivateCode.append("AWS_GRID_DS.reload();\n");
	    buildGridPrivateCode
		    .append("var box = Ext.MessageBox.alert('<I18N#提示框>','<I18N#数据重新加载完毕>');\n");
	    buildGridPrivateCode
		    .append("setTimeout(function(){box.hide();},1000);\n");
	    buildGridPrivateCode.append("}}\n");
	    buildGridPrivateCode.append("function doRefreshGrid(btn){\n");
	    buildGridPrivateCode.append("if(btn == 'yes'){\n");
	    buildGridPrivateCode.append("AWS_GRID_DS.reload();\n");
	    buildGridPrivateCode
		    .append("var box = Ext.MessageBox.alert('<I18N#提示框>','<I18N#数据重新加载完毕>');\n");
	    buildGridPrivateCode
		    .append("setTimeout(function(){box.hide();},1000);\n");
	    buildGridPrivateCode.append("}}\n");

	    toolbar.append("'-',");
	    if (WorkFlowStepImpExpCache.check(workflowStepModel._id,
		    sheetModel.getId(), "Export", "Excel")) {
		toolbarButton = new ExtToolbarModel();
		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.efrom.sheet.toexcel"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"将当前表格数据导出到Excel文件"));
		toolbarButton.setIconCls("exportExcel");
		toolbarButton.setHandler("ext_object:exportExcel");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append("function exportExcel(){\n");
		addRefCheck(buildGridPrivateCode, sheetModel);
		buildGridPrivateCode
			.append(" exportSheetData(frmMain,'AWS_DTS_ExportExcel_FormSheetData',")
			.append(sheetModel.getId()).append(");\n");
		buildGridPrivateCode.append("}\n");
	    }
	    if (WorkFlowStepImpExpCache.check(workflowStepModel._id,
		    sheetModel.getId(), "Import", "Excel")) {
		toolbarButton = new ExtToolbarModel();

		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.efrom.sheet.byexcel"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"导入符合当前表格模板的Excel数据"));
		toolbarButton.setIconCls("importExcel");
		toolbarButton.setHandler("ext_object:importExcel");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append("function importExcel(){\n");
		addRefCheck(buildGridPrivateCode, sheetModel);
		buildGridPrivateCode
			.append("uploadSheetData(frmMain,'AWS_DTS_ExportExcel_FormSheeData_Upload',"
				+ sheetModel.getId() + ");\n");
		buildGridPrivateCode.append("}\n");

		toolbarButton = new ExtToolbarModel();
		toolbarButton.setText(I18nRes.findValue(lang,
			"aws.common.efrom.sheet.getexcel"));
		toolbarButton.setTooltip(I18nRes.findValue(me.getLanguage(),
			"自动生成符合当前表格数据格式的Excel模板"));
		toolbarButton.setIconCls("exportExcelModel");
		toolbarButton.setHandler("ext_object:exportExcelModel");
		toolbar.append(toolbarButton).append(",");

		buildGridPrivateCode.append("function exportExcelModel(){\n");
		addRefCheck(buildGridPrivateCode, sheetModel);
		buildGridPrivateCode
			.append(" exportSheetModel(frmMain,'AWS_DTS_ExportExcel_FormSheetModel',")
			.append(sheetModel.getId()).append(");\n");
		buildGridPrivateCode.append("}\n");
	    }

	    toolbar.append("'-',");
	}

	addFilterButton(toolbar, buildGridPrivateCode, sheetModel,
		extExtColumnList, extRecordConstructionList, metaDataModel);
	toolbar.append("]");
	extGridPanelModel.setTbar(toolbar.toString());
	gridPanel.append(extGridPanelModel).append(")");
	gridReady.setGridPanel(gridPanel.toString());

	scopCode.append("function getColumnDateType(ind){\n");
	scopCode.append(columnsDateTypeJS);
	scopCode.append("}");

	StringBuilder addGridListener = new StringBuilder();
	gridReady.setAddGridListener(addGridListener.toString());

	gridReady.setSetupDataSourcePrivateCode(setupDataSourcePrivateCode
		.toString());
	gridReady.setBuildGridPrivateCode(buildGridPrivateCode.toString());
	gridReady.setScopCode(scopCode.toString());
	try {
	    buildScrip = gridReady.build();
	} catch (ExtGridException ege) {
	    buildScrip = "警告：" + ege.toString();
	} catch (Exception e) {
	    e.printStackTrace();
	    buildScrip = "异常：" + e.getStackTrace();
	}

	return buildScrip;
    }

    private void addFilterButton(StringBuilder toolbar,
	    StringBuilder buildGridPrivateCode, SheetModel sheetModel,
	    Map extExtColumnList, Map extRecordConstructionList,
	    MetaDataModel metaDataModel) {
	List arrayList = new ArrayList();
	String[] ignore = { "AWS_SHEET_FIELTER", "ID", "PARENTSUBID", "RID",
		"ISLEAF", "RPARENTID" };
	for (int i = 0; i < extExtColumnList.size(); ++i) {
	    ExtColumnModel extColumnModel = (ExtColumnModel) extExtColumnList
		    .get(Integer.valueOf(i));
	    String fn = extColumnModel.getDataIndex();
	    ExtRecordConstructionModel extRecordConstructionModel = (ExtRecordConstructionModel) extRecordConstructionList
		    .get(fn);

	    boolean ig = false;
	    for (int j = 0; j < ignore.length; ++j) {
		if (ignore[j].equals(fn)) {
		    ig = true;
		}
	    }

	    if (ig) {
		continue;
	    }

	    String[] s = new String[5];
	    String tl = extColumnModel.getHeader();
	    String icon = "(<img src=../aws_img/colNotNull.gif alt=必填项>)";
	    if (tl != null && tl.endsWith(icon)) {
		tl = tl.substring(0, tl.indexOf(icon));
	    }

	    s[0] = tl;
	    s[1] = fn;
	    s[2] = "string";
	    if (extRecordConstructionModel.getType() != null) {
		s[2] = extRecordConstructionModel.getType();
		if (s[2].equals("date")) {
		    MetaDataMapModel metaDataMapModel = (MetaDataMapModel) MetaDataMapCache
			    .getModel(metaDataModel.getId(), s[1]);
		    if (metaDataMapModel.getDisplayType().equals("日期时间")) {
			s[2] = "datetime";
		    }
		}
	    }

	    s[3] = "=";
	    s[4] = "";
	    arrayList.add(s);
	}
	JSONArray data = JSONArray.fromObject(arrayList);
	toolbar.append("new ListFilter({gp:"
		+ metaDataModel.getEntityName()
		+ ".Grid,store:new Ext.data.SimpleStore({fields: ['title', 'name', 'type','op','filter'],data : "
		+ data + "})})");
    }

    private void addRefCheck(StringBuilder sb, SheetModel sheetModel) {
	boolean subRefNew = this.parent.get_businessObjectId() == 0
		&& FormUtil.isFieldSubSheet(sheetModel);
	if (subRefNew)
	    sb.append("alert('" + I18nRes.findValue("请先保存表单数据")
		    + "');return false;");
    }

    private boolean editable(int workflowTaskState,
	    WorkFlowModel workflowModel, WorkFlowStepModel workflowStepModel,
	    boolean isJQXT_Modify) {
	return isJQXT_Modify
		|| (workflowStepModel._reportIsAdd
			|| workflowStepModel._reportIsModify || workflowStepModel._reportIsRemove)
		&& (workflowTaskState == 1 || workflowTaskState == 0
			&& workflowModel._workFlowType == 1
			|| workflowTaskState == 3 || workflowTaskState == 1);
    }

    protected String getGridPanelType(boolean editable) {
	return editable ? "Ext.grid.EditorGridPanel" : "Ext.grid.GridPanel";
    }

    private String getColumnCSS(HashMap colColors) {
	if (colColors == null) {
	    return "";
	}
	if (colColors.size() == 0) {
	    return "";
	}
	StringBuilder css = new StringBuilder("");
	Iterator it = colColors.keySet().iterator();
	String key = "";
	while (it.hasNext()) {
	    key = (String) it.next();
	    css.append(".").append("colCss_").append(key);
	    css.append("{background-color:").append(colColors.get(key))
		    .append(";}\n");
	}
	return css.toString();
    }

    private boolean isGetFormDate(String xmlFile) {
	if (xmlFile.indexOf("|") > -1)
	    xmlFile = (String) new UtilString(xmlFile).split("|").get(0);
	String xml = UtilFile.readAll("dictionary/" + xmlFile);

	return xml != null
		&& (xml.toLowerCase().indexOf("$getform(") > -1 || xml
			.toLowerCase().indexOf("$getgrid(") > -1);
    }

    private String getRenderer(MetaDataMapModel metaDataMapModel,
	    HashMap colColors, HashMap fontColors, FormModel formModel) {
	String fieldName = metaDataMapModel.getFieldName();
	StringBuilder r = new StringBuilder();
	r.append("ext_object:function(value, cell, record, rowIndex, columnIndex, store){ \n");
	r.append("\tvar RV = (function(value, cell, record, rowIndex, columnIndex, store){\n");
	r.append("try{\n");

	if (metaDataMapModel.getDisplayType().equals("列表")
		|| metaDataMapModel.getDisplayType().equals("单选按纽组")) {
	    String rowData = metaDataMapModel.getDisplaySetting();

	    if (rowData.length() > 2
		    && !rowData.substring(0, 3).toLowerCase().equals("sql")) {
		UtilString us = new UtilString(rowData);
		Vector v = us.split("|");
		for (int rowInd = 0; rowInd < v.size(); ++rowInd) {
		    String data = (String) v.get(rowInd);
		    data = data.trim();

		    if (data.length() > 0) {
			if (data.substring(0, 1).equals(":"))
			    data = data.substring(1);
			String dataOfValue;
			String dataOfDisplay;
			if (data.indexOf(":") == -1) {
			    dataOfValue = data;
			    dataOfDisplay = data;
			} else {
			    dataOfValue = data.substring(0, data.indexOf(":"));
			    dataOfDisplay = data
				    .substring(data.indexOf(":") + 1);
			    r.append("if(value =='" + dataOfValue + "'){");
			    r.append("value='" + dataOfDisplay + "';}");
			}
		    }
		}
	    } else {
		UtilString us = new UtilString(rowData.substring(4));
		Vector v = us.split("|");
		String key = (String) v.get(0);
		String source = v.size() > 2 ? (String) v.get(2) : "";
		String displayField = "";
		String dataField = "";

		if (key.indexOf(":") > -1) {
		    dataField = key.substring(0, key.indexOf(":"));
		    displayField = key.substring(key.indexOf(":") + 1);
		} else {
		    dataField = key;
		    displayField = key;
		}
		displayField = "".equals(displayField) ? dataField
			: displayField;
		String sql = (String) v.get(1);
		sql = this.parent.convertMacrosValue(sql);
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
		    conn = UIDBSourceUtil.open(source, this.parent.get_me());
		    stmt = conn.createStatement();
		    rset = DBSql.executeQuery(conn, stmt, sql);
		    while (rset.next()) {
			String dataOfValue = rset.getString(dataField);
			String dataOfDisplay = rset.getString(displayField);
			if (dataOfValue == null)
			    dataOfValue = "";
			if (dataOfDisplay == null)
			    dataOfDisplay = "";
			r.append("if(value =='" + dataOfValue + "'){");
			r.append("value='" + dataOfDisplay + "';}");
		    }
		} catch (Exception e) {
		    e.printStackTrace(System.err);
		} finally {
		    UIDBSourceUtil.close(source, conn, stmt, rset);
		}

	    }

	}

	r.append("var id=record.get('ID');");
	r.append("var fielter=record.get('AWS_SHEET_FIELTER');");
	r.append("var data = fielter.split('|');");
	r.append("var rowcss=data[0].substring(7);");
	r.append("if(rowcss==null||rowcss==''){");

	if (metaDataMapModel.getDisplayType().equals("日期")) {
	    if (colColors.containsKey(fieldName)
		    && fontColors.containsKey(fieldName)) {
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("if(value!=''){");
		r.append("var v= new Date(value).format('Y-m-d');");
		r.append("if(v=='NaN-NaN-NaN'){return value;} else {");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + value + \"</span>\";");
		r.append("}}");
	    } else if (colColors.containsKey(fieldName)) {
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("if(value!=''){var v= new Date(value).format('Y-m-d');if(v=='NaN-NaN-NaN')return value;else return v;}");
	    } else if (fontColors.containsKey(fieldName)) {
		r.append("if(value!=''){");
		r.append("var v= new Date(value).format('Y-m-d');");
		r.append("if(v=='NaN-NaN-NaN'){");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + value + \"</span>\";");
		r.append("}else{");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + v + \"</span>\";");
		r.append("}}else{");
		r.append("return value;");
		r.append("}");
	    } else {
		r.append("if(value!=''){var v= new Date(value).format('Y-m-d');if(v=='NaN-NaN-NaN')return value;else return v;}");
	    }
	} else if (metaDataMapModel.getDisplayType().equals("日期时间")) {
	    if (colColors.containsKey(fieldName)
		    && fontColors.containsKey(fieldName)) {
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("if(value!=''){");
		r.append("var v= new Date(value).format('Y-m-d H:i:s');");
		r.append("if(v=='NaN-NaN-NaN NaN:NaN:NaN'){return value;} else {");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + v + \"</span>\";");
		r.append("}}");
	    } else if (colColors.containsKey(fieldName)) {
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("if(value!=''){var v= new Date(value).format('Y-m-d H:i:s');if(v=='NaN-NaN-NaN NaN:NaN:NaN')return value;else return v;}");
	    } else if (fontColors.containsKey(fieldName)) {
		r.append("if(value!=''){");
		r.append("var v= new Date(value).format('Y-m-d H:i:s');");
		r.append("if(v=='NaN-NaN-NaN NaN:NaN:NaN'){");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + value + \"</span>\";");
		r.append("}else{");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + v + \"</span>\";");
		r.append("}}else{");
		r.append("return value;");
		r.append("}");
	    } else {
		r.append("if(value!=''){var v= new Date(value).format('Y-m-d H:i:m');if(v=='NaN-NaN-NaN NaN:NaN:NaN')return value;else return v;}");
	    }
	} else if (metaDataMapModel.getDisplayType().equals("时间")) {
	    if (colColors.containsKey(fieldName)
		    && fontColors.containsKey(fieldName)) {
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("if(value!=''){");
		r.append("var v= new Date(value).format('H:i:s');");
		r.append("if(v=='NaN:NaN:NaN'){return value;} else {");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + v + \"</span>\";");
		r.append("}}");
	    } else if (colColors.containsKey(fieldName)) {
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("if(value!=''){var v= new Date(value).format('H:i:s');if(v=='NaN:NaN:NaN')return value;else return v;}");
	    } else if (fontColors.containsKey(fieldName)) {
		r.append("if(value!=''){");
		r.append("var v= new Date(value).format('H:i:s');");
		r.append("if(v=='NaN:NaN:NaN'){");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + value + \"</span>\";");
		r.append("}else{");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + v + \"</span>\";");
		r.append("}}else{");
		r.append("return value;");
		r.append("}");
	    } else {
		r.append("if(value!=''){var v= new Date(value).format('H:i:s');if(v=='NaN:NaN:NaN')return value;else return v;}");
	    }
	} else if (metaDataMapModel.getDisplayType().equals("货币")) {
	    if (colColors.containsKey(fieldName)
		    && fontColors.containsKey(fieldName)) {
		String moneyType = metaDataMapModel.getDisplaySetting() == null
			|| metaDataMapModel.getDisplaySetting().trim().length() == 0 ? "￥"
			: metaDataMapModel.getDisplaySetting();
		r.append("var money=number2MoneyOfMoneyGridFormUIComponent('"
			+ moneyType + "',value);");
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + money + \"</span>\";");
	    } else if (colColors.containsKey(fieldName)) {
		String moneyType = metaDataMapModel.getDisplaySetting() == null
			|| metaDataMapModel.getDisplaySetting().trim().length() == 0 ? "￥"
			: metaDataMapModel.getDisplaySetting();
		r.append("var money=number2MoneyOfMoneyGridFormUIComponent('"
			+ moneyType + "',value);");
		r.append("cell.css='colCss_" + fieldName + "';");
		r.append("return money;");
	    } else if (fontColors.containsKey(fieldName)) {
		String moneyType = metaDataMapModel.getDisplaySetting() == null
			|| metaDataMapModel.getDisplaySetting().trim().length() == 0 ? "￥"
			: metaDataMapModel.getDisplaySetting();
		r.append("var money=number2MoneyOfMoneyGridFormUIComponent('"
			+ moneyType + "',value);");
		r.append("return \"<span style='color:"
			+ fontColors.get(fieldName)
			+ ";'>\" + money + \"</span>\";");
	    } else {
		String moneyType = metaDataMapModel.getDisplaySetting() == null
			|| metaDataMapModel.getDisplaySetting().trim().length() == 0 ? "￥"
			: metaDataMapModel.getDisplaySetting();
		r.append("var money=number2MoneyOfMoneyGridFormUIComponent('"
			+ moneyType + "',value);");
		r.append("return money;");
	    }
	} else if (colColors.containsKey(fieldName)
		&& fontColors.containsKey(fieldName)) {
	    r.append("cell.css='colCss_" + fieldName + "';");
	    r.append("return \"<span style='color:" + fontColors.get(fieldName)
		    + ";'>\" + value + \"</span>\";");
	} else if (colColors.containsKey(fieldName)) {
	    r.append("cell.css='colCss_" + fieldName + "';");
	    r.append("return value;");
	} else if (fontColors.containsKey(fieldName)) {
	    r.append("return \"<span style='color:" + fontColors.get(fieldName)
		    + ";'>\" + value + \"</span>\";");
	} else {
	    r.append("return value;");
	}

	r.append("} else {");
	r.append("cell.attr=rowcss;");
	if (metaDataMapModel.getDisplayType().equals("日期")) {
	    r.append("if(value!=''){var v= new Date(value).format('Y-m-d');if(v=='NaN-NaN-NaN')return value;else return v;}");
	} else if (metaDataMapModel.getDisplayType().equals("日期时间")) {
	    r.append("if(value!=''){var v= new Date(value).format('Y-m-d H:i:m');if(v=='NaN-NaN-NaN NaN:NaN:NaN')return value;else return v;}");
	} else if (metaDataMapModel.getDisplayType().equals("货币")) {
	    String moneyType = metaDataMapModel.getDisplaySetting() == null
		    || metaDataMapModel.getDisplaySetting().trim().length() == 0 ? "￥"
		    : metaDataMapModel.getDisplaySetting();
	    r.append("var money=number2MoneyOfMoneyGridFormUIComponent('"
		    + moneyType + "',value);");
	    r.append("return money;");
	} else if (metaDataMapModel.getDisplayType().equals("时间")) {
	    r.append("if(value!=''){var v= new Date(value).format('Y-m-d H:i:m');if(v=='NaN:NaN:NaN')return value;else return v;}");
	} else {
	    r.append("return value;");
	}
	r.append("}");
	if (metaDataMapModel.getDisplayType().equals("日期")) {
	    r.append("}catch(e){if(value!=''){var v= new Date(value).format('Y-m-d');if(v=='NaN-NaN-NaN')return value;else return v;}}");
	} else if (metaDataMapModel.getDisplayType().equals("日期时间")) {
	    r.append("}catch(e){if(value!=''){var v= new Date(value).format('Y-m-d H:i:m');if(v=='NaN-NaN-NaN NaN:NaN:NaN')return value;else return v;}}");
	} else if (metaDataMapModel.getDisplayType().equals("货币")) {
	    String moneyType = metaDataMapModel.getDisplaySetting() == null
		    || metaDataMapModel.getDisplaySetting().trim().length() == 0 ? "￥"
		    : metaDataMapModel.getDisplaySetting();
	    r.append("}catch(e){ var money=number2MoneyOfMoneyGridFormUIComponent('"
		    + moneyType + "',value);return money;}");
	} else if (metaDataMapModel.getDisplayType().equals("时间")) {
	    r.append("}catch(e){if(value!=''){var v= new Date(value).format('H:i:s');if(v=='NaN:NaN:NaN')return value;else return v;}}");
	} else {
	    r.append("}catch(e){return value;}");
	}
	r.append("})(value, cell, record, rowIndex, columnIndex, store);");
	r.append("if (RV=='undefined' || RV==undefined){RV='';}");
	r.append("return Ext.isGecko?\"<div class='ellipsis_for_ff'>\" + RV + \"</div>\":RV;");
	r.append("}");
	return r.toString();
    }

    private String parseJavaScriptOnChangeEvent(String extendHtmlCode) {
	String extEventCode = "";
	if (extendHtmlCode.trim().length() < 15) {
	    return "";
	}
	int firstPosition = 10;
	int endPosition = extendHtmlCode.length() - firstPosition;
	String scopFlag = "";
	if (extendHtmlCode.indexOf("\"") > 9)
	    scopFlag = "\"";
	else if (extendHtmlCode.indexOf("'") > 9) {
	    scopFlag = "'";
	}
	if (scopFlag.length() > 0) {
	    endPosition = extendHtmlCode.substring(10).indexOf(scopFlag);
	}
	String changeCode = extendHtmlCode.substring(firstPosition, endPosition
		+ firstPosition - 1);
	return extEventCode;
    }
}