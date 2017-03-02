package com.ligure.aws;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import sun.misc.BASE64Encoder;

import com.actionsoft.application.server.PBE;
import com.actionsoft.application.server.PBEException;
import com.actionsoft.application.server.ServerBaseCode;
import com.actionsoft.application.server.ServerBindInfo;
import com.actionsoft.application.server.ServerCrypto;
import com.actionsoft.application.server.ServerInfoTools;
import com.actionsoft.awf.util.Base64;

public class LicenseGen {

    public static void main(String[] args) throws Exception {

	Method m1 = PBEException.class.getMethod("if", String.class);
	// 服务器mac地址使用DESede算法进行加密
	System.out.println(m1.invoke(new PBEException(), "p\033g;P;"));
	// 服务器mac地址使用DESede算法进行加密时使用的秘钥27jrWz2sxrVbR+pnyg6jWHhgNk4sZo46
	System.out.println(m1.invoke(new PBEException(),
		"\006i^,c$\006-L,b<fuD0M9\0024c\026\\9z5\000-n1\000h"));
	
	System.out.println("********************************************************");
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ=="));
	byte[] desb = Base64
		.decode("HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ=="
			.getBytes());
	System.out.println(new String(CryptUtil.decrypt(desb,
		ServerBaseCode.decode("27jrWz2sxrVbR+pnyg6jWHhgNk4sZo46"),
		"DESede", "ECB")));

	System.out.println("********************************************************");
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

	System.out.println("********************************************************");
	System.out.println(ServerInfoTools.buildFromIp("10.58.125.70"));
	System.out.println("********************************************************");
	System.out.println(ServerInfoTools.buildFromIp("10.58.125.70")
		.getCryptoBindInfo());
	System.out.println(new ServerBindInfo("WINDOWS", "amd64",
		"10.58.125.70", "38B1DBA5BF1F").getCryptoBindInfo());

	Set<String> physicalAddress = new HashSet<String>();
	physicalAddress
		.add("HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA==");
	physicalAddress
		.add("HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw==");
	physicalAddress
		.add("HuSUu+CaxKoI0XLng/l4HOOyUYPw4Zt5yxUfEJijvdnGlgNSGA34zw==");
	physicalAddress
		.add("HuSUu+CaxKoI0XLng/l4HOOyUYPw4Zt5NPFITLlHYSX8ezbIUTG9AA==");
	physicalAddress
		.add("HuSUu+CaxKoI0XLng/l4HMcAP8lgbRYXnxizzmBqgneHltx+bUPJDQ==");
	physicalAddress
		.add("HuSUu+CaxKoI0XLng/l4HMcAP8lgbRYXuJcVClH6LVFyf/HOc33j2w==");
	physicalAddress.add(ServerInfoTools.buildFromIp("10.57.129.89")
		.getCryptoBindInfo());

	String key1 = "JVM INSTR swap";
	String key2 = "~O~Y~ACTIONSOFT~@~";
	String data = "ACTIONSOFT 本许可证授权内容由北京炎黄盈动科技发展有限公司颁发，用于授权用户在指定硬件服务器（授权码）部署一套AWS平台，不允许在多台机器上安装，在规定期限或永久使用AWS平台 _licenseVersion[5.0]licenseVersion_ _companyName[乐视网信息技术（北京）股份有限公司(评估)]companyName_ _systemName[BPM Solution]systemName_ _version[5.2]version_ _licenseType[0]licenseType_  _versionType[3]versionType_ _isFree[0]isFree_ _maxUser[10000]maxUser_ _invalidDate[2016-12-31]invalidDate_ _series[0]series_ _organizationNumber[100]organizationNumber_ _businessModelNumber[2000]businessModelNumber_ _suiteSecurity[Email_ CmChannel EIP_CoWork_ EIP_Related_ EIP_VOTE2 Info_Department_ Document_Enterprise HRM_ EHR_ HRM2_ FM_ EAM2_ Asset_ SFA2_ SFA_ MeetingRoom_ Car_ GOV_ ]suiteSecurity_ _ASP[off]ASP_ _PhysicalAddress"
		+ physicalAddress
		+ "PhysicalAddress_ _CLUSTER[on]CLUSTER_ _FULLSEARCH_FILE[on]FULLSEARCH_FILE_ _SUITE_PLATFORM[on]SUITE_PLATFORM_ _ONLINEOFFICE[on]ONLINEOFFICE_ _COE[off]COE_ _BAM[off]BAM_ _BPA[off]BPA_ _CC[on]CC_ _XBUS[on]XBUS_ _SAM[off]SAM_ _MWP[on]MWP_";
	String license = new String(Base64.encode(PBE.encrypt(
		Base64.encode(PBE.encrypt(data.getBytes(), key2)), key1)),
		"utf-8");
	BufferedWriter bw = new BufferedWriter(new FileWriter("license.dat"));
	bw.write(license);
	bw.flush();
	bw.close();
    }
}
