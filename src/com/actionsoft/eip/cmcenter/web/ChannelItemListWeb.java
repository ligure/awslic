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

package com.actionsoft.eip.cmcenter.web;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.PageIndex;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.cache.CmSchemaCache;
import com.actionsoft.eip.cmcenter.dao.CmContent;
import com.actionsoft.eip.cmcenter.dao.CmDaoFactory;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmContentModel;
import com.actionsoft.eip.cmcenter.model.CmContentReadModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaModel;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;

/**
 * @author David,Yang
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class ChannelItemListWeb extends ActionsoftWeb {

	public ChannelItemListWeb(UserContext me) {
		super(me);

	}

	public ChannelItemListWeb() {
		super();
	}

	/**
	 * 获得栏目更多信息列表，查看当前栏目更多信息
	 * 
	 * @param ChannelId
	 * @return
	 */
	public String getChannelItemListPage(int ChannelId, String schemaId, int pageNow) {
		int lineCount = 0;
		int pageCount = 0;
		String currRole = this.getContext().getRoleModel().getRoleName();
		int lineNumber = 12;
		int i = 0, ii = 0;
		int lineFirst = lineNumber * (pageNow - 1);
		UserContext me = this.getContext();
		Hashtable channelItemList = this.getChannelItemList(me, ChannelId);
		StringBuffer list = new StringBuffer();
		String title = "";
		if (ChannelId > 0) {
			CmChannelModel cmChannelModel = (CmChannelModel) CmChannelCache.getModel(ChannelId);
			String s = I18nRes.findValue(getContext().getLanguage(),"信息资讯中心");
			if (schemaId != null && !"".equals(schemaId)) {
				try {
					CmSchemaModel sm = (CmSchemaModel) CmSchemaCache.getModel(Integer.parseInt(schemaId));
					if (sm != null) {
						s = sm._schemaName;
					}
				} catch (Exception e) {
				}
			}
			//title = "<a href ='' onClick=\"execMyCommand2(frmMain,'CmChannel_Release_Open');return false;\">" + s + "</a>\\" + I18nRes.findValue(getContext().getLanguage(),cmChannelModel._channelName);
			title = I18nRes.findValue(getContext().getLanguage(),cmChannelModel._channelName);
		}
		if (channelItemList != null) {
			list.append("<table width = 100% border=0 cellspacing=0 cellpadding=0 style=\"border-width:1px;border-left-style:solid;border-right-style:none;border-top-style:solid;border-bottom-style:solid\">");
			list.append("<tr style=\"background-color:#EFEFE7\"><td style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\" width=40% height='30px' >" +
					"&nbsp;<b>&nbsp;&nbsp;标&nbsp;&nbsp;&nbsp;&nbsp;题</b></td><td width=15% align = center height='30px' style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\"><b>创建人</b></td><td width=20% align = center height='30px' style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\"><b>创建时间</b></td></tr>");
			for (int p = 0; p < channelItemList.size(); p++) {
				CmContentModel model = (CmContentModel) channelItemList.get(new Integer(p));
				UserContext uc = null;
				// ===================权限过滤===================================
				if (model._securitylist != null) {
					if (!"".equals(model._securitylist)) {
						if (!CmUtil.getContentSecurityList(currRole, model._securitylist)) {
							continue;
						}
					}
				}
				if (!CmUtil.getContentSecurityList(this.getContext(), model._bindId)) {
					continue;
				}
				try {
				    uc = new UserContext(model._createuser);
				} catch (Exception e) {
				    try {
					uc = new UserContext("admin");
				    } catch (Exception ee) {
					ee.printStackTrace(System.err);
				    }
				    e.printStackTrace(System.err);
				}
				String rowWeb = new CmUtil().getChannelModelWeb(model, ChannelId, uc);
				if (!rowWeb.equals("")) {
				    if (++i > lineFirst) {
					if (++ii > lineNumber) {
					    continue;
					}
					//list.append("<tr><td style=\"border-width:1px;border-left-style:none;border-right-style:none;border-top-style:none;border-bottom-style:none\">");
					list.append(rowWeb);
					//list.append("</td></tr>");
				    }
				} else {
				    continue;
				}

			}
			list.append("</table>");
			lineCount = i;
			pageCount = (lineCount / lineNumber) + 1;
			//System.out.println("list.toString=="+list.toString());
		}
		Hashtable hashTags = new Hashtable();
		if (!LICENSE.getASPModel()) {// 如果是非ASP运营模式
			hashTags.put("more", "<a href=''  onClick=\"execMyCommand(frmMain,'CmContent_Search_Open');return false;\"><img src=../aws_img/find_obj.gif border=0>查询更多信息...</a>");
		} else {
			hashTags.put("more", "");
		}
		hashTags.put("searchKey", "");
		hashTags.put("TopTen", getTopTenInfoWeb());
		hashTags.put("title", title);
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("ContentList", list.toString());
		hashTags.put("channelId", new Integer(ChannelId));
		hashTags.put("schemaId", schemaId);
		hashTags.put("pageIndex", new PageIndex("CmChannel_Item_List_Open", pageNow, lineCount, lineNumber).toString());
		hashTags.put("pageNow", Integer.toString(pageNow));
		hashTags.put("searchCondition", "");
		hashTags.put("advanceSearchCondition", new ContentReleaseSearchWeb(super.getContext()).getReleaseSearchConditionHtml());
		hashTags.put("readUser", "");
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("CmContent_MorePage.htm"), hashTags);
	}

	/**
	 * 
	 * @return
	 */
	public String getTopTenInfoWeb() {

		ArrayList list = this.getTopTenInfoList();
		StringBuffer html = new StringBuffer();
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		html.append("<table>");
		for (int i = 0; i < list.size(); i++) {
			CmContentReadModel ccrm = (CmContentReadModel) list.get(i);
			CmContentModel model = cmContent.getInstance(ccrm._contentId);
			int count = ccrm._readCount;
			html.append("<tr>");
			if (model._title.length() > 10) {
				html.append("<td>").append(model._title.substring(0, 9)).append("...").append("</td>").append("<td>").append(count).append("</td>");
			} else {
				html.append("<td>").append(model._title).append("</td>").append("<td>").append(count).append("</td>");
			}
			html.append("<tr>");
		}
		html.append("</table>");
		return html.toString();
		
	}

	/**
	 * 获得点击率前10的信息记录
	 * 
	 * @return
	 */
	private ArrayList getTopTenInfoList() {
		int rowNum = 10;
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;
		String currRole = this.getContext().getRoleModel().getRoleName();
		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		CmContentReadModel ccrm = null;
		ArrayList list = new ArrayList();
		int i = 0;
		String sql = "select count(*) as num ,EIP_CMCONTENTREAD.contentid   from eip_cm_content ,EIP_CMCONTENTREAD where EIP_CMCONTENTREAD.contentid = eip_cm_content.id and isclose='否' and SUBPICTURE <>'' group by contentid  order by num desc";
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			while (rset.next()) {
				ccrm = new CmContentReadModel();
				int contentid = rset.getInt("contentid");
				int count = rset.getInt("num");
				CmContentModel model = cmContent.getInstance(contentid);
				if (model._securitylist == null || "".equals(model._securitylist)) {
					if (CmUtil.getContentSecurityList(currRole, model._securitylist)) {
						if (i > rowNum) {
							break;
						}
						i++;
					}
				} else {
					if (i > rowNum) {
						break;
					}
					i++;
				}
				ccrm = new CmContentReadModel();
				ccrm._contentId = contentid;
				ccrm._readCount = count;

				list.add(ccrm);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}

	/**
	 * 返回一个指定方案得所有栏目内容，如果schemaId=-1 表示要获取所有栏目得数据
	 * 
	 * @param cmSchemaModel
	 *            方案模型
	 * @param line
	 *            显示得行数
	 * @return
	 * .
	 */
	private Hashtable getChannelItemList(UserContext me, int channelId) {
		Hashtable list = new Hashtable();

		CmContent cmContent = (CmContent) CmDaoFactory.createContent();
		java.sql.Connection conn = DBSql.open();
		java.sql.Statement stmt = null;
		java.sql.ResultSet rset = null;

		String sql = "";
		if (!LICENSE.getASPModel()) {// 如果是非ASP运营模式
			sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + me.getLanguage() + "' and channelid=" + channelId + " order by eip_cm_content.releasedate desc";
		} else {
			sql = "select eip_cm_content.*,eip_cm_channelcont.channelid cid from eip_cm_content ,eip_cm_channelcont where eip_cm_content.id=contentid and eip_cm_content.orgno='" + me.getCompanyModel().getId() + "' and isclose='否' and displaytitle is not null and releasedate is not null and language = '" + me.getLanguage() + "' and channelid=" + channelId + " order by eip_cm_content.releasedate desc";
		}
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			int i = 0;
			while (rset != null && rset.next()) {
				CmContentModel model = (CmContentModel) cmContent.record2Model(rset);
				model._channelId = rset.getInt("cid");
				list.put(new Integer(i), model);
				// if (i == line)
				// break;
				i++;
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		return list;
	}

}