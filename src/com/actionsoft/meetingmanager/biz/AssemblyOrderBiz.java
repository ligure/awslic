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

package com.actionsoft.meetingmanager.biz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.model.UserTaskHistoryOpinionModel;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.meetingmanager.util.MeetingDataUtil;
import com.actionsoft.sdk.local.level0.BOInstanceAPI;
import com.actionsoft.xbus.integration.server.rmi.client.test.SARTest;

/**
 * 
 * @author 杜楠
 * 
 * @version 1.0
 * 
 * 
 * 
 *          会议室预定流程，起草办理时判断预定时间的RtClass类
 */

public class AssemblyOrderBiz extends WorkFlowStepRTClassA {
	private static Hashtable<String, String> xqys = new Hashtable<String, String>();
	static{
		xqys.put("星期一", "Monday");
		xqys.put("星期二", "Tuesday");
		xqys.put("星期三", "Wednesday");
		xqys.put("星期四", "Thursday");
		xqys.put("星期五", "Friday");
		xqys.put("星期六", "Saturday");
		xqys.put("星期日", "Sunday");
	}
	/**
	 * 
	 * 构造方法,用于在后台注册rtclass类后显示注册类信息
	 * 
	 * 
	 * 
	 * @param UserContext
	 * 
	 * 
	 */

	public AssemblyOrderBiz(UserContext uc) {

		super(uc);

		setVersion("1.0");

		setDescription("会议室预定流程，起草办理时判断预定时间是否合理,合理则执行预定，2014-01-18huh修改");

	}

	/**
	 * 
	 * 接口实现方法（WorkFlowStepRTClassA没有实现的抽象方法，子类必须实现该方法）
	 * 
	 * 
	 * 
	 * @return boolean
	 * 
	 * 
	 */

