package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.eip.cmcenter.cache.CmChannelCache;
import com.actionsoft.eip.cmcenter.dao.CmDaoFactory;
import com.actionsoft.eip.cmcenter.model.CmChannelModel;
import com.actionsoft.eip.cmcenter.model.CmContentModel;
import com.actionsoft.eip.cmcenter.model.CmContentReadModel;
import com.actionsoft.eip.cmcenter.model.CmSchemaModel;
import com.actionsoft.eip.cmcenter.portlet.ChannelViewPortlet;
import com.actionsoft.eip.cmcenter.web.ChannelDesignWeb;
import com.actionsoft.eip.cmcenter.web.ChannelItemListWeb;
import com.actionsoft.eip.cmcenter.web.ChannelReleaseWeb;
import com.actionsoft.eip.cmcenter.web.ChannelSuperManagerList;
import com.actionsoft.eip.cmcenter.web.ContentDesignWeb;
import com.actionsoft.eip.cmcenter.web.ContentReadPageWeb;
import com.actionsoft.eip.cmcenter.web.ContentReleaseSearchWeb;
import com.actionsoft.eip.cmcenter.web.SchemaDesignWeb;

public class CmSocketCommand implements BaseSocketCommand {
    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("CmSchema_List_Open")) {
	    myOut.write(new SchemaDesignWeb(me).getSchemaListWeb());
	} else if (socketCmd.equals("CmContent_Release_Open")) {
	    ChannelReleaseWeb web = new ChannelReleaseWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    String channelId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getChannelReleasePage(Integer.parseInt(schemaId),
		    Integer.parseInt(channelId)));
	    web = null;
	} else if (socketCmd.equals("CmSchema_Tree_Open")) {
	    myOut.write(new SchemaDesignWeb(me).getTreeWeb());
	} else if (socketCmd.equals("CmSchema_List_GridJsonData")) {
	    myOut.write(new SchemaDesignWeb(me).getSchemaGridJson());
	} else if (socketCmd.equals("CmSchema_Create_Open")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    myOut.write(web.getSchemaCreateWeb(0));
	    web = null;
	} else if (socketCmd.equals("CmSchema_Change_State")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String state = myCmdArray.elementAt(4).toString();
	    if (id.trim().length() == 0) {
		id = "0";
	    }
	    if (state.trim().length() == 0) {
		state = "0";
	    }
	    myOut.write(new SchemaDesignWeb(me).changeSchemaState(
		    Integer.parseInt(id), Integer.parseInt(state)));
	} else if (socketCmd.equals("CmSchema_Create_Save")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String schemaStyleId = myCmdArray.elementAt(4).toString();
	    String modelName = myCmdArray.elementAt(5).toString();
	    String isClose = myCmdArray.elementAt(6).toString();
	    String topN = myCmdArray.elementAt(7).toString();
	    String isShowTopN = myCmdArray.elementAt(8).toString();
	    String isShowPicPortlet = myCmdArray.elementAt(9).toString();
	    String schemaManager = UtilCode.decode(myStr.matchValue(
		    "_schemaManager[", "]schemaManager_"));
	    String channelList = UtilCode.decode(myStr.matchValue(
		    "_channelList[", "]channelList_"));
	    String schemaName = UtilCode.decode(myStr.matchValue(
		    "_schemaName[", "]schemaName_"));
	    String schemaDesc = UtilCode.decode(myStr.matchValue(
		    "_schemaDesc[", "]schemaDesc_"));
	    String channelBarCSS = UtilCode.decode(myStr.matchValue(
		    "_channelBarCSS[", "]channelBarCSS_"));
	    String dtf = UtilCode.decode(myStr.matchValue("_dtf[", "]dtf_"));
	    if (isShowPicPortlet.trim().length() == 0) {
		isShowPicPortlet = "0";
	    }
	    if (isClose.trim().length() == 0) {
		isClose = "0";
	    }
	    if (isShowTopN.trim().length() == 0) {
		isShowTopN = "0";
	    }
	    if (topN.trim().length() == 0) {
		topN = "10";
	    }
	    if (schemaStyleId.trim().length() == 0) {
		schemaStyleId = "  ";
	    }
	    CmSchemaModel model = new CmSchemaModel();
	    model._id = Integer.parseInt(id);
	    model._isClose = (Integer.parseInt(isClose) == 1);
	    model._modelName = (modelName.equals("") ? "model" : modelName);
	    model._schemaDesc = schemaDesc;
	    model._schemaManager = schemaManager;
	    model._schemaName = schemaName;
	    model._schemaStyleId = schemaStyleId;
	    model._isShowPicPortlet = (Integer.parseInt(isShowPicPortlet) == 1);
	    model._isShowTopN = (Integer.parseInt(isShowTopN) == 1);
	    model._topNum = Integer.parseInt(topN);
	    model._channelBarCSS = channelBarCSS;
	    model._dtf = dtf;
	    myOut.write(web.save(model, channelList));
	    web = null;
	} else if (socketCmd.equals("CmSchema_Create_Modify")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getSchemaCreateWeb(Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("CmSchema_Create_Del")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    String id = UtilCode.decode(myStr.matchValue("_id[", "]id_"));
	    myOut.write(web.remove(id));
	    web = null;
	} else if (socketCmd.equals("CmSchema_Create_Deployment")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getSchemaDeploymentWeb(Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("CmSchema_Create_Back")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    myOut.write(web.getSchemaListWeb());
	    web = null;
	} else if (socketCmd.equals("CmChannel_List_Open")) {
	    String id = myCmdArray.elementAt(3).toString();
	    if (id.trim().length() == 0) {
		id = "0";
	    }
	    myOut.write(new ChannelSuperManagerList(me)
		    .getSuperManagerLists(Integer.parseInt(id)));
	} else if (socketCmd.equals("CmChannel_List_GridJsonData")) {
	    String schemaId = myCmdArray.elementAt(3).toString();
	    myOut.write(new ChannelSuperManagerList(me)
		    .ChannelManagerGridJson(Integer.parseInt(schemaId)));
	} else if (socketCmd.equals("CmChannel_Create_Open")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getChannelCreateWeb(Integer.parseInt(schemaId), 0));
	    web = null;
	} else if (socketCmd.equals("CmChannel_Create_Save")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String channelStyleId = myCmdArray.elementAt(4).toString();

	    String isClose = myCmdArray.elementAt(5).toString();
	    String schemaId = myCmdArray.elementAt(6).toString();

	    String isMainIndex = "0";
	    String schemaIdList = UtilCode.decode(myStr.matchValue(
		    "_schemaIdList[", "]schemaIdList_"));
	    String channelManager = UtilCode.decode(myStr.matchValue(
		    "_channelManager[", "]channelManager_"));
	    String channelName = UtilCode.decode(myStr.matchValue(
		    "_channelName[", "]channelName_"));
	    String channelDesc = UtilCode.decode(myStr.matchValue(
		    "_channelDesc[", "]channelDesc_"));
	    CmChannelModel model = new CmChannelModel();
	    model._id = Integer.parseInt(id);
	    model._isClose = (Integer.parseInt(isClose) == 1);
	    model._channelDesc = channelDesc;
	    model._channelManager = channelManager;
	    model._channelName = channelName;
	    model._channelStyleId = channelStyleId;
	    myOut.write(web.save(Integer.parseInt(schemaId),
		    Integer.parseInt(isMainIndex), model, schemaIdList));
	    web = null;
	} else if (socketCmd.equals("CmChannel_Create_Del")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_id[", "]id_"));
	    myOut.write(web.remove(Integer.parseInt(schemaId), idList));
	    web = null;
	} else if (socketCmd.equals("CmChannel_Create_Modify")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String schemaId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getChannelCreateWeb(Integer.parseInt(schemaId),
		    Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("CmChannel_Create_Back")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getChannelListWeb(Integer.parseInt(schemaId)));
	    web = null;
	} else if (socketCmd.equals("CmContent_List_Open")) {
	    ContentDesignWeb web = new ContentDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String schemaId = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    if (pageNow.equals("")) {
		pageNow = "1";
	    }
	    myOut.write(web.getContentListWeb(Integer.parseInt(id),
		    Integer.parseInt(schemaId), Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd.equals("CmContent_Page_Open")) {
	    ContentDesignWeb web = new ContentDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String schemaId = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    if (pageNow.equals("")) {
		pageNow = "1";
	    }
	    myOut.write(web.getContentListWeb(Integer.parseInt(id),
		    Integer.parseInt(schemaId), Integer.parseInt(pageNow)));
	    web = null;
	} else if (socketCmd.equals("CmContent_WorkFlow_Execute_Open")) {
	    ContentDesignWeb web = new ContentDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String openState = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getMessagePage(Integer.parseInt(id),
		    Integer.parseInt(openState), Integer.parseInt("0"),
		    Integer.parseInt("0")));
	    web = null;
	} else if (socketCmd.equals("CmChannel_Release_Open")) {
	    ChannelReleaseWeb web = new ChannelReleaseWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    if (schemaId.trim().length() == 0) {
		schemaId = "-1";
	    }
	    myOut.write(web.getChannelReleasePage(Integer.parseInt(schemaId), 0));
	    web = null;
	} else if (socketCmd.equals("CmContent_Preview")) {
	    ContentReadPageWeb web = new ContentReadPageWeb(me);
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String taskId = myCmdArray.elementAt(4).toString();
	    myOut.write(web.geContentPreview(Integer.parseInt(instanceId),
		    Integer.parseInt(taskId)));
	    web = null;
	} else if (socketCmd.equals("CmContent_Read_Open")) {
	    ContentReadPageWeb web = new ContentReadPageWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    if (schemaId.trim().length() == 0) {
		schemaId = "-1";
	    }
	    String channelId = myCmdArray.elementAt(4).toString();
	    String contentId = myCmdArray.elementAt(5).toString();
	    String readUser = myCmdArray.elementAt(6).toString();
	    String isPreview = myCmdArray.elementAt(8).toString();
	    if (isPreview.trim().length() == 0) {
		isPreview = "0";
	    }
	    String pageNow = myCmdArray.elementAt(7).toString();
	    if (pageNow.equals("") || pageNow.equals("0")) {
		pageNow = "1";
	    }
	    CmContentReadModel crModel = new CmContentReadModel();
	    crModel._contentId = Integer.parseInt(contentId);
	    crModel._readUser = readUser;
	    int readId = 0;
	    if (Integer.parseInt(isPreview) == 0) {
		readId = CmDaoFactory.createContentRead().create(crModel);
	    }
	    myOut.write(web.readContent(Integer.parseInt(schemaId),
		    Integer.parseInt(channelId), Integer.parseInt(contentId),
		    readId, Integer.parseInt(pageNow),
		    Integer.parseInt(isPreview)));
	    web = null;
	} else if (socketCmd.equals("CmContent_Create_Del")) {
	    ContentDesignWeb web = new ContentDesignWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    if ("".equals(schemaId)) {
		schemaId = "0";
	    }
	    String channelId = myCmdArray.elementAt(4).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_id[", "]id_"));
	    myOut.write(web.remove(idList));
	    web = null;
	} else if (socketCmd.equals("CmContent_Talk_Save")) {
	    ContentReadPageWeb web = new ContentReadPageWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String schemaId = myCmdArray.elementAt(4).toString();
	    String channelId = myCmdArray.elementAt(5).toString();
	    String contentId = myCmdArray.elementAt(6).toString();
	    String talkName = UtilCode.decode(myStr.matchValue("_talkName[",
		    "]talkName_"));
	    String talkContent = UtilCode.decode(myStr.matchValue(
		    "_talkContent[", "]talkContent_"));
	    int readId = DBSql.getInt(
		    "select ID from eip_cmcontentread where id=" + id, "ID");
	    if (readId == 0) {
		CmContentReadModel model = new CmContentReadModel();
		model._contentId = Integer.parseInt(contentId);
		model._readUser = me.getUID();
		model._ipaddress = me.getIP();
		model._talkContent = talkContent;
		readId = CmDaoFactory.createContentRead().create(model);
	    }
	    CmContentReadModel model = CmDaoFactory.createContentRead()
		    .getInstance(readId);
	    model._talkName = talkName;
	    model._talkContent = talkContent;
	    model._ipaddress = me.getIP();
	    myOut.write(web.saveTalk(Integer.parseInt(schemaId),
		    Integer.parseInt(channelId), Integer.parseInt(contentId),
		    model));
	    web = null;
	} else if (socketCmd.equals("CmContent_Talk_Del")) {
	    ContentReadPageWeb web = new ContentReadPageWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    String channelId = myCmdArray.elementAt(4).toString();
	    String contentId = myCmdArray.elementAt(5).toString();
	    String id = myCmdArray.elementAt(6).toString();
	    String del_id = myCmdArray.elementAt(7).toString();
	    myOut.write(web.delTalk(schemaId, channelId, contentId, id, del_id));
	    web = null;
	} else if (socketCmd.equals("CmSchema_List_ArrorUp")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    myOut.write(web.upIndex(Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("CmSchema_List_ArrorDown")) {
	    SchemaDesignWeb web = new SchemaDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    myOut.write(web.downIndex(Integer.parseInt(id)));
	    web = null;
	} else if (socketCmd.equals("CmContent_Change_Type")) {
	    ContentDesignWeb web = new ContentDesignWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    String channelId = myCmdArray.elementAt(4).toString();
	    String type = myCmdArray.elementAt(5).toString();
	    String action = myCmdArray.elementAt(6).toString();
	    String id = myCmdArray.elementAt(7).toString();
	    myOut.write(web.changeType(Integer.parseInt(id),
		    Integer.parseInt(channelId), Integer.parseInt(schemaId),
		    type, action));
	    web = null;
	} else if (socketCmd.equals("CmContent_Open_eForm")) {
	    String instanceId = myCmdArray.elementAt(3).toString();
	    String formId = myCmdArray.elementAt(4).toString();
	    myOut.write(new RuntimeFormManager(me,
		    Integer.parseInt(instanceId), 0, 7, Integer
			    .parseInt(formId)).getFormPage(1));
	} else if (socketCmd.equals("CmContent_Search_Open")) {
	    ContentReleaseSearchWeb web = new ContentReleaseSearchWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    if (schemaId.trim().length() == 0) {
		schemaId = "-1";
	    }
	    CmContentModel model = new CmContentModel();
	    web = null;
	} else if (socketCmd.equals("CmContent_Search_Action")) {
	    ContentReleaseSearchWeb web = new ContentReleaseSearchWeb(me);
	    String schemaId = myCmdArray.elementAt(3).toString();
	    String releaseManS = myCmdArray.elementAt(4).toString();
	    String releaseDepartmentS = myCmdArray.elementAt(5).toString();
	    String releaseDateS = myCmdArray.elementAt(6).toString();
	    String isCloseS = myCmdArray.elementAt(7).toString();
	    String isTalkS = myCmdArray.elementAt(8).toString();
	    String titleS = UtilCode.decode(myStr.matchValue("_titleS[",
		    "]titleS_"));
	    String subTitleS = UtilCode.decode(myStr.matchValue("_subTitleS[",
		    "]subTitleS_"));
	    String contentS = UtilCode.decode(myStr.matchValue("_contentS[",
		    "]contentS_"));
	    CmContentModel model = new CmContentModel();
	    model._releaseMan = releaseManS;
	    model._releaseDepartment = releaseDepartmentS;
	    if (releaseDateS.trim().length() > 0) {
		model._releaseDate = Timestamp.valueOf(releaseDateS
			+ " 00:00:00");
	    }
	    if (schemaId.trim().length() == 0) {
		schemaId = "-1";
	    }
	    model._isClose = isCloseS;
	    model._isTalk = isTalkS;
	    model._title = titleS;
	    model._subTitle = subTitleS;
	    model._content = contentS;
	    web = null;
	} else if (socketCmd.equals("CmChannel_Release_Portlet")) {
	    String channelId = myCmdArray.elementAt(3).toString();
	    String schemaId = myCmdArray.elementAt(4).toString();
	    if (channelId.trim().length() == 0) {
		channelId = "0";
	    }
	    if (schemaId.trim().length() == 0) {
		schemaId = "-1";
	    }
	    myOut.write(new ChannelViewPortlet(me).getChannelView(
		    Integer.parseInt(channelId), Integer.parseInt(schemaId)));
	} else if (socketCmd.equals("CmChannel_Release_PicPortlet")) {
	    String schemaId = myCmdArray.elementAt(3).toString();
	    if (schemaId.trim().length() == 0) {
		schemaId = "-1";
	    }
	    myOut.write(new ChannelViewPortlet(me).getChannelPicView(Integer
		    .parseInt(schemaId)));
	} else if (socketCmd.equals("CmChannel_Info_Modify")) {
	    String id = myCmdArray.elementAt(3).toString();
	    String flag = myCmdArray.elementAt(4).toString();
	    myOut.write(new ChannelSuperManagerList(me)
		    .getChannelInfoModifyWeb(Integer.parseInt(id), flag, -1));
	} else if (socketCmd.equals("CmChannel_Info_Save")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String id = myCmdArray.elementAt(3).toString();
	    String isClose = myCmdArray.elementAt(4).toString();
	    String isMainIndex = "0";
	    String channelManager = UtilCode.decode(myStr.matchValue(
		    "_channelManager[", "]channelManager_"));
	    String channelName = UtilCode.decode(myStr.matchValue(
		    "_channelName[", "]channelName_"));
	    String channelDesc = UtilCode.decode(myStr.matchValue(
		    "_channelDesc[", "]channelDesc_"));
	    String addPopeDom = UtilCode.decode(myStr.matchValue(
		    "_addPopeDom[", "]addPopeDom_"));
	    String channelStyleId = UtilCode.decode(myStr.matchValue(
		    "_channelStyleId[", "]channelStyleId_"));
	    CmChannelModel model = new CmChannelModel();
	    model._id = Integer.parseInt(id);
	    model._isClose = (Integer.parseInt(isClose) == 1);
	    model._channelDesc = channelDesc;
	    model._channelManager = channelManager;
	    model._channelName = channelName;
	    model._channelStyleId = channelStyleId;
	    model._addpopedom = addPopeDom;
	    myOut.write(web.save(model));
	} else if (socketCmd.equals("CmChannel_Info_BackList")) {
	    ChannelDesignWeb web = new ChannelDesignWeb(me);
	    String channelId = myCmdArray.elementAt(3).toString();
	    if (channelId.trim().length() == 0) {
		channelId = "0";
	    }
	    myOut.write(new SchemaDesignWeb(me).getSuperManagerList(Integer
		    .parseInt(channelId)));
	    web = null;
	} else if (socketCmd.equals("CmContentJson")) {
	    myOut.write(new SchemaDesignWeb(me).getSchemasJson());
	} else if (!socketCmd.equals("CmContentList")) {
	    if (socketCmd.equals("CmContent_List_Open2")) {
		ContentDesignWeb web = new ContentDesignWeb(me);
		String id = myCmdArray.elementAt(3).toString();
		String pageNow = myCmdArray.elementAt(4).toString();
		if (pageNow.equals("")) {
		    pageNow = "1";
		}
		myOut.write(web.getContentListWeb(Integer.parseInt(id),
			Integer.parseInt(pageNow)));
		web = null;
	    } else if (socketCmd.equals("ContentManager_GridJson")) {
		String channelId = myCmdArray.elementAt(3).toString();
		String pageNow = myCmdArray.elementAt(4).toString();
		if (pageNow.equals("")) {
		    pageNow = "1";
		}
		ContentDesignWeb web = new ContentDesignWeb(me);
		myOut.write(web.ContentManagerGridJson(
			Integer.parseInt(channelId), Integer.parseInt(pageNow)));
		web = null;
	    } else if (socketCmd.equals("CmContent_Change_Type2")) {
		ContentDesignWeb web = new ContentDesignWeb(me);
		String channelId = myCmdArray.elementAt(3).toString();
		String type = myCmdArray.elementAt(4).toString();
		String action = myCmdArray.elementAt(5).toString();
		String id = myCmdArray.elementAt(6).toString();
		myOut.write(web.changeType(Integer.parseInt(id),
			Integer.parseInt(channelId), type, action));
		web = null;
	    } else if (socketCmd.equals("CmChannel_Create_Open2")) {
		ChannelDesignWeb web = new ChannelDesignWeb(me);
		String flag = myCmdArray.elementAt(3).toString();
		String schemaId = myCmdArray.elementAt(4).toString();
		if (schemaId.trim().length() == 0) {
		    schemaId = "0";
		}
		myOut.write(new ChannelSuperManagerList(me)
			.getChannelInfoModifyWeb(0, flag,
				Integer.parseInt(schemaId)));
		web = null;
	    } else if (socketCmd.equals("CmChannel_Create_Del2")) {
		ChannelDesignWeb web = new ChannelDesignWeb(me);
		String idList = UtilCode.decode(myStr
			.matchValue("_id[", "]id_"));
		myOut.write(web.remove(idList));
		web = null;
	    } else if (socketCmd.equals("CmChannel_Change_Type2")) {
		String id = myCmdArray.elementAt(3).toString();
		ChannelDesignWeb web = new ChannelDesignWeb(me);
		CmChannelModel model = (CmChannelModel) CmChannelCache
			.getModel(Integer.parseInt(id));
		web.changeType(model);
		myOut.write(new ChannelSuperManagerList(me)
			.getSuperManagerLists(0));
	    } else if (socketCmd.equals("CmContent_Create_Del2")) {
		ContentDesignWeb web = new ContentDesignWeb(me);
		String channelId = myCmdArray.elementAt(4).toString();
		String idList = UtilCode.decode(myStr
			.matchValue("_id[", "]id_"));
		myOut.write(web.remove(Integer.parseInt(channelId), idList));
		web = null;
	    } else if (socketCmd.equals("CmChannel_Item_List_Open")) {
		ChannelItemListWeb web = new ChannelItemListWeb(me);
		String channelId = myCmdArray.elementAt(3).toString();
		String schemaId = myCmdArray.elementAt(4).toString();
		String pageNow = myCmdArray.elementAt(5).toString();
		if (pageNow.equals("") || pageNow == null) {
		    pageNow = "1";
		}
		myOut.write(web.getChannelItemListPage(
			Integer.parseInt(channelId), schemaId,
			Integer.parseInt(pageNow)));
		web = null;
	    } else if (socketCmd.equals("CmContent_FullSearch")) {
		ContentReleaseSearchWeb web = new ContentReleaseSearchWeb(me);
		String pageNow = myCmdArray.elementAt(3).toString();
		String schemaId = myCmdArray.elementAt(4).toString();
		String channelId = myCmdArray.elementAt(5).toString();
		if (pageNow.equals("") || pageNow == null) {
		    pageNow = "1";
		}
		if (schemaId.trim().length() == 0) {
		    schemaId = "-1";
		}
		String searchKey = UtilCode.decode(myStr.matchValue(
			"_searchKey[", "]searchKey_"));
		String searchCondition = UtilCode.decode(myStr.matchValue(
			"_searchCondition[", "]searchCondition_"));
		myOut.write(web.getQuickSearchWeb(searchKey,
			Integer.parseInt(pageNow), Integer.parseInt(schemaId),
			channelId, searchCondition));
		web = null;
	    } else if (socketCmd.equals("CmChannel_Manager_SaveNewPosition")) {
		ChannelReleaseWeb web = new ChannelReleaseWeb(me);
		String newPosition = UtilCode.decode(myStr.matchValue(
			"_newPosition[", "]newPosition_"));
		myOut.write(web.saveNewPosition(newPosition));
		web = null;
	    } else if (socketCmd.equals("CmChannel_Change_Sort")) {
		ChannelReleaseWeb web = new ChannelReleaseWeb(me);
		String storeId = myCmdArray.elementAt(3).toString();
		String changeId = myCmdArray.elementAt(4).toString();
		myOut.write(web.saveStore(Integer.parseInt(storeId),
			Integer.parseInt(changeId)));
		web = null;
	    } else {
		return false;
	    }
	}
	return true;
    }
}