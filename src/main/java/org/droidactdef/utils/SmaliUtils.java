package org.droidactdef.utils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.droidactdef.commons.C;
import org.droidactdef.domains.SmaliField;
import org.droidactdef.domains.SmaliMethod;

/**
 * Smali文件解析工具类<Br />
 * 
 * @author range
 * 
 */
public class SmaliUtils {
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * 获取类的成员变量<br />
	 * 
	 * @param lines
	 *            从文件中读取的行列表
	 * @return 成员变量列表
	 */
	public static List<SmaliField> getSmaliFieldList(List<String> lines) {
		List<SmaliField> fields = new ArrayList<SmaliField>();

		for (Iterator<String> it = lines.listIterator(); it.hasNext();) {
			String line = it.next();
			if (line.startsWith(C.FIELD_START)) {
				line = StringUtils.substringAfterLast(line, C.SPACE_BEFORE);
				String[] lineArr = line.split(C.FIELD_SPLITER);
				if (lineArr != null && lineArr.length > 0) {
					SmaliField field = new SmaliField();
					field.setVarName(lineArr[0]);
					field.setVarType(lineArr[1]);
					fields.add(field);
				}
			}
		}

		return fields;
	}

	/**
	 * 获取类的相关信息<br />
	 * 可获取类的名称，类的超类，类的Java源文件名称<br />
	 * 
	 * @param lines
	 *            从文件中读取的行列表
	 * @return <attrName, attrValue> 类信息
	 */
	public static Map<String, String> getSmaliClazzInfo(List<String> lines) {
		Map<String, String> clazzInfo = new HashMap<String, String>();
		String clazzName = "";
		String siuperClazzName = "";
		String inderfazeName = "";
		String sourceName = "";

		for (Iterator<String> it = lines.listIterator(); it.hasNext();) {
			String line = it.next();
			if (line.startsWith(C.CLAZZ_START)) { // Class name
				clazzName = StringUtils
						.substringAfterLast(line, C.SPACE_BEFORE);
				clazzInfo.put(C.MAP_CLAZZ, clazzName);
			} else if (line.startsWith(C.SUPER_START)) { // Super class
															// name
				siuperClazzName = StringUtils.substringAfterLast(line,
						C.SPACE_BEFORE);
				clazzInfo.put(C.MAP_SUPER, siuperClazzName);
			} else if (line.startsWith(C.INTERFACE_START)) {
				inderfazeName = StringUtils.substringAfterLast(line,
						C.SPACE_BEFORE);
				clazzInfo.put(C.MAP_INTERFACE, inderfazeName);
			} else if (line.startsWith(C.SRC_START)) { // Source file name
				sourceName = StringUtils.substringBetween(line,
						C.SRC_NAME_START, C.SRC_NAME_END);
				clazzInfo.put(C.MAP_SOURCE, sourceName);
			}
		}

		return clazzInfo;
	}

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("UpdateCheck.smali"),
				C.FILE_ENCODING);
		Map<String, String> clazzMap = getSmaliClazzInfo(lines);
		List<List<String>> methodList = getSmaliMethodList(lines);
		List<String> smtdBeanList = new ArrayList<>();
		for (List<String> method : methodList) {
			List<String> mtd = getMethodInfoAsString(method,
					clazzMap.get(C.MAP_CLAZZ), clazzMap.get(C.MAP_SUPER),
					clazzMap.get(C.MAP_INTERFACE), "", "", "", "");
			smtdBeanList.addAll(mtd);
		}

		for (String smtd : smtdBeanList) {
			System.out.println(smtd);
		}
	}

	/**
	 * 获取方法段落供解析<br />
	 * 
	 * @param lines
	 *            从文件中读取的行列表
	 * @return smali方法列表<String>
	 * @throws IOException
	 */
	public static List<List<String>> getSmaliMethodList(List<String> lines)
			throws IOException {
		List<List<String>> methods = new ArrayList<>();
		boolean inMethod = false;

		List<String> mtd = new ArrayList<>();

		for (Iterator<String> it = lines.listIterator(); it.hasNext();) {
			String line = it.next();
			if (line.startsWith(C.METHOD_START)) { // 函数开始，获取函数名，函数参数列表，函数返回值
				line = StringUtils.substringAfter(line, C.METHOD_START);
				mtd.add(line);

				// Enter the method
				inMethod = true;
			} else if (line.equals(C.METHOD_END)) { // 函数结尾 ，将函数添加入列表
				methods.add(mtd);
				// Exit the method
				inMethod = false;
				mtd = new ArrayList<>();
			} else if (inMethod) { // 在函数体中
				// Append method body
				mtd.add(line);
			}
		}

		return methods;
	}

	/**
	 * 解析方法段落并存入SmaliMethod类型的方法Bean<br />
	 * 
	 * @param method
	 * @param clazzName
	 * @param suberClazzName
	 * @param interfazeName
	 * @param srcApkName
	 * @return
	 */
	public static SmaliMethod getMethodInfo(List<String> method,
			String clazzName, String suberClazzName, String interfazeName,
			String srcApkName) {
		int lineCount = method.size();
		String nameLine = method.get(0);
		// System.out.println(nameLine);
		SmaliMethod mtdBean = new SmaliMethod();
		String[] lineArr = nameLine.split(" ");

		String name = lineArr[lineArr.length - 1];
		StringBuilder sb = new StringBuilder(clazzName);
		sb.append("->").append(name);
		String mtdName = sb.toString();
		mtdBean.setName(mtdName);
		mtdBean.setSuberClazz(suberClazzName);
		mtdBean.setInterfaze(interfazeName);
		mtdBean.setRetType(StringUtils.substringAfter(mtdName, ")"));
		mtdBean.setArgList(StringUtils.substringBetween(mtdName, "(", ")"));
		mtdBean.setSrcApkName(srcApkName);
		if (lineCount == 1) { // abstract方法、native方法、或空实现的方法
			Arrays.sort(lineArr);
			int existAbstract = Arrays.binarySearch(lineArr,
					C.MODIFIER_ABSTRACT);
			if (existAbstract < 0) {
				int existNative = Arrays.binarySearch(lineArr,
						C.MODIFIER_NATIVE);
				if (existNative < 0) { // 空实现的方法
					mtdBean = null; // 存数据库时，若为空，则丢弃
				} else {
					mtdBean.setNative(true);
					// System.out.println("native");
				}
			} else {
				mtdBean.setAbstract(true);
				// System.out.println("abstract");
			}
		} else { // 提取方法体，方法体的分段和字段划分在存入数据库之前做
			List<String> bodyLines = new ArrayList<>();
			for (int i = 1; i < method.size(); i++) {
				bodyLines.add(method.get(i));
			}

			mtdBean.setBody(bodyLines);
		}

		return mtdBean;
	}

	/**
	 * 解析方法段落并存入一个String<br />
	 * 
	 * @param method
	 * @param clazzName
	 * @param suberClazzName
	 * @param interfazeName
	 * @param srcApkName
	 * @return
	 */
	public static List<String> getMethodInfoAsString(List<String> method,
			String clazzName, String suberClazzName, String interfazeName,
			String srcApkName, String srcApkCrc32, String srcApkMd5,
			String srcApkSha1) {
		List<String> mtdList = new ArrayList<>();

		StringBuilder sb = new StringBuilder("");
		StringBuilder temp = new StringBuilder("");
		int lineCount = method.size();
		String nameLine = method.get(0);
		String[] lineArr = nameLine.split(" ");
		String name = lineArr[lineArr.length - 1];

		sb.append(clazzName).append("->").append(name).append(" ||| "); // 方法名
		sb.append(suberClazzName).append(" ||| "); // 超类名
		sb.append(interfazeName).append(" ||| "); // 接口名
		sb.append(StringUtils.substringAfterLast(name, ")")).append(" ||| "); // 返回值
		sb.append(StringUtils.substringBetween(name, "(", ")")).append(" ||| "); // 参数
		sb.append(srcApkName).append(" ||| "); // apk名称
		sb.append(srcApkCrc32).append(" ||| "); // CRC32
		sb.append(srcApkMd5).append(" ||| "); // MD5
		sb.append(srcApkSha1).append(" ||| "); // SHA1
		if (lineCount == 1) { // abstract方法、native方法、或空实现的方法
			Arrays.sort(lineArr);
			int existAbstract = Arrays.binarySearch(lineArr,
					C.MODIFIER_ABSTRACT);
			int existNative = Arrays.binarySearch(lineArr, C.MODIFIER_NATIVE);
			if (existAbstract < 0) {
				if (existNative < 0) { // 空实现的方法
					return null; // 存数据库时，若为空，则丢弃
				}
			}

			sb.append(existNative >= 0 ? 1 : 0).append(" ||| "); // 是否native
			sb.append(existAbstract >= 0 ? 1 : 0).append(" ||| "); // 是否抽象
		} else { // 提取并划分方法体
			sb.append(0).append(" ||| ").append(0).append(" ||| "); // 属性
			sb.append(dateFormat.format(new Date())).append(" ||| "); // 创建时间
			temp.append(sb); // 对于超长body，这是头

			int charCount = 0;
			for (int i = 1; i < method.size(); i++) { // 方法体
				if (charCount > 60000) {
					mtdList.add(sb.toString());
					charCount = 0;
					sb = new StringBuilder("");
					sb.append(temp);
				}

				sb.append(method.get(i)).append(C.CRLF);
				charCount += sb.length();
				// System.out.println(charCount);
			}

		}

		mtdList.add(sb.toString());

		// System.out.println(sb.toString());
		return mtdList;
	}
}
