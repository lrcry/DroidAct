package org.droidactdef.report;

/**
 * 分析报告生成器<br />
 * 
 * @author range
 * 
 */
public interface Reporter {
	/**
	 * 报告：<br />
	 * 1. 在固定目录下生成报告文件<br />
	 * 2. 修改数据库中对应apk文件的报告路径地址<br />
	 * 
	 * @param params
	 *            实现者自定义的任何意义、任何顺序的参数
	 */
	void report(Object... params);
}
