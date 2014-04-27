package org.droidactdef.utils;

import java.io.*;
import java.util.*;


public class PropertiesUtil {
	/**
	 * 获取Properties对象<br />
	 * 
	 * @param pFileName
	 * @return
	 * @throws IOException
	 */
	public static Properties getProperties(String pFileName) throws IOException {
		Properties p = new Properties();
		InputStream in = new BufferedInputStream(new FileInputStream(pFileName));
		p.load(in);
		return p;
	}
}
