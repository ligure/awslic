package com.actionsoft.awf.form.execute.plugins.component;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.util.UtilFile;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.i18n.I18nRes;

public class FormUIComponentDictButtonImpl extends FormUIComponentAbst {
    public FormUIComponentDictButtonImpl(MetaDataMapModel metaDataMapModel,
	    String value) {
	super(metaDataMapModel, value);
    }

    public String getModifyHtmlDefine(Hashtable params) {
	StringBuilder html = new StringBuilder();
	html.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr>");
	html.append("<td nowrap valign=\"middle\" >");
	String innerEvent = getMetaDataMapModel().getHtmlInner();
	String displaySql = getMetaDataMapModel().getDisplaySetting();
	String tmpSql = "";
	if (getMetaDataMapModel().getDisplaySetting().toUpperCase()
		.startsWith("CASCADE")) {
	    displaySql = getMetaDataMapModel().getDisplaySetting().split("\\>")[1];
	    tmpSql = getMetaDataMapModel().getDisplaySetting().split("\\>")[1];
	} else {
	    tmpSql = getMetaDataMapModel().getDisplaySetting();
	}
	String xmlFile = "";
	int separatedChecked = 0;
	if (innerEvent != null
		&& innerEvent.toLowerCase().indexOf("onchange") == -1
		&& displaySql.indexOf("|多选") == -1) {
	    if (displaySql.indexOf("|数据效验") != -1) {
		UtilString util = new UtilString(tmpSql);
		Vector v = util.split("|");
		displaySql = v.get(0).toString();
		xmlFile = v.get(0).toString();

		if (isGetFormDate(xmlFile)) {
		    displaySql = displaySql + "$";
		}
		innerEvent = innerEvent
			+ " onchange=\"openAjaxDictionary(frmMain,this,'"
			+ displaySql + "',this.value);\"";
		displaySql = v.get(0).toString();
	    } else if (displaySql.indexOf("|清空") != -1) {
		UtilString util = new UtilString(tmpSql);
		Vector v = util.split("|");
		displaySql = v.get(0).toString();
		xmlFile = v.get(0).toString();
	    } else {
		xmlFile = displaySql;
	    }

	    if (isGetFormDate(xmlFile))
		displaySql = displaySql + "$";
	} else if ((innerEvent != null)
		&& (innerEvent.toLowerCase().indexOf("onchange") == -1)
		&& (displaySql.indexOf("|多选") != -1)) {
	    String[] disArray = displaySql.split("\\|");
	    String separated = disArray.length > 1 ? disArray.length > 2 ? disArray.length > 3 ? disArray.length > 4 ? disArray[4]
		    : disArray[3]
		    : disArray[2]
		    : disArray[1]
		    : "";
	    try {
		separatedChecked = Integer.parseInt(separated);
	    } catch (Exception e) {
		separatedChecked = 0;
	    }
	    UtilString util = new UtilString(tmpSql);
	    Vector v = util.split("|");
	    displaySql = v.get(0).toString() + "|" + v.get(1).toString() + "|"
		    + separatedChecked;
	    xmlFile = v.get(0).toString();

	    if (isGetFormDate(xmlFile)) {
		displaySql = displaySql + "$";
	    }
	}
	int size = getMetaDataMapModel().getInputWidth();
	if (params.get("bindId") == null)
	    size = 16;
	html.append("<input ").append(innerEvent)
		.append(" type='text'  name='")
		.append(getMetaDataMapModel().getFieldName()).append("' ")
		.append(" class ='actionsoftInput' size='").append(size)
		.append("' value='").append(getValue()).append("'>");
	html.append("</td>");
	html.append("<td nowrap valign=\"middle\" >");
	if (getMetaDataMapModel().isNotNull() && params.get("bindId") != null) {
	    html.append("<img src=../aws_img/notNull.gif alt='<I18N#必须填写>'>");
	} else {
	    html.append("<img src=../aws_img/null.gif alt='<I18N#允许空>'>");
	}
	html.append("</td>");
	html.append("<td nowrap valign=\"middle\" >");
	html.append("<img style='display:none' name=AWS_DICT_LOADING_"
		+ getMetaDataMapModel().getFieldName()
		+ "  id=AWS_DICT_LOADING_"
		+ getMetaDataMapModel().getFieldName()
		+ " src=../aws_img/loading.gif><input name=AWS_DICT_BUT_"
		+ getMetaDataMapModel().getFieldName()
		+ " type=button title='<I18N#弹出选择窗口>' class ='actionsoftDictButton' onClick=\"openDictionary(frmMain,'com.actionsoft.awf.form.execute.plugins.dictionary.extend1.GridDictionary','"
		+ displaySql + "',frmMain."
		+ getMetaDataMapModel().getFieldName()
		+ ".value);return false;\" border='0'>");
	html.append("</td>");
	html.append("<td nowrap valign=\"middle\" >");

	if (tmpSql.indexOf("清空") != -1) {
	    html.append("&nbsp;&nbsp;<img style='display:none' name=AWS_DICT_Clear_"
		    + getMetaDataMapModel().getFieldName()
		    + "  id=AWS_DICT_Clear_"
		    + getMetaDataMapModel().getFieldName()
		    + " src=../aws_img/loading.gif><input name=AWS_DICT_DEL_BUT_"
		    + getMetaDataMapModel().getFieldName()
		    + " type=button title='<I18N#清空选择>' class ='actionsoftDictDeleteButton' onClick=\"clearDictionary(frmMain,'com.actionsoft.awf.form.execute.plugins.dictionary.extend1.GridDictionary','"
		    + xmlFile + "');return false;\" border='0'>");
	}
	html.append("</td>");
	html.append("</tr></table>");
	return html.toString();
    }

