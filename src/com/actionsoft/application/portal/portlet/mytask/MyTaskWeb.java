package com.actionsoft.application.portal.portlet.mytask;

import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.TaskWorklistAPI;
import java.util.Hashtable;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MyTaskWeb extends ActionsoftWeb
{
  public MyTaskWeb(UserContext uc)
  {
    super(uc);
  }

  public String redirectTaskPage(String topStr)
  {
    Hashtable hashTags = new Hashtable();
    String sidFlag = super.getSIDFlag();
    hashTags.put("sid", sidFlag);
    String sidStr = sidFlag.substring(sidFlag.lastIndexOf("=") + 1, sidFlag.indexOf(">"));
    hashTags.put("sidstr", super.getContext().getSessionId());
    hashTags.put("top", topStr);
    return getHtmlPage("portlet_MyTasks.htm", hashTags);
  }

  public String getTaskListData(String topStr)
  {
    int top = getContext().getUserModel().getLineNumber();
    if ((topStr != null) && (!topStr.equals(""))) {
      top = Integer.parseInt(topStr);
    }

    String taskList = null;
    int taskCount = 0;

    String historyTaskList = null;
    int historyTaskCount = 0;
    try {
      TaskWorklistAPI api = TaskWorklistAPI.getInstance();
      taskList = api.getTaskList(getContext().getUID(), 0, "", "", top);
      taskCount = api.getTaskCount(getContext().getUID(), 0, "", "");
      historyTaskList = api.getHistoryTaskList(getContext().getUID(), 0, "", "", top, "endtime", 0);
      historyTaskCount = api.getHistoryTaskCount(getContext().getUID(), 0, "", "");
    } catch (AWSSDKException e) {
      e.printStackTrace();
    }

    return constructTaskJson(taskList, taskCount, historyTaskList, historyTaskCount);
  }

  private String constructTaskJson(String taskList, int taskCount, String historyTaskList, int historyTaskCount)
  {
    JSONObject taskJson = JSONObject.fromObject(taskList);
    JSONArray taskArray = (JSONArray)taskJson.get("tasks");
    Iterator ite = taskArray.iterator();
    String locale = getContext().getLanguage();
    while (ite.hasNext()) {
      JSONObject obj = (JSONObject)ite.next();
      UserModel owner = (UserModel)UserCache.getModel(obj.get("owner").toString());
      if (owner != null) {
        obj.put("owner", owner.getUserName());
      }
      obj.put("beginTime", UtilDate.getAliasDatetime(obj.getString("beginTime"))
        .replaceAll("今天", I18nRes.findValue(locale, "今天"))
        .replaceAll("昨天", I18nRes.findValue(locale, "昨天"))
        .replaceAll("前天", I18nRes.findValue(locale, "前天"))
        .replaceAll("明天", I18nRes.findValue(locale, "明天"))
        .replaceAll("今年", I18nRes.findValue(locale, "今年"))
        .replaceAll("去年", I18nRes.findValue(locale, "去年"))
        .replaceAll("前年", I18nRes.findValue(locale, "前年"))
        .replaceAll("明年", I18nRes.findValue(locale, "明年"))
        .replaceAll("年", I18nRes.findValue(locale, "年")));
    }
    taskJson.put("count", String.valueOf(taskCount));

    JSONObject historyJson = JSONObject.fromObject(historyTaskList);
    JSONArray historyTaskArray = (JSONArray)historyJson.get("tasks");
    Iterator historyIte = historyTaskArray.iterator();
    while (historyIte.hasNext()) {
      JSONObject obj = (JSONObject)historyIte.next();
      UserModel owner = (UserModel)UserCache.getModel(obj.get("owner").toString());
      if (owner != null) {
        obj.put("owner", owner.getUserName());
      }
      obj.put("beginTime", UtilDate.getAliasDatetime(obj.getString("beginTime"))
        .replaceAll("今天", I18nRes.findValue(locale, "今天"))
        .replaceAll("昨天", I18nRes.findValue(locale, "昨天"))
        .replaceAll("前天", I18nRes.findValue(locale, "前天"))
        .replaceAll("明天", I18nRes.findValue(locale, "明天"))
        .replaceAll("今年", I18nRes.findValue(locale, "今年"))
        .replaceAll("去年", I18nRes.findValue(locale, "去年"))
        .replaceAll("前年", I18nRes.findValue(locale, "前年"))
        .replaceAll("明年", I18nRes.findValue(locale, "明年"))
        .replaceAll("年", I18nRes.findValue(locale, "年")));
    }
    historyJson.put("count", String.valueOf(historyTaskCount));

    JSONArray array = new JSONArray();
    array.add(taskJson);
    array.add(historyJson);
    return array.toString();
  }
}