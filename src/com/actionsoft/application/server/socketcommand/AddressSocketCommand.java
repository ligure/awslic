package com.actionsoft.application.server.socketcommand;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.addresstree.EmailAddressTreeWeb;
import com.actionsoft.awf.organization.addresstree.OrganizationAddressTreeWeb;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;
/**
 * 
 * @description 修改平台自带的地址簿方法，将组织树默认不显示。
 * @version 1.0
 * @author wangaz
 * @update 2014-3-18 下午06:06:27
 */
public class AddressSocketCommand
  implements BaseSocketCommand
{
  public boolean executeCommand(UserContext me, Socket myProcessSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
    throws Exception
  {
	  /**
	   * 地址簿onclick事件调用的cmd
	   */
    if (socketCmd.equals("Address_Inner_Open")) {
      OrganizationAddressTreeWeb web = new OrganizationAddressTreeWeb(me);
      String groupstyle = myCmdArray.elementAt(3).toString();
      String groupstylevalue = myCmdArray.elementAt(4).toString();
      String mail_target = myCmdArray.elementAt(5).toString();
      String grid = myCmdArray.elementAt(6).toString();
      String mail_user = UtilCode.decode(myStr.matchValue("_mail_user[", "]mail_user_"));
      myOut.write(web.getAddressWindow(Integer.parseInt(groupstyle), groupstylevalue, mail_target, mail_user, grid));
      web = null;
    }
    else if (socketCmd.equals("Address_Inner_Company_Open")) {
      OrganizationAddressTreeWeb web = new OrganizationAddressTreeWeb(me);
      String grid = myCmdArray.elementAt(3).toString();
      myOut.write(web.showCompany(0, grid));
      web = null;
    }
    else if (socketCmd.equals("Address_Inner_Account_Open2")) {
      OrganizationAddressTreeWeb web = new OrganizationAddressTreeWeb(me);
      String p1 = myCmdArray.elementAt(3).toString();
      String p2 = myCmdArray.elementAt(4).toString();
      String p3 = myCmdArray.elementAt(5).toString();

      if ((p2 == null) || (p2.equals(""))) {
        p2 = "0";
      }

      if ((p3 == null) || (p3.equals(""))) {
        p3 = "0";
      }

      myOut.write(web.getAddressTree(Integer.parseInt(p1), Integer.parseInt(p2), "0", !p3.equals("0")));
      web = null;
    } else if (socketCmd.equals("Address_Tree_Findfilter")) {
      OrganizationAddressTreeWeb web = new OrganizationAddressTreeWeb(me);
      String companyId = myCmdArray.elementAt(3).toString();
      if (companyId.equals("")) {
        companyId = "0";
      }
      String filterValue = myCmdArray.elementAt(4).toString();

      String p3 = myCmdArray.elementAt(5).toString();
      if ((p3 == null) || (p3.equals(""))) {
        p3 = "0";
      }
      String groupStyle = myCmdArray.elementAt(6).toString();
      if (groupStyle.trim().length() == 0)
        groupStyle = "0";
      myOut.write(web.getAddressTree(Integer.parseInt(companyId), Integer.parseInt(groupStyle), filterValue, !p3.equals("0")));
      web = null;
    }
    else if (socketCmd.equals("Address_Inner_Target_Open")) {
      OrganizationAddressTreeWeb web = new OrganizationAddressTreeWeb(me);
      String p1 = myCmdArray.elementAt(3).toString();
      String p3 = myCmdArray.elementAt(4).toString();
      String p2 = UtilCode.decode(myStr.matchValue("_p2[", "]p2_"));
      myOut.write(web.showMailTarget(p1, p2, p3));
      web = null;
    }
    else if (socketCmd.equals("Address_Inner_Tree_JSONDATE")) {
      OrganizationAddressTreeWeb web = new OrganizationAddressTreeWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param1 = myCmdArray.elementAt(4).toString();
      String param2 = myCmdArray.elementAt(5).toString();
      String param3 = myCmdArray.elementAt(6).toString();
      String param4 = myCmdArray.elementAt(7).toString();
      param4 = param4.trim().length() == 0 ? "0" : param4;

      if ("newAddress".equals(param3)) {
        EmailAddressTreeWeb web2 = new EmailAddressTreeWeb(me);
        myOut.write(web2.getNewJsonTreeOfORG(requestType, param1, param2, param4));
      }
      else if ("personAddress".equals(requestType)) {
        myOut.write(web.getJsonTreeOfORG(param1));
      } else {
        myOut.write(web.getJsonTreeOfORG(requestType, param1, param2, param3));
      }
      web = null;
    } else {
      return false;
    }

    return true;
  }
}