package com.actionsoft.sdk.local.level0;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.workflow.design.model.WrokFlowEmailTemplateModel;
import com.actionsoft.awf.workflow.design.util.WFFlexDesignEmailTemplateUtil;
import com.actionsoft.eai.shortmessage.SMSContext;
import com.actionsoft.eai.shortmessage.SendSMSUtil;
import com.actionsoft.plugs.email.dao.MailDaoFactory;
import com.actionsoft.plugs.email.model.MailModel;
import com.actionsoft.plugs.email.model.MailTaskModel;
import com.actionsoft.plugs.email.util.AWSMailUtil;
import com.actionsoft.sdk.AWSSDKException;

public class IMAPI {

    private static IMAPI imAPI;
    public static int _CREATE_USERCONTEXT_ERROR = -1;
    public static int _SENDMAIL_OK = 0;
    public static int _SENDMAIL_ERROR = -9;

    public static IMAPI getInstance() {
	if (imAPI == null)
	    imAPI = new IMAPI();
	return imAPI;
    }

    public int sendMail(String mailFrom, String mailTo, String subject,
	    String content) {
	UserContext uc = null;
	try {
	    uc = new UserContext(mailFrom);
	} catch (Exception e) {
	    return _CREATE_USERCONTEXT_ERROR;
	}
	MailModel model = new MailModel();
	model._content = content;
	model._createUser = uc.getUserModel().getUID();
	model._id = 0;
	model._isImportant = false;
	model._mailFrom = uc.getDepartmentModel().getDepartmentName();
	model._mailSize = 0;
	model._mailType = 0;
	model._title = subject;
	model._to = mailTo;
	String returnCode = AWSMailUtil.getInstance().SendMail(uc, 0, model);
	try {
	    int mailId = Integer.parseInt(returnCode);
	    return mailId;
	} catch (Exception e) {
	}
	return _SENDMAIL_ERROR;
    }

    public int sendMail(String mailFrom, String mailTo, String mailToCC,
	    String subject, String content) {
	return sendMail(mailFrom, mailTo, mailToCC, subject, content, null);
    }

