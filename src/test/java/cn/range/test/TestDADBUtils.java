package cn.range.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.droidactdef.utils.DroidActDBUtils;
import org.junit.Test;

public class TestDADBUtils {

	@Test
	public void test() throws Exception {
		DbUtils.loadDriver("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/droidact", "root", "admin");
		List<Object[]> result = DroidActDBUtils
				.getMethodsInvokingAPI(
						conn,
						"Landroid/telephony/TelephonyManager;->getDeviceId()Ljava/lang/String;",
						"21CAA179", "4f65245c31844079");

		
		for (int i = 0; i < result.size(); i++) {
			Object[] temp = result.get(i);
			for (int j = 0; j < temp.length; j++) {
				System.out.print(temp[j] + "\t");

			}
			System.out.println();
		}

	}

	// @Test
	public void testSqlBuilder() throws Exception {
		DbUtils.loadDriver("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/droidact", "root", "admin");

		List<String> permissions = new ArrayList<>();
		permissions.add("android.permission.INTERNET");
		permissions.add("android.permission.READ_CONTACTS");
		List<Object[]> result = DroidActDBUtils.getApiByPermission(conn,
				permissions);
		for (int i = 0; i < result.size(); i++) {
			Object[] temp = result.get(i);
			for (int j = 0; j < temp.length; j++) {
				System.out.println(temp[j]);
			}
		}
		/*
		 * QueryRunner runner = new QueryRunner(); List<Object[]> result =
		 * runner.query(conn,
		 * "select * from da_android_api where api_name like 'nothing'", new
		 * ArrayListHandler()); System.out.println(result.size());
		 */
	}
}
