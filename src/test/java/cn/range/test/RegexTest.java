package cn.range.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.droidactdef.analyze.SmaliFlowAnalyzer;
import org.droidactdef.analyze.impl.ControlFlowAnalyzer;
import org.droidactdef.analyze.utils.FlowUtils;
import org.droidactdef.commons.C;

public class RegexTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String str = "return-void";
		System.out.println(FlowUtils.lineIsReturn(str));
		
		/*
		List<String> lines = FileUtils.readLines(new File("cfTest_circulation.smali"), "UTF-8");
		List<String> trimedLines = new ArrayList<>();
		for (String line : lines) {
			trimedLines.add(line.trim());
		}
		
		Map<Integer, String> insMap = new ControlFlowAnalyzer().getJumpLabel(trimedLines);
		for (Map.Entry<Integer, String> entry : insMap.entrySet()) {
			System.out.println("lineNo=" + entry.getKey() + ", line=" + entry.getValue());
		}*/
		
		
		/* control flow extraction original 
		String regex = "";
		String str = "invoke-direct {p0, v0, v1}, Lxx/xx/X;->mtd(I[BLxx/x/X;)Lx/x/X;";
		List<String> lines = new ArrayList<>();
		lines = FileUtils.readLines(new File("cfTest_switch.smali"), "UTF-8");
		
		// 必要的预处理：从文件或DB中取出的方法体是有缩进的，因此需去首尾空格
		for (int i = 0; i < lines.size(); i++) {
			lines.set(i, lines.get(i).trim());
		}
		str.equals("");
		str.isEmpty();
		ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
		Pattern ptn = null;
		Matcher mch = null;
		
		for (String line : lines) {
			line = line.trim();
			System.out.println(line);
			// Method invocation
			ptn = Pattern.compile(C.PTN_METHOD_INVOKE);
			mch = ptn.matcher(line);
			if (mch.find()) {
				String mtdName = analyzer.getMethodName(line);
				System.out.println("mtd_name=" + mtdName);
			}
			
			// if-xxx
			ptn = Pattern.compile(C.PTN_IF);
			mch = ptn.matcher(line);
			if (mch.matches()) {
				String typeIf = analyzer.getIfType(line);
				String condIf = analyzer.getIfCondLabel(line);
				
				int condLoc = lines.indexOf(condIf);
				System.out.println("if_type=" + typeIf + ", if_cond=" + condIf + ", cond_loc=" + condLoc);
				// 去cond标号处,找最终goto
			}
			
			// goto...
			ptn = Pattern.compile(C.PTN_GOTO);
			Pattern ptn16 = Pattern.compile(C.PTN_GOTO_16);
			mch = ptn.matcher(line);
			Matcher mch16 = ptn16.matcher(line);
			if (mch.matches() || mch16.matches()) {
				String labelGoto = analyzer.getGotoLabel(line);
				int labelLoc = lines.indexOf(labelGoto);
				System.out.println("goto_label=" + labelGoto + ", goto_loc=" + labelLoc);
				// 去goto标号处开始，再找跳转
			}
			
			System.out.println("__________");
		}
		*/
		
		/*
		String str = "invoke-direct/jumbo {p0}, lajoidsfasdfa";
		Pattern ptn = Pattern.compile(C.PTN_METHOD_INVOKE);
		Matcher mch = ptn.matcher(str);
		if (mch.find()) {
			System.out.println(mch.group());
		}
		*/ 
		/* 
		String str = "hahaha if-nez v1, :cond_0 hahahaasdfasdf";
		
		String regex = "if-[a-z]* [vp][0-9]*, :cond_[0-9]*"; // 匹配比较语句的正则表达式
		String condRegex = "cond_[0-9]*"; // 找出比较语句中涉及的条件 cond_0
		String compRegex = "if-[a-z]*"; // 找出比较语句中的比较指令 if-nez/...
		
		String gotoRegex = "goto :goto_[0-9]*"; // goto :goto_0
		String goto16Regex = "goto/16 :goto_[0-9]*"; // goto/16 :goto_0
		String gotoLabelRegex = "goto_[0-9]*"; // goto标号
		
		String gotoStr = "goto :goto_162";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			String cond = matcher.group();
			System.out.println(cond);
			pattern = Pattern.compile(condRegex);
			matcher = pattern.matcher(cond);
			if (matcher.find()) {
				System.out.println(matcher.group());
			}
			
			pattern = Pattern.compile(gotoRegex);
			matcher = pattern.matcher(gotoStr);
			if (matcher.find()) {
				System.out.println(matcher.group());
			}
		} */
	}

}
