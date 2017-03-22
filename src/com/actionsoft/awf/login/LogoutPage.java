/*
 * Copyright(C)2001-2012 Actionsoft Co.,Ltd
 * AWS(Actionsoft workflow suite) BPM(Business Process Management) PLATFORM Source code 
 * AWS is a application middleware for BPM System

  
 * 本软件工程编译的二进制文件及源码版权归北京炎黄盈动科技发展有限责任公司所有，
 * 受中国国家版权局备案及相关法律保护，未经书面法律许可，任何个人或组织都不得泄漏、
 * 传播此源码文件的全部或部分文件，不得对编译文件进行逆向工程，违者必究。

 * $$本源码是炎黄盈动最高保密级别的文件$$
 * 
 * http://www.actionsoft.com.cn
 * 
 */

package com.actionsoft.awf.login;

import java.util.Iterator;
import java.util.Map;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.session.SessionImpl;
import com.actionsoft.awf.session.cache.SessionCache;
import com.actionsoft.awf.session.model.SessionModel;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.sdk.local.level1.SessionAPI;

/**
 * 
 * 登出BPM客户端 || CONSOLE
 */
public class LogoutPage {

	//注销session，退出页面
	public String logoutAction(UserContext me,String path) {
		String locale = me.getLanguage();
		destroyAll(me);
		return RepleaseKey.replaceI18NTag(locale,HtmlModelFactory.getHtmlModel("sys_logout.htm").replace("&path&", path));
	}

/**
 * 
 * 登出BPM客户端 || CONSOLE
 */
	public void destroyAll(UserContext me) {
		try{
		SessionModel raw = (SessionModel) SessionCache.getModel(me.getSessionId());
		Map<String, SessionModel> m = SessionCache.getList();
		Iterator<SessionModel> it = m.values().iterator();
		while (it.hasNext()) {
			SessionModel model = it.next();
			if (model._UID.equals(raw._UID)) {
				new SessionImpl().destroySession(model._sessionId);
			}
		}
	}catch(Exception e){
		e.printStackTrace();	
		}
	}
	
}
