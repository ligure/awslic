package com.actionsoft.application.server.fs;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;

import com.actionsoft.application.Debug;
import com.actionsoft.application.logging.AuditLogger;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.AppError;
import com.actionsoft.awf.form.execute.FormFileUtil;
import com.actionsoft.awf.form.execute.plugins.component.electroncachet.web.ElectroncachetCreateWeb;
import com.actionsoft.awf.form.execute.plugins.component.signature.model.SignatureModel;
import com.actionsoft.awf.form.execute.plugins.component.signature.web.SignatureWeb;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.Base64;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.DownFile;
import com.actionsoft.awf.util.DownFileSecurity;
import com.actionsoft.awf.util.UpFile;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskUrgeWeb;
import com.actionsoft.deploy.transfer.imp.web.ImpWeb;
import com.actionsoft.eip.document.enterprise.util.KMUtil;
import com.actionsoft.eip.document.persion.web.KMPersionDocWeb;
import com.actionsoft.plugs.cowork.dao.CoWorkDaoFactory;
import com.actionsoft.plugs.email.dao.MailDaoFactory;
import com.actionsoft.plugs.workmanage.wm.util.WMAttacheUtil;

public class AcceptFileStream {

    private Socket myProcessSocket;
    private String myCmdLine = null;
    private String socketCmd = new String();

    public AcceptFileStream(Socket receivedSocket) {
	this.myProcessSocket = receivedSocket;
    }

