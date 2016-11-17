package nc.sgzw.uap.org.impl;

import java.util.ArrayList;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.sgzw.uap.org.dto.GWCreateGWDto;
import nc.sgzw.uap.org.dto.GWCreateGW_DJInfoDto;
import nc.sgzw.uap.org.dto.GWCreateGW_FILEDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.vo.arap.uforeport.SqlBuffer;

public class GWCreateGWDaoImpl {
	private ServiceException exception = new ServiceException();
	public static BaseDAO baseDAO;
	public static BaseDAO getBaseDAO() {	
		if (baseDAO == null){
			baseDAO = new BaseDAO();
		}
		return baseDAO;
	}
	//登录人信息
	public String getCuserid(String userid) {
		String cuserid = "";
		SqlBuffer sql = new SqlBuffer();
		sql.append("select cuserid from cp_user  where user_code = ? ");
		SQLParameter pstam=new SQLParameter();
			try {
				pstam.addParam(userid);
				cuserid = (String)getBaseDAO().executeQuery(sql.toString(),pstam, new ColumnProcessor());
				
			} catch (DAOException e) {
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
				System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
			} 
		return cuserid;
	}
	
	public ServiceException exceptionInfo() {
		ServiceException exception  = new ServiceException();
		exception.setCode(this.exception.getCode());
		exception.setDesc(this.exception.getDesc());
		return exception;
	}

	//初始化数据
	@SuppressWarnings("unchecked")
	public GWCreateGWDto getinfo(String userid) {
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		GWCreateGWDto createFWdto = new GWCreateGWDto();
		String currid = getCuserid(userid);
		createFWdto.setCuserid(currid);
		ArrayList<GWCreateGW_DJInfoDto> djinfodata = new ArrayList<GWCreateGW_DJInfoDto>();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select os.title,os.dispatchno,os.issuer,os.issuedate,os.dispatchunit,os.senddate, ");
		sql.append("os.signdate,os.receiptstatus,os.recipient,os.returnreason,os.pk_file ,os.pk_swapreceivedoc,bfh.name as pk_file_name ,bfh.filesize as pk_file_size  ");
		sql.append("from oaod_SwapReceiptDoc os ");
		sql.append(" left join bap_fs_header bfh on bfh.path = os.pk_file");
		if (ServiceException.SUCCESS_CODE == exception.getCode()){    
			try {
				djinfodata = (ArrayList<GWCreateGW_DJInfoDto>)getBaseDAO().executeQuery(sql.toString(), new BeanListProcessor(GWCreateGW_DJInfoDto.class));
			       for (int i = 0;i<djinfodata.size();i++){
			    	   String receiptstatus = djinfodata.get(i).getReceiptstatus();
			    	   if (receiptstatus.equals("Presign")){
			    		    djinfodata.get(i).setReceiptstatus("待签收");
						}else if (receiptstatus.equals("Signed")){
							djinfodata.get(i).setReceiptstatus("已签收");	
						}else if (receiptstatus.equals("Reject")){
							djinfodata.get(i).setReceiptstatus("退回");	
						}else {
							djinfodata.get(i).setReceiptstatus("");
						}
			    	   ArrayList<GWCreateGW_FILEDto> datalist = new ArrayList<GWCreateGW_FILEDto>();
						if (djinfodata.get(i).getPk_swapreceivedoc() != null && !djinfodata.get(i).equals("") ) {
							datalist = getfiledata(djinfodata.get(i).getPk_swapreceivedoc());
						}
						djinfodata.get(i).setFiledata(datalist);
			       }
				createFWdto.setDjinfodata(djinfodata);
			} catch (DAOException e) {
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
				System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
			} 
		}
		return createFWdto;
	}

	//附件信息
	@SuppressWarnings("unchecked")
	public ArrayList<GWCreateGW_FILEDto> getfiledata(String pk_swapreceivedoc) {
		
		ArrayList<GWCreateGW_FILEDto> datalist = new ArrayList<GWCreateGW_FILEDto>();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select bfh.filesize,bfh.name as filename  ,bfh.path as filepath from bap_fs_header bfh where bfh.guid in ");
		sql.append("(select  tul.fld_headerId from  tb_uw_lfwfile tul where tul.fld_pk_billitem = ");
		sql.append("(select ose.pk_senddoc from oaod_senddoc ose where ose.pk_senddoc = ");
		sql.append("(select osa.pk_senddoc from oaod_swapdoc osa where osa.pk_swapdoc = ");
		sql.append("(select ost.pk_swapdoc from oaod_swapdocdetail ost where ost.pk_swapdocdetail = ");
		sql.append("(select osd.clueno from oaod_SwapReceiptDoc osd where osd.pk_swapreceivedoc = ?)))))");
		SQLParameter pstam=new SQLParameter();
		if (ServiceException.SUCCESS_CODE == exception.getCode()){    
			try {
				pstam.addParam(pk_swapreceivedoc);
				datalist = (ArrayList<GWCreateGW_FILEDto>)getBaseDAO().executeQuery(sql.toString(),pstam,new  BeanListProcessor(GWCreateGW_FILEDto.class));
				
			} catch (DAOException e) {
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
				System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
			} 
		}

		return datalist;
	}

}
