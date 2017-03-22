package com.actionsoft.application.server.socketcommand;

import com.actionsoft.application.portal.lfm.StyleManagerWeb;
import com.actionsoft.application.portal.manage.PortalManager;
import com.actionsoft.application.portal.manage.ResourceManager;
import com.actionsoft.application.portal.model.SysPortalModel;
import com.actionsoft.application.portal.model.UserPortalModel;
import com.actionsoft.application.portal.navigation.cache.NavigationFunctionCache;
import com.actionsoft.application.portal.navigation.model.NavigationFunctionModel;
import com.actionsoft.application.portal.navigation.web.RuntimeNavigation;
import com.actionsoft.application.portal.portlet.OnlinePortlet;
import com.actionsoft.application.portal.portlet.TaskWorklistPortlet;
import com.actionsoft.application.portal.portlet.coe.CoEInfoPortlet;
import com.actionsoft.application.portal.portlet.coe.CoETeamPortlet;
import com.actionsoft.application.portal.portlet.flexcmschannel.FlexChannelPortlet;
import com.actionsoft.application.portal.portlet.mytask.MyTaskWeb;
import com.actionsoft.application.portal.portlet.navtree.MyFunctionNavTreePortlet;
import com.actionsoft.application.portal.portlet.quickcms.QuickCMSPortlet;
import com.actionsoft.application.portal.portlet.quicklink.MyDef55QuickLinkPortal;
import com.actionsoft.application.portal.portlet.quicklink.MyQuickLinkPortlet;
import com.actionsoft.application.portal.portlet.quicklink.MyQuickLinkPortlet2;
import com.actionsoft.application.portal.web.DesignPortalWeb;
import com.actionsoft.application.portal.web.DesktopPortalManagerWeb;
import com.actionsoft.application.portal.web.PortalManagerCardWeb;
import com.actionsoft.application.portal.web.PortalManagerWeb;
import com.actionsoft.application.portal.web.PortalResourceWeb;
import com.actionsoft.application.portal.web.PublisherPortalManager;
import com.actionsoft.application.portal.web.QuickDesktopPortalWeb;
import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.application.server.LICENSE;
import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.HttpDConf;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.CompanyModel;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.RoleModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilSession;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.i18n.I18nRes;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import com.actionsoft.application.portal.portlet.flexcmschannel.*;

