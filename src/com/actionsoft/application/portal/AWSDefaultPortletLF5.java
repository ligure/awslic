package com.actionsoft.application.portal;

import com.actionsoft.application.portal.jsr168.PortletModeException;
import com.actionsoft.application.portal.jsr168.WindowState;
import com.actionsoft.application.portal.navigation.util.NavUtil;
import com.actionsoft.awf.organization.control.UserContext;
/**
 * 
 * @description portalet标题，增加标题前小图标。
 * @version 1.0
 * @author wangaz
 * @update 2014-2-10 下午03:00:46
 */
public class AWSDefaultPortletLF5
  implements PortletLookAndFeel
{
  public String getWindow(WindowState windowState, AWSPortletModel portletModel, String lookAndFeelKey, UserContext me, String systemUUID)
  {
    StringBuffer sb = new StringBuffer();

    AWSPortletURL url = new AWSPortletURL();
    String protalURL = "";
    try {
      url.setPortletMode(portletModel);
      url.addParameter("userContext", me);
      url.addParameter("systemUUID", systemUUID);
      protalURL = url.toString();
    } catch (PortletModeException pme) {
      pme.printStackTrace(System.err);
    }
    String urlSource = "";
    if ((portletModel.getHeight().equals("")) || (portletModel.getHeight().equals("0")))
        urlSource ="<script type='text/javascript'>document.write(\"<iframe id='AWSWindow" + portletModel.getOid() + "' name='AWSWindow" + portletModel.getOid() + "' width=100% onload='document.getElementById('AWSWindow" + portletModel.getOid() + "').style.height=AWSWindow" + portletModel.getOid() + ".document.body.scrollHeight;' height=0  src='\"+encodeURI('" + 
        protalURL + "')+\"' frameborder=0 scrolling=auto marginheight=0 marginwidth=0></iframe>\");</script>";
    else {
      urlSource = "<script type='text/javascript'>document.write(\"<iframe id='AWSWindow" + portletModel.getOid() + "' name='AWSWindow" + portletModel.getOid() + "' width=100% height=100%  src='\"+encodeURI('" + protalURL + 
        "')+\"' frameborder=0 scrolling=auto marginheight=0 marginwidth=0></iframe>\");</script>";
    }
    sb.append("<!--Start Portlet:").append(portletModel.getOid()).append(" -->\n");

    sb.append("<table valign=top ondrag=draging(); ondragend=dragEnd(); class=aws-portal-window id=aws-portlet-id" + portletModel.getOid() + " width='").append(portletModel.getWidth()).append("' border='0' cellpadding='0' cellspacing='0'>");

    sb.append("<tr  class=aws-portal-window-titlebar-title>\n");
  /**
   * 增加标题行的箭头标示
   */
    String st = portletModel.getTitle();
    String st2 = portletModel.getTitle();
    if("导航图".equals(st)||"各公司风采".equals(st)){
    	//sb.append(");
    //	sb.append("<td style='cursor:default;' class=dragTR onmousedown=dragStart();>").append(NavUtil.getLangName(me.getLanguage(), "<img style='padding-right:10px;marign-top:5px;' src=../aws_skins/_def54/letv/images/control.png>"+portletModel.getTitle())).append("</td>\n");

    }/**
    else if("各公司风采".equals(st)){
//    	sb.append("<td style='cursor:default;' class=dragTR onmousedown=dragStart();>").append(NavUtil.getLangName(me.getLanguage(), "<img style='padding-right:10px;marign-top:5px;' src=../aws_skins/_def54/letv/images/control.png>"+portletModel.getTitle()))
//    	.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<select name='Select1' class='selbox'><option selected='selected'>乐视网</option><option>乐视致新</option></select>")
//    	.append("</td>\n");
    	
    }*/
    else{
    	//去掉了鼠标拖动方法onmousedown=dragStart()
    //	sb.append("<td cursor:default;' class=dragTR onmousedown=dragStart();>").append(NavUtil.getLangName(me.getLanguage(), "&nbsp;&nbsp;<img style='vertical-align: middle;padding-right:10px;marign-top:5px;' src=../aws_skins/_def54/letv/images/control.png>"+portletModel.getTitle())).append("</td>\n");
       	sb.append("<td cursor:default;class=dragTR >").append(NavUtil.getLangName(me.getLanguage(), "&nbsp;&nbsp;<img style='vertical-align: middle;padding-right:10px;marign-top:5px;' src=../aws_skins/_def54/letv/images/control.png>"+portletModel.getTitle())).append("</td>\n");

    }
    sb.append("</tr>\n");

    sb.append("<tr id=AWSCONTENT" + portletModel.getOid() + ">\n");
    sb.append("<td class=aws-portal-window-content height='" + portletModel.getHeight() + "'>\n");
    sb.append(urlSource);
    sb.append("</td>\n");
    sb.append("</tr>\n");

    sb.append("</table>\n");

    sb.append("<!--End Portlet  -->\n");

    return sb.toString();
  }
}