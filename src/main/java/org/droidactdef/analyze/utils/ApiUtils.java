package org.droidactdef.analyze.utils;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

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
	 * 方法有问题，在最顶层方法中不能提取到所有的Android API<br />
	 * 本地备份于__tempCodes/apiutilsTemp.java<br />
	 * 
	 * 新方法仍有问题，无法迭代至集合size=0<br />
	 * 2014 05 09<br />
	 * 
	 * 2014 05 12 自上而下分析尝试<br />
	 * 2014 05 14继续<br />
	 * 发现官方工具apktool也不能反编译出apk的全部内容<br />
	 * 
	 * @return <方法名， MtdIncludeApi对象>
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public static Map<String, TopLevelMtd> getFinalMtdNameIncludingApi(
			Connection conn, String md5) throws SQLException,
			InterruptedException {
		// TODO move allNames to the starter
		List<Object[]> allMtds = DroidActDBUtils.getMethodsNames(conn, null,
				md5); // 该APK所有方法名称。该集合不应放在此处，应放在starter中。此处测试用
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

		// Map<String, TopLevelMtd> temp = new HashMap<>();
		Map<String, SmaliMethod> temp = new HashMap<>();

		// 存储所有顶层方法调用的非API方法名，<name, List<Object[]> names>
		Map<String, List<Object[]>> tlNonApiNames = new HashMap<>();

		// Iterate topLevelMap
		Iterator<String> tlMapIt = topLevelMap.keySet().iterator();
		while (tlMapIt.hasNext()) {
			List<Object[]> mtdNames = new ArrayList<>(); // 存储当前顶层方法调用的非API方法名

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
							Object[] nonApiName = new String[]{mtdName};
							mtdNames.add(nonApiName);
							
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
