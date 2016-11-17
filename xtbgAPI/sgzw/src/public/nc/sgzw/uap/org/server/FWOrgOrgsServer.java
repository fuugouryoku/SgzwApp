package nc.sgzw.uap.org.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.dto.FWOrgDto;
import nc.sgzw.uap.org.dto.FWOrgOrgsDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.impl.FWOrgOrgsDaoImpl;
import net.sf.json.JSONObject;

public class FWOrgOrgsServer implements IHttpServletAdaptor {

	public FWOrgDto getListData() {
		FWOrgDto orgdto = new FWOrgDto();
		FWOrgOrgsDaoImpl orgorgsdaoimpl = new FWOrgOrgsDaoImpl();
		ArrayList<FWOrgOrgsDto> orgorgsList = orgorgsdaoimpl.getOrgOrgsData();
		ServiceException  exception = orgorgsdaoimpl.exceptionInfo();
		orgdto.setState(exception.getCode());
		orgdto.setMessage(exception.getDesc());
		orgdto.setData(orgorgsList);
		return orgdto;
	}

	public JSONObject toJson(FWOrgDto orgdto) {

		JSONObject joObj = new JSONObject();
		joObj.put("state", orgdto.getState());
		joObj.put("message", orgdto.getMessage());
		joObj.put("total", orgdto.getTotal());
		joObj.put("data", orgdto.getData()); 
		return joObj;
	}


	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String result = null;
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setHeader("Access-Control-Allow-Headers", "*, X-Requested-With,Authorization, X-Prototype-Version, X-CSRF-Token, Content-Type");
		response.setContentType("text/html;charset=utf-8");
		if ( request.getMethod().equals("POST") || request.getMethod().equals("GET") ) {
			FWOrgDto orgdto = getListData();
			result = toJson(orgdto).toString();
		}else {
			FWOrgDto orgdto = new FWOrgDto();
			ServiceException  exception = new ServiceException();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC_CONN_TYPE);
			orgdto.setState(exception.getCode());
			orgdto.setMessage(exception.getDesc());
			orgdto.setData(new ArrayList<FWOrgOrgsDto>());
			result = toJson(orgdto).toString();
		}
		try {
			response.getWriter().write(result);

		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			System.out.println("******************"+e.toString()+"******************");
		}	


	}


}
