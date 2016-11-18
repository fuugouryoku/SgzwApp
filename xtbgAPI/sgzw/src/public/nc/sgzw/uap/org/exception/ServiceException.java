package nc.sgzw.uap.org.exception;


public class ServiceException {

	private int code;
	private String desc;	
	public static int SUCCESS_CODE = 200;
	public static String SUCCESS_DESC = "success";
	public static int FAIL_CODE = -400; 
	public static String FAIL_DESC = "fali";
	public static String FAIL_DESC_CONN_TYPE = "fali:��ʹ����ȷ��HTTP����ʽ";
	public static String FAIL_DESC_ENCTYPE_TYPE = "fali:��ʹ��multipart/form-data";
	public static String FAIL_DESC_FILE_SIZE = "fali:���ϴ�С��10M���ļ�";
	public static String FAIL_DESC_FILE_ANALYSIS = "fali:�ļ�����ʧ��";
	public static String FAIL_DESC_FILE_UPLOAD = "fali:�ļ��ϴ�ʧ��";
	public static String FAIL_DESC_FILE_DOWNLOAD = "fali:�ļ�����ʧ��";
	public static String FAIL_DESC_FILE_GONE = "fali:�ļ��޷�����";
	public static String FAIL_DESC_FILE_DELETE = "fali:�ļ�ɾ��ʧ��";	
	public static String FAIL_DESC_FILEINFO_DELETE = "fali:�ļ���Ϣɾ��ʧ��";	
	public static String FAIL_DESC_FILE_FIND = "fali:�ļ���ȡʧ��";
	public static String FAIL_DESC_USER_FIND = "fali:��ǰ�û���Ϣ������";
	public static String FAIL_DESC_ISUNIT = "fali:�޷��ж���֯����";
	public static String FAIL_DESC_SCOPE = "fali:����/���Ͳ���ʧ��";
	public static String FAIL_DESC_INFO = "fali:��Ϣ�ϴ�ʧ��";
	public static String FAIL_DESC_INFO_ANALYSIS = "fali:���ݽ���ʧ��";

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
