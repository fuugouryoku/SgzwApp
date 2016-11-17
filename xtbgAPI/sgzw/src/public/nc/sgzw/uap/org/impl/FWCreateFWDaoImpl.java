package nc.sgzw.uap.org.impl;


import java.util.ArrayList;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.BeanProcessor;
import nc.sgzw.uap.org.dto.FWCreateFWDto;
import nc.sgzw.uap.org.dto.FWCreateFW_BMDJDto;
import nc.sgzw.uap.org.dto.FWCreateFW_FWLXDto;
import nc.sgzw.uap.org.dto.FWCreateFW_FWZZDto;
import nc.sgzw.uap.org.dto.FWCreateFW_MJDto;
import nc.sgzw.uap.org.dto.FWCreateFW_OaDefdocDto;
import nc.sgzw.uap.org.dto.FWCreateFW_ZTCDto;
import nc.sgzw.uap.org.dto.FWUserInfoDto;
import nc.sgzw.uap.org.exception.ServiceException;
import nc.sgzw.uap.properties.NCInfoGet;
import nc.vo.arap.uforeport.SqlBuffer;

public class FWCreateFWDaoImpl{
	private ServiceException exception = new ServiceException();
	public static BaseDAO baseDAO;
	public static BaseDAO getBaseDAO() {	
		if (baseDAO == null){
			baseDAO = new BaseDAO();
		}
		return baseDAO;
	}
	//登录人信息
	public FWUserInfoDto getCuserid(String userid) {
		FWUserInfoDto userinfo = new FWUserInfoDto();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select cp.cuserid ,bp.name,bp.pk_group,bp.pk_org from cp_user cp  ");
		sql.append("left join bd_psndoc bp on bp.pk_psndoc = cp.pk_base_doc  ");
		sql.append("where cp.user_code = ? ");
		SQLParameter pstam=new SQLParameter();
		try {
			pstam.addParam( userid);
			userinfo = (FWUserInfoDto)getBaseDAO().executeQuery(sql.toString(), pstam, new  BeanProcessor(FWUserInfoDto.class));
			userinfo.setUser_code(userid);
		} catch (DAOException e) {
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
			System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
		} 

		return userinfo;
	}

	//
	public ServiceException exceptionInfo() {
		ServiceException exception  = new ServiceException();
		exception.setCode(this.exception.getCode());
		exception.setDesc(this.exception.getDesc());
		return exception;
	}

	//初始数据
	@SuppressWarnings("unchecked")
	public FWCreateFWDto getinfo(String userid) {
		exception.setCode(ServiceException.SUCCESS_CODE);
		exception.setDesc(ServiceException.SUCCESS_DESC);
		FWCreateFWDto createFWdto = new FWCreateFWDto();
		FWUserInfoDto userinfo = getCuserid(userid);
		createFWdto.setUserInfo(userinfo);
		ArrayList<FWCreateFW_FWLXDto> fwlxlist = getFWLX(userinfo.getCuserid());
		if (ServiceException.SUCCESS_CODE == exception.getCode()){  	
			createFWdto.setFWNXData(fwlxlist);
		}
				ArrayList<FWCreateFW_FWZZDto> fwzzlist = getFWZZ(userid);
				if (ServiceException.SUCCESS_CODE == exception.getCode()){  	
					createFWdto.setFwzzData(fwzzlist);
				}
		SqlBuffer sql = new SqlBuffer();
		sql.append("select name,pk_defdoclist,pk_defdoc from oa_defdoc where pk_defdoclist in(?,?,?) ");
		SQLParameter pstam=new SQLParameter();
		try {
			pstam.addParam( NCInfoGet.getBmdj());
			pstam.addParam( NCInfoGet.getJjcd());
			pstam.addParam( NCInfoGet.getZtc());
			ArrayList<FWCreateFW_OaDefdocDto>  list = new ArrayList<FWCreateFW_OaDefdocDto>() ;
			ArrayList<FWCreateFW_BMDJDto> BMDJList = new ArrayList<FWCreateFW_BMDJDto>();
			ArrayList<FWCreateFW_MJDto> MJList = new ArrayList<FWCreateFW_MJDto>();
			ArrayList<FWCreateFW_ZTCDto> ZTCList = new ArrayList<FWCreateFW_ZTCDto>();
			list= (ArrayList<FWCreateFW_OaDefdocDto>) getBaseDAO().executeQuery(sql.toString(), pstam, new BeanListProcessor(FWCreateFW_OaDefdocDto.class));
			for (int i = 0;i <list.size();i++){
				FWCreateFW_OaDefdocDto rs = list.get(i);
				String  pk_defdoclist = rs.getPk_defdoclist()!=null ? rs.getPk_defdoclist():"";
				String  name = rs.getName()!=null ? rs.getName():"";
				String  pk_defdoc = rs.getPk_defdoc()!=null ? rs.getPk_defdoc():"";

				if (pk_defdoclist.equals(NCInfoGet.getBmdj())) {
					FWCreateFW_BMDJDto createFW_BMDJdto = new FWCreateFW_BMDJDto();
					createFW_BMDJdto.setBmdj_name(name);
					createFW_BMDJdto.setBmdj_pk_defdoc(pk_defdoc);
					BMDJList.add(createFW_BMDJdto);
				} else if(pk_defdoclist.equals(NCInfoGet.getJjcd())) {
					FWCreateFW_MJDto createFW_MJdto = new FWCreateFW_MJDto();
					createFW_MJdto.setMj_name(name);
					createFW_MJdto.setMj_pk_defdoc(pk_defdoc);
					MJList.add(createFW_MJdto);
				}else if (pk_defdoclist.equals(NCInfoGet.getZtc())) {
					FWCreateFW_ZTCDto createFW_ZTCdto = new  FWCreateFW_ZTCDto();
					createFW_ZTCdto.setZtc_name(name);
					createFW_ZTCdto.setZtc_pk_defdoc(pk_defdoc);
					ZTCList.add(createFW_ZTCdto);
				}
			}
			createFWdto.setBmdjData(BMDJList);
			createFWdto.setJjcdData(MJList);
			createFWdto.setZtcData(ZTCList);
		} catch (DAOException e) {
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
			System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
		} 
		return createFWdto;
	}

	//来文类型
	@SuppressWarnings("unchecked")
	public ArrayList<FWCreateFW_FWLXDto> getFWLX(String cuserid) {
		
		ArrayList<FWCreateFW_FWLXDto> fwlxList = new ArrayList<FWCreateFW_FWLXDto>();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select oo.name as fwlx_name ,oo.pk_type as fwlx_pk_type,oo.dispatchflwtype as fwlx_dispatchflwtype ,wp.pk_prodef as fwlx_pk_prodef from oaod_officialdoctype oo  ");
		sql.append("join wfm_prodef wp on wp.flwtype = oo.dispatchflwtype ");
		sql.append("where wp.isnotstartup = ? and oo.pk_group = ? and oo.pk_org= ? ");
		SQLParameter pstam=new SQLParameter();
		try {
			pstam.addParam("Y");
			pstam.addParam("0001X710000000000E5W");
			pstam.addParam("0001X710000000002G3R");
			fwlxList= (ArrayList<FWCreateFW_FWLXDto>) getBaseDAO().executeQuery(sql.toString(), pstam, new BeanListProcessor(FWCreateFW_FWLXDto.class));
		} catch (DAOException e ) {
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
			System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());
		} 

		return fwlxList;
	}

	//发文组织
	@SuppressWarnings("unchecked")
	public ArrayList<FWCreateFW_FWZZDto> getFWZZ(String userid) {
		ArrayList<FWCreateFW_FWZZDto> list = new ArrayList<FWCreateFW_FWZZDto>();
		SqlBuffer sql = new SqlBuffer();
		sql.append("select  oa.name as fwzz_name ,pk_adminorg as pk_adminorg  from org_adminorg oa ");
		sql.append("left join cp_user cp on cp.pk_org = oa.pk_org and cp.pk_group = oa.pk_group ");
		sql.append("where cp.user_code = ? and oa.pk_fatherorg =? ");
		SQLParameter pstam=new SQLParameter();	
		try {
			pstam.addParam(userid);
			pstam.addParam("~");
			list= (ArrayList<FWCreateFW_FWZZDto>) getBaseDAO().executeQuery(sql.toString(), pstam, new BeanListProcessor(FWCreateFW_FWZZDto.class));
		} catch (DAOException e) {
			e.printStackTrace();
			exception.setCode(ServiceException.FAIL_CODE);
			exception.setDesc(ServiceException.FAIL_DESC+":"+e.getMessage());
			System.out.println("******************"+e.toString()+"******************"+this.getClass().getName());

		}
		return list;
	}
}
