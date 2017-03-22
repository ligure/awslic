/*
 * Copyright(C)2001-2012 Actionsoft Co.,Ltd
 * AWS(Actionsoft workflow suite) BPM(Business Process Management) PLATFORM Source code 
 * AWS is a application middleware for BPM System

  
 * 本软件工程编译的二进制文件及源码版权归北京炎黄盈动科技发展有限责任公司所有，
 * 受中国国家版权局备案及相关法律保护，未经书面法律许可，任何个人或组织都不得泄漏、
 * 传播此源码文件的全部或部分文件，不得对编译文件进行逆向工程，违者必究。

 * $$本源码是炎黄盈动最高保密级别的文件$$
 * 
 * http://www.actionsoft.com.cn
 * 
 */

package com.actionsoft.awf.workflow.design.flex.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.actionsoft.application.logging.AuditLogger;
import com.actionsoft.application.logging.model.Action;
import com.actionsoft.application.logging.model.AuditObj;
import com.actionsoft.application.logging.model.Catalog;
import com.actionsoft.application.logging.model.Channel;
import com.actionsoft.application.logging.model.Level;
import com.actionsoft.application.server.Console;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.commons.security.mgtgrade.util.GradeSecurityUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.EncryptFileUtil;
import com.actionsoft.awf.util.UtilFile;
import com.actionsoft.awf.workflow.design.cache.WorkFlowCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowLaneCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepOpinionCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepRuleCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowSubCache;
import com.actionsoft.awf.workflow.design.dao.WFDesignDaoFactory;
import com.actionsoft.awf.workflow.design.flex.constant.AWSConst;
import com.actionsoft.awf.workflow.design.flex.constant.FPDConst;
import com.actionsoft.awf.workflow.design.model.WorkFlowLaneModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepOpinionModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepRuleModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowSubModel;
import com.actionsoft.deploy.transfer.uuid.UIDNotSupportException;
import com.actionsoft.deploy.transfer.uuid.UUID;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;

/**
 * @author zhanghf
 * 
 */
public class AWSFPDWorkFlowTransact extends ActionsoftWeb {
	/**导出XPDL 开始*/
	protected AuditLogger logger = AuditLogger.getLogger(Channel.SYSTEM, Catalog.MODEL, AuditObj.MODEL_WF);
	/**导出XPDL  结束*/
	private Object _fileWRObj = new Object();

	private final String mainPath = "fpd/";
	
	private final String subPath = "process_tobe/";
	
	public UserContext uc;
	
	public AWSFPDWorkFlowTransact(UserContext uc) {
		super(uc);
		this.uc = uc;
	}
	
