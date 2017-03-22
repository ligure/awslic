package com.actionsoft.awf.workflow.execute.dao;

import java.util.Hashtable;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WrokFlowEmailTemplateModel;
import com.actionsoft.awf.workflow.design.util.WFFlexDesignEmailTemplateUtil;
import com.actionsoft.awf.workflow.execute.engine.helper.EmailAlertUtil;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.actionsoft.sdk.local.level0.TaskWorklistAPI;

class NotifyEMAIL implements Runnable {

    private UserContext owner;
    private String participant;
    private String title;
    private int taskId;
    private int emailType;

    public NotifyEMAIL(UserContext owner, String participant, String title,
	    int taskId, int emailType) {
	this.owner = owner;
	this.participant = participant;
	this.title = title;
	this.taskId = taskId;
	this.emailType = emailType;
    }

    public void run() {
	String uid = Function.getUID(this.participant.trim());
	String uuid = new EmailAlertUtil().createAlert(this.taskId);
	String portalHost = AWFConfig._awfServerConf.getPortalHost();
	if (!uid.equals(this.owner.getUID())) {
	    UserModel userModel = (UserModel) UserCache.getModel(uid);
	    if (userModel == null) {
		System.err.println("发送邮件到达通知失败，不存在账户[" + uid + "]");
		return;
	    }
	    String email = userModel.getEmail();
	    String mailTo = uid;
	    if (email != null && email.trim().length() > 0) {
		mailTo = email;
	    }
	    if (uuid != null) {
		TaskInstanceModel taskInstanceModel = new TaskInstance()
			.getInstanceOfActive(this.taskId);
		if (taskInstanceModel != null) {
		    WorkFlowStepModel wfsModel = (WorkFlowStepModel) WorkFlowStepCache
			    .getModel(taskInstanceModel
				    .getActivityDefinitionId());
		    if (wfsModel != null) {
			String emailTemplate = wfsModel._emailTemplate;
			WrokFlowEmailTemplateModel wrokFlowEmailTemplateModel = WFFlexDesignEmailTemplateUtil
				.getEmailTemplateModel(emailTemplate);
			wrokFlowEmailTemplateModel = wrokFlowEmailTemplateModel == null ? WFFlexDesignEmailTemplateUtil
				.getDefaultEmailTemplateModel("任务到达通知")
				: wrokFlowEmailTemplateModel;
			Hashtable param = new UnsyncHashtable();
			param.put("%CURRENT_CONTEXT%", this.owner.getUID());
			param.put("%TASK_LINK_HEAD%", "<a href='" + portalHost
				+ "/workflow/processAlert?alert=" + uuid
				+ "' target='_blank'>");
			param.put("%TASK_LINK_END%", "</a>");
			param.put(
				"%MY_WORKBOX_HEAD%",
				"<a href='"
					+ portalHost
					+ "/workflow/processAlert?alert=INTO_MY_WORKBOX"
					+ uid + "' target='_blank'>");
			param.put("%MY_WORKBOX_END%", "</a>");
			try {
			    param.put("%MY_WORKLIST_COUNT%", String
				    .valueOf(TaskWorklistAPI.getInstance()
					    .getTaskCount(uid, 0, null, null)));
			} catch (AWSSDKException e1) {
			    param.put("%MY_WORKLIST_COUNT%", "0");
			}
			try {
			    IMAPI.getInstance().sendMailByModel(
				    wrokFlowEmailTemplateModel.getNo(),
				    this.owner.getUID(), mailTo,
				    taskInstanceModel.getProcessInstanceId(),
				    taskInstanceModel.getId(), param);
			} catch (AWSSDKException e) {
			    e.printStackTrace(System.err);
			}
		    }
		}
	    }
	}
    }
}