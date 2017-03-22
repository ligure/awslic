package com.actionsoft.meetingmanager;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.execute.worklist.web.UserTaskFormsWeb;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.meetingmanager.biz.MeetingRoomOrderApplyProcess;
import com.actionsoft.meetingmanager.util.MeetingQuitOrderUtil;
import com.actionsoft.meetingmanager.web.MeetingRoomsOrderWeb;
import com.actionsoft.meetingmanager.web.MeetingRoomsWeb;
import com.letv.meeting.event.EmailThread;
import com.letv.meeting.web.MeetingOrderWeb;

public class MeetingManagerSocketCommand
  implements BaseSocketCommand
{
  public boolean executeCommand(UserContext paramUserContext, Socket paramSocket, OutputStreamWriter paramOutputStreamWriter, Vector paramVector, UtilString paramUtilString, String paramString)
    throws Exception
  {
    Object localObject1;
    String str1;
    String str2;
    if (paramString.equals("MeetingRoom_Order_List")) {
      localObject1 = new MeetingRoomsWeb(paramUserContext);
      str1 = paramVector.elementAt(3).toString();
      str2 = UtilCode.decode(paramUtilString.matchValue("_selectDate[", "]selectDate_"));
      paramOutputStreamWriter.write(((MeetingRoomsWeb)localObject1).getMeetingRoomsStatusList(str1, str2));
    }
    else
    {
      String str3;
      String str4;
      Object localObject2;
      if (paramString.equals("MeetingRoom_Time_Order")) {
        localObject1 = paramVector.elementAt(3).toString();
        str1 = paramVector.elementAt(4).toString();
        str2 = paramVector.elementAt(5).toString();
        str3 = paramVector.elementAt(6).toString();
        if (str3.equals("")) {
          str3 = "0";
        }
        str4 = paramVector.elementAt(7).toString();
        if (str4.equals("")) {
          str4 = "0";
        }
        localObject2 = new MeetingRoomOrderApplyProcess();

        int[] arrayOfInt = ((MeetingRoomOrderApplyProcess)localObject2).getId(paramUserContext.getSessionId(), (String)localObject1, str1, str2);
        if (arrayOfInt == null) {
          paramOutputStreamWriter.write(AlertWindow.getWarningWindow(I18nRes.findValue(paramUserContext.getLanguage(), "提示"), I18nRes.findValue(paramUserContext.getLanguage(), "您没有权限启动会议室预定流程,请与系统管理员联系!")));
        } else {
          UserTaskFormsWeb localUserTaskFormsWeb = new UserTaskFormsWeb(paramUserContext);
          paramOutputStreamWriter.write(localUserTaskFormsWeb.getFramesPage(arrayOfInt[0], Integer.parseInt(str3), arrayOfInt[1], Integer.parseInt(str4)));
          localUserTaskFormsWeb = null;
        }
      } else if (paramString.equals("MeetingRoom_Modify_OrderEndTime")) {
        localObject1 = new MeetingRoomsOrderWeb(paramUserContext);
        str1 = paramVector.elementAt(3).toString();
        if (str1.trim().length() == 0) {
          str1 = "0";
        }
        paramOutputStreamWriter.write(((MeetingRoomsOrderWeb)localObject1).getModifyMeettingOrderEndTimeWeb(Integer.parseInt(str1)));
        localObject1 = null;
      } else if (paramString.equals("MeetingRoom_Modify_OrderEndTime_Save")) {
        localObject1 = paramVector.elementAt(3).toString();
        str1 = paramVector.elementAt(4).toString();
        str2 = paramVector.elementAt(5).toString();
        str3 = paramVector.elementAt(6).toString();
        str4 = paramVector.elementAt(7).toString();
        if (((String)localObject1).trim().length() == 0) {
          localObject1 = "0";
        }
        paramOutputStreamWriter.write(MeetingQuitOrderUtil.updateMeetingOrderEndTime(paramUserContext, Integer.parseInt((String)localObject1), str3, str4, str1, str2));
      }
      else if (paramString.equals("Quit_MeetionRoom")) {
        localObject1 = new MeetingRoomsOrderWeb(paramUserContext);
        paramOutputStreamWriter.write(((MeetingRoomsOrderWeb)localObject1).getMeetingRoomsOrderList());
        localObject1 = null;
      } else if (paramString.equals("Open_MeetingRoom")) {
        localObject1 = new MeetingRoomsOrderWeb(paramUserContext);
        str1 = paramVector.elementAt(3).toString();
        paramOutputStreamWriter.write(((MeetingRoomsOrderWeb)localObject1).openQuitMeetingRooms(Integer.parseInt(str1)));
        localObject1 = null;
      } else if (paramString.equals("updateModifyMeetingOrderRoom")) {
        localObject1 = new MeetingRoomsOrderWeb(paramUserContext);
        str1 = paramVector.elementAt(3).toString();
        str2 = paramVector.elementAt(4).toString();
        str3 = paramVector.elementAt(5).toString();
        str4 = paramVector.elementAt(6).toString();
        localObject2 = paramVector.elementAt(7).toString();
        paramOutputStreamWriter.write(((MeetingRoomsOrderWeb)localObject1).updateModifyMeetingRooms(Integer.parseInt(str1), str2, str3, str4, (String)localObject2));
      } else if (paramString.equals("QuitMeetingOrderRoom")) {
        localObject1 = new MeetingRoomsOrderWeb(paramUserContext);
        str1 = paramVector.elementAt(3).toString();
        paramOutputStreamWriter.write(((MeetingRoomsOrderWeb)localObject1).quitMeetingRoom(Integer.parseInt(str1)));
      } else if (paramString.equals("QRCodeInfo")) {
	  MeetingOrderWeb web = new MeetingOrderWeb(paramUserContext);
	  str1 = paramVector.elementAt(3).toString();
	  paramOutputStreamWriter.write(web.getQRCodeInfoHtml(str1));
	  web = null;
      } else if (paramString.equals("ScanQRCode")) {
	  MeetingOrderWeb web = new MeetingOrderWeb(paramUserContext);
	  str1 = paramVector.elementAt(3).toString();
	  paramOutputStreamWriter.write(web.getAttendanceInfo(str1));
	  web = null;
      } else if (paramString.equals("ADD_MEETING_ATTENDANCE")) {
	  MeetingOrderWeb web = new MeetingOrderWeb(paramUserContext);
	  str1 = paramVector.elementAt(3).toString();
	  paramOutputStreamWriter.write(web.addAttendance(str1));
	  web = null;
      } else if (paramString.equals("MEETING_ATTENDANCE_REPORT")) {
	  MeetingOrderWeb web = new MeetingOrderWeb(paramUserContext);
	  str1 = paramVector.elementAt(3).toString();
	  str2 = UtilCode.decode(paramUtilString.matchValue("_diggerId[", "]diggerId_"));
	  paramOutputStreamWriter.write(web.getAttendanceReport(str1,str2));
	  web = null;
      } else if (paramString.equals("MEETING_ATTENDANCE_NEWRPT")) {
	  MeetingOrderWeb web = new MeetingOrderWeb(paramUserContext);
	  str1 = paramVector.elementAt(3).toString();
	  paramOutputStreamWriter.write(web.getAttNewReport(str1));
	  web = null;
      } else if (paramString.equals("SEND_MEETING_EMAIL")) {
	  str1 = paramVector.elementAt(3).toString();
	  str2 = paramVector.elementAt(4).toString();
	  str3 = UtilCode.decode(paramUtilString.matchValue("_mailTo[", "]mailTo_"));
	  int instanceId = 0;
	  int taskId = 0;
	  try {
	      instanceId = Integer.parseInt(str1);
	      taskId = Integer.parseInt(str2);
          } catch (Exception e) {
              e.printStackTrace();
          }
	  if (instanceId > 0 && str3.length() > 0) {
	      Thread t = new Thread(new EmailThread(paramUserContext.getUID(), str3, instanceId, taskId));
	      t.start();
	  }
      } else {
        return false;
      }
    }
    return true;
  }
}