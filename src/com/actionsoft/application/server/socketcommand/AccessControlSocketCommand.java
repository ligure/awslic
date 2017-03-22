package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.commons.security.ac.web.AccessControlWeb;
import com.actionsoft.awf.organization.addresstree.ACAddressTreeWeb;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;

public class AccessControlSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("AC_Action_Open")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String resourceId = myCmdArray.elementAt(3).toString();
	    if (resourceId == null || resourceId.equals(""))
		resourceId = "0";
	    String tableName = myCmdArray.elementAt(4).toString();
	    String htmlStr = web.getWeb(resourceId, tableName, "", true, me
		    .getCompanyModel().getId(), "", 0, "");
	    myOut.write(htmlStr);
	    web = null;
	} else if (socketCmd.equals("API_Action_Open2")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String sid = myCmdArray.elementAt(2).toString();
	    String userid = sid.substring(0, sid.indexOf("_"));
	    UserContext uc = null;
	    try {
		uc = new UserContext(userid);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    int uid = uc.getID();
	    UserModel userModel = (UserModel) UserCache.getModel(uid);
	    int dept = userModel.getDepartmentId();
	    DepartmentModel dm = (DepartmentModel) DepartmentCache
		    .getModel(dept);
	    String htmlStr = web.getAUWeb(dm.getCompanyId(), sid, uid, dept);
	    myOut.write(htmlStr);
	    web = null;
	} else if (socketCmd.equals("AccessControl_Company_Open")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String resourceId = myCmdArray.elementAt(3).toString();
	    if (resourceId == null || resourceId.equals(""))
		resourceId = "0";
	    String tableName = myCmdArray.elementAt(4).toString();
	    String companyId = myCmdArray.elementAt(5).toString();
	    String acType = myCmdArray.elementAt(6).toString();
	    String orgType = myCmdArray.elementAt(7).toString();
	    if (orgType == null || orgType.equals(""))
		orgType = "0";
	    myOut.write(web.getWeb(resourceId, tableName, acType, true,
		    Integer.parseInt(companyId), "", Integer.parseInt(orgType),
		    ""));
	    web = null;
	} else if (socketCmd.equals("AccessControl_Ac_Remove")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String resourceId = myCmdArray.elementAt(3).toString();
	    if (resourceId == null || resourceId.equals(""))
		resourceId = "0";
	    String tableName = myCmdArray.elementAt(4).toString();
	    String companyId = myCmdArray.elementAt(5).toString();
	    String acType = myCmdArray.elementAt(6).toString();
	    String orgType = myCmdArray.elementAt(7).toString();
	    if (orgType == null || orgType.equals(""))
		orgType = "0";
	    String id = myCmdArray.elementAt(8).toString();
	    if (id == null || id.equals(""))
		id = "0";
	    myOut.write(web.removeACResource(resourceId, tableName, acType,
		    true, Integer.parseInt(companyId), "",
		    Integer.parseInt(orgType), Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("AccessControl_Ac_Search")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String resourceId = myCmdArray.elementAt(3).toString();
	    if (resourceId == null || resourceId.equals(""))
		resourceId = "0";
	    String tableName = myCmdArray.elementAt(4).toString();
	    String companyId = myCmdArray.elementAt(5).toString();
	    String acType = myCmdArray.elementAt(6).toString();
	    String orgType = myCmdArray.elementAt(7).toString();
	    if (orgType == null || orgType.equals(""))
		orgType = "0";
	    String search = UtilCode.decode(myStr.matchValue("_search[",
		    "]search_"));
	    myOut.write(web.getWeb(resourceId, tableName, acType, true,
		    Integer.parseInt(companyId), "", Integer.parseInt(orgType),
		    search));
	    web = null;
	} else if (socketCmd.equals("AccessControl_Create")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String resourceId = myCmdArray.elementAt(3).toString();
	    String tableName = myCmdArray.elementAt(4).toString();
	    String acType = myCmdArray.elementAt(5).toString();
	    String companyId = myCmdArray.elementAt(6).toString();
	    String orgType = myCmdArray.elementAt(7).toString();
	    String acOrg = UtilCode.decode(myStr.matchValue("_acOrg[",
		    "]acOrg_"));
	    myOut.write(web.appendACResource(resourceId, tableName, acOrg,
		    acType, Integer.parseInt(companyId),
		    Integer.parseInt(orgType)));
	    web = null;
	} else if (socketCmd.equals("AccessControl_Change_Type")) {
	    AccessControlWeb web = new AccessControlWeb(me);
	    String resourceId = myCmdArray.elementAt(3).toString();
	    String tableName = myCmdArray.elementAt(4).toString();
	    String acType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.getWeb(resourceId, tableName, acType, true, me
		    .getCompanyModel().getId(), "", 0, ""));
	    web = null;
	} else if (socketCmd.equals("AccessControl_TreeLoader_JSONDATE")) {
	    ACAddressTreeWeb web = new ACAddressTreeWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    String search = UtilCode.decode(myStr.matchValue("_search[",
		    "]search_"));
	    String tableName = UtilCode.decode(myStr.matchValue(
		    "_resourceTableName[", "]resourceTableName_"));
	    new AccessControlWeb(me);
	    myOut.write(web.getJsonTreeOfORG(requestType, param1, param2,
		    param3, search, AccessControlWeb.ignoreFilter(tableName)));
	    web = null;
	} else if (socketCmd.equals("AccessControl_TreeLoader_JSONDATE2")) {
	    ACAddressTreeWeb web = new ACAddressTreeWeb(me);
	    String requestType = myCmdArray.elementAt(3).toString();
	    String param1 = myCmdArray.elementAt(4).toString();
	    String param2 = myCmdArray.elementAt(5).toString();
	    String param3 = myCmdArray.elementAt(6).toString();
	    String htmlStr = web.getJsonTreeOfORG(requestType, param1, param2,
		    param3, "", true);
	    htmlStr = htmlStr.replaceAll("x-tree-node-department",
		    "treeIconDirectory2");
	    htmlStr = htmlStr.replaceAll("'checked':false,", " ");
	    myOut.write(htmlStr);
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
