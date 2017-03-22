package com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.DictionaryModel;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.FieldModel;

public class JSONArrayResult extends DataResult {
    private JSONArray ja;
    private int i = -1;
    private DictionaryModel dictionaryModel;
    private String filter;
    private boolean filterLocal = true;

    public JSONArrayResult(JSONArray ja) {
	this.ja = ja;
    }

    public DictionaryModel getDictionaryModel() {
	return this.dictionaryModel;
    }

    public void setDictionaryModel(DictionaryModel dictionaryModel) {
	this.dictionaryModel = dictionaryModel;
	String sql = dictionaryModel._sql.trim();
	if (sql.startsWith("{") && sql.endsWith("}")) {
	    try {
		JSONObject jo = JSONObject.fromObject(sql);
		this.filterLocal = jo.containsKey("filterLocal") ? jo
			.getBoolean("filterLocal") : true;
	    } catch (Exception exception) {
	    }
	}
    }

    public String getFilter() {
	return this.filter;
    }

    public void setFilter(String filter) {
	this.filter = filter;
    }

    public void close() {
    }

    public Object getFiledValue(FieldModel fm) throws Exception {
	return getFiledValue(fm.getName());
    }

    public Object getFiledValue(String field) throws Exception {
	JSONObject jo = this.ja.getJSONObject(this.i);
	if (jo != null && jo.containsKey(field)) {
	    return jo.getString(field);
	}
	return "";
    }

    public boolean next() {
	while (this.ja != null && this.ja.size() > 0 && this.i < this.ja.size()) {
	    if (findNext()) {
		return true;
	    }
	}
	return false;
    }

    private boolean findNext() {
	this.i += 1;
	if (this.ja == null || this.ja.size() == 0 || this.i >= this.ja.size()) {
	    return false;
	}
	if (this.dictionaryModel != null && this.filter != null
		&& !"".equals(this.filter) && this.filterLocal) {
	    int i = 0;
	    while (i < getDictionaryModel()._fields.size()) {
		FieldModel model = (FieldModel) getDictionaryModel()._fields
			.get(new Integer(i));
		if (model.getFilter().equals("true")) {
		    try {
			String obj = (String) getFiledValue(model.getName());
			if (obj != null && !"".equals(obj)
				&& obj.indexOf(this.filter) != -1)
			    return true;
		    } catch (Exception localException) {
		    }
		}
		++i;
	    }
	    return false;
	}
	return true;
    }
}