package com.actionsoft.awf.form.execute.plugins.dictionary;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.component.FormUIFactory;
import com.actionsoft.awf.form.execute.plugins.component.UIComponentInterface;
import com.actionsoft.awf.form.execute.plugins.component.UIUtil;
import com.actionsoft.awf.form.execute.plugins.component.web.UICascadeUtil;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.DictionaryModel;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.FieldModel;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.htmlframework.html.Button;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;

public abstract class DictionaryObject extends ActionsoftWeb {
    private HashMap _webFormData = new HashMap();
    private String _bindValue = "";
    private Vector _paraList = null;
    private int _pageNow = 1;
    protected int _instanceId;
    protected int _taskId;
    private String _xmlFile = "";
    private String _htmlModel = "DictionaryMain.htm";
    private String _dbFilter = "";
    private String _workflowId = "";
    private String _workflowStepId = "";
    private String _viewType = "";
    private int refId;
    private int formType;
    private static final String _PREFX = "_prefix:";
    private String _gridRowData;

    public String getWebFormValue(String name) {
	Object obj = this._webFormData.get(name);
	if (obj != null) {
	    return (String) obj;
	}
	return "";
    }

    public String getGridRowData() {
	return this._gridRowData;
    }

    public int getFormType() {
	return this.formType;
    }

    public void setFormType(int formType) {
	this.formType = formType;
    }

    public int getRefId() {
	return this.refId;
    }

    public void setRefId(int meId) {
	this.refId = meId;
    }

    public HashMap getWebFormValues() {
	return this._webFormData;
    }

    public void setWebFormData(String bindValue) {
	this._webFormData = new HashMap();
	this._bindValue = bindValue;
	if (bindValue.indexOf("_prefix:") != -1) {
	    this._gridRowData = bindValue.substring(bindValue
		    .indexOf("_prefix:") + "_prefix:".length());
	    bindValue = bindValue.substring(0, bindValue.indexOf("_prefix:"));
	}

	bindValue = bindValue.trim();
	String reg = "_(\\w+)\\{(.*?)\\}";
	Matcher m = Pattern.compile(reg).matcher(bindValue);
	while (m.find()) {
	    String fn = m.group(1);
	    int li = bindValue.lastIndexOf("}" + fn + "_");
	    if (li != -1)
		this._webFormData.put(fn, bindValue.substring(m.start(2), li));
	}
    }

    public DictionaryObject(UserContext userContext) {
	super(userContext);
    }

    public String getHtmlModel() {
	return this._htmlModel;
    }

    public String getWorkFlowId() {
	return this._workflowId;
    }

    public String getWorkFlowStepId() {
	return this._workflowStepId;
    }

    public void setWorkFlowId(String workFlowId) {
	this._workflowId = workFlowId;
    }

    public void setWorkFlowStepId(String workFlowStepId) {
	this._workflowStepId = workFlowStepId;
    }

    public void setHtmlModel(String htmlModel) {
	this._htmlModel = htmlModel;
    }

    public String getXmlFile() {
	return this._xmlFile;
    }

    public void setXmlFile(String xmlFile) {
	this._xmlFile = xmlFile;
    }

    public void setViewType(String viewType) {
	this._viewType = viewType;
    }

    public String getViewType() {
	return this._viewType;
    }

    public int getPageNow() {
	return this._pageNow;
    }

    public void setPageNow(int pageNow) {
	this._pageNow = pageNow;
    }

    public void setInstanceId(int instanceId) {
	this._instanceId = instanceId;
    }

    public int getInstanceId() {
	return this._instanceId;
    }

    public void setTaskId(int taskId) {
	this._taskId = taskId;
    }

    public int getTaskId() {
	return this._taskId;
    }

    public void setParaList(Vector paraList) {
	this._paraList = paraList;
    }

    public Vector getParaList() {
	return this._paraList;
    }

