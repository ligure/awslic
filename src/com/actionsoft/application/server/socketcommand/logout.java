package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.login.LogoutPage;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;
/**
 * 
 * @description 重写退出类
 * @version 1.0
 * @author wangaz
 * @update 2014年6月8日 下午1:26:48
 */
public class logout implements BaseSocketCommand {

	public logout() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean executeCommand(UserContext me, Socket myProcessSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
			throws Exception {
		 if (socketCmd.equals("AQTCLETV")) {
	    	 LogoutPage web = new LogoutPage();
	         String path = myCmdArray.elementAt(3).toString();
	         myOut.write(web.logoutAction(me, path));
	         web = null;
	    }
		return false;
	}

}
