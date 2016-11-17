package nc.sgzw.uap.org.dto;
//��֯ (org_orgs) 
public class FWOrgOrgsDto {
	
	private String pk_fatherorg = null; // �ϼ�����
	private String pk_org = null; // ����ҵ��Ԫ
	private String name = null; // ���
	private String isunit = null; // ���
	private boolean ischeck = false; 


	public boolean isIscheck() {
		return ischeck;
	}
	public void setIscheck(boolean ischeck) {
		this.ischeck = ischeck;
	}
	public String getIsunit() {
		return isunit;
	}
	public void setIsunit(String isunit) {
		this.isunit = isunit;
	}
	
	public String getPk_fatherorg() {
		return pk_fatherorg;
	}
	public void setPk_fatherorg(String pk_fatherorg) {
		this.pk_fatherorg = pk_fatherorg;
	}
	public String getPk_org() {
		return pk_org;
	}
	public void setPk_org(String pk_org) {
		this.pk_org = pk_org;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

	 

	

}
