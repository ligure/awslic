package com.actionsoft.awf.organization.addresstree;

import com.actionsoft.application.server.LICENSE;
import com.actionsoft.awf.commons.security.ac.util.AccessControlUtil;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.organization.cache.CompanyCache;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.RoleCache;
import com.actionsoft.awf.organization.cache.TeamCache;
import com.actionsoft.awf.organization.cache.TeamMemberCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.cache.UserMapCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.organization.model.TeamMemberModel;
import com.actionsoft.awf.organization.model.TeamModel;
import com.actionsoft.awf.organization.model.UserMapModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.organization.util.OrgUtil;
import com.actionsoft.awf.organization.util.SecurityUtil;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.plugs.address.outer.dao.OuterAddress;
import com.actionsoft.plugs.address.outer.dao.OuterAddressDaoFactory;
import com.actionsoft.plugs.address.outer.model.OuterAddressModel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
/**
 * 
 * @description 修改平台自带的地址簿方法，将组织树默认不显示。
 * @version 1.0
 * @author wangaz
 * @update 2014-3-18 下午06:06:27
 */
public class OrganizationAddressTreeWeb extends ActionsoftWeb
{
  private static final boolean IS_SHOW_MAP = false;
  UserContext _me;
  private int documentLayerSecurity = 0;

  public OrganizationAddressTreeWeb(UserContext me) {
    super(me);
    this._me = me;
  }

