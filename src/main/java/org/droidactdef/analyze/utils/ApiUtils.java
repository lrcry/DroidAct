package org.droidactdef.analyze.utils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.io.FileUtils;
import org.droidactdef.analyze.commons.ApiConst;
import org.droidactdef.analyze.domains.MtdIncludeApi;
import org.droidactdef.analyze.domains.TopLevelMtd;
import org.droidactdef.commons.C;
import org.droidactdef.domains.SmaliMethod;
import org.droidactdef.utils.DroidActDBUtils;
import org.droidactdef.utils.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Android API工具<br />
 * 
 * @author range
 * 
 */
public class ApiUtils {
	private static Pattern pattern = Pattern
			.compile(ApiConst.REGEX_ANDROID_API);
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ApiUtils.class);

	/**
	 * 从apk的代码中提取出调用了Android API方法的最上层方法名<br />
	 * 即没有被别的方法再调用的方法<br />
	 * 
	 * @return <方法名， MtdIncludeApi对象>
	 * @throws SQLException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static Map<String, TopLevelMtd> getFinalMtdNameIncludingApi(
			Connection conn, String md5) throws SQLException,
			InterruptedException, IOException {
		// TODO move allNames to the starter
		List<String> fileLines = new ArrayList<>();
		List<Object[]> allMtds = DroidActDBUtils.getMethodsNames(conn, null,
				md5); // 该APK所有方法名称。该集合不应放在此处，应放在starter中。此处测试用
		List<String> allNames = new ArrayList<>();
		for (Object[] mtd : allMtds) {
			if (mtd != null && mtd.length == 3)
				allNames.add((String) mtd[0]);
		}

		// 方法开始
		// 获取所有顶层方法
		List<Object[]> tlMtdList = DroidActDBUtils
				.getAllTopLevelMtds(conn, md5);
		Map<String, TopLevelMtd> topLevelMap = getTopLvMtdMap(tlMtdList);
		// <name, {name, superclass, interface, body, apis}>

		// Map<String, TopLevelMtd> temp = new HashMap<>();
		Map<String, SmaliMethod> temp = new HashMap<>();

		// 存储所有顶层方法调用的非API方法名，<topLevelName, List<Object[]> names>
		Map<String, List<String>> tlNonApiNames = new HashMap<>();

		// Iterate topLevelMap
		Iterator<String> tlMapIt = topLevelMap.keySet().iterator();
		while (tlMapIt.hasNext()) {
			List<String> mtdNames = new ArrayList<>(); // 存储当前顶层方法调用的非API方法名

			String name = tlMapIt.next();
			TopLevelMtd tlm = topLevelMap.get(name);
			final List<String> body = tlm.getBody();
			HashSet<String> apis = new HashSet<>();

			// Iterate body lines
			for (String line : body) {

				if (RegexUtils.matchStringFromLineByRegex(line,
						ApiConst.REGEX_METHOD_INVOCATION)) { // 是否为方法调用
					if (RegexUtils.matchStringFromLineByRegex(line,
							ApiConst.REGEX_ANDROID_API_INVOKE)) { // 是API调用
						apis.add(RegexUtils.findStringFromLineByRegex(line,
								ApiConst.REGEX_ANDROID_API));
					} else { // 不是API调用
						String mtdName = RegexUtils.findStringFromLineByRegex(
								line, C.PTN_METHOD);
						if (allNames.contains(mtdName)) { // 是本APK方法但不是API，则存入temp，否则丢弃
						// Object[] nonApiName = new String[] { mtdName };
							mtdNames.add(mtdName);

						}
					}
				}

			}

			// 更新顶层方法集合topLevelMap
			tlm.setApis(apis);
			topLevelMap.put(name, tlm);

			// 将非API方法名添加
			tlNonApiNames.put(name, mtdNames);
		}

		// 查询非API方法，并迭代该集合直至该集合为空
		while (tlNonApiNames.size() > 0) {

			Iterator<String> iter = tlNonApiNames.keySet().iterator();

			// 对每个顶层方法的非API集合进行循环
			while (iter.hasNext()) {
				// flag

				String tlName = iter.next();
				List<String> mtdNames = tlNonApiNames.get(tlName);
				// System.out.println((mtdNames == null || mtdNames.size() == 0)
				// + tlName);
				if (mtdNames.size() == 0) {
					iter.remove();
					// System.out.println("No non apis in " + tlName);
					continue;
				}

				// for (String o : mtdNames) {
				// System.out.println(o);
				// }
				// Thread.sleep(2000);

				List<Object[]> mtds = DroidActDBUtils.getMethodsByName(conn,
						null, md5, mtdNames);
				// System.out.println("mtds size: " + mtds.size());
				List<String> mtdsNew = new ArrayList<>(); // 新添加非API方法【名称】
				// Thread.sleep(2000);

				Iterator<Object[]> mtdsIt = mtds.iterator();
				while (mtdsIt.hasNext()) { // 顶层方法的一个非API调用
				// System.out.println("while executed");
					Object[] mtd = mtdsIt.next();
					List<String> body = stringToLines((String) mtd[6]);
					// System.out.println("body size: " + body.size());
					for (String line : body) { // mtd_name = mtd[1]; mtd_body =
												// mtd[6]
						String found = RegexUtils.findStringFromLineByRegex(
								line, ApiConst.REGEX_ANDROID_API);
						// System.out.println(found);
						fileLines.add(found);
						// System.out.println("add?" + b);

						if (found == null || found.equals("")) { // 不是API方法调用
							found = RegexUtils.findStringFromLineByRegex(line,
									C.PTN_METHOD);
							if (found == null || found.equals("")) { // 不是方法调用
								; // do nothing
							} else { // 是方法调用（非API）

								mtdsNew.add(found);
							}
						} else { // API方法

							TopLevelMtd tlm = topLevelMap.get(tlName);
							HashSet<String> apis = tlm.getApis();
							apis.add(found); // 添加API
							tlm.setApis(apis);
							topLevelMap.put(tlName, tlm);
						}
					}
				}

				tlNonApiNames.put(tlName, mtdsNew);

				// count++;
			}

			// System.out.println(tlNonApiNames.size() + " left");
			// Thread.sleep(500);
		}

		return topLevelMap;
	}

	/**
	 * 获取直接调用API的方法名<br />
	 * 方法的入参必须是DroidActDBUtils.getMtdWhoseBodyIncludesAndroid()方法查询到的结果<br />
	 * 测试通过<br />
	 * 
	 * @param mtds
	 *            [mtd_name, mtd_superclass, mtd_body]
	 * @return <mtdName, MtdIncludeApi object>
	 */
	public static Map<String, MtdIncludeApi> getMtdInvokingApi(
			List<Object[]> mtds) {
		// System.out
		// .println("getMtdInvokingApi: mtds is null? " + (mtds == null));
		System.out
				.println("mtds including Landroid || Lcom/android -- mtds.size="
						+ mtds.size());
		Map<String, MtdIncludeApi> mtdsWithApis = new HashMap<>();

		for (Object[] mtd : mtds) {
			List<String> apis = new ArrayList<>();
			// System.out.println("array length: " + mtd.length);
			if (mtd != null && mtd.length == 3) {
				String body = (String) mtd[2];
				List<String> lines = stringToLines(body);
				for (String line : lines) {
					// System.out.println(line);
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						String api = matcher.group();
						// logger.debug(api);
						apis.add(api);
					}
				}

			}

			if (apis.size() > 0) {
				String mtdName = (String) mtd[0];
				String mtdSuperClazzName = (String) mtd[1];
				MtdIncludeApi mia = new MtdIncludeApi();
				mia.setMtdName(mtdName);
				mia.setMtdSuperClazzName(mtdSuperClazzName);
				mia.setApis(apis);
				mtdsWithApis.put(mtdName, mia);
			}

		}

		return mtdsWithApis;
	}

	/**
	 * 处理查询顶层方法得到的结果，返回格式化的结果<br />
	 * 
	 * @param tlMtdList
	 *            DroidActDBUtils.getAllTopMtds的查询结果[name, superclass,
	 *            interface, body]
	 * @return <name, topLevelMtd object>
	 */
	public static Map<String, TopLevelMtd> getTopLvMtdMap(
			List<Object[]> tlMtdList) {
		Map<String, TopLevelMtd> topLevels = new HashMap<>();
		for (Object[] topArr : tlMtdList) {
			if (topArr != null && topArr.length == 4) {
				String name = (String) topArr[0];
				String suberCl = (String) topArr[1];
				String intf = (String) topArr[2];
				List<String> bodyLines = stringToLines((String) topArr[3]);
				if (topLevels.containsKey(name)) { // 已经存在方法名，则说明该方法有多条记录
					TopLevelMtd tlm = topLevels.get(name);
					List<String> oriBody = tlm.getBody();
					oriBody.addAll(bodyLines);
					tlm.setBody(oriBody);
					topLevels.put(name, tlm);
				} else { // 不存在方法名，说明该方法只有一条记录
					TopLevelMtd tlm = new TopLevelMtd();
					tlm.setName(name);
					tlm.setSuberClazz(suberCl);
					tlm.setInterfaze(intf);
					tlm.setBody(bodyLines);
					topLevels.put(name, tlm);
				}
			}
		}

		return topLevels;
	}

	/**
	 * 将String类型的方法体转换为行集合<br />
	 * 
	 * @param body
	 * @return
	 */
	public static List<String> stringToLines(String body) {
		List<String> lines = new ArrayList<>();
		if (body != null && body.length() > 0) {
			String[] bodyArr = body.split(C.CRLF);
			if (bodyArr != null & bodyArr.length > 0) {
				for (int i = 0; i < bodyArr.length; i++) {
					if (!bodyArr[i].equals(""))
						lines.add(bodyArr[i].trim());
				}
			}
		}

		return lines;
	}
}
