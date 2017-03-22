package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.Cookie;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.actionsoft.application.portal.web.OnlineWeb;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.application.server.LICENSE;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.system.runtimemanager.web.AWFRuntimeManagerConfigWeb;
import com.actionsoft.application.system.runtimemanager.web.AWFRuntimeManagerLogWeb;
import com.actionsoft.application.system.runtimemanager.web.AWFRuntimeManagerMonitorWeb;
import com.actionsoft.application.system.runtimemanager.web.OSMonitorWeb;
import com.actionsoft.awf.commons.functionhistory.RecordWeb;
import com.actionsoft.awf.login.LoginActionProxy;
import com.actionsoft.awf.login.LogoutPage;
import com.actionsoft.awf.login.impl.BPMClientLoginImpl;
import com.actionsoft.awf.login.impl.BPMConsoleLoginImpl;
import com.actionsoft.awf.login.impl.CoELoginImpl;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.session.SessionImpl;
import com.actionsoft.awf.session.model.SessionModel;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level1.SessionAPI;
import com.actionsoft.Util;
/**
 * 
 * @description 增加了SSO验证登入内容，登入后拿到sso的cookie值来设置自己的cookie值,以后刷新后使用自己的cookie值。
 * sso的cookie值只能使用一次，设置自己的cookie值哎皮肤的html中
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:42:42
 */
