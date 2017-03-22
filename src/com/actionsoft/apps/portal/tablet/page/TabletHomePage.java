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

package com.actionsoft.apps.portal.tablet.page;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.actionsoft.application.logging.AuditLogger;
import com.actionsoft.application.logging.model.Action;
import com.actionsoft.application.logging.model.AuditObj;
import com.actionsoft.application.logging.model.Catalog;
import com.actionsoft.application.logging.model.Channel;
import com.actionsoft.application.logging.model.Level;
import com.actionsoft.application.portal.cache.SysPortalCache;
import com.actionsoft.application.portal.model.SysPortalModel;
import com.actionsoft.application.portal.navigation.cache.NavigationDirectoryCache;
import com.actionsoft.application.portal.navigation.cache.NavigationFunctionCache;
import com.actionsoft.application.portal.navigation.cache.NavigationSystemCache;
import com.actionsoft.application.portal.navigation.model.NavigationDirectoryModel;
import com.actionsoft.application.portal.navigation.model.NavigationFunctionModel;
import com.actionsoft.application.portal.navigation.model.NavigationSystemModel;
import com.actionsoft.application.portal.navigation.util.NavUtil;
import com.actionsoft.application.portal.personal.Awake;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.apps.portal.tablet.config.TabletConfig;
import com.actionsoft.apps.portal.tablet.store.constant.TabletConstant;
import com.actionsoft.apps.portal.tablet.store.dao.TabletUserInfo;
import com.actionsoft.apps.portal.tablet.store.dao.TabletUserInfoDaoFactory;
import com.actionsoft.apps.portal.tablet.store.model.TabletUserInfoModel;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.login.constant.LoginConst;
import com.actionsoft.awf.login.control.LoginControl;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.session.SessionImpl;
import com.actionsoft.awf.session.cache.SessionCache;
import com.actionsoft.awf.session.model.SessionModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.URLParser;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.TaskWorklistAPI;

/**
 * 平板首页
 * 
 * @author Administrator
 * 
 */
public class TabletHomePage extends ActionsoftWeb {
	protected AuditLogger logger = AuditLogger.getLogger(Channel.CLIENT, Catalog.TABLET, AuditObj.MODEL_TABLET);
	protected AuditLogger seclogger = AuditLogger.getLogger(Channel.SECURITY, Catalog.USERLOGIN, AuditObj.MODEL_TABLET);
	protected AuditLogger loggers = AuditLogger.getLogger(Channel.SECURITY, Catalog.USERFREEZE, AuditObj.MODEL_TABLET);

	public TabletHomePage(UserContext userContext) {
		super(userContext);
	}

	public TabletHomePage() {
	}

