package org.droidactdef.analyze.domains;

import java.util.List;

import org.droidactdef.commons.C;

/**
 * 
 * @author range
 * 
 */
public class MtdIncludeApi {
	private String mtdName;

	private String mtdSuperClazzName;

	private List<String> apis;

	public String getMtdName() {
		return mtdName;
	}

	public void setMtdName(String mtdName) {
		this.mtdName = mtdName;
	}

	public String getMtdSuperClazzName() {
		return mtdSuperClazzName;
	}

	public void setMtdSuperClazzName(String mtdSuperClazzName) {
		this.mtdSuperClazzName = mtdSuperClazzName;
	}

	public List<String> getApis() {
		return apis;
	}

	public void setApis(List<String> apis) {
		this.apis = apis;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(
				"====+====+====+ MTD START +====+====+====").append(C.CRLF);
		sb.append("NAME: ").append(this.mtdName).append(C.CRLF);
		sb.append("SUPER CLASS NAME: ").append(this.mtdSuperClazzName).append(C.CRLF);
		if (this.apis != null && this.apis.size() > 0) {
			sb.append("APIS: ").append(C.CRLF);
			for (int i = 0; i < this.apis.size() - 1; i++) {
				sb.append(this.apis.get(i)).append(C.CRLF);
			}
			sb.append(this.apis.get(this.apis.size() - 1)).append(C.CRLF);
		}

		sb.append("====+====+====+ MTD END +====+====+====").append(C.CRLF);

		return sb.toString();
	}
}