    private boolean isGetFormDate(String xmlFile) {
	if (xmlFile.indexOf("|") > -1)
	    xmlFile = (String) new UtilString(xmlFile).split("|").get(0);
	String xml = UtilFile.readAll("dictionary/" + xmlFile);

	return xml != null
		&& (xml.toLowerCase().indexOf("$getform(") > -1 || xml
			.toLowerCase().indexOf("$getgrid(") > -1);
    }

    public String getReadHtmlDefine(Hashtable params) {
	StringBuilder html = new StringBuilder();
	html.append("<input type=hidden name='")
		.append(getMetaDataMapModel().getFieldName())
		.append("' value=\"").append(getValue()).append("\">")
		.append(getValue());
	return html.toString();
    }

    public String getSettingWeb() {
	StringBuilder settingHtml = new StringBuilder();
	settingHtml.append("<tr height='10px'>");
	String isMoreCheckChecked = "";
	int separatedChecked = 0;
	String displayValue = getMetaDataMapModel().getDisplaySetting();
	boolean isCascade = displayValue.toUpperCase().startsWith("CASCADE");
	if (isCascade) {
	    displayValue = displayValue.split("\\>")[1];
	}
	Thread currentThread = Thread.currentThread();
	String ctName = currentThread.getName();
	String lang = ctName.substring(ctName.lastIndexOf("--") + 2);
	if (displayValue != null && displayValue.trim().length() > 0) {
	    String[] disArray = displayValue.split("\\|");
	    settingHtml.append("<td width='100%' class='lightyellow'>");
	    settingHtml
		    .append("<input type='text'   size='55' maxlength='99' id='AWS_DICTIONRY' name='AWS_DICTIONRY'   value='"
			    + disArray[0] + "' class='actionsoftInput' />");

	    isMoreCheckChecked = disArray.length > 1
		    && disArray[1].equals("多选") ? "checked" : "";
	    String isFormDataCheckChecked = (disArray.length > 1 && disArray[1]
		    .equals("数据效验"))
		    || (disArray.length > 2 && disArray[2].equals("数据效验")) ? "checked"
		    : "";
	    String isFormDataClearChecked = (disArray.length > 1 && disArray[1]
		    .equals("清空"))
		    || (disArray.length > 2 && disArray[2].equals("清空"))
		    || (disArray.length > 3 && disArray[3].equals("清空")) ? "checked"
		    : "";
	    String separated = disArray.length > 1 ? disArray.length > 2 ? disArray.length > 3 ? disArray.length > 4 ? disArray[4]
		    : disArray[3]
		    : disArray[2]
		    : disArray[1]
		    : "";
	    try {
		separatedChecked = Integer.parseInt(separated);
	    } catch (Exception e) {
		separatedChecked = 0;
	    }

	    settingHtml
		    .append("<label onclick='displaySeparatedTr();'><input name='isMoreCheck' id='isMoreCheck' type='checkbox' ")
		    .append(isMoreCheckChecked)
		    .append(" value='|多选' /> <label>"
			    + I18nRes.findValue(lang, "允许行多选")
			    + " </label></label>&nbsp;");
	    settingHtml
		    .append("<input name='isFormDataCheck' id='isFormDataCheck' type='checkbox' ")
		    .append(isFormDataCheckChecked)
		    .append(" value='|数据效验' /> <label>"
			    + I18nRes.findValue(lang, "数据效验")
			    + "</label>&nbsp;");
	    settingHtml
		    .append("<input name='isFormDataClear' id='isFormDataClear' type='checkbox' ")
		    .append(isFormDataClearChecked)
		    .append(" value='|清空' /> <label> "
			    + I18nRes.findValue(lang, "允许清空")
			    + "</label>&nbsp;");

	    settingHtml
		    .append("<a href='###' onclick='openTabEditDic();return false'>")
		    .append("<img src='../aws_img/edit.gif' alt='"
			    + I18nRes.findValue(lang, "编辑数据字典") + "' boder=0>"
			    + I18nRes.findValue(lang, "数据字典管理器"))
		    .append("</a>");
	    if (!isExists(disArray[0])) {
		settingHtml.append("<font color='red'>").append(disArray[0])
			.append(I18nRes.findValue(lang, "不存在") + "</font>");
	    }
	    settingHtml.append("</td>").append("\n");
	} else {
	    settingHtml
		    .append("<td width='100%' class='lightyellow' nowrap=\"nowrap\">");
	    settingHtml
		    .append("<input type='text'   size='55' maxlength='99' id='AWS_DICTIONRY' name='AWS_DICTIONRY'   value='' class='actionsoftInput' />");
	    settingHtml
		    .append("<label onclick='displaySeparatedTr();'><input name='isMoreCheck' id='isMoreCheck' type='checkbox'  value='|多选' /> <label> "
			    + I18nRes.findValue(lang, "允许行多选")
			    + "</label></label>&nbsp;&nbsp;");
	    settingHtml
		    .append("<input name='isFormDataCheck' id='isFormDataCheck' type='checkbox' checked value='|数据效验' /> <label> "
			    + I18nRes.findValue(lang, "数据效验")
			    + "</label>&nbsp;&nbsp;");
	    settingHtml
		    .append("<input name='isFormDataClear' id='isFormDataClear' type='checkbox' checked value='|清空' /> <label> "
			    + I18nRes.findValue(lang, "允许清空")
			    + "</label>&nbsp;");
	    settingHtml
		    .append("<a href='###' onclick='openTabEditDic();return false;'>")
		    .append("<img src='../aws_img/edit.gif' alt='"
			    + I18nRes.findValue(lang, "数据字典管理器") + "' boder=0>"
			    + I18nRes.findValue(lang, "数据字典管理器"))
		    .append("</a>");
	    settingHtml.append("</td>").append("\n");
	}

	settingHtml.append("</tr>").append("\n");

	settingHtml.append("<tr id=\"separatedTr\" style=\"display:")
		.append(isMoreCheckChecked.equals("checked") ? "" : "none")
		.append("\">").append("\n");
	settingHtml
		.append("<td>")
		.append("<br><img src=../aws_img/uicomponent/ExternalPart.gif border=0><label>&nbsp;"
			+ I18nRes.findValue(lang, "行多选分隔符") + ":</label>")
		.append("<input type=radio name=separated value=0 ")
		.append(separatedChecked == 0 ? "checked" : "")
		.append("><img src=../aws_img/uicomponent/space.gif border=0> ("
			+ I18nRes.findValue(lang, "空格") + ")\n")
		.append("&nbsp;&nbsp;<input type=radio name=separated value=1 ")
		.append(separatedChecked == 1 ? "checked" : "")
		.append("><img src=../aws_img/uicomponent/comma.gif border=0 > ("
			+ I18nRes.findValue(lang, "逗号") + ")\n")
		.append("&nbsp;&nbsp;<input type=radio name=separated value=2 ")
		.append(separatedChecked == 2 ? "checked" : "")
		.append("><img src=../aws_img/uicomponent/vertical.gif border=0 > ("
			+ I18nRes.findValue(lang, "竖线") + ")\n")
		.append("</td>\n");
	settingHtml.append("</tr>").append("\n");
	settingHtml.append(
		"<tr><td width='100%' > <br><img src=../aws_img/bf_nonew.gif><label>"
			+ I18nRes.findValue(lang, "请选择一个XML数据字典配置方案")
			+ "(<font color=red>"
			+ I18nRes.findValue(lang, "输入框支持模糊搜索功能") + "</font>),"
			+ I18nRes.findValue(lang, "若不存在可点击右侧图标进行新建")
			+ "</label> </td></tr>").append("\n");
	settingHtml.append("").append("\n");
	settingHtml.append("<script>\n");
	settingHtml.append("function initEditor(){\n");
	settingHtml.append("\tvar displayEditorValue='';").append("\n");
	settingHtml
		.append("\tvar xmlValue=document.getElementById('AWS_DICTIONRY').value;")
		.append("\n");

	settingHtml
		.append("\t\tif(document.getElementById('isMoreCheck').checked==true&&document.getElementById('AWS_DICTIONRY').value.indexOf('\\|多选')==-1){");
	settingHtml
		.append("\t\t\tdisplayEditorValue=displayEditorValue+document.getElementById('isMoreCheck').value;")
		.append("\n");
	settingHtml.append("\t\t}").append("\n");

	settingHtml
		.append("\t\tif(document.getElementById('isFormDataCheck').checked==true&&document.getElementById('AWS_DICTIONRY').value.indexOf('\\|数据效验')==-1){");
	settingHtml
		.append("\t\t\tdisplayEditorValue=displayEditorValue+document.getElementById('isFormDataCheck').value;")
		.append("\n");
	settingHtml.append("\t\t}").append("\n");

	settingHtml
		.append("\t\tif(document.getElementById('isFormDataClear').checked==true&&document.getElementById('AWS_DICTIONRY').value.indexOf('\\|清空')==-1){");
	settingHtml
		.append("\t\t\tdisplayEditorValue=displayEditorValue+document.getElementById('isFormDataClear').value;")
		.append("\n");
	settingHtml.append("\t\t}").append("\n");

	settingHtml
		.append("\t\tif(document.getElementById('isMoreCheck').checked==true&&document.getElementById('AWS_DICTIONRY').value.indexOf('\\|多选')==-1){");
	settingHtml
		.append("\t\t\tdisplayEditorValue=displayEditorValue+getCheckedRadioValue('separated');")
		.append("\n");
	settingHtml.append("\t\t}").append("\n");

	settingHtml.append(
		"\t\tfrmMain.displayEditor.value=displayEditorValue;").append(
		"\n");
	settingHtml
		.append("\tif(xmlValue==''){frmMain.displayEditor.value='';alert(\""
			+ I18nRes.findValue(lang, "请选择字典文件")
			+ "\");return false;}else{frmMain.displayEditor.value=xmlValue+displayEditorValue;}\n");
	settingHtml.append("}").append("\n");
	settingHtml.append(getSearch());
	settingHtml.append("\n");
	settingHtml.append("function displaySeparatedTr(){  \n");
	settingHtml
		.append("\tif(document.getElementById('isMoreCheck').checked){\n");
	settingHtml
		.append("\t\tdocument.getElementById('separatedTr').style.display='';\n");
	settingHtml.append("\t}else {\n");
	settingHtml
		.append("\t\tdocument.getElementById('separatedTr').style.display='none';\t\n");
	settingHtml.append("\t}\n");
	settingHtml.append("}\n");
	settingHtml.append("function getCheckedRadioValue(radioName){\n");
	settingHtml
		.append("\t\tvar checkedNames=document.getElementsByName(radioName);\n");
	settingHtml.append("\t\tfor(var i=0;i<checkedNames.length;i++){\n");
	settingHtml.append("\t\t\tif(checkedNames[i].checked){\n");
	settingHtml.append("\t\t\t\treturn '|'+checkedNames[i].value;\n");
	settingHtml.append("\t\t   }\n");
	settingHtml.append("\t    }\n");
	settingHtml.append("\t\treturn '';\n");
	settingHtml.append("}\n");
	settingHtml.append("</script>").append("\n");
	return settingHtml.toString();
    }

