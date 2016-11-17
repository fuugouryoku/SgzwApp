package nc.sgzw.uap.org.impl;


import java.util.ArrayList;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.sgzw.uap.org.dto.SWCreateSWDto;
import nc.sgzw.uap.org.dto.SWCreateSW_LWDWDto;
import nc.sgzw.uap.org.dto.SWCreateSW_SWLXDto;
import nc.sgzw.uap.org.dto.SWUserInfoDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.vo.arap.uforeport.SqlBuffer;

public class SWCreateSWDaoImpl {
	private ServiceException exception = new ServiceException();
	public static BaseDAO baseDAO;
	public static BaseDAO getBaseDAO() {	
		if (baseDAO == null){
			baseDAO = new BaseDAO();
		}
		return baseDAO;
	}
	//初始数据
	public SWCreateSWDto getinfo(String userid) {
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		SWCreateSWDto createFWdto = new SWCreateSWDto();
		SWUserInfoDto curr = getCuser(userid);
		createFWdto.setCreateSWUSERdto(curr);
		createFWdto.setSWLXData(getSWLX(curr.getCuserid()));
		createFWdto.setLWDWData(getlwdwdata(userid));
		return createFWdto;
	}
	
	
	public ServiceException exceptionInfo() {
		ServiceException exception  = new ServiceException();
		exception.setCode(this.exception.getCode());
		exception.setDesc(this.exception.getDesc());
		return exception;
	}
	//登录人信息
	public SWUserInfoDto getCuser(String userid) {
		SWUserInfoDto curruser = new SWUserInfoDto();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select cp.cuserid ,bp.name,bp.pk_group,bp.pk_org from cp_user cp  ");
		sql.append("left join bd_psndoc bp on bp.pk_psndoc = cp.pk_base_doc  ");
		sql.append("where cp.user_code = ? ");
		SQLParameter pstam=new SQLParameter();
			try {
				pstam.addParam(userid);
				curruser = (SWUserInfoDto)getBaseDAO().executeQuery(sql.toString(),pstam,new  BeanProcessor(SWUserInfoDto.class));
				curruser.setUser_code(userid);
			} catch (DAOException e) {
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
				System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
			} 

		return curruser;
	}
	//收文类型
	@SuppressWarnings("unchecked")
	public ArrayList<SWCreateSW_SWLXDto> getSWLX(String cuserid) {
		// TODO 自动生成的方法存根
		ArrayList<SWCreateSW_SWLXDto> swlxList = new ArrayList<SWCreateSW_SWLXDto>();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select oo.name as swlx_name,oo.pk_type as swlx_pk_type,oo.receiptflwtype as swlx_dispatchflwtype,wp.pk_prodef as swlx_pk_prodef from oaod_officialdoctype oo  ");
		sql.append("join wfm_prodef wp on wp.flwtype = oo.receiptflwtype ");
		sql.append("where wp.isnotstartup = ? and oo.pk_group = ? and oo.pk_org= ? ");
		SQLParameter pstam=new SQLParameter();
			try {
			
				pstam.addParam("Y");
				pstam.addParam("0001X710000000000E5W");
				pstam.addParam("0001X710000000002G3R");
				swlxList = (ArrayList<SWCreateSW_SWLXDto>)getBaseDAO().executeQuery(sql.toString(),pstam,new  BeanListProcessor(SWCreateSW_SWLXDto.class));
			} catch (DAOException e) {
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
				System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
			} 

		return swlxList;
	}

	//来文单位
	@SuppressWarnings("unchecked")
	public ArrayList<SWCreateSW_LWDWDto> getlwdwdata(String userid) {
		// TODO 自动生成的方法存根
		ArrayList<SWCreateSW_LWDWDto> list = new ArrayList<SWCreateSW_LWDWDto>();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select od.name as lwdw_name,od.pk_dispatchunit as lwdw_pk_dispatchunit  from oaod_dispatchunit od ");
		sql.append("right join bd_psndoc bp on  od.pk_org = bp.pk_org and od.pk_group = bp.pk_group ");
		sql.append("right join cp_user cu on cu.user_code = bp.code where cu.user_code= ?");
		SQLParameter pstam=new SQLParameter();
		if (ServiceException.SUCCESS_CODE == exception.getCode()){    
			try {
				pstam.addParam(userid);
				list = (ArrayList<SWCreateSW_LWDWDto>)getBaseDAO().executeQuery(sql.toString(),pstam,new  BeanListProcessor(SWCreateSW_LWDWDto.class));
			} catch (DAOException e) {
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
				System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
			} 
		}
		
		return list;
	}




}
