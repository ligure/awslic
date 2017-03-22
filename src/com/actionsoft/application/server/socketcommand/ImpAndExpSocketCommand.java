package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.form.execute.plugins.imp2exp.ImpExpUtil;
import com.actionsoft.awf.form.execute.plugins.imp2exp.excel.ExportExcel;
import com.actionsoft.awf.form.execute.plugins.imp2exp.excel.ImportExcel;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilString;

public class ImpAndExpSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("AWS_DTS_ExportExcel_FormSheetModel")) {
	    int sheetId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    int bindId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    int meId = 0;
	    if (!"".equals(myCmdArray.elementAt(5).toString()))
		meId = Integer.parseInt(myCmdArray.elementAt(5).toString());
	    myOut.write(ExportExcel.getExcelModelOfAWSSheet(me, bindId,
		    sheetId, meId));
	} else if (socketCmd.equals("AWS_DTS_ExportExcel_FormSheetData")) {
	    int bindId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    int sheetId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    int meId = Integer.parseInt(myCmdArray.elementAt(5).toString());
	    myOut.write(ExportExcel.getExcelDataOfAWSSheet(me, bindId, sheetId,
		    meId));
	} else if (socketCmd.equals("AWS_DTS_ExportExcel_FormSheeData_Upload")) {
	    String bindId = myCmdArray.elementAt(3).toString();
	    String sheetId = myCmdArray.elementAt(4).toString();
	    myOut.write(ImpExpUtil.getUpFilePage(me, "ImportSheetData", bindId
		    + "-" + sheetId, "tmp"));
	} else if (socketCmd.equals("AWS_DTS_ExportExcel_FormSheeData_Import")) {
	    String floderName = myCmdArray.elementAt(3).toString();
	    String fileName = myCmdArray.elementAt(4).toString();
	    String formType = myCmdArray.elementAt(5).toString();
	    myOut.write(ImportExcel.importExcelToFormSheet(me, floderName,
		    fileName, formType));
	} else if (socketCmd.equals("AWS_DTS_ExportExcel_FormData")) {
	    int bindId = Integer.parseInt(myCmdArray.elementAt(3).toString());
	    int formId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    myOut.write(ExportExcel.getExcelDataOfAWSForm(me, bindId, formId));
	} else if (socketCmd.equals("AWS_DTS_Excel_FormData_Upload")) {
	    String bindId = myCmdArray.elementAt(3).toString();
	    String formId = myCmdArray.elementAt(4).toString();
	    myOut.write(ImpExpUtil.getUpFilePage(me, "ImportFormData", bindId
		    + "-" + formId, "tmp"));
	} else if (socketCmd.equals("AWS_DTS_Excel_FormData_Import")) {
	    String floderName = myCmdArray.elementAt(3).toString();
	    String fileName = myCmdArray.elementAt(4).toString();
	    myOut.write(ImportExcel.importExcelToForm(me, floderName, fileName));
	} else {
	    return false;
	}
	return true;
    }
}