public class SystemSocketCommandBak
  implements BaseSocketCommand
{
  public boolean executeCommand(UserContext me, Socket myProcessSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
    throws Exception
  {
    if (socketCmd.equals("Logout")) {
    	 LogoutPage web = new LogoutPage();
         String path = myCmdArray.elementAt(3).toString();
         myOut.write(web.logoutAction(me, path));
         web = null;
    }
    
    else if (socketCmd.equals("Login")) {
//---------------------------------HttpClient Startup--------------------------------------------------------------------------//
     //value为页面中传过来的cookie值
    	
       String value = UtilCode.decode(myStr.matchValue("_pwd[", "]pwd_"));
       String uid = null;
       //判断pwd中的值是否为admin，如是admin直接获取用户中的值。
       if("admin".equals(value)){
    	 uid = myCmdArray.elementAt(2).toString();;   
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
   		String urlmd5 = "site="+site+"&time="+time+"&type="+type+"&v="+value+"rfvfghjui9";
   		String sign = Util.getMD5Str(urlmd5);
   	    //SSO登录验证URL
   		String url ="http://sso.family.com:20008/transcode.php?site="+site+"&time="+time+"&type="+type+"&v="+value+"&sign="+sign+"";
   		try{
   			GetMethod gt = new GetMethod(url);
   			ht.executeMethod(gt);
   		    //用来存放json返回的值。
   			byte[] responseBody = gt.getResponseBody();
   			responseMsg = new String(responseBody);
   			//获取到json串利用&符号进行截取
   			String [] strarr = responseMsg.split("&");
   			//再利用=号截取出uid
   			int i = strarr.length;
   			String [] strarr2 = strarr[1].split("=");
   				uid = strarr2[1];				
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
//---------------------------------HttpClient End--------------------------------------------------------------------------//
      String userid = uid;
      String bip = myCmdArray.elementAt(3).toString();
      String lang = myCmdArray.elementAt(4).toString();
      String pwd = "123456";
      String UserCert = UtilCode.decode(myStr.matchValue("_UserCert[", "]UserCert_"));
      String ContainerName = UtilCode.decode(myStr.matchValue("_ContainerName[", "]ContainerName_"));
      String UserSignedData = UtilCode.decode(myStr.matchValue("_UserSignedData[", "]UserSignedData_"));
      String p1 = UtilCode.decode(myStr.matchValue("_p1[", "]p1_"));
      String p2 = UtilCode.decode(myStr.matchValue("_p2[", "]p2_"));
      String p3 = UtilCode.decode(myStr.matchValue("_p3[", "]p3_"));
      String p4 = UtilCode.decode(myStr.matchValue("_p4[", "]p4_"));
      String p5 = UtilCode.decode(myStr.matchValue("_p5[", "]p5_"));

      LoginActionProxy web = new LoginActionProxy();
      String defaultSystemKey = "21";
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
      web = new LoginActionProxy(defaultSystemKey);
      try{
    	  myOut.write(web.loginSystem(userid, pwd, bip, params));

      }catch(Exception e){
    	  e.printStackTrace();
      }
      web = null;
    }
    else if (socketCmd.equals("LoginBPM")) {
      String userid = myCmdArray.elementAt(2).toString();
      String bip = myCmdArray.elementAt(3).toString();
      String lang = myCmdArray.elementAt(4).toString();
      String pwd = UtilCode.decode(myStr.matchValue("_pwd[", "]pwd_"));
      LoginActionProxy web = new LoginActionProxy();
      String defaultSystemKey = "19";
      try {
        defaultSystemKey = web.getDefaultSystemKey(userid);
      } catch (Exception localException) {
      }
      if (LICENSE.getVersionType().equals("1")) {
        myOut.write("<script>alert('" + I18nRes.findValue(me.getLanguage(), "版本不支持BPM平台管理!") + "');history.go(-1);</script>");
      }
      else if (Integer.parseInt(defaultSystemKey) > 20) {
        myOut.write("<script>alert('" + I18nRes.findValue(lang, "权限不够!") + "');history.go(-1);</script>");
      } else {
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
        params.put("lang", lang);
        params.put("p1", p1);
        params.put("p2", p2);
        params.put("p3", p3);
        params.put("p4", p4);
        params.put("p5", p5);
        web = new LoginActionProxy(defaultSystemKey);
        myOut.write(web.loginSystem(userid, pwd, bip, params));
        web = null;
      }

    }
    else if (socketCmd.equals("LoginCoE")) {
      String userid = myCmdArray.elementAt(2).toString();
      String bip = myCmdArray.elementAt(3).toString();
      String lang = myCmdArray.elementAt(4).toString();
      String pwd = UtilCode.decode(myStr.matchValue("_pwd[", "]pwd_"));

      LoginActionProxy web = new LoginActionProxy();
      String defaultSystemKey = "9";

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
      params.put("lang", lang);
      params.put("p1", p1);
      params.put("p2", p2);
      params.put("p3", p3);
      params.put("p4", p4);
      params.put("p5", p5);
      web = new LoginActionProxy(defaultSystemKey);
      myOut.write(web.loginSystem(userid, pwd, bip, params));
      web = null;
    }
    else if (socketCmd.equals("LoginBPM_UpdateSkins")) {
      BPMConsoleLoginImpl web = new BPMConsoleLoginImpl("20");
      String skinsName = myCmdArray.elementAt(3).toString();
      myOut.write(web.updateSkins(me, skinsName));
      web = null;
    } else if (socketCmd.equals("LoginBPM_UpdateRoleRange")) {
      BPMConsoleLoginImpl web = new BPMConsoleLoginImpl("20");
      String roleRange = myCmdArray.elementAt(3).toString();
      myOut.write(web.updateRoleRange(me, roleRange));
      web = null;
    } else if (socketCmd.equals("System_Online_List")) {
      OnlineWeb web = new OnlineWeb(me);
      myOut.write(web.getOnline());
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Main")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getMainWeb());
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Main")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getMonitorMainWeb());
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Client_Server")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getMonitorClientServerWeb(hostName));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Data_CPU")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getSystemCPUUsageXML());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Data_Memory")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getSystemMemoryUsageXML());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Data_DBPool")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getAWSDBPoolUsageXML());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Data_Thread")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getAWSThreadUsageXML());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Monitor_Data_Session")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getAWSSessionUsageXML());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Monitor_OS_Process")) {
      OSMonitorWeb web = new OSMonitorWeb(me);
      String type = myCmdArray.elementAt(3).toString();
      myOut.write(web.getOSMonitorWeb(type));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Main")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getConfigMainWeb(hostName));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Config_Xml_Data")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      myOut.write(web.getConfigDataXML());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Config_System")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getSystemConfigWeb(hostName, configType));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Xml_Data_System")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      myOut.write(web.getConfigSystemDataXML(configType));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Xml_Data_System_Save")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      String cluster = myCmdArray.elementAt(4).toString();
      String configValue = UtilCode.decode(myStr.matchValue("_configValue[", "]configValue_"));
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      myOut.write(web.saveSystemConfigData(configType, configValue, hostName, cluster));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Config_Email")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getEmailConfigWeb(hostName, configType));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Group")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getGroupConfigWeb(hostName, configType));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Xml_Data_Group")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      myOut.write(web.getConfigGroupDataXML(configType));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Group_Remove")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      String key = UtilCode.decode(myStr.matchValue("_key[", "]key_"));
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      myOut.write(web.removeGroupConfig(configType, key, hostName, "1"));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Config_Group_Save")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String configType = myCmdArray.elementAt(3).toString();
      String cluster = myCmdArray.elementAt(4).toString();
      String subConfigValue = UtilCode.decode(myStr.matchValue("_subConfigValue[", "]subConfigValue_"));
      String mainConfigValue = UtilCode.decode(myStr.matchValue("_mainConfigValue[", "]mainConfigValue_"));
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      myOut.write(web.saveGroupConfig(configType, mainConfigValue, subConfigValue, hostName, cluster));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Log_Main")) {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getLogMainWeb(hostName));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Monitor_ASH_Main")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      hostName = (hostName == null) || (hostName.trim().length() == 0) ? "" : hostName;
      myOut.write(web.getASHMainWeb(hostName));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Monitor_ASH_XmlData")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      String start = myCmdArray.elementAt(3).toString();
      String limit = myCmdArray.elementAt(4).toString();
      myOut.write(web.getASHXmlData(Integer.parseInt(start), Integer.parseInt(limit)));
      web = null;
    }
    else if (socketCmd.equals("AWF_RuntimeManager_Monitor_ASH_Exp_Excel")) {
      AWFRuntimeManagerMonitorWeb web = new AWFRuntimeManagerMonitorWeb(me);
      myOut.write(web.getExcelDataOfAWSPerformance());
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Config_ModelTree_JSon")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      myOut.write(web.getJsonData(requestType, param));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Log_ModelTree_JSon")) {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      myOut.write(web.getJsonData(requestType, param));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Log_File_Page"))
    {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String logFileName = myCmdArray.elementAt(3).toString();
      String hostName = UtilCode.decode(myStr.matchValue("_BPMHostName[", "]BPMHostName_"));
      myOut.write(web.getlogFilePage(logFileName, hostName));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Log_LogConsole")) {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      myOut.write(web.getLogConsole(requestType, param));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Log_Export")) {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      String qs = UtilCode.decode(myStr.matchValue("_QStr[", "]QStr_"));
      myOut.write(web.exportLog(requestType, param, qs));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Log_GridList")) {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String start = myCmdArray.elementAt(3).toString();
      String limit = myCmdArray.elementAt(4).toString();
      String requestType = myCmdArray.elementAt(5).toString();
      String param = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      String qs = UtilCode.decode(myStr.matchValue("_QStr[", "]QStr_"));
      myOut.write(web.queryLogData(start, limit, requestType, param, qs));
      web = null;
    } else if (socketCmd.equals("AWF_RuntimeManager_Log_File_Viewer")) {
      AWFRuntimeManagerLogWeb web = new AWFRuntimeManagerLogWeb(me);
      String logFileName = myCmdArray.elementAt(3).toString();
      String mode = myCmdArray.elementAt(4).toString();
      String pageSize = myCmdArray.elementAt(5).toString();
      pageSize = (pageSize == null) || (pageSize.trim().length() == 0) ? "0" : pageSize;
      myOut.write(web.getlogFileViewer(logFileName, mode, Integer.parseInt(pageSize)));
      web = null;
    } else if (socketCmd.equals("AWF_Reload_Start")) {
      AWFRuntimeManagerConfigWeb web = new AWFRuntimeManagerConfigWeb(me);
      myOut.write(web.restartServer());
      web = null;
    } else if (socketCmd.equals("FunctionRecord_Load_Div")) {
      String system = myCmdArray.elementAt(3).toString();
      String pageNow = myCmdArray.elementAt(4).toString();
      RecordWeb web = new RecordWeb(me);
      myOut.write(web.buildDIVHtml(system, pageNow));
      web = null;
    } else if (socketCmd.equals("FunctionRecord_Load_Content")) {
      String system = myCmdArray.elementAt(3).toString();
      String pageNow = myCmdArray.elementAt(4).toString();
      RecordWeb web = new RecordWeb(me);
      myOut.write(web.buildContentHtml(system, pageNow));
      web = null;
    } else if (socketCmd.equals("Switching_CoE")) {
      SessionImpl mySession = new SessionImpl(Integer.parseInt(AWFConfig._awfServerConf.getSessionOnlineLife()));
      SessionModel sessionModel = mySession.registerSession(me.getUID(), me.getIP(), me.getLanguage(), "");
      me = new UserContext(sessionModel._sessionId, me.getLanguage());

      if (!LICENSE.isCOE()) {
        myOut.write("<script>alert('" + I18nRes.findValue(me.getLanguage(), "AWS 服务器使用的软件许可证不支持 CoE，登录被拒绝") + "');history.go(-1);</script>");
      }
      else {
        CoELoginImpl web = new CoELoginImpl("9");
        myOut.write(web.getSystemPage(me));
      }
    } else if (socketCmd.equals("Switching_BPM")) {
      SessionImpl mySession = new SessionImpl(Integer.parseInt(AWFConfig._awfServerConf.getSessionOnlineLife()));
      SessionModel sessionModel = mySession.registerSession(me.getUID(), me.getIP(), me.getLanguage(), "");
      me = new UserContext(sessionModel._sessionId, me.getLanguage());

      BPMClientLoginImpl web = new BPMClientLoginImpl("21");
      myOut.write(web.getSystemPage(me));
      web = null;
    } else if (socketCmd.equals("Switching_Console")) {
      SessionImpl mySession = new SessionImpl(Integer.parseInt(AWFConfig._awfServerConf.getSessionOnlineLife()));
      SessionModel sessionModel = mySession.registerSession(me.getUID(), me.getIP(), me.getLanguage(), "");
      me = new UserContext(sessionModel._sessionId, me.getLanguage());

      String defaultSystemKey = "19";
      LoginActionProxy web = new LoginActionProxy();
      try {
        defaultSystemKey = web.getDefaultSystemKey(me.getUID());
      } catch (Exception localException1) {
      }
      if (Integer.parseInt(defaultSystemKey) > 20) {
        myOut.write("<script>alert('" + I18nRes.findValue(me.getLanguage(), "对不起,您的权限不够!") + "');history.go(-1);</script>");
      }
      else {
        BPMConsoleLoginImpl bPMConsoleLoginImpl = new BPMConsoleLoginImpl(defaultSystemKey);
        myOut.write(bPMConsoleLoginImpl.getSystemPage(me));
      }
    } else {
      return false;
    }
    return true;
  }
}