    private String getSearch() {
	StringBuilder buffer = new StringBuilder();
	buffer.append("\tExt.onReady(function(){ \n");
	buffer.append("\tvar searchStore = new Ext.data.Store({ \n");
	buffer.append("\t\tproxy : new Ext.data.HttpProxy({ \n");
	buffer.append("\t\t\tmethod : 'POST', \n");
	buffer.append("\t\t\turl : \"./login.wf\" \n");
	buffer.append("\t\t}), \n");
	buffer.append("\t\tbaseParams : { \n");
	buffer.append("\t\t\tsid : document.getElementById(\"sid\").value, \n");
	buffer.append("\t\t\tcmd : 'Display_Editor_Dict_Search' \n");
	buffer.append("\t\t}, \n");
	buffer.append("\t\treader : new Ext.data.JsonReader({ \n");
	buffer.append("\t\t\troot : 'rows', \n");
	buffer.append("\t\t\ttotalProperty : 'totalCount' \n");
	buffer.append("\t\t}, ['value']) \n");
	buffer.append("\t}); \n");
	buffer.append("\tvar resultTpl = new Ext.XTemplate( \n");
	buffer.append("\t\t\t'<table border=\"0\"  cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" class=\"x-combo-list-talbe-ist\">', \n");
	buffer.append("\t\t\t'<tpl for=\".\">', \n");
	buffer.append("\t\t\t'<tr class=\"x-combo-list-item search-item\"><td width=\"50%\" >{value}</td></tr>', \n");
	buffer.append("\t\t\t'</tpl>', '</table>' \n");
	buffer.append("\t); \n");
	buffer.append("\tvar search = new Ext.form.ComboBox({ \n");
	buffer.append("\t\tstore : searchStore, \n");
	buffer.append("\t\tapplyTo : 'AWS_DICTIONRY', \n");
	buffer.append("\t\ttypeAhead : false, \n");
	buffer.append("\t\tloadingText : 'Searching...', \n");
	buffer.append("\t\twidth : 200, \n");
	buffer.append("\t\tpageSize : 5, \n");
	buffer.append("\t\tvalueField:'', \n");
	buffer.append("\t\tlistWidth : 300, \n");
	buffer.append("\t\thideTrigger : true, \n");
	buffer.append("\t\ttpl : resultTpl, \n");
	buffer.append("\t\tminChars : 1, \n");
	buffer.append("\t\tframe : true, \n");
	buffer.append("\t\ttpl : resultTpl, \n");
	buffer.append("\t\tonSelect : function(record, index) { \n");
	buffer.append("\t\t\tvar params=record.get(\"value\"); \n");
	buffer.append("\t\t\tdocument.getElementById(\"AWS_DICTIONRY\").value = params; \n");
	buffer.append("\t\t\tthis.collapse(); }\n");
	buffer.append("\t}); \n");
	buffer.append("\t}); \n");
	return buffer.toString();
    }

    private boolean isExists(String dictName) {
	File file = new File("./dictionary/" + dictName);
	return file.exists();
    }

}