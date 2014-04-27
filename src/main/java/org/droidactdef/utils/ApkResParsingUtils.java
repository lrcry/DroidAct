package org.droidactdef.utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;
import org.dom4j.*;
import org.dom4j.io.*;
import org.droidactdef.commons.C;

import brut.androlib.*;

/**
 * 将APK中的Manifest文件和Java文件反编译出来。<br />
 * 
 * @author range
 * 
 */
public class ApkResParsingUtils {
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static void main(String[] args) throws DocumentException,
			AndrolibException, IOException {
		// apktoolUnApk("test/test_aidl_endcall/AIDLDemo2.apk",
		// "test/test_aidl_endcall/output/");
		Document document = XMLParser
				.getDocumentFromXML("test/apkoutput/AndroidManifest.xml");
		List<String> apps = XMLParser.getApplications(document);
		for (String app : apps) {
			System.out.println(app);
		}
	}

	/* ============= APK information ============= */
	/**
	 * 获取APK文件的唯一信息（名称，crc，md5，sha1）<br />
	 * 
	 * @param apk
	 *            apk文件对象
	 * @return apk文件信息Map
	 */
	public static Map<String, String> getApkFileInfo(File apk) {
		InputStream in = null;
		Map<String, String> apkFileInfo = null;
		try {
			apkFileInfo = new HashMap<String, String>();
			in = new FileInputStream(apk);
			String apkName = apk.getName();
			apkFileInfo.put("apk.name", apkName);
			String crc32 = getCrc32FromStream(in);
			String md5 = getMdFromStream(in, "MD5");
			String sha1 = getMdFromStream(in, "SHA1");
			apkFileInfo.put("apk.crc32", crc32);
			apkFileInfo.put("apk.md5", md5);
			apkFileInfo.put("apk.sha1", sha1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return apkFileInfo;
	}

	/**
	 * 计算输入流的CRC32校验值<br />
	 * 
	 * @param in
	 *            输入流
	 * @return CRC32
	 * @throws IOException
	 */
	public static String getCrc32FromStream(InputStream in) throws IOException {
		String crc32 = null;
		CheckedInputStream chkIn = null;
		CRC32 crc = new CRC32();
		try {
			chkIn = new CheckedInputStream(in, crc);
			while (chkIn.read() != -1) {
				; //
			}
			crc32 = Long.toHexString(crc.getValue()).toUpperCase();
		} finally {
			if (chkIn != null) {
				try {
					chkIn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return crc32;
	}

	/**
	 * 计算输入流的信息摘要<br />
	 * 
	 * @param in
	 *            输入流
	 * @param algorithm
	 *            信息摘要算法名称
	 * @return md5或sha1值
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String getMdFromStream(InputStream in, String algorithm)
			throws NoSuchAlgorithmException, IOException {
		String mdStr = null;
		MessageDigest md = null;
		byte[] buf = new byte[1024];
		int len = 0;
		md = MessageDigest.getInstance(algorithm);
		while ((len = in.read(buf, 0, 1024)) != -1) {
			md.update(buf, 0, len);
		}
		byte[] mdByte = md.digest();
		mdStr = getHexText(mdByte);

		return mdStr;
	}

	/**
	 * 转换为16进制<br />
	 * 
	 * @param mdByte
	 *            信息摘要得到的byte数组
	 * @return 转换为16进制的值
	 */
	public static String getHexText(byte[] mdByte) {
		int len = mdByte.length;
		StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			sb.append(HEX_DIGITS[(mdByte[i] >> 4) & 0x0f]);
		}

		return sb.toString();
	}

	/* ============= Handling smali code ============= */
	/**
	 * 从smali源码中提取数据存入到数据库中<br />
	 * 
	 * @param apkDir
	 *            存放apk信息的目录
	 * @param conn
	 *            数据库连接
	 * @param colCount
	 *            列数
	 * @param maxLines
	 *            批量写入的最大行数
	 * @param srcApkName
	 *            apk名称
	 * @param srcApkCrc32
	 *            apk的CRC32校验值
	 * @param srcApkMd5
	 *            apk的MD5值
	 * @param srcApkSha1
	 *            apk的SHA1值
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void getApkMethodIntoDb(String apkDir, Connection conn,
			int colCount, int maxLines, String srcApkName, String srcApkCrc32,
			String srcApkMd5, String srcApkSha1) throws SQLException,
			IOException {
		Collection<File> smaliFiles = getAllFiles(apkDir, "smali");
		System.out.println(smaliFiles.size());
		List<String> methods = new ArrayList<>();
		QueryRunner runner = new QueryRunner();
		String sql = DroidActDBUtils
				.sqlBuilder(
						"insert",
						"da_methods",
						"mtd_name,mtd_superclass,mtd_interface,mtd_retype, mtd_params, mtd_src_apk_name,mtd_src_apk_crc32,mtd_src_apk_md5,mtd_src_apk_sha1,mtd_isnative,mtd_isabstract,mtd_create_at,mtd_body",
						colCount, null);
		for (File file : smaliFiles) {
			if (methods.size() > maxLines) { // 超过最大行数就要先写
				Object[][] params = DroidActDBUtils.listToObjectArray(methods,
						colCount);
				runner.batch(conn, sql, params);
				methods.clear();
				System.out.println("write maxline");
			}
			System.out.println("file executed");
			// 之后再向集合中写新的
			List<String> temp = new ArrayList<>();
			temp = getMethodFromClass(file, srcApkName, srcApkCrc32, srcApkMd5,
					srcApkSha1);
			methods.addAll(temp);
		}

		Object[][] restParams = DroidActDBUtils.listToObjectArray(methods,
				colCount);
		runner.batch(conn, sql, restParams);
	}

	/**
	 * 从class中提取数据<br />
	 * 
	 * @param smaliFile
	 *            smali文件对象
	 * @param srcApkName
	 *            apk名称
	 * @param srcApkCrc32
	 * @param srcApkMd5
	 * @param srcApkSha1
	 * @return smali方法行集合
	 * @throws IOException
	 */
	public static List<String> getMethodFromClass(File smaliFile,
			String srcApkName, String srcApkCrc32, String srcApkMd5,
			String srcApkSha1) throws IOException {
		System.out.println("getMethodFromClass");
		List<String> lines = FileUtils.readLines(smaliFile, C.FILE_ENCODING);
		Map<String, String> clazzInfo = SmaliUtils.getSmaliClazzInfo(lines);
		List<String> smaliMethods = new ArrayList<>();
		String clazzName = clazzInfo.get(C.MAP_CLAZZ);
		String suberClazzName = clazzInfo.get(C.MAP_SUPER);
		String inderfazeName = clazzInfo.get(C.MAP_INTERFACE);

		List<List<String>> mtdList = SmaliUtils.getSmaliMethodList(lines);
		for (List<String> method : mtdList) {
			List<String> mtd = new ArrayList<>();
			mtd = SmaliUtils.getMethodInfoAsString(method, clazzName,
					suberClazzName, inderfazeName, srcApkName, srcApkCrc32,
					srcApkMd5, srcApkSha1);
			smaliMethods.addAll(mtd);
		}
		return smaliMethods;
	}

	/*
	 * ============= Handling AndroidManifest.xml =============
	 */
	/**
	 * XMLParser类，从清单文件中获得组件<br />
	 * 先要调用getDocumentFromXml方法得到唯一的文档对象，之后再调用getAnalyzeList方法获取需要的数据<br />
	 * 
	 * @author range
	 * 
	 */
	public static class XMLParser {
		/**
		 * 获取主Activity<br />
		 * 
		 * @param document
		 *            文档对象
		 * @return 主Activity的名称Lpackname/sub1/sub2/MAname;
		 */
		@SuppressWarnings("rawtypes")
		public static String getMainActivity(Document document) {
			String mainActName = null;
			StringBuilder sb = new StringBuilder("L");
			Element manifest = document.getRootElement();
			String packageName = manifest.attributeValue("package");
			sb.append(packageName);
			Iterator appIt = manifest.elementIterator("application");
			while (appIt.hasNext()) {
				Element application = (Element) appIt.next();
				Iterator actIt = application.elementIterator("activity");
				while (actIt.hasNext()) {
					Element activity = (Element) actIt.next();
					String actName = activity.attributeValue("name");
					Iterator intentIt = activity
							.elementIterator("intent-filter");
					if (intentIt.hasNext()) {
						Element intentFilter = (Element) intentIt.next();
						Iterator actionIt = intentFilter
								.elementIterator("action");
						Iterator catIt = intentFilter
								.elementIterator("category");
						if (actionIt.hasNext()) {
							Element action = (Element) actionIt.next();
							while (catIt.hasNext()) {

								Element category = (Element) catIt.next();
								if (action.attributeValue("name").equals(
										"android.intent.action.MAIN")
										&& category
												.attributeValue("name")
												.equals("android.intent.category.LAUNCHER")) {
									System.out.println("main found");
									sb.append(".").append(actName).append(";");
									String name = sb.toString().replace('.',
											'/');
									return name;
								}
							}
						}
					}
				}
			}

			return mainActName;
		}

		/**
		 * 提取application<br />
		 * 
		 * @param document
		 * @return
		 */
		public static List<String> getApplications(Document document) {
			return getAnalyzeList(document, "//manifest/application");
		}

		/**
		 * 提取权限<br/ >
		 * 
		 * @param document
		 *            文档对象
		 * @return 权限列表
		 */
		public static List<String> getPermissions(Document document) {
			return getAnalyzeList(document, C.PERM_ELEMENT_NAME);
		}

		/**
		 * 提取广播接收器<br/ >
		 * 
		 * @param document
		 *            文档对象
		 * @return 广播接收器receiver组件名称列表
		 */
		public static List<String> getBroadcastReceivers(Document document) {
			return getAnalyzeList(document, C.RECVER_ELEMENT_NAME);
		}

		/**
		 * 提取Activity<br />
		 * 
		 * @param document
		 *            文档对象
		 * @return activity组件名称列表
		 */
		public static List<String> getActivities(Document document) {
			return getAnalyzeList(document, C.ACTV_ELEMENT_NAME);
		}

		/**
		 * 提取服务<br/ >
		 * 
		 * @param document
		 *            文档对象
		 * @return 服务service组件名称列表
		 */
		public static List<String> getServices(Document document) {
			return getAnalyzeList(document, C.SRV_ELEMENT_NAME);
		}

		/**
		 * 从XML中提取需要分析的结果<br />
		 * 通过解析清单文件，可以获得：<br />
		 * 1. APK使用的权限 (Consts.PERM_ELEMENT_NAME)<br />
		 * 2. 广播接收器清单 (Consts.RECVER_ELEMENT_NAME)<br />
		 * 3. 服务清单 (Consts.SRV_ELEMENT_NAME)<br />
		 * 4. Activity清单(Consts.ACTV_ELEMENT_NAME)<br />
		 * 
		 * @param document
		 *            XML的文档模型
		 * @param dataName
		 *            要得到的数据名称
		 * @return 返回分析结果列表
		 */
		private static List<String> getAnalyzeList(Document document,
				String dataName) {
			List<?> xmlList = document.selectNodes(dataName);
			List<String> nameList = new ArrayList<String>();

			for (Iterator<?> it = xmlList.iterator(); it.hasNext();) {
				Element element = (Element) it.next();
				nameList.add(element.attributeValue(C.ATTR_NAME));
			}
			return nameList;
		}

		/**
		 * 初始化Reader并从XML得到可以遍历的Document<br />
		 * 这个方法应该在解析一个XML文件的始末只被调用一次，以节省内存空间<br />
		 * 
		 * @param fileName
		 *            XML文件内容
		 * @return XML的文档模型
		 * @throws FileNotFoundException
		 * @throws DocumentException
		 */
		public static Document getDocumentFromXML(String fileName)
				throws FileNotFoundException, DocumentException {
			File xml = new File(fileName);
			InputStream is = new FileInputStream(xml);
			SAXReader reader = new SAXReader();
			Document doc = null;
			doc = reader.read(is);

			if (doc != null)
				return doc;
			else
				throw new RuntimeException("Document for XML parser is null!");
		}
	}

	/**
	 * 用于分割method body，防止其超长导致数据库错误<br />
	 * 
	 * @param lines
	 * 
	 *            private static List<String> splitTooLongStringFromList(String
	 *            line) {
	 * 
	 *            return null; }
	 */

	/* ============= General ============= */
	/**
	 * 获取目录下所有文件<br />
	 * 
	 * @param directory
	 *            目录
	 * @param extensions
	 *            扩展名，可指定多个
	 * @return 目录下所有文件的对象集合
	 */
	public static Collection<File> getAllFiles(String directory,
			String... extensions) {
		Collection<File> files = new ArrayList<>();
		files = FileUtils.listFiles(new File(directory), extensions, true);
		return files;
	}

	/**
	 * 使用apktool的API将APK解压到指定输出目录下<br />
	 * 解压出的有用的资源：<br />
	 * 1. smali目录下的smali代码<br />
	 * 2. AndroidManifest.xml清单<br />
	 * 得到资源的处理办法：<br />
	 * 1. 生成日志文件，用MapReduce处理<br />
	 * 2. 灌入数据库，用MapReduce处理<br />
	 * 
	 * @param apkName
	 *            APK名称
	 * @param destDir
	 *            输出目录
	 * @throws AndrolibException
	 * @throws IOException
	 */
	public static void apktoolUnApk(String apkName, String destDir)
			throws AndrolibException, IOException {
		ApkDecoder decoder = new ApkDecoder();
		File apk = new File(apkName);
		File dest = new File(destDir);
		if (!apk.exists())
			throw new FileNotFoundException("Cannot find source APK");

		decoder.setApkFile(apk);
		decoder.setOutDir(dest);
		decoder.decode();
	}

	public static void main2(String[] args) throws Exception {
		File file = new File("test/virus.apk");
		InputStream in = new FileInputStream(file);
		String crc32 = getCrc32FromStream(in);
		System.out.println("crc32=" + crc32);
		in = new FileInputStream(file);
		String md5 = getMdFromStream(in, "md5");
		System.out.println("md5=" + md5);
		in = new FileInputStream(file);
		String sha1 = getMdFromStream(in, "sha1");
		System.out.println("sha1=" + sha1);
	}

	public static void main3(String[] args) throws SQLException, IOException {
		DbUtils.loadDriver("com.mysql.jdbc.Driver");

		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/droidact", "root", "admin");

		// 将方法导入的使用示例
		getApkMethodIntoDb("test", conn, 13, 1024, "sth.apk", "21CAA179",
				"4f65245c31844079", "66c1e26cce2ab61df901");
	}
}
