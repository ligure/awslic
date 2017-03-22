package com.actionsoft.eip.cmcenter.rtclass;

import java.util.Date;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.Sequence;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
/**
 * 
 * @description cms栏目第一个节点，修改第一个节点办理后在前台显示，但是点击提示已删除。
 * @version 1.0
 * @author wangaz
 * @update 2014年7月2日 下午3:22:30
 */
public class SpecifyChannel extends WorkFlowStepRTClassA
{
  public SpecifyChannel(UserContext uc)
  {
    super(uc);
    super.setDescription("SpecifyChannel类可以根据流程设计的名称来自动将内容发布到相关的栏目上，其流程名称的命名规则为：指定发布:%栏目名称%");
    super.setVersion("5.1.3");
  }

  public boolean execute()
  {
    int instanceId = getParameter("PARAMETER_WORKFLOW_INSTANCE_ID").toInt();

    int workflowId = getParameter("PARAMETER_WORKFLOW_ID").toInt();

    String uid = getUserContext().getUserModel().getUID();
    WorkFlowModel workflowModel = (WorkFlowModel)WorkFlowCache.getModel(workflowId);
    if (workflowModel != null) {
      if (workflowModel._flowName.indexOf("指定发布:") == 0) {
        String channelName = workflowModel._flowName.substring(5);
        CmChannelModel cmChannelModel = (CmChannelModel)CmChannelCache.getModel(channelName);
        if (cmChannelModel != null)
        {
          int BOID = 0;
          int ORGNO = super.getUserContext().getCompanyModel().getId();
          int BINDID = instanceId;
          int WORKFLOWID = workflowId;
          int WORKFLOWSTEPID = getParameter("PARAMETER_WORKFLOW_STEP_ID").toInt();
          try {
            BOID = Sequence.getSequence("USER_WORKFLOWREPORT");
          } catch (Exception localException) {
          }
          DBSql.executeUpdate("delete from eip_cm_channelcont where bindid=" + instanceId);

          int contentId = DBSql.getInt("select id from eip_cm_content where bindid=" + instanceId, "id");

          String sql = "INSERT INTO eip_cm_channelcont(ID,ORGNO,BINDID,CREATEDATE,CREATEUSER,UPDATEDATE,UPDATEUSER,WORKFLOWID,WORKFLOWSTEPID,ISEND,CHANNELID,CONTENTID,CHANNELNAME)values(" + BOID + ",'" + ORGNO + "'," + BINDID + "," + DBSql.getDateDefaultValue() + ",'" + super.getUserContext().getUID() + "'," + DBSql.getDateDefaultValue() + ",'" + super.getUserContext().getUID() + "'," + WORKFLOWID + "," + WORKFLOWSTEPID + ",1," + cmChannelModel._id + "," + contentId + ",'" + cmChannelModel._channelName + "')";
          DBSql.executeUpdate(sql);

          DBSql.executeUpdate("UPDATE eip_cm_content set DEPTID=" + getUserContext().getDepartmentModel().getId() + ",TITLE=DISPLAYTITLE where bindId=" + instanceId);

          sql = "select POPEDOMLIST from SYS_CMCHANNEL where CHANNELNAME='" + cmChannelModel._channelName + "'";
          String popedom = DBSql.getString(sql, "POPEDOMLIST");
          if ((popedom != null) && (!"".equals(popedom))) {
            String[] popedomlist = (String[])CmUtil.stringTokenizer(popedom, " ");
            if (popedomlist != null) {
              for (int i = 0; i < popedomlist.length; i++)
              {
                if (!uid.equals(Function.getUID(popedomlist[i])))
                  continue;
                int channelCount = DBSql.getInt("select count(*) as c from eip_cm_channelcont where bindId=" + instanceId, "c");
                if (channelCount == 0)
                {
                  MessageQueue.getInstance().putMessage(super.getUserContext().getUID(), "没有指定内容发布栏目，发布被终止，请在待办任务中重新打开办理");
                  return false;
                }
                String isclose = DBSql.getString("select ISCLOSE from eip_cm_content where bindid=" + instanceId, "ISCLOSE");
                if ("是".equals(isclose)) {
                  break;
                }
                //isclos默认为是(既关闭状态)，所以出现第一个节点就显示的问题，将此行注释，问题解决
              //  int updateflag = DBSql.executeUpdate("UPDATE eip_cm_content set ISCLOSE='否',releasedate=" + DBSql.convertLongDate(UtilDate.datetimeFormat(new Date())) + " where bindId=" + instanceId);

//                if (updateflag > 0) {
//                  break;
//                }
                MessageQueue.getInstance().putMessage(super.getUserContext().getUID(), "发布失败");
                return false;
              }

            }

          }

          return true;
        }
        MessageQueue.getInstance().putMessage(super.getUserContext().getUID(), "没有发现指定的栏目[" + channelName + "]，流程的命名规则应该为：指定发布:%栏目名称%");
        return false;
      }

      MessageQueue.getInstance().putMessage(super.getUserContext().getUID(), "当前流程设计的名称不符合命名规则，流程的命名规则应该为：指定发布:%栏目名称%");
      return false;
    }

    MessageQueue.getInstance().putMessage(super.getUserContext().getUID(), "当前流程已经被删除!");
    return false;
  }
}