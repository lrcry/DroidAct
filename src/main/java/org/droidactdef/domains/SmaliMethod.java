package org.droidactdef.domains;

import java.util.List;

import org.droidactdef.commons.C;

/**
 * 描述smali的方法<br />
 * 
 * @author range
 * 
 */
public class SmaliMethod {
	// 函数名称
	private String name;

	private String suberClazz;

	private String interfaze;

	// 参数列表
	private String argList;

	// 返回值类型
	private String retType;

	// 函数体
	private List<String> body;

	private boolean isNative;

	private boolean isAbstract;

	private String srcApkName;

	public String getSuberClazz() {
		return suberClazz;
	}

	public void setSuberClazz(String suberClazz) {
		this.suberClazz = suberClazz;
	}

	public String getInterfaze() {
		return interfaze;
	}

	public void setInterfaze(String interfaze) {
		this.interfaze = interfaze;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArgList() {
		return argList;
	}

	public void setArgList(String argList) {
		this.argList = argList;
	}

	public String getRetType() {
		return retType;
	}

	public void setRetType(String retType) {
		this.retType = retType;
	}

	public List<String> getBody() {
		return body;
	}

	public void setBody(List<String> body) {
		this.body = body;
	}

	public boolean isNative() {
		return isNative;
	}

	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public String getSrcApkName() {
		return srcApkName;
	}

	public void setSrcApkName(String srcApkName) {
		this.srcApkName = srcApkName;
	}

	/**
	 * 覆写toString方法<br />
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("======Method start======").append(C.CRLF);
		sb.append("mtd.name=").append(this.getName()).append(C.CRLF);
		sb.append("mtd.superclass=").append(this.getSuberClazz())
				.append(C.CRLF);
		sb.append("mtd.interface=").append(this.getInterfaze()).append(C.CRLF);
		sb.append("mtd.retype=").append(this.getRetType()).append(C.CRLF);
		sb.append("mtd.arglist=").append(this.getArgList()).append(C.CRLF);
		sb.append("mtd.isnative=").append(this.isNative).append(C.CRLF);
		sb.append("mtd.isabstract=").append(this.isAbstract).append(C.CRLF);
		sb.append("mtd.srcapkname=").append(this.getSrcApkName())
				.append(C.CRLF);
		sb.append("mtd.body: {").append(C.CRLF);
		List<String> mtdBody = this.getBody();
		if (mtdBody != null) {
			for (String line : mtdBody) {
				sb.append(line).append(C.CRLF);
			}
		}
		sb.append("}").append(C.CRLF);
		sb.append("======Method end======").append(C.CRLF);
		return sb.toString();
	}
}
