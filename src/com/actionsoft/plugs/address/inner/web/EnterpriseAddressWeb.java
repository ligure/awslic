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

package com.actionsoft.plugs.address.inner.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import com.actionsoft.awf.form.execute.plugins.imp2exp.ImpExpUtil;
import com.actionsoft.awf.organization.cache.CompanyCache;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.cache.UserMapCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.dao.OrganizationDaoFactory;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserMapModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.organization.util.OrgUtil;
import com.actionsoft.awf.organization.util.SecurityUtil;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.plugs.address.inner.model.EnterpriseAddressExpExcelModel;
import com.actionsoft.plugs.address.inner.util.AddressUtil;
import com.actionsoft.plugs.address.inner.util.EnterpriseAddressConfig;
import com.actionsoft.plugs.address.inner.util.EnterpriseAddressFieldModel;
import com.actionsoft.plugs.address.util.AdressUtil;

/**
 * 企业通讯录
 * 
 * @author wjx
 * @version 1.0
 * @update 2014-05-07取消邮箱点击发邮件功能修改450,1131行，Email_API_SendMail
 */
public class EnterpriseAddressWeb extends ActionsoftWeb {
	private int sumUsers=0;
	UserContext myUser = null;
	public EnterpriseAddressWeb(UserContext me) {
		super(me);
		myUser = me;
	}