	public boolean execute() {

		int workflowInstanceId = getParameter(this.PARAMETER_INSTANCE_ID)
				.toInt();// 获得当前工作流的id

		String strAudit = null;

		Hashtable opinionList = ProcessRuntimeDaoFactory
				.createUserTaskHistoryOpinion().getInstanceOfProcess(
						workflowInstanceId);

		if (opinionList != null) {

			UserTaskHistoryOpinionModel model = (UserTaskHistoryOpinionModel) opinionList
					.get(new Integer(opinionList.size() - 1));

			strAudit = model.getAuditMenuName();// 获得审核菜单选项

		}

		if (strAudit.equals("同意") || strAudit.equals("同意预定")) {

			Hashtable ht = BOInstanceAPI.getInstance().getBOData(
					"BO_MEETING_ROOM_ORDER", workflowInstanceId);

			String endtime = ht.get("ENDTIME").toString();// 填写的会议室结束时间

			String rq = ht.get("MEETINGDATE").toString();// 预定会议室日期

			String startdate = (String) ht.get("STARTDATE");

			String starttime = ht.get("STARTTIME").toString();

			String enddate = (String) ht.get("ENDDATE");

			String assemblyno = ht.get("MEETINGROOMNO").toString();// 会议室编号

			endtime = enddate + " " + endtime;

			starttime = startdate + " " + starttime;

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm");

			Date zoneEnd;

			try {

				zoneEnd = formatter.parse(endtime);

				Date zoneNow = new Date();

				if (zoneEnd.compareTo(zoneNow) <= 0) {// 判断预定结束时间是否小于当前时间，如果时，不允许办理

					MessageQueue.getInstance().putMessage(
							super.getUserContext().getUID(),
							"<I18N#会议室预定失败>，当前时间()小于结束时间(" + endtime + ")",
							true);// 弹出提示信息

					return false;

				}

			} catch (ParseException e1) {

			}

			Connection conn = null;

			Statement st = null;

			ResultSet rs = null;

			try {

				conn = DBSql.open();

				// String sql =
				// "select * from BO_MEETING_ROOM_ORDER where MEETINGROOMNO='"+assemblyno+"' and STARTDATE>"
				// + DBSql.convertLongDate(starttime) + " and STARTDATE<" +
				// DBSql.convertLongDate(endtime) + " and STATUS='预定'";

				String sql = "select * from BO_MEETING_ROOM_ORDER where MEETINGROOMNO='"
						+ assemblyno + "' and STATUS='预定'";
				String countsql = "select count(*) as c from BO_MEETING_ROOM_ORDER where MEETINGROOMNO='"
						+ assemblyno + "' and STATUS='预定'";
				int count = DBSql.getInt(conn, countsql, "c");// getInt("c",countsql);
				st = conn.createStatement();

				rs = st.executeQuery(sql);

				StringBuffer sb = new StringBuffer();
				if (count > 0) {
					while (rs.next()) {
						String start = rs.getString("STARTDATE").split("\\.")[0];
						String starttim = start; // 数据库中的记录的开始时间
						String end = rs.getString("ENDDATE").split("\\.")[0];
						String endtim = end;// 数据库中的记录的结束时间
						if ((compareTime(starttime, endtim) >= 0)
								|| compareTime(endtime, starttim) <= 0) { // 合法时间段
							sb.append("1");
						} else {
							sb.append("-1");
						}

					}
					if (sb.indexOf("-1") == -1) { // 合法时间段
						sql = "update BO_MEETING_ROOM_ORDER set STATUS='预定',ENDDATE="
								+ DBSql.convertLongDate(endtime)
								+ ",STARTDATE="
								+ DBSql.convertLongDate(starttime)
								+ " where bindid=" + workflowInstanceId;
						int i = DBSql.executeUpdate(sql);
						if (i > 0) {
							// 发送会议通知
							MeetingDataUtil.sendMeetingNotice(getUserContext(),
									workflowInstanceId, "发送给您的会议通知");
							// 向记录员发送会议记录流程
							MeetingDataUtil.sendMeetingRecord(getUserContext(),
									workflowInstanceId);

							/**
							 * @param args
							 * @description 会议室周期性预定
							 * @version 1.0
							 * @author huh
							 * @throws ParseException
							 * @update 2014-1-16 下午12:10:35
							 */
							String zqxhyyd = DBSql.getString(
									"select ZQXHYYD from BO_MEETING_ROOM_ORDER where bindid = '"
											+ workflowInstanceId + "' ",
									"ZQXHYYD");
							if (zqxhyyd.equals("是")) {
								meetingRoom(workflowInstanceId);
							}

							MessageQueue.getInstance().putMessage(
									super.getUserContext().getUID(),
									"<I18N#会议室预定成功>", true);// 弹出提示信息
						} else {
							MessageQueue.getInstance().putMessage(
									super.getUserContext().getUID(),
									"<I18N#会议室预定失败>", true);// 弹出提示信息
							return false;
						}
					} else {
						// 在此时间段内已经被预定
						MessageQueue.getInstance().putMessage(
								super.getUserContext().getUID(),
								"<I18N#此会议室在该时间段内已经被预定，请您选择其他预定时间>", true);// 弹出提示信息
						return false;
					}
				} else {

					sql = "update BO_MEETING_ROOM_ORDER set STATUS='预定',ENDDATE="
							+ DBSql.convertLongDate(endtime)
							+ ",STARTDATE="
							+ DBSql.convertLongDate(starttime)
							+ " where bindid=" + workflowInstanceId;
					int i = DBSql.executeUpdate(sql);
					if (i > 0) {
						// 发送会议通知
						MeetingDataUtil.sendMeetingNotice(getUserContext(),
								workflowInstanceId, "发送给您的会议通知");
						// 向记录员发送会议记录流程
						MeetingDataUtil.sendMeetingRecord(getUserContext(),
								workflowInstanceId);

						/**
						 * @param args
						 * @description 会议室周期性预定
						 * @version 1.0
						 * @author huh
						 * @throws ParseException
						 * @update 2014-1-16 下午12:10:35
						 */
						String zqxhyyd = DBSql.getString(
								"select ZQXHYYD from BO_MEETING_ROOM_ORDER where bindid = '"
										+ workflowInstanceId + "' ", "ZQXHYYD");
						if (zqxhyyd.equals("是")) {
							meetingRoom(workflowInstanceId);
						}

						MessageQueue.getInstance().putMessage(
								super.getUserContext().getUID(),
								"<I18N#会议室预定成功>", true);// 弹出提示信息
					} else {
						MessageQueue.getInstance().putMessage(
								super.getUserContext().getUID(),
								"<I18N#会议室预定失败>", true);// 弹出提示信息
						return false;
					}
				}

				// 在此时间段内已经被预定

				// if (rs.next()) {

				// MessageQueue.getInstance().putMessage(super.getUserContext().getUID(),
				// "<I18N#此会议室在该时间段内已经被预定，请您选择其他预定时间>", true);// 弹出提示信息

				// return false;

				// } else {//// 会议室空闲，可以预定

				//
				// sql = "update BO_MEETING_ROOM_ORDER set STATUS='预定',ENDDATE="
				// + DBSql.convertLongDate(endtime) + ",STARTDATE=" +
				// DBSql.convertLongDate(starttime) +
				// " where bindid="+workflowInstanceId;

				// int i = DBSql.executeUpdate(sql);

				// if (i > 0) {

				// //发送会议通知

				// MeetingDataUtil.sendMeetingNotice(getUserContext(),
				// workflowInstanceId,"发送给您的会议通知");

				// // 向记录员发送会议记录流程

				// MeetingDataUtil.sendMeetingRecord(getUserContext(),
				// workflowInstanceId);

				// MessageQueue.getInstance().putMessage(super.getUserContext().getUID(),
				// "<I18N#会议室预定成功>", true);// 弹出提示信息

				// } else {

				// MessageQueue.getInstance().putMessage(super.getUserContext().getUID(),
				// "<I18N#会议室预定失败>", true);// 弹出提示信息

				// return false;

				// }

				// }

			} catch (Exception e) {

				e.printStackTrace(System.err);

				MessageQueue.getInstance().putMessage(
						super.getUserContext().getUID(),
						"<I18N#预定会议室流程异常，请与管理员联系>");// 弹出提示信息

				return false;

			} finally {

				DBSql.close(conn, st, rs);

			}

		}

		return true;

	}

