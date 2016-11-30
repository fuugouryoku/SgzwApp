package nc.sgzw.uap.org.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.dto.UserInfo;
import nc.sgzw.uap.org.util.FileKit;
import nc.sgzw.uap.properties.StaticWordProperties;

public class GetuserinfoServer implements IHttpServletAdaptor {
	List<String> log=new ArrayList<String>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO 自动生成的方法存根
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		log.add(format.format(new Date())+"开始调用nc.sgzw.uap.org.server.GetuserinfoServer获取用户信息");
		UserInfo userInfoSession = new UserInfo();
		String code = request.getParameter("code") != null? request.getParameter("code"):"";
		log.add("获得参数code="+code);
		String accessToken = "";
		accessToken = getAccessToken(StaticWordProperties.secret, StaticWordProperties.appid);
		log.add("获得参数accessToken="+accessToken+"");
		if (accessToken != null && !accessToken.equals("")){
			 DefaultHttpClient httpClient = new DefaultHttpClient();
			 String certifiedUrl = StaticWordProperties.host + "certified/userInfo/" + code + "/?access_token=" + accessToken;
			 HttpGet method = new HttpGet(certifiedUrl);
			 method.addHeader("Content-Type","text/html;?charset=utf-8");
			 HttpResponse result = httpClient.execute(method);
		 if (result.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
				   HttpEntity entity = result.getEntity();
					String s = EntityUtils.toString(entity, "utf-8");
					JSONObject json = JSON.parseObject(s);
					if (json.get("data") != null) {
						JSONObject dataJson = JSON.parseObject(json.get("data").toString());
						userInfoSession = JSON.toJavaObject(dataJson, UserInfo.class);
						request.setAttribute("userinfo", userInfoSession);
					}else{
						log.add("返回信息data为空，信息获取失败");	
					}
			 }else{
				 log.add(result.getStatusLine().getStatusCode()+"错误:"+result.getStatusLine().getReasonPhrase());	 
			 }
			 response.getWriter().write(toJson(userInfoSession).toString()); 
		 }else{
			 response.getWriter().write("fail：AccessToken获取失败");
		 }
		log.add("获得用户信息接口调用完成");
		FileKit.addTask(log, "", "");
	}

	private String getAccessToken(String secret, String appid) {
		// TODO 自动生成的方法存根
		String accessToken = getCacheAccessToken();
		
		if (accessToken == "" ||accessToken == null) {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String tokenUrl = StaticWordProperties.host + "token/?appid=" + appid + "&secret=" + secret;	
			log.add(tokenUrl);
			FileKit.addTask(log, "", "");
			HttpGet method = new HttpGet(tokenUrl);
			try {
				HttpResponse result = httpClient.execute(method);
				if (result.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
					HttpEntity entity = result.getEntity();
					String s = EntityUtils.toString(entity, "utf-8");
					JSONObject json = JSON.parseObject(s);
					accessToken = JSON.parseObject(json.get("data").toString()).getString("access_token");
					setCacheAccessToken(accessToken);
				}
			} catch (ClientProtocolException e ){
				e.printStackTrace();
				log.add(e.getMessage());
				FileKit.addTask(log, "", "");
			} catch (IOException e){
				e.printStackTrace();
				log.add(e.getMessage());
				FileKit.addTask(log, "", "");
			}
		
		}
		
		return accessToken;
	}
	private String getCacheAccessToken() {
		// TODO  这里可以先缓存一份，比如存在redis里面或者存在session里面，注意要accessToken有有效期，如果用redis，建议设置redis里面的失效时间和accessToken一致。
		
		return "";
	}
	private String setCacheAccessToken(String accessToken) {

		// TODO 从接口获取到accessToken，存入缓存中策略中。
		return "";
	}
	public JSONObject toJson(UserInfo userinfo) {
		JSONObject joObj = new JSONObject();
		joObj.put("userinfo",userinfo);
		return joObj;
	}

}
