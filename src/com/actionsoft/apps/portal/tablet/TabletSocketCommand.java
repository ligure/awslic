package com.actionsoft.apps.portal.tablet;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.actionsoft.Util;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.application.server.socketcommand.ApplictionUtil;
import com.actionsoft.apps.portal.tablet.console.config.TabletConsoleConfig;
import com.actionsoft.apps.portal.tablet.page.TabletFunctionPage;
import com.actionsoft.apps.portal.tablet.page.TabletHomePage;
import com.actionsoft.apps.portal.tablet.page.TabletPersonCofigPage;
import com.actionsoft.apps.portal.tablet.page.TabletSkinPage;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.plugs.email.util.AWSMailUtil;
import com.actionsoft.plugs.email.www.AutoAcceptServer;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level1.SessionAPI;

public class TabletSocketCommand
  implements BaseSocketCommand
{
  public boolean executeCommand(UserContext paramUserContext, Socket paramSocket, OutputStreamWriter paramOutputStreamWriter, Vector paramVector, UtilString paramUtilString, String paramString)
    throws Exception
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    Object localObject4;
    String str1;
    String str2;
    String str3;
    String str4;
    String str5;
    String str6;
    String str7;
    String str8;
    String str9;
    Object localObject5;
    if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Login")) {
	   String uid = paramVector.elementAt(2).toString();
	   String pwd = paramVector.elementAt(3).toString();
	   if("mysid".equals(uid)){
	       String[] sid = pwd.split("_");
	       uid = sid[0];
	       boolean flag = SessionAPI.getInstance().checkSession(uid, pwd);
	       if(!flag){
		   paramOutputStreamWriter.write("<script language='javascript'>window.location.href='" +  ApplictionUtil.getProperty("SSO_URL").replaceFirst("index_blue.jsp", "t") + "';</script>");
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
       			    paramOutputStreamWriter.write("<script language='javascript'>window.location.href='" +  ApplictionUtil.getProperty("SSO_URL").replaceFirst("index_blue.jsp", "t") + "';</script>");
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
    	/********************************pad端登录str****************/	
      localObject1 = uid;
      localObject2 = paramVector.elementAt(3).toString();
      localObject3 = paramVector.elementAt(4).toString();
      localObject4 = paramVector.elementAt(5).toString();
      str1 = UtilCode.decode(paramUtilString.matchValue("_extInfo[", "]extInfo_"));
      str2 = UtilCode.decode(paramUtilString.matchValue("_UserCert[", "]UserCert_"));
      str3 = UtilCode.decode(paramUtilString.matchValue("_ContainerName[", "]ContainerName_"));
      str4 = UtilCode.decode(paramUtilString.matchValue("_UserSignedData[", "]UserSignedData_"));
      str5 = UtilCode.decode(paramUtilString.matchValue("_p1[", "]p1_"));
      str6 = UtilCode.decode(paramUtilString.matchValue("_p2[", "]p2_"));
      str7 = UtilCode.decode(paramUtilString.matchValue("_p3[", "]p3_"));
      str8 = UtilCode.decode(paramUtilString.matchValue("_p4[", "]p4_"));
      str9 = UtilCode.decode(paramUtilString.matchValue("_p5[", "]p5_"));
      localObject5 = new Hashtable();
      ((Hashtable)localObject5).put("CA_UserCert", str2);
      ((Hashtable)localObject5).put("CA_ContainerName", str3);
      ((Hashtable)localObject5).put("CA_UserSignedData", str4);
      ((Hashtable)localObject5).put("UserCert", str2);
      ((Hashtable)localObject5).put("ContainerName", str3);
      ((Hashtable)localObject5).put("UserSignedData", str4);
      ((Hashtable)localObject5).put("lang", localObject4);
      ((Hashtable)localObject5).put("p1", str5);
      ((Hashtable)localObject5).put("p2", str6);
      ((Hashtable)localObject5).put("p3", str7);
      ((Hashtable)localObject5).put("p4", str8);
      ((Hashtable)localObject5).put("p5", str9);
      TabletHomePage localTabletHomePage = new TabletHomePage();
      paramOutputStreamWriter.write(localTabletHomePage.login((String)localObject1, (String)localObject2, (String)localObject3, (String)localObject4, str1, (Hashtable)localObject5));
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Logout")) {
      TabletHomePage web = new TabletHomePage();
      paramOutputStreamWriter.write(web.logout(paramUserContext));
      web = null;
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_TabletList")) {
      localObject1 = new TabletHomePage(paramUserContext);
      paramOutputStreamWriter.write(((TabletHomePage)localObject1).getTabletList());
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_FunctionList")) {
      localObject1 = new TabletFunctionPage(paramUserContext);
      paramOutputStreamWriter.write(((TabletFunctionPage)localObject1).getFunctionList());
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_FunctionList2")) {
      localObject1 = paramVector.elementAt(3).toString();
      localObject2 = paramVector.elementAt(4).toString();
      localObject3 = paramVector.elementAt(5).toString();
      localObject4 = new TabletFunctionPage(paramUserContext);
      paramOutputStreamWriter.write(((TabletFunctionPage)localObject4).getFunctionList2(Integer.parseInt((String)localObject1), (String)localObject2, (String)localObject3));
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_AddFunction")) {
      localObject1 = paramVector.elementAt(3).toString();
      localObject2 = paramVector.elementAt(4).toString();
      localObject3 = UtilCode.decode(paramUtilString.matchValue("_userId[", "]userId_"));
      localObject4 = new TabletFunctionPage(paramUserContext);
      paramOutputStreamWriter.write(((TabletFunctionPage)localObject4).addFunction(Integer.parseInt((String)localObject1), (String)localObject2, (String)localObject3));
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_ChangeSkin")) {
      localObject1 = new TabletSkinPage(paramUserContext);
      paramOutputStreamWriter.write(((TabletSkinPage)localObject1).getTabletSkin());
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_SetBg")) {
      localObject1 = new TabletSkinPage(paramUserContext);
      localObject2 = UtilCode.decode(paramUtilString.matchValue("_bgUrl[", "]bgUrl_"));
      paramOutputStreamWriter.write(((TabletSkinPage)localObject1).setTabletBackground((String)localObject2));
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_UpFile")) {
      localObject1 = new TabletHomePage(paramUserContext);
      paramOutputStreamWriter.write(((TabletHomePage)localObject1).getUpload());
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_DeleteIcon")) {
      localObject1 = new TabletHomePage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      localObject3 = paramVector.elementAt(4).toString();
      paramOutputStreamWriter.write(((TabletHomePage)localObject1).removeIcon(Integer.parseInt((String)localObject2), (String)localObject3));
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_UpdateOrderIndex")) {
      localObject1 = new TabletHomePage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      paramOutputStreamWriter.write(((TabletHomePage)localObject1).updateTabletOrderIndex((String)localObject2));
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_PW_Open")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).getChangeUserPwdDialog((String)localObject2));
      localObject1 = null;
    } else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_PW_Save")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      localObject3 = paramVector.elementAt(4).toString();
      localObject4 = paramVector.elementAt(5).toString();
      if ((localObject4 == null) || (((String)localObject4).equals(""))) {
        localObject4 = "pc";
      }

      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).changePassword((String)localObject2, (String)localObject3, (String)localObject4));
    } else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_Base_Open")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      if ((localObject2 == null) || (((String)localObject2).equals(""))) {
        localObject2 = "pc";
      }
      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).getUserInfoDialog((String)localObject2));
      localObject1 = null;
    } else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_Base_Save")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = UtilCode.decode(paramUtilString.matchValue("_officeTel[", "]officeTel_"));
      localObject3 = UtilCode.decode(paramUtilString.matchValue("_officeFax[", "]officeFax_"));
      localObject4 = UtilCode.decode(paramUtilString.matchValue("_mobile[", "]mobile_"));
      str1 = UtilCode.decode(paramUtilString.matchValue("_homeTel[", "]homeTel_"));
      str2 = UtilCode.decode(paramUtilString.matchValue("_email[", "]email_"));
      str3 = UtilCode.decode(paramUtilString.matchValue("_jjlinkman[", "]jjlinkman_"));
      str4 = UtilCode.decode(paramUtilString.matchValue("_jjtel[", "]jjtel_"));
      str5 = UtilCode.decode(paramUtilString.matchValue("_smid[", "]smid_"));
      str6 = UtilCode.decode(paramUtilString.matchValue("_pcMan[", "]pcMan_"));
      str7 = UtilCode.decode(paramUtilString.matchValue("_msnId[", "]msnId_"));
      str8 = UtilCode.decode(paramUtilString.matchValue("_emailPass[", "]emailPass_"));
      str9 = UtilCode.decode(paramUtilString.matchValue("_type[", "]type_"));
      localObject5 = new UserModel();
      ((UserModel)localObject5).setModel(paramUserContext.getUserModel());
      ((UserModel)localObject5).setEmail(str2);
      ((UserModel)localObject5).setHomeTel(str1);
      ((UserModel)localObject5).setMobile((String)localObject4);
      ((UserModel)localObject5).setOfficeFax((String)localObject3);
      ((UserModel)localObject5).setOfficeTel((String)localObject2);
      ((UserModel)localObject5).setJjLinkMan(str3);
      ((UserModel)localObject5).setJjTel(str4);
      ((UserModel)localObject5).setSMid(str5);
      ((UserModel)localObject5).setPcMan(str6);
      ((UserModel)localObject5).setMsnId(str7);
      if ((str8 != null) && (str8.trim().length() > 0)) {
        str8 = AWSMailUtil.getInstance().encryptPass(paramUserContext.getUID(), str8);
        int j = DBSql.executeUpdate("update orguser set emailpass='" + str8 + "' where userid='" + paramUserContext.getUID() + "'");
        if (j > 0) {
          UserModel localUserModel = paramUserContext.getUserModel();
          localUserModel.setEmailPass(str8);
          UserCache.updateModel(localUserModel);
          AutoAcceptServer.getInstance().acceptUserMail(localUserModel.getUID());
        }
      }
      if ((str9 == null) || (str9.equals(""))) {
        str9 = "pc";
      }
      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).saveBaseData((UserModel)localObject5, str9));
      localObject1 = null;
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_Para_Open")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      if ((localObject2 == null) || (((String)localObject2).equals(""))) {
        localObject2 = "pc";
      }
      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).getChangeParameterDialog((String)localObject2));
      localObject1 = null;
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_Para_Save")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      localObject3 = paramVector.elementAt(4).toString();
      localObject4 = paramVector.elementAt(5).toString();
      long l = paramUserContext.getUserModel().getSessionTime();
      int i = paramUserContext.getUserModel().getLineNumber();
      try
      {
        l = Long.parseLong((String)localObject2);
        i = Integer.parseInt((String)localObject3);
      } catch (Exception localException) {
      }
      if ((localObject4 == null) || (((String)localObject4).equals(""))) {
        localObject4 = "pc";
      }
      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).savePersonConfig(l, i, (String)localObject4));
      localObject1 = null;
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet_Tablet_Portal_Person_Config_Log_Open")) {
      localObject1 = new TabletPersonCofigPage(paramUserContext);
      localObject2 = paramVector.elementAt(3).toString();
      if ((localObject2 == null) || (((String)localObject2).equals(""))) {
        localObject2 = "pc";
      }
      paramOutputStreamWriter.write(((TabletPersonCofigPage)localObject1).getMyLogDialog((String)localObject2));
      localObject1 = null;
    }
    else if (paramString.equals("com.actionsoft.apps.portal.tablet.TabletProcessPortalArgsConfig")) {
      localObject1 = new TabletConsoleConfig(paramUserContext);
      paramOutputStreamWriter.write(((TabletConsoleConfig)localObject1).getTabletConfigPage());
    } else if (paramString.equals("com.actionsoft.apps.portal.tablet.TabletProcessPortalArgsConfigSave")) {
      localObject1 = UtilCode.decode(paramUtilString.matchValue("_title[", "]title_"));

      localObject2 = paramVector.elementAt(4).toString();

      localObject3 = new TabletConsoleConfig(paramUserContext);

      paramOutputStreamWriter.write(((TabletConsoleConfig)localObject3).saveTabletConfigPage((String)localObject1, (String)localObject2));
    } else if (paramString.equals("com.actionsoft.apps.portal.tablet.TabletProcessPortalArgsConfigItemDelete")) {
      localObject1 = UtilCode.decode(paramUtilString.matchValue("_imgBgUrl[", "]imgBgUrl_"));
      localObject2 = new TabletConsoleConfig(paramUserContext);
      paramOutputStreamWriter.write(((TabletConsoleConfig)localObject2).deleteTabletConfigItem((String)localObject1));
    } else if (paramString.equals("com.actionsoft.apps.portal.tablet.TabletProcessPortalArgsConfigItemCheckImageIfExist")) {
      localObject1 = UtilCode.decode(paramUtilString.matchValue("_imgName[", "]imgName_"));
      localObject2 = new TabletConsoleConfig(paramUserContext);
      paramOutputStreamWriter.write(((TabletConsoleConfig)localObject2).checkIsExistTabletItemModelID((String)localObject1));
    }
    else
    {
      boolean bool;
      if (paramString.equals("com.actionsoft.apps.portal.tablet.TabletProcessPortalArgsConfigSwitchDisplay")) {
        localObject1 = UtilCode.decode(paramUtilString.matchValue("_tabletBgImgUrl[", "]tabletBgImgUrl_"));
        localObject2 = paramVector.elementAt(4).toString();
        bool = ((String)localObject2).equals("true");
        localObject4 = new TabletConsoleConfig(paramUserContext);
        paramOutputStreamWriter.write(((TabletConsoleConfig)localObject4).switchDisplay((String)localObject1, bool));
      } else if (paramString.equals("com.actionsoft.apps.portal.tablet.TabletProcessPortalArgsConfigItemSave")) {
        localObject1 = UtilCode.decode(paramUtilString.matchValue("_imgName[", "]imgName_"));
        localObject2 = paramVector.elementAt(4).toString();
        bool = ((String)localObject2).equals("show");
        localObject4 = new TabletConsoleConfig(paramUserContext);
        paramOutputStreamWriter.write(((TabletConsoleConfig)localObject4).saveTabletConfigItem((String)localObject1, bool));
      } else {
        return false;
      }
    }
    return true;
  }
}