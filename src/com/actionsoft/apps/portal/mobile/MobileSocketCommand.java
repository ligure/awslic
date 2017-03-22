package com.actionsoft.apps.portal.mobile;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.actionsoft.Util;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.application.server.socketcommand.ApplictionUtil;
import com.actionsoft.apps.portal.mobile.console.config.MobileConsoleConfig;
import com.actionsoft.apps.portal.mobile.page.MobileAddressPage;
import com.actionsoft.apps.portal.mobile.page.MobileCreateProcessPage;
import com.actionsoft.apps.portal.mobile.page.MobileHomePage;
import com.actionsoft.apps.portal.mobile.page.MobileInformation;
import com.actionsoft.apps.portal.mobile.page.MobileMailPage;
import com.actionsoft.apps.portal.mobile.page.MobileNoticePage;
import com.actionsoft.apps.portal.mobile.page.MobileOnlinePage;
import com.actionsoft.apps.portal.mobile.page.MobileProcessCenter;
import com.actionsoft.apps.portal.mobile.page.MobileTaskPage;
import com.actionsoft.apps.portal.mobile.page.MobileUserInfoPage;
import com.actionsoft.apps.portal.mobile.page.MobileWorkListLaunchTrackingPage;
import com.actionsoft.apps.portal.mobile.page.MobileWorklistPage;
import com.actionsoft.awf.form.execute.plugins.ext.AjaxDataDecode;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.eip.cmcenter.dao.CmDaoFactory;
import com.actionsoft.eip.cmcenter.model.CmContentReadModel;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.plugs.email.model.MailModel;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level1.SessionAPI;
/**
 * 
 * @description 移动端单点登录
 * @version 1.0
 * @author wangaz
 * @update 2014年6月3日 下午2:26:21
 */
