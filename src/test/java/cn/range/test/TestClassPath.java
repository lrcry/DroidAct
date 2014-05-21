package cn.range.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.droidactdef.analyze.domains.TopLevelMtd;

public class TestClassPath {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Collection<String> hashSet = new HashSet<>();
		hashSet.add("1");
		hashSet.add("2");
		hashSet.add("3gs");
		hashSet.add("4s");
		hashSet.add("1");
		
		
		Collection<String> list = new ArrayList<>();
		list.add("1");
		list.add("2");
		list.add("3gs");
		list.add("list3");
		list.add("list5");
		
		list.retainAll(hashSet);
//		list.removeall
		System.out.println(list);
		
	}

}
