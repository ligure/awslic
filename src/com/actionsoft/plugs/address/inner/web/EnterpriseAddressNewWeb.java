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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.actionsoft.awf.commons.security.ac.util.AccessControlUtil;
import com.actionsoft.awf.commons.security.mgtgrade.util.GradeSecurityUtil;
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
import com.actionsoft.plugs.address.inner.util.AddressUtil;
import com.actionsoft.plugs.address.inner.util.AddressUtilNew;
import com.actionsoft.plugs.address.inner.util.EnterpriseAddressConfig;
import com.actionsoft.plugs.address.inner.util.EnterpriseAddressFieldModel;
import com.actionsoft.plugs.address.util.AdressUtil;

/**
 * 
 * @author Zhanghf
 * @version 1.0
 * @desc 现通讯录样式未使用，切换样式时执行
 * @update 2014-05-07取消邮箱点击发邮件功能修改410,705行，Email_API_SendMail
 */
public class EnterpriseAddressNewWeb extends ActionsoftWeb {
	private int sumUsers=0;
	private final String tableHead = "<table align='center' width='95%' cellpadding='2' cellspacing='0' border='1' bordercolor='#999999' style='border-collapse:collapse'>\n";
	private int currentDeptUserCount = 0;
	private boolean limit = false;

	public EnterpriseAddressNewWeb(UserContext me) {
		super(me);
	}
	
	public String getAcStr(int depId) {
		String ac = "";
//		if(GradeSecurityUtil.isSuperMaster(getContext().getUID())) {
//			ac = "<a href='' onclick=\"openAc(frmMain," + depId + ",'InnerAddressAC','AC_Action_Open'); return false\"><img src='../aws_img/user8.gif' border=0 align='absmiddle' alt='设置修改当前部门人员信息的权限'></a>";
//		}
		return ac;
	}

	/**
	 * 企业通讯录，部门通讯录列表
	 * 
	 * @author wjx
	 * @modify jackliu
	 * @modify Zhanghf
	 * @version 1.1
	 */
	public String getDetailWeb(String strCompanyId, String strDeptId, String filterType, String filterValue) {
		currentDeptUserCount=0;
	
		StringBuffer sbAddressList = new StringBuffer();
		sbAddressList.append(tableHead);
		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		sbAddressList.append(this.getTableCaption());
		sbAddressList.append(getTableDetail(strCompanyId, strDeptId, filterType, filterValue, true));
		//sbAddressList.append(getSubDepartment(null, depModel, filterType, filterValue, count, 1, deptTreeHtml));
		sbAddressList.append("</table>\n");
		
		String toolbar = this.getSearchToolbar(filterType, filterValue);
		
		String noUser = "";
		int deptId = Integer.parseInt(strDeptId);
		String deptName = ((DepartmentModel)DepartmentCache.getModel(deptId)).getDepartmentName();
		if(currentDeptUserCount==0 && GradeSecurityUtil.isSuperMaster(getContext().getUID())) {
			noUser = deptName + "&nbsp;" + getAcStr(deptId);
		}

		Hashtable hashTags = new Hashtable();
		hashTags.put("searchToolbar", toolbar);
		hashTags.put("addresslist", sbAddressList.toString());
		hashTags.put("companyid", strCompanyId);
		hashTags.put("deptid", strDeptId);
		hashTags.put("noUser", noUser);
		hashTags.put("sid", super.getSIDFlag());
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_Page_New_Detail.htm"), hashTags);
	}