public class MobileSocketCommand
  implements BaseSocketCommand
{
  public boolean executeCommand(UserContext me, Socket myProcessSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
    throws Exception
  {
    if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Login")) {
	String uid = myCmdArray.elementAt(2).toString();
	String pwd = myCmdArray.elementAt(3).toString();
 /*************************开始*********************************/
	   if("mysid".equals(uid)){
	       String[] sid = pwd.split("_");
	       uid = sid[0];
	       boolean flag = SessionAPI.getInstance().checkSession(uid, pwd);
	       if(!flag){
		   myOut.write("<script language='javascript'>window.location.href='" +  ApplictionUtil.getProperty("SSO_URL").replaceFirst("index_blue.jsp", "m") + "';</script>");
		   return true;
	       }
           }else{
        	//调用客户方API解析cookie里面的值，最终获得用户名
       		HttpClient ht = new HttpClient();
       		//实例化util公共类
       		Util ut = new Util();
       		String responseMsg= null;
       	    //SSO登录验证site参数
       		String site = "oa";
       	    //SSO登录验证type参数
       		String type = "DECODE";
       	    //SSO登录验证time参数
       		String time = ut.getTime(ut.getdatetime());
       	    //UC用户中心的sign参数，将参数进行拼接，按字母降序排列进行MD5加密
       		String urlmd5 = "site="+site+"&time="+time+"&type="+type+"&v="+pwd+ApplictionUtil.getProperty("SSO_KEY");
       		String sign = Util.getMD5Str(urlmd5);
       	    //SSO登录验证URL
       		String url = ApplictionUtil.getProperty("SSO_API") + "?site="+site+"&time="+time+"&type="+type+"&v="+pwd+"&sign="+sign+"";
       		try{
       			GetMethod gt = new GetMethod(url);
       			ht.executeMethod(gt);
       		    //用来存放json返回的值。
       			byte[] responseBody = gt.getResponseBody();
       			responseMsg = new String(responseBody);
       			
       			String status = net.sf.json.JSONObject.fromObject(net.sf.json.JSONObject.fromObject(responseMsg).getString("respond")).getString("status");
       			if("1".equals(status)){
       			    String objects = net.sf.json.JSONObject.fromObject(responseMsg).getString("objects");
       			    int begin = objects.indexOf("&u=") + 3;
       			    int end = objects.indexOf("&time=");
       			    uid = objects.substring(begin, end);
       			} else {
       			    myOut.write("<script language='javascript'>window.location.href='" +  ApplictionUtil.getProperty("SSO_URL").replaceFirst("index_blue.jsp", "m") + "';</script>");
       			}				
       		}catch(Exception e){
       		   //将错误日志存储到公共日志表中
       			Hashtable err = new Hashtable();
    			err.put("YXT", "BPM AWS");
    			err.put("MBXT", "SSO");
    			err.put("MKMC", "登录验证");
    			err.put("JKMC", "SSO");
    			err.put("JLSJ", ut.getdatetime());
    			err.put("CZRXM", "管理员");
    			err.put("CZRXM", "admin");
    			err.put("BS", "失败");
    			err.put("MSXX", "登录失败！请查看AWSerror日志。错误："+e.getMessage());
    			BOInstanceAPI.getInstance().createBOData("BO_INTEGRATION_LOG", err, "admin");
       		e.printStackTrace();
       		}    
           }
   /**********************结束************************/
      String bip = myCmdArray.elementAt(4).toString();
      String lang = myCmdArray.elementAt(5).toString();
      String UserCert = UtilCode.decode(myStr.matchValue("_UserCert[", "]UserCert_"));
      String ContainerName = UtilCode.decode(myStr.matchValue("_ContainerName[", "]ContainerName_"));
      String UserSignedData = UtilCode.decode(myStr.matchValue("_UserSignedData[", "]UserSignedData_"));
      String p1 = UtilCode.decode(myStr.matchValue("_p1[", "]p1_"));
      String p2 = UtilCode.decode(myStr.matchValue("_p2[", "]p2_"));
      String p3 = UtilCode.decode(myStr.matchValue("_p3[", "]p3_"));
      String p4 = UtilCode.decode(myStr.matchValue("_p4[", "]p4_"));
      String p5 = UtilCode.decode(myStr.matchValue("_p5[", "]p5_"));
      Hashtable params = new Hashtable();
      params.put("CA_UserCert", UserCert);
      params.put("CA_ContainerName", ContainerName);
      params.put("CA_UserSignedData", UserSignedData);
      params.put("UserCert", UserCert);
      params.put("ContainerName", ContainerName);
      params.put("UserSignedData", UserSignedData);
      params.put("lang", lang);
      params.put("p1", p1);
      params.put("p2", p2);
      params.put("p3", p3);
      params.put("p4", p4);
      params.put("p5", p5);
      String extInfo = UtilCode.decode(myStr.matchValue("_extInfo[", "]extInfo_"));
      MobileHomePage web = new MobileHomePage();
      myOut.write(web.login(uid, pwd, bip, lang, extInfo, params));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Logout")) {
      MobileHomePage web = new MobileHomePage();
      myOut.write(web.logout(me));
      web = null;
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Home")) {
      MobileHomePage web = new MobileHomePage(me);
      myOut.write(web.getPortalHome(me));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_ProcessCenter")) {
      MobileProcessCenter web = new MobileProcessCenter(me);
      myOut.write(web.getPorcessCenterHome());
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Information")) {
      MobileInformation web = new MobileInformation(me);
      myOut.write(web.getInformationHome());
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Information_Open")) {
      MobileInformation web = new MobileInformation(me);
      String schemaId = myCmdArray.elementAt(3).toString();
      String channelId = myCmdArray.elementAt(4).toString();
      String contentId = myCmdArray.elementAt(5).toString();
      String readUser = myCmdArray.elementAt(6).toString();

      String pageNow = myCmdArray.elementAt(7).toString();
      if ((pageNow.equals("")) || (pageNow.equals("0"))) {
        pageNow = "1";
      }

      CmContentReadModel crModel = new CmContentReadModel();
      crModel._contentId = Integer.parseInt(contentId);
      crModel._readUser = readUser;
      int readId = CmDaoFactory.createContentRead().create(crModel);
      myOut.write(web.getInformationDetail(Integer.parseInt(schemaId), Integer.parseInt(channelId), 
        Integer.parseInt(contentId), readId, Integer.parseInt(pageNow)));
      web = null;
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_UserInfo")) {
      MobileUserInfoPage web = new MobileUserInfoPage(me);
      String userid = myCmdArray.elementAt(3).toString();
      myOut.write(web.getUserInfoPage(userid));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Worklist_Home")) {
      MobileWorklistPage web = new MobileWorklistPage(me);
      String pageNow = myCmdArray.elementAt(3).toString();
      String type = myCmdArray.elementAt(4).toString();
      String key = myCmdArray.elementAt(5).toString();

      if (("".equals(pageNow)) || (pageNow == null)) {
        pageNow = "1";
      }
      if (("".equals(type)) || (type == null)) {
        type = "1";
      }
      myOut.write(web.getWorklist(Integer.parseInt(pageNow), type, key));
    }
    else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Notice_Home")) {
      MobileNoticePage web = new MobileNoticePage(me);
      String pageNow = myCmdArray.elementAt(3).toString();
      if (("".equals(pageNow)) || (pageNow == null)) {
        pageNow = "1";
      }
      myOut.write(web.getNoticeList(Integer.parseInt(pageNow)));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Task_Open")) {
      MobileTaskPage web = new MobileTaskPage(me);
      String processInstanceId = myCmdArray.elementAt(3).toString();
      String taskInstanceId = myCmdArray.elementAt(4).toString();
      String openstate = myCmdArray.elementAt(5).toString();
      String ntype = myCmdArray.elementAt(6).toString();
      String client = "";
      try {
        client = myCmdArray.elementAt(7).toString();
      } catch (Exception e) {
        client = "";
      }
      String title = UtilCode.decode(myStr.matchValue("_title[", "]title_"));

      if ((ntype == null) || (ntype.equals(""))) {
        ntype = "1";
      }
      if (title == null) {
        title = "";
      }
      myOut.write(web.getTaskPage(Integer.parseInt(processInstanceId), Integer.parseInt(taskInstanceId), 
        Integer.parseInt(openstate), Integer.parseInt(ntype), title, client));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Subsheet_Open")) {
      MobileTaskPage web = new MobileTaskPage(me);
      int processInstId = Integer.parseInt(myCmdArray.elementAt(3).toString());
      int taskInstId = Integer.parseInt(myCmdArray.elementAt(4).toString());
      int subSheetModelID = Integer.parseInt(myCmdArray.elementAt(5).toString());
      String formModelUUID = myCmdArray.elementAt(6).toString();
      int boId = Integer.parseInt(myCmdArray.elementAt(7).toString());
      String back_title = UtilCode.decode(myStr.matchValue("_back_title[", "]back_title_"));
      int back_openstate = Integer.parseInt(myCmdArray.elementAt(8).toString());
      int back_type = Integer.parseInt(myCmdArray.elementAt(9).toString());
      String subSheetModelTitle = UtilCode.decode(myStr
        .matchValue("_subSheetModelTitle[", "]subSheetModelTitle_"));
      myOut.write(web.getSubMobileSheet(boId, processInstId, taskInstId, subSheetModelTitle, subSheetModelID, 
        formModelUUID, back_openstate, back_type, back_title));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Task_SelfDispose")) {
      MobileTaskPage web = new MobileTaskPage(me);
      String processInstanceId = myCmdArray.elementAt(3).toString();
      String taskInstanceId = myCmdArray.elementAt(4).toString();
      String openstate = myCmdArray.elementAt(5).toString();
      myOut.write(web.changeToSelfDispose(Integer.parseInt(processInstanceId), Integer.parseInt(taskInstanceId), 
        Integer.parseInt(openstate)));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Task_CC")) {
      MobileTaskPage web = new MobileTaskPage(me);
      String processInstanceId = myCmdArray.elementAt(3).toString();
      String taskInstanceId = myCmdArray.elementAt(4).toString();
      String client = "";
      try {
        client = myCmdArray.elementAt(5).toString();
      } catch (Exception e) {
        client = "";
      }
      String readToType = UtilCode.decode(myStr.matchValue("_readToType[", "]readToType_"));

      myOut.write(web.getTaskCC(Integer.parseInt(processInstanceId), Integer.parseInt(taskInstanceId), 
        readToType, client));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Task_SaveFormData")) {
      MobileTaskPage web = new MobileTaskPage(me);
      String processInstanceId = myCmdArray.elementAt(3).toString();
      String taskInstanceId = myCmdArray.elementAt(4).toString();
      String openstate = myCmdArray.elementAt(5).toString();
      String auditId = myCmdArray.elementAt(6).toString();
      String tmpAuditType = UtilCode.decode(myStr.matchValue("_auditMenuType[", "]auditMenuType_"));
      String opinion = UtilCode.decode(myStr.matchValue("_opinion[", "]opinion_"));
      String mobileFormData = UtilCode.decode(myStr.matchValue("_mobileFormData[", "]mobileFormData_"));
      tmpAuditType = AjaxDataDecode.getInstance().decode(tmpAuditType);
      String auditName = "";
      String auditType = "";
      if ((tmpAuditType != null) && (!tmpAuditType.equals(""))) {
        if (tmpAuditType.indexOf("/") != -1) {
          auditName = tmpAuditType.substring(0, tmpAuditType.indexOf("/"));
          auditType = tmpAuditType.substring(tmpAuditType.indexOf("/") + 1, tmpAuditType.length());
        }
      } else {
        auditName = "";
        auditType = "-99";
      }
      if (auditType.equals(""))
        auditType = "0";
      myOut.write(web.saveData(Integer.parseInt(processInstanceId), Integer.parseInt(taskInstanceId), 
        Integer.parseInt(openstate), Integer.parseInt(auditId), Integer.parseInt(auditType), auditName, 
        opinion, mobileFormData));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Task_Execute")) {
      MobileTaskPage web = new MobileTaskPage(me);
      String processInstanceId = myCmdArray.elementAt(3).toString();
      String taskInstanceId = myCmdArray.elementAt(4).toString();
      String openstate = myCmdArray.elementAt(5).toString();
      String auditId = myCmdArray.elementAt(6).toString();
      String tmpAuditType = UtilCode.decode(myStr.matchValue("_auditMenuType[", "]auditMenuType_"));
      String opinion = UtilCode.decode(myStr.matchValue("_opinion[", "]opinion_"));
      String mobileFormData = UtilCode.decode(myStr.matchValue("_mobileFormData[", "]mobileFormData_"));
      tmpAuditType = AjaxDataDecode.getInstance().decode(tmpAuditType);
      String auditName = "";
      String auditType = "";
      if ((tmpAuditType != null) && (!tmpAuditType.equals(""))) {
        auditName = tmpAuditType.substring(0, tmpAuditType.indexOf("/"));
        auditType = tmpAuditType.substring(tmpAuditType.indexOf("/") + 1, tmpAuditType.length());
      } else {
        auditName = "";
        auditType = "-99";
      }
      if (auditType.equals(""))
        auditType = "0";
      myOut.write(web.executeTaskPage(Integer.parseInt(processInstanceId), Integer.parseInt(taskInstanceId), 
        Integer.parseInt(openstate), Integer.parseInt(auditId), Integer.parseInt(auditType), auditName, 
        opinion, mobileFormData));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_CreateProcess_Home")) {
      MobileCreateProcessPage web = new MobileCreateProcessPage(me);
      String searchKey = UtilCode.decode(myStr.matchValue("_searchKey[", "]searchKey_"));

      String pageNow = myCmdArray.elementAt(4).toString();
      if (searchKey == null) {
        searchKey = "";
      }
      if ((pageNow == null) || (pageNow.equals(""))) {
        pageNow = "1";
      }
      myOut.write(web.getProcessListHome(Integer.parseInt(pageNow), searchKey));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_CreateProcess_Page")) {
      MobileCreateProcessPage web = new MobileCreateProcessPage(me);
      String processInstanceUUID = myCmdArray.elementAt(3).toString();
      String instanceTitle = UtilCode.decode(myStr.matchValue("_instanceTitle[", "]instanceTitle_"));
      myOut.write(web.createProcessInstance(processInstanceUUID, instanceTitle));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Address_Home")) {
      MobileAddressPage web = new MobileAddressPage(me);
      String companyId = myCmdArray.elementAt(3).toString();
      if ((companyId != null) && (companyId.equals(""))) {
        companyId = "0";
      }
      String deptId = myCmdArray.elementAt(4).toString();
      if ((deptId != null) && (deptId.equals(""))) {
        deptId = "0";
      }

      myOut.write(web.getDetailWeb(companyId, deptId, "", ""));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Address_Search")) {
      MobileAddressPage web = new MobileAddressPage(me);
      String searchKey = myCmdArray.elementAt(3).toString();
      myOut.write(web.getAddressSearchPage(searchKey));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Online_Home")) {
      MobileOnlinePage web = new MobileOnlinePage(me);
      myOut.write(web.getOnlinePage());
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_CreatePage")) {
      MobileMailPage web = new MobileMailPage(me);
      myOut.write(web.newMailPage());
    }
    else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_Actions")) {
      MobileMailPage web = new MobileMailPage(me);
      String actions = myCmdArray.elementAt(3).toString();
      String searchKey = myCmdArray.elementAt(4).toString();
      String selected = myCmdArray.elementAt(5).toString();
      String mailId = myCmdArray.elementAt(6).toString();
      String taskId = myCmdArray.elementAt(7).toString();
      String preTaskId = myCmdArray.elementAt(8).toString();
      if ((preTaskId == null) || ("".equals(preTaskId))) {
        preTaskId = "0";
      }
      String content = UtilCode.decode(myStr.matchValue("_content[", "]content_"));
      String mailTo = UtilCode.decode(myStr.matchValue("_mailTo[", "]mailTo_"));
      String mailCc = UtilCode.decode(myStr.matchValue("_mailCc[", "]mailCc_"));
      String mailBcc = UtilCode.decode(myStr.matchValue("_mailBcc[", "]mailBcc_"));
      String mailTitle = UtilCode.decode(myStr.matchValue("_mailTitle[", "]mailTitle_"));

      if ((selected == null) || ("".equals(selected))) {
        selected = "";
      }
      if ((mailId == null) || ("".equals(mailId))) {
        mailId = "0";
      }
      if ((taskId == null) || ("".equals(taskId))) {
        taskId = "0";
      }
      MailModel model = new MailModel();
      model._title = mailTitle;
      model._content = content;
      model._cc = mailCc;
      model._to = mailTo;
      model._bcc = mailBcc;
      model._id = Integer.parseInt(mailId);

      if ("newEmail".equals(actions))
        myOut.write(web.mailActions(actions, model, Integer.parseInt(taskId), "", Integer.parseInt(preTaskId)));
      else if (!"sendEmail".equals(actions))
      {
        if (!"".equals(selected)) {
          if (!"$backFlag$".equals(selected))
          {
            if (I18nRes.findValue(me.getLanguage(), "收件人").equals(actions)) {
              if ((model._to == null) || ("".equals(model._to)))
                model._to = selected;
              else if (model._to.trim().toLowerCase().indexOf(selected.toLowerCase()) < 0)
                model._to = (model._to + " " + selected);
            }
            else if (I18nRes.findValue(me.getLanguage(), "抄送").equals(actions)) {
              if ((model._cc == null) || ("".equals(model._cc)))
                model._cc = selected;
              else if (model._cc.trim().toLowerCase().indexOf(selected.toLowerCase()) < 0)
                model._cc = (model._cc + " " + selected);
            } else if (I18nRes.findValue(me.getLanguage(), "密送").equals(actions)) {
              if ((model._bcc == null) || ("".equals(model._bcc)))
                model._bcc = selected;
              else if (model._bcc.trim().toLowerCase().indexOf(selected.toLowerCase()) < 0)
                model._bcc = (model._bcc + " " + selected);
            }
          }
          myOut.write(web.mailActions(actions, model, Integer.parseInt(taskId), "", 
            Integer.parseInt(preTaskId)));
        } else {
          myOut.write(web.getAddressPage(searchKey, actions, model, Integer.parseInt(taskId), 
            Integer.parseInt(preTaskId)));
        }
      }
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_SendMail")) {
      MobileMailPage web = new MobileMailPage(me);
      String mailId = myCmdArray.elementAt(3).toString();
      String taskId = myCmdArray.elementAt(4).toString();
      String preTaskId = myCmdArray.elementAt(5).toString();
      String content = UtilCode.decode(myStr.matchValue("_content[", "]content_"));
      String mailTo = UtilCode.decode(myStr.matchValue("_mailTo[", "]mailTo_"));
      String mailCc = UtilCode.decode(myStr.matchValue("_mailCc[", "]mailCc_"));
      String mailBcc = UtilCode.decode(myStr.matchValue("_mailBcc[", "]mailBcc_"));
      String mailTitle = UtilCode.decode(myStr.matchValue("_mailTitle[", "]mailTitle_"));

      MailModel model = new MailModel();
      model._title = mailTitle;
      if ("".equals(model._title.trim())) {
        model._title = "(无主题)";
      }
      model._content = content;
      model._cc = mailCc;
      model._to = mailTo;
      model._bcc = mailBcc;
      model._id = Integer.parseInt(mailId);
      if ((preTaskId == null) || ("".equals(preTaskId))) {
        preTaskId = "0";
      }
      myOut.write(web.sendMail(model, Integer.parseInt(taskId), Integer.parseInt(preTaskId)));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_BoxList")) {
      MobileMailPage web = new MobileMailPage(me);
      String pageNow = myCmdArray.elementAt(3).toString();
      String key = myCmdArray.elementAt(4).toString();
      if ((pageNow == null) || (pageNow.equals("")))
        pageNow = "1";
      myOut.write(web.getMailBoxList(Integer.parseInt(pageNow), key));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_removeMail")) {
      MobileMailPage web = new MobileMailPage(me);
      String mids = myCmdArray.elementAt(3).toString();
      myOut.write(web.removeMail(mids, 0));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_ReadPage")) {
      MobileMailPage web = new MobileMailPage(me);
      String userId = myCmdArray.elementAt(3).toString();
      String taskId = myCmdArray.elementAt(4).toString();
      String pageNow = myCmdArray.elementAt(5).toString();

      myOut.write(web.getReadMail(Integer.parseInt(userId), Integer.parseInt(taskId), Integer.parseInt(pageNow)));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_reMail")) {
      MobileMailPage web = new MobileMailPage(me);
      String taskId = myCmdArray.elementAt(3).toString();
      if ((taskId == null) || ("".equals(taskId))) {
        taskId = "0";
      }
      if ((taskId == null) || ("".equals(taskId))) {
        taskId = "0";
      }
      myOut.write(web.reMail(Integer.parseInt(taskId), false));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_reAllMail")) {
      MobileMailPage web = new MobileMailPage(me);
      String taskId = myCmdArray.elementAt(3).toString();
      if ((taskId == null) || ("".equals(taskId))) {
        taskId = "0";
      }
      myOut.write(web.reMail(Integer.parseInt(taskId), true));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Mail_fwMail")) {
      MobileMailPage web = new MobileMailPage(me);
      String taskId = myCmdArray.elementAt(3).toString();
      if ((taskId == null) || ("".equals(taskId))) {
        taskId = "0";
      }
      myOut.write(web.reMail(Integer.parseInt(taskId), true, true));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile_Mobile_Portal_Launch_Tracking")) {
      MobileWorkListLaunchTrackingPage web = new MobileWorkListLaunchTrackingPage(me);
      String pageNow = myCmdArray.elementAt(3).toString();
      if ((pageNow == null) || (pageNow.equals(""))) {
        pageNow = "1";
      }
      String sqlCondition = UtilCode.decode(myStr.matchValue("_sqlCondition[", "]sqlCondition_"));
      if (sqlCondition == null) {
        sqlCondition = "";
      }
      myOut.write(web.getLaunchTracingList(null, sqlCondition, Integer.parseInt(pageNow)));
    }
    else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfig")) {
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.getMobileConfigPage());
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigSave")) {
      int pageSize = Integer.parseInt(myCmdArray.elementAt(3).toString());
      String pageHeaderTheme = myCmdArray.elementAt(4).toString();

      String pageFooterTheme = myCmdArray.elementAt(5).toString();
      String homePageBgImgUrl = UtilCode.decode(myStr.matchValue("_homePageBgImgUrl[", "]homePageBgImgUrl_"));
      String homePageBgBigimgurl = UtilCode.decode(myStr.matchValue("_homePageBgBigimgurl[", 
        "]homePageBgBigimgurl_"));
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.saveMobileConfigPage(pageSize, pageHeaderTheme, pageFooterTheme, homePageBgImgUrl, 
        homePageBgBigimgurl));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigItemSave")) {
      String mobileItemModelID = UtilCode.decode(myStr.matchValue("_mobileItemModelID[", "]mobileItemModelID_"));
      String mobileItemModelTitle = UtilCode.decode(myStr.matchValue("_mobileItemModelTitle[", 
        "]mobileItemModelTitle_"));
      String isShowName = myCmdArray.elementAt(5).toString();
      boolean showName = false;
      if (isShowName.equals("show")) {
        showName = true;
      }
      String mobileItemModelIconURL = UtilCode.decode(myStr.matchValue("_mobileItemModelIconURL[", 
        "]mobileItemModelIconURL_"));
      String isShowIcon = myCmdArray.elementAt(7).toString();
      boolean showIcon = false;
      if (isShowIcon.equals("show")) {
        showIcon = true;
      }
      String mobileItemModelURL = UtilCode.decode(myStr
        .matchValue("_mobileItemModelURL[", "]mobileItemModelURL_"));
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.saveMobileConfigItem(mobileItemModelID, mobileItemModelTitle, showName, 
        mobileItemModelIconURL, showIcon, mobileItemModelURL));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigGetItemEdit")) {
      String itemId = UtilCode.decode(myStr.matchValue("_itemId[", "]itemId_"));
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.getEditItemData(itemId));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigGetItemAdd")) {
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.getUUID());
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigItemEdit")) {
      String mobileItemModelID = UtilCode.decode(myStr.matchValue("_mobileItemModelID[", "]mobileItemModelID_"));
      String mobileItemModelTitle = UtilCode.decode(myStr.matchValue("_mobileItemModelTitle[", 
        "]mobileItemModelTitle_"));
      String isShowName = myCmdArray.elementAt(5).toString();
      boolean showName = false;
      if (isShowName.equals("show")) {
        showName = true;
      }
      String mobileItemModelIconURL = UtilCode.decode(myStr.matchValue("_mobileItemModelIconURL[", 
        "]mobileItemModelIconURL_"));
      String isShowIcon = myCmdArray.elementAt(7).toString();
      boolean showIcon = false;
      if (isShowIcon.equals("show")) {
        showIcon = true;
      }
      String mobileItemModelURL = UtilCode.decode(myStr
        .matchValue("_mobileItemModelURL[", "]mobileItemModelURL_"));
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.editMobileConfigItem(mobileItemModelID, mobileItemModelTitle, showName, 
        mobileItemModelIconURL, showIcon, mobileItemModelURL));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigItemDelete")) {
      String itemId = UtilCode.decode(myStr.matchValue("_itemId[", "]itemId_"));
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.deleteMobileConfigItem(itemId));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.MobileProcessPortalArgsConfigSwitchDisplay")) {
      String itemId = UtilCode.decode(myStr.matchValue("_itemId[", "]itemId_"));
      String isShow = myCmdArray.elementAt(4).toString();
      boolean show = isShow.equals("true");
      MobileConsoleConfig web = new MobileConsoleConfig(me);
      myOut.write(web.switchDisplay(itemId, show));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.Attachment")) {
      String boId = myCmdArray.elementAt(3).toString();

      String taskId = myCmdArray.elementAt(4).toString();
      String filedUUID = myCmdArray.elementAt(5).toString();

      MobileTaskPage web = new MobileTaskPage(me);
      myOut.write(web.getAttachmentPage(boId, taskId, filedUUID));
    } else if (socketCmd.equals("com.actionsoft.apps.portal.mobile.AttachmentBoFiled")) {
      String boId = myCmdArray.elementAt(3).toString();
      String fieldUUID = myCmdArray.elementAt(4).toString();
      String taskId = myCmdArray.elementAt(5).toString();
      String fileName = UtilCode.decode(myStr.matchValue("_fileName[", "]fileName_"));
      MobileTaskPage web = new MobileTaskPage(me);
      myOut.write(web.updateFiled(boId, fileName, fieldUUID, taskId));
    } else {
      return false;
    }
    return true;
  }
}