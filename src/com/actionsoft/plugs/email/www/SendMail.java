package com.actionsoft.plugs.email.www;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.actionsoft.application.server.Console;
import com.actionsoft.application.server.IniSystem;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.plugs.email.conf.MailServerConf;
import com.actionsoft.plugs.email.conf.MailServerModel;

public class SendMail {
    private MimeMessage mimeMsg;
    private Properties props;
    private String mailAccount = "";

    private String password = "";
    private Multipart mp;
    private Session session;

    public SendMail(String awsUID, String mailAccount, String password) {
	MailServerModel sm = MailServerConf.getConfModel(mailAccount);
	if (sm == null)
	    MessageQueue.getInstance().putMessage(awsUID,
		    "<I18N#邮件服务器没有配置可用的POP服务，邮件[" + mailAccount + "]无法发送>",
		    true);
	else {
	    this.props = MailServerConf.getConfModel(mailAccount)
		    .getSessionProps();
	}

	this.mailAccount = mailAccount;
	this.password = password;
	createMimeMessage();
    }

    private boolean createMimeMessage() {
	try {
	    Authenticator auth = new PopupAuthenticator(this.mailAccount,
		    this.password);
	    this.session = Session.getInstance(this.props, auth);
	    this.session.setDebug(false);
	} catch (Exception e) {
	    System.err.println("获取邮件会话对象时发生错误!" + e);
	    return false;
	}

	try {
	    this.mimeMsg = new MimeMessage(this.session);
	    this.mp = new MimeMultipart();
	    return true;
	} catch (Exception e) {
	    System.err.println("创建MIME邮件对象失败!" + e);
	}
	return false;
    }

    public boolean setSubject(String mailSubject) {
	try {
	    this.mimeMsg.setSubject(mailSubject);
	    return true;
	} catch (Exception e) {
	    System.err.println("设置邮件主题发生错误!" + e);
	}
	return false;
    }

    public boolean setBody(String mailBody) {
	try {
	    MimeBodyPart bp = new MimeBodyPart();
	    bp.setContent(
		    "<meta http-equiv=Content-Type content=text/html;charset=GB2312>"
			    + mailBody, "text/html;charset=GB2312");
	    this.mp.addBodyPart(bp);
	    return true;
	} catch (Exception e) {
	    System.err.println("设置邮件正文时发生错误!" + e);
	}
	return false;
    }

    public boolean addFileAffix(String filename) {
	try {
	    MimeBodyPart bp = new MimeBodyPart();
	    FileDataSource fields = new FileDataSource(filename);
	    bp.setDataHandler(new DataHandler(fields));
	    bp.setFileName(MimeUtility.encodeText(fields.getName(), "GB2312",
		    "B"));
	    this.mp.addBodyPart(bp);
	    return true;
	} catch (Exception e) {
	    System.err.println("增加邮件附件:" + filename + "发生错误" + e);
	}
	return false;
    }

    public boolean setFrom(String from) {
	try {
	    this.mimeMsg.setFrom(encodeNameUtf8(new InternetAddress(from)));
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean setTo(String to) {
	if (to == null)
	    return false;
	try {
	    this.mimeMsg.setRecipients(Message.RecipientType.TO,
		    encodeNameUtf8(InternetAddress.parse(to)));
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    private InternetAddress encodeNameUtf8(InternetAddress raw) {
	if (raw != null) {
	    String name = raw.getPersonal();
	    if ((name != null) && (!"".equals(name)))
		try {
		    raw.setPersonal(MimeUtility.encodeText(name, "UTF-8", "B"));
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	return raw;
    }

    private InternetAddress[] encodeNameUtf8(InternetAddress[] raws) {
	if (raws != null) {
	    for (int i = 0; i < raws.length; ++i) {
		encodeNameUtf8(raws[i]);
	    }
	}
	return raws;
    }

    public boolean setCopyTo(String copyto) {
	if (copyto == null)
	    return false;
	try {
	    this.mimeMsg.setRecipients(Message.RecipientType.CC,
		    encodeNameUtf8(InternetAddress.parse(copyto)));
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean setBCC(String copyto) {
	if (copyto == null)
	    return false;
	try {
	    this.mimeMsg.setRecipients(Message.RecipientType.BCC,
		    encodeNameUtf8(InternetAddress.parse(copyto)));
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean setReplyTo(String replyTo) {
	if (replyTo == null)
	    return false;
	try {
	    this.mimeMsg.setReplyTo(encodeNameUtf8(InternetAddress
		    .parse(replyTo)));
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean setNotification(String notification) {
	if (notification == null)
	    return false;
	try {
	    this.mimeMsg.addHeader("Disposition-Notification-To", notification);
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean setPriority(String priority) {
	if (priority == null)
	    return false;
	try {
	    this.mimeMsg.addHeader("X-Priority", priority);
	    return true;
	} catch (Exception e) {
	}
	return false;
    }

    public boolean send() {
	return send("");
    }

    public boolean send(String awsFlag) {
	try {
	    this.mimeMsg.setContent(this.mp);
	    this.mimeMsg.saveChanges();
	    this.mimeMsg.setSentDate(new Date());
	    this.mimeMsg.addHeader(
		    "X-Mailer",
		    "Actionsoft AWS-BPM Platform,Core Version="
			    + Console.getBuildVersion());
	    this.mimeMsg.addHeader("X-AWSInfo", awsFlag);
	    String[] language = { "GBK", "GB2312" };
	    this.mimeMsg.setContentLanguage(language);
	    Transport.send(this.mimeMsg, this.mimeMsg.getAllRecipients());
	    return true;
	} catch (Exception e) {
	    System.err.println("发送邮件失败!");
	    e.printStackTrace(System.err);
	}
	return false;
    }

    public static void main(String[] args) throws Exception {
	IniSystem.ini();
	SendMail sendMail = new SendMail("admin", "mis_test@letv.com",
		"123.mis");
	sendMail.setBody("<a href='http://www.leshiren.cn'>乐视人</a>");
	sendMail.setFrom("mis_test@letv.com");
	sendMail.setTo("shengliguo@letv.com");
	sendMail.setSubject("测试邮件");
	sendMail.send();
    }
}