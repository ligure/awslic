package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.application.server.SSOUtil;
import com.actionsoft.application.server.ShutdownServer;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.session.SessionImpl;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.execute.engine.SubWorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.helper.EmailAlertUtil;
import com.actionsoft.eai.sso.NtlmSSOWeb;
import com.actionsoft.loadbalancer.cluster.AWSMemberState;
import com.actionsoft.plugs.calendarplan.web.PlanRemindWeb;

public class APISessionSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("API_Session_Check")) {
	    String uid = myCmdArray.elementAt(2).toString();
	    String sid = myCmdArray.elementAt(3).toString();
	    String check = "0";
	    if (uid != null && !uid.equals("") && sid != null
		    && !sid.equals("")
		    && sid.substring(0, sid.indexOf("_")).equals(uid)
		    && new SessionImpl().checkSession(sid) == 1)
		check = "1";
	    myOut.write(check);
	} else if (socketCmd.equals("API_Session_Register")) {
	    String uid = myCmdArray.elementAt(2).toString();
	    String pwd = myCmdArray.elementAt(3).toString();
	    String ip = myCmdArray.elementAt(4).toString();
	    String lang = myCmdArray.elementAt(5).toString();
	    myOut.write(new SSOUtil().registerSession(uid, pwd, ip, lang));
	} else if (socketCmd.equals("API_AWS_Shutdown")) {
	    String securityCode = myCmdArray.elementAt(2).toString();
	    if (ShutdownServer.shutdown(securityCode))
		myOut.write("1");
	    else
		myOut.write("0");
	} else if (socketCmd.equals("API_Reload_User"))
	    UserCache.reload();
	else if (socketCmd.equals("API_Reload_Department"))
	    DepartmentCache.reload();
	else if (socketCmd.equals("API_Cluster_State")) {
	    String state = AWSMemberState.getInstance().getState();
	    myOut.write(state + "\n");

	} else if (socketCmd.equals("API_MyPlan_To_Remind")) {
	    String UID = myCmdArray.elementAt(2).toString();
	    try {
		me = new UserContext(UID);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    PlanRemindWeb web = new PlanRemindWeb(me);
	    myOut.write(web.getRemindWeb());
	    web = null;
	} else if (socketCmd.equals("API_MyPlan_To_Remind2")) {
	    String UID = myCmdArray.elementAt(2).toString();
	    try {
		me = new UserContext(UID);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    PlanRemindWeb web = new PlanRemindWeb(me);
	    myOut.write(web.getWeb());
	    web = null;
	} else if (socketCmd.equals("API_SystemSubProcess_Close")) {
	    String subProcessControlId = myCmdArray.elementAt(2).toString();
	    String SubProcessInstanceId = myCmdArray.elementAt(3).toString();
	    String SubProcessTaskInstanceId = myCmdArray.elementAt(4)
		    .toString();
	    String desc = UtilCode.decode(myStr.matchValue("_desc[", "]desc_"));
	    if (SubProcessInstanceId.equals(""))
		SubProcessInstanceId = "0";
	    if (SubProcessTaskInstanceId.equals(""))
		SubProcessTaskInstanceId = "0";
	    myOut.write(Integer.toString(SubWorkflowEngine.getInstance()
		    .closeSystemSubProcessInstance(
			    Integer.parseInt(subProcessControlId),
			    Integer.parseInt(SubProcessInstanceId),
			    Integer.parseInt(SubProcessTaskInstanceId), desc)));
	} else if (socketCmd.equals("API_ProcessAlert")) {
	    String ip = myCmdArray.elementAt(2).toString();
	    String uuid = myCmdArray.elementAt(3).toString();
	    String client = myCmdArray.elementAt(4).toString();
	    myOut.write(new EmailAlertUtil().access(uuid, ip, client));
	} else if (socketCmd.equals("API_SSO_Ntlm_Audit")) {
	    String user = myCmdArray.elementAt(2).toString();
	    String ip = myCmdArray.elementAt(3).toString();
	    String msg = myCmdArray.elementAt(4).toString();
	    NtlmSSOWeb.audit(user, ip, msg);
	    myOut.write("1\n");
	} else if (socketCmd.equals("API_SSO_Ntlm_ChangePWD")) {
	    String user = myCmdArray.elementAt(2).toString();
	    String newPwd = myCmdArray.elementAt(4).toString();
	    String r = NtlmSSOWeb.changeLdapPWD(user, "", newPwd);
	    myOut.write(r);
	} else {
	    return false;
	}
	return true;
    }
}
