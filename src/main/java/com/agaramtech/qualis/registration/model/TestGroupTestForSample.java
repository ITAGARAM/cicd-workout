package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestForSample extends CustomizedResultsetRowMapper<TestGroupTestForSample>
		implements Serializable, RowMapper<TestGroupTestForSample> {

	private static final long serialVersionUID = 1L;

	private int ntestgrouptestcode;
	private int nspecsampletypecode;
	private int ntestcode;
	private int nsectioncode;
	private int nmethodcode;
	private int ninstrumentcatcode;
	private int ntestpackagetestcode;
	private int ntestpackagecode;
	private String stestsynonym;

	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter =  (short) Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nisadhoctest", nullable = false)
	private short nisadhoctest =  (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nisvisible", nullable = false)
	private short nisvisible =  (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nrepeatcountno", nullable = false)
	private int nrepeatcountno =  (int) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =  (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient Map<String, Object> jsondata;
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient String ssectionname;
	
	@Transient
	private  transient String smethodname;
	
	@Transient
	private transient String slabname;
	
	@Transient
	private transient String sinstrumentcatname;
	
	@Transient
	private transient String ssourcename;
	
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
	private transient String steststatus;
	
	@Transient
	private transient String stestpackagename;
	
	@Transient
	private transient float ncost;

	@Override
	public TestGroupTestForSample mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		TestGroupTestForSample objTestGroupTest = new TestGroupTestForSample();
		objTestGroupTest.setNtestgrouptestcode(getInteger(arg0, "ntestgrouptestcode", arg1));
		objTestGroupTest.setNspecsampletypecode(getInteger(arg0, "nspecsampletypecode", arg1));
		objTestGroupTest.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objTestGroupTest.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objTestGroupTest.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objTestGroupTest.setNmethodcode(getInteger(arg0, "nmethodcode", arg1));
		objTestGroupTest.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objTestGroupTest.setNcost(getFloat(arg0, "ncost", arg1));
		objTestGroupTest.setNsorter(getShort(arg0, "nsorter", arg1));
		objTestGroupTest.setNisadhoctest(getShort(arg0, "nisadhoctest", arg1));
		objTestGroupTest.setNisvisible(getShort(arg0, "nisvisible", arg1));
		objTestGroupTest.setNstatus(getShort(arg0, "nstatus", arg1));
		objTestGroupTest.setStestname(getString(arg0, "stestname", arg1));
		objTestGroupTest.setSmethodname(getString(arg0, "smethodname", arg1));
		objTestGroupTest.setSsourcename(getString(arg0, "ssourcename", arg1));
		objTestGroupTest.setSinstrumentcatname(getString(arg0, "sinstrumentcatname", arg1));
		objTestGroupTest.setSlabname(getString(arg0, "slabname", arg1));
		objTestGroupTest.setSsectionname(getString(arg0, "ssectionname", arg1));
		objTestGroupTest.setSsectionname(getString(arg0, "ssectionname", arg1));
		objTestGroupTest.setSfilename(getString(arg0, "sfilename", arg1));
		objTestGroupTest.setSlinkname(getString(arg0, "slinkname", arg1));
		objTestGroupTest.setSinstrumentcatname(getString(arg0, "sinstrumentcatname", arg1));
		objTestGroupTest.setSmethodname(getString(arg0, "smethodname", arg1));
		objTestGroupTest.setSsourcename(getString(arg0, "ssourcename", arg1));
		objTestGroupTest.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		objTestGroupTest.setSstoragelocationname(getString(arg0, "sstoragelocationname", arg1));
		objTestGroupTest.setNparametercount(getInteger(arg0, "nparametercount", arg1));
		objTestGroupTest.setNlinkcode(getInteger(arg0, "nlinkcode", arg1));
		objTestGroupTest.setNtestgroupfilecode(getInteger(arg0, "ntestgroupfilecode", arg1));
		objTestGroupTest.setSteststatus(getString(arg0, "steststatus", arg1));
		objTestGroupTest.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objTestGroupTest.setStestpackagename(getString(arg0, "stestpackagename", arg1));
		objTestGroupTest.setNtestpackagecode(getInteger(arg0, "ntestpackagecode", arg1));
		objTestGroupTest.setNtestpackagetestcode(getInteger(arg0, "ntestpackagetestcode", arg1));
		objTestGroupTest.setNrepeatcountno(getInteger(arg0, "nrepeatcountno", arg1));
		
		return objTestGroupTest;
	}

}
