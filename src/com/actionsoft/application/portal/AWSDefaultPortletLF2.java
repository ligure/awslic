package com.actionsoft.application.portal;

import com.actionsoft.application.portal.jsr168.PortletModeException;
import com.actionsoft.application.portal.jsr168.WindowState;
import com.actionsoft.application.portal.navigation.util.NavUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
/**
 * 
 * @description 修改首页各portalet的格局及标题样式
 * @version 1.0
 * @author wangaz
 * @update 2014-1-20 下午06:13:25
 */
public class AWSDefaultPortletLF2
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
    sb.append("<!--Start Portlet:").append(portletModel.getOid()).append(" -->\n");

    String onDragAction = "style='cursor:move;' ondrag=draging(); ondragend=dragEnd();";
    String onMourseDownAction = "class=dragTR onmousedown=dragStart();";
    if ((me.getUserModel().isRoving()) || (windowState.equals(WindowState.READ))) {
      onDragAction = "";
      onMourseDownAction = "";
    }

    sb.append("<table valign=top class=aws-portal-window id=aws-portlet-id" + portletModel.getOid() + " width='").append(portletModel.getWidth()).append("' border='0' cellpadding='0' cellspacing='0' ").append(onDragAction).append(">");

    sb.append("<tr class=aws-portal-window-titlebar-title>\n");
/**
 * 在此增加portalet标题前文字或图片
 */
    sb.append("<td id='aws_portlet_title_id_").append(portletModel.getOid()).append("'").append(onMourseDownAction).append(">").append(NavUtil.getLangName(me.getLanguage(), ""+portletModel.getTitle())).append("</td>");
    String configButton = "<a href='' onclick=\"javascript:return modifyPortlet(frmMain," + portletModel.getOid() + ");\"><img src=../aws_skins/portlet/AWSDefaultPortletLF2/conf.gif title='<I18N#配置窗口>' border=0></a>";
    String removeButton = "<a href='' onclick=\"javascript:return removePortlet(frmMain," + portletModel.getOid() + ");\"><img src=../aws_skins/portlet/AWSDefaultPortletLF2/rem.gif title='<I18N#移走窗口>' border=0></a>";

    if ((me.getUserModel().isRoving()) || (windowState.equals(WindowState.READ))) {
      removeButton = "";
      configButton = "";
    }
    sb.append("<td ").append(onMourseDownAction).append(" style='background-repeat: no-repeat;background-position:right;' width=50% align=right>").append("<a href='' onclick=\"javascript:return minPortlet(frmMain,").append(
      portletModel.getOid()).append(");\"><img id=AWSFLEXWINDOW" + portletModel.getOid() + " src=../aws_skins/portlet/AWSDefaultPortletLF2/min.gif title='<I18N#缩小窗口>' border=0></a>").append(
      "<a href='' onclick=\"javascript:return maxPortlet('" + url + "');\"><img src=../aws_skins/portlet/AWSDefaultPortletLF2/new.gif title='<I18N#新窗口>' border=0></a>").append("<a href='' onclick=\"javascript:return refPortlet(frmMain,")
      .append(portletModel.getOid()).append(",'" + url + "');\"><img src=../aws_skins/portlet/AWSDefaultPortletLF2/ref.gif title='<I18N#刷新窗口>' border=0></a>").append(configButton).append(removeButton).append("</td>");
    sb.append("</tr>\n");

    String urlSource = "";

    protalURL = URLEncodedUtil.URLEncoded(protalURL);

    if ((portletModel.getHeight().equals("")) || (portletModel.getHeight().equals("0"))) {
      sb.append("<tr id=AWSCONTENT" + portletModel.getOid() + " height='100%'>\n");
      urlSource = "<iframe id='AWSWindow" + portletModel.getOid() + "' name='AWSWindow" + portletModel.getOid() + "' width=100% onload=\"document.getElementById('AWSWindow" + portletModel.getOid() + "').style.height=AWSWindow" + portletModel.getOid() + ".document.body.scrollHeight;\" height=0  src='" + protalURL + "' frameborder=0 scrolling=auto marginheight=0 marginwidth=0></iframe>";
    } else {
      sb.append("<tr id=AWSCONTENT" + portletModel.getOid() + ">\n");
      urlSource = "<iframe id='AWSWindow" + portletModel.getOid() + "' name='AWSWindow" + portletModel.getOid() + "' width=100% height=100%  src='" + protalURL + "' frameborder=0 scrolling=auto marginheight=0 marginwidth=0></iframe>";
    }
    sb.append("<td colspan=2 class=aws-portal-window-content height='" + portletModel.getHeight() + "' >\n");
    sb.append(urlSource.toString());
    sb.append("<script>initPortlet(frmMain," + portletModel.getOid() + ");</script>");
    sb.append("</td>\n");
    sb.append("</tr>\n");

    sb.append("</table>\n");

    sb.append("<!--End Portlet  -->\n");
    return sb.toString();
  }
}