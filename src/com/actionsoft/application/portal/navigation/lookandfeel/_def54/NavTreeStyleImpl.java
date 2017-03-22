package com.actionsoft.application.portal.navigation.lookandfeel._def54;

import com.actionsoft.application.portal.navigation.cache.NavigationDirectoryCache;
import com.actionsoft.application.portal.navigation.cache.NavigationFunctionCache;
import com.actionsoft.application.portal.navigation.cache.NavigationSystemCache;
import com.actionsoft.application.portal.navigation.lookandfeel.LookAndFeelAbst;
import com.actionsoft.application.portal.navigation.model.NavigationDirectoryModel;
import com.actionsoft.application.portal.navigation.model.NavigationFunctionModel;
import com.actionsoft.application.portal.navigation.model.NavigationSystemModel;
import com.actionsoft.application.portal.navigation.util.NavUtil;
import com.actionsoft.application.portal.portlet.quicklink.model.QuickLinkModel;
import com.actionsoft.application.portal.portlet.quicklink.util.QuickLinkUtil;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.AWFServerConf;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.util.URLParser;
import com.actionsoft.awf.util.UtilString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.imageio.stream.FileImageInputStream;

public class NavTreeStyleImpl extends LookAndFeelAbst
{
  public NavTreeStyleImpl()
  {
  }

  public NavTreeStyleImpl(UserContext paramUserContext, String paramString)
  {
    super(paramUserContext, paramString);
  }

  public String toString()
  {
    Object localObject = (NavigationSystemModel)NavigationSystemCache.getModel(Integer.parseInt(super.getSystemId()));

    int i = 0;
    Map localMap = NavigationSystemCache.getList();
    if (localMap != null) {
      for (int j = 0; j < localMap.size(); j++) {
        NavigationSystemModel localNavigationSystemModel = (NavigationSystemModel)localMap.get(new Integer(j));
        if ((localNavigationSystemModel == null) || (!localNavigationSystemModel._isActivity))
        {
          continue;
        }
        if ((!SecurityProxy.checkModelSecurity(getUserContext().getUID(), Integer.toString(localNavigationSystemModel._id))) || (localNavigationSystemModel._id < 21)) {
          continue;
        }
        if ((!"21".equals(super.getSystemId())) && (super.getSystemId().equals(String.valueOf(localNavigationSystemModel._id)))) {
          i = 1;
          localObject = localNavigationSystemModel; break;
        }if (!"21".equals(super.getSystemId())) break;
        i = 1; break;
      }

    }

    StringBuffer localStringBuffer = new StringBuffer();
    if (i != 0) {
      localStringBuffer.append(getPersonConfig());
    }
//    return (String)localStringBuffer.append(getKeepHistory((NavigationSystemModel)localObject)).append(getNavMenu((NavigationSystemModel)localObject)).toString();
    return (String)localStringBuffer.append(getNavMenu((NavigationSystemModel)localObject)).toString();

  }

  private String getPersonConfig()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("<!--nav config-->\n");
    localStringBuffer.append("<div class='navConfig'>\n");
    localStringBuffer.append("<div class='context'>\n");
    localStringBuffer.append("<div class=\"context_head\">\n");
    localStringBuffer.append("<div class=\"context_head_title\"> " + getUserContext().getDepartmentModel().getDepartmentName() + " </div>\n");

    localStringBuffer.append(" <div class=\"navConfig_context_photo\">\n");
    String str1 = super.getUserContext().getUID();
    String str2 = AWFConfig._awfServerConf.getDocumentPath() + "Photo/group" + str1 + "/file0/" + str1 + ".jpg";
    File localFile = new File(str2);
    String str3 = "onclick=\"parent.$('#personsetting').dialog('<I18N#上传我的头像>','Org_User_Photo_Open_V2&user_id=" + super.getUserContext().getUID() + "','" + super.getUserContext().getSessionId() + "','500','350');\"";
    if (!localFile.exists())
      localStringBuffer.append("<img style=\"cursor:pointer;\" src='../aws_img/userPhoto.jpg' border=\"0\"  ").append(str3).append(">");
    else {
      localStringBuffer.append("<img style=\"cursor:pointer;\" src='./downfile.wf?flag1=").append(str1).append("&flag2=0&sid=").append(super.getUserContext().getSessionId()).append("&rootDir=Photo&filename=").append(str1).append(".jpg&j=").append(Math.random()).append("' border=0 ").append(str3).append(">");
    }

