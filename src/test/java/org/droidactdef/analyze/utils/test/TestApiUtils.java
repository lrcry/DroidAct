package org.droidactdef.analyze.utils.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.droidactdef.analyze.domains.MtdIncludeApi;
import org.droidactdef.analyze.utils.ApiUtils;
import org.junit.Test;

public class TestApiUtils {

	@Test
	public void test() throws SQLException, InterruptedException {
		// fail("Not yet implemented");
		DbUtils.loadDriver("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/droidact", "root", "admin");
		Map<String, MtdIncludeApi> mapMtdApi = ApiUtils
				.getFinalMtdNameIncludingApi(conn, "4f65245c31844079");
		
		System.out.println(mapMtdApi.size());
		
		Iterator<String> it = mapMtdApi.keySet().iterator();
		System.out.println("Result will display below after 5s...");
		Thread.sleep(5000);
		
		while (it.hasNext()) {
			String name = it.next();
			System.out.println("============== METHOD: " + name + " ==============");
			MtdIncludeApi mia = mapMtdApi.get(name);
			System.out.println(mia.toString());
		}
		
	}

}