    public void setDbFilter(String dbFilter) {
	this._dbFilter = dbFilter;
    }

    public String getDbFilter() {
	return this._dbFilter;
    }

    public String getMainPage(Vector rtClassParaList, String dbFilter,
	    int pageNow, int instanceId, int taskId, String xmlFile,
	    String viewType) {
	setPageNow(pageNow);
	setInstanceId(instanceId);
	setTaskId(taskId);
	setXmlFile(xmlFile);
	setViewType(viewType);

	Hashtable hashTags = new UnsyncHashtable();
	String rtClassName = "";
	for (int i = 0; i < rtClassParaList.size(); ++i) {
	    rtClassName = rtClassName + (String) rtClassParaList.get(i) + "|";
	}
	this._paraList = rtClassParaList;
	this._dbFilter = dbFilter;

	String content = DIYDataGrild(dbFilter);
	String nDbFilter = "";
	String choiceValue = "";
	String search = "";
	String choice = "";
	if (dbFilter.indexOf("^^^") == 0) {
	    Vector dbFilterValue = new UnsyncVector();
	    UtilString us = new UtilString(dbFilter);
	    dbFilterValue = us.split("^^^");
	    nDbFilter = (String) dbFilterValue.get(0);
	    choiceValue = (String) dbFilterValue.get(1);
	    search = "<input type='text' style='display:none' name='dbFilter'  class ='actionsoftInput' size=15 value='"
		    + dbFilter
		    + "'>&nbsp;"
		    + new Button("<I18N#刷 新>", "refreshMe()") + "&nbsp;";
	    choice = DIYChoiceArea();
	} else if (dbFilter.indexOf("~~~~") == 0) {
	    search = "<input type='text' style='display:none' name='dbFilter'  class ='actionsoftInput' size=15 value='"
		    + dbFilter
		    + "'>&nbsp;"
		    + new Button("<I18N#刷 新>", "refreshMe()") + "&nbsp;";
	} else {
	    nDbFilter = dbFilter;
	    search = "<I18N#请输入模糊值>：<input type='text' onkeypress=\"if (window.event.keyCode==13){refreshMe();return false;}\" name='dbFilter'  class ='actionsoftInput' size=15 value='"
		    + nDbFilter
		    + "'>&nbsp;"
		    + "&nbsp;"
		    + new Button("<I18N#筛 选>", "refreshMe()") + "&nbsp;";
	    choice = DIYChoiceArea();
	}

	String checkHidden = getCheckHidden();
	if (viewType.equals("")) {
	    hashTags.put("button1", new Button("<I18N#取消>", "window.close()"));
	    hashTags.put("button2", new Button("<I18N#确 认>", "getParameter(1)"));
	} else {
	    hashTags.put("button1", "");
	    hashTags.put("button2", "");
	}
	hashTags.put("content", content);
	hashTags.put("sid", getSIDFlag());
	hashTags.put("page_title", "<I18N#" + DIYTitle() + ">");
	hashTags.put("checkHidden", checkHidden);
	hashTags.put("pageNow", Integer.toString(pageNow));
	hashTags.put("id", Integer.toString(instanceId));
	hashTags.put("task_id", Integer.toString(taskId));
	hashTags.put("js", DIYJavaScript());
	hashTags.put("bindValue", this._bindValue);
	hashTags.put("xmlFile", xmlFile);
	hashTags.put("viewType", this._viewType);
	hashTags.put("RTClass",
		rtClassName.substring(0, rtClassName.length() - 1));
	hashTags.put("search", search);
	hashTags.put("choice", choice);
	hashTags.put("choiceHidden",
		"<input type=hidden name=choiceHidden value='" + choiceValue
			+ "'>");
	return getHtmlPage(this._htmlModel, hashTags);
    }

