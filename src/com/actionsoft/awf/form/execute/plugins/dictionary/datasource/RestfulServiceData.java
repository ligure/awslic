package com.actionsoft.awf.form.execute.plugins.dictionary.datasource;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpMethod;
import org.dom4j.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.dictionary.DictionaryObject;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.DataResult;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.cc.Adapter;
import com.actionsoft.cc.CCUtil;
import com.actionsoft.cc.cache.CPCache;
import com.actionsoft.cc.connector.http.HTTPProfile;
import com.actionsoft.cc.model.CPModel;
import com.actionsoft.i18n.I18nRes;

public class RestfulServiceData extends DataAbs implements
	DataAdapterConfigInterface {
    private String httpUuid;
    private Element parameters;
    private Element uuidEl;

    public Element getUuid() {
	return this.uuidEl;
    }

    public void setUuid(Element httpUuid) {
	this.uuidEl = httpUuid;
	this.httpUuid = httpUuid.getTextTrim();
    }

    public Element getParameters() {
	return this.parameters;
    }

    public void setParameters(Element parameters) {
	this.parameters = parameters;
    }

    public String getName() {
	return "<option value='" + RestfulServiceData.class.getSimpleName()
		+ ":::" + this.httpUuid + "'>"
		+ CPCache.getModel(this.httpUuid)._name + "</option>";
    }

    public DataResult queryChoice(DictionaryObject dictionaryObject)
	    throws Exception {
	return null;
    }

    private String prepare(DictionaryObject dictionaryObject, String v) {
	if (v == null || "".equals(v)) {
	    return "";
	}

	if (dictionaryObject != null) {
	    for (Iterator it = dictionaryObject.getWebFormValues().keySet()
		    .iterator(); it.hasNext();) {
		String key = (String) it.next();
		String value = dictionaryObject.getWebFormValue(key);
		v = Pattern.compile("\\$getForm\\(" + key + "\\)", 2)
			.matcher(v).replaceAll(value);
	    }

	    if (dictionaryObject.getGridRowData() != null) {
		JSONObject jo = JSONObject.fromObject(dictionaryObject
			.getGridRowData());
		for (Iterator it = jo.keySet().iterator(); it.hasNext();) {
		    String key = (String) it.next();
		    String value = jo.getString(key);
		    v = Pattern.compile("\\$getGrid\\(" + key + "\\)", 2)
			    .matcher(v).replaceAll(value);
		}
	    }
	}

	return v;
    }

    public DataResult queryData(DictionaryObject dictionaryObject,
	    String dbFilter) throws Exception {
	DataReaderConfig cfg = initReaderConfig(getDictionaryModel()._sql
		.trim());
	Properties p = new Properties();
	if (this.parameters != null) {
	    Iterator it = this.parameters.elementIterator();
	    RuntimeFormManager rfm = new RuntimeFormManager(
		    dictionaryObject.getContext());
	    while (it.hasNext()) {
		Element el = (Element) it.next();
		String v = prepare(dictionaryObject, el.getTextTrim());
		p.put(el.attributeValue("key"), rfm.convertMacrosValue(v));
	    }
	}

	p.put(UserContext.class, dictionaryObject.getContext());
	p.put(cfg.getPageNowProperty(),
		Integer.toString(dictionaryObject.getPageNow()));
	p.put(cfg.getFilterProperty(), dbFilter);
	p.put(cfg.getPageSizeProperty(), getDictionaryModel()._line);

	Adapter.HTTP http = Adapter.HTTP.binding(this.httpUuid);
	http.setParam(p);
	HttpMethod method = http.getMethod();
	http.executeMethod(method);
	if (method.getStatusCode() == 200) {
	    String s = method.getResponseBodyAsString();
	    if (cfg.getDataType().equalsIgnoreCase("xml")) {
		DocumentBuilderFactory df = DocumentBuilderFactory
			.newInstance();
		df.setNamespaceAware(true);
		InputSource is = new InputSource(new StringReader(s));
		Document doc = df.newDocumentBuilder().parse(is);
		return parseXMLResult(doc.getDocumentElement(), cfg);
	    }
	    return parseJsonResult(s, cfg, dbFilter);
	}
	throw new Exception("数据字典:" + getDictionaryModel()._title + ",cc http["
		+ this.httpUuid + "]连接失败,code：" + method.getStatusCode()
		+ ",body：" + method.getResponseBodyAsString());
    }

    public DataResult querySourceField() throws Exception {
	return null;
    }

    public String getDataAdapterJS() {
	StringBuilder sb = new StringBuilder();
	sb.append("\t\tif(params[0]=='" + super.getClass().getSimpleName()
		+ "'){\n");
	sb.append(" \t\tvar s = '<dataAdapter class = \""
		+ super.getClass().getName() + "\">" + "';\n");
	sb.append("\t\t\ts+='<property name=\"uuid\">{0}</property>';\n");
	sb.append("\t\t\ts+='<property name=\"parameters\">{1}</property>';\n");
	sb.append("\t\t\ts+='</dataAdapter>';\n");
	sb.append("\t\t\treturn String.format(s,params[1],params[2]);\n");
	sb.append("\t \t }\n");
	return sb.toString();
    }

    public String getOptionsHtml(String type) {
	StringBuilder sb = new StringBuilder();
	sb.append("<optgroup label=\"CC HTTP" + I18nRes.findValue("数据源")
		+ "\">");

	Hashtable h = CPCache
		.getListByType(HTTPProfile.getInstance().getType());
	for (int i = 0; i < h.size(); ++i) {
	    CPModel cp = (CPModel) h.get(Integer.valueOf(i));
	    sb.append("<option value='" + super.getClass().getSimpleName()
		    + ":::" + cp._uuid + "'>" + CCUtil.getName(cp)
		    + "</option>");
	}

	return sb.toString();
    }
}