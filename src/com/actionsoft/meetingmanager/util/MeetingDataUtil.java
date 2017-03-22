package com.actionsoft.meetingmanager.util;

import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.execute.PriorityType;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.meetingmanager.model.MeetingRoomConfigModel;
import com.actionsoft.meetingmanager.model.MeetingRoomInfoModel;
import com.actionsoft.meetingmanager.model.MeetingRoomOrderModel;
import com.actionsoft.meetingmanager.model.MeetingTypebean;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
/**
 * 
 * @description 修改了会议室预定成功后，鼠标移动上去后会显示一些信息，在其中增加了预订人、预定人部门、预定开始时间、预定结束时间。
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:46:49
 */
public class MeetingDataUtil
{
  public HashMap getDateSelect()
  {
    Calendar localCalendar = Calendar.getInstance();
    HashMap localHashMap = new HashMap();
    for (int i = 0; i < 15; i++) {
      localHashMap.put(new Integer(i), UtilDate.dateFormat(localCalendar.getTime()) + " " + UtilDate.getDayOfWeekSymbols(localCalendar.get(1), localCalendar.get(2) + 1, localCalendar.get(5)));
      localCalendar.add(5, 1);
    }
    return localHashMap;
  }

  public ArrayList getAssemblyName()
  {
    ArrayList localArrayList = new ArrayList();
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    try {
      localConnection = DBSql.open();
      localConnection.setAutoCommit(false);
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery("select MEETINGNO,MEETINGNAME from BO_MEETINGROOM");
      while (localResultSet.next()) {
        if ((localResultSet.getString(1) != null) && (localResultSet.getString(2) != null)) {
          MeetingTypebean localMeetingTypebean = new MeetingTypebean();
          localMeetingTypebean.setMeetingno(localResultSet.getString(1));
          localMeetingTypebean.setMeetingName(localResultSet.getString(2));
          localArrayList.add(localMeetingTypebean);
        }
      }
      localConnection.commit();
    } catch (Exception localException) {
      localException.printStackTrace(System.err);
      Object localObject1 = null;
      Object localObject3;
      return (ArrayList) localObject1;
    }
    finally
    {
      try
      {
        localConnection.rollback();
        localConnection.setAutoCommit(true);
      } catch (SQLException localSQLException3) {
        localSQLException3.printStackTrace(System.err);
        Object localObject6 = null;
        return (ArrayList) localObject6; } finally { DBSql.close(localConnection, localStatement, localResultSet);
      }
    }
    return localArrayList;
  }

  public MeetingRoomOrderModel getAssemblyState(UserContext paramUserContext, String paramString1, String paramString2, String paramString3, MeetingRoomConfigModel paramMeetingRoomConfigModel, MeetingRoomInfoModel paramMeetingRoomInfoModel)
  {
    MeetingRoomOrderModel localMeetingRoomOrderModel = new MeetingRoomOrderModel();
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    int i = 1;
    String str1 = UtilDate.datetimeFormat();
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String str2 = "select * from BO_MEETING_ROOM_ORDER where MEETINGROOMNO='" + paramString1 + "' and STATUS='预定' and " + DBSql.convertLongDate(paramString3) + ">=STARTDATE and " + DBSql.convertLongDate(paramString3) + "<ENDDATE ";
    try {
      Date localDate1 = localSimpleDateFormat.parse(paramString3 + ":00");

      Date localDate2;
	if ((paramMeetingRoomInfoModel.getIsLock() == 1) && (paramMeetingRoomInfoModel.getLockFromDate().trim().length() > 0) && (paramMeetingRoomInfoModel.getLockEndDate().trim().length() > 0) && (paramMeetingRoomInfoModel.getLockFromTime().trim().length() > 0) && (paramMeetingRoomInfoModel.getLockEndTime().trim().length() > 0)) {
        localDate2 = localSimpleDateFormat.parse(paramMeetingRoomInfoModel.getLockFromDate().substring(0, 10) + " " + paramMeetingRoomInfoModel.getLockFromTime() + ":00");
        Date localObject1 = localSimpleDateFormat.parse(paramMeetingRoomInfoModel.getLockEndDate().substring(0, 10) + " " + paramMeetingRoomInfoModel.getLockEndTime() + ":00");
        if ((localDate1.compareTo(localDate2) >= 0) && (localDate1.compareTo((Date)localObject1) <= 0)) {
          localMeetingRoomOrderModel.setStatus(5);
          localMeetingRoomOrderModel.setPopInfo(paramMeetingRoomInfoModel.getLockTip().trim().length() == 0 ? "<I18N#暂无提示>" : paramMeetingRoomInfoModel.getLockTip());
          MeetingRoomOrderModel localObject2 = localMeetingRoomOrderModel;
          return localObject2;
        }
      }
      localDate2 = localSimpleDateFormat.parse(str1);
      if (localDate2.compareTo(localDate1) > 0) {
        i = 4;
      }
      Object localObject1 = new Timestamp(localDate2.getTime());
      localConnection = DBSql.open();
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(str2);
      if (localResultSet.next()) {
        String localObject2 = localResultSet.getString("CREATEUSER");
        Timestamp localTimestamp1 = localResultSet.getTimestamp("STARTDATE");
        Timestamp localTimestamp2 = localResultSet.getTimestamp("ENDDATE");
        localMeetingRoomOrderModel.setBindid(localResultSet.getInt("BINDID"));
        if ((((Timestamp)localObject1).compareTo(localTimestamp1) > 0) && (localTimestamp2.compareTo((Timestamp)localObject1) > 0))
          i = 2;
        else if ((i != 4) && (localTimestamp1.compareTo((Timestamp)localObject1) > 0)) {
          i = 3;
        }
        localMeetingRoomOrderModel.setPopInfo(getPopInfo(paramUserContext, localResultSet, paramMeetingRoomConfigModel, paramMeetingRoomInfoModel, i, (String)localObject2));
      }
      localMeetingRoomOrderModel.setStatus(i);
      Object localObject2 = localMeetingRoomOrderModel;
      return (MeetingRoomOrderModel) localObject2;
    }
    catch (Exception localException)
    {
      localException.printStackTrace(System.err);
      MeetingRoomOrderModel localDate2 = null;
	return localDate2; } finally { DBSql.close(localConnection, localStatement, localResultSet); } 

  }