    public String getCheckHidden() {
	StringBuilder html = new StringBuilder();
	html.append("<input type=hidden name=p1> ").append("\n");
	html.append("<input type=hidden name=p2> ").append("\n");
	html.append("<input type=hidden name=p3> ").append("\n");
	html.append("<input type=hidden name=p4> ").append("\n");
	html.append("<input type=hidden name=p5> ").append("\n");
	html.append("<input type=hidden name=p6> ").append("\n");
	html.append("<input type=hidden name=p7> ").append("\n");
	html.append("<input type=hidden name=p8> ").append("\n");
	html.append("<input type=hidden name=p9> ").append("\n");
	html.append("<input type=hidden name=p10>").append("\n");
	html.append("<input type=hidden name=p11>").append("\n");
	html.append("<input type=hidden name=p12>").append("\n");
	html.append("<input type=hidden name=p13>").append("\n");
	html.append("<input type=hidden name=p14>").append("\n");
	html.append("<input type=hidden name=p15>").append("\n");
	html.append("<input type=hidden name=p16>").append("\n");
	html.append("<input type=hidden name=p17>").append("\n");
	html.append("<input type=hidden name=p18>").append("\n");
	html.append("<input type=hidden name=p19>").append("\n");
	html.append("<input type=hidden name=p20>").append("\n");
	html.append("<input type=hidden name=p21>").append("\n");
	html.append("<input type=hidden name=p22>").append("\n");
	html.append("<input type=hidden name=p23>").append("\n");
	html.append("<input type=hidden name=p24>").append("\n");
	html.append("<input type=hidden name=p25>").append("\n");
	html.append("<input type=hidden name=p26>").append("\n");
	html.append("<input type=hidden name=p27>").append("\n");
	html.append("<input type=hidden name=p28>").append("\n");
	html.append("<input type=hidden name=p29>").append("\n");

	return html.toString();
    }

