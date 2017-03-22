package com.actionsoft.application.server.socketcommand;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.report.design.cache.DiggerCache;
import com.actionsoft.awf.report.design.model.DiggerModel;
import com.actionsoft.awf.report.design.util.DiggerUtil;
import com.actionsoft.awf.report.design.web.DiggerBaseDataTabWeb;
import com.actionsoft.awf.report.design.web.DiggerCardWeb;
import com.actionsoft.awf.report.design.web.DiggerChartConfTabWeb;
import com.actionsoft.awf.report.design.web.DiggerConditionTabWeb;
import com.actionsoft.awf.report.design.web.DiggerCrossColTabWeb;
import com.actionsoft.awf.report.design.web.DiggerGroupByTabWeb;
import com.actionsoft.awf.report.design.web.DiggerQueryTabWeb;
import com.actionsoft.awf.report.design.web.DiggerSQLTabWeb;
import com.actionsoft.awf.report.design.web.DiggerSecurityTabWeb;
import com.actionsoft.awf.report.design.web.DiggerSortTabWeb;
import com.actionsoft.awf.report.design.web.DiggerStatTabWeb;
import com.actionsoft.awf.report.design.web.DiggerSumFieldTabWeb;
import com.actionsoft.awf.report.design.web.DiggerTitleTabWeb;
import com.actionsoft.awf.report.design.web.DiggerURLTabWeb;
import com.actionsoft.awf.report.design.web.DiggerViewSQLTabWeb;
import com.actionsoft.awf.report.design.web.DiggerWeb;
import com.actionsoft.awf.report.design.web.ExtendsPropertyWeb;
import com.actionsoft.awf.report.execute.web.DiggerCenterWeb;
import com.actionsoft.awf.report.execute.web.DiggerExecuteWeb;
import com.actionsoft.awf.report.execute.web.DiggerListWeb;
import com.actionsoft.awf.report.execute.web.DiggerPortlet;
import com.actionsoft.awf.report.execute.web.chart.AWSChartUtil;
import com.actionsoft.awf.report.execute.web.style.ChartReport;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.htmlframework.AlertWindow;
import com.actionsoft.i18n.I18nRes;

public class DiggerSocketCommand implements BaseSocketCommand {