  private String getPopInfo(UserContext paramUserContext, ResultSet paramResultSet, MeetingRoomConfigModel paramMeetingRoomConfigModel, MeetingRoomInfoModel paramMeetingRoomInfoModel, int paramInt, String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    String str1 = "";
    String str2 = "";
    String str3 = "";
    String str4 = "";
    String str5 = "";
    int i = 0;
    String str6 = "";
    try {
      str1 = paramResultSet.getString("ORDERNAME");
      str2 = paramResultSet.getString("DEPARTMENTNAME");
      str3 = paramResultSet.getString("STARTDATE");
      str3 = str3.substring(0, str3.length());
      str4 = paramResultSet.getString("ENDDATE");
      str4 = str4.substring(0, str4.length());
      str5 = paramResultSet.getString("MEETINGTITLE") == null ? "" : paramResultSet.getString("MEETINGTITLE");
      i = paramResultSet.getInt("BINDID");
      str6 = paramResultSet.getString("CREATEUSER");
    } catch (Exception localException) {
      localException.printStackTrace(System.err);
      return null;
    }
    localStringBuffer.append("<table width='250px' border=0><tr>");
    localStringBuffer.append("<td  valign=top>");
    if (paramMeetingRoomConfigModel.getTipConfigName().toLowerCase().equals("on")) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#预定人>").append(":<b>").append(str1).append("</b></div></li>");
    }
    if (paramMeetingRoomConfigModel.getTipConfigDept().toLowerCase().equals("on")) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#预定部门>").append(":<b>").append(str2).append("</b></div></li>");
    }
    localStringBuffer.append("<li ><div class=actionsoftPopupTitle2>").append("<I18N#预定人>").append(":<b>").append(str1).append("</b></div></li>");
    localStringBuffer.append("<li ><div class=actionsoftPopupTitle2>").append("<I18N#预定部门>").append(":<b>").append(str2).append("</b></div></li>");
    localStringBuffer.append("<li ><div class=actionsoftPopupTitle2>").append("<I18N#会议开始时间>").append(":<b>").append(str3).append("</b></div></li>");
    localStringBuffer.append("<li ><div class=actionsoftPopupTitle2>").append("<I18N#会议结束时间>").append(":<b>").append(str4).append("</b></div></li>");
    if (paramMeetingRoomConfigModel.getTipConfigTitle().toLowerCase().equals("on")) {
      localStringBuffer.append("<li ><div class=actionsoftPopupTitle2>").append("<I18N#会议主题>").append(":<b>").append(str5).append("</b></div></li>");
    }
    if (paramMeetingRoomConfigModel.getTipConfigDate().toLowerCase().equals("on")) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#会议开始时间>").append(":<b>").append(UtilDate.getAliasDatetime(str3)).append("</b></div></li>");
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#会议结束时间>").append(":<b>").append(UtilDate.getAliasDatetime(str4)).append("</b></div></li>");
    }
    UserModel localUserModel;
    if (paramMeetingRoomConfigModel.getTipConfigTel().toLowerCase().equals("on")) {
      localUserModel = (UserModel)UserCache.getModel(str6);
      if ((localUserModel != null) && (!localUserModel.isDisabled())) {
        localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#预定人电话>").append(":<b>").append(localUserModel.getOfficeTel().trim().length() == 0 ? "无" : localUserModel.getOfficeTel()).append("</b></div></li>");
      }
    }
    if (paramMeetingRoomConfigModel.getTipConfigEmail().toLowerCase().equals("on")) {
      localUserModel = (UserModel)UserCache.getModel(str6);
      if ((localUserModel != null) && (!localUserModel.isDisabled())) {
        localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#预定人邮箱>").append(":<b>").append(localUserModel.getEmail().trim().length() == 0 ? "无" : localUserModel.getEmail()).append("</b></div></li>");
      }
    }
    localStringBuffer.append("</td></tr>");
    if (((paramInt == 2) || (paramInt == 3)) && (paramMeetingRoomInfoModel.getRoomManager() != null) && ((checkRoomManager(paramUserContext, paramMeetingRoomInfoModel.getRoomManager())) || (paramUserContext.getUID().equals(paramString)))) {
      localStringBuffer.append("<tr>");
      localStringBuffer.append("<td valign=top align=right width=100% >");
      localStringBuffer.append("<div class=actionsoftPopupTitle2>");
      if ((paramMeetingRoomConfigModel != null) && (paramMeetingRoomConfigModel.getApplymodify().toLowerCase().equals("on"))) {
        localStringBuffer.append("<input type=button bindid=" + i + "  onclick=modifyMeettingOrderEndTime(frmMain,this) title=<I18N#修改本次预约的会议室>  class=actionsoftButton value=<I18N#修改> />").append("&nbsp;&nbsp;&nbsp;&nbsp;");
      }
      if ((paramInt == 3) && (paramMeetingRoomConfigModel != null) && (paramMeetingRoomConfigModel.getApplycancle().toLowerCase().equals("on"))) {
        localStringBuffer.append("<input type=button bindid=" + i + "  cancelCmd=QuitMeetingOrderRoom  title=<I18N#取消本次预约的会议室> onclick=createCancelOrderMeetingRoomTime(frmMain,this) class=actionsoftButton value=<I18N#取消> />");
      }
      localStringBuffer.append("</div>");
      localStringBuffer.append("</td></tr>");
    }
    localStringBuffer.append("</table>");
    return localStringBuffer.toString();
  }

  public static String getMeetingRoomBasePopInfo(MeetingRoomInfoModel paramMeetingRoomInfoModel)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("<table border=0><tr>");
    localStringBuffer.append("<td valign=top>");
    if ((paramMeetingRoomInfoModel.getRoomType() != null) && (paramMeetingRoomInfoModel.getRoomType().trim().length() > 0)) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#会议室类型 >").append(":<b>").append(paramMeetingRoomInfoModel.getRoomType()).append("</b></div></li>");
    }
    if ((paramMeetingRoomInfoModel.getRoomSize() != null) && (paramMeetingRoomInfoModel.getRoomSize().trim().length() > 0)) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#容纳人数(个)>").append(":<b>").append(paramMeetingRoomInfoModel.getRoomSize()).append("</b></div></li>");
    }
    if ((paramMeetingRoomInfoModel.getDimensions() != null) && (paramMeetingRoomInfoModel.getDimensions().trim().length() > 0)) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#会议室规模>").append(":<b>").append(paramMeetingRoomInfoModel.getDimensions()).append("</b></div></li>");
    }
    if ((paramMeetingRoomInfoModel.getRoomConf() != null) && (paramMeetingRoomInfoModel.getRoomConf().trim().length() > 0)) {
      localStringBuffer.append("<li><div class=actionsoftPopupTitle2>").append("<I18N#主要配置介绍>").append(":<b>").append(paramMeetingRoomInfoModel.getRoomConf()).append("</b></div></li>");
    }
    localStringBuffer.append("</td></tr>");
    localStringBuffer.append("</table>");
    if (localStringBuffer.length() == 0) {
      return "<I18N#暂无介绍>";
    }
    return localStringBuffer.toString();
  }

  private Date strToDate(String paramString)
  {
    paramString = paramString + " 00:00:00";
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date localDate = null;
    try {
      localDate = localSimpleDateFormat.parse(paramString);
    } catch (Exception localException) {
      localException.printStackTrace(System.err);
    }
    return localDate;
  }

  public static boolean checkTheRoomIsLock(String paramString1, String paramString2)
  {
    Hashtable localHashtable = MeetingRoomInfoUtil.getInstance().getList(paramString2);
    if ((localHashtable != null) && (localHashtable.size() > 0)) {
      for (int i = 0; i < localHashtable.size(); i++) {
        MeetingRoomInfoModel localMeetingRoomInfoModel = (MeetingRoomInfoModel)localHashtable.get(new Integer(i));
        if (localMeetingRoomInfoModel != null) {
          SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

          if ((localMeetingRoomInfoModel.getIsLock() != 1) || (localMeetingRoomInfoModel.getLockFromDate().trim().length() <= 0) || (localMeetingRoomInfoModel.getLockEndDate().trim().length() <= 0) || (localMeetingRoomInfoModel.getLockFromTime().trim().length() <= 0) || (localMeetingRoomInfoModel.getLockEndTime().trim().length() <= 0)) continue;
          try {
            Date localDate1 = localSimpleDateFormat.parse(localMeetingRoomInfoModel.getLockFromDate().substring(0, 10) + " " + localMeetingRoomInfoModel.getLockFromTime() + ":00");
            Date localDate2 = localSimpleDateFormat.parse(localMeetingRoomInfoModel.getLockEndDate().substring(0, 10) + " " + localMeetingRoomInfoModel.getLockEndTime() + ":00");
            Date localDate3 = localSimpleDateFormat.parse(paramString1 + ":00");
            if ((localDate3.compareTo(localDate1) >= 0) && (localDate3.compareTo(localDate2) <= 0))
              return true;
          }
          catch (Exception localException) {
            localException.printStackTrace();
          }
        }
      }
    }

    return false;
  }

  public static String checkTheRoomIsUser(String paramString1, String paramString2, String paramString3, int paramInt)
  {
    String str1 = "select * from BO_MEETING_ROOM_ORDER where MEETINGROOMNO='" + paramString3 + "' and BINDID<>" + paramInt + " and status='预定'";
    Connection localConnection = null;
    Statement localStatement = null;
    ResultSet localResultSet = null;
    try {
      localConnection = DBSql.open();
      localStatement = localConnection.createStatement();
      localResultSet = localStatement.executeQuery(str1);
      SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date localDate1 = localSimpleDateFormat.parse(paramString1 + ":00");
      Date localDate2 = localSimpleDateFormat.parse(paramString2 + ":00");
      Timestamp localTimestamp = new Timestamp(localDate2.getTime());
      while (localResultSet.next()) {
        String str2 = localResultSet.getString("ENDDATE");
        String str3 = (localResultSet.getString("ENDTIME") == null) || ("".equals(localResultSet.getString("ENDTIME"))) ? "00:00" : localResultSet.getString("ENDTIME");
        String str4 = localResultSet.getString("STARTDATE");
        String str5 = localResultSet.getString("STARTTIME");

        String str6 = str4.substring(0, 10) + " " + str5 + ":00";
        String str7 = str2.substring(0, 10) + " " + str3 + ":00";

        Date localDate3 = localSimpleDateFormat.parse(str6);
        Date localDate4 = localSimpleDateFormat.parse(str7);
        String str8;
        if ((localDate2.compareTo(localDate3) > 0) && (localDate4.compareTo(localDate2) > 0)) {
          str8 = str6 + "到" + str7 + "已被占用,请重新选择预定的结束时间 ";
          return str8;
        }
        if ((localDate3.compareTo(localDate1) > 0) && (localDate2.compareTo(localDate4) > 0)) {
          str8 = str6 + "到" + str7 + "已被占用,请重新选择预定的结束时间 ";
          return str8;
        }
      }
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    } finally {
      DBSql.close(localConnection, localStatement, localResultSet);
    }
    return "";
  }

  public static void sendMeetingNotice(UserContext paramUserContext, int paramInt, String paramString)
  {
    Hashtable localHashtable1 = BOInstanceAPI.getInstance().getBOData("BO_MEETING_ROOM_ORDER", paramInt);
    String str1 = localHashtable1.get("ISSENDNOTICE").toString();
    if ((str1 != null) && (str1.trim().equals("是"))) {
      String str2 = "0089864df9a7ac833affa3d7dd46749b";
      String str3 = localHashtable1.get("YHRY").toString();
      UtilString localUtilString = new UtilString(str3);
      Vector localVector = localUtilString.split(" ");
      for (int i = 0; i < localVector.size(); i++) {
        String str4 = localVector.get(i).toString();
        if (str4.trim().length() == 0) {
          continue;
        }
        Hashtable localHashtable2 = new Hashtable();
        String str5 = localHashtable1.get("ENDTIME").toString();
        String str6 = localHashtable1.get("STARTDATE").toString();
        String str7 = localHashtable1.get("STARTTIME").toString();
        String str8 = localHashtable1.get("ENDDATE").toString();
        String str9 = localHashtable1.get("CREATEUSER").toString();
        str5 = str8 + " " + str5;
        str7 = str6 + " " + str7;
        localHashtable2.put("MEETINGNO", localHashtable1.get("MEETINGROOMNO") == null ? "" : localHashtable1.get("MEETINGROOMNO"));
        localHashtable2.put("MEETINGTIME", str7 + "到" + str5);
        localHashtable2.put("MEETINGADDRESS", localHashtable1.get("MEETINGNAME") == null ? "" : localHashtable1.get("MEETINGNAME"));
        localHashtable2.put("MEETINGAGENDA", localHashtable1.get("MEETINGAGENDA") == null ? "" : localHashtable1.get("MEETINGAGENDA"));
        localHashtable2.put("MEETINGPERSIONS", localHashtable1.get("YHRY") == null ? "" : localHashtable1.get("YHRY"));
        localHashtable2.put("MEETINGTITLE", localHashtable1.get("MEETINGTITLE") == null ? "" : localHashtable1.get("MEETINGTITLE"));
        localHashtable2.put("MEETINGMANAGEMENT", localHashtable1.get("ZCR") == null ? "" : localHashtable1.get("ZCR"));
        localHashtable2.put("QTRY", localHashtable1.get("QTRY") == null ? "" : localHashtable1.get("QTRY"));
        localHashtable2.put("JLY", localHashtable1.get("JLY") == null ? "" : localHashtable1.get("JLY"));
        try {
          int j = WorkflowInstanceAPI.getInstance().createProcessInstance(str2, str9, "会议通知流程");
          if (j > 0) {
            BOInstanceAPI.getInstance().createBOData("BO_MEETING_NOTICE", localHashtable2, j, paramUserContext.getUID());
            WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(str9, j, SynType.synchronous, PriorityType.normal, 1, str4, Function.getAddressName(str9) + "-" + paramString, false, 0);
          } else {
            MessageQueue.getInstance().putMessage(paramUserContext.getUID(), "发送给" + Function.getAddressName(str4) + "-" + paramString + "失败");
          }
        }
        catch (Exception localException)
        {
        }
      }
    }
  }

  public static void sendMeetingRecord(UserContext paramUserContext, int paramInt)
  {
    Hashtable localHashtable1 = BOInstanceAPI.getInstance().getBOData("BO_MEETING_ROOM_ORDER", paramInt);
    String str1 = "0089864df9a7ac833affa3d7dd46749b";
    String str2 = localHashtable1.get("JLY").toString();
    UtilString localUtilString = new UtilString(str2);
    Vector localVector = localUtilString.split(" ");
    for (int i = 0; i < localVector.size(); i++) {
      String str3 = localVector.get(i).toString();
      if (str3.trim().length() == 0) {
        continue;
      }
      Hashtable localHashtable2 = new Hashtable();
      String str4 = localHashtable1.get("ENDTIME").toString();
      String str5 = localHashtable1.get("STARTDATE").toString();
      String str6 = localHashtable1.get("STARTTIME").toString();
      String str7 = localHashtable1.get("ENDDATE").toString();
      String str8 = localHashtable1.get("CREATEUSER").toString();
      str4 = str7 + " " + str4;
      str6 = str5 + " " + str6;
      localHashtable2.put("MEETINGNO", localHashtable1.get("MEETINGROOMNO") == null ? "" : localHashtable1.get("MEETINGROOMNO"));
      localHashtable2.put("MEETINGTIME", str6 + "到" + str4);
      localHashtable2.put("MEETINGADDRESS", localHashtable1.get("MEETINGNAME") == null ? "" : localHashtable1.get("MEETINGNAME"));
      localHashtable2.put("MEETINGAGENDA", localHashtable1.get("MEETINGAGENDA") == null ? "" : localHashtable1.get("MEETINGAGENDA"));
      localHashtable2.put("YHRY", localHashtable1.get("YHRY") == null ? "" : localHashtable1.get("YHRY"));
      localHashtable2.put("MEETINGTITLE", localHashtable1.get("MEETINGTITLE") == null ? "" : localHashtable1.get("MEETINGTITLE"));
      localHashtable2.put("LEADER", localHashtable1.get("ZCR") == null ? "" : localHashtable1.get("ZCR"));
      localHashtable2.put("QTRY", localHashtable1.get("QTRY") == null ? "" : localHashtable1.get("QTRY"));
      localHashtable2.put("RECORDER", localHashtable1.get("JLY") == null ? "" : localHashtable1.get("JLY"));
      try {
        int j = WorkflowInstanceAPI.getInstance().createProcessInstance(str1, str8, "会议纪要流程");
        if (j > 0) {
          BOInstanceAPI.getInstance().createBOData("BO_MEETING_RECORD", localHashtable2, j, paramUserContext.getUID());
          WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(str8, j, SynType.synchronous, PriorityType.normal, 1, str3, Function.getAddressName(str8) + "-提醒您填写会议纪要", false, 0);
        } else {
          MessageQueue.getInstance().putMessage(paramUserContext.getUID(), "发送给" + Function.getAddressFullName(str3) + "的会议纪要失败");
        }
      } catch (Exception localException) {
        localException.printStackTrace();
      }
    }
  }

  private boolean checkRoomManager(UserContext paramUserContext, String paramString)
  {
    UtilString localUtilString = new UtilString(paramString);
    Vector localVector = localUtilString.split(" ");
    for (int i = 0; i < localVector.size(); i++) {
      String str = localVector.get(i).toString();
      if (str.trim().length() == 0) {
        continue;
      }
      if (Function.getUID(str).equals(paramUserContext.getUID())) {
        return true;
      }
    }
    return false;
  }
  public static String getEndTimeTagUI(String paramString1, String paramString2, String paramString3, String paramString4) {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("<select  name=ENDTIME id=ENDTIME class=actionsoftSelect>\n");
    localStringBuffer.append("<option value=''><I18N#请选择...></option>\n");
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      Date localDate1 = new Date();
      if (paramString1.trim().length() > 0 && paramString2.trim().length() > 0) {
	  localDate1 = localSimpleDateFormat.parse(paramString1 + " " + paramString2 + ":00");
      }
      MeetingRoomConfigModel localMeetingRoomConfigModel = MeetingRoomConfigUtil.getInstance().getConfig();
      if (localMeetingRoomConfigModel != null)
      {
        HashMap localHashMap = MeetingRoomConfigUtil.getInstance().getOrderTimeZoneList(localMeetingRoomConfigModel);
        if ((localHashMap != null) && (localHashMap.size() > 0)) {
          for (int i = 1; i <= localHashMap.size(); i++)
            if ((paramString3.trim().length() == 0) && (paramString1.trim().length() == 0)) {
              localStringBuffer.append("<option selected value='").append(localHashMap.get(String.valueOf(i))).append("'>").append(localHashMap.get(String.valueOf(i))).append("</option>");
            }
            else {
              Date localDate2 = localSimpleDateFormat.parse(paramString3 + " " + localHashMap.get(String.valueOf(i)) + ":00");
              if (localDate2.compareTo(localDate1) >= 0)
                if (paramString4.trim().length() > 0) {
                  Date localDate3 = localSimpleDateFormat.parse(paramString3 + " " + paramString4 + ":00");
                  if (localDate3.compareTo(localDate2) == 0)
                    localStringBuffer.append("<option selected value='").append(localHashMap.get(String.valueOf(i))).append("'>").append(localHashMap.get(String.valueOf(i))).append("</option>\n");
                  else
                    localStringBuffer.append("<option value='").append(localHashMap.get(String.valueOf(i))).append("'>").append(localHashMap.get(String.valueOf(i))).append("</option>\n");
                }
                else {
                  localStringBuffer.append("<option value='").append(localHashMap.get(String.valueOf(i))).append("'>").append(localHashMap.get(String.valueOf(i))).append("</option>\n");
                }
            }
        }
      }
    }
    catch (ParseException localParseException)
    {
      localParseException.printStackTrace();
    }
    localStringBuffer.append("</select><img src=../aws_img/notNull.gif alt='必须填写'>");
    return localStringBuffer.toString();
  }
}