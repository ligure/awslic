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

package com.actionsoft.eip.cmcenter.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.commons.security.ac.cache.AccessControlCache;
import com.actionsoft.awf.commons.security.ac.model.AccessControlModel;
import com.actionsoft.awf.commons.security.ac.util.AccessControlUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.cache.CmSchemaCache;
import com.actionsoft.eip.cmcenter.dao.CmContent;
import com.actionsoft.eip.cmcenter.dao.CmDaoFactory;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmContentModel;
import com.actionsoft.eip.cmcenter.model.CmContentReadModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaChannelModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaModel;
import com.actionsoft.eip.cmcenter.web.ChannelReleaseWeb;

/**
 * @author David.yang
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CmUtil {
	
	//优化，一次查出所有
	public static Map getVisitStaticOfChannel() {
		String sql = "select " + CmContentReadModel.FIELD_CONTENTID + ",count(*) as c from " + CmContentReadModel.DATABASE_ENTITY + " group by " + CmContentReadModel.FIELD_CONTENTID;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Map cMap = new HashMap();
		try {
			conn = DBSql.open();
			stmt = conn.createStatement();
			rs = DBSql.executeQuery(conn, stmt, sql);
			if (rs != null) {
				while (rs.next()) {
					cMap.put(rs.getInt(CmContentReadModel.FIELD_CONTENTID), rs.getInt("c"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(conn, stmt, rs);
		}

		return cMap;
	}
	
	/**
	 * 获得栏目下单条记录的WEB
	 * @param contentModel
	 * @param channelId
	 * @return
	 */
	public String getChannelModelWeb(CmContentModel contentModel,int channelId,UserContext uc){
		StringBuffer list = new StringBuffer();
			if(contentModel!=null){
					if (contentModel._channelId == channelId && contentModel._releaseDate != null && contentModel._releaseDate.toString().length()>0) {
						String releaseDate = contentModel._releaseDate.toString();
						if(contentModel._releaseDate.toString().indexOf(" ")>0){
							releaseDate = contentModel._releaseDate.toString().substring(0, contentModel._releaseDate.toString().indexOf(" "));
						} 
						String resultDate = UtilDate.dateFormat(new Date());
						String newImg="";
						int clickCount =0;
						int talkCount=0;
						try {
							Hashtable clickHash=CmDaoFactory.createContentRead().getVisitInstance(contentModel._id);
							clickCount = clickHash==null?0:clickHash.size();
							Hashtable talkHash=CmDaoFactory.createContentRead().getTalkInstance(contentModel._id);
							talkCount = talkHash==null?0:talkHash.size();
						} catch (RuntimeException e) {
							e.printStackTrace(System.err);
						} 
						DepartmentModel dtpModel= uc.getDepartmentModel();
						System.out.println("dtpModel="+dtpModel.getDepartmentName());
						UserModel uModel=uc.getUserModel();
						if(dtpModel==null || uModel==null)return "";
						if(contentModel._positionType==0){
							//拼凑一个完整的Table
							//list.append("<table width=100% border=1 cellspacing=0 cellpadding=0   >\n");
							if (CmUtil.getSubtractionDate(releaseDate, resultDate) <= 3) {
								newImg = "<img src='../aws_img/new1.gif' border='0'> ";
								list.append(getChannelModelContext(contentModel,newImg,dtpModel,releaseDate,uModel,channelId,talkCount,clickCount));
							} else {
								newImg = "";
								list.append(getChannelModelContext(contentModel,newImg,dtpModel,releaseDate,uModel,channelId,talkCount,clickCount));
							}
							//list.append("</table>\n");
							
						}
					}
			}
		return list.toString();
	}
	private String getChannelModelContext(CmContentModel contentModel,String newImg,DepartmentModel dtpModel,String releaseDate,UserModel uModel,int channelId,int talkCount,int clickCount){
		StringBuffer list = new StringBuffer();
		if(contentModel._title.indexOf("WORKFLOW:")!=-1){
			list.append("<tr><td width=2%><img src = '../aws_skins/_def54/img/link2.gif' height=\"10px\" width=\"5px\" border=0>&nbsp;</td>")
			.append("<td width=46% height='30px'><b><a href=''  onclick=\"execMyCommand2(frmMain,").append(contentModel._bindId).append(",8,'WorkFlow_Execute_Worklist_File_Open'); return false;\">").append(ChannelReleaseWeb.getCMSTitle(contentModel)).append("</a></b>").append(newImg)
			//.append("&nbsp;&nbsp;<font color='#999999'><I18N#讨论>").append(talkCount).append("<I18N#次>&nbsp;&nbsp;<I18N#点击>:").append(clickCount).append("<I18N#次></font>")
			.append("</td>")
		//	.append("<td  width=25% align = right>").append(dtpModel.getDepartmentFullNameOfCache()).append("</td>")
			.append("<td  width=15% align = right>").append(Function.getAddressFullName(uModel.getUID())).append("</td>")
			.append("<td width=20% align = right>&nbsp;").append(releaseDate).append("</td><td width=2%>&nbsp;</td></tr>\n");
		}else{
			list.append("<tr><td width=40% height='30px' style=\" border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\">&nbsp;&nbsp;<img src = '../aws_skins/_def54/img/pin.png' height=\"10px\"   border=0>&nbsp;<b><a href=''  onClick=\"openContent(frmMain,").append(-1).append(",").append(channelId).append(",").append(contentModel._id).append(",'CmContent_Read_Open');return false;\">&nbsp;&nbsp;").append(ChannelReleaseWeb.getCMSTitle(contentModel)).append("</a></b>")
			//.append(newImg).append("&nbsp;&nbsp;<font color='#999999'><I18N#讨论>").append(talkCount).append("<I18N#次>&nbsp;&nbsp;<I18N#点击>").append(clickCount).append("<I18N#次></font>")
			.append("</td>")
		//	.append("<td width=25% align = right>").append(dtpModel.getDepartmentFullNameOfCache()).append("</td>")
			
			.append("<td width=15% align = center height='30px' style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\">").append(Function.getUserNameList(uModel.getUID())).append("</td>")
			.append("<td width=20% align = center height='30px' style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\">&nbsp;").append(releaseDate).append("</td></tr>\n");
		}
		return list.toString();
	}
	
	
	/**
	 * 根据传入的条件，获得搜索结果列表
	 * @param searchKey
	 * @return
	 */
	public static Map getSearchResultSet(String searchKey,UserContext uc,String channelId){
		Map list = new HashMap();
		Hashtable channelList = CmChannelCache.getACNotCloseChannelList(uc);
		Hashtable h=CmUtil.getCMSList(uc,channelList,channelId);
		if(h!=null){
			for(int i=0;i<h.size();i++){
				if(searchKey==null ){
					continue;
				}
				CmContentModel cModel=(CmContentModel)h.get(new Integer(i));
				if(cModel._securitylist!=null&&"".equals(cModel._securitylist)){
					if (!CmUtil.getContentSecurityList(uc,cModel._bindId)) {
						continue;
					}
				}
				if(cModel._archives.indexOf(searchKey)>=0||
					cModel._displaytitle.indexOf(searchKey)>=0||
					cModel._content.indexOf(searchKey)>=0||
					cModel._isClose.indexOf("否")>0||
					cModel._releaseDepartment.indexOf(searchKey)>0||
					cModel._releaseMan.indexOf(searchKey)>0||
					cModel._subTitle.indexOf(searchKey)>=0||
					cModel._title.indexOf(searchKey)>=0){
					list.put(list.size(), cModel);
				}
			}
		}
		return list;
	}
	
	/**
	 * 返回最新的图片新闻列表
	 * @return
	 */
	public static Hashtable getPictureInfo(UserContext uc){
		int rowNum = 5;
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		Hashtable list = new Hashtable();
		int i=0;
		 String sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否'  order by releasedate desc";
		 try {
				stmt = conn.createStatement();
				rset = DBSql.executeQuery(conn, stmt, sql);
				while (rset.next()) {
					CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
					model._channelId = rset.getInt("cid");
					String archives = rset.getString("SUBPICTURE");
					if(AccessControlUtil.accessControlCheck(uc,"SYS_CMCHANNEL",model._channelId+"","R")){
						if(archives==null||"".equals(archives)){
							continue;
						}
						list.put(new Integer(list.size()), model);
//						if (i>rowNum){
//							break;
//						}
						i++;
					}
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace(System.err);
			} finally {
				DBSql.close(conn, stmt, rset);
			}
		return list;
	}

	/**
	 * 求时间差
	 * @param inputDate：被减时间
	 * @param resultDate
	 * @return
	 */
	public static long getSubtractionDate(String inputDate, String resultDate) {
		if (!inputDate.equals("") && !resultDate.equals("") && !resultDate.equals("&nbsp;")) {
			String year = inputDate.substring(0, 4);
			String month = inputDate.substring(5, 7);
			String day = inputDate.substring(8, 10);
			String resultYear = resultDate.substring(0, 4);
			String resultMonth = resultDate.substring(5, 7);
			String resultDay = resultDate.substring(8, 10);
			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
			//   cal.set(2004, Integer.parseInt("05"), 1);
			Date d1 = cal.getTime();
			cal.set(Integer.parseInt(resultYear), Integer.parseInt(resultMonth), Integer.parseInt(resultDay));

			//   cal.set(2004, Integer.parseInt("06"), 1);
			Date d2 = cal.getTime();
			long daterange = d2.getTime() - d1.getTime();
			long time = 1000 * 3600 * 24; //A day in milliseconds

			return daterange / time;
		} else {
			return -1;
		}
	}
	
	/**
	 * 返回一个指定方案得最新内容，如果schemaId=-1
	 * 表示要获取所有栏目得最新数据
	 * 
	 * @param cmSchemaModel 方案模型
	 * @param line	显示得行数
	 * @return
	 * .
	 */
	public static Hashtable getQuickViewCMSList2(UserContext me,int line,Hashtable channelList) {
		Hashtable list = new Hashtable();
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;
		//SQL语句得Schema包含的栏目，组成Where过滤片断
		StringBuffer filterWhere = new StringBuffer();
		if (channelList != null && channelList.size() > 0) {
			for (int i = 0; i < channelList.size(); i++) {
				CmChannelModel model = (CmChannelModel) channelList.get(new Integer(i));
				if (filterWhere.length() == 0) {
					filterWhere.append(" and (channelid=").append(model._id);
				} else {
					filterWhere.append(" or channelid=").append(model._id);
				}
			}
			filterWhere.append(") ");
		} else {
			filterWhere.append(" and 1=2 ");
		}
		String sql ="";
		if (!LICENSE.getASPModel()) {//如果是非ASP运营模式
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}else{
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_content.orgno='"+me.getCompanyModel().getId()+"' and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			while (rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(list.size()), model);
				if (list.size() == line)
					break;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}
		/**
	 * 返回一个指定方案得所有栏目内容，如果schemaId=-1
	 * 表示要获取所有栏目得数据
	 * 
	 * @param cmSchemaModel 方案模型
	 * @param line	显示得行数
	 * @return
	 * .
	 */
	public static Hashtable getCMSList(UserContext me,Hashtable channelList,String channelId) {
		int maxline = 3000000;
		Hashtable list = new Hashtable();
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		Connection conn = DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		//SQL语句得Schema包含的栏目，组成Where过滤片断
		StringBuffer filterWhere = new StringBuffer();
		//传递过一个共享的list，这是为了优化设计的
		if (channelList != null && channelList.size() > 0) {
			for (int i = 0; i < channelList.size(); i++) {
				CmChannelModel model = (CmChannelModel) channelList.get(new Integer(i));
				if (filterWhere.length() == 0) {
					filterWhere.append(" and (channelid=").append(model._id);
				} else {
					filterWhere.append(" or channelid=").append(model._id);
				}
			}
			filterWhere.append(") ");
		} else {
			filterWhere.append(" and 1=2 ");
		}
		String sql="";
		if (!LICENSE.getASPModel()) {//如果是非ASP运营模式
		    //sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
			sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_channelcont.channelid ="+Integer.valueOf(channelId )+" and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + me.getLanguage() + "' " + filterWhere.toString() + " order by eip_cm_content.releasedate desc";
		}else{
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_content.orgno='"+me.getCompanyModel().getId()+"' and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + me.getLanguage() + "' " + filterWhere.toString() + " order by eip_cm_content.releasedate desc";
		}
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			int i = 0;
			while (rset!=null && rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(i), model);
				if (i == maxline)
					break;
				i++;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}
	/**
	 * 
	 * @param me
	 * @param channelList
	 * @param line
	 * @return
	 */
	public static Hashtable getCMSList(UserContext me,Hashtable channelList,int line) {
		int maxline = line;
		Hashtable list = new Hashtable();
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		Connection conn = DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		//SQL语句得Schema包含的栏目，组成Where过滤片断
		StringBuffer filterWhere = new StringBuffer();
		//传递过一个共享的list，这是为了优化设计的
		if (channelList != null && channelList.size() > 0) {
			for (int i = 0; i < channelList.size(); i++) {
				CmChannelModel model = (CmChannelModel) channelList.get(new Integer(i));
				if (filterWhere.length() == 0) {
					filterWhere.append(" and (channelid=").append(model._id);
				} else {
					filterWhere.append(" or channelid=").append(model._id);
				}
			}
			filterWhere.append(") ");
		} else {
			filterWhere.append(" and 1=2 ");
		}
		String sql="";
		if (!LICENSE.getASPModel()) {//如果是非ASP运营模式
		    //sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
			sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}else{
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_content.orgno='"+me.getCompanyModel().getId()+"' and isclose='否' " + filterWhere.toString() + " order by  releasedate desc";
		}
		//System.out.println(sql);
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			int i = 0;
			while (rset!=null && rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(i), model);
				if (i == maxline)
					break;
				i++;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}
	/**
	 * cms content earch 
	 * @param me
	 * @param channelList
	 * @param conditionModel
	 * @return
	 */
	public static Map getCMSSearchList(UserContext me,Hashtable channelList,CmContentModel conditionModel){
		StringBuffer searchSql = new StringBuffer();
		StringBuffer filterWhere = new StringBuffer();
		System.out.println(channelList.size() );
		if (channelList != null && channelList.size() > 0) {
			for (int i = 0; i < channelList.size(); i++) {
				CmChannelModel model = (CmChannelModel) channelList.get(new Integer(i));
				if (filterWhere.length() == 0) {
					filterWhere.append(" and (channelid=").append(model._id);
				} else {
					filterWhere.append(" or channelid=").append(model._id);
				}
			}
			filterWhere.append(") ");
		}
		searchSql.append("select content.*,channelcont.channelid cid from eip_cm_content content,eip_cm_channelcont channelcont where content.id = channelcont.contentid ");
		searchSql.append(filterWhere);
		searchSql.append(" and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + me.getLanguage() + "' ");
		//conditionModel append
		if(conditionModel != null){
			if(conditionModel._releaseMan.trim().length() > 0){
				searchSql.append(" and content.createuser = '").append(conditionModel._releaseMan).append("'");
			}
			if(conditionModel._releaseDepartment.trim().length() > 0){
				searchSql.append(" and source like '%").append(conditionModel._releaseDepartment).append("%' ");
			}
			if(conditionModel._isTalk.trim().length() > 0){
				searchSql.append(" and istalk = '").append(conditionModel._isTalk).append("' ");
			}
			if(conditionModel._content.trim().length() > 0){
				searchSql.append(" and content like '%").append(conditionModel._content).append("%' ");
			}
			if(conditionModel._title.trim().length() > 0){
				searchSql.append(" and DISPLAYTITLE like '%").append(conditionModel._title).append("%' ");
			}
			if(conditionModel._releaseDate != null){
				String releaseDate = conditionModel._releaseDate.toString();
				if(releaseDate.trim().length() > 0){
					releaseDate = releaseDate.substring(0,releaseDate.indexOf(" "));
				}
				searchSql.append(" and releasedate >= ").append(DBSql.convertShortDate(releaseDate)).append(" ");
			}
			if(conditionModel._subTitle .trim().length() > 0){
				searchSql.append(" and subTitle  like '%").append(conditionModel._subTitle).append("%' ");
			}
		}
		searchSql.append(" order by content.releasedate desc ");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		Map list = new HashMap();
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		try{
			conn = DBSql.open();
			stmt = conn.createStatement();
			rset = stmt.executeQuery(searchSql.toString());
			int i = 0;
			while (rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(i), model);
				i++;
			}
		}catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}
	/**
	 * 获取设置的权限列表
	 * @param 
	 * @return
	 * @author dunan 2009-08-07
	 */
	public static boolean getContentSecurityList(UserContext uc,int id){
		boolean isRead=false;
		//查看当前项是否设置了分级授权
		boolean isRSeuritySetting = false;
		Map acList = AccessControlCache.getACList("CMS_CONTENT_AC", Integer.toString(id));
		for (int i = 0; i < acList.size(); i++) {
			AccessControlModel acModel = (AccessControlModel) acList.get(new Integer(i));
			if (acModel._acType.equals("R"))
				isRSeuritySetting = true;
		}
		if(isRSeuritySetting){
			//是否设置可读
			isRead = AccessControlUtil.accessControlCheck(uc, "CMS_CONTENT_AC", Integer.toString(id), "R");
			
		}else{
			isRead = true;
		}
		return isRead;
	}
	/**
	 * 获取设置的角色列表
	 * @param str
	 * @return
	 * update by dunan 2009-08-07
	 */
	
	public static boolean getContentSecurityList(String currRole,String str){
		String[] securityList = null;
		boolean flag = false;
		if(str!=null){
			if(!str.equals("")){
				securityList = str.split(" ");
				for(int i = 0;i<securityList.length;i++){
					if(currRole.equals(securityList[i].trim())){
						flag = true;
						break;
					}
				}
			}else{
				flag = true;
			}
			
		}else{
			flag = true;
		}
		return flag;
	}
	 
	/**
	 * 返回一个指定方案得所有栏目内容，如果schemaId=-1
	 * 表示要获取所有栏目得数据
	 * 
	 * @param cmSchemaModel 方案模型
	 * @param line	显示得行数
	 * @return
	 * .
	 */
	public static Hashtable getCMSList(UserContext me, CmSchemaModel cmSchemaModel, CmChannelModel cmChannelModel, int line,Hashtable channelList) {
		Hashtable list = new Hashtable();

		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;

		//SQL语句得Schema包含的栏目，组成Where过滤片断
		StringBuffer filterWhere = new StringBuffer();
		//传递过一个共享的list，这是为了优化设计的
		//所以注释了下面的代码
		//		if (cmSchemaModel != null && cmChannelModel == null) {
//			channelList = CmSchemaChannelCache.getNotCloseChannelList(me, cmSchemaModel._id);
//		} else {
//		    channelList = CmSchemaChannelCache.getACNotCloseChannelList(me);
//		}
		if (channelList != null && channelList.size() > 0) {
			for (int i = 0; i < channelList.size(); i++) {
				CmSchemaChannelModel model = (CmSchemaChannelModel) channelList.get(new Integer(i));
				if (filterWhere.length() == 0) {
					filterWhere.append(" and (channelid=").append(model._channelId);
				} else {
					filterWhere.append(" or channelid=").append(model._channelId);
				}
			}
			filterWhere.append(") ");
		} else {
			filterWhere.append(" and 1=2 ");
		}
		//SQL语句组成栏目Where过滤片断
		if (cmChannelModel != null) {
			filterWhere.append(" and (channelid=").append(cmChannelModel._id).append(")");
		}
		String sql="";
		if (!LICENSE.getASPModel()) {//如果是非ASP运营模式
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}else{
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_content.orgno='"+me.getCompanyModel().getId()+"' and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			int i = 0;
			while (rset!=null && rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(i), model);
				if (i == line)
					break;
				i++;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}
	
	/**
	 * 返回一个指定方案得最新内容，如果schemaId=-1
	 * 表示要获取所有栏目得最新数据
	 * 
	 * @param cmSchemaModel 方案模型
	 * @param line	显示得行数
	 * @return
	 * .
	 */
	public static Hashtable getQuickViewCMSList(UserContext me,int line,Hashtable channelList) {
		Hashtable list = new Hashtable();

		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;
		//SQL语句得Schema包含的栏目，组成Where过滤片断
		StringBuffer filterWhere = new StringBuffer();
		if (channelList != null && channelList.size() > 0) {
			for (int i = 0; i < channelList.size(); i++) {
				CmSchemaChannelModel model = (CmSchemaChannelModel) channelList.get(new Integer(i));
				if (filterWhere.length() == 0) {
					filterWhere.append(" and (channelid=").append(model._channelId);
				} else {
					filterWhere.append(" or channelid=").append(model._channelId);
				}
			}
			filterWhere.append(") ");
		} else {
			filterWhere.append(" and 1=2 ");
		}
		String sql ="";
		if (!LICENSE.getASPModel()) {//如果是非ASP运营模式
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}else{
		    sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_content.orgno='"+me.getCompanyModel().getId()+"' and isclose='否' " + filterWhere.toString() + " order by releasedate desc";
		}
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			while (rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(list.size()), model);
				if (list.size() == line)
					break;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}
	
	
	/**
	 * 分割字符串
	 * 
	 * @param str
	 * @param delim
	 * @return Object [] 
	 * 
	 */
	public static Object [] stringTokenizer(String str,String delim) {
		StringTokenizer st = new StringTokenizer(str,delim);
		String ss;
		String [] s = null;
		ArrayList result = new ArrayList();
		while(st.hasMoreTokens()) {
			ss = st.nextToken();
			result.add(ss);
		}
		if(result.size() > 0) {
        	s = new String[result.size()];
            result.toArray(s);
        }
		return s;
	}
	/**
	 * 根所schema配置信息，处理releaseData字符串
	 * @param schemaId
	 * @return
	 */
	public static String getReleaseDateStr(int schemaId, int channelId, Timestamp releaseDate) {
		CmSchemaModel model = (CmSchemaModel) CmSchemaCache.getModel(schemaId);
		if (model == null && channelId > 0) {
			CmChannelModel channel = (CmChannelModel) CmChannelCache.getModel(channelId);
			if (channel != null) {
				Map schemaHash = CmSchemaCache.getList();
				for (int i = 0; i < schemaHash.size(); i++) {
					CmSchemaModel schemaModel = (CmSchemaModel) schemaHash.get(new Integer(i));
					if (channel._channelStyleId.equals(schemaModel._schemaStyleId) || channel._channelStyleId.equals(String.valueOf(schemaModel._id))) {
						model = schemaModel;
						break;
					}
				}
			}
		}

		String dtf = "yyyy-MM-dd";
		if (model != null && model._dtf != null && model._dtf.trim().length() > 0) {
			dtf = model._dtf;
		}
		return releaseDate == null ? "" : new SimpleDateFormat(dtf).format(releaseDate);
	}
	
	/** 判断是否是当前栏目的管理员
	 * 
	 * @param channelModel
	 * @return
	 */
	 public static boolean isChannelManager(CmChannelModel channelModel,UserContext user) {
		boolean flag = false;
		String currUser = user.getUID();
		 if (currUser.equals("admin")) { // 超级管理员具有绝对权限
				flag = true;
		}else if ("".equals(channelModel._channelManager)	|| channelModel._channelManager == null) {
			flag = false;
		} else {
			String[] managerList = (channelModel._channelManager).split(" ");
			for (int i = 0; i < managerList.length; i++) {
				if (Function.getUID(managerList[i].trim()).equals(currUser)) { // 如果管理员列表与当前用户相匹配，返回true
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
   /**
	* 判断是否是当前Schema的管理员
	* 
	* @param channelModel
	* @return
	*/
	public static  boolean isSchemaManager(CmSchemaModel schemaModel,UserContext user) {
			String currUser = user.getUID();
			if (currUser.equals("admin")) { // 超级管理员具有绝对权限
				return true;
			}if ("".equals(schemaModel._schemaManager)	|| schemaModel._schemaManager == null) {
				return true;
			}  else {
				String[] managerList = (schemaModel._schemaManager).split(" ");
				for (int i = 0; i < managerList.length; i++) {
					if (Function.getUID(managerList[i].trim()).equals(currUser)) { // 如果管理员列表与当前用户相匹配，返回true
						return true;
					}
				}
			}
			return false;
	}
	public static String getRefreshLeftTreeJavascript(UserContext user){
		StringBuffer javascript = new StringBuffer();
		javascript.append("<script>\n");
		javascript.append(" var treeIframe = parent.document.getElementById('treeiframe');\n");
		javascript.append(" treeIframe.src='./login.wf?sid=" + user.getSessionId() + "&cmd=CmSchema_Tree_Open';\n");
		javascript.append("</script>");
		return javascript.toString();
	}
	
	public static String getRefreshSchemaGridJavascript(UserContext user){
		StringBuffer javascript = new StringBuffer();
		javascript.append("<script>\n");
		javascript.append(" var iframe = parent.document.getElementById('mainiframe');\n");
		javascript.append(" iframe.src='./login.wf?sid=" + user.getSessionId() + "&cmd=CmSchema_List_Open';\n");
		javascript.append("</script>");
		return javascript.toString();
	}
}