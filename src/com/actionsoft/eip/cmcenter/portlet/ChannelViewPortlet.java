package com.actionsoft.eip.cmcenter.portlet;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.cache.CmSchemaCache;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaModel;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.eip.cmcenter.web.ChannelReleaseWeb;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
/**
 * 
 * @description 二级页面修改，对最新文章模块内容进行样式性修改。
 * @version 1.0
 * @author wangaz
 * @update 2014-2-14 上午10:13:37
 */
public class ChannelViewPortlet extends ActionsoftWeb
{
  private UserContext uc;
  public ChannelViewPortlet(UserContext userContext)
  {
    super(userContext);
    this.uc = userContext;
  }

  public ChannelViewPortlet()
  {
  }

  public String getChannelView(int channelId, int schemaId)
  {
    int oldChannelId = channelId;
    StringBuffer list = new StringBuffer();
    String sql = "select t2.securitylist,t2.bindid,contentid,channelid,title,releasedate,YDFW,ZYTS,DISPLAYTITLE,iszd from eip_cm_channelcont t1,eip_cm_content t2 where t1.contentid=t2.id and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + uc.getLanguage() + "' and channelid=" + channelId + " order by t2.releasedate desc, t2.id desc";
    if (channelId == 0) {
      sql = "select t2.securitylist,t2.bindid,contentid,channelid,channelname,title,releasedate,YDFW,ZYTS,DISPLAYTITLE,iszd from eip_cm_channelcont t1,eip_cm_content t2 where t1.contentid=t2.id and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + uc.getLanguage() + "' order by t2.releasedate desc, t2.id desc";
    }

    Hashtable currentChannelList = null;
    CmSchemaModel schemaModel = null;
    if (schemaId != -1) {
      schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
      if (schemaModel != null) {
        String title = schemaModel._schemaName;
        if (schemaModel._isClose) {
          String t = I18nRes.findValue(super.getContext().getLanguage(), "提示");
          String tt = I18nRes.findValue(super.getContext().getLanguage(), "主题");
          String close = I18nRes.findValue(super.getContext().getLanguage(), "已关闭");
          String nolook = I18nRes.findValue(super.getContext().getLanguage(), "您不能查看该主题内容");

          return AlertWindow.getWarningWindow2(t, tt + "【" + title + "】" + close + "！" + nolook + "！");
        }
      } else {
        String t = I18nRes.findValue(super.getContext().getLanguage(), "提示");
        String tt = I18nRes.findValue(super.getContext().getLanguage(), "所访问的主题已删除，请联系系统管理员");
        return AlertWindow.getWarningWindow2(t, tt);
      }
      currentChannelList = CmChannelCache.getACNotCloseChannelList(getContext(), schemaModel);
    } else {
      currentChannelList = CmChannelCache.getACNotCloseChannelList(getContext());
    }

    Connection conn = null;
    Statement stmt = null;
    ResultSet rset = null;
    try {
      conn = DBSql.open();
      stmt = conn.createStatement();
      rset = DBSql.executeQuery(conn, stmt, sql);
      int count = 0;
      list.append("<tr><td><ul style=\"list-style-type:disc;margin-left:5px\" class=\"listlin\">");
      while (rset.next()) {
        count++;
        //改为显示10条数据
        if (count > 11) {
          break;
        }
        int contentId = rset.getInt("contentid");

        String YD = rset.getString("YDFW");
        String YDFW ="";
        if(YD!=null){
        	 if(YD.length()>12){
             	YDFW = YD.substring(0, 12)+"...";
             }else{
             	YDFW=YD;
             }
        }else{
        	
        }
       
        YDFW = (YDFW == null) || (YDFW.trim().length() == 0) ? "" : YDFW;
        String zyts = rset.getString("ZYTS");
        zyts = zyts == null ? "" : zyts;
        String title2 = rset.getString("DISPLAYTITLE");
        title2 = (title2 == null) || (title2.trim().length() == 0) ? rset.getString("title") : title2;
        YDFW = new ChannelReleaseWeb(super.getContext()).getCMSTitle(YDFW, zyts, contentId, title2);
        channelId = rset.getInt("channelid");

        CmChannelModel model = (CmChannelModel)CmChannelCache.getModel(channelId);
        if (model == null) {
          count--;
        }
        else if (!currentChannelList.contains(model)) {
          count--;
        }
        else
        {
          String securitylist = rset.getString("securitylist");
          String currRole = getContext().getRoleModel().getRoleName();
          if ((securitylist != null) && 
            (!"".equals(securitylist)) && 
            (!CmUtil.getContentSecurityList(currRole, securitylist))) {
            count--;
          }
          else
          {
            int bindid_CM = rset.getInt("bindid");
            if (!CmUtil.getContentSecurityList(getContext(), bindid_CM)) {
              count--;
            }
            else {
              String releaseDate = CmUtil.getReleaseDateStr(schemaId, channelId, rset.getTimestamp("RELEASEDATE"));
              if (releaseDate.trim().length() == 0) {
                count--;
              }
              else {
                String tmp_releaseDate = "";
                if (releaseDate.trim().length() < 19)
                  tmp_releaseDate = tmp_releaseDate + releaseDate + " 00:00:00";
                else {
                  tmp_releaseDate = releaseDate;
                }
                Timestamp releaseTime = Timestamp.valueOf(tmp_releaseDate);
                if (YDFW == null)
                  YDFW = "无标题";
                if ((releaseDate == null) || (releaseDate.equals(""))) {
                  count--;
                }
                else {
                 // String icon = "<img src=../aws_img/dot.gif border=0>";
                  String newsTitle = "<a href=''  onClick=\"openContent(frmMain," + schemaId + "," + channelId + "," + contentId + ",'CmContent_Read_Open');return false;\">" + YDFW + "</a>";
                  Calendar calendar = Calendar.getInstance();
                  calendar.add(5, -5);
//                  if (calendar.getTime().getTime() < releaseTime.getTime()) {
//                    newsTitle = newsTitle + "<img src=../aws_img/new1.gif border=0>";
//                  }
                 // list.append("<tr>");
                  list.append("<li width=78%>").append(  "&nbsp;" + newsTitle).append("</li>");
                  //去掉日期显示类
                //  list.append("<td width=22% align=right nowrap><font color=gray style='font-size:9px'>(").append(releaseDate).append(")</font></td>");
                 // list.append("</tr>");
                 // list.append("<tr><td  height=3 colspan=2 background=../aws_skins/_defC1/img/line.gif></td>");
                }
              }
            }
          }
        }
      }
      list.append("</ul></td></tr>");
      if (count == 0) {
        list.append("<tr><td  colspan=2 ><font color=gray><--" + I18nRes.findValue(getContext().getLanguage(), "aws_portal_空") + "--></font></td>");
        list.append("<tr><td  height=3 colspan=2 background=../aws_skins/_defC1/img/line.gif></td>");
      }

      while (count < 10) {
        count++;
        list.append("<tr><td  colspan=2 >&nbsp;</td>");
        list.append("<tr><td  height=3 colspan=2 background=../aws_skins/_defC1/img/line.gif></td>");
      }
      if (count > 8) {
    	  //去掉更多的连接
       // String command = "[<a href='#' alt='<I18N#信息资讯>' onclick=\"openLocation('./login.wf?sid=" + getContext().getSessionId() + "&cmd=CmChannel_Release_Open&schemaId=-1','mainFrame','<I18N#信息资讯>')\"><I18N#更多></a>]";

      //  list.append("<tr>");
      //  list.append("<td width=78%>").append("&nbsp;").append("</td>");
       // list.append("<td width=22% align=right nowrap>").append(command).append("</td>");
       // list.append("</tr>");
       // list.append("<tr><td  height=3 colspan=2 background=../aws_skins/_defC1/img/line.gif></td>");
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace(System.err);
    } finally {
      DBSql.close(conn, stmt, rset);
    }
    Hashtable hashTags = new Hashtable();
    hashTags.put("sid", super.getSIDFlag());
    hashTags.put("list", list.toString());
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("CmContent_ChannelPortlet.htm"), hashTags);
  }

  public String getChannelPicView(int schemaId)
  {
    Hashtable hashTags = new Hashtable();
    hashTags.put("sid", super.getSIDFlag());
    hashTags.put("picView", new ChannelReleaseWeb(super.getContext()).getPictureView(schemaId));
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("CmContent_ChannelPicPortlet.htm"), hashTags);
  }
}