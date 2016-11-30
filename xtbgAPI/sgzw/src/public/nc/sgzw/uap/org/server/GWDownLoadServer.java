package nc.sgzw.uap.org.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.file.bapub.BaFileManager;
import nc.uap.lfw.file.vo.LfwFileVO;

public class GWDownLoadServer implements IHttpServletAdaptor {

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws  IOException {
		// TODO 自动生成的方法存�?
		BaFileManager filemanager = new BaFileManager();
		String filePath = "";
		OutputStream out = null;
		if ("".equals(request.getParameter("filepath")) ||request.getParameter("filepath")==null) {
			filePath = request.getQueryString() != null ? request.getQueryString() :"";
		}else {
			filePath = request.getParameter("filepath");
		}
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("GBK");
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		if (filePath == null){
			filePath = "";
		}
		//获取文件信息
		LfwFileVO filevo = null;
		try {
			filevo	= filemanager.getFileVO(filePath);
		} catch (LfwBusinessException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		//文件信息存在设置返回为文件下载
		if (filevo != null){
			try {
				response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(filevo.getFilename(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			//下载文件
			try {
				out = response.getOutputStream();
				filemanager.download(filePath, out);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		if (out != null) {
			try {
				out.flush();
				out.close();
		
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	



}
