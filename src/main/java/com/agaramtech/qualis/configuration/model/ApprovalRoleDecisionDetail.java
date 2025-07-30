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
@Table(name = "approvalroledecisiondetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalRoleDecisionDetail extends CustomizedResultsetRowMapper<ApprovalRoleDecisionDetail>
		implements Serializable, RowMapper<ApprovalRoleDecisionDetail> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "napprovaldecisioncode")
	private int napprovaldecisioncode;

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

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String suserrolename;

	@Transient
	private transient int napproveconfversioncode;

	@Transient
	private transient String sdecisionstatus;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient String stransstatus;

	@Transient
	private transient int ntranscode;

	@Transient
	private transient String sdefaultstatus;

	@Transient
	private transient int nesignneed;

	@Override
	public ApprovalRoleDecisionDetail mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ApprovalRoleDecisionDetail objApprovalConfigRoleDecisionDetail = new ApprovalRoleDecisionDetail();

		objApprovalConfigRoleDecisionDetail.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objApprovalConfigRoleDecisionDetail.setNapprovaldecisioncode(getInteger(arg0, "napprovaldecisioncode", arg1));
		objApprovalConfigRoleDecisionDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		objApprovalConfigRoleDecisionDetail.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objApprovalConfigRoleDecisionDetail.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objApprovalConfigRoleDecisionDetail
				.setNapprovalconfigrolecode(getInteger(arg0, "napprovalconfigrolecode", arg1));
		objApprovalConfigRoleDecisionDetail.setSdecisionstatus(getString(arg0, "sdecisionstatus", arg1));
		objApprovalConfigRoleDecisionDetail.setNtranscode(getInteger(arg0, "ntranscode", arg1));
		objApprovalConfigRoleDecisionDetail.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		objApprovalConfigRoleDecisionDetail.setStransstatus(getString(arg0, "stransstatus", arg1));
		objApprovalConfigRoleDecisionDetail
				.setNapproveconfversioncode(getInteger(arg0, "napproveconfversioncode", arg1));
		objApprovalConfigRoleDecisionDetail.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objApprovalConfigRoleDecisionDetail.setSdefaultstatus(getString(arg0, "sdefaultstatus", arg1));
		objApprovalConfigRoleDecisionDetail.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objApprovalConfigRoleDecisionDetail.setNesignneed(getInteger(arg0, "nesignneed", arg1));
		objApprovalConfigRoleDecisionDetail.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objApprovalConfigRoleDecisionDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objApprovalConfigRoleDecisionDetail;
	}

}
