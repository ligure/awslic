package com.actionsoft.awf.form.execute.plugins.component;

import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.component.web.UIDBSourceUtil;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.tools.portconfig.util.UtilString;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

public class FormUIComponentCheckImpl extends FormUIComponentAbst{
	
	public FormUIComponentCheckImpl(MetaDataMapModel metaDataMapModel,String value) {
		super(metaDataMapModel, value);
	}
  
	/**
	 * 获取组件可编辑HTML
	 */
	public String getModifyHtmlDefine(Hashtable params) {
		UserContext me = (UserContext) params.get("me");
		StringBuilder html = new StringBuilder();
		String rowData = getMetaDataMapModel().getDisplaySetting();
		RuntimeFormManager rfm = (RuntimeFormManager) params
				.get("runtimeFormManager");
		rowData = rfm.convertMacrosValue(rowData);

		if (rowData.length() == 0) {
			return I18nRes.findValue(me != null ? me.getLanguage() : "cn","复选框_没有给出参考值，请在数据模型中配置此字段_");
		}
		if ((rowData.toUpperCase().startsWith("CASCADE"))&& (rowData.toUpperCase().indexOf(">>") > "CASCADE".length())) {
			UtilString util = new UtilString(rowData);
			Vector v = util.split(">>");
			rowData = v.get(1).toString();
		}

		if (rowData.toUpperCase().startsWith("SQL>")) {
			rowData = rowData.substring("SQL>".length());
			getSqlModifyHtmlDefine(html, params, rowData);
		} else {
			getConsModifyHtmlDefine(html, params, rowData);
		}

		if ((getMetaDataMapModel().isNotNull()) && (params.get("bindId") != null)){
			html.append("<img src=../aws_img/notNull.gif alt='<I18N#必须填写>'>");
		}else {
			html.append("<img src=../aws_img/null.gif alt='<I18N#允许空>'>");
		}
		return html.toString();
	}
  
	/**
	 * 获取组件只读HTML
	 */
	public String getReadHtmlDefine(Hashtable params) {
		StringBuilder html = new StringBuilder();
		String rowData = getMetaDataMapModel().getDisplaySetting();

		if (rowData.length() == 0) {
			return "<#I18N#复选框_没有给出参考值，请在数据模型中配置此字段_>";
		}
		RuntimeFormManager rfm = (RuntimeFormManager) params
				.get("runtimeFormManager");
		rowData = rfm.convertMacrosValue(rowData);
		if ((rowData.toUpperCase().startsWith("CASCADE"))
				&& (rowData.toUpperCase().indexOf(">>") > "CASCADE".length())) {
			UtilString util = new UtilString(rowData);
			Vector v = util.split(">>");
			rowData = v.get(1).toString();
		}

		if (rowData.toUpperCase().startsWith("SQL>")) {
			rowData = rowData.split("\\>")[1];
			getSqlReadHtmlDefine(html, params, rowData);
		} else {
			getConsReadHtmlDefine(html, rowData);
		}
		return html.toString();
	}
	
