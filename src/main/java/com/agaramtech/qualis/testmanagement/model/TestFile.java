package com.agaramtech.qualis.testmanagement.model;

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
 * This class is used to map the fields of 'testfile' table of the
 * Database. 
 */

@Entity
@Table(name = "testfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestFile extends CustomizedResultsetRowMapper<TestFile> implements Serializable,RowMapper<TestFile> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestfilecode")
	private int ntestfilecode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;
	
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;
	
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	
	@Column(name="ntzcreateddate" )  
	private short ntzcreateddate;
	
	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;
	
	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String slinkname;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sattachmenttype;
	
	@Transient
	private transient String screateddate;
	
	@Transient
	private transient String sfilesize;
	
	@Transient
	private transient int nqualislite;

	
	
	
	
	@Override
	public TestFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestFile objTestFile = new TestFile();
		objTestFile.setNtestfilecode(getInteger(arg0,"ntestfilecode",arg1));
		objTestFile.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objTestFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objTestFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objTestFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTestFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objTestFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objTestFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objTestFile.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objTestFile.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objTestFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objTestFile.setSfilename(getString(arg0,"sfilename",arg1));
		objTestFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objTestFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objTestFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestFile.setNqualislite(getInteger(arg0,"nqualislite",arg1));

		return objTestFile;
	}
	
 
}
