package org.droidactdef.analyze;

import java.util.*;

import org.droidactdef.domains.FlowNode;

/**
 * 对smali文件进行流分析的分析器接口<br />
 * 
 * @author range
 * 
 */
public interface SmaliFlowAnalyzer extends Analyzer {
	/**
	 * 获取流<br />
	 * 
	 * @param lines
	 *            方法body行集合
	 * @return 流
	 */
	public Map<Integer, List<FlowNode>> getFlow(List<String> lines, Map<String, Boolean> genMap);
}
