package org.droidactdef.analyze.utils;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.droidactdef.analyze.domains.*;
import org.droidactdef.commons.C;

/**
 * 处理流的工具类<br />
 * 
 * @author range
 * 
 */
public class FlowUtils {
	/*
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * 控制流获取
	 */
	/**
	 * 获取一个方法的控制流<br />
	 * 可能投机取巧的做法：没有足够的调研就判定每个smali方法中只有一个switch片段<br />
	 * 已修改为：将pswitch起点标号存入一个map中<br />
	 * 
	 * @param bbs
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<Integer, CFNode> getControlFlow(List<BasicBlock> bbs)
			throws Exception {
		Map<Integer, CFNode> cfMap = new HashMap<>();
//		List<CFNode> cf = new ArrayList<>();

		Map<Object, Object> idMap = new HashMap<>(); // 跳转表<当前ID,跳转至>
		Map<String, Integer> jmpLbMap = getJmpLabelMap(bbs); // 跳转标记表<标号,块ID>

		// System.out.println(jmpLbMap);
		List<BasicBlock> bbJmpLb = new ArrayList<>();
//		List<CFNode> jmpLbNodes = new ArrayList<>();
		Map<Integer, CFNode> jmpLbNodesMap = new HashMap<>();

		Map<Object, Object> pswitchMap = new HashMap<>(); // 跳转头尾表<pswitch分支,
															// pswitch起始>

		// int pswitchStart = -1;

		// if if-jump
		// if goto-jump
		// if packed-switch 跳转指令只有一个入口，多个出口（goto单入单出）
		// if jump-label 跳转标记有多个入口，一个出口
		// if return
		for (BasicBlock bb : bbs) {
			int curBbId = bb.getBlockId();
			if (bb.isJump()) {
				String jump = bb.getBlockBody().get(0);
				String jmpLb = bb.getJmpLabel();
				// System.out.println(jmpLb);
				int jmpTo = -1;
				if (lineIsCondJump(jump)) { // if if-jump
					jmpTo = jmpLbMap.get(jmpLb);

					List<Integer> prev = new ArrayList<>();
					prev.add(curBbId - 1);
					List<Integer> next = new ArrayList<>();
					next.add(curBbId + 1);
					next.add(jmpTo);

					CFNode node = setCFNodeValue(curBbId, prev, next, bb);
					cfMap.put(curBbId, node);
//					cf.add(node);
					idMap.put(curBbId, jmpTo);
				} else if (lineIsGotoJump(jump)) { // if goto-jump
					jmpTo = jmpLbMap.get(jmpLb);

					List<Integer> prev = new ArrayList<>();
					prev.add(curBbId - 1);
					List<Integer> next = new ArrayList<>();
					next.add(jmpTo);
					CFNode node = setCFNodeValue(curBbId, prev, next, bb);
					cfMap.put(curBbId, node);
//					cf.add(node);
					idMap.put(curBbId, jmpTo);
				} else { // if packed-switch
					System.out.println("is this packed switch?"
							+ bb.getBlockBody().get(0));
					// pswitchStart = curBbId;
					List<Integer> prev = new ArrayList<>();
					prev.add(curBbId - 1);
					List<Integer> next = getPswitchLabelFromMap(jmpLbMap);

					// 添加到pswitchmap
					for (int i : next) {
						pswitchMap.put(i, curBbId);
					}

					CFNode node = setCFNodeValue(curBbId, prev, next, bb);
					cfMap.put(curBbId, node);
//					cf.add(node);
				}
			} else if (isBbJmpLabel(bb)) { // 加入到jmpLbNodes，等到全部完成之后，通过idMap反推
				bbJmpLb.add(bb);
			} else { // 顺序执行
				// throw new Exception("??????????????????????");
				System.out
						.println("============= sequence or root node ==============="
								+ bb.getBlockId());
				List<Integer> prev = new ArrayList<>();
				prev.add(curBbId - 1);
				List<Integer> next = new ArrayList<>();
				next.add(curBbId + 1);
				CFNode node = setCFNodeValue(curBbId, prev, next, bb);
//				cf.add(setCFNodeValue(curBbId, prev, next, bb));
				cfMap.put(curBbId, node);
			}
		}

		// 反推,这里都是跳转标号的块
		if (idMap.size() > 0) {
			// System.out.println("ADD!" + bbJmpLb.size());
			for (BasicBlock bb : bbJmpLb) {
				int bbId = bb.getBlockId();
				List<String> body = bb.getBlockBody();
				// System.out.println("ft: " + bbId);

				List<Integer> prev = new ArrayList<>();
				List<Integer> next = new ArrayList<>();

				if (containsPswitchLabel(body)) { // pswitch label
					prev.add((Integer) pswitchMap.get(bbId));
				}

				// cond or goto
				prev.addAll((List<Integer>) getKeyByValueFromMap(idMap, bbId));
				next.add(bbId + 1);
				CFNode node = setCFNodeValue(bbId, prev, next, bb);
//				jmpLbNodes.add(node);
				jmpLbNodesMap.put(bbId, node);
			}

//			cf.addAll(jmpLbNodes);
			cfMap.putAll(jmpLbNodesMap);
		}

//		return cf;
		return cfMap;
	}

	/**
	 * 判断该基本块是否是跳转标号<br />
	 * 
	 * @param bb
	 * @return
	 */
	private static boolean isBbJmpLabel(BasicBlock bb) {
		List<String> body = bb.getBlockBody();
		if (lineIsJmpLabel(body.get(0))) {
			return true;
		}

		return false;
	}

