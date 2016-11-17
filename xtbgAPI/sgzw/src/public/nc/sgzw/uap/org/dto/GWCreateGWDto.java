package nc.sgzw.uap.org.dto;

import java.util.ArrayList;

public class GWCreateGWDto {
	private String cuserid ="";
	private int state;
	private String message ="";
	private ArrayList<GWCreateGW_DJInfoDto> djinfodata =null;

	
	

	public ArrayList<GWCreateGW_DJInfoDto> getDjinfodata() {
		return djinfodata;
	}

	public void setDjinfodata(ArrayList<GWCreateGW_DJInfoDto> djinfodata) {
		this.djinfodata = djinfodata;
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

	public String getCuserid() {
		return cuserid;
	}

	public void setCuserid(String cuserid) {
		this.cuserid = cuserid;
	}



}