    public boolean executeCommand(UserContext me, Socket myProcessSocket,
	    OutputStreamWriter myOut, Vector myCmdArray, UtilString myStr,
	    String socketCmd) throws Exception {
	if (socketCmd.equals("Digger_Design_List")) {
	    DiggerWeb web = new DiggerWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    if (formId == null || formId.equals(""))
		formId = "0";
	    myOut.write(web.getDiggerList(Integer.parseInt(formId), groupName,
		    false));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_UnitList")) {
	    DiggerWeb web = new DiggerWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    if (formId == null || formId.equals(""))
		formId = "0";
	    myOut.write(web.getDiggerList(Integer.parseInt(formId), groupName,
		    false));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Remove")) {
	    DiggerWeb web = new DiggerWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String groupName = myCmdArray.elementAt(4).toString();
	    String diggerId = UtilCode.decode(myStr.matchValue("_diggerId[",
		    "]diggerId_"));
	    if (formId == null || formId.equals(""))
		formId = "0";
	    myOut.write(web.removeDigger(Integer.parseInt(formId), diggerId,
		    groupName));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_CreatePage")) {
	    DiggerBaseDataTabWeb web = new DiggerBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    if ("".equals(formId))
		formId = "0";
	    String groupName = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getCreateDiggerPage(Integer.parseInt(formId),
		    groupName, false));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Create")) {
	    DiggerBaseDataTabWeb web = new DiggerBaseDataTabWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    String diggerName = myCmdArray.elementAt(4).toString();
	    String diggerType = myCmdArray.elementAt(5).toString();
	    String addType = myCmdArray.elementAt(6).toString();
	    String groupName = myCmdArray.elementAt(7).toString();
	    DiggerModel model = new DiggerModel();
	    model.setName(diggerName);
	    model.setFormId(Integer.parseInt(formId));
	    model.setType(Integer.parseInt(diggerType));
	    model.setMaster(me.getUID());
	    model.setAdvOpt(true);
	    model.setDiyCond(true);
	    model.setRepToolbar(true);
	    myOut.write(web.saveDigger(model, addType, groupName));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Open")) {
	    DiggerCardWeb web = new DiggerCardWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String pageType = myCmdArray.elementAt(4).toString();
	    if (pageType == null || pageType.equals(""))
		pageType = "0";
	    myOut.write(web.getCardPage(Integer.parseInt(diggerId),
		    Integer.parseInt(pageType)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_BaseData_Open")) {
	    DiggerBaseDataTabWeb web = new DiggerBaseDataTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId), false));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_BaseData_Save")) {
	    DiggerBaseDataTabWeb web = new DiggerBaseDataTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String diggerName = myCmdArray.elementAt(4).toString();
	    String isHyperLink = myCmdArray.elementAt(5).toString();
	    String pageLine = myCmdArray.elementAt(6).toString();
	    String orientation = myCmdArray.elementAt(7).toString();
	    String pageWidth = myCmdArray.elementAt(8).toString();
	    String pageHeight = myCmdArray.elementAt(9).toString();
	    String conditionCol = myCmdArray.elementAt(10).toString();
	    String autoExec = myCmdArray.elementAt(11).toString();
	    String ISDIYCOND = myCmdArray.elementAt(12).toString();
	    String ISADVOPT = myCmdArray.elementAt(13).toString();
	    String ISREPTOOLBAR = myCmdArray.elementAt(14).toString();
	    String master = UtilCode.decode(myStr.matchValue("_master[",
		    "]master_"));
	    String catpionHead = UtilCode.decode(myStr.matchValue(
		    "_catpionHead[", "]catpionHead_"));
	    String catpionFooter = UtilCode.decode(myStr.matchValue(
		    "_catpionFooter[", "]catpionFooter_"));
	    String formatClass = UtilCode.decode(myStr.matchValue(
		    "_formatClass[", "]formatClass_"));
	    if (master == null || master.equals(""))
		master = me.getUID();
	    if (isHyperLink == null || isHyperLink.equals(""))
		isHyperLink = "0";
	    if (pageLine == null || pageLine.equals(""))
		pageLine = "0";
	    if (orientation == null || orientation.equals(""))
		orientation = "0";
	    if (conditionCol == null || conditionCol.equals(""))
		conditionCol = "2";
	    if (pageWidth == null || pageWidth.equals(""))
		pageWidth = "0.0";
	    if (pageHeight == null || pageHeight.equals(""))
		pageHeight = "0.0";
	    DiggerModel model = (DiggerModel) DiggerCache.getModel(Integer
		    .parseInt(diggerId));
	    model.setName(diggerName);
	    model.setMaster(master);
	    model.setHyperLink(Integer.parseInt(isHyperLink));
	    model.setCatpionFooter(catpionFooter);
	    model.setCatpionHead(catpionHead);
	    model.setFormatClass(formatClass);
	    model.setPrintPageLine(Integer.parseInt(pageLine));
	    model.setPrintOrientation(Integer.parseInt(orientation));
	    model.setConditionCol(Integer.parseInt(conditionCol));
	    model.setAutoExec(autoExec.equals("1"));
	    model.setAdvOpt(ISADVOPT.equals("1"));
	    model.setDiyCond(ISDIYCOND.equals("1"));
	    model.setRepToolbar(ISREPTOOLBAR.equals("1"));
	    model.setPrintPageWidth(Double.parseDouble(pageWidth));
	    model.setPrintPageHeight(Double.parseDouble(pageHeight));
	    myOut.write(web.saveDigger(model, "0", ""));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_URL_Open")) {
	    DiggerURLTabWeb web = new DiggerURLTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_URL_Save")) {
	    DiggerURLTabWeb web = new DiggerURLTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String diggerURL = myCmdArray.elementAt(4).toString();
	    DiggerModel model = (DiggerModel) DiggerCache.getModel(Integer
		    .parseInt(diggerId));
	    model.setUrl(diggerURL);
	    myOut.write(web.saveDigger(model));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_DBSource_Open")) {
	    DiggerBaseDataTabWeb web = new DiggerBaseDataTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getDataSource(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_SumField_Open")) {
	    DiggerSumFieldTabWeb web = new DiggerSumFieldTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_SumField_Save")) {
	    DiggerSumFieldTabWeb web = new DiggerSumFieldTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String sumField = UtilCode.decode(myStr.matchValue("_sumField[",
		    "]sumField_"));
	    DiggerModel model = (DiggerModel) DiggerCache.getModel(Integer
		    .parseInt(diggerId));
	    model.setSumField(sumField);
	    myOut.write(web.saveDigger(model));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_ViewSQL_Open")) {
	    DiggerViewSQLTabWeb web = new DiggerViewSQLTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_ViewSQL_Save")) {
	    DiggerViewSQLTabWeb web = new DiggerViewSQLTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String dataSourceType = myCmdArray.elementAt(4).toString();
	    String ccUUID = myCmdArray.elementAt(5).toString();
	    String diggerSQL = new UtilString(UtilCode.decode(myStr.matchValue(
		    "_diggerSQL[", "]diggerSQL_"))).replace("__eol__", "");
	    DiggerModel model = (DiggerModel) DiggerCache.getModel(Integer
		    .parseInt(diggerId));
	    model.setSql(DiggerViewSQLTabWeb.assemble(dataSourceType,
		    diggerSQL, ccUUID));
	    myOut.write(web.saveDigger(model));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_CorssCol_Open")) {
	    DiggerCrossColTabWeb web = new DiggerCrossColTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_CrossCol_Create")) {
	    DiggerCrossColTabWeb web = new DiggerCrossColTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String fieldList = myCmdArray.elementAt(4).toString();
	    myOut.write(web.createDiggerMap(Integer.parseInt(diggerId),
		    fieldList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_CrossCol_Remove")) {
	    DiggerCrossColTabWeb web = new DiggerCrossColTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Title_Open")) {
	    DiggerTitleTabWeb web = new DiggerTitleTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.openTitleEditor(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Title_Open_JSONData")) {
	    DiggerTitleTabWeb web = new DiggerTitleTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.fetchJSonData(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Title_Save_New")) {
	    DiggerTitleTabWeb web = new DiggerTitleTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String titleData = UtilCode.decode(myStr.matchValue("_titleData[",
		    "]titleData_"));
	    myOut.write(web.updateDiggerMap(titleData,
		    Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_ChartConf_Open")) {
	    DiggerChartConfTabWeb web = new DiggerChartConfTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_ChartConf_Save")) {
	    DiggerChartConfTabWeb web = new DiggerChartConfTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String confData = UtilCode.decode(myStr.matchValue("_confData[",
		    "]confData_"));
	    DiggerModel model = (DiggerModel) DiggerCache.getModel(Integer
		    .parseInt(diggerId));
	    model.setUrl(confData.trim());
	    myOut.write(web.saveDigger(model));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_SQL_Open")) {
	    DiggerSQLTabWeb web = new DiggerSQLTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_SQL_Save")) {
	    DiggerSQLTabWeb web = new DiggerSQLTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String diggerSQL = UtilCode.decode(myStr.matchValue("_diggerSQL[",
		    "]diggerSQL_"));
	    DiggerModel model = (DiggerModel) DiggerCache.getModel(Integer
		    .parseInt(diggerId));
	    model.setSql(new UtilString(diggerSQL).replace("__eol__", ""));
	    myOut.write(web.saveDigger(model));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_GroupBy_Open")) {
	    DiggerGroupByTabWeb web = new DiggerGroupByTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_GroupBy_Create")) {
	    DiggerGroupByTabWeb web = new DiggerGroupByTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String fieldList = myCmdArray.elementAt(4).toString();
	    myOut.write(web.createDiggerMap(Integer.parseInt(diggerId),
		    fieldList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_GroupBy_Create_Open")) {
	    DiggerGroupByTabWeb web = new DiggerGroupByTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.openDiggerMapPage(Integer.parseInt(diggerId), false));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_GroupBy_Remove")) {
	    DiggerGroupByTabWeb web = new DiggerGroupByTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Stat_Open")) {
	    DiggerStatTabWeb web = new DiggerStatTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Stat_Property_Open")) {
	    ExtendsPropertyWeb web = new ExtendsPropertyWeb(me);
	    String diggerMapId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getStatPropertyPage(Integer.parseInt(diggerMapId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Stat_Property_Update")) {
	    ExtendsPropertyWeb web = new ExtendsPropertyWeb(me);
	    String diggerMapId = myCmdArray.elementAt(3).toString();
	    String fieldTitle = UtilCode.decode(myStr.matchValue(
		    "_fieldTitle[", "]fieldTitle_"));
	    String valueDefault = UtilCode.decode(myStr.matchValue(
		    "_valueDefault[", "]valueDefault_"));
	    myOut.write(web.saveStatProperty(Integer.parseInt(diggerMapId),
		    fieldTitle, valueDefault));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Stat_Create")) {
	    DiggerStatTabWeb web = new DiggerStatTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String fieldList = myCmdArray.elementAt(4).toString();
	    String statType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.createDiggerMap(Integer.parseInt(diggerId),
		    fieldList, statType));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Stat_Remove")) {
	    DiggerStatTabWeb web = new DiggerStatTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Condition_Open")) {
	    DiggerConditionTabWeb web = new DiggerConditionTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Condition_Create")) {
	    DiggerConditionTabWeb web = new DiggerConditionTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String fieldList = myCmdArray.elementAt(4).toString();
	    String joinType = myCmdArray.elementAt(5).toString();
	    String compareType = myStr.matchValue("_compareType[",
		    "]compareType_");
	    myOut.write(web.createDiggerMap(Integer.parseInt(diggerId),
		    fieldList, joinType, compareType));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Condition_Property_Open")) {
	    ExtendsPropertyWeb web = new ExtendsPropertyWeb(me);
	    String diggerMapId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getConditionPropertyPage(Integer
		    .parseInt(diggerMapId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Condition_Property_Update")) {
	    ExtendsPropertyWeb web = new ExtendsPropertyWeb(me);
	    String diggerMapId = myCmdArray.elementAt(3).toString();
	    String rtFieldType = myCmdArray.elementAt(4).toString();
	    String rtFieldDisplayType = myCmdArray.elementAt(5).toString();
	    String fieldTitle = UtilCode.decode(myStr.matchValue(
		    "_fieldTitle[", "]fieldTitle_"));
	    String rtFieldDisplayRef = UtilCode.decode(myStr.matchValue(
		    "_rtFieldDisplayRef[", "]rtFieldDisplayRef_"));
	    String mapDefaultValue = UtilCode.decode(myStr.matchValue(
		    "_mapDefaultValue[", "]mapDefaultValue_"));
	    myOut.write(web.saveConditionProperty(
		    Integer.parseInt(diggerMapId), fieldTitle, rtFieldType,
		    rtFieldDisplayType, rtFieldDisplayRef, mapDefaultValue));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Property_Open")) {
	    ExtendsPropertyWeb web = new ExtendsPropertyWeb(me);
	    String diggerMapId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getQueryPropertyPage(Integer.parseInt(diggerMapId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Property_Update")) {
	    ExtendsPropertyWeb web = new ExtendsPropertyWeb(me);
	    String diggerMapId = myCmdArray.elementAt(3).toString();
	    String rtFieldSum = myCmdArray.elementAt(4).toString();
	    String rtFieldDisplayWidth = myCmdArray.elementAt(5).toString();
	    String rtFieldDisplayBgColor = myCmdArray.elementAt(6).toString();
	    String orderIndex = myCmdArray.elementAt(7).toString();
	    String fieldTitle = UtilCode.decode(myStr.matchValue(
		    "_fieldTitle[", "]fieldTitle_"));
	    String valueDefault = UtilCode.decode(myStr.matchValue(
		    "_valueDefault[", "]valueDefault_"));
	    if (rtFieldSum.equals(""))
		rtFieldSum = "0";
	    myOut.write(web.saveQueryProperty(Integer.parseInt(diggerMapId),
		    fieldTitle, rtFieldDisplayWidth, rtFieldDisplayBgColor,
		    Integer.parseInt(rtFieldSum), Integer.parseInt(orderIndex),
		    valueDefault));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Condition_Remove")) {
	    DiggerConditionTabWeb web = new DiggerConditionTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Condition_ChangeFieldTitle")) {
	    DiggerConditionTabWeb web = new DiggerConditionTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String diggerMapId = myCmdArray.elementAt(4).toString();
	    String fieldTitle = myCmdArray.elementAt(5).toString();
	    myOut.write(web.changeFieldTitle(Integer.parseInt(diggerId),
		    Integer.parseInt(diggerMapId), fieldTitle));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_ChangeFieldTitle")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String diggerMapId = myCmdArray.elementAt(4).toString();
	    String fieldTitle = myCmdArray.elementAt(5).toString();
	    myOut.write(web.changeFieldTitle(Integer.parseInt(diggerId),
		    Integer.parseInt(diggerMapId), fieldTitle));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Sort_Open")) {
	    DiggerSortTabWeb web = new DiggerSortTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Sort_Create")) {
	    DiggerSortTabWeb web = new DiggerSortTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String fieldList = myCmdArray.elementAt(4).toString();
	    String sortType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.createDiggerMap(Integer.parseInt(diggerId),
		    fieldList, sortType));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Sort_Remove")) {
	    DiggerSortTabWeb web = new DiggerSortTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Security_Open")) {
	    DiggerSecurityTabWeb web = new DiggerSecurityTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId), 0));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Security_ChangeType")) {
	    DiggerSecurityTabWeb web = new DiggerSecurityTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String securityType = myCmdArray.elementAt(4).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId),
		    Integer.parseInt(securityType)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Security_Save")) {
	    DiggerSecurityTabWeb web = new DiggerSecurityTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String securityList = UtilCode.decode(myStr.matchValue(
		    "_securityList[", "]securityList_"));
	    myOut.write(web.saveSecurity(Integer.parseInt(diggerId),
		    securityList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Check_SQL")) {
	    DiggerCardWeb web = new DiggerCardWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.checkStatSQL(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Json")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getQueryJson(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Save")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String updateJson = UtilCode.decode(myStr.matchValue(
		    "_updateJson[", "]updateJson_"));
	    myOut.write(web.saveDiggerMap(updateJson));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Open")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getTab(Integer.parseInt(diggerId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Create_Open")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getQueryDisFieldPage(Integer.parseInt(diggerId),
		    false));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Create")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String fieldList = UtilCode.decode(myStr.matchValue("_fieldList[",
		    "]fieldList_"));
	    myOut.write(web.createDiggerMap(Integer.parseInt(diggerId),
		    fieldList));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Remove")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_Center")) {
	    DiggerCenterWeb web = new DiggerCenterWeb(me);
	    myOut.write(web.getCenter());
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_CenterList")) {
	    DiggerCenterWeb web = new DiggerCenterWeb(me);
	    String searchKey = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getCenterList(searchKey));
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_List")) {
	    DiggerListWeb web = new DiggerListWeb(me);
	    String formId = myCmdArray.elementAt(3).toString();
	    myOut.write(web.getDiggerList(Integer.parseInt(formId)));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_Query_Remove")) {
	    DiggerQueryTabWeb web = new DiggerQueryTabWeb(me);
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String idList = UtilCode.decode(myStr.matchValue("_idList[",
		    "]idList_"));
	    myOut.write(web.removeDiggerMap(Integer.parseInt(diggerId), idList));
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_Center")) {
	    DiggerCenterWeb web = new DiggerCenterWeb(me);
	    myOut.write(web.getCenter());
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_Open")) {
	    String sdiggerId = myCmdArray.elementAt(3).toString();
	    int diggerId = 0;
	    DiggerModel diggerModel = null;
	    try {
		diggerId = Integer.parseInt(sdiggerId);
		diggerModel = (DiggerModel) DiggerCache.getModel(diggerId);
	    } catch (Exception e) {
		diggerModel = (DiggerModel) DiggerCache.getModel(sdiggerId);
		if (diggerModel != null)
		    diggerId = diggerModel.getId();
	    }
	    if (diggerModel != null) {
		if (!DiggerUtil.checkDiggerCMDAccess(diggerId, me)) {
		    myOut.write(AlertWindow.getWarningWindow("",
			    "您没有本报表的执行权限，请与管理员联系"));
		} else {
		    DiggerExecuteWeb web = new DiggerExecuteWeb(me);
		    if (diggerModel.getType() == 2)
			myOut.write(web.getURLReport(diggerId));
		    else
			myOut.write(web
				.getDiggerConditionPageOfReport(diggerId));
		    web = null;
		}
	    } else {
		myOut.write(AlertWindow.getWarningWindow(I18nRes.findValue(
			me.getLanguage(), "报表定义丢失")
			+ "[" + sdiggerId + "]"));
	    }
	} else if (socketCmd.equals("Digger_Execute_DIY_Page")) {
	    String diggerIdParam = myCmdArray.elementAt(3).toString();
	    int diggerId = 0;
	    try {
		diggerId = Integer.parseInt(diggerIdParam);
	    } catch (Exception e) {
		DiggerModel diggerModel = (DiggerModel) DiggerCache
			.getModel(diggerIdParam);
		if (diggerModel != null)
		    diggerId = diggerModel.getId();
	    }
	    myOut.write(new DiggerExecuteWeb(me).getDIYCondPage(diggerId));
	} else if (socketCmd.equals("Digger_Execute_DIY_LoadUI")) {
	    String diggerId = myCmdArray.elementAt(3).toString();
	    String refField = myCmdArray.elementAt(4).toString();
	    myOut.write(new DiggerExecuteWeb(me).getAjaxDiyUI(
		    Integer.parseInt(diggerId), refField));
	} else if (socketCmd.equals("Digger_Execute_Portlet")) {
	    DiggerPortlet web = new DiggerPortlet(me);
	    String sdiggerId = myCmdArray.elementAt(3).toString();
	    int diggerId = 0;
	    try {
		diggerId = Integer.parseInt(sdiggerId);
	    } catch (Exception e) {
		DiggerModel model = (DiggerModel) DiggerCache
			.getModel(sdiggerId);
		diggerId = model.getId();
	    }
	    myOut.write(web.getContent(diggerId));
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_Report")) {
	    String pageSetting = "";
	    pageSetting = myCmdArray.elementAt(7).toString();
	    if (pageSetting == null || pageSetting.trim().equals(""))
		pageSetting = "0";
	    String sdiggerId = myCmdArray.elementAt(3).toString();
	    String dataState = myCmdArray.elementAt(4).toString();
	    String pageNow = myCmdArray.elementAt(5).toString();
	    String reportType = myCmdArray.elementAt(6).toString();
	    String sql = UtilCode.decode(myStr.matchValue("_sql[", "]sql_"));
	    String displayFields = UtilCode.decode(myStr.matchValue(
		    "_displayFields[", "]displayFields_"));
	    String conditionTitle = UtilCode.decode(myStr.matchValue(
		    "_conditionTitle[", "]conditionTitle_"));
	    String parentLink = UtilCode.decode(myStr.matchValue(
		    "_parentLink[", "]parentLink_"));
	    String currentLink = UtilCode.decode(myStr.matchValue(
		    "_currentLink[", "]currentLink_"));
	    String currentLinkValueTitle = UtilCode.decode(myStr.matchValue(
		    "_currentLinkValueTitle[", "]currentLinkValueTitle_"));
	    String preLinkObj = UtilCode.decode(myStr.matchValue(
		    "_preLinkObj[", "]preLinkObj_"));
	    if (pageNow == null || pageNow.equals(""))
		pageNow = "1";
	    int diggerId = 0;
	    try {
		diggerId = Integer.parseInt(sdiggerId);
	    } catch (Exception e) {
		DiggerModel model = (DiggerModel) DiggerCache
			.getModel(sdiggerId);
		if (model != null)
		    diggerId = model.getId();
	    }
	    if (diggerId == 0)
		myOut.write(AlertWindow.getWarningWindow(I18nRes
			.findValue("报表模型定义丢失") + "[" + sdiggerId + "]"));
	    else if (!DiggerUtil.checkDiggerCMDAccess(diggerId, me)) {
		myOut.write(AlertWindow.getWarningWindow("",
			I18nRes.findValue("您没有本报表的执行权限，请与管理员联系")));
	    } else {
		DiggerExecuteWeb web = new DiggerExecuteWeb(me);
		String content = web.getDiggerReport(diggerId, sql,
			Integer.parseInt(dataState), Integer.parseInt(pageNow),
			reportType, Integer.parseInt(pageSetting),
			displayFields, conditionTitle, currentLink, parentLink,
			currentLinkValueTitle, preLinkObj);
		if (content
			.indexOf("<script> window.location=encodeURI('');</script>") > -1) {
		    myOut.write(AlertWindow.getWarningWindow("",
			    I18nRes.findValue("您没有本报表的执行权限，请与管理员联系")));
		} else {
		    myOut.write(content);
		}
	    }
	} else if (socketCmd.equals("Digger_Execute_Chart_GetXML")) {
	    ChartReport web = new ChartReport(me);
	    String sdiggerId = myCmdArray.elementAt(3).toString();
	    String chartType = myCmdArray.elementAt(4).toString();
	    String chartX = myCmdArray.elementAt(5).toString();
	    String chartY = myCmdArray.elementAt(6).toString();
	    String sql = UtilCode.decode(myStr.matchValue("_sql[", "]sql_"));
	    int diggerId;
	    try {
		diggerId = Integer.parseInt(sdiggerId);
	    } catch (Exception e) {
		DiggerModel model = (DiggerModel) DiggerCache
			.getModel(sdiggerId);
		diggerId = model.getId();
	    }
	    if (chartX == null || chartX.equals(""))
		chartX = AWSChartUtil.getDiggerGroupByFieldList(diggerId);
	    if (chartY == null || chartY.equals(""))
		chartY = AWSChartUtil.getDiggerStatFieldList(diggerId);
	    myOut.write(web
		    .getDataXML(diggerId, sql, chartType, chartX, chartY));
	    web = null;
	} else if (socketCmd.equals("Digger_Execute_Chart_GetTreeNodes")) {
	    ChartReport web = new ChartReport(me);
	    String sdiggerId = myCmdArray.elementAt(3).toString();
	    int diggerId;
	    try {
		diggerId = Integer.parseInt(sdiggerId);
	    } catch (Exception e) {
		DiggerModel model = (DiggerModel) DiggerCache
			.getModel(sdiggerId);
		diggerId = model.getId();
	    }
	    myOut.write(web.getChartConditionTreeNodes(diggerId));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_ArrorUp")) {
	    DiggerWeb web = new DiggerWeb(me);
	    int diggerMapId = Integer.parseInt(myCmdArray.elementAt(3)
		    .toString());
	    int diggerId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    String mayType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.upIndex(diggerMapId, mayType, diggerId));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_ArrorDown")) {
	    DiggerWeb web = new DiggerWeb(me);
	    int diggerMapId = Integer.parseInt(myCmdArray.elementAt(3)
		    .toString());
	    int diggerId = Integer.parseInt(myCmdArray.elementAt(4).toString());
	    String mayType = myCmdArray.elementAt(5).toString();
	    myOut.write(web.downIndex(diggerMapId, mayType, diggerId));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_DownloadXML")) {
	    DiggerWeb web = new DiggerWeb(me);
	    String list = UtilCode.decode(myStr.matchValue("_list[", "]list_"));
	    myOut.write(web.getDownloadXMLDialog(list));
	    web = null;
	} else if (socketCmd.equals("Digger_Design_UploadXMLWindow")) {
	    DiggerWeb web = new DiggerWeb(me);
	    myOut.write(web.getUpFilePage());
	    web = null;
	} else if (socketCmd.equals("Digger_Design_UploadXMLImport")) {
	    DiggerWeb web = new DiggerWeb(me);
	    String groupName = myCmdArray.elementAt(3).toString();
	    myOut.write(web.installUploadXML(groupName));
	    web = null;
	} else {
	    return false;
	}
	return true;
    }
}