	public static int compareTime(String s1, String s2) {
		s1 = s1 + ":00";
		s2 = s2 + ":00";
		java.text.DateFormat df = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		java.util.Calendar c1 = java.util.Calendar.getInstance();
		java.util.Calendar c2 = java.util.Calendar.getInstance();
		try {
			c1.setTime(df.parse(s1));
			c2.setTime(df.parse(s2));
		} catch (java.text.ParseException e) {
			System.out.println(e.getMessage());
			System.err.println("格式不正确");
		}
		int result = c1.compareTo(c2);
		if (result == 0)
			return 0;
		else if (result < 0)
			return -1;
		else
			return 1;

	}

	/**
	 * 
	 * @param bindid
	 * @description 实现会议室的周期性预定
	 * @version 1.0
	 * @author huh
	 * @update 2014-1-18 下午2:20:37
	 */
	public static void meetingRoom(int bindid) {
		Connection conn = DBSql.open();
		String sql = "select * from BO_MEETING_ROOM_ORDER m where bindid = '"
				+ bindid + "'";
		Hashtable<String, String> datas = new Hashtable<String, String>();
		String sqxq = DBSql.getString(
				"select SQXQ from BO_MEETING_ROOM_ORDER where bindid = '"
						+ bindid + "'", "SQXQ");
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String orgno = rs.getString("ORGNO");
					datas.put("orgno", orgno);
					String bid = rs.getString("BINDID");
					datas.put("bindid", bid);
					String createuser = rs.getString("CREATEUSER");
					datas.put("createuser", createuser);
					String ordername = rs.getString("ORDERNAME");
					datas.put("ordername", ordername);
					String meetingname = rs.getString("MEETINGNAME");
					datas.put("meetingname", meetingname);
					String meetingpersons = rs.getString("MEETINGPERSONS");
					if (meetingpersons != null) {
						datas.put("meetingpersons", meetingpersons);
					} else {
						datas.put("meetingpersons", "");
					}
					String meetingroomno = rs.getString("MEETINGROOMNO");
					datas.put("meetingroomno", meetingroomno);
					String departmentname = rs.getString("DEPARTMENTNAME");
					datas.put("departmentname", departmentname);
					String userextend = rs.getString("USEREXTEND");
					datas.put("userextend", userextend);
					String status = rs.getString("STATUS");
					datas.put("status", status);
					String starttime = rs.getString("STARTTIME");
					datas.put("starttime", starttime);
					String endtime = rs.getString("ENDTIME");
					datas.put("endtime", endtime);

					String yhry = rs.getString("YHRY");
					if (yhry != null) {
						datas.put("yhry", yhry);
					} else {
						datas.put("yhry", "");
					}

					String qtry = rs.getString("QTRY");
					if (qtry != null) {
						datas.put("qtry", qtry);
					} else {
						datas.put("qtry", "");
					}

					String meetingtitle = rs.getString("MEETINGTITLE");
					if (meetingtitle != null) {
						datas.put("meetingtitle", meetingtitle);
					} else {
						datas.put("meetingtitle", "");
					}

					String meetingassettyy = rs.getString("MEETINGASSETTYY");
					if (meetingassettyy != null) {
						datas.put("meetingassettyy", meetingassettyy);
					} else {
						datas.put("meetingassettyy", "");
					}

					String meetingassettsdn = rs.getString("MEETINGASSETTSDN");
					if (meetingassettsdn != null) {
						datas.put("meetingassettsdn", meetingassettsdn);
					} else {
						datas.put("meetingassettsdn", "");
					}

					String meetingassetsphy = rs.getString("MEETINGASSETSPHY");
					if (meetingassetsphy != null) {
						datas.put("meetingassetsphy", meetingassetsphy);
					} else {
						datas.put("meetingassetsphy", "");
					}

					String startdate = rs.getString("STARTDATE");
					datas.put("startdate", startdate);
					String enddate = rs.getString("ENDDATE");
					datas.put("enddate", enddate);
					String issendnotice = rs.getString("ISSENDNOTICE");
					datas.put("issendnotice", issendnotice);
					String zqxydkssj = rs.getString("ZQXYDKSSJ");
					datas.put("zqxydkssj", zqxydkssj);
					String zqxydjssj = rs.getString("ZQXYDJSSJ");
					datas.put("zqxydjssj", zqxydjssj);
				}

