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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.PageIndex;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.cache.CmSchemaCache;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmContentModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaModel;
import com.actionsoft.eip.cmcenter.util.CmUtil;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

/**
 * @author highsun
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContentReleaseSearchWeb extends ActionsoftWeb{
	
	private static final String _searchButton = "<input type=button value='<I18N#开始检索>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'CmContent_Search_Action');return false;\"    border='0'>";
	private static final String keyStr = "请输入查询关键字";
	private static final String keyStr1 = "Please enter a query keyword";
	
	private String _pageIndex = "";
	
	public ContentReleaseSearchWeb(UserContext me) {
        super(me);
    }
	
	
	/**
	 * 执行快速搜索查询页面
	 * @param searchKey
	 * @return
	 */
	public String getQuickSearchWeb(String searchKey,int pageNow,int schemaId,String channelId, String advanceSearchCondition){
		String title ="";
		CmSchemaModel schemaModel = (CmSchemaModel)CmSchemaCache.getModel(schemaId);
		if(schemaId != -1 && schemaModel != null){
		//	title = "<a href ='' onClick=\"execMyCommand2(frmMain,'CmChannel_Release_Open');return false;\">" + schemaModel._schemaName + "</a>\\";
		} else {
			//title = "<a href ='' onClick=\"execMyCommand2(frmMain,'CmChannel_Release_Open');return false;\"><I18N#信息资讯中心></a>\\";
		}
		if(searchKey.equals("")||searchKey.equals(keyStr)){
			title += "<I18N#查询>:<I18N#全部> <I18N#结果>";
		}else{
			title += "<I18N#查询>:"+searchKey+"<I18N#结果>";
		}
		int lineCount = 0;
		int pageCount = 0;
	//int lineNumber = getContext().getUserModel().getLineNumber();
		int lineNumber =12;//每业多少行
		int i = 0, ii = 0;
		int lineFirst = lineNumber * (pageNow - 1);
		if(searchKey.equals(keyStr) || searchKey.equals(keyStr1)){
			searchKey="";
		}
		Map list = null;
		CmContentModel searchconditionModel = getReleaseSearchCondition(advanceSearchCondition);
		if(searchconditionModel != null){
			 Hashtable channelList ;
		     if(schemaId != -1){
		    	 channelList = CmChannelCache.getACNotCloseChannelList(this.getContext(),schemaModel);
		     }else {
		    	 channelList = CmChannelCache.getACNotCloseChannelList(this.getContext());
		     }
			list = CmUtil.getCMSSearchList(this.getContext(), channelList, searchconditionModel);
		} else {
			//list = CmUtil.getSearchResultSet(searchKey,this.getContext());
			list = CmUtil.getSearchResultSet(searchKey,this.getContext(),channelId);
		}
		UserContext uc = null;
		StringBuffer rowList = new StringBuffer();
		rowList.append("<table width = 100% border=0 cellspacing=0 cellpadding=0 style=\"border-width:1px;border-left-style:solid;border-right-style:none;border-top-style:solid;border-bottom-style:solid\">");
		rowList.append("<tr style=\"background-color:#EFEFE7\"><td style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\" width=40% height='30px' >" +
				"&nbsp;<b>&nbsp;&nbsp;标&nbsp;&nbsp;&nbsp;&nbsp;题</b></td><td width=15% align = center height='30px' style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\"><b>创建人</b></td><td width=20% align = center height='30px' style=\"border-width:1px;border-left-style:none;border-right-style:solid;border-top-style:none;border-bottom-style:solid\"><b>创建时间</b></td></tr>");
		for(int num = 0; num < list.size() ; num++){
			CmContentModel model = (CmContentModel) list.get(num);
			String currRole = this.getContext().getRoleModel().getRoleName();
			if(model._securitylist!=null){
				if(!"".equals(model._securitylist)){
					if(!CmUtil.getContentSecurityList(currRole,model._securitylist)){
						continue;
					}
				}
			}
			if (!CmUtil.getContentSecurityList(this.getContext(),model._bindId)) {
				continue;
			}
			CmChannelModel cmChannelModel = (CmChannelModel) CmChannelCache.getModel(model._channelId);
			if(cmChannelModel!=null){
				if(cmChannelModel._isClose){
					continue;
				} else if(schemaId != -1 && schemaModel != null){
					if(!cmChannelModel._channelStyleId.equals(String.valueOf(schemaId)) && !cmChannelModel._channelStyleId.equals(schemaModel._schemaStyleId)){
						continue;
					}
				}
			}else{
				continue;
			}
			try {
				uc = new UserContext(model._createuser);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
			i++;
			if (i > lineFirst) {
				ii++;
				if (ii > lineNumber) {
					break;
				}
				//rowList.append("<tr><td>");
				rowList.append(new CmUtil().getChannelModelWeb(model,model._channelId,uc));
				//rowList.append("</td></tr>");
			}
		}
		rowList.append("</table>");
		lineCount = list.size();
		pageCount = (lineCount / lineNumber) + 1;
		
		if(ii<1){
			if (pageNow < pageCount) {//如果当前页没有权限的咨询，将返回下一页
				return getQuickSearchWeb(searchKey, pageNow + 1, schemaId, channelId, advanceSearchCondition);
			}
			
			//rowList.append("<table width=100% style=\"border-width:1px;border-left-style:solid;border-right-style:none;border-top-style:solid;border-bottom-style:solid\"><tr><td align = 'center'>");
			rowList.append("<table width=100% border=0 cellspacing=0 cellpadding=0 style=\"border-width:1px;border-left-style:solid;border-right-style:none;border-top-style:solid;border-bottom-style:solid\">");
			rowList.append("<I18N#未检索到您查询的内容>！");
			rowList.append("</table>");
		}
		Hashtable hashTags = new Hashtable();
		if (!LICENSE.getASPModel()) {//如果是非ASP运营模式
			hashTags.put("more","<a href=''  onClick=\"execMyCommand(frmMain,'CmContent_Search_Open');return false;\"><img src=../aws_img/find_obj.gif border=0></a>");
		}else{
			hashTags.put("more","");
		}
		hashTags.put("title",title);
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("ContentList",rowList.toString());
		if(searchKey.equals("")||searchKey.equals(keyStr)){
		hashTags.put("searchKey",keyStr);
		}else{
	     hashTags.put("searchKey",searchKey);
		}
		hashTags.put("pageIndex", new PageIndex("CmContent_FullSearch", pageNow, lineCount, lineNumber).toString());
		hashTags.put("pageNow", Integer.toString(pageNow));
		hashTags.put("schemaId", String.valueOf(schemaId));
		hashTags.put("channelId", channelId);
		hashTags.put("readUser", "");//??
		hashTags.put("searchKey", searchKey.trim().length() == 0 ? "<I18N#请输入查询关键字>" : searchKey);
		hashTags.put("advanceSearchCondition", new ContentReleaseSearchWeb(super.getContext()).getReleaseSearchConditionHtml());
		hashTags.put("searchCondition", advanceSearchCondition);
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("CmContent_MorePage.htm"), hashTags);
	}
	/**
	 * 拼凑高级搜索选项 条件
	 * @param condition
	 * @return
	 */
	private CmContentModel getReleaseSearchCondition(String condition){
		if(condition.trim().length() == 0){
			return null;
		}
		UtilString myStr = new UtilString(condition);
		CmContentModel model = new CmContentModel();
		model._releaseMan = UtilCode.decode(myStr.matchValue("_releaseManS[", "]releaseManS_"));
		model._releaseDepartment = UtilCode.decode(myStr.matchValue("_releaseDepartmentS[", "]releaseDepartmentS_"));
		String releaseDateS = UtilCode.decode(myStr.matchValue("_releaseDateS[", "]releaseDateS_"));
		if(releaseDateS.trim().length() > 0){
			model._releaseDate = Timestamp.valueOf(releaseDateS + " 00:00:00");
		}
		model._isTalk = UtilCode.decode(myStr.matchValue("_isTalkS[", "]isTalkS_"));
		model._title = UtilCode.decode(myStr.matchValue("_titleS[", "]titleS_"));
		model._subTitle =  UtilCode.decode(myStr.matchValue("_subTitleS[", "]subTitleS_"));
		model._content = UtilCode.decode(myStr.matchValue("_contentS[", "]contentS_"));
		return model;
	}
	/**
	 * 拼凑高级搜索选项 html
	 * @return
	 */
	public String getReleaseSearchConditionHtml(){
		StringBuffer condition = new StringBuffer();
		condition.append("<table width=\"95%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" bgcolor=\"#ffffff\">\n");
		condition.append("<tr>\n");
		condition.append("<td valign=\"middle\"><table width=\"95%\" border=\"0\">\n");
		condition.append("<tr>\n");
		condition.append("<td width=\"20%\" align=\"left\"><I18N#发布人>：</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td width=\"100%\" align=\"left\" nowrap><input type='text' name='releaseManS' id='releaseManS' size=55 maxlength=30 class ='actionsoftInput' value=''> </td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td width=\"15%\" align=\"left\"><I18N#来源包含>：</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td width=\"21%\" align=\"left\"><input type='text' name='releaseDepartmentS' id='releaseDepartmentS' size=55 maxlength=30 class ='actionsoftInput' ></td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><I18N#导读包含>：</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\" nowrap><input type='text' name='subTitleS' id='subTitleS' size=30 maxlength=55 class ='actionsoftInput' ></td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><I18N#标题包含>：</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><input type='text' name='titleS' id='titleS' size=55 maxlength=30 class ='actionsoftInput' ></td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><I18N#发布日期>：</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\" nowrap><select style=\"width:100%\" name=\"releaseDateS\" id=\"releaseDateS\">  <option value=\"\" selected><I18N#请选择...></option>  <option value=\"2012-03-18\" ><I18N#最近一个月内></option>  <option value=\"2011-10-18\" ><I18N#最近半年内></option>  <option value=\"2011-04-18\" ><I18N#最近一年内></option>  <option value=\"2010-04-18\" ><I18N#最近两年内></option></select</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><I18N#是否允许讨论>：</td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><select name='isTalkS' id='isTalkS' style=\"width:100%\"  class ='actionsoftSelect'><option value=''><I18N#请选择...></option><option value='是'><I18N#允许></option><option value='否'><I18N#不允许></option></select></td>\n");
		condition.append("</tr>\n");
		condition.append("<tr>\n");
		condition.append("<td align=\"left\"><img src=\"../aws_img/searchButton.jpg\" style=\"cursor:pointer\" border=\"0\" onClick=\"advanceSearch(frmMain,'CmContent_FullSearch');return false;\"></td>\n");
		condition.append("<td align=\"left\"></td>\n");
		condition.append("</tr>\n");
		condition.append("</table></td>\n");
		condition.append("</tr>\n");
		condition.append("</table>\n");

		return condition.toString();
	}
	
	
	/**
	 * 获取搜索时间段的日期
	 * @param type 0/1/2/3:近一个月内/近半年内/近一年内/近二年内
	 * @return
	 */
	private String getSearchDate(int type){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar  calendar=java.util.Calendar.getInstance();  
		calendar.setTime(new   java.util.Date());  
		if(type == 0 ){//近一个月内
			calendar.set(Calendar.MONDAY,calendar.get(Calendar.MONDAY)-1);  
			calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);  
			return sdf.format(calendar.getTime());
		}else if(type == 1){//近半年内
			calendar.set(Calendar.MONDAY,calendar.get(Calendar.MONDAY)-6);  
			calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);  
			return sdf.format(calendar.getTime());
		}else if(type == 2){//近一年内
			calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-1);  
			calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);  
			return sdf.format(calendar.getTime());
		}else if(type == 3){//近二年内
			calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-2);  
			calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);  
			return sdf.format(calendar.getTime());
		}
		return null;
	}
	/**
	 * 
	 * @param model
	 * @return
	 */
	public Hashtable search(CmContentModel model,int pageNow){
		Hashtable searchList=new Hashtable();
		Hashtable channelList = CmChannelCache.getACNotCloseChannelList(this.getContext());
		Hashtable h = CmUtil.getCMSList(this.getContext(),channelList,"");
		if(h!=null){
			for(int i = 0 ; i < h.size(); i++){
				if(model==null){
					continue;
				}
				if(model._securitylist!=null&&"".equals(model._securitylist)){
					if(!CmUtil.getContentSecurityList(this.getContext().getRoleModel().getRoleName(),model._securitylist)){
						continue;
					}
				}
				if (!CmUtil.getContentSecurityList(this.getContext(),model._bindId)) {
					continue;
				}
				String releaseDate = model._releaseDate == null ? "" : model._releaseDate.toString();
				CmContentModel cModel=(CmContentModel)h.get(new Integer(i));
				if(model._archives.equals("")&&	model._content.equals("")&&	model._isClose.equals("")&&	model._isTalk.equals("")&& releaseDate.equals("")&&
						model._releaseDepartment.equals("")&&
						model._releaseMan.equals("")&&
						model._subTitle.equals("")&&
						model._title.equals("")){
					continue;
				} else if(cModel._archives.indexOf(model._archives)!=-1&&
					cModel._content.indexOf(model._content)!=-1&&
					cModel._isClose.indexOf("否")!=-1&&
					cModel._isTalk.indexOf(model._isTalk)!=-1&&
					cModel._releaseDate.toString().indexOf(model._releaseDate.toString())!=-1&&
					cModel._releaseDepartment.indexOf(model._releaseDepartment)!=-1&&
					cModel._releaseMan.indexOf(model._releaseMan)!=-1&&
					cModel._subTitle.indexOf(model._subTitle)!=-1&&
					cModel._title.indexOf(model._title)!=-1){
					searchList.put(new Integer(searchList.size()),cModel);
				}
//				
			}
		}
		return searchList;
	}

}
