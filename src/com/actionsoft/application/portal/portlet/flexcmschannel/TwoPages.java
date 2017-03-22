package com.actionsoft.application.portal.portlet.flexcmschannel;

import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.cache.MetaDataMapCache;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmContentModel;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TwoPages extends ActionsoftWeb
{
  public static final int TITLE_LENGTH = 20;

  public TwoPages(UserContext uc)
  {
    super(uc);
  }

  public String getMainPage(String top, String channelTab, String channelConf, String isMorePage, String start, String isDepartment)
    throws UnsupportedEncodingException
  {
    String css = " .dojoxGridHeader .dojoxGridCell{ display:none; }";
    if (isMorePage.equals("1")) {
      css = "";
    }
    String threetop = String.valueOf(3 * Integer.parseInt(top));
    String sid = URLEncoder.encode(super.getContext().getSessionId(), "UTF-8");
    if (channelTab.trim().length() == 0) {
      return AlertWindow.getWarningWindow(I18nRes.findValue(super.getContext().getLanguage(), "portlet实施错误，参数channelTab缺少值"));
    }

    Vector tabs = new UtilString(channelTab).split(",");
    Vector channelNames = new Vector();
    Vector channels = new Vector();

    Vector tmpChannels = new UtilString(channelConf).split("|");
    for (int i = 0; i < tmpChannels.size(); i++) {
      String tmpChannel = (String)tmpChannels.get(i);
      channelNames.add(tmpChannel);
      Vector tmpList = new UtilString(tmpChannel).split(",");
      String channelIds = "";
      for (int ii = 0; ii < tmpList.size(); ii++) {
        String channelName = (String)tmpList.get(ii);
        if (channelName.equals("*")) {
          channelIds = "*";
        } else {
          CmChannelModel model = (CmChannelModel)CmChannelCache.getModel(channelName);
          if (model == null) {
            return AlertWindow.getWarningWindow(I18nRes.findValue(super.getContext().getLanguage(), "portlet实施错误，参数channelTab指未指定或指定错误， 例如 channelTab=最新通知,最新公告"));
          }

          channelIds = channelIds + model._id + ",";
        }
      }
      channelIds = channelIds.indexOf(",") > -1 ? channelIds.substring(0, channelIds.length() - 1) : channelIds;
      channels.add(channelIds);
    }

    StringBuffer html = new StringBuffer();
    StringBuffer js = new StringBuffer();
    String str = "";
    if (tabs.size() > 0)
    {
      if (tabs.size() == 1) {
        html.append(getTabHtml(sid, top, threetop, 1, channels, tabs, channelNames, isMorePage, start, isDepartment, channelConf, 0));
      str = jsSsFunction(sid, top, threetop, 1, channels, tabs, channelNames, isMorePage, start, isDepartment, channelConf, 0);
      }
      else {
        if (channels.size() != tabs.size()) {
          return AlertWindow.getWarningWindow(I18nRes.findValue(super.getContext().getLanguage(), "portlet实施错误，参数channelConf指定的栏目分类个数与页签分页个数不匹配") + "。channelConf=" + channelConf + I18nRes.findValue(super.getContext().getLanguage(), "例如 channelTab=最新通知,最新公告&channelConf=通知|公告"));
        }
        html.append("<div dojoType='dijit.layout.TabContainer' useSlider='false' persist='false' tabStrip='true' class='dojoTabPane'>\n");
        for (int i = 0; i < tabs.size(); i++) {
          html.append(getTabHtml(sid, top, threetop, tabs.size(), channels, tabs, channelNames, isMorePage, start, isDepartment, channelConf, i));
        str = jsSsFunction(sid, top, threetop, tabs.size(), channels, tabs, channelNames, isMorePage, start, isDepartment, channelConf, i);
          js.append(jsFunction(i, top));
        }
        html.append("</div>");
      }

    }
    StringBuffer sbif = new StringBuffer();
    sbif.append("<IFRAME frameborder=\"0\"  src='./login.wf?sid="+super.getContext().getSessionId()+"&cmd=CmChannel_Release_Portlet' style='width:100%;height:320px;border:0px;margin:0;padding:0;'></IFRAME>");
    StringBuffer sbif1 = new StringBuffer();
    sbif1.append("<IFRAME frameborder=\"0\"  src='./login.wf?sid="+super.getContext().getSessionId()+"&cmd=CmChannel_Item_List_Open&channelId="+top+"' style='width:100%;height:505px;border:0px;margin:0;padding:0;'>111111111111111</IFRAME>");
  //./login.wf?sid="+getContext().getSessionId()+"&amp;cmd=CmChannel_Item_List_Open&channelId=30'
    //  增加二级页面的图片
    StringBuffer towsb = new StringBuffer();
   // String sql = "select ID,SUBPICTURE from (select ID,SUBPICTURE from EIP_CM_CONTENT WHERE SUBPICTURE IS NOT NULL AND SUBPICTURE LIKE 'LETV_%' ORDER BY updatedate DESC) where rownum = 1";
 	String sql = "select * from (select *" +
	"          from EIP_CM_CONTENT" +
	"         WHERE SUBPICTURE IS NOT NULL" +
	"         ORDER BY updatedate DESC)" +
	" where rownum = 1";
    String filename = DBSql.getString(sql, "SUBPICTURE");
 	int id = DBSql.getInt(sql, "ID"); 
	 MetaDataModel metaDataModel = (MetaDataModel)MetaDataCache.getModel("EIP_CM_CONTENT");
	 MetaDataMapModel metaDataMapModel = (MetaDataMapModel)MetaDataMapCache.getModel(metaDataModel.getId(), "SUBPICTURE");
	 towsb.append("<div align='center' ><img style='width:280;height:148px;' name=subPicture onclick='gridTitleClick("+id+",30)' onload=\"javascript:if(this.width>110){this.width=110;};\" src ='./downfile.wf?flag1=")
        .append(id).append("&flag2=").append(metaDataMapModel.getId()).append("&sid=").append(super.getContext().getSessionId()).append("&rootDir=FormFile&filename=")
        .append(filename).append("' border=0></div>");
    Hashtable hashTags = new Hashtable();
    hashTags.put("iframe", sbif);
    hashTags.put("iframe1", sbif1);
    hashTags.put("title1", channelConf);
    hashTags.put("towsb", towsb);
    hashTags.put("list", html.toString());
    hashTags.put("js", js.toString());
    hashTags.put("jsSs", str);
    hashTags.put("sid", getContext().getSessionId());
    hashTags.put("uid", getContext().getUID());
    hashTags.put("top", top);
    hashTags.put("hiddenHead", css);
    hashTags.put("isMorePage", isMorePage);
    hashTags.put("isDepartment", isDepartment);
    hashTags.put("channelConf", channels.get(0).toString());
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("portlet_TwoPagesCms.htm"), hashTags);
  }

  private WorkFlowModel getChannelToWorkflow(String ChannelName)
  {
    Map h = WorkFlowCache.getList();
    WorkFlowModel wfmodel = null;
    if (h != null) {
      for (int i = 0; i < h.size(); i++) {
        WorkFlowModel model = (WorkFlowModel)h.get(new Integer(i));
        if ((model._flowName.equals("指定发布:" + ChannelName)) && (!model._isClose)) {
          wfmodel = model;
          break;
        }
      }
    }
    return wfmodel;
  }

  private String getTabHtml(String sid, String top, String threetop, int tabCount, Vector channels, Vector tabs, Vector channelNames, String isMorePage, String start, String isDepartment, String channelConf, int m)
  {
    StringBuffer html = new StringBuffer();
    WorkFlowModel flowModel = new WorkFlowModel();
    if (tabCount == 1) {
      boolean isAc = false;
      Hashtable hatable = CmChannelCache.getACNotCloseChannelList(getContext());
      if (hatable.size() > 0) {
        for (int i = 0; i < hatable.size(); i++) {
          CmChannelModel channelModel = (CmChannelModel)hatable.get(new Integer(i));
          try {
            if (channelModel._id == Integer.parseInt(channels.get(0).toString())) {
              isAc = true;
              flowModel = getChannelToWorkflow(channelModel._channelName);
            }
          } catch (Exception e) {
            isAc = false;
          }
        }
      }
//      html.append("<table>")
//      .append("<tr><td>1</td><td>2</td><td>3</td><td>4</td></tr>");
//      html.append("</table>");
      html.append("<div dojoType='dijit.layout.BorderContainer'  style='width:100%; height:100%; border:0px; padding:0px; margin:0px;'>\n");
      html.append("<div  dojoType='dijit.layout.ContentPane' region='center'  style='width:100%; height:100%; border:0px; padding:0px; margin:0px;'>\n");
     html.append(" <span id='zibiaodate' dojoType='dojo.data.ItemFileWriteStore'  style=\"background-color:#00C\" jsId='jsonStore'  url='./login.wf?sid=").append(sid).append("&cmd=Portal_Execute_Portlet_FlexCMSChannelGetListData&top=").append(top).append("&channelConf=").append(channels.get(0).toString()).append("&start=").append(start).append("&isDepartment=").append(isDepartment).append("&isMore=").append(isMorePage).append("'> </span>");
      html.append("<table dojoType='dojox.grid.DataGrid' jsid='grid' id='grid' store='jsonStore' rowsPerPage='20' rowSelector='0px' noDataMessage='<I18N#无信息>'>");
      html.append("<thead>");
      html.append("<tr height=\"30px\">");
      html.append("<th  field='title' width='480px' height='30px' formatter='formatGridTitle'><I18N#标题></th>");
      html.append("<th  field='date' width='155px' height='30px' ><I18N#日期></th>");
      html.append("<th  field='name' width='155px'  ><I18N#发布人></th>");
     // html.append("<th field='departmentName' height='30px' width='116px' ><I18N#部门></th>");
      html.append("</tr>\n");
      html.append("</thead>\n");
      html.append("</table>\n");
      html.append("</div>\n");
      html.append("<div dojoType='dijit.layout.ContentPane' region='bottom' style='width:100%;border:0px; padding:0px; margin:0px;'>\n");
      html.append("<div id='moreTaskEntrance' style='float:right;  padding-top:-2px'>");
      html.append("<a   href='javascript:void(0);' onclick=\"openLocation('./login.wf?sid=").append(super.getContext().getSessionId()).append("&cmd=TwoPages&top=").append(threetop).append("&channelTab=").append(tabs.get(0).toString()).append("&channelConf=").append(channelNames.get(0).toString()).append("&isDepartment=").append(isDepartment).append("&isMorePage=").append(isMorePage).append("','mainFrame','").append(tabs.get(0).toString()).append("');return false;\" >[<I18N#更多>]</a>\n");
      html.append("</div>\n");


      if ((isAc) && (SecurityProxy.checkWorkFlowInUser(super.getContext().getUID(), flowModel._id))) {
        html.append("<div  id='public' style='float:right;  padding-top:-2px'><a href='#' onclick=\"openPublic('./login.wf?sid=" + super.getContext().getSessionId() + "&cmd=Portal_Execute_Portlet_QuickCMS_Create&channelId=" + channels.get(0) + "&title=','');return false;\">[<I18N#发布>]</a></div>\n");
      }
      html.append("<div id='page'>\n");
      String td1 = "<td align='center' valign='middle' nowrap='nowrap'>";
      String td2 = "</td>\n";

      html.append("<div dojoType=\"dijit.Toolbar\" id=\"dojo_pagebar_toolbar\" style=\"padding-top:3px;\">");
      html.append("<table border='0' cellspacing='0' cellpadding='0'>\n<tr>");
      html.append(td1).append("<div id=\"dojo_pagebar_first\" dojoType=\"dijit.form.Button\"  title='<I18N#首页>'></div>").append(td2);
      html.append(td1).append("<div id=\"dojo_pagebar_pre\" dojoType=\"dijit.form.Button\"   title='<I18N#上一页>'></div>").append(td2);
      html.append(td1).append("<div dojoType=\"dijit.ToolbarSeparator\"></div>\n").append(td2);
      html.append(td1).append("&nbsp;<I18N#第>").append(td2);
      html.append(td1).append("<div dojoType=\"dijit.form.TextBox\" id=\"dojo_pagebar_textbox\" style='text-align:center;width:20px;margin:2xp 6px 0px 6px;' title='<I18N#请输入页数>'></div>").append(td2);
      html.append(td1).append("<I18N#页>&nbsp;<I18N#共><span id='dojo_pagebar_pagecount'>0</span><I18N#页>&nbsp;").append(td2);
      html.append(td1).append("<div dojoType=\"dijit.ToolbarSeparator\"></div>\n").append(td2);
      html.append(td1).append("<div id=\"dojo_pagebar_next\" dojoType=\"dijit.form.Button\" title='<I18N#下一页>'></div>").append(td2);
      html.append(td1).append("<div id=\"dojo_pagebar_end\" dojoType=\"dijit.form.Button\" title='<I18N#尾页>'></div>").append(td2);
      html.append("<td align='center' valign='middle' nowrap='nowrap' width='100%'></td>\n");
      html.append(td1).append("<div id=\"dojo_pagebar_info\"></div>").append(td2);
      html.append("</tr>\n");
      html.append("</table>\n");
      html.append("</div>\n");

      html.append("<script>\n");
      html.append("function initGridPagebar() {").append("\n");
      html.append(" dojo.connect(dojo.byId('dojo_pagebar_first'),'onclick',function(){ gotoFirstPages(); });").append("\n");
      html.append(" dojo.connect(dojo.byId('dojo_pagebar_pre'),'onclick',function(){ gotoPrevPage(); });").append("\n");
      html.append(" dojo.connect(dojo.byId('dojo_pagebar_next'),'onclick',function(){ gotoNextPage(); });").append("\n");
      html.append(" dojo.connect(dojo.byId('dojo_pagebar_end'),'onclick',function(){ gotoLastPage(); });").append("\n");
      html.append(" dojo.connect(dojo.byId('dojo_pagebar_textbox'),'onkeypress',function(e){  gotoInputPage(e); });").append("\n");
      html.append("}").append("\n");
      html.append("</script>\n");
      html.append("</div>\n");
      html.append("</div>\n");
      html.append("</div>\n");
    } else {
      html.append("<div  dojoType='dijit.layout.ContentPane'  title='").append(I18nRes.findValue(super.getContext().getLanguage(), tabs.get(m).toString())).append("'  style='width:100%; height:100%; border:0px; padding:0px; margin:5px;'>");
      html.append("<div dojoType='dijit.layout.BorderContainer' style='width:100%; height:100%; border:0px; padding:0px; margin:0px;'>\n");
      html.append("<div  dojoType='dijit.layout.ContentPane' region='center' style='width:100%; border:0px; padding:0px; margin:0px;'>\n");
      html.append("<span dojoType='dojo.data.ItemFileWriteStore'  jsId='jsonStore' url='./login.wf?sid=").append(sid).append("&cmd=Portal_Execute_Portlet_FlexCMSChannelGetListData&top=").append(top).append("&channelConf=").append(channels.get(m).toString()).append("&start=").append(start).append("&isDepartment=").append(isDepartment).append("&isMore=").append(isMorePage).append("'></span>\n");
      html.append("<table dojoType='dojox.grid.DataGrid' jsid='grid").append(m).append("' id='grid").append(m).append("' store='jsonStore' rowsPerPage='20' rowSelector='0px' noDataMessage='<I18N#无信息>'>\n");
      html.append("<thead>\n");
      html.append("<tr>\n");
      html.append("<th field='title' width='480px'formatter='formatGrid").append(m).append("Title'><I18N#标题></th>\n");
      html.append("<th field='date' width='155px' ><I18N#日期></th>\n");
      html.append("<th field='name' width='155px' ><I18N#发布人></th>\n");
     // html.append("<th field='departmentName' width='56px' ><I18N#部门></th>");
      html.append("</tr>\n");
      html.append("</thead>\n");
      html.append("</table>\n");
      html.append("</div>\n");
      html.append("<div dojoType='dijit.layout.ContentPane' region='bottom' style='width:100%; border:0px; padding:0px; margin:0px;'>\n");
      html.append("<div id='moreTask").append(m).append("Entrance' style='float:right;'>\n");
      html.append("<a href='javascript:void(0);' onclick=\"openLocation('./login.wf?sid=").append(super.getContext().getSessionId()).append("&cmd=Portal_Execute_Portlet_FlexCMSChannelMore&top=").append(threetop).append("&channelTab=").append(tabs.get(m).toString()).append("&channelConf=").append(channelNames.get(m).toString()).append("&isDepartment=").append(isDepartment).append("&isMorePage=").append(isMorePage).append("','mainFrame','").append(I18nRes.findValue(super.getContext().getLanguage(), tabs.get(m).toString())).append("');return false;\" >[<I18N#更多>]</a>\n");
      html.append("</div>\n");
      html.append("</div>\n");
      html.append("</div>\n");
      html.append("</div>\n");
    }
    return html.toString();
  }
