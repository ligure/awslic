package com.actionsoft.awf.form.execute.plugins.dictionary;

import java.lang.reflect.Constructor;
import java.util.Vector;

import com.actionsoft.awf.form.execute.plugins.dictionary.extend1.GridDictionary;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.ClassReflect;
import com.actionsoft.awf.util.UnsyncVector;
import com.actionsoft.awf.util.UtilString;

public class DictionaryLoader {
    public static String loaderDictionary(UserContext userContext,
	    String rtClass, String dbFilter, int pageNow, int instanceId,
	    int taskId, String xmlFile, String bindValue, String viewType,
	    String meId, String formType) {
	if (rtClass.equals("")) {
	    return "该实现该字典的Java类没有发现或尚未注册到AWS平台中!";
	}
	String cn = GridDictionary.class.getName();
	if (rtClass.indexOf(".dictionary.") != -1) {
	    rtClass = cn.substring(0, cn.indexOf(".dictionary."))
		    + rtClass.substring(rtClass.indexOf(".dictionary."));
	}
	Constructor cons = null;
	Class[] parameterTypes = { UserContext.class };
	String cRtClass = rtClass;
	Vector rtClassList = new UnsyncVector();
	Vector xmlFileType = new UnsyncVector();
	if (cRtClass.indexOf("|") == -1) {
	    rtClassList.add(0, cRtClass);
	} else {
	    UtilString us = new UtilString(cRtClass);
	    rtClassList = us.split("|");
	}
	try {
	    cons = ClassReflect.getConstructor((String) rtClassList.get(0),
		    parameterTypes);
	} catch (Exception ce) {
	    ce.printStackTrace();
	    return "字典类[" + rtClass + "]加载失败!<br>" + ce.toString();
	}
	if (cons != null) {
	    Object[] initargs = { userContext };
	    try {
		DictionaryObject dictionaryObject = (DictionaryObject) cons
			.newInstance(initargs);
		dictionaryObject.setWebFormData(bindValue);

		dictionaryObject.setRefId(Integer.parseInt(meId.replace(
			"__eol____eol____eol__<script>", "")));
		try {
		    dictionaryObject.setFormType(Integer.parseInt(formType));
		} catch (Exception localException1) {
		}
		if (xmlFile.indexOf("|") == -1 || xmlFile.indexOf("|清空") > 0) {
		    xmlFile = xmlFile.replace("|清空", "");
		    return dictionaryObject.getMainPage(rtClassList, dbFilter,
			    pageNow, instanceId, taskId, xmlFile, viewType);
		}
		UtilString us = new UtilString(xmlFile);
		xmlFileType = us.split("|");
		if (((String) xmlFileType.get(1)).equals("多选")) {
		    int separatedType = 0;
		    try {
			separatedType = Integer.parseInt(xmlFileType.get(2)
				.toString());
		    } catch (Exception e) {
			separatedType = 0;
		    }
		    return dictionaryObject.getMuiltDictFramePage(rtClass,
			    dbFilter, pageNow, instanceId, taskId,
			    (String) xmlFileType.get(0), viewType,
			    separatedType);
		}
		if (((String) xmlFileType.get(1)).indexOf("意见") != -1)
		    return dictionaryObject.getHelpFramePage(rtClass, dbFilter,
			    pageNow, instanceId, taskId, xmlFileType);
		if (((String) xmlFileType.get(1)).indexOf("子表录入") != -1)
		    return dictionaryObject.getCheckMainPage(rtClass, dbFilter,
			    pageNow, instanceId, taskId, xmlFile, false);
	    } catch (Exception ee) {
		ee.printStackTrace(System.err);
		return "类方法调用发生错误！<br>" + ee.toString();
	    }
	}
	return "ClassLoader说类[" + rtClass + "]构造失败，改类可能没有继承DictionaryObject类!";
    }

    public static String loaderDictionary_clear(UserContext userContext,
	    String rtClass, int instanceId, int taskId, String xmlFile) {
	Constructor cons = null;
	Class[] parameterTypes = { UserContext.class };
	String cRtClass = rtClass;
	Vector rtClassList = new UnsyncVector();
	Vector xmlFileType = new UnsyncVector();
	if (cRtClass.indexOf("|") == -1) {
	    rtClassList.add(0, cRtClass);
	} else {
	    UtilString us = new UtilString(cRtClass);
	    rtClassList = us.split("|");
	}
	try {
	    cons = ClassReflect.getConstructor((String) rtClassList.get(0),
		    parameterTypes);
	} catch (ClassNotFoundException ce) {
	    return "ClassLoader说类[" + rtClass + "]没有找到!<br>" + ce.toString();
	} catch (NoSuchMethodException ne) {
	    return "ClassLoader说类[" + rtClass + "]构造方法不匹配!<br>" + ne.toString();
	}
	if (cons != null) {
	    Object[] initargs = { userContext };
	    try {
		DictionaryObject dictionaryObject = (DictionaryObject) cons
			.newInstance(initargs);
		if (xmlFile.indexOf("|") == -1) {
		    return dictionaryObject.getClearJavascriptFunction(
			    rtClassList, instanceId, taskId, xmlFile);
		}
		return "";
	    } catch (Exception ee) {
		ee.printStackTrace(System.err);
		return "类方法调用发生错误！<br>" + ee.toString();
	    }
	}
	return "ClassLoader说类[" + rtClass + "]构造失败，改类可能没有继承DictionaryObject类!";
    }
}