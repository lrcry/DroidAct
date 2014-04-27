package org.droidactdef.domains;

import org.droidactdef.commons.C;

/**
 * 描述smali的变量<br />
 * 
 * @author range
 * 
 */
public class SmaliField {
	// Variable name
	private String varName;

	// Variable type
	private String varType;

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarType() {
		return varType;
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(C.FHEADER_NAME);
		sb.append(this.varName);
		sb.append(", ");
		sb.append(C.FHEADER_TYPE);
		sb.append(this.varType);
		return sb.toString();
	}
}
