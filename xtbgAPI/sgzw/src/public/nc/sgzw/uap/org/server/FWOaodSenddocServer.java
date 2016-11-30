package nc.sgzw.uap.org.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.oa.oaco.basecomp.associateform.AssociateFormUtil;
import nc.sgzw.uap.org.dto.FWUserInfoDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.org.util.FileKit;
import nc.sgzw.uap.org.util.IdGenertor;
import nc.sgzw.uap.properties.StaticWordProperties;
import nc.uap.wfm.constant.WfmConstants;
import nc.uap.wfm.exe.WfmCoreCmd;
import nc.uap.wfm.exe.WfmParams;
import nc.vo.oa.oaod.senddoc.AggSendDocVO;
import nc.vo.oa.oaod.senddoc.SDDispenseVO;
import nc.vo.oa.oaod.senddoc.SendDocVO;
import nc.vo.oa.oaod.senddoc.SendDocVOWfmImp;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONArray;
import org.json.JSONException;

public class FWOaodSenddocServer implements IHttpServletAdaptor {

	private ServiceException exception = null;

	public JSONObject toJson(ServiceException exception) {
		JSONObject joObj = new JSONObject();
		joObj.put("state", exception.getCode());
		joObj.put("message", exception.getDesc());
		joObj.put("num", 1);
		return joObj;
	}
	List<String> log=new ArrayList<String>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss");
	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		//�Զ����ɵķ������
		SendDocVO vo = null;
		exception = new ServiceException();
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		ISecurityTokenCallback sc = NCLocator
				.getInstance().lookup(ISecurityTokenCallback.class);
		sc.token("WSSystem".getBytes(), "WSSystem".getBytes());
		response.setCharacterEncoding("utf-8");
		response.setHeader("contentType", "text/html; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		log.add(format.format(new Date())+"��ʼ����nc.sgzw.uap.org.server.FWOaodSenddocServer�ύ���ĵ�");
		String  datastring = "";
		//�ж��Ƿ�Ϊpost����
		if ( request.getMethod().equals("POST") ) {
			//ȡ�ô������������
			datastring  = request.getParameter("data") !=null ? request.getParameter("data") :"" ;
			org.json.JSONObject object = null;
			try {		
				object = new org.json.JSONObject(datastring);
			} catch (JSONException e2) {
				//�Զ����ɵ� catch ��
				e2.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC_INFO_ANALYSIS);
			}
			if (object == null){
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC_INFO_ANALYSIS);
			}else {
				//��Ա��Ϣ���
				FWUserInfoDto userinfo = new FWUserInfoDto();
				org.json.JSONObject userObj = object.optJSONObject("userinfo") != null ?object.optJSONObject("userinfo"):null;
				if (userObj != null){
					userinfo.setCuserid(userObj.optString("cuserid"));
					userinfo.setName(userObj.optString("name"));
					userinfo.setPk_group(userObj.optString("pk_group"));
					userinfo.setPk_org(userObj.optString("pk_org"));
					String pk_senddoc = object.optString("pk_senddoc")!=null ?object.optString("pk_senddoc"):IdGenertor.generatePK();
					vo = new  SendDocVO(); 
					String fileid = IdGenertor.genUUID();
					//����
					vo.setSdtext(object.optString("sdtext")!=null ?object.optString("sdtext"):"");
					if(vo.getSdtext().equals("")){
						FWUploadServer upload = new FWUploadServer();
						File folder =new File(StaticWordProperties.filepath);
						File file =new File(StaticWordProperties.filepath+"/send.docx");
					    if  (!folder .exists()  && !folder.isDirectory()) { 
					    	folder.mkdir();
					    }
					    if(!file.exists()){
					    	 try {
									file.createNewFile();
								} catch (IOException e) {
									// TODO �Զ����ɵ� catch ��
									e.printStackTrace();
								}   
					    }
					    FileItem item = createFileItem(StaticWordProperties.filepath+"/send.docx");
					    log.add("�ϴ�Ĭ�����ĸ���");
					    String sdpath = upload.processUploadField(item, fileid,userinfo.getCuserid());
					    if (!sdpath.equals("")&& sdpath!=null){
					    	vo.setSdtext(sdpath);
					    	log.add("�ϴ�Ĭ�����ĳɹ�");
					    }else{
					    	log.add("�ϴ�Ĭ�����ĸ���ʧ��");	
					    }
					}
					//����ռλ����
					vo.setPk_senddoc(pk_senddoc);
					// TODO /**��������ǰ��¼�û��ģ�����**/
					//������֯����
					vo.setPk_group(userinfo.getPk_group());
					vo.setPk_org(userinfo.getPk_org());
					//����
					vo.setTitle(object.optString("title")!=null ?object.optString("title"):"");
					//�����
					org.json.JSONArray thematwordarray = object.optJSONArray("thematword") !=null ?object.optJSONArray("thematword"):new org.json.JSONArray();
					if (thematwordarray.length()>0){
						String thematword = "";
						for(int i = 0; i<thematwordarray.length();i++){
							if (i == thematwordarray.length()-1){
								thematword = thematword + thematwordarray.optString(i);
							}else {
								thematword = thematword + thematwordarray.optString(i)+",";	
							}
						}
						vo.setThematword(thematword);
					}else {
						vo.setThematword("");	
					}
					//��������
					String dispatchflwtype = "";
					String pk_prodef = "";
					String fwlxarray = object.optString("sdtype");
					org.json.JSONObject fwlx = null;
					try {
						fwlx = new org.json.JSONObject(fwlxarray);
					} catch (JSONException e2) {
						// TODO �Զ����ɵ� catch ��
						e2.printStackTrace();
					}
					//					org.json.JSONObject fwlx = object.optJSONObject("sdtype");
					if (fwlx != null){
						dispatchflwtype = fwlx.optString("fwlx_dispatchflwtype");
						pk_prodef = fwlx.optString("fwlx_pk_prodef");
						vo.setSdtype(fwlx.optString("fwlx_pk_type")!=null ?fwlx.optString("fwlx_pk_type"):"");
					}else{
						log.add("û�з�������");
					}
					//�����̶�
					vo.setEmergency(object.optString("emergency")!=null ?object.optString("emergency"):"~");
					//�ļ��ܼ�
					vo.setFilelevel(object.optString("filelevel")!=null ?object.optString("filelevel"):"~");
					//������֯
					vo.setSdorg(object.optString("sdorg")!=null ?object.optString("sdorg"):"~");
					//��ע
					vo.setRemark(object.optString("remark")!=null ?object.optString("remark"):"~");
					//��Ϣ�������
					vo.setVdef1(object.optString("vdef1")!=null ?object.optString("vdef1"):"~");
					//�����
					vo.setCreator(object.optString("creatorid")!=null ?object.optString("creatorid"):"");
					//�������
					String creationtime = object.optString("creationtime")!=null ?object.optString("creationtime"):"";
					if (creationtime != null && !"".equals(creationtime)){
						vo.setCreationtime(new UFDateTime(creationtime));
					}else {
						vo.setCreationtime(new UFDateTime(new Date()));
					}
					vo.setFileitem(object.optString("fileid")!=null ?object.optString("fileid"):"");
				
					//��������
					vo.setStatus(VOStatus.NEW);
					vo.setIschrom(UFBoolean.FALSE);
					vo.setIsdispense(UFBoolean.FALSE);
					vo.setIsfinaltext(UFBoolean.FALSE);
					vo.setIskeep(UFBoolean.FALSE);
					vo.setIssdmark(UFBoolean.FALSE);
					vo.setIsseal(UFBoolean.FALSE);
					vo.setIssign(UFBoolean.FALSE);
					vo.setIsend(UFBoolean.FALSE);
					//���ͣ�����
					SDDispenseVO[] sddispenseArray = null;
					String mainscope = object.optString("mainscope");//����
					String annxscope = object.optString("annxscope");//����
					JSONArray mainscopeList = new JSONArray();
					JSONArray annxscopeList = new JSONArray();
					if (mainscope != null && !mainscope.equals("")){
						try {
							mainscopeList = new  JSONArray(mainscope);

						} catch (JSONException e1) {
							e1.printStackTrace();
							exception.setCode(ServiceException.FAIL_CODE);
							exception.setDesc(ServiceException.FAIL_DESC+"���ͽ���ʧ��");
							log.add("���ͽ���ʧ��");
						}
					}
					if (annxscope != null && !annxscope.equals("")){
						try {
							annxscopeList = new  JSONArray(annxscope);

						} catch (JSONException e1) {
							e1.printStackTrace();
							exception.setCode(ServiceException.FAIL_CODE);
							exception.setDesc(ServiceException.FAIL_DESC+"���ͽ���ʧ��");
							log.add("���ͽ���ʧ��");
						}
					}
					int arraycount = mainscopeList.length() + annxscopeList.length();
					sddispenseArray = new SDDispenseVO[arraycount];
					//����
					if(mainscope !=null && !"".equals(mainscope)){
						try {
							for (int i = 0 ;i<mainscopeList.length();i++){
								SDDispenseVO ssvo = new SDDispenseVO();
								String pk_org = mainscopeList.getJSONObject(i).getString("pk_org");
								String isunit = mainscopeList.getJSONObject(i).getString("isunit"); 
								//�ַ�����
								ssvo.setDispensetype("1");
								ssvo.setStatus(VOStatus.NEW);
								//��Χ���� 
								if (isunit.toString().trim().equals("2")){
									ssvo.setPk_org(pk_org);
									ssvo.setScopetype("org");
								}else if(isunit.toString().trim().equals("3")){
									ssvo.setPk_dept(pk_org);
									ssvo.setScopetype("dept");
								}else if(isunit.toString().trim().equals("1")){
									ssvo.setPk_group(pk_org);
									ssvo.setScopetype("group");
								}else if(isunit.toString().trim().equals("psn")){
									ssvo.setPk_psndoc(pk_org);
									ssvo.setScopetype("psn");
								}
								//��������
								ssvo.setIsext(UFBoolean.FALSE);
								ssvo.setIssend(UFBoolean.FALSE);
								ssvo.setToall(UFBoolean.FALSE);
								ssvo.setToallchild(UFBoolean.FALSE);
								ssvo.setToamanu(UFBoolean.FALSE);
								ssvo.setTopic(UFBoolean.FALSE);
								ssvo.setTosdorg(UFBoolean.FALSE);
								sddispenseArray[i] = ssvo;
							}
						} catch (JSONException e) {
							e.printStackTrace();
							exception.setCode(ServiceException.FAIL_CODE);
							exception.setDesc(ServiceException.FAIL_DESC+"���ͷַ�ʧ��");
							log.add("���ͷַ�ʧ��");
						}
					}
					//����
					if(annxscope !=null && !"".equals(annxscope)){
						try {
							for (int i = 0 ;i<annxscopeList.length();i++){
								SDDispenseVO ssvo = new SDDispenseVO();
								String pk_org = annxscopeList.getJSONObject(i).getString("pk_org");
								String isunit = annxscopeList.getJSONObject(i).getString("isunit"); 
								//�ַ�����
								ssvo.setDispensetype("2");
								ssvo.setStatus(VOStatus.NEW);
								//��Χ���� 
								if (isunit.toLowerCase().trim().equals("2")){
									ssvo.setPk_org(pk_org);
									ssvo.setScopetype("org");
								}else if(isunit.toLowerCase().trim().equals("3")){
									ssvo.setPk_dept(pk_org);
									ssvo.setScopetype("dept");
								}else if(isunit.toLowerCase().trim().equals("1")){
									ssvo.setPk_group(pk_org);
									ssvo.setScopetype("group");
								}else if(isunit.toLowerCase().trim().equals("psn")){
									ssvo.setPk_psndoc(pk_org);
									ssvo.setScopetype("psn");
								}
								//��������
								ssvo.setIsext(UFBoolean.FALSE);
								ssvo.setIssend(UFBoolean.FALSE);
								ssvo.setToall(UFBoolean.FALSE);
								ssvo.setToallchild(UFBoolean.FALSE);
								ssvo.setToamanu(UFBoolean.FALSE);
								ssvo.setTopic(UFBoolean.FALSE);
								ssvo.setTosdorg(UFBoolean.FALSE);

								sddispenseArray[i+mainscopeList.length()] = ssvo;
							}
						} catch (JSONException e) {
							e.printStackTrace();
							exception.setCode(ServiceException.FAIL_CODE);
							exception.setDesc(ServiceException.FAIL_DESC+"���ͷַ�ʧ��");
							log.add("���ͷַ�ʧ��");
						}
					}
					//��������
					log.add("��ʼ��������");
					dataProcess(vo,userinfo,sddispenseArray,dispatchflwtype,pk_prodef);
				}else {
					exception.setCode(ServiceException.FAIL_CODE);
					exception.setDesc(ServiceException.FAIL_DESC+"��Ա��Ϣ��ȡʧ��");
					log.add("��Ա��Ϣ��ȡʧ��");
				}
			}
		}else {//
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC_CONN_TYPE);
			log.add(request.getMethod()+"�����޷�ʵ�֣������POST����");
		}
		//����json��Ϣ
		String result = toJson(exception).toString();
		response.getWriter().write(result);
		log.add("���ĵ��ύ�ӿڵ������");
		FileKit.addTask(log, "", "");
	}
	// TODO ��������
	public void dataProcess(SendDocVO vo,FWUserInfoDto userinfo,SDDispenseVO[] sddispenseArray,String dispatchflwtype,String pk_prodef) {
		AggSendDocVO  aggvo = new AggSendDocVO ();
		aggvo.setParentVO(vo);
		aggvo.setChildrenVO(sddispenseArray);
		SendDocVOWfmImp sendDocVOWfmImp = new SendDocVOWfmImp(aggvo);
		AssociateFormUtil.saveRelativeForm(vo.getPk_senddoc());
		// TODO /**��������ǰ��¼�û��ģ�����**/
		InvocationInfoProxy.getInstance().setGroupId(userinfo.getPk_group());
		InvocationInfoProxy.getInstance().setUserId(userinfo.getCuserid());
		WfmParams wfmParams = new WfmParams();
		wfmParams.setFormInfoCtx(sendDocVOWfmImp);
		wfmParams.setAttachBillitem(vo.getPk_senddoc());
		wfmParams.setSysId("bafile");
		wfmParams.setOperator(WfmConstants.WfmOperator_Agree);
		//��������
		wfmParams.setFlowTypePk(dispatchflwtype);
		//��������
		wfmParams.setProdefPk(pk_prodef);
		//��ǰ��֯���ɶ��й����ʲ��ල����ίԱ�ᣩ
		wfmParams.setCurGroupPk("0001X710000000000E5W");
		//��ǰ���ţ��ɶ��й����ʲ��ල����ίԱ�ᣩ
		wfmParams.setCurOrgPk("0001X710000000002G3R");
		//��ǰ�û�
		wfmParams.setCurUserPk(userinfo.getCuserid());

		try {
			new WfmCoreCmd(wfmParams).execute();
			log.add("���̲���ɹ�");
		}catch (Exception e){
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			if (e.getMessage() == null||e.getMessage().equals("")){
				exception.setDesc(ServiceException.FAIL_DESC+":���̲���ʧ��");
				log.add("���̲���ʧ��");
			}else{
				exception.setDesc(e.getMessage());
				log.add("���̲���ʧ�ܣ�"+e.getMessage());
			}

		}
	}
	//
	private FileItem createFileItem(String filePath)
    {   log.add("����Ĭ��send.docx�ļ�");
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "send";
        int num = filePath.lastIndexOf(".");
        String extFile = filePath.substring(num);
        FileItem item = factory.createItem(textFieldName, "text/plain", true,
            "send" + extFile);
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
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        	log.add("�ļ�����ʧ��");
        }
        log.add("�ļ����ɳɹ�");
        return item;

    }
}
