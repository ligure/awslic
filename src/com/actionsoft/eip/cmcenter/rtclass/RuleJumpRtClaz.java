package com.actionsoft.eip.cmcenter.rtclass;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.execute.dao.ProcessInstance;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.loader.core.ValueAdapter;
import com.actionsoft.loader.core.WorkFlowStepJumpRuleRTClassA;

public class RuleJumpRtClaz extends WorkFlowStepJumpRuleRTClassA
{
  public RuleJumpRtClaz(UserContext uc)
  {
    super(uc);
    super.setDescription("如果具有快速发布权限，直接结束流程");
  }

  public int getNextNodeNo()
  {
    int instanceId = getParameter("PARAMETER_WORKFLOW_INSTANCE_ID").toInt();

    int workflowId = getParameter("PARAMETER_WORKFLOW_ID").toInt();
    String title = DBSql.getString("select DISPLAYTITLE from eip_cm_content where bindid=" + instanceId, "DISPLAYTITLE");
    if ((title != null) && (title.length() > 0) && (!title.toLowerCase().equals("null"))) {
      ProcessRuntimeDaoFactory.createProcessInstance().setTitle(instanceId, title);
    }
    String uid = getUserContext().getUserModel().getUID();
    WorkFlowModel workflowModel = (WorkFlowModel)WorkFlowCache.getModel(workflowId);
    if ((workflowModel != null) && 
      (workflowModel._flowName.indexOf("指定发布:") == 0)) {
      String channelName = workflowModel._flowName.substring(5);
      CmChannelModel cmChannelModel = (CmChannelModel)CmChannelCache.getModel(channelName);
      if (cmChannelModel != null) {
        String sql = "select POPEDOMLIST from SYS_CMCHANNEL where CHANNELNAME='" + cmChannelModel._channelName + "'";
        String popedom = DBSql.getString(sql, "POPEDOMLIST");
        if ((popedom != null) && (!"".equals(popedom))) {
          String[] popedomlist = (String[])CmUtil.stringTokenizer(popedom, " ");
          if (popedomlist != null) {
            for (int i = 0; i < popedomlist.length; i++)
            {
              if (uid.equals(Function.getUID(popedomlist[i]))) {
                MessageQueue.getInstance().putMessage(getUserContext().getUID(), "<I18N#恭喜：信息已发布>!");
                return 9999;
              }
            }
          }
        }
      }
    }

    return 2;
  }

  public String getNextTaskUser()
  {
    return null;
  }
}