package com.actionsoft.awf.userworkview.component;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.userworkview.event.UserWorkViewEvent;
import com.actionsoft.i18n.I18nRes;
/**
 * 
 * @description // wangz 2013-12-17修改，将提交改为true，上行为原代码。功能为：“去掉用户视图最上一行div”
 * @version 1.0
 * @author wangz
 * @update 2014-1-6 上午10:36:02
 */
public class Title extends AbstractComponent {
	public Title(UserContext me) {
		super(me);
	}

	public String toString() {
		UserWorkViewEvent.executeCustomizeComponentEvent(getContext(), this);
		// if (!getIsShow()) {
		if (true) {// wangz 2013-12-17修改，将提交改为true，上行为原代码。功能为：“去掉用户视图最上一行div”
			return "<script>try{dojo.byId('UserWorkView_Top_Container').style.display='none';}catch(e){}</script>\n";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<table border='0' cellspacing='0' cellpadding='5'><tr>\n");
		if (getLabel().trim().length() > 0) {
			sb
					.append("<td nowrap='nowrap' valign='top' style='font-size:16px;font-weight:bold;padding:2px 5px 2px 2px;'>");
			sb.append(
					I18nRes.findValue(super.getContext().getLanguage(),
							getLabel())).append("</td>\n");
		}

		sb.append("<td style='padding:2px 5px 2px 2px;'>").append(
				getExtendCode()).append("</td>\n");
		sb.append("</tr>\n</table>\n");
		return sb.toString();
	}
}