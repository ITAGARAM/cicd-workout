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
@Data
@Table(name = "approvalroleactiondetail")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalRoleActionDetail extends CustomizedResultsetRowMapper<ApprovalRoleActionDetail>
		implements Serializable, RowMapper<ApprovalRoleActionDetail> {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "napprovalactioncode")
	private int napprovalactioncode;

	@Column(name = "napprovalconfigrolecode", nullable = false)
	private int napprovalconfigrolecode;

	@Column(name = "napprovalconfigcode", nullable = false)
	private short napprovalconfigcode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nlevelno", nullable = false)
	private short nlevelno;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "nsitecode")
	private short nsitecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient short nneedvalidation;
	@Transient
	private transient int napproveconfversioncode;
	@Transient
	private transient String sactiondisplaystatus;
	@Transient
	private transient short nesignneed;
	@Transient
	private transient short napprovalstatuscode;

	@Override
	public ApprovalRoleActionDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ApprovalRoleActionDetail objApprovalConfigRoleActionDetail = new ApprovalRoleActionDetail();
		objApprovalConfigRoleActionDetail.setNlevelno(getShort(arg0, "nlevelno", arg1));
		objApprovalConfigRoleActionDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		objApprovalConfigRoleActionDetail.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objApprovalConfigRoleActionDetail.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objApprovalConfigRoleActionDetail.setNapprovalconfigrolecode(getInteger(arg0, "napprovalconfigrolecode", arg1));
		objApprovalConfigRoleActionDetail.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		objApprovalConfigRoleActionDetail.setNapprovalactioncode(getInteger(arg0, "napprovalactioncode", arg1));
		objApprovalConfigRoleActionDetail.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objApprovalConfigRoleActionDetail.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objApprovalConfigRoleActionDetail.setNneedvalidation(getShort(arg0, "nneedvalidation", arg1));
		objApprovalConfigRoleActionDetail.setNapproveconfversioncode(getInteger(arg0, "napproveconfversioncode", arg1));
		objApprovalConfigRoleActionDetail.setSactiondisplaystatus(getString(arg0, "sactiondisplaystatus", arg1));
		objApprovalConfigRoleActionDetail.setNesignneed(getShort(arg0, "nesignneed", arg1));
		objApprovalConfigRoleActionDetail.setNapprovalstatuscode(getShort(arg0, "napprovalstatuscode", arg1));
		objApprovalConfigRoleActionDetail.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objApprovalConfigRoleActionDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objApprovalConfigRoleActionDetail;
	}

}
