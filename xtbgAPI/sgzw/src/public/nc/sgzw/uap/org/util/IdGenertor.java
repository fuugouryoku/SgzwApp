package nc.sgzw.uap.org.util;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.vo.cmp.util.StringUtils;

public class IdGenertor  {
	public static String genGUID(){
		return new BigInteger(165, new Random()).toString(36).toUpperCase();
	}
	public static String genUUID(){
		return UUID.randomUUID().toString();
	}
	public static String generatePK(){
		String groupno = getGroupNO();
		return new SequenceGenerator().generate(groupno, 1)[0];
	}
	private static String getGroupNO() {
		String groupNo = null;
		groupNo = InvocationInfoProxy.getInstance().getGroupNumber();
		if(StringUtils.isEmpty(groupNo))
			groupNo = "0000";
		return groupNo;
	}
	public static String getRandomString(int length) { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyz";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }  

}