  public String getAddressWindow(int groupstyle, String groupvalue, String mail_target, String address, String grid) {
    String cgi = "./mailtree.wf";
    String sid = getContext().getSessionId();
    Hashtable hashTags = new Hashtable(4);

    hashTags.put("AWSAddress_CompanyPanel", 
      cgi + "?sid=" + sid + "&cmd=Address_Inner_Company_Open&grid=" + String.valueOf(grid));

    hashTags.put("AWSAddress_Target", cgi + "?sid=" + sid + "&cmd=Address_Inner_Target_Open&address=" + address + 
      "&mail_target=" + mail_target + "&grid=" + grid);
    hashTags.put("sid", super.getSIDFlag());
    hashTags.put("targetAddress", address);
    hashTags.put("metaDataMapUUID", mail_target);
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("AddressBook_Frame.htm"), hashTags);
  }

  public String showCompany(int groupStyle, String grid)
  {
    CompanyModel companyModel = getContext().getCompanyModel();
    String sid = "<input type=hidden name=sid value=" + getContext().getSessionId() + ">\n";
    StringBuffer companyList = new StringBuffer("");
//    companyList
//      .append("<select name='select_company'  class ='actionsoftSelect' onchange=\"changeCompanyAddress(frmMain,'Address_Inner_Account_Open2');return false;\">");
    companyList
    .append("<select Style='display:none' name='select_company'  class ='actionsoftSelect' onchange=\"changeCompanyAddress(frmMain,'Address_Inner_Account_Open2');return false;\">");

    Map h = CompanyCache.getList();
    if (h != null)
    {
      if (companyModel == null)
        companyList.append("<option value=0>请选择</option>");
      else {
        companyList.append("<option value=" + companyModel.getId() + ">" + 
          I18nRes.findValue(getContext().getLanguage(), companyModel.getCompanyName()) + "</option>");
      }
      if (!LICENSE.getASPModel()) {
        for (int i = 0; i < h.size(); i++) {
          CompanyModel model = (CompanyModel)h.get(new Integer(i));
          if ((companyModel.getId() == model.getId()) || 
            (!SecurityUtil.hasCompanySec(getContext(), model.getId()))) continue;
          companyList.append("<option value=" + model.getId() + " >" + model.getCompanyName() + 
            "</option>");
        }
      }

      companyList.append("</select>");
    }
    String enter = ""; String cancel = "";
    if ((grid.indexOf("|") != -1) && (grid.indexOf("true") != -1)) {
      enter = "parent.AWSAddress_Target.insertAddressGrid(parent.AWSAddress_Target.frmMain);return false;";

      cancel = "parent.parent.AWSORGOperateWinObj.hide();";
    } else {
      enter = "parent.AWSAddress_Target.insertAddress(parent.AWSAddress_Target.frmMain);return false;";
      cancel = "parent.window.close();return false;";
    }
    Hashtable hashTags = new Hashtable(2);
    hashTags.put("companyList", companyList.toString());
    hashTags.put("sid", sid);
    hashTags.put("grid", "");
    hashTags.put("groupStyle", showGroup(groupStyle));
    hashTags.put("groupstyle", String.valueOf(groupStyle));
    hashTags.put("enter", enter);
    hashTags.put("cancel", cancel);
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("AddressBook_Company.htm"), hashTags);
  }

  private String showGroup(int groupstyle)
  {
    StringBuffer groupIndexHtml = new StringBuffer("");
  /**  if (!LICENSE.getASPModel()) {
      if (groupstyle == 0) {
        groupIndexHtml
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_显示方式"))
          .append("<input type='radio' name='groupstyle' value=0  checked onclick=\"changeCompanyAddress2(frmMain,this,'Address_Inner_Account_Open2');\">")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按组织结构"));
        groupIndexHtml
          .append("<input type='radio' name='groupstyle' value=1 onclick=\"changeCompanyAddress2(frmMain,this,'Address_Inner_Account_Open2');\">")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按角色"));
        groupIndexHtml
          .append("<input type='radio' name='groupstyle' value=2 onclick=\"changeCompanyAddress2(frmMain,this,'Address_Inner_Account_Open2');\">")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按团队"));
      }
      if (groupstyle == 1) {
        groupIndexHtml
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_显示方式"))
          .append("<input type='radio' name='groupstyle' value=0 onclick='changeGroupAddress(frmMain,'Address_Inner_Account_Open');'>")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按组织结构"));
        groupIndexHtml
          .append("<input type='radio' name='groupstyle' value=1 checked onclick='changeGroupAddress(frmMain,'Address_Inner_Account_Open');'>")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按角色"));
        groupIndexHtml
          .append("<input type='radio' name='groupstyle' value=2 onclick='changeGroupAddress(frmMain,'Address_Inner_Account_Open');'>")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按组"));
      }
      if (groupstyle == 2) {
        groupIndexHtml
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_显示方式"))
          .append("<input type='radio' name='groupstyle' value=0 onclick='changeGroupAddress(frmMain,'Address_Inner_Account_Open');'>")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按组织结构"));
        groupIndexHtml
          .append("<input type='radio' name='groupstyle' value=1 onclick='changeGroupAddress(frmMain,'Address_Inner_Account_Open');'>")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按角色"));
        groupIndexHtml
          .append("<input type='radio' name='groupstyle' value=2 checked onclick='changeGroupAddress(frmMain,'Address_Inner_Account_Open');'>")
          .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_按组"));
      }

    }*/
    groupIndexHtml
    .append("&nbsp");
    return groupIndexHtml.toString();
  }

  public String getAddressTree(int companyid, int groupstyle, String filterValue, boolean isShowAll)
  {
    StringBuffer treeList = new StringBuffer("");
    String sid = "<input type=hidden name=sid value=" + getContext().getSessionId() + ">\n";
    if (companyid == 0) {
      companyid = getContext().getCompanyModel().getId();
    }
    if (groupstyle == 0)
    {
      Hashtable rootDepartmentList = DepartmentCache.getDepartmentListOfLayer(1, companyid);
      if (rootDepartmentList != null)
      {
        int ownerDepartmentRootId = 0;

        for (int i = 0; i < rootDepartmentList.size(); i++) {
          DepartmentModel rootModel = (DepartmentModel)rootDepartmentList.get(new Integer(i));
          if (rootModel.getId() == ownerDepartmentRootId) {
            treeList.append(getNodeObjectOfDepartment("root", rootModel, false));
            StringBuffer tmpSub = new StringBuffer();
            treeList.append(eachORG(rootModel, tmpSub));
          }
          else {
            treeList.append(getNodeObjectOfDepartment("root", rootModel, true));
          }
        }

        treeList.append("\ttreePanel.render();\n root.expand(false,true);\n");
        if (ownerDepartmentRootId > 0)
        {
          treeList.append("AWS_NODE_OD_ID_" + ownerDepartmentRootId + ".toggle();\n");
        }
      }
    } else if (groupstyle == 1)
    {
      Map roleList = RoleCache.getList();
      String[] groupNames = RoleCache.getGroupNames();
      for (int i = 0; i < groupNames.length; i++) {
        String groupName = groupNames[i];
        String node = "var AWS_NODE_RG_ID_" + i + "= new Ext.tree.TreeNode({id:'AWS_NODE_RG_ID_" + i + 
          "',text:'" + groupName + 
          "',type:'RoleGroup',loader:new Ext.tree.TreeLoader({dataUrl:encodeURI('./login.wf?sid=" + 
          getContext().getSessionId() + 
          "&cmd=Address_Inner_Tree_JSONDATE'),baseParams:{requestType:'RoleGroup',param1:'" + groupName + 
          "',param2:'" + companyid + "'}}),'leaf':false,'cls':'x-tree-node-roleGroup','checked':false,'wasChecked':false});\n";
        treeList.append(node + "root.appendChild(AWS_NODE_RG_ID_" + i + ");\n");

        if (roleList != null) {
          for (int ii = 0; ii < roleList.size(); ii++) {
            RoleModel roleModel = (RoleModel)roleList.get(new Integer(ii));
            if (roleModel.getGroupName().equals(groupName)) {
              treeList.append(getNodeObjectOfRole("AWS_NODE_RG_ID_" + i, roleModel, companyid));
            }
          }
        }
      }
      treeList.append("\ttreePanel.render();\n root.expand(false, true);\n");
    } else if (groupstyle == 2)
    {
      Map teamHash = TeamCache.getList();
      LinkedList teamModels = new LinkedList();
      for (int i = 0; i < teamHash.size(); i++) {
        TeamModel model = (TeamModel)teamHash.get(new Integer(i));
        boolean r = AccessControlUtil.accessControlCheck(this._me, "ORGTEAM", String.valueOf(model.getId()),"R");
        boolean rw = AccessControlUtil.accessControlCheck(super.getContext(), "ORGTEAM", String.valueOf(model.getId()), "RW");
        boolean isAccess = false;
        if ((r) || (rw)) {
          isAccess = true;
        }
        if ((model.getTeamType() == 0) || (model.getTeamType() == this._me.getID()) || (isAccess)) {
          teamModels.add(model);
        }
      }
      int i = 0; for (int size = teamModels.size(); i < size; i++) {
        TeamModel teamModel = (TeamModel)teamModels.get(new Integer(i).intValue());
        String node = "var AWS_NODE_OT_ID_" + teamModel.getId() + 
          "= new Ext.tree.AsyncTreeNode({id:'AWS_NODE_OT_ID_" + teamModel.getId() + "',text:'" + 
          teamModel.getTeamName() + 
          "',type:'Team',loader:new Ext.tree.TreeLoader({dataUrl:encodeURI('./login.wf?sid=" + 
          getContext().getSessionId() + 
          "&cmd=Address_Inner_Tree_JSONDATE'),baseParams:{requestType:'Team',param1:'" + 
          teamModel.getId() + 
          "'}}),'leaf':false,'cls':'x-tree-node-team','checked':false,'wasChecked':false});\n";
        treeList.append(node + "root.appendChild(AWS_NODE_OT_ID_" + teamModel.getId() + ");\n");
      }
      treeList.append("\ttreePanel.render();\n root.expand(false, true);\n");
    }
    Hashtable hashTags = new Hashtable(2);
    hashTags.put("treeList", treeList.toString());
    hashTags.put("companyid", Integer.toString(companyid));
    hashTags.put("isShowAll", isShowAll ? "1" : "0");
    hashTags.put("sid", sid);
    hashTags.put("onLoadJs", "d.openTo(" + filterValue + ",true);");
    if (filterValue.equals("0"))
      hashTags.put("searchResult", "");
    else {
      hashTags.put("searchResult", getSearchResult(filterValue));
    }
//    hashTags.put("userDepartFullId", getUserTreeFulId(groupstyle));
    if (groupstyle == 0)
      hashTags.put("treeName", I18nRes.findValue(getContext().getLanguage(), "aws.portal_组织结构"));
//    else if (groupstyle == 1)
//      hashTags.put("treeName", I18nRes.findValue(getContext().getLanguage(), "aws.portal_角色"));
//    else if (groupstyle == 2)
//      hashTags.put("treeName", I18nRes.findValue(getContext().getLanguage(), "aws.portal_团队"));
//    else
//      hashTags.put("treeName", "");
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("AddressBook_Tree.htm"), hashTags);
  }

  public String getUserTreeFulId(int groupstyle)
  {
    StringBuffer full = new StringBuffer();
    UserModel model = (UserModel)UserCache.getModel(this._me.getUID());
    if (groupstyle == 0) {
      DepartmentModel departModel = (DepartmentModel)DepartmentCache.getModel(model.getDepartmentId());
      String[] fullId = departModel.getDepartmentFullIdOfCache().split("/");
      for (int i = 0; i < fullId.length; i++) {
        if ((fullId[i] != null) && (fullId[i].trim().length() != 0)) {
          full.append("AWS_NODE_OD_ID_").append(fullId[i]).append("|");
        }
      }
      if (fullId.length > 0)
        full.setLength(full.length() - 1);
    }
    else if (groupstyle == 1) {
      Map roleList = RoleCache.getList();

      String[] groupNames = RoleCache.getGroupNames();
      for (int i = 0; i < groupNames.length; i++) {
        String groupName = groupNames[i];
        if (roleList != null) {
          for (int ii = 0; ii < roleList.size(); ii++) {
            RoleModel roleModel = (RoleModel)roleList.get(new Integer(ii));
            if ((!roleModel.getGroupName().equals(groupName)) || (roleModel.getId() != model.getRoleId())) continue;
            full.append("AWS_NODE_RG_ID_").append(i).append("|").append("AWS_NODE_OR_ID_")
              .append(roleModel.getId());
          }
        }
      }
    }
    else if (groupstyle == 2) {
      Map teamHash = TeamCache.getList();
      if (teamHash != null) {
        for (int i = 0; i < teamHash.size(); i++) {
          TeamModel teamModel = (TeamModel)teamHash.get(new Integer(i));
          Map teamMemberList = TeamMemberCache.getListOfMember(teamModel.getId());
          for (int p = 0; p < teamMemberList.size(); p++) {
            TeamMemberModel teamMemberModel = (TeamMemberModel)teamMemberList.get(new Integer(p));
            if (teamMemberModel.getUserId().equals(model.getUID())) {
              full.append("AWS_NODE_OT_ID_").append(teamModel.getId()).append("|");
            }
          }
        }
      }
    }
    return full.toString();
  }

  private String eachORG(DepartmentModel departmentModel, StringBuffer js)
  {
    Hashtable subDeptList = DepartmentCache.getSubDepartmentList(departmentModel.getId());
    if (subDeptList != null) {
      for (int i = 0; i < subDeptList.size(); i++) {
        DepartmentModel model = (DepartmentModel)subDeptList.get(new Integer(i));
        js.append(getNodeObjectOfDepartment("AWS_NODE_OD_ID_" + departmentModel.getId(), model, false));
      }

    }

    Hashtable userHash = UserCache.getUserListOfDepartment(departmentModel.getId());
    if (userHash != null) {
      for (int i = 0; i < userHash.size(); i++) {
        UserModel userModel = (UserModel)userHash.get(new Integer(i));
        if (userModel.isDisabled())
          continue;
        js.append(getNodeObjectOfUser("AWS_NODE_OD_ID_" + departmentModel.getId(), userModel));
      }
    }
    if (subDeptList != null) {
      for (int i = 0; i < subDeptList.size(); i++) {
        DepartmentModel model = (DepartmentModel)subDeptList.get(new Integer(i));

        js = new StringBuffer(eachORG(model, js));
      }
    }

    return js.toString();
  }

  public String getJsonTreeOfORG(String param)
  {
    StringBuffer jsonStr = new StringBuffer("[");
    if ("0".equals(param)) {
      String sql = "select groupname from PUB_OUTERADDRESS where userid='" + getContext().getUID() + 
        "' group by groupname order by groupname";
      Hashtable groupList = OuterAddressDaoFactory.createOuterAddress().getSearchResultInstance(sql);
      for (int i = 0; i < groupList.size(); i++) {
        OuterAddressModel model = (OuterAddressModel)groupList.get(new Integer(i));
        jsonStr.append("{'id':'AWS_NODE_PERSON_GROUP_" + model._id + i + "',");
        jsonStr.append("'nid':'" + model._id + i + "',");
        jsonStr.append("'text':'" + model._groupName + "',");
        jsonStr.append("'cls':'x-tree-node-department',");
        jsonStr.append("'leaf':false,");
        jsonStr.append("'draggable':false,");
        jsonStr.append("'type':'Group'},");
      }
    } else {
      String sql = "select * from PUB_OUTERADDRESS where userid='" + getContext().getUID() + "' and groupname='" + 
        param + "' order by fullname asc";
      Hashtable addressList = OuterAddressDaoFactory.createOuterAddress().getSearchResultInstance(sql);

      for (int i = 0; i < addressList.size(); i++) {
        OuterAddressModel model = (OuterAddressModel)addressList.get(new Integer(i));
        if (model._pMail.length() > 0) {
          jsonStr.append("{'id':'AWS_NODE_PERSON_" + model._id + "',");
          jsonStr.append("'nid':'" + model._id + "',");
          jsonStr.append("'text':'" + model._fullName + "',");
          jsonStr.append("'email':'" + model._pMail + "',");
          jsonStr.append("'qtip':'" + model._pMail + "',");
          jsonStr.append("'cls':'x-tree-node-person',");
          jsonStr.append("'leaf':true,");
          jsonStr.append("'type':'Person'},");
        }
      }
    }

    if (jsonStr.lastIndexOf(",") > -1) {
      jsonStr.setLength(jsonStr.length() - 1);
    }
    jsonStr.append("]");
    return jsonStr.toString();
  }

  private String getJsonOfObject(String name)
  {
    StringBuffer json = new StringBuffer();
    Hashtable list = RoleCache.getListOfGroup(name);
    if (list.size() != 0) {
      for (int i = 0; i < list.size(); i++) {
        RoleModel model = (RoleModel)list.get(new Integer(i));
        json.append("{'id':'AWS_NODE_RG_SUB_" + model.getId() + "',");
        json.append("'text':'" + model.getRoleName() + "',");
        json.append("'type':'SubRoleGroup',");
        json.append("'leaf':false,");
        json.append("'checked':false,");
        json.append("'wasChecked':false,");
        json.append("'cls':'x-tree-node-roleGroup'},");
      }
    }
    return json.toString();
  }

  private boolean checkDocumentLayerSecurityEmailAddress(String uid)
  {
    if (this.documentLayerSecurity > 0) {
      for (int i = 3; i >= this.documentLayerSecurity; i--) {
        if ((i == 1) && (SecurityProxy.checkDocumentLayerSecurity(uid, "AWFDocumentLayerUnit_秘密")))
          return true;
        if ((i == 2) && (SecurityProxy.checkDocumentLayerSecurity(uid, "AWFDocumentLayerUnit_机密")))
          return true;
        if ((i == 3) && (SecurityProxy.checkDocumentLayerSecurity(uid, "AWFDocumentLayerUnit_绝密"))) {
          return true;
        }
      }
    }
    else {
      return true;
    }
    return false;
  }

  public String getJsonTreeOfORG(String requestType, String param1, String param2, String param3)
  {
    StringBuffer jsonStr = new StringBuffer("[");
    if (requestType.equals("Department")) {
      int rootDepartmentId = Integer.parseInt(param1);
      if (OrgUtil.getOrgPeopleListPriority().equals("low"))
        getPeopleListPriorityLow(jsonStr, rootDepartmentId);
      else if (OrgUtil.getOrgPeopleListPriority().equals("high"))
        getPeopleListPriorityHigh(jsonStr, rootDepartmentId);
    }
    else if (requestType.equals("Role")) {
      int rootRoleId = Integer.parseInt(param1);
      int rootCompanyId;
      if ("".equals(param2))
        rootCompanyId = getContext().getCompanyModel().getId();
      else {
        rootCompanyId = Integer.parseInt(param2);
      }

      ArrayList userListExist = new ArrayList();

      Hashtable deptList = DepartmentCache.getListOfCompany(rootCompanyId);
      for (int ii = 0; ii < deptList.size(); ii++) {
        DepartmentModel departmentModel = (DepartmentModel)deptList.get(new Integer(ii));
        Hashtable userList = UserCache.getUserListOfDepartment(departmentModel.getId());
        for (int iii = 0; iii < userList.size(); iii++) {
          UserModel currentUser = (UserModel)userList.get(new Integer(iii));
          if ((currentUser.getRoleId() != rootRoleId) || (currentUser.isDisabled()) || 
            (userListExist.contains(currentUser.getUID()))) continue;
          userListExist.add(currentUser.getUID());
          jsonStr.append(getJsonOfUser(currentUser)).append(",");
        }

        Hashtable userMapList = UserMapCache.getMapListOfDepartment(departmentModel.getId());
        for (int iiii = 0; iiii < userMapList.size(); iiii++) {
          UserMapModel currentMapUser = (UserMapModel)userMapList.get(new Integer(iiii));
          if (currentMapUser.getRoleId() == rootRoleId) {
            UserModel uModel = (UserModel)UserCache.getModel(currentMapUser.getMapId());
            if ((uModel.isDisabled()) || (userListExist.contains(uModel.getUID())))
              continue;
            userListExist.add(uModel.getUID());
            jsonStr.append(getJsonOfUser(uModel)).append(",");
          }
        }
      }
    }
    else if (requestType.equals("Team")) {
      int teamRootId = Integer.parseInt(param1);

      Map teamMemberList = TeamMemberCache.getListOfMember(teamRootId);
      for (int p = 0; p < teamMemberList.size(); p++) {
        TeamMemberModel teamMemberModel = (TeamMemberModel)teamMemberList.get(new Integer(p));
        UserModel userModel = (UserModel)UserCache.getModel(teamMemberModel.getUserId());
        if (userModel.isDisabled()) {
          continue;
        }
        DepartmentModel dm = (DepartmentModel)DepartmentCache.getModel(userModel.getDepartmentId());

        if ((dm.getCompanyId() != getContext().getCompanyModel().getId()) && 
          (!SecurityUtil.hasCompanySec(getContext(), dm.getCompanyId())))
        {
          continue;
        }

        jsonStr.append(getJsonOfUser(userModel)).append(",");
      }
    }
    if (jsonStr.toString().lastIndexOf(",") > -1)
      jsonStr.setLength(jsonStr.length() - 1);
    jsonStr.append("]");

    return jsonStr.toString();
  }

  public void getPeopleListPriorityHigh(StringBuffer jsonStr, int rootDepartmentId)
  {
    Hashtable userHash = UserCache.getUserListOfDepartment(rootDepartmentId);
    if (userHash != null) {
      for (int i = 0; i < userHash.size(); i++) {
        UserModel userModel = (UserModel)userHash.get(new Integer(i));
        if ((userModel.isDisabled()) || (!userModel.isManager()))
          continue;
        jsonStr.append(getJsonOfUser(userModel)).append(",");
      }
    }

    if (userHash != null) {
      for (int i = 0; i < userHash.size(); i++) {
        UserModel userModel = (UserModel)userHash.get(new Integer(i));
        if ((userModel.isDisabled()) || (userModel.isManager()))
          continue;
        jsonStr.append(getJsonOfUser(userModel)).append(",");
      }
    }

    Hashtable userMapHash = UserMapCache.getMapListOfDepartment(rootDepartmentId);
    ArrayList userListExist = new ArrayList();
    if (userMapHash != null) {
      for (int i = 0; i < userMapHash.size(); i++) {
        UserMapModel userMapModel = (UserMapModel)userMapHash.get(new Integer(i));
        if (userMapModel.isShow()) {
          UserModel userModel = (UserModel)UserCache.getModel(userMapModel.getMapId());
          if ((userModel.isDisabled()) || (!userModel.isManager()) || ((userHash != null) && (userHash.contains(userModel))) || (!checkDocumentLayerSecurityEmailAddress(userModel.getUID())))
            continue;
          if (userListExist.contains(Integer.valueOf(userModel.getId()))) {
            continue;
          }
          userListExist.add(Integer.valueOf(userModel.getId()));
          jsonStr.append(getJsonOfUser(userModel, userMapModel)).append(",");
        }
      }
    }

    if (userMapHash != null) {
      for (int i = 0; i < userMapHash.size(); i++) {
        UserMapModel userMapModel = (UserMapModel)userMapHash.get(new Integer(i));
        if (userMapModel.isShow()) {
          UserModel userModel = (UserModel)UserCache.getModel(userMapModel.getMapId());
          if ((userModel.isDisabled()) || (userModel.isManager()) || ((userHash != null) && (userHash.contains(userModel))) || (!checkDocumentLayerSecurityEmailAddress(userModel.getUID())))
            continue;
          jsonStr.append(getJsonOfUser(userModel, userMapModel)).append(",");
        }
      }
    }

    Hashtable subDepartmentList = DepartmentCache.getSubDepartmentList(rootDepartmentId);
    if (subDepartmentList != null)
      for (int i = 0; i < subDepartmentList.size(); i++) {
        DepartmentModel departmentModel = (DepartmentModel)subDepartmentList.get(new Integer(i));
        jsonStr.append(getJsonOfDepartment(departmentModel)).append(",");
      }
  }

  public void getPeopleListPriorityLow(StringBuffer jsonStr, int rootDepartmentId)
  {
    Hashtable subDepartmentList = DepartmentCache.getSubDepartmentList(rootDepartmentId);
    if (subDepartmentList != null) {
      for (int i = 0; i < subDepartmentList.size(); i++) {
        DepartmentModel departmentModel = (DepartmentModel)subDepartmentList.get(new Integer(i));
        jsonStr.append(getJsonOfDepartment(departmentModel)).append(",");
      }
    }

    Hashtable userHash = UserCache.getUserListOfDepartment(rootDepartmentId);
    if (userHash != null) {
      for (int i = 0; i < userHash.size(); i++) {
        UserModel userModel = (UserModel)userHash.get(new Integer(i));
        if ((userModel.isDisabled()) || (!userModel.isManager()))
          continue;
        jsonStr.append(getJsonOfUser(userModel)).append(",");
      }
    }

    Hashtable userMapHash = UserMapCache.getMapListOfDepartment(rootDepartmentId);
    ArrayList userListExist = new ArrayList();
    if (userMapHash != null) {
      for (int i = 0; i < userMapHash.size(); i++) {
        UserMapModel userMapModel = (UserMapModel)userMapHash.get(new Integer(i));
        if (userMapModel.isShow()) {
          UserModel userModel = (UserModel)UserCache.getModel(userMapModel.getMapId());
          if ((userModel.isDisabled()) || (!userModel.isManager()) || ((userHash != null) && (userHash.contains(userModel))) || (!checkDocumentLayerSecurityEmailAddress(userModel.getUID())))
            continue;
          if (userListExist.contains(Integer.valueOf(userModel.getId()))) {
            continue;
          }
          userListExist.add(Integer.valueOf(userModel.getId()));
          jsonStr.append(getJsonOfUser(userModel, userMapModel)).append(",");
        }
      }
    }

    if (userHash != null) {
      for (int i = 0; i < userHash.size(); i++) {
        UserModel userModel = (UserModel)userHash.get(new Integer(i));
        if ((userModel.isDisabled()) || (userModel.isManager()))
          continue;
        jsonStr.append(getJsonOfUser(userModel)).append(",");
      }
    }
    if (userMapHash != null)
      for (int i = 0; i < userMapHash.size(); i++) {
        UserMapModel userMapModel = (UserMapModel)userMapHash.get(new Integer(i));
        if (userMapModel.isShow()) {
          UserModel userModel = (UserModel)UserCache.getModel(userMapModel.getMapId());
          if ((userModel.isDisabled()) || (userModel.isManager()) || ((userHash != null) && (userHash.contains(userModel))) || (!checkDocumentLayerSecurityEmailAddress(userModel.getUID())))
            continue;
          jsonStr.append(getJsonOfUser(userModel, userMapModel)).append(",");
        }
      }
  }

  private String getJsonOfDepartment(DepartmentModel model)
  {
    StringBuffer jsonStr = new StringBuffer("{");
    jsonStr.append("'id':'AWS_NODE_OD_ID_" + model.getId() + "',");
    jsonStr.append("'text':'" + model.getDepartmentName() + "',");
    jsonStr.append("'cls':'x-tree-node-department',");
    jsonStr.append("'wasChecked':false,");
    jsonStr.append("'leaf':false,");
    jsonStr.append("'checked':false,");
    jsonStr.append("'type':'Department'");

    jsonStr.append("}");
    return jsonStr.toString();
  }

  private String getJsonOfRole(RoleModel model)
  {
    StringBuffer jsonStr = new StringBuffer("{");
    jsonStr.append("'id':'AWS_NODE_OR_ID_" + model.getId() + "',");
    jsonStr.append("'text':'" + model.getRoleName() + "',");
    jsonStr.append("'cls':'x-tree-node-role',");
    jsonStr.append("'wasChecked':false,");
    jsonStr.append("'leaf':false,");
    jsonStr.append("'checked':false,");
    jsonStr.append("'type':'role'");
    jsonStr.append("}");
    return jsonStr.toString();
  }

  private String getJsonOfUser(UserModel model)
  {
    StringBuffer jsonStr = new StringBuffer("{");
    jsonStr.append("'id':'AWS_NODE_OU_ID_" + model.getId() + "',");
    jsonStr.append("'text':'" + model.getUserName() + OrgUtil.getOnlineAlt(model.getUID()) + "',");
    if (model.isManager()) {
      jsonStr.append("'cls':'x-tree-node-manager',");
      jsonStr.append("'qtip':'" + I18nRes.findValue(super.getContext().getLanguage(), "管理者，登录帐户") + "：" + model.getUID() + "',");
    } else {
      jsonStr.append("'cls':'x-tree-node-user',");
      jsonStr.append("'qtip':'" + I18nRes.findValue(super.getContext().getLanguage(), "登录帐户") + "：" + model.getUID() + "',");
    }
    jsonStr.append("'leaf':true,");
    jsonStr.append("'wasChecked':false,");
    jsonStr.append("'checked':false,");
    jsonStr.append("'uid':'" + model.getUID() + "',");
    jsonStr.append("'type':'user'");

    jsonStr.append("}");
    return jsonStr.toString();
  }

  private String getJsonOfUser(UserModel model, UserMapModel userMapMpdel)
  {
    StringBuffer jsonStr = new StringBuffer("{");
    jsonStr.append("'id':'AWS_NODE_OU_ID_" + userMapMpdel.getId() + "',");
    jsonStr.append("'text':'" + model.getUserName() + OrgUtil.getOnlineAlt(model.getUID()) + "',");
    if (userMapMpdel.isManager()) {
      jsonStr.append("'cls':'x-tree-node-manager',");
      jsonStr.append("'qtip':'" + I18nRes.findValue(super.getContext().getLanguage(), "管理者，登录帐户") + "：" + model.getUID() + "',");
    } else {
      jsonStr.append("'cls':'x-tree-node-user',");
      jsonStr.append("'qtip':'" + I18nRes.findValue(super.getContext().getLanguage(), "登录帐户") + "：" + model.getUID() + "',");
    }
    jsonStr.append("'leaf':true,");
    jsonStr.append("'wasChecked':false,");
    jsonStr.append("'checked':false,");
    jsonStr.append("'uid':'" + model.getUID() + "',");
    jsonStr.append("'type':'user'");

    jsonStr.append("}");
    return jsonStr.toString();
  }

  private String getNodeObjectOfDepartment(String parentNode, DepartmentModel model, boolean isAsync)
  {
    String nodeType = "AsyncTreeNode";
    if (!isAsync)
      nodeType = "TreeNode";
    String node = "var AWS_NODE_OD_ID_" + model.getId() + "= new Ext.tree." + nodeType + "({id:'AWS_NODE_OD_ID_" + 
      model.getId() + "',text:'" + model.getDepartmentName() + 
      "',type:'Department',loader:new Ext.tree.TreeLoader({dataUrl:encodeURI('./login.wf?sid=" + 
      getContext().getSessionId() + 
      "&cmd=Address_Inner_Tree_JSONDATE'),baseParams:{requestType:'Department',param1:'" + model.getId() + 
      "'}}),'leaf':false,'cls':'x-tree-node-department','checked':false,'wasChecked':false});\n";
    return node + parentNode + ".appendChild(AWS_NODE_OD_ID_" + model.getId() + ");\n";
  }

  private String getNodeObjectOfRole(String parentNode, RoleModel model, int companyid)
  {
    String nodeType = "AsyncTreeNode";
    String node = "var AWS_NODE_OR_ID_" + model.getId() + "= new Ext.tree." + nodeType + "({id:'AWS_NODE_OR_ID_" + 
      model.getId() + "',text:'" + model.getRoleName() + 
      "',type:'Role',loader:new Ext.tree.TreeLoader({dataUrl:encodeURI('./login.wf?sid=" + 
      getContext().getSessionId() + 
      "&cmd=Address_Inner_Tree_JSONDATE'),baseParams:{requestType:'Role',param1:'" + model.getId() + 
      "',param2:'" + companyid + "'}}),'leaf':false,'cls':'x-tree-node-role','checked':false,'wasChecked':false});\n";
    return node + parentNode + ".appendChild(AWS_NODE_OR_ID_" + model.getId() + ");\n";
  }

  private String getNodeObjectOfUser(String parentNode, UserModel model)
  {
    String isManager = "";
    if (model.isManager())
      isManager = "'cls':'x-tree-node-manager','qtip':'" + I18nRes.findValue(super.getContext().getLanguage(), "管理者，登录帐户") + "：" + model.getUID() + "'";
    else {
      isManager = "'cls':'x-tree-node-user','qtip':'" + I18nRes.findValue(super.getContext().getLanguage(), "登录帐户") + "：" + model.getUID() + "'";
    }
    String node = "var AWS_NODE_OU_ID_" + model.getId() + "= new Ext.tree.TreeNode({id:'AWS_NODE_OU_ID_" + 
      model.getId() + "',text:'" + model.getUserName() + "',type:'User','leaf':true," + isManager + 
      ",'checked':false,'uid':'" + model.getUID() + "','wasChecked':false});\n";
    return node + parentNode + ".appendChild(AWS_NODE_OU_ID_" + model.getId() + ");\n";
  }

  private String getSearchResult(String filterValue)
  {
    Map userHash = UserCache.getList();
    StringBuffer html = new StringBuffer();
    html.append("<table width='100%' border='1'  cellspacing='0' cellpadding='0' bordercolorlight='#CCCCCC' bordercolordark='#FFFFFF'>");
    for (int i = 0; i < userHash.size(); i++) {
      UserModel model = (UserModel)userHash.get(new Integer(i));
      boolean filterUnit = true;
      if (!LICENSE.getASPModel()) {
        filterUnit = true;
      } else {
        DepartmentModel myDeptModel = (DepartmentModel)DepartmentCache.getModel(model.getDepartmentId());
        if (myDeptModel.getCompanyId() != getContext().getCompanyModel().getId())
          filterUnit = false;
        else {
          filterUnit = true;
        }
      }
      if ((model == null) || (!filterUnit) || 
        ((model.getUID().toUpperCase().indexOf(filterValue.toUpperCase()) == -1) && 
        (model.getUserName().toUpperCase().indexOf(filterValue.toUpperCase()) == -1)) || 
        (model.isDisabled())) {
        continue;
      }
      DepartmentModel myDeptModel = (DepartmentModel)DepartmentCache.getModel(model.getDepartmentId());
      if (myDeptModel == null || !SecurityUtil.hasCompanySec(getContext(), myDeptModel.getCompanyId()))
      {
        continue;
      }
      html.append("<tr>");
      html.append("<td width='20%' class=actionsoftReportData align=right nowrap=\"nowrap\"><strong><I18N#用户帐户></strong></td>");
      html.append("<td width='80%'  class=actionsoftReportData><input type='checkBox' name=")
        .append(model.getUID()).append(" value='").append(model.getUID()).append("<")
        .append(I18nRes.findValue(getContext().getLanguage(), model.getUserName())).append(">'>")
        .append(model.getUID()).append("</td>");
      html.append("</tr>");
      html.append("<tr>");
      html.append("<td  class=actionsoftReportData><strong><I18N#用户姓名></strong></td>");
      html.append("<td  class=actionsoftReportData>")
        .append(I18nRes.findValue(getContext().getLanguage(), model.getUserName()))
        .append(OrgUtil.getOnlineAlt(model.getUID())).append("</td>");
      html.append("</tr>");
      html.append("<tr>");
      String departmentName = DepartmentCache.getFullName(model.getDepartmentId());
      html.append("<td class=actionsoftReportData><div align='right'><strong><I18N#所在部门></strong></div></td>");
      html.append("<td class=actionsoftReportData>")
        .append(I18nRes.findValue(getContext().getLanguage(), departmentName)).append("</td>");
      html.append("</tr>");
      html.append("<tr>");
      RoleModel roleModel = (RoleModel)RoleCache.getModel(model.getRoleId());
      html.append("<td class=actionsoftReportData><div align='right'><strong><I18N#角色名称></strong></div></td>");
      html.append("<td class=actionsoftReportData>")
        .append(I18nRes.findValue(getContext().getLanguage(), roleModel.getRoleName()))
        .append("</td>");
      html.append("</tr>");
      html.append("<tr>");
      html.append("<td colspan=2 valign='top'>&nbsp;</td>");
      html.append("</tr>");
    }

    html.append("</table>");

    return html.toString();
  }

  public String showMailTarget(String mail_target, String address, String p3)
  {
    StringBuffer optBut = new StringBuffer("");
    StringBuffer list = new StringBuffer("");

    String sid = "<input type=hidden name=sid value=" + getContext().getSessionId() + ">\n";

    optBut.append(
      "<a href='###'  onclick='selectTo();return false;'><img src='<I18N#../aws_img/address/addressCompany_add.gif>' border=\"0\" title='")
      .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_添加")).append("'></a>\n");
    optBut.append(
      "<a href='###' onclick='removeitem();return false;'><img src='<I18N#../aws_img/address/addressCompany_move.gif>' border=\"0\" title='")
      .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_移走")).append("'></a>\n");
    optBut.append(
      "<a href='###' onclick='delAll();return false;'><img src='<I18N#../aws_img/address/addressCompany_allMove.gif>' border=\"0\" title='")
      .append(I18nRes.findValue(getContext().getLanguage(), "aws.portal_全移走")).append("'></a>&nbsp;&nbsp;\n");

    UtilString myStr = new UtilString(address.trim());

    StringBuffer js = new StringBuffer();
    if ((p3.indexOf("|") != -1) && (p3.indexOf("true") != -1)) {
      UtilString util = new UtilString(p3);
      Vector v = util.split("|");
      String enitiyName = "";
      if ((v != null) && (v.size() > 0))
        enitiyName = v.get(1).toString();
      js.append("<script>\n");
      js.append("function getParameter(value){\n");
      js.append("stroeRowIndex=this.parent.parent.window.").append(enitiyName)
        .append(".Grid.getCurrentRowInd();\n");
      js.append("this.parent.parent.window.")
        .append(enitiyName)
        .append(".Grid.getDataSource().getAt(stroeRowIndex).set(frmMain.mail_target.value,value.replace(/“/g,'\"'));\n");
      js.append("this.parent.parent.window.").append(enitiyName).append(".Grid.getGridPanel().stopEditing();\n");
      js.append("}\n");
      js.append("</script>\n");
    }
    Hashtable hashTags = new Hashtable(5);
    hashTags.put("optBut", optBut.toString());
    hashTags.put("list", list.toString());
    hashTags.put("sid", sid);
    hashTags.put("mail_target", mail_target);
    hashTags.put("js", js);
    return RepleaseKey.replace(HtmlModelFactory.getHtmlModel("AddressBook_Target.htm"), hashTags);
  }
}