package com.actionsoft.application.portal.portlet;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.AWFServerConf;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.session.dao.Session;
import com.actionsoft.awf.session.model.SessionModel;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.workflow.execute.workbox2.util.TaskDataSource;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.TaskWorklistAPI;
import com.actionsoft.sdk.services.level0.worklist.GetTaskCount;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;

/**
 * 
 * @description 修改我的个人信息样式，在样式中增加通讯录、和工作台两个功能。
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:40:28
 */
public class PersonInfoPortlet extends ActionsoftWeb {
    UserContext userContext;

    public PersonInfoPortlet(UserContext userContext) {
	super(userContext);
	this.userContext = userContext;
    }

    public PersonInfoPortlet() {
    }

    public String getPage() throws AWSSDKException {
	String photoDir = AWFConfig._awfServerConf.getDocumentPath()
		+ "Photo/group" + getContext().getUserModel().getUID()
		+ "/file0/" + getContext().getUserModel().getUID() + ".jpg";
	File photoFile = new File(photoDir);
	// Hashtable h = new Session().getLogList(getContext().getUID());
	String loginInfo = "";
	Hashtable h = null;
	if (h != null) {
	    SessionModel model = (SessionModel) h.get(new Integer(1));
	    if (model != null) {
		String logtime1 = UtilDate.datetimeFormat(new Date(
			model._startTime));
		loginInfo = "<I18N#上次登录信息><hr><I18N#登入>：" + logtime1 + "<br>";
		if (model._IsLogOff) {
		    String logtime2 = UtilDate.datetimeFormat(new Date(
			    model._refreshTime));
		    loginInfo = loginInfo + "<I18N#登出>：" + logtime2 + "<br>";
		}
		loginInfo = loginInfo + "IP&nbsp;&nbsp;&nbsp;&nbsp;："
			+ model._ip + "<br>";
	    }
	}
	Hashtable hashTags = new Hashtable();
	if (photoFile.exists()) {
	    hashTags.put("photo",
		    "<img border=0  width='80' height='80' src='./downfile.wf?flag1="
			    + getContext().getUserModel().getUID()
			    + "&flag2=0&sid=" + getContext().getSessionId()
			    + "&rootDir=Photo&filename="
			    + getContext().getUserModel().getUID() + ".jpg'>");
	} else {
	    hashTags.put("photo",
		    "<img border=0  height='80' width=80 src='../aws_img/userPhoto.jpg'/></a>");
	}
	int daibansum = TaskWorklistAPI.getInstance().getTaskCount(
		getContext().getUID(), 1, "", "");
	int myTaskCount = TaskDataSource.getAllTaskList(
		super.getContext().getUID(),
		" and (status=1 or status=3 or status=11 or status=4) ").size();
	int yibansun = TaskWorklistAPI.getInstance().getHistoryTaskCount(
		getContext().getUID(), 1, "", "");
	hashTags.put("daibansum", myTaskCount);
	hashTags.put("yibansun", yibansun);
	hashTags.put("userName", getContext().getUserModel().getUserName());
	hashTags.put("loginInfo", loginInfo);
	hashTags.put("sid", super.getSIDFlag());
	String departmentFullName = userContext.getDepartmentModel()
		.getDepartmentFullNameOfCache() == null ? "" : userContext
		.getDepartmentModel().getDepartmentFullNameOfCache();
	StringBuffer departmentSb = new StringBuffer()
		.append(departmentFullName);
	hashTags.put("DepartmentFullName", departmentSb);
	return RepleaseKey.replace(
		HtmlModelFactory.getHtmlModel("personalInfo_Portlet.htm"),
		hashTags);
    }
}