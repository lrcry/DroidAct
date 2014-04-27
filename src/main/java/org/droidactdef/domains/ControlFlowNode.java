package org.droidactdef.domains;

/**
 * 控制流节点的数据定义<br />
 * 
 * @author range
 * 
 */
public class ControlFlowNode extends FlowNode {
	private int nodeId;
	
	private String mtdName;
	
	private boolean isApi;
	
	private int parentNodeId;

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public String getMtdName() {
		return mtdName;
	}

	public void setMtdName(String mtdName) {
		this.mtdName = mtdName;
	}

	public boolean isApi() {
		return isApi;
	}

	public void setApi(boolean isApi) {
		this.isApi = isApi;
	}

	public int getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(int parentNodeId) {
		this.parentNodeId = parentNodeId;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("node_id=").append(this.nodeId).append(",");
		sb.append("mtd_name=").append(this.mtdName).append(",");
		sb.append("is_api=").append(this.isApi).append(",");
		sb.append("parent_id=").append(this.parentNodeId).append(".");
		sb.append("=======================================");
		return sb.toString();
	}
}