    public String getCheckMainPage(String rtClass, String dbFilter,
	    int pageNow, int instanceId, int taskId, String xmlFile,
	    boolean reflese) {
	setPageNow(pageNow);
	setInstanceId(instanceId);

	UtilString us2 = new UtilString(xmlFile);
	Vector xmlFileType = us2.split("|");
	setTaskId(taskId);
	setXmlFile((String) xmlFileType.get(0));

	Hashtable hashTags = new UnsyncHashtable();

	this._dbFilter = dbFilter;
	hashTags.put("button1", new Button("<I18N#取 消>", "window.close();"));
	hashTags.put("button2", new Button("<I18N#确认选择>",
		"execMyCommand(frmMain,'Dictionary_Insert_SubForm')"));
	hashTags.put("content", DIYDataGrild(dbFilter));

	String nDbFilter = "";
	String choiceValue = "";
	String search = "";
	String choice = "";
	if (dbFilter.indexOf("^^^") == 0) {
	    Vector dbFilterValue = new UnsyncVector();
	    UtilString us = new UtilString(dbFilter);
	    dbFilterValue = us.split("^^^");
	    nDbFilter = (String) dbFilterValue.get(0);
	    choiceValue = (String) dbFilterValue.get(1);
	    search = "<input type='text' style='display:none' name='dbFilter'  class ='actionsoftInput' size=15 value='"
		    + dbFilter
		    + "'>&nbsp;"
		    + new Button("<I18N#刷 新>", "refreshMe()") + "&nbsp;";
	    choice = DIYChoiceArea();
	} else if (dbFilter.indexOf("~~~~") == 0) {
	    search = "<input type='text' style='display:none' name='dbFilter'  class ='actionsoftInput' size=15 value='"
		    + dbFilter
		    + "'>&nbsp;"
		    + new Button("<I18N#刷 新>", "refreshMe()") + "&nbsp;";
	} else {
	    DictionaryModel dictionaryModel = DictionaryUtil
		    .fromFile(xmlFileType.get(0).toString());
	    if (DictionaryUtil.isFullSearch(dictionaryModel)) {
		nDbFilter = dbFilter;
		search = "<I18N#请输入模糊值>：<input type='text' name='dbFilter' onkeypress=\"if (window.event.keyCode==13){refreshMe();return false;}\" class ='actionsoftInput' size=15 value='"
			+ nDbFilter
			+ "'>&nbsp;"
			+ new Button("<I18N#筛 选>", "refreshMe()")
			+ "&nbsp;"
			+ new Button("<I18N#刷 新>", "refreshMe()") + "&nbsp;";
		choice = DIYChoiceArea();
	    } else {
		search = "<input type='text' style='display:none' name='dbFilter'  class ='actionsoftInput' size=15 value='"
			+ dbFilter
			+ "'>&nbsp;"
			+ new Button("<I18N#刷 新>", "refreshMe()") + "&nbsp;";
	    }
	}

	hashTags.put("page_title", DIYTitle());
	hashTags.put("sid", getSIDFlag());
	hashTags.put("pageNow", Integer.toString(pageNow));
	hashTags.put("id", Integer.toString(instanceId));
	hashTags.put("instanceId", Integer.toString(instanceId));
	hashTags.put("task_id", Integer.toString(taskId));
	hashTags.put("js", DIYJavaScript());
	hashTags.put("xmlFile", xmlFile);
	hashTags.put("RTClass", rtClass);
	hashTags.put("search", search);
	hashTags.put("bindValue", this._bindValue);
	hashTags.put("meId", Integer.toString(getRefId()));
	hashTags.put("formType", Integer.toString(getFormType()));
	hashTags.put("choice", DIYChoiceArea());
	hashTags.put("workflowId", "<input type=hidden name=workflowId value='"
		+ (String) xmlFileType.get(2) + "'>");
	hashTags.put("workflowStepId",
		"<input type=hidden name=workflowStepId value='"
			+ (String) xmlFileType.get(3) + "'>");
	hashTags.put("choiceHidden",
		"<input type=hidden name=choiceHidden value='" + choiceValue
			+ "'>");
	if (reflese)
	    hashTags.put("reloadScript", "<script>opener.saveForm();</script>");
	else {
	    hashTags.put("reloadScript", "");
	}
	return getHtmlPage("DictionaryCheckMain.htm", hashTags);
    }

    public String getMuiltDictFramePage(String rtClass, String dbFilter,
	    int pageNow, int instanceId, int taskId, String xmlFile,
	    String viewType, int separatedType) {
	setPageNow(pageNow);
	setInstanceId(instanceId);
	setTaskId(taskId);
	setXmlFile(xmlFile);
	setDbFilter(dbFilter);
	String cgi = "./login.wf";
	String sid = getContext().getSessionId();
	Hashtable hashTags = new UnsyncHashtable(4);

	hashTags.put("target1", cgi + "?sid=" + enc(sid)
		+ "&cmd=Dictionary_Action_Open&rtClass=" + rtClass
		+ "&dbFilter=" + dbFilter + "&pageNow=" + pageNow
		+ "&instanceId=" + instanceId + "&taskId=" + taskId
		+ "&xmlFile=" + xmlFile + "&separatedType=" + separatedType
		+ "&viewType=" + viewType + "&isMuilt=1&bindValue="
		+ enc(this._bindValue));

	hashTags.put("target3", cgi + "?sid=" + enc(sid)
		+ "&cmd=Dictionary_List_Open&rtClass=" + rtClass + "&dbFilter="
		+ dbFilter + "&pageNow=" + pageNow + "&instanceId="
		+ instanceId + "&taskId=" + taskId + "&xmlFile=" + xmlFile
		+ "&separatedType=" + separatedType + "&viewType=" + viewType
		+ "&isMuilt=1&bindValue=" + enc(this._bindValue));

	hashTags.put("target4", cgi + "?sid=" + enc(sid)
		+ "&cmd=Dictionary_Target_Open&viewType=" + viewType);

	return RepleaseKey
		.replace(HtmlModelFactory.getHtmlModel("Dictionary_frame.htm"),
			hashTags);
    }

