package org.droidactdef.analyze.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.droidactdef.analyze.SmaliFlowAnalyzer;
import org.droidactdef.commons.C;
import org.droidactdef.domains.ControlFlowNode;
import org.droidactdef.domains.FlowNode;

/**
 * smali控制流分析器的实现<br />
 * 
 * @author range
 * 
 */
public class ControlFlowAnalyzer implements SmaliFlowAnalyzer {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		

		List<String> lines = FileUtils.readLines(
				new File("cfTest_noJump.smali"), "UTF-8");

		Map<Integer, List<FlowNode>> flowMap = new ControlFlowAnalyzer()
				.getFlow(lines, null);
		List<FlowNode> flow = flowMap.get(1);
		for (FlowNode node : flow) {
			System.out.println(node.toString());
		}

	}

	@Override
	public void analyze(Object... params) {
		// TODO Auto-generated method stub

	}

	// 顺序的控制流获取
	@Override
	public Map<Integer, List<FlowNode>> getFlow(List<String> lines,
			Map<String, Boolean> genMap) {
		// TODO Auto-generated method stub
		int flowLevel = 0;
		Map<Integer, List<FlowNode>> flowMap = new TreeMap<>();
		List<FlowNode> flow = new ArrayList<>();
		Pattern ptn = Pattern.compile(C.PTN_METHOD);
		Matcher mch = null;
		for (String line : lines) {
			mch = ptn.matcher(line);
			if (mch.find()) {
				String mtd = mch.group();
				// System.out.println(mtd);
				ControlFlowNode node = new ControlFlowNode();
				node.setNodeId(flowLevel);
				node.setMtdName(mtd);
				node.setApi(false);
				if (flowLevel == 0)
					node.setParentNodeId(-1);
				else
					node.setParentNodeId(flowLevel - 1);

				flow.add(node);
				flowLevel++;
			}
		}

		flowMap.put(1, flow);

		return flowMap;
	}

	/*
	 * ================== Jump ==================
	 * 
	 * 跳转指令相关
	 */
	/**
	 * 获得方法体中所有跳转标号<br />
	 * 
	 * @param mtdBody
	 * @return <line number, line content>
	 */
	public Map<Integer, String> getJumpLabel(List<String> mtdBody) {
		Map<Integer, String> jumpLb = new HashMap<>();
		for (int i = 0; i < mtdBody.size(); i++) {
			String line = mtdBody.get(i);
			if (matchStringFromLineByRegex(line, C.PTN_IF_COND + "|"
					+ C.PTN_GOTO_LABEL))
				jumpLb.put(i, line);
		}

		return jumpLb;
	}

	/**
	 * 获取方法体中所有跳转指令集合<br />
	 * 
	 * @param mtdBody
	 *            方法体
	 * @return <line number, line content>
	 */
	public Map<Integer, String> getJumpIns(List<String> mtdBody) {
		Map<Integer, String> jumpIns = new HashMap<>();
		for (int i = 0; i < mtdBody.size(); i++) {
			String line = mtdBody.get(i);
			if (lineIsCondJump(line))
				jumpIns.put(i, line);
			else if (lineIsGotoJump(line))
				jumpIns.put(i, line);

		}

		return jumpIns;
	}

	/**
	 * 得到无条件跳转的标号<br />
	 * 
	 * @param line
	 * @return
	 */
	public String getGotoLabel(String line) {
		return findStringFromLineByRegex(line, C.PTN_GOTO_LABEL);
	}

	/**
	 * 得到条件跳转的标号<br />
	 * 
	 * @param line
	 *            当前行
	 * @return 标号cond_x
	 */
	public String getIfCondLabel(String line) {
		return findStringFromLineByRegex(line, C.PTN_IF_COND);
	}

	/**
	 * 得到条件跳转的类型<br />
	 * 
	 * @param line
	 * @return
	 */
	public String getIfType(String line) {
		return findStringFromLineByRegex(line, C.PTN_IF_IF);
	}

	/**
	 * 判断当前行是否为无条件跳转<br />
	 * 
	 * @param line
	 * @return
	 */
	public boolean lineIsGotoJump(String line) {
		return matchStringFromLineByRegex(line, C.PTN_GOTO)
				|| matchStringFromLineByRegex(line, C.PTN_GOTO_16);
	}

	/**
	 * 判断当前行是否为条件跳转<br />
	 * 
	 * @param line
	 * @return
	 */
	public boolean lineIsCondJump(String line) {
		return matchStringFromLineByRegex(line, C.PTN_IF);
	}

	/*
	 * ================== Method Invocation ==================
	 * 
	 * 获取方法调用信息
	 */
	/**
	 * 获取方法的全部名称<br />
	 * 
	 * @param line
	 *            当前行
	 * @return 方法全名
	 */
	public String getMethodName(String line) {
		return findStringFromLineByRegex(line, C.PTN_METHOD);
	}

	/**
	 * 获取一个方法调用使用的寄存器数组<br />
	 * 
	 * @param line
	 *            当前行
	 * @return 寄存器数组
	 */
	public String[] getMethodArgs(String line) {
		String oriArgs = new String();
		oriArgs = findStringFromLineByRegex(line, C.PTN_METHOD_REGS);

		oriArgs = StringUtils.substringBetween(oriArgs, "{", "}");
		oriArgs = StringUtils.replace(oriArgs, " ", "");
		String[] args = oriArgs.split(",");
		return args;
	}

	/**
	 * 获取一个方法调用的类型： invoke-kind[/range][/jumbo(starting from Android 4.0)]<br />
	 * 1. invoke-static for static methods<br />
	 * 2. invoke-virtual for public/protected ones<br />
	 * 3. invoke-direct for private ones<br />
	 * 4. invoke-interface probably AIDL-like invocation<br />
	 * 5. invoke-super for super invocation<br />
	 * 
	 * @param line
	 *            当前行
	 * @return 调用类型
	 */
	public String getMethodInvokeType(String line) {
		return findStringFromLineByRegex(line, C.PTN_METHOD_INVOKE);
	}

	/*
	 * ================== Regex ==================
	 * 
	 * 正则表达式
	 */
	/**
	 * 根据正则表达式匹配（必须完全匹配）行中的字符串<br />
	 * 
	 * @param line
	 * @param regex
	 * @return 是否匹配
	 */
	private boolean matchStringFromLineByRegex(String line, String regex) {
		Pattern ptn = Pattern.compile(regex);
		Matcher mch = ptn.matcher(line);
		return mch.matches();
	}

	/**
	 * 根据正则表达式查找行中包含的字符串<br />
	 * 
	 * @param line
	 *            当前行
	 * @param regex
	 *            正则表达式
	 * @return 字符串
	 */
	private String findStringFromLineByRegex(String line, String regex) {
		Pattern ptn = Pattern.compile(regex);
		Matcher mch = ptn.matcher(line);
		String str = new String();
		if (mch.find()) {
			str = mch.group();
		}

		return str;
	}
}
