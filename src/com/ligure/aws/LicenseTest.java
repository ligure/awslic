package com.ligure.aws;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Vector;

import sun.misc.BASE64Encoder;

import com.actionsoft.application.server.AWSServerInfo;
import com.actionsoft.application.server.PBE;
import com.actionsoft.application.server.PBEException;
import com.actionsoft.application.server.ServerBaseCode;
import com.actionsoft.application.server.ServerBindInfo;
import com.actionsoft.application.server.ServerCrypto;
import com.actionsoft.application.server.ServerInfoTools;
import com.actionsoft.application.server.ShutdownServer;
import com.actionsoft.awf.util.Base64;
import com.actionsoft.awf.util.UtilString;

public class LicenseTest {

    public static void main(String[] args) throws Exception {
	System.out.println(ShutdownServer
		.ALLATORI_DEMO("n\013n\006x\0373\np\036")); // system.xml

	Method m = AWSServerInfo.class.getMethod("if", String.class);
	// AWS BPMS Solution-Evaluation
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"!83O\"?-<@<\017\003\025\033\t\000\016B%\031\001\003\025\016\024\006\017\001"));
	// Email_ CmChannel EIP_CoWork_ EIP_Related_ EIP_VOTE2 Info_Department_
	// Document_Enterprise Document_Enterprise FM_ EAM2_ Asset_ SFA2_ SFA_
	// QIANBAO_ Document_Enterprise MeetingRoom_ GOV_
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"*\r\016\t\003?O#\002#\007\001\001\016\n\fO%&00#\0007\000\022\004?O%&002\n\f\016\024\n\0040@*)??9/;%]@&\016\t\0170$\n\020\016\022\033\r\n\016\033?O$\000\003\032\r\n\016\033?*\016\033\005\035\020\035\t\034\005O$\000\003\032\r\n\016\033?*\016\033\005\035\020\035\t\034\005O&\"?O%.-]?O!\034\023\n\0240@<&.R0@<&.?O1&!!\"./0@+\017\f\025\002\005\001\0240%\001\024\n\022\037\022\006\023\n@\"\005\n\024\006\016\b2\000\017\002?O' 60"));
	// PBEWithSHAAndTwofish-CBC SecretKeyFactory not available
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"?\"*7\006\024\0073'!.\016\0134\030\017\t\t\034\bB#-#O3\n\003\035\005\033+\n\031)\001\f\024\000\022\026@\001\017\033@\016\026\016\t\003\001\r\f\n"));
	// 当前JRE的设置不符合AWS安全要求，建议使用安装盘自带的JDK或为以下JRE设置相关安全选项
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"\u5F3C\u522D%2*\u76E4\u8BD1\u7F0E\u4E62\u7B46\u5467!83\u5BE6\u5108\u89EE\u6C22\uFF63\u5E9A\u8BC1\u4F1F\u7547\u5BE9\u88AA\u76B8\u8185\u5E46\u76EB*++\u6279\u4E5A\u4E8A\u4E6B%2*\u8BDE\u7F01\u7698\u511C\u5BE9\u5107\u9069\u9816"));
	// javax.crypto.IllegalBlockSizeException
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"\n\016\026\016\030A\003\035\031\037\024\000N&\f\003\005\b\001\003\"\003\017\f\013<\t\025\005*\030\f\005\037\024\006\017\001"));
	// 许可证文件[license.dat]格式可能已损坏，请与供应商联系
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"\u8BD7\u538F\u8BAE\u65E7\u4E99;\003\t\f\005\001\023\nN\013\001\033=\u6853\u5F6F\u5380\u809D\u5D9D\u633F\u5720\uFF6C\u8B98\u4E6E\u4FF4\u5EF4\u5529\u8034\u7C94"));
	// u5317u4eacu708eu9ec4u76c8u52a8u79d1u6280u53d1u5c55u6709u9650u8d23u4efbu516cu53f8
	System.out
		.println(m
			.invoke(new AWSServerInfo(),
				"\025ZS^W\032T\n\001\f\025XPW\005\032Y\n\003[\025XV\fX\032U]\001W\025XY\013Q\032V]X_\025ZS\013Q\032U\fUZ\025YW_Y\032YYU_\025W\004]S\032T\n\006\r\025ZQY\003\032U\\\006W"));
	// Actionsoft Co.,Ltd
	System.out.println(m.invoke(new AWSServerInfo(),
		"!\f\024\006\017\001\023\000\006\033@,\017AL#\024\013"));
	// CopyRight(C)2001-2013
	System.out.println(m.invoke(new AWSServerInfo(),
		",\017\037\031=\t\b\b\033H,I]P_QBR_Q\\"));
	// ~O~Y~ACTIONSOFT~@~
	System.out.println(new String(Base64.decode(m
		.invoke(new AWSServerInfo(),
			"\006\004YD77U-1^2%4_U;4_::\006\004\"D").toString()
		.getBytes())));
	// license.dat
	System.out.println(new String(Base64.decode(m
		.invoke(new AWSServerInfo(), "\002(\f\005:8U\025:<U\004971R")
		.toString().getBytes())));
	System.out.println("------------------------------------------------");
	FileInputStream fis = new FileInputStream("license.dat");
	int i = fis.available();
	byte[] dat = new byte[i];
	fis.read(dat);
	fis.close();
	System.out.println(new String(dat));
	String key1 = m.invoke(new AWSServerInfo(),
		"*9-O)!3;2O\023\030\001\037").toString();
	key1 = "JVM INSTR swap";
	String encData = new String(PBE.decrypt(Base64.decode(dat), key1));
	System.out.println(encData);
	String key2 = "~O~Y~ACTIONSOFT~@~";
	String decDate = new String(
		PBE.decrypt(Base64.decode(PBEUtil.decrypt(Base64.decode(dat),
			key1)), key2), "utf-8");
	System.out.println(decDate);
	System.out.println("------------------------------------------------");
	String data = "ACTIONSOFT 本许可证授权内容由北京炎黄盈动科技发展有限公司颁发，用于授权用户在指定硬件服务器（授权码）部署一套AWS平台，不允许在多台机器上安装，在规定期限或永久使用AWS平台 _licenseVersion[5.0]licenseVersion_ _companyName[乐视网集团(开发)]companyName_ _systemName[BPM Solution]systemName_ _version[5.2]version_ _licenseType[0]licenseType_  _versionType[3]versionType_ _isFree[0]isFree_ _maxUser[10000]maxUser_ _invalidDate[2015-08-11]invalidDate_ _series[0]series_ _organizationNumber[100]organizationNumber_ _businessModelNumber[1000]businessModelNumber_ _suiteSecurity[Email_ CmChannel EIP_CoWork_ EIP_Related_ EIP_VOTE2 Info_Department_ Document_Enterprise FM_ EAM2_ Asset_ SFA2_ SFA_ MeetingRoom_ Car_ GOV_ ]suiteSecurity_ _ASP[off]ASP_ _PhysicalAddress[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYmee6dwXzWWZr7dPrrVQivQ==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYW+AJynk89Qt9scyO7jePtg==,HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA==,HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYqpcqEjmhl91z3NCNYTI0kg==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ==   1.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYmee6dwXzWWZr7dPrrVQivQ==]......(ok) 2.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYW+AJynk89Qt9scyO7jePtg==]......(ok) 3.[HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA==]......(ok) 4.[HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw==]......(ok) 5.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYqpcqEjmhl91z3NCNYTI0kg==]......(ok) 6.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ==]......(ok)]PhysicalAddress_ _CLUSTER[on]CLUSTER_ _FULLSEARCH_FILE[on]FULLSEARCH_FILE_ _SUITE_PLATFORM[on]SUITE_PLATFORM_ _ONLINEOFFICE[on]ONLINEOFFICE_ _COE[off]COE_ _BAM[off]BAM_ _BPA[off]BPA_ _CC[on]CC_ _XBUS[on]XBUS_ _SAM[off]SAM_ _MWP[on]MWP_";
	String edat = new String(Base64.encode(PBE.encrypt(data.getBytes(),
		key2)), "utf-8");
	String license = new String(Base64.encode(PBE.encrypt(edat.getBytes(),
		key1)), "utf-8");
	System.out.println(license);
	Vector<String> v = new UtilString(
		"HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYmee6dwXzWWZr7dPrrVQivQ==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYW+AJynk89Qt9scyO7jePtg==,HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA==,HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYqpcqEjmhl91z3NCNYTI0kg==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ==,IBt3Fp6zt0bL/YK6ojPzPsaH/CnVRQ29sVTvKKgMEGdK845RNZtivQ==   1.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYmee6dwXzWWZr7dPrrVQivQ==]......(ok) 2.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYW+AJynk89Qt9scyO7jePtg==]......(ok) 3.[HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA==]......(ok) 4.[HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw==]......(ok) 5.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYqpcqEjmhl91z3NCNYTI0kg==]......(ok) 6.[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ==]......(ok)")
		.split(",");
	for (String s : v) {
	    ServerBindInfo serverbindinfo = ServerBindInfo
		    .buildFromCryptoBingInfo(s.trim());
	    System.out.println(serverbindinfo.getOs());
	    System.out.println(serverbindinfo.getArch());
	    System.out.println(serverbindinfo.getIp());
	    System.out.println(serverbindinfo.getMac());
	    if (serverbindinfo.equals(ServerInfoTools
		    .buildFromIp(serverbindinfo.getIp())))
		break;
	}
	Method m1 = PBEException.class.getMethod("if", String.class);
	// 服务器mac地址使用DESede算法进行加密
	System.out.println(m1.invoke(new PBEException(), "p\033g;P;"));
	// 服务器mac地址使用DESede算法进行加密时使用的秘钥27jrWz2sxrVbR+pnyg6jWHhgNk4sZo46
	System.out.println(m1.invoke(new PBEException(),
		"\006i^,c$\006-L,b<fuD0M9\0024c\026\\9z5\000-n1\000h"));
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ=="));
	byte[] desb = Base64
		.decode("HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ=="
			.getBytes());
	System.out.println(new String(CryptUtil.decrypt(desb,
		ServerBaseCode.decode("27jrWz2sxrVbR+pnyg6jWHhgNk4sZo46"),
		"DESede", "ECB")));

	System.out.println(new ServerBindInfo("Linux", "amd64",
		"10.182.100.26", "005056961147").getCryptoBindInfo());
	String nul = new String(new char[] { (int) 0 });
	String bindData = "Linux" + nul + "amd64" + nul + "10.182.100.26" + nul
		+ "005056961147";
	System.out.println(new ServerCrypto().encode(bindData));

	byte[] encb = CryptUtil.encrypt(bindData.getBytes(),
		ServerBaseCode.decode("27jrWz2sxrVbR+pnyg6jWHhgNk4sZo46"),
		"DESede", "ECB");
	System.out.println(new String(Base64.encode(encb)));
	System.out.println(new String(new BASE64Encoder().encode(encb)));
	System.out.println(new String(ServerBaseCode.encodeBytes(encb)));

    }
}
