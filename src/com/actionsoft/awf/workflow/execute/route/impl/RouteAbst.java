package com.actionsoft.awf.workflow.execute.route.impl;

import com.actionsoft.apps.portal.mobile.config.MobileConfig;
import com.actionsoft.apps.portal.mobile.model.MobileConfigModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.rule.ParticipantRuleEngine;
import com.actionsoft.awf.rule.ProcessRuleEngine;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UnsyncHashtable;
import com.actionsoft.awf.util.UtilString;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.util.WFSFlexDesignActorUtil;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.cache.CloudCacheValuesIterator;
import com.actionsoft.cache.ConcurrentCacheValuesIterator;
import com.actionsoft.htmlframework.htmlmodel.HtmlModelFactory;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.htmlframework.web.ActionsoftWeb;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Vector;
import net.sf.json.JSONObject;

public abstract class RouteAbst extends ActionsoftWeb
{
  public static String _cancelButton = "<input type=button value='<I18N#取消>'  class ='actionsoftButton' onClick='parent.window.close();return false;'   class='input'   border='0'>";
  public static String _preButton;
  public DepartmentModel _localDepartmentModel;
  public static String _addresssButton;
  public ProcessInstanceModel _instanceModel;
  public int _ownerDepartmentId;
  public static String _sendButton;

  public RouteAbst(UserContext arg0, ProcessInstanceModel arg1, DepartmentModel arg2, int arg3)
  {
    super(arg0);

    this._instanceModel = arg1;

    this._localDepartmentModel = arg2;

    this._ownerDepartmentId = arg3;
  }

  static
  {
    _cancelButton = "";

    _preButton = "";

    _sendButton = "<input type=button value='<I18N#发送>'  class ='actionsoftButton' onClick=\"execMyCommand(frmMain,'WorkFlow_Execute_Worklist_Transaction_Send');return false;\"  class='input'   border='0'>";

    _addresssButton = "<input type=button value='<I18N#地址簿>'  class ='actionsoftButton' onClick=\"openmailtree(frmMain,'MAIL_TO','Address_Inner_Open');return false;\"  class='input'   border='0'>";
  }

  public String getJumpUserAddress(WorkFlowStepModel arg0, int arg1)
  {
    ParticipantRuleEngine localParticipantRuleEngine = ProcessRuleEngine.getInstance().participantRuleEngine(
      getContext(), this._instanceModel.getId(), arg1);

    return localParticipantRuleEngine.getJumpUser();
  }

