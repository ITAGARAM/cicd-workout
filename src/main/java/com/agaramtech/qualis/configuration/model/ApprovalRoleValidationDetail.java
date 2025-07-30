package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
@Table(name = "approvalrolevalidationdetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ApprovalRoleValidationDetail extends CustomizedResultsetRowMapper<ApprovalRoleValidationDetail> implements Serializable,RowMapper<ApprovalRoleValidationDetail> {

	private static final long serialVersionUID = 1L;

	
	 @Id
	 @Column(name = "napprovalvalidationcode")
	 private int napprovalvalidationcode;
	 
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
	 
	 @ColumnDefault("1")
	 @Column(name = "nstatus",nullable=false)
	 private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	 
	 @Column(name = "dmodifieddate")
	 private Instant dmodifieddate;
	 
	 @ColumnDefault("-1")
	 @Column(name="nsitecode") 
	 private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	 
	 @Transient
	 private transient String svalidationstatus;
	 @Transient
	 private transient int ntranscode;
	 @Transient
	 private transient String stransstatus;
	 @Transient
	 private transient String suserrolename;
	 
	@Override
	public ApprovalRoleValidationDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ApprovalRoleValidationDetail objApprovalConfigRoleValidationDetail = new ApprovalRoleValidationDetail();
		 objApprovalConfigRoleValidationDetail.setNapprovalvalidationcode(getInteger(arg0,"napprovalvalidationcode",arg1));
		 objApprovalConfigRoleValidationDetail.setSuserrolename(getString(arg0,"suserrolename",arg1));
		 objApprovalConfigRoleValidationDetail.setNlevelno(getShort(arg0,"nlevelno",arg1));
		 objApprovalConfigRoleValidationDetail.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		 objApprovalConfigRoleValidationDetail.setNstatus(getShort(arg0,"nstatus",arg1));
		 objApprovalConfigRoleValidationDetail.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		 objApprovalConfigRoleValidationDetail.setSvalidationstatus(getString(arg0,"svalidationstatus",arg1));
		 objApprovalConfigRoleValidationDetail.setNapprovalconfigrolecode(getInteger(arg0,"napprovalconfigrolecode",arg1));
		 objApprovalConfigRoleValidationDetail.setStransstatus(getString(arg0,"stransstatus",arg1));
		 objApprovalConfigRoleValidationDetail.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		 objApprovalConfigRoleValidationDetail.setNtranscode(getInteger(arg0,"ntranscode",arg1));
		 objApprovalConfigRoleValidationDetail.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		 objApprovalConfigRoleValidationDetail.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objApprovalConfigRoleValidationDetail;
	}
	
}
