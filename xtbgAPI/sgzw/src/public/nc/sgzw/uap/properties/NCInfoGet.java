package nc.sgzw.uap.properties;


import java.util.ResourceBundle;

public class NCInfoGet {
	private static String bmdj;
	private static String jjcd;
	private static String ztc;

	

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

	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
