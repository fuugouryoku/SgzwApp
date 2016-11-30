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
		// TODO �Զ����ɵķ������
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		log.add(format.format(new Date())+"��ʼ����nc.sgzw.uap.org.server.FWCreateFWServer����һ�ŷ��ĵ�");
		FWCreateFWDto createFWdto = new FWCreateFWDto();
		if ( request.getMethod().equals("POST") ) {
			String userid = request.getParameter("userid");
			log.add("��ȡ����¼��id-userid=");
			if (userid !=null && !userid.equals("")) {
				FWCreateFWDaoImpl createFWdaoimpl = new FWCreateFWDaoImpl();
				createFWdto = createFWdaoimpl.getinfo(userid);
				createFWdto.setPk_senddoc(IdGenertor.generatePK());
				ServiceException exception = createFWdaoimpl.exceptionInfo();
				if (ServiceException.SUCCESS_CODE == exception.getCode()){
				createFWdto.setState(ServiceException.SUCCESS_CODE);
				createFWdto.setMessage(ServiceException.SUCCESS_DESC);
				log.add("��ȡ�����ĵ���Ϣ");
				} else {
					createFWdto.setState(ServiceException.FAIL_CODE);
					createFWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);
					log.add("���ĵ���Ϣ��ȡʧ��");
				}
			}else {
				createFWdto.setState(ServiceException.FAIL_CODE);
				createFWdto.setMessage(ServiceException.FAIL_DESC_USER_FIND);
				log.add("��¼����Ϣ��ȡʧ��");
			}
		}else {
			createFWdto.setState(ServiceException.FAIL_CODE);
			createFWdto.setMessage(ServiceException.FAIL_DESC_CONN_TYPE);
			log.add(request.getMethod()+"�����޷�ʵ�֣������POST����");
		}
		String result = toJson(createFWdto).toString();
		try {
			response.getWriter().write(result);
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
			log.add("response������Ϣʧ��");
		}
		log.add("��÷��ĵ���Ϣ�ӿڵ������");
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
