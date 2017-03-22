package com.actionsoft.awf.form.execute.plugins.dictionary.extend1;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.component.web.UICascadeUtil;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryConditionUI;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryObject;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryUtil;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.DataResult;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Html;
import com.actionsoft.awf.util.PageIndex;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.i18n.I18nRes;

public class GridDictionary extends DictionaryObject {
    private DictionaryModel _dictionaryModel = new DictionaryModel();

    private Map _fields = new UnsyncHashtable();

    private Map _condition = new UnsyncHashtable();

    private boolean isInsert = false;

    private boolean isUpdate = false;

    private boolean isDelete = false;

    private WorkFlowModel _appModel = null;

    private WorkFlowStepModel _stepModel = null;
    private int _formId;
    int _subSheetId;

    public GridDictionary(UserContext uct) {
	super(uct);
    }

    public Map getCondition() {
	return this._condition;
    }

    public void init(String fileName) {
	this._dictionaryModel = DictionaryUtil.fromFile(fileName);
	this._fields = this._dictionaryModel._fields;
	this._condition = this._dictionaryModel._condition;

	if (this._dictionaryModel._htmlModel != null
		&& this._dictionaryModel._htmlModel.length() > 0)
	    setHtmlModel(this._dictionaryModel._htmlModel);
    }

