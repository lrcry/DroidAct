package org.droidactdef.utils.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.droidactdef.domains.SmaliField;
import org.droidactdef.domains.SmaliMethod;
import org.droidactdef.utils.SmaliUtils;
import org.junit.Test;

public class TestSmaliUtils {
	/**
	 * Test for <code>List<SmaliMethod> SmaliUtils.getSmaliMethodList(List<String>)</code>
	
	@Test
	public void testGetSmaliMethodList() throws IOException {
//		Iterator<File> it = FileUtils.iterateFiles(new File("test/apkoutput/smali"), 
//				new String[]{"smali"}, true);
		List<SmaliMethod> methods = new ArrayList<SmaliMethod>();
//		while (it.hasNext()) {
//			File smali = it.next();
		File smali = new File("UpdateCheck.smali");
			List<String> lines = FileUtils.readLines(smali);
			List<SmaliMethod> smaliMtds = SmaliUtils.getSmaliMethodList(lines);
			methods.addAll(smaliMtds);
//		}
		
		for (Iterator<SmaliMethod> mtdIt = methods.listIterator(); mtdIt.hasNext(); ) {
			SmaliMethod mtd = mtdIt.next();
//			System.out.println(mtdIt.next());
			System.out.println(mtd.getName());
		}
		
		System.out.println("Method count: " + methods.size());
	}
	 */
	
	/**
	 * Test for Map<String, String> SmaliUtils.getSmaliClazzInfo(List<String>)
	 * @throws IOException
	
	@Test
	public void testGetSmaliClazzInfo() throws IOException {
		Iterator<File> it = FileUtils.iterateFiles(new File("test/apkoutput/smali"), 
				new String[]{"smali"}, true);
		List<Map<String, String>> clazzInfo = new ArrayList<Map<String, String>>();
		while (it.hasNext()) {
			File smali = it.next();
			List<String> lines = FileUtils.readLines(smali);
			Map<String, String> smaliClazz = SmaliUtils.getSmaliClazzInfo(lines);
			clazzInfo.add(smaliClazz);
		}
		
		for (Iterator<Map<String, String>> clzIt = clazzInfo.listIterator(); clzIt.hasNext(); ) {
			System.out.println("New Clazz: ");
			Map<String, String> info = clzIt.next();
			for (Map.Entry<String, String> entry : info.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
		}
		
		System.out.println(clazzInfo.size());
	} */
	
	/**
	 * Test for List<SmaliField> SmaliUtils.getSmaliFieldList(List<String>)
	 * @throws IOException
	 
	@Test
	public void testGetSmaliFieldsList() throws IOException {
		File smali = new File("UpdateCheck.smali");
		List<String> lines = FileUtils.readLines(smali);
		List<SmaliField> fields = SmaliUtils.getSmaliFieldList(lines);
		for (Iterator<SmaliField> it = fields.listIterator(); it.hasNext(); ) {
			System.out.println(it.next());
		}
	}*/
}
