package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "approvalrolefilterdetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalRoleFilterDetail extends CustomizedResultsetRowMapper<ApprovalRoleFilterDetail> implements Serializable,RowMapper<ApprovalRoleFilterDetail>{

	private static final long serialVersionUID = 1L;
	
	 @Id
	 @Column(name = "napprovalfiltercode")
	 private int napprovalfiltercode;
	 
	 @Column(name = "napprovalconfigrolecode",nullable=false)
	 private int  napprovalconfigrolecode;
	 
	 @Column(name = "napprovalconfigcode",nullable=false)
	 private short napprovalconfigcode;
	 
	 @Column(name = "nuserrolecode",nullable=false)
	 private int  nuserrolecode;
	 
	 @Column(name = "nlevelno",nullable=false)
	 private short nlevelno;
	 
	 @Column(name = "ntransactionstatus",nullable=false)
	 private short  ntransactionstatus;
	 
	 @ColumnDefault("4")
	 @Column(name = "ndefaultstatus",nullable=false)
	 private short  ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	 
	 @ColumnDefault("1")
	 @Column(name = "nstatus",nullable=false)
	 private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	 
	 @ColumnDefault("-1")
	 @Column(name="nsitecode") 
	 private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	 
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String sdefaultstatus;
	@Transient
	private transient String sfilterstatus;
	@Transient
	private transient int ntranscode;
	@Transient
	private transient String stransstatus;
	 
	@Override
	public ApprovalRoleFilterDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ApprovalRoleFilterDetail objApprovalConfigRoleFilterDetail = new ApprovalRoleFilterDetail();
		
		 objApprovalConfigRoleFilterDetail.setNtranscode(getInteger(arg0,"ntranscode",arg1));
		 objApprovalConfigRoleFilterDetail.setSfilterstatus(getString(arg0,"sfilterstatus",arg1));
		 objApprovalConfigRoleFilterDetail.setNapprovalconfigrolecode(getInteger(arg0,"napprovalconfigrolecode",arg1));
		 objApprovalConfigRoleFilterDetail.setNapprovalfiltercode(getInteger(arg0,"napprovalfiltercode",arg1));
		 objApprovalConfigRoleFilterDetail.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		 objApprovalConfigRoleFilterDetail.setStransstatus(getString(arg0,"stransstatus",arg1));
		 objApprovalConfigRoleFilterDetail.setNlevelno(getShort(arg0,"nlevelno",arg1));
		 objApprovalConfigRoleFilterDetail.setSuserrolename(getString(arg0,"suserrolename",arg1));
		 objApprovalConfigRoleFilterDetail.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		 objApprovalConfigRoleFilterDetail.setNstatus(getShort(arg0,"nstatus",arg1));
		 objApprovalConfigRoleFilterDetail.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		 objApprovalConfigRoleFilterDetail.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		 objApprovalConfigRoleFilterDetail.setSdefaultstatus(getString(arg0,"sdefaultstatus",arg1));
		 objApprovalConfigRoleFilterDetail.setNsitecode(getShort(arg0,"nsitecode",arg1));

		 return objApprovalConfigRoleFilterDetail;
	}
	
}
