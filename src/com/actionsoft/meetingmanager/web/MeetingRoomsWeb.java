package com.actionsoft.meetingmanager.web;

import com.actionsoft.awf.commons.security.ac.cache.AccessControlCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.meetingmanager.model.MeetingRoomConfigModel;
import com.actionsoft.meetingmanager.model.MeetingRoomInfoModel;
import com.actionsoft.meetingmanager.model.MeetingRoomOrderModel;
import com.actionsoft.meetingmanager.model.MeetingTypebean;
import com.actionsoft.meetingmanager.upgrade.MeetingOrderAutoUpgradeBizServerPack;
import com.actionsoft.meetingmanager.util.MeetingDataUtil;
import com.actionsoft.meetingmanager.util.MeetingRoomConfigUtil;
import com.actionsoft.meetingmanager.util.MeetingViewUtil;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level1.SecurityAPI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.sf.saxon.functions.Substring;
/**
 * 
 * @description 这里修改了会议室预定界面的内容，增加了三列：规模、人数、配置，其数据均出自会议室维护，并修改了相关的样式。
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:47:17
 */
public class MeetingRoomsWeb extends ActionsoftWeb
{
  UserContext uc = null;

  public MeetingRoomsWeb(UserContext paramUserContext) {
    this.uc = paramUserContext;
  }

