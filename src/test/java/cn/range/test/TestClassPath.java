package cn.range.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TestClassPath {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			List<String> lines = FileUtils.readLines(new File("1.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("something");
		}
	}

}
