package org.droidactdef.analyze.utils.test;

import java.util.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.io.FileUtils;
import org.droidactdef.analyze.domains.BasicBlock;
import org.droidactdef.analyze.domains.CFNode;
import org.droidactdef.analyze.domains.TopLevelMtd;
import org.droidactdef.analyze.utils.ApiUtils;
import org.droidactdef.analyze.utils.FlowUtils;
import org.droidactdef.utils.DroidActDBUtils;

public class TestFlowUtilsEfficiency {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// Map<String, Map<Integer, CFNode>> cfMap = new HashMap<>();
		Map<Integer, CFNode> cfcf;
		String md5 = "4f65245c31844079";
		String mtdName = "Lcom/tebs3/cuttherope/MainActivity;->onCreate(Landroid/os/Bundle;)V";

		if (DbUtils.loadDriver("com.mysql.jdbc.Driver")) {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/droidact", "root", "admin");
			long start = System.currentTimeMillis();
			long end = 0L;
			// DroidActDBUtils.getAllMethodsBody(conn, "21CAA179",
			// "4f65245c31844079");
			Map<String, List<String>> mtds = DroidActDBUtils
					.getAllMethodsBodies(conn, "21CAA179", "4f65245c31844079");

			System.out.println("Getting cfg...");
			List<String> body = mtds.get(mtdName);
			body = FlowUtils.removeBlankLines(body);
			FlowUtils.removePswitchDefinition(body);
			List<BasicBlock> bbs = FlowUtils.getBasicBlockPartition(body,
					mtdName);
			cfcf = FlowUtils.getControlFlowGraph(bbs);
			cfcf = FlowUtils.getControlFlowGraphWithReturn(cfcf);

			end = System.currentTimeMillis();

			System.out.println("Costs: " + (end - start) + " ms");
			System.out.println("Now sleep 3s");
			Thread.sleep(3000);

			start = System.currentTimeMillis();
			System.out.println("Getting top levels ...");
			List<Object[]> allMnObj = DroidActDBUtils.getMethodsNames(conn,
					null, md5);
			List<String> allNames = DroidActDBUtils.convertObjListToStrList(
					allMnObj, 0);
			Map<String, TopLevelMtd> allTopLvs = ApiUtils
					.getTopLevelMtdsWithApi(conn, md5, allNames);

			System.out.println("Start simplifying");
			cfcf = FlowUtils.cfSimplify(cfcf, allTopLvs, mtdName);
			end = System.currentTimeMillis();
			System.out.println("complete in " + (end - start) + "ms, waiting 5s for results ...");
			Thread.sleep(5000);
			System.out.println("Here for results:");
			
			for (Map.Entry<Integer, CFNode> e : cfcf.entrySet()) {
				System.out.println("[[node: " + e.getKey() + "]]");
				System.out.println(e.getValue());
			}

		}
	}
}
