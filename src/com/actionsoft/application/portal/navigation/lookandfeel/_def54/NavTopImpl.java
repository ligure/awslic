package com.actionsoft.application.portal.navigation.lookandfeel._def54;

import com.actionsoft.application.portal.navigation.cache.NavigationSystemCache;
import com.actionsoft.application.portal.navigation.lookandfeel.LookAndFeelAbst;
import com.actionsoft.application.portal.navigation.model.NavigationSystemModel;
import com.actionsoft.application.portal.navigation.util.NavUtil;
import com.actionsoft.awf.commons.security.basic.SecurityProxy;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.URLParser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 
 * @description 取消掉def54皮肤，上侧知识库查询的内容。
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:36:27
 */
public class NavTopImpl extends LookAndFeelAbst {
	public NavTopImpl() {
	}

	public NavTopImpl(UserContext paramUserContext, String paramString) {
		super(paramUserContext, paramString);
	}

	public String toString() {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("<div id='topBar' class='system'>");

		Map localMap = NavigationSystemCache.getList();
		Object localObject1 = "";
		int i = 0;
		String str1 = "";
		if (localMap != null) {
			int j = 0;
			for (int k = 0; j < localMap.size(); j++) {
				NavigationSystemModel localObject2 = (NavigationSystemModel) localMap
						.get(new Integer(j));
				if ((localObject2 == null)
						|| (!((NavigationSystemModel) localObject2)._isActivity)) {
					continue;
				}
				if ((!SecurityProxy.checkModelSecurity(getUserContext()
						.getUID(), Integer
						.toString(((NavigationSystemModel) localObject2)._id)))
						|| (((NavigationSystemModel) localObject2)._id < 21)) {
					continue;
				}
				String str2 = URLParser.repleaseNavURL(getUserContext(),
						((NavigationSystemModel) localObject2)._systemUrl);
				String str3 = NavUtil.getLangName(getUserContext()
						.getLanguage(),
						((NavigationSystemModel) localObject2)._systemName);
				if (((NavigationSystemModel) localObject2)._id == 21) {

				}
				String str4 = " onclick=\"openSystem(this,'"
						+ ((NavigationSystemModel) localObject2)._systemIcon
						+ "','" + str2 + "','"
						+ ((NavigationSystemModel) localObject2)._workTarget
						+ "','" + ((NavigationSystemModel) localObject2)._id
						+ "','" + str3 + "')\"";
				if (str2.trim().length() > 3) {
					str4 = "onclick=\"openSystemOuter(this,'"
							+ str2
							+ "','"
							+ ((NavigationSystemModel) localObject2)._systemIcon
							+ "','"
							+ ((NavigationSystemModel) localObject2)._workTarget
							+ "','"
							+ ((NavigationSystemModel) localObject2)._id
							+ "','"
							+ ((NavigationSystemModel) localObject2)._systemName
							+ "')\"";
				}
				if (k > 0) {
					localStringBuffer1
							.append("<img style='margin-top:5px;display:inline-block;float:left' src='../aws_skins/_def54/img/TOP_Separate.gif' border='0' align='absmiddle'>\n");
				}

				// if (k == 0)
				// str1 = "<span id='km'
				// style='width:188px;margin-top:0px;'><span
				// style='width:130px'></span>
				// <img id='hide' src='../aws_skins/_def54/img/hide.png'
				// style='display:none;width:71px;height:22px' align='right'
				// onclick='ahide();'>
				// <img id='show' src='../aws_skins/_def54/img/show.png'
				// style='display:none;width:71px;height:22px'
				// onclick='ashow();'></span>\n";
				// str1 =
				// "<span id='km' style='margin-top:0px;'><s  name=\"searchKey\" id=\"searchKey\" class=\"searchInput\"/><span id='km' style='margin-top:0px;'><img id='hide' src='../aws_skins/_def54/img/hide.png' title='<I18N#知识库搜索>' style='width:71px;height:20px' onclick='hideopen();'></span>\n</span>\n";
				// str1 =
				// "<span id='km' style='margin-top:0px;'><input title='<I18N#知识库搜索>' type=\"text\" name=\"searchKey\" id=\"searchKey\" class=\"searchInput\"/><img id='searchImg' src='../aws_skins/_def54/img/search.png' title='<I18N#知识库搜索>' class='search' onclick='fullSearch();'><img src='../aws_skins/_def54/img/TOP_Separate.gif' border='0'></span>\n";

				// else {
				// / str1 = "";
				// }
				if (((String) localObject1).trim().length() == 0) {
					localObject1 = str3;
					i = ((NavigationSystemModel) localObject2)._id;
					//if (i == 21) {
					if(false){
					String str44 = "onclick=\"openSystem1(this,'/','/','leftFrame','21','首页')\"";
						/**
						 * 增加首页前小房子图片
						 */
						localStringBuffer1
								.append("<a class='home_a' style='align:right;width:30px;cursor:pointer;display:inline-block;'><img  onclick=\"openSystem1(document,'/','/','leftFrame','21','首页')\" src='../aws_skins/_def54/letv/images/in_r17_c7.png' style='height:22px;width:26px;' /></a>");
//										+ str1 + "<a id=\"")//width: auto; height: auto; min-width: 80px; min-height: 35px;
//								.append(((NavigationSystemModel) localObject2)._id)
//								.append("\" style=\"cursor:pointer\" ")
//								.append(str44)
//								//删除首页横向菜单栏鼠标经过、离开事件，使时间不能运行
//								.append("/ Test-onmouseOver=\"mouseOver(this)\" Test-onmouseOut=\"mouseOut(this)\" class=\"select\" >")
//								.append(str3).append("</a>\n");
					} else {
						localStringBuffer1
								.append(str1 + "<a id=\"")
								.append(((NavigationSystemModel) localObject2)._id)
								.append("\" style=\"cursor:pointer;font-family:微软雅黑;\" ")
								.append(str4)
								//删除首页横向菜单栏鼠标经过、离开事件，使时间不能运行
								.append(" Test-onmouseOver=\"mouseOver(this)\" Test-onmouseOut=\"mouseOut(this)\" class=\"select\" >")
								.append(str3).append("</a>\n");

					}
				} else {
					localStringBuffer1
							.append(str1 + "<a id=\"")
							.append(((NavigationSystemModel) localObject2)._id)
							.append("\" style=\"display:inline-block;float:left;cursor:pointer;font-family:微软雅黑;\" ")
							.append(str4)
							//删除首页横向菜单栏鼠标经过、离开事件，使时间不能运行
							.append(" Test-onmouseOver=\"mouseOver(this)\" Test-onmouseOut=\"mouseOut(this)\">")
							.append(str3).append("</a>\n");
				}
				k += 1;
			}
		}
		localStringBuffer1.append("</div>");
		StringBuffer localStringBuffer2 = new StringBuffer();
		Date localDate = new Date();
		Object localObject2 = new SimpleDateFormat("HH:mm:ss");
		String str2 = ((SimpleDateFormat) localObject2).format(localDate);
		localStringBuffer2
				.append("<div class=\"navLeft\" style='float:left;width:60px;display:none;' id='clock'>")
				.append(str2).append("</div>\n");
		localStringBuffer2
				.append("<div class=\"navRight\" style='float:left;'>")
				.append(localStringBuffer1).append("</div>\n");
		localStringBuffer2.append("<script>\n");
		localStringBuffer2.append(" var systemId='").append(i).append("';\n");
		localStringBuffer2.append("</script>\n");
		return (String) (String) localStringBuffer2.toString();
	}
}