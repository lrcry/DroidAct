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
import org.droidactdef.analyze.utils.FlowUtils;
import org.droidactdef.utils.DroidActDBUtils;

public class TestFlowUtilsEfficiency {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Map<Integer, CFNode>> cfMap = new HashMap<>();

		if (DbUtils.loadDriver("com.mysql.jdbc.Driver")) {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/droidact", "root", "admin");
			long start = System.currentTimeMillis();
			long end = 0L;
			// DroidActDBUtils.getAllMethodsBody(conn, "21CAA179",
			// "4f65245c31844079");
			Map<String, List<String>> mtds = DroidActDBUtils
					.getAllMethodsBodies(conn, "21CAA179", "4f65245c31844079");

			for (Map.Entry<String, List<String>> entry : mtds.entrySet()) {
				String name = entry.getKey();
				// System.out.println("+++++++++++++++++++ Current method name: "
				// + name + "+++++++++++++++++++");
				List<String> body = entry.getValue();
				// System.out.println("=========================== body ======================");

				List<String> bodyNew = FlowUtils.removeBlankLines(body);
				FlowUtils.removePswitchDefinition(bodyNew);
				// for (int i = 0; i < bodyNew.size(); i++) {
				// System.out.println(bodyNew.get(i));
				// }
				List<BasicBlock> bbs = FlowUtils.getBasicBlockPartition(
						bodyNew, name);
				Map<Integer, CFNode> cf = FlowUtils.getControlFlowGraph(bbs);
				Map<Integer, CFNode> cfFinal = FlowUtils.getControlFlowGraphWithReturn(cf);
				// mtds.remove(name);
				// System.out.println("Now sleep: 2000ms");
				// Thread.sleep(2000);
				cfMap.put(name, cfFinal);
//				cfMap.put(name, cf);
			}

			end = System.currentTimeMillis();

			System.out.println("Costs: " + (end - start) + " ms");
			System.out.println(cfMap.get(-1));
			Thread.sleep(2000);

			List<String> cfList = new ArrayList<>();
			
			for (Map.Entry<String, Map<Integer, CFNode>> entry : cfMap
					.entrySet()) {
				String name = entry.getKey();
				System.out.println("======================== name: "
						+ name + " ========================");
				cfList.add("======================== name: "
						+ name + " ========================");
				Map<Integer, CFNode> cf = entry.getValue();
				for (Map.Entry<Integer, CFNode> mEntry : cf.entrySet()) {
					System.out.println("I'm here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					System.out.println("-------------- node start --------------");
					cfList.add("-------------- node start --------------");
					
					int k = mEntry.getKey();
					
					if (k == -1) {
						continue;
					}
					
					CFNode v = mEntry.getValue();
					System.out.println("k=" + k + ", v is null? " + v == null);
					System.out.println("Node_" + k);
					cfList.add("Node_" + k);
					System.out.println(v);
					cfList.add(v.toString());
					System.out.println("-------------- node end --------------");
					cfList.add("-------------- node end --------------");
				}
				
				System.out.println("=======++=======++=======++=======++=======");
				cfList.add("=======++=======++=======++=======++=======");
			}
			
//			FileUtils.writeLines(new File("cfresult.txt"), "UTF-8", cfList, null, false);
			
		}
	}
}
