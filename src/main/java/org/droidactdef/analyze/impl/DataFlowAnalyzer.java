package org.droidactdef.analyze.impl;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.droidactdef.analyze.SmaliFlowAnalyzer;
import org.droidactdef.analyze.domains.Flow;
import org.droidactdef.domains.FlowNode;

/**
 * smali数据流分析器的实现<br />
 * 
 * @author range
 * 
 */
public class DataFlowAnalyzer implements SmaliFlowAnalyzer {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {

	}

	@Override
	public void analyze(Object...params) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Flow> getFlow(Connection conn, String md5)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
