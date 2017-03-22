package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.commons.urlmanager.dao.UrlDaoFactory;
import com.actionsoft.awf.commons.urlmanager.model.UrlRepositoryModel;
import com.actionsoft.awf.commons.urlmanager.web.UrlManagerDataWeb;
import com.actionsoft.awf.commons.urlmanager.web.UrlManagerWeb;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.i18n.I18nRes;

public class URLManagerSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("AWS_URLManager_Main")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    myOut.write(web.getPortalWeb());
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Goup_Add")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getGroupWeb(groupUUID, 0));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Goup_Edit")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getGroupWeb(groupUUID, 1));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Goup_Remove")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.remove(groupUUID));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Goup_Move_Save")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String sourceId = myCmdArray.elementAt(3).toString();
	    String targetId = myCmdArray.elementAt(4).toString();
	    if (sourceId.trim().length() == 0)
		sourceId = "0";
	    if (targetId.trim().length() == 0)
		targetId = "0";
	    myOut.write(web.dragGroupMove(Integer.parseInt(sourceId),
		    Integer.parseInt(targetId)));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Goup_Save")) {
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    String status = myCmdArray.elementAt(4).toString();
	    String groupName = UtilCode.decode(myStr.matchValue("_groupName[",
		    "]groupName_"));
	    String urlMaster = UtilCode.decode(myStr.matchValue("_urlMaster[",
		    "]urlMaster_"));
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    myOut.write(web.saveGroup(groupUUID, status, groupName, urlMaster));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Goup_Tree_JsonData")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    myOut.write(web.getUrlManagerTreeJsonData(requestType, param1,
		    param2, param3));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_List")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getFormList(groupUUID));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_OpenForm")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    String uuid = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getFormOpen(groupUUID, uuid));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Save")) {
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    String uuid = myCmdArray.elementAt(4).toString();
	    String status = myCmdArray.elementAt(5).toString();
	    String pageType = myCmdArray.elementAt(6).toString();
	    String urlName = UtilCode.decode(myStr.matchValue("_urlName[",
		    "]urlName_"));
	    String url = UtilCode.decode(myStr.matchValue("_url[", "]url_"));
	    if (status.trim().length() == 0)
		status = "0";
	    if (pageType.trim().length() == 0)
		pageType = "0";
	    int flag = 0;
	    if (uuid.trim().length() == 0) {
		UrlRepositoryModel model = new UrlRepositoryModel();
		model._updateuser = me.getUID();
		model._groupuuid = groupUUID;
		model._pagetype = Integer.parseInt(pageType);
		model._status = Integer.parseInt(status);
		model._urlname = urlName;
		model._url = url;
		model._createuser = me.getUID();
		flag = UrlDaoFactory.createUrlRepository().create(model);
		if (flag >= DBSql.SQL_EXECUTE_STATUS_OK)
		    myOut.write(DBSql.SQL_EXECUTE_STATUS_OK
			    + I18nRes.findValue(me.getLanguage(), "数据新增成功")
			    + "!");
		else
		    myOut.write(DBSql.SQL_EXECUTE_STATUS_ERROR
			    + I18nRes.findValue(me.getLanguage(), "数据新增失败")
			    + "!");
	    } else {
		UrlRepositoryModel model = (UrlRepositoryModel) UrlDaoFactory
			.createUrlRepository().getModel(uuid);
		model._updateuser = me.getUID();
		model._pagetype = Integer.parseInt(pageType);
		model._status = Integer.parseInt(status);
		model._urlname = urlName;
		model._url = url;
		flag = UrlDaoFactory.createUrlRepository().store(model);
		if (flag == DBSql.SQL_EXECUTE_STATUS_OK)
		    myOut.write(DBSql.SQL_EXECUTE_STATUS_OK
			    + I18nRes.findValue(me.getLanguage(), "数据保存成功")
			    + "!");
		else
		    myOut.write(DBSql.SQL_EXECUTE_STATUS_ERROR
			    + I18nRes.findValue(me.getLanguage(), "数据保存失败")
			    + "!");
	    }
	} else if (socketCmd.equals("AWS_URLManager_Remove")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String idLists = UtilCode.decode(myStr.matchValue("_idLists[",
		    "]idLists_"));
	    myOut.write(web.removeUrlManager(idLists));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_List_AjaxSheetXML")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    String start = myCmdArray.elementAt(4).toString();
	    if (start.length() == 0)
		start = "0";
	    String limit = myCmdArray.elementAt(5).toString();
	    if (limit.length() == 0)
		limit = "0";
	    myOut.write(web.getUrlManagerListAjaxSheetXML(groupUUID,
		    Integer.parseInt(start), Integer.parseInt(limit)));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Move_Save")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String sourceId = myCmdArray.elementAt(3).toString();
	    String targetId = myCmdArray.elementAt(4).toString();
	    if (sourceId.trim().length() == 0)
		sourceId = "0";
	    if (targetId.trim().length() == 0)
		targetId = "0";
	    myOut.write(web.dragMove(Integer.parseInt(sourceId),
		    Integer.parseInt(targetId)));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_UpdateStatus_Save")) {
	    String uuid = myCmdArray.elementAt(3).toString();
	    String status = myCmdArray.elementAt(4).toString();
	    if (status.trim().length() == 0)
		status = "0";
	    UrlRepositoryModel model = (UrlRepositoryModel) UrlDaoFactory
		    .createUrlRepository().getModel(uuid);
	    if (model != null)
		model._status = Integer.parseInt(status);
	    int r = UrlDaoFactory.createUrlRepository().store(model);
	    if (r == DBSql.SQL_EXECUTE_STATUS_DBPOOL_ERROR)
		myOut.write(r + "数据更新失败!");
	    else
		myOut.write(r + "数据更新成功!");
	} else if (socketCmd.equals("AWS_URLManager_DownloadXML")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String list = UtilCode.decode(myStr.matchValue("_idLists[",
		    "]idLists_"));
	    myOut.write(web.getDownloadXMLDialog(list));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_UploadXMLImport")) {
	    UrlManagerDataWeb web = new UrlManagerDataWeb(me);
	    String groupUUID = myCmdArray.elementAt(3).toString();
	    myOut.write(web.installUploadXML(groupUUID));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Display")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getDisplay(uuid));
	    web = null;
	} else if (socketCmd.equals("AWS_URLManager_Workflow_Refers")) {
	    UrlManagerWeb web = new UrlManagerWeb(me);
	    String uuid = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getWorkflowStepRefers(uuid));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
