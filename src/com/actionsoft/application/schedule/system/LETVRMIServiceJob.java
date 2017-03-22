package com.actionsoft.application.schedule.system;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.logging.IntegratedLogger;
import com.actionsoft.application.schedule.IJob;
import com.actionsoft.application.server.LICENSE;
import com.actionsoft.application.server.cluster.SynchronizationServer;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.system.runtimemanager.cluster.AWSRemotePerformanceServer;
import com.actionsoft.awf.services.WSSecurity;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.coe.system.api.AnalysisRemoteServer;
import com.actionsoft.sdk.local.WSDK;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.sdk.local.level0.IMAPI;
import com.actionsoft.sdk.local.level0.ORGAPI;
import com.actionsoft.sdk.local.level0.TaskWorklistAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
import com.actionsoft.sdk.local.level0.XBusAPI;
import com.actionsoft.sdk.local.level1.MetaDataAPI;
import com.actionsoft.sdk.local.level1.SecurityAPI;
import com.actionsoft.sdk.local.level1.SessionAPI;
import com.actionsoft.xbus.integration.resouce.rmi.CatalogKeys;
import com.actionsoft.xbus.integration.resouce.rmi.UploadManager;
import com.actionsoft.xbus.integration.resouce.rmi.XBusServer;
import com.actionsoft.xbus.integration.server.rmi.ServerRMI;
import com.app.rs.MyLocalService;

public class LETVRMIServiceJob implements IJob {
	private XBusServer xbusServer = new XBusServer();
	private ServerRMI serverRMI = new ServerRMI();
	private UploadManager uploadManager = new UploadManager();
	private CatalogKeys catalogKeys = new CatalogKeys();

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			String serverHost = null;
			if ((!AWFConfig._awfServerConf.getIp().toLowerCase().equals(
					"localhost"))
					&& (!AWFConfig._awfServerConf.getIp().toLowerCase().equals(
							"127.0.0.1"))) {
				serverHost = AWFConfig._awfServerConf.getIp();
			}
			Remote.config(serverHost, Integer.parseInt(AWFConfig._awfServerConf
					.getRmiPort()), null, 0);
			startAWSRmi();
			startXBusRmi();
		} catch (Exception e) {
			System.out.println("警告: [" + UtilDate.datetimeFormat(new Date())
					+ "]RMI远程过程调用服务启动失败");
			e.printStackTrace(System.out);
		}
	}

	private void startAWSRmi() {
		try {
			// 新增的监听服务
			ItemServer.bind(new MyLocalService(), "letvpc");
			ItemServer.bind(new WSSecurity(), "WSSecurity");
			ItemServer.bind(new IntegratedLogger(), "AWSLogger");
			ItemServer.bind(new RMIWsServer(), "WebServiceProxy");
			ItemServer.bind(BOInstanceAPI.getInstance(), "BOService");
			ItemServer.bind(WorkflowInstanceAPI.getInstance(),
					"WorkflowService");
			ItemServer.bind(WorkflowTaskInstanceAPI.getInstance(),
					"WorkflowTaskService");
			ItemServer.bind(MetaDataAPI.getInstance(), "MetaDataService");
			ItemServer.bind(XBusAPI.getInstance(), "XBusService");
			ItemServer.bind(IMAPI.getInstance(), "IMService");
			ItemServer.bind(ORGAPI.getInstance(), "ORGService");
			ItemServer.bind(SecurityAPI.getInstance(), "SecurityService");
			ItemServer.bind(SessionAPI.getInstance(), "SessionService");
			ItemServer.bind(TaskWorklistAPI.getInstance(),
					"TaskWorklistService");
			ItemServer.bind(new WSDK(), "WSDKService");
			if (LICENSE.isCOE()) {
				ItemServer.bind(new AnalysisRemoteServer(),
						"AnalysisRemoteServer");
			}
			ItemServer.bind(new SynchronizationServer(), "clusterServer");

			ItemServer.bind(new AWSRemotePerformanceServer(),
					"performanceServer");
			
			System.err.println("信息: [" + UtilDate.datetimeFormat(new Date())
					+ "]AWS RMI远程过程调用服务已启动");
		} catch (Exception e) {
			System.err.println("警告: [" + UtilDate.datetimeFormat(new Date())
					+ "]AWS RMI远程过程调用服务启动失败");
			e.printStackTrace(System.out);
		}
	}

	private void startXBusRmi() {
		try {
			ItemServer.bind(this.xbusServer, "XBusServer");
			ItemServer.bind(this.serverRMI, "ServerRMI");
			ItemServer.bind(this.uploadManager, "UploadManager");
			ItemServer.bind(this.catalogKeys, "CatalogKeys");
			System.out.println("信息: [" + UtilDate.datetimeFormat(new Date())
					+ "]XBUS RMI远程过程调用服务已启动");
		} catch (Exception e) {
			System.out.println("警告: [" + UtilDate.datetimeFormat(new Date())
					+ "]XBUS RMI远程过程调用服务启动失败");
			e.printStackTrace(System.out);
		}
	}
}