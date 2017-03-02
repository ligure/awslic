package com.ligure.aws;

import java.io.FileInputStream;
import java.lang.reflect.Method;

import com.actionsoft.application.server.AWSServerInfo;
import com.actionsoft.application.server.PBE;
import com.actionsoft.application.server.ServerCrypto;
import com.actionsoft.awf.util.Base64;

public class LicenseAna {

    public static void main(String[] args) throws Exception {

	FileInputStream fis = new FileInputStream("d:/license.dat");
	int i = fis.available();
	byte[] dat = new byte[i];
	fis.read(dat);
	fis.close();
	System.out.println(new String(dat));
	Method m = AWSServerInfo.class.getMethod("if", String.class);
	String key1 = m.invoke(new AWSServerInfo(),
		"*9-O)!3;2O\023\030\001\037").toString();
	key1 = "JVM INSTR swap";
	String encData = new String(PBE.decrypt(Base64.decode(dat), key1));
	System.out.println(encData);
	String encData1 = new String(PBEUtil.decrypt(Base64.decode(dat), key1));
	System.out.println(encData1);
	String key2 = "~O~Y~ACTIONSOFT~@~";
	String decDate = new String(PBE.decrypt(
		Base64.decode(encData.getBytes()), key2), "utf-8");
	System.out.println(decDate);
	String decDate1 = new String(PBEUtil.decrypt(
		Base64.decode(encData1.getBytes()), key2), "utf-8");
	System.out
		.println(decDate1
			.equals("ACTIONSOFT 本许可证授权内容由北京炎黄盈动科技发展有限公司颁发，用于授权用户在指定硬件服务器（授权码）部署一套AWS平台，不允许在多台机器上安装，在规定期限或永久使用AWS平台 _licenseVersion[5.0]licenseVersion_ _companyName[乐视网集团]companyName_ _systemName[BPM Solution]systemName_ _version[5.2]version_ _licenseType[0]licenseType_  _versionType[3]versionType_ _isFree[0]isFree_ _maxUser[10000]maxUser_ _invalidDate[2016-12-31]invalidDate_ _series[0]series_ _organizationNumber[100]organizationNumber_ _businessModelNumber[1000]businessModelNumber_ _suiteSecurity[Email_ CmChannel EIP_CoWork_ EIP_Related_ EIP_VOTE2 Info_Department_ Document_Enterprise EAM2_ Asset_ MeetingRoom_ Car_ GOV_ ]suiteSecurity_ _ASP[off]ASP_ _PhysicalAddress[HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYmee6dwXzWWZr7dPrrVQivQ==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYW+AJynk89Qt9scyO7jePtg==,HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA==,HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYqpcqEjmhl91z3NCNYTI0kg==,HuSUu+CaxKoI0XLng/l4HMQD4k2/89HYZOGC/2GyHrl92WCLwMvxXQ==,IBt3Fp6zt0bL/YK6ojPzPsaH/CnVRQ29dtQL99psUWVK845RNZtivQ==]PhysicalAddress_ _CLUSTER[on]CLUSTER_ _FULLSEARCH_FILE[on]FULLSEARCH_FILE_ _SUITE_PLATFORM[on]SUITE_PLATFORM_ _ONLINEOFFICE[on]ONLINEOFFICE_ _COE[off]COE_ _BAM[off]BAM_ _BPA[off]BPA_ _CC[on]CC_ _XBUS[on]XBUS_ _SAM[off]SAM_ _MWP[on]MWP_"));

	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKovZS9/QK+ZYGYvr4t3Fhak8zb6hCUWWMy/HCjZFOCVGA=="));
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKovZS9/QK+ZYF9ddF7rhxkU8zb6hCUWWMx3rhDOubFtbw=="));
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKoI0XLng/l4HOOyUYPw4Zt5yxUfEJijvdnGlgNSGA34zw=="));
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKoI0XLng/l4HOOyUYPw4Zt5NPFITLlHYSX8ezbIUTG9AA=="));
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKoI0XLng/l4HMcAP8lgbRYXnxizzmBqgneHltx+bUPJDQ=="));
	System.out
		.println(new ServerCrypto()
			.decode("HuSUu+CaxKoI0XLng/l4HMcAP8lgbRYXuJcVClH6LVFyf/HOc33j2w=="));

    }

}
