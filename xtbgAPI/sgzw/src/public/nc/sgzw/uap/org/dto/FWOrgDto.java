package nc.sgzw.uap.org.dto;

import java.util.ArrayList;


public class FWOrgDto {
	private int state ;
	private String message = null;
	private int total = 1;
	private ArrayList<FWOrgOrgsDto> data = null;
	
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
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public ArrayList<FWOrgOrgsDto> getData() {
		return data;
	}
	public void setData(ArrayList<FWOrgOrgsDto> data) {
		this.data = data;
	}

	

}
