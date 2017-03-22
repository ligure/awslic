package com.actionsoft;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 
 * @description 修改平台源码时所需的公共类
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:48:11
 */
public class Util {
	/**
	 * 进行MD5加密
	 * @param str
	 * @return
	 */
	public static String getMD5Str(String str) {  
         MessageDigest messageDigest = null;  
        try {  
             messageDigest = MessageDigest.getInstance("MD5");  
             messageDigest.reset();  
             messageDigest.update(str.getBytes("UTF-8"));  
         } catch (NoSuchAlgorithmException e) {  
             System.out.println("NoSuchAlgorithmException caught!");  
             System.exit(-1);  
         } catch (UnsupportedEncodingException e) {  
             e.printStackTrace();  
         }  
        byte[] byteArray = messageDigest.digest();  
         StringBuffer md5StrBuff = new StringBuffer();  
        for (int i = 0; i < byteArray.length; i++) {              
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
                 md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
            else  
                 md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
         }  
        return md5StrBuff.toString();  
     }
	/**
	 * 
	 * @return
	 * @description 获取当前时间
	 * @version 1.0
	 * @author wangaz
	 * @update 2014-1-6 上午10:49:56
	 */
	 public static String getdatetime(){
			 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			 String date = df.format(new Date());
			 return date;
	    }
	/**
	 * 
	 * @param user_time
	 * @return
	 * @description 获取传进来的时间戳
	 * @version 1.0
	 * @author wangaz
	 * @update 2014-1-6 上午10:49:42
	 */
	 public static String getTime(String user_time) {
	 String re_time = null;
	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 Date d;
	 try {
	 d = sdf.parse(user_time);
	 long l = d.getTime();
	 String str = String.valueOf(l);
	 re_time = str.substring(0, 10);
	 } catch (Exception e) {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
	 }
	 return re_time;
	 }
/**
 * 
 * @return
 * @description 获取服务的IP地址
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:50:30
 */
	 public static String getIpAddress(){ 
		 InetAddress address;
		try {
			address = InetAddress.getLocalHost();
		     return address.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return"";
	 } 
/**
 * 
 * @param time
 * @return
 * @description 将时间戳转换为时间
 * @version 1.0
 * @author wangaz
 * @update 2014-1-6 上午10:50:52
 */
	 public static String getsjc(long time){
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 String date = df.format(new Date(time));
		 return date;
    }
//	 public static void main(String []args){
//		 System.out.println(getIpAddress());
//	 }
//	 
}
