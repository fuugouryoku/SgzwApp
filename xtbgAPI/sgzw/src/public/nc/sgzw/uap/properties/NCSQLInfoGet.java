package nc.sgzw.uap.properties;


import java.util.ResourceBundle;

public class NCSQLInfoGet {
	private static String driver;
	private static String dburl;
	private static String user;
	private static String password;
	private static String filepath;
	public static String getFilepath() {
		return filepath;
	}

	public static void setFilepath(String filepath) {
		NCSQLInfoGet.filepath = filepath;
	}
	static{
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("NCSQL");
			driver = bundle.getString("driver");
			dburl = bundle.getString("dburl");
			user = bundle.getString("user");
			password = bundle.getString("password");
			filepath = bundle.getString("filepath");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getDriver() {
		return driver;
	}

	public static void setDriver(String driver) {
		NCSQLInfoGet.driver = driver;
	}

	public static String getDburl() {
		return dburl;
	}

	public static void setDburl(String dburl) {
		NCSQLInfoGet.dburl = dburl;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		NCSQLInfoGet.user = user;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		NCSQLInfoGet.password = password;
	}

	
	
	
	
}
