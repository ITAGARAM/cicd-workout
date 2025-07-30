package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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

/**
 * This class is used to map the fields of 'approvalconfigversion' table of the
 * Database.
 */
@Entity
@Table(name = "approvalconfigversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalConfigVersion extends CustomizedResultsetRowMapper<ApprovalConfigVersion>
		implements Serializable, RowMapper<ApprovalConfigVersion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "napproveconfversioncode")
	private int napproveconfversioncode;

	@Column(name = "napprovalconfigcode", nullable = false)
	private short napprovalconfigcode;

	@Column(name = "napproveconfversionno", nullable = false)
	private int napproveconfversionno;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "ntreeversiontempcode", nullable = false)
	private int ntreeversiontempcode;

	@Column(name = "sapproveconfversiondesc", length = 100)
	private String sapproveconfversiondesc;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sversionname;

	@Transient
	private transient String sneedautoapproval;

	@Transient
	private transient String sneedautocomplete;

	@Transient
	private transient String sneedjoballocation;

	@Transient
	private transient String sautoallot;

	@Transient
	private transient String sautodescisionstatus;

	@Transient
	private transient String sautoapprovalstatus;

	@Transient
	private transient String sversionstatus;

	@Transient
	private transient int napprovalconfigrolecode;

	@Transient
	private transient String stemplatemappingversion;

	@Override
	public ApprovalConfigVersion mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ApprovalConfigVersion objApproveConfVersion = new ApprovalConfigVersion();

		objApproveConfVersion.setNapproveconfversioncode(getInteger(arg0, "napproveconfversioncode", arg1));
		objApproveConfVersion.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objApproveConfVersion.setNapproveconfversionno(getInteger(arg0, "napproveconfversionno", arg1));
		objApproveConfVersion.setSapproveconfversiondesc(StringEscapeUtils.unescapeJava(getString(arg0, "sapproveconfversiondesc", arg1)));
		objApproveConfVersion.setNtreeversiontempcode(getInteger(arg0, "ntreeversiontempcode", arg1));
		objApproveConfVersion.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		objApproveConfVersion.setSversionstatus(getString(arg0, "sversionstatus", arg1));
		objApproveConfVersion.setSversionname(getString(arg0, "sversionname", arg1));
		objApproveConfVersion.setSneedautoapproval(getString(arg0, "sneedautoapproval", arg1));
		objApproveConfVersion.setSneedautocomplete(getString(arg0, "sneedautocomplete", arg1));
		objApproveConfVersion.setSneedjoballocation(getString(arg0, "sneedjoballocation", arg1));
		objApproveConfVersion.setSautoallot(getString(arg0, "sautoallot", arg1));
		objApproveConfVersion.setSautoapprovalstatus(getString(arg0, "sautoapprovalstatus", arg1));
		objApproveConfVersion.setSautodescisionstatus(getString(arg0, "sautodescisionstatus", arg1));
		objApproveConfVersion.setNapprovalconfigrolecode(getInteger(arg0, "napprovalconfigrolecode", arg1));
		objApproveConfVersion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objApproveConfVersion.setNstatus(getShort(arg0, "nstatus", arg1));
		objApproveConfVersion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objApproveConfVersion.setStemplatemappingversion(getString(arg0, "stemplatemappingversion", arg1));
		return objApproveConfVersion;
	}
}
