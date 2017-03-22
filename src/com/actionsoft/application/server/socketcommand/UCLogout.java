/**
 * 
 */
package com.actionsoft.application.server.socketcommand;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.actionsoft.Util;

/**
 * @author Administrator
 *
 */
public class UCLogout {
	public void uclogout(String sessionId){
		String backurl = "http://oa.letv.test:8088/portal";
		String site = "bpm";
		String sessionid=sessionId;
		Util ut = new Util();
		String sign =ut.getMD5Str(backurl+site);
		String url = "http://sso.letv.local:20008/logout.php?backurl=http://oa.letv.test:8088/portal&site=bpm&sign="+sign+"";
		HttpClient hc = new HttpClient();
		try{
		PostMethod pm = new PostMethod(url);
		hc.executeMethod(pm);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
