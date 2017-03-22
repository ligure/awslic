package com.actionsoft.apps.portal.mobile.page;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.actionsoft.application.logging.AuditLogger;
import com.actionsoft.application.logging.model.Action;
import com.actionsoft.application.logging.model.AuditObj;
import com.actionsoft.application.logging.model.Catalog;
import com.actionsoft.application.logging.model.Channel;
import com.actionsoft.application.logging.model.Level;
import com.actionsoft.application.portal.personal.Awake;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.apps.portal.mobile.config.MobileConfig;
import com.actionsoft.apps.portal.mobile.model.MobileConfigModel;
import com.actionsoft.apps.portal.mobile.model.MobileItemModel;
import com.actionsoft.apps.portal.mobile.util.MobileUtil;
import com.actionsoft.awf.login.control.LoginControl;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.session.SessionImpl;
import com.actionsoft.awf.session.cache.SessionCache;
import com.actionsoft.awf.session.model.SessionModel;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.TaskWorklistAPI;

public class MobileHomePage extends ActionsoftWeb {
	protected AuditLogger logger = AuditLogger.getLogger(Channel.CLIENT,
			Catalog.MOBILE, AuditObj.MODEL_MOBILE);
	protected AuditLogger seclogger = AuditLogger.getLogger(Channel.SECURITY,
			Catalog.USERLOGIN, AuditObj.MODEL_MOBILE);
	protected AuditLogger loggers = AuditLogger.getLogger(Channel.SECURITY,
			Catalog.USERFREEZE, AuditObj.MODEL_MOBILE);

	public MobileHomePage(UserContext userContext) {
		super(userContext);
	}

	public MobileHomePage() {
	}

	public String login(String userId, String pwd, String bip, String lang,
			String extInfo, Hashtable params) {
		Map trace = new HashMap();
		UserContext user = null;
		LoginControl login = new LoginControl(userId, pwd, bip, params);
		int i = login.check();
		if (i == -8) {
			trace.put("错误信息[errorMsg]", "口令错误!");
			this.seclogger.log("AWS手机客户端_登录失败", Action.LOGIN, trace,
					Level.ERROR);
			return "<div style='font-size:25px;text-align:center;'><div><a href='../m'>"
					+ I18nRes.findValue(lang, "返回")
					+ "</a></div><div>"
					+ I18nRes.findValue(lang, "口令错误") + "!</div></div>";
		}
		if (i == -77) {
			trace.put("错误信息[errorMsg]", "用户已经被冻结!");
			this.loggers.log("AWS手机客户端_账户冻结", Action.LOGIN, trace, Level.WARN);
			return "<div style='font-size:25px;text-align:center;'><div><a href='../m'>"
					+ I18nRes.findValue(lang, "返回")
					+ "</a></div><div>"
					+ I18nRes.findValue(lang, "用户已经被冻结") + "!</div></div>";
		}

		if (i == -7) {
			trace.put("错误信息[errorMsg]", "用户已经被注销!");
			this.seclogger.log("AWS手机客户端_登录失败", Action.LOGIN, trace,
					Level.ERROR);
			return "<div style='font-size:25px;text-align:center;'><div><a href='../m'>"
					+ I18nRes.findValue(lang, "返回")
					+ "</a></div><div>"
					+ I18nRes.findValue(lang, "该用户帐户已经被注销") + "!</div></div>";
		}
		if (i == -9) {
			trace.put("错误信息[errorMsg]", "用户名不存在!");
			this.seclogger.log("AWS手机客户端_登录失败", Action.LOGIN, trace,
					Level.ERROR);
			return "<div style='font-size:25px;text-align:center;'><div><a href='../m'>"
					+ I18nRes.findValue(lang, "返回")
					+ "</a></div><div>"
					+ I18nRes.findValue(lang, "用户名不存在") + "!</div></div>";
		}
		if (i == -999) {
			trace.put("错误信息[errorMsg]", "数据库连接异常!");
			this.seclogger.log("AWS手机客户端_登录失败", Action.LOGIN, trace,
					Level.ERROR);
			return "<div style='font-size:25px;text-align:center;'><div><a href='../m'>"
					+ I18nRes.findValue(lang, "返回")
					+ "</a></div><div>"
					+ I18nRes.findValue(lang, "与数据库服务的连接异常，请通知系统管理员")
					+ "!</div></div>";
		}

		SessionImpl mySession = new SessionImpl(Integer
				.parseInt(AWFConfig._awfServerConf.getSessionOnlineLife()));
		SessionModel sessionModel = mySession.registerSession(userId, bip,
				lang, "mobile");
		try {
			user = new UserContext(sessionModel._sessionId, bip);
		} catch (Exception e) {
			e.printStackTrace();
		}

		trace.put(" 登录成功[login]", "登录成功");
		this.seclogger.log("AWS手机客户端_登录成功", Action.LOGIN, trace, Level.INFO);
		MobileConfig.reload();
		MobileHomePage web = new MobileHomePage(user);
		return web.getPortalHome(user);
	}

