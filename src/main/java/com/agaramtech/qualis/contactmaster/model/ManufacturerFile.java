package com.agaramtech.qualis.contactmaster.model;

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
@Data
@Table(name = "manufacturerfile")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ManufacturerFile extends CustomizedResultsetRowMapper<ManufacturerFile>
		implements Serializable, RowMapper<ManufacturerFile> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmanufacturerfilecode")
	private int nmanufacturerfilecode ;
	
	@Column(name = "nmanufcode", nullable = false)
	private int nmanufcode;
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;
	
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename = "";
	
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize = (int)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	
	@Column(name = "ntzcreateddate")
	private short ntzcreateddate;
	
	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate = (int)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename = "";
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String slinkname = "";
	
	@Transient
	private transient String stransdisplaystatus = "";
	
	@Transient
	private transient String sattachmenttype = "";
	
	@Transient
	private transient String screateddate = "";
	
	@Transient
	private transient String sfilesize = "";

	@Override
	public ManufacturerFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ManufacturerFile manufacturerFile = new ManufacturerFile();
		manufacturerFile.setNmanufacturerfilecode(getInteger(arg0, "nmanufacturerfilecode", arg1));
		manufacturerFile.setNmanufcode(getInteger(arg0, "nmanufcode", arg1));
		manufacturerFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		manufacturerFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		manufacturerFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		manufacturerFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		manufacturerFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		manufacturerFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		manufacturerFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		manufacturerFile.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		manufacturerFile.setNstatus(getShort(arg0, "nstatus", arg1));
		manufacturerFile.setSlinkname(getString(arg0, "slinkname", arg1));
		manufacturerFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		manufacturerFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		manufacturerFile.setScreateddate(getString(arg0, "screateddate", arg1));
		manufacturerFile.setSfilename(getString(arg0, "sfilename", arg1));
		manufacturerFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		manufacturerFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		manufacturerFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return manufacturerFile;
	}
}