	/**
	 * 获取基本块中跳转标号的信息<br />
	 * 
	 * @param bbs
	 * @return <标号，块号>
	 */
	private static Map<String, Integer> getJmpLabelMap(List<BasicBlock> bbs) {
		Map<String, Integer> jlMap = new HashMap<>();
		for (BasicBlock bb : bbs) {
			int bbId = bb.getBlockId();
			// System.out.println(bb.isJumpLabel());
			List<String> body = bb.getBlockBody();
			for (String line : body) {
				if (lineIsJmpLabel(line)) {
					jlMap.put(line, bbId);
				}

			}
		}

		return jlMap;
	}

	/**
	 * 设置控制流节点的字段值<br />
	 * 
	 * @param nodeId
	 * @param prev
	 * @param next
	 * @param bb
	 * @return
	 */
	private static CFNode setCFNodeValue(int nodeId, List<Integer> prev,
			List<Integer> next, BasicBlock bb) {
		CFNode node = new CFNode();
		node.setNodeId(nodeId);
		node.setPrev(prev);
		node.setNext(next);
		node.setBb(bb);
		return node;
	}

	/*
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * 基本块
	 */
	/**
	 * 获取方法中的基本块<br />
	 * 
	 * @param bodyLines
	 *            方法体
	 * @param mtdName
	 *            方法名
	 * @return 方法基本块集合
	 */
	public static List<BasicBlock> getBasicBlockPartition(
			List<String> bodyLines, String mtdName) {
		int bbLabel = 0;
		List<BasicBlock> bbs = new ArrayList<>();
		List<String> tempLines = new ArrayList<>();
		for (int i = 0; i < bodyLines.size(); i++) {
			String line = bodyLines.get(i);
			// System.out.print(line);
			if (lineIsJmp(line) || line.matches(C.PTN_PSWITCH_START)) { // goto
																		// goto/16
																		// if
																		// 指令作为一个基本块,
				// 此外packed-switch也应作为一个基本块，switch开始的地方
				// 先将以上的tempLines作为一个基本块存入，因为tempLines中存储的是顺序的
				if (tempLines.size() > 0) {
					List<String> bbpBody = new ArrayList<>(tempLines);
					BasicBlock bbPrevious = setBasicBlockValue(bbLabel,
							mtdName, false, false, "", -1, -1, bbpBody);
					bbs.add(bbPrevious);
					bbLabel++;
					tempLines.clear();
				}

				// 再保存goto / if基本块
				tempLines.add(line);
				List<String> bodyJmp = new ArrayList<>(tempLines);
				String labelJmp = findStringFromLineByRegex(line,
						C.PTN_GOTO_LABEL + "|" + C.PTN_IF_COND);
				int labelLineNum = bodyLines.indexOf(labelJmp);
				BasicBlock bbJmp = setBasicBlockValue(bbLabel, mtdName, true,
						false, labelJmp, labelLineNum, i, bodyJmp);
				bbs.add(bbJmp);
				bbLabel++;
				tempLines.clear();
				// System.out.println("跳转指令");
			} else if (lineIsJmpLabel(line)) { // 是跳转标记
				if (tempLines.size() > 0) {
					String prev = tempLines.get(tempLines.size() - 1);
					if (!lineIsJmpLabel(prev)) { // 如果上一个（tempLines的最后一个）不是标号，那就保存上一步，并将标号作为下一个基本块的起始点
						List<String> bbpBody = new ArrayList<>(tempLines);
						BasicBlock bbPrevious = setBasicBlockValue(bbLabel,
								mtdName, false, false, "", -1, -1, bbpBody);
						bbs.add(bbPrevious);
						bbLabel++;
						tempLines.clear();
					}
				}

				// 再将这个作为第一行
				tempLines.add(line);
				// System.out.println("跳转标记");
			} else { // 顺序执行的，只有在遇到跳转指令或跳转标记时才保存上一步的基本块，不管trycatch
				tempLines.add(line);
				// System.out.println("顺序执行");
			}
		}

		// 剩余的基本块信息写入
		if (tempLines.size() > 0) {
			BasicBlock bbRemain = setBasicBlockValue(bbLabel, mtdName, false,
					false, "", -1, -1, tempLines);
			bbs.add(bbRemain);
		}

		return bbs;
	}

