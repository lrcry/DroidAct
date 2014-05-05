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
	 * 整理CF将CF的终点的后继节点修改为-1<br />
	 * 
	 * @param cfOriginal
	 *            没有进行终点化处理的cf
	 * @return 将终点处理的cf
	 */
	public static Map<Integer, CFNode> getControlFlowGraphWithReturn(
			Map<Integer, CFNode> cfOriginal) {
		int idReturn = -1;
		CFNode nodeReturn = null;
		for (Map.Entry<Integer, CFNode> entry : cfOriginal.entrySet()) {
			int curId = entry.getKey();
			CFNode node = entry.getValue();
			if (isBbReturn(node.getBb())) {
				idReturn = curId;
				List<Integer> next = new ArrayList<>();
				next.add(-1);
				node.setNext(next);
				nodeReturn = node;
				break;
			}
		}

		cfOriginal.put(idReturn, nodeReturn);
		return cfOriginal;
	}

	/**
	 * 获取一个方法的控制流<br />
	 * 可解析：顺序节点，if-xxx, goto, cond_x, goto_x, pswitch_x, packed-switch,
	 * try-catch<br />
	 * 
	 * 已修改为：将pswitch起点标号存入一个map中<br />
	 * 通过另一方法<code>getControlFlowGraphWithReturn</code>来获取带终点的控制流<br />
	 * 
	 * @param bbs
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<Integer, CFNode> getControlFlowGraph(List<BasicBlock> bbs)
			throws Exception {
		// System.out.println("This is getControFlow executing");
		Map<Integer, CFNode> cfMap = new HashMap<>();
		// List<CFNode> cf = new ArrayList<>();

		Map<Object, Object> idMap = new HashMap<>(); // 跳转表<当前ID,跳转至>
		Map<String, Integer> jmpLbMap = getJmpLabelMap(bbs); // 跳转标记表<标号,块ID>

		// for (Map.Entry<String, Integer> entry : jmpLbMap.entrySet()) {
		// System.out.println(entry.getKey());
		// }
		// System.out.println("==============");
		// System.out.println(jmpLbMap);
		List<BasicBlock> bbJmpLb = new ArrayList<>();
		// List<CFNode> jmpLbNodes = new ArrayList<>();
		Map<Integer, CFNode> jmpLbNodesMap = new HashMap<>();

		Map<Object, Object> pswitchMap = new HashMap<>(); // 跳转头尾表<pswitch分支,
															// pswitch起始>

		Map<Object, Object> mapTryCatch = new HashMap<>(); // try-catch表<catch标号,
															// catch起始>

		// int pswitchStart = -1;

		// if if-jump
		// if goto-jump
		// if packed-switch 跳转指令只有一个入口，多个出口（goto单入单出）
		// if jump-label 跳转标记有多个入口，一个出口
		// if return
		for (BasicBlock bb : bbs) {
			int curBbId = bb.getBlockId();
			if (bb.isJump()) {
				// System.out.println("I'm coming in!");
				String jump = bb.getBlockBody().get(0);
				String jmpLb = bb.getJmpLabel();
				// System.out.println(jmpLb);
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
					// cf.add(node);
					idMap.put(curBbId, jmpTo);
				} else if (lineIsGotoJump(jump)) { // if goto-jump
					jmpTo = jmpLbMap.get(jmpLb);

					List<Integer> prev = new ArrayList<>();
					prev.add(curBbId - 1);
					List<Integer> next = new ArrayList<>();
					next.add(jmpTo);
					CFNode node = setCFNodeValue(curBbId, prev, next, bb);
					cfMap.put(curBbId, node);
					// cf.add(node);
					idMap.put(curBbId, jmpTo);
				} else if (lineIsCatchIns(jump)) { // if catch
					// System.out.println(jmpLbMap == null);
					jmpTo = jmpLbMap.get(jmpLb);
					List<Integer> prev = new ArrayList<>();
					prev.add(curBbId - 1);
					List<Integer> next = new ArrayList<>();
					next.add(curBbId + 1); // 下一个，或跳转至的catch标号
					next.add(jmpTo);

					mapTryCatch.put(curBbId, jmpTo);

					CFNode node = setCFNodeValue(curBbId, prev, next, bb);
					cfMap.put(curBbId, node);
					idMap.put(curBbId, jmpTo);
					// System.out.println("Catch Ins -- get!!<" + curBbId + ", "
					// + jmpTo + ">");
				} else { // if packed-switch
					// System.out.println("is this packed switch?"
					// + bb.getBlockBody().get(0));
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
					// cf.add(node);
				}
			} else if (isBbJmpLabel(bb)) { // 加入到jmpLbNodes，等到全部完成之后，通过idMap反推
				bbJmpLb.add(bb);
			} else { // 顺序执行

				List<Integer> prev = new ArrayList<>();
				prev.add(curBbId - 1);
				List<Integer> next = new ArrayList<>();
				next.add(curBbId + 1);
				CFNode node = setCFNodeValue(curBbId, prev, next, bb);
				// cf.add(setCFNodeValue(curBbId, prev, next, bb));
				cfMap.put(curBbId, node);
			}
		} // 终点从外面考虑

		// 反推,这里都是跳转标号的块
		if (idMap.size() > 0) {
			// System.out.println("ADD!" + bbJmpLb.size());
			for (BasicBlock bb : bbJmpLb) {
				int bbId = bb.getBlockId();
				List<String> body = bb.getBlockBody();
				// System.out.println("ft: " + bbId);

				List<Integer> prev = new ArrayList<>();
				List<Integer> next = new ArrayList<>();

				// System.out.println(containsCatchLabel(body) + "--"
				// + body.get(0));

				if (containsCatchLabel(body)) { // catch标号是顺序执行不到的，且只能通过catch指令跳转到，因此catch标号不能添加到其他跳转
					// catch标号可以从多个catch指令过来
					// System.out.println("---------------- this is catch label speaking: "
					// + bbId + " ----------------");
					prev = (List<Integer>) getKeyByValueFromMap(mapTryCatch,
							bbId);

					next.add(bbId + 1);
					CFNode node = setCFNodeValue(bbId, prev, next, bb);
					jmpLbNodesMap.put(bbId, node);
					continue;
				}

				if (containsPswitchLabel(body)) { // pswitch
													// label，顺序和跳转可能执行到，因此还要增加到标号中
					prev.add((Integer) pswitchMap.get(bbId));
				}

				// cond or goto
				prev.addAll((List<Integer>) getKeyByValueFromMap(idMap, bbId));
				next.add(bbId + 1);
				CFNode node = setCFNodeValue(bbId, prev, next, bb);
				// jmpLbNodes.add(node);
				jmpLbNodesMap.put(bbId, node);
			}

			// cf.addAll(jmpLbNodes);
			cfMap.putAll(jmpLbNodesMap);
		}

		// return cf;
		return cfMap;
	}

	/**
	 * 判断基本块是否为终点<br />
	 * 
	 * @param bb
	 * @return
	 */
	private static boolean isBbReturn(BasicBlock bb) {
		List<String> body = bb.getBlockBody();
		for (String line : body) {
			if (lineIsReturn(line))
				return true;
		}

		return false;
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
	 * cond_x, goto_x, pswitch_x, catch_x<br />
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
				if (lineIsJmpLabel(line) || lineIsCatchLabel(line)) {
					// System.out.println("getjmplabelmap = " + line);
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
			if (lineIsJmp(line) || line.matches(C.PTN_PSWITCH_START)
					|| lineIsCatchIns(line)) { // goto, goto/16, if指令作为一个基本块,
				// 此外packed-switch也应作为一个基本块，switch开始的地方
				// catch语句也是基本块
				// 先将以上的tempLines作为一个基本块存入，因为tempLines中存储的是顺序的
				if (tempLines.size() > 0) {
					List<String> bbpBody = new ArrayList<>(tempLines);
					BasicBlock bbPrevious = setBasicBlockValue(bbLabel,
							mtdName, false, false, "", -1, -1, bbpBody);
					bbs.add(bbPrevious);
					bbLabel++;
					tempLines.clear();
				}

				// 再保存goto / if基本块, packed-switch, catch语句
				tempLines.add(line);
				List<String> bodyJmp = new ArrayList<>(tempLines);
				String labelJmp = findStringFromLineByRegex(line,
						C.PTN_GOTO_LABEL + "|" + C.PTN_IF_COND + "|"
								+ C.PTN_CATCH_LABEL + "|"
								+ C.PTN_CATCHALL_LABEL);
				int labelLineNum = bodyLines.indexOf(labelJmp);
				BasicBlock bbJmp = setBasicBlockValue(bbLabel, mtdName, true,
						false, labelJmp, labelLineNum, i, bodyJmp);
				bbs.add(bbJmp);
				bbLabel++;
				tempLines.clear();
				// System.out.println("跳转指令");
			} else if (lineIsJmpLabel(line) || lineIsCatchLabel(line)) { // 是跳转标记或catch标记
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
	 * 判断当前行是否是终点
	 */
	/**
	 * 判断当前行是否是return语句<br />
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsReturn(String line) {
		return lineIsReturnReg(line) || lineIsReturnObject(line)
				|| lineIsReturnVoid(line) || lineIsReturnWide(line);
	}

	private static boolean lineIsReturnWide(String line) {
		// TODO Auto-generated method stub
		String returnWide = findStringFromLineByRegex(line, C.PTN_RETURN_WIDE);
		if (returnWide == null || returnWide.equals(""))
			return false;
		else return true;
	}

	/**
	 * 判断是否是return vx<br />
	 * 
	 * @param line
	 * @return
	 */
	private static boolean lineIsReturnReg(String line) {
		return matchStringFromLineByRegex(line, C.PTN_RETURN);
	}

	/**
	 * 判断是否是return-void<br />
	 * 
	 * @param line
	 * @return
	 */
	private static boolean lineIsReturnVoid(String line) {
		return matchStringFromLineByRegex(line, C.PTN_RETURN_VOID);
	}

	/**
	 * 判断是否是return-object<br />
	 * 
	 * @param line
	 * @return
	 */
	private static boolean lineIsReturnObject(String line) {
		String str = findStringFromLineByRegex(line, C.PTN_RETURN_OBJECT);

		if (str != null && !str.equals(""))
			return true;
		else
			return false;
	}

	/*
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * *********************************************************
	 * 判断行中的try catch
	 */
	/**
	 * 判断当前行是否为catch标号
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsCatchLabel(String line) {
		return matchStringFromLineByRegex(line, C.PTN_CATCH_LABEL)
				|| matchStringFromLineByRegex(line, C.PTN_CATCHALL_LABEL);
	}

	/**
	 * 判断当前行是否为catch指令
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsCatchIns(String line) {
		return matchStringFromLineByRegex(line, C.PTN_TRY_CATCHALL_CATCH)
				|| matchStringFromLineByRegex(line, C.PTN_TRY_CATCH_CATCH);
	}

	/**
	 * 取得catch指令中的catch标号
	 * 
	 * @param line
	 * @return
	 */
	public static String getCatchLabelFromCatchIns(String line) {
		return findStringFromLineByRegex(line, C.PTN_CATCH_LABEL)
				+ findStringFromLineByRegex(line, C.PTN_CATCHALL_LABEL);
	}

	/**
	 * 当前行是否为try块开始
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsTryStart(String line) {
		return matchStringFromLineByRegex(line, C.PTN_TRY_START);
	}

	/**
	 * 当前行是否为try块结束
	 * 
	 * @param line
	 * @return
	 */
	public static boolean lineIsTryEnd(String line) {
		return matchStringFromLineByRegex(line, C.PTN_TRY_END);
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
	 * 判断是否包含catch标号<br />
	 * 
	 * @param body
	 * @return
	 */
	public static boolean containsCatchLabel(List<String> body) {
		for (String line : body) {
			if (lineIsCatchLabel(line)) {
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
				|| lineIsPswitchLabel(line) || lineIsCatchLabel(line);
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
	public static List<String> removeBlankLines(List<String> lines) {
		List<String> removedLines = new ArrayList<>();
		removedLines.add("");
		removedLines.add(C.CRLF);
		lines.removeAll(removedLines);

		List<String> linesNew = new ArrayList<>();
		for (String line : lines) {
			line = line.trim();
			// System.out.println("-----------removeblanklines---------");
			// System.out.println(line);
			linesNew.add(line);
		}

		return linesNew;
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
		return getLabelFromJumpLabelMap(jmpLbMap, C.PTN_PSWITCH_LABEL);
	}

	/**
	 * 从跳转标号列表中获取catch标号<br />
	 * 
	 * @param jmpLbMap
	 * @return 跳转标号块号列表
	 */
	public static List<Integer> getCatchLabelFromMap(
			Map<String, Integer> jmpLbMap) {
		return getLabelFromJumpLabelMap(jmpLbMap, C.PTN_CATCH_LABEL);
	}

	/**
	 * 从跳转标号表中获取符合正则表达式的跳转标号块号<br />
	 * 
	 * @param jmpLbMap
	 * @param labelRegex
	 * @return 跳转标号块号列表
	 */
	private static List<Integer> getLabelFromJumpLabelMap(
			Map<String, Integer> jmpLbMap, String labelRegex) {
		List<Integer> idList = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : jmpLbMap.entrySet()) {
			String key = entry.getKey();
			if (matchStringFromLineByRegex(key, labelRegex)) {
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

		// System.out.println(keys.toString());

		return keys;
	}
}
