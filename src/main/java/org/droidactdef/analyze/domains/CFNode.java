package org.droidactdef.analyze.domains;

import java.util.*;

import org.droidactdef.commons.C;

/**
 * 基于基本块的控制流节点<br />
 * 
 * @author range
 * 
 */
public class CFNode {
	private int nodeId;

	private List<Integer> prev;

	private List<Integer> next;

	private BasicBlock bb;

	public List<Integer> getPrev() {
		return prev;
	}

	public void setPrev(List<Integer> prev) {
		this.prev = prev;
	}

	public List<Integer> getNext() {
		return next;
	}

	public void setNext(List<Integer> next) {
		this.next = next;
	}

	public BasicBlock getBb() {
		return bb;
	}

	public void setBb(BasicBlock bb) {
		this.bb = bb;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("CFNode_");
		sb.append(this.nodeId).append(C.CRLF);
		sb.append("Prev:").append(C.CRLF);
		for (int i : this.prev) {
			sb.append(i).append(" ");
		}
		sb.append(C.CRLF);
		sb.append("Next:").append(C.CRLF);
		for (int i : this.next) {
			sb.append(i).append(" ");
		}
		sb.append(C.CRLF);
		sb.append("Body:").append(C.CRLF);
		List<String> body = this.bb.getBlockBody();
		for (int i = 0; i < body.size(); i++) {
			sb.append(body.get(i)).append(C.CRLF);
		}
		return sb.toString();
	}
}
