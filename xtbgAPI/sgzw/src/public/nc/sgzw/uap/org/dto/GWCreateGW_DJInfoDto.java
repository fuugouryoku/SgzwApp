package nc.sgzw.uap.org.dto;

import java.util.ArrayList;


public class GWCreateGW_DJInfoDto {
	private String pk_swapreceivedoc = "";//主键
	private String title =""; //标题
	private String dispatchno =""; //发文文号
	private String issuer =""; //签发人
	private String issuedate =""; //签发日期
	private String dispatchunit =""; //发文单位
	private String senddate =""; //发送日期
	private String signdate =""; //发送日期
	private String receiptstatus =""; //接收状态
	private String recipient =""; //接收人
	private String returnreason =""; //退回原因
	private String  pk_file  =""; //正文附件地址
	private String  pk_file_name  =""; //正文附件名称
	private String  pk_file_size  =""; //正文附件大小
	
	public String getPk_file_size() {
		return pk_file_size;
	}
	public void setPk_file_size(String pk_file_size) {
		this.pk_file_size = pk_file_size;
	}
	private ArrayList<GWCreateGW_FILEDto> filedata =null;//附件 
	
	public String getPk_file_name() {
		return pk_file_name;
	}
	public void setPk_file_name(String pk_file_name) {
		this.pk_file_name = pk_file_name;
	}
	public String getPk_swapreceivedoc() {
		return pk_swapreceivedoc;
	}
	public void setPk_swapreceivedoc(String pk_swapreceivedoc) {
		this.pk_swapreceivedoc = pk_swapreceivedoc;
	}
	public ArrayList<GWCreateGW_FILEDto> getFiledata() {
		return filedata;
	}
	public void setFiledata(ArrayList<GWCreateGW_FILEDto> filedata) {
		this.filedata = filedata;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDispatchno() {
		return dispatchno;
	}
	public void setDispatchno(String dispatchno) {
		this.dispatchno = dispatchno;
	}
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public String getIssuedate() {
		return issuedate;
	}
	public void setIssuedate(String issuedate) {
		this.issuedate = issuedate;
	}
	public String getDispatchunit() {
		return dispatchunit;
	}
	public void setDispatchunit(String dispatchunit) {
		this.dispatchunit = dispatchunit;
	}
	public String getSenddate() {
		return senddate;
	}
	public void setSenddate(String senddate) {
		this.senddate = senddate;
	}
	public String getSigndate() {
		return signdate;
	}
	public void setSigndate(String signdate) {
		this.signdate = signdate;
	}
	public String getReceiptstatus() {
		return receiptstatus;
	}
	public void setReceiptstatus(String receiptstatus) {
		this.receiptstatus = receiptstatus;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getReturnreason() {
		return returnreason;
	}
	public void setReturnreason(String returnreason) {
		this.returnreason = returnreason;
	}
	public String getPk_file() {
		return pk_file;
	}
	public void setPk_file(String pk_file) {
		this.pk_file = pk_file;
	}

	

}
