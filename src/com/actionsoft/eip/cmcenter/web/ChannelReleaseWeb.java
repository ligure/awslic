package com.actionsoft.eip.cmcenter.web;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.bo.cache.MetaDataMapCache;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.commons.security.mgtgrade.util.GradeSecurityUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.cache.CmSchemaCache;
import com.actionsoft.eip.cmcenter.dao.CmChannel;
import com.actionsoft.eip.cmcenter.dao.CmContent;
import com.actionsoft.eip.cmcenter.dao.CmDaoFactory;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmContentModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaModel;
import com.actionsoft.eip.cmcenter.upgrade.CmBusinessBoAutoUpgrade;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
/**
 * 
 * @description 修改了首页导航图模块，显示效果的修改
 * @version 1.0
 * @author wangaz
 * @update 2013-12-30 上午10:52:50
 */
public class ChannelReleaseWeb extends ActionsoftWeb
{
  public static final int TITLE_LENGTH = 200;
  public static final int PAGE_LINE = 8;

  public ChannelReleaseWeb(UserContext me)
  {
    super(me);
  }

  public String getChannelReleaseFrameWeb()
  {
    String cgi = "./login.wf";
    String sid = super.getContext().getSessionId();
    Hashtable hashTags = new Hashtable();
    hashTags.put("main", cgi + "?sid=" + sid + "&cmd=CmChannel_Release_Open");
    hashTags.put("left", cgi + "?sid=" + sid + "&cmd=CmChannel_ReleaseRight_Open");
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("CmContent_Release_Frames.htm"), hashTags);
  }

  public String getChannelReleasePage(int schemaId, int channelId)
  {
    try
    {
      if (CmBusinessBoAutoUpgrade.getInstance().checkSubPrictureFieldMapSeted()) {
        CmBusinessBoAutoUpgrade.getInstance().setSubPictureFieldMap();
        CmBusinessBoAutoUpgrade.getInstance().setContentFieldMap();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    CmSchemaModel schemaModel = null;
    String title = "<I18N#信息资讯中心>";
    String quickView = "";
    String picView = "";
    String cmsList = "";
    Hashtable channelList = null;
    if (schemaId != -1) {
      schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
      if (schemaModel != null) {
        title = schemaModel._schemaName;
        if (schemaModel._isClose) {
          return AlertWindow.getWarningWindow2("<I18N#提示>", "<I18N#主题>【" + title + "】<I18N#已关闭！您不能查看该主题内容!>");
        }
        channelList = CmChannelCache.getACNotCloseChannelList(super.getContext(), schemaModel);
        if (schemaModel._isShowTopN) {
          quickView = getQuickViewInfo(channelId, channelList, schemaId);
        }
        if (schemaModel._isShowPicPortlet) {
          picView = getPictureViewInfo(schemaId);
        }
        cmsList = getCMSList(schemaId, channelList);
      } else {
        return AlertWindow.getWarningWindow2("<I18N#提示>", "<I18N#所访问的主题已删除，请联系系统管理员>");
      }
    } else {
      channelList = CmChannelCache.getACNotCloseChannelList(super.getContext());
      quickView = getQuickViewInfo(channelId, channelList, schemaId);
      picView = getPictureViewInfo(schemaId);
      cmsList = getCMSList(schemaId, CmUtil.getCMSList(super.getContext(), channelList,String.valueOf(channelId)));
    }
    StringBuffer quickView_picView = new StringBuffer();
    if ((quickView.trim().length() > 0) && (picView.trim().length() > 0)) {
      quickView_picView.append("<table width=\"99%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\" align=\"center\" STYLE=\"table-layout:fixed\">\n");
      quickView_picView.append("<tr>\n");
      quickView_picView.append("<td align=\"left\" valign=\"top\" width=\"50%\" >").append(picView).append("</td>\n");
      quickView_picView.append("<td width=\"50%\" align=\"center\" valign=\"top\">").append(quickView).append("</td>").append("\n");
      quickView_picView.append("</tr>\n");
      quickView_picView.append("</table>\n");
    } else if (quickView.trim().length() > 0) {
      quickView_picView.append("<table width=\"99%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n");
      quickView_picView.append("<tr>\n");
      quickView_picView.append("<td align=\"left\" valign=\"top\" width=\"100%\" >").append(quickView).append("</td>\n");
      quickView_picView.append("</tr>\n");
      quickView_picView.append("</table>\n");
    } else if (picView.trim().length() > 0) {
      quickView_picView.append("<table width=\"99%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n");
      quickView_picView.append("<tr>\n");
      quickView_picView.append("<td align=\"left\" valign=\"top\" width=\"100%\" >").append(picView).append("</td>\n");
      quickView_picView.append("</tr>\n");
      quickView_picView.append("</table>\n");
    }
    Hashtable hashTags = new Hashtable();
    String ManagerPage = "";
    hashTags.put("sid", super.getSIDFlag());
    hashTags.put("title", title);
    hashTags.put("quickView_picView", quickView_picView.toString());
    hashTags.put("contentList", cmsList);
    if (!LICENSE.getASPModel())
      hashTags.put("more", "<a href=''  onClick=\"execMyCommand(frmMain,'CmContent_Search_Open');return false;\"><img title='<I18N#查询更多信息>' src=../aws_img/find_obj.gif border=0></a>");
    else {
      hashTags.put("more", "");
    }
    hashTags.put("readUser", super.getContext().getUID());
    hashTags.put("schemaId", String.valueOf(schemaId));
    hashTags.put("advanceSearchCondition", new ContentReleaseSearchWeb(super.getContext()).getReleaseSearchConditionHtml());
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("CmContent_Release.htm"), hashTags);
  }

  private String getCMSList(int schemaId, Hashtable cmList)
  {
    StringBuffer list = new StringBuffer();
    if (schemaId != -1) {
      if (cmList.size() == 0) {
        return "";
      }
      return getCmsChannelInSchema(schemaId);
    }

    if (cmList.size() > 0) {
      return getCmsChannelInSchema(schemaId);
    }
    return getNotContentChannel(schemaId);
  }

  private String getCmsChannelInSchema(int schemaId)
  {
    StringBuffer list = new StringBuffer();
    Hashtable currentChannelList = new Hashtable();
    CmSchemaModel schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
    if (schemaModel == null)
      currentChannelList = CmChannelCache.getACNotCloseChannelList(getContext());
    else {
      currentChannelList = CmChannelCache.getACNotCloseChannelList(getContext(), schemaModel);
    }
    list.append("<table id=PortalsTable  width=100% cellspacing=0 cellpadding=3 >\n");
    int count = 0;
    Map staticMap = CmUtil.getVisitStaticOfChannel();
    Map channelMap = CmDaoFactory.createContent().getInstanceGroupChannelId();
    for (int ccl = 0; ccl < currentChannelList.size(); ccl++) {
      int channelLeftId = 0; int channelRightId = 0;
      CmChannelModel ccm = (CmChannelModel)currentChannelList.get(new Integer(ccl));
      channelLeftId = ccm._id;
      count++;
      ccl++;
      boolean isHaveRight = false;
      if (ccl < currentChannelList.size()) {
        channelRightId = ((CmChannelModel)currentChannelList.get(new Integer(ccl)))._id;
        isHaveRight = true;
      }

      Map contenListOfChannel = (Map)channelMap.get(Integer.valueOf(channelLeftId));
      Map sameRowOtherChannelContentList = (Map)channelMap.get(Integer.valueOf(channelRightId));

      list.append("<tr><td class=\"PortletContainer\" nowrap id=PortletContainer").append(count).append(" align='left' valign='top' width='50%'>\n");
      list.append(getChannelBar(schemaId, channelLeftId));
      list.append("<tr><td nowrap witdh=50%  colspan=2 align = center>\n");

      list.append(getChannelContent(channelLeftId, contenListOfChannel, schemaId, 8, sameRowOtherChannelContentList, staticMap));
      list.append("</td></tr>\n");
      list.append("</table></td>");

      if (isHaveRight) {
        count++;
        list.append("<td class=\"PortletContainer\" height=100% nowrap witdh=50% id=PortletContainer").append(count).append(" align='right' valign='top'>\n");
        list.append(getChannelBar(schemaId, channelRightId));
        list.append("<tr><td nowrap colspan=2 align = center>\n");

        list.append(getChannelContent(channelRightId, sameRowOtherChannelContentList, schemaId, 8, contenListOfChannel, staticMap));
        list.append("</td></tr></table>\n");
      }
    }
    list.append("</td></tr>\n");
    list.append("</table>\n");

    StringBuffer hiddenValue = new StringBuffer();
    hiddenValue.append("\n<input type='hidden' name='defaultPosition' value=''>");
    list.append(hiddenValue);
    return list.toString();
  }

  private String getNotContentChannel(int schemaId)
  {
    StringBuffer list = new StringBuffer();
    Hashtable hash = CmChannelCache.getACNotCloseChannelList(getContext());
    list.append("<table id=PortalsTable  width=100% cellspacing=0 cellpadding=0 >\n");
    for (int i = 0; i < hash.size(); i++) {
      CmChannelModel model = (CmChannelModel)hash.get(new Integer(i));

      list.append("<tr>");
      list.append("<td nowrap id=PortletContainer").append(i).append("  align='left' valign='top'>\n");
      list.append("<table class=dragTable id=Portlet").append(model._id).append(" ondrag=draging(); ondragend=dragEnd();  width=100%  cellspacing=0 cellpadding=0 >\n");
      list.append(getChannelBar(schemaId, model._id));
      list.append("<tr><td nowrap>" + I18nRes.findValue(getContext().getLanguage(), "此栏目无内容") + "\n").append("</td></tr>\n");
      list.append("<tr><td>");
      list.append(getChannelBottom(model._id, 0));
      list.append("</td></tr>");
      list.append("</table>");
      list.append("</td>");
      i++;
      if (i < hash.size()) {
        CmChannelModel model2 = (CmChannelModel)hash.get(new Integer(i));
        if (model2 == null)
        {
          continue;
        }
        list.append("<td nowrap id=PortletContainer").append(i).append("  align='left' valign='top'>\n");
        list.append("<table class=dragTable id=Portlet").append(model2._id).append(" ondrag=draging(); ondragend=dragEnd();  width=100%  cellspacing=0 cellpadding=0 >\n");
        list.append(getChannelBar(schemaId, model2._id));
        list.append("<tr><td nowrap>" + I18nRes.findValue(getContext().getLanguage(), "此栏目无内容") + "\n").append("</td></tr>\n");
        list.append("<tr><td>");
        list.append(getChannelBottom(model._id, 0));
        list.append("</td></tr>");
        list.append("</table>");
        list.append("</td>");
        list.append("</tr>");
      }
      list.append("</td></tr>\n");
    }
    list.append("</table>");

    StringBuffer hiddenValue = new StringBuffer();
    hiddenValue.append("\n<input type='hidden' name='defaultPosition' value=''>");
    list.append(hiddenValue);
    return list.toString();
  }

  private String getChannelContent(int channelId, Map contenListOfChannel, int schemaId, int PAGE_LINE, Map sameRowOtherChannelContentList, Map staticMap)
  {
    StringBuffer list = new StringBuffer();

    int contentOfOtherChannelNum = 0;
    if ((sameRowOtherChannelContentList != null) && (sameRowOtherChannelContentList.size() > 0)) {
      for (int i = 0; i < sameRowOtherChannelContentList.size(); i++) {
        if (contentOfOtherChannelNum >= PAGE_LINE) {
          break;
        }
        CmContentModel contentModel = (CmContentModel)sameRowOtherChannelContentList.get(new Integer(i));
        if (contentModel == null) {
          continue;
        }
        if (contentModel._isClose.equals("是"))
        {
          continue;
        }
        if (!CmUtil.getContentSecurityList(getContext(), contentModel._bindId)) {
          continue;
        }
        String releaseDate = CmUtil.getReleaseDateStr(schemaId, channelId, contentModel._releaseDate);
        if (releaseDate.trim().length() > 0) {
          contentOfOtherChannelNum++;
        }
      }
    }
    int contentOfChannelNum = 0;
    if ((contenListOfChannel != null) && (contenListOfChannel.size() > 0)) {
      for (int i = 0; i < contenListOfChannel.size(); i++) {
        int clickCount = 0;
        CmContentModel contentModel = (CmContentModel)contenListOfChannel.get(new Integer(i));
        if (contentModel == null) {
          continue;
        }
        if (contentModel._isClose.equals("是")) {
          continue;
        }
        if (staticMap.get(Integer.valueOf(contentModel._id)) != null) {
          clickCount = ((Integer)staticMap.get(Integer.valueOf(contentModel._id))).intValue();
        }

        String clickCounts = clickCount + " " + I18nRes.findValue(getContext().getLanguage(), "aws_platform_次");
        if (!CmUtil.getContentSecurityList(getContext(), contentModel._bindId)) {
          continue;
        }
        String releaseDate = CmUtil.getReleaseDateStr(schemaId, channelId, contentModel._releaseDate);
        if (releaseDate.trim().length() > 0) {
          contentOfChannelNum++;
          if (contentOfChannelNum <= PAGE_LINE) {
            String resultDate = UtilDate.dateFormat(new Date());

            if (contentModel._positionType == 0) {
              list.append("<table width=100% border=0 cellspacing=0 cellpadding=2>\n");
              String archives = "";
              if ((contentModel._archives != null) && 
                (!contentModel._archives.trim().equals(""))) {
                archives = "<img src ='../aws_img/link.gif' border='0' width='10' height='10' alt = " + contentModel._archives + ">";
              }

              if ("".equals(contentModel._displaytitle)) {
                contentModel._title = getCMSTitle(contentModel);
                if (CmUtil.getSubtractionDate(releaseDate, resultDate) <= 5L) {
                  String newImg = "<img src='../aws_img/new1.gif' border='0'>";
                  getContent(list, contentModel, newImg, archives, clickCounts, channelId, releaseDate, schemaId);
                } else {
                  String newImg = "";
                  getContent(list, contentModel, newImg, archives, clickCounts, channelId, releaseDate, schemaId);
                }

              }
              else if (CmUtil.getSubtractionDate(releaseDate, resultDate) <= 5L) {
                String newImg = "<img src='../aws_img/new1.gif' border='0'>";
                getContent(list, contentModel, newImg, archives, clickCounts, channelId, releaseDate, schemaId);
              } else {
                String newImg = "";
                getContent(list, contentModel, newImg, archives, clickCounts, channelId, releaseDate, schemaId);
              }

              list.append("</table>\n");
            }
          }
        }
      }
    }
    if (contentOfChannelNum < contentOfOtherChannelNum) {
      int num = contentOfOtherChannelNum - contentOfChannelNum;
      for (int m = 0; m < num; m++) {
        list.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\"><tr><td><span style='height:18px;'>&nbsp;</span></td></tr></table>");
      }
    }

    list.append(getChannelBottom(channelId, contentOfChannelNum));
    return list.toString();
  }

  private void getContent(StringBuffer list, CmContentModel contentModel, String newImg, String archives, String clickCounts, int channelId, String releaseDate, int schemaId) {
    String fulltitle = getCMSTitle(contentModel);
    if (contentModel._title.indexOf("WORKFLOW:") != -1)
      list.append("<tr>").append("<td nowrap width=2%>").append("&nbsp;&nbsp;<img src = '../aws_img/dot.gif' border=0>&nbsp;</td>").append("<td nowrap width=70% align='left'>").append("<a href='' onclick=\"execMyCommand2(frmMain,").append(contentModel._bindId).append(",8,'WorkFlow_Execute_Worklist_File_Open'); return false;\" title = '").append(fulltitle).append("'>").append(contentModel._displaytitle + newImg).append("</a>&nbsp;").append(archives).append("</td>").append("<td nowrap width=23% align = right>&nbsp;").append(releaseDate).append("</td>").append(
        "<td nowrap align='right' width='60'>" + clickCounts + "</td>").append("</tr>\n");
    else
      list.append("<tr>").append("<td nowrap width=2%>").append("&nbsp;&nbsp;<img src = '../aws_img/dot.gif' border=0>&nbsp;</td>").append("<td nowrap width=70% align='left'>").append("<a href='' onClick=\"openContent(frmMain," + schemaId + ",").append(channelId).append(",").append(contentModel._id).append(",'CmContent_Read_Open');return false;\" title = '").append(contentModel._title).append("'>").append(fulltitle + newImg).append("</a>&nbsp;").append(archives).append("</td>").append("<td nowrap width=23% align = right>&nbsp;").append(releaseDate).append("</td>")
        .append("<td nowrap align='right' width='60'>" + clickCounts + "</td>").append("</tr>\n");
  }

  private String getChannelBar(int schemaId, int channelId)
  {
    CmChannelModel model = (CmChannelModel)CmChannelCache.getModel(channelId);
    StringBuffer list = new StringBuffer();

    String uid = getContext().getUID();
    boolean isSuperManager = GradeSecurityUtil.isSuperMaster(uid);
    String dragTR = ""; String dragTrClassName = "dragTR";
    if (schemaId != -1) {
      CmSchemaModel schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
      if ((schemaModel != null) && (schemaModel._channelBarCSS.trim().length() > 0)) {
        dragTrClassName = schemaModel._channelBarCSS;
      }
    }
    dragTR = "class=" + dragTrClassName;

    list.append("<table class=dragTable id=Portlet").append(channelId).append(" width=100%  cellspacing=0 cellpadding=0 >\n");
    if ((isManager(model._channelManager)) || (isSuperManager)) {
      list.append("<tr>\n");
      String remove = "";
      list.append("<td nowrap align=\"left\" height=28 ").append(dragTR).append(">").append(remove).append("&nbsp;&nbsp;<b><I18N#").append(model._channelName).append("></b></td>\n");
      list.append("<td nowrap ").append(dragTR).append(" align = right>").append(getAddButton(channelId)).append("&nbsp;&nbsp;<a href='' onClick=\"execMyCommand4(frmMain,").append(channelId).append(",'CmChannel_Info_BackList');return false;\">").append("<img src=../aws_img/but_set.png border=0 alt='<I18N#管理此栏目>'>").append("</a>&nbsp;").append("</td>");
      list.append("</tr>\n");
    } else {
      list.append("<tr>\n");
      String remove = "";
      list.append("<td nowrap align=\"left\" height=28 ").append(dragTR).append(">").append(remove).append("&nbsp;&nbsp;<b>").append(model._channelName).append("</b></td>\n");
      list.append("<td nowrap ").append(dragTR).append(" align=right>").append(getAddButton(channelId)).append("</td>");
      list.append("</tr>\n");
    }
    return list.toString();
  }

  public boolean isManager(String managerList)
  {
    boolean flag = false;
    String currUser = getContext().getUID();
    String[] ml = managerList.split(" ");
    for (int i = 0; i < ml.length; i++) {
      if (ml[i].trim().equals("")) {
        continue;
      }
      if (Function.getUID(ml[i].trim()).equals(currUser)) {
        flag = true;
        break;
      }

    }

    return flag;
  }

  private String getAddButton(int channelId)
  {
    CmChannelModel model = (CmChannelModel)CmChannelCache.getModel(channelId);
    String command = "<img src=../aws_img/but_add2.gif border=0 alt='发布内容' >";

    Map workflowList = WorkFlowCache.getList();
    WorkFlowModel useWorkflowModel = null;
    for (int i = 0; i < workflowList.size(); i++) {
      WorkFlowModel workflowModel = (WorkFlowModel)workflowList.get(new Integer(i));
      if ((workflowModel._isClose) || (!workflowModel._flowName.equals("指定发布:" + model._channelName))) {
        continue;
      }
      if (SecurityProxy.checkWorkFlowInUser(super.getContext().getUID(), workflowModel._id)) {
        useWorkflowModel = workflowModel;
      }

    }

    if (useWorkflowModel != null) {
      int wfId = 0;
      try {
        wfId = WorkflowEngine.getInstance().getWorkflowDefId(useWorkflowModel._version_uuid);
      }
      catch (WorkflowException e) {
        e.printStackTrace();
      }
      wfId = wfId == 0 ? useWorkflowModel._id : wfId;
      command = "" +
      		" href='' onClick=\"createNewContent(frmMain," + wfId + ",'" + useWorkflowModel._flowStyle + "'," + UtilDate.yearFormat(new Date()) + ");return false\">" + command + "</a>";
    } else {
      command = "";
    }

    StringBuffer list = new StringBuffer();

    list.append(command);
    return list.toString();
  }

  private String getChannelBottom(int channelId, int rowcount)
  {
    CmChannelModel model = (CmChannelModel)CmChannelCache.getModel(channelId);
    String command = "[<a href='#' onclick=\"OpenChannelIdList(frmMain,'CmChannel_Item_List_Open'," + channelId + ");return false;\"><I18N#更多></a>]";

    Map workflowList = WorkFlowCache.getList();
    WorkFlowModel useWorkflowModel = null;
    for (int i = 0; i < workflowList.size(); i++) {
      WorkFlowModel workflowModel = (WorkFlowModel)workflowList.get(new Integer(i));
      if ((workflowModel._isClose) || (!workflowModel._flowName.equals("指定发布:" + model._channelName)))
      {
        continue;
      }

      useWorkflowModel = workflowModel;
      break;
    }

    if ((useWorkflowModel != null) && (rowcount >= 8))
      command = "<a href='' onClick=\"createNewContent(frmMain," + useWorkflowModel._id + ",'" + useWorkflowModel._flowStyle + "'," + UtilDate.yearFormat(new Date()) + ");return false\"><b>" + command + "</b></a>";
    else {
      command = "";
    }

    StringBuffer list = new StringBuffer();

    list.append("<table width=100% border=0 cellspacing=0 cellpadding=0>");
    list.append("<tr ><td nowrap width=96% align='right'>" + (command.trim().length() == 0 ? "&nbsp;" : command) + "</td></tr>");
    list.append("</table>");
    return list.toString();
  }

  private String getQuickViewInfo(int channelId, Hashtable channelList, int schemaId) {
    CmSchemaModel schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
    int topN = 10;
    if ((schemaModel != null) && (schemaModel._topNum > 0)) {
      topN = schemaModel._topNum;
    }
    StringBuffer info = new StringBuffer();
    info.append("<table  class=dragTable width=\"95%\" height=\"100%\">\n");
    info.append("<tr>\n");
    info.append("<td class=dragTR height=20><I18N#最新的> ").append(topN).append(" <I18N#条资讯></td>\n");
    info.append("</tr>\n");
    info.append("<tr>\n");
    info.append("<td><marquee behavior='scroll' onMouseOver=this.stop() onMouseOut=this.start() scrollamount=\"2.3\" scrolldelay=\"50\" direction=\"up\" width=\"100%\" height=\"230px\">\n");

    info.append(getQuickView(channelId, channelList, topN, schemaId));

    info.append("</marquee></td>\n");
    info.append("</tr>\n");
    info.append("<tr>\n");
    info.append("<td>&nbsp;</td>\n");
    info.append("</tr>\n");
    info.append("</table>\n");
    return info.toString();
  }

  public String getPictureView(int schemaId)
  {
    Hashtable picList = CmUtil.getPictureInfo(getContext());
    StringBuffer pics = new StringBuffer();
    String url = "";
    int picNum = 0;
    if (picList.size() > 0) {
      for (int i = 0; i < picList.size(); i++) {
        CmContentModel contentModel = (CmContentModel)picList.get(new Integer(i));
        if (contentModel == null)
        {
          continue;
        }
        if (schemaId != -1) {
          CmSchemaModel schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
          if (schemaModel != null) {
            String title = schemaModel._schemaName;
            if (schemaModel._isClose)
              return AlertWindow.getWarningWindow2("提示", "主题【" + title + "】已关闭！您不能查看该主题内容!");
          }
          else {
            return AlertWindow.getWarningWindow2("提示", "所访问的主题已删除，请联系系统管理员");
          }
          CmChannelModel channelModel = (CmChannelModel)CmChannelCache.getModel(contentModel._channelId);
          if (channelModel == null)
            continue;
          if (!channelModel._channelStyleId.equals(String.valueOf(schemaId)))
          {
            continue;
          }
        }

        if (!CmUtil.getContentSecurityList(getContext(), contentModel._bindId))
        {
          continue;
        }
        picNum++; if (picNum > 5)
        {
          break;
        }

        MetaDataMapModel metadataMapModel = (MetaDataMapModel)MetaDataMapCache.getModel("EIP_CM_CONTENT", "SUBPICTURE");

        UtilString util = new UtilString(contentModel._subPicture);
        Vector imgFileNames = util.split("@@@@");
        for (int j = 0; j < imgFileNames.size(); j++) {
          String imgFileName = imgFileNames.get(j).toString();
          if (imgFileName.trim().length() == 0) {
            continue;
          }
          String image = "";
          try {
            image = "./downfile.wf?flag1=" + contentModel._id + "&flag2=" + metadataMapModel.getId() + "&sid=" + super.getContext().getSessionId() + "&filename=" + URLEncoder.encode(imgFileName, "UTF-8") + "&rootDir=FormFile";
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
          }

          url = "./login.wf?sid=" + super.getContext().getSessionId() + "&cmd=CmContent_Read_Open&schemaId=-1&channelId=" + contentModel._channelId + "&contentId=" + contentModel._id;

          String fName = "";
          String subTitle = contentModel._displaytitle;
          if (subTitle.indexOf("__eol__") >= 0) {
            subTitle = subTitle.substring(0, subTitle.indexOf("__eol__"));
          }
          fName = new UtilString(new UtilString(subTitle).replace("\"", "’")).replace("'", "’");

          if ("".equals(fName.trim())) {
            subTitle = contentModel._subTitle;
            if (subTitle.indexOf("__eol__") >= 0) {
              subTitle = subTitle.substring(0, subTitle.indexOf("__eol__"));
            }
            fName = new UtilString(new UtilString(subTitle).replace("\"", "’")).replace("'", "’");
          }
          if (("".equals(fName.trim())) && 
            (contentModel._title != null)) {
            fName = new UtilString(new UtilString(contentModel._title).replace("\"", "‘")).replace("'", "‘");
          }
         /**
          *图片大小的修改
          */
          pics.append("<a style='top:0;left:0;position:absolute;' href=\"").append(url).append("\" ").append(i == 0 ? "class=\"show\"" : "").append(" target=\"_blank\">");
          pics.append("<img border='0' style='width:100%px;height:100%;'src=\"").append(image).append("\" alt=\"").append(fName).append("\" title=\"\" rel=\"<h3>").append(fName).append("</h3>").append("").append("\"/>");
          pics.append("</a>");
        }
      }
    }
    return pics.toString();
  }

  private String getPictureViewInfo(int schemaId) {
    StringBuffer html = new StringBuffer();
    //去掉图片新闻的黑标题行
    html.append("<!-- 图片新闻框架-->\n");
    html.append("<table  id='myt' class=dragTable width=\"95%\" height=\"100%\">\n");
    html.append("<tr>\n");
    html.append("<td><div id=\"gallery\" style='position :relative;height:350px;width:100%;background-color:#fffff;vertical-align:middle;border:1px solid #666;overflow:hidden;'>");
    html.append("<div class=\"caption\" style='display:none;position:absolute;bottom:0;width:100%;background-color:#000;color:#fff;z-index:600;'>\n");
    html.append("<div class=\"content\"></div>\n");
    html.append("</div>\n");
    html.append("</div>\n");
    html.append("<div class=\"clear\"></div></td>\n");
    html.append("</tr>\n");
    html.append("</table>\n");
    return html.toString();
  }

  private String getQuickView(int channelId, Hashtable channelList, int topN, int schemaId)
  {
    Hashtable cmsList = CmUtil.getQuickViewCMSList2(super.getContext(), topN + 10, channelList);
    String currRole = getContext().getRoleModel().getRoleName();
    StringBuffer list = new StringBuffer();

    list.append("<table border=0 cellpadding=0 valign=top cellspacing=2 width=100% >\n");
    int count = 0;
    int m = 0;
    for (int i = 0; i < cmsList.size(); i++) {
      CmContentModel contentModel = (CmContentModel)cmsList.get(new Integer(i));
      if (!CmUtil.getContentSecurityList(getContext(), contentModel._bindId)) {
        continue;
      }
      String releaseDate = CmUtil.getReleaseDateStr(schemaId, channelId > 0 ? channelId : contentModel._channelId, contentModel._releaseDate);
      if (m == topN) {
        break;
      }
      if (releaseDate.trim().length() > 0) {
        count++;
        String resultDate = UtilDate.dateFormat(new Date());

        String fulltitle = getCMSTitle(contentModel);
        String newImg;
        if (CmUtil.getSubtractionDate(releaseDate, resultDate) <= 3L)
          newImg = "<img src='../aws_img/new1.gif' border='0'>";
        else {
          newImg = "";
        }
        list.append("<tr><td nowrap width=1% height=20 style='vertical-align:top;'>&nbsp;</td><td height=20 style='word-break:break-all' width=80%>&middot;<a href='' onClick='openContent(frmMain,0,").append(channelId).append(",").append(contentModel._id).append(");return false;'><span class='quickViewTitle' title='" + contentModel._title + "'>").append(fulltitle + newImg).append("</span></a>").append("</td><td width=19% nowrap><font color=\"#666666\">").append(releaseDate).append("</font></td></tr>");
        count++;
      }
      m++;
    }

    list.append("</table><br>\n");
    if (count == 0)
      list.append("&nbsp;<font color=gray><I18N#没有新信息></font>");
    return list.toString();
  }

  private String getQuickView(int schemaId, int channelId, Hashtable channelList)
  {
    if (schemaId > 0) {
      return "";
    }
    Hashtable cmsList = CmUtil.getQuickViewCMSList2(super.getContext(), 6, channelList);

    StringBuffer list = new StringBuffer();

    list.append("<table width=100% border=0 cellspacing=0 cellpadding=0>");
    list.append("<tr class=aws-portal-window-titlebar-title><td nowrap >头五条</td></tr>");
    list.append("</table>");
    list.append("<script>var scrollerdelay='1000';var scrollerwidth='100%';var scrollerheight='105px';var scrollerbgcolor='';var scrollerbackground='';\n");
    list.append("var messages=new Array();\n");
    int count = 0;
    for (int i = 0; i < cmsList.size(); i++) {
      CmContentModel contentModel = (CmContentModel)cmsList.get(new Integer(i));
      String releaseDate = CmUtil.getReleaseDateStr(schemaId, channelId, contentModel._releaseDate);
      if (releaseDate.trim().length() > 0) {
        count++;
        String resultDate = UtilDate.dateFormat(new Date());
        String newImg = "";

        list.append("messages[" + i + "]=\"<table width=100% border=0 cellspacing=0 cellpadding=2>");
        if (CmUtil.getSubtractionDate(releaseDate, resultDate) <= 3L) {
          String fulltitle = "";
          if (contentModel._title.length() > 23) {
            fulltitle = contentModel._title;
            fulltitle = contentModel._title.substring(0, 23) + "...";
          } else {
            fulltitle = contentModel._title;
          }
          newImg = "<img src='../aws_img/new1.gif' border='0'>";
          list.append("<tr><td nowrap width=2%>&nbsp;</td><td nowrap width=81%><b><a href=''  onClick='openContent(frmMain," + schemaId + ",").append(channelId).append(",").append(contentModel._id).append(");return false;'>").append(i + 1 + "." + fulltitle + newImg).append("</a></td><td nowrap width=15%><img src=../aws_img/calendar_task1.gif>&nbsp;").append(releaseDate).append("</td><td nowrap width=2%>&nbsp;</td></tr>");
        } else {
          String fulltitle = "";
          if (contentModel._title.length() > 23) {
            fulltitle = contentModel._title;
            fulltitle = contentModel._title.substring(0, 23) + "...";
          } else {
            fulltitle = contentModel._title;
          }
          newImg = "";
          list.append("<tr><td nowrap width=2%>&nbsp;</td><td nowrap width=81%><a href=''  onClick='openContent(frmMain," + schemaId + ",").append(channelId).append(",").append(contentModel._id).append(");return false;'>").append(fulltitle + newImg).append("</a></td><td nowrap width=15%><img src=../aws_img/calendar_task1.gif>&nbsp;").append(releaseDate).append("</td><td nowrap width=2%>&nbsp;</td></tr>");
        }
        list.append("<tr><td nowrap width=2%>&nbsp;</td><td nowrap colspan=2><font color=gray>作者:").append(contentModel._releaseMan).append("&nbsp;&nbsp;来自:").append(contentModel._releaseDepartment).append("&nbsp;&nbsp;").append(contentModel._isTalk.equals("是") ? "<img src=../aws_img/newitem.gif>可对此发出评论" : "").append("</font></td><td nowrap width=2%>&nbsp;</td></tr>");

        if ((contentModel._prePicture != null) && (contentModel._prePicture.length() > 5)) {
          list.append("<tr><td nowrap width=2%>&nbsp;</td><td nowrap colspan=2>").append(contentModel._prePicture).append("</td><td nowrap width=2%>&nbsp;</td></tr>");
        }
        list.append("</table><br>\";\n");
      }
    }
    if (count == 0)
      list.append("messages[0]='&nbsp;<font color=gray><I18N#没有新信息></font>'");
    list.append("</script>");
    return list.toString();
  }

  public String saveNewPosition(String newPositionStr)
  {
    String tmp = "";
    Connection conn = null;
    try {
      conn = DBSql.open();
      if (newPositionStr.length() > 0) {
        Vector list = new UtilString(newPositionStr).split(" ");
        int sortNo = 0;
        for (int i = 0; i < list.size(); i++) {
          tmp = (String)list.get(i);
          if ((tmp == null) || (tmp.trim().length() == 0))
            continue;
          int feedId = Integer.parseInt(tmp.substring(7));
          sortNo++;
          DBSql.executeUpdate(conn, "update SYS_CMCHANNEL set sortno=" + sortNo + "  where id=" + feedId);
        }
      }
      CmChannelCache.reload();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      DBSql.close(conn, null, null);
    }

    return getChannelReleasePage(-1, 0);
  }

  /**
	 * 根据标题设置属性，返回标题显示样式
	 * 
	 * @param contentModel
	 * @return
	 */
	public static String getCMSTitle(CmContentModel contentModel) {
		String title = "";
		String color_begin = "";
		String color_end = "";

		String strong_begin = "";
		String strong_end = "";
		if (contentModel._zyts != null && "是".equals(contentModel._zyts)) {
			color_begin = "<font color='#AC0A00' title='" + contentModel._title + "'>";
			color_end = "</font>";
		}

		if (contentModel._btjc != null && "是".equals(contentModel._btjc)) {
			strong_begin = "<b title='" + contentModel._title + "'>";
			strong_end = "</b>";
		}

		String titlefull = contentModel._displaytitle == null || contentModel._displaytitle.trim().length() == 0 ? contentModel._title : contentModel._displaytitle;
		if (titlefull != "") {
			int num = 0, size = 0;// 字节数
			String displayTitle = "";
			for (int i = 0; i < titlefull.length(); i++) {
				char a = titlefull.charAt(i);
				if (a > 255 || a < 0) {// {//ASCII码的范围在0-255，汉字占2字节，不在内
					num += 2;
				} else {
					num += 1;
				}
				displayTitle += a;
				if (num > 50) {
					titlefull = displayTitle + "...";
					break;
				}
			}

			// if(num>50){
			// titlefull=titlefull.substring(0,25)+"...";
			// }
		}
		// 继鲜版本不是显示评论数
		// int talkQuan = getTalkQquan(contentModel._id);
		// if(talkQuan == 0) {
		// title = color_begin+strong_begin+titlefull+strong_end+color_end;
		// }else {
		// title =
		// color_begin+strong_begin+titlefull+"("+talkQuan+")"+strong_end+color_end;
		// }
		title = color_begin + strong_begin + titlefull + strong_end + color_end;
		return title;
	}


  private String getCMSDisplayTitle(CmContentModel contentModel)
  {
    String title = "";
    String color_begin = "";
    String color_end = "";

    String strong_begin = "";
    String strong_end = "";
    if ((contentModel._zyts != null) && ("是".equals(contentModel._zyts))) {
      color_begin = "<font color='#AC0A00'>";
      color_end = "</font>";
    }

    if ((contentModel._btjc != null) && ("是".equals(contentModel._btjc))) {
      strong_begin = "<b>";
      strong_end = "</b>";
    }

    String titlefull = (contentModel._displaytitle == null) || (contentModel._displaytitle.trim().length() == 0) ? contentModel._title : contentModel._displaytitle;
    if (titlefull.length() > 23)
    {
      titlefull = titlefull.substring(0, 23) + "...";
    }

    title = color_begin + strong_begin + titlefull + strong_end + color_end;
    return title;
  }

  public String getCMSTitle(String btjc, String zyts, int contentId, String title2) {
    String title = "";
    String color_begin = "";
    String color_end = "";

    String strong_begin = "";
    String strong_end = "";
    if ("是".equals(zyts)) {
      color_begin = "<font color='#AC0A00'>";
      color_end = "</font>";
    }

    if ("是".equals(btjc)) {
      strong_begin = "<b>";
      color_end = "</b>";
    }
    int talkQuan = getTalkQquan(contentId);
    if ((title2 != null) && (title2.length() > 35)) {
      title2 = title2.substring(0, 35) + "...";
    }

    title = color_begin + strong_begin + title2 + strong_end + color_end;
    return title;
  }

  public static int getTalkQquan(int contentid)
  {
    int quan = 0;
    quan = DBSql.getInt("select count(*) c from eip_cmcontentread where contentid=" + contentid + " and talkname is not null and talkname<>''", "c");
    return quan;
  }

  public int saveStore(int channelSortId, int channelChangeId)
  {
    int tmp_ChangeSortNO = 0;
    CmChannelModel sortChannelModel = (CmChannelModel)CmChannelCache.getModel(channelSortId);
    CmChannelModel changeChannelModel = (CmChannelModel)CmChannelCache.getModel(channelChangeId);
    if ((sortChannelModel != null) && (changeChannelModel != null)) {
      tmp_ChangeSortNO = sortChannelModel._sortno;
      sortChannelModel._sortno = changeChannelModel._sortno;
      changeChannelModel._sortno = tmp_ChangeSortNO;
      int flag = CmDaoFactory.createChannel().store(sortChannelModel);
      if (flag <= 0) {
        return -1;
      }
      flag = CmDaoFactory.createChannel().store(changeChannelModel);
      if (flag <= 0) {
        return -1;
      }
    }
    return 1;
  }
}