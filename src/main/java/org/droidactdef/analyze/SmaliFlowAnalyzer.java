package org.droidactdef.analyze;

import java.sql.Connection;
import java.util.*;

import org.droidactdef.analyze.domains.Flow;
import org.droidactdef.analyze.domains.TopLevelMtd;
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
	 * @param conn
	 * @param md5
	 * @param mtdName
	 * @return
	 */
	public Map<String, Flow> getFlow(Connection conn, String md5, Map<String, TopLevelMtd> topLevels) throws Exception;
}
