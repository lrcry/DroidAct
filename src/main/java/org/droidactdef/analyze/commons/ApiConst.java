package org.droidactdef.analyze.commons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.droidactdef.commons.C;

/**
 * 关于Android API的常量<br />
 * 
 * @author range
 * 
 */
public class ApiConst {
	/*
	 * 
	 * Regex
	 */
	public static final String REGEX_L_ANDROID = "Landroid/";
	public static final String REGEX_L_COM_ANDROID = "Lcom/android/";

	public static final String REGEX_ANDROID_API = "((" + REGEX_L_ANDROID + ")" + "|"
			+ "(" + REGEX_L_COM_ANDROID + "))" + C.PTN_METHOD;

	// 匹配单行是否为方法调用
	public static final String REGEX_ANDROID_API_INVOKE = C.PTN_METHOD_INVOKE
			+ " " + C.PTN_METHOD_REGS + ", " + REGEX_ANDROID_API;
	/*
	 * 
	 * SQL
	 */

	/*
	 * 
	 * Keys of Maps
	 */
	public static final String SUPERCLAZZ_SERVICE = "Landroid/app/Service;";
	public static final String SUPERCLAZZ_ACTIVITY = "Landroid/app/Activity";

	/**
	 * A test of regex<br />
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String str = "invoke-virtual {p0}, Lcom/android/telephony/TelephonyManager;->getDeviceId()Ljava/lang/String;";
		String str2 = "invoke-virtual {v0, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;";
		String strCast = "check-cast v0, Landroid/telephony/TelephonyManager;";
		String strNonApi = "invoke-virtual {v0}, Ljava/lang/String;->getBytes()[B";
		Pattern ptn = Pattern.compile(REGEX_ANDROID_API);
		Matcher mch = ptn.matcher(strCast);
//		System.out.println(REGEX_ANDROID_API);
		if (mch.find())
			System.out.println(mch.group());
	}
}
