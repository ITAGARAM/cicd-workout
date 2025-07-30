package com.agaramtech.qualis.storagemanagement.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "samplestoragemaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageMaster extends CustomizedResultsetRowMapper<SampleStorageMaster>
		implements Serializable, Cloneable, RowMapper<SampleStorageMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplecode")
	private int nsamplecode;

	@Column(name = "nsamplestoragelocationcode")
	private short nsamplestoragelocationcode;

	@Column(name = "nstoragecategorycode")
	private short nstoragecategorycode;

	@Column(name = "scontainercode")
	private String scontainercode = "";

	@Column(name = "ssampletraycode")
	private String ssampletraycode = "";

	@Column(name = "ssamplearno")
	private String ssamplearno = "";

	@Column(name = "nsampleqty")
	private float nsampleqty = 0;

	@ColumnDefault("-1")
	@Column(name = "nunitcode")
	private int nunitcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "slocationhierarchy")
	private String slocationhierarchy = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleStorageMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageMaster sampleStorageMaster = new SampleStorageMaster();
		sampleStorageMaster.setNsamplecode(getInteger(arg0, "nsamplecode", arg1));
		sampleStorageMaster.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		sampleStorageMaster.setNstoragecategorycode(getShort(arg0, "nstoragecategorycode", arg1));
		sampleStorageMaster.setScontainercode(StringEscapeUtils.unescapeJava(getString(arg0, "scontainercode", arg1)));
		sampleStorageMaster
				.setSsampletraycode(StringEscapeUtils.unescapeJava(getString(arg0, "ssampletraycode", arg1)));
		sampleStorageMaster.setSsamplearno(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplearno", arg1)));
		sampleStorageMaster.setNsampleqty(getFloat(arg0, "nsampleqty", arg1));
		sampleStorageMaster.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		sampleStorageMaster
				.setSlocationhierarchy(StringEscapeUtils.unescapeJava(getString(arg0, "slocationhierarchy", arg1)));
		sampleStorageMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleStorageMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleStorageMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		return sampleStorageMaster;
	}
}