    protected String enc(String s) {
	try {
	    return URLEncoder.encode(s, "UTF8");
	} catch (Exception localException) {
	}
	return s;
    }

    public String getHelpFramePage(String rtClass, String dbFilter,
	    int pageNow, int instanceId, int taskId, Vector xmlFileType) {
	setPageNow(pageNow);
	setInstanceId(instanceId);
	setTaskId(taskId);
	setXmlFile((String) xmlFileType.get(0));
	setDbFilter(dbFilter);
	String cgi = "./login.wf";
	String sid = getContext().getSessionId();
	Hashtable hashTags = new UnsyncHashtable(4);

	hashTags.put("target1", cgi + "?sid=" + enc(sid)
		+ "&cmd=Dictionary_Help_Action_Open&rtClass=" + rtClass
		+ "&dbFilter=" + dbFilter + "&pageNow=" + pageNow
		+ "&instanceId=" + instanceId + "&taskId=" + taskId
		+ "&xmlFile=" + (String) xmlFileType.get(0));

	hashTags.put("target3", cgi + "?sid=" + enc(sid)
		+ "&cmd=Dictionary_Help_List_Open&rtClass=" + rtClass
		+ "&dbFilter=" + dbFilter + "&pageNow=" + pageNow
		+ "&instanceId=" + instanceId + "&taskId=" + taskId
		+ "&xmlFile=" + (String) xmlFileType.get(0) + "&xmlType="
		+ (String) xmlFileType.get(1));

	hashTags.put("target4", cgi + "?sid=" + enc(sid)
		+ "&cmd=Dictionary_Help_Target_Open");

	return RepleaseKey
		.replace(HtmlModelFactory.getHtmlModel("Dictionary_frame.htm"),
			hashTags);
    }

    public abstract String DIYDataGrild(String paramString);

    public String DIYChoiceArea() {
	return "";
    }

    public abstract String DIYJavaScript();

    public abstract String DIYTitle();

    public Vector getParameters() {
	return this._paraList;
    }

