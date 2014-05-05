package org.droidactdef.utils;

import java.io.*;
import java.util.*;
import java.sql.*;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.io.FileUtils;
import org.droidactdef.analyze.commons.ApiConst;
import org.droidactdef.commons.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 操作droidact数据库的工具类<br />
 * 
 * @author range
 * 
 */
public class DroidActDBUtils {
	private static final Logger logger = LoggerFactory
			.getLogger(DroidActDBUtils.class);

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		boolean isLoaded = DbUtils.loadDriver("com.mysql.jdbc.Driver");
		if (isLoaded) {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/droidact", "root", "admin");
			List<String> perms = new ArrayList<>();
			perms.add("android.permission.READ_CONTACTS");
			perms.add("android.permission.INTERNET");
			List<Object[]> result = getApiByPermission(conn, perms);
			for (Object[] rec : result) {
				for (int i = 0; i < rec.length; i++) {
					System.out.print(rec[i] + " ");
				}
				System.out.println();
			}
		}
	}

	/*
	 * ==========================================================================
	 * ===
	 * ======================================================================
	 * =======
	 * ==================================================================
	 * ===========
	 * ==============================================================
	 * ============= Read from database =============
	 */
	/**
	 * 获取方法体中含有Landroid或Lcom/android的方法<br />
	 * 为了从中提取含有API调用的方法<br />
	 * 
	 * @param conn
	 * @param crc32
	 * @param md5
	 * @return
	 * @throws SQLException 
	 */
	public static List<Object[]> getMtdWhoseBodyIncludesAndroid(
			Connection conn, String md5) throws SQLException {
		String sql = "select mtd_name, mtd_superclass, mtd_body from da_methods where mtd_body like '%"
				+ ApiConst.REGEX_L_ANDROID
				+ "%' or '%"
				+ ApiConst.REGEX_L_COM_ANDROID
				+ "%' and mtd_src_apk_md5='"
				+ md5 + "'";
		logger.debug(sql);
		QueryRunner runner = new QueryRunner();
		List<Object[]> result = runner.query(conn, sql, new ArrayListHandler());
		
		return result;
	}

	/**
	 * 根据APK申请的权限获取对应敏感API<br />
	 * 判断API属于获取型还是发送型：看起返回值是否为V<br />
	 * <code>
	 * select api_name, api_retype<br />
	 * from da_android_api<br />
	 * where api_permission in (?,?,...)<br />
	 * </code>
	 * 
	 * @param conn
	 *            数据库连接
	 * @param permissions
	 *            APK权限
	 * @return 权限对应的API
	 * @throws SQLException
	 */
	public static List<Object[]> getApiByPermission(Connection conn,
			List<String> permissions) throws SQLException {
		List<Object[]> apis = new ArrayList<>();
		Object[] permArr = permissions.toArray();
		StringBuilder sbWhere = new StringBuilder("where api_permission in (");
		for (int i = 0; i < permissions.size() - 1; i++) {
			sbWhere.append('?').append(',');
		}
		sbWhere.append("?)");
		String sql = sqlBuilder("select", "da_android_api",
				"api_name, api_retype, api_permission, api_type", 4,
				sbWhere.toString(), permArr);
		QueryRunner runner = new QueryRunner();
		apis = runner.query(conn, sql, new ArrayListHandler(), permArr);

		// String sth = constructWhereClause(new String[]{}, permissions);
		// logger.info(sth);

		return apis;
	}

	/**
	 * 获取apk所有方法的方法体<br />
	 * 
	 * @param conn
	 * @param crc32
	 * @param md5
	 * @return <mtdName, mtdBody>
	 * @throws SQLException
	 */
	public static Map<String, List<String>> getAllMethodsBodies(
			Connection conn, String crc32, String md5) throws SQLException {
		Map<String, List<String>> mtdsBodies = new HashMap<>();

		String sql = sqlBuilder("select", "da_methods", "mtd_name, mtd_body",
				2, "where mtd_src_apk_crc32='" + crc32
						+ "' and mtd_src_apk_md5='" + md5 + "'");
		QueryRunner runner = new QueryRunner();
		List<Object[]> results = runner
				.query(conn, sql, new ArrayListHandler());

		for (Object[] result : results) {
			if (result != null && result.length == 2) {
				String name = (String) result[0];
				String bodyStr = (String) result[1];
				if (name != null && bodyStr != null) {
					List<String> body = new ArrayList<>();
					String[] bodyArr = bodyStr.split(C.CRLF);
					if (bodyArr != null && bodyArr.length > 0) {
						for (int i = 0; i < bodyArr.length; i++) {
							body.add(bodyArr[i]);
						}
					}

					if (mtdsBodies.containsKey(name)) {
						List<String> valist = mtdsBodies.get(name);
						valist.addAll(body);
						mtdsBodies.put(name, valist);
					} else {
						mtdsBodies.put(name, body);
					}
				}
			}
		}

		return mtdsBodies;
	}

	/**
	 * 根据某个方法名称获取方法体，供控制流分析<br />
	 * 
	 * @param conn
	 * @param mtdName
	 * @param crc32
	 * @param md5
	 * @return
	 * @throws SQLException
	 */
	public static List<String> getBodyByMethodName(Connection conn,
			String mtdName, String crc32, String md5) throws SQLException {
		String sql = sqlBuilder("select", "da_methods", "mtd_body", 1,
				"where mtd_name='" + mtdName + "' and mtd_src_apk_crc32='"
						+ crc32 + "' and mtd_src_apk_md5='" + md5);
		QueryRunner runner = new QueryRunner();
		List<Object[]> body = runner.query(conn, sql, new ArrayListHandler());

		List<String> bodyLines = convertObjListToStrList(body, 0);

		return bodyLines;
	}

	/**
	 * 获得某个apk所有方法名称<br />
	 * 
	 * @param conn
	 * @param crc32
	 * @param md5
	 * @return
	 * @throws SQLException
	 */
	public static List<Object[]> getMethodsNames(Connection conn, String crc32,
			String md5) throws SQLException {
		String sql = sqlBuilder("select", "da_methods", "distinct mtd_name", 1,
				"where mtd_src_apk_crc32='" + crc32 + "' and mtd_src_apk_md5='"
						+ md5 + "'", crc32, md5);

		QueryRunner runner = new QueryRunner();
		List<Object[]> names = runner.query(conn, sql, new ArrayListHandler());

		for (Object[] name : names) {
			System.out.println(name[0]);
		}

		return names;
	}

	/**
	 * 从某APK反编译的代码中提取某些方法名的方法<br />
	 * <code>select * <br />
	 * from da_methods<br />
	 * where mtd_src_apk_crc32='sth'<br />
	 * and mtd_src_apk_md5='sth2'<br />
	 * and mtd_name in (?,?,?,...) <br /></code>
	 * 
	 * @param conn
	 *            数据库连接
	 * @param crc32
	 *            APK的CRC32
	 * @param md5
	 *            APK的MD5，与CRC32共同确定唯一APK
	 * @param mtdNames
	 *            方法名称集合
	 * @return 方法详细
	 * @throws SQLException
	 */
	public static List<Object[]> getMethodsByName(Connection conn,
			String crc32, String md5, List<Object[]> mtdNames)
			throws SQLException {
		List<Object[]> methods = new ArrayList<>();
		Object[] mtdNamesArr = mtdNames.toArray();
		StringBuilder sbWhere = new StringBuilder("where mtd_src_apk_crc32='"
				+ crc32 + "' and mtd_src_apk_md5='" + md5
				+ "' and mtd_name in (");
		for (int i = 0; i < mtdNames.size() - 1; i++) {
			sbWhere.append('?').append(',');
		}
		sbWhere.append("?)");
		String sql = sqlBuilder("select", "da_methods", "*", 14,
				sbWhere.toString(), mtdNamesArr);
		logger.info(sql);
		QueryRunner runner = new QueryRunner();
		methods = runner.query(conn, sql, new ArrayListHandler(), mtdNamesArr);

		return methods;
	}

	/**
	 * 获得调用某API的全部方法<br />
	 * <code>
	 * SELECT *<br />
	FROM da_methods<br />
	WHERE mtd_src_apk_crc32='21CAA179'<br />
	AND mtd_src_apk_md5='4f65245c31844079'<br />
	HAVING mtd_name IN<br />
	(<br />
		SELECT mtd_name <br />
		FROM da_methods<br />
		WHERE mtd_body LIKE '%Landroid/telephony/TelephonyManager;->getDeviceId()Ljava/lang/String;%'<br />
		AND mtd_src_apk_crc32='21CAA179'<br />
		AND mtd_src_apk_md5='4f65245c31844079'<br />
	) <br /></code>
	 * 
	 * @param conn
	 *            数据库连接
	 * @param apiName
	 *            API名称
	 * @param crc32
	 * @param md5
	 * @return 全部方法信息
	 * @throws SQLException
	 */
	public static List<Object[]> getMethodsInvokingAPI(Connection conn,
			String apiName, String crc32, String md5) throws SQLException {
		List<Object[]> methods = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("select * from da_methods ");
		sb.append("where mtd_src_apk_crc32='").append(crc32).append("' ");
		sb.append("and mtd_src_apk_md5='").append(md5).append("' ");
		sb.append("having mtd_name in (");
		sb.append("select mtd_name from da_methods ");
		sb.append("where mtd_body like '%").append(apiName).append("%' ");
		sb.append("and mtd_src_apk_crc32='").append(crc32).append("' ");
		sb.append("and mtd_src_apk_md5='").append(md5).append("')");
		String sql = new String(sb);
		logger.info(sql);
		QueryRunner runner = new QueryRunner();
		methods = runner.query(conn, sql, new ArrayListHandler());

		return methods;
	}

	/*
	 * ==========================================================================
	 * ===
	 * ======================================================================
	 * =======
	 * ==================================================================
	 * ===========
	 * ==============================================================
	 * ============= Modify database =============
	 */
	/**
	 * 将APK信息输入至数据库<br />
	 * 
	 * @param conn
	 * @param apkName
	 * @param crc32
	 * @param md5
	 * @param sha1
	 * @param permissions
	 * @param applications
	 * @param mainActivity
	 * @param activities
	 * @param receivers
	 * @param services
	 * @param contentProvider
	 * @param maliciousLevel
	 *            初次写入为-1
	 * @param reportLoc
	 *            初次写入为""
	 * @throws SQLException
	 */
	public static void insertApkInfo(Connection conn, String apkName,
			String crc32, String md5, String sha1, List<String> permissions,
			List<String> applications, String mainActivity,
			List<String> activities, List<String> receivers,
			List<String> services, List<String> contentProvider,
			int maliciousLevel, String reportLoc) throws SQLException {
		String sql = sqlBuilder(
				"insert",
				"da_apk_info",
				"apk_name,apk_crc32,apk_md5,apk_sha1,"
						+ "apk_permissions,apk_application,apk_main_activity,apk_activity,"
						+ "apk_receiver,apk_service,apk_contentprovider,"
						+ "apk_malicious_level,apk_report_location", 13, null);
		String perms = convertListIntoString(permissions, C.DB_STR_SPLIT);
		String apps = convertListIntoString(applications, C.DB_STR_SPLIT);
		String actvs = convertListIntoString(activities, C.DB_STR_SPLIT);
		String recvs = convertListIntoString(receivers, C.DB_STR_SPLIT);
		String servs = convertListIntoString(services, C.DB_STR_SPLIT);
		String cps = convertListIntoString(contentProvider, C.DB_STR_SPLIT);
		Object[] params = new Object[] { apkName, crc32, md5, sha1, perms,
				apps, mainActivity, actvs, recvs, servs, cps, maliciousLevel,
				reportLoc };

		QueryRunner runner = new QueryRunner();
		runner.update(conn, sql, params);
	}

	/**
	 * 按照SQL生成表<br />
	 * 
	 * @param fileName
	 *            SQL文件名
	 * @param conn
	 *            数据库连接
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void createTable(String fileName, Connection conn)
			throws IOException, SQLException {
		if (!fileName.endsWith(".sql")) {
			logger.error("文件错误：{}不是SQL文件", fileName);
			throw new RuntimeException("文件类型异常： " + fileName + "不是SQL文件");
		}

		File file = FileUtils.getFile(fileName);
		String sql = FileUtils.readFileToString(file, C.FILE_ENCODING);
		QueryRunner runner = new QueryRunner();
		runner.update(conn, sql);
	}

	/**
	 * 从.dbres文件批量导入表的数据<br />
	 * .dbres文件规范：<br />
	 * 1. 第一行为表头，表头各字段使用三个竖线|||分开<br />
	 * 2. 第一行以下为数据，数据各字段使用三个竖线|||分开，顺序应同上表头<br />
	 * 3. 扩展名为.dbres<br />
	 * 
	 * @param fileName
	 *            dbres数据文件名
	 * @param conn
	 *            数据库连接
	 * @param tableName
	 *            表明
	 * @param maxLines
	 *            批量写入的最大行数
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void importFromFileToTable(String fileName, Connection conn,
			String tableName, int maxLines) throws IOException, SQLException {
		if (!fileName.endsWith(".dbres")) {
			logger.error("文件错误：{}不是数据库资源dbres文件", fileName);
			throw new RuntimeException("文件类型异常： " + fileName + "不是数据库资源dbres文件");
		}
		File file = FileUtils.getFile(fileName);
		List<String> apiLines = FileUtils.readLines(file, C.FILE_ENCODING);

		// 解析文件头，也即表头
		String[] sArr = null;
		String schemaLine = apiLines.get(0);
		int colCount = 0;
		if (schemaLine != null && schemaLine.length() > 0) {
			sArr = schemaLine.split(C.FILE_SPLIT);
			colCount = sArr.length;
		} else {
			logger.error("文件错误：{}表头错误", fileName);
			throw new RuntimeException("文件格式异常：" + fileName + "表头错误");
		}

		Object[][] apis = listToObjectArray(apiLines, colCount);

		StringBuilder sb = new StringBuilder("");
		int i = 0;
		for (; i < sArr.length - 1; i++) {
			sb.append(sArr[i]).append(",");
		}
		sb.append(sArr[i]);

		String sql = sqlBuilder("insert", tableName, sb.toString(), colCount,
				null);

		batchWriteToDb(conn, sql, colCount, apis, maxLines);
	}

	/**
	 * 批量将数据写入数据库<br />
	 * 
	 * @param conn
	 *            数据库连接
	 * @param sql
	 *            SQL语句
	 * @param colCount
	 *            列数
	 * @param params
	 *            Object二维数组形式的参数
	 * @throws SQLException
	 */
	public static void batchWriteToDb(Connection conn, String sql,
			int colCount, Object[][] params, int maxLines) throws SQLException {
		QueryRunner runner = new QueryRunner();
		if (params.length < maxLines)
			runner.batch(conn, sql, params);
		else {
			Object[][] temp = new Object[maxLines][colCount];
			int tmpCnt = 0;
			for (int k = 0; k < params.length; k++) {
				temp[tmpCnt] = params[k];

				if (tmpCnt == maxLines) {
					runner.batch(conn, sql, temp);
					temp = new Object[maxLines][colCount];
					tmpCnt = 0;
					// System.out.println("Writing..");
					logger.debug("batchWriteToDb: reach to {}, writing...",
							maxLines);
				} else {
					tmpCnt++;
				}
			}

			// 将剩余的条目写入数据库
			List<Object[]> tList = new ArrayList<>();
			for (int t = 0; t < temp.length; t++) {
				if (temp[t][0] != null)
					tList.add(temp[t]);
			}
			Object[][] newTemp = new Object[tList.size()][colCount];
			Object[] tmpArr = tList.toArray();
			for (int t = 0; t < tmpArr.length; t++) {
				// System.out.println(tmpArr[t]);
				newTemp[t] = (Object[]) tmpArr[t];
			}
			runner.batch(conn, sql, newTemp);
		}
	}

	/*
	 * ==========================================================================
	 * ===
	 * ======================================================================
	 * =======
	 * ==================================================================
	 * ===========
	 * ==============================================================
	 * =============== ============= General =============
	 */
	/**
	 * 将List转换为Object[][]，为了使用DbUtils的批量操作batch<br />
	 * 输入的List规范：<br />
	 * 1. 首行是schema，即列名，使用|||分割<br />
	 * 2. 下面是数据，使用|||分割每列<br />
	 * 
	 * @param paramLines
	 *            参数行集合
	 * @param colCount
	 *            参数列数
	 * @return 转换后Object[][]形式的参数
	 */
	public static Object[][] listToObjectArray(List<String> paramLines,
			int colCount) {
		// System.out.println("listToObjectArray");
		Object[][] params = new Object[paramLines.size()][colCount];
		int apiArrCnt = 0;
		for (int j = 1; j < paramLines.size(); j++) {
			String line = (String) paramLines.get(j);
			// System.out.println(line);

			String[] arr = line.split(C.FILE_SPLIT);
			for (int i = 0; i < arr.length; i++) {
				arr[i] = arr[i].trim();
			}
			if (arr != null && arr.length == colCount
					&& apiArrCnt < params.length) {
				params[apiArrCnt] = (Object[]) arr;
				// System.out.println(apiArrCnt);
				apiArrCnt++;
			}
		}

		return params;
	}

	/**
	 * 构造一般SQL<br />
	 * insert: sqlBuilder("insert", tableName, "col1, col2, col3, col4, ...",
	 * 5(e.g.), null)<br />
	 * update: sqlBuilder("update", tableName, "col", 0, "where-clause...")<br />
	 * delete: sqlBuilder("delete", tableName, null, 0, "where-clause...")<br />
	 * 简单select: sqlBuilder("select", tableName, "c1, c2, ...", 0,
	 * "where-clause..", whereparam1, wp2, wp3, ...)<br />
	 * 
	 * @param keyword
	 *            关键字，可为select, insert, update, delete
	 * @param tableName
	 *            要操作的表名
	 * @param schema
	 *            表头或列名，可为null
	 * @param colCnt
	 *            列数，如果使用insert语句必须指定，否则可置为0
	 * @param where
	 *            where子句，可选
	 * @param whereParams
	 *            where子句中的参数
	 * @return 构造的SQL语句
	 */
	public static String sqlBuilder(String keyword, String tableName,
			String schema, int colCnt, String where, Object... whereParams) {
		StringBuilder sb = new StringBuilder();
		if (keyword == null || keyword.length() == 0) {
			throw new RuntimeException("查询语句构造异常： 必须指定keyword字段");
		} else if (tableName == null || tableName.length() == 0) {
			throw new RuntimeException("查询语句构造异常： 必须指定待操作的表名");
		} else {
			switch (keyword) {
			case "select": {
				sb.append(keyword).append(" ").append(schema);
				sb.append(" from ").append(tableName).append(" ");
				if (where != null && where.length() > 0)
					sb.append(where);
				break;
			}
			case "insert": {
				sb.append(keyword).append(" into ").append(tableName)
						.append(" (");
				if (schema != null && schema.length() > 0) {
					sb.append(schema).append(") values (");
				}

				int cnt = 0;
				for (; cnt < colCnt - 1; cnt++) {
					sb.append("?,");
				}
				sb.append("?)");
				break;
			}
			case "delete": {
				sb.append(keyword).append(" from ").append(tableName)
						.append(" ");
				if (where != null && where.length() > 0)
					sb.append(where);
				break;
			}
			case "update": {
				sb.append(keyword).append(" ").append(tableName).append(" ");
				sb.append("set ").append(schema).append("=? ");
				if (where != null && where.length() > 0)
					sb.append(where);
				break;
			}
			default:
				break;
			}
		}

		String sql = sb.toString();
		// System.out.println(sql);
		logger.info(sql);
		return sql;
	}

	/**
	 * 将List中的所有字符串转换为使用splitter连接的字符串形式
	 * 
	 * @param strs
	 *            输入字符串集合
	 * @param splitter
	 *            分隔符
	 * @return 返回字符串
	 */
	private static String convertListIntoString(List<String> strs,
			String splitter) {
		StringBuilder sb = new StringBuilder();
		int max = strs.size() - 1;
		for (int i = 0; i < max; i++) {
			sb.append(strs.get(i)).append(splitter);
		}
		sb.append(strs.get(max));
		System.out.println(sb.toString());
		return sb.toString();
	}

	/**
	 * 将Object[]型的List转换为String型的List,只用于转换查询结果<br />
	 * 
	 * @param objList
	 * @param index
	 *            转换数组的第几列
	 * @return
	 */
	public static List<String> convertObjListToStrList(List<Object[]> objList,
			int index) {
		List<String> strList = new ArrayList<>();
		for (Object[] obj : objList) {
			if (obj != null && obj[index] != null) {
				strList.add((String) obj[index]);
			}
		}

		return strList;
	}
}
