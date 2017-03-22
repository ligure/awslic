package com.actionsoft.application.server.socketcommand;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

/**
 * 
 * @description 平台自带类修改，所使用的常量配置
 * @version 1.0
 * @author wangaz
 * @update 2014年9月16日 上午11:02:08
 */
public class ApplictionUtil {
    
    private static long modified;

    private static Properties p = new Properties();

    public static String getProperty(String propertyName) {
	File f = new File("plugs" + File.separator + "LETV.properties");
	if (f.exists()) {
	    long lastModified = f.lastModified();
	    if (lastModified > modified) {
		try {
		    Reader r = new FileReader(f);
		    p.load(r);
		    r.close();
		    r = null;
		} catch (Exception e) {
		    e.printStackTrace();
		}
		modified = lastModified;
	    }
	}
	return p.getProperty(propertyName, "");
    }
    
}
