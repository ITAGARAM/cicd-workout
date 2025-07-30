package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'approvalsubtype' table of the
 * Database.
 */
@Entity
@Table(name = "approvalsubtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalSubType extends CustomizedResultsetRowMapper<ApprovalSubType> implements Serializable, RowMapper<ApprovalSubType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "napprovalsubtypecode")
	private short napprovalsubtypecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "ntemplatecode", nullable = false)
	private int ntemplatecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nisregsubtypeconfigneed", nullable = false)
	private short nisregsubtypeconfigneed;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String ssubtypename;

	@Transient
	private transient String sapprovalsubtypename;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient String sdefaultname;

	@Override
	public ApprovalSubType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ApprovalSubType objApprovalSubType = new ApprovalSubType();

		objApprovalSubType.setSsubtypename(getString(arg0, "ssubtypename", arg1));
		objApprovalSubType.setNstatus(getShort(arg0, "nstatus", arg1));
		objApprovalSubType.setNapprovalsubtypecode(getShort(arg0, "napprovalsubtypecode", arg1));
		objApprovalSubType.setNtemplatecode(getShort(arg0, "ntemplatecode", arg1));
		objApprovalSubType.setNisregsubtypeconfigneed(getShort(arg0, "nisregsubtypeconfigneed", arg1));
		objApprovalSubType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objApprovalSubType.setSapprovalsubtypename(getString(arg0, "sapprovalsubtypename", arg1));
		objApprovalSubType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objApprovalSubType.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objApprovalSubType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objApprovalSubType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objApprovalSubType;
	}

}
