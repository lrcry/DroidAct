package org.droidactdef.analyze.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.droidactdef.analyze.commons.ApiConst;
import org.droidactdef.analyze.domains.MtdIncludeApi;
import org.droidactdef.commons.C;
import org.droidactdef.utils.DroidActDBUtils;
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
	private static Logger logger = LoggerFactory.getLogger(ApiUtils.class);

	/**
	 * 从apk的代码中提取出调用了Android API方法的最上层方法名<br />
	 * 即没有被别的方法再调用的方法<br />
	 * 方法有问题，在最顶层方法中不能提取到所有的Android API<br />
	 * 
	 * @return <方法名， MtdIncludeApi对象>
	 * @throws SQLException
	 */
	public static Map<String, MtdIncludeApi> getFinalMtdNameIncludingApi(
			Connection conn, String md5) throws SQLException {
		Map<String, MtdIncludeApi> mtdNameApiFinal = new HashMap<>();

		List<Object[]> mtdIncludeDroid = DroidActDBUtils
				.getMtdWhoseBodyIncludesAndroid(conn, md5);
		// [mtd_name, mtd_superclass, mtd_body]

		Map<String, MtdIncludeApi> mtdInvokingApi = getMtdInvokingApi(mtdIncludeDroid);
		// <mtdName, {mtdName, mtdSuperClazz, mtdApis}>

		Map<String, MtdIncludeApi> mapForPut = new HashMap<>();
		Map<String, MtdIncludeApi> mapForRmv = new HashMap<>();

		while (mtdInvokingApi.size() > 0) {
			Iterator<String> it = mtdInvokingApi.keySet().iterator();
			while (it.hasNext()) { // 对集合进行迭代
				String name = it.next(); // 调用api的方法name
				MtdIncludeApi mia = mtdInvokingApi.get(name);

				// this may cause problems
				if (DroidActDBUtils.isMethodUppestLevel(conn, md5, name)) { // 已经最上层
					mtdNameApiFinal.put(name, mia);
					it.remove(); // a method may be removed before analyzed
					continue;
				}

				List<String> apis = mia.getApis();
				List<Object[]> mtdIncludeName = DroidActDBUtils
						.getMtdWhoseBodyIncludeName(conn, md5, name); // 调用了方法name的所有方法
				if (mtdIncludeName != null && mtdIncludeName.size() > 0) {
					for (Object[] mtd : mtdIncludeName) {
						String mtdName = (String) mtd[0];
						MtdIncludeApi mtdApi = mtdInvokingApi.get(mtdName);
						if (mtdApi == null) { // 如果该方法第一次出现在mtdInvokingApi
							mtdApi = new MtdIncludeApi();
							mtdApi.setMtdName(mtdName);
							mtdApi.setMtdSuperClazzName((String) mtd[1]);
							mtdApi.setApis(apis);
							mapForPut.put(mtdName, mtdApi);
						} else { // 如果该方法已经存在于mtdInvokingApi中
							List<String> mtdGetApi = mtdApi.getApis();
							mtdGetApi.addAll(apis);
							mtdApi.setMtdName(mtdName);
							mtdApi.setMtdSuperClazzName((String) mtd[1]);
							mtdApi.setApis(mtdGetApi);
							mapForPut.put(mtdName, mtdApi);
						}
					}
				}

				mapForRmv.put(name, mia);
			}

			Iterator<String> rmIt = mtdInvokingApi.keySet().iterator();
			while (rmIt.hasNext()) { // 删除下层的方法
				String key = rmIt.next();
				if (mapForRmv.containsKey(key)) {
					rmIt.remove();
				}
			}
			mtdInvokingApi.putAll(mapForPut); // 添加上层的方法
		}

		return mtdNameApiFinal;
	}

	/**
	 * 
	 * 2. 获取当前方法名上层的方法，即调用了当前方法的方法<br />
	 * 方法的入参是DroidActDBUtils.getMtdWhoseBodyIncludeName()的结果<br />
	 * 
	 * @Deprecated
	 * 
	 * @param mia
	 *            a method in the form of MtdIncludeApi object
	 * @param mtds
	 *            [mtd_name, mtd_superclass, mtd_body]
	 * @return mtdName
	 */
	@Deprecated
	public static Map<String, MtdIncludeApi> getUpperLevelMtdName(
			MtdIncludeApi mia, List<Object[]> mtds) {

		return null;
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
		System.out
				.println("getMtdInvokingApi: mtds is null? " + (mtds == null));
		System.out.println("mtds.size=" + mtds.size());
		Map<String, MtdIncludeApi> mtdsWithApis = new HashMap<>();

		for (Object[] mtd : mtds) {
			List<String> apis = new ArrayList<>();
			System.out.println("array length: " + mtd.length);
			if (mtd != null && mtd.length == 3) {
				String body = (String) mtd[2];
				List<String> lines = stringToLines(body);
				for (String line : lines) {
					// System.out.println(line);
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						String api = matcher.group();
						logger.debug(api);
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

	public static void main(String[] args) throws Exception {
		boolean isLd = DbUtils.loadDriver("com.mysql.jdbc.Driver");
		if (isLd) {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/droidact", "root", "admin");
			QueryRunner runner = new QueryRunner();
			List<Object[]> result = runner
					.query(conn,
							"select mtd_name, mtd_superclass, mtd_body from da_methods "
									+ "where mtd_name="
									+ "'Lad/imadpush/com/poster/a;-><init>(Lad/imadpush/com/poster/PosterInfoActivity;Ljava/util/List;)V'",
							new ArrayListHandler());

			Map<String, MtdIncludeApi> mtds = getMtdInvokingApi(result);
			for (Map.Entry<String, MtdIncludeApi> entry : mtds.entrySet()) {
				MtdIncludeApi mtd = entry.getValue();
				System.out.println(mtd.toString());
			}
		}
	}
}