	public String logout(UserContext uc) {
		String img = MobileConfig.getConfModel().getHomePageBgImgUrl();
		Hashtable hashTags = new Hashtable();
		hashTags.put("logoUrl", img);
		hashTags.put("title", I18nRes.findValue(uc.getLanguage(), "欢迎下次使用"));
		hashTags.put("ok", I18nRes.findValue(uc.getLanguage(), "您已安全退出协同办公平台"));
		hashTags.put("cxdl", I18nRes.findValue(uc.getLanguage(), "重新登录"));
		hashTags.put("logo", I18nRes.findValue(uc.getLanguage(), "logo"));
		MobileUtil.setCommonTags(uc, hashTags);
		
		SessionModel raw = (SessionModel) SessionCache.getModel(uc.getSessionId());
		Map<String, SessionModel> m = SessionCache.getList();
		Iterator<SessionModel> it = m.values().iterator();
		while (it.hasNext()) {
			SessionModel model = it.next();
			if (model._UID.equals(raw._UID)) {
				new SessionImpl().destroySession(model._sessionId);
			}
		}
		
		return RepleaseKey
				.replace(
						HtmlModelFactory
								.getHtmlModel("com.actionsoft.apps.portal.mobile_Portal_Logout.htm"),
						hashTags);
	}

	public String getPortalHome(UserContext user) {
		UserModel userModel = user.getUserModel();
		int newTaskCount = 0;
		try {
			newTaskCount = TaskWorklistAPI.getInstance().getTaskCount(
					userModel.getUID(), 1, null, null);
		} catch (AWSSDKException e1) {
			e1.printStackTrace();
		}

		int newMessage = 0;
		try {
			newMessage = TaskWorklistAPI.getInstance().getTaskCount(
					userModel.getUID(), 2, null, null);
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
		int m = Integer.parseInt(AWFConfig._awfServerConf
				.getSessionOnlineLife());
		Hashtable h = new SessionImpl().getOnline(m);
		int onlineUserCount = h.size();

		StringBuffer listHtml = new StringBuffer();
		MobileConfigModel mc = MobileConfig.getConfModel();
		HashMap itemList = MobileConfig.getItemList();
		String bgimg = "";
		String bgbigimg = "";
		String headerTheme = "";
		String task = " ";
		String newMail = "";
		String online = "";

		if (mc != null) {
			bgimg = mc.getHomePageBgImgUrl();
			bgbigimg = mc.getHomePageBgBigimgurl();
			headerTheme = mc.getHeaderTheme();
			for (int i = 0; i < itemList.size(); i++) {
				MobileItemModel itemmoel = (MobileItemModel) itemList
						.get(Integer.valueOf(i));
				if ((itemmoel != null) && (itemmoel.isDisplay())) {
					String img = "";
					if (!itemmoel.getIconUrl().equals("")) {
						img = "<img src=\""
								+ itemmoel.getIconUrl()
								+ "\"   alt=\"img\" width='24px' height='19px'class=\"ui-li-icon\">";
					}
					if (itemmoel.getId().equals("tasklist"))
						task = "<span class=\"ui-li-count\">" + newTaskCount
								+ "</span>";
					else {
						task = "";
					}
					if (itemmoel.getId().equals("inbox"))
						newMail = "<span class=\"ui-li-count\">"
								+ String.valueOf(new Awake(user)
										.getNewMailCount()) + "</span>";
					else {
						newMail = " ";
					}
					if (itemmoel.getId().equals("onlineuser"))
						online = "<span class=\"ui-li-count\">"
								+ String.valueOf(onlineUserCount) + "</span>";
					else {
						online = " ";
					}

					listHtml.append("<li><a href=\""
							+ itemmoel.getUrl().replace("<#sessionId>",
									getContext().getSessionId())
							+ "\" data-transition=\"" + mc.getTransition()
							+ "\"");

					listHtml.append(" rel=\"external\"");

					listHtml.append(">"
							+ img
							+ I18nRes.findValue(super.getContext()
									.getLanguage(), itemmoel.getTitle()) + task
							+ newMail + online + " </a></li>");
				}
			}
		}

		Map trace = new HashMap();
		trace.put(" 登录首页[longinIndex]", "登录首页成功");
		this.logger.log("AWS手机客户端", Action.SELECT, trace, Level.INFO);
		Hashtable hashTags = new Hashtable();
		hashTags.put("sid", getSIDFlag());

		hashTags.put("userName", userModel.getUserName());
		hashTags.put("listHtml", listHtml.toString());
		hashTags.put("bgurl", bgimg);
		hashTags.put("bgbigurl", bgbigimg);
		hashTags.put("headerTheme", headerTheme);
		hashTags.put("newTaskCount", Integer.toString(newTaskCount));
		hashTags.put("newMailCount", Integer.toString(new Awake(user)
				.getNewMailCount()));
		hashTags.put("newNotice", String.valueOf(newMessage));
		hashTags.put("onlineUserCount", String.valueOf(onlineUserCount));
		hashTags
				.put(
						"createMail",
						"login.wf?sid="
								+ user.getSessionId()
								+ "&cmd=com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_Actions&actions=newEmail");
		hashTags.put("footer", MobileUtil.getPageFooterHtml(user, hashTags, 1,
				true));
		MobileUtil.setCommonTags(super.getContext(), hashTags);
		return RepleaseKey
				.replace(
						HtmlModelFactory
								.getHtmlModel("com.actionsoft.apps.portal.mobile_Portal_Home.htm"),
						hashTags);
	}
}