/*
 * 公告搜索栏
 */
  private String jsSsFunction(String sid, String top, String threetop, int tabCount, Vector channels, Vector tabs, Vector channelNames, String isMorePage, String start, String isDepartment, String channelConf, int m){
	  StringBuffer js = new StringBuffer();
	  js.append("function onSousu(){").append(" var oTxt =document.getElementById(\"text1\");").
	  append("openPublic('./login.wf?sid=" + super.getContext().getSessionId() + "&cmd=Portal_Execute_Portlet_QuickCMS_Create&channelId=" + channels.get(0) + "&title=','');");
	  js.append("}");
	  return js.toString();
  }
  
  private String jsFunction(int i, String top)
  {
    StringBuffer js = new StringBuffer();
    js.append("function formatGrid").append(i).append("Title(inValue, rowNum){\n");
    js.append("var values").append(i).append("=inValue.split('&lt;');\n");
    js.append("var item").append(i).append(" = grid").append(i).append(".getItem(rowNum);\n");
    js.append("if(flag==true){\n");
    js.append("changeMoreTask").append(i).append("Button(item").append(i).append(".count);\n");
    js.append(" }\n");
    js.append("if(item").append(i).append(".isJq=='是'&&item" + i + ".titlered == '是'){  \n");
    js.append("return \"<a name='gridTitle'  style='color:#AC0A00;font-weight:bold;TEXT-DECORATION: none' href='#' onclick='gridTitleClick(\"+item").append(i).append(".id+\",\"+item" + i + ".channelid+\");return false;'>\"+  values").append(i).append("[0] +\"<\"+values").append(i).append("[1]+\" </a>\";\n");
    js.append("}else if(item").append(i).append(".btjc=='是'&&item").append(i).append(".titlered == '是'){\n");
    js.append("return \"<a name='gridTitle' href='#' style='color:#AC0A00;font-weight:bold;TEXT-DECORATION: none' onclick='gridTitleClick(\"+item").append(i).append(".id+\",\"+item").append(i).append(".channelid+\");return false;'>\"+  values").append(i).append("[0] +\"<\"+values").append(i).append("[1]+\" </a>\";\n");
    js.append("}else if(item").append(i).append(".btjc=='是'){\n");
    js.append("return \"<a name='gridTitle' style='font-weight:bold;TEXT-DECORATION: none ' href='#' onclick='gridTitleClick(\"+item").append(i).append(".id+\",\"+item").append(i).append(".channelid+\");return false;' >\"+ values").append(i).append("[0] +\"<\"+values").append(i).append("[1]+\"</a>\";\n");
    js.append("}else if(item").append(i).append(".titlered=='是'){\n");
    js.append("return \"<a name='gridTitle' style='color:#AC0A00;TEXT-DECORATION: none' href='#' onclick='gridTitleClick(\"+item").append(i).append(".id+\",\"+item").append(i).append(".channelid+\");return false;' >\"+ values").append(i).append("[0] +\"<\"+values").append(i).append("[1]+\"</a>\";\n");
    js.append("}else if(item").append(i).append(".isJq=='是'){\n ");
    js.append("return \"<a name='gridTitle' style='font-weight:bold;TEXT-DECORATION: none' href='javascript:' onclick='gridTitleClick(\"+item").append(i).append(".id+\",\"+item").append(i).append(".channelid+\");return false;' >\"+ values").append(i).append("[0] +\"<\"+values").append(i).append("[1]+\"</a>\";\n");
    js.append("}else{\n");
    js.append("return \"<a name='gridTitle'  href='#' style='TEXT-DECORATION: none' onclick='gridTitleClick(\"+item").append(i).append(".id+\",\"+item").append(i).append(".channelid+\");return false;' >\"+ values").append(i).append("[0] +\"<\"+values").append(i).append("[1]+\"</a>\";\n");
    js.append("}\n");
    js.append("}\n");
    js.append("function changeMoreTask").append(i).append("Button(count1){ \n");
    js.append("if(parseInt(count1)>").append(top).append("){\n");
    js.append("\tdocument.getElementById('moreTask").append(i).append("Entrance').style.display = '';\n");
    js.append("}else{\n");
    js.append("document.getElementById('moreTask").append(i).append("Entrance').style.display = 'none';\n");
    js.append("}\n");
    js.append("if(isMorePage=='1'){");
    js.append("document.getElementById('moreTask").append(i).append("Entrance').style.display = 'none';\n");
    js.append("}");
    js.append("}\n");
    return js.toString();
  }

  public String getListJsonData(int top, String channelIds, int start, String isDepartment, String isMore)
  {
    channelIds = channelIds + ",";
    String jsonstr = "";
    JSONObject json = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    int totalcount = 0;
    int newstart = (start - 1) * top;
    int end = start * top - 1;
    Hashtable channelList = CmChannelCache.getACNotCloseChannelList(super.getContext());

    int cc = 0;
    cc = DBSql.getInt("select count(*) as c from EIP_CM_CONTENT ", "c");
    Hashtable cmList = CmUtil.getCMSList(super.getContext(), channelList, cc);

    List filterList = new ArrayList();
    for (int i = 0; i < cmList.size(); i++) {
      CmContentModel contentModel = (CmContentModel)cmList.get(new Integer(i));
      if (!CmUtil.getContentSecurityList(getContext(), contentModel._bindId)) {
        continue;
      }
      if ((channelIds.indexOf(",") <= -1) || 
        (channelIds.indexOf(contentModel._channelId + ",") <= -1))
        continue;
      if (isDepartment.equals("1")) {
        if (contentModel._releaseDepartment.equals(getContext().getDepartmentModel().getDepartmentFullNameOfCache())) {
          totalcount++;
          if (isMore.equals("0")) {
            if (totalcount > top) {
              break;
            }
            Map map = getPortalJSONMap(contentModel, totalcount, start);
            jsonArray.add(map);
          }
          else if ((newstart < totalcount) && (totalcount - 1 <= end)) {
            Map map = getPortalJSONMap(contentModel, totalcount, start);
            jsonArray.add(map);
          }
        }
      }
      else
      {
        totalcount++;
        if (isMore.equals("0")) {
          if (totalcount > top)
          {
            break;
          }
          Map map = getPortalJSONMap(contentModel, totalcount, start);
          jsonArray.add(map);
        }
        else if ((newstart < totalcount) && (totalcount - 1 <= end)) {
          Map map = getPortalJSONMap(contentModel, totalcount, start);
          jsonArray.add(map);
        }

      }

    }

    if (totalcount == 0) {
      Map map = getPortalJSONMap(null, 0, start);
      jsonArray.add(map);
    }
    int pageSize = top;
    int lineCount = totalcount;
    int total = lineCount;
    int pageNow = start;
    int pageCount = lineCount % pageSize == 0 ? lineCount / pageSize : lineCount / pageSize + 1;
    int from = (pageNow - 1) * pageSize + 1;
    int to = end + 1;
    if (end + 1 > total) {
      to = total;
    }
    json.element("items", jsonArray);
    jsonstr = json.toString().replaceAll("<#total>", String.valueOf(totalcount)).replaceAll("<#pageCount>", String.valueOf(pageCount)).replaceAll("<#from>", String.valueOf(from)).replaceAll("<#to>", String.valueOf(to));
    return jsonstr;
  }

  private Map getPortalJSONMap(CmContentModel contentModel, int totalcount, int start)
  {
    HashMap map = new HashMap();
    if (totalcount == 0)
    {
      map.put("id", new Integer(0));
      map.put("title", "");
      map.put("date", I18nRes.findValue(super.getContext().getLanguage(), "无信息"));
      map.put("channelid", new Integer(0));
      map.put("name", "");
      map.put("titlered", "否");
      map.put("btjc", "否");
      map.put("isJq", "否");
      map.put("departmentName", "");
      map.put("count", new Integer(0));
      map.put("total", new Integer(0));
      map.put("pageNow", new Integer(0));
      map.put("pageCount", new Integer(0));
      map.put("from", new Integer(0));
      map.put("to", new Integer(0));
      map.put("line", "no");
    } else {
      String newsTitle = contentModel._displaytitle;
      if ((newsTitle == null) || (newsTitle.trim().length() == 0)) {
        newsTitle = "[空标题]";
      }
      String fulltitle = "";
      if (newsTitle.length() > 20) {
        fulltitle = newsTitle;
        newsTitle = newsTitle.substring(0, 20) + "...";
      } else {
        fulltitle = newsTitle;
      }

      String isJq = "否";
      Calendar calendar = Calendar.getInstance();
      calendar.add(5, -5);
      Timestamp releaseTime = null;
      String releaseDate = "";
      releaseTime = contentModel._releaseDate;
      if (releaseTime != null) {
        releaseDate = UtilDate.getAliasDate(releaseTime);
        if (calendar.getTime().getTime() < releaseTime.getTime()) {
          isJq = "是";
          newsTitle = newsTitle + "<img src=../aws_img/new1.gif border=0>";
        }
      }
      String dep = "";
      dep = contentModel._releaseDepartment;

      if (dep.indexOf("/") > -1) {
        dep = dep.substring(0, dep.length() - 1);
        if (dep.indexOf("/") > -1) {
          dep = dep.substring(dep.lastIndexOf("/") + 1, dep.length());
        }
      }
      if (dep.equals("")) {
        dep = I18nRes.findValue(super.getContext().getLanguage(), "无部门");
      }
      map.put("id", Integer.valueOf(contentModel._id));
      map.put("title", newsTitle);
      map.put("fulltitle", fulltitle);
      map.put("date", releaseDate);
      map.put("channelid", Integer.valueOf(contentModel._channelId));
      map.put("name", Function.getUserNameList(contentModel._createuser));
      map.put("titlered", contentModel._zyts);
      map.put("btjc", contentModel._btjc);
      map.put("isJq", isJq);
      map.put("departmentName", dep);
      map.put("count", "<#total>");

      map.put("total", "<#total>");
      map.put("pageNow", Integer.valueOf(start));
      map.put("pageCount", "<#pageCount>");
      map.put("from", "<#from>");
      map.put("to", "<#to>");
      map.put("line", "yes");
    }
    return map;
  }
}