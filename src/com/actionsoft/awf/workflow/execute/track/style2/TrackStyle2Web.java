package com.actionsoft.awf.workflow.execute.track.style2;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.AWFServerConf;
import com.actionsoft.awf.database.DBPoolsManager;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.RoleCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.cache.UserMapCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.organization.model.UserMapModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.util.UtilNumber;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowSubCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowSubModel;
import com.actionsoft.awf.workflow.design.util.WFFlexDesignVersionUtil;
import com.actionsoft.awf.workflow.design.web.WFDesignDocumentWeb;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.awf.workflow.execute.dao.ProcessInstance;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.dao.SubProcessInstance;
import com.actionsoft.awf.workflow.execute.dao.TaskInstance;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.SubProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.eip.document.enterprise.dao.EnterpriseDocDaoFactory;
import com.actionsoft.eip.document.enterprise.dao.EnterpriseDocGroup;
import com.actionsoft.eip.document.enterprise.model.EnterpriseDocGroupModel;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class TrackStyle2Web extends ActionsoftWeb
{
  public TrackStyle2Web(UserContext userContext)
  {
    super(userContext);
  }

  public TrackStyle2Web()
  {
  }

  public String getWorflowTrack(int instanceId)
  {
    ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory.createProcessInstance().getInstance(instanceId);
    if (processInstanceModel == null) {
      return "<font color='red'><I18N#流程实例不存在或已删除></font>";
    }
    WorkFlowModel workFlowModel = (WorkFlowModel)WorkFlowCache.getModel(processInstanceModel.getProcessDefinitionId());
    Connection conn = DBSql.open();
    Statement stmt = null;
    ResultSet rset = null;

    StringBuilder process = new StringBuilder();
    if (!processInstanceModel.isEnd()) {
      double stepCount = WorkFlowStepCache.getListOfWorkFlow(processInstanceModel.getProcessDefinitionId()).size();
      double stepNow = processInstanceModel.getActivityDefinitionNo();
      if (stepCount > 0.0D) {
        stepNow = stepNow == 0.0D ? 1.0D : stepNow;
        double finashRate = 0.0D;
        stepCount += 1.0D;
        try {
          finashRate = UtilNumber.fixPoint(stepNow / stepCount, 4);
        } catch (Exception localException1) {
        }
        process.append("<table  border=0 cellspacing=0 cellpadding=0><tr><td height=16 width=").append(finashRate * 100.0D).append(" background=../aws_img/process_2.gif></td><td width=").append((1.0D - finashRate) * 100.0D).append(" background=../aws_img/process_1.gif></td><td><I18N#约为>").append(UtilNumber.fixPoint(finashRate * 100.0D, 2)).append("%</td></tr></table>");
      }

    }

    String authorInfo = "";
    UserModel authorModel = (UserModel)UserCache.getModel(processInstanceModel.getCreateUser());
    if (authorModel != null) {
      authorInfo = authorModel.getUserName();
    }

    StringBuilder trackJS = new StringBuilder();
    trackJS.append("var pools=new Array();\n");
    trackJS.append("var tasks=new Array();\n");
    trackJS.append("var sequenceFlows=new Array();\n\n");

    Hashtable pools = new UnsyncHashtable();
    Hashtable tasks = new UnsyncHashtable();

    int firstStepNo = 0;

    Map stepList = WorkFlowStepCache.getListOfWorkFlow(workFlowModel._id);

    Hashtable taskLog = ProcessRuntimeDaoFactory.createTaskInstance().getEndTaskListByProcessInstance(instanceId);
    if (taskLog != null) {
      for (int i = 0; i < taskLog.size(); i++) {
        TaskInstanceModel taskInstanceModel = (TaskInstanceModel)taskLog.get(new Integer(i));
        if ((taskInstanceModel.getStatus() != 1) && (taskInstanceModel.getStatus() != 11))
          continue;
        for (int ii = 0; ii < stepList.size(); ii++) {
          WorkFlowStepModel stepModel = (WorkFlowStepModel)stepList.get(new Integer(ii));
          if (stepModel._id == taskInstanceModel.getActivityDefinitionId()) {
            tasks.put(new Integer(tasks.size()), taskInstanceModel);
            pools.put(new Integer(stepModel._id), stepModel);
            if (firstStepNo != 0) break;
            firstStepNo = stepModel._stepNo;

            break;
          }
        }
      }

    }

    Hashtable taskWork = ProcessRuntimeDaoFactory.createTaskInstance().getActiveTaskListByProcessInstance(instanceId);
    TaskInstanceModel lastWorkTaskModel = null;
    if (taskWork != null) {
      for (int i = 0; i < taskWork.size(); i++) {
        TaskInstanceModel taskInstanceModel = (TaskInstanceModel)taskWork.get(new Integer(i));
        for (int ii = 0; ii < stepList.size(); ii++) {
          WorkFlowStepModel stepModel = (WorkFlowStepModel)stepList.get(new Integer(ii));
          if ((stepModel._id == taskInstanceModel.getActivityDefinitionId()) && ((taskInstanceModel.getStatus() == 1) || (taskInstanceModel.getStatus() == 11) || (taskInstanceModel.getStatus() == 4))) {
            tasks.put(new Integer(tasks.size()), taskInstanceModel);
            pools.put(new Integer(stepModel._id), stepModel);
            lastWorkTaskModel = taskInstanceModel;
            if (firstStepNo != 0) break;
            firstStepNo = stepModel._stepNo;

            break;
          }
        }
      }
    }

    trackJS.append("var firstStepNo=" + firstStepNo + ";\n\n");

    if (lastWorkTaskModel != null) {
      String sql = "select *  from wf_messagepoint  where parent_id=" + instanceId + " and state=0 and wfs_id=" + lastWorkTaskModel.getActivityDefinitionId() + " order by id";
      try {
        stmt = conn.createStatement();
        rset = DBSql.executeQuery(conn, stmt, sql);
        while (rset.next()) {
          TaskInstanceModel taskInstanceModel = new TaskInstanceModel();
          taskInstanceModel.setOwner(rset.getString("OWNER"));
          taskInstanceModel.setTarget(rset.getString("TARGET"));
          taskInstanceModel.setActivityDefinitionId(rset.getInt("WFS_ID"));
          taskInstanceModel.setProcessDefinitionId(rset.getInt("WF_ID"));
          taskInstanceModel.setProcessInstanceId(rset.getInt("PARENT_ID"));
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
        }
      } catch (Exception e) {
        e.printStackTrace(System.err);
        try
        {
          if (stmt != null) {
            stmt.close();
          }
          if (rset != null)
            rset.close();
        }
        catch (Exception localException2)
        {
        }
      }
      finally
      {
        try
        {
          if (stmt != null) {
            stmt.close();
          }
          if (rset != null) {
            rset.close();
          }
        }
        catch (Exception localException3)
        {
        }
      }
    }
    Hashtable subProcessList = ProcessRuntimeDaoFactory.createSubProcessInstance().getInstanceByParentProcessInstanceId(instanceId);

    int poolIndex = 0;
    String poolColor = "FLOW_POOL_FILL_COLOR1";
    HashMap step2pool = new HashMap();
    for (int i = 0; i < stepList.size(); i++) {
      WorkFlowStepModel stepModel = (WorkFlowStepModel)stepList.get(new Integer(i));
      if (stepModel._id <= 0)
        continue;
      if (i == 0) {
        trackJS.append("pools[").append(poolIndex).append("]=new AWSFlowPoolY(").append(poolIndex).append("*FLOW_POOL_WIDTH,0,\"<I18N#开始>\",").append(poolColor).append(");\n");
        step2pool.put(new Integer(100000001), new Integer(poolIndex));
        poolIndex++;
        if (poolColor.equals("FLOW_POOL_FILL_COLOR1"))
          poolColor = "FLOW_POOL_FILL_COLOR2";
        else {
          poolColor = "FLOW_POOL_FILL_COLOR1";
        }
      }

      if (((workFlowModel._trackDiagramType == 1) && (pools.get(new Integer(stepModel._id)) != null)) || (workFlowModel._trackDiagramType == 0))
      {
        trackJS.append("pools[").append(poolIndex).append("]=new AWSFlowPoolY(").append(poolIndex).append("*FLOW_POOL_WIDTH,0,\"").append("<I18N#" + stepModel._stepName + ">").append("\",").append(poolColor).append(");\n");
        step2pool.put(new Integer(stepModel._id), new Integer(poolIndex));
        poolIndex++;
        if (poolColor.equals("FLOW_POOL_FILL_COLOR1"))
          poolColor = "FLOW_POOL_FILL_COLOR2";
        else {
          poolColor = "FLOW_POOL_FILL_COLOR1";
        }
      }

      if (WorkFlowSubCache.getBeginListOfWorkflowStep(stepModel._uuid).size() > 0) {
        trackJS.append("pools[").append(poolIndex).append("]=new AWSFlowPoolY(").append(poolIndex).append("*FLOW_POOL_WIDTH,0,\"[" + I18nRes.findValue(super.getContext().getLanguage(), "子流程") + "]\",").append(poolColor).append(");\n");
        step2pool.put(new Integer(stepModel._id + 1000000), new Integer(poolIndex));
        poolIndex++;
        if (poolColor.equals("FLOW_POOL_FILL_COLOR1"))
          poolColor = "FLOW_POOL_FILL_COLOR2";
        else {
          poolColor = "FLOW_POOL_FILL_COLOR1";
        }
      }

      if (i == stepList.size() - 1) {
        trackJS.append("pools[").append(poolIndex).append("]=new AWSFlowPoolY(").append(poolIndex).append("*FLOW_POOL_WIDTH,0,\"<I18N#结束>\",").append(poolColor).append(");\n");
        step2pool.put(new Integer(100000000), new Integer(poolIndex));
        poolIndex++;
        if (poolColor.equals("FLOW_POOL_FILL_COLOR1"))
          poolColor = "FLOW_POOL_FILL_COLOR2";
        else {
          poolColor = "FLOW_POOL_FILL_COLOR1";
        }
      }
    }

    int taskIndex = 0;
    int prePoolIndex = -1;
    int sequenceIndex = 0;
    for (int i = 0; i < tasks.size(); i++) {
      TaskInstanceModel taskInstanceModel = (TaskInstanceModel)tasks.get(new Integer(i));

      Integer ind = (Integer)step2pool.get(new Integer(taskInstanceModel.getActivityDefinitionId()));
      if (ind == null)
        continue;
      int currentPoolInd = ind.intValue();

      WorkFlowStepModel stepModel = getStepModel(stepList, taskInstanceModel.getActivityDefinitionId());

      String taskState = "FINISH";
      if (taskInstanceModel.getEndTime() == null)
        taskState = "WORK";
      if ((taskInstanceModel.getEndTime() == null) && (taskInstanceModel.getBeginTime() == null)) {
        taskState = "WAIT";
      }
      String taskTitle = taskInstanceModel.getTarget();
      String taskRole = "";
      String info1 = ""; String info2 = ""; String hotTitle = ""; String kmInfo = "";
      try {
        UserContext target = new UserContext(taskInstanceModel.getTarget());
        taskTitle = "<I18N#" + target.getUserModel().getUserName() + ">";

        taskRole = "(<I18N#" + target.getRoleModel().getRoleName() + ">)";

        int deptid = taskInstanceModel.getTargetDepartmentId();

        Map list = UserMapCache.getMapListOfUser(taskInstanceModel.getTarget());
        for (int j = 0; j < list.size(); j++) {
          UserMapModel umm = (UserMapModel)list.get(Integer.valueOf(j));
          if (umm.getDepartmentId() == deptid) {
            RoleModel rm = (RoleModel)RoleCache.getModel(umm.getRoleId());
            if (rm == null) break;
            taskRole = "(<I18N#" + rm.getRoleName() + ">)";

            break;
          }
        }
      }
      catch (Exception localException5)
      {
      }
      if (taskInstanceModel.getBeginTime() != null)
        hotTitle = "<I18N#到达时间>:" + UtilDate.datetimeFormat(taskInstanceModel.getBeginTime());
      boolean isRead = true;
      if (taskInstanceModel.getEndTime() != null) {
        hotTitle = hotTitle + "\\n<I18N#办理时间>:" + UtilDate.datetimeFormat(taskInstanceModel.getEndTime());

        if (workFlowModel._isTrackForm) {
          info1 = "<a href='' onClick='openForm(frmMain," + instanceId + "," + taskInstanceModel.getActivityDefinitionId() + "," + taskInstanceModel.getId() + ");return false;'><I18N#查看数据></a>";
        }

        if (stepModel._kmDirectoryId > 0) {
          EnterpriseDocGroupModel docGroupModel = EnterpriseDocDaoFactory.createDocGroup().getInstance(stepModel._kmDirectoryId);
          if (docGroupModel != null)
            kmInfo = "<I18N#入库到>：" + docGroupModel._root_name + "/" + docGroupModel._groupName;
        }
      }
      else if ((taskInstanceModel.getEndTime() == null) && (taskInstanceModel.getBeginTime() != null)) {
        hotTitle = hotTitle + "\\n<I18N#正在办理>...";
        if (taskInstanceModel.getReadTask() == 0) {
          hotTitle = hotTitle + " (<I18N#未读>)";
          isRead = false;
        }
        info1 = "<img src=../aws_img/waite.process.gif >";
      } else {
        hotTitle = hotTitle + "\\n<I18N#等待上一任务结束>";
        info1 = "<img src=../aws_img/wait.gif width=20>";
        isRead = false;
      }

      if (taskInstanceModel.getStatus() == 11) { String threadName = Thread.currentThread().getName();
        String lang;
        try { lang = threadName.split("--")[3];
        }
        catch (Exception e)
        {
          lang = "cn";
        }
        if (taskInstanceModel.getTitle().trim().length() > 0) {
          if (taskInstanceModel.getTitle().indexOf("(阅办)") == 0)
            info1 = "<font color=red>(" + I18nRes.findValue(lang, "aws_platform_阅办") + ")</font>";
          else if (taskInstanceModel.getTitle().indexOf("(会签)") == 0)
            info1 = "<font color=red>(" + I18nRes.findValue(lang, "会签") + ")</font>";
          else if (taskInstanceModel.getTitle().indexOf("(协同)") == 0)
            info1 = "<font color=red>(" + I18nRes.findValue(lang, "协同") + ")</font>";
          else
            info1 = "<font color=red>(" + I18nRes.findValue(lang, "加签") + ")</font>";
        }
        else {
          info1 = "<font color=red>(" + I18nRes.findValue(lang, "加签") + ")</font>";
        }
      }

      boolean isTimeout = false;
      try
      {
        if ((taskState.equals("FINISH")) && (DBSql.getInt(conn, "select count(*) as c from BO_AWS_RT_COSTLOG where TASKID=" + taskInstanceModel.getId(), "c") > 0))
          isTimeout = true;
      }
      catch (SQLException e) {
        e.printStackTrace(System.err);
      }

      String photoDir = AWFConfig._awfServerConf.getDocumentPath() + "Photo/group" + taskInstanceModel.getTarget() + "/file0/" + taskInstanceModel.getTarget() + ".jpg";
      File photoFile = new File(photoDir);
      String photoURL = "";
      if (photoFile.exists())
        photoURL = "./downfile.wf?flag1=" + taskInstanceModel.getTarget() + "&flag2=0&sid=" + getContext().getSessionId() + "&rootDir=Photo&filename=" + taskInstanceModel.getTarget() + ".jpg";
      else {
        photoURL = "../aws_img/userPhoto.png";
      }
      trackJS.append("tasks[").append(taskIndex).append("]=new AWSTask(").append(currentPoolInd).append(",").append(taskIndex).append(",'" + taskTitle + "','" + taskRole + "',\"" + info1 + "\",\"" + info2 + "\",\"" + kmInfo + "\",'" + hotTitle + "',new AWSTaskType(new AWSTaskType().").append(taskState).append("),").append(isTimeout).append(",").append(isRead).append(",'").append(photoURL).append("');\n");
      if (prePoolIndex > -1)
      {
        if (currentPoolInd > prePoolIndex)
          trackJS.append("sequenceFlows[").append(sequenceIndex).append("]=new AWSSequenceFlow(").append(prePoolIndex + 1).append("*FLOW_POOL_WIDTH-23,").append(taskIndex - 1).append("*FLOW_POOL_HEIGHT+50,").append(currentPoolInd).append("*FLOW_POOL_WIDTH+38,").append(sequenceIndex + 1).append("*FLOW_POOL_HEIGHT+50,").append(sequenceIndex + 1).append(");\n");
        else if (currentPoolInd < prePoolIndex)
          trackJS.append("sequenceFlows[").append(sequenceIndex).append("]=new AWSSequenceFlow(").append(prePoolIndex + 1).append("*FLOW_POOL_WIDTH-23,").append(taskIndex - 1).append("*FLOW_POOL_HEIGHT+50,").append(currentPoolInd).append("*FLOW_POOL_WIDTH+38,").append(taskIndex).append("*FLOW_POOL_HEIGHT,").append(sequenceIndex + 1).append(");\n");
        else if (currentPoolInd == prePoolIndex) {
          trackJS.append("sequenceFlows[").append(sequenceIndex).append("]=new AWSSequenceFlow(").append(prePoolIndex + 1).append("*FLOW_POOL_WIDTH-FLOW_POOL_WIDTH/2,").append(taskIndex).append("*FLOW_POOL_HEIGHT-23,0,0,").append(sequenceIndex + 1).append(",'" + stepModel._routePointType + "');\n");
        }
        sequenceIndex++;
      }

      prePoolIndex = ind.intValue();
      taskIndex++;

      for (int subP = 0; subP < subProcessList.size(); subP++) {
        SubProcessInstanceModel subProcessModel = (SubProcessInstanceModel)subProcessList.get(new Integer(subP));
        if (subProcessModel.getParentTaskInstanceId() == taskInstanceModel.getId()) {
          WorkFlowSubModel profileModel = (WorkFlowSubModel)WorkFlowSubCache.getModel(subProcessModel.getSubProcessProfileId());
          Integer subProcessPoolIND = (Integer)step2pool.get(new Integer(taskInstanceModel.getActivityDefinitionId() + 1000000));
          currentPoolInd = subProcessPoolIND.intValue();
          int isJoin = 0;
          String synTitle = "<I18N#该异步子流程指定了汇聚节点>";
          if (profileModel == null) {
            taskTitle = "<I18N#子流程>";
          } else {
            if (profileModel._synType == SynType.asynchronous.getValue()) {
              taskTitle = "[" + I18nRes.findValue(getContext().getLanguage(), "异步") + "]" + I18nRes.findValue(getContext().getLanguage(), "子流程");
            } else {
              taskTitle = "[" + I18nRes.findValue(getContext().getLanguage(), "同步") + "]" + I18nRes.findValue(getContext().getLanguage(), "子流程");
              isJoin = -1;
            }
            if (profileModel._endStepUUID.length() > 0) {
              isJoin = 1;
              WorkFlowStepModel joinStepModel = (WorkFlowStepModel)WorkFlowStepCache.getUUIDModel(profileModel._endStepUUID);
              if (joinStepModel != null) {
                synTitle = "<I18N#该异步子流程指定了汇聚节点>[" + joinStepModel._stepName + "]";
              }
            }
          }
          int taskInstId = subProcessModel.getSubTaskInstanceId();
          TaskInstanceModel tm = ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfActive(taskInstId);
          if (tm == null) {
            tm = ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfEnd(taskInstId);
          }
          taskTitle = taskTitle + "(" + ((UserModel)UserCache.getModel(tm.getTarget())).getUserName() + ")";
          if (subProcessModel.getStatus() == 1) {
            taskState = "FINISH";
            hotTitle = "<<I18N#已结束>>";
          } else {
            taskState = "WORK";
            hotTitle = "<<I18N#正在执行中>>";
          }
          ProcessInstanceModel subProcessInstance = ProcessRuntimeDaoFactory.createProcessInstance().getInstance(subProcessModel.getSubProcessInstanceId());
          if (subProcessInstance != null) {
            hotTitle = hotTitle + subProcessInstance.getTitle();
            hotTitle = hotTitle + "\\n" + I18nRes.findValue(getContext().getLanguage(), "启动日期") + "：" + UtilDate.datetimeFormat24(subProcessInstance.getCreateDate());
            taskTitle = "<a href=\"\" onclick=\"openSubProcessTrack(frmMain," + subProcessModel.getSubProcessInstanceId() + ");return false;\">" + taskTitle + "</a>";
          }

          String subProcessServicesType = "inner";
          if (profileModel._servicesLocation.indexOf("system:") == 0)
            subProcessServicesType = "system";
          else if (profileModel._servicesLocation.indexOf("remote:") == 0) {
            subProcessServicesType = "remote";
          }
          trackJS.append("tasks[").append(taskIndex).append("]=new AWSSubProcessTask(").append(subProcessPoolIND.intValue()).append(",").append(taskIndex).append(",'" + taskTitle + "','" + hotTitle + "',new AWSTaskType(new AWSTaskType().").append(taskState).append(")," + isJoin + ",'" + synTitle + "','" + subProcessServicesType + "');\n");
          if (prePoolIndex > -1)
          {
            if (currentPoolInd > prePoolIndex)
              trackJS.append("sequenceFlows[").append(sequenceIndex).append("]=new AWSSequenceFlow(").append(prePoolIndex + 1).append("*FLOW_POOL_WIDTH-23,").append(taskIndex - 1).append("*FLOW_POOL_HEIGHT+50,").append(currentPoolInd).append("*FLOW_POOL_WIDTH+38,").append(sequenceIndex + 1).append("*FLOW_POOL_HEIGHT+50,").append(sequenceIndex + 1).append(");\n");
            else if (currentPoolInd < prePoolIndex)
              trackJS.append("sequenceFlows[").append(sequenceIndex).append("]=new AWSSequenceFlow(").append(prePoolIndex + 1).append("*FLOW_POOL_WIDTH-23,").append(taskIndex - 1).append("*FLOW_POOL_HEIGHT+50,").append(currentPoolInd).append("*FLOW_POOL_WIDTH+38,").append(taskIndex).append("*FLOW_POOL_HEIGHT,").append(sequenceIndex + 1).append(");\n");
            else if (currentPoolInd == prePoolIndex) {
              trackJS.append("sequenceFlows[").append(sequenceIndex).append("]=new AWSSequenceFlow(").append(prePoolIndex + 1).append("*FLOW_POOL_WIDTH-FLOW_POOL_WIDTH/2,").append(taskIndex).append("*FLOW_POOL_HEIGHT-23,0,0,").append(sequenceIndex + 1).append(");\n");
            }
            sequenceIndex++;
          }
          taskIndex++;
        }
      }

    }

    trackJS.append("new AWSFlowPoolsY(pools,tasks,sequenceFlows," + (processInstanceModel.isEnd() ? "1" : "0") + ").draw();\n");

    DBPoolsManager.getInstance().freeConnection(conn);
    int trackType = 0;
    StringBuilder trackContent = new StringBuilder();
    if ((workFlowModel != null) && (workFlowModel._trackDiagramType == 3)) {
      trackType = 1;
      trackContent.append("<iframe id=AWS_MONITOR_FLEX_TRACK name=AWS_MONITOR_FLEX_TRACK src='").append("./login.wf?sid=").append(super.getContext().getSessionId()).append("&cmd=AWSFlexProcessTrack_Portal&wfId=").append(workFlowModel._id).append("&extendConditions=_conditions{_instanceId{").append(instanceId).append("}instanceId_}conditions_").append("' marginwidth=\"0\" marginheight=\"0\" frameborder=\"0\" scrolling=\"no\" width=\"100%\" height=\"100%\"").append(" onload=\"tuneWidthAndHeight('AWS_MONITOR_FLEX_TRACK','AWS_MONITOR_FLEX_TRACK');\"").append(">");
      trackContent.append("</iframe>");
    }
    else {
      trackContent.append("<table border=0 width=" + (pools.size() + 2) * 180 + " cellspacing=\"0\" cellpadding=\"0\"><tr><td  height=" + (tasks.size() + 1) * 90 + ">&nbsp;</td></tr></table>\n");
      trackContent.append("<script> \n");
      trackContent.append(trackJS.toString());
      trackContent.append("drawLabel(25,140,\"<img src=../aws_img/workflow/" + I18nRes.findValue(super.getContext().getLanguage(), "style2_info.gif") + " border=0>\",1);");
      trackContent.append("</script> \n");
    }
    String wfName = WFFlexDesignVersionUtil.getFlowNameOfVersion(workFlowModel);
    String statue = wfName.substring(wfName.indexOf(".0(") - 2, wfName.length());
    wfName = I18nRes.findValue(getContext().getLanguage(), wfName.substring(0, wfName.indexOf(".0(") - 2)) + statue;
    Hashtable hashTags = new UnsyncHashtable();
    hashTags.put("title", I18nRes.findValue(getContext().getLanguage(), processInstanceModel.getTitle()));
    hashTags.put("taskTime", process.toString());
    hashTags.put("taskProcessName", "<I18N#目前工作进度>：");
    hashTags.put("readDiagram", getReadDiagram(instanceId));
    hashTags.put("trackContent", trackContent.toString());
    hashTags.put("createDate", UtilDate.datetimeFormat(processInstanceModel.getCreateDate()));
    hashTags.put("wfGroup", I18nRes.findValue(getContext().getLanguage(), processInstanceModel.getProcessGroupName()));
    hashTags.put("flowDesc", getWorkflowDocData(workFlowModel));
    hashTags.put("wf", wfName);
    hashTags.put("instanceId", Integer.toString(instanceId));
    hashTags.put("fileFrom", processInstanceModel.getCreateUserLocation() + authorInfo);
    hashTags.put("trackType", String.valueOf(trackType));
    hashTags.put("sid", getSIDFlag());
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("wf_messageTrack_style2.htm"), hashTags);
  }

  private String getWorkflowDocData(WorkFlowModel workFlowModel)
  {
    StringBuilder docBuffer = new StringBuilder();
    WFDesignDocumentWeb documentWeb = new WFDesignDocumentWeb(super.getContext());
    String flowDesc = new UtilString(documentWeb.getWFDocData(workFlowModel._uuid)).replace("__eol__", "<br>");
    if (flowDesc.trim().length() == 0) {
      return "";
    }
    docBuffer.append("<DIV class=\"tabcont bracket\">\n");
    docBuffer.append("<H1 class=tab id=awsTAB4 title=\"" + I18nRes.findValue(getContext().getLanguage(), "流程说明") + "\">" + I18nRes.findValue(getContext().getLanguage(), "fee70b6d027d871e147603402aeb2fc9") + "</H1>\n");
    docBuffer.append(flowDesc).append("\n");
    docBuffer.append("</DIV>\n");
    return docBuffer.toString();
  }

  private String getReadDiagram(int instanceId)
  {
    boolean isReadDiagram = false;
    StringBuilder readDiagram = new StringBuilder();
    readDiagram.append("<DIV class=\"tabcont bracket\">\n");
    readDiagram.append("<H1 class=tab id=awsTAB2 title=\"" + I18nRes.findValue(getContext().getLanguage(), "传阅传输跟踪") + "\">" + I18nRes.findValue(getContext().getLanguage(), "传阅跟踪") + "</H1> \n");
    readDiagram.append("<br><table width=100%  border=1 cellspacing=0 cellpadding=0 align=center bordercolorlight=#CCCCCC bordercolordark=#FFFFFF>\n");
    readDiagram.append("<tr>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=1>&nbsp;</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=1>&nbsp;</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=10%>" + I18nRes.findValue(getContext().getLanguage(), "aws.common.org.department") + "</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=10%>" + I18nRes.findValue(getContext().getLanguage(), "传阅人") + "</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=10%>" + I18nRes.findValue(getContext().getLanguage(), "aws.common.org.department") + "</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=10%>" + I18nRes.findValue(getContext().getLanguage(), "接收人") + "</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=20%>" + I18nRes.findValue(getContext().getLanguage(), "标题") + "</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=20%>" + I18nRes.findValue(getContext().getLanguage(), "接收时间") + "</td>\n");
    readDiagram.append("<td class=actionsoftReportTitle width=20%>" + I18nRes.findValue(getContext().getLanguage(), "aws_portal_阅读时间") + "</td>\n");
    readDiagram.append("</tr>\n");
    String sql = "select *  from WF_TASK  where STATUS=2 and BIND_ID =" + instanceId + " order by id";
    Connection conn = null;
    Statement stmt = null;
    ResultSet rset = null;
    try {
      conn = DBSql.open();
      stmt = conn.createStatement();
      rset = DBSql.executeQuery(conn, stmt, sql);
      while (rset.next()) {
        isReadDiagram = true;
        readDiagram.append("<tr>");
        readDiagram.append("<td width=1><img src=../aws_img/mail.gif></td>");
        String title = rset.getString("TITLE");
        if (title.contains("(传阅)")) {
          title = "(" + I18nRes.findValue(getContext().getLanguage(), title.split("\\)")[0].split("\\(")[1]) + ")" + title.split("\\)")[1];
        }
        String owner = rset.getString("OWNER");
        String target = rset.getString("TARGET");
        if (title == null)
          title = "&nbsp;";
        readDiagram.append("<td class=actionsoftReportTitle width=1>&nbsp;</td>\n");
        readDiagram.append("<td align=center><font color=blue><b>").append(DepartmentCache.getFullName(new UserContext(owner).getUserModel().getDepartmentId())).append("</b></font></td>\n");
        readDiagram.append("<td align=center><font color=blue><b>").append(new UserContext(owner).getUserModel().getUserNameAlias()).append("</b></font></td>\n");
        readDiagram.append("<td align=center><font color=blue><b>").append(DepartmentCache.getFullName(new UserContext(target).getUserModel().getDepartmentId())).append("</b></font></td>\n");
        readDiagram.append("<td align=center><font color=blue><b>").append(new UserContext(target).getUserModel().getUserNameAlias()).append("</b></font></td>\n");

        readDiagram.append("<td><font color=blue><b>").append(title).append("</b></font></td>\n");
        readDiagram.append("<td align=center><font color=blue><b>").append(UtilDate.datetimeFormat(rset.getTimestamp("BEGINTIME"))).append("</b></font></td>\n");
        readDiagram.append("<td align=center><font color=blue><b>" + I18nRes.findValue(getContext().getLanguage(), "尚未阅读") + "</b></font></td>\n");
        readDiagram.append("</tr>\n");
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
      try
      {
        if (stmt != null) {
          stmt.close();
        }
        if (rset != null)
          rset.close();
      }
      catch (Exception localException1)
      {
      }
    }
    finally
    {
      try
      {
        if (stmt != null) {
          stmt.close();
        }
        if (rset != null) {
          rset.close();
        }
      }
      catch (Exception localException2)
      {
      }
    }
    sql = "select *  from WF_TASK_LOG  where STATUS=2 and BIND_ID =" + instanceId + " order by id";
    try {
      stmt = conn.createStatement();
      rset = DBSql.executeQuery(conn, stmt, sql);
      while (rset.next()) {
        isReadDiagram = true;
        readDiagram.append("<tr>");
        readDiagram.append("<td width=1><img src=../aws_img/macrodef_obj.gif></td>");
        String title = rset.getString("TITLE");
        if (title.contains("(传阅)")) {
          title = "(" + I18nRes.findValue(getContext().getLanguage(), title.split("\\)")[0].split("\\(")[1]) + ")" + title.split("\\)")[1];
        }
        String owner = rset.getString("OWNER");
        String target = rset.getString("TARGET");
        if (title == null)
          title = "&nbsp;";
        readDiagram.append("<td class=actionsoftReportTitle width=1>&nbsp;</td>");
        readDiagram.append("<td align=center><font color=blue><b>").append(DepartmentCache.getFullName(new UserContext(owner).getUserModel().getDepartmentId())).append("</b></font></td>");
        readDiagram.append("<td align=center><font color=blue>").append(new UserContext(owner).getUserModel().getUserNameAlias()).append("</font></td>");
        readDiagram.append("<td align=center><font color=blue><b>").append(DepartmentCache.getFullName(new UserContext(target).getUserModel().getDepartmentId())).append("</b></font></td>");
        readDiagram.append("<td align=center><font color=blue>").append(new UserContext(target).getUserModel().getUserNameAlias()).append("</font></td>");
        readDiagram.append("<td><font color=blue>").append(title).append("</font></td>");
        readDiagram.append("<td align=center><font color=blue>").append(UtilDate.datetimeFormat(rset.getTimestamp("BEGINTIME"))).append("</font></td>");
        readDiagram.append("<td align=center><font color=blue>").append(UtilDate.datetimeFormat(rset.getTimestamp("ENDTIME"))).append("</font></td>");
        readDiagram.append("</tr>");
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    } finally {
      DBSql.close(conn, stmt, rset);
    }
    readDiagram.append("</table>");
    readDiagram.append("</DIV>");

    if (isReadDiagram) {
      return readDiagram.toString();
    }
    return "";
  }

  private WorkFlowStepModel getStepModel(Map stepList, int id)
  {
    for (int i = 0; i < stepList.size(); i++) {
      WorkFlowStepModel model = (WorkFlowStepModel)stepList.get(new Integer(i));
      if (model._id == id)
        return model;
    }
    return null;
  }

  public String getWorkflowStepTrackStatus(int instanceId, WorkFlowModel wfModel, WorkFlowStepModel sModel)
  {
    Hashtable tasks = new UnsyncHashtable();
    Hashtable taskLog = ProcessRuntimeDaoFactory.createTaskInstance().getEndTaskListByProcessInstance(instanceId);
    if (taskLog != null) {
      for (int i = 0; i < taskLog.size(); i++) {
        TaskInstanceModel taskInstanceModel = (TaskInstanceModel)taskLog.get(new Integer(i));
        if ((taskInstanceModel.getStatus() != 1) && (taskInstanceModel.getStatus() != 11))
          continue;
        if (sModel._id == taskInstanceModel.getActivityDefinitionId()) {
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
        }
      }

    }

    Hashtable taskWork = ProcessRuntimeDaoFactory.createTaskInstance().getActiveTaskListByProcessInstance(instanceId);
    TaskInstanceModel lastWorkTaskModel = null;
    if (taskWork != null) {
      for (int i = 0; i < taskWork.size(); i++) {
        TaskInstanceModel taskInstanceModel = (TaskInstanceModel)taskWork.get(new Integer(i));
        if ((sModel._id == taskInstanceModel.getActivityDefinitionId()) && ((taskInstanceModel.getStatus() == 1) || (taskInstanceModel.getStatus() == 11) || (taskInstanceModel.getStatus() == 4))) {
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
          lastWorkTaskModel = taskInstanceModel;
          break;
        }
      }

    }

    if (lastWorkTaskModel != null) {
      String sql = "select *  from wf_messagepoint  where parent_id=" + instanceId + " and state=0 and wfs_id=" + lastWorkTaskModel.getActivityDefinitionId() + " order by id";
      Connection conn = null;
      Statement stmt = null;
      ResultSet rset = null;
      try {
        conn = DBSql.open();
        stmt = conn.createStatement();
        rset = DBSql.executeQuery(conn, stmt, sql);
        while (rset.next()) {
          TaskInstanceModel taskInstanceModel = new TaskInstanceModel();
          taskInstanceModel.setOwner(rset.getString("OWNER"));
          taskInstanceModel.setTarget(rset.getString("TARGET"));
          taskInstanceModel.setActivityDefinitionId(rset.getInt("WFS_ID"));
          taskInstanceModel.setProcessDefinitionId(rset.getInt("WF_ID"));
          taskInstanceModel.setProcessInstanceId(rset.getInt("PARENT_ID"));
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
        }
      } catch (Exception e) {
        e.printStackTrace(System.err);
      } finally {
        DBSql.close(conn, stmt, rset);
      }
    }
    if ((tasks == null) || (tasks.size() == 0)) {
      return "TRACK:_control{WAIT}control_ _instanceId{" + instanceId + "}instanceId_";
    }
    String taskState = "";
    for (int i = 0; i < tasks.size(); i++) {
      TaskInstanceModel taskInstanceModel = (TaskInstanceModel)tasks.get(new Integer(i));
      if (taskInstanceModel != null) {
        taskState = "";
        if (taskInstanceModel.getEndTime() == null)
          taskState = "WORK";
        else if (taskInstanceModel.getBeginTime() == null)
          taskState = "WAIT";
        else
          taskState = "FINISH";
      }
    }
    return "TRACK:_control{" + taskState + "}control_ _instanceId{" + instanceId + "}instanceId_";
  }

  public String getWorkflowStepTrackInfo(int instanceId, WorkFlowModel wfModel, int wfsId)
  {
    WorkFlowStepModel sModel = (WorkFlowStepModel)WorkFlowStepCache.getModel(wfsId);
    if (sModel == null) {
      return null;
    }
    Hashtable tasks = new UnsyncHashtable();
    Hashtable taskLog = ProcessRuntimeDaoFactory.createTaskInstance().getEndTaskListByProcessInstance(instanceId);
    if (taskLog != null) {
      for (int i = 0; i < taskLog.size(); i++) {
        TaskInstanceModel taskInstanceModel = (TaskInstanceModel)taskLog.get(new Integer(i));
        if ((taskInstanceModel.getStatus() != 1) && (taskInstanceModel.getStatus() != 11))
          continue;
        if (wfsId == taskInstanceModel.getActivityDefinitionId()) {
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
        }

      }

    }

    Hashtable taskWork = ProcessRuntimeDaoFactory.createTaskInstance().getActiveTaskListByProcessInstance(instanceId);
    TaskInstanceModel lastWorkTaskModel = null;
    if (taskWork != null) {
      for (int i = 0; i < taskWork.size(); i++) {
        TaskInstanceModel taskInstanceModel = (TaskInstanceModel)taskWork.get(new Integer(i));
        if ((wfsId == taskInstanceModel.getActivityDefinitionId()) && ((taskInstanceModel.getStatus() == 1) || (taskInstanceModel.getStatus() == 11) || (taskInstanceModel.getStatus() == 4))) {
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
          lastWorkTaskModel = taskInstanceModel;
        }

      }

    }

    if (lastWorkTaskModel != null) {
      String sql = "select *  from wf_messagepoint  where parent_id=" + instanceId + " and state=0 and wfs_id=" + lastWorkTaskModel.getActivityDefinitionId() + " order by id";
      Connection conn = null;
      Statement stmt = null;
      ResultSet rset = null;
      try {
        conn = DBSql.open();
        stmt = conn.createStatement();
        rset = DBSql.executeQuery(conn, stmt, sql);
        while (rset.next()) {
          TaskInstanceModel taskInstanceModel = new TaskInstanceModel();
          taskInstanceModel.setOwner(rset.getString("OWNER"));
          taskInstanceModel.setTarget(rset.getString("TARGET"));
          taskInstanceModel.setActivityDefinitionId(rset.getInt("WFS_ID"));
          taskInstanceModel.setProcessDefinitionId(rset.getInt("WF_ID"));
          taskInstanceModel.setProcessInstanceId(rset.getInt("PARENT_ID"));
          tasks.put(new Integer(tasks.size()), taskInstanceModel);
        }
      } catch (Exception e) {
        e.printStackTrace(System.err);
      } finally {
        DBSql.close(conn, stmt, rset);
      }
    }

    if (tasks.size() == 0) {
      return null;
    }
    StringBuilder infoBuffer = new StringBuilder();
    String taskTitle = "";
    String taskRole = "";
    String info1 = ""; String info2 = ""; String hotTitle = ""; String kmInfo = ""; String taskState = "";
    for (int i = 0; i < tasks.size(); i++) {
      TaskInstanceModel taskInstanceModel = (TaskInstanceModel)tasks.get(new Integer(i));
      if (taskInstanceModel != null) {
        taskState = "FINISH";
        if (taskInstanceModel.getEndTime() == null)
          taskState = "WORK";
        if ((taskInstanceModel.getEndTime() == null) && (taskInstanceModel.getBeginTime() == null))
          taskState = "WAIT";
        try
        {
          UserContext target = new UserContext(taskInstanceModel.getTarget());
          taskTitle = target.getUserModel().getUserName();
          taskRole = "(" + target.getRoleModel().getRoleName() + ")";

          int deptid = taskInstanceModel.getTargetDepartmentId();

          Map list = UserMapCache.getMapListOfUser(taskInstanceModel.getTarget());
          for (int j = 0; j < list.size(); j++) {
            UserMapModel umm = (UserMapModel)list.get(Integer.valueOf(j));
            if (umm.getDepartmentId() == deptid) {
              RoleModel rm = (RoleModel)RoleCache.getModel(umm.getRoleId());
              if (rm == null) break;
              taskRole = "(<I18N#" + rm.getRoleName() + ">)";

              break;
            }
          }
        }
        catch (Exception localException1)
        {
        }
        if (taskInstanceModel.getBeginTime() != null)
          hotTitle = "<I18N#到达时间>:" + UtilDate.datetimeFormat(taskInstanceModel.getBeginTime());
        boolean isRead = true;
        if (taskInstanceModel.getEndTime() != null) {
          hotTitle = hotTitle + "<br/><I18N#办理时间>:" + UtilDate.datetimeFormat(taskInstanceModel.getEndTime());

          if (wfModel._isTrackForm) {
            info1 = "<a href='###' onClick=\"parent.openForm(frmMain," + instanceId + "," + taskInstanceModel.getActivityDefinitionId() + ");return false;\">查看数据</a>";
          }

          if (sModel._kmDirectoryId > 0) {
            EnterpriseDocGroupModel docGroupModel = EnterpriseDocDaoFactory.createDocGroup().getInstance(sModel._kmDirectoryId);
            if (docGroupModel != null)
              kmInfo = "<I18N#入库到>：" + docGroupModel._root_name + "/" + docGroupModel._groupName;
          }
        }
        else if ((taskInstanceModel.getEndTime() == null) && (taskInstanceModel.getBeginTime() != null)) {
          hotTitle = hotTitle + "<br /><I18N#正在办理>...";
          if (taskInstanceModel.getReadTask() == 0) {
            hotTitle = hotTitle + " <<I18N#未读>>";
            isRead = false;
          }
          info1 = "<img src=../aws_img/waite.process.gif >";
        } else {
          hotTitle = hotTitle + "\\n<I18N#等待上一任务结束>";
          info1 = "<img src=../aws_img/wait.gif width=20>";
        }

        if (taskInstanceModel.getStatus() == 11) {
          if (taskInstanceModel.getTitle().trim().length() > 0) {
            if (taskInstanceModel.getTitle().indexOf("(阅办)") == 0)
              info1 = "<font color=red>(<I18N#阅办>)</font>";
            else if (taskInstanceModel.getTitle().indexOf("(会签)") == 0)
              info1 = "<font color=red>(<I18N#会签>)</font>";
            else
              info1 = "<font color=red>(<I18N#加签>)</font>";
          }
          else {
            info1 = "<font color=red>(<I18N#加签>)</font>";
          }
        }

        String photoDir = AWFConfig._awfServerConf.getDocumentPath() + "Photo/group" + taskInstanceModel.getTarget() + "/file0/" + taskInstanceModel.getTarget() + ".jpg";
        File photoFile = new File(photoDir);
        String photoURL = "";
        if (photoFile.exists())
          photoURL = "./downfile.wf?flag1=" + taskInstanceModel.getTarget() + "&flag2=0&sid=" + getContext().getSessionId() + "&rootDir=Photo&filename=" + taskInstanceModel.getTarget() + ".jpg";
        else {
          photoURL = "../aws_img/userPhoto.png";
        }
        infoBuffer.append("<tr >");
        infoBuffer.append("<td width=\"20%\" rowspan=\"2\">").append("<img src=\"").append(photoURL).append("\" border=\"0\">").append("</td>");
        infoBuffer.append("<td width=\"50%\">").append(info1).append("</td>");
        String infoTaskUser = taskTitle + taskRole;
        if (infoTaskUser.length() > 9) {
          infoTaskUser = infoTaskUser.substring(0, 9);
        }
        infoBuffer.append("<td width=\"30%\" nowrap=\"nowrap\" title=\"").append(taskTitle + taskRole).append("\">").append(infoTaskUser).append("..</td>");
        infoBuffer.append("</tr>\n");
        infoBuffer.append("<tr>");
        infoBuffer.append("<td>").append(hotTitle).append("</td>");
        infoBuffer.append("</tr>");
        if (i + 1 < tasks.size()) {
          infoBuffer.append("<tr ><td colspan=\"3\"><div style=\"border-top:1px dashed #cccccc;height: 1px;overflow:hidden\"></div></td></tr>");
        }
      }
    }
    Hashtable hashTags = new UnsyncHashtable();
    hashTags.put("sid", getContext().getSessionId());
    hashTags.put("stepTrackInfo", infoBuffer.toString());
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("WorkFlow_Flex_Process_Track_info.htm"), hashTags);
  }
}