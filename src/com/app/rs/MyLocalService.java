package com.app.rs;

import net.sf.json.JSONObject;

import com.app.rs.ppc.impl.PersonPositionChangeImpl;

public class MyLocalService {

	public String positionChange(String tableInfo) {
		System.err.println(tableInfo);
		JSONObject json = new JSONObject();
		System.err.println("json");
		if ("".equals(tableInfo) || null == tableInfo) {
			System.err.println("json=''");
			json.put("errorMsg", "调用addUser方法参数为空");
			json.put("isSuccess", false);
		} else {
			JSONObject obj = null;
			try {
				System.err.println("objnull");
				obj = JSONObject.fromObject(tableInfo);
				System.err.println("obj");
			} catch (Exception e) {
				System.err.println("objerrr");
				json.put("errorMsg", "错误格式的参数集合!");
				json.put("isSuccess", false);
			}
			PersonPositionChangeImpl ppci = new PersonPositionChangeImpl();
			json = ppci.startWorkFlow(obj);
			System.out.println("json.tostring"+json);
		}
		return json.toString();
	}

}
