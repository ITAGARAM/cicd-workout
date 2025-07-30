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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="testgroupspecfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupSpecFile extends CustomizedResultsetRowMapper<TestGroupSpecFile> implements Serializable,RowMapper<TestGroupSpecFile>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nspecfilecode")
	private int nspecfilecode;
	
	@Column(name = "nallottedspeccode", nullable = false)
	private int nallottedspeccode;
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;
	
	@Column(name = "sfilename", length = 100 , nullable = false)
	private String sfilename;
	
	@Column(name = "sdescription", length = 255 )
	private String sdescription="";
	
	@Column(name="nfilesize", nullable = false)
	private int nfilesize;
	
	@Column(name="dcreateddate")
	private Instant dcreateddate;
	
	@Column(name="ntzcreateddate" )  
	private short ntzcreateddate;
	
	@Column(name="noffsetdcreateddate")
	private int noffsetdcreateddate;
	
	@Column(name = "ssystemfilename", length = 100 )
	private String ssystemfilename="";
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String slinkname;
	@Transient
	private transient String sattachmenttype;
	@Transient
	private transient String screateddate;
	@Transient
	private transient String stypename;
	@Transient
	private transient String sfilesize;
	
	
	@Override
	public TestGroupSpecFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupSpecFile objTestGroupSpecFile = new TestGroupSpecFile();
		objTestGroupSpecFile.setNspecfilecode(getInteger(arg0,"nspecfilecode",arg1));
		objTestGroupSpecFile.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestGroupSpecFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objTestGroupSpecFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objTestGroupSpecFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objTestGroupSpecFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTestGroupSpecFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objTestGroupSpecFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objTestGroupSpecFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objTestGroupSpecFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupSpecFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objTestGroupSpecFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objTestGroupSpecFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objTestGroupSpecFile.setStypename(getString(arg0,"stypename",arg1));
		objTestGroupSpecFile.setSfilesize(getString(arg0,"sfilesize",arg1));
		objTestGroupSpecFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objTestGroupSpecFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objTestGroupSpecFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupSpecFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objTestGroupSpecFile;
	}

}
