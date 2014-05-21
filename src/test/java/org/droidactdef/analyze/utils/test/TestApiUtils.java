package org.droidactdef.analyze.utils.test;

import static org.junit.Assert.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.dbutils.DbUtils;
import org.droidactdef.analyze.domains.MtdIncludeApi;
import org.droidactdef.analyze.domains.TopLevelMtd;
import org.droidactdef.analyze.utils.ApiUtils;
import org.droidactdef.utils.DroidActDBUtils;
import org.junit.Test;

public class TestApiUtils {

	@Test
	public void test() throws Exception {
		// fail("Not yet implemented");
		DbUtils.loadDriver("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/droidact", "root", "admin");
		
		List<Object[]> allMtdNamesObj = DroidActDBUtils.getMethodsNames(conn, null, "4f65245c31844079");
		List<String> allMtdNames = new ArrayList<>();
		for (Object[] o : allMtdNamesObj) {
			allMtdNames.add((String) o[0]);
		}
		
		Map<String, TopLevelMtd> mapMtdApi = ApiUtils
				.getTopLevelMtdsWithApi(conn, "4f65245c31844079", allMtdNames);
		System.out.println("get top level over, now sleeping 5s...");
		Thread.sleep(5000);
		Iterator<String> iter = mapMtdApi.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			System.out.println(mapMtdApi.get(name).toString());
		}
		
	}

}
