package nc.sgzw.uap.org.server;


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.dto.UserInfo;

public class LoginServer implements IHttpServletAdaptor {

	// 开放平台地址
	private static final String HOST = "https://openapi.upesn.com/";

	// 对应企业空间轻应用CropSecret, 实际要对应修改
	private static final String secret = "9b3e69319d232124";

	// 对应企业空间轻应用CropId, 实际要对应修改
	private static final String appid 
	= "4dd8576115974b1d48a89de72623e814937dc5373133e23a1fb13750bf20";


	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response){
		// TODO 自动生成的方法存根
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		UserInfo userInfoSession = new UserInfo();
		String code = request.getParameter("code");
		try {
			String accessToken = getAccessToken(secret, appid);
			CloseableHttpClient httpclient = HttpClients.createDefault();
			String certifiedUrl = HOST + "certified/userInfo/" + code + "/?access_token=" + accessToken;
			HttpGet httpget2 = new HttpGet(certifiedUrl);
			CloseableHttpResponse rsp2 = httpclient.execute(httpget2);
			if (rsp2.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
				HttpEntity entity = rsp2.getEntity();
				String result = EntityUtils.toString(entity, "utf-8");
				JSONObject json = JSON.parseObject(result);
				if (json.get("data") != null) {
					JSONObject dataJson = JSON.parseObject(json.get("data").toString());
					userInfoSession = JSON.toJavaObject(dataJson, UserInfo.class);
					request.setAttribute("userinfo", userInfoSession);
				}	
			}
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		try {
			response.getWriter().write(toJson(userInfoSession).toString());
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	/**
	 * 获取接口调用令牌
	 * @param appid
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	private String getAccessToken(String appid, String secret) throws Exception {
		String accessToken = getCacheAccessToken();
		if (accessToken == "" ||accessToken == null) {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			String tokenUrl = HOST + "token/?appid=" + appid + "&secret=" + secret;
			HttpGet httpget = new HttpGet(tokenUrl);
			CloseableHttpResponse rsp = httpclient.execute(httpget);
			if (rsp.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
				HttpEntity entity = rsp.getEntity();
				String result = EntityUtils.toString(entity, "utf-8");
				JSONObject json = JSON.parseObject(result);
				accessToken = JSON.parseObject(json.get("data").toString()).getString("access_token");
				setCacheAccessToken(accessToken);
			}
		}
		return accessToken;
	}

	/**
	 * 获取缓存的令牌
	 * @return
	 */
	private String getCacheAccessToken() {
		// TODO  这里可以先缓存一份，比如存在redis里面或者存在session里面，注意要accessToken有有效期，如果用redis，建议设置redis里面的失效时间和accessToken一致。
		return "";
	}

	/**
	 * 设置缓存令牌
	 * @param accessToken
	 * @return
	 */
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
