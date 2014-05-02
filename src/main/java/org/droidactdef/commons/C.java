package org.droidactdef.commons;

/**
 * 常量池<br />
 * 
 * @author range
 * 
 */
public class C {
	/*
	 * 
	 * 通用
	 */
	public static final String FILE_ENCODING = "UTF-8";
	public static final String FILE_SPLIT = "\\|\\|\\|";
	public static final String DB_STR_SPLIT = "|||";

	public static final String DB_FILENAME_DA_ANDROID_API_CREATE = "src/main/resources/db_res/create_da_android_api.sql";
	public static final String DB_FILENAME_DA_ANDROID_API_INSERT = "src/main/resources/db_res/da_android_api.dbres";
	public static final String DB_FILENAME_DA_METHODS_CREATE = "src/main/resources/db_res/create_da_methods.sql";

	public static final String CONF_PATH_DB = "src/main/resources/conf/dbconf.properties";
	public static final String DB_RES_PATH = "src/main/resources/db_res";

	// 敏感权限

	/*
	 * 
	 * 正则表达式
	 */
	// Registers
	private static final String PTN_REG = "[vp][0-9]*";
	private static final String PTN_REGS = "[" + PTN_REG + ", ]*" + PTN_REG;
	
	// Method invocation
	public static final String PTN_METHOD = "[a-zA-Z0-9_/$]*;->[a-zA-Z0-9<>_/$]*\\([a-zA-Z0-9_/$;\\[]*\\)[a-zA-Z0-9_/$;]*";
	public static final String PTN_METHOD_INVOKE = "invoke-[a-z\\/]*";
	public static final String PTN_METHOD_REGS = "\\{" + PTN_REGS + "\\}";
	public static final String PTN_CLAZZ = "L[a-zA-Z0-9_/$]*;";
	
	// Jumping
	public static final String PTN_GOTO_LABEL = ":goto_[0-9a-z]"; // goto标号
	public static final String PTN_IF_COND = ":cond_[0-9a-z]";
	public static final String PTN_IF = "if-[a-z]* " + PTN_REGS + ", "
			+ PTN_IF_COND; // if-nez v1, :cond_0
	public static final String PTN_IF_IF = "if-[a-z]*";
	public static final String PTN_GOTO = "goto " + PTN_GOTO_LABEL; // goto
																	// :goto_0
	public static final String PTN_GOTO_16 = "goto/16 " + PTN_GOTO_LABEL; // goto/16
																			// :goto_0
	// Switch jumping
	public static final String PTN_PSWITCH_LABEL = ":pswitch_[0-9a-z]";
	public static final String PTN_PSWITCH_DATA = ":pswitch_data_[0-9a-z]";
	public static final String PTN_PSWITCH_START = "packed-switch [vp][0-9a-z], :pswitch_data_[0-9a-z]";

	// Returning
	public static final String PTN_RETURN = "return [pv][0-9a-z]"; // matches
	public static final String PTN_RETURN_VOID = "return-void"; // matches
	public static final String PTN_RETURN_OBJECT = "return-object"; // find

	// Try and catch
	public static final String PTN_CATCH_LABEL = ":catch_[0-9a-z]";
	public static final String PTN_CATCHALL_LABEL = ":catchall_[0-9a-z]";
	public static final String PTN_TRY_START = ":try_start_[0-9a-z]";
	public static final String PTN_TRY_END = ":try_end_[0-9a-z]";
	public static final String PTN_TRY_CATCH_CATCH = ".catch " + PTN_CLAZZ + " \\{"
			+ PTN_TRY_START + " \\.\\. " + PTN_TRY_END + "\\} "
			+ PTN_CATCH_LABEL;;
	public static final String PTN_TRY_CATCHALL_CATCH = ".catchall \\{"
			+ PTN_TRY_START + " \\.\\. " + PTN_TRY_END + "\\} "
			+ PTN_CATCHALL_LABEL;

	/*
	 * 
	 * XML相关常量
	 */
	public static final String ATTR_NAME = "name";
	public static final String ROOT_ELEMENT_NAME = "//manifest/";
	public static final String PERM_ELEMENT_NAME = ROOT_ELEMENT_NAME
			+ "uses-permission";
	public static final String APP_ELEMENT_NAME = ROOT_ELEMENT_NAME
			+ "application/";
	public static final String SRV_ELEMENT_NAME = APP_ELEMENT_NAME + "service";
	public static final String RECVER_ELEMENT_NAME = APP_ELEMENT_NAME
			+ "receiver";
	public static final String ACTV_ELEMENT_NAME = APP_ELEMENT_NAME
			+ "activity";

	/*
	 * 
	 * SMALI代码相关常量
	 */
	public static final String CRLF = System.getProperty("line.separator");
	public static final String MODIFIER_ABSTRACT = "abstract";
	public static final String MODIFIER_NATIVE = "native";
	public static final String CLAZZ_START = ".class";
	public static final String SPACE_BEFORE = " ";
	public static final String SUPER_START = ".super";
	public static final String INTERFACE_START = ".implements";
	public static final String SRC_START = ".source";
	public static final String SRC_NAME_START = "\"";
	public static final String SRC_NAME_END = "\"";
	public static final String MAP_CLAZZ = "class.className";
	public static final String MAP_SUPER = "class.superClassName";
	public static final String MAP_INTERFACE = "class.interfaceName";
	public static final String MAP_SOURCE = "class.sourceName";

	public static final String METHOD_START = ".method";
	public static final String ARGLIST_START = "(";
	public static final String ARG_SPLITER = ";";
	public static final String ARGLIST_END = ")";
	public static final String METHOD_END = ".end method";

	public static final String FIELD_START = ".field";
	public static final String FIELD_SPLITER = ":";

	public static final String MHEADER_NAME = "Method name: ";
	public static final String MHEADER_ARGS = "Method args: ";
	public static final String MHEADER_ARG_SPLITER = "\t";
	public static final String MHEADER_RET_TYPE = "Method return type: ";
	public static final String MHEADER_BODY = "Method body: " + CRLF;

	public static final String FHEADER_NAME = "Field name: ";
	public static final String FHEADER_TYPE = "Field type: ";

	public static final String PSWITCH_DATA_START = "pswitch_data";
	public static final String PSWITCH_DATA_END = ".end packed-switch";
}
