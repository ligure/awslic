package com.app.rs.ppc.impl;

import java.util.Hashtable;
import java.util.Iterator;

import net.sf.json.JSONObject;

import com.actionsoft.awf.util.DBSql;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
import com.app.rs.ppc.constanct.PersonPositionChangeConstanct;
import com.app.rs.util.Utils;

public class PersonPositionChangeImpl {
	private Hashtable<String,String> data = null;
	private boolean result = true;
	private StringBuffer errorMsg = new StringBuffer();
	
	public JSONObject startWorkFlow(JSONObject obj) {
		JSONObject response = new JSONObject();
		//校验参数
		checkParam(obj);
		if(result){
			//转换参数
			mapParam(obj);
			data.put("ZT","1");
			String cbHigherPerson = data.get(PersonPositionChangeConstanct.PPC_KEY_CBHIGHERPERSON);
			String higherManagerUid = DBSql.getString("select userid from orguser where userno='"+ cbHigherPerson.split("-")[0] + "'", "userid");
			String HRid = data.get(PersonPositionChangeConstanct.PPC_KEY_HRLOGINNAME);
			String changeRange = data.get(PersonPositionChangeConstanct.PPC_KEY_CHANGERANGE);
			
			//得到异动后部门领导编号
			String ydhldNO=data.get(PersonPositionChangeConstanct.PPC_KEY_CAHIGHERPERSON);
			String ydhld= DBSql.getString("select userid from orguser where userno='"+ ydhldNO.split("-")[0] + "'", "userid");
			data.put("YDHLD", ydhld);
			//启动部门内员工异动审批流程
			if (changeRange.contains("部门内")) {
				createWorkflowInstanceInsideDepartment(HRid, data, higherManagerUid,ydhld);
			} 
			//启动其他类员工异动审批流程
			else {
				createWorkflowInstanceOutsideDepartment(HRid, data, higherManagerUid);
			}
		}
		response.put("isSuccess", result);
		response.put("errorMsg", errorMsg.toString());
		return response;
	}
	