	public String getPortalIndex(String strCompanyId) {
		String treeURL = "";
		String detailURL = "";
		String sid = super.getContext().getSessionId();
		try {
			sid = URLEncoder.encode(super.getContext().getSessionId(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		treeURL = "./login.wf?sid="+sid+"&cmd=Inner_Address_ShowDeptTree_New&companyId="+strCompanyId;
		CompanyModel companyModel = (CompanyModel) CompanyCache.getModel(Integer.parseInt(strCompanyId));
		int  deptId=getContext().getDepartmentModel().getId();
		int  companyId=getContext().getCompanyModel().getId();
		if(companyModel!=null){
			Hashtable rootDepartmentList = DepartmentCache.getDepartmentListOfLayer(1, companyModel.getId());
			if(rootDepartmentList.size()>0){
				DepartmentModel model=(DepartmentModel) rootDepartmentList.get(0);
				if(model!=null){
					deptId=model.getId();
					companyId=Integer.parseInt(strCompanyId);
				}
			}
		}
		detailURL = "./login.wf?sid="+sid+"&cmd=Inner_Address_ShowDetail_New&companyId="+companyId+"&deptId="+deptId;
		EnterpriseAddressNewDeptTreeWeb treeWeb = new EnterpriseAddressNewDeptTreeWeb(getContext());
		String treeData = treeWeb.getTreeData(strCompanyId);
		Hashtable hashTags = new Hashtable();
		hashTags.put("sessionId", getContext().getSessionId());
		hashTags.put("AWSTree", treeData);
		hashTags.put("treeURL", treeURL);
		hashTags.put("detailURL", detailURL);
		hashTags.put("sid", super.getSIDFlag());
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_Page_New.htm"), hashTags);
	}

	/**
	 * 输出表格标题
	 * @param fieldList
	 * @return
	 * @author ZHF
	 */
	public String getTableCaption() {
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
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>\n");
		// 显示头
		int count = fieldList.size();
		
		for (int i = 0; i < count; i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
			if (!accessFieldList.containsKey(model.getKey())) {
				continue;
			}
			sb.append("<td height=25 style='background-color:cccccc;font-size:13px;color:000000' align='center' width='").append(model.getWidth()).append("' nowrap ><I18N#").append(model.getTitle()).append("></td>\n");
		}
		sb.append("</tr>\n");
		//增加一行，实现部门无人员时，也能对该部门进行授权操作
//		sb.append("<tr id='deptNoHaveUser1' style='display:none;'><td id='deptNoHaveUser2' colspan='"+count+"' valign='middle' bgcolor='#ffffff'></td></tr>\n");
		return sb.toString();
	}
	
	public String getSearchToolbar(String filterType, String filterValue) {
		if (!filterValue.equals(""))
			filterValue = filterValue.trim();
		if (filterType.equals(""))
			filterType = "USERNAME";// 默认按姓名查询
		StringBuffer toolbar = new StringBuffer();
		toolbar.append("\n<style media=print>.Noprint{display:none;}</style>\n");
		toolbar.append("<div style='height:26px;border-bottom:solid 1px gray;padding:2px 10px 2px 0px;margin:0px 0px 2px 0px;background-color:#eeeeee;vertical-align:middle;' align=right class=Noprint>\n");
		toolbar.append("<table align='right' cellpadding='0' cellspacing='0'  bgcolor='#eeeeee'>\n");
		toolbar.append("<tr><td align='right' valign='middle'>\n");
		if (!filterValue.equals("")) {
			int companyId = this.getContext().getCompanyModel().getId();
			int deptId = this.getContext().getDepartmentModel().getId();
			toolbar.append("<a href='./login.wf?sid="+this.getContext().getSessionId()+"&cmd=Inner_Address_ShowDetail_New&companyId="+companyId+"&deptId="+deptId+"' title='"+I18nRes.findValue(getContext().getLanguage(),"点击显示本部门人员信息")+"'><span style='color:gray'><I18N#显示本部门人员></span></a>\n");
		}
		toolbar.append("<a href='' onClick='JavaScript:window.print();return false;'><img src=../aws_img/Print16.gif alt='打印当前页面' border=0 align='absmiddle'><I18N#打印></a>\n");
		//toolbar.append("<a href='' onClick=\"downAddressFile(frmMain,'Inner_Address_Down_Detail'," + strCompanyId + ");return false;\"><img src=../aws_img/download2.gif alt='下载通讯录文件到本地' border=0 align='absmiddle'>下载</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		// 准备工具条
		StringBuffer filter = new StringBuffer();
		filter.append("<select name='filterType'>\n");
		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		HashMap accessFieldList = new HashMap();// 用户可以看到的字段（有些数据只能某些角色范围内能看到）
		for (int i = 0; i < fieldList.size(); i++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
			if(!model.getKey().equals("NUM")){
				filter.append("<option value='").append(model.getKey()).append("'").append(model.getKey().equals(filterType) ? " selected" : "").append(">").append( I18nRes.findValue(getContext().getLanguage(), model.getTitle())).append("</option>\n");
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
		filter.append("<input type=text name=filterValue class='actionsoftInput' onBlur=\"blurField(this,'"+I18nRes.findValue(getContext().getLanguage(),"请输入查询条件")+"')\" style=\"color: #999;\" onFocus=\"focusField(this,'"+I18nRes.findValue(getContext().getLanguage(),"请输入查询条件")+"')\" onkeypress=\"search_onkeypress(frmMain,'Inner_Address_Search_New');\" value='").append(filterValue.equals("") ? I18nRes.findValue(getContext().getLanguage(), "请输入查询条件") : filterValue).append("'>\n");
		filter.append("<a href='' onclick=\"filterAddress2(frmMain,'Inner_Address_Search_New');return false;\"><img src=../aws_img/zoomInBtn.gif border=0 align='absmiddle'>&nbsp;<I18N#查询></a>\n");
		toolbar.append(filter);
		toolbar.append("</td></tr>\n</table>\n");
		toolbar.append("</div>");
		return toolbar.toString();
	}
	
	public String getSearchResult(String filterType, String filterValue) {
		StringBuffer sb = new StringBuffer();
		Map htCompanyList = CompanyCache.getList();
		sb.append(this.tableHead);
		sb.append(this.getTableCaption());
		Map htDepartment = DepartmentCache.getList();
		for(int j=0;j<htDepartment.size();j++) {
			DepartmentModel depModel = (DepartmentModel) htDepartment.get(new Integer(j));
			int companyId = depModel.getCompanyId();
			int deptId = depModel.getId();
			String deptName = depModel.getDepartmentName();
			// 部门过滤
			if (!filterValue.equals("")) {
				if (filterType.equals("DEPARTMENTNAME")) {
					if (deptName.indexOf(filterValue) == -1) {
						continue;
					}
				}
			}
			sb.append(getTableDetail(String.valueOf(companyId), String.valueOf(deptId), filterType, filterValue, false));
		}
		sb.append("</table>");
		
		String toolbar = this.getSearchToolbar(filterType, filterValue);
		
		Hashtable hashTags = new Hashtable();
		hashTags.put("searchToolbar", toolbar);
		hashTags.put("addresslist", sb.toString());
		hashTags.put("companyid", "0");
		hashTags.put("deptid", "0");
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("noUser", "");
		return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("InnerAddress_Page_New_Detail.htm"), hashTags);
	}
	
	/**
	 * 获得通讯录表格列表，不包括<table>标签
	 * @param strCompanyId 公司ID
	 * @param strDeptId 部门ID
	 * @param filterType 过滤类型
	 * @param filterValue 过滤类型的值
	 * @param isShowSubDept 是否显示子部门
	 * @return
	 * @author ZHF
	 */
	public String getTableDetail(String strCompanyId, String strDeptId, String filterType, String filterValue, boolean isShowSubDept) {
//		Map htCompanyList = CompanyCache.getList();
//		CompanyModel companymodel = (CompanyModel) CompanyCache.getModel(Integer.parseInt(strCompanyId));
		StringBuffer sbAddressList = new StringBuffer();
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
		
		int count = fieldList.size();
		
		//TODO: 修改通讯录人员信息的权限控制
		// 判断是否有可修改人员信息的权限
		boolean isMaster = false;
		//if (("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel()._roleName + ",") > -1) {
		//	isMaster = true;
		//}
		boolean rule = ("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1;
		boolean bAc = AccessControlUtil.accessControlCheck(getContext(), "InnerAddressAC", strDeptId, "R");//表示可修改人员信息
		if(rule || bAc) {
			isMaster = true;
		}
		if(bAc) {//当判断为当前用户可访问这个部门时，将全局limit至为true，以便子部门也能有权限修改人员信息
			limit = true;
		} else {//向该部门的上级部门查找是否有权限
			limit = getParentDeptAc(Integer.parseInt(strDeptId));
		}

		String bgColor = "'#ffffff'";
		//根据部门ID列出该部门全部人员
		int deptId = Integer.parseInt(strDeptId);
		DepartmentModel depModel = (DepartmentModel)DepartmentCache.getModel(deptId);
		Hashtable userHash = new Hashtable();
		Hashtable user = UserCache.getUserListOfDepartment(deptId);
		//获取该部门的所有兼职信息
		Hashtable usermap = UserMapCache.getMapListOfDepartment(deptId);
		//利用原部门用户列表和兼职信息列表，获得全部的人员信息列表
		userHash = getNewUserList(user, usermap);
		
		int mapSize = usermap.size();
		
		StringBuffer sbAddressList2=new StringBuffer();
		if (userHash != null) {
			//sbAddressList.append("<tr><td  align=\"left\" valign=\"top\" bgcolor = '#F2F2F2' colspan=").append(count).append("><img style='display:none' src= '../aws_img/expand_but2.gif' name = deptName").append(depModel._id).append("><b>").append(depModel._departmentName).append("</b></td></tr>");
			int usercount = userHash.size();
			if (usercount < 1) {
				if(isShowSubDept) {
					//显示子部门信息
					sbAddressList.append(getSubDepartmentString(depModel, filterType, filterValue, count, 1));
				}
			} else {
				int jj=0;
				Vector vcTd = new Vector();
				Hashtable htTd = null;
				for (int ii = 0; ii < userHash.size(); ii++) {
					UserModel userModel = (UserModel) userHash.get(new Integer(ii));
					
					AddressUtilNew util = new AddressUtilNew();
					Hashtable htuser = util.getUserModel(userModel);
					// 如果是超级管理员，或者是帐户被注销，继续下层循环
					if (userModel.isDisabled() || userModel.getUID().equals("admin")) {
						usercount=usercount-1;
						continue;
					}
					// 帐户过滤
					if (!filterValue.equals("")) {
						DepartmentModel dm = (DepartmentModel) DepartmentCache.getModel(userModel.getDepartmentId());
						if (!SecurityUtil.hasCompanySec(getContext(), dm.getCompanyId())) {
							continue;
						}
						boolean next = false;
						for (int k = 0; k < fieldList.size(); k++) {
							EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(k));
							if(model.getKey().equals("DEPARTMENTNAME")) {
								continue;
							}
							if(filterType.equals(model.getKey())) {
								String filter = (String)htuser.get(model.getKey());
								if(filter.indexOf(filterValue) == -1) {
									next = true;
									break;
								}
							}
						}
						if(next) {
							continue;
						}
					}//end 帐户过滤

//					String email = "<a href='###' onclick=\"openSendEmailWeb('"+userModel.getEmail()+"','"+userModel.getUserName()+"','./login.wf?sid=" + this.getContext().getSessionId() + "&cmd=Email_API_SendMail&mailTo=" + userModel.getEmail() + "')\">" + userModel.getEmail() + "</a>";
					String email = "<a href='###'>" + userModel.getEmail() + "</a>";
					String strDeptName = depModel.getDepartmentName();
					String strDeptFullName = depModel.getDepartmentFullNameOfCache();
					// 根据级次缩进
					if (depModel.getLayer() > 1) {
						for (int tab = 1; tab < depModel.getLayer(); tab++) {
							//strDeptName = "&nbsp;&nbsp;" + strDeptName;
						}
					}

					sumUsers++;

					// iRowNum++;
					//TODO: 修改通讯录人员信息的权限控制
					String ac = getAcStr(depModel.getId());
					String admin = "";
					String onlineAlt = OrgUtil.getOnlineAlt(userModel.getUID());
					if (limit || isMaster || userModel.getUID().equals(getContext().getUID())) {
						admin = "<a href='#' onclick=\"modifyAddress(frmMain,'Inner_Address_Modify_Detail_New','" + userModel.getUID() + "');return false;\"><img src=../aws_img/find_obj.gif border=0 align='absmiddle' alt='"+I18nRes.findValue(getContext().getLanguage(),"修改这个信息")+"'></a> ";
					} else {
						admin = "";
					}
					//权限信息代码结束
					String strtmp = "";
					htTd = new Hashtable();
					for (int iii = 0; iii < fieldList.size(); iii++) {
						EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
						if (!accessFieldList.containsKey(model.getKey())) {
							continue;
						}
						// 首先判断他能否访问这个字段
						if (accessFieldList.get(model.getKey()) == null) {
							strtmp = "<td valign='middle' bgcolor='#ffffff'>-</td>\n";
						} else {
							//根据key从htuser对象中取值
							String field = (String)htuser.get(model.getKey());
							if (field == null) {
								field = "";
							}
							String title = "", nowrap = "";
							if (model.getKey().equals("DEPARTMENTNAME")) {
								field = strDeptName + "&nbsp;" + ac;
								title = " title='"+strDeptFullName+"'";
								nowrap = " nowrap";
							} else if (model.getKey().equals("USERNAME")) {
								field = onlineAlt+ "<span id='"+AddressUtil.getTooltipSpanId(userModel, isUserMap(userModel, depModel), sumUsers)+"'>" + field + "</span>"+admin+"\n"
										+ AddressUtil.getTooltipScript(userModel, getContext().getSessionId(), isUserMap(userModel, depModel), sumUsers);
								nowrap = " nowrap";
							} else if (model.getKey().equals("POSITIONNAME")) {
								if(isUserMap(userModel, depModel)) {
									field = field + "<font color='blue' title='"+I18nRes.findValue(getContext().getLanguage(),"兼任职位")+"'>("+I18nRes.findValue(getContext().getLanguage(),"兼")+")</font>";
								}
							} else if (model.getKey().equals("EMAIL")) {
								field = field.equals("")?field:(email);
							} else if (model.getKey().equals("NUM")) {
								field = String.valueOf(sumUsers);
								nowrap = " nowrap align='center'";
							}
							strtmp = "<td valign='middle' bgcolor='#ffffff'" + nowrap + title + ">" + field + "</td>\n";
						}
						htTd.put(model.getKey()+String.valueOf(iii), strtmp);
					}
					vcTd.add(htTd);
					jj++;//当前部门人员累加的和
					currentDeptUserCount++;
				}// end for
				
				StringBuffer sb = new StringBuffer();
				//根据Vector重新输出表格
				for(int j=0;j<vcTd.size();j++) {
					htTd = (Hashtable)vcTd.elementAt(j);
					Set e = fieldList.keySet();//得到可以显示的列数
					sb.append("<tr>\n");
					for(int k=0;k<e.size();k++) {//根据列数循环出一行
						EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(k));
						if (!accessFieldList.containsKey(model.getKey())) {
							continue;
						}
						if(j==0 && model.getKey().equals("DEPARTMENTNAME")) {//当该部门部门第一行时输出部门，实现部门跨行合并单元格
							String key = model.getKey()+String.valueOf(k);
							String dept = htTd.get(key).toString();
							//String deptName = dept.substring(dept.indexOf("nowrap>")+"nowrap>".length(),dept.lastIndexOf("</td>"));
							//sb.append("<td align='left' valign='middle' bgcolor=" + bgColor + " rowspan='"+jj+"'>"+deptName+"</td>\n");
							String newDept = dept.replaceAll("nowrap", "rowspan='"+jj+"'");
							sb.append(newDept);
						} else {
							if(model.getKey().equals("DEPARTMENTNAME")) {//当不是当前部门的第一行数据，但是部门列，跳过
								continue;
							}
							sb.append(htTd.get(model.getKey()+String.valueOf(k)));
						}
					}
					sb.append("</tr>\n\n");
					
				}
				sbAddressList.append(sb.toString());
				if(isShowSubDept) {
					//显示子部门信息
					sbAddressList.append(getSubDepartmentString(depModel, filterType, filterValue, count, 1));
				}
			}//usercount>0 end
		} else {//userHash为空时
			if(isShowSubDept) {
				//显示子部门信息
				sbAddressList.append(getSubDepartmentString(depModel, filterType, filterValue, count, 1));
			}
		}
		return sbAddressList.toString();
	}
	
	private boolean isUserMap(UserModel userModel, DepartmentModel depModel) {
		Hashtable mapModels=(Hashtable) UserMapCache.getMapListOfDepartment(depModel.getId());
		for (int i = 0; i < mapModels.size(); i++) {
			UserMapModel mapModel=(UserMapModel) mapModels.get(i);
			if(mapModel.getMapId()==userModel.getId()){
				if(mapModel.isShow()){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 根据一个部门，获得其子部门的列表，获得子部门的人员信息
	 * @param depModel
	 * @param filterType
	 * @param filterValue
	 * @param count
	 * @param forcount
	 * @return
	 */
	private String getSubDepartmentString(DepartmentModel depModel, String filterType, String filterValue, int count, int forcount) {
		StringBuffer sb = new StringBuffer();
		//取得子部门信息
		Hashtable subDepartmentList = DepartmentCache.getSubDepartmentList(depModel.getId());
		if (subDepartmentList != null || subDepartmentList.size() != 0) {
			for (int iii = 0; iii < subDepartmentList.size(); iii++) {
				DepartmentModel subdeptModel = (DepartmentModel) subDepartmentList.get(new Integer(iii));
				sb.append(getSubDepartment(depModel, subdeptModel, filterType, filterValue, count, forcount));
			}
		}
		return sb.toString();
	}
	
	/**
	 * 根据部门信息，获得上级部门是否有访问权限
	 * @param deptId
	 * @return
	 */
	private boolean getParentDeptAc(int deptId) {
		DepartmentModel model = (DepartmentModel)DepartmentCache.getModel(deptId);
		if(model==null) {
			return false;
		}
		int parentDeptId = model.getParentDepartmentId();
		if(parentDeptId==0) {
			return false;
		}
		boolean ac = false;
		if(AccessControlUtil.accessControlCheck(getContext(), "InnerAddressAC", String.valueOf(parentDeptId), "R")) {
			return true;
		} else {
			ac = getParentDeptAc(parentDeptId);
			return ac;
		}
	}

	/**
	 * 获得子部门信息
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
	private String getSubDepartment(DepartmentModel rootModel, DepartmentModel depModel, String filterType, String filterValue, int Feildcount, int forcount) {
		boolean limitSubDept = false;
		StringBuffer sbAddressList = new StringBuffer();
		if (!filterValue.equals(""))
			filterValue = filterValue.trim();
		if (filterType.equals(""))
			filterType = "USERNAME";// 默认按姓名查询

		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段

		//TODO: 修改通讯录人员信息的权限控制
		// 判断是否有可修改人员信息的权限
		boolean isMaster = false;
		//if (("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel()._roleName + ",") > -1) {
		//	isMaster = true;
		//}
		boolean rule = ("," + EnterpriseAddressConfig.getAddressModel().getMasterRole() + ",").indexOf("," + getContext().getRoleModel().getRoleName() + ",") > -1;
		boolean bAc = AccessControlUtil.accessControlCheck(getContext(), "InnerAddressAC", String.valueOf(depModel.getId()), "R");//表示可修改人员信息
		if(rule || bAc) {
			isMaster = true;
		}
		if(getParentDeptAc(depModel.getId())) {//判断为当前用户可访问这个部门的上级部门时，以便子部门也能有权限修改人员信息
			limitSubDept = true;
		} else {
			limitSubDept = false;
		}

		int iRowNum = 0;
		String bgColor = "'#ffffff'";
		
		Hashtable userHash = new Hashtable();
		// 得到这个部门的所有人员定义
		Hashtable user = UserCache.getUserListOfDepartment(depModel.getId());
		//获取该部门的所有兼职信息
		Hashtable usermap = UserMapCache.getMapListOfDepartment(depModel.getId());
		//利用原部门用户列表和兼职信息列表，获得全部的人员信息列表
		userHash = getNewUserList(user, usermap);
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
		
		int usercount = AdressUtil.getWorkEmployee(userHash);
		if (usercount < 1) {
			//显示子部门信息
			sbAddressList.append(getSubDepartmentString(depModel, filterType, filterValue, Feildcount, 1));
		} else {
			// 根据级次缩进
			String backgroud = "line1.gif";
			if (depModel.getLayer() > 1) {
				backgroud = "line" + depModel.getLayer() + ".gif";
			}

			if (userHash != null) {
				int num = 0;
				StringBuffer tmpAddressList = new StringBuffer();
				for (int ii = 0,jj=0; ii < userHash.size(); ii++) {
					UserModel userModel = (UserModel) userHash.get(new Integer(ii));
					AddressUtilNew util = new AddressUtilNew();
					Hashtable htuser = util.getUserModel(userModel);

					if (userModel.isDisabled() || userModel.getUID().equals("admin")) {
						continue;
					}
					
					// 帐户过滤
					if (!filterValue.equals("")) {
						boolean next = false;
						for (int k = 0; k < fieldList.size(); k++) {
							EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(k));
							if(model.getKey().equals("DEPARTMENTNAME")) {
								continue;
							}
							if(filterType.equals(model.getKey())) {
								String filter = (String)htuser.get(model.getKey());
								if(filter.indexOf(filterValue) == -1) {
									next = true;
									break;
								}
							}
						}
						if(next) {
							continue;
						}
					}
					iRowNum++;
					sumUsers++;
					String admin = "";
					String onlineAlt = OrgUtil.getOnlineAlt(userModel.getUID());
					if (limitSubDept || isMaster || userModel.getUID().equals(this.getContext().getUID())) {
						admin = "<a href='#' onclick=\"modifyAddress(frmMain,'Inner_Address_Modify_Detail_New','" + userModel.getUID() + "');return false;\"><img src=../aws_img/find_obj.gif border=0 align='absmiddle' alt='"+I18nRes.findValue(getContext().getLanguage(),"修改这个信息")+"'></a> ";
					} else {
						admin = " ";
					}
					//准备email链接
//					String email1 = "<a target='mainFrame' href=./login.wf?sid=" + getContext().getSessionId() + "&cmd=Email_API_SendMail&mailTo=";
					String email1 = "<a href='###'";
					String email2 = ">";
					String email3 = "</a>";

					int count = fieldList.size();
					//根据fieldList输出表格的一行
					for (int i = 0; i < count; i++) {
						String field = "", title = "", nowrap = "";
						EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(i));
						if (!accessFieldList.containsKey(model.getKey())) {
							continue;
						}
						field = (String)htuser.get(model.getKey());
						if (field == null) {
							field = "";
						}
						if (model.getKey().equals("DEPARTMENTNAME")) {//如果是部门字段，跳过
							if (jj == 0) {
								tmpAddressList.append("<#deptnametd>\n");
							}
							continue;
						} else if (model.getKey().equals("USERNAME")) {//如果是姓名字段，拼接是否可管理链接
							field = onlineAlt + "<span id='"+AddressUtil.getTooltipSpanId(userModel, isUserMap(userModel, depModel), sumUsers)+"'>" + field + "</span>"+admin+"\n"
									+ AddressUtil.getTooltipScript(userModel, getContext().getSessionId(), isUserMap(userModel, depModel), sumUsers);
							nowrap = " nowrap";
						} else if (model.getKey().equals("POSITIONNAME")) {
							if(isUserMap(userModel, depModel)) {
								field = field + "<font color='blue' title='"+I18nRes.findValue(getContext().getLanguage(),"兼任职位")+"'>("+I18nRes.findValue(getContext().getLanguage(),"兼")+")</font>";
							}
						} else if (model.getKey().equals("EMAIL")) {//如果是email字段，拼接email链接
							field = field.equals("")?field:(email1+field+email2+field+email3);
						} else if (model.getKey().equals("NUM")) {
							field = String.valueOf(sumUsers);
							nowrap = " nowrap align='center'";
						}
						tmpAddressList.append("<td valign='middle' bgcolor='#ffffff'" + nowrap + ">" + field + "</td>\n");
					}
					jj++;
					tmpAddressList.append("</tr>\n");
					num++;
				}
				//sbAddressList.append("<tr>\n<td valign=\"middle\" bgcolor = '#ffffff' rowspan=").append(num).append(">").append(AdressUtil.getSubDeptNameTitle(depModel._departmentFullNameOfCache, depModel._departmentName)).append("</td>");
				String tmp = "<td valign='middle' bgcolor='#ffffff' rowspan='"+num+"' title='"+depModel.getDepartmentFullNameOfCache()+"'>"+depModel.getDepartmentName()+"&nbsp;"+getAcStr(depModel.getId())+"</td>\n";
//				int len = sbAddressList.length();//先求出sbAddressList的长度
//				sbAddressList.append(tmp);
//				if (tmpAddressList.toString().equals("")) {//只要tmpAddressList为空时，就把tmp串删除
//					//此行代码会出现搜索时出现无人员的问题，
//					//sbAddressList.append("<td colspan="+(colspan-1)+" bgcolor=" + bgColor + " nowrap><img src='../aws_img/warning.gif' align='absmiddle'>该部门暂无职员</td>\n");
//					sbAddressList.delete(len, len+tmp.length());//将tmp串删除，解决上面出现的问题
//				} else {
				sbAddressList.append("<tr>\n");
				sbAddressList.append(tmpAddressList.toString().replace("<#deptnametd>", tmp));
//				}

			}
			//显示子部门信息
			sbAddressList.append(getSubDepartmentString(depModel, filterType, filterValue, Feildcount, 1));
		}
		return sbAddressList.toString();
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
			isManager = userMapModel.isShow();
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
			if(!isManager&&isShow) {
				newList.put(new Integer(j), userModel);
				j++;
			}
		}
		return newList;
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
				html.append("编号：<input type=text name=userNo maxLength=32 class=actionsoftInput size=50 value='" + userModel.getUserNo() + "'><br>\n");
			} else if (model.getKey().equals("USERNAME")) {
				html.append("姓名：<input type=text name=userName maxLength=32 class=actionsoftInput size=50 value='" + userModel.getUserName() + "'><br>\n");
			} else if (model.getKey().equals("POSITIONNAME")) {
				html.append("职务：<input type=text name=positionName maxLength=32 class=actionsoftInput size=50 value='" + userModel.getPositionName() + "'><br>\n");
			} else if (model.getKey().equals("OFFICETEL")) {
				html.append("电话：<input type=text name=tel maxLength=32 class=actionsoftInput size=50 value='" + userModel.getOfficeTel() + "'><br>\n");
			} else if (model.getKey().equals("OFFICEFAX")) {
				html.append("传真：<input type=text name=fax maxLength=32 class=actionsoftInput size=50 value='" + userModel.getOfficeFax() + "'><br>\n");
			} else if (model.getKey().equals("MOBILE")) {
				html.append("手机：<input type=text name=mobile maxLength=32 class=actionsoftInput size=50 value='" + userModel.getMobile() + "'><br>\n");
			} else if (model.getKey().equals("EMAIL")) {
				html.append("邮件：<input type=text name=email maxLength=32 class=actionsoftInput size=50 value='" + userModel.getEmail() + "'><br>\n");
			}
		}
		html.append("<hr>");
		html.append("<div align=right><input type=button value='更新信息'  class ='actionsoftButton' onClick=\"modifyAddressAction(frmMain,'Inner_Address_Modify_Detail_Action_New');return false;\"  border='0' >&nbsp;&nbsp;<input type=button value='关闭窗口'  class ='actionsoftButton' onClick=\"window.close();\" border='0' ></div>");

		Hashtable hashTags = new Hashtable();
		hashTags.put("addresslist", html.toString());
		hashTags.put("uid", uid);
		hashTags.put("userName", userModel.getUserName());
		hashTags.put("sid", super.getSIDFlag());
		hashTags.put("page_title", "修改[" + userModel.getUserName() + "]的通讯信息");
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
	public String updateInfo(String uid, String userName, String userNo, String tel, String fax, String mobile, String email, String positionName) {
		UserModel user = (UserModel) UserCache.getModel(uid);

		HashMap fieldList = EnterpriseAddressConfig.getDisplayField();// 允许显示的字段
		for (int iii = 0; iii < fieldList.size(); iii++) {
			EnterpriseAddressFieldModel model = (EnterpriseAddressFieldModel) fieldList.get(new Integer(iii));
			if (model.getKey().equals("NO")) {
				user.setUserNo(userNo);
			} else if (model.getKey().equals("USERNAME")) {
				user.setUserName(userName);
			} else if (model.getKey().equals("POSITIONNAME")) {
				user.setPositionName(positionName);
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
}
