package com.ligure.aws;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import sun.misc.BASE64Decoder;

public class PBEUtil {

    private static final String KEY_PRE = "##Actionsoft Co.,Ltd##^2003^";
    private static final String ALGORITHM = "PBEWithSHAAndTwofish-CBC";
    private static final String SALT = "salt";

    public static byte[] encrypt(byte data[], String key) throws Exception {
	// 实例化秘钥工厂
	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
	// 密钥材料
	PBEKeySpec keySpec = new PBEKeySpec((KEY_PRE + key).toCharArray());
	// 生成密钥
	SecretKey secretKey = keyFactory.generateSecret(keySpec);
	// 盐
	PBEParameterSpec paramSpec = new PBEParameterSpec(
		new BASE64Decoder().decodeBuffer(SALT), 1000);
	// 实例化加密算法
	Cipher cipher = Cipher.getInstance(ALGORITHM);

	cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
	return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte data[], String key) throws Exception {
	// 实例化秘钥工厂
	SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
	// 密钥材料
	PBEKeySpec keySpec = new PBEKeySpec((KEY_PRE + key).toCharArray());
	// 生成密钥
	SecretKey secretKey = keyFactory.generateSecret(keySpec);
	// 盐
	PBEParameterSpec paramSpec = new PBEParameterSpec(
		new BASE64Decoder().decodeBuffer(SALT), 1000);
	// 实例化加密算法
	Cipher cipher = Cipher.getInstance(ALGORITHM);

	cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
	return cipher.doFinal(data);
    }

}
