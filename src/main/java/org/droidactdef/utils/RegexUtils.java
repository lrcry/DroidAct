package org.droidactdef.utils;

import java.util.regex.*;

public final class RegexUtils {
	private static Pattern pattern;
	private static Matcher matcher;
	
	/**
	 * 根据正则表达式匹配（必须完全匹配）行中的字符串<br />
	 * 
	 * @param line
	 * @param regex
	 * @return 是否匹配
	 */
	public static boolean matchStringFromLineByRegex(String line, String regex) {
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(line);
		return matcher.matches();
	}

	/**
	 * 根据正则表达式查找行中包含的字符串<br />
	 * 
	 * @param line
	 *            当前行
	 * @param regex
	 *            正则表达式
	 * @return 字符串
	 */
	public static String findStringFromLineByRegex(String line, String regex) {
		pattern = Pattern.compile(regex);
		matcher = pattern.matcher(line);
		String str = new String();
		if (matcher.find()) {
			str = matcher.group();
		}

		return str;
	}
	
}
