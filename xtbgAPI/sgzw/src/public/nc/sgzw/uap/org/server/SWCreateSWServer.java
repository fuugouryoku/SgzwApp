package nc.sgzw.uap.org.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.dto.SWCreateSWDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.impl.SWCreateSWDaoImpl;
import nc.sgzw.uap.org.util.IdGenertor;
import net.sf.json.JSONObject;


public class SWCreateSWServer implements IHttpServletAdaptor {

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		SWCreateSWDto createSWdto = new SWCreateSWDto();
		if ( request.getMethod().equals("POST") ) {
			String userid = request.getParameter("userid");
			if (userid !=null && !userid.equals("")) {
				SWCreateSWDaoImpl createSWdaoimpl = new SWCreateSWDaoImpl();
				createSWdto = createSWdaoimpl.getinfo(userid);
				ServiceException exception = createSWdaoimpl.exceptionInfo();
				createSWdto.setPk_receiptregdoc(IdGenertor.generatePK());
				createSWdto.setState(exception.getCode());
				createSWdto.setMessage(exception.getDesc());	
			}else {
				createSWdto.setState(ServiceException.FAIL_CODE);
				createSWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);	
			}
		}else {
			createSWdto.setState(ServiceException.FAIL_CODE);
			createSWdto.setMessage(ServiceException.FAIL_DESC_CONN_TYPE);
		}
		String result = toJson(createSWdto).toString();
		try {
			response.getWriter().write(result);
		} catch (IOException e) {

			e.printStackTrace();
			System.out.println("******************"+e.toString()+"******************");
		}
	}
	public JSONObject toJson(SWCreateSWDto createSWdto) {
		JSONObject joObj = new JSONObject();
		joObj.put("state", createSWdto.getState());
		joObj.put("message", createSWdto.getMessage());
		joObj.put("pk_receiptregdoc", createSWdto.getPk_receiptregdoc());
		joObj.put("userinfo", createSWdto.getCreateSWUSERdto());
		joObj.put("swlxdata", createSWdto.getSWLXData());
		joObj.put("lwdwdata", createSWdto.getLWDWData());
		joObj.put("file", createSWdto.getPk_receiptregdoc());
		joObj.put("sdfile",IdGenertor.genUUID());
		return joObj;
	}

	

}
