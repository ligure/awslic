package com.actionsoft.application.portal;

import com.actionsoft.application.portal.cache.UserPortalCache;
import com.actionsoft.application.portal.jsr168.PortletModeException;
import com.actionsoft.application.portal.lfm.util.StyleManagerUtil;
import com.actionsoft.application.portal.model.PortletLayoutModel;
import com.actionsoft.application.portal.model.PortletVendorModel;
import com.actionsoft.application.portal.model.UserPortalModel;
import com.actionsoft.application.portal.navigation.cache.NavigationFunctionCache;
import com.actionsoft.application.portal.navigation.cache.NavigationSystemCache;
import com.actionsoft.application.portal.navigation.model.NavigationFunctionModel;
import com.actionsoft.application.portal.navigation.model.NavigationSystemModel;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.URLParser;
import com.actionsoft.awf.util.UtilFile;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
/**
 * 
 * @description portalet默认只支持8个，需要修改增加更多
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:42:20
 */
public class AWSDesktopPortal
  implements DesktopPortal
{
  private String _systemId;
  private String _systemUUID;
  private UserContext _me;
  private Hashtable _list;
  private NavigationSystemModel _systemModel = null;
  String portletOwner;
  private static Vector _portalVendor = new Vector();

  private static HashMap _portalLayout = new HashMap();
  static Object lock = new Object();

  private static int isSubscibe = -1;

  static { reload();
  }

  public static void reload()
  {
    synchronized (lock) {
      _portalLayout = new HashMap();
      _portalVendor = new Vector();
      String xml = "aws-portlet-init.xml";
      SAXReader saxreader = new SAXReader();
      Document doc = DocumentFactory.getInstance().createDocument();
      try {
        doc = saxreader.read(new File(xml));
        Iterator it = doc.getRootElement().elementIterator();
        while (it.hasNext()) {
          Element ielement = (Element)it.next();
          if (ielement.getName().equals("portlet-lookandfeel")) {
            Iterator it2 = ielement.elementIterator();
            PortletVendorModel vendorModel = new PortletVendorModel();
            while (it2.hasNext()) {
              Element ielement2 = (Element)it2.next();
              if (ielement2.getName().equals("vendor"))
                vendorModel.setVendor(ielement2.getText());
              if (ielement2.getName().equals("desc"))
                vendorModel.setDesc(ielement2.getText());
              if (ielement2.getName().equals("class"))
                vendorModel.setClassName(ielement2.getText());
              if ((!ielement2.getName().equals("default")) || 
                (!ielement2.getText().toUpperCase().equals("TRUE"))) continue;
              vendorModel.setDefault(true);
            }

            _portalVendor.add(vendorModel);
          }
        }

        it = doc.getRootElement().elementIterator();
        while (it.hasNext()) {
          Element ielement = (Element)it.next();
          if (ielement.getName().equals("portlet-layout")) {
            Iterator it2 = ielement.elementIterator();
            PortletLayoutModel layoutModel = new PortletLayoutModel();
            while (it2.hasNext()) {
              Element ielement2 = (Element)it2.next();
              if (ielement2.getName().equals("layout-title"))
                layoutModel.setTitle(ielement2.getText());
              if (ielement2.getName().equals("layout-preview"))
                layoutModel.setPreview(ielement2.getText());
              if (ielement2.getName().equals("layout-model")) {
                layoutModel.setModel(ielement2.getText());

                UtilFile uf = new UtilFile(ielement2.getText());
                layoutModel.setModelContent(uf.readAll());
                uf.close();
              }
              if ((!ielement2.getName().equals("default")) || 
                (!ielement2.getText().toUpperCase().equals("TRUE"))) continue;
              layoutModel.setDefault(true);
            }

            _portalLayout.put(new Integer(_portalLayout.size()), layoutModel);
          }
        }
      } catch (Exception e) {
        e.printStackTrace(System.err);
      }
    }
  }

  public AWSDesktopPortal()
  {
  }

  public AWSDesktopPortal(UserContext me, String systemId, String systemUUID)
  {
    this._systemId = systemId;
    this._systemUUID = systemUUID;
    this._systemModel = ((NavigationSystemModel)NavigationSystemCache.getModel(this._systemUUID));
    this._me = me;
    String offer = StyleManagerUtil.getoffer(Integer.parseInt(systemId), this._me);

    if ((offer != null) && (offer.trim().length() > 0)) {
      this.portletOwner = offer;
    }
    else if (!isSubscibe())
      this.portletOwner = "admin";
    else {
      this.portletOwner = this._me.getUID();
    }

    this._list = UserPortalCache.getListOfUser(this.portletOwner, systemId);
  }

  public String getPortals()
  {
    String layoutT = "";
    if ((this._systemModel != null) && (this._systemModel._portletLayout.trim().length() > 0)) {
      layoutT = this._systemModel._portletLayout;
    }
    if (layoutT.length() == 0) {
      if (!isSubscibe())
        layoutT = ((UserModel)UserCache.getModel("admin")).getLayoutModel();
      else {
        layoutT = this._me.getUserModel().getLayoutModel();
      }
    }
    PortletLayoutModel layoutModel = getPortalLayout(layoutT);
    StringBuffer defaultPosition = new StringBuffer();
    Hashtable hashTags = new Hashtable();
    for (int i = 1; i < 9; i++) {
      hashTags.put("portletWindow" + i, getPortals("POOL" + i, defaultPosition));
    }
    return RepleaseKey.replace(layoutModel.getModelContent(), hashTags) + "\n<input type=hidden name=newPosition>\n<input type=hidden name=defaultPosition value='" + defaultPosition.toString() + "'>";
  }

  public String getPortals(String portalType, StringBuffer defaultPosition)
  {
    StringBuffer sb = new StringBuffer();

    defaultPosition.append("_").append(portalType).append(":");
    for (int i = 0; i < this._list.size(); i++) {
      UserPortalModel model = (UserPortalModel)this._list.get(new Integer(i));
      if ((model.getUserId().equals(this.portletOwner)) && (model.getSystemId().equals(this._systemId)) && (model.getPortalType().equals(portalType))) {
        sb.append(getPortlet(model));

        defaultPosition.append("aws-portlet-id").append(model.getId()).append(" ");
      }
    }
    if (sb.length() == 0) {
      sb.append("<span style='height:1px'></span>");
    }
    return sb.toString();
  }

  public String getPortlet(Object obj)
  {
    UserPortalModel userPortalModel = (UserPortalModel)obj;
    boolean isPersionSubscibe = isSubscibe();
    if (!this._me.getUID().equals(this.portletOwner)) {
      isPersionSubscibe = false;
    }
    StringBuffer sb = new StringBuffer();

    if (userPortalModel != null) {
      AWSPortletModel portletModel = new AWSPortletModel();
      portletModel.setHeight(userPortalModel.getPortalHeight());
      portletModel.setExtend(true);
      portletModel.setOid(Integer.toString(userPortalModel.getId()));
      portletModel.setTitle(userPortalModel.getPortalTitle());
      portletModel.setWidth(userPortalModel.getPortalWidth());
      portletModel.setRoll(userPortalModel.isRoll());
      portletModel.setUrl(userPortalModel.getPortalUrl());

      AWSPortletURL url = new AWSPortletURL();
      try {
        url.setPortletMode(portletModel);
        url.addParameter("userContext", this._me);
        url.addParameter("systemUUID", this._systemUUID);

        portletModel.setUrl(url.toString());
      } catch (PortletModeException pme) {
        pme.printStackTrace(System.err);
      }
      String protalURL = portletModel.getUrl();

      if ((protalURL.length() > 6) && (protalURL.substring(0, 6).toLowerCase().equals("nav://"))) {
        try {
          int functionId = Integer.parseInt(protalURL.substring(6, protalURL.indexOf("?")));
          NavigationFunctionModel functionModel = (NavigationFunctionModel)NavigationFunctionCache.getModel(functionId);
          if (functionModel == null) {
            protalURL = "../aws_html/portal-notfind.html";
          }

          if (!SecurityProxy.checkModelSecurity(this._me.getUID(), Integer.toString(functionModel._id))) {
            protalURL = "../aws_html/portal-noaccess.html";
          }
          else
            protalURL = URLParser.repleaseNavURL(this._me, functionModel._functionUrl);
        }
        catch (Exception e) {
          protalURL = "../aws_html/portal-notfind.html";
        }
      }

      portletModel.setUrl(protalURL);

      AWSPortletImpl awsPortlet = new AWSPortletImpl(portletModel, this._me.getLookAndFeelType(), this._me, isPersionSubscibe, this._systemUUID);

      sb.append(RepleaseKey.replaceI18NTag(this._me.getLanguage(), awsPortlet.getWindows()));
    }
    return sb.toString();
  }

  public String getSystemId()
  {
    return this._systemId;
  }

  public void setSystemId(String systemId)
  {
    this._systemId = systemId;
  }

  public boolean isSubscibe()
  {
    if (this._me.getUID().equals("admin")) {
      return true;
    }
    if (isSubscibe > -1)
    {
      return isSubscibe != 0;
    }

    String xml = "aws-portlet-init.xml";
    SAXReader saxreader = new SAXReader();
    Document doc = DocumentFactory.getInstance().createDocument();
    try
    {
      doc = saxreader.read(xml);

      Iterator it = doc.getRootElement().elementIterator();
      Hashtable hs = new Hashtable();
      int i = 0;
      while (it.hasNext()) {
        Element ielement = (Element)it.next();
        if (ielement.getName().equals("subscibe")) {
          String value = ielement.getText();
          if (value.equals("true")) {
            isSubscibe = 1;
            return true;
          }
          isSubscibe = 0;
          return false;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
    }
    return true;
  }

  public static Vector getPortalVendor()
  {
    return _portalVendor;
  }

  public static HashMap getPortalLayout()
  {
    synchronized (lock) {
      return _portalLayout;
    }
  }

  public static PortletLayoutModel getPortalLayout(String modelName)
  {
    for (int i = 0; i < _portalLayout.size(); i++) {
      PortletLayoutModel model = (PortletLayoutModel)_portalLayout.get(new Integer(i));
      if (model.getModel().equals(modelName)) {
        return model;
      }
    }
    return new PortletLayoutModel();
  }
}