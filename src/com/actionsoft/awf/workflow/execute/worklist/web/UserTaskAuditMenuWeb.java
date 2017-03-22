package com.actionsoft.awf.workflow.execute.worklist.web;

import java.util.Hashtable;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepOpinionCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepOpinionModel;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.model.UserTaskAuditMenuModel;
import com.actionsoft.i18n.I18nRes;

public class UserTaskAuditMenuWeb {
    private static final String _saveButton = "";

    public String getAuditWindow(UserContext me, int workflowId, int stepNo,
	    int taskId, int processInstanceId) {
	return getAuditWindow(me, workflowId, stepNo, taskId, false,
		processInstanceId);
    }

    public String getAuditWindow(UserContext me, int workflowId, int stepNo,
	    int taskId, boolean isHiddenMenu, int processInstanceId) {
	return getAuditWindow(me, workflowId, stepNo, taskId, isHiddenMenu, 0,
		processInstanceId);
    }

    public String getAuditWindow(UserContext me, int processDefinitionId,
	    int stepNo, int taskInstanceId, boolean isHiddenMenu,
	    int positionOption, int processInstanceId) {
	StringBuilder sb = new StringBuilder();
	UserTaskAuditMenuModel model = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory
		.createUserTaskAuditMenu().getInstanceOfTask(taskInstanceId);
	if (model == null) {
	    model = new UserTaskAuditMenuModel();
	}
	WorkFlowStepModel stepModel = WorkFlowStepCache.getModelOfStepNo(
		processDefinitionId, stepNo);
	if (stepModel == null) {
	    stepModel = new WorkFlowStepModel();
	}

	if (!stepModel._isAudit) {
	    return "";
	}
	sb.append(
		"<a name='ACTIONSOFT-AUDIT-MENU'></a><style media=print>.Noprint{display:none;}</style><div  class=Noprint id=AWS_AUDIT_MENUS><table border='0' cellpadding='0' cellspacing='0' width='100%' bgcolor='#FFFFFF' align='center' id=AWS_AUDIT_TABLE>")
		.append("<tr AWS_AUDIT_TR1><td id=AWS_AUDIT_TD1 background='../aws_skins/_def/img/bg-background.gif' style=\"background-attachment: fixed;font-size:14px;vertical-align: bottom;\" height=19> <font face='黑体，宋体' color=#CCCCCC><b>&nbsp;")
		.append(I18nRes.findValue(me.getLanguage(),
			"aws.common.efrom.audit.menu.title"))
		.append("&nbsp;&nbsp;&nbsp;&nbsp;</b></font>").append("")
		.append("</td>").append("").append("</tr>\n");
	if (positionOption == 1) {
	    sb.append("<tr id=AWS_AUDIT_TR2>\n");
	    sb.append("<td id=AWS_AUDIT_TD2 class='actionsoftToolBar' ><input type=button value='下一步'  class ='actionsoftButton' onClick=\"saveAndNext(frmMain);return false;\" name='sv'  border='0'></td>");
	    sb.append("</tr>\n");
	}
	sb.append(
		"<TR id=AWS_AUDIT_TR3><TD id=AWS_AUDIT_TD3 class=actionsoftReportData>")
		.append("<table border='0' cellpadding='0' cellspacing='0' width='100%'>")
		.append("<tr>\n").append("<td align=left><br>");

	String auditType = "";
	Hashtable opinionList = new UnsyncHashtable();
	opinionList = WorkFlowStepOpinionCache.getListOfWorkFlowOpinion2(
		processDefinitionId, stepModel._id);
	if (opinionList.size() != 0 && !isHiddenMenu) {
	    if (opinionList.size() > 0)
		sb.append("<img src='../aws_img/check.gif'><b>")
			.append(I18nRes.findValue(me.getLanguage(),
				"aws.common.efrom.audit.menu.select"))
			.append("</b></label>");
	    for (int i = 0; i < opinionList.size(); i++) {
		WorkFlowStepOpinionModel opinionModel = (WorkFlowStepOpinionModel) opinionList
			.get(new Integer(i));
		auditType = opinionModel._opinionName + "/"
			+ opinionModel._opinionType;
		sb.append(
			"&nbsp;<label style='cursor:hand;' onclick='try{auditType["
				+ i
				+ "].checked=true;}catch(e){}'><input type=\"radio\" name=\"auditType\" value=\"")
			.append(auditType).append("\" ");
		if (model.getTaskInstanceId() == 0) {
		    sb.append(opinionModel._isCheck == 1 ? "checked" : "");
		} else if (model.getAuditMenuName().equals(
			opinionModel._opinionName)) {
		    sb.append(model.getAuditType() == opinionModel._opinionType ? "checked"
			    : "");
		}

		sb.append("><I18N#" + opinionModel._opinionName
			+ ">&nbsp;</label>");
	    }
	} else {
	    auditType = " /-99";
	}
	sb.append("</td>").append("</tr>\n");
	sb.append("<tr id=AWS_AUDIT_TR4>").append("<td id=AWS_AUDIT_TD4> ");
	if (stepModel._auditMenuOpinion != 2) {
	    sb.append(
		    "<table width=100% ID=AWS_AUDIT_OPINION_TABLE style=\"display:\"  border=0 ><label><tr id=AWS_AUDIT_OPINION_TR><td id=· align=left>")
		    .append(I18nRes.findValue(me.getLanguage(),
			    "aws.common.efrom.audit.menu.memo"));
	    if (stepModel._auditMenuOpinion == 1
		    || stepModel._auditMenuOpinion == 4) {
		sb.append("<img src='../aws_img/notNull.gif' title='<I18N#必须填写意见留言>'>");
	    }
	    sb.append(
		    "<a href=\"#\" onClick=\"resizeIndexWindows();return false;\"><img id=resizeIndexIcon  src=\"../aws_img/expandbtn2.gif\" border=\"0\"></a>")
		    .append("</label></td></tr>\n<tr height=4><td background='../aws_img/line2.gif' ></td></tr>\n<tr id=mainIndexZone><td>")
		    .append("<textarea name=opinion  style='width:100%;height:"
			    + (positionOption == 0 ? 4 : 10) * 30
			    + "px' wrap='on'>").append(model.getOpinion())
		    .append("</textarea>").append("</td>").append("</tr>\n")
		    .append("</table>\n");
	    if (stepModel._auditMenuOpinion == 3
		    || stepModel._auditMenuOpinion == 4) {
		String btn = "<input type='button' class='actionsoftButton' style='height:25px;' onClick=\"upFileAudit("
			+ processInstanceId
			+ ","
			+ taskInstanceId
			+ ");return false;\" value='<I18N#上传>' />";
		String img = "";
		if (stepModel._auditMenuOpinion == 4) {
		    img = "<img src='../aws_img/notNull.gif'>";
		}
		sb.append("<I18N#附件文件>" + img + btn);
		sb.append("<div id='attachmentList'></div>");
		sb.append("<script> Ext.onReady(function(){try{callBack();}catch(e){}});</script>");
	    }
	} else {
	    sb.append("<span style='height:5px'><input type=hidden name=opinion></span>");
	}
	sb.append("</TD></TR>\n")
		.append("</table>")
		.append("<input type=hidden name=audtmenuopinionId id=audtmenuopinionId value=")
		.append(stepModel._auditMenuOpinion)
		.append(" >")
		.append("<input type=hidden name=auditId id=auditId value=")
		.append(model.getId())
		.append(">\n<script>\nfunction gotoActionsoftAuditMenu(){\nwindow.location.href=window.location.href+'#ACTIONSOFT-AUDIT-MENU';\n}\n</script>\n<script>\nvar resizeIndexType=0; \nfunction resizeIndexWindows(){\nif(resizeIndexType==0){\nfrmMain.resizeIndexIcon.src=\"../aws_img/expandbtn2.gif\";\nmainIndexZone.style.display='none';\nresizeIndexType=1;\n}else{\nfrmMain.resizeIndexIcon.src=\"../aws_img/expanded_button.gif\";\nresizeIndexType=0;\nmainIndexZone.style.display='';\n}\n}\n</script>\n");

	return sb.toString();
    }

    public String getAuditDialogWindow(UserContext me, int processDefinitionId,
	    int stepNo, int taskInstanceId, boolean isHiddenMenu) {
	StringBuilder sb = new StringBuilder();
	UserTaskAuditMenuModel model = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory
		.createUserTaskAuditMenu().getInstanceOfTask(taskInstanceId);
	if (model == null) {
	    model = new UserTaskAuditMenuModel();
	}

	WorkFlowStepModel stepModel = WorkFlowStepCache.getModelOfStepNo(
		processDefinitionId, stepNo);

	if (stepModel == null) {
	    stepModel = new WorkFlowStepModel();
	}

	if (!stepModel._isAudit) {
	    return "";
	}
	sb.append(
		"<a name='ACTIONSOFT-AUDIT-MENU'></a><style media=print>.Noprint{display:none;}</style><div  class=Noprint><table border='0' cellpadding='0' cellspacing='0' width='98%' bgcolor='#FFFFFF' align='center' >")
		.append("<tr><td background='../aws_skins/_def/img/bg-background.gif' style=\"background-attachment: fixed;font-size:14px;vertical-align: bottom;\" height=19> <font face='黑体，宋体' color=#CCCCCC><b>&nbsp;")
		.append(I18nRes.findValue(me.getLanguage(),
			"aws.common.efrom.audit.menu.title"))
		.append("&nbsp;&nbsp;&nbsp;&nbsp;</b></font>")
		.append("")
		.append("</td>")
		.append("")
		.append("</tr>")
		.append("<TR><TD class=actionsoftReportData>")
		.append("<table border='0' cellpadding='0' cellspacing='0' width='100%'>")
		.append("<tr>").append("<td align=left><br>");

	String auditType = "";
	WorkFlowStepModel tmpStepModel = WorkFlowStepCache.getModelOfStepNo(
		processDefinitionId, stepNo);
	Hashtable opinionList = new UnsyncHashtable();
	opinionList = WorkFlowStepOpinionCache.getListOfWorkFlowOpinion2(
		processDefinitionId, tmpStepModel._id);
	if (opinionList.size() != 0 && !isHiddenMenu) {
	    if (opinionList.size() > 0)
		sb.append("<img src='../aws_img/check.gif'><b>")
			.append(I18nRes.findValue(me.getLanguage(),
				"aws.common.efrom.audit.menu.select"))
			.append("</b></label>");
	    for (int i = 0; i < opinionList.size(); i++) {
		WorkFlowStepOpinionModel opinionModel = (WorkFlowStepOpinionModel) opinionList
			.get(new Integer(i));
		auditType = opinionModel._opinionName + "/"
			+ opinionModel._opinionType;
		sb.append(
			"&nbsp;<label style='cursor:hand;' onclick='try{auditType["
				+ i
				+ "].checked=true;}catch(e){}'><input type=\"radio\" name=\"auditType\" value=\"")
			.append(auditType).append("\" ");
		if (model.getTaskInstanceId() == 0) {
		    sb.append(opinionModel._isCheck == 1 ? "checked" : "");
		} else if (model.getAuditMenuName().equals(
			opinionModel._opinionName)) {
		    sb.append(model.getAuditType() == opinionModel._opinionType ? "checked"
			    : "");
		}

		sb.append("><I18N#" + opinionModel._opinionName
			+ ">&nbsp;</label>");
	    }
	} else {
	    auditType = " /-99";
	}
	sb.append("</td>").append("</tr>");
	sb.append("<tr>")
		.append("<td> ")
		.append("<table width=100%  style=\"display:\"  border=0 ><label><tr><td align=left>")
		.append(I18nRes.findValue(me.getLanguage(),
			"aws.common.efrom.audit.menu.memo"))
		.append("<a href=\"#\" onClick=\"resizeIndexWindows();return false;\"><img id=resizeIndexIcon  src=\"../aws_img/expandbtn2.gif\" border=\"0\"></a>")
		.append("</label></td></tr><tr height=4><td background='../aws_img/line2.gif' ></td></tr><tr id=mainIndexZone><td>")
		.append("<textarea name=opinion cols=100% rows=4 wrap='on'>")
		.append(model.getOpinion())
		.append("</textarea>")
		.append("</td>")
		.append("</tr>")
		.append("</table>")
		.append("</TD></TR>")
		.append("</table>")
		.append("<input type=hidden name=auditId id=auditId value=")
		.append(model.getId())
		.append("> \n<script>\nfunction gotoActionsoftAuditMenu(){\nwindow.location.href=window.location.href+'#ACTIONSOFT-AUDIT-MENU';\n}\n</script>\n<script>\nvar resizeIndexType=0; \nfunction resizeIndexWindows(){\nif(resizeIndexType==0){\nfrmMain.resizeIndexIcon.src=\"../aws_img/expandbtn2.gif\";\nmainIndexZone.style.display='none';\nresizeIndexType=1;\n}else{\nfrmMain.resizeIndexIcon.src=\"../aws_img/expanded_button.gif\";\nresizeIndexType=0;\nmainIndexZone.style.display='';\n}\n}\n</script>\n");

	return sb.toString();
    }

    public String getAuditAttachmentList(UserContext me, int taskId,
	    int processInstatceId) {
	String attachment = ProcessRuntimeDaoFactory.createUserTaskAuditMenu()
		.getAttachment(taskId);
	String html = UserTaskHistoryOpinionWeb.getDownLoadLink(attachment,
		processInstatceId, taskId, me, true);
	return html;
    }

    public String removeFile(int taskId, int processInstanceId, String fn) {
	int r = ProcessRuntimeDaoFactory.createUserTaskAuditMenu().removeFile(
		taskId, processInstanceId, fn);
	return "" + r;
    }
}