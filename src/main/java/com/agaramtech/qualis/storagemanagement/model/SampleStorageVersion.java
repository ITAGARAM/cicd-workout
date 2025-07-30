package com.agaramtech.qualis.storagemanagement.model;

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

@Entity
@Table(name = "samplestorageversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageVersion extends CustomizedResultsetRowMapper<SampleStorageVersion>
		implements Serializable, RowMapper<SampleStorageVersion> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsamplestorageversioncode")
	private short nsamplestorageversioncode;

	@Column(name = "nsamplestoragelocationcode")
	private short nsamplestoragelocationcode;

	@Column(name = "nversionno")
	private short nversionno;

	@Column(name = "napprovalstatus")
	private short napprovalstatus = 8;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String ssamplestoragelocationname;

	@Override
	public SampleStorageVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageVersion sampleStorageVersion = new SampleStorageVersion();
		sampleStorageVersion.setNsamplestorageversioncode(getShort(arg0, "nsamplestorageversioncode", arg1));
		sampleStorageVersion.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		sampleStorageVersion.setNversionno(getShort(arg0, "nversionno", arg1));
		sampleStorageVersion.setNapprovalstatus(getShort(arg0, "napprovalstatus", arg1));
		sampleStorageVersion.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		sampleStorageVersion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleStorageVersion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleStorageVersion.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleStorageVersion.setSsamplestoragelocationname(getString(arg0, "ssamplestoragelocationname", arg1));
		return sampleStorageVersion;
	}

}