    public String getUI(String fieldType, String fieldName, String fieldValue,
	    MetaDataMapModel metaDataMapModel, Map condition) {
	StringBuilder ui = new StringBuilder();
	String lang = "";
	if ("单行".equals(fieldType)) {
	    Hashtable params = new UnsyncHashtable();
	    params.put("me", getContext());
	    params.put("runtimeFormManager", new RuntimeFormManager(
		    getContext()));
	    params.put("isVerifySQL", "false");
	    metaDataMapModel.setFieldName(fieldName);
	    UIComponentInterface UIComponent = FormUIFactory.getUIInstance(
		    metaDataMapModel, fieldValue);
	    String UICode = UIComponent.getModifyHtmlDefine(params);
	    ui.append(UICode);
	} else if (fieldType.equals("日期")) {
	    if (getContext().getLanguage().equals("cn"))
		lang = "{lang:'zh-cn'}";
	    else if (getContext().getLanguage().equals("en"))
		lang = "{lang:'en'}";
	    else if (getContext().getLanguage().equals("big5")) {
		lang = "{lang:'zh-tw'}";
	    }
	    ui.append("<input type='text' name='")
		    .append(fieldName)
		    .append("'  onClick=\"WdatePicker(" + lang
			    + ")\" class ='actionsoftInput' size=16 value='"
			    + fieldValue + "'").append(">");
	} else if (fieldType.equals("日期时间")) {
	    if (getContext().getLanguage().equals("cn"))
		lang = "lang:'zh-cn'";
	    else if (getContext().getLanguage().equals("en"))
		lang = "lang:'en'";
	    else if (getContext().getLanguage().equals("big5")) {
		lang = "lang:'zh-tw'";
	    }
	    ui.append(
		    "<input type='text' title='<I18N#aws.common.digger.exec_点击选择日期和时间>' name='")
		    .append(fieldName)
		    .append("'  onClick=\"WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',"
			    + lang
			    + "})\" class ='actionsoftInput' size=16 value='"
			    + fieldValue + "'>");
	} else if (fieldType.equals("时间")) {
	    if (getContext().getLanguage().equals("cn"))
		lang = "lang:'zh-cn'";
	    else if (getContext().getLanguage().equals("en"))
		lang = "lang:'en'";
	    else if (getContext().getLanguage().equals("big5")) {
		lang = "lang:'zh-tw'";
	    }
	    ui.append(
		    "<input type='text'  title='<I18N#aws.common.digger.exec_点击选择时间>' name='")
		    .append(fieldName)
		    .append("'  onClick=\"WdatePicker({dateFmt:'HH:mm:ss',"
			    + lang
			    + "})\" class ='actionsoftInput' size=16 value='"
			    + fieldValue + "'>");
	} else if (fieldType.equals("数值")) {
	    ui.append("<input type='text'   name='")
		    .append(fieldName)
		    .append("'  onblur='checkNumber(this)' class ='actionsoftInput' size=16 value='"
			    + fieldValue + "'>");
	    ui.append("<script>\n");
	    ui.append("function checkNumber(obj){ \n");
	    ui.append("var tst = /^\\d+$/.test(obj.value);\n");
	    ui.append("if (!tst){\n");
	    ui.append("alert('【")
		    .append(I18nRes.findValue(getContext().getLanguage(),
			    metaDataMapModel.getFieldTitle()))
		    .append("】"
			    + I18nRes.findValue(getContext().getLanguage(),
				    "aws.common.digger.exec_未通过合法性校验")
			    + "');\n");
	    ui.append("return false;}}\n");
	    ui.append("</script>");
	} else if (UIUtil.runOuter(fieldType)) {
	    Hashtable params = new UnsyncHashtable();
	    params.put("me", getContext());
	    params.put("runtimeFormManager", new RuntimeFormManager(
		    getContext()));
	    params.put("cascadeType", "XMLDICT");
	    params.put("cascadeCondition", condition);
	    if (!"".equals(getDbFilter())) {
		params.put("DBFilter", getDbFilter());
	    }

	    params.put("isVerifySQL", "false");
	    params.put("fileName", getXmlFile());
	    UIComponentInterface UIComponent = FormUIFactory.getUIInstance(
		    metaDataMapModel, fieldValue);
	    String UICode = UIComponent.getModifyHtmlDefine(params);
	    ui.append(UICode);
	} else {
	    ui.append("<input type='text' name='")
		    .append(fieldName)
		    .append("' class ='actionsoftInput' size=16  value='"
			    + fieldValue + "'>");
	}

	ui.append(UICascadeUtil
		.injectDictCascsdeJS(metaDataMapModel, condition));
	return ui.toString();
    }

    public String getClearJavascriptFunction(Vector rtClassList,
	    int instanceId, int taskId, String xmlFile) {
	StringBuilder javascriptBuffer = new StringBuilder();

	if (xmlFile.length() == 0) {
	    return "";
	}
	String filePath = "./dictionary/" + xmlFile;
	if (!new File(filePath).exists()) {
	    return "";
	}

	DictionaryModel _dictionaryModel = DictionaryUtil.fromFile(xmlFile);
	Map _fields = _dictionaryModel._fields;
	for (int i = 0; i < _fields.size(); ++i) {
	    FieldModel model = (FieldModel) _fields.get(new Integer(i));
	    if (model != null && model.getTargetName() != null
		    && model.getTargetName().trim().length() > 0) {
		javascriptBuffer.append("try{\n").append("form.")
			.append(model.getTargetName()).append(".value='';\n")
			.append("}catch(e){}\n");
	    }
	}
	return javascriptBuffer.toString();
    }
}