  public String getSelectTargetUser(String arg0, WorkFlowStepModel arg1)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    RuntimeFormManager localRuntimeFormManager;
    UtilString localUtilString;
    Vector localVector;
    String str1;
    int i;
    int j;
    Object localObject;
    if (getContext().isMobileClient())
    {
      if ((arg0 != null) && (!arg0.equals("")))
      {
        localRuntimeFormManager = new RuntimeFormManager(super.getContext(), this._instanceModel.getId(), 0, 0, 0);

        arg0 = new UtilString(arg0).replace(CloudCacheValuesIterator.ALLATORI_DEMO("\023*"), ConcurrentCacheValuesIterator.ALLATORI_DEMO("~j\036"));

        arg0 = localRuntimeFormManager.convertMacrosValue(arg0);

        localUtilString = new UtilString(arg0);

        localVector = new Vector();

        str1 = "";

        localVector = localUtilString.split(CloudCacheValuesIterator.ALLATORI_DEMO("J"));

        localStringBuilder
          .append(ConcurrentCacheValuesIterator.ALLATORI_DEMO("}q-7b-7a: * s31-;|y'7$2%=.05?(0f`}2 <$2a-5'-;|y'1/*l)$7&65da0.,,?-ef`}\027pf\017}厮遗荂嚪d}q-?#;-`"));

        localStringBuilder.append(CloudCacheValuesIterator.ALLATORI_DEMO("VU\003V\006W\031V\036\023\016R\036RGA\005_\017\016MP\005]\036A\005_\rA\005F\032\024T"));

        i = 1;

        j = 1;

        if (arg1 != null)
        {
          String str2 = arg1._routeText;

          localObject = WFSFlexDesignActorUtil.getDesigData(str2);

          if (((JSONObject)localObject).containsKey(ConcurrentCacheValuesIterator.ALLATORI_DEMO("34257\022;-;\"*"))) {
            i = ((JSONObject)localObject).optInt(CloudCacheValuesIterator.ALLATORI_DEMO("\007F\006G\003`\017_\017P\036")) > 0 ? 1 : 0;
          }

          if (((JSONObject)localObject).containsKey(ConcurrentCacheValuesIterator.ALLATORI_DEMO("34257\022;-;\"*\00572?#2$:\0026$=*\034.&"))) {
            j = ((JSONObject)localObject).optInt(CloudCacheValuesIterator.ALLATORI_DEMO("\007F\006G\003`\017_\017P\036w\003@\013Q\006V\016p\002V\tX(\\\022")) > 0 ? 1 : 0;
          }

        }

        for (int k = 0; k < localVector.size(); k++)
        {
          localObject = (String)localVector.get(k);

          if (((String)localObject).trim().length() == 0)
          {
            continue;
          }

          localObject = Function.getUID((String)localObject);

          if (!Function.checkAddress((String)localObject).equals(ConcurrentCacheValuesIterator.ALLATORI_DEMO(".5")))
          {
            continue;
          }
          str1 = str1 + CloudCacheValuesIterator.ALLATORI_DEMO("VZ\004C\037GJG\023C\017\016MP\002V\tX\b\\\022\024JW\013G\013\036\007Z\004ZW\024\036A\037VM\023\003WW\021\036R\031X9_\017P\036") + k + ConcurrentCacheValuesIterator.ALLATORI_DEMO("|a0 3$c5?25\022;-;\"*") + 
            System.currentTimeMillis();

          str1 = str1 + CloudCacheValuesIterator.ALLATORI_DEMO("JE\013_\037VW") + (String)localObject + (i != 0 ? "" : new StringBuilder(ConcurrentCacheValuesIterator.ALLATORI_DEMO("a=);\"5$:")).append(j != 0 ? CloudCacheValuesIterator.ALLATORI_DEMO("JW\003@\013Q\006V\016") : "").toString());

          str1 = str1 + ConcurrentCacheValuesIterator.ALLATORI_DEMO("~b-?#;-~'13cc* -*\r-;\"*") + k + CloudCacheValuesIterator.ALLATORI_DEMO("\021T") + 
            releaseHtmlTag(Function.getAddressFullName((String)localObject)) + ConcurrentCacheValuesIterator.ALLATORI_DEMO("}q-?#;-`");
        }

        localStringBuilder.append(str1);

        localStringBuilder.append(CloudCacheValuesIterator.ALLATORI_DEMO("V\034\fZ\017_\016@\017GT"));
      }
    }
    else if ((arg0 != null) && (!arg0.equals("")))
    {
      localRuntimeFormManager = new RuntimeFormManager(super.getContext(), this._instanceModel.getId(), 0, 0, 0);

      arg0 = new UtilString(arg0).replace(ConcurrentCacheValuesIterator.ALLATORI_DEMO("a\036"), CloudCacheValuesIterator.ALLATORI_DEMO("J\030*"));

      arg0 = localRuntimeFormManager.convertMacrosValue(arg0);

      localUtilString = new UtilString(arg0);

      localVector = new Vector();

      str1 = "";

      localVector = localUtilString.split(ConcurrentCacheValuesIterator.ALLATORI_DEMO("~"));

      localStringBuilder
        .append(CloudCacheValuesIterator.ALLATORI_DEMO("\017\036AT\017\036WJD\003W\036[W\024[\nO\024J[\017Z\r[\036\016M\002Z\024J]\005D\030R\032\016M]\005D\030R\032\024J@\036J\006VW\024\035\\\030WGD\030R\032\t\bA\017R\001\036\035\\\030WQ\024T\023VW\003EJR\006Z\r]W\024\030Z\r[\036\024T\017\031G\030\\\004TT\017#\002R}I叜遣茰嚞\rｰ\017E@\036A\005]\r\rV\034\016Z\034\rV\034\036WT"));

      localStringBuilder.append(ConcurrentCacheValuesIterator.ALLATORI_DEMO("}*%~67%*)cffp{f~);(9)*|ypnf~\"1--1?/cfmf`}:((a-5'-;|y67%*)dpnq{f`"));

      i = 1;

      j = 1;

      if (arg1 != null)
      {
        String str3 = arg1._routeText;

        localObject = WFSFlexDesignActorUtil.getDesigData(str3);

        if (((JSONObject)localObject).containsKey(CloudCacheValuesIterator.ALLATORI_DEMO("\007F\006G\003`\017_\017P\036")))
          i = ((JSONObject)localObject).optInt(ConcurrentCacheValuesIterator.ALLATORI_DEMO("34257\022;-;\"*")) > 0 ? 1 : 0;
        if (((JSONObject)localObject).containsKey(CloudCacheValuesIterator.ALLATORI_DEMO("\007F\006G\003`\017_\017P\036w\003@\013Q\006V\016p\002V\tX(\\\022")))
        {
          j = ((JSONObject)localObject).optInt(ConcurrentCacheValuesIterator.ALLATORI_DEMO("34257\022;-;\"*\00572?#2$:\0026$=*\034.&")) > 0 ? 1 : 0;
        }
      }
      for (int m = 0; m < localVector.size(); m++)
      {
        localObject = (String)localVector.get(m);

        if (((String)localObject).trim().length() == 0)
          continue;
        localObject = Function.getUID((String)localObject);

        if (!Function.checkAddress((String)localObject).equals(CloudCacheValuesIterator.ALLATORI_DEMO("\\\001")))
          continue;
        String str4 = ConcurrentCacheValuesIterator.ALLATORI_DEMO("* -*\r$2$=5\001") + (String)localObject;

        str1 = str1 + CloudCacheValuesIterator.ALLATORI_DEMO("VZ\004C\037GJG\023C\017\016MP\002V\tX\b\\\022\024JZ\016\016M") + str4 + ConcurrentCacheValuesIterator.ALLATORI_DEMO("ya0 3$c") + str4;

        str1 = str1 + CloudCacheValuesIterator.ALLATORI_DEMO("JE\013_\037VW") + (String)localObject + (
          i != 0 ? "" : new StringBuilder(ConcurrentCacheValuesIterator.ALLATORI_DEMO("a=);\"5$:")).append(j != 0 ? CloudCacheValuesIterator.ALLATORI_DEMO("JW\003@\013Q\006V\016") : "").toString());

        str1 = str1 + ConcurrentCacheValuesIterator.ALLATORI_DEMO("a`}2 <$2a8.,|y") + str4 + CloudCacheValuesIterator.ALLATORI_DEMO("\024T") + releaseHtmlTag(Function.getAddressFullName((String)localObject)) + ConcurrentCacheValuesIterator.ALLATORI_DEMO("}q-?#;-`g0#-1eg0#-1e");
      }

      localStringBuilder.append(str1);

      localStringBuilder.append(CloudCacheValuesIterator.ALLATORI_DEMO("\017EW\003ET\017EG\016\rV\034\036AT"));
    }

