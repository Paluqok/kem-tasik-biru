package com.heroku.java.model;

public class Staff {
	private int staffId;
	private String staffName;
	private String staffEmail;
	private String staffAddress;
	private String staffPhoneNo;
	private String staffPassword;
	private Integer managerId;
	
	public Staff() {}
	
	public Staff(int staffId,String staffName,String staffEmail,String staffAddress,String staffPhoneNo,String staffPassword,Integer managerId) {
		this.staffId=staffId;
		this.staffName=staffName;
		this.staffEmail=staffEmail;
		this.staffAddress=staffAddress;
		this.staffPhoneNo=staffPhoneNo;
		this.staffPassword=staffPassword;
		this.managerId=managerId;
	}
	
	
	public int getStaffId() {
		return staffId;
	}
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
	public String getStaffName() {
		return staffName;
	}
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	public String getStaffEmail() {
		return staffEmail;
	}
	public void setStaffEmail(String staffEmail) {
		this.staffEmail = staffEmail;
	}
	public String getStaffAddress() {
		return staffAddress;
	}
	public void setStaffAddress(String staffAddress) {
		this.staffAddress = staffAddress;
	}
	public String getStaffPhoneNo() {
		return staffPhoneNo;
	}
	public void setStaffPhoneNo(String staffPhoneNo) {
		this.staffPhoneNo = staffPhoneNo;
	}
	public String getStaffPassword() {
		return staffPassword;
	}
	public void setStaffPassword(String staffPassword) {
		this.staffPassword = staffPassword;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}
	
}