				try {
					// Calendar calendar = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

					String sdate = datas.get("zqxydkssj");
					String edate = datas.get("zqxydjssj");

					// Date startdate = sdf.parse(sdate); // 获得会议的开始时间
					// Date enddate = sdf.parse(edate); // 获得会议的结束时间

					long num = DateDifferent(sdate, edate); // 获得周期性会议申请的天数
					// for (long i = 1; i <= num; i++) {
					String stempdate = sdate.split(" ")[0];
					String etempdate = edate.split(" ")[0];
					String[] stdate = stempdate.split("-");
					String[] etdate = etempdate.split("-");

					List<String> tempweeks = weekUtils(stdate[0], stdate[1],
							stdate[2], etdate[0], etdate[1], etdate[2], sqxq);

					for (int j = 0; j < tempweeks.size(); j++) {
						int maxid = Integer.parseInt(DBSql.getString(
								"select max(id) id from BO_MEETING_ROOM_ORDER",
								"id"));
						maxid++;
						// to_date('2014-01-20 12:30:00','yyyy-mm-dd
						// hh24:mi:ss')
						sql = "INSERT INTO BO_MEETING_ROOM_ORDER m("
								+ "m.id,m.orgno,m.bindid,m.createuser,"
								+ "m.ordername,m.meetingname,m.meetingpersons,"
								+ "m.meetingroomno,m.departmentname,m.meetingdate,"
								+ "m.status,m.startdate,m.enddate,m.starttime,"
								+ "m.endtime,m.yhry,m.qtry,m.meetingtitle,"
								+ "m.meetingassettyy,m.meetingassettsdn,"
								+ "m.meetingassetsphy,m.issendnotice) VALUES "
								+ "("
								+ maxid
								+ ",'"
								+ datas.get("orgno")
								+ "','"
								+ datas.get("bindid")
								+ "','"
								+ datas.get("createuser")
								+ "',"
								+ "'"
								+ datas.get("ordername")
								+ "','"
								+ datas.get("meetingname")
								+ "','"
								+ datas.get("meetingpersons")
								+ "',"
								+ "'"
								+ datas.get("meetingroomno")
								+ "','"
								+ datas.get("departmentname")
								+ "',to_date('"
								+ tempweeks.get(j)
								+ "','yyyy-mm-dd'),"
								+ "'"
								+ datas.get("status")
								+ "',to_date('"
								+ tempweeks.get(j)
								+ " "
								+ datas.get("startdate").split(" ")[1]
								+ "','yyyy-mm-dd hh24:mi:ss'),to_date('"
								+ tempweeks.get(j)
								+ " "
								+ datas.get("enddate").split(" ")[1]
								+ "','yyyy-mm-dd hh24:mi:ss'),"
								+ "'"
								+ datas.get("starttime")
								+ "','"
								+ datas.get("endtime")
								+ "','"
								+ datas.get("yhry")
								+ "','"
								+ datas.get("qtry")
								+ "','"
								+ datas.get("meetingtitle")
								+ "',"
								+ "'"
								+ datas.get("meetingassettyy")
								+ "','"
								+ datas.get("meetingassettsdn")
								+ "','"
								+ datas.get("meetingassetsphy")
								+ "','"
								+ datas.get("issendnotice") + "')";

						ps = conn.prepareStatement(sql);
						ps.executeUpdate();
					}

					// }

				} catch (ParseException e) {
					e.printStackTrace(System.err);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, null, null);
		}
	}

	/**
	 * 
	 * @param sart
	 * @param end
	 * @return
	 * @throws ParseException
	 * @description 返回周期预定的天数
	 * @version 1.0
	 * @author huh
	 * @update 2014-1-17 下午7:35:41
	 */
	public static long DateDifferent(String sart, String end)
			throws ParseException {
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = sdf.parse(sart);
		Date date2 = sdf.parse(end);

		calendar1.setTime(date1);
		calendar2.setTime(date2);
		long milliseconds1 = calendar1.getTimeInMillis();
		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		// long diffSeconds = diff / 1000;
		// long diffMinutes = diff / (60 * 1000);
		// long diffHours = diff / (60 * 60 * 1000);
		long diffDays = diff / (24 * 60 * 60 * 1000);
		return diffDays;
	}

	/**
	 * 
	 * @param syear
	 * @param smonth
	 * @param sday
	 * @param eyear
	 * @param emonth
	 * @param eday
	 * @param sqxq
	 * @return
	 * @description 获得周期性预定内指定的 预定星期 日期
	 * @version 1.0
	 * @author huh
	 * @update 2014-1-17 下午7:33:38
	 */
	public static List<String> weekUtils(String syear, String smonth,
			String sday, String eyear, String emonth, String eday, String sqxq) {
		List<String> weekLits = new ArrayList<String>();
		Calendar c_begin = new GregorianCalendar();
		Calendar c_end = new GregorianCalendar();
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] weeks = dfs.getWeekdays();

		String[] sqxqs = sqxq.split("\\|");

		c_begin.set(Integer.parseInt(syear), Integer.parseInt(smonth) - 1,
				Integer.parseInt(sday)); // Calendar的月从0-11，所以4月是3.
		c_end.set(Integer.parseInt(eyear), Integer.parseInt(emonth) - 1,
				Integer.parseInt(eday)); // Calendar的月从0-11，所以5月是4.

		int count = 1;
		c_end.add(Calendar.DAY_OF_YEAR, 1); // 结束日期下滚一天是为了包含最后一天

		while (c_begin.before(c_end)) {
			String tempxq = weeks[c_begin.get(Calendar.DAY_OF_WEEK)];
			for (int i = 0; i < sqxqs.length; i++) {
				if (xqys.get(sqxqs[i]) != null && xqys.get(sqxqs[i]).equals(tempxq)) {
					weekLits.add(new java.sql.Date(c_begin.getTime().getTime())
							.toString());
				}
			}
			System.err.println("第" + count + "周  日期："
					+ new java.sql.Date(c_begin.getTime().getTime()) + ","
					+ weeks[c_begin.get(Calendar.DAY_OF_WEEK)]);

			if (c_begin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				count++;
			}
			c_begin.add(Calendar.DAY_OF_YEAR, 1);
		}
		System.out.println(weekLits);
		return weekLits;
	}

}
