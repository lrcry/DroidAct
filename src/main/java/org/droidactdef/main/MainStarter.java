package org.droidactdef.main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.droidactdef.commons.C;
import org.droidactdef.utils.DroidActDBUtils;
import org.droidactdef.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目的主启动器<br />
 * 
 * @author range
 * 
 */
public class MainStarter {
	private static final Logger logger = LoggerFactory
			.getLogger(MainStarter.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		logger.info("{}: 正在启动DroidAct", MainStarter.class.getName());

		/* 初始化阶段：检查文件完整性，加载配置文件，连接数据库，检查各库表是否存在并完整，若不存在或第一次加载，则建立库表并导入初始数据 */
		Connection conn = null;
		boolean isFilesExist = false;
		boolean isDbChecked = false;
		try {

			isFilesExist = checkFiles();
			if (!isFilesExist) {
				logger.error("文件完整性检查失败，文件缺失");
				throw new RuntimeException("启动失败： 文件完整性条件不满足");
			}
			logger.info("文件完整性检查完成");

			Properties props = PropertiesUtil.getProperties(C.CONF_PATH_DB);
			String drvName = props.getProperty("mysql.driverName");
			String url = props.getProperty("mysql.url");
			String user = props.getProperty("mysql.user");
			String pwd = props.getProperty("mysql.pwd");
			logger.info("数据库配置文件加载完成，正在连接数据库");

			boolean isDrvLoad = DbUtils.loadDriver(drvName);
			if (!isDrvLoad) {
				logger.error("数据库驱动加载失败，请检查数据库驱动jar包是否正确导入");
				throw new RuntimeException("启动失败：数据库连接失败");
			}
			conn = DriverManager.getConnection(url, user, pwd);
			logger.info("数据库连接已取得，正在检查数据库完整性");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("加载数据库配置文件不成功: ", e);
			// e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("取得数据库连接异常：{}", e.toString());
			// e.printStackTrace();
		}

		try {
			isDbChecked = checkDb(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("校验完整性异常：" + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("建表失败：建表文件不存在");
		}

		if (isFilesExist && isDbChecked)
			logger.info("运行时环境检查完成，准备启动定时分析程序");

		/* 运行阶段：循环定时检测是否有新增apk，有则开启新线程进行反汇编、分析、与数据库的交互等 */

	}

	/**
	 * 检查文件完整性<br />
	 * 检查列表：<br />
	 * 1. src/main/resources/conf下的db配置文件 <br />
	 * 2. src/main/resources/db_res下的db资源文件
	 * 
	 * @return
	 */
	public static boolean checkFiles() {
		// boolean isFilesExist = true;
		File dbresFileDir = new File(C.DB_RES_PATH);
		if (dbresFileDir.exists() && dbresFileDir.isDirectory()) {
			File[] files = dbresFileDir.listFiles();
			if (files.length == 0) {
				return false;
			}
		} else {
			return false;
		}

		File conf = new File(C.CONF_PATH_DB);
		if (!conf.exists()) {
			return false;
		}

		return true;
	}

	/**
	 * 检查数据库完整性<br />
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static boolean checkDb(Connection conn) throws SQLException,
			IOException {
		QueryRunner runner = new QueryRunner();
		List<Object[]> tables = runner.query(conn, "show tables",
				new ArrayListHandler());
		List<String> tableNames = new ArrayList<>();

		logger.debug("当前库中有{}张表", tables.size());
		for (Object[] table : tables) {
			if (table.length == 1) {
				logger.debug("table: {}", table[0]);
				tableNames.add((String) table[0]);
			}
		}

		if (!tableNames.contains("da_android_api")) {
			logger.info("表da_android_api不存在，建表并导入数据");
			DroidActDBUtils.createTable(C.DB_FILENAME_DA_ANDROID_API_CREATE,
					conn);
			DroidActDBUtils.importFromFileToTable(
					C.DB_FILENAME_DA_ANDROID_API_INSERT, conn,
					"da_android_api", 1024);
		}

		if (!tableNames.contains("da_methods")) {
			logger.info("表da_methods不存在，建表");
			DroidActDBUtils.createTable(C.DB_FILENAME_DA_METHODS_CREATE, conn);
		}

		return true;
	}
}
