package com.actionsoft.application.portal;

import com.actionsoft.application.portal.jsr168.PortletModeException;
import com.actionsoft.application.portal.jsr168.WindowState;
import com.actionsoft.awf.organization.control.UserContext;
/**
 * 
 * @description 为首页所有的portalet加外边框
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:41:53
 */
public class AWSDefaultPortletLF3
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
    sb.append("<!--Start Portlet:").append(portletModel.getOid()).append(" -->\n");
    //sb.append("<div style='border-collapse:collapse;width:100%;height:100%;border:0px solid #CCC;-webkit-box-shadow: 0px 0px 0px #AAA;-moz-box-shadow: 0px 0px 0px #aaa;box-shadow: 0px 0px 0px #AAA;'>");
    sb.append("<div style='background-color:#FFFFFF;margin-top:0px;border-collapse:collapse;width:100%;height:100%;border:0px; border-collapse:collapse'>");
    sb.append("<table width='").append(portletModel.getWidth()).append("' border='0' cellpadding='0' cellspacing='0'>");

    if ((portletModel.getHeight().equals("")) || (portletModel.getHeight().equals("0"))) {
      sb.append("<tr id=AWSCONTENT" + portletModel.getOid() + " height='100%'>\n<td>\n");
      urlSource = "<script type='text/javascript'>document.write(\"<iframe id='AWSWindow" + portletModel.getOid() + "' name='AWSWindow" + portletModel.getOid() + "' width=100% onload='document.getElementById('AWSWindow" + portletModel.getOid() + "').style.height=AWSWindow" + portletModel.getOid() + ".document.body.scrollHeight;' height=0  src='\"+encodeURI('" + 
        protalURL + "')+\"' frameborder=0 scrolling=auto marginheight=0 marginwidth=0></iframe>\");</script>";
    } else {
      sb.append("<tr id=AWSCONTENT" + portletModel.getOid() + " >\n<td height='" + portletModel.getHeight() + "'>\n");
      urlSource = "<script type='text/javascript'>document.write(\"<iframe id='AWSWindow" + portletModel.getOid() + "' name='AWSWindow" + portletModel.getOid() + "' width=100% height=100%  src='\"+encodeURI('" + protalURL + 
        "')+\"' frameborder=0 scrolling=auto marginheight=0 marginwidth=0></iframe>\");</script>";
    }

    sb.append(urlSource);

    sb.append("</td></tr></table>\n");
    sb.append("</div>");
    sb.append("<!--End Portlet  -->\n");
    return sb.toString();
  }
}