public class PortalSocketCommand
  implements BaseSocketCommand
{
  public boolean executeCommand(UserContext me, Socket myProcessSocket, OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr, String socketCmd)
    throws Exception
  {
    if (socketCmd.equals("Portal_Design_List")) {
      String SystemId = myCmdArray.elementAt(3).toString();
      String userId = myCmdArray.elementAt(4).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.getPortalList(SystemId, userId));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_CreatePage")) {
      String SystemId = myCmdArray.elementAt(3).toString();
      String userId = myCmdArray.elementAt(4).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.getCreatePortletPage(SystemId, userId));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_Create")) {
      String SystemId = myCmdArray.elementAt(3).toString();
      String isInner = myCmdArray.elementAt(4).toString();
      String userId = myCmdArray.elementAt(5).toString();
      if (isInner.equals("1")) {
        String functionId = myCmdArray.elementAt(6).toString();
        String publicId = myCmdArray.elementAt(7).toString();
        String dirId = myCmdArray.elementAt(8).toString();
        if (functionId.equals("")) {
          functionId = "0";
        }
        if (publicId.equals("")) {
          publicId = "0";
        }
        if (dirId.equals("")) {
          dirId = "0";
        }
        DesignPortalWeb web = new DesignPortalWeb(me);
        myOut.write(web.savePortlet(SystemId, userId, Integer.parseInt(functionId), Integer.parseInt(publicId), Integer.parseInt(dirId)));
        web = null;
      } else {
        UserPortalModel model = new UserPortalModel();
        model.setInner(false);
        model.setSystemId(SystemId);
        model.setUserId(userId);

        model.setPortalUrl(UtilCode.decode(myStr.matchValue("_outerPortallet[", "]outerPortallet_")));
        if (model.getPortalUrl().indexOf("http://") == -1) {
          model.setPortalUrl("http://" + model.getPortalUrl());
        }
        DesignPortalWeb web = new DesignPortalWeb(me);
        myOut.write(web.savePortlet(model));
        web = null;
      }

    }
    else if (socketCmd.equals("Portal_Design_Modify")) {
      String portalId = myCmdArray.elementAt(3).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.getModifyPortletPage(Integer.parseInt(portalId)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_Save")) {
      String SystemId = myCmdArray.elementAt(3).toString();
      String portalTitle = myCmdArray.elementAt(4).toString();
      String portalHeight = myCmdArray.elementAt(5).toString();
      String isRoll = myCmdArray.elementAt(6).toString();
      String portalType = myCmdArray.elementAt(7).toString();
      String portalId = myCmdArray.elementAt(8).toString();
      UserPortalModel model = new UserPortalModel();
      model.setId(Integer.parseInt(portalId));
      model.setRoll(isRoll.equals("1"));
      model.setPortalHeight(portalHeight);
      model.setPortalTitle(portalTitle);
      model.setPortalType(portalType);
      model.setSystemId(SystemId);
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.savePortlet(model));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_Remove")) {
      String systemId = myCmdArray.elementAt(3).toString();
      String portalId = myCmdArray.elementAt(4).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.removePortallet(systemId, Integer.parseInt(portalId)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Runtime_Remove")) {
      String portalId = myCmdArray.elementAt(3).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.removeRuntimePortallet(Integer.parseInt(portalId)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_MoveUp")) {
      String systemId = myCmdArray.elementAt(3).toString();
      String portalId = myCmdArray.elementAt(4).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.upIndex(systemId, Integer.parseInt(portalId)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_MoveDown")) {
      String systemId = myCmdArray.elementAt(3).toString();
      String portalId = myCmdArray.elementAt(4).toString();
      DesignPortalWeb web = new DesignPortalWeb(me);
      myOut.write(web.downIndex(systemId, Integer.parseInt(portalId)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Execute_MessageWorkFlowTransaction")) {
      TaskWorklistPortlet web = new TaskWorklistPortlet(me);
      myOut.write(web.getPortletContext());
      web = null;
    }
    else if (socketCmd.equals("Portal_Execute_SystemOnline")) {
      OnlinePortlet web = new OnlinePortlet(me);
      myOut.write(web.getPortletContext());
      web = null;
    }
    else if (socketCmd.equals("Portal_Manager_Open")) {
      PortalManagerCardWeb web = new PortalManagerCardWeb(me);
      String pageType = myCmdArray.elementAt(3).toString();
      if ((pageType == null) || (pageType.equals(""))) {
        pageType = "0";
      }
      myOut.write(web.getPortalManagerPage(me.getUID(), Integer.parseInt(pageType)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Resource_Pub_Create_Open")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.addSysPortalWeb());
      web = null;
    }
    else if (socketCmd.equals("Portal_Resource_Pub_Add")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      String portalTitle = myCmdArray.elementAt(3).toString();
      String portalUrl = myCmdArray.elementAt(4).toString();
      String portalHeight = myCmdArray.elementAt(5).toString();
      String isRoll = myCmdArray.elementAt(6).toString();

      SysPortalModel model = new SysPortalModel();
      model._portalTitle = portalTitle;
      model._portalUrl = portalUrl;
      model._portalHeight = portalHeight;
      model._isRoll = (isRoll.equals("1"));
      model._portalWidth = "100%";
      model._isInner = true;
      model._systemId = "21";
      myOut.write(web.addSysPortal(model));
      web = null;
    }
    else if (socketCmd.equals("Portal_Resource_Pub_Del")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      String portalId = myCmdArray.elementAt(3).toString();
      myOut.write(web.delSysPortal(Integer.parseInt(portalId)));
      web = null;
    }
    else if (socketCmd.equals("Portal_Design_PubPage")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.getPubPortalList(""));
      web = null;
    }
    else if (socketCmd.equals("Portal_Resource_Pub_Create")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.getPubPortalList("createAction"));
      web = null;
    }
    else if (socketCmd.equals("Portal_Publisher_Create_Open")) {
      PublisherPortalManager web = new PublisherPortalManager(me);
      myOut.write(web.getPublisherWeb("0", "", ""));
      web = null;
    }
    else if (socketCmd.equals("Publisher_Portal_UserUsed_List")) {
      String functionId = myCmdArray.elementAt(3).toString();
      String publicId = myCmdArray.elementAt(4).toString();
      String dirId = myCmdArray.elementAt(5).toString();
      if (functionId.equals("")) {
        functionId = "0";
      }
      if (publicId.equals("")) {
        publicId = "0";
      }
      if (dirId.equals("")) {
        dirId = "0";
      }
      PublisherPortalManager web = new PublisherPortalManager(me, Integer.parseInt(functionId), Integer.parseInt(publicId), Integer.parseInt(dirId));
      myOut.write(web.getPublisherWeb("0", functionId, ""));
      web = null;
    }
    else if (socketCmd.equals("Publisher_Portal_Actiont"))
    {
      String functionId = myCmdArray.elementAt(3).toString();
      String uid = UtilCode.decode(myStr.matchValue("_addressText[", "]addressText_"));
      if (uid == null) {
        uid = "";
      }
      String pAction = myCmdArray.elementAt(4).toString();
      if ((pAction == null) || (pAction.equals(""))) {
        pAction = "0";
      }
      String publicId = myCmdArray.elementAt(5).toString();
      String dirId = myCmdArray.elementAt(6).toString();
      if (functionId.equals("")) {
        functionId = "0";
      }
      if (publicId.equals("")) {
        publicId = "0";
      }
      if (dirId.equals("")) {
        dirId = "0";
      }
      PublisherPortalManager web = new PublisherPortalManager(me, Integer.parseInt(functionId), Integer.parseInt(publicId), Integer.parseInt(dirId));
      myOut.write(web.getPublisherWeb(pAction, functionId, uid));
      web = null;
    }
    else if (socketCmd.equals("Portal_Publishe_Create"))
    {
      String functionId = myCmdArray.elementAt(3).toString();

      String pAction = myCmdArray.elementAt(4).toString();
      if ((pAction == null) || (pAction.equals(""))) {
        pAction = "0";
      }
      String innerSystem = myCmdArray.elementAt(5).toString();
      if ((innerSystem == null) || (innerSystem.equals(""))) {
        innerSystem = "21";
      }
      String portalPosition = myCmdArray.elementAt(6).toString();
      if ((portalPosition == null) || (portalPosition.equals(""))) {
        portalPosition = "澶?";
      }
      String isRoll = myCmdArray.elementAt(7).toString();
      if ((isRoll == null) || (isRoll.equals(""))) {
        isRoll = "0";
      }
      String publicId = myCmdArray.elementAt(8).toString();
      String dirId = myCmdArray.elementAt(9).toString();
      String higth = myCmdArray.elementAt(10).toString();
      String uidList = UtilCode.decode(myStr.matchValue("_addressText[", "]addressText_"));

      UserPortalModel model = new UserPortalModel();
      NavigationFunctionModel functionModel = (NavigationFunctionModel)NavigationFunctionCache.getModel(Integer.parseInt(functionId));

      model.setPortalWidth("100%");
      model.setPortalHeight(higth);

      model.setRoll(isRoll.equals("1"));
      model.setSystemId(innerSystem);
      model.setInner(true);
      StringBuffer userList = new StringBuffer();
      if (uidList.equals("*")) {
        Map h = UserCache.getList();
        for (int i = 0; i < h.size(); i++) {
          UserModel userModel = (UserModel)h.get(new Integer(i));
          if (!userModel.isDisabled()) {
            userList.append(userModel.getUID()).append(" ");
          }
        }

        model.setUserId(userList.toString().trim());
      } else {
        model.setUserId(uidList);
      }
      model.setPortalType(portalPosition);
      PublisherPortalManager web = new PublisherPortalManager(me, Integer.parseInt(functionId), Integer.parseInt(publicId), Integer.parseInt(dirId));
      myOut.write(web.createAction(model, pAction, functionId));
      web = null;
    }
    else if (socketCmd.equals("Portal_Desktop_List")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      String filterValue = myCmdArray.elementAt(3).toString();
      myOut.write(web.getSearchPage(filterValue));
      web = null;
    }
    else if (socketCmd.equals("Portal_New_Portal_Open")) {
      QuickDesktopPortalWeb web = new QuickDesktopPortalWeb(me);
      String systemId = myCmdArray.elementAt(3).toString();
      String url = UtilCode.decode(myStr.matchValue("_theURL[", "]theURL_"));
      String title = UtilCode.decode(myStr.matchValue("_theTitle[", "]theTitle_"));
      myOut.write(web.getQuickAddPortletDialog(systemId, url, title));
      web = null;
    }
    else if (socketCmd.equals("Portal_New_Portal_Save")) {
      String SystemId = myCmdArray.elementAt(3).toString();
      String url = UtilCode.decode(myStr.matchValue("_theURL[", "]theURL_"));
      String title = UtilCode.decode(myStr.matchValue("_portalTitle[", "]portalTitle_"));
      QuickDesktopPortalWeb web = new QuickDesktopPortalWeb(me);
      myOut.write(web.createNewPortlet(SystemId, title, url));
      web = null;
    }
    else if (socketCmd.equals("Portal_Resource_List")) {
      PortalResourceWeb web = new PortalResourceWeb(me);
      myOut.write(web.getListPage());
    }
    else if (socketCmd.equals("Portal_Resource_Json_Data")) {
      PortalResourceWeb web = new PortalResourceWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param1 = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      String json = "";
      if (requestType.equals("1"))
        json = web.getGroupJson();
      else if (requestType.equals("2"))
        json = web.getPortalJson(param1);
      else {
        json = "[]";
      }
      myOut.write(json);
    }
    else if (socketCmd.equals("Portal_Resource_List_XML_Data")) {
      PortalResourceWeb web = new PortalResourceWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String param1 = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      myOut.write(web.getPortalXMLData(requestType, param1));
    }
    else if (socketCmd.equals("Portal_Resource_Save")) {
      String savedata = UtilCode.decode(myStr.matchValue("_savedata[", "]savedata_"));
      PortalResourceWeb web = new PortalResourceWeb(me);
      myOut.write(web.savePortalData(savedata));
    }
    else if (socketCmd.equals("Portal_Resource_Remove")) {
      String resourcListId = UtilCode.decode(myStr.matchValue("_ResourcListId[", "]ResourcListId_"));
      PortalResourceWeb web = new PortalResourceWeb(me);
      myOut.write(web.removePortalData(resourcListId));
    }
    else if (socketCmd.equals("Portal_Out_List")) {
      PortalManagerWeb web = new PortalManagerWeb(me);
      myOut.write(web.getOutPage(null));
    }
    else if (socketCmd.equals("Portal_Out_Open")) {
      PortalManagerWeb web = new PortalManagerWeb(me);
      int resourceType = Integer.parseInt(myCmdArray.elementAt(3).toString());
      int resourceId = Integer.parseInt(myCmdArray.elementAt(4).toString());
      UserPortalModel model = ResourceManager.getUserResource(resourceType, resourceId);
      myOut.write(web.getOutPage(model));
    }
    else if (socketCmd.equals("Portal_Resource_Out")) {
      UserPortalModel model = new UserPortalModel();
      model.setId(Integer.parseInt(myCmdArray.elementAt(3).toString()));
      model.setPortalWidth(myCmdArray.elementAt(4).toString());
      model.setPortalType("POOL1");
      model.setPortalIndex(Integer.parseInt(myCmdArray.elementAt(6).toString()));
      model.setRoll(myCmdArray.elementAt(7).toString().equals("1"));
      model.setInner(myCmdArray.elementAt(8).toString().equals("1"));
      model.setSystemId(myCmdArray.elementAt(9).toString());
      model.setPortalTitle(UtilCode.decode(myStr.matchValue("_portalTitle[", "]portalTitle_")));
      model.setPortalHeight(UtilCode.decode(myStr.matchValue("_portalHeight[", "]portalHeight_")));
      model.setPortalUrl(UtilCode.decode(myStr.matchValue("_portalUrl[", "]portalUrl_")));
      String content = UtilCode.decode(myStr.matchValue("_userList[", "]userList_"));

      StringTokenizer userList = new StringTokenizer(content.replaceAll("__eol__", " ").replaceAll("__crt__", " "));
      StringBuffer message = new StringBuffer();
      message.append("'").append(model.getPortalTitle()).append("'").append("已经发布到用户：");
      while (userList.hasMoreTokens()) {
        String user = userList.nextToken();
        model.setUserId(user);
        model.setUserId(Function.getUID(model.getUserId()));
        if (PortalManager.putOutPortal(model)) {
          message.append(user).append("、");
        }
      }
      MessageQueue.getInstance().putMessage(me.getUID(), message.toString());
      PortalManagerWeb web = new PortalManagerWeb(me);
      myOut.write(web.getOutPage(null));
    }
    else if (socketCmd.equals("Portal_In_List")) {
      PortalManagerWeb web = new PortalManagerWeb(me);
      myOut.write(web.getInPage(null));
    }
    else if (socketCmd.equals("Portal_In_Open")) {
      PortalManagerWeb web = new PortalManagerWeb(me);
      int resourceType = Integer.parseInt(myCmdArray.elementAt(3).toString());
      int resourceId = Integer.parseInt(myCmdArray.elementAt(4).toString());
      UserPortalModel model = ResourceManager.getUserResource(resourceType, resourceId);
      myOut.write(web.getInPage(model));
    }
    else if (socketCmd.equals("Portal_Resource_In")) {
      String portalUrl = UtilCode.decode(myStr.matchValue("_portalUrl[", "]portalUrl_"));
      StringTokenizer userList = new StringTokenizer(UtilCode.decode(myStr.matchValue("_userList[", "]userList_")));

      while (userList.hasMoreTokens()) {
        PortalManager.takeBackPortal(userList.nextToken(), portalUrl);
      }
      PortalManagerWeb web = new PortalManagerWeb(me);
      myOut.write(web.getInPage(null));
    }
    else if (socketCmd.equals("Portal_User_SetLayoutWindow")) {
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.getSetMyLayoutDialog());
    }
    else if (socketCmd.equals("Portal_User_SetLayout")) {
      String layoutModel = myCmdArray.elementAt(3).toString();
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.setMyLayout(layoutModel));
    }
    else if (socketCmd.equals("Portal_User_SaveNewPosition")) {
      String newPosition = UtilCode.decode(myStr.matchValue("_newPosition[", "]newPosition_"));
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.saveNewPosition(newPosition));
    }
    else if (socketCmd.equals("Portal_User_AddContentWindow")) {
      String systemId = myCmdArray.elementAt(3).toString();
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.getAddContentDialog(systemId));
    }
    else if (socketCmd.equals("Portal_User_AddContent")) {
      String systemId = myCmdArray.elementAt(3).toString();
      String portletType = myCmdArray.elementAt(4).toString();
      String portletValue = myCmdArray.elementAt(5).toString();
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.addContent(systemId, portletType, portletValue));
    }
    else if (socketCmd.equals("Portal_User_ModifyContentDialog")) {
      String portalId = myCmdArray.elementAt(3).toString();
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.getModifyContentDialog(portalId));
    }
    else if (socketCmd.equals("Portal_User_ModifyContent")) {
      String portalId = myCmdArray.elementAt(3).toString();
      String portalTitle = UtilCode.decode(myStr.matchValue("_portalTitle[", "]portalTitle_"));
      String portalHeight = UtilCode.decode(myStr.matchValue("_portalHeight[", "]portalHeight_"));
      DesktopPortalManagerWeb web = new DesktopPortalManagerWeb(me);
      myOut.write(web.modifyContent(portalId, portalTitle, portalHeight));
    }
    else if (socketCmd.equals("AWSPortlet_MyFunctionNavTree_Index")) {
      MyFunctionNavTreePortlet web = new MyFunctionNavTreePortlet(me);
      String style = myCmdArray.elementAt(3).toString();
      String isRefresh = myCmdArray.elementAt(4).toString();
      if ((isRefresh == null) || (isRefresh.equals(""))) {
        isRefresh = "false";
      }
      if ((style == null) || (style.equals(""))) style = "";
      myOut.write(web.getIndex(style, isRefresh.equals("true")));
    }
    else if (socketCmd.equals("AWSPortlet_MyFunctionNavTree_IndexMenu")) {
      MyFunctionNavTreePortlet web = new MyFunctionNavTreePortlet(me);
      String style = myCmdArray.elementAt(3).toString();
      String isRefresh = myCmdArray.elementAt(4).toString();
      if ((isRefresh == null) || (isRefresh.equals(""))) {
        isRefresh = "false";
      }
      if ((style == null) || (style.equals(""))) style = "";

      myOut.write(web.getMenuNew(style, isRefresh.equals("true")));
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_Index")) {
      MyQuickLinkPortlet web = new MyQuickLinkPortlet(me);
      String cols = myCmdArray.elementAt(3).toString();
      String groupName = myCmdArray.elementAt(4).toString();
      String PORTLET_PARAM_SYSTEM_UUID = myCmdArray.elementAt(5).toString();
      if ((groupName == null) || (groupName.equals(""))) groupName = "未分类";
      if ((PORTLET_PARAM_SYSTEM_UUID != null) && (!PORTLET_PARAM_SYSTEM_UUID.equals(""))) groupName = PORTLET_PARAM_SYSTEM_UUID;
      if (cols.equals("")) cols = "4";
      myOut.write(web.getIndex(Integer.parseInt(cols), groupName));
    }
    else if (socketCmd.equals("com.action.skins.def55_listSortOrder")) {
      MyDef55QuickLinkPortal web = new MyDef55QuickLinkPortal(me);
      String listSortOrder = myCmdArray.elementAt(3).toString();
      myOut.write(web.listSortOrder(listSortOrder));
    }
    else if (socketCmd.equals("com.actionsoft.skins.def55_getQuickLink")) {
      MyDef55QuickLinkPortal web = new MyDef55QuickLinkPortal(me);
      myOut.write(web.getPortalButtom());
    }
    else if (socketCmd.equals("com.action.skins.def55_CreatePage")) {
      MyDef55QuickLinkPortal web = new MyDef55QuickLinkPortal(me);
      String ind = myCmdArray.elementAt(3).toString();
      String groupName = myCmdArray.elementAt(4).toString();
      if (groupName.trim().length() == 0) {
        groupName = "未分类";
      }
      if (ind.trim().length() == 0) {
        ind = "0";
      }
      myOut.write(web.getCreatePage(Integer.parseInt(ind), groupName));
    }
    else if (socketCmd.equals("com.action.skins.def55_Create")) {
      MyDef55QuickLinkPortal web = new MyDef55QuickLinkPortal(me);
      String ind = myCmdArray.elementAt(3).toString();
      String groupName = myCmdArray.elementAt(4).toString();
      if ((groupName == null) || (groupName.equals(""))) {
        groupName = "未分类";
      }
      if (ind.trim().length() == 0) {
        ind = "0";
      }
      String incUrl = UtilCode.decode(myStr.matchValue("_incUrl[", "]incUrl_"));
      String linkSource = UtilCode.decode(myStr.matchValue("_linkSource[", "]linkSource_"));
      String title = UtilCode.decode(myStr.matchValue("_linkTitle[", "]linkTitle_"));
      String createLink = "";
      if (title.equals("-"))
        createLink = web.createLinks(Integer.parseInt(ind), linkSource, title, groupName, incUrl);
      else {
        createLink = web.createLink(Integer.parseInt(ind), linkSource, title, groupName, incUrl);
      }
      myOut.write(createLink);
    }
    else if (socketCmd.equals("com.action.skins.def55_Remove")) {
      MyDef55QuickLinkPortal web = new MyDef55QuickLinkPortal(me);
      String ind = myCmdArray.elementAt(3).toString();
      String groupName = myCmdArray.elementAt(4).toString();
      if ((groupName == null) || (groupName.equals(""))) groupName = "未分类";
      myOut.write(web.removeLink(Integer.parseInt(ind), groupName));
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_CreatePage")) {
      MyQuickLinkPortlet web = new MyQuickLinkPortlet(me);
      String ind = myCmdArray.elementAt(3).toString();
      String cols = myCmdArray.elementAt(4).toString();
      String groupName = myCmdArray.elementAt(5).toString();
      if (cols.trim().length() == 0) {
        cols = "4";
      }
      if (ind.trim().length() == 0) {
        ind = "0";
      }
      if (groupName.trim().length() == 0) {
        groupName = "未分类";
      }
      myOut.write(web.getCreatePage(Integer.parseInt(ind), Integer.parseInt(cols), groupName));
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_Create")) {
      MyQuickLinkPortlet web = new MyQuickLinkPortlet(me);
      String ind = myCmdArray.elementAt(3).toString();
      String cols = myCmdArray.elementAt(4).toString();
      String groupName = myCmdArray.elementAt(5).toString();
      if ((groupName == null) || (groupName.equals(""))) groupName = "未分类";
      if (cols.equals("")) cols = "4";
      String linkSource = UtilCode.decode(myStr.matchValue("_linkSource[", "]linkSource_"));
      String title = UtilCode.decode(myStr.matchValue("_linkTitle[", "]linkTitle_"));
      myOut.write(web.createLink(Integer.parseInt(ind), Integer.parseInt(cols), linkSource, title, groupName));
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_Remove")) {
      MyQuickLinkPortlet web = new MyQuickLinkPortlet(me);
      String ind = myCmdArray.elementAt(3).toString();
      String cols = myCmdArray.elementAt(4).toString();
      String groupName = myCmdArray.elementAt(5).toString();
      if ((groupName == null) || (groupName.equals(""))) groupName = "未分类";
      if (cols.equals("")) cols = "4";
      myOut.write(web.removeLink(Integer.parseInt(ind), Integer.parseInt(cols), groupName));
    }
    else if (socketCmd.equals("Portal_Design_Style_Main")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      myOut.write(web.getMainPage());
    }
    else if (socketCmd.equals("Portal_Design_Style_Edit")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      String editType = myCmdArray.elementAt(3).toString();
      String editKey = UtilCode.decode(myStr.matchValue("_editKey[", "]editKey_"));
      myOut.write(web.getEditPage(editType, editKey));
    }
    else if (socketCmd.equals("Portal_Design_Style_LookAndFeel_RoleTree_JSon")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      String requestType = myCmdArray.elementAt(3).toString();
      String typeKey = myCmdArray.elementAt(4).toString();
      String param = UtilCode.decode(myStr.matchValue("_param1[", "]param1_"));
      myOut.write(web.getLookAndFeelEditRoleData(requestType, param, typeKey));
    }
    else if (socketCmd.equals("Portal_Design_Style_LookAndFeel_Save")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      String lookAndFeel = myCmdArray.elementAt(3).toString();
      String defCheck = myCmdArray.elementAt(4).toString();
      String roleList = UtilCode.decode(myStr.matchValue("_roleList[", "]roleList_"));
      String companyList = UtilCode.decode(myStr.matchValue("_companyList[", "]companyList_"));
      myOut.write(web.saveLookAndFeel(lookAndFeel.trim(), defCheck.trim().equals("1"), roleList.trim(), companyList.trim()));
    }
    else if (socketCmd.equals("Portal_Design_Style_NavSystem_Save")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      String isHidden = myCmdArray.elementAt(3).toString();
      String isExtendAdmin = myCmdArray.elementAt(4).toString();
      String navSystem = myCmdArray.elementAt(5).toString();
      String layoutId = UtilCode.decode(myStr.matchValue("_layoutId[", "]layoutId_"));
      myOut.write(web.saveNavSystem(navSystem.trim(), isHidden.trim().equals("1"), isExtendAdmin, layoutId.trim()));
    } else if (socketCmd.equals("Portal_Design_Style_Layout_Save")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      String isdefault = myCmdArray.elementAt(3).toString();
      String layoutModel = UtilCode.decode(myStr.matchValue("_layoutModel[", "]layoutModel_"));
      String layoutModelContent = UtilCode.decode(myStr.matchValue("_layoutModelContent[", "]layoutModelContent_"));
      myOut.write(web.saveLayout(layoutModel, layoutModelContent, isdefault.trim().equals("1")));
    }
    else if (socketCmd.equals("Portal_Design_Style_NavSystem_Extends_Xml_Data")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      String systemId = myCmdArray.elementAt(3).toString();
      myOut.write(web.getNavSystemExtendsXmlData(systemId));
    }
    else if (socketCmd.equals("Portal_Design_Style_NavSystem_Extends_GetId")) {
      StyleManagerWeb web = new StyleManagerWeb(me);
      myOut.write(web.getNavSystemExtendsGetId());
    }
    else if (socketCmd.equals("Portal_Design_Style_NavSystem_Extends_Save")) {
      String systemId = myCmdArray.elementAt(3).toString();
      String configValue = UtilCode.decode(myStr.matchValue("_subConfigValue[", "]subConfigValue_"));
      StyleManagerWeb web = new StyleManagerWeb(me);
      myOut.write(web.getNavSystemExtendsGetId());
    }
    else if (socketCmd.equals("Portal_Design_Style_NavSystem_Extends_Remove")) {
      String systemId = myCmdArray.elementAt(3).toString();
      String selectKey = UtilCode.decode(myStr.matchValue("_key[", "]key_"));
      StyleManagerWeb web = new StyleManagerWeb(me);
      myOut.write(web.removeNavSystemExtends(systemId, selectKey));
    }
    else if (socketCmd.equals("Portal_Execute_Portlet_MyTaskList")) {
      String top = myCmdArray.elementAt(3).toString();
      MyTaskWeb web = new MyTaskWeb(me);
      myOut.write(web.redirectTaskPage(top));
      web = null;
    }
    else if (socketCmd.equals("Portal_Execute_Portlet_MyTaskGetListData")) {
      String top = myCmdArray.elementAt(3).toString();
      MyTaskWeb web = new MyTaskWeb(me);
      myOut.write(web.getTaskListData(top));
      web = null;
    }
    else if (socketCmd.equals("Portal_Execute_Portlet_FlexCMSChannel")) {
      String tp = myCmdArray.elementAt(3).toString();
      if ((tp == null) || (tp.equals(""))) {
        tp = "8";
      }
      String top = tp;
      String channelTab = myCmdArray.elementAt(4).toString();
      String channelConf = myCmdArray.elementAt(5).toString();
      String isMorePage = myCmdArray.elementAt(6).toString();
      String start = myCmdArray.elementAt(7).toString();
      String isDepartment = myCmdArray.elementAt(8).toString();
      if ((start == null) || (start.equals(""))) {
        start = "1";
      }
      if ((isDepartment == null) || (isDepartment.equals(""))) {
        isDepartment = "0";
      }
      if ((isMorePage == null) || (isMorePage.equals(""))) {
        isMorePage = "0";
      }
      FlexChannelPortlet web = new FlexChannelPortlet(me);
      myOut.write(web.getMainPage(top, channelTab, channelConf, isMorePage, start, isDepartment));
      web = null;
    }
    else if (socketCmd.equals("Portal_Execute_Portlet_FlexCMSChannelGetListData")) {
      String tp = myCmdArray.elementAt(3).toString();
      if ((tp == null) || (tp.equals(""))) {
        tp = "8";
      }
      int top = Integer.parseInt(tp);
      String channelConf = myCmdArray.elementAt(4).toString();
      String sta = myCmdArray.elementAt(5).toString();
      String isDepartment = myCmdArray.elementAt(6).toString();
      String isMore = myCmdArray.elementAt(7).toString();
      if ((isDepartment == null) || (isDepartment.equals(""))) {
        isDepartment = "0";
      }
      if ((sta == null) || (sta.equals(""))) {
        sta = "1";
      }
      if ((isMore == null) || (isMore.equals(""))) {
        isMore = "0";
      }

      int start = Integer.parseInt(sta);
      FlexChannelPortlet web = new FlexChannelPortlet(me);
      myOut.write(web.getListJsonData(top, channelConf, start, isDepartment, isMore));
    }
    /**
     * 二级页面##############################################################/*
     */
    else if (socketCmd.equals("TwoPages")) {
        String tp = myCmdArray.elementAt(3).toString();
        if ((tp == null) || (tp.equals(""))) {
        }
        String top = tp;
        String channelTab = myCmdArray.elementAt(4).toString();
        String channelConf = myCmdArray.elementAt(5).toString();
        String isMorePageTitle = "1";
        String start = myCmdArray.elementAt(7).toString();
        String isDepartment = myCmdArray.elementAt(8).toString();
        if ((start == null) || (start.equals(""))) {
          start = "1";
        }
        TwoPages web = new TwoPages(me);
        myOut.write(web.getMainPage(top, channelTab, channelConf, isMorePageTitle, start, isDepartment));
      }
    /**
     * ################################################结束##############################################
     */
    else if (socketCmd.equals("Portal_Execute_Portlet_FlexCMSChannelMore")) {
      String tp = myCmdArray.elementAt(3).toString();
      if ((tp == null) || (tp.equals(""))) {
      }
      String top = tp;
      String channelTab = myCmdArray.elementAt(4).toString();
      String channelConf = myCmdArray.elementAt(5).toString();
      String isMorePageTitle = "1";
      String start = myCmdArray.elementAt(7).toString();
      String isDepartment = myCmdArray.elementAt(8).toString();
      if ((start == null) || (start.equals(""))) {
        start = "1";
      }
      FlexChannelPortlet web = new FlexChannelPortlet(me);
      myOut.write(web.getMainPage(top, channelTab, channelConf, isMorePageTitle, start, isDepartment));
    }
    else if (socketCmd.equals("Portal_Execute_Portlet_QuickCMS_Page")) {
      QuickCMSPortlet web = new QuickCMSPortlet(me);
      myOut.write(web.getChannels());
    }
    else if (socketCmd.equals("Portal_Execute_Portlet_QuickCMS_Create")) {
      QuickCMSPortlet web = new QuickCMSPortlet(me);
      String channelId = myCmdArray.elementAt(3).toString();
      String title = UtilCode.decode(myStr.matchValue("_title2[", "]title2_"));
      myOut.write(web.quickCMSCreate(Integer.parseInt(channelId), title));
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_CreatePage_V2")) {
      MyQuickLinkPortlet2 web = new MyQuickLinkPortlet2(me);
      String groupName = myCmdArray.elementAt(3).toString();
      if (groupName.trim().length() == 0) {
        groupName = "未分类";
      }
      myOut.write(web.getCreatePage(groupName));
      web = null;
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_Create_V2")) {
      MyQuickLinkPortlet2 web = new MyQuickLinkPortlet2(me);
      String groupName = myCmdArray.elementAt(3).toString();
      if ((groupName == null) || (groupName.equals(""))) groupName = "未分类";
      String linkSource = UtilCode.decode(myStr.matchValue("_linkSource[", "]linkSource_"));
      String webTitle = UtilCode.decode(myStr.matchValue("_linkTitle[", "]linkTitle_"));
      String webUrl = UtilCode.decode(myStr.matchValue("_linkUrl[", "]linkUrl_"));
      myOut.write(web.createLink(linkSource, webTitle, webUrl, groupName));
      web = null;
    }
    else if (socketCmd.equals("AWSPortlet_MyQuickLink_Remove_V2")) {
      MyQuickLinkPortlet2 web = new MyQuickLinkPortlet2(me);
      String groupName = myCmdArray.elementAt(3).toString();
      String linkId = myCmdArray.elementAt(4).toString();
      if ((groupName == null) || (groupName.equals(""))) groupName = "未分类";
      myOut.write(web.removeLink(groupName, linkId));
      web = null;
    }
    else if (socketCmd.equals("com.actionsoft.skins.def55_ToDef51Page")) {
      Hashtable hashTags = new Hashtable();
      String sid = "<input type=hidden name=sid id=sid value=" + me.getSessionId() + ">";
      CompanyModel companyModel = me.getCompanyModel();
      DepartmentModel departmentModel = me.getDepartmentModel();
      UserModel userModel = me.getUserModel();
      hashTags.put("pageCmd", myCmdArray.elementAt(3).toString());
      hashTags.put("pageName", myCmdArray.elementAt(4).toString());
      hashTags.put("sid", sid);

      hashTags.put("uid", me.getUID());
      hashTags.put("isKMSearch", LICENSE.getSuiteSecurity().indexOf("Document_Enterprise") > -1 ? "" : "none");
      hashTags.put("companyId", Integer.toString(companyModel.getId()));
      hashTags.put("email", userModel.getEmail() != null ? userModel.getEmail() : "");
      hashTags.put("companyNo", companyModel.getCompanyNo());
      hashTags.put("companyName", companyModel.getCompanyName());
      hashTags.put("departmentNo", departmentModel.getDepartmentNo());
      hashTags.put("departmentName", departmentModel.getDepartmentName());
      hashTags.put("userName", userModel.getUserName());
      hashTags.put("userNo", userModel.getUserNo() == null ? "" : userModel.getUserNo());
      hashTags.put("clientIP", new UtilSession(me.getSessionId()).getIP());
      hashTags.put("departmentId", Integer.toString(departmentModel.getId()));
      hashTags.put("roleName", me.getRoleModel().getRoleName());
      Calendar now = Calendar.getInstance();
      String currentDate = now.get(1) + "-" + (now.get(2) + 1) + "-" + now.get(5);
      hashTags.put("currentDate", currentDate);

      myOut.write(RepleaseKey.replace(HtmlModelFactory.getFile(AWFConfig._httpdConf.getServerRoot() + "/aws_skins/" + me.getLookAndFeelType() + "/model/Def51_Main_Frame_Top.htm"), hashTags));
    }
    else if (socketCmd.equals("com.actionsoft.skins.def55_getNewInfo")) {
      myOut.write(new RuntimeNavigation(me, "21").getTopMenuBar());
    }
    else if (socketCmd.equals("Portal_CoE_Info_Flow")) {
      CoEInfoPortlet web = new CoEInfoPortlet(me);
      myOut.write(web.getPortletContext());
      web = null;
    }
    else if (socketCmd.equals("Portal_CoE_Team_Info")) {
      CoETeamPortlet web = new CoETeamPortlet(me);
      myOut.write(web.getPortletContext());
      web = null;
    }
    else if (socketCmd.equals("System_I18n_Support")) {
      myOut.write(I18nRes.findValue(me.getLanguage(), myCmdArray.elementAt(3).toString()));
    }
    else {
      return false;
    }
    return true;
  }
}