package nc.sgzw.uap.org.server;


import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.file.bapub.BaFileManager;
import nc.uap.lfw.file.vo.LfwFileVO;
import net.sf.json.JSONObject;

public class FWDeleteFileServer implements IHttpServletAdaptor{

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
	{
		// TODO 自动生成的方法存根
		ServiceException exception = new ServiceException();
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/html;charset=utf-8");
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
			String filePK  = request.getParameter("filepath") !=null ? request.getParameter("filepath") :"" ;
			BaFileManager filemanager = new BaFileManager();
			filemanager.setBamodule("oaod");
			LfwFileVO fileVO;
			try {
				fileVO = filemanager.getFileVO(filePK);
				filemanager.delete(fileVO);
			} catch (LfwBusinessException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":文件查询失败");
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":文件删除失败");
			}

		try {
			response.getWriter().write(toJson(exception).toString());
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}
	public JSONObject toJson(ServiceException exception) {
		JSONObject joObj = new JSONObject();
		joObj.put("state", exception.getCode());
		joObj.put("message", exception.getDesc());
		return joObj;
	}

}
