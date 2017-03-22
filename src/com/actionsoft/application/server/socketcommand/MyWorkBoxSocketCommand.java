package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.commons.security.ac.util.AccessControlUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.execute.workbox.web.MyWorklistCardWeb;
import com.actionsoft.awf.workflow.execute.workbox.web.TasklistTabWeb;
import com.actionsoft.awf.workflow.execute.workbox2.MyWorkBoxExcuteAbs;
import com.actionsoft.awf.workflow.execute.workbox2.cache.MyWorkBoxConfig;
import com.actionsoft.awf.workflow.execute.workbox2.imp.DefaultMyWorkBoxExcuteImp;
import com.actionsoft.awf.workflow.execute.workbox2.model.MyWorkBoxConfigModel;
import com.actionsoft.i18n.I18nRes;

public class MyWorkBoxSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("My_WorkBox2_Main")) {
	    MyWorklistCardWeb web = new MyWorklistCardWeb(me);
	    String pageType = myCmdArray.elementAt(3).toString();
	    if (pageType == null || pageType.equals(""))
		pageType = I18nRes.findValue(me.getLanguage(),
			"aws.common.worklist.tab.pagetype.default");
	    myOut.write(web.getMyWorkBoxCard(Integer.parseInt(pageType)));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_Worklist")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String worklistType = myCmdArray.elementAt(3).toString();
	    String listType = myCmdArray.elementAt(4).toString();
	    String dateRange = UtilCode.decode(myStr.matchValue("_dateRange[",
		    "]dateRange_"));
	    if (worklistType == null || worklistType.equals(""))
		worklistType = "0";
	    if (listType == null || listType.equals(""))
		listType = "0";
	    myOut.write(web.getMyWorklist(Integer.parseInt(worklistType),
		    Integer.parseInt(listType), dateRange));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_FindFinish")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String owner = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String dateFrom = myCmdArray.elementAt(6).toString();
	    String dateTo = myCmdArray.elementAt(7).toString();
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "0";
	    myOut.write(web.getMyFinishQuery(instanceId, owner, title,
		    dateFrom, dateTo, Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_FindHistoryMessage")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String owner = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String dateFrom = myCmdArray.elementAt(6).toString();
	    String dateTo = myCmdArray.elementAt(7).toString();
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "0";
	    myOut.write(web.getMyHistoryMessage(instanceId, owner, title,
		    dateFrom, dateTo, Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_CommisionTaskList")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String pageNow = myCmdArray.elementAt(3).toString();
	    String policyId = myCmdArray.elementAt(4).toString();
	    String operator = myCmdArray.elementAt(5).toString();
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "0";
	    myOut.write(web.getCommisionTaskList(Integer.parseInt(pageNow),
		    Integer.parseInt(policyId), operator));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_FinishUndo")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String opinion = UtilCode.decode(myStr.matchValue("_opinion[",
		    "]opinion_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "0";
	    myOut.write(web.undoMyFinishTask2(Integer.parseInt(instanceId),
		    Integer.parseInt(taskId), opinion,
		    Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_Worklist_ViewOvertime")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String taskId = myCmdArray.elementAt(3).toString();
	    if (taskId == null || taskId.equals(""))
		taskId = "0";
	    myOut.write(web.getOvertimePage(Integer.parseInt(taskId)));
	} else if (socketCmd.equals("My_WorkBoxCard_FindSend")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String flowState = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String dateFrom = myCmdArray.elementAt(6).toString();
	    String dateTo = myCmdArray.elementAt(7).toString();
	    String title = UtilCode.decode(myStr.matchValue("_title[",
		    "]title_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "0";
	    myOut.write(web.getMySendQuery(instanceId, flowState, title,
		    dateFrom, dateTo, Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd.equals("My_WorkBoxCard_BatchTaskWin")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String taskIds = UtilCode.decode(myStr.matchValue("_taskIds[",
		    "]taskIds_"));
	    myOut.write(web.getBatchTaskWin(taskIds));
	} else if (socketCmd.equals("My_WorkBoxCard_BatchTaskExec")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String taskIds = UtilCode.decode(myStr.matchValue("_taskIds[",
		    "]taskIds_"));
	    String params = UtilCode.decode(myStr.matchValue("_params[",
		    "]params_"));
	    myOut.write(params);
	} else if (socketCmd.equals("My_Apply_CommisionList")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    myOut.write(web.getMyApplyCommisionList());
	} else if (socketCmd.equals("My_Apply_Commision_XML_Data")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    myOut.write(web.getMyApplyCommisionXmlData());
	} else if (socketCmd.equals("My_Apply_Commision_Cancel")) {
	    TasklistTabWeb web = new TasklistTabWeb(me);
	    String instanceids = UtilCode.decode(myStr.matchValue(
		    "_instanceid[", "]instanceid_"));
	    myOut.write(web.cancelCommision(instanceids));
	} else if (socketCmd.equals("My_WorkBoxCard")) {
	    String pageType = myCmdArray.elementAt(3).toString();
	    if (pageType == null || pageType.equals(""))
		pageType = "0";
	    int worklistType = Integer.valueOf(pageType).intValue();
	    Iterator iterator = MyWorkBoxConfig.getMyWorkBoxConfig().iterator();
	    MyWorkBoxExcuteAbs myWorkBoxWebAbs = null;
	    while (iterator.hasNext()) {
		MyWorkBoxConfigModel model = (MyWorkBoxConfigModel) iterator
			.next();
		if (AccessControlUtil.accessControlCheck(me,
			"BO_AWS_WORKBOX_P", Integer.toString(model.getId()),
			"R")) {
		    Constructor cons = model.getImpConstructor();
		    if (cons != null) {
			Object[] params = { me };
			myWorkBoxWebAbs = (MyWorkBoxExcuteAbs) cons
				.newInstance(params);
			break;
		    }
		}
	    }
	    if (myWorkBoxWebAbs == null)
		myWorkBoxWebAbs = new DefaultMyWorkBoxExcuteImp(me);
	    myOut.write(myWorkBoxWebAbs.getWorkBoxWeb(me, worklistType));
	    myWorkBoxWebAbs = null;
	} else if (socketCmd.equals("My_WorkBox2_Card")) {
	    DefaultMyWorkBoxExcuteImp web = new DefaultMyWorkBoxExcuteImp(me);
	    String worklistType = myCmdArray.elementAt(3).toString();
	    String group = myCmdArray.elementAt(4).toString();
	    String condition = UtilCode.decode(myStr.matchValue("_condition[",
		    "]condition_"));
	    worklistType = worklistType != null
		    && worklistType.trim().length() != 0 ? worklistType : "0";
	    myOut.write(web.getWorkListCard(Integer.parseInt(worklistType),
		    group, condition));
	    web = null;
	} else if (socketCmd.equals("My_WorkBox2_WorkList_DataXml")) {
	    String worklistType = myCmdArray.elementAt(3).toString();
	    String start = myCmdArray.elementAt(4).toString();
	    String limit = myCmdArray.elementAt(5).toString();
	    String condition = UtilCode.decode(myStr.matchValue("_condition[",
		    "]condition_"));
	    DefaultMyWorkBoxExcuteImp web = new DefaultMyWorkBoxExcuteImp(me);
	    worklistType = worklistType != null
		    && worklistType.trim().length() != 0 ? worklistType : "0";
	    start = start != null && start.trim().length() != 0 ? start : "0";
	    limit = limit != null && limit.trim().length() != 0 ? limit : "0";
	    myOut.write(web.getWorkListXMLData(Integer.parseInt(worklistType),
		    Integer.parseInt(start), Integer.parseInt(limit), condition));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