	/**
	 * 启动部门内员工异动审批流程
	 * @param HRid
	 * @param ht
	 * @param higherManager
	 */
	private void createWorkflowInstanceInsideDepartment(String HRid,Hashtable<String,String> ht, String higherManager,String ydhld){
		System.out.println("****部门内异动");
		//启动流程
		int processInstanceId = 0;
		try {
			processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(PersonPositionChangeConstanct.PPCFLOW_UUID_INDEPARTMENTCHANGE,HRid, "", "部门内员工异动审批流程");
		} catch (AWSSDKException e) {
			result = false;
			errorMsg.append("AWS流程启动异常:"+e.getMessage());
			e.printStackTrace(System.err);
		}
		// 创建任务
		int[] tid = null;
		if (processInstanceId > 0) {
			try {
				//获得异动人员上级领导账号
				String sjUseridSql="select userid from orguser where USERNO=(select EXTEND2 from orguser where  userid='"+HRid+"')";
				String sjUserid=DBSql.getString(sjUseridSql, "userid");
				//判断上级领导是否是一级部门领导（一级部门副总裁、CEO、COO、CFO、CTO、包含首席、总裁办公室主任）
				String resSql="select count(*) as amount from orguser where userid='"+sjUserid+"' and (extend5 ='CEO' or extend5 like '%COO%' or extend5 like '%CFO%' or extend5 like '%CTO%' or extend5 like '%首席%' or extend5 ='总裁办公室主任' or extend5 ='董事长' or (extend5 like '%副总裁%' and 3=(select extend2 from orgdepartment where id =(select departmentid from orguser where userid='"+sjUserid+"'))))";
				System.out.println("****异动流程是否二级领导"+resSql);
				//确定初始节点号
				int amount=DBSql.getInt(resSql, "amount");
				System.out.println("****异动流程是否二级领导"+amount);
				int stepNO=2;
				if(amount>0){
					stepNO=6;
					sjUserid=ydhld;//更改为异动后上级领导
				}
				tid = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(HRid, processInstanceId, stepNO,sjUserid, "部门内员工异动审批流程");
			} catch (AWSSDKException e) {
				try {
					WorkflowInstanceAPI.getInstance().removeProcessInstance(processInstanceId);
				} catch (AWSSDKException e1) {
					e1.printStackTrace();
				}
				result = false;
				errorMsg.append("AWS流程任务创建异常:" + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		// 初始化数据
		if (tid.length > 0) {
			try {
				BOInstanceAPI.getInstance().createBOData("BO_HR_YD", ht,processInstanceId, HRid);
			} catch (AWSSDKException e) {
				try {
					for (int i = 0; i < tid.length; i++) {
						WorkflowTaskInstanceAPI.getInstance().removeProcessTaskInstance(tid[i]);
					}
					WorkflowInstanceAPI.getInstance().removeProcessInstance(processInstanceId);
				} catch (AWSSDKException e1) {
					e1.printStackTrace();
				}
				result = false;
				errorMsg.append("AWS流程数据初始化异常:" + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
			
	}
	
	/**
	 * 初始化部门间员工异动流程
	 * @param HRid
	 * @param ht
	 * @param higherManager
	 */
	private void createWorkflowInstanceOutsideDepartment(String HRid,Hashtable ht, String higherManager){
		System.out.println("****部门间异动");
		//启动流程
		int processInstanceId = 0;
		try {
			processInstanceId = WorkflowInstanceAPI.getInstance().createProcessInstance(PersonPositionChangeConstanct.PPCFLOW_UUID_OUTDEPARTMENTCHANGE,HRid, "", "部门间员工异动审批流程");
		} catch (AWSSDKException e) {
			result = false;
			errorMsg.append("AWS流程启动异常:"+e.getMessage());
			e.printStackTrace(System.err);
		}
		// 创建任务
		int[] tid = null;
		if (processInstanceId > 0) {
			try {
				//获得异动人员上级领导账号
				String sjUseridSql="select userid from orguser where USERNO=(select EXTEND2 from orguser where  userid='"+HRid+"')";
				String shUserid=DBSql.getString(sjUseridSql, "userid");
				tid = WorkflowTaskInstanceAPI.getInstance().createProcessTaskInstance(HRid, processInstanceId, 2,shUserid, "部门间员工异动审批流程");
			} catch (AWSSDKException e) {
				try {
					WorkflowInstanceAPI.getInstance().removeProcessInstance(processInstanceId);
				} catch (AWSSDKException e1) {
					e1.printStackTrace();
				}
				result = false;
				errorMsg.append("AWS流程任务创建异常:" + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		// 初始化数据
		if (tid.length > 0) {
			try {
				BOInstanceAPI.getInstance().createBOData("BO_HR_YD", ht,processInstanceId, HRid);
			} catch (AWSSDKException e) {
				try {
					for (int i = 0; i < tid.length; i++) {
						WorkflowTaskInstanceAPI.getInstance().removeProcessTaskInstance(tid[i]);
					}
					WorkflowInstanceAPI.getInstance().removeProcessInstance(processInstanceId);
				} catch (AWSSDKException e1) {
					e1.printStackTrace();
				}
				result = false;
				errorMsg.append("AWS流程数据初始化异常:" + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * 校验参数有效性
	 * @param params 参数集合
	 * @return
	 */
	private void checkParam(JSONObject params){
		//异动前上级主管
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CBHIGHERPERSON)){
			errorMsg.append("missing param:[CBHIGHERPERSON]!\n");
		}else{
			String cbHigherPerson = params.getString(PersonPositionChangeConstanct.PPC_KEY_CBHIGHERPERSON);
			if(cbHigherPerson == null || cbHigherPerson.length() == 0){
				errorMsg.append("Invalid param:[CBHIGHERPERSON] cannot be empty!\n");
			}else{
				String cbHigherPersonNo = cbHigherPerson.split("-")[0];
				String userId = Utils.getUserIdByNo(cbHigherPersonNo);
				if(!Utils.checkUser(userId)){
					errorMsg.append("Invalid param:[CBHIGHERPERSON] cannot contains a Effective AWS account!\n");
				}
			}
		}
		//HR登录用户名
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_HRLOGINNAME)){
			errorMsg.append("missing param:[HRLOGINNAME]!\n");
		}else{
			String hrLoginName = params.getString(PersonPositionChangeConstanct.PPC_KEY_HRLOGINNAME);
			if(hrLoginName == null || hrLoginName.length() == 0){
				errorMsg.append("Invalid param:[HRLOGINNAME] cannot be empty!\n");
			}else{
				if(!Utils.checkUser(hrLoginName)){
					errorMsg.append("Invalid param:[HRLOGINNAME] cannot be a Effective AWS account!\n");
				}
			}
		}
		//员工姓名
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_PERSONNAME)){errorMsg.append("missing param:[PERSONNAME]!\n");}
		//员工编号
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_PERSONNO)){
			errorMsg.append("missing param:[PERSONNO]!\n");
		}else{
			String personNo = params.getString(PersonPositionChangeConstanct.PPC_KEY_PERSONNO);
			if(personNo == null || personNo.length() == 0){
				errorMsg.append("Invalid param:[PERSONNO] cannot be empty!\n");
			}
		}
		//入职日期
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_JOINDATE)){errorMsg.append("missing param:[JOINDATE]!\n");}
		//异动类型
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CHANGETYPE)){
			errorMsg.append("missing param:[CHANGETYPE]!\n");
		}else{
			String changeType = params.getString(PersonPositionChangeConstanct.PPC_KEY_CHANGETYPE);
			if(changeType == null || changeType.length() == 0){
				errorMsg.append("Invalid param:[CHANGETYPE] cannot be empty!\n");
			}
		}
		//异动类型代码
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CHANGETYPECODE)){
			errorMsg.append("missing param:[CHANGETYPECODE]!\n");
		}else{
			String changeTypeCode = params.getString(PersonPositionChangeConstanct.PPC_KEY_CHANGETYPECODE);
			if(changeTypeCode == null || changeTypeCode.length() == 0){
				errorMsg.append("Invalid param:[CHANGETYPECODE] cannot be empty!\n");
			}
		}
		//异动范围
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CHANGERANGE)){
			errorMsg.append("missing param:[CHANGERANGE]!\n");
		}else{
			String changeRange = params.getString(PersonPositionChangeConstanct.PPC_KEY_CHANGERANGE);
			if(changeRange == null || changeRange.length() == 0){
				errorMsg.append("Invalid param:[CHANGERANGE] cannot be empty!\n");
			}
		}
		//异动范围代码
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CHANGERANGECODE)){
			errorMsg.append("missing param:[CHANGERANGECODE]!\n");
		}else{
			String changeRangeCode = params.getString(PersonPositionChangeConstanct.PPC_KEY_CHANGERANGECODE);
			if(changeRangeCode == null || changeRangeCode.length() == 0){
				errorMsg.append("Invalid param:[CHANGERANGECODE] cannot be empty!\n");
			}
		}
		//异动原因
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CHANGEREASON)){errorMsg.append("missing param:[CHANGEREASON]!\n");}
		//异动原因代码
		if(!params.containsKey(PersonPositionChangeConstanct.PPC_KEY_CHANGEREASONCODE)){errorMsg.append("missing param:[CHANGEREASONCODE]!\n");}
		
		
		if(errorMsg.length() > 0 ){
			result = false;
		}
	}
	
	/**
	 * 映射参数到数据
	 * @param params 参数集合
	 * @return
	 */
	private void mapParam(JSONObject params){
		data = new Hashtable<String,String>();
		Iterator it = params.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            String param = params.getString(key);
            data.put(key, returnNullStr(param));
        }
	}
	
	/**
	 * 处理空字符串
	 * @param str
	 * @return
	 */
	private String returnNullStr(String str){
		if(str == null || str.trim().length() == 0){
			return "";
		}else{
			return str;
		}
	}
	
	
}
