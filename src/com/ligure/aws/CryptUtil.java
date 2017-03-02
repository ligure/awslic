package com.ligure.aws;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 一般加密解密工具<br>
 * DES/DESede/AES/BlowFish<br>
 * DES的密钥(key)长度为8个字节,加密解密的速度是最快的,容易破解<br>
 * DESede(Triple DES、三重DES)是DES的加强版,密钥(key)长度是24个字节<br>
 * AES的密钥(key)长度为16个字节,加密速度、强度都好于DES以及DESede<br>
 * BlowFish的密钥(key)长度是可变的(1<=key<=16),加密最快,强度最高<br>
 */
public class CryptUtil {

    public final static String DES = "DES";
    public final static String DESEDE = "DESede";
    public final static String AES = "AES";
    public final static String BLOWFISH = "BlowFish";
    public final static String MD5 = "MD5";

    /**
     * md5加密
     * 
     * @param plainText
     * @return
     */
    public static String getMD5(String plainText) {
	return null == plainText ? "" : getMD5(string2byte(plainText));
    }

    private static String getMD5(byte str[]) {
	try {
	    MessageDigest md = MessageDigest.getInstance(MD5);
	    md.update(str);
	    byte b[] = md.digest();
	    return byte2hex(b);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "";
    }

    /**
     * 字符串加密
     * 
     * @param data
     *            字符串数据
     * @param key
     *            密钥
     * @param name
     *            算法名称
     * @throws Exception
     */
    public static String encrypt(String data, String key, String name,
	    String mode) {
	try {
	    return byte2hex(encrypt(string2byte(data), string2byte(key), name,
		    mode));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "";
    }

    /**
     * 字符串解密
     * 
     * @param data
     *            字符串加密数据
     * @param key
     *            密钥
     * @param name
     *            算法名称
     * @return
     * @throws Exception
     */
    public static String decrypt(String data, String key, String name,
	    String mode) {
	try {
	    return byte2string(decrypt(hex2byte(string2byte(data)),
		    string2byte(key), name, mode));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "";
    }

    /**
     * 对数据源进行加密
     * 
     * @param src
     *            数据源
     * @param key
     *            密钥
     * @param name
     *            算法的名称
     * @return 返回加密后的数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] src, byte[] key, String name,
	    String mode) throws Exception {
	Cipher cipher = null;
	byte iv[] = new byte[8];
	SecretKey securekey = new SecretKeySpec(key, name);
	if ("CBC".equalsIgnoreCase(mode)) {
	    cipher = Cipher.getInstance(name + "/CBC/PKCS5Padding");
	    if (DESEDE.equals(name)) {
		System.arraycopy(key, 0, iv, 0, 8);
	    } else if (BLOWFISH.equals(name)) {
		if (key.length > 7) {
		    System.arraycopy(key, 0, iv, 0, 8);
		} else {
		    System.arraycopy(key, 0, iv, 0, key.length);
		}
	    } else {
		iv = key;
	    }
	    AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
	    cipher.init(Cipher.ENCRYPT_MODE, securekey, paramSpec);
	} else {
	    cipher = Cipher.getInstance(name);
	    cipher.init(Cipher.ENCRYPT_MODE, securekey);
	}
	return cipher.doFinal(src);
    }

    /**
     * 对加密的数据源进行解密
     * 
     * @param src
     *            数据源
     * @param key
     *            密钥
     * @param name
     *            算法的名称
     * @return 返回解密后的原始数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] src, byte[] key, String name,
	    String mode) throws Exception {
	Cipher cipher = null;
	byte iv[] = new byte[8];
	SecretKey securekey = new SecretKeySpec(key, name);
	if ("CBC".equalsIgnoreCase(mode)) {
	    cipher = Cipher.getInstance(name + "/CBC/PKCS5Padding");
	    if (DESEDE.equals(name)) {
		System.arraycopy(key, 0, iv, 0, 8);
	    } else if (BLOWFISH.equals(name)) {
		if (key.length > 7) {
		    System.arraycopy(key, 0, iv, 0, 8);
		} else {
		    System.arraycopy(key, 0, iv, 0, key.length);
		}
	    } else {
		iv = key;
	    }
	    AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
	    cipher.init(Cipher.DECRYPT_MODE, securekey, paramSpec);
	} else {
	    cipher = Cipher.getInstance(name);
	    cipher.init(Cipher.DECRYPT_MODE, securekey);
	}
	return cipher.doFinal(src);
    }

    /**
     * 二进制转十六进制
     * 
     * @param bytes
     * @return
     */
    private static String byte2hex(byte[] bytes) {
	String hex = "";
	if (bytes != null) {
	    final int size = bytes.length;
	    if (size > 0) {
		String tmp;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
		    tmp = (java.lang.Integer.toHexString(bytes[i] & 0XFF));
		    if (tmp.length() == 1) {
			sb.append("0" + tmp);
		    } else {
			sb.append(tmp);
		    }
		}
		hex = sb.toString().toUpperCase();
	    }
	}
	return hex;
    }

    /**
     * 十六进制转二进制
     * 
     * @param bytes
     * @return
     */
    private static byte[] hex2byte(byte[] bytes) {
	if ((bytes.length % 2) != 0) {
	    return null;
	}
	String item;
	byte[] result = new byte[bytes.length / 2];
	for (int i = 0; i < bytes.length; i += 2) {
	    item = new String(bytes, i, 2);
	    result[i / 2] = (byte) Integer.parseInt(item, 16);
	}
	return result;
    }

    /**
     * 把字符串转换成 Unicode Bytes.
     * 
     * @param s
     *            String
     * @return byte[]
     */
    private static byte[] string2byte(String s) {
	byte[] bytes = null;
	if (s != null) {
	    try {
		bytes = s.getBytes("utf-8");
	    } catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	    }
	}
	return bytes;
    }

    /**
     * 根据 Unicode Bytes 构造字符串.
     * 
     * @param bytes
     *            byte[]
     * @return String
     */
    private static String byte2string(byte[] bytes) {
	String s = null;
	if (bytes != null) {
	    try {
		s = new String(bytes, "utf-8");
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return s;
    }

}
