package com.actionsoft.meetingmanager.biz;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.ValueAdapter;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.meetingmanager.util.MeetingDataUtil;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import java.util.HashMap;
import java.util.Hashtable;
/**
 * 
 * @description 会议室预定流程第一个节点，将会议室申请结束时间修改为24小时制。
 * @version 1.0
 * @author wangaz
 * @update 2014年7月14日 下午3:55:44
 */
public class MeetingOrderFormLoadBiz extends WorkFlowStepRTClassA
{
  public MeetingOrderFormLoadBiz(UserContext paramUserContext)
  {
    super(paramUserContext);
    super.setDescription("表单加载后,根据配置信息,动态替换'会议室使用结束时间标签[@ENDTIME]'");
    super.setProvider("actionsoft");
    super.setVersion("1.0");
  }

  public MeetingOrderFormLoadBiz() {
  }

  public boolean execute() {
    int i = getParameter("PARAMETER_WORKFLOW_INSTANCE_ID").toInt();
    Hashtable localHashtable1 = BOInstanceAPI.getInstance().getBOData("BO_MEETING_ROOM_ORDER", i);
    getClass(); Hashtable localHashtable2 = getParameter("PARAMETER_FORM_DATA").toHashtable();
    if ((localHashtable1 != null) && (localHashtable1.size() > 0)) {
      String str1 = localHashtable1.get("STARTDATE").toString();
      String str2 = localHashtable1.get("STARTTIME").toString();
      String str3 = localHashtable1.get("ENDDATE").toString();
      String str4 = localHashtable1.get("ENDTIME").toString();
      localHashtable2.put("ENDTIME", MeetingDataUtil.getEndTimeTagUI(str1, str2, str3, str4));
    } else {
      localHashtable2.put("ENDTIME", MeetingDataUtil.getEndTimeTagUI("", "", "", ""));
    }
    return true;
  }
  private HashMap getTimeZone() {
    HashMap localHashMap = new HashMap();    
    localHashMap.put("1", "08:00");
    localHashMap.put("2", "08:30");
    localHashMap.put("3", "09:00");
    localHashMap.put("4", "09:30");
    localHashMap.put("5", "10:00");
    localHashMap.put("6", "10:30");
    localHashMap.put("7", "11:00");
    localHashMap.put("8", "11:30");
    localHashMap.put("9", "12:00");
    localHashMap.put("10", "12:30");
    localHashMap.put("11", "13:00");
    localHashMap.put("12", "13:30");
    localHashMap.put("13", "14:00");
    localHashMap.put("14", "14:30");
    localHashMap.put("15", "15:00");
    localHashMap.put("16", "15:30");
    localHashMap.put("17", "16:00");
    localHashMap.put("18", "16:30");
    localHashMap.put("19", "17:00");
    localHashMap.put("20", "17:30");
    localHashMap.put("21", "18:00");
    localHashMap.put("22", "18:30");
    
    localHashMap.put("23", "19:00");
    localHashMap.put("24", "19:30");
    localHashMap.put("25", "20:00");
    localHashMap.put("26", "20:30");
    localHashMap.put("27", "21:00");
    localHashMap.put("28", "21:30");
    localHashMap.put("29", "22:00");
    localHashMap.put("30", "22:30");
    localHashMap.put("31", "23:00");
    localHashMap.put("32", "23:30");
    localHashMap.put("33", "00:00");
    return localHashMap;
  }
}