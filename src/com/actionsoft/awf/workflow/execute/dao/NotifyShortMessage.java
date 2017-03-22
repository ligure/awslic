package com.actionsoft.awf.workflow.execute.dao;

import java.util.HashMap;

import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.eai.shortmessage.SMSContext;
import com.actionsoft.eai.shortmessage.SendSMSUtil;

class NotifyShortMessage implements Runnable {

    private UserContext owner;
    private int processInstanceId;
    private int taskInstanceId;
    private String participant;
    private ProcessInstanceModel processInstanceModel;
    private String title;

    public NotifyShortMessage(UserContext owner, int processInstanceId,
	    String participant, ProcessInstanceModel processInstanceModel,
	    String title, int taskInstanceId) {
	this.owner = owner;
	this.participant = participant;
	this.processInstanceId = processInstanceId;
	this.processInstanceModel = processInstanceModel;
	this.taskInstanceId = taskInstanceId;
	this.title = title;
    }

    public void run() {
	WorkFlowModel currentWorkFlowModel = (WorkFlowModel) WorkFlowCache
		.getModel(this.processInstanceModel.getProcessDefinitionId());
	String sender = this.owner.getUID();
	UserContext senderContext = null;
	try {
	    String targetUID = Function.getUID(this.participant.trim());
	    senderContext = new UserContext(sender);
	    SMSContext sms = new SMSContext();
	    sms.setWorkflowInstanceId(this.processInstanceId);
	    sms.setCompanyName(senderContext.getCompanyModel().getCompanyName());
	    sms.setDepartmentName(senderContext.getDepartmentModel()
		    .getDepartmentName());
	    sms.setUid(senderContext.getUID());
	    sms.setUserName(senderContext.getUserModel().getUserName());
	    if (!targetUID.equals(sender)) {
		UserModel model = (UserModel) UserCache.getModel(targetUID);
		if (model != null && model.getSMid() != null
			&& !model.getSMid().equals(""))
		    sms.setMobileCode(model.getSMid().trim());
		else if (model != null && model.getMobile() != null
			&& !model.getMobile().equals("")) {
		    sms.setMobileCode(model.getMobile().trim());
		}
		sms.setMobileID(model.getUID().trim());
		sms.setMobileUserName(model.getUserName());
	    } else {
		return;
	    }

	    String smContent = "";
	    if (currentWorkFlowModel._shortMessageModel.length() > 0) {
		RuntimeFormManager rfm = new RuntimeFormManager(this.owner,
			this.processInstanceId, this.taskInstanceId, 0, 0);
		smContent = "待办提醒:"
			+ rfm.convertMacrosValue(currentWorkFlowModel._shortMessageModel);
	    } else {
		smContent = "待办提醒:" + this.title;
	    }
	    SendSMSUtil send = new SendSMSUtil(sms);
	    HashMap result = send.send(smContent);

	    Object[] mobiles = result.keySet().toArray();
	    for (int i = 0; i < mobiles.length; ++i) {
		String mobile = (String) mobiles[i];
		String msg = (String) result.get(mobile);
		MessageQueue.getInstance().putMessage(this.owner.getUID(),
			"手机号[" + mobile + "]短信状态[" + msg + "]", true);
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }
}