	/**
	 * 单位列表，如果当前只有一个单位，直接列出通讯录
	 * 
	 * @author David.yang
	 * @modify jackliu
	 * @version 1.1
	 */
	public String getCompanyList(String companyName) {
		Map htCompanyList = CompanyCache.getList();
		String cgi = "./login.wf";
		String sid = this.getContext().getSessionId();
		String targetUrl = "";
		Hashtable hashTags = new Hashtable();
		if ("".equals(companyName.trim())) {
			targetUrl = cgi + "?sid=" + sid + "&cmd=Inner_Address_Show_SelectInfo&companyid=" + this.getContext().getCompanyModel().getCompanyName() + "";
		} else {
			targetUrl = cgi + "?sid=" + sid + "&cmd=Inner_Address_Show_SelectInfo&companyid=" + companyName;
		}
		StringBuffer pageButton = new StringBuffer();
		ArrayList list = AddressUtil.getUnderlingEnterpriseTypeList();
		pageButton.append("<td background=../aws_img/bar_left.gif width=11></td>");
		for (int i = 0; i < htCompanyList.size(); i++) {
			CompanyModel companyModel = (CompanyModel) htCompanyList.get(new Integer(i));
			if (getContext().getCompanyModel().getId() == companyModel.getId() || SecurityUtil.hasCompanySec(getContext(), companyModel.getId())) {
				String tmpName = companyModel.getCompanyName();
				if (companyName.equals(companyModel.getCompanyName())) {
					pageButton.append("<td class=actionsoftTopMenuButtonLeft background='../aws_img/bar_select_left.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
					pageButton.append("<td class=actionsoftTopMenuButton background='../aws_img/bar_select_middle.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"><a href='' ><img src=../aws_img/Menu1.gif border=0>")
							.append(I18nRes.findValue(getContext().getLanguage(), companyModel.getCompanyName())).append("</a></td>");
					pageButton.append("<td class=actionsoftTopMenuButtonRight background='../aws_img/bar_select_right.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
					pageButton.append("<td class=actionsoftTopMenuButtonSpace></td>");
				} else {
					pageButton.append("<td class=actionsoftTopMenuButtonLeft background='../aws_img/bar_noselect_left.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
					pageButton.append("<td class=actionsoftTopMenuButton background='../aws_img/bar_noselect_middle.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"><a href='' ><img src=../aws_img/Menu1.gif border=0>")
							.append(I18nRes.findValue(getContext().getLanguage(), companyModel.getCompanyName())).append("</a></td>");
					pageButton.append("<td class=actionsoftTopMenuButtonRight background='../aws_img/bar_noselect_right.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
					pageButton.append("<td class=actionsoftTopMenuButtonSpace></td>");
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			String tmpName = list.get(i).toString();
			if (companyName.equals(tmpName.trim())) {
				pageButton.append("<td class=actionsoftTopMenuButtonLeft background='../aws_img/bar_select_left.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
				pageButton.append("<td class=actionsoftTopMenuButton background='../aws_img/bar_select_middle.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"><a href='' ><img src=../aws_img/Menu1.gif border=0>").append(I18nRes.findValue(getContext().getLanguage(), tmpName)).append("</a></td>");
				pageButton.append("<td class=actionsoftTopMenuButtonRight background='../aws_img/bar_select_right.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
				pageButton.append("<td class=actionsoftTopMenuButtonSpace></td>");
			} else {
				pageButton.append("<td class=actionsoftTopMenuButtonLeft background='../aws_img/bar_noselect_left.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
				pageButton.append("<td class=actionsoftTopMenuButton background='../aws_img/bar_noselect_middle.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"><a href='' ><img src=../aws_img/Menu1.gif border=0>").append(I18nRes.findValue(getContext().getLanguage(), tmpName)).append("</a></td>");
				pageButton.append("<td class=actionsoftTopMenuButtonRight background='../aws_img/bar_noselect_right.gif' onclick=\"changePage(frmMain,'" + tmpName + "');return false;\"></td>");
				pageButton.append("<td class=actionsoftTopMenuButtonSpace></td>");
			}
		}
		if(getContext().getUID().equals("admin")){
			pageButton.append("<td  class=actionsoftTopMenuButtonLeft background='../aws_img/bar_noselect_left.gif' onclick=\"pageSetParam();return false;\"></td>");
			pageButton.append("<td class=actionsoftTopMenuButton background='../aws_img/bar_noselect_middle.gif' onclick=\"pageSetParam();return false;\"><a href='' ><img src=../aws_img/Menu1.gif border=0>").append(I18nRes.findValue(getContext().getLanguage(), "高级设置")).append("</a></td>");
			pageButton.append("<td class=actionsoftTopMenuButtonRight background='../aws_img/bar_noselect_right.gif' onclick=\"pageSetParam();return false;\"></td>");
			pageButton.append("<td class=actionsoftTopMenuButtonSpace></td>");
		}
		
		String title2 = I18nRes.findValue(getContext().getLanguage(), "单位通讯录");
		if (EnterpriseAddressConfig.getAddressModel().getTitle().trim().length()>0) {
			title2 = I18nRes.findValue(getContext().getLanguage(), EnterpriseAddressConfig.getAddressModel().getTitle());
		}

		hashTags.put("companyid", companyName);
		hashTags.put("pageButton", pageButton);
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("Title", companyName);
		hashTags.put("pageUrl", targetUrl);
		hashTags.put("title2", I18nRes.findValue(getContext().getLanguage(), title2));
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_AllList.htm"), hashTags);
	}

	/**
	 * 企业通讯录，部门通讯录列表
	 * 
	 * @author wjx
	 * @modify jackliu
	 * @version 1.1
	 */
	public String getDetailWeb(String strCompanyId, String filterType, String filterValue) {
		
		if (EnterpriseAddressConfig.getAddressModel().getDeptTree().toLowerCase().equals(EnterpriseAddressConfig.DEPT_TREE_POSITION_LEFT)) {
			EnterpriseAddressNewWeb web = new EnterpriseAddressNewWeb(getContext());
			return web.getPortalIndex(strCompanyId);
		}
		
		if (!filterValue.equals(""))
			filterValue = filterValue.trim();
		if (filterType.equals(""))
			filterType = "USERNAME";// 默认按姓名查询
		Map htCompanyList = CompanyCache.getList();

		CompanyModel companymodel = (CompanyModel) CompanyCache.getModel(Integer.parseInt(strCompanyId));
		String strTitle = "<img src = '../aws_img/private/folder263.gif'>" + I18nRes.findValue(getContext().getLanguage(), companymodel.getCompanyName()) +  I18nRes.findValue(getContext().getLanguage(),"通讯录");
		StringBuffer sbAddressList = new StringBuffer();
		StringBuffer toolbar = new StringBuffer();
		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		HashMap accessFieldList = new HashMap();// 用户可以看到的字段（有些数据只能某些角色范围内能看到）
		toolbar.append("<style media=print>.Noprint{display:none;}</style><div style='background-color:eeeeee' align=right class=Noprint>");
		// 准备工具条
		StringBuffer filter = new StringBuffer();
		filter.append("<select name=filterType >");
		for (int i = 0; i < fieldList.size(); i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
			/**
			 * huh
			 * 2014-1-18
			 * 过滤查询条件的选项，如果是部门名称，跳出本次循环。
			 */
			if(model.getTitle().equals("部门名称")){
				continue;
			}
			if(!model.getKey().equals("NUM")){
				filter.append("<option value='").append(model.getKey()).append("' ").append(model.getKey().equals(filterType) ? "selected" : "").append(" >").append(I18nRes.findValue(getContext().getLanguage(), model.getTitle())).append("</option>\n");
			}
			

			// 判断当前用户是否能看这个字段
			// 判断是否为管理员
			boolean isAccess = false;
			if (model.getAccessRole() != null) {
				if (model.getAccessRole().length() == 0) {
					isAccess = true;
				} else if (("," + model.getAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
					isAccess = true;
				}
				// 是否排除我不能看
				if (model.getNoAccessRole().length() > 0) {
					if (("," + model.getNoAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
						isAccess = false;
					}
				}
				if (isAccess) {
					accessFieldList.put(model.getKey(), model.getKey());
				}
			}
		}
		filter.append("</select>\n");
		//
		filter.append("<input onBlur=\"blurFieldAddress(this,'<I18N#请输入查询条件>');\"  onFocus=\"focusFieldAddress(this,'<I18N#请输入查询条件>');\" style=\"color: #999;\" type=text name=filterValue value='").append(filterValue.equals("") ? I18nRes.findValue(getContext().getLanguage(), "请输入查询条件") : filterValue).append("' onkeypress=\"search_onkeypress(frmMain,'Inner_Address_Show_Detail');\" >&nbsp;<a href='' onclick=\"filterAddress(frmMain,'Inner_Address_Show_Detail');return false;\"><img src=../aws_img/zoomInBtn.gif border=0 alt=查询当前单位的通讯录>&nbsp;<I18N#查询></a>&nbsp;");
		if (!filterValue.equals("")) {
			filter.append("&nbsp;&nbsp;<a href='' onclick=\"rsetTable(frmMain,'Inner_Address_Show_Detail');return false;\"><span style='color:gray'><I18N#点击此处再次显示全部记录></span></a>");
		}
	//	toolbar.append("<a href='' onClick='JavaScript:window.print();return false;'><img src=../aws_img/Print16.gif alt='打印当前页面' border=0>"+I18nRes.findValue(getContext().getLanguage(), "打印")+"</a>&nbsp;&nbsp;");
	//	toolbar.append("<a href='' onClick=\"downAddressFile(frmMain,'Inner_Address_Down_Detail'," + strCompanyId + ");return false;\"><img src=../aws_img/download2.gif alt='下载通讯录文件到本地' border=0><I18N#下载></a>&nbsp;&nbsp;&nbsp;&nbsp;");
		toolbar.append(filter);
		toolbar.append("<span style='width:10px'></span></div><table width=100% border=0 cellspacing=0 cellpadding=0 class=Noprint><tr><td height=1 bgcolor=gray></td></tr></table>");
		sbAddressList.append("<table align='center' width='95%' cellspacing=1  bgcolor = '#999999'>\n");
		sbAddressList.append("<tr>\n");
		// 显示头
		int count = fieldList.size();
		for (int i = 0; i < count; i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
			/**
			 * huh 
			 * 2014-1-18
			 * 把原有的代码注释掉（原因：它把“部门名称”这个title过滤掉了）
			 */
//			if (!accessFieldList.containsKey(model.getKey())) {
//				continue;
//			}
			sbAddressList.append("<td height=25 style='background-color:cccccc;font-size:13px;color:000000' align=center width='").append(model.getWidth()).append("' nowrap ><I18N#").append(model.getTitle()).append("></td>\n");
		}
		sbAddressList.append("</tr>\n");
		// 判断是否为管理员
		boolean isMaster = false;
		if (("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1)
			isMaster = true;
		Hashtable htDepartment = DepartmentCache.getDepartmentListOfLayer(1, Integer.parseInt(strCompanyId));
		StringBuffer deptTreeHtml = new StringBuffer();
		String bgColor = "#ffffff";
		if (htDepartment.size() > 0) {
			deptTreeHtml.append("<ul>\n");
		}
		for (int i = 0; i < htDepartment.size(); i++) {
			DepartmentModel depModel = (DepartmentModel) htDepartment.get(new Integer(i));
			deptTreeHtml.append("<li>");
			deptTreeHtml.append("<a href='#D").append(depModel.getId()).append("'>");
			deptTreeHtml.append(depModel.getDepartmentName());
			deptTreeHtml.append("</a>");
			deptTreeHtml.append("\n");
			//modify by wangwq 去掉原有树状显示  改为平板式显示
			sbAddressList.append(getSubDepartment(null, depModel, filterType, filterValue, count, 1, deptTreeHtml));
			deptTreeHtml.append("</li>\n");
		}
		if (htDepartment.size() > 0) {
			deptTreeHtml.append("</ul>\n");
		}
		sbAddressList.append("</table>\n");
		
		Hashtable hashTags = new Hashtable();
		hashTags.put("toolBar", toolbar.toString());
		hashTags.put("companyid", strCompanyId);
		hashTags.put("addresslist", sbAddressList.toString());
		if (EnterpriseAddressConfig.getAddressModel().getDeptTree().toLowerCase().equals(EnterpriseAddressConfig.DEPT_TREE_POSITION_TOP)) {
			hashTags.put("deptTreeHtml", deptTreeHtml.toString().replace("<ul>\n</ul>", ""));
			hashTags.put("deptTreeDisplay", "");
		}
		if (EnterpriseAddressConfig.getAddressModel().getDeptTree().toLowerCase().equals(EnterpriseAddressConfig.DEPT_TREE_POSITION_NONE)) {
			hashTags.put("deptTreeHtml", "");
			hashTags.put("deptTreeDisplay", "none");
		}
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("page_title", I18nRes.findValue(getContext().getLanguage(),"通讯录"));
		hashTags.put("title", "");
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_Page_Detail.htm"), hashTags);
	}

	/**
	 * 获得子部门
	 * 
	 * @param rootModel
	 * @param depModel
	 * @param filterType
	 * @param filterValue
	 * @param Feildcount
	 * @param forcount
	 *            循环次数
	 * @return
	 */
	private String getSubDepartment(DepartmentModel rootModel, DepartmentModel depModel, String filterType, String filterValue, int Feildcount, int forcount, StringBuffer deptTreeHtml) {
		StringBuffer sbAddressList = new StringBuffer();
		if (!filterValue.equals(""))
			filterValue = filterValue.trim();
		if (filterType.equals(""))
			filterType = "USERNAME";// 默认按姓名查询
/**
 *  huh 
 *  2014-1-17
 *  deptId : 当前部门全路径ID
 *  myUserDeptId : 当前登录用户部门全路径ID
 */
boolean isexecute = false;
String deptId = depModel.getDepartmentFullIdOfCache();
String[] deptIds = deptId.split("\\/");

String myUserDeptId = myUser.getDepartmentModel().getDepartmentFullIdOfCache();
String[] myUserDeptIds = myUserDeptId.split("\\/"); 


boolean qq = (myUserDeptIds[myUserDeptIds.length-1] == deptIds[deptIds.length-1]);
if(!myUserDeptIds[0].equals(deptIds[0])){
	return "";
}
		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		HashMap accessFieldList = new HashMap();// 用户可以看到的字段（有些数据只能某些角色范围内能看到）
		for (int i = 0; i < fieldList.size(); i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));

			// 判断当前用户是否能看这个字段
			// 判断是否为管理员
			boolean isAccess = false;
			if (model.getAccessRole() != null) {
				if (model.getAccessRole().length() == 0) {
					isAccess = true;
				} else if (("," + model.getAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
					isAccess = true;
				}
				// 是否排除我不能看
				if (model.getNoAccessRole().length() > 0) {
					if (("," + model.getNoAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
						isAccess = false;
					}
				}
				if (isAccess) {
					accessFieldList.put(model.getKey(), model.getKey());
				}
			}
		}
		// 判断是否为管理员
		boolean isMaster = false;
		if (("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {
			isMaster = true;
		}
//		sbAddressList.append("<tr><td  align=\"left\" valign=\"top\" bgcolor = '#F2F2F2' colspan=").append(fieldList.size()).append("><img style='display:none' src= '../aws_img/expand_but2.gif' name = deptName").append(depModel._id).append("><b>").append(depModel._departmentFullNameOfCache).append("</b></td></tr>");
		int iRowNum = 0;
		String bgColor = "#ffffff";
		// 得到这个部门的所有人员定义
		Hashtable userHash = UserCache.getUserListOfDepartment(depModel.getId());
		//获取该部门的所有兼职信息
		Hashtable usermap = UserMapCache.getMapListOfDepartment(depModel.getId());
		//利用原部门用户列表和兼职信息列表，获得全部的人员信息列表
		Hashtable user = UserCache.getUserListOfDepartment(depModel.getId());
		userHash = getNewUserList(user, usermap);
		
		int usercount = AdressUtil.getWorkEmployee(userHash);
		if (usercount < 1) {
			Hashtable subDepartmentList = DepartmentCache.getSubDepartmentList(depModel.getId());
			if (subDepartmentList != null || subDepartmentList.size() != 0) {
				deptTreeHtml.append("<ul>\n");
				for (int iii = 0; iii < subDepartmentList.size(); iii++) {
					DepartmentModel subdeptModel = (DepartmentModel) subDepartmentList.get(new Integer(iii));
					deptTreeHtml.append("<li>");
					deptTreeHtml.append("<a href='#D").append(subdeptModel.getId()).append("'>");
					deptTreeHtml.append(subdeptModel.getDepartmentName());
					deptTreeHtml.append("</a>\n");

					sbAddressList.append(getSubDepartment(depModel, subdeptModel, filterType, filterValue, Feildcount, forcount + 1, deptTreeHtml));
					deptTreeHtml.append("</li>\n");
				}
				deptTreeHtml.append("</ul>\n");
			}
		} else {
			// 根据级次缩进
			String nbsp = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
			String backgroud = "line1.gif";
			if (depModel.getLayer() > 1) {
				// for (int tab = 1; tab < depModel._layer; tab++) {
				// nbsp += "&nbsp;&nbsp;&nbsp;&nbsp;";
				// }
				backgroud = "line" + depModel.getLayer() + ".gif";
			}
			// nbsp+="-";

			if (userHash != null) {
				/**
				 *  huh 
				 *  2014-1-17
				 *  
				 */
				if(depModel.getDepartmentFullIdOfCache().indexOf(myUserDeptId) != -1){     // 判断当前全路径是否包含登录用户部门全路径
					int num = 0;
					StringBuffer tmpAddressList = new StringBuffer();
					for (int ii = 0; ii < userHash.size(); ii++) {
						UserModel userModel = (UserModel) userHash.get(new Integer(ii));

						if (userModel.isDisabled() || userModel.getUID().equals("admin")) {
							continue;
						}
						if (userModel.isDisabled() == false) {

							// 帐户过滤
							if (!filterValue.equals("")) {
								if (filterType.equals("USERNAME")) {
									if (userModel.getUserName() != filterValue)
										continue;
								}
								if (filterType.equals("USERNO")) {
									if (userModel.getUserNo() != filterValue)
										continue;
								}
								if (filterType.equals("UID")) {
									if (userModel.getUID() != filterValue)
										continue;
								}
								if (filterType.equals("OFFICETEL")) {
									if (userModel.getOfficeTel() != filterValue)
										continue;
								}
								if (filterType.equals("OFFICEFAX")) {
									if (userModel.getOfficeFax()!= filterValue)
										continue;
								}
								if (filterType.equals("MOBILE")) {
									if (userModel.getMobile()!= filterValue)
										continue;
								}
								if (filterType.equals("EMAIL")) {
									if (userModel.getEmail()!= filterValue)
										continue;
								}
								if (filterType.equals("POSITIONNAME")) {
									if (userModel.getPositionName()!= filterValue)
										continue;
								}
								if (filterType.equals("JJTEL")) {
									if (userModel.getJjTel()!= filterValue)
										continue;
								}
							}
							sumUsers++;
							String strOfficeTel = (userModel.getOfficeTel() == null || userModel.getOfficeTel().trim().equals("")|| userModel.getOfficeTel().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getOfficeTel();
							String strOfficeFax = (userModel.getOfficeFax() == null || userModel.getOfficeFax().trim().equals("")|| userModel.getOfficeFax().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getOfficeFax();
							String strCode = (userModel.getUserNo() == null || userModel.getUserNo().trim().equals("")|| userModel.getUserNo().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getUserNo();
							String strMobile = (userModel.getMobile() == null || userModel.getMobile().trim().equals("")|| userModel.getMobile().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getMobile();
//							String strEMail = (userModel.getEmail() == null || userModel.getEmail().trim().equals("")|| userModel.getEmail().trim().indexOf("null")!=-1) ? "&nbsp;" : "<a href='###' onclick=\"openSendEmailWeb('"+userModel.getEmail()+"','"+userModel.getUserName()+"','./login.wf?sid=" + this.getContext().getSessionId() + "&cmd=Email_API_SendMail&mailTo=" + userModel.getEmail() + "')\">" + userModel.getEmail() + "</a>";
							String strEMail = (userModel.getEmail() == null || userModel.getEmail().trim().equals("")|| userModel.getEmail().trim().indexOf("null")!=-1) ? "&nbsp;" : "<a href='###'>" + userModel.getEmail() + "</a>";
							String strUserName = userModel.getUserNameAlias();
							String strPositionName = (userModel.getPositionName() == null || userModel.getPositionName().trim().equals("")|| userModel.getPositionName().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getPositionName();
							iRowNum++;
							String admin = "";
							if (isMaster || userModel.getUID().equals(this.getContext().getUID())) {
							//	admin = "<a href='' onclick=\"modifyAddress(frmMain,'Inner_Address_Modify_Detail','" + userModel.getUID() + "');return false;\"><img src=../aws_img/find_obj.gif border=0 title='"+I18nRes.findValue(getContext().getLanguage(),"修改这个信息")+"' align='absmiddle'></a>";
								admin="";
							} 
//							else {
//								admin = "<img src='../aws_img/mssfgrant.gif' align='absmiddle'>";
//							}
							
							boolean isAdd = true;//是否添加了锚点
							String anchor = "<a id='D"+depModel.getId()+"' name='D"+depModel.getId()+"'></a>\n";
							String onlineAlt = OrgUtil.getOnlineAlt(userModel.getUID());
							//根据fieldList的字段信息判断是否显示该字段
							for (int iii = 0; iii < fieldList.size(); iii++) {
								EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
								if (!accessFieldList.containsKey(model.getKey())) {
									continue;
								}
								
								if (model.getKey().equals("USERNO")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px;\" bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + strCode + "</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("USERNAME")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px; \" bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") 
											+ onlineAlt + "&nbsp;<span id='"+AddressUtil.getTooltipSpanId(userModel, isUserMap(userModel, depModel), sumUsers)+"'>" + strUserName//userModel.getId()  //I18nRes.findValue(getContext().getLanguage(), 
											+ "</span>\n"
											/**
											 * 20140829
											 * wangaz
											 * 通讯录初次进入，时去掉鼠标移动到人员姓名显示弹出框的事件
											+(AddressUtil.getTooltipScript(userModel, getContext().getSessionId(),isUserMap(userModel, depModel),sumUsers))+ admin + 
											*/
											+"</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("UID")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px; \" bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + userModel.getUID() + "</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("OFFICETEL")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px; \" bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + strOfficeTel + "</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("OFFICEFAX")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px; \" bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + strOfficeFax + "</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("MOBILE")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px; \" bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + strMobile + "</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("EMAIL")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px;\"  bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + strEMail + "</td>\n");
									isAdd = false;
								} else if (model.getKey().equals("POSITIONNAME")) {
									String field=strPositionName;
									if(isUserMap(userModel, depModel)) {
										field = field + "<font color='blue' title='"+I18nRes.findValue(getContext().getLanguage(),"兼任职位")+"'>("+I18nRes.findValue(getContext().getLanguage(),"兼")+")</font>";
									}
									tmpAddressList.append("<td align='left' style=\"padding:4px;\"  bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + field + "</td>\n");
									isAdd = false;
								}else if (model.getKey().equals("NUM")) {
									tmpAddressList.append("<td align='center' style=\"padding:4px;\"  bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + sumUsers + "</td>\n");
									isAdd = false;
								}else if (model.getKey().equals("JJTEL")) {
									tmpAddressList.append("<td align='left' style=\"padding:4px;\"  bgcolor=").append(bgColor).append(" nowrap>" + (num==0&&isAdd?anchor:"") + userModel.getJjTel() + "</td>\n");
									isAdd = false;
								}
							}
							tmpAddressList.append("</tr>\n");
						}
						num++;
					}
					
					sbAddressList.append("<tr>\n");
					for (int iii = 0; iii < fieldList.size(); iii++) {
							EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
							if (!accessFieldList.containsKey(model.getKey())) {
								continue;
							}
							if (model.getKey().equals("DEPARTMENTNAME")) {
								sbAddressList.append("<td align=\"left\" nowrap valign=\"middle\" bgcolor = '#ffffff' rowspan=").append(num).append(">").append(AdressUtil.getSubDeptNameTitle(depModel.getDepartmentFullNameOfCache(), depModel.getDepartmentName())).append("</td>\n");
							}
					}
					if (tmpAddressList.toString().equals("")) {
						sbAddressList.append("<td colspan=5 style=\"padding:4px;\" bgcolor=" + bgColor + " nowrap><img src=../aws_img/warning.gif>该部门暂无职员</td>\n");
					} else {
						sbAddressList.append(tmpAddressList.toString());
					}

				}
				
			}
			Hashtable subDepartmentList = DepartmentCache.getSubDepartmentList(depModel.getId());
			if (subDepartmentList != null || subDepartmentList.size() != 0) {
				deptTreeHtml.append("<ul>\n");
				for (int iii = 0; iii < subDepartmentList.size(); iii++) {
					DepartmentModel subdeptModel = (DepartmentModel) subDepartmentList.get(new Integer(iii));
					deptTreeHtml.append(" <li>");
					deptTreeHtml.append("<a href='#D").append(subdeptModel.getId()).append("'>");
					deptTreeHtml.append(subdeptModel.getDepartmentName());
					deptTreeHtml.append("</a>");
					deptTreeHtml.append("\n");

					sbAddressList.append(getSubDepartment(depModel, subdeptModel, filterType, filterValue, Feildcount, forcount + 1, deptTreeHtml));
					deptTreeHtml.append(" </li>\n");
				}
				deptTreeHtml.append("</ul>\n");
			}
			// if (iRowNum == 0) {
			// sbAddressList.append("<tr><td
			// colspan='").append((fieldList.size() + 1)).append("'
			// align='center'><img
			// src=../aws_img/warning.gif>没有查询记录</td></tr>\n");
			// }
		}

		// sbAddressList.append("</table>\n");
		return sbAddressList.toString();

	}
	private boolean isUserMap(UserModel userModel, DepartmentModel depModel) {
		int curDeptId = userModel.getDepartmentId();
		int deptId = depModel.getId();
		
		if(curDeptId!=deptId) {
			return true;
		} else {
			return false;
		}
	}
	 

	/**
	 * 修改一个人的联系信息（页面）
	 * 
	 * @param uid
	 * @return
	 * @author jack
	 */
	public String getModifyPage(String uid) {
		UserModel userModel = (UserModel) UserCache.getModel(uid);

		StringBuffer html = new StringBuffer();
		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		for (int iii = 0; iii < fieldList.size(); iii++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
			if (model.getKey().equals("NO")) {
				html.append("<tr>");
				html.append("<td width='10%' nowrap align=right>").append(I18nRes.findValue(getContext().getLanguage(),"aws.common.number")+"：").append("</td>");
				html.append("<td width='90%'>").append("<input type=text name=userNo maxLength=32 class=actionsoftInput size=50 value='" + userModel.getUserNo() + "'>").append("</td>");
				html.append("</tr>\n");
//				html.append("编号：<input type=text name=userNo maxLength=32 class=actionsoftInput size=50 value='" + userModel._userNo + "'><br>\n");
			} else if (model.getKey().equals("USERNAME")) {
				html.append("<tr>");
				html.append("<td width='1%' nowrap align=right>").append(I18nRes.findValue(getContext().getLanguage(),"aws.common.org.user_name")+"：").append("</td>");
				html.append("<td width='99%' align=left>").append("<input type=text name=userName maxLength=32 class=actionsoftInput size=50 value='" + userModel.getUserName() + "'>").append("</td>");
				html.append("</tr>\n");
//				html.append("姓名：<input type=text name=userName maxLength=32 class=actionsoftInput size=50 value='" + userModel._userName + "'><br>\n");
			} else if (model.getKey().equals("OFFICETEL")) {
				html.append("<tr>");
				html.append("<td>").append(I18nRes.findValue(getContext().getLanguage(),"电话")+"：").append("</td>");
				html.append("<td width='90%'>").append("<input type=text name=tel maxLength=32 class=actionsoftInput size=50 value='" + userModel.getOfficeTel() + "'>").append("</td>");
				html.append("</tr>\n");
//				html.append("电话：<input type=text name=tel maxLength=32 class=actionsoftInput size=50 value='" + userModel._officeTel + "'><br>\n");
			} else if (model.getKey().equals("OFFICEFAX")) {
				html.append("<tr>");
				html.append("<td>").append(I18nRes.findValue(getContext().getLanguage(),"传真")+"：").append("</td>");
				html.append("<td>").append("<input type=text name=fax maxLength=32 class=actionsoftInput size=50 value='" + userModel.getOfficeFax() + "'>").append("</td>");
				html.append("</tr>\n");
//				html.append("传真：<input type=text name=fax maxLength=32 class=actionsoftInput size=50 value='" + userModel._officeFax + "'><br>\n");
			} else if (model.getKey().equals("MOBILE")) {
				html.append("<tr>");
				html.append("<td>").append(I18nRes.findValue(getContext().getLanguage(),"手机")+"：").append("</td>");
				html.append("<td>").append("<input type=text name=mobile maxLength=32 class=actionsoftInput size=50 value='" + userModel.getMobile() + "'>").append("</td>");
				html.append("</tr>\n");
//				html.append("手机：<input type=text name=mobile maxLength=32 class=actionsoftInput size=50 value='" + userModel._mobile + "'><br>\n");
			} else if (model.getKey().equals("EMAIL")) {
				html.append("<tr>");
				html.append("<td>").append(I18nRes.findValue(getContext().getLanguage(),"aws.common.mail")+"：").append("</td>");
				html.append("<td>").append("<input type=text name=email maxLength=32 class=actionsoftInput size=50 value='" + userModel.getEmail() + "'>").append("</td>");
				html.append("</tr>\n");
//				html.append("邮件：<input type=text name=email maxLength=32 class=actionsoftInput size=50 value='" + userModel._email + "'><br>\n");
			}
		}
//		html.append("<hr>");
		html.append("<div align=right><input type=button value='"+I18nRes.findValue(getContext().getLanguage(),"更新信息")+"'  class ='actionsoftButton' onClick=\"modifyAddressAction(frmMain,'Inner_Address_Modify_Detail_Action');return false;\"  border='0' >&nbsp;&nbsp;<input type=button value='"+I18nRes.findValue(getContext().getLanguage(),"aws.common.eform_关闭窗口")+"'  class ='actionsoftButton' onClick=\"window.close();\" border='0' ></div>");

		Hashtable hashTags = new Hashtable();
		hashTags.put("addresslist", html.toString());
		hashTags.put("uid", uid);
		hashTags.put("userName", userModel.getUserName());
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("page_title", I18nRes.findValue(getContext().getLanguage(),"aws.common_修改")+"[" + userModel.getUserName() + "]"+I18nRes.findValue(getContext().getLanguage(),"的通讯信息"));
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_Page_Detail_Edit.htm"), hashTags);
	}

	/**
	 * 更新用户联系信息
	 * 
	 * @param uid
	 * @param userName
	 * @param userNo
	 * @param tel
	 * @param fax
	 * @param mobile
	 * @param email
	 * @return
	 * @author jack
	 */
	public String updateInfo(String uid, String userName, String userNo, String tel, String fax, String mobile, String email) {
		UserModel user = (UserModel) UserCache.getModel(uid);

		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		for (int iii = 0; iii < fieldList.size(); iii++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
			if (model.getKey().equals("NO")) {
				user.setUserNo(userNo);
			} else if (model.getKey().equals("USERNAME")) {
				user.setUserName(userName);
			} else if (model.getKey().equals("OFFICETEL")) {
				user.setOfficeTel(tel);
			} else if (model.getKey().equals("OFFICEFAX")) {
				user.setOfficeFax(fax);
			} else if (model.getKey().equals("MOBILE")) {
				user.setMobile(mobile);
			} else if (model.getKey().equals("EMAIL")) {
				user.setEmail(email);
			}
		}
		OrganizationDaoFactory.createUser().store(user);

		return getModifyPage(uid);
	}

	/**
	 * 下载
	 * 
	 * @param strCompanyId
	 * @return
	 * @author jack
	 */
	public String downloadExcel(String strCompanyId) {
		CompanyModel companymodel = (CompanyModel) CompanyCache.getModel(Integer.parseInt(strCompanyId));
		String strTitle = companymodel.getCompanyName() + I18nRes.findValue(getContext().getLanguage(),"通讯录");
		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Address");
		HSSFRow row = null;
		HSSFCell cell = null;
		HSSFFont font = wb.createFont();

		// 模板标题
		short rowIndex = 0;
		row = sheet.createRow(rowIndex);
		cell = row.createCell((short) 0);
		
		HSSFCellStyle aws_head_cell = wb.createCellStyle();
		font.setFontHeight((short) 400);
		aws_head_cell.setFont(font);
		cell.setCellStyle(aws_head_cell);
		cell.setCellValue(strTitle);

		rowIndex++;
		row = sheet.createRow(rowIndex);
		cell = row.createCell((short) 0);
		
		cell.setCellValue(I18nRes.findValue(getContext().getLanguage(),"下载日期")+ (new Date().toGMTString()));

		font = wb.createFont();
		HSSFCellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
		styleHead.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleHead.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleHead.setBottomBorderColor(HSSFColor.BLACK.index);
		styleHead.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleHead.setLeftBorderColor(HSSFColor.BLACK.index);
		styleHead.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleHead.setRightBorderColor(HSSFColor.BLACK.index);
		styleHead.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleHead.setTopBorderColor(HSSFColor.BLACK.index);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		styleHead.setFont(font);

		HSSFCellStyle styleData = wb.createCellStyle();
		styleData.setFillForegroundColor(HSSFColor.WHITE.index);
		styleData.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleData.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleData.setBottomBorderColor(HSSFColor.BLACK.index);
		styleData.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleData.setLeftBorderColor(HSSFColor.BLACK.index);
		styleData.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleData.setRightBorderColor(HSSFColor.BLACK.index);
		styleData.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleData.setTopBorderColor(HSSFColor.BLACK.index);
		// styleData.setFont(font);

		rowIndex++;
		row = sheet.createRow(rowIndex);
		cell = row.createCell((short) 0);
		cell.setCellStyle(styleHead);
		short column = 0;

		HashMap accessFieldList = new HashMap();
		// 头
		for (int iii = 0; iii < fieldList.size(); iii++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
			cell = row.createCell(column);
			sheet.setColumnWidth(column, (short) (150 * 256 / 5));
			
			cell.setCellValue(I18nRes.findValue(getContext().getLanguage(),model.getTitle()));
			cell.setCellStyle(styleHead);
			column++;

			// 判断当前用户是否能看这个字段
			// 判断是否为管理员
			boolean isAccess = false;
			if (model.getAccessRole().length() == 0) {
				isAccess = true;
			} else if (("," + model.getAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
				isAccess = true;
			}
			// 是否排除我不能看
			if (model.getNoAccessRole().length() > 0) {
				if (("," + model.getNoAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
					isAccess = false;
				}
			}
			if (isAccess) {
				accessFieldList.put(model.getKey(), model.getKey());
			}
		}
		Hashtable htDepartment = DepartmentCache.getDepartmentListOfLayer(1, Integer.parseInt(strCompanyId));
		List expExcelHash = new ArrayList();
		for (int i = 0; i < htDepartment.size(); i++) {
			DepartmentModel depModel = (DepartmentModel) htDepartment.get(new Integer(i));
			
			Hashtable htUser = UserCache.getUserListOfDepartment(depModel.getId());
			//获取该部门的所有兼职信息
			Hashtable usermap = UserMapCache.getMapListOfDepartment(depModel.getId());
			htUser=getNewUserList(htUser, usermap);
			
			for (int ii = 0; ii < htUser.size(); ii++) {
				UserModel userModel = (UserModel) htUser.get(new Integer(ii));
				if (userModel.isDisabled())
					continue;
				if(userModel.getUID().equals("admin")){
					continue;
				}
				EnterpriseAddressExpExcelModel expExcelModel = new EnterpriseAddressExpExcelModel();
				String str="";
				if(isUserMap(userModel, depModel)) {
					str="("+I18nRes.findValue(getContext().getLanguage(),"兼")+")";
				}
				expExcelModel._strPositionName = ((userModel.getPositionName() == null || userModel.getPositionName().trim().equals("")) ? "" : userModel.getPositionName())+str;
				expExcelModel._strOfficeTel = (userModel.getOfficeTel() == null || userModel.getOfficeTel().trim().equals("")|| userModel.getOfficeTel().trim().indexOf("null")!=-1) ? "" : userModel.getOfficeTel();
				expExcelModel._strOfficeFax = (userModel.getOfficeFax() == null || userModel.getOfficeFax().trim().equals("")|| userModel.getOfficeFax().trim().indexOf("null")!=-1) ? "" : userModel.getOfficeFax();
				expExcelModel._strCode = (userModel.getUserNo() == null || userModel.getUserNo().trim().equals("")|| userModel.getUserNo().trim().indexOf("null")!=-1) ? "" : userModel.getUserNo();
				expExcelModel._strMobile = (userModel.getMobile() == null || userModel.getMobile().trim().equals("")|| userModel.getMobile().trim().indexOf("null")!=-1) ? "" : userModel.getMobile();
				expExcelModel._strEMail = (userModel.getEmail() == null || userModel.getEmail().trim().equals("")|| userModel.getEmail().trim().indexOf("null")!=-1) ? "" :  userModel.getEmail();
				expExcelModel._strDept = AdressUtil.getSubDeptNameTitle(depModel.getDepartmentFullNameOfCache(), depModel.getDepartmentName());
				expExcelModel._userName = userModel.getUserName();
				expExcelModel._uid = userModel.getUID();
				expExcelHash.add(expExcelModel);
			}
			getDepartment(expExcelHash ,depModel);
		}
//		System.out.println(expExcelHash.size());
		if(expExcelHash != null && expExcelHash.size() > 0){
			for(int i = 0 ; i < expExcelHash.size() ; i++){
				EnterpriseAddressExpExcelModel expExcelModel = (EnterpriseAddressExpExcelModel)expExcelHash.get(i);
				if(expExcelModel != null){
					rowIndex++;
					row = sheet.createRow(rowIndex);
					cell = row.createCell((short) 0);
					cell.setCellStyle(styleHead);
					column = 0;
					for (int iii = 0; iii < fieldList.size(); iii++) {
						EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
						cell = row.createCell(column);
						cell.setCellStyle(styleData);
						// 首先判断他能否访问这个字段
						if (accessFieldList.get(model.getKey()) == null) {
							cell.setCellValue("-");
						} else {
							if (model.getKey().equals("DEPARTMENTNAME")) {
								cell.setCellValue(expExcelModel._strDept);
							} else if (model.getKey().equals("USERNO")) {
								cell.setCellValue(expExcelModel._strCode);
							} else if (model.getKey().equals("USERNAME")) {
								cell.setCellValue(expExcelModel._userName);
							} else if (model.getKey().equals("UID")) {
								cell.setCellValue(expExcelModel._uid);
							} else if (model.getKey().equals("POSITIONNAME")) {
								cell.setCellValue(expExcelModel._strPositionName);
							} else if (model.getKey().equals("OFFICETEL")) {
								cell.setCellValue(expExcelModel._strOfficeTel);
							} else if (model.getKey().equals("OFFICEFAX")) {
								cell.setCellValue(expExcelModel._strOfficeFax);
							} else if (model.getKey().equals("MOBILE")) {
								cell.setCellValue(expExcelModel._strMobile);
							} else if (model.getKey().equals("EMAIL")) {
								cell.setCellValue(expExcelModel._strEMail);
							}else if(model.getKey().equals("NUM")) {
								int num=rowIndex-2;
								cell.setCellValue(num);
							}
						}
						column++;
					}
				}
			}
		}
		String fileName = ImpExpUtil.createExcelTmpFile(wb);
		String fileURL = "downfile.wf?flag1=Excel&flag2=_&sid=" + getContext().getSessionId() + "&rootDir=tmp&filename=" + fileName;

		return "<html><title></title><script> window.location='" + fileURL + "';</script></html>";
	}
	 
	/**
	 * 根据用户列表和兼职信息列表，获得新的用户列表
	 * @param user
	 * @param usermap
	 * @return
	 */
	private Hashtable getNewUserList(Hashtable user, Hashtable usermap) {
		Hashtable newList = new Hashtable();
		if(usermap!=null && usermap.size()==0) {
			for(int i = 0;i<user.size();i++){
				UserModel userModel = (UserModel) user.get(new Integer(i));
				if(userModel.isDisabled()||userModel.getUID().equals("admin")){
					continue;
				}
				newList.put(new Integer(newList.size()), userModel);;
			}
			return newList;
		}
		UserModel userModel = new UserModel();
		boolean isManager = false;
		boolean isShow=true;
		//将是管理者的人员加到列表前面
		int j=0;
		for(int i=0;i<usermap.size();i++) {
			UserMapModel userMapModel = (UserMapModel)usermap.get(new Integer(i));
			userModel = (UserModel)UserCache.getModel(userMapModel.getMapId());
			
			//根据userModel信息在user列表中遍历比较是否有该用户兼职，防止用户兼任本部门的角色时显示两次
			boolean isHas = false;
			for(int k=0;k<user.size();k++) {
				UserModel userModel2 = (UserModel)user.get(Integer.valueOf(k));
				if (userModel.getUID() == userModel2.getUID()) {
					isHas = true;
					break;
				}
			}
			if (isHas) {
				continue;
			}
			
			isManager = userMapModel.isManager();
			isShow = userMapModel.isShow();
			if(isManager&&isShow) {
				newList.put(new Integer(j), userModel);
				j++;
			}
		}
		for(int i=0;i<user.size();i++) {
			newList.put(new Integer(j), user.get(new Integer(i)));
			j++;
		}
		//将非管理者加到人员列表后面
		for(int i=0;i<usermap.size();i++) {
			UserMapModel userMapModel = (UserMapModel)usermap.get(new Integer(i));
			userModel = (UserModel)UserCache.getModel(userMapModel.getMapId());
			
			//根据userModel信息在user列表中遍历比较是否有该用户兼职，防止用户兼任本部门的角色时显示两次
			boolean isHas = false;
			for(int k=0;k<user.size();k++) {
				UserModel userModel2 = (UserModel)user.get(Integer.valueOf(k));
				if (userModel.getUID() == userModel2.getUID()) {
					isHas = true;
					break;
				}
			}
			if (isHas) {
				continue;
			}
			
			isManager = userMapModel.isManager();
			isShow = userMapModel.isShow();
			if(!isManager&&isShow) {
				newList.put(new Integer(j), userModel);
				j++;
			}
		}
		return newList;
	}
	
	private void getDepartment(List expExcelHash,DepartmentModel departModel){
		Hashtable subDepartmentList = DepartmentCache.getSubDepartmentList(departModel.getId());
		if(subDepartmentList != null && subDepartmentList.size() > 0){
			for (int i = 0; i < subDepartmentList.size(); i++) {
				DepartmentModel depModel = (DepartmentModel) subDepartmentList.get(new Integer(i));
				Hashtable htUser = UserCache.getUserListOfDepartment(depModel.getId());
				//获取该部门的所有兼职信息
				Hashtable usermap = UserMapCache.getMapListOfDepartment(depModel.getId());
				htUser=getNewUserList(htUser, usermap);
				 
				for (int ii = 0; ii < htUser.size(); ii++) {
					UserModel userModel = (UserModel) htUser.get(new Integer(ii));
					if (userModel == null || userModel.isDisabled())
						continue;
					if(userModel.getUID().equals("admin")){
						continue;
					}
					EnterpriseAddressExpExcelModel expExcelModel = new EnterpriseAddressExpExcelModel();
					String str="";
					if(isUserMap(userModel, depModel)) {
						str="("+I18nRes.findValue(getContext().getLanguage(),"兼")+")";
					}
					expExcelModel._strPositionName = ((userModel.getPositionName() == null || userModel.getPositionName().trim().equals("")) ? "" : userModel.getPositionName())+str;
					expExcelModel._strOfficeTel = (userModel.getOfficeTel() == null || userModel.getOfficeTel().trim().equals("")|| userModel.getOfficeTel().trim().indexOf("null")!=-1) ? "" : userModel.getOfficeTel();
					expExcelModel._strOfficeFax = (userModel.getOfficeFax() == null || userModel.getOfficeFax().trim().equals("")|| userModel.getOfficeFax().trim().indexOf("null")!=-1) ? "" : userModel.getOfficeFax();
					expExcelModel._strCode = (userModel.getUserNo() == null || userModel.getUserNo().trim().equals("")|| userModel.getUserNo().trim().indexOf("null")!=-1) ? "" : userModel.getUserNo();
					expExcelModel._strMobile = (userModel.getMobile() == null || userModel.getMobile().trim().equals("")|| userModel.getMobile().trim().indexOf("null")!=-1) ? "" : userModel.getMobile();
					expExcelModel._strEMail = (userModel.getEmail() == null || userModel.getEmail().trim().equals("")|| userModel.getEmail().trim().indexOf("null")!=-1) ? "" :  userModel.getEmail();
					expExcelModel._strDept = AdressUtil.getSubDeptNameTitle(depModel.getDepartmentFullNameOfCache(), depModel.getDepartmentName());
					expExcelModel._userName = userModel.getUserName();
					expExcelModel._uid = userModel.getUID();
					expExcelHash.add(expExcelModel);
				}
				getDepartment(expExcelHash ,depModel);
			}
		}
	}

	/**
	 * 获得查询列表
	 * 
	 * @return
	 */
	public String getSearchList(String strCompanyId, String filterType, String filterValue) {

		if (!filterValue.equals(""))
			filterValue = filterValue.trim();
		if (filterType.equals(""))
			filterType = "USERNAME";// 默认按姓名查询
		Map htCompanyList = CompanyCache.getList();

		CompanyModel companymodel = (CompanyModel) CompanyCache.getModel(Integer.parseInt(strCompanyId));
		String strTitle = companymodel.getCompanyName() + "通讯录";
		StringBuffer sbAddressList = new StringBuffer();
		StringBuffer toolbar = new StringBuffer();
		Map fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		HashMap accessFieldList = new HashMap();// 用户可以看到的字段（有些数据只能某些角色范围内能看到）
		toolbar.append("<style media=print>.Noprint{display:none;}</style><div style='background-color:eeeeee' align=right class=Noprint>");
		// 准备工具条
		StringBuffer filter = new StringBuffer();
		filter.append("<select name=filterType >");
		for (int i = 0; i < fieldList.size(); i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
			/**
			 * huh
			 * 2014-1-18
			 * 过滤查询条件的选项，如果是部门名称，跳出本次循环。
			 */
			if(!model.getKey().equals("NUM") && !model.getTitle().equals("部门名称")){
				filter.append("<option value='").append(model.getKey()).append("' ").append(model.getKey().equals(filterType) ? "selected" : "").append(" >").append(I18nRes.findValue(getContext().getLanguage(), model.getTitle())).append("</option>\n");
			}

			// 判断当前用户是否能看这个字段
			// 判断是否为管理员
			boolean isAccess = false;
			if (model.getAccessRole().length() == 0) {
				isAccess = true;
			} else if (("," + model.getAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
				isAccess = true;
			}
			// 是否排除我不能看
			if (model.getNoAccessRole().length() > 0) {
				if (("," + model.getNoAccessRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1) {// 我在可访问的列表内
					isAccess = false;
				}
			}
			if (isAccess) {
				accessFieldList.put(model.getKey(), model.getKey());
			}
		}
		filter.append("</select>\n");
		filter.append("<input onBlur=\"blurFieldAddress(this,'<I18N#请输入查询条件>');\"  onFocus=\"focusFieldAddress(this,'<I18N#请输入查询条件>');\" style=\"color: #999;\" type=text name=filterValue value='").append(filterValue.equals("") ? "请输入查询条件" : filterValue).append("' onkeypress=\"search_onkeypress(frmMain,'Inner_Address_Show_Detail');\" >&nbsp;<a href='' onclick=\"filterAddress(frmMain,'Inner_Address_Show_Detail');return false;\"><img src=../aws_img/zoomInBtn.gif border=0 alt=查询当前单位的通讯录>&nbsp;<I18N#查询></a>&nbsp;");
		if (!filterValue.equals("")) {
			filter.append("&nbsp;&nbsp;<a href='' onclick=\"rsetTable(frmMain,'Inner_Address_Show_SelectInfo');return false;\"><span style='color:gray'><I18N#aws.portal_点击此处再次显示全部记录></span></a>");
		}
		// modify by wangwq 去掉其他单位查询功能
//		if (htCompanyList.size() > 1) {// 有其他单位
//			toolbar.append("<a href='' onClick=\"execMyCommand(frmMain,'Inner_Address_Show_Company',0);return false;\"><img src=../aws_img/process_file.gif alt='查看其他单位的通讯录' border=0>其他单位</a>&nbsp;&nbsp;");
//		} 
		//toolbar.append("<a href='' onClick='JavaScript:window.print();return false;'><img src=../aws_img/Print16.gif alt='打印当前页面' border=0><I18N#打印></a>&nbsp;&nbsp;");
		// modify by wangwq 去掉查询结构 excel 输出功能
		//		toolbar.append("<a href='' onClick=\"downAddressFile(frmMain,'Inner_Address_Down_Detail'," + strCompanyId + ");return false;\"><img src=../aws_img/download2.gif alt='下载通讯录文件到本地' border=0>下载</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		toolbar.append(filter);
		toolbar.append("<span style='width:10px'></span></div><table width=100% border=0 cellspacing=0 cellpadding=0 class=Noprint><tr><td height=1 bgcolor=gray></td></tr></table>");

		sbAddressList.append("<table align='center' width='95%' cellspacing=1  bgcolor = '#999999'>\n");
		sbAddressList.append("<tr>\n");
		// 显示头
		sbAddressList.append("<td align='center' style='background-color:cccccc;font-size:13px;color:000000' width='2%' nowrap >&nbsp;</td>\n");
		for (int i = 0; i < fieldList.size(); i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
			if(model.getKey().equals("NUM")){
				continue;
			}
			sbAddressList.append("<td height=25 style='background-color:cccccc;font-size:13px;color:000000' align=center width='").append(model.getWidth()).append("' nowrap >").append(I18nRes.findValue(getContext().getLanguage(), model.getTitle())).append("</td>\n");
		}
		// 判断是否为管理员
		boolean isMaster = false;
		if (("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1)
			isMaster = true;
		//modify by wangwq 修正 查询其他公司人员列表
		Hashtable htDepartment = DepartmentCache.getListOfCompany(Integer.parseInt(strCompanyId));
		// Hashtable htDepartment = DepartmentCache.getDepartmentListOfLayer(1,
		// Integer.parseInt(strCompanyId));
		int iRowNum = 0;
		String bgColor = "#ffffff";
		for (int i = 0; i < htDepartment.size(); i++) {
			DepartmentModel depModel = (DepartmentModel) htDepartment.get(new Integer(i));
			if (!SecurityUtil.hasCompanySec(getContext(), depModel.getCompanyId())) {
				continue;
			}
			// 部门过滤
			if (!filterValue.equals("")) {
				if (filterType.equals("DEPARTMENTNAME")) {
					if (depModel.getDepartmentFullNameOfCache().indexOf(filterValue) == -1)
						continue;
				}
			}
			Hashtable htUser = UserCache.getUserListOfDepartment(depModel.getId());
			for (int ii = 0; ii < htUser.size(); ii++) {
				UserModel userModel = (UserModel) htUser.get(new Integer(ii));
				if (userModel.isDisabled())
					continue;

				// 帐户过滤
				if (!filterValue.equals("")) {
					if (filterType.equals("USERNAME")) {
						if (userModel.getUserName().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("USERNO")) {
						if (userModel.getUserNo().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("UID")) {
						if (userModel.getUID().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("OFFICETEL")) {
						if (userModel.getOfficeTel().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("OFFICEFAX")) {
						if (userModel.getOfficeFax().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("MOBILE")) {
						if (userModel.getMobile().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("EMAIL")) {
						if (userModel.getEmail().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("POSITIONNAME")) {
						if (userModel.getPositionName().indexOf(filterValue) == -1)
							continue;
					}
					if (filterType.equals("JJTEL")) {
						if (userModel.getJjTel().indexOf(filterValue) == -1)
							continue;
					}
				}
				sumUsers++;
				String strOfficeTel = (userModel.getOfficeTel() == null || userModel.getOfficeTel().trim().equals("")|| userModel.getOfficeTel().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getOfficeTel();
				String strOfficeFax = (userModel.getOfficeFax() == null || userModel.getOfficeFax().trim().equals("")|| userModel.getOfficeFax().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getOfficeFax();
				String strCode = (userModel.getUserNo() == null || userModel.getUserNo().trim().equals("")|| userModel.getUserNo().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getUserNo();
				String strMobile = (userModel.getMobile() == null || userModel.getMobile().trim().equals("")|| userModel.getMobile().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getMobile();
//				String strEMail = (userModel.getEmail() == null || userModel.getEmail().trim().equals("")|| userModel.getEmail().trim().indexOf("null")!=-1) ? "&nbsp;" : "<a target='mainFrame' href=./login.wf?sid=" + this.getContext().getSessionId() + "&cmd=Email_API_SendMail&mailTo=" + userModel.getEmail() + ">" + userModel.getEmail() + "</a>";
				String strEMail = (userModel.getEmail() == null || userModel.getEmail().trim().equals("")|| userModel.getEmail().trim().indexOf("null")!=-1) ? "&nbsp;" : "<a href='###'>" + userModel.getEmail() + "</a>";
				String strDept = AdressUtil.getSubDeptNameTitle(depModel.getDepartmentFullNameOfCache(), depModel.getDepartmentName());
				String strPositionName = (userModel.getPositionName() == null || userModel.getPositionName().trim().equals("")|| userModel.getPositionName().trim().indexOf("null")!=-1) ? "&nbsp;" : userModel.getPositionName();
			
				
				
				String strUserName = userModel.getUserNameAlias();

				iRowNum++;
				String modify = "";
				String tip = OrgUtil.getOnlineAlt(userModel.getUID());
				if (isMaster || userModel.getUID().equals(getContext().getUID())) {
					//modify = "<a href='' onclick=\"modifyAddress(frmMain,'Inner_Address_Modify_Detail','" + userModel.getUID() + "');return false;\"><img src=../aws_img/find_obj.gif border=0 alt='"+I18nRes.findValue(getContext().getLanguage(),"修改这个信息")+"'></a>";
				} 
				/**
				 * 20140829
				 * modify wangaz
				 * 修改通讯录查询条件为直等，并且将鼠标移动到姓名上的事件去掉
				 */
				if(strUserName.equals(filterValue) || strDept.equals(filterValue) || strMobile.equals(filterValue) || strEMail.equals(filterValue)||strPositionName.equals(filterValue)|| strCode.equals(filterValue) || userModel.getUID().equals(filterValue) || strOfficeTel.equals(filterValue) || strOfficeFax.equals(filterValue) || userModel.getJjTel().equals(filterValue)){
				sbAddressList.append("<tr>\n");
				sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>"  + tip + "</td>\n");
				for (int iii = 0; iii < fieldList.size(); iii++) {
					EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
					// 首先判断他能否访问这个字段
					if (accessFieldList.get(model.getKey()) == null) {
						sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap valign=top>-</td>\n");
					} else {
						if (model.getKey().equals("DEPARTMENTNAME")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap valign=top>" + I18nRes.findValue(getContext().getLanguage(), strDept)  + "</td>\n");
						} else if (model.getKey().equals("USERNO")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" +  I18nRes.findValue(getContext().getLanguage(), strCode) + "</td>\n");
						} else if (model.getKey().equals("USERNAME")) {
							//sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" + I18nRes.findValue(getContext().getLanguage(), strUserName) + "</td>\n");
								sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" 
										+ "<span id='"+AddressUtil.getTooltipSpanId(userModel, isUserMap(userModel, depModel), sumUsers)+"'>" + I18nRes.findValue(getContext().getLanguage(), strUserName) + modify
										+ "</span>\n"+
										/**wangaz
										 * 将鼠标移上去的事件去掉，姓名出
										(AddressUtil.getTooltipScript(userModel, getContext().getSessionId(),isUserMap(userModel, depModel),sumUsers))
										*/
										"</td>\n");
						} else if (model.getKey().equals("UID")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" + userModel.getUID() + "</td>\n");
						} else if (model.getKey().equals("OFFICETEL")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" + I18nRes.findValue(getContext().getLanguage(), strOfficeTel)  + "</td>\n");
						} else if (model.getKey().equals("OFFICEFAX")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" + strOfficeFax + "</td>\n");
						} else if (model.getKey().equals("MOBILE")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" + strMobile + "</td>\n");
						} else if (model.getKey().equals("EMAIL")) {
							sbAddressList.append("<td align='left' bgcolor=").append(bgColor).append(" nowrap>" + strEMail + "</td>\n");
						} else if (model.getKey().equals("POSITIONNAME")) {
							sbAddressList.append("<td align='left' style=\"padding:4px;\"  bgcolor=").append(bgColor).append(" nowrap>" + strPositionName + "</td>\n");
						} else if (model.getKey().equals("JJTEL")) {
							sbAddressList.append("<td align='left' style=\"padding:4px;\"  bgcolor=").append(bgColor).append(" nowrap>" + userModel.getJjTel() + "</td>\n");
						}
					}
				}
				}
			}
			iRowNum++;
		}
		if (iRowNum == 0) {
			sbAddressList.append("<tr><td colspan='").append((fieldList.size() + 1)).append("' align='center'><img src=../aws_img/warning.gif><I18N#没有查询记录></td></tr>\n");
		}
		sbAddressList.append("</table>\n");

		Hashtable hashTags = new Hashtable();
		hashTags.put("toolBar", toolbar.toString());
		hashTags.put("companyid", strCompanyId);
		hashTags.put("addresslist", sbAddressList.toString());
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("title", "");
		hashTags.put("deptTreeHtml", "");
		hashTags.put("deptTreeDisplay", "none");
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_Page_Detail.htm"), hashTags);
	}
}
