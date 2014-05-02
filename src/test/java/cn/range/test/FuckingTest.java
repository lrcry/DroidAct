package cn.range.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.droidactdef.analyze.domains.BasicBlock;
import org.droidactdef.analyze.domains.CFNode;
import org.droidactdef.analyze.utils.FlowUtils;
import org.droidactdef.commons.C;

public class FuckingTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		List<String> lines = FileUtils.readLines(new File("cfTest_complexTryCatch.smali"), "UTF-8");
		List<String> trimedLines = new ArrayList<>();
		for (String line : lines) {
			trimedLines.add(line.trim());
		}
		FlowUtils.removeBlankLines(trimedLines);
		FlowUtils.removePswitchDefinition(trimedLines);
		
		// 获取基本块划分
		List<BasicBlock> bbs = FlowUtils.getBasicBlockPartition(trimedLines, "lovewhatwhat");
		for (BasicBlock bb : bbs) {
			System.out.println(bb.toString());
		}
		Thread.sleep(5000);
		
		// 获取控制流图
		Map<Integer, CFNode> cf = FlowUtils.getControlFlowGraph(bbs);
		cf = FlowUtils.getControlFlowGraphWithReturn(cf);
		System.out.println("+++++++++++++++++++++ CF start +++++++++++++++++++++");
		for (Map.Entry<Integer, CFNode> entry : cf.entrySet()) {
//			if (node.getPrev().size() == 0 || node.getNext().size() == 0) {
			System.out.println("--------------- ID=" + entry.getKey() + " ---------------");
			CFNode node = entry.getValue();
				System.out.println(node.toString());
				System.out.println("+====+====+====+====+====+====+");
//			}
		}
		
		// Why don't try adjacent matrix?
		/**/
		int[][] adjMx = new int[cf.size()][cf.size()];
		for (int i = 0; i < cf.size(); i++) {
			CFNode node = cf.get(i);
			int id = node.getNodeId();
			List<Integer> prev = node.getPrev();
			List<Integer> next = node.getNext();
			
			if (prev.get(0) > 0)
				for (int prevInt : prev) {
					adjMx[prevInt][id] = 1;
				}
			
			for (int nextInt : next) {
				if (nextInt != -1)
					adjMx[id][nextInt] = 1;
			}
		}
		
		for (int i = 0; i < adjMx.length; i++) {
			for (int j = 0; j < adjMx[i].length; j++) {
				System.out.print(adjMx[i][j] + " ");
			}
			System.out.println();
		}
	}

}