	/**
	 * 获取常量方式可编辑HTML
	 * @param html
	 * @param params 当前表单及流程上下文参数
	 * @param rowData 常量
	 */
	private void getConsModifyHtmlDefine(StringBuilder html, Hashtable params,String rowData) {
		String value = getValue();
		String key = "";
		String displayValue = "";
		if ((rowData.trim().length() > 0) || (rowData.indexOf("|") > 0)) {
			String[] rowDatas = rowData.trim().split("\\|");
			String[] values = value.trim().split("\\|");
			for (int i = 0; i < rowDatas.length; i++) {
				UtilString tmpUtil = new UtilString(rowDatas[i]);
				Vector tmpV = tmpUtil.split(":");
				key = tmpV.get(0).toString();
				if (tmpV.size() == 1)
					displayValue = key;
				else {
					displayValue = tmpV.get(1).toString();
				}
				boolean flag = false;
				for (int j = 0; j < values.length; j++) {
					if (key.equals(values[j])) {
						flag = true;
						break;
					}
				}
				if (flag){
					html.append("<label><input class='actionsoftChk' type=checkbox ").append(
							getMetaDataMapModel().getHtmlInner()).append(
							"  name=").append(
							getMetaDataMapModel().getFieldName()).append(
							" value='").append(key).append("' checked>")
							.append(I18nRes.findValue(displayValue)).append(
									"</label>");
				}else{
					html.append("<label><input class='actionsoftChk' type=checkbox  ").append(
							getMetaDataMapModel().getHtmlInner()).append(
							" name=").append(
							getMetaDataMapModel().getFieldName()).append(
							" value='").append(key).append("'>").append(
							I18nRes.findValue(displayValue)).append("</label>");
				}
			}
		} else {
			UtilString tmpUtil = new UtilString(rowData);
			Vector tmpV = tmpUtil.split(":");
			key = tmpV.get(0).toString();
			if (tmpV.size() == 1)
				displayValue = key;
			else {
				displayValue = tmpV.get(1).toString();
			}
			if (key.equals(value))
				html.append("<label><input class='actionsoftChk' type=checkbox ").append(
						getMetaDataMapModel().getHtmlInner()).append("  name=")
						.append(getMetaDataMapModel().getFieldName()).append(
								" value='").append(key).append("' checked>")
						.append(
								I18nRes.findValue(I18nRes
										.findValue(displayValue))).append(
								"</label>");
			else
				html.append("<label><input class='actionsoftChk' type=checkbox  ").append(
						getMetaDataMapModel().getHtmlInner()).append(" name=")
						.append(getMetaDataMapModel().getFieldName()).append(
								" value='").append(key).append("'>").append(
								I18nRes.findValue(I18nRes
										.findValue(displayValue))).append(
								"</label>");
		}
	}
	
	/**
	 * 获取SQL方式可编辑HTML
	 * @param html
	 * @param params 当前表单及流程上下文参数
	 * @param rowData 常量
	 */
	private void getSqlModifyHtmlDefine(StringBuilder html, Hashtable params,String rowData) {
		String sqlGet = "";
		String sqlShow = "";
		String sqlSelect = "";
		String DBSOURCE = "";
		UtilString util = new UtilString(rowData);
		Vector v = util.split("|");
		if ((v != null) && (v.size() > 1)) {
			sqlSelect = v.get(1).toString();
			if (sqlSelect.trim().length() == 0) {
				return;
			}
			if (v.size() > 2) {
				DBSOURCE = v.get(2).toString();
			}
			util = new UtilString(v.get(0).toString());
			v = util.split(":");
			if ((v == null) || (v.size() == 0))
				return;
			if (v.size() > 1) {
				sqlGet = v.get(1).toString().trim();
				sqlShow = v.get(0).toString().trim();
			} else {
				sqlGet = v.get(0).toString().trim();
				sqlShow = sqlGet;
			}
			getOptionsOfCheckforModify(DBSOURCE, html, sqlGet, sqlShow,sqlSelect, params);
		}
	}