    public void executeCommand() {
	UserContext me = null;
	DataInputStream myIn = null;
	OutputStreamWriter myOut = null;
	Vector myCmdArray = null;
	int success = 0;
	String clientIP = "";
	String sid = "";
	try {
	    myIn = new DataInputStream(this.myProcessSocket.getInputStream());
	    myOut = new OutputStreamWriter(
		    this.myProcessSocket.getOutputStream());

	    this.myCmdLine = myIn.readUTF();
	    UtilString myStr = new UtilString(this.myCmdLine);
	    myCmdArray = myStr.split(" ");

	    this.socketCmd = myCmdArray.elementAt(1).toString();

	    String s1 = myCmdArray.elementAt(0).toString();
	    clientIP = s1.substring(s1.lastIndexOf("}") + 1);

	    if (myCmdArray.elementAt(0).toString().equals(
		    AWFConfig._httpdConf.getSecurityCode() + clientIP)) {
		sid = myCmdArray.elementAt(2).toString();
		try {
		    if (sid.indexOf("_") > 0)
			Thread.currentThread()
				.setName("DOWNFILE--" + UserContext.getUid(sid)
					+ "--" + this.socketCmd + "--"
					+ clientIP);
		} catch (Exception e) {
		    e.printStackTrace(System.err);
		}
		if (sid.equals("")) {
		    myOut.write(AppError.getErrorPage("ERROR-0506"));
		} else
		    try {
			me = new UserContext(sid, clientIP);

			int r = me.verifySession();
			if (r == 10)
			    myOut.write(AppError.getErrorPage("ERROR-0500"));
			else if (r == 9)
			    myOut.write(AppError.getErrorPage("ERROR-0505"));
			else if ((r == 8) && (!this.socketCmd
				.equals("Document_File_Download"))) {
			    myOut.write(AppError.getErrorPage("ERROR-0507"));
			} else if (r == 7)
			    myOut.write(AppError.getErrorPage("ERROR-0508"));
			else {
			    success = 1;
			}
		    } catch (Exception e) {
			e.printStackTrace(System.err);
			myOut.write(AppError.getErrorPage("ERROR-0504", sid));
		    }
	    }

	    Debug.info("[FILE ACCESS]");

	    if (success == 1) {
		if (this.socketCmd.equals("Document_File_Upload")) {
		    String flag1 = myCmdArray.elementAt(3).toString();
		    String flag2 = myCmdArray.elementAt(4).toString();
		    String path = UtilCode
			    .decode(myStr.matchValue("_path[", "]path_"));
		    String fn = UtilCode
			    .decode(myStr.matchValue("_fn[", "]fn_"));
		    String rootDir = myCmdArray.elementAt(5).toString();

		    boolean isUpdateFileName = true;

		    if ((isUpdateFileName) && (!rootDir.equals("Document"))
			    && (!rootDir.equals("kms-doc-files"))) {
			if (rootDir.equals("MessageFile")) {
			    int upFlags = ProcessRuntimeDaoFactory
				    .createProcessInstance()
				    .appendFile(Integer.parseInt(flag2), fn);
			    if (upFlags == -1) {
				MessageQueue.getInstance()
					.putMessage(me.getUID(),
						"<I18N#已超出该字段允许文件名之和的总长度,附件>:["
							+ fn + "]<I18N#上传失败>!",
						true);
				isUpdateFileName = false;
			    }
			} else if (rootDir.equals("FormFile")) {
			    int upFlags = FormFileUtil.appendFormFile(
				    Integer.parseInt(flag1),
				    Integer.parseInt(flag2), fn);
			    if (upFlags == -1) {
				MessageQueue.getInstance()
					.putMessage(me.getUID(),
						"<I18N#已超出该字段允许文件名之和的总长度,附件>:["
							+ fn + "]<I18N#上传失败>!",
						true);
				isUpdateFileName = false;
			    }
			} else if (rootDir.equals("UrgeFile")) {
			    UserTaskUrgeWeb messageUrgeWeb = new UserTaskUrgeWeb(
				    me);
			    int upFlags = messageUrgeWeb.appendFormFile(
				    Integer.parseInt(flag1),
				    Integer.parseInt(flag2), fn);
			    if (upFlags == -1) {
				MessageQueue.getInstance()
					.putMessage(me.getUID(),
						"<I18N#已超出该字段允许文件名之和的总长度,附件>:["
							+ fn + "]<I18N#上传失败>!",
						true);
				isUpdateFileName = false;
			    }
			} else if (rootDir.equals("Email")) {
			    int upFlags = MailDaoFactory.createMail(me.getID())
				    .appendFile(Integer.parseInt(flag2), fn);
			    if (upFlags == -1) {
				MessageQueue.getInstance()
					.putMessage(me.getUID(),
						"<I18N#附件>:[" + fn
							+ "]<I18N#上传失败>!",
						true);
				isUpdateFileName = false;
			    }
			} else if (rootDir.equals("opinion")) {
			    int upFlags = ProcessRuntimeDaoFactory
				    .createUserTaskAuditMenu()
				    .appendFile(Integer.parseInt(flag2), fn);
			    if (upFlags == -1) {
				MessageQueue.getInstance()
					.putMessage(me.getUID(),
						"<I18N#附件>:[" + fn
							+ "]<I18N#上传失败>!",
						true);
				isUpdateFileName = false;
			    }
			} else if (rootDir.equals("WorkManager")) {
			    new WMAttacheUtil()
				    .appendFile(Integer.parseInt(flag2), fn);
			} else if (rootDir.equals("cachet")) {
			    ElectroncachetCreateWeb cachetWeb = new ElectroncachetCreateWeb(
				    me);
			    cachetWeb.appendFormFile(Integer.parseInt(flag1),
				    Integer.parseInt(flag2), fn);
			} else if (rootDir.equals("CoWorkData")) {
			    CoWorkDaoFactory.createCoWorkData()
				    .appendFile(Integer.parseInt(flag2), fn);
			} else if (rootDir.equals("CoWorkReData")) {
			    CoWorkDaoFactory.createCoWorkReData()
				    .appendFile(Integer.parseInt(flag2), fn);
			} else if (rootDir.equals("persionDoc")) {
			    KMPersionDocWeb pd = new KMPersionDocWeb(me);
			    if (flag2.trim().length() == 0) {
				flag2 = "0";
			    }
			    if (Integer.parseInt(flag2) == 0) {
				flag2 = pd.createFile(flag1);
			    }
			    pd.upFile(flag2, fn);
			    path = path.substring(0, path.length() - 1);
			    path = path + flag2;
			}
		    }

		    if (isUpdateFileName) {
			boolean r = false;

			r = UpFile.uploadAttachFile(myIn, path, fn,
				me.getUID());
			if (!r) {
			    if ((!rootDir.equals("Document"))
				    && (!rootDir.equals("kms-doc-files"))) {
				if (rootDir.equals("MessageFile")) {
				    ProcessRuntimeDaoFactory
					    .createProcessInstance()
					    .removeFile(Integer.parseInt(flag2),
						    fn);
				} else if (rootDir.equals("FormFile")) {
				    FormFileUtil.removeFormFile(
					    Integer.parseInt(flag1),
					    Integer.parseInt(flag2), fn);
				} else if (rootDir.equals("UrgeFile")) {
				    UserTaskUrgeWeb messageUrgeWeb = new UserTaskUrgeWeb(
					    me);
				    messageUrgeWeb.removeFormFile(
					    Integer.parseInt(flag1),
					    Integer.parseInt(flag2), fn);
				} else if (rootDir.equals("Email")) {
				    MailDaoFactory.createMail(me.getID())
					    .removeFile(Integer.parseInt(flag2),
						    fn);
				} else if (rootDir.equals("WorkManager")) {
				    new WMAttacheUtil().removeFile(
					    Integer.parseInt(flag2), fn, me);
				} else if (rootDir.equals("cachet")) {
				    ElectroncachetCreateWeb cachetWeb = new ElectroncachetCreateWeb(
					    me);
				    cachetWeb.removeFormFile(
					    Integer.parseInt(flag1),
					    Integer.parseInt(flag2), fn);
				} else if (rootDir.equals("CoWorkData")) {
				    CoWorkDaoFactory.createCoWorkData()
					    .removeFile(Integer.parseInt(flag2),
						    fn);
				} else if (rootDir.equals("CoWorkReData")) {
				    CoWorkDaoFactory.createCoWorkReData()
					    .removeFile(Integer.parseInt(flag2),
						    fn);
				} else if (rootDir.equals("persionDoc")) {
				    KMPersionDocWeb pd = new KMPersionDocWeb(
					    me);
				    pd.upFile(flag2, "");
				}
			    }
			    MessageQueue.getInstance().putMessage(me.getUID(),
				    "<I18N#附件>:[" + fn + "]<I18N#上传失败>!");
			    AuditLogger.logFile(fn, path, false, true);
			    myOut.write("error");
			} else {
			    if (rootDir.equals("TransferFile")) {
				ImpWeb imp = new ImpWeb(me);
				imp.getUpFileEnd(fn);
			    }
			    if (rootDir.equals("WebOffice")) {
				if ((!fn.startsWith("Doc_"))
					&& (!fn.endsWith(".pdf")))
				    AuditLogger.logFile(fn, path, true, true);
			    } else {
				AuditLogger.logFile(fn, path, true, true);
			    }

			    myOut.write("OK");
			}
		    } else {
			AuditLogger.logFile(fn, path, false, true);
		    }
		} else if (this.socketCmd.equals("Document_File_Download")) {
		    String p1 = myCmdArray.elementAt(3).toString();
		    String p2 = myCmdArray.elementAt(4).toString();
		    String path = UtilCode
			    .decode(myStr.matchValue("_path[", "]path_"));
		    String fn = UtilCode
			    .decode(myStr.matchValue("_fn[", "]fn_"));
		    try {
			String a = URLDecoder.decode(fn, "UTF-8");
			fn = URLEncoder.encode(a, "UTF-8");
			fn = URLDecoder.decode(fn, "UTF-8");
		    } catch (Exception e) {
			fn = URLEncoder.encode(fn, "UTF-8");
		    }

		    if (fn.indexOf("AWS-ONLINE-OFFICE-PREFIX") > -1) {
			fn = fn.substring("AWS-ONLINE-OFFICE-PREFIX".length());
		    }

		    if ((((path.indexOf("/FormFile/") != -1)
			    || (path.indexOf("\\FormFile\\") != -1)))
			    && (!new DownFileSecurity().isAccessFormFile(me, p1,
				    p2, fn, path))) {
			myOut.write("警告,您无权访问该文件!");
		    } else {
			if (AWFConfig._awfServerConf.getPlatform().toLowerCase()
				.equals("windows")) {
			    path = new UtilString(path).replace("/", "\\");
			    AuditLogger.logFile(fn, path,
				    DownFile.downAttachLoad(
					    this.myProcessSocket,
					    path + "\\" + fn),
				    false);
			} else {
			    AuditLogger.logFile(fn, path,
				    DownFile.downAttachLoad(
					    this.myProcessSocket,
					    path + "/" + fn),
				    false);
			}

			String tmpPath = new UtilString(path).replace("\\",
				"/");
			if (tmpPath.indexOf("Document/group") > 0) {
			    int fileid = DBSql
				    .getInt("select id from eip_enterprisedoc_file where filename='"
					    + fn + "' and cardid="
					    + Integer.parseInt(p2), "id");
			    KMUtil.addDocReadHistoryLog(Integer.parseInt(p2),
				    fileid, me, 0, 0);
			}
		    }
		} else if (this.socketCmd.equals("APP_File_Download")) {
		    String type = myCmdArray.elementAt(3).toString();
		    String pathname = UtilCode
			    .decode(myStr.matchValue("_path[", "]path_"));
		    AuditLogger.logFile(pathname, type, DownFile.downAPPFile(
			    this.myProcessSocket, type, pathname), false);
		} else if (this.socketCmd.equals("WebOffice_Signature_Save")) {
		    SignatureWeb web = new SignatureWeb(me);
		    String signatureId = UtilCode.decode(
			    myStr.matchValue("_signatureId[", "]signatureId_"));

		    String _password = UtilCode.decode(
			    myStr.matchValue("_password[", "]password_"));
		    String _markname = UtilCode.decode(
			    myStr.matchValue("_markName[", "]markName_"));
		    String tmpMarkbody = UtilCode.decode(
			    myStr.matchValue("_markBody[", "]markBody_"));
		    String _markSize = UtilCode.decode(
			    myStr.matchValue("_markSize[", "]markSize_"));
		    String _markType = UtilCode.decode(
			    myStr.matchValue("_markType[", "]markType_"));
		    String _markPath = UtilCode.decode(
			    myStr.matchValue("_markPath[", "]markPath_"));
		    String _userName = UtilCode.decode(
			    myStr.matchValue("_userName[", "]userName_"));
		    byte[] _markBody = Base64.decode(tmpMarkbody.getBytes());
		    String fn = UtilCode
			    .decode(myStr.matchValue("_fn[", "]fn_"));
		    if (signatureId.trim().length() == 0) {
			signatureId = "0";
		    }
		    SignatureModel model = new SignatureModel();
		    model._signatureId = Integer.parseInt(signatureId);
		    model._markbody = _markBody;
		    model._markname = _markname;
		    model._password = _password;
		    model._username = _userName;
		    model._marktype = _markType;
		    model._markpath = _markPath;
		    model._marksize = Integer.parseInt(_markSize);
		    myOut.write(web.saveSignature(model));
		    web = null;
		}

	    }

	} catch (Exception e) {
	    try {
		myOut.write(AppError.getErrorPage("ERROR-0202",
			"执行Socket指令：[" + this.socketCmd + "] 出错!"));
	    } catch (IOException e2) {
		e2.printStackTrace(System.err);
	    }
	    e.printStackTrace(System.err);
	} finally {
	    try {
		myOut.flush();
		myOut.close();
		myIn.close();
		this.myProcessSocket.close();
	    } catch (IOException e) {
		try {
		    myOut.write(AppError.getErrorPage("ERROR-0106",
			    "执行Socket指令：[" + this.socketCmd + "] 出错!\n指令内容："
				    + myCmdArray));
		} catch (IOException e3) {
		    e3.printStackTrace();
		}

		e.printStackTrace(System.err);
	    }
	}
    }

    private void debugTo(OutputStreamWriter myOut) {
	try {
	    myOut.write(this.myCmdLine);
	} catch (Exception localException) {
	}
    }
}