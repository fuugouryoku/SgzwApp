package nc.sgzw.uap.org.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.util.FileKit;
import nc.uap.lfw.file.bapub.BaFileManager;

public class SWUploadServer implements IHttpServletAdaptor {

	private ServiceException exception = new ServiceException();
	//判断multipart/form-data的传输方式
	public boolean checkRequestEnctype(HttpServletRequest request) {

		return ServletFileUpload.isMultipartContent(request);
	}
	List<String> log=new ArrayList<String>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");
	@SuppressWarnings("unchecked")
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO 自动生成的方法存根
		String fileid = "";
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET");
		response.setContentType("text/html;charset=utf-8");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		request.setCharacterEncoding("utf-8");
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		log.add(format.format(new Date())+"开始调用nc.sgzw.uap.org.server.SWUploadServer上传文件");
		
        String result = "";
		//判断是否为post请求
		if ( request.getMethod().equals("POST") ) {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload sfu = new ServletFileUpload(factory);
			//设置单个文件的大小
			sfu.setFileSizeMax(10*1024*1024);
			List<FileItem> items = new ArrayList<FileItem>();
			try {
				items = sfu.parseRequest(request);
				FileItem uploaditem = null;
				String cuserid = "";
				//遍历上传的信息
				for(FileItem item : items) {
					if (item.isFormField()) {//非文件信息的处理
						String fieldName = item.getFieldName();
						String fieldValue = item.getString();
						if (fieldName.equals("fileid")&&fileid.endsWith("")) {
							fileid = fieldValue;
						}else if (fieldName.equals("userinfo")){
							cuserid = fieldValue;
						}
					}else {//处理文件，得到文件集合
						if(uploaditem == null ){
							uploaditem = item;	
						}
					}
				}
				if (fileid.equals("")||uploaditem == null){
					exception.setCode(ServiceException.FAIL_CODE);
					exception.setDesc(ServiceException.FAIL_DESC+"：文件或文件主键获取失败");
					log.add("文件或文件主键获取失败");
				}else {
					result = processUploadField(uploaditem,fileid,cuserid);
				}

			} catch (FileSizeLimitExceededException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC_FILE_SIZE);
				log.add("文件超过限制大小");
			} catch (FileUploadException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC_FILE_ANALYSIS);
				log.add("文件解析失败");
			}
		}else {
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC_CONN_TYPE);
			log.add(request.getMethod()+"请求无法实现，请采用POST请求");
		}

		
		response.getWriter().write(toJson(result).toString());
		log.add("文件上传接口调用完成");
		FileKit.addTask(log, "", "");
	}
	//上传文件解析
	public String processUploadField(FileItem item,String fileid,String cuserid) {
		BaFileManager filemanager = new BaFileManager();
		String name = FilenameUtils.getName(item.getName());
		String filetype = "wfm_attach";
//		filemanager.setBamodule("oaod");
		String resu = "";
		InvocationInfoProxy.getInstance().setUserId(cuserid);
		try {
		    resu = filemanager.upload(name, filetype, fileid+"_file", item.getSize(), item.getInputStream());
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC_FILE_UPLOAD);
			log.add("文件上传失败");
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC_FILE_UPLOAD);
			log.add("文件上传失败");
		}
		log.add("文件上传成功");
		return resu;
	}
	public net.sf.json.JSONObject toJson(String result) {
		net.sf.json.JSONObject joObj = new net.sf.json.JSONObject();
		joObj.put("state", exception.getCode());
		joObj.put("message", exception.getDesc());
		joObj.put("filepath", result);
		return joObj;
	}
//	private String getfiletype(String filename) {
//		String prefix=filename.substring(filename.lastIndexOf(".")+1);
//		return prefix;
//	}


}
