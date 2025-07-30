package com.agaramtech.qualis.barcode.model;

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

@Entity
@Table(name = "sampledonor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleDonor extends CustomizedResultsetRowMapper<SampleDonor> implements Serializable, RowMapper<SampleDonor> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsampledonorcode ")
	private int nsampledonorcode;
	
	@Column(name = "nprojecttypecode ")
	private int nprojecttypecode;

	@Column(name = "ssampledonor ", length = 50, nullable = false)
	private String ssampledonor;

	@Column(name = "ncode", nullable = false)
	private short ncode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private boolean iscode;
	@Transient
	private boolean issampledonor;
	@Transient
	private String sprojecttypename;


	@Override
	public SampleDonor mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleDonor objSampleDonor = new SampleDonor();
		objSampleDonor.setNsampledonorcode(getInteger(arg0, "nsampledonorcode", arg1));
		objSampleDonor.setSsampledonor(StringEscapeUtils.unescapeJava(getString(arg0, "ssampledonor", arg1)));
		objSampleDonor.setNcode(getShort(arg0, "ncode", arg1));
		objSampleDonor.setIscode(getBoolean(arg0, "iscode", arg1));
		objSampleDonor.setIssampledonor(getBoolean(arg0, "issampledonor", arg1));
		objSampleDonor.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleDonor.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleDonor.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleDonor.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objSampleDonor.setSprojecttypename(StringEscapeUtils.unescapeJava(getString(arg0, "sprojecttypename", arg1)));


		return objSampleDonor;
	}

	
}