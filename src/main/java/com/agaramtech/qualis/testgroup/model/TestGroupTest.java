package com.agaramtech.qualis.testgroup.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
 * This class is used to map the fields of 'testgrouptest' table of the Database.
 */
@Entity
@Table(name = "testgrouptest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestGroupTest extends CustomizedResultsetRowMapper<TestGroupTest> implements Serializable,RowMapper<TestGroupTest> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestcode")
	private int ntestgrouptestcode;
	
	@Column(name = "nspecsampletypecode", nullable = false)
	private int nspecsampletypecode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode;
	
	@Column(name = "nmethodcode", nullable = false)
	private int nmethodcode;
	
	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ncontainertypecode", nullable = false)
	private int ncontainertypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ntestpackagecode", nullable = false)
	private int ntestpackagecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "stestsynonym", length = 255, nullable = false)
	private String stestsynonym;
	
	@ColumnDefault("1")
	@Column(name = "nrepeatcountno", nullable = false)
	private short nrepeatcountno = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
	@Column(name = "ncost", nullable = false)
	private Double ncost;
	
	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter = (short)Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nisadhoctest", nullable = false)
	private short nisadhoctest = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nisvisible", nullable = false)
	private short nisvisible = (short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@Column(name="dmodifieddate",nullable = false) 
	private Instant dmodifieddate;		
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient Map<String, Object> jsondata;
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient String ssectionname;
	
	@Transient
	private transient String smethodname;
	
	@Transient
	private transient String slabname;
	
	@Transient
	private transient String sinstrumentcatname;
	
	@Transient
	private transient String ssourcename;
	
	@Transient
	private transient int slno;
	
	@Transient
	private transient String slinkname;
	
	@Transient
	private transient String sfilename;
	
	@Transient
	private transient String sstoragelocationname;
	
	@Transient
	private transient String sstorageconditionname;
	
	@Transient
	private transient int nparametercount;
	
	@Transient
	private transient int nlinkcode;
	
	@Transient
	private transient int ntestgroupfilecode;
	
	@Transient
	private String steststatus;
	
	@Transient
	private transient int ntestparametercode;
		
//	@Transient
//	private Double  ncost;
	
	@Transient
	private transient String scontainertype;
	
	@Transient
	private transient int ntestpackagetestcode;
	
	@Transient
	private transient String stestpackagename;
	
	@Transient
	private transient String sparametersynonym;
	
	@Transient
	private transient int nparametertypecode;
	
	@Transient
	private transient int ntestgrouptestparametercode;
	
	@Transient
	private transient int nallottedspeccode;
	
	@Transient
	private transient int nneedsample;
	
	@Transient
	private transient int nexternalordertestcode;
	
	@Transient
	private transient int npkAtestparametercode;
	

	@Override
	public TestGroupTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final TestGroupTest objTestGroupTest = new TestGroupTest();
	
		objTestGroupTest.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objTestGroupTest.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objTestGroupTest.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestGroupTest.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objTestGroupTest.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		objTestGroupTest.setNmethodcode(getInteger(arg0,"nmethodcode",arg1));
		objTestGroupTest.setStestsynonym(StringEscapeUtils.unescapeJava(getString(arg0,"stestsynonym",arg1)));
		objTestGroupTest.setNcost(getDouble(arg0,"ncost",arg1));
		objTestGroupTest.setNsorter(getShort(arg0,"nsorter",arg1));
		objTestGroupTest.setNisadhoctest(getShort(arg0,"nisadhoctest",arg1));
		objTestGroupTest.setNisvisible(getShort(arg0,"nisvisible",arg1));
		objTestGroupTest.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupTest.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestGroupTest.setStestname(getString(arg0,"stestname",arg1));
		objTestGroupTest.setSmethodname(getString(arg0,"smethodname",arg1));
		objTestGroupTest.setSsourcename(getString(arg0,"ssourcename",arg1));
		objTestGroupTest.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objTestGroupTest.setSlabname(getString(arg0,"slabname",arg1));
		objTestGroupTest.setSsectionname(getString(arg0,"ssectionname",arg1));
		objTestGroupTest.setSlno(getInteger(arg0,"slno",arg1));
		objTestGroupTest.setSsectionname(getString(arg0,"ssectionname",arg1));
		objTestGroupTest.setSfilename(getString(arg0,"sfilename",arg1));
		objTestGroupTest.setSlinkname(getString(arg0,"slinkname",arg1));
		objTestGroupTest.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objTestGroupTest.setSmethodname(getString(arg0,"smethodname",arg1));
		objTestGroupTest.setSsourcename(getString(arg0,"ssourcename",arg1));
		objTestGroupTest.setSstorageconditionname(getString(arg0,"sstorageconditionname",arg1));
		objTestGroupTest.setSstoragelocationname(getString(arg0,"sstoragelocationname",arg1));
		objTestGroupTest.setNparametercount(getInteger(arg0,"nparametercount",arg1));
		objTestGroupTest.setNlinkcode(getInteger(arg0,"nlinkcode",arg1));
		objTestGroupTest.setNtestgroupfilecode(getInteger(arg0,"ntestgroupfilecode",arg1));
		objTestGroupTest.setSteststatus(getString(arg0,"steststatus",arg1));
		objTestGroupTest.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objTestGroupTest.setNrepeatcountno(getShort(arg0,"nrepeatcountno",arg1));
		objTestGroupTest.setNcontainertypecode(getInteger(arg0,"ncontainertypecode",arg1));
		objTestGroupTest.setScontainertype(getString(arg0,"scontainertype",arg1));
		objTestGroupTest.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupTest.setNtestpackagetestcode(getInteger(arg0,"ntestpackagetestcode",arg1));
		objTestGroupTest.setStestpackagename(getString(arg0,"stestpackagename",arg1));
		objTestGroupTest.setNtestpackagecode(getInteger(arg0,"ntestpackagecode",arg1));
		objTestGroupTest.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestGroupTest.setSparametersynonym(getString(arg0,"sparametersynonym",arg1));
		objTestGroupTest.setNparametertypecode(getInteger(arg0,"nparametertypecode",arg1));
		objTestGroupTest.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objTestGroupTest.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestGroupTest.setNneedsample(getInteger(arg0,"nneedsample",arg1));
		objTestGroupTest.setNexternalordertestcode(getInteger(arg0,"nexternalordertestcode",arg1));
		objTestGroupTest.setNpkAtestparametercode(getInteger(arg0,"npkAtestparametercode",arg1));
		
		return objTestGroupTest;
	}

}
