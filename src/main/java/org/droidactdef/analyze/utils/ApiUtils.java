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
	 * 从APK代码中提取出调用了AndroidAPI方法的： <br />
	 * 1. 顶层方法名及其调用的所有API<br />
	 * 2. 顶层方法名的下第一级方法名及其调用的所有API<br />
	 * 为控制流分析专门改进的获取顶层方法的方法<br />
	 * 
	 * @param conn
	 * @param md5
	 * @param allMtdNames
	 *            当前APK的所有方法名
	 * @return
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static Map<String, TopLevelMtd> getTopLevelMtdsWithApi(
			Connection conn, String md5, List<String> allMtdNames)
			throws SQLException, InterruptedException {
		// 获取所有顶层方法
		List<Object[]> tlMtdList = DroidActDBUtils
				.getAllTopLevelMtds(conn, md5);
		Map<String, TopLevelMtd> topLevelMap = getTopLvMtdMap(tlMtdList);
		// <name, {name, superclass, interface, body, apis}>
		System.out.println("get top level over");

		// Iterate topLevelMap
		Iterator<String> tlMapIt = topLevelMap.keySet().iterator();
		while (tlMapIt.hasNext()) {

			String name = tlMapIt.next();
			// System.out.println(name);
			TopLevelMtd tlm = topLevelMap.get(name);
			final List<String> body = tlm.getBody();
			HashSet<String> apis = new HashSet<>();
			Map<String, HashSet<String>> lvSecond = new HashMap<>(); // 非API集合（下一层方法）

			// Iterate body lines， 为了迭代出顶层方法的下一层方法
			// 内层第一次迭代,将当前顶层方法 直接调用的API 和其下层方法(非API)迭代出来
			for (String line : body) {
				String found = RegexUtils.findStringFromLineByRegex(line,
						ApiConst.REGEX_ANDROID_API);
				if (isStrNullEmpty(found)) { // 非API
					found = RegexUtils.findStringFromLineByRegex(line,
							C.PTN_METHOD);
					if (isStrNullEmpty(found)) { // 非方法调用
						;
					} else if (allMtdNames.contains(found)) { // 非API本APK方法调用
						lvSecond.put(found, new HashSet<String>());
					}
				} else { // API
					apis.add(found);
				}

			}
			// 内层第一次迭代结束，获得的TLM中，包含方法信息、TLM直接调用的API、TLM的次级方法,以上代码测试通过

			// 内层第二次迭代,将TLM次级方法调用的所有方法筛选出来
			Iterator<String> lvSecIter = lvSecond.keySet().iterator();
			Map<String, HashSet<String>> lvSecUpdate = new HashMap<>();
			while (lvSecIter.hasNext()) {
				String lvSecName = lvSecIter.next();
				List<String> lvSecBody = DroidActDBUtils.getBodyByMethodName(
						conn, lvSecName, null, md5);
				HashSet<String> lvSecInvoc = new HashSet<>();
				for (String line : lvSecBody) {
					String found = RegexUtils.findStringFromLineByRegex(line,
							ApiConst.REGEX_ANDROID_API);
					if (isStrNullEmpty(found)) { // 非API
						found = RegexUtils.findStringFromLineByRegex(line,
								C.PTN_METHOD);
						if (isStrNullEmpty(found)) { // 非方法调用
							;
						} else if (allMtdNames.contains(found)) { // 非API本APK方法调用
							lvSecInvoc.add(found);
						}
					} else { // API
						lvSecInvoc.add(found);
						apis.add(found);
					}
				}
				lvSecUpdate.put(lvSecName, lvSecInvoc);
			}
			lvSecond.putAll(lvSecUpdate);
			// 内层第二次迭代结束，在第一步基础上更新了TLM的nonApis
			// for (Map.Entry<String, HashSet<String>> e : lvSecond.entrySet())
			// {
			// System.out.println(e.getKey() + "\t" + e.getValue());
			// }

			// System.out.println("Start 3rd iteration");
			// TODO 内层第三次迭代，迭代lvSecond集合，获取其所有方法 存在问题，获取API不全，死循环
			Map<String, HashSet<String>> lvSecUpdThird = new HashMap<>();
			f: for (Map.Entry<String, HashSet<String>> e : lvSecond.entrySet()) {

				String lvSecName = e.getKey();
				HashSet<String> lvSecHashSet = e.getValue();
				if (lvSecHashSet.size() == 0) // 如果second层方法的调用集合为空
					continue f;

				w: while (true) {
					// 否则，先筛选lvSecHashSet中在下一轮迭代需要去除的方法（非API方法）
					HashSet<String> setForRmv = new HashSet<>();
					for (String mtd : lvSecHashSet) {
						if (!RegexUtils.matchStringFromLineByRegex(mtd,
								ApiConst.REGEX_ANDROID_API))
							setForRmv.add(mtd);
					}

					if (lvSecHashSet.size() == 0)
						break w;

					// 再循环查找
					List<Object[]> lowers = DroidActDBUtils.getMethodsByName(
							conn, null, md5, lvSecHashSet);
					lvSecHashSet.removeAll(setForRmv);
					if (lowers.size() == 0) { // 如果没有结果说明已经到达底层方法
						lvSecUpdThird.put(lvSecName, lvSecHashSet);
						apis.addAll(lvSecHashSet);
						break w;
					}

					for (Object[] lower : lowers) {
						String lowerName = (String) lower[1];
						System.out.println(lowerName);
						List<String> lowerBody = stringToLines((String) lower[6]);
						for (String line : lowerBody) {
							String found = RegexUtils
									.findStringFromLineByRegex(line,
											ApiConst.REGEX_ANDROID_API);
							if (isStrNullEmpty(found)) { // 不是API
								found = RegexUtils.findStringFromLineByRegex(
										line, C.PTN_METHOD);
								if (isStrNullEmpty(found)
										|| !allMtdNames.contains(found)) // 不是方法或本APK方法调用，丢弃
									;
								else { // 非API方法调用
									lvSecHashSet.add(found);
								}
							} else { // API
								apis.add(found);
								lvSecHashSet.add(found);
							}
						}
					}
					// Thread.sleep(5000);
				}

				System.out.println("【】更新LVSECOND");
				// 更新lvSecond
				lvSecUpdThird.put(lvSecName, lvSecHashSet);
			}
			lvSecond.putAll(lvSecUpdThird);
			// for (Map.Entry<String, HashSet<String>> e : lvSecond.entrySet())
			// {
			// System.out.println(e.getKey() + "\t" + e.getValue());
			// }

			// 更新顶层方法集合topLevelMap
			tlm.setApis(apis);
			tlm.setNonApiMtdApis(lvSecond);
			topLevelMap.put(name, tlm);
			// 一个顶层方法处理完成
		}

		return topLevelMap;
	}

	/**
	 * 从apk的代码中提取出调用了Android API方法的最上层方法名<br />
	 * 即没有被别的方法再调用的方法<br />
	 * 此方法更适用于数据流分析过程，一个可以尝试的思路是，通过观察调用了API的组件之间的关系<br />
	 * 
	 * @return <方法名， MtdIncludeApi对象>
	 * @throws SQLException
	 */
	public static Map<String, TopLevelMtd> getFinalMtdNameIncludingApi(
			Connection conn, String md5) throws SQLException {
		// 获取所有方法名，以排除不是本APK中的方法
		List<Object[]> allMtds = DroidActDBUtils.getMethodsNames(conn, null,
				md5);
		List<String> allNames = new ArrayList<>();
		for (Object[] mtd : allMtds) {
			if (mtd != null && mtd.length == 3)
				allNames.add((String) mtd[0]);
		}

		// 获取所有顶层方法
		List<Object[]> tlMtdList = DroidActDBUtils
				.getAllTopLevelMtds(conn, md5);
		Map<String, TopLevelMtd> topLevelMap = getTopLvMtdMap(tlMtdList);
		// <name, {name, superclass, interface, body, apis}>

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
			// int iterCount = 0; //
			// 迭代次数，如果是第一次迭代，则在过程当中更新TopLevel中的nonApiMtdApis集合

			Iterator<String> iter = tlNonApiNames.keySet().iterator();

			// 对每个顶层方法的非API集合进行循环
			while (iter.hasNext()) {
				String tlName = iter.next();
				List<String> mtdNames = tlNonApiNames.get(tlName);
				if (mtdNames.size() == 0) {
					iter.remove();
					continue;
				}

				List<Object[]> mtds = DroidActDBUtils.getMethodsByName(conn,
						null, md5, mtdNames);
				List<String> mtdsNew = new ArrayList<>(); // 新添加非API方法【名称】

				Iterator<Object[]> mtdsIt = mtds.iterator();
				while (mtdsIt.hasNext()) { // 顶层方法的一个非API调用
					Object[] mtd = mtdsIt.next();
					List<String> body = stringToLines((String) mtd[6]);
					for (String line : body) { // mtd_name = mtd[1]; mtd_body =
												// mtd[6]
						String found = RegexUtils.findStringFromLineByRegex(
								line, ApiConst.REGEX_ANDROID_API);

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

							// if (iterCount == 0) {
							// tlm.
							// }

							topLevelMap.put(tlName, tlm);

						}
					}
				}

				tlNonApiNames.put(tlName, mtdsNew);
			}
			// iterCount++;
		}

		// for () {

		// }
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

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

	private static boolean isStrNullEmpty(String str) {
		return str == null || str.equals("");
	}
}
