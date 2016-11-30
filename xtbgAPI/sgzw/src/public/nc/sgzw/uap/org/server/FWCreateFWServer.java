package nc.sgzw.uap.org.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.dto.FWCreateFWDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.impl.FWCreateFWDaoImpl;
import nc.sgzw.uap.org.util.FileKit;
import nc.sgzw.uap.org.util.IdGenertor;
import net.sf.json.JSONObject;

public class FWCreateFWServer implements IHttpServletAdaptor {
	List<String> log=new ArrayList<String>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
	{
		// TODO 自动生成的方法存根
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		log.add(format.format(new Date())+"开始调用nc.sgzw.uap.org.server.FWCreateFWServer创建一张发文单");
		FWCreateFWDto createFWdto = new FWCreateFWDto();
		if ( request.getMethod().equals("POST") ) {
			String userid = request.getParameter("userid");
			log.add("获取到登录人id-userid=");
			if (userid !=null && !userid.equals("")) {
				FWCreateFWDaoImpl createFWdaoimpl = new FWCreateFWDaoImpl();
				createFWdto = createFWdaoimpl.getinfo(userid);
				createFWdto.setPk_senddoc(IdGenertor.generatePK());
				ServiceException exception = createFWdaoimpl.exceptionInfo();
				if (ServiceException.SUCCESS_CODE == exception.getCode()){
				createFWdto.setState(ServiceException.SUCCESS_CODE);
				createFWdto.setMessage(ServiceException.SUCCESS_DESC);
				log.add("获取到发文单信息");
				} else {
					createFWdto.setState(ServiceException.FAIL_CODE);
					createFWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);
					log.add("发文单信息获取失败");
				}
			}else {
				createFWdto.setState(ServiceException.FAIL_CODE);
				createFWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);
				log.add("登录人信息获取失败");
			}
		}else {
			createFWdto.setState(ServiceException.FAIL_CODE);
			createFWdto.setMessage(ServiceException.FAIL_DESC_CONN_TYPE);
			log.add(request.getMethod()+"请求无法实现，请采用POST请求");
		}
		String result = toJson(createFWdto).toString();
		try {
			response.getWriter().write(result);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			log.add("response返回信息失败");
		}
		log.add("获得发文单信息接口调用完成");
		FileKit.addTask(log, "", "");
	}
	public JSONObject toJson(FWCreateFWDto createFWdto) {
		JSONObject joObj = new JSONObject();
		joObj.put("state", createFWdto.getState());
		joObj.put("message", createFWdto.getMessage());
		joObj.put("file", createFWdto.getPk_senddoc());
		joObj.put("sdfile", IdGenertor.genUUID());
		joObj.put("userinfo", createFWdto.getUserInfo());
		joObj.put("pk_senddoc", createFWdto.getPk_senddoc());
		joObj.put("jjcddata", createFWdto.getJjcdData());
		joObj.put("bmdjdata", createFWdto.getBmdjData());
		joObj.put("ztcdata", createFWdto.getZtcData());
		joObj.put("fwlxdata", createFWdto.getFWLXData());
		joObj.put("fwzzdata", createFWdto.getFwzzData());
		return joObj;
	}

}
