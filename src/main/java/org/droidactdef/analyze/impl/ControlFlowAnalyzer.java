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

	@Override
	public void analyze(Object... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, List<FlowNode>> getFlow(List<String> lines,
			Map<String, Boolean> genMap) {
		// TODO Auto-generated method stub
		return null;
	}
}
