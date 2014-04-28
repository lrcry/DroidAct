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
		List<String> lines = FileUtils.readLines(new File("cfTest_eg2.smali"), "UTF-8");
		List<String> trimedLines = new ArrayList<>();
		for (String line : lines) {
			trimedLines.add(line.trim());
		}
		FlowUtils.removeBlankLines(trimedLines);
		FlowUtils.removePswitchDefinition(trimedLines);
		
		// 获取基本块划分
		List<BasicBlock> bbs = FlowUtils.getBasicBlockPartition(trimedLines, "lovewhatwhat");
//		for (BasicBlock bb : bbs) {
//			System.out.println(bb.toString());
//			System.out.println("+====+====+====+====+====+====+");
//		}
		
		// 获取控制流
		Map<Integer, CFNode> cf = FlowUtils.getControlFlow(bbs);
		System.out.println("+++++++++++++++++++++ CF start +++++++++++++++++++++");
		for (Map.Entry<Integer, CFNode> entry : cf.entrySet()) {
//			if (node.getPrev().size() == 0 || node.getNext().size() == 0) {
			System.out.println("--------------- ID=" + entry.getKey() + " ---------------");
			CFNode node = entry.getValue();
				System.out.println(node.toString());
				System.out.println("+====+====+====+====+====+====+");
//			}
		}
		
	}
}
