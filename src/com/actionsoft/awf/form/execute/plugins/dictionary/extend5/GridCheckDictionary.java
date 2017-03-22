package com.actionsoft.awf.form.execute.plugins.dictionary.extend5;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.cache.MetaDataMapCache;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.commons.bigtext.WorkFlowBigText;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryConditionUI;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryObject;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryUtil;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.AdapterData;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.db.DBData;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.DBResult;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.DataResult;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.DictionaryModel;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.FieldModel;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSequence;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.PageIndex;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepBindReportCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.i18n.I18nRes;

public class GridCheckDictionary extends DictionaryObject {

    private DictionaryModel _dictionaryModel = new DictionaryModel();
    private DictionaryModel _dictionaryModel2 = new DictionaryModel();
    private Map _fields = new UnsyncHashtable();
    private Map _condition = new UnsyncHashtable();
    private Map _fields2 = new UnsyncHashtable();
    private boolean isInsert = false;
    private boolean isUpdate = false;
    private boolean isDelete = false;
    private WorkFlowModel _appModel = null;
    private WorkFlowStepModel _stepModel = null;
    private int _formId;
    int _subSheetId;
    private String _idList = "";
    private String _workflowId = "";
    private String _workflowStepId = "";
    private String _instanceId = "";
    private String _taskId = "";
    private String _dbFilter = "";
    private String _rtClass = "";
    private String _pageNow = "";
    private Hashtable _parentSubId = new UnsyncHashtable();
    private boolean _isSubTable = false;

    public GridCheckDictionary(UserContext uct) {
	super(uct);
    }

    private void init(String fileName) {
	this._dictionaryModel = DictionaryUtil.fromFile(fileName);
	this._fields = this._dictionaryModel._fields;
	this._condition = this._dictionaryModel._condition;

	if (this._dictionaryModel._htmlModel != null
		&& this._dictionaryModel._htmlModel.length() > 0)
	    setHtmlModel(this._dictionaryModel._htmlModel);
    }

    private void init2(String fileName) {
	this._dictionaryModel2 = DictionaryUtil.fromFile(fileName);
	this._fields2 = this._dictionaryModel2._fields;

	if (this._dictionaryModel2._htmlModel != null
		&& this._dictionaryModel2._htmlModel.length() > 0)
	    setHtmlModel(this._dictionaryModel2._htmlModel);
    }

    public String DIYDataGrild(String dbFilter) {
	init(getXmlFile());
	StringBuilder html = new StringBuilder();

	int tableWidth = 0;
	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));
	    try {
		tableWidth += Integer.parseInt(model.getWidth());
	    } catch (Exception exception) {
	    }
	}
	html.append("<div id=AWS_DICTIONARY_TABLE name=AWS_DICTIONARY_TABLE style='height:100%;width:100%;background-color:ffffff'><div style='overflow:auto;width:expression(document.body.clientWidth-30);height:expression(document.body.clientHeight-105)'>");
	if (this._condition.size() > 0) {
	    StringBuilder js = new StringBuilder();
	    js.append("function filterCondition(){var tmpCond='~~~~';\n");

	    html.append("<fieldset  style='padding:5px;width:100%;border:1px solid gray; line-height:2.0;'>");
	    html.append("<legend style='padding:0px;background-color:ffffff;color:000000'><I18N#过滤条件></legend>");
	    html.append("<table width=700px align=left border=0 cellspacing=0 cellpadding=0><tr><td>");
	    html.append("<table id='tbCondition' name='tbCondition' width=600px align=left border=0 cellspacing=0 cellpadding=0>");
	    for (int i = 0; i < this._condition.size(); ++i) {
		DictionaryConditionUI condModel = (DictionaryConditionUI) this._condition
			.get(new Integer(i));
		MetaDataMapModel newMetaDataMapModel = new MetaDataMapModel();
		newMetaDataMapModel.setDisplaySetting(condModel.getUiRef());
		newMetaDataMapModel.setDisplayType(condModel.getUiComponent());
		newMetaDataMapModel.setFieldTitle(condModel.getFieldTitle());
		newMetaDataMapModel.setFieldType(condModel.getFieldType());
		newMetaDataMapModel.setFieldName(condModel.getUiName());
		newMetaDataMapModel.setFieldDefault(condModel.getUiDefault());
		String fieldName = condModel.getUiName();

		String value = new UtilString(dbFilter).matchValue("_"
			+ fieldName + "{", "}" + fieldName + "_");
		if (value != null && value.length() > 0) {
		    String tmpValue = value;
		    if (condModel.getCompareType().toLowerCase().equals("like")) {
			tmpValue = "%" + tmpValue + "%";
		    }
		    if (condModel.getFieldType().equals("文本"))
			tmpValue = "'" + tmpValue + "'";
		    else if (condModel.getFieldType().equals("日期")) {
			tmpValue = DBSql.convertShortDate(tmpValue);
		    }
		}
		if (i % 2 == 0) {
		    html.append("<tr>");
		}
		html.append("<td width=20%>")
			.append(I18nRes.findValue(condModel.getFieldTitle()))
			.append("</td>");

		String defaultValue = condModel.getUiDefault();
		if (value.length() > 0) {
		    defaultValue = value;
		}
		defaultValue = new RuntimeFormManager(super.getContext())
			.convertMacrosValue(defaultValue);
		html.append("<td width=30%>")
			.append(getUI(condModel.getUiComponent(), fieldName,
				defaultValue, newMetaDataMapModel,
				this._condition)).append("</td>");

		if (i % 2 == 1) {
		    html.append("</tr>");
		}

		js.append("\n");
		js.append("if(frmMain.").append(fieldName)
			.append(".value!=''){tmpCond=tmpCond+' '+'_")
			.append(fieldName).append("{'+frmMain.")
			.append(fieldName).append(".value+'}")
			.append(fieldName).append("_';}");
		js.append("\n");
	    }
	    html.append("</table></td><td><input type='submit' value='<I18N#执行过滤条件>' class='actionsoftButton' onClick=\"filterCondition();return false;\"  border='0'></td></tr>");
	    html.append("</table></fieldset>");
	    js.append("frmMain.dbFilter.value=tmpCond;refreshMe3();\n};\n");
	    html.append("\n<script>\n");
	    html.append(js);
	    html.append("\n</script>\n");
	}

	html.append("<table width=")
		.append(tableWidth + "px")
		.append(" cellpadding=0 border=1 cellspacing=0 bordercolorlight=#CCCCCC bordercolordark=#FFFFFF>");

	html.append("<tr>");
	html.append("<td class=actionsoftReportTitle width=1%><b><a href='' onclick=\"AutoSelectList(frmMain);return false;\"><font color=#FF0000 ><div align=center>√</div></font></a></b></td>");
	if (this.isUpdate) {
	    html.append("<td class=actionsoftReportTitle width=2%><I18N#修改></td>");
	}

	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));
	    if (!model.getIsHidden().toUpperCase().equals("TRUE")) {
		html.append("<td class=actionsoftReportTitle width=")
			.append(model.getWidth() + "px").append(">")
			.append(I18nRes.findValue(model.getDisplay()))
			.append("</td>");
	    }
	}
	html.append("</tr>");

	int lineCount = 0;

	int currentLine = 0;

	int lineNumber = Integer.parseInt(this._dictionaryModel._line);

	DataResult dataResult = null;
	try {
	    int lineFirst = lineNumber * (getPageNow() - 1);

	    dataResult = this._dictionaryModel.getDataFactory().queryData(this,
		    dbFilter);
	    while (dataResult.next()) {
		++lineCount;
		if (lineNumber == 0 || lineCount > lineFirst) {
		    ++currentLine;
		    if (currentLine <= lineNumber) {
			StringBuilder clickEvent = new StringBuilder();
			StringBuilder tr = new StringBuilder();
			int boId = 0;
			int bindId = 0;

			clickEvent.append("onClick=\"setParameter(").append(
				currentLine);

			for (int i = 0; i < this._fields.size(); ++i) {
			    FieldModel model = (FieldModel) this._fields
				    .get(new Integer(i));
			    Object obj = dataResult.getFiledValue(model);
			    String value = obj == null ? "" : String
				    .valueOf(obj);
			    if (value == null)
				value = "&nbsp;";
			    value = new UtilString(value).replace("\n", "<br>");
			    value = new UtilString(value).replace("__eol__",
				    "<br>");

			    if (value.lastIndexOf("00:00:00.0") > 0) {
				value = new UtilString(value).replace(
					"00:00:00.0", "");
			    }
			    if (value.indexOf(".") == 0) {
				value = "0" + value;
			    }
			    if (!model.getIsHidden().toUpperCase()
				    .equals("TRUE")) {
				tr.append("<td >")
					.append(value == null
						|| "".equals(value) ? "&nbsp;"
						: value).append("</td>");
			    }

			    if (!model.getTargetName().equals("")) {
				value = obj == null ? "" : String.valueOf(obj);
				if (value == null)
				    value = "";
				value = new UtilString(value).replace(
					"__eol__", " ");

				if (value.lastIndexOf("00:00:00.0") > 0) {
				    value = new UtilString(value).replace(
					    "00:00:00.0", "");
				}
				value = value.replace("\\", "\\\\");
				clickEvent.append(",'").append(value)
					.append("'");
			    }
			}
			clickEvent
				.append(");return false;\" onDblClick=\"getParameter();\"");

			String joinTmp = "";
			String idValue = String.valueOf(dataResult
				.getFiledValue("ID"));
			html.append(
				"<tr title='<I18N#单击可以选择或取消>' onClick=\"selectCheck('chk"
					+ idValue
					+ "');\" style='cursor:pointer; ' onmouseout=\"out_change(this,'#FFFFFF');\" onmouseover=\"over_change(this,'#EBF#F6');\"")
				.append(joinTmp).append(">\n");
			html.append(
				"<td >\n<input type='checkbox' onClick=\"selectCheck('chk"
					+ idValue + "')\" id='chk" + idValue
					+ "' name='chk" + idValue + "' value='")
				.append(idValue).append("'>\n</td>");

			joinTmp = tr.toString();
			html.append(joinTmp);
			html.append("</tr>\n");
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    return I18nRes.findValue(getContext().getLanguage(),
		    "执行错误,详细请参考error.log") + "<br>" + e.toString();
	} finally {
	    if (dataResult != null) {
		dataResult.close();
	    }
	}
	html.append("</table></div>");
	if (lineNumber > 0) {
	    html.append(new PageIndex("Dictionary_Public_Open1", getPageNow(),
		    lineCount, lineNumber).toString());
	}
	html.append("</div>");
	return html.toString();
    }

    public String DIYChoiceArea() {
	StringBuilder html = new StringBuilder();
	String nowDbFilter = "";
	if (getDbFilter().indexOf("^^^") != -1) {
	    Vector dbFilterValue = new UnsyncVector();
	    UtilString us = new UtilString(getDbFilter());
	    dbFilterValue = us.split("^^^");
	    nowDbFilter = (String) dbFilterValue.get(1);
	}

	if (!this._dictionaryModel._choiceSql.equals("")) {
	    html.append("<select name=choiceValue class=actionsoftSelect onchange=\"refreshMe2();\">");

	    if (!nowDbFilter.equals("")) {
		html.append("<option value='" + nowDbFilter + "' selected >"
			+ nowDbFilter + "</option>");
	    }
	    html.append("<option value=''><I18N#请选择>"
		    + this._dictionaryModel._choiceName + ".....</option>");
	    try {
		html.append(DictionaryUtil.getChoice(this._dictionaryModel,
			this));
	    } catch (Exception e) {
		e.printStackTrace(System.err);
		return I18nRes.findValue(getContext().getLanguage(),
			"执行错误,详细请参考error.log") + "<br>" + e.toString();
	    }
	    html.append("</select>");
	}
	return html.toString();
    }

    public String DIYJavaScript() {
	StringBuilder js = new StringBuilder();
	js.append("<script>\n function getParameter(){\n");
	int p = 0;
	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));

	    if (!model.getTargetName().equals("")) {
		++p;
		js.append("try{\n ");
		js.append("parent.opener.frmMain.")
			.append(model.getTargetName())
			.append(".value=frmMain.p").append(p)
			.append(".value;\n");
		js.append("}catch(e){}\n");
	    }
	}
	js.append("\n window.close();}\n</script>\n");
	return js.toString();
    }

    public String DIYTitle() {
	return this._dictionaryModel._title;
    }

    private void intValue(String idList, String workflowId,
	    String workflowStepId, String instanceId, String taskId,
	    String dbFilter, String rtClass, String pageNow) {
	this._idList = idList;
	this._workflowId = workflowId;
	this._workflowStepId = workflowStepId;
	this._instanceId = instanceId;
	this._taskId = taskId;
	this._dbFilter = dbFilter;
	this._rtClass = rtClass;
	this._pageNow = pageNow;
    }

    public String insert2Sheet(String idList, String workflowId,
	    String workflowStepId, String xmlName, String instanceId,
	    String taskId, String dbFilter, String rtClass, String pageNow,
	    String ft, String bindValue) {
	this._fields.clear();
	intValue(idList, workflowId, workflowStepId, instanceId, taskId,
		dbFilter, rtClass, pageNow);
	UtilString us2 = new UtilString(xmlName);
	Vector xmlFileType = us2.split("|");
	init((String) xmlFileType.get(0));
	DataResult dataResult = null;
	try {
	    UtilString myStr = new UtilString(idList.trim());
	    Vector myArray = myStr.split(" ");
	    setWebFormData(bindValue);
	    dataResult = this._dictionaryModel.getDataFactory().queryData(this,
		    dbFilter);
	    if (myArray != null) {
		while (dataResult != null && dataResult.next()) {
		    Object idstr = dataResult.getFiledValue("ID").toString();
		    for (int i = 0; i < myArray.size(); ++i) {
			if (myArray.elementAt(i).equals(idstr)) {
			    StringBuilder insertSql = new StringBuilder();
			    int id = 0;
			    try {
				id = new DBSequence()
					.getSequence("USER_WORKFLOWREPORT");

				this._parentSubId.put(myArray.elementAt(i)
					.toString(), Integer.valueOf(id));
			    } catch (Exception e) {
				System.out.println("获得子表ID序列值出错!");
				e.printStackTrace(System.err);
			    }
			    insertSql
				    .append("insert into "
					    + this._dictionaryModel._targetTable
					    + " (ID,ORGNO,BINDID,CREATEDATE,CREATEUSER,UPDATEDATE,UPDATEUSER,WORKFLOWID,WORKFLOWSTEPID,ISEND,");
			    getDbDefaultValueField(insertSql);
			    int j = 0;
			    String valueSql = getInsertSql(workflowId,
				    workflowStepId, instanceId, id, j, myArray,
				    dataResult, i);
			    insertSql.append(valueSql);

			    int insertFlag = DBSql.executeUpdate(insertSql
				    .toString());
			    if (insertFlag == DBSql.SQL_EXECUTE_STATUS_ERROR)
				MessageQueue
					.getInstance()
					.putMessage(
						super.getContext().getUID(),
						"\""
							+ insertSql.toString()
							+ "\"该语句插入失败,详细信息,请查看error.log错误日志文件!");
			}
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    if (dataResult != null) {
		dataResult.close();
	    }
	}
	this._fields.clear();
	if (ft.equals(Integer.toString(2)))
	    return "<html><script>parent.opener.refData(parent.opener.document.frmMain);window.close(); </script></html>";
	if (ft.equals(Integer.toString(3))) {
	    return "<html><script>parent.opener.refData(parent.opener.document.frmMain);window.close(); </script></html>";
	}
	return "<html><script>parent.opener.saveData(parent.opener.document.frmMain,'WorkFlow_Execute_Worklist_BindReport_P_Save');window.close(); </script></html>";
    }

    private String getInsertSql(String workflowId, String workflowStepId,
	    String instanceId, int id, int j, Vector myArray,
	    DataResult dataResult, int o) {
	StringBuilder insertSql = new StringBuilder();
	Map newmap = new HashMap();
	int count = 0;
	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));
	    if (!model.getTargetName().equals("")) {
		if (i == this._fields.size() - 1) {
		    if (model.getNameType().equals("日期")) {
			try {
			    ((DBResult) dataResult).getRs().getTimestamp(
				    model.getName());
			    insertSql.append(model.getTargetName()).append(")");
			    newmap.put(Integer.valueOf(count), model);
			    ++count;
			} catch (SQLException e) {
			    e.printStackTrace();
			}
		    } else {
			newmap.put(Integer.valueOf(count), model);
			insertSql.append(model.getTargetName()).append(")");
			++count;
		    }
		} else {
		    if (model.getNameType().equals("日期")) {
			try {
			    ((DBResult) dataResult).getRs().getTimestamp(
				    model.getName());
			    insertSql.append(model.getTargetName()).append(",");
			    newmap.put(Integer.valueOf(count), model);
			    ++count;
			} catch (SQLException e) {
			    e.printStackTrace();
			}
		    } else {
			insertSql.append(model.getTargetName()).append(",");
			newmap.put(Integer.valueOf(count), model);
			++count;
		    }
		    ++j;
		}
	    }
	}

	insertSql.setLength(insertSql.length() - 1);
	insertSql.append(")");
	insertSql.append(
		" VALUES (" + id + ",'"
			+ super.getContext().getCompanyModel().getId() + "',"
			+ instanceId + "," + DBSql.getDateDefaultValue() + ",'"
			+ super.getContext().getUID() + "',"
			+ DBSql.getDateDefaultValue() + ",'"
			+ super.getContext().getUID() + "',").append(
		workflowId + "," + workflowStepId + ",0,");
	getDbDefaultValueValue(insertSql);
	for (int i = 0; i < newmap.size(); ++i) {
	    FieldModel model = (FieldModel) newmap.get(new Integer(i));
	    if (!model.getTargetName().equals("")) {
		MetaDataMapModel mapModel = (MetaDataMapModel) MetaDataMapCache
			.getModel(this._dictionaryModel._fromTable,
				model.getName());
		MetaDataMapModel targetMapModel = (MetaDataMapModel) MetaDataMapCache
			.getModel(this._dictionaryModel._targetTable,
				model.getTargetName());
		if (i == newmap.size() - 1)
		    insertSql.append(
			    getDbRset(myArray.elementAt(o).toString(),
				    model.getNameType(), model.getName(),
				    ((DBResult) dataResult).getRs(), mapModel,
				    workflowId, instanceId, targetMapModel))
			    .append(")");
		else {
		    insertSql.append(
			    getDbRset(myArray.elementAt(o).toString(),
				    model.getNameType(), model.getName(),
				    ((DBResult) dataResult).getRs(), mapModel,
				    workflowId, instanceId, targetMapModel))
			    .append(",");
		}
	    }
	}
	insertSql.setLength(insertSql.length() - 1);
	insertSql.append(")");
	return insertSql.toString();
    }

    private String getDbRset(Object nowId, String nameType, String fieldName,
	    ResultSet rset, MetaDataMapModel mapModel, String workflowId,
	    String instanceId, MetaDataMapModel targetMapModel) {
	StringBuilder valueSql = new StringBuilder();
	try {
	    if (AWFConfig._databaseServerConf.getSupply().toLowerCase()
		    .equals("oracle")) {
		if (nameType.equals("文本")) {
		    String tempValue = rset.getString(fieldName);
		    if (tempValue == null) {
			tempValue = "";
		    }
		    valueSql.append("'").append(DBSql.convertSign(tempValue))
			    .append("'");
		} else if (nameType.equals("数值")) {
		    if (targetMapModel.getFieldLenth().indexOf(',') != -1) {
			valueSql.append(rset.getDouble(fieldName));
		    } else {
			valueSql.append(rset.getInt(fieldName));
		    }
		} else if (nameType.equals("日期")) {
		    valueSql.append("")
			    .append(DBSql.convertShortDate(UtilDate
				    .dateFormat(rset.getTimestamp(fieldName))))
			    .append("");
		} else if (nameType.equals("备注")) {
		    valueSql.append("'")
			    .append(DBSql.convertSign(rset.getString(fieldName)))
			    .append("'");
		} else if (nameType.equals("附件")) {
		    FormFileUpWeb cp = new FormFileUpWeb(
			    AWFConfig._httpdConf.getServerRoot(),
			    Integer.toString(rset.getInt("ID")),
			    Integer.toString(mapModel.getId()),
			    rset.getString(fieldName));
		    String httpURL = "";
		    if (rset.getString(fieldName) != null
			    && !rset.getString(fieldName).equals("")) {
			httpURL = cp.copyFile();
			valueSql.append(
				"'<a href=/" + httpURL + " target=newWin>")
				.append(rset.getString(fieldName))
				.append("</a>'");
		    } else {
			valueSql.append("''");
		    }
		} else if (nameType.indexOf("HTML") != -1) {
		    WorkFlowBigText bigText = new WorkFlowBigText(
			    rset.getInt("WORKFLOWID"), rset.getInt("BINDID"),
			    fieldName + ".act");
		    UtilString us = new UtilString(bigText.getBigText());
		    String content = us.replace("__eol__", "<br>");
		    content = new UtilString(content).replace("\\", "");
		    us = new UtilString(content);
		    valueSql.append("'").append(content).append("'");
		} else if (nameType.indexOf("xml") != -1) {
		    insertSubSub(nowId, this._idList, this._workflowId,
			    this._workflowStepId, nameType + "|",
			    this._instanceId, this._taskId, this._dbFilter,
			    this._rtClass, this._pageNow);
		    valueSql.append("'").append("").append("'");
		} else {
		    valueSql.append("'").append(rset.getLong(fieldName))
			    .append("'");
		}
	    } else if (AWFConfig._databaseServerConf.getSupply().toLowerCase()
		    .equals("mysql")) {
		if (nameType.equals("文本")) {
		    String tempValue = rset.getString(fieldName);
		    if (tempValue == null) {
			tempValue = "";
		    }
		    tempValue = tempValue.replace("\\", "\\\\");
		    valueSql.append("'").append(DBSql.convertSign(tempValue))
			    .append("'");
		} else if (nameType.equals("数值")) {
		    if (targetMapModel.getFieldLenth().indexOf(',') != -1) {
			valueSql.append(rset.getDouble(fieldName));
		    } else {
			valueSql.append(rset.getInt(fieldName));
		    }
		} else if (nameType.equals("日期")) {
		    valueSql.append("")
			    .append(DBSql.convertShortDate(UtilDate
				    .dateFormat(rset.getTimestamp(fieldName))))
			    .append("");
		} else if (nameType.equals("备注")) {
		    valueSql.append("'")
			    .append(DBSql.convertSign(rset.getString(fieldName)))
			    .append("'");
		} else if (nameType.equals("附件")) {
		    FormFileUpWeb cp = new FormFileUpWeb(
			    AWFConfig._httpdConf.getServerRoot(),
			    Integer.toString(rset.getInt("ID")),
			    Integer.toString(mapModel.getId()),
			    rset.getString(fieldName));
		    String httpURL = "";
		    if (rset.getString(fieldName) != null
			    && !rset.getString(fieldName).equals("")) {
			httpURL = cp.copyFile();
			valueSql.append(
				"'<a href=/" + httpURL + " target=newWin>")
				.append(rset.getString(fieldName))
				.append("</a>'");
		    } else {
			valueSql.append("''");
		    }
		} else if (nameType.indexOf("HTML") != -1) {
		    WorkFlowBigText bigText = new WorkFlowBigText(
			    rset.getInt("WORKFLOWID"), rset.getInt("BINDID"),
			    fieldName + ".act");
		    UtilString us = new UtilString(bigText.getBigText());
		    String content = us.replace("__eol__", "<br>");
		    content = new UtilString(content).replace("\\", "");
		    us = new UtilString(content);
		    valueSql.append("'").append(rset.getString(content))
			    .append("'");
		} else if (nameType.indexOf("xml") != -1) {
		    insertSubSub(nowId, this._idList, this._workflowId,
			    this._workflowStepId, nameType + "|",
			    this._instanceId, this._taskId, this._dbFilter,
			    this._rtClass, this._pageNow);
		    valueSql.append("'").append("").append("'");
		} else {
		    valueSql.append("'").append(rset.getString(fieldName))
			    .append("'");
		}
	    } else {
		if (nameType.equals("文本")) {
		    String tempValue = rset.getString(fieldName);
		    if (tempValue == null) {
			tempValue = "";
		    }
		    valueSql.append("'").append(DBSql.convertSign(tempValue))
			    .append("'");
		} else if (nameType.equals("数值")) {
		    if (targetMapModel.getFieldLenth().indexOf(44) != -1) {
			valueSql.append(rset.getDouble(fieldName));
		    }
		    valueSql.append(rset.getInt(fieldName));

		} else if (nameType.equals("日期")) {
		    valueSql.append("")
			    .append(DBSql.convertShortDate(UtilDate
				    .dateFormat(rset.getTimestamp(fieldName))))
			    .append("");
		} else if (nameType.equals("备注")) {
		    valueSql.append("'")
			    .append(DBSql.convertSign(rset.getString(fieldName)))
			    .append("'");
		} else if (nameType.equals("附件")) {
		    FormFileUpWeb cp = new FormFileUpWeb(
			    AWFConfig._httpdConf.getServerRoot(),
			    Integer.toString(rset.getInt("ID")),
			    Integer.toString(mapModel.getId()),
			    rset.getString(fieldName));
		    String httpURL = "";
		    if (rset.getString(fieldName) != null
			    && !rset.getString(fieldName).equals("")) {
			httpURL = cp.copyFile();
			valueSql.append(
				"'<a href=/" + httpURL + " target=newWin>")
				.append(rset.getString(fieldName))
				.append("</a>'");
		    } else {
			valueSql.append("''");
		    }
		} else if (nameType.indexOf("HTML") != -1) {
		    WorkFlowBigText bigText = new WorkFlowBigText(
			    rset.getInt("WORKFLOWID"), rset.getInt("BINDID"),
			    fieldName + ".act");
		    UtilString us = new UtilString(bigText.getBigText());
		    String content = us.replace("__eol__", "<br>");
		    content = new UtilString(content).replace("\\", "");
		    us = new UtilString(content);
		    valueSql.append("'").append(rset.getString(content))
			    .append("'");
		} else if (nameType.indexOf("xml") != -1) {
		    insertSubSub(nowId, this._idList, this._workflowId,
			    this._workflowStepId, nameType + "|",
			    this._instanceId, this._taskId, this._dbFilter,
			    this._rtClass, this._pageNow);
		    valueSql.append("'").append("").append("'");
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
	return valueSql.toString();
    }

    private void insertSubSub(Object parentId, String idList,
	    String workflowId, String workflowStepId, String xmlName,
	    String instanceId, String taskId, String dbFilter, String rtClass,
	    String pageNow) {
	this._fields2.clear();
	intValue(idList, workflowId, workflowStepId, instanceId, taskId,
		dbFilter, rtClass, pageNow);
	UtilString us2 = new UtilString(xmlName);
	Vector xmlFileType = us2.split("|");
	init2((String) xmlFileType.get(0));
	Connection conn = null;
	Statement stmt = null;
	ResultSet rset = null;
	DBData dbData = null;
	StringBuilder insertSql;
	try {
	    dbData = (DBData) ((AdapterData) this._dictionaryModel
		    .getDataFactory()).getDataAbs();
	    conn = dbData.open();
	    stmt = conn.createStatement();
	    UtilString myStr = new UtilString(idList.trim());
	    Vector myArray = myStr.split(" ");
	    String sql = "select * from " + this._dictionaryModel2._fromTable
		    + " where PARENTSUBID=" + parentId;
	    rset = DBSql.executeQuery(conn, stmt, sql);
	    while (rset.next()) {
		insertSql = new StringBuilder();
		int id = 0;
		try {
		    id = new DBSequence().getSequence("USER_WORKFLOWREPORT");
		} catch (Exception e) {
		    System.out.println("获得子表ID序列值出错!");
		    e.printStackTrace(System.err);
		}
		insertSql
			.append("insert into "
				+ this._dictionaryModel2._targetTable
				+ " (ID,ORGNO,BINDID,CREATEDATE,CREATEUSER,UPDATEDATE,UPDATEUSER,WORKFLOWID,WORKFLOWSTEPID,ISEND,");
		insertSql.append("PARENTSUBID").append(",");
		for (int i = 0; i < this._fields2.size(); ++i) {
		    FieldModel model = (FieldModel) this._fields2
			    .get(new Integer(i));
		    if (!(model.getTargetName().equals(""))) {
			if (i == this._fields2.size() - 1) {
			    insertSql.append(model.getTargetName()).append(")");
			} else {
			    insertSql.append(model.getTargetName()).append(",");
			}
		    }
		}
		insertSql.append(
			" VALUES (" + id + ","
				+ super.getContext().getCompanyModel().getId()
				+ "," + instanceId + ","
				+ DBSql.getDateDefaultValue() + ",'"
				+ super.getContext().getUID() + "',"
				+ DBSql.getDateDefaultValue() + ",'"
				+ super.getContext().getUID() + "',").append(
			workflowId + "," + workflowStepId + ",0,");
		insertSql.append(this._parentSubId.get(parentId)).append(",");
		for (int i = 0; i < this._fields2.size(); ++i) {
		    FieldModel model = (FieldModel) this._fields2
			    .get(new Integer(i));
		    if (!model.getTargetName().equals("")) {
			MetaDataMapModel mapModel = (MetaDataMapModel) MetaDataMapCache
				.getModel(this._dictionaryModel2._fromTable,
					model.getName());
			MetaDataMapModel targetMapModel = (MetaDataMapModel) MetaDataMapCache
				.getModel(this._dictionaryModel2._targetTable,
					model.getTargetName());
			if (i == this._fields2.size() - 1) {
			    insertSql.append(
				    getDbRset(
					    Integer.valueOf(rset.getInt("ID")),
					    model.getNameType(),
					    model.getName(), rset, mapModel,
					    workflowId, instanceId,
					    targetMapModel)).append(")");
			} else {
			    insertSql.append(
				    getDbRset(
					    Integer.valueOf(rset.getInt("ID")),
					    model.getNameType(),
					    model.getName(), rset, mapModel,
					    workflowId, instanceId,
					    targetMapModel)).append(",");
			}
		    }

		}
		DBSql.executeUpdate(insertSql.toString());
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	} finally {
	    dbData.createResult(conn, stmt, rset).close();
	}
	this._fields2.clear();
    }

    private String getRuntimeDataValue(String targetName) {
	MetaDataMapModel mapModel = (MetaDataMapModel) MetaDataMapCache
		.getModel(this._dictionaryModel._targetTable, targetName);
	if (mapModel != null && mapModel.getFieldDefault() != null
		&& mapModel.getFieldDefault().trim().length() > 0) {
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    Integer.parseInt(this._instanceId));
	    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		    .getModelOfStepNo(
			    processInstanceModel.getProcessDefinitionId(),
			    processInstanceModel.getActivityDefinitionNo());
	    RuntimeFormManager rf = new RuntimeFormManager(
		    super.getContext(),
		    Integer.parseInt(this._instanceId),
		    Integer.parseInt(this._taskId),
		    1,
		    Integer.parseInt(this._instanceId),
		    WorkFlowStepBindReportCache
			    .getWorkFlowStepDefaultFormId(workFlowStepModel._id),
		    this._subSheetId);
	    UtilString us = new UtilString(mapModel.getFieldDefault());
	    return rf.convertMacrosValue(us.replace("!@", "@"));
	}
	return "";
    }

    private int getRuntimeDataValueInteger(String targetName) {
	MetaDataMapModel mapModel = (MetaDataMapModel) MetaDataMapCache
		.getModel(this._dictionaryModel._targetTable, targetName);
	if (mapModel != null && mapModel.getFieldDefault() != null
		&& mapModel.getFieldDefault().trim().length() > 0) {
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    Integer.parseInt(this._instanceId));
	    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		    .getModelOfStepNo(
			    processInstanceModel.getProcessDefinitionId(),
			    processInstanceModel.getActivityDefinitionNo());
	    RuntimeFormManager rf = new RuntimeFormManager(
		    super.getContext(),
		    Integer.parseInt(this._instanceId),
		    Integer.parseInt(this._taskId),
		    1,
		    Integer.parseInt(this._instanceId),
		    WorkFlowStepBindReportCache
			    .getWorkFlowStepDefaultFormId(workFlowStepModel._id),
		    this._subSheetId);
	    UtilString us = new UtilString(mapModel.getFieldDefault());
	    String value = rf.convertMacrosValue(us.replace("!@", "@"));
	    if (value == null || value.trim().length() == 0) {
		return 0;
	    }
	    return Integer.parseInt(value);
	}

	return 0;
    }

    private double getRuntimeDataValueDouble(String targetName) {
	MetaDataMapModel mapModel = (MetaDataMapModel) MetaDataMapCache
		.getModel(this._dictionaryModel._targetTable, targetName);
	if (mapModel != null && mapModel.getFieldDefault() != null
		&& mapModel.getFieldDefault().trim().length() > 0) {
	    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory
		    .createProcessInstance().getInstance(
			    Integer.parseInt(this._instanceId));
	    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache
		    .getModelOfStepNo(
			    processInstanceModel.getProcessDefinitionId(),
			    processInstanceModel.getActivityDefinitionNo());
	    RuntimeFormManager rf = new RuntimeFormManager(
		    super.getContext(),
		    Integer.parseInt(this._instanceId),
		    Integer.parseInt(this._taskId),
		    1,
		    Integer.parseInt(this._instanceId),
		    WorkFlowStepBindReportCache
			    .getWorkFlowStepDefaultFormId(workFlowStepModel._id),
		    this._subSheetId);
	    UtilString us = new UtilString(mapModel.getFieldDefault());
	    String value = rf.convertMacrosValue(us.replace("!@", "@"));
	    if (value == null || value.trim().length() == 0) {
		return 0.0D;
	    }
	    return Double.valueOf(value).doubleValue();
	}

	return 0.0D;
    }

    private void getDbDefaultValueField(StringBuilder insertSql) {
	MetaDataModel mdModel = (MetaDataModel) MetaDataCache
		.getModel(this._dictionaryModel._targetTable);
	if (mdModel != null) {
	    Hashtable metadataMapList = MetaDataMapCache
		    .getEntityListOfMetaData(mdModel.getId());
	    if (metadataMapList != null && metadataMapList.size() > 0)
		for (int i = 0; i < metadataMapList.size(); ++i) {
		    MetaDataMapModel mapModel = (MetaDataMapModel) metadataMapList
			    .get(new Integer(i));
		    if (mapModel.getFieldName().equals("PARENTSUBID")) {
			insertSql.append(mapModel.getFieldName()).append(",");
		    } else if (mapModel.getFieldDefault().trim().length() > 0
			    && isFieldHave(mapModel.getFieldName())) {
			insertSql.append(mapModel.getFieldName()).append(",");
		    }
		}
	}
    }

    private boolean isFieldHave(String _fieldName) {
	for (int j = 0; j < this._fields.size(); ++j) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(j));
	    if (!model.getTargetName().equals("")
		    && model.getTargetName().equals(_fieldName)) {
		return false;
	    }
	}

	return true;
    }

    private void getDbDefaultValueValue(StringBuilder insertSql) {
	MetaDataModel mdModel = (MetaDataModel) MetaDataCache
		.getModel(this._dictionaryModel._targetTable);
	if (mdModel != null) {
	    Hashtable metadataMapList = MetaDataMapCache
		    .getEntityListOfMetaData(mdModel.getId());
	    if (metadataMapList != null && metadataMapList.size() > 0) {
		for (int i = 0; i < metadataMapList.size(); ++i) {
		    MetaDataMapModel mapModel = (MetaDataMapModel) metadataMapList
			    .get(new Integer(i));
		    if (mapModel.getFieldName().equals("PARENTSUBID")) {
			insertSql.append(getRefId() + ",");
		    } else if (mapModel.getFieldDefault().trim().length() > 0
			    && isFieldHave(mapModel.getFieldName())) {
			if (mapModel.getFieldType().equals("文本"))
			    insertSql
				    .append("'")
				    .append(DBSql
					    .convertSign(getRuntimeDataValue(mapModel
						    .getFieldName())))
				    .append("',");
			else if (mapModel.getFieldType().equals("数值")) {
			    if (mapModel.getFieldLenth().indexOf(',') != -1)
				insertSql.append(
					getRuntimeDataValueDouble(mapModel
						.getFieldName())).append(",");
			    else
				insertSql.append(
					getRuntimeDataValueInteger(mapModel
						.getFieldName())).append(",");
			} else if (mapModel.getFieldType().equals("日期")) {
			    insertSql
				    .append(DBSql.convertShortDate(UtilDate.dateFormat(Timestamp
					    .valueOf(getRuntimeDataValue(mapModel
						    .getFieldName())))))
				    .append(",");
			}
		    }
		}
	    }
	}
    }
}