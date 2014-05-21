package org.droidactdef.analyze.domains;

import java.util.*;

import org.droidactdef.commons.C;

public class TopLevelMtd {
	private String name;

	private String suberClazz;

	private String interfaze;

	private List<String> body;

	private HashSet<String> apis;

	private Map<String, HashSet<String>> nonApiMtdApis;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public List<String> getBody() {
		return body;
	}

	public void setBody(List<String> body) {
		this.body = body;
	}

	public HashSet<String> getApis() {
		return apis;
	}

	public void setApis(HashSet<String> apis) {
		this.apis = apis;
	}

	public Map<String, HashSet<String>> getNonApiMtdApis() {
		return nonApiMtdApis;
	}

	public void setNonApiMtdApis(Map<String, HashSet<String>> nonApiMtdApis) {
		this.nonApiMtdApis = nonApiMtdApis;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> strs = new ArrayList<>();
		strs.add("========R 顶层方法: " + this.name
				+ " xx=================================");
		strs.add("基类: " + this.suberClazz);
		strs.add("接口: " + this.interfaze);
		strs.add("API列表：");
		if (this.apis != null && this.apis.size() > 0)
			strs.addAll(this.apis);

		strs.add("非API方法调用列表：");
		if (this.nonApiMtdApis != null && this.nonApiMtdApis.size() > 0) {
			for (Map.Entry<String, HashSet<String>> e : this.nonApiMtdApis
					.entrySet()) {
				strs.add(e.getKey() + " ||| " + e.getValue());
			}
		}

		for (String str : strs) {
			sb.append(str).append(C.CRLF);
		}

		return sb.toString();
	}
}