  public String getMeetingRoomsStatusList(String paramString1, String paramString2)
  {
    MeetingOrderAutoUpgradeBizServerPack localMeetingOrderAutoUpgradeBizServerPack = new MeetingOrderAutoUpgradeBizServerPack();
    try {
      if (localMeetingOrderAutoUpgradeBizServerPack.isUpgrade())
        localMeetingOrderAutoUpgradeBizServerPack.run();
    }
    catch (AWSSDKException localAWSSDKException) {
      localAWSSDKException.printStackTrace();
    }
    MeetingDataUtil localMeetingDataUtil = new MeetingDataUtil();
    ArrayList localArrayList = localMeetingDataUtil.getAssemblyName();
    MeetingViewUtil localMeetingViewUtil = new MeetingViewUtil();
    String str1 = "";
    if (paramString2.trim().length() == 0)
      str1 = UtilDate.dateFormat(new Date());
    else {
      str1 = paramString2;
    }

    MeetingRoomConfigModel localMeetingRoomConfigModel = MeetingRoomConfigUtil.getInstance().getConfig();

    HashMap localHashMap = MeetingRoomConfigUtil.getInstance().getOrderTimeZoneList(localMeetingRoomConfigModel);

    Map localMap = localMeetingViewUtil.getAssemblyInfo(this.uc, str1, paramString1, localArrayList.size(), localHashMap, localMeetingRoomConfigModel);

    String str2 = getMeetingRoomsListHtml(localMap, str1, localMeetingRoomConfigModel);

    String str3 = getMeetingSelectMeetingRoomHtml(localArrayList, paramString1);

    String str4 = getMeetingSelectDateHtml(paramString2);

    String str5 = "";
    String str6 = "all";
    String str7 = "";
    String str8 = str1;
    Hashtable localHashtable = new Hashtable();
    localHashtable.put("meeting_select_assembly", str3);
    localHashtable.put("meeting_room_timezone_head", getMeetingRoomHeadHtml(localHashMap));
    localHashtable.put("meeting_select_date", str4);
    localHashtable.put("Select_Display_Script", str5);
    localHashtable.put("assemblyno", str6);
    localHashtable.put("metting_view", str2);
    localHashtable.put("assemblyno", str6);
    localHashtable.put("selectMeetingRoomNo", paramString1);
    localHashtable.put("selectMeetingRoomDate", str8);
    localHashtable.put("sid", this.uc.getSessionId());
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("Meeting_View_Time_List.htm"), localHashtable);
  }

  private String getMeetingRoomsListHtml(Map<Integer, Object> paramMap, String paramString, MeetingRoomConfigModel paramMeetingRoomConfigModel)
  {
    StringBuffer localStringBuffer1 = new StringBuffer();
    String str1 = this.uc.getLanguage();
    if (paramMap != null) {
      StringBuffer localStringBuffer2 = new StringBuffer();

      localStringBuffer2.append("\n<SCRIPT type=text/javascript>\n");
      localStringBuffer2.append("$(document).ready(function(){\n");

      for (int i = 0; i < paramMap.size(); i++) {
        MeetingRoomInfoModel localMeetingRoomInfoModel = (MeetingRoomInfoModel)paramMap.get(new Integer(i));
        if (localMeetingRoomInfoModel == null) {
          continue;
        }
        String metingname=localMeetingRoomInfoModel.getRoomName();
        localStringBuffer1.append("<tr>\n");
        localStringBuffer1.append("<td nowrap height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
        localStringBuffer1.append("<span class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(localMeetingRoomInfoModel.getRoomName()).append("></span>").append("</td>");
        //---------------------------修改开始-------------------------------------------------
        //规模
        String mentingguimo = DBSql.getString("select * from BO_MEETINGROOM where MEETINGNAME ='"+metingname+"'", "DIMENSIONS");
        if("".equals(mentingguimo)||mentingguimo==null){
        	metingname="";
        	localStringBuffer1.append("<td  height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
        	localStringBuffer1.append("<span class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append("").append("></span>").append("</td>");
        }
        else if(mentingguimo!=null){
            mentingguimo = DBSql.getString("select * from BO_MEETINGROOM where MEETINGNAME ='"+metingname+"'", "DIMENSIONS");
            localStringBuffer1.append("<td  height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
            localStringBuffer1.append("<span class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(mentingguimo).append("></span>").append("</td>");
        }
        //人数
        String mentingrenshu = DBSql.getString("select * from BO_MEETINGROOM where MEETINGNAME ='"+metingname+"'", "MEETINGSIZE");
        if("".equals(mentingrenshu)|| mentingrenshu == null){
        	mentingrenshu="";
        	localStringBuffer1.append("<td  height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
        	localStringBuffer1.append("<span class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(mentingrenshu).append("></span>").append("</td>");
        }
        else if(mentingrenshu!=null){
        	localStringBuffer1.append("<td  height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
        	localStringBuffer1.append("<span class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(mentingrenshu).append("></span>").append("</td>");
        }
         //配置
        String mentingpeizhi = DBSql.getString("select * from BO_MEETINGROOM where MEETINGNAME ='"+metingname+"'", "MEETINGCONF");
        if("".equals(mentingpeizhi)||mentingpeizhi==null){
        	mentingpeizhi="";
        	localStringBuffer1.append("<td  calss='td' height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
        	localStringBuffer1.append("<span class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(mentingpeizhi).append("></span>").append("</td>");
        }
        else if(mentingpeizhi!=null){
        	localStringBuffer1.append("<td  calss='td' height=\"25\" align=\"center\" bgcolor=\"fffbed\">");
      //  	localStringBuffer1.append("<span style='width: 700px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;' class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(mentingpeizhi).append("></span>").append("</td>");
            localStringBuffer1.append("<span style='Width: 100%;text-overflow: ellipsis;-o-text-overflow: ellipsis;white-space: nowrap;overflow:  hidden;' class='title_normal'  title=\"cssbody=[dvbdy] cssheader=[dvhdr] header=[<img border='0' src='../aws_img/home_nav.gif' align='absmiddle' >(<I18N#基本配置>)<I18N#").append(localMeetingRoomInfoModel.getRoomName()).append(">] body=[").append(MeetingDataUtil.getMeetingRoomBasePopInfo(localMeetingRoomInfoModel)).append("] delay=[300]\" ><I18N#").append(mentingpeizhi).append("></span>").append("</td>");

        	
        }
        //新增的字段结束
        /**
         * 此地方设置会议室预定的图片大小等内容，默认是35 25比例
         */
        HashMap localHashMap = localMeetingRoomInfoModel.getroomDate();
        for (int j = 1; j <= localHashMap.size(); j++) {
          String str2 = "";
          MeetingRoomOrderModel localMeetingRoomOrderModel = (MeetingRoomOrderModel)localHashMap.get(new Integer(j));
          int k = localMeetingRoomOrderModel.getStatus();
          if (k == 1) {
            str2 = "<input src=\"../aws_img/meeting/bk03.png\" type=\"image\"  onClick=\"createApplyMeetingRoomTime(frmMain," + j + ",'" + paramString + "','" + localMeetingRoomInfoModel.getRoomNo() + "'," + 1 + ",'MeetingRoom_Time_Order');return false;\"  align=\"middle\" width=\"100%\" height=\"100%\" border=\"0\"/>";
          } else if (k == 2)
          {
            str2 = "<img  id='img_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "' src=\"../aws_img/meeting/bk05.png\" align=\"middle\" width=\"100%\" height=\"100%\"  border=\"0\"  class=\"meeting-room\" ><DIV class=hidden> <DIV id='meeting_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "'>" + localMeetingRoomOrderModel.getPopInfo() + "</DIV></DIV> ";

            localStringBuffer2.append("$('#img_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "').mopTip({'w':200,'style':\"overClick\",'get':\"#meeting_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "\"});\n");
          }
          else if (k == 3) {
            str2 = "<img  id='img_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "' flag=3 src=\"../aws_img/meeting/bk04.png\" align=\"middle\" width=\"100%\" height=\"100%\"  border=\"0\"  class=\"meeting-room\" ><DIV class=hidden> <DIV id='meeting_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "'>" + localMeetingRoomOrderModel.getPopInfo() + "</DIV></DIV> ";

            localStringBuffer2.append("$('#img_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "').mopTip({'w':200,'style':\"overClick\",'get':\"#meeting_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "\"});\n");
          } else if (k == 4)
          {
            str2 = "<img src=\"../aws_img/meeting/bk06.png\" align=\"middle\" width=\"100%\" height=\"100%\"  border=\"0\" >";
          } else if (k == 5) {
            str2 = "<img  id='img_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "' flag=5 src=\"../aws_img/meeting/bk07.png\" align=\"middle\" width=\"100%\" height=\"100%\"  border=\"0\"  class=\"meeting-room\" ><DIV class=hidden> <DIV id='meeting_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "'>" + localMeetingRoomOrderModel.getPopInfo() + "</DIV></DIV> ";
            localStringBuffer2.append("$('#img_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "').mopTip({'w':200,'style':\"overClick\",'get':\"#meeting_" + localMeetingRoomInfoModel.getRoomNo() + "_" + j + "\"});\n");
          }
          else
          {
            str2 = I18nRes.findValue(str1, "<I18N#会议室异常>");
          }
          localStringBuffer1.append("<td width=\"35\" height=\"25\" align=\"center\" valign=\"top\"  >").append(str2).append("</td>\n");
        }
        localStringBuffer1.append("</tr>");
      }
      //------------------------------------修改结束----------------------------------------------------------------------------
      localStringBuffer2.append("});");
      localStringBuffer2.append("</SCRIPT>\n");
      localStringBuffer1.append(localStringBuffer2);
    } else {
      localStringBuffer1.append("<tr>").append("<td height=\"21\" align=\"middle\" bgcolor=\"fffbed\">").append(I18nRes.findValue(str1, "<I18N#没有会议室可以预定，请与管理员联系>")).append("</td>").append("</tr>");
    }

    return RepleaseKey.replaceI18NTag(this.uc.getLanguage(), localStringBuffer1.toString());
  }

  private String getMeetingRoomHeadHtml(HashMap paramHashMap)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 1; i <= paramHashMap.size(); i++) {
      String str = paramHashMap.get(String.valueOf(i)).toString();
      if (str.indexOf(":30") != -1) {
        continue;
      }
      localStringBuffer.append("<td height=\"21\" colspan=\"2\" align=\"center\" valign=\"middle\" nowrap background=\"aws_img/metting_destine.gif\"><span class=\"STYLE2\">").append(str).append("</span></td>\n");
    }
    return localStringBuffer.toString();
  }

  private String getMeetingSelectMeetingRoomHtml(ArrayList paramArrayList, String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("<select name=\"Select_MeetingRoom\" onChange=\"selectMeetingRoomDestine(frmMain,'MeetingRoom_Order_List')\" language=\"javascript\" id=\"Select_MeetingRoom\" style=\"font-family:新宋体;width:145px;\">");
    if (paramArrayList != null) {
      localStringBuffer.append("<option value=\"\">").append("<I18N#全部>").append("</option>");
      Iterator localIterator = paramArrayList.iterator();
      while (localIterator.hasNext()) {
        MeetingTypebean localMeetingTypebean = (MeetingTypebean)localIterator.next();
        Map localMap = AccessControlCache.getACList("MEETING_CATEGORY", localMeetingTypebean.getMeetingno());
        try
        {
          if ((localMap != null) && (localMap.size() > 0) && (!SecurityAPI.getInstance().acCheck(this.uc.getUID(), "MEETING_CATEGORY", localMeetingTypebean.getMeetingno(), "R")))
            continue;
        }
        catch (AWSSDKException localAWSSDKException) {
          localAWSSDKException.printStackTrace();
        }
        if (paramString.equals(localMeetingTypebean.getMeetingno()))
          localStringBuffer.append("<option selected value=" + localMeetingTypebean.getMeetingno() + "><I18N#").append(localMeetingTypebean.getMeetingName()).append("></option>");
        else
          localStringBuffer.append("<option value=" + localMeetingTypebean.getMeetingno() + "><I18N#").append(localMeetingTypebean.getMeetingName()).append("></option>");
      }
    }
    else
    {
      localStringBuffer.append("<option value=\"\">").append(I18nRes.findValue(this.uc.getLanguage(), "<I18N#会议室暂时无法使用>")).append("</option>");
    }
    localStringBuffer.append("</select>");
    return localStringBuffer.toString();
  }

  private String getMeetingSelectDateHtml(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("<select name=\"Select_Date\" onChange=\"selectMeetingRoomDestine(frmMain,'MeetingRoom_Order_List')\" language=\"javascript\" id=\"Select_Date\" style=\"font-family:新宋体;width:145px;\">");
    HashMap localHashMap = new MeetingDataUtil().getDateSelect();
    for (int i = 0; i < localHashMap.size(); i++) {
      String str2 = ((String)localHashMap.get(new Integer(i))).substring(0, 10);
      if (paramString.equals(str2))
        localStringBuffer.append("<option selected value=" + str2 + ">").append(((String)localHashMap.get(new Integer(i))).toString()).append("</option>");
      else {
        localStringBuffer.append("<option value=" + str2 + ">").append(((String)localHashMap.get(new Integer(i))).toString()).append("</option>");
      }
    }
    localStringBuffer.append("</select>");
    String str1 = this.uc.getLanguage();
    String str2 = localStringBuffer.toString().replaceAll("星期一", I18nRes.findValue(str1, "星期一")).replaceAll("星期二", I18nRes.findValue(str1, "星期二")).replaceAll("星期三", I18nRes.findValue(str1, "星期三")).replaceAll("星期四", I18nRes.findValue(str1, "星期四")).replaceAll("星期五", I18nRes.findValue(str1, "星期五")).replaceAll("星期六", I18nRes.findValue(str1, "星期六")).replaceAll("星期日", I18nRes.findValue(str1, "星期日"));

    return str2;
  }
}