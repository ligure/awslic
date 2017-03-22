package com.app.rs.util;

import java.sql.Connection;
import java.sql.SQLException;

import com.actionsoft.awf.util.DBSql;

public class Utils {
	
	/**
	 * 检测是否是有效的AWS账户
	 * @param userId AWS账户
	 * @return 是/否
	 */
	public static boolean checkUser(String userId){
		Connection conn = null;
		try{
			conn = DBSql.open();
			String sql = "select count(id) cnt from orguser where userid='"+userId+"'";
			int cnt = DBSql.getInt(conn, sql, "CNT");
			if(cnt == 0){
				return false;
			}else{
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, null, null);
		}
		return false;
	}
	
	/**
	 * 根据用户编号获取用户账户
	 * @param userNo 用户编号
	 * @return 
	 */
	public static String getUserIdByNo(String userNo){
		Connection conn = null;
		try{
			conn = DBSql.open();
			String sql = "select userId from orguser where userno='"+userNo+"'";
			String userId = DBSql.getString(conn, sql, "USERID");
			return userId;
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			DBSql.close(conn, null, null);
		}
		return "";
	}
	
}