    localStringBuffer.append("</div>\n");
    localStringBuffer.append("</div>\n");
    localStringBuffer.append("</div>\n");
    localStringBuffer.append("<!--end-->\n");
    return localStringBuffer.toString();
  }

  private String getKeepHistory(NavigationSystemModel paramNavigationSystemModel)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("<!--keep history-->\n");
    localStringBuffer.append("<div class='navKeepHistory'>\n");
    localStringBuffer.append("<div class='navKeepHistory_context'>\n");
    localStringBuffer.append("<div class=\"navKeepHistory_context_head\" >\n");
    localStringBuffer.append("<div class=\"navKeepHistory_context_head_title\" onclick=\"displayKeepHistory();\"><I18N#我的收藏></div>\n");
    localStringBuffer.append("<div class=\"navKeepHistory_context_head_config\" id=\"navKeepHistory_context_head_config\" onclick=\"parent.$('#personsetting').dialog('<I18N#添加我的收藏>','AWSPortlet_MyQuickLink_CreatePage_V2&groupName=").append(paramNavigationSystemModel._uuid).append("','").append(super.getUserContext().getSessionId()).append("','500','430');\"><img border=\"0\" src=\"../aws_skins/_def54/img/add.png\"></div>\n");
    localStringBuffer.append("</div>\n");
 
    localStringBuffer.append("<div class=\"navKeepHistory_context_neirong\" id=\"directory_keepHistory\" style=\"display:none;\">\n");
    Vector localVector = QuickLinkUtil.getInstancesOfOwner(super.getUserContext().getUID(), paramNavigationSystemModel._uuid);
    if ((localVector != null) && (localVector.size() > 0)) {
      for (int i = 0; i < localVector.size(); i++) {
        QuickLinkModel localQuickLinkModel = (QuickLinkModel)localVector.get(i);
        if (localQuickLinkModel != null) {
          int j = 0;
          try {
            j = Integer.parseInt(localQuickLinkModel.getLinkSource().substring(6));
          }
          catch (NumberFormatException localNumberFormatException)
          {
          }
          String str1;
          String str2;
          if ((j > 0) && (SecurityProxy.checkModelSecurity(super.getUserContext().getUID(), Integer.toString(j)))) {
            str1 = ""; str2 = ""; String str3 = "mainFrame"; String str4 = "";

            NavigationDirectoryModel localNavigationDirectoryModel = (NavigationDirectoryModel)NavigationDirectoryCache.getModel(j);
            if (localNavigationDirectoryModel != null) {
              str1 = localNavigationDirectoryModel._navIcon;
              str2 = localNavigationDirectoryModel._directoryUrl;
              str4 = NavUtil.getLangName(super.getUserContext().getLanguage(), localNavigationDirectoryModel._directoryName);
            }
            else {
              NavigationFunctionModel localNavigationFunctionModel = (NavigationFunctionModel)NavigationFunctionCache.getModel(j);
              if (localNavigationFunctionModel != null) {
                str1 = localNavigationFunctionModel._navIcon;
                str2 = localNavigationFunctionModel._functionUrl;
                str4 = NavUtil.getLangName(super.getUserContext().getLanguage(), localNavigationFunctionModel._functionName);
              }
            }
            str2 = URLParser.repleaseNavURL(super.getUserContext(), str2);
            if (str1.indexOf("/aws_") == 0) {
              str1 = ".." + str1;
            }
            if (str1.length() == 0) str1 = "../aws_img/undo2.gif";
            str1 = new UtilString(str1).replace("iwork_", "aws_");
            localStringBuffer.append("<a href=\"#\"><div class=\"navKeepHistory_context_neirong_content\">").append("<div class=\"navKeepHistory_context_neirong_content_function\" onclick=\"openLocation('").append(str2).append("','" + str3 + "','" + UtilString.cutString(str4, 8) + "');return false;\">").append("<img src='../aws_skins/_def54/img/function.png' border='0'>&nbsp;").append(UtilString.cutString(str4, 8)).append("</div>").append("<div class=\"navKeepHistory_context_neirong_content_op\" onclick=\"removeKeepHistory('").append(paramNavigationSystemModel._uuid).append("','").append(localQuickLinkModel.getId()).append("')\"></div></div></a>\n");
          }
          else
          {
            str1 = localQuickLinkModel.getTitle();
            str2 = localQuickLinkModel.getLinkSource();
            localStringBuffer.append("<a href=\"#\"><div class=\"navKeepHistory_context_neirong_content\">").append("<div class=\"navKeepHistory_context_neirong_content_function\" onclick=\"openLocation('").append(str2).append("','mainFrame','" + str1 + "');return false;\">").append("<img src='../aws_skins/_def54/img/function.png' border='0'>&nbsp;").append(UtilString.cutString(str1, 8)).append("</div>").append("<div class=\"navKeepHistory_context_neirong_content_op\" onclick=\"removeKeepHistory('").append(paramNavigationSystemModel._uuid).append("','").append(localQuickLinkModel.getId()).append("')\"></div></div></a>\n");
          }

        }

      }

    }

    localStringBuffer.append("</div>\n");
    localStringBuffer.append("</div>\n");
    localStringBuffer.append("</div>\n");
    localStringBuffer.append("<!-- end-->\n");
    return localStringBuffer.toString();
  }

  private String getNavMenu(NavigationSystemModel paramNavigationSystemModel)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    Hashtable localHashtable1 = NavigationDirectoryCache.getListOfSystem(paramNavigationSystemModel._id);
    if ((localHashtable1 != null) && (localHashtable1.size() > 0)) {
      localStringBuffer.append(" <!--nav directory-->\n");
      localStringBuffer.append("<div class='navMenu'>\n");
      
      localStringBuffer.append("<div class='navMenu_context'>\n");
      int i = 0;
      for (int j = 0; j < localHashtable1.size(); j++) {
        NavigationDirectoryModel localNavigationDirectoryModel = (NavigationDirectoryModel)localHashtable1.get(new Integer(j));
        if ((localNavigationDirectoryModel == null) || (!localNavigationDirectoryModel._isActivity))
        {
          continue;
        }
        if (!SecurityProxy.checkModelSecurity(getUserContext().getUID(), Integer.toString(localNavigationDirectoryModel._id))) {
          continue;
        }
        Hashtable localHashtable2 = NavigationFunctionCache.getListOfDirectory(localNavigationDirectoryModel._id);
        if ((localHashtable2 == null) || ((localHashtable2.size() == 0) && (localNavigationDirectoryModel._directoryUrl.length() <= 3))) {
          continue;
        }
        String str1 = NavUtil.getLangName(getUserContext().getLanguage(), localNavigationDirectoryModel._directoryName);
        if (str1.length() > 9) {
          str1 = str1.substring(0, 9) + "...";
        }
        String str2 = "";
        String str3 = "";
        String str4 = NavUtil.getLangName(getUserContext().getLanguage(), paramNavigationSystemModel._systemName);
        if (localNavigationDirectoryModel._directoryUrl.length() > 3) {
          str2 = URLParser.repleaseNavURL(getUserContext(), localNavigationDirectoryModel._directoryUrl);
          str2 = "openDirectory('" + str2 + "','" + localNavigationDirectoryModel._directoryTarget + "','" + localNavigationDirectoryModel._id + "');";
        } else {
          str2 = "displayDirectory('directory_" + localNavigationDirectoryModel._id + "');";
        }
        localStringBuffer.append("<div class=\"navMenu_context_head\" onclick=\"").append(str2).append("\">\n");
        localStringBuffer.append("<div class=\"navMenu_context_head_title\">").append("<img src=\"").append(localNavigationDirectoryModel._navIcon.trim().length() == 0 ? "../aws_skins/_def54/img/directory.png" : localNavigationDirectoryModel._navIcon).append("\">&nbsp;").append(str1).append("</div>\n");
        localStringBuffer.append("<div class=\"").append(i == 0 ? "navMenu_context_head_op_exp" : "navMenu_context_head_op_col").append("\" id=\"directory_").append(localNavigationDirectoryModel._id).append("_navMenu_op\"></div>\n");
        localStringBuffer.append("</div>\n");

        if ((localHashtable2 != null) && (localHashtable2.size() > 0)) {
          localStringBuffer.append("<div class=\"navMenu_context_neirong\" id=\"directory_").append(localNavigationDirectoryModel._id).append("\" style=\"display:").append(i == 0 ? "" : "none").append(";\">\n");
          for (int k = 0; k < localHashtable2.size(); k++) {
            NavigationFunctionModel localNavigationFunctionModel = (NavigationFunctionModel)localHashtable2.get(new Integer(k));
            if ((localNavigationFunctionModel == null) || (!localNavigationFunctionModel._isActivity)) {
              continue;
            }
            if (!SecurityProxy.checkModelSecurity(getUserContext().getUID(), Integer.toString(localNavigationFunctionModel._id))) {
              continue;
            }
            String str5 = URLParser.repleaseNavURL(getUserContext(), localNavigationFunctionModel._functionUrl);
            str5 = str5.length() == 0 ? "-" : str5;
            String str6 = localNavigationFunctionModel._workTarget.length() == 0 ? "-" : localNavigationFunctionModel._workTarget;
            String str7 = NavUtil.getLangName(getUserContext().getLanguage(), localNavigationFunctionModel._functionName);
            //wangaz将汉子超过12个字后出现省略号去掉。
//            if (str7.length() > 12) {
//              str7 = str7.substring(0, 12) + "...";
//            }
            String str8 = "onclick=\"openFuntion(this,'" + str5 + "','" + str6 + "');return false;\"";
            localStringBuffer.append("<a title ="+str7+" href=\"\" ").append(str8).append("><div class=\"navMenu_context_neirong_function\">").append("<img src='../aws_skins/_def54/img/function.png' border='0'>&nbsp;").append(str7).append("</div></a>");
          }
          localStringBuffer.append("</div>\n");
        }
        i = localNavigationDirectoryModel._id;
      }
      localStringBuffer.append(" </div>\n");
    //  localStringBuffer.append("<div   style='width:10px;' class='leftMenu' ><img onclick='ahide()' height='600px' width='10px' src='../aws_skins/_def54/img/noopen.png'></img></div>");
      localStringBuffer.append("<//div>\n");
      //localStringBuffer.append("<!--end-->\n");

      localStringBuffer.append("<script> var systemId=").append(paramNavigationSystemModel._id).append(";</script>");
    }
    return localStringBuffer.toString();
  }
}