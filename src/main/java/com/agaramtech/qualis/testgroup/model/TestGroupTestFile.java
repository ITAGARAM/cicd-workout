package com.agaramtech.qualis.testgroup.model;

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
@Table(name = "testgrouptestfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestFile extends CustomizedResultsetRowMapper<TestGroupTestFile> implements Serializable,RowMapper<TestGroupTestFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgroupfilecode")
	private int ntestgroupfilecode;
	
	@Column(name = "ntestgrouptestcode", nullable = false)
	private int ntestgrouptestcode;
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nattachmenttypecode", nullable = false)	
	private short nattachmenttypecode;
	
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename="";
	
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;
	
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	
	@Column(name="ntzcreateddate" )  
	private short ntzcreateddate;
	
	@Column(name="noffsetdcreateddate")
	private int noffsetdcreateddate;
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();



	@Override
	public TestGroupTestFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestFile objTestGroupTestFile = new TestGroupTestFile();
		objTestGroupTestFile.setNtestgroupfilecode(getInteger(arg0,"ntestgroupfilecode",arg1));
		objTestGroupTestFile.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objTestGroupTestFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objTestGroupTestFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objTestGroupTestFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objTestGroupTestFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTestGroupTestFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objTestGroupTestFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objTestGroupTestFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objTestGroupTestFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupTestFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objTestGroupTestFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objTestGroupTestFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupTestFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objTestGroupTestFile;
	}
}
