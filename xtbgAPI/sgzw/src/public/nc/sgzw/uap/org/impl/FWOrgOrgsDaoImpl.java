package nc.sgzw.uap.org.impl;


import java.util.ArrayList;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.sgzw.uap.org.dto.FWOrgOrgsDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.vo.arap.uforeport.SqlBuffer;


public class FWOrgOrgsDaoImpl  {

	private ServiceException exception = new ServiceException();
	public static BaseDAO baseDAO;
	public static BaseDAO getBaseDAO() {	
		if (baseDAO == null){
			baseDAO = new BaseDAO();
		}
		return baseDAO;
	}
	//发文组织
	@SuppressWarnings("unchecked")
	public ArrayList<FWOrgOrgsDto> getOrgOrgsData() {
		
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		ArrayList<FWOrgOrgsDto> dataList = new ArrayList<FWOrgOrgsDto>();
		SqlBuffer sql = new SqlBuffer();
		 sql.append("select name,pk_org,pk_fatherorg,orglevel as isunit from cp_orgs ");
		 sql.append("where orglevel in (?,?,?) ");
		 SQLParameter pstam=new SQLParameter();
		 try {
				pstam.addParam("1");
				pstam.addParam("2");
				pstam.addParam("3");
				dataList = (ArrayList<FWOrgOrgsDto>)getBaseDAO().executeQuery(sql.toString(),pstam,new  BeanListProcessor(FWOrgOrgsDto.class));
				
			} catch (DAOException e) {//conn.prepareStatement
				e.printStackTrace();
				exception.setCode(ServiceException.FAIL_CODE);
				exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
			} 
		return dataList;
	}

	

	public ServiceException exceptionInfo() {
		ServiceException exception  = new ServiceException();
		exception.setCode(this.exception.getCode());
		exception.setDesc(this.exception.getDesc());
		return exception;
	}





}
