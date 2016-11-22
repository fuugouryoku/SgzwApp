package nc.sgzw.uap.properties;


import java.util.ResourceBundle;

public class NCInfoGet {
	private static String bmdj;
	private static String jjcd;
	private static String ztc;
	private static String host;
	private static String secret;
	private static String appid;

	

	public static String getHost() {
		return host;
	}



	public static void setHost(String host) {
		NCInfoGet.host = host;
	}



	public static String getSecret() {
		return secret;
	}



	public static void setSecret(String secret) {
		NCInfoGet.secret = secret;
	}



	public static String getAppid() {
		return appid;
	}



	public static void setAppid(String appid) {
		NCInfoGet.appid = appid;
	}



	public static String getBmdj() {
		return bmdj;
	}



	public static void setBmdj(String bmdj) {
		NCInfoGet.bmdj = bmdj;
	}



	public static String getJjcd() {
		return jjcd;
	}



	public static void setJjcd(String jjcd) {
		NCInfoGet.jjcd = jjcd;
	}



	public static String getZtc() {
		return ztc;
	}



	public static void setZtc(String ztc) {
		NCInfoGet.ztc = ztc;
	}



	static{
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("FWNGNEEDWORD");
			bmdj = bundle.getString("bmdj");
			jjcd = bundle.getString("jjcd");
			ztc = bundle.getString("ztc");
			host = bundle.getString("host");
			secret = bundle.getString("secret");
			appid = bundle.getString("appid");

	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
