package nc.sgzw.uap.org.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONException;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.oa.oaco.basecomp.associateform.AssociateFormUtil;
import nc.sgzw.uap.org.dto.SWUserInfoDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.util.FileKit;
import nc.sgzw.uap.org.util.IdGenertor;
import nc.sgzw.uap.properties.StaticWordProperties;
import nc.uap.wfm.constant.WfmConstants;
import nc.uap.wfm.exe.WfmCoreCmd;
import nc.uap.wfm.exe.WfmParams;
import nc.vo.oa.oaod.receiptregdoc.ReceiptRegDocVO;
import nc.vo.oa.oaod.receiptregdoc.ReceiptRegDocVOWfmImp;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import net.sf.json.JSONObject;

public class SWOaodReceiptregdocServer implements IHttpServletAdaptor {
	private ServiceException exception = null;
	List<String> log=new ArrayList<String>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		// TODO 自动生成的方法存根
		exception = new ServiceException();
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		ReceiptRegDocVO vo = null;
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
		log.add(format.format(new Date())+"开始调用nc.sgzw.uap.org.server.SWOaodReceiptregdocServer提交收文单");
		
		String  datastring = "";
		//判断是否为post请求
		if ( request.getMethod().equals("POST") ) {
		    datastring  = request.getParameter("data") !=null ? request.getParameter("data") :"" ;
			org.json.JSONObject object = null;
			try {
				object = new org.json.JSONObject(datastring);
			} catch (JSONException e2) {
				// TODO 自动生成的 catch 块
				e2.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC_INFO_ANALYSIS);
				log.add("数据解析失败");
			}
			if (object == null){
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC_INFO_ANALYSIS);
				log.add("数据解析失败");
			}else {
				//人员信息获得
				SWUserInfoDto userinfo = new SWUserInfoDto();
				org.json.JSONObject userObj = object.optJSONObject("userinfo") != null ?object.optJSONObject("userinfo"):null;
				if (userObj != null){
					userinfo.setCuserid(userObj.optString("cuserid"));
					userinfo.setName(userObj.optString("name"));
					userinfo.setPk_group(userObj.optString("pk_group"));
					userinfo.setPk_org(userObj.optString("pk_org"));
					String pk_receiptregdoc = object.optString("pk_receiptregdoc")!=null ?object.optString("pk_receiptregdoc"):IdGenertor.generatePK();
					vo = new ReceiptRegDocVO();
					String fileid = IdGenertor.genUUID();
					vo.setPk_file(object.optString("pk_file")!=null ?object.optString("pk_file"):"");
					if(vo.getPk_file().equals("")){
						SWUploadServer upload = new SWUploadServer();
						File folder =new File(StaticWordProperties.filepath);
						File file =new File(StaticWordProperties.filepath+"/reg.docx");
					    if  (!folder .exists()  && !folder.isDirectory()) { 
					    	folder.mkdir();
					    }
					    if(!file.exists()){
					    	 try {
									file.createNewFile();
								} catch (IOException e) {
									// TODO 自动生成的 catch 块
									e.printStackTrace();
								}   
					    }
					    FileItem item = createFileItem(StaticWordProperties.filepath+"/reg.docx");
					    log.add("正文附件上传开始");
					    String sdpath = upload.processUploadField(item, fileid,userinfo.getCuserid());
					    if (!sdpath.equals("")&& sdpath!=null){
					    	vo.setPk_file(sdpath);
					    	log.add("正文附件上传成功");
					    }else{
					    	log.add("正文附件上传失败");
					    }
					}
					//主键
					vo.setPk_receiptregdoc(pk_receiptregdoc);
					//传入组织部门
					vo.setPk_group(userinfo.getPk_group());
					vo.setPk_org(userinfo.getPk_org());
					//办理类型
					vo.setDealtype("Nothing");
					//收文日期
					String receiptdate = object.optString("receiptdate")!=null ?object.optString("receiptdate"):"";
					if (receiptdate != null && !"".equals(receiptdate)){
						vo.setReceiptdate(new UFDate(receiptdate));
					}else {
						vo.setReceiptdate(new UFDate(new Date()));
					}
					//来文单位
					vo.setDispatchunit(object.optString("dispatchunit")!=null ?object.optString("dispatchunit"):"~");
					//来文文号
					vo.setDispatchno(object.optString("dispatchno")!=null ?object.optString("dispatchno"):"~");
					//标题
					vo.setTitle(object.optString("title")!=null ?object.optString("title"):"");
					//备注
					vo.setRemark(object.optString("remark")!=null ?object.optString("remark"):"");
					//签收人
					vo.setRecipient(object.optString("recipient")!=null ?object.optString("recipient"):"");
					//登记人
					vo.setRegistrant(object.optString("registrant")!=null ?object.optString("registrant"):"");
					//拟办人
					vo.setDealer(object.optString("dealer")!=null ?object.optString("dealer"):"");
					//拟办意见
					vo.setDealopinion(object.optString("dealopinion")!=null ?object.optString("dealopinion"):"");
					//主办人
					vo.setHandler(object.optString("handler")!=null ?object.optString("handler"):"");
					//办理类型
					vo.setDealtype(object.optString("dealtype")!=null ?object.optString("dealtype"):"");
					//收文编号
                    vo.setDef1(object.optString("def1")!=null ?object.optString("def1"):"");
                    //附件
                    String attachBillitem = object.optString("fileid")!=null ?object.optString("fileid"):"";
                    vo.setFileitem(attachBillitem+"_file");
                    vo.setDocorigin("1");
                  //其他参数
					vo.setStatus(VOStatus.NEW);
					vo.setIsarchive(UFBoolean.FALSE);
					vo.setIsover(UFBoolean.FALSE);
					vo.setBillstatus("Run");
					
					//收文类型
					String dispatchflwtype = "";
					String pk_prodef = "";
					String fwlxarray = object.optString("doctype");
					org.json.JSONObject swlx = null;
					try {
						swlx = new org.json.JSONObject(fwlxarray);
					} catch (JSONException e2) {
						// TODO 自动生成的 catch 块
						e2.printStackTrace();
					}
					if (swlx != null){
						dispatchflwtype = swlx.optString("swlx_dispatchflwtype");
						pk_prodef = swlx.optString("swlx_pk_prodef");
						vo.setDoctype(swlx.optString("swlx_pk_type")!=null ?swlx.optString("swlx_pk_type"):"");
					}else{
						log.add("没有收文类型");
					}
				  //发送流程
					log.add("开始插入流程");
					dataProcess(vo,userinfo,dispatchflwtype,pk_prodef,attachBillitem);	
				
				}else {
						exception.setCode(ServiceException.FAIL_CODE);
						exception.setDesc(ServiceException.FAIL_DESC+"人员信息获取失败");
						log.add("人员信息获取失败");
					}
			}
		}else {
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC_CONN_TYPE);
			log.add(request.getMethod()+"请求无法实现，请采用POST请求");
		}
		String result = toJson().toString();

		try {
			response.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
			log.add("response返回信息失败");
		}
		log.add("提交收文单接口调用完成");
		FileKit.addTask(log, "", "");
	}
	public JSONObject toJson() {
		JSONObject joObj = new JSONObject();
		joObj.put("state", exception.getCode());
		joObj.put("message", exception.getDesc());
		return joObj;
	}
	//收文流程
	public void dataProcess(ReceiptRegDocVO vo,SWUserInfoDto userinfo,String dispatchflwtype,String pk_prodef,String attachBillitem ) {
		ReceiptRegDocVOWfmImp receiptRegDocVOWfmImp = new ReceiptRegDocVOWfmImp(vo);
		AssociateFormUtil.saveRelativeForm(vo.getPk_receiptregdoc());
		// TODO /**（？？当前登录用户的？？）**/
		InvocationInfoProxy.getInstance().setGroupId(userinfo.getPk_group());
		InvocationInfoProxy.getInstance().setUserId(userinfo.getCuserid());
		WfmParams wfmParams = new WfmParams();
		wfmParams.setFormInfoCtx(receiptRegDocVOWfmImp);
		wfmParams.setOperator(WfmConstants.WfmOperator_Agree);
		wfmParams.setAttachBillitem(vo.getPk_receiptregdoc()+"_file");
		//流程类型
		wfmParams.setFlowTypePk(dispatchflwtype);
		//审批流程
		wfmParams.setProdefPk(pk_prodef);
		//当前组织（成都市国有资产监督管理委员会）
		wfmParams.setCurGroupPk("0001X710000000000E5W");
		//当前部门（成都市国有资产监督管理委员会）
		wfmParams.setCurOrgPk("0001X710000000002G3R");
		//当前用户
		wfmParams.setCurUserPk(userinfo.getCuserid());

		try {
			new WfmCoreCmd(wfmParams).execute();
			log.add("流程插入成功");
		}catch (Exception e){
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			if (e.getMessage() == null||e.getMessage().equals("")){
				exception.setDesc(ServiceException.FAIL_DESC+":流程插入失败");
				log.add("流程插入失败");
			}else{
				exception.setDesc(e.getMessage());
				log.add("流程插入失败:"+e.getMessage());
			}
		}
	}
	
	private FileItem createFileItem(String filePath)
    {    log.add("生成默认正文附件reg.docx");
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "reg";
        int num = filePath.lastIndexOf(".");
        String extFile = filePath.substring(num);
        FileItem item = factory.createItem(textFieldName, "text/plain", true,
            "reg" + extFile);
        File newfile = new File(filePath);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try
        {
            FileInputStream fis = new FileInputStream(newfile);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192))
                != -1)
            {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
            log.add("正文附件创建 成功");
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        	log.add("正文附件创建失败");
        }

        return item;

    }

}