    public int sendMail(String mailFrom, String mailTo, String mailToCC,
	    String subject, String content, Hashtable attachments) {
	UserContext uc = null;
	int mailId = 0;
	try {
	    uc = new UserContext(mailFrom);
	} catch (Exception e) {
	    return _CREATE_USERCONTEXT_ERROR;
	}
	MailModel model = new MailModel();
	model._content = content;
	model._createUser = uc.getUserModel().getUID();
	model._id = 0;
	model._isImportant = false;
	model._mailFrom = uc.getDepartmentModel().getDepartmentName();
	model._mailSize = 0;
	model._mailType = 0;
	model._title = subject;
	model._to = mailTo;
	model._cc = mailToCC;

	if (attachments != null) {
	    mailId = MailDaoFactory.createMail(uc.getID()).create(model);
	    for (Enumeration e = attachments.keys(); e.hasMoreElements();) {
		String fileName = (String) e.nextElement();
		byte[] fileContent = (byte[]) attachments.get(fileName);
		String tmpFileName = System.currentTimeMillis() + ".tmp";
		File tmpFile = new File("tmp/" + tmpFileName);
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(tmpFile);
		    if (fileContent != null)
			out.write(fileContent);
		    out.flush();
		    AWSMailUtil.getInstance().appendFileToMail(uc.getID(),
			    mailId, fileName, "tmp/" + tmpFileName);
		} catch (Exception fe) {
		    fe.printStackTrace(System.err);
		} finally {
		    try {
			if (out != null)
			    out.close();
		    } catch (Exception localException2) {
		    }
		    tmpFile.delete();
		}
	    }
	    MailTaskModel taskModel = new MailTaskModel();
	    taskModel._bindId = mailId;
	    taskModel._isImportant = model._isImportant;
	    taskModel._isRead = true;
	    taskModel._mailBox = -1;
	    taskModel._mailSize = model._mailSize;
	    taskModel._owner = model._createUser;
	    taskModel._createTime = model._createDate;
	    taskModel._title = model._title;
	    int mailTaskId = MailDaoFactory.createMailTask(uc.getID()).create(
		    taskModel);
	    String returnCode = AWSMailUtil.getInstance().SendMail(uc,
		    mailTaskId, model);
	    return mailId;
	}
	String returnCode = AWSMailUtil.getInstance().SendMail(uc, 0, model);
	try {
	    mailId = Integer.parseInt(returnCode);
	    return mailId;
	} catch (Exception localException4) {
	}
	return _SENDMAIL_ERROR;
    }

    public HashMap sendShortMessage(String senderUID, String mobileNo,
	    String content) throws AWSSDKException {
	SMSContext sms = new SMSContext();
	UserContext owner = null;
	try {
	    owner = new UserContext(senderUID);
	} catch (Exception e) {
	    throw new AWSSDKException("senderUID参数不正确，该参数必须为一个合法的AWS登录帐户");
	}
	if (!AWFConfig._awfServerConf.getShortmessageServer().toLowerCase()
		.equals("on")) {
	    throw new AWSSDKException("AWS服务器的短信服务未开通");
	}
	sms.setCompanyName(owner.getCompanyModel().getCompanyName());
	sms.setDepartmentName(owner.getDepartmentModel().getDepartmentName());
	sms.setUid(owner.getUID());
	sms.setUserName(owner.getUserModel().getUserName());
	sms.setMobileCode(mobileNo.toString().trim());
	SendSMSUtil send = new SendSMSUtil(sms);
	return send.send(content);
    }

    public String[] getMailTemplate(String templateNo) throws AWSSDKException {
	WrokFlowEmailTemplateModel wrokFlowEmailTemplateModel = WFFlexDesignEmailTemplateUtil
		.getEmailTemplateModel(templateNo);
	if (wrokFlowEmailTemplateModel == null) {
	    throw new AWSSDKException("不存在编号为 [" + templateNo + "]邮件模板");
	}
	String[] emailTemplete = new String[5];
	emailTemplete[0] = wrokFlowEmailTemplateModel.getFromUser();
	emailTemplete[1] = wrokFlowEmailTemplateModel.getCcUser();
	emailTemplete[2] = wrokFlowEmailTemplateModel.getTitle();
	emailTemplete[3] = wrokFlowEmailTemplateModel.getMainBody();
	emailTemplete[4] = wrokFlowEmailTemplateModel.getNo();
	return emailTemplete;
    }

    public String[] getMailDefaultTemplateByGroupName(String groupName)
	    throws AWSSDKException {
	WrokFlowEmailTemplateModel wrokFlowEmailTemplateModel = WFFlexDesignEmailTemplateUtil
		.getDefaultEmailTemplateModel(groupName);
	if (wrokFlowEmailTemplateModel == null) {
	    throw new AWSSDKException("不存在分类为 [" + groupName + "]系统默认邮件模板");
	}
	String[] emailTemplete = new String[5];
	emailTemplete[0] = wrokFlowEmailTemplateModel.getFromUser();
	emailTemplete[1] = wrokFlowEmailTemplateModel.getCcUser();
	emailTemplete[2] = wrokFlowEmailTemplateModel.getTitle();
	emailTemplete[3] = wrokFlowEmailTemplateModel.getMainBody();
	emailTemplete[4] = wrokFlowEmailTemplateModel.getNo();
	return emailTemplete;
    }

    public int sendMailByModel(String templateNo, String sender, String mailTo)
	    throws AWSSDKException {
	return sendMailByModel(templateNo, sender, mailTo, 0, 0, null);
    }

    public int sendMailByModel(String templateNo, String sender, String mailTo,
	    int processInstanceId, int taskInstanceId, Hashtable param)
	    throws AWSSDKException {
	WrokFlowEmailTemplateModel wrokFlowEmailTemplateModel = WFFlexDesignEmailTemplateUtil
		.getEmailTemplateModel(templateNo);
	if (wrokFlowEmailTemplateModel == null) {
	    throw new AWSSDKException("不存在编号为 [" + templateNo + "]邮件模板");
	}
	String[] emailTemplete = new String[4];
	emailTemplete[0] = wrokFlowEmailTemplateModel.getFromUser();
	emailTemplete[1] = wrokFlowEmailTemplateModel.getCcUser();
	String title = wrokFlowEmailTemplateModel.getTitle();
	if (title == null) {
	    title = "空标题";
	}
	emailTemplete[2] = title;
	emailTemplete[3] = wrokFlowEmailTemplateModel.getMainBody();
	UserContext me = null;
	try {
	    me = new UserContext(sender);
	} catch (Exception e) {
	    throw new AWSSDKException("构造用户 [" + sender + "]失败，发送邮件失败");
	}
	if (param != null) {
	    for (Iterator it = param.keySet().iterator(); it.hasNext();) {
		String key = (String) it.next();
		String value = (String) param.get(key);
		emailTemplete[0] = emailTemplete[0].replaceAll(key, value);
		emailTemplete[1] = emailTemplete[1].replaceAll(key, value);
		emailTemplete[2] = emailTemplete[2].replaceAll(key, value);
		emailTemplete[3] = emailTemplete[3].replaceAll(key, value);
	    }
	}
	String mailFrom = processInstanceId > 0 ? RuleAPI.getInstance()
		.executeRuleScript(emailTemplete[0], me, processInstanceId,
			taskInstanceId) : emailTemplete[0];
	mailFrom = mailFrom == null || mailFrom.trim().length() == 0 ? sender
		: mailFrom;
	String mailCC = processInstanceId > 0 ? RuleAPI.getInstance()
		.executeRuleScript(emailTemplete[1], me, processInstanceId,
			taskInstanceId) : emailTemplete[1];
	title = processInstanceId > 0 ? RuleAPI.getInstance()
		.executeRuleScript(emailTemplete[2], me, processInstanceId,
			taskInstanceId) : emailTemplete[2];
	String content = processInstanceId > 0 ? RuleAPI.getInstance()
		.executeRuleScript(emailTemplete[3], me, processInstanceId,
			taskInstanceId) : emailTemplete[3];
	return getInstance().sendMail(mailFrom, mailTo, mailCC, title, content);
    }
}