	/**
	 * 设置基本块的值<br />
	 * 
	 * @param bbLabel
	 *            基本块标号
	 * @param mtdName
	 *            方法名
	 * @param isJump
	 *            是否为跳转指令
	 * @param isJumpLabel
	 *            是否为跳转标号
	 * @param jmpLabel
	 *            如果是跳转指令，其跳转标号
	 * @param jmpToLineNumber
	 *            如果是跳转指令，其跳转标号所在行（用于判断循环）
	 * @param blockBody
	 *            基本块指令体
	 * @return
	 */
	private static BasicBlock setBasicBlockValue(int bbLabel, String mtdName,
			boolean isJump, boolean isJumpLabel, String jmpLabel,
			int jmpToLineNumber, int curLineNum, List<String> blockBody) {
		BasicBlock bb = new BasicBlock();
		bb.setBlockId(bbLabel);
		bb.setMtdName(mtdName);
		bb.setJump(isJump);
		bb.setJumpLabel(isJumpLabel);
		bb.setJmpLabel(jmpLabel);
		bb.setJmpToLineNumber(jmpToLineNumber);
		bb.setCurLineNumber(curLineNum);
		bb.setBlockBody(blockBody);
		return bb;
	}

	/*
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * 判断行中的跳转
	 */
	/**
	 * 判断是否包含分支跳转pswitch标号<br />
	 * 
	 * @param body
	 * @return
	 */
	public static boolean containsPswitchLabel(List<String> body) {
		for (String line : body) {
			if (lineIsPswitchLabel(line)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 判断当前指令是否为跳转标号<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsJmpLabel(String line) {
		return lineIsCondlabel(line) || lineIsGotoLabel(line)
				|| lineIsPswitchLabel(line);
	}

	/**
	 * 判断当前指令是否为跳转<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsJmp(String line) {
		return lineIsGotoJump(line) || lineIsCondJump(line);
	}

	/**
	 * 判断当前指令是否是分支跳转定义<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsPswitchDefinition(String line) {
		return matchStringFromLineByRegex(line, C.PTN_PSWITCH_DATA);
	}

	/**
	 * 判断当前指令是否是分支跳转标记<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsPswitchLabel(String line) {
		return matchStringFromLineByRegex(line, C.PTN_PSWITCH_LABEL);
	}

	/**
	 * 判断当前指令是否为goto跳转标记<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsGotoLabel(String line) {
		return matchStringFromLineByRegex(line, C.PTN_GOTO_LABEL);
	}

	/**
	 * 判断当前指令是否为if跳转标记<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsCondlabel(String line) {
		return matchStringFromLineByRegex(line, C.PTN_IF_COND);
	}

	/**
	 * 判断当前行是否为无条件跳转<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsGotoJump(String line) {
		return matchStringFromLineByRegex(line, C.PTN_GOTO)
				|| matchStringFromLineByRegex(line, C.PTN_GOTO_16);
	}

	/**
	 * 判断当前行是否为条件跳转<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsCondJump(String line) {
		return matchStringFromLineByRegex(line, C.PTN_IF);
	}

	/*
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * 正则表达式
	 */
	/**
	 * 根据正则表达式匹配（必须完全匹配）行中的字符串<br />
	 * 
	 * @param line
	 * @param regex
	 * @return 是否匹配
	 */
	private static boolean matchStringFromLineByRegex(String line, String regex) {
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
	private static String findStringFromLineByRegex(String line, String regex) {
		Pattern ptn = Pattern.compile(regex);
		Matcher mch = ptn.matcher(line);
		String str = new String();
		if (mch.find()) {
			str = mch.group();
		}

		return str;
	}

	/*
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * 预处理
	 */
	/**
	 * 对行进行预处理
	 * 
	 * @param lines
	 */
	public static void preHandleLines(List<String> lines) {
		removeBlankLines(lines);
		removePswitchDefinition(lines);
	}

	/**
	 * 移除空行<br />
	 * 
	 * @param lines
	 */
	public static void removeBlankLines(List<String> lines) {
		List<String> removedLines = new ArrayList<>();
		removedLines.add("");
		removedLines.add(C.CRLF);
		lines.removeAll(removedLines);
	}

	/**
	 * 从方法体删除packedswitch定义<br />
	 * 
	 * @param lines
	 */
	public static void removePswitchDefinition(List<String> lines) {
		if (!lines.contains(":pswitch_0")) // 不存在pswitch，直接返回
			return;
		else {
			for (String line : lines) {
				if (line.matches(C.PTN_PSWITCH_DATA)) {
					int pswitchDataStart = lines.indexOf(line);
					int pswitchDataEnd = lines.indexOf(C.PSWITCH_DATA_END);
					// System.out.println("Start=" + pswitchDataStart + ", end="
					// + pswitchDataEnd + ", size=" + lines.size());
					for (int i = pswitchDataEnd; i >= pswitchDataStart; i--) {
						// System.out.println("remove " + i);
						lines.remove(i);
					}

					break;
				}
			}
		}

		// lines.removeAll(removedLines);
	}

	/**
	 * 从跳转标号列表中获取pswitch标号<br />
	 * 
	 * @param jmpLbMap
	 * @return 跳转标号块号列表
	 */
	public static List<Integer> getPswitchLabelFromMap(
			Map<String, Integer> jmpLbMap) {
		List<Integer> idList = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : jmpLbMap.entrySet()) {
			String key = entry.getKey();
			if (matchStringFromLineByRegex(key, C.PTN_PSWITCH_LABEL)) {
				idList.add(entry.getValue());
			}
		}

		return idList;
	}

	/**
	 * 根据Map的值获取键<br />
	 * 
	 * @param map
	 * @param value
	 * @return
	 */
	public static List<? extends Object> getKeyByValueFromMap(
			Map<Object, Object> map, Object value) {
		// System.out.println("getkeybyvaluefrommap-mapsize: " + map.size());
		List<Object> keys = new ArrayList<>();
		Iterator<Entry<Object, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Object> entry = (Entry<Object, Object>) it.next();
			Object val = entry.getValue();
			// System.out.println("getkeybyvaluefrommap-val: " + val);
			if (val != null && val.equals(value)) {
				keys.add(entry.getKey());
			}
		}

		System.out.println(keys.toString());

		return keys;
	}
}
