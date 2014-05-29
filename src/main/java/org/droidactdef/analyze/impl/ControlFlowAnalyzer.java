package org.droidactdef.analyze.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.droidactdef.analyze.domains.BasicBlock;
import org.droidactdef.analyze.domains.CFNode;
import org.droidactdef.analyze.domains.ControlFlow;
import org.droidactdef.analyze.domains.Flow;
import org.droidactdef.analyze.domains.TopLevelMtd;
import org.droidactdef.analyze.utils.ApiUtils;
import org.droidactdef.analyze.utils.FlowUtils;
import org.droidactdef.commons.C;
import org.droidactdef.domains.ControlFlowNode;
import org.droidactdef.domains.FlowNode;
import org.droidactdef.utils.DroidActDBUtils;

/**
 * smali控制流分析器的实现<br />
 * 
 * @author range
 * 
 */
public class ControlFlowAnalyzer implements SmaliFlowAnalyzer {

	/**
	 * 控制流分析参数 [connection, md5, ]
	 * 
	 */
	@Override
	public void analyze(Object... params) {
		// 参数校验
		if (params == null || params.length != 2)
			throw new RuntimeException("控制流分析：参数数量不正确");

		Connection conn = null;
		String md5 = "";
		try {
			conn = (Connection) params[0];
			md5 = (String) params[1];
		} catch (ClassCastException e) {
			throw new RuntimeException("控制流分析：参数类型不正确：" + e);
		}

		// 获取所有方法名称
		try {
			List<Object[]> allNamesObj = DroidActDBUtils.getMethodsNames(conn,
					null, md5);
			List<String> allMtdNames = DroidActDBUtils.convertObjListToStrList(
					allNamesObj, 0);

			// 获取所有顶层方法
			Map<String, TopLevelMtd> topLevels = ApiUtils
					.getTopLevelMtdsWithApi(conn, md5, allMtdNames);

			// 获取流
			Map<String, Flow> flows = getFlow(conn, md5, topLevels);
			// <mtdname, cfg>
			
			

		} catch (Exception e) {
			throw new RuntimeException("控制流分析：异常：" + e);
		}

	}

	@Override
	public Map<String, Flow> getFlow(Connection conn, String md5,
			Map<String, TopLevelMtd> topLevels) throws Exception {

		// 生成顶层方法的控制流图
		Map<String, Flow> cfs = new HashMap<>();
		for (Map.Entry<String, TopLevelMtd> e : topLevels.entrySet()) {
			String name = e.getKey();
			TopLevelMtd tlm = e.getValue();
			List<String> tlmBody = tlm.getBody();

			// 方法体预处理
			tlmBody = FlowUtils.removeBlankLines(tlmBody);
			FlowUtils.removePswitchDefinition(tlmBody);

			// 基本块划分
			List<BasicBlock> bbs = FlowUtils.getBasicBlockPartition(tlmBody,
					name);

			// 生成原始控制流图
			Map<Integer, CFNode> cf = FlowUtils.getControlFlowGraph(bbs);
			cf = FlowUtils.getControlFlowGraphWithReturn(cf);

			// 简化控制流图
			cf = FlowUtils.cfSimplify(cf, topLevels, name);

			// 添加结果
			ControlFlow flow = new ControlFlow();
			flow.setMtdName(name);
			flow.setCf(cf);
			cfs.put(name, flow);
		}

		return cfs;
	}
}
