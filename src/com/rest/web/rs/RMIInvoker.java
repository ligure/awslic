package com.rest.web.rs;

import gnu.cajo.invoke.Remote;
import gnu.cajo.invoke.Remote_Stub;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.cxf.common.logging.LogUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class RMIInvoker {

    private static String RMIIP = "127.0.0.1";
    private static String RMIPORT = "1198";
    private static Logger LOG = LogUtils.getL7dLogger(RMIInvoker.class);

    static {
	InputStream is = Thread.currentThread().getContextClassLoader()
		.getResourceAsStream("service-config.xml");
	if (is == null) {
	    is = RMIInvoker.class.getResourceAsStream("service-config.xml");
	}
	try {
	    Document doc = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder().parse(is);
	    NodeList nl = doc.getElementsByTagName("rmi-ip");
	    if (nl != null) {
		RMIIP = ((Text) (((Element) nl.item(0)).getFirstChild()))
			.getData().trim();
	    }
	    nl = doc.getElementsByTagName("rmi-port");
	    if (nl != null) {
		RMIPORT = ((Text) (((Element) nl.item(0)).getFirstChild()))
			.getData().trim();
	    }
	} catch (Exception e) {
	    LOG.log(Level.SEVERE, "解析service-config.xml异常", e);
	}
    }

    public static Remote_Stub refStub(String token) throws Exception {
	String url = "//" + RMIIP + ":" + RMIPORT + "/" + token;
	try {
	    return (Remote_Stub) Remote.getItem(url);
	} catch (Exception e) {
	    LOG.log(Level.SEVERE, "MyLocal restful服务【" + token + "】调用异常", e);
	    throw e;
	}
    }

}