    public String DIYDataGrild(String dbFilter) {
	String fileName = getXmlFile();

	if (fileName.length() == 0) {
	    return "<script>window.close();alert('"
		    + I18nRes.findValue(getContext().getLanguage(),
			    "这个字段组件的模型定义不符合规则，没有在参考值中找到对应的xml数据字典定义文件")
		    + "');</script>";
	}

	File dir = new File("./dictionary/");
	File[] files = dir.listFiles();
	boolean findFile = false;
	for (File file : files) {
	    if (file.getName().equalsIgnoreCase(fileName)) {
		findFile = true;
		fileName = file.getName();
		break;
	    }
	}

	String filePath = "./dictionary/" + fileName;

	if (!findFile) {
	    return "<script>window.close();alert('"
		    + I18nRes.findValue(getContext().getLanguage(),
			    "这个数据字典的xml定义文件没有找到") + "!"
		    + I18nRes.findValue(getContext().getLanguage(), "文件位置")
		    + " ：[" + filePath + "]');</script>";
	}

	init(fileName);
	StringBuilder html = new StringBuilder();
	int k = 0;
	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));
	    try {
		k += Integer.parseInt(model.getWidth());
	    } catch (Exception localException1) {
	    }
	}
	html.append("<div id=AWS_DICTIONARY_TABLE name=AWS_DICTIONARY_TABLE style='height:100%;background-color:ffffff'><div name=AWS_DICTIONARY_TABLE_CONTENT id=AWS_DICTIONARY_TABLE_CONTENT style='overflow:auto;heigth:100%;'>");

	if (this._condition.size() > 0) {
	    StringBuilder js = new StringBuilder();
	    StringBuilder bindValueJS = new StringBuilder(
		    "function getBindValue(){var tmpCond='';\n");
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
		html.append("<td width=20% nowrap>")
			.append(I18nRes.findValue(getContext().getLanguage(),
				condModel.getFieldTitle())).append("</td>");

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
		if (fieldName.trim().length() > 0) {
		    if (condModel.getUiComponent().equals("单选按纽组")) {
			js.append("tmpCond=tmpCond+' '+'_").append(fieldName)
				.append("{'+");
			js.append("getRadio").append(fieldName).append("Value")
				.append("()");
			js.append("+'}").append(fieldName).append("_'");
			bindValueJS.append("tmpCond=tmpCond+' '+'_")
				.append(fieldName).append("{'+");
			bindValueJS.append("getRadio").append(fieldName)
				.append("Value").append("()");
			bindValueJS.append("+'}").append(fieldName)
				.append("_'");
		    } else if (condModel.getUiComponent().equals("复选框")) {
			js.append("tmpCond=tmpCond+' '+'_").append(fieldName)
				.append("{'+");
			js.append("getCheck").append(fieldName).append("Value")
				.append("()");
			js.append("+'}").append(fieldName).append("_'");
			bindValueJS.append("tmpCond=tmpCond+' '+'_")
				.append(fieldName).append("{'+");
			bindValueJS.append("getCheck").append(fieldName)
				.append("Value").append("()");
			bindValueJS.append("+'}").append(fieldName)
				.append("_'");
		    } else {
			js.append("try{");
			js.append("if(frmMain.").append(fieldName)
				.append(".value!=''){tmpCond=tmpCond+' '+'_")
				.append(fieldName).append("{'+frmMain.")
				.append(fieldName).append(".value+'}")
				.append(fieldName).append("_';}");
			js.append("}catch(e){}");
			bindValueJS.append("try{");
			bindValueJS.append("if(frmMain.").append(fieldName)
				.append(".value!=''){tmpCond=tmpCond+' '+'_")
				.append(fieldName).append("{'+frmMain.")
				.append(fieldName).append(".value+'}")
				.append(fieldName).append("_';}");
			bindValueJS.append("}catch(e){}");
		    }
		}
		js.append("\n");
		bindValueJS.append("\n");
	    }
	    html.append("</table></td><td><input type='button' value='<I18N#执行过滤条件>' class='actionsoftButton' onClick=\"filterCondition();return false;\"  border='0'></td></tr>");
	    html.append("</table></fieldset>");
	    js.append("frmMain.dbFilter.value=tmpCond;try{refreshMe3();}catch(e){}\n};\n");
	    bindValueJS.append(" return tmpCond;\n};\n");

	    for (int j = 0; j < this._condition.size(); ++j) {
		DictionaryConditionUI condModel = (DictionaryConditionUI) this._condition
			.get(new Integer(j));
		if (condModel != null) {
		    if (condModel.getUiComponent().equals("单选按纽组")) {
			js.append("function getRadio")
				.append(condModel.getUiName())
				.append("Value() {");
			js.append("var radio=document.getElementsByName('")
				.append(condModel.getUiName()).append("');");
			js.append("for(var i=0;i<radio.length;i++){");
			js.append("if(radio[i].checked){return radio[i].value;}");
			js.append("}");
			js.append("}");
		    } else if (condModel.getUiComponent().equals("复选框")) {
			js.append("function getCheck")
				.append(condModel.getUiName())
				.append("Value() {");
			js.append("var check=document.getElementsByName('")
				.append(condModel.getUiName()).append("');");
			js.append(" var x='';");
			js.append("for(var i=0;i<check.length;i++){");
			js.append("if(check[i].checked){x=x+''+ check[i].value;}");
			js.append("}");
			js.append("return x;");
			js.append("}");
		    }
		}
	    }
	    html.append("\n<script>\n");
	    html.append(js);
	    html.append(bindValueJS);
	    html.append("\n</script>\n");
	}

	html.append("<table width=")
		.append(k == 0 ? "100%" : Integer.toString(k))
		.append(" cellpadding=0 border=1 cellspacing=0 bordercolorlight=#CCCCCC bordercolordark=#FFFFFF>");
	html.append("<tr>");
	html.append("<td class=actionsoftReportTitle width=1%>&nbsp;</td>");
	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));
	    if (!model.getIsHidden().toUpperCase().equals("TRUE")) {
		html.append("<td class=actionsoftReportTitle width=")
			.append(model.getWidth())
			.append(">")
			.append(I18nRes.findValue(getContext().getLanguage(),
				model.getDisplay())).append("</td>");
	    }
	}

	html.append("<tr>\n");

	DataResult dataResult = null;

	int lineCount = 0;

	int currentLine = 0;

	int lineNumber = Integer.parseInt(this._dictionaryModel._line);

	try {
	    dataResult = this._dictionaryModel.getDataFactory().queryData(this,
		    dbFilter);

	    int lineFirst = lineNumber * (getPageNow() - 1);

	    while (dataResult != null && dataResult.next()) {
		++lineCount;
		if (dataResult.isUsePaging() || lineCount > lineFirst) {
		    ++currentLine;
		    if (dataResult.isUsePaging() || lineNumber == 0
			    || currentLine <= lineNumber) {
			StringBuilder clickEvent = new StringBuilder();
			StringBuilder tr = new StringBuilder();
			int boId = 0;
			int bindId = 0;
			if (this.isInsert || this.isDelete || this.isUpdate) {
			    boId = Integer.parseInt(dataResult.getFiledValue(
				    this._dictionaryModel._boIdField)
				    .toString());
			    bindId = Integer.parseInt(dataResult.getFiledValue(
				    this._dictionaryModel._boBindIdField)
				    .toString());
			}

			clickEvent.append(" onClick=\"setParameterFlex(")
				.append(currentLine).append(",'");

			for (int i = 0; i < this._fields.size(); ++i) {
			    FieldModel model = (FieldModel) this._fields
				    .get(new Integer(i));
			    Object obj = dataResult.getFiledValue(model);
			    String value = (obj == null) ? "" : String
				    .valueOf(obj);
			    String targetValue = value;
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

			    if (model.getTargetName().equals(""))
				continue;
			    value = targetValue;
			    if (value == null) {
				value = "";
			    }
			    value = new UtilString(value).replace("__eol__",
				    "<br>");

			    if (value.lastIndexOf("00:00:00.0") > 0) {
				value = new UtilString(value).replace(
					"00:00:00.0", "");
			    }

			    value = Html.escape(value.replaceAll("'", "\\\\'"));
			    if (value.indexOf("\\") > -1) {
				value = value.replace("\\", "\\\\");
			    }
			    clickEvent.append("_P_").append(value);
			}

			if (getViewType().equals(""))
			    clickEvent
				    .append("');\" onDblClick=\"getParameter();\"");
			else {
			    clickEvent.append("')\" ");
			}

			String joinTmp = clickEvent.toString();
			html.append(
				"<tr style='cursor:pointer;cursor:hand;' onmouseout=\"out_change(this,'#FFFFFF');\" onmouseover=\"over_change(this,'#EBF#F6');\"")
				.append(joinTmp).append(">");
			html.append(
				"<td ><input type=radio style='border-color: gray'  name=checkButton value='")
				.append(currentLine).append("'></td>");

			joinTmp = tr.toString();
			html.append(joinTmp);
			html.append("</tr>\n");
		    }
		}
	    }
	} catch (SQLException sqle) {
	    return "<b><I18N#字典xml描述错误></b>：<br>" + sqle.toString() + "<br>";
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    return I18nRes.findValue(getContext().getLanguage(),
		    "执行错误,详细请参考error.log") + "<br>" + e.toString();
	} finally {
	    if (dataResult != null)
		dataResult.close();
	}
	if (dataResult != null) {
	    dataResult.close();
	}

	html.append("</table></div>");
	if (lineNumber > 0) {
	    if (dataResult != null && dataResult.isUsePaging()) {
		lineCount = dataResult.getTotal();
	    }
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
	    html.append("<I18N#分类>：<select name=choiceValue class=actionsoftSelect onchange=\"refreshMe2();\">");
	    if (!nowDbFilter.equals("")) {
		html.append("<option value='" + nowDbFilter + "' selected >"
			+ nowDbFilter + "</option>");
	    }
	    html.append("<option value='' style='color:gray'><I18N#请选择>  "
		    + I18nRes.findValue(getContext().getLanguage(),
			    this._dictionaryModel._choiceName) + "...</option>");
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

    public String getCheckHidden() {
	StringBuilder html = new StringBuilder();
	int p = 0;
	for (int i = 0; i < this._fields.size(); ++i) {
	    ++p;
	    html.append("<input type=hidden name=p" + p + "> ").append("\n");
	}
	return html.toString();
    }

    public String DIYJavaScript() {
	StringBuilder js = new StringBuilder();
	js.append("\n\n<script type='text/javascript'>\n function getParameter(){\n var stroeRowIndex;");

	if (this._dictionaryModel._insertBeforeJavaScript.length() > 0) {
	    js.append("\n //此处声明了在插入数据到父窗体前，扩展的JavaScript代码");
	    js.append("\n //--------insertBeforeJavaScript begin----------\n\n");
	    js.append(this._dictionaryModel._insertBeforeJavaScript);
	    js.append("\n //--------insertBeforeJavaScript end----------\n\n");
	}
	int p = 0;
	for (int i = 0; i < this._fields.size(); ++i) {
	    FieldModel model = (FieldModel) this._fields.get(new Integer(i));

	    if (!model.getTargetName().equals("")) {
		++p;

		if (getViewType().equals("")) {
		    js.append("try{\n ");
		    js.append("if(this.parent.opener.document.frmMain.")
			    .append(model.getTargetName())
			    .append(".type == 'textarea'){\n");

		    js.append("var dis=this.parent.opener.document.frmMain.")
			    .append(model.getTargetName())
			    .append(".style.display;\n");
		    js.append("if(dis=='none'){\n");
		    js.append(
			    " this.parent.opener.document.getElementById('"
				    + model.getTargetName()
				    + "_div').innerHTML=frmMain.p")
			    .append(p)
			    .append(".value.replace(/“/g,'\"').replace(/<br>/g,'\\r\\n');\n");
		    js.append("}\n");
		    js.append("this.parent.opener.document.frmMain.")
			    .append(model.getTargetName())
			    .append(".value=frmMain.p")
			    .append(p)
			    .append(".value.replace(/“/g,'\"').replace(/<br>/g,'\\r\\n');\n");
		    js.append("} else {\n");
		    js.append("this.parent.opener.document.frmMain.")
			    .append(model.getTargetName())
			    .append(".value=frmMain.p").append(p)
			    .append(".value.replace(/“/g,'\"');\n");
		    js.append("}\n");
		    js.append(UICascadeUtil
			    .fireCascadeEvt("parent.opener.document.frmMain."
				    + model.getTargetName()));
		    js.append("}catch(e){}\n");
		} else {
		    String nameSpace = getViewType().substring(
			    getViewType().indexOf("|") + 1);
		    js.append("try{\n ");
		    js.append("stroeRowIndex=this.parent.window." + nameSpace
			    + ".Grid.getCurrentRowInd();\n");
		    js.append(
			    "this.parent.window."
				    + nameSpace
				    + ".Grid.getDataSource().getAt(stroeRowIndex).set('")
			    .append(model.getTargetName())
			    .append("',frmMain.p").append(p)
			    .append(".value.replace(/“/g,'\"'));\n");
		    js.append("}catch(e){}\n");
		}
	    }
	}

	if (this._dictionaryModel._insertAfterJavaScript.length() > 0) {
	    js.append("\n //此处声明了在插入数据到父窗体后，扩展的JavaScript代码");
	    js.append("\n //--------insertAfterJavaScript begin----------\n\n");
	    js.append(this._dictionaryModel._insertAfterJavaScript);
	    js.append("\n //--------insertAfterJavaScript end----------\n\n");
	}
	if (getViewType().equals(""))
	    js.append("\n window.close();}\n</script>\n");
	else {
	    js.append("\n }\n</script>\n");
	}
	return js.toString();
    }

    public String DIYTitle() {
	return this._dictionaryModel._title;
    }
}