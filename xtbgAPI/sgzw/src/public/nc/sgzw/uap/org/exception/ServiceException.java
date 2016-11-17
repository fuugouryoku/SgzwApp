package nc.sgzw.uap.org.exception;


public class ServiceException {

	private int code;
	private String desc;	
	public static int SUCCESS_CODE = 200;
	public static String SUCCESS_DESC = "success";
	public static int FAIL_CODE = -400; 
	public static String FAIL_DESC = "fali";
	public static String FAIL_DESC_CONN_TYPE = "fali:请使用正确的HTTP请求方式";
	public static String FAIL_DESC_ENCTYPE_TYPE = "fali:请使用multipart/form-data";
	public static String FAIL_DESC_FILE_SIZE = "fali:请上传小于10M的文件";
	public static String FAIL_DESC_FILE_ANALYSIS = "fali:文件解析失败";
	public static String FAIL_DESC_FILE_UPLOAD = "fali:文件上传失败";
	public static String FAIL_DESC_FILE_DOWNLOAD = "fali:文件下载失败";
	public static String FAIL_DESC_FILE_GONE = "fali:文件无法查找";
	public static String FAIL_DESC_FILE_DELETE = "fali:文件删除失败";	
	public static String FAIL_DESC_FILEINFO_DELETE = "fali:文件信息删除失败";	
	public static String FAIL_DESC_FILE_FIND = "fali:文件获取失败";
	public static String FAIL_DESC_USER_FIND = "fali:当前用户信息不存在";
	public static String FAIL_DESC_ISUNIT = "fali:无法判断组织部门";
	public static String FAIL_DESC_SCOPE = "fali:主送/抄送插入失败";
	public static String FAIL_DESC_INFO = "fali:信息上传失败";
	public static String FAIL_DESC_INFO_ANALYSIS = "fali:数据解析失败";

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	
}