	public String getFPDPortal(int wfID, int editable) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfID);
		return getFPDPortal(wfModel, editable);
	}
	
	public String getFPDPortal(String wfUUID, int editable) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
		return getFPDPortal(wfModel, editable);
	}
	
	public String getFPDPortal(int wfID, int editable, String scheme) {
		return getFPDPortal(wfID, editable, scheme, false);
	}
	
	public String getFPDPortal(int wfID, int editable, String scheme, boolean debug) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfID);
		return getFPDPortal(wfModel, editable, scheme, debug);
	}
	
	public String getFPDPortal(String wfUUID, int editable, String scheme) {
		return getFPDPortal(wfUUID, editable, scheme, false);
	}
	
	public String getFPDPortal(String wfUUID, int editable, String scheme, boolean debug) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
		return getFPDPortal(wfModel, editable, scheme, debug);
	}
	
	private String getFPDPortal(WorkFlowModel wfModel, int editable) {
		//2011.04.21 modify by zhanghf
		//增加scheme方案选项，针对不同方案，确定画布的展示
		return getFPDPortal(wfModel, editable, "awsdefault", false);
	}
	
	/**
	 * 输出一个流程的流程设计器页面
	 * @param wfID 流程ID
	 * @param editable 画布状态，1:可编辑，0:画布只读
	 * @param scheme 方案选项，针对不同方案，确定画布的展示
	 * @return
	 * @author Actionsoft
	 */
	private String getFPDPortal(WorkFlowModel wfModel, int editable, String scheme, boolean debug) {
		String html = "WorkFlow_Flex_Process_Designer.htm";
		Map  trace=new HashMap();
		if (wfModel == null) {
			/********************初始化布局*************************/
			trace.put("错误信息[error]","流程模型读取错误，请检查是否存在");
			logger.log("当前流程    布局初始化失败", Action.CREATE, trace, Level.ERROR);
			/*********************初始化布局*************************/
			return new AlertWindow().getWarningWindow(I18nRes.findValue(super.getContext().getLanguage(), "流程模型读取错误，请检查是否存在"));
		}
		trace.put("流程ID [workflowID]",String.valueOf(wfModel._id));
		trace.put("流程名称 [workflowname]",wfModel._flowName);
		
		if (scheme.toLowerCase().equals("awsdefault")) {
			// 判断流程管理权限
			boolean isSuperMaster = GradeSecurityUtil.isSuperMaster(super.getContext().getUID());
			// 是否为超级管理员
			if (!isSuperMaster) {
				if (wfModel != null && (wfModel._flowMaster + " ").indexOf(super.getContext().getUID() + " ") == -1) {
					/************************************初始化布局****************************************/
					trace.put("警告信息[warning]","您没有该流程的设计权限，请与系统管理员联系");
					logger.log("["+wfModel._flowName+"]流程   布局初始化失败", Action.CREATE, trace, Level.WARN);
					/************************************初始化布局****************************************/
					return new AlertWindow().getWarningWindow(I18nRes.findValue(super.getContext().getLanguage(), "您没有该流程的设计权限，请与系统管理员联系"));
				}
			}
		}
		
		int hasWFXML = 0;
		String xml = readWorkFlowXPDL(wfModel._uuid);
		if (!xml.equals("0")) {
			hasWFXML = 1;
		}
		/************************************初始化布局****************************************/
		logger.log("["+wfModel._flowName+"]流程   布局初始化成功", Action.CREATE, trace, Level.INFO);
		/************************************初始化布局****************************************/
		Hashtable hashTags = new com.actionsoft.awf.util.UnsyncHashtable();
		hashTags.put("sid", getSIDFlag());
		hashTags.put("sessionId", getContext().getSessionId());
		hashTags.put("userid", getContext().getUID());
		String ln = getContext().getLanguage();
		ln = ln.equals("cn")?"zh_CN":ln;
		ln = ln.equals("en")?"en_US":ln;
		ln = ln.equals("big5")?"zh_TW":ln;
		hashTags.put("Language", ln);
		hashTags.put("WorkFlowID", String.valueOf(wfModel._id));
		hashTags.put("WorkFlowUUID", wfModel._uuid);
		hashTags.put("WorkFlowName", wfModel._flowStyle + " &raquo; " +wfModel._flowName);
		hashTags.put("WorkFlowEName",wfModel._flowName);
		hashTags.put("WorkFlowType", String.valueOf(wfModel._workFlowType));
		hashTags.put("MainCanvasEditable", String.valueOf(editable));
		hashTags.put("WFVersion", String.valueOf(wfModel._version_flag));
		hashTags.put("hasWFXML", String.valueOf(hasWFXML));
		hashTags.put("groupName", wfModel._groupName);
		hashTags.put("XML", xml);
		hashTags.put("flowName", I18nRes.findValue(getContext().getLanguage(),wfModel._flowStyle));
		hashTags.put("scheme", scheme);
		if (debug) {
			hashTags.put("path", "../aws_plus/flex/FlexProcessDesignerDebug/");//debug
		} else {
			hashTags.put("path", "../aws_plus/flex/FlexProcessDesigner/");
		}
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel(html), hashTags);
	}
	
	public String getWorkFlowXMLData(int wfId) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfId);
		if (wfModel == null) {
			return "";
		}
		return getWorkFlowXMLData(wfModel._uuid);
	}
	
	public String getWorkFlowXMLData(String wfUUID) {
		
		StringBuilder xml = new StringBuilder();
		//流程数据
		xml.append(getWorkFlowXMLItemData(wfUUID, false));
		//流程节点数据
		xml.append(getWorkFlowStepsXMLItemData(wfUUID, false));
		//流程节点审核数据和规则数据
		xml.append(getWorkFlowStepOpinionsAndRulesXMLItemData(wfUUID, false));
		
		return getXMLData(xml.toString());
	}
	
	public String getWorkFlowXMLItemData(int wfId, boolean isFullXMLData) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfId);
		return getWorkFlowXMLItemData(wfModel, isFullXMLData);
	}
	
	public String getWorkFlowXMLItemData(String wfUUID, boolean isFullXMLData) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
		return getWorkFlowXMLItemData(wfModel, isFullXMLData);
	}
	
	private String getWorkFlowXMLItemData(WorkFlowModel wfModel, boolean isFullXMLData) {
		if (wfModel == null) {
			return "-1";
		}
		HashMap wfMap = new HashMap();
		wfMap.put(AWSConst.AWSID, String.valueOf(wfModel._id));
		wfMap.put(WorkFlowModel.FIELD_UUID, wfModel._uuid);
		wfMap.put(WorkFlowModel.FIELD_FLOW_NAME, wfModel._flowName);
		wfMap.put("TYPE", FPDConst.POOL_TYPE);

		if (isFullXMLData) {
			return getXMLData(getXMLItemData(wfMap));
		} else {
			return getXMLItemData(wfMap);
		}
	}
	
	public String getWorkFlowStepsXMLItemData(int wfId, boolean isFullXMLData) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfId);
		return getWorkFlowStepsXMLItemData(wfModel, isFullXMLData);
	}
	
	public String getWorkFlowStepsXMLItemData(String wfUUID, boolean isFullXMLData) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
		return getWorkFlowStepsXMLItemData(wfModel, isFullXMLData);
	}
	
	private String getWorkFlowStepsXMLItemData(WorkFlowModel wfModel, boolean isFullXMLData) {
		if (wfModel == null) {
			return "";
		}
		//从LANECache中读取该流程的泳道模型，如果泳道模型数据为空，则表示FPD第一次读取流程模型，
		//需要根据节点创建泳道信息，一个节点对应一个泳道信息
		//1.读取泳道信息
		Hashtable lanes = WorkFlowLaneCache.getListOfWorkFlow(wfModel._id);
		
		Map steps = WorkFlowStepCache.getListOfWorkFlow(wfModel._id);
		
		//2.如果没有泳道信息，创建
		if (lanes.size() == 0 || lanes.size() != steps.size()) {
			for (int i=0; i<steps.size(); i++) {
				WorkFlowStepModel sModel = (WorkFlowStepModel) steps.get(Integer.valueOf(i));
				
				WorkFlowLaneModel lane = null;
				lane = (WorkFlowLaneModel) WorkFlowLaneCache.getModelByWorkFlowIdAndLaneName(wfModel._uuid, sModel._stepName);
				if (lane == null) {
					lane = new WorkFlowLaneModel();
					lane._flowId = sModel._flowId;
					lane._flowUUID = sModel._flowUUID;
					lane._laneName = sModel._stepName;
					int r = WFDesignDaoFactory.createWorkFlowLane().create(lane);
					if (r == -3) {//如果存在重名的泳道，则读取该泳道信息
						lane = (WorkFlowLaneModel) WorkFlowLaneCache.getModelByWorkFlowIdAndLaneName(lane._flowUUID, lane._laneName);
					}
					sModel._laneId = lane._id;
					sModel._laneUUID = lane._uuid;
					WFDesignDaoFactory.createWorkFlowStep().store(sModel);
				}
				
			}
		} else if (lanes.size() == steps.size()) {
			for (int i=0; i<steps.size(); i++) {
				WorkFlowStepModel sModel = (WorkFlowStepModel) steps.get(Integer.valueOf(i));
				WorkFlowLaneModel lane = null;
				lane = (WorkFlowLaneModel) WorkFlowLaneCache.getModelByWorkFlowIdAndLaneName(wfModel._uuid, sModel._stepName);
				//使用节点命名的泳道如果存在，且该节点又不在泳道里时，移动节点到该泳道
				if (lane != null && !lane._uuid.equals(sModel._laneUUID)) {
					sModel._laneId = lane._id;
					sModel._laneUUID = lane._uuid;
					WFDesignDaoFactory.createWorkFlowStep().store(sModel);
				}
			}
		}
		
		steps = WorkFlowStepCache.getListOfWorkFlow(wfModel._id);
		
		lanes = WorkFlowLaneCache.getListOfWorkFlow(wfModel._id);
		
		HashMap map = new HashMap();
		
		StringBuilder sbLane = new StringBuilder();//泳道数据
		StringBuilder sb = new StringBuilder();//节点数据
		StringBuilder st = new StringBuilder();//规则中出现的进入子流程和指定办理者的节点

		WorkFlowLaneModel firstLane = new WorkFlowLaneModel();
		if (lanes != null) {
			//拼接泳道信息的JSON
			for (int i=0; i<lanes.size(); i++) {
				WorkFlowLaneModel lModel = (WorkFlowLaneModel) lanes.get(Integer.valueOf(i));
				if (i==0) {
					firstLane = lModel;
				}
				//组织泳道数据
				map = new HashMap();
				map.put(AWSConst.AWSID, String.valueOf(lModel._id));
				map.put(WorkFlowLaneModel.FIELD_UUID, lModel._uuid);
				map.put(WorkFlowLaneModel.FIELD_FLOW_UUID, lModel._flowUUID);
				map.put(WorkFlowLaneModel.FIELD_LANE_NAME, lModel._laneName);
				map.put(AWSConst.TYPE, FPDConst.LANE_TYPE);
				sbLane.append(getXMLItemData(map));
			}
		}

		if (steps != null) {
			//拼接泳道信息的JSON
			for (int i=0; i<steps.size(); i++) {
				WorkFlowStepModel sModel = (WorkFlowStepModel) steps.get(Integer.valueOf(i));
				if (sModel._laneId == 0) {
					sModel._laneId = firstLane._id;
					sModel._laneUUID = firstLane._uuid;
					WFDesignDaoFactory.createWorkFlowStep().store(sModel);
				}
				//校验泳道信息
				WorkFlowLaneModel lModel = (WorkFlowLaneModel) WorkFlowLaneCache.getModel(sModel._laneId);
				if (lModel != null) {
					if (lModel._flowId != sModel._flowId) {
						sModel._laneId = firstLane._id;
						sModel._laneUUID = firstLane._uuid;
						WFDesignDaoFactory.createWorkFlowStep().store(sModel);
					}
				} else {
					sModel._laneId = firstLane._id;
					sModel._laneUUID = firstLane._uuid;
					WFDesignDaoFactory.createWorkFlowStep().store(sModel);
				}
				//构造节点数据
				map = new HashMap();
				map.put(AWSConst.AWSID, String.valueOf(sModel._id));
				map.put(WorkFlowStepModel.FIELD_UUID, sModel._uuid);
				map.put(WorkFlowStepModel.FIELD_LANE_UUID, sModel._laneUUID);
				map.put(WorkFlowStepModel.FIELD_STEP_NAME, sModel._stepName);
				map.put(WorkFlowStepModel.FIELD_STEP_NO, String.valueOf(sModel._stepNo));
				map.put(AWSConst.TYPE, FPDConst.ACTIVITY_NODE_TYPE);
				sb.append(getXMLItemData(map));
			}
		}
		
		String str = sbLane.toString() + sb.toString() + st.toString();
		
		if (isFullXMLData) {
			return getXMLData(str);
		} else {
			return str;
		}
	}
	
	public String getWorkFlowStepOpinionsAndRulesXMLItemData(int wfId, boolean isFullXMLData) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfId);
		return getWorkFlowStepOpinionsAndRulesXMLItemData(wfModel, isFullXMLData);
	}
	
	public String getWorkFlowStepOpinionsAndRulesXMLItemData(String wfUUID, boolean isFullXMLData) {
		WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
		return getWorkFlowStepOpinionsAndRulesXMLItemData(wfModel, isFullXMLData);
	}
	
	/**
	 * 生成流程节点的审核数据和规则数据
	 * @param wfModel 流程模型
	 * @param isFullXMLData 是否需要完整结构的xml数据
	 * @return
	 * @author Administrator
	 */
	private String getWorkFlowStepOpinionsAndRulesXMLItemData(WorkFlowModel wfModel, boolean isFullXMLData) {
		if (wfModel == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		StringBuilder ruleName = new StringBuilder();
		String oUUID = "";
		String rUUID = "";
		boolean hasContinueTransact = false;//有无继续办理节点
		HashMap map = new HashMap();
		Map steps = WorkFlowStepCache.getListOfWorkFlow(wfModel._id);
		
		//拼接泳道信息的JSON
		for (int i=-1; steps != null && i<steps.size(); i++) {
			WorkFlowStepModel stepModel = null;
			if (i==-1) {
				stepModel = (WorkFlowStepModel) WorkFlowStepCache.getModel(wfModel._id, 0);
			} else {
				stepModel = (WorkFlowStepModel) steps.get(Integer.valueOf(i));
			}
			int stepNo = stepModel._stepNo;

			oUUID = "O_" + stepNo + "_" + stepModel._uuid;
			rUUID = "R_" + stepNo + "_" + stepModel._uuid;

			WorkFlowStepModel firstStep = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(wfModel._id, 1);
			WorkFlowStepModel prevStep = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(wfModel._id, stepNo - 1);
			WorkFlowStepModel nextStep = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(wfModel._id, stepNo + 1);

			Hashtable oList = WorkFlowStepOpinionCache.getListOfWorkFlowOpinion(stepModel._id);
			Hashtable startRuleList = WorkFlowStepRuleCache.getListOfWorkFlowStep(wfModel._uuid, 0);
			Hashtable rList = WorkFlowStepRuleCache.getListOfWorkFlowStep(wfModel._uuid, stepModel._id);

			//增加开始节点和第一个节点的连线
			if (i == -1 && startRuleList.size() > 0) {
				map = new HashMap();
				map.put(AWSConst.AWSID, "0");
				map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
				map.put(AWSConst.LINE_FROM, FPDConst.START_NODE_ID);
				map.put(AWSConst.LINE_FROM_TYPE, FPDConst.START_NODE_TYPE);
				map.put(AWSConst.LINE_TO, rUUID);
				map.put(AWSConst.LINE_TO_TYPE, FPDConst.RULE_NODE_TYPE);
				map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
				setLineProperty(map, false, false);
				sb.append(getXMLItemData(map));
			}

			//当遍历第一个节点时，如果没有开始节点的规则，则将开始节点和第一个节点做连线
			if (i == 0 && startRuleList.size() == 0) {
				map = new HashMap();
				map.put(AWSConst.AWSID, "0");
				map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
				map.put(AWSConst.LINE_FROM, FPDConst.START_NODE_ID);
				map.put(AWSConst.LINE_FROM_TYPE, FPDConst.START_NODE_TYPE);
				map.put(AWSConst.LINE_TO, stepModel._uuid);
				map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
				map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
				setLineProperty(map, false, false);
				sb.append(getXMLItemData(map));
			}
			if (oList != null && rList != null) {
				//没有审核同时没有规则
				if (i != -1 && oList.size() == 0 && rList.size() == 0) {//
					map = new HashMap();
					map.put(AWSConst.AWSID, "0");
					map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
					map.put(AWSConst.LINE_FROM, stepModel._uuid);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					if (nextStep != null) {
						map.put(AWSConst.LINE_TO, nextStep._uuid);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					} else {
						map.put(AWSConst.LINE_TO, FPDConst.END_NODE_ID);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.END_NODE_TYPE);
					}
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, false, false);
					sb.append(getXMLItemData(map));
				}

				for (int j=0; j<oList.size(); j++) {
					WorkFlowStepOpinionModel opinion = (WorkFlowStepOpinionModel) oList.get(Integer.valueOf(j));
					if (opinion._opinionType == 0) {//继续办理
						hasContinueTransact = true;
						break;
					}
				}

				HashMap oMap = new HashMap();
				oMap.put(WorkFlowStepModel.FIELD_UUID, oUUID);
				oMap.put(WorkFlowStepModel.FIELD_LANE_UUID, stepModel._laneUUID);
				oMap.put(AWSConst.NAME, "");
				oMap.put(AWSConst.ACTIVITY_NODE, stepModel._uuid);
				oMap.put(AWSConst.TYPE, FPDConst.OPINION_NODE_TYPE);

				HashMap rMap = new HashMap();
				rMap.put(WorkFlowStepModel.FIELD_UUID, rUUID);
				rMap.put(WorkFlowStepModel.FIELD_LANE_UUID, stepModel._laneUUID);
				rMap.put(AWSConst.NAME, "");
				rMap.put(AWSConst.ACTIVITY_NODE, stepModel._uuid);
				rMap.put(AWSConst.TYPE, FPDConst.RULE_NODE_TYPE);

				if (oList.size() > 0 && rList.size() > 0) {//该节点同时有审核和规则时

					sb.append(getXMLItemData(oMap));

					sb.append(getXMLItemData(rMap));

					//将当前节点和审核节点做连线
					map = new HashMap();
					map.put(AWSConst.AWSID, "0");
					map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, false, false);
					map.put(AWSConst.LINE_FROM, stepModel._uuid);
					map.put(AWSConst.LINE_TO, oUUID);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					map.put(AWSConst.LINE_TO_TYPE, FPDConst.OPINION_NODE_TYPE);
					sb.append(getXMLItemData(map));

					if (!hasContinueTransact) {
						//将审核节点和规则节点做连线
						map = new HashMap();
						map.put(AWSConst.AWSID, "0");
						map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
						map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
						setLineProperty(map, false, false);
						map.put(AWSConst.LINE_FROM, oUUID);
						map.put(AWSConst.LINE_TO, rUUID);
						map.put(AWSConst.LINE_FROM_TYPE, FPDConst.OPINION_NODE_TYPE);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.RULE_NODE_TYPE);
						map.put(AWSConst.NAME, I18nRes.findValue(super.getContext().getLanguage(), "当前规则不能被正确执行"));
						map.put("COLOR", "#FF0000");
						sb.append(getXMLItemData(map));
					}
				} else if (oList.size() > 0 && rList.size() == 0) {//该节点只有审核

					sb.append(getXMLItemData(oMap));

					//将当前节点和审核节点做连线
					map = new HashMap();
					map.put(AWSConst.AWSID, "0");
					map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, false, false);
					map.put(AWSConst.LINE_FROM, stepModel._uuid);
					map.put(AWSConst.LINE_TO, oUUID);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					map.put(AWSConst.LINE_TO_TYPE, FPDConst.OPINION_NODE_TYPE);
					sb.append(getXMLItemData(map));
				} else if (oList.size() == 0 && rList.size() > 0) {//该节点只有规则

					sb.append(getXMLItemData(rMap));

					//将当前节点和规则节点做连线
					map = new HashMap();
					map.put(AWSConst.AWSID, "0");
					map.put(WorkFlowStepModel.FIELD_UUID, getUUID());
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, false, false);
					map.put(AWSConst.LINE_FROM, stepModel._uuid);
					map.put(AWSConst.LINE_TO, rUUID);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					map.put(AWSConst.LINE_TO_TYPE, FPDConst.RULE_NODE_TYPE);
					sb.append(getXMLItemData(map));
				}

				for (int j=0; j<oList.size(); j++) {
					WorkFlowStepOpinionModel opinion = (WorkFlowStepOpinionModel) oList.get(Integer.valueOf(j));

					map = new HashMap();
					map.put(AWSConst.AWSID, String.valueOf(opinion._id));
					map.put(WorkFlowStepOpinionModel.FIELD_UUID, opinion._uuid);
					map.put(AWSConst.NAME, opinion._opinionType == -6 ? "" : opinion._opinionName.replaceAll("\"", "`"));
					map.put(AWSConst.LINE_FROM, oUUID);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.OPINION_NODE_TYPE);
					if (opinion._opinionType == 0 || opinion._opinionType == -6) {//继续办理
						map.put(WorkFlowStepOpinionModel.FIELD_OPINION_TYPE, String.valueOf(opinion._opinionType));
						String nextUUID = "";
						if (rList.size() > 0) {//如果有规则，则直接连接规则节点
							map.put(AWSConst.LINE_TO, rUUID);
							map.put(AWSConst.LINE_TO_TYPE, FPDConst.RULE_NODE_TYPE);
							map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "1");
						} else {
							WorkFlowStepModel stepModelNext = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(wfModel._id, stepModel._stepNo + 1);
							if (stepModelNext != null) {
								nextUUID = stepModelNext._uuid;
								map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
							} else {
								nextUUID = FPDConst.END_NODE_ID;
								map.put(AWSConst.LINE_TO_TYPE, FPDConst.END_NODE_TYPE);
							}
							map.put(AWSConst.LINE_TO, nextUUID);

							if (stepModel._stepNo == steps.size()) {
								map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
							} else {
								if (stepModelNext != null&&(stepModel._stepNo + 1 == stepModelNext._stepNo && stepModel._laneId == stepModelNext._laneId || stepModel._laneId == stepModelNext._laneId)) {
									map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
								} else {
									map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "4");
								}
							}
							map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "0");
						}
					} else if (opinion._opinionType == -2) {//送作者修改，完毕后发送给驳回者继续办理
						//2011.03.24 modify by zhanghf 暂时屏蔽该连线
						//map.put(WorkFlowStepOpinionModel.FIELD_OPINION_TYPE, String.valueOf(opinion._opinionType));
						//map.put(AWSConst.LINE_TO, firstStep._uuid);
						//map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					} else if (opinion._opinionType == -3) {//流程终止办理
						map.put(WorkFlowStepOpinionModel.FIELD_OPINION_TYPE, String.valueOf(opinion._opinionType));
						map.put(AWSConst.LINE_TO, FPDConst.END_NODE_ID);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.END_NODE_TYPE);
						//根据审核的动作，方向，初始化锚点位置
						if (rList.size()>0) {
							map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "4");
						} else {
							map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
						}
						map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "3");
					} else if (opinion._opinionType == -4) {//送作者修改(并提醒所有办理过的人)，完毕后发送给驳回者继续办理
						//2011.03.24 modify by zhanghf 暂时屏蔽该连线
						//map.put(WorkFlowStepOpinionModel.FIELD_OPINION_TYPE, String.valueOf(opinion._opinionType));
						//map.put(AWSConst.LINE_TO, firstStep._uuid);
						//map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					} else {
						map.put(WorkFlowStepOpinionModel.FIELD_OPINION_TYPE, String.valueOf(opinion._opinionType));
						WorkFlowStepModel stepModel2 = (WorkFlowStepModel) WorkFlowStepCache.getModel(opinion._opinionType);
						map.put(AWSConst.LINE_TO, stepModel2._uuid);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
						
						if (stepModel._stepNo >= stepModel2._stepNo) {//向上的连线
							map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "3");
							map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "3");
						} else {//向下的连线
							if (stepModel._stepNo + 1 == stepModel2._stepNo && stepModel._laneId == stepModel2._laneId || stepModel._laneId == stepModel2._laneId) {//紧邻的节点并且同一泳道，则使用2
								map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
							} else {
								map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "4");
							}
							if (rList.size() > 0) {
								map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "1");
							} else {
								map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "0");
							}
						}
					}
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, true, false);
					sb.append(getXMLItemData(map));
				}

				String name = "";
				for (int k=0; k<rList.size(); k++) {
					WorkFlowStepRuleModel rule = (WorkFlowStepRuleModel) rList.get(Integer.valueOf(k));

					name = "";

					map = new HashMap();
					map.put(AWSConst.AWSID, String.valueOf(rule._id));
					map.put(WorkFlowStepRuleModel.FIELD_UUID, rule._uuid);
					map.put(AWSConst.LINE_FROM, rUUID);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.RULE_NODE_TYPE);
					map.put(WorkFlowStepOpinionModel.FIELD_OPINION_TYPE, "-");

					if (rule._jumpStepId == -1) { // end flow

						ruleName = new StringBuilder();
						//ruleName.append("如果");
						ruleName.append("[").append(rule._fieldTitle).append("]").append(rule._compareType).append("'").append(rule._compareValue).append("'");
						Hashtable ruleGroupList = WorkFlowStepRuleCache.getListOfWorkFlowStepRuleGroup(Integer.toString(rule._id));
						if (ruleGroupList != null) {
							for (int ii = 0; ii < ruleGroupList.size(); ii++) {
								WorkFlowStepRuleModel ruleModel = (WorkFlowStepRuleModel) ruleGroupList.get(new Integer(ii));
								ruleName.append(" "+I18nRes.findValue(super.getContext().getLanguage(),"并且")+" [").append(ruleModel._fieldTitle).append("]").append(ruleModel._compareType).append("'").append(ruleModel._compareValue).append("'");
							}
						}
						//ruleName.append(" 时，结束流程!");

						map.put("RULETYPE", "1");

						name = ruleName.toString().replace("<", "&lt;");
						name = name.replace(">", "&gt;");
						name = name.replace("\"", "&quot;");

						map.put(AWSConst.NAME, name);
						map.put(AWSConst.LINE_TO, FPDConst.END_NODE_ID);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.END_NODE_TYPE);
						
						map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "0");
						map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "3");
						
					} else if (rule._jumpStepId == -2) { // in sub-process

						ruleName = new StringBuilder();
						//ruleName.append("如果");
						ruleName.append("[").append(rule._fieldTitle).append("]").append(rule._compareType).append("'").append(rule._compareValue).append("'");
						Hashtable ruleGroupList = WorkFlowStepRuleCache.getListOfWorkFlowStepRuleGroup(Integer.toString(rule._id));
						if (ruleGroupList != null) {
							for (int ii = 0; ii < ruleGroupList.size(); ii++) {
								WorkFlowStepRuleModel ruleModel = (WorkFlowStepRuleModel) ruleGroupList.get(new Integer(ii));
								ruleName.append(" "+I18nRes.findValue(super.getContext().getLanguage(),"并且")+" [").append(ruleModel._fieldTitle).append("]").append(ruleModel._compareType).append("'").append(ruleModel._compareValue).append("'");
							}
						}
						WorkFlowSubModel subModel = (WorkFlowSubModel) WorkFlowSubCache.getModelOfBindUUID(rule._uuid);
						if (subModel != null) {
							//ruleName.append(" 时，" + (subModel._synType == 0 ? ("(同步)" + (subModel._muliInstanceLoop == 0 ? "(单例)" : "(多例)")) : "(异步)" + (subModel._muliInstanceLoop == 0 ? "(单例)" : "(多例)")) + "启动子流程");

							map.put("RULETYPE", "2");
							map.put("SYNTYPE", String.valueOf(subModel._synType));
							if (subModel._synType == 0) {
								map.put("ENDSTEPUUID", stepModel._uuid);
							} else {
								map.put("ENDSTEPUUID", subModel._endStepUUID);
							}
							WorkFlowModel subWfModel = (WorkFlowModel) WorkFlowCache.getModel(subModel._subFlowUUID);
							map.put("WORKFLOWID", subWfModel!=null?String.valueOf(subWfModel._id):"");
							map.put("WORKFLOWNAME", subWfModel!=null?subWfModel._flowName:"");

							name = ruleName.toString().replace("<", "&lt;");
							name = name.replace(">", "&gt;");
							name = name.replace("\"", "&quot;");

							map.put(AWSConst.NAME, name);
							//创建子流程节点
							map.put(AWSConst.LINE_TO, FPDConst.SUBFLOW_NODE_TYPE);
							map.put(AWSConst.LINE_TO_TYPE, FPDConst.SUBFLOW_NODE_TYPE);
							
							map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
							map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "1");
						} else {
							ruleName.append(" "+I18nRes.findValue(super.getContext().getLanguage(),"注意")+"：" + I18nRes.findValue(super.getContext().getLanguage(),"子流程可能被删除，请检查！"));
							map.put("RULETYPE", "2");
							map.put("SYNTYPE", "-");
							map.put("ENDSTEPUUID", "-");
							map.put("WORKFLOWID", "-");
							map.put("WORKFLOWNAME", I18nRes.findValue(super.getContext().getLanguage(),"子流程可能被删除，请检查！"));

							name = ruleName.toString().replace("<", "&lt;");
							name = name.replace(">", "&gt;");
							name = name.replace("\"", "&quot;");

							map.put(AWSConst.NAME, name);
							//创建子流程节点
							map.put(AWSConst.LINE_TO, FPDConst.SUBFLOW_NODE_TYPE);
							map.put(AWSConst.LINE_TO_TYPE, FPDConst.SUBFLOW_NODE_TYPE);
						}
					} else { // jump flow
						if (rule._jumpUser.equals("")) {
							ruleName = new StringBuilder();
							WorkFlowStepModel tempModel = (WorkFlowStepModel) WorkFlowStepCache.getModel(rule._jumpStepId);
							if (tempModel == null) {
								tempModel = new WorkFlowStepModel();
							}
							//ruleName.append("如果");
							ruleName.append("[").append(rule._fieldTitle).append("]").append(rule._compareType).append("'").append(rule._compareValue).append("'");
							Hashtable ruleGroupList = WorkFlowStepRuleCache.getListOfWorkFlowStepRuleGroup(Integer.toString(rule._id));
							if (ruleGroupList != null) {
								for (int ii = 0; ii < ruleGroupList.size(); ii++) {
									WorkFlowStepRuleModel ruleModel = (WorkFlowStepRuleModel) ruleGroupList.get(new Integer(ii));
									ruleName.append(" "+I18nRes.findValue(super.getContext().getLanguage(),"并且")+"[").append(ruleModel._fieldTitle).append("]").append(ruleModel._compareType).append("'").append(ruleModel._compareValue).append("'");
								}
							}
							//ruleName.append("时，跳转到'").append(tempModel._stepName).append("'").append("");

							map.put("RULETYPE", "1");

							name = ruleName.toString().replace("<", "&lt;");
							name = name.replace(">", "&gt;");
							name = name.replace("\"", "&quot;");

							map.put(AWSConst.NAME, name);
							map.put(AWSConst.LINE_TO, tempModel._uuid);
							map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
							
							if (stepModel._stepNo >= tempModel._stepNo) {//向上的连线
								map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "3");
								map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "3");
							} else {//向下的连线
								if (stepModel._stepNo + 1 == tempModel._stepNo && stepModel._laneId == tempModel._laneId) {
									map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
								} else if (stepModel._stepNo < tempModel._stepNo && stepModel._laneId != tempModel._laneId) {
									map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "2");
								} else {
									map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "4");
								}
								map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "0");
							}
						} else {
							ruleName = new StringBuilder();
							//ruleName.append("如果");
							ruleName.append("[").append(rule._fieldTitle).append("]").append(rule._compareType).append("'").append(rule._compareValue).append("'");
							Hashtable ruleGroupList = WorkFlowStepRuleCache.getListOfWorkFlowStepRuleGroup(Integer.toString(rule._id));
							if (ruleGroupList != null) {
								for (int ii = 0; ii < ruleGroupList.size(); ii++) {
									WorkFlowStepRuleModel ruleModel = (WorkFlowStepRuleModel) ruleGroupList.get(new Integer(ii));
									ruleName.append(" "+I18nRes.findValue(super.getContext().getLanguage(),"并且 [")).append(ruleModel._fieldTitle).append("]").append(ruleModel._compareType).append("'").append(ruleModel._compareValue).append("'");
								}
							}
							//ruleName.append("时，下个节点指定由'").append(rule._jumpUser).append("'").append("参与办理");

							map.put("RULETYPE", "3");
							
							name = rule._jumpUser.replace("<", "&lt;");
							name = name.replace(">", "&gt;");
							map.put("TRANSACTOR", name);

							name = ruleName.toString().replace("<", "&lt;");
							name = name.replace(">", "&gt;");
							name = name.replace("\"", "&quot;");

							map.put(AWSConst.NAME, name);
							//创建办理者节点
							map.put(AWSConst.LINE_TO, FPDConst.TRANSACTOR_NODE_TYPE);
							map.put(AWSConst.LINE_TO_TYPE, FPDConst.TRANSACTOR_NODE_TYPE);

							map.put(AWSConst.LINE_FROM_ANCHOR_POSITION, "0");
							map.put(AWSConst.LINE_TO_ANCHOR_POSITION, "3");
						}
					}
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, false, true);
					sb.append(getXMLItemData(map));
				}

				//1.当存在审核并且有继续办理并且有规则的同时的情况，添加一条所有规则不匹配的连线 或者 2.当没有审核并且存在规则时，添加一条所有规则不匹配的连线
				if ((oList.size() > 0 && hasContinueTransact && rList.size() > 0) || (oList.size() == 0 && rList.size() > 0)) {
					map = new HashMap();
					map.put(AWSConst.AWSID, "0");
					map.put(WorkFlowStepRuleModel.FIELD_UUID, getUUID());
					map.put(AWSConst.LINE_FROM, rUUID);
					map.put(AWSConst.LINE_FROM_TYPE, FPDConst.RULE_NODE_TYPE);
					map.put(AWSConst.NAME, I18nRes.findValue(super.getContext().getLanguage(),"规则不匹配时"));
					if (stepNo != steps.size()) {
						map.put(AWSConst.LINE_TO, nextStep._uuid);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.ACTIVITY_NODE_TYPE);
					} else {
						map.put(AWSConst.LINE_TO, FPDConst.END_NODE_ID);
						map.put(AWSConst.LINE_TO_TYPE, FPDConst.END_NODE_TYPE);
					}
					map.put(AWSConst.TYPE, FPDConst.LINE_TYPE);
					setLineProperty(map, false, false);
					sb.append(getXMLItemData(map));
				}

			}
		}
		if (isFullXMLData) {
			return getXMLData(sb.toString());
		} else {
			return sb.toString();
		}
	}
	
	public String readWorkFlowXPDL(String wfUUID) {
		return readWorkFlowXPDL(wfUUID, true);
	}
	
	public String readWorkFlowXPDL(String wfUUID, boolean isHtmlFormat) {
		synchronized (_fileWRObj) {
			WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
			if (wfModel == null) {
				return "-1";
			}
			String fileName = mainPath + subPath + wfModel._uuid + ".xpdl";

			HashMap map = new HashMap();
			map.put(WorkFlowModel.FIELD_UUID, wfModel._uuid);
			map.put(WorkFlowModel.FIELD_FLOW_NAME, wfModel._flowName);

			if (new File(fileName).exists()) {
				try {
					String xml = "";
					xml = UtilFile.readAll(fileName, "UTF-8");

					if (isHtmlFormat) {
						xml = xml.replaceAll("\"", "&quot;");
						xml = xml.replaceAll("\n", "");
					}

					map.put("XML", xml);

					return xml;
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
			return "0";
		}
	}
	
	public String saveWorkFlowXPDL(String wfUUID, String xml, String isExportImage) {
		synchronized (_fileWRObj) {
			WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
			if (wfModel == null) {
				return "-1";
			}
			String path = subPath;
			String fileName = mainPath + path + wfModel._uuid + ".xpdl";
			String directory = mainPath + path;
			if (!new File(directory).exists()) {
				new File(directory).mkdirs();
			}

			String ver = "<Created>AWS BPM Engine(version)</Created>";
			String currentVer = "<Created>AWS BPM Engine(" + Console.getBuildVersion() + ")</Created>";

			xml = xml.replaceAll("__eol__", "\n");
			if (xml.indexOf(ver) > -1) {
				xml = xml.replace(ver, currentVer);
			}
			String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			if (!xml.startsWith(head)) {
				xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml;
			}

			FileOutputStream fos = null;
			Writer out = null;
			try {
				fos = new FileOutputStream(fileName);
				out = new OutputStreamWriter(fos, "UTF-8");
				out.write(xml);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			} finally {
				try {
					out.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (isExportImage.equalsIgnoreCase("true")) {
				return "1";
			} else {
				return "0";
			}
		}
	}
	
	public String initWorkFlowLayout(String wfUUID) {
		synchronized (_fileWRObj) {
			WorkFlowModel wfModel = (WorkFlowModel) WorkFlowCache.getModel(wfUUID);
			if (wfModel == null) {
				return "-1";
			}
			boolean r = WFDesignDaoFactory.createWorkFlow().removeWorkFlowXPDL(wfUUID);
			if (r) {
				return I18nRes.findValue(super.getContext().getLanguage(),"流程布局初始化成功")+"！";
			} else {
				return "-1"+I18nRes.findValue(super.getContext().getLanguage(),"流程布局初始化失败")+"！";
			}
		}
	}
	
	private String getXMLData(String item) {
		StringBuilder xml = new StringBuilder();
		xml.append("<Items>\n");
		xml.append(item);
		xml.append("</Items>\n");
		return xml.toString();
	}

	private String getXMLItemData(HashMap map) {
		StringBuilder xml = new StringBuilder();
		String key = "";
		
		StringBuilder content = new StringBuilder();
		Object[] keys = map.keySet().toArray();
		Arrays.sort(keys);
		
		String itemType = "item";
		if (map.containsKey(AWSConst.TYPE)) {
			itemType = map.get(AWSConst.TYPE).toString();
		}
	    xml.append("<" + itemType);
	    for (int i = 0; i < keys.length; i++) {
	    	key = keys[i].toString();
	    	content.append(key).append("=\"").append(map.get(key)).append("\" ");
		}
	    if (content.length() > 0) {
	    	xml.append(" ").append(content.toString().trim());
	    }
		xml.append("/>\n");
		return xml.toString();
	}
	
	private String getUUID() {
		String uuid = "";
		try {
			uuid = UUID.getInstance(UUID.UID_UUID).getNextUID();
		} catch (UIDNotSupportException e) {
			e.printStackTrace(System.err);
		}
		return uuid;
	}
	
	private void setLineProperty(HashMap map, boolean isOpinionLine, boolean isRuleLine) {
		map.put("COLOR", "#000000");
		map.put("isOpinionLine", String.valueOf(isOpinionLine));
		map.put("isRuleLine", String.valueOf(isRuleLine));
	}
	
	public String downloadImage(String uuid, String imgData) {
		imgData = imgData.replaceAll("__eol__", "\n");
		Base64 bd = new Base64();
		byte[] b = new byte[0];
		b=bd.decodeBase64(imgData.getBytes());
		String targetDir = "fpd/process_tobe/";
		String r = writeFile(uuid, b, "png", targetDir, false);
		String rr=getDownPage(uuid, b, "png");
		/***********  导出 图片  添加审计日志开始**********/ 
		WorkFlowModel wf= (WorkFlowModel) WorkFlowCache.getModel(uuid);
		Map  trace=new HashMap();
		String wfName="";
		if(wf!=null){
			wfName=wf._flowName;
		}
		 trace.put("流程名称[WorkFlowName]",wfName);	
		 trace.put("导出类型[WorkFlowName]","图片");	
		if (rr.equals("-1")) {
			logger.log("["+wfName+"]流程   导出 图片失败", Action.ASSIGN, trace, Level.ERROR); 
		}else{
			logger.log("["+wfName+"]流程   导出图片成功", Action.ASSIGN, trace, Level.INFO);
		}
		/***********  导出 图片  添加审计日志 结束**********/
		return rr;
	}
	
	public String downloadXPDL(String uuid) {
		String xpdl = readWorkFlowXPDL(uuid, false);
		byte[] b = new byte[0];
		try {
			b = xpdl.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		 String r=getDownPage(uuid, b, "xpdl");
		/***********  导出 xpdl  添加审计日志开始**********/ 
		WorkFlowModel wf= (WorkFlowModel) WorkFlowCache.getModel(uuid);
		Map  trace=new HashMap();
		String wfName="";
		if(wf!=null){
			wfName=wf._flowName;
		}
		 trace.put("流程名称[WorkFlowName]",wfName);	
		 trace.put("导出类型[WorkFlowName]","XPDL");	
		if (r.equals("-1")) {
			logger.log("["+wfName+"]流程   导出XPDL失败", Action.ASSIGN, trace, Level.ERROR); 
		}else{
			logger.log("["+wfName+"]流程   导出XPDL成功", Action.ASSIGN, trace, Level.INFO);
		}
		/***********  导出 xpdl  添加审计日志 结束**********/ 
		return r;
	}
	
	public String getDownPage(String uuid, byte[] content, String type) {
		String targetDir = AWFConfig._awfServerConf.getDocumentPath() + "FPD/groupModel/filefpd/";
		
		String targetFile = targetDir;
		String fileName = uuid + "." + type;
		targetFile += fileName;
		
		String r = writeFile(uuid, content, type, targetDir, true);
		if (r.equals("-1")) {
			return r;
		}

		StringBuilder response = new StringBuilder();
		response.append("downfile.wf?flag1=Model&flag2=fpd");
		response.append("&sid=" + this.getContext().getSessionId());
		response.append("&filename=" + fileName + "&rootDir=FPD");//&attachment=true

		return response.toString();
	}
	
	private String writeFile(String uuid, byte[] content, String type, String targetDir, boolean isEncrypt) {
		String targetFile = targetDir;
		String fileName = uuid + "." + type;
		targetFile += fileName;
		File dir = new File(targetDir);
		dir.mkdirs();
		FileOutputStream fileoutputstream = null;
		try {
			fileoutputstream = new FileOutputStream(targetFile);// 建立文件输出流
			fileoutputstream.write(content);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "-1";
		} finally {
			try {
				fileoutputstream.flush();
				fileoutputstream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (isEncrypt) {
			// 加密，准备下载
			EncryptFileUtil.encryptFile(targetFile);
		}
		return "1";
	}

}

