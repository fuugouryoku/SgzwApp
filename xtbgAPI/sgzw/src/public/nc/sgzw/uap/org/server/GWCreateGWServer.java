package nc.sgzw.uap.org.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.dto.GWCreateGWDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.impl.GWCreateGWDaoImpl;
import net.sf.json.JSONObject;

public class GWCreateGWServer implements IHttpServletAdaptor {

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
	{
	
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		GWCreateGWDto createFWdto = new GWCreateGWDto();
		if ( request.getMethod().equals("POST") ) {
			String userid = request.getParameter("userid");
			if (userid !=null && !userid.equals("")) {
				GWCreateGWDaoImpl createFWdaoimpl = new GWCreateGWDaoImpl();
				createFWdto = createFWdaoimpl.getinfo(userid);
				ServiceException exception = createFWdaoimpl.exceptionInfo();
				if (ServiceException.SUCCESS_CODE == exception.getCode()){
				createFWdto.setState(ServiceException.SUCCESS_CODE);
				createFWdto.setMessage(ServiceException.SUCCESS_DESC);
				} else {
					createFWdto.setState(ServiceException.FAIL_CODE);
					createFWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);
				}
			}else {
				createFWdto.setState(ServiceException.FAIL_CODE);
				createFWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);	
			}
		}else {
			createFWdto.setState(ServiceException.FAIL_CODE);
			createFWdto.setMessage(ServiceException.FAIL_DESC_CONN_TYPE);
		}
		String result = toJson(createFWdto).toString();
		try {
			response.getWriter().write(result);
		} catch (IOException e) {
			// 
			e.printStackTrace();
			System.out.println("******************"+e.toString()+"******************");
		}

	}
	public JSONObject toJson(GWCreateGWDto createFWdto) {
		JSONObject joObj = new JSONObject();
		joObj.put("state", createFWdto.getState());
		joObj.put("message", createFWdto.getMessage());
		joObj.put("cuserid", createFWdto.getCuserid());
		joObj.put("infodata", createFWdto.getDjinfodata());
		return joObj;
	}

}
