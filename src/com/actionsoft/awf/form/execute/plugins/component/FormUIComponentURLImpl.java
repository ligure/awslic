package com.actionsoft.awf.form.execute.plugins.component;

import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.URLParser;
import java.util.Hashtable;
/**
 * 
 * @description 解决红头文件高度过低的问题，修改height为1100
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:45:50
 */
public class FormUIComponentURLImpl extends FormUIComponentAbst
{
  public FormUIComponentURLImpl(MetaDataMapModel metaDataMapModel, String value)
  {
    super(metaDataMapModel, value);
  }

  public String getModifyHtmlDefine(Hashtable params)
  {
    UserContext usercontext = (UserContext)params.get("me");
    MetaDataMapModel model = getMetaDataMapModel();
    StringBuffer html = new StringBuffer();
    RuntimeFormManager rfm = (RuntimeFormManager)params.get("runtimeFormManager");
    String urls = rfm.convertUrlMacrosValue(model.getDisplaySetting());
    urls = URLParser.repleaseNavURL(usercontext, urls);
    html.append("<iframe id='URL_").append(model.getFieldName()).append("' name='URL_").append(model.getFieldName()).append("' ").append(model.getHtmlInner()).append("  width='100%'  src=\"" + urls + "\" frameborder='no' border='0'  marginwidth='0' marginheight='0' height='1100' scrolling='auto'></iframe>");
    return html.toString();
  }

  public String getReadHtmlDefine(Hashtable params)
  {
    return getModifyHtmlDefine(params);
  }

  public String getValidateJavaScript(Hashtable params)
  {
    return "";
  }

  public String getValueJavaScript(Hashtable params)
  {
    return "";
  }

  public String getSettingWeb() {
    StringBuffer settingHtml = new StringBuffer();

    settingHtml.append("<tr >");
    settingHtml.append("<td width='100%' ><input onchange=\"creatInputConfig('AWS_UI_URL');\" type=text onkeypress='if(event.keyCode==13||event.which==13){return false;}' name=AWS_UI_URL id=AWS_UI_URL size=70 value='' class=actionsoftInput></td>");
    settingHtml.append("</tr>");

    settingHtml.append("<tr onclick=\"insertAtCaret(frmMain.AWS_UI_URL,'http://www.actionsoft.com.cn');creatInputConfig('AWS_UI_URL');\" style=\"cursor:pointer;cursor:hand;\" onmouseout=\"out_change(this,'#FFFFFF');\" onmouseover=\"over_change(this,'#EBF#F6');\">");
    settingHtml.append("<td width='100%' >一个http://前缀的URL地址</td>");
    settingHtml.append("</tr>");

    settingHtml.append("<script>");
    settingHtml.append("try{initInputConfig(escape('" + getMetaDataMapModel().getDisplaySetting() + "'),'AWS_UI_URL');}catch(e){alert(e.description);};");
    settingHtml.append("</script>\n");
    return settingHtml.toString();
  }
}