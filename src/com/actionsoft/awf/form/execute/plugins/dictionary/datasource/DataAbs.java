package com.actionsoft.awf.form.execute.plugins.dictionary.datasource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.DataResult;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.JSONArrayResult;
import com.actionsoft.awf.form.execute.plugins.dictionary.datasource.result.NodeListResult;
import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.DictionaryModel;

public abstract class DataAbs implements DataFactory {
    private DictionaryModel dictionaryModel;

    public DictionaryModel getDictionaryModel() {
	return this.dictionaryModel;
    }

    public void setDictionaryModel(DictionaryModel dictionaryModel) {
	this.dictionaryModel = dictionaryModel;
    }

    DataReaderConfig initReaderConfig(String sql) throws Exception {
	DataReaderConfig cfg = new DataReaderConfig();
	cfg.read(sql);
	return cfg;
    }

    DataResult parseXMLResult(Element xml, DataReaderConfig cfg) {
	NodeList nl = xml.getElementsByTagName(cfg.getDataRecords());
	NodeListResult dr = new NodeListResult(nl);
	if (cfg.getUsePaging()) {
	    dr.setUsePaging(cfg.getUsePaging());
	    NodeList l = xml.getElementsByTagName(cfg.getTotalProperty());
	    if (l.getLength() > 0) {
		Node totalTextNode = l.item(0).getFirstChild();
		try {
		    dr.setTotal(Integer.parseInt(totalTextNode.getNodeValue()));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
	return dr;
    }

    DataResult parseJsonResult(String s, DataReaderConfig cfg, String dbFilter)
	    throws Exception {
	if (s != null) {
	    s = s.trim();
	    JSONArrayResult jr = null;
	    if (s.startsWith("[") && s.endsWith("]")) {
		JSONArray ja = JSONArray.fromObject(s);
		jr = new JSONArrayResult(ja);
		jr.setTotal(ja.size());
	    } else if (s.startsWith("{") && s.endsWith("}")) {
		JSONObject jo = JSONObject.fromObject(s);
		String errorPro = cfg.getErrorProperty();
		if (errorPro != null && !"".equals(errorPro)
			&& jo.containsKey(errorPro)
			&& jo.getString(errorPro) != null
			&& !"".equals(jo.getString(errorPro))) {
		    throw new Exception(jo.getString(errorPro));
		}
		String dataPro = cfg.getDataRecords();
		String[] keys = dataPro.split("\\.");
		JSONObject jo1 = jo;
		for (int i = 0; i < keys.length - 1; ++i) {
		    jo1 = jo1.getJSONObject(keys[i]);
		}

		JSONArray ja = jo1.getJSONArray(keys[(keys.length - 1)]);
		jr = new JSONArrayResult(ja);
		String total = cfg.getTotalProperty();
		if (total != null && !"".equals(total) && jo.containsKey(total)) {
		    jr.setTotal(jo.getInt(total));
		}

	    }
	    if (jr != null) {
		jr.setDictionaryModel(getDictionaryModel());
		jr.setFilter(dbFilter);
		jr.setUsePaging(cfg.getUsePaging());
	    }
	    return jr;
	}
	return null;
    }
}