package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.commons.security.basic.web.SecurityAutoReportWeb;
import com.actionsoft.awf.form.design.web.DesignFormAutoReportWeb;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.web.OrganizationAutoReportWeb;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.web.WFDesignAutoReportWeb;

public class AutoReportSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("AutoReport_Org")) {
	    OrganizationAutoReportWeb web = new OrganizationAutoReportWeb(me);
	    myOut.write(web.getReport());
	    web = null;
	} else if (socketCmd.equals("AutoReport_Org_Build")) {
	    OrganizationAutoReportWeb web = new OrganizationAutoReportWeb(me);
	    myOut.write(web.buildReport());
	    web = null;
	} else if (socketCmd.equals("AutoReport_Security")) {
	    SecurityAutoReportWeb web = new SecurityAutoReportWeb(me);
	    myOut.write(web.getReport());
	    web = null;
	} else if (socketCmd.equals("AutoReport_Security_Build")) {
	    SecurityAutoReportWeb web = new SecurityAutoReportWeb(me);
	    myOut.write(web.buildReport());
	    web = null;
	} else if (socketCmd.equals("AutoReport_MetaData")) {
	    DesignFormAutoReportWeb web = new DesignFormAutoReportWeb(me);
	    String filterName = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getReport(groupName, filterName));
	    web = null;
	} else if (socketCmd.equals("AutoReport_MetaData_Build")) {
	    String groupName = myCmdArray.elementAt(3).toString();
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    DesignFormAutoReportWeb web = new DesignFormAutoReportWeb(me);
	    myOut.write(web.buildReport(groupName, list));
	    web = null;
	} else if (socketCmd.equals("AutoReport_WorkFlow")) {
	    WFDesignAutoReportWeb web = new WFDesignAutoReportWeb(me);
	    myOut.write(web.getReport());
	    web = null;
	} else if (socketCmd.equals("AutoReport_WorkFlow_JSON")) {
	    WFDesignAutoReportWeb web = new WFDesignAutoReportWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param = UtilCode.decode(myStr.matchValue("_param1[",
		    "]param1_"));
	    myOut.write(web.getWFMTreeJson(requestType, param));
	    web = null;
	} else if (socketCmd.equals("AutoReport_WorkFlow_Build")) {
	    String models = UtilCode.decode(myStr.matchValue("_models[",
		    "]models_"));
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    WFDesignAutoReportWeb web = new WFDesignAutoReportWeb(me);
	    myOut.write(web.buildReport(models, list));
	    web = null;
	} else if (socketCmd.equals("AutoReport_WorkFlow_Report")) {
	    String models = UtilCode.decode(myStr.matchValue("_models[",
		    "]models_"));
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    WFDesignAutoReportWeb web = new WFDesignAutoReportWeb(me);
	    myOut.write(web.getReport(models, list));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
