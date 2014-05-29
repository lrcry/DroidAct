package org.droidactdef.analyze.utils.test;

import static org.junit.Assert.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.dbutils.DbUtils;
import org.droidactdef.analyze.commons.ApiConst;
import org.droidactdef.analyze.domains.MtdIncludeApi;
import org.droidactdef.analyze.domains.TopLevelMtd;
import org.droidactdef.analyze.utils.ApiUtils;
import org.droidactdef.utils.DroidActDBUtils;
import org.droidactdef.utils.RegexUtils;
import org.junit.Test;

public class TestApiUtils {

	@Test
	public void test() throws Exception {
		// fail("Not yet implemented");
		DbUtils.loadDriver("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/droidact", "root", "admin");
		
		Map<String, List<String>> bodies = 
				DroidActDBUtils.getAllMethodsBodies(conn, null, "b72397f676d3adf6");
		Set<String> apis = new HashSet<>();
		for (Map.Entry<String, List<String>> e : bodies.entrySet()) {
//			System.out.println("============== method = " + e.getKey() + " ==============");
			for (String line : e.getValue()) {
				String found = RegexUtils.findStringFromLineByRegex(line, ApiConst.REGEX_ANDROID_API);
				if (found != null && found.length() > 0)
					apis.add(found);
			}
		}
		
		for (String api : apis) {
			System.out.println(api);
		}
		
		/*
		List<Object[]> allMtdNamesObj = DroidActDBUtils.getMethodsNames(conn, null, "397d909465bac2fa");
		List<String> allMtdNames = new ArrayList<>();
		for (Object[] o : allMtdNamesObj) {
			allMtdNames.add((String) o[0]);
		}
		
		Map<String, TopLevelMtd> mapMtdApi = ApiUtils
				.getTopLevelMtdsWithApi(conn, "397d909465bac2fa", allMtdNames);
		System.out.println("get top level over, now sleeping 5s...");
		Thread.sleep(5000);
		Iterator<String> iter = mapMtdApi.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			TopLevelMtd tlm = mapMtdApi.get(name);
			if (tlm.getApis() != null && tlm.getApis().size() > 0)
				System.out.println(tlm.toString());
		}*/
		
	}

}