	/**
	 * 登录首页信息
	 * 
	 * @param userId
	 * @param pwd
	 * @param bip
	 * @param lang
	 * @param extInfo
	 * @return
	 */
	public String login(String userId, String pwd, String bip, String lang, String extInfo, Hashtable params) {
		Map trace = new HashMap();
		String back = I18nRes.findValue(lang, "返回");
		LoginControl login = new LoginControl(userId, pwd, bip, params);
		int i = login.check();
		trace.put(" 登录失败[login]", "登录失败");
		if (i == LoginConst.LOGIN_STATUS_PWD_ERROR) /* 口令有误 */{
			trace.put(" 错误消息[errorMes]", "口令错误!");
			seclogger.log("平板电脑流程门户_登录失败", Action.LOGIN, trace, Level.ERROR);
			String kl = I18nRes.findValue(lang, "口令错误");
			return "<div style='font-size:25px;text-align:center;'><div><a href='../t'>" + back + "</a></div><div>" + kl + "!</div></div>";
		} else if (i == LoginConst.LOGIN_STATUS_FREEZE) /* 帐户已被冻结 */{
			trace.put(" 错误消息[errorMes]", "用户已经被冻结!");
			loggers.log("平板电脑流程门户_账户冻结", Action.LOGIN, trace, Level.WARN);
			String dj = I18nRes.findValue(lang, "用户已经被冻结");
			return "<div style='font-size:25px;text-align:center;'><div><a href='../t'>" + back + "</a></div><div>" + dj + "!</div></div>";
		} else if (i == LoginConst.LOGIN_STATUS_DISENABLE) /* 帐户已被注销 */{
			trace.put(" 错误消息[errorMes]", "用户已经被注销!");
			seclogger.log("平板电脑流程门户_登录失败", Action.LOGIN, trace, Level.ERROR);
			String zx = I18nRes.findValue(lang, "该用户帐户已经被注销");
			return "<div style='font-size:25px;text-align:center;'><div><a href='../t'>" + back + "</a></div><div>" + zx + "!</div></div>";
		} else if (i == LoginConst.LOGIN_STATUS_USER_NOTFIND) {
			trace.put(" 错误消息[errorMes]", "用户名不存在!");
			seclogger.log("平板电脑流程门户_登录失败", Action.LOGIN, trace, Level.ERROR);
			String bcz = I18nRes.findValue(lang, "用户名不存在");
			return "<div style='font-size:25px;text-align:center;'><div><a href='../t'>" + back + "</a></div><div>" + bcz + "!</div></div>";
		} else if (i == LoginConst.LOGIN_STATUS_DATABASE_ERROR) /* 数据库连接失败或数据表结构异常 */{
			trace.put(" 错误消息[errorMes]", "数据库连接异常!");
			seclogger.log("平板电脑流程门户_登录失败", Action.LOGIN, trace, Level.ERROR);
			String tzgly = I18nRes.findValue(lang, "与数据库服务的连接异常，请通知系统管理员");
			return "<div style='font-size:25px;text-align:center;'><div><a href='../t'>" + back + "</a></div><div>" + tzgly + "!</div></div>";
		}

		SessionImpl mySession = new SessionImpl(Integer.parseInt(AWFConfig._awfServerConf.getSessionOnlineLife()));
		SessionModel sessionModel = mySession.registerSession(login.getAWSUID(), bip, lang, "tablet");
		UserContext user = null;
		try {
			user = new UserContext(sessionModel._sessionId, bip);
		} catch (Exception e) {
			e.printStackTrace();
		}

		TabletConfig.reload();
		TabletHomePage web = new TabletHomePage(user);
		trace.put(" 登录成功[login]", "登录成功");
		seclogger.log("平板电脑流程门户_登录成功", Action.LOGIN, trace, Level.INFO);
		initDefaultIcon(login.getAWSUID());// 初始化默认图标
		return web.getProtalHome(user);
	}

	/*
	 * 初始化默认图标
	 */
	public void initDefaultIcon(String userId) {
		String tableName = "TABLET_USERINFO";
		String USER_ID = "USERID";
		String FUNCTION_ID = "FUNCTIONID";
		String ORDER_INDEX = "ORDERINDEX";
		String TYPE = "TYPE";
		Map navigationFunctions = NavigationFunctionCache.getList();
		String sql = "";
		for (int i = 0; i < navigationFunctions.size(); i++) {
			NavigationFunctionModel functionModel = (NavigationFunctionModel) navigationFunctions.get(i);
			if (functionModel != null) {
				if (functionModel._functionUrl.equals("./login.wf?sid=<#sid>&cmd=My_WorkBoxCard")) { // 我的
					sql = "select count(*) as c from " + tableName + " where " + USER_ID + "='" + userId + "' AND " + FUNCTION_ID + "=" + functionModel._id + " AND " + TYPE + "='1'";
					// System.out.println("sql>>"+sql);
					int ii = DBSql.getInt(sql, "c");
					// System.out.println("ii>>"+ii);
					if (ii <= 0 && SecurityProxy.checkModelSecurity(userId, String.valueOf(functionModel._id))) {
						DBSql.executeUpdate("INSERT INTO TABLET_USERINFO (" + USER_ID + "," + FUNCTION_ID + "," + ORDER_INDEX + "," + TYPE + ")values('" + userId + "'," + functionModel._id + ",1,'1') ");
					}
				}
//				if (functionModel._functionUrl.equals("./login.wf?sid=<#sid>&&cmd=Email_Box_Portal")) {
//					sql = "select count(*) as c from " + tableName + " where " + USER_ID + "='" + userId + "' AND " + FUNCTION_ID + "=" + functionModel._id + "  AND " + TYPE + "='1'";
//					int ii = DBSql.getInt(sql, "c");
//					if (ii <= 0 && SecurityProxy.checkModelSecurity(userId, String.valueOf(functionModel._id))) {
//						DBSql.executeUpdate("INSERT INTO TABLET_USERINFO (" + USER_ID + "," + FUNCTION_ID + "," + ORDER_INDEX + "," + TYPE + ")values('" + userId + "'," + functionModel._id + ",2,'1') ");
//					}
//				}
//
//				if (functionModel._functionUrl.equals("./login.wf?sid=<#sid>&cmd=CmChannel_Release_Open&schemaId=-1")) { // 信息资讯
//					sql = "select count(*) as c from " + tableName + " where " + USER_ID + "='" + userId + "' AND " + FUNCTION_ID + "=" + functionModel._id + " AND " + TYPE + "='1'";
//					int ii = DBSql.getInt(sql, "c");
//					if (ii <= 0 && SecurityProxy.checkModelSecurity(userId, String.valueOf(functionModel._id))) {
//						DBSql.executeUpdate("INSERT INTO TABLET_USERINFO (" + USER_ID + "," + FUNCTION_ID + "," + ORDER_INDEX + "," + TYPE + ")values('" + userId + "'," + functionModel._id + ",3,'1') ");
//					}
//				}
//				// System.out.println(">>>>>"+functionModel._functionUrl);
//				if (functionModel._functionUrl.equals("./login.wf?sid=<#sid>&cmd=Inner_Address_Show_Company")) { // 单位通讯录
//					sql = "select count(*) as c from " + tableName + " where " + USER_ID + "='" + userId + "' AND " + FUNCTION_ID + "=" + functionModel._id + "  AND " + TYPE + "='1'";
//					int ii = DBSql.getInt(sql, "c");
//					if (ii <= 0 && SecurityProxy.checkModelSecurity(userId, String.valueOf(functionModel._id))) {
//						DBSql.executeUpdate("INSERT INTO TABLET_USERINFO (" + USER_ID + "," + FUNCTION_ID + "," + ORDER_INDEX + "," + TYPE + ")values('" + userId + "'," + functionModel._id + ",4,'1') ");
//					}
//				}
			}
		}

	}