	private void getOptionsOfCheckforModify(String source, StringBuilder html,String sqlGet, String sqlShow, String sqlSelect, Hashtable params) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		String tmpGetValue = "";
		String tmpShowValue = "";
		try {
			conn = UIDBSourceUtil.open(source, (UserContext) params.get("me"));
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sqlSelect);
			while (rset.next()) {
				if (sqlGet.trim().length() != 0)
					tmpGetValue = rset.getString(sqlGet);
				else {
					tmpGetValue = rset.getString(1);
				}
				if (sqlShow.trim().length() != 0)
					tmpShowValue = rset.getString(sqlShow);
				else {
					tmpShowValue = tmpGetValue;
				}
				String[] values = getValue().trim().split("\\|");
				boolean flag = false;
				for (int j = 0; j < values.length; j++) {
					if (tmpGetValue.equals(values[j])) {
						flag = true;
						break;
					}
				}
				if (flag)
					html.append("<label><input class='actionsoftChk' type=checkbox ").append(
							getMetaDataMapModel().getHtmlInner()).append(
							"  name=").append(
							getMetaDataMapModel().getFieldName()).append(
							" value='").append(tmpGetValue)
							.append("' checked>").append(
									I18nRes.findValue(tmpShowValue)).append(
									"</label>");
				else
					html.append("<label><input class='actionsoftChk' type=checkbox ").append(
							getMetaDataMapModel().getHtmlInner()).append(
							"  name=").append(
							getMetaDataMapModel().getFieldName()).append(
							" value='").append(tmpGetValue).append("' >")
							.append(I18nRes.findValue(tmpShowValue)).append(
									"</label>");
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			UIDBSourceUtil.close(source, conn, stmt, rset);
		}
	}
	
	/**
	 * 获取常量方式组件只读HTML
	 * @param html
	 * @param rowData 参数
	 */
	private void getConsReadHtmlDefine(StringBuilder html, String rowData) {
		String value = getValue();
		String key = "";
		String displayValue = "";
		if ((rowData.trim().length() > 0) || (rowData.indexOf("|") > 0)) {
			String[] rowDatas = rowData.trim().split("\\|");
			String[] values = value.trim().split("\\|");
			for (int i = 0; i < rowDatas.length; i++) {
				UtilString tmpUtil = new UtilString(rowDatas[i]);
				Vector tmpV = tmpUtil.split(":");
				key = tmpV.get(0).toString();
				if (tmpV.size() == 1)
					displayValue = key;
				else {
					displayValue = tmpV.get(1).toString();
				}
				boolean flag = false;
				for (int j = 0; j < values.length; j++) {
					if (key.equals(values[j])) {
						flag = true;
						break;
					}
				}
				if (flag) {
					html.append(
							"<div style='display:none'><input class='actionsoftChk' type=checkbox ")
							.append(getMetaDataMapModel().getHtmlInner())
							.append("  name=").append(
									getMetaDataMapModel().getFieldName())
							.append(" value='").append(key)
							.append("' checked>").append(
									I18nRes.findValue(displayValue)).append(
									"</div>");
					html
							.append(
									"<label><img class='actionsoftChkIcon chkOk' src='../aws_img/check-ok.gif' border=0>")
							.append(I18nRes.findValue(displayValue)).append(
									"</label>");
				} else {
					html.append(
							"<div style='display:none'><input class='actionsoftChk' type=checkbox ")
							.append(getMetaDataMapModel().getHtmlInner())
							.append("  name=").append(
									getMetaDataMapModel().getFieldName())
							.append(" value='").append(key).append("'>")
							.append(I18nRes.findValue(displayValue)).append(
									"</div>");
					html
							.append(
									"<label><img class='actionsoftChkIcon chkNo' src='../aws_img/check-no.gif' border=0>")
							.append(I18nRes.findValue(displayValue)).append(
									"</label>");
				}
			}
		} else {
			UtilString tmpUtil = new UtilString(rowData);
			Vector tmpV = tmpUtil.split(":");
			key = tmpV.get(0).toString();
			if (tmpV.size() == 1)
				displayValue = key;
			else {
				displayValue = tmpV.get(1).toString();
			}
			if (key.equals(getValue())) {
				html.append("<div style='display:none'><input class='actionsoftChk' type=checkbox ")
						.append(getMetaDataMapModel().getHtmlInner()).append(
								"  name=").append(
								getMetaDataMapModel().getFieldName()).append(
								" value='").append(rowData).append(
								"' checked></div>");
				html.append(
						"<label><img class='actionsoftChkIcon chkOk' src='../aws_img/check-ok.gif' border=0>")
						.append(rowData).append("</label>");
			} else {
				html.append("<div style='display:none'><input class='actionsoftChk' type=checkbox ")
						.append(getMetaDataMapModel().getHtmlInner()).append(
								"  name=").append(
								getMetaDataMapModel().getFieldName()).append(
								" value='").append(rowData).append("'></div>");
				html.append(
						"<label><img class='actionsoftChkIcon chkNo' src='../aws_img/check-no.gif' border=0>")
						.append(rowData).append("</label>");
			}
		}
	}
	
	/**
	 * 获取SQL方式组件只读HTML
	 * @param html
	 * @param rowData 参数
	 */
	private void getSqlReadHtmlDefine(StringBuilder html, Hashtable params,String rowData) {
		String sqlGet = "";
		String sqlShow = "";
		String sqlSelect = "";
		String DBSOURCE = "";
		UtilString util = new UtilString(rowData);
		Vector v = util.split("|");
		if ((v != null) && (v.size() > 1)) {
			sqlSelect = v.get(1).toString();
			if (sqlSelect.trim().length() == 0) {
				return;
			}
			if (v.size() > 2) {
				DBSOURCE = v.get(2).toString();
			}
			util = new UtilString(v.get(0).toString());
			v = util.split(":");
			if ((v == null) || (v.size() == 0))
				return;
			if (v.size() > 1) {
				sqlGet = v.get(1).toString();
				sqlShow = v.get(0).toString();
			} else {
				sqlGet = v.get(0).toString();
				sqlShow = sqlGet;
			}
			getOptionsOfCheckforRead(DBSOURCE, html, sqlGet, sqlShow,sqlSelect, params);
		}
	}

  private void getOptionsOfCheckforRead(String source, StringBuilder html, String sqlGet, String sqlShow, String sqlSelect, Hashtable params)
  {
    Connection conn = null;
    Statement stmt = null;
    ResultSet rset = null;
    String tmpGetValue = "";
    String tmpShowValue = "";
    try {
      conn = UIDBSourceUtil.open(source, (UserContext)params.get("me"));
      stmt = conn.createStatement();
      rset = DBSql.executeQuery(conn, stmt, sqlSelect);
      while (rset.next())
      {
        if (sqlGet.trim().length() != 0)
          tmpGetValue = rset.getString(sqlGet);
        else {
          tmpGetValue = rset.getString(1);
        }
        if (sqlShow.trim().length() != 0)
          tmpShowValue = rset.getString(sqlShow);
        else {
          tmpShowValue = tmpGetValue;
        }

        String[] values = getValue().trim().split("\\|");
        boolean flag = false;
        for (int j = 0; j < values.length; j++) {
          if (tmpGetValue.equals(values[j])) {
            flag = true;
            break;
          }
        }
        if (flag) {
          html.append("<div style='display:none'><input class='actionsoftChk' type=checkbox ").append(getMetaDataMapModel().getHtmlInner()).append("  name=").append(getMetaDataMapModel().getFieldName()).append(" value='").append(tmpGetValue).append("' checked>").append(tmpShowValue).append("</div>");
          html.append("<label><img class='actionsoftChkIcon chkOk' src='../aws_img/check-ok.gif' border=0>").append(tmpShowValue).append("</label>");
        } else {
          html.append("<label><img class='actionsoftChkIcon chkNo' src='../aws_img/check-no.gif' border=0>").append(tmpShowValue).append("</label>");
        }
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    } finally {
      UIDBSourceUtil.close(source, conn, stmt, rset);
    }
  }

  public String getValueJavaScript(Hashtable params)
  {
    StringBuilder javaScript = new StringBuilder();
    MetaDataMapModel metaDataMapModel = getMetaDataMapModel();
    String rowData = metaDataMapModel.getDisplaySetting();
    if ((rowData.trim().length() > 0) && ((rowData.indexOf("|") > 0) || (rowData.toUpperCase().startsWith("SQL>")))) {
      javaScript.append("\n try{\n var check_")
        .append(metaDataMapModel.getFieldName()).append("=''; \nif (form.")
        .append(metaDataMapModel.getFieldName()).append(".length){\nfor(var i=0;i<form.")
        .append(metaDataMapModel.getFieldName()).append(".length;i++){\nif(form.")
        .append(metaDataMapModel.getFieldName()).append("[i].checked)\ncheck_")
        .append(metaDataMapModel.getFieldName()).append("+=form.").append(metaDataMapModel.getFieldName()).append("[i].value+'|';\n}")
        .append("\n}else{\ncheck_")
        .append(metaDataMapModel.getFieldName()).append("+=form.").append(metaDataMapModel.getFieldName()).append(".value+'|';\n}bv=bv+'_'+'")
        .append(metaDataMapModel.getFieldName()).append("'+'{'+").append("check_").append(metaDataMapModel.getFieldName()).append("+'}'+'").append(metaDataMapModel.getFieldName()).append("'+'_  ';\n }catch(e){}\n");
    }
    else {
      javaScript.append("\n try{ var check_").append(metaDataMapModel.getFieldName()).append("=''; if(form.").append(metaDataMapModel.getFieldName()).append(".checked)check_").append(metaDataMapModel.getFieldName()).append("=form.").append(metaDataMapModel.getFieldName()).append(".value;").append("bv=bv+'_'+'").append(metaDataMapModel.getFieldName()).append("'+'{'+").append("check_").append(metaDataMapModel.getFieldName()).append("+'}'+'").append(metaDataMapModel.getFieldName()).append("'+'_  ';\n }catch(e){}\n");
    }
    return javaScript.toString();
  }

  public String getValidateJavaScript(Hashtable params)
  {
    UserContext me = (UserContext)params.get("me");
    String lang = me != null ? me.getLanguage() : "cn";
    MetaDataMapModel metaDataMapModel = getMetaDataMapModel();
    StringBuilder jsCheckvalue = new StringBuilder();
    String rowData = metaDataMapModel.getDisplaySetting();
    if ((rowData.trim().length() > 0) && (rowData.indexOf("|") > 0))
      jsCheckvalue.append("\n var check_").append(metaDataMapModel.getFieldName()).append("=''; \n")
        .append(" try{  for(var i=0;i<form.").append(metaDataMapModel.getFieldName()).append(".length;i++){\n")
        .append("if(form.").append(metaDataMapModel.getFieldName()).append("[i].checked) {\n")
        .append("check_").append(metaDataMapModel.getFieldName()).append("+=form.").append(metaDataMapModel.getFieldName()).append("[i].value+'|';}\n")
        .append("} \n")
        .append("bv=bv+'_'+'").append(metaDataMapModel.getFieldName()).append("'+'{'+").append("check_").append(metaDataMapModel.getFieldName()).append("+'}'+'").append(metaDataMapModel.getFieldName()).append("'+'_  ';\n ")
        .append("}catch(e){}\n");
    else {
      jsCheckvalue.append("\n var check_").append(metaDataMapModel.getFieldName()).append("=''; try{  if(form.").append(metaDataMapModel.getFieldName()).append(".checked)check_").append(metaDataMapModel.getFieldName()).append("=form.").append(metaDataMapModel.getFieldName()).append(".value;").append("bv=bv+'_'+'").append(metaDataMapModel.getFieldName()).append("'+'{'+").append("check_").append(metaDataMapModel.getFieldName()).append("+'}'+'").append(metaDataMapModel.getFieldName()).append("'+'_  ';\n }catch(e){}\n");
    }

    if (metaDataMapModel.isNotNull()) {
      jsCheckvalue.append("\n try{ if(check_").append(metaDataMapModel.getFieldName()).append(".length==0)").append("{ alert ('<I18N#请至少给字段>:[<I18N#").append(metaDataMapModel.getFieldTitle()).append(">]<I18N#选择一个值，该字段值不允许为空！>'); return false; } }catch(e){}\n\n ");
    }

    if ((!metaDataMapModel.getFieldLenth().equals("0")) && (!metaDataMapModel.getFieldType().equals("LONG"))) {
      if (metaDataMapModel.getFieldLenth().indexOf(",") != -1) {
        int fLength = Integer.parseInt(metaDataMapModel.getFieldLenth().substring(0, metaDataMapModel.getFieldLenth().indexOf(",")));
        int bLength = Integer.parseInt(metaDataMapModel.getFieldLenth().substring(metaDataMapModel.getFieldLenth().indexOf(",") + 1, metaDataMapModel.getFieldLenth().length()));
        int length = fLength - bLength;
        jsCheckvalue.append("\n try{ if(check_").append(metaDataMapModel.getFieldName()).append(".indexOf('.')==-1 && length2(check_").append(metaDataMapModel.getFieldName()).append(")>").append(length).append("){ alert ('" + I18nRes.findValue(lang, "字段:{0}值太长了，此字段最多可输入{1}位数字！", new StringBuilder(String.valueOf(metaDataMapModel.getFieldTitle())).append("|").append(length).toString()) + "');").append("form.").append(metaDataMapModel.getFieldName()).append(".style.border =\"3 dotted green\";").append("form.").append(metaDataMapModel.getFieldName()).append(".focus();").append("form.").append(metaDataMapModel.getFieldName()).append(".select();").append(" return false; } }catch(e){}\n\n ");
        jsCheckvalue.append("\n try{ if(check_").append(metaDataMapModel.getFieldName()).append(".substring(0,check_").append(metaDataMapModel.getFieldName()).append(".indexOf('.')).length>").append(length).append("){ alert ('" + I18nRes.findValue(lang, "字段:{0}值太长了，此字段最多可输入{1}个整数字符{2}个小数字符！", new StringBuilder(String.valueOf(metaDataMapModel.getFieldTitle())).append("|").append(length).append("|").append(bLength).toString()) + "'); return false; } }catch(e){}\n\n ");
      } else {
        jsCheckvalue.append("\n try{ if(length2(check_").append(metaDataMapModel.getFieldName()).append(")>").append(metaDataMapModel.getFieldLenth()).append("){ alert ('" + I18nRes.findValue(lang, "字段:{0}值太长了，此字段最多可输入{1}个字符！", new StringBuilder(String.valueOf(metaDataMapModel.getFieldTitle())).append("|").append(metaDataMapModel.getFieldLenth()).toString()) + "');").append(" return false; } }catch(e){}\n\n ");
      }

    }

    if (metaDataMapModel.getDisplayType().equals("日期")) {
      jsCheckvalue.append("\n try{ if(!IsDate(check_").append(metaDataMapModel.getFieldName()).append("))").append("{ alert ('" + I18nRes.findValue(lang, "字段:_{0}_选择的值是一个非法的日期格式！", metaDataMapModel.getFieldTitle()) + "'); return false; } }catch(e){}\n\n ");
    }
    if (metaDataMapModel.getDisplayType().equals("数值")) {
      jsCheckvalue.append("\n try{ if(!IsNumber(check_").append(metaDataMapModel.getFieldName()).append("))").append("{ alert ('" + I18nRes.findValue(lang, "字段:_{0}_选择的值是一个非法的数字格式！", metaDataMapModel.getFieldTitle()) + "'); return false; } }catch(e){}\n\n ");
    }

    return jsCheckvalue.toString();
  }
  public String getSettingWeb() {
    String displaySql = getMetaDataMapModel().getDisplaySetting();

    boolean sqlType = false;
    String sqlGet = "";
    String sqlShow = "";
    String sqlSelect = "";
    String DBSOURCE = "";
    if ((displaySql != null) && (displaySql.trim().length() > 0)) {
      if (displaySql.toUpperCase().indexOf(">>") > "CASCADE".length()) {
        UtilString util = new UtilString(displaySql);
        Vector v = util.split(">>");
        displaySql = v.get(1).toString();
      }
      if (displaySql.toUpperCase().indexOf("SQL>") == 0) {
        sqlType = true;
        displaySql = displaySql.substring("SQL>".length());
        UtilString util = new UtilString(displaySql);
        Vector v = util.split("|");
        sqlSelect = v.get(1).toString();
        if (v.size() > 2) {
          DBSOURCE = v.get(2).toString();
        }
        util = new UtilString(v.get(0).toString());
        v = util.split(":");
        if (v.size() > 1) {
          sqlShow = v.get(0).toString();
          sqlGet = v.get(1).toString();
        } else {
          sqlGet = v.get(0).toString();
        }
      }
    }

    StringBuilder settingHtml = new StringBuilder();
    settingHtml.append("<tr><td><table  width=\"100%\" align=\"center\"><tr>");
    settingHtml.append(" <td width=\"18%\" nowrap><span onclick=\"visableCombox('upType');\" id=AWS_RADIO>");
    Thread currentThread = Thread.currentThread();
    String ctName = currentThread.getName();
    String lang = ctName.substring(ctName.lastIndexOf("--") + 2);
    settingHtml.append("<input type=\"radio\"  name=\"upType\" value=\"常量参考\" ").append(!sqlType ? "checked" : "").append("/>" + I18nRes.findValue(lang, "常量"));
    settingHtml.append(" <input type=\"radio\" name=\"upType\" value=\"SQL数据\"  ").append(sqlType ? "checked" : "").append("/> " + I18nRes.findValue(lang, "SQL数据") + "</span></td></tr>");
    settingHtml.append("<tr id=\"consTrea\" style=\"display:").append(!sqlType ? "" : "none").append("\">");
    settingHtml.append("<td colspan=\"2\" >");
    settingHtml.append("<input type=text  name='AWS_COMBOX_CONS' onchange=\"creatComboxConfig('0','AWS_COMBOX_CONS');\" id='AWS_COMBOX_CONS' class ='actionsoftInput' value='").append(!sqlType ? displaySql : "").append("' size=70 ></td></tr>");
    settingHtml.append("<tr id=\"consText\" style=\"display:").append(!sqlType ? "" : "none").append("\" onclick=\"insertAtCaret(frmMain.AWS_COMBOX_CONS,'A|B|C|D');creatComboxConfig('0','AWS_COMBOX_CONS');\" onmouseout=\"out_change(this,'#FFFFFF');\" onmouseover=\"over_change(this,'#EBF#F6');\">");
    settingHtml.append("<td colspan=\"2\">" + I18nRes.findValue(lang, "语法格式=[值1:]显示1|[值2:]显示2...") + "</td></tr>");
    settingHtml.append("<tr id=\"sqlTrea\" style=\"display:").append(sqlType ? "" : "none").append("\">");
    settingHtml.append("<td colspan=\"2\" >");
    settingHtml.append("<table>");
    settingHtml.append("<tr><td>" + UIDBSourceUtil.getRadioImplUISettingDBSource(DBSOURCE) + "</td></tr>");
    settingHtml.append("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;" + I18nRes.findValue(lang, "显示字段") + "&nbsp;&nbsp;&nbsp;&nbsp;<input class ='actionsoftInput' type='text'   name='AWS_COMBOX_SQLSHOW' value='").append(sqlShow).append("' id='AWS_COMBOX_SQLSHOW'></td></tr>");
    settingHtml.append("<tr><td>" + I18nRes.findValue(lang, "取值字段名") + "(<font color=red>*</font>)<input class ='actionsoftInput' type='text'   name='AWS_COMBOX_SQLGET' value='").append(sqlGet).append("' id='AWS_COMBOX_SQLGET'></td></tr>");
    settingHtml.append("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;" + I18nRes.findValue(lang, "SQL语句") + "(<font color=red>*</font>)<input type='text'  class ='actionsoftInput'  name='AWS_COMBOX_SQLSELECT' value=\"").append(sqlSelect).append("\" id='AWS_COMBOX_SQLSELECT' size='50'><a href=\"###\" onclick=\"openAnalog(frmMain,'check');return false;\"><img src='../aws_img/admin.gif' title='" + I18nRes.findValue(lang, "模拟使用") + "'>" + I18nRes.findValue(lang, "测试") + "</a></td></tr>");
    settingHtml.append("</table>");
    settingHtml.append("</td></tr>");
    settingHtml.append("</table></td></tr>");
    settingHtml.append("<script>");
    settingHtml.append("function initEditor(){\n");
    settingHtml.append("\tvar radioValues=document.getElementsByName('upType');\n");
    settingHtml.append("\tvar str;\n");
    settingHtml.append("\tvar sqlGet = $('AWS_COMBOX_SQLGET');\n");
    settingHtml.append("\tvar sqlSelect = $('AWS_COMBOX_SQLSELECT');\n");
    settingHtml.append("\tvar con = $('AWS_COMBOX_CONS');\n");
    settingHtml.append("\tfor(var i=0;i<radioValues.length;i++){\n");
    settingHtml.append("\t\tif(radioValues[i].checked==true){\n");
    settingHtml.append("\t\t\tif(radioValues[i].value=='SQL数据'){\n");
    settingHtml.append("\t\t\t\tif(sqlSelect.value.replace(/^(\\s)|(\\s)$/g,'')==''){\n");
    settingHtml.append("\t\t\t\t\tfrmMain.displayEditor.value='';\n");
    settingHtml.append("\t\t\t\t\treturn;\n");
    settingHtml.append("\t\t\t\t}else{\n");
    settingHtml.append("\t\t\t\t\tstr=\"SQL>\";\n");
    settingHtml.append("                    if(document.getElementById('AWS_COMBOX_SQLGET').value==''||document.getElementById('AWS_COMBOX_SQLSELECT').value==''){\n");
    settingHtml.append("                      return;\n}\n");
    settingHtml.append("\t\t\t\t\tstr+=document.getElementById('AWS_COMBOX_SQLSHOW').value+':';\n");
    settingHtml.append("\t\t\t\t\tstr+=document.getElementById('AWS_COMBOX_SQLGET').value+'|';\n");
    settingHtml.append("\t\t\t\t\tstr+=document.getElementById('AWS_COMBOX_SQLSELECT').value+'|';\n");
    settingHtml.append("\t\t\t\t\tstr+=document.getElementById('DBSOURCE').value;\n");
    settingHtml.append("\t\t\t\t}\n");
    settingHtml.append("\t\t\t}else {\n");
    settingHtml.append("\t\t\t\tif(con.value.replace(/^(\\s)|(\\s)$/g,'')==''){frmMain.displayEditor.value='';return;}else{\n");
    settingHtml.append("\t\t\t\t\tstr=con.value;\n");
    settingHtml.append("\t\t\t\t}\n");
    settingHtml.append("\t\t\t}\n");
    settingHtml.append("\t\t}\n");
    settingHtml.append("\t}\n");
    settingHtml.append("\tfrmMain.displayEditor.value=str;\n");
    settingHtml.append("}\n");
    settingHtml.append("</script>\n");
    return settingHtml.toString();
  }
}