package nc.sgzw.uap.org.dto;

import java.util.ArrayList;

public class FWCreateFWDto {
	private FWUserInfoDto userInfo ;
	private int state;
	private String message ="";
	private String pk_senddoc ="";
	private ArrayList<FWCreateFW_MJDto> jjcdData = null;
	private ArrayList<FWCreateFW_BMDJDto> bmdjData = null;
	private ArrayList<FWCreateFW_ZTCDto> ztcData = null;
	private ArrayList<FWCreateFW_FWLXDto> FWLXData = null;
	private ArrayList<FWCreateFW_FWZZDto> fwzzData = null;

	public ArrayList<FWCreateFW_FWZZDto> getFwzzData() {
		return fwzzData;
	}

	public void setFwzzData(ArrayList<FWCreateFW_FWZZDto> fwzzData) {
		this.fwzzData = fwzzData;
	}

	public String getPk_senddoc() {
		return pk_senddoc;
	}

	public void setPk_senddoc(String pk_senddoc) {
		this.pk_senddoc = pk_senddoc;
	}

	public void setFWLXData(ArrayList<FWCreateFW_FWLXDto> fWLXData) {
		FWLXData = fWLXData;
	}

	public ArrayList<FWCreateFW_FWLXDto> getFWLXData() {
		return FWLXData;
	}

	public void setFWNXData(ArrayList<FWCreateFW_FWLXDto> fWLXData) {
		FWLXData = fWLXData;
	}

	public ArrayList<FWCreateFW_ZTCDto> getZtcData() {
		return ztcData;
	}

	public void setZtcData(ArrayList<FWCreateFW_ZTCDto> ztcData) {
		this.ztcData = ztcData;
	}

	public ArrayList<FWCreateFW_BMDJDto> getBmdjData() {
		return bmdjData;
	}

	public void setBmdjData(ArrayList<FWCreateFW_BMDJDto> bmdjData) {
		this.bmdjData = bmdjData;
	}

	public ArrayList<FWCreateFW_MJDto> getJjcdData() {
		return jjcdData;
	}

	public void setJjcdData(ArrayList<FWCreateFW_MJDto> jjcdData) {
		this.jjcdData = jjcdData;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public FWUserInfoDto getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(FWUserInfoDto userInfo) {
		this.userInfo = userInfo;
	}





}
