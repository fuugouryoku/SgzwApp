package nc.sgzw.uap.org.dto;

import java.util.ArrayList;


public class SWCreateSWDto {

	private int state;
	private String message ="";
	private String  pk_receiptregdoc ="";
	private SWUserInfoDto createSWUSERdto = null;
	private ArrayList<SWCreateSW_SWLXDto> SWLXData = null;
	private ArrayList<SWCreateSW_LWDWDto> LWDWData = null;
	public ArrayList<SWCreateSW_LWDWDto> getLWDWData() {
		return LWDWData;
	}
	public void setLWDWData(ArrayList<SWCreateSW_LWDWDto> lWDWData) {
		LWDWData = lWDWData;
	}
	public String getPk_receiptregdoc() {
		return pk_receiptregdoc;
	}
	public void setPk_receiptregdoc(String pk_receiptregdoc) {
		this.pk_receiptregdoc = pk_receiptregdoc;
	}
	public ArrayList<SWCreateSW_SWLXDto> getSWLXData() {
		return SWLXData;
	}
	public void setSWLXData(ArrayList<SWCreateSW_SWLXDto> sWLXData) {
		SWLXData = sWLXData;
	}
	public SWUserInfoDto getCreateSWUSERdto() {
		return createSWUSERdto;
	}
	public void setCreateSWUSERdto(SWUserInfoDto createSWUSERdto) {
		this.createSWUSERdto = createSWUSERdto;
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
	



}