	/**
	 **退出
	 * @param uc 
	 * 
	 * @return
	 */
	public String logout(UserContext uc) {
		// String img = MobileConfig.getConfModel().getHomePageBgImgUrl();
		Hashtable hashTags = new Hashtable();
		hashTags.put("title", I18nRes.findValue(uc.getLanguage(), "tablet_欢迎下次使用"));
		hashTags.put("ok", I18nRes.findValue(uc.getLanguage(), "tablet_您已安全退出协同办公平台"));
		hashTags.put("cxdl", I18nRes.findValue(uc.getLanguage(), "重新登录"));
		hashTags.put("logo", I18nRes.findValue(uc.getLanguage(), "tablet_logo"));
		// hashTags.put("logoUrl", img);
		
		SessionModel raw = (SessionModel) SessionCache.getModel(uc.getSessionId());
		Map<String, SessionModel> m = SessionCache.getList();
		Iterator<SessionModel> it = m.values().iterator();
		while (it.hasNext()) {
			SessionModel model = it.next();
			if (model._UID.equals(raw._UID)) {
				new SessionImpl().destroySession(model._sessionId);
			}
		}
		
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("com.actionsoft.apps.portal.tablet_Portal_Logout.htm"), hashTags);
	}

	public String getUpload() {
		Hashtable hashTags = new Hashtable(3);
		hashTags.put("flag1", super.getContext().getUID());
		hashTags.put("flag2", "0");
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("rootDir", "Photo");
		hashTags.put("upFileType", "img");
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("upFile.htm"), hashTags);
	}

	/**
	 * 返回首页信息
	 * 
	 * @return
	 */
	public String getProtalHome(UserContext user) {
		Map trace = new HashMap();

		String sessionId = user.getSessionId();
		try {
			sessionId = java.net.URLEncoder.encode(sessionId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		UserModel use = super.getContext().getUserModel();
		DepartmentModel departmentModel = super.getContext().getDepartmentModel();

		Hashtable hashTags = new Hashtable();
		hashTags.put("sid", getSIDFlag());
		hashTags.put("sessionId", sessionId);
		hashTags.put("uid", super.getContext().getUID());
		hashTags.put("userName", use.getUserName());
		String uid = super.getContext().getUID();
		String imgPhoto = getImgPhoto(sessionId, uid); // 头像
		hashTags.put("departmentName", departmentModel.getDepartmentName());
		trace.put("查询首页信息[Home information query]", "查询首页信息");
		logger.log("平板电脑流程门户", Action.SELECT, trace, Level.INFO);

		hashTags.put("id", super.getContext().getID());
		hashTags.put("title", I18nRes.findValue(super.getContext().getLanguage(), TabletConfig.getTabletConfModel().getTitle()));
		hashTags.put("imgPhoto", imgPhoto);
		hashTags.put("url", "./login.wf?sid=" + sessionId + "&cmd=com.actionsoft.apps.portal.tablet_Tablet_Portal_TabletList"); // 首页
		// 列表
		hashTags.put("startUrl", "./login.wf?sid=" + sessionId + "&cmd=com.actionsoft.apps.portal.tablet_Tablet_Portal_FunctionList"); // 添加页面列表
		hashTags.put("skinUrl", "./login.wf?sid=" + sessionId + "&cmd=com.actionsoft.apps.portal.tablet_Tablet_Portal_ChangeSkin"); // 换肤
		hashTags.put("logoutUrl", "./login.wf?sid=" + sessionId + "&cmd=com.actionsoft.apps.portal.tablet_Tablet_Portal_Logout"); // 退出
		TabletConstant.setHashtbaleTheme(hashTags);
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("com.actionsoft.apps.portal.tablet_Tablet_Portal_Home.htm"), hashTags);
	}

	/**
	 * 获取头像
	 * 
	 * @param sessionId
	 * @param uid
	 * @return
	 */
	public String getImgPhoto(String sessionId, String uid) {
		String imgPhoto = "../aws_img/userPhoto.jpg";
		String sourceDir = AWFConfig._awfServerConf.getDocumentPath() + "Photo/group" + uid + "/file0/" + uid + ".jpg";
		File file = new File(sourceDir);
		if (file.exists()) {
			double rand = Math.random();// 解决 ie7缓存
			imgPhoto = "./downfile.wf?flag1=" + uid + "&flag2=0&sid=" + sessionId + "&rootDir=Photo&filename=" + uid + ".jpg&v=" + rand;
		}
		return imgPhoto;
	}

	/**
	 * 返回首页列表信息icon
	 * 
	 * @return
	 */
	public String getTabletList() {
		String tabletListHtml = getHomeListHtml();
		// <span
		// class="current">1</span><span>2</span><span>3</span><span>4</span><span>5</span>
		String pages = getPageList();
		String orderIndexs = getOrderIndexs();
		String imgUrl = "";
		imgUrl = getBgImg();
		// Map lists =
		// TabletPagesDaoFactory.createTabletPages().getInstance(super.getContext().getUID());
		// TabletPagesModel pageModel = (TabletPagesModel)
		// lists.get(lists.size() - 1);
		// int myTotalPage=0;
		// if(pageModel!=null){
		// myTotalPage=pageModel.getPage();
		// }
		if (imgUrl.equals("") || imgUrl == null || imgUrl.equals("null")) {
			imgUrl = "../app/com.actionsoft.apps.portal.tablet/bg/defaultbg.jpg";
		}
		Hashtable hashTags = new Hashtable();
		hashTags.put("sid", getSIDFlag());
		hashTags.put("uid", super.getContext().getUID());
		hashTags.put("id", super.getContext().getID());
		hashTags.put("sessionId", super.getContext().getSessionId());
		hashTags.put("tabletListHtml", tabletListHtml);
		hashTags.put("imgUrl", imgUrl);
		hashTags.put("orderIndex", orderIndexs);
		hashTags.put("pages", pages);
		// hashTags.put("myTotalPage", myTotalPage);

		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("com.actionsoft.apps.portal.tablet_Tablet_Portal_HomeList.htm"), hashTags);
	}

	/**
	 * 格式化 sessionID
	 * 
	 * @param sid
	 * @return
	 */
	public String encodeSid(String sid) {
		try {
			sid = java.net.URLEncoder.encode(sid, "UTF-8");
		} catch (UnsupportedEncodingException e) {

		}
		return sid;

	}

	/**
	 ** 获得首页图标列表
	 * 
	 * @return
	 */
	public String getHomeListHtml() {
		TabletUserInfo tableInfo = TabletUserInfoDaoFactory.createTabletUserInfo();
		Map tabletList = tableInfo.getFunctionListByOrder(super.getContext().getUID());
		StringBuffer html = new StringBuffer();
		int pageSize = 20;
		int total = tabletList.size();
		int page = ((total % pageSize) == 0 ? (total / pageSize) : (total / pageSize + 1));
		// int ii = 0;
		// Map lists =
		// TabletPagesDaoFactory.createTabletPages().getInstance(super.getContext().getUID());
		// if (lists != null && lists.size() > 0) {
		// TabletPagesModel pageModel = (TabletPagesModel)
		// lists.get(lists.size() - 1);
		// page = pageModel.getPage();
		// for (int i = 0; i < page; i++) {
		// TabletPagesModel pagesModel = (TabletPagesModel) lists.get(i);
		// html.append("<div class='page' id='page_" + i + "'>");
		// List myList = getMyList(pagesModel.getPageSize(), tabletList,ii);
		// ii = ii + myList.size();
		// for (int j = 0; j < myList.size(); j++) {
		// TabletUserInfoModel model = (TabletUserInfoModel) myList.get(j);
		// // 读取page表中的值
		// html.append(getHomeListHtml(model));
		// }
		// html.append("</div>\n");
		// }
		// } else {
		for (int i = 0; i < page; i++) {
			html.append("<div class='page' id='page_" + i + "'>");
			for (int j = (i * pageSize); j < ((i + 1) * pageSize); j++) {
				TabletUserInfoModel model = (TabletUserInfoModel) tabletList.get(j);
				// 20个图标显示一页
				html.append(getHomeListHtml(model, j));
			}
			html.append("</div>\n");
		}
		// }
		// System.out.println(html.toString());
		return html.toString();
	}

	public String getHomeListHtml(TabletUserInfoModel model, int j) {
		StringBuffer html = new StringBuffer();
		String sessionId = encodeSid(super.getContext().getSessionId());
		// 20个图标显示一页
		if (model != null) {
			String type = model.getType();
			String label = "";
			String icon = "";
			String url = "";
			int id = 0;

			if (type.equals(TabletConstant.DirSystem)) { // 如果是 系统
				NavigationSystemModel systemModel = (NavigationSystemModel) NavigationSystemCache.getModel(model.getFunctionID());
				if (systemModel != null) {
					label = NavUtil.getLangName(getContext().getLanguage(), systemModel._systemName);
					icon = systemModel._systemIcon;
					url = systemModel._systemUrl;
					id = systemModel._id;
				}
			} else if (type.equals(TabletConstant.DirSubSystem)) { // 如果是 子系统
				NavigationDirectoryModel navigationModel = (NavigationDirectoryModel) NavigationDirectoryCache.getModel(model.getFunctionID());
				if (navigationModel != null) {
					label = NavUtil.getLangName(getContext().getLanguage(), navigationModel._directoryName);
					icon = navigationModel._navIcon;
					url = navigationModel._directoryUrl;
					id = navigationModel._id;
				}
			} else if (type.equals(TabletConstant.DirSubFunction)) { // 子功能
				NavigationFunctionModel functionModel = (NavigationFunctionModel) NavigationFunctionCache.getModel(model.getFunctionID());
				if (functionModel != null) {
					label = NavUtil.getLangName(getContext().getLanguage(), functionModel._functionName);
					icon = functionModel._navIcon;
					url = URLParser.repleaseNavURL(getContext(), functionModel._functionUrl);
					id = functionModel._id;
				}

			} else if (type.equals(TabletConstant.DirPortlet)) { // 公共门户
				SysPortalModel sysPortalModel = (SysPortalModel) SysPortalCache.getModel(model.getFunctionID());
				if (sysPortalModel != null) {
					label = NavUtil.getLangName(getContext().getLanguage(), sysPortalModel._portalTitle);
					icon = "../aws_img/private/default.gif";
					url = sysPortalModel._portalUrl;
					id = sysPortalModel._id;
				}
			}

			if (icon.indexOf("..") == -1) {
				icon = ".." + icon;
			}
			if (icon.equals("") || icon.indexOf("/") == -1) {
				icon = "../aws_img/private/folder9.gif";
			}
			url = url.replaceAll("<#sid>", sessionId);
			String counts = getWorkTableOrMailCount(label);
			String closeButton = getCloseButtonHtml(label, type, id);
			String imgHeight = "";
			if (!counts.equals("")) {
				imgHeight = "height:60px;";
			}
			if (label.length() > 6) {
				label = label.substring(0, 6) + "..";

			}
			html.append("<span class='rounded-img2' style='background: url(" + icon + ") no-repeat center center; margin-bottom:35px; width: 75px; height: 75px;' > \n");
			html.append(counts).append(closeButton).append(
					"<img src='" + icon + "' style='opacity: 0;" + imgHeight + "' myurl='" + url + "' functionId=" + model.getFunctionID() + " type=" + model.getType() + " orderIndex='" + model.getOrderIndex() + "'  />\n");
			html.append("<label><I18N#").append(label).append("></label>\n");
			html.append("</span>\n");
		}

		return html.toString();
	}

	public List getMyList(int f, Map tabletList, int p) {
		StringBuffer html = new StringBuffer();
		int page = f;
		int start = 0;
		if (p > 0) {
			start = p;
			page = p + f;
		}
		List list = new ArrayList();
		for (int i = start; i < page; i++) { // 1 19 2 20 3 5 44
			TabletUserInfoModel model = (TabletUserInfoModel) tabletList.get(i);
			list.add(model);
		}
		return list;

	}

	/**
	 * 获取orderIndex 列表
	 * 
	 * @return
	 */

	public String getOrderIndexs() {
		StringBuffer orderIndexHtml = new StringBuffer();
		TabletUserInfo tableInfo = TabletUserInfoDaoFactory.createTabletUserInfo();
		Map tabletList = tableInfo.getFunctionListByOrder(super.getContext().getUID());
		if (tabletList != null && tabletList.size() > 0) {
			for (int i = 0; i < tabletList.size(); i++) {
				TabletUserInfoModel model = (TabletUserInfoModel) tabletList.get(i);
				if (i == 0) {
					orderIndexHtml.append(model.getFunctionID()).append(":").append(model.getType()).append(":").append(model.getOrderIndex());
				} else {
					orderIndexHtml.append("|").append(model.getFunctionID()).append(":").append(model.getType()).append(":").append(model.getOrderIndex());
				}
			}
		}
		return orderIndexHtml.toString();
	}

	/**
	 **移除图标
	 * 
	 * @param functionId
	 * @param type
	 * @return
	 */
	public String removeIcon(int functionId, String type) {
		Map trace = new HashMap();
		String isRmove = "-1";
		TabletUserInfo info = TabletUserInfoDaoFactory.createTabletUserInfo();
		int f = info.removeByFunctionID(functionId, super.getContext().getUID(), type);
		if (f > 0) {
			isRmove = "1";
			trace.put(" 移除快捷方式成功[removeShortcut]", "移除快捷方式成功");
		} else {
			trace.put(" 登录成功[removeShortcut]", "移除快捷方式失败");
		}
		logger.log("平板电脑流程门户", Action.DELETE, trace, Level.INFO);
		return isRmove;
	}

	/**
	 * 获取底部分页显示区域
	 * 
	 * @return
	 */
	public String getPageList() {
		StringBuffer pages = new StringBuffer();
		// <span
		// class="current">1</span><span>2</span><span>3</span><span>4</span><span>5</span>
		TabletUserInfo tableInfo = TabletUserInfoDaoFactory.createTabletUserInfo();
		Map tabletList = tableInfo.getFunctionListByOrder(super.getContext().getUID());
		StringBuffer html = new StringBuffer();
		int pageSize = 20;
		int total = tabletList.size();
		int page = ((total % pageSize) == 0 ? (total / pageSize) : (total / pageSize + 1));
		// Map lists =
		// TabletPagesDaoFactory.createTabletPages().getInstance(super.getContext().getUID());
		// if (lists != null && lists.size() > 0) {
		// TabletPagesModel pageModel = (TabletPagesModel)
		// lists.get(lists.size() - 1);
		// page = pageModel.getPage();
		// }
		for (int i = 0; i < page; i++) {
			if (i == 0) {
				pages.append("<span style='margin-left:5px;' class='current'>" + (i + 1) + "</span>");
			} else {
				pages.append("<span style='margin-left:5px;' >" + (i + 1) + "</span>");
			}
		}
		return pages.toString();
	}

	/**
	 * 获取工作台或企业邮箱 数量
	 * 
	 * @return
	 */
	public String getWorkTableOrMailCount(String label) {
		StringBuffer countHtml = new StringBuffer();
		int count = 0;
		if (label.equals("我的工作台") || label.equals("企业邮箱")) {
			if (label.equals("我的工作台")) { // 我的工作台 待办数量
				try {
					count = TaskWorklistAPI.getInstance().getTaskCount(super.getContext().getUID(), 1, null, null);
				} catch (AWSSDKException e) {
					e.printStackTrace();
				}

			} else if (label.equals("企业邮箱")) { //
				count = new Awake(super.getContext()).getNewMailCount();
			}
			if (count > 0) {
				countHtml.append("<div class='count'  style='background:white;width:25px; height:25px;margin:-10px 0px 0px -10px'>\n");
				countHtml.append("<div class='count'  style='color:white;white-space:nowrap; font-size:13px;border:0px solid white;background:red;width:20px; height:20px;margin:2px '>\n");
				countHtml.append("" + count + "\n");
				countHtml.append("</div>\n");
				countHtml.append("</div>\n");
			}
		}

		return countHtml.toString();
	}

	public String getCloseButtonHtml(String labe, String type, int functionId) {
		StringBuffer closeButtonHtml = new StringBuffer();
		String margin = "-10px 0px 0px 62px";
		if (labe.equals("我的工作台") || labe.equals("企业邮箱")) {
			margin = "-26px 0px 0px 62px";
		}
		closeButtonHtml.append(" <div class='close'  style='background:white;width:25px; display:none; height:25px;  margin:" + margin + "'>\n");
		closeButtonHtml.append(" <div class='innerclose'  style='color:white;white-space:nowrap; font-size:15px;background:black;width:20px; height:20px;margin:2px ' onclick=\"removeIcon(this,'" + functionId + "','" + type + "')\" >\n");
		closeButtonHtml.append(" X");
		closeButtonHtml.append(" </div>\n");
		closeButtonHtml.append(" </div>\n");
		return closeButtonHtml.toString();
	}

	/**
	 * 获取背景图片
	 * 
	 * @return
	 */
	public String getBgImg() {
		StringBuffer bgImg = new StringBuffer("");
		String sql = "SELECT * FROM SYS_USER_PROFILE  WHERE USERID='" + super.getContext().getUID() + "'";
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		conn = DBSql.open();
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				bgImg.append(rs.getString("P4"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, st, rs);
		}
		return bgImg.toString();
	}

	/**
	 * 更新排序字段
	 * 
	 * @param orderIndex
	 * @return
	 */
	public String updateTabletOrderIndex(String orderIndex) {
		String result = "";
		TabletUserInfo tableInfo = TabletUserInfoDaoFactory.createTabletUserInfo();
		result = tableInfo.updateOrderIndexs(orderIndex, super.getContext().getUID());
		return result;
	}

	// /**
	// * 更新页面容量
	// * @param pages
	// * @return
	// */
	// public String updateTabletPages(String pages) {
	// String result = "1";
	// StringBuffer tmp = new StringBuffer();
	// if (pages.indexOf("|") > -1) { // 多页
	// String pa[] = pages.split("\\|");
	// for (int i = 0; i < pa.length; i++) {
	// if (pa[i].indexOf(":") > -1) {
	// String p[] = pa[i].split(":");
	// TabletPages tabletpages = TabletPagesDaoFactory.createTabletPages();
	// try {
	// tabletpages.insertTabletAndUpdate(super.getContext().getUID(),
	// Integer.parseInt(p[0]), Integer.parseInt(p[1]));
	// tmp.append("1");
	// } catch (Exception ex) {
	// tmp.append("-1");
	// }
	// }
	//
	// }
	// } else { // 一页
	// if (pages.indexOf(":") > -1) {
	// String p[] = pages.split(":");
	// try {
	// TabletPages tabletpages = TabletPagesDaoFactory.createTabletPages();
	// tabletpages.insertTabletAndUpdate(super.getContext().getUID(),
	// Integer.parseInt(p[0]), Integer.parseInt(p[1]));
	// tmp.append("1");
	// } catch (Exception ex) {
	// tmp.append("-1");
	// }
	//
	// } else {
	// tmp.append("-1");
	// }
	//
	// if (tmp.indexOf("-1") > -1) {
	//
	// result = "-1";
	// }
	// }
	// return result;
	// }
	//	 
}
