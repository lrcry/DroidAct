package org.droidactdef.analyze.domains;

import java.util.*;

/**
 * 基本块<br />
 * 定义：所谓基本块，是指程序—顺序执行的语句序列，其中只有一个入口和一个出口，
 * 入口就是其中的第—个语句，出口就是其中的最后一个语句。对一个基本块来说，执行时只从其入口进入，从其出口退出。<br />
 * 特点：只要基本块中第一条指令被执行了，那么基本块内所有执行都会按照顺序仅执行一次。<br />
 * 
 * @author range
 * 
 */
public class BasicBlock {
	private int blockId; // 块ID

	private String mtdName; // 块所在的方法名

	private boolean isJump; // 是否为跳转指令

	private boolean isJumpLabel; // 是否为跳转标记

	private String jmpLabel; // 跳转标记号

	private int jmpToLineNumber; // 跳转到的行号, 除了跳转指令isJump=true为行号外,其他为-1
									// 此外，判断是packed-switch的方法是，是跳转指令，跳转到-1.

	private int curLineNumber; // 当前行号，如果其不是跳转指令，则为-1

	private List<String> blockBody; // 块中的指令

	public int getBlockId() {
		return blockId;
	}

	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}

	public boolean isJump() {
		return isJump;
	}

	public void setJump(boolean isJump) {
		this.isJump = isJump;
	}

	public String getJmpLabel() {
		return jmpLabel;
	}

	public void setJmpLabel(String jmpLabel) {
		this.jmpLabel = jmpLabel;
	}

	public List<String> getBlockBody() {
		return blockBody;
	}

	public void setBlockBody(List<String> blockBody) {
		this.blockBody = blockBody;
	}

	public int getJmpToLineNumber() {
		return jmpToLineNumber;
	}

	public void setJmpToLineNumber(int jmpToLineNumber) {
		this.jmpToLineNumber = jmpToLineNumber;
	}

	public String getMtdName() {
		return mtdName;
	}

	public void setMtdName(String mtdName) {
		this.mtdName = mtdName;
	}

	public boolean isJumpLabel() {
		return isJumpLabel;
	}

	public void setJumpLabel(boolean isJumpLabel) {
		this.isJumpLabel = isJumpLabel;
	}

	public int getCurLineNumber() {
		return curLineNumber;
	}

	public void setCurLineNumber(int curLineNumber) {
		this.curLineNumber = curLineNumber;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("基本块:" + this.blockId);
		sb.append(",来自方法" + this.mtdName).append("\n");
		sb.append("是跳转指令? ").append(this.isJump).append("\n");
		sb.append("是跳转标号? ").append(this.isJumpLabel).append("\n");
		if (this.isJump) {
			sb.append("跳转标记号:").append(this.jmpLabel).append("\n");
			sb.append("跳转至行数:").append(this.jmpToLineNumber).append("\n");
			sb.append("跳转指令当前行数:").append(this.curLineNumber).append("\n");
		}
		sb.append("块体:").append("\n");
		for (String line : this.blockBody) {
			sb.append(line).append("\n");
		}

		return sb.toString();
	}
}