    return (String)localStringBuilder.toString();
  }

  public String getSelectTargetUser(String arg0)
  {
    return getSelectTargetUser(arg0, null);
  }

  public abstract String getTargetUserAddress(WorkFlowStepModel paramWorkFlowStepModel);

  public abstract String getTargetUserAddress1(WorkFlowStepModel paramWorkFlowStepModel, int paramInt);

  private void ALLATORI_DEMO(UserContext arg0, Hashtable arg1)
  {
    String str = arg0.getSessionId();
    try
    {
      str = URLEncoder.encode(str, ConcurrentCacheValuesIterator.ALLATORI_DEMO("\013\025\030lf"));
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
    }

    arg1.put(CloudCacheValuesIterator.ALLATORI_DEMO("\031V\031@\003\\\004z\016"), str);

    arg1.put(ConcurrentCacheValuesIterator.ALLATORI_DEMO("6$?%;3\n);,;"), MobileConfig.getConfModel().getHeaderTheme());

    arg1.put(CloudCacheValuesIterator.ALLATORI_DEMO("\036[\017^\017"), MobileConfig.getConfModel().getTheme());

    arg1.put(ConcurrentCacheValuesIterator.ALLATORI_DEMO("8.15;3\n);,;"), MobileConfig.getConfModel().getFooterTheme());

    arg1.put(CloudCacheValuesIterator.ALLATORI_DEMO("G\030R\004@\003G\003\\\004"), MobileConfig.getConfModel().getTransition());
  }

  public RouteAbst(UserContext arg0)
  {
    super(arg0);
  }

  public String releaseHtmlTag(String arg0)
  {
    if (arg0.indexOf(ConcurrentCacheValuesIterator.ALLATORI_DEMO("*8.$cf=);\"5#19y")) == -1)
    {
      arg0 = new UtilString(arg0).replace(CloudCacheValuesIterator.ALLATORI_DEMO("V"), ConcurrentCacheValuesIterator.ALLATORI_DEMO("g25e"));

      arg0 = new UtilString(arg0).replace(CloudCacheValuesIterator.ALLATORI_DEMO("T"), ConcurrentCacheValuesIterator.ALLATORI_DEMO("g95e"));
    }
    return arg0;
  }

  public String getTargetUserAddress(WorkFlowStepModel arg0, int arg1)
  {
    String str = getJumpUserAddress(arg0, arg1);

    if ((str == null) || ("".equals(str.trim()))) {
      if (RouteControl.isRouteVORG(arg0))
      {
        str = RouteControl.find4VORG(this, arg0, arg1);
      }
      else {
        str = getTargetUserAddress1(arg0, arg1);
      }

    }

    return normalizeUserFt(str);
  }

  public String normalizeUserFt(String arg0)
  {
    if (arg0 == null)
    {
      return "";
    }
    Vector localVector = new UtilString(arg0.trim()).split(CloudCacheValuesIterator.ALLATORI_DEMO("J"));
    String str1 = "";

    for (int i = 0; i < localVector.size(); i++)
    {
      String str2 = (String)localVector.get(i);

      if (str2.trim().length() == 0)
        continue;
      str2 = Function.getUID(str2);

      UserModel localUserModel = (UserModel)UserCache.getModel(str2);

      if ((localUserModel == null) || (localUserModel.isDisabled()))
        continue;
      if (!localUserModel.getUID().equals(localUserModel.getUserName()))
        str2 = str2 + ConcurrentCacheValuesIterator.ALLATORI_DEMO("b") + localUserModel.getUserName() + CloudCacheValuesIterator.ALLATORI_DEMO("T");
      str1 = str1 + ConcurrentCacheValuesIterator.ALLATORI_DEMO("~") + str2;
    }

    return str1.trim();
  }

  public String alertMessage2(String arg0, String arg1, String arg2)
  {
    if (getContext().isMobileClient())
    {
      UnsyncHashtable localObject = new UnsyncHashtable();

      ((Hashtable)localObject).put(CloudCacheValuesIterator.ALLATORI_DEMO("\031Z\016"), ConcurrentCacheValuesIterator.ALLATORI_DEMO("}7/.4*a*8.$c)7%:$0a0 3$c27%~7?-+$c") + getContext().getSessionId() + CloudCacheValuesIterator.ALLATORI_DEMO("\r`"));

      ((Hashtable)localObject).put(ConcurrentCacheValuesIterator.ALLATORI_DEMO("329"), arg0 + CloudCacheValuesIterator.ALLATORI_DEMO("P") + arg1 + arg2);

      ALLATORI_DEMO(getContext(), (Hashtable)localObject);

      return RepleaseKey.replace(HtmlModelFactory.getHtmlModel(ConcurrentCacheValuesIterator.ALLATORI_DEMO("\"1,p =57.021'*o?1.2p113* 2o3.<(2$\001\02113* 2\036\n -*\001\f-&\034.&o653")), 
        (Hashtable)localObject);
    }
    Object localObject = "";

    String str = CloudCacheValuesIterator.ALLATORI_DEMO("U\037]\tG\003\\\004\023\t_\005@\017z=|8xB\032\021CG\036QZ\f\033\032\016W\003CC\013A\017]\036\035\035Z\004W\005DDP\006\\\031VB\032QN\034R\030\023\032\016Y\b\031V\036z\004G\017A\034R\006\033MP\006\\\031V#d%a!\033C\024F\002Z\003Z\032Q");

    UnsyncHashtable localUnsyncHashtable = new UnsyncHashtable();

    localUnsyncHashtable.put(ConcurrentCacheValuesIterator.ALLATORI_DEMO("-(:"), CloudCacheValuesIterator.ALLATORI_DEMO("\017\003]\032F\036\023\036J\032VW[\003W\016V\004\023\004R\007VW@\003WJE\013_\037VW") + getContext().getSessionId() + ConcurrentCacheValuesIterator.ALLATORI_DEMO("T"));

    localUnsyncHashtable.put(CloudCacheValuesIterator.ALLATORI_DEMO("\036Z\036_\017"), arg0);

    localUnsyncHashtable.put(ConcurrentCacheValuesIterator.ALLATORI_DEMO("8-?&o"), arg1);

    localUnsyncHashtable.put(CloudCacheValuesIterator.ALLATORI_DEMO("\f_\013TX"), arg2);

    localUnsyncHashtable.put(ConcurrentCacheValuesIterator.ALLATORI_DEMO("8-?&m"), localObject);

    localUnsyncHashtable.put(CloudCacheValuesIterator.ALLATORI_DEMO("Y\031"), str);

    localUnsyncHashtable.put(ConcurrentCacheValuesIterator.ALLATORI_DEMO(".0-1 :"), "");

    return (String)RepleaseKey.replace(HtmlModelFactory.getHtmlModel(CloudCacheValuesIterator.ALLATORI_DEMO("\031J\031l\013_\017A\036\035\002G\007")), localUnsyncHashtable);
  }

  public abstract String getRoutePage(int paramInt1, int paramInt2);
}