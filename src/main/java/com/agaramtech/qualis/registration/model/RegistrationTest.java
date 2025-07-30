package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
 * This class is used to map the fields of 'registrationtest' table of the Database.
 */
@Entity
@Table(name = "registrationtest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationTest  extends CustomizedResultsetRowMapper<RegistrationTest> implements Serializable,RowMapper<RegistrationTest> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntransactiontestcode")
	private int ntransactiontestcode;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntestgrouptestcode", nullable = false)
	private int ntestgrouptestcode; 
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;

	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode;	

	@Column(name = "nmethodcode")
	private int nmethodcode;

	@Column(name = "ninstrumentcatcode")
	private int ninstrumentcatcode;

	@Column(name = "ntestrepeatno", nullable = false)
	private short ntestrepeatno;

	@Column(name = "ntestretestno", nullable = false)
	private short ntestretestno;	
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		
	@Transient
	private transient int nparenttestgrouptestcode; 
	
	@Transient
	private transient int ntestgrouprulesenginecode;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient int ntransactionstatus;
	
	@Transient
	private transient String ssamplename;
	
	@Transient
	private transient String scomponentname;
	
	@Transient
	private transient String ssectionname;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String ssampleqty;
	
	@Transient
	private transient Date djobaccepteddate;
	
	@Transient
	private transient Date dallotteddate;
	
	@Transient
	private transient Date dcompleteddate;
	
	@Transient
	private transient Date dteststarteddate;
	
	@Transient
	private transient int njoballocationcode;
	
	@Transient
	private transient int ntestgrouptestparametercode;
	
	@Transient
	private transient int napprovalversioncode;
	
	@Transient
	private transient String ssourcename;
	
	@Transient
	private transient String smethodname;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String sinstrumentcatname;
	
	@Transient
	private transient String nretestrepeat;
	
	@Transient
	private transient String sactiondisplaystatus;
	
	@Transient
	private transient String dtransactiondate;
	
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient String smanuflotno;
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient int nchecklistcode;
	
	@Transient
	private transient short nrepeatcountno ;
	
	@Transient
	private transient float  ncost;
	
	@Transient
	private transient int ntesthistorycode;
	
	@Transient
	private transient String samplearno;
	
	@Transient
	private transient int nspecsampletypecode;
	
	@Transient
	private transient int nchecklistversioncode;
	
	@Transient
	private transient int ncomponentcode;
	
	@Transient
	private transient int nallottedspeccode;
	
	@Transient
	private transient String sparametersynonym;
	
	@Transient
	private transient String sresult;
	
	@Transient
	private transient String sgradename;
	
	@Transient
	private transient String repretest;
	
	@Transient
	private transient int limsprimarycode;
	
	@Transient
	private transient String sbatcharno;
	
	@Transient
	private transient String sampleID;
	
	@Transient
	private transient int nprojectmastercode;
	
	@Transient	
	private transient String sremarks;
	
	@Transient
	private transient String ssampleid;
	
	@Transient
	private transient String sshipmenttracking;
	
	@Transient
	private transient Date doutsourcedate;
	
	@Transient
	private transient int nexternalordertestcode;

	@Override
	public RegistrationTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationTest objRegistrationTest = new RegistrationTest();
		
		objRegistrationTest.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objRegistrationTest.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objRegistrationTest.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationTest.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objRegistrationTest.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objRegistrationTest.setNtestrepeatno(getShort(arg0,"ntestrepeatno",arg1));
		objRegistrationTest.setNtestretestno(getShort(arg0,"ntestretestno",arg1));
		objRegistrationTest.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objRegistrationTest.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		objRegistrationTest.setNmethodcode(getInteger(arg0,"nmethodcode",arg1));
		objRegistrationTest.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationTest.setNchecklistcode(getInteger(arg0,"nchecklistcode",arg1));
		objRegistrationTest.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objRegistrationTest.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistrationTest.setSsamplename(getString(arg0,"ssamplename",arg1));
		objRegistrationTest.setSsectionname(getString(arg0,"ssectionname",arg1));
		objRegistrationTest.setScomponentname(getString(arg0,"scomponentname",arg1));
		objRegistrationTest.setSsampleqty(getString(arg0,"ssampleqty",arg1));
		objRegistrationTest.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objRegistrationTest.setDjobaccepteddate(getDate(arg0,"djobaccepteddate",arg1));
		objRegistrationTest.setDallotteddate(getDate(arg0,"dallotteddate",arg1));
		objRegistrationTest.setDcompleteddate(getDate(arg0,"dcompleteddate",arg1));
		objRegistrationTest.setDteststarteddate(getDate(arg0,"dteststarteddate",arg1));
		objRegistrationTest.setNjoballocationcode(getInteger(arg0,"njoballocationcode",arg1));
		objRegistrationTest.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objRegistrationTest.setNapprovalversioncode(getInteger(arg0,"napprovalversioncode",arg1));
		objRegistrationTest.setSsourcename(getString(arg0,"ssourcename",arg1));
		objRegistrationTest.setSmethodname(getString(arg0,"smethodname",arg1));
		objRegistrationTest.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		objRegistrationTest.setSarno(getString(arg0,"sarno",arg1));
		objRegistrationTest.setScolorhexcode(getString(arg0,"scolorhexcode",arg1));
		objRegistrationTest.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objRegistrationTest.setNretestrepeat(getString(arg0,"nretestrepeat",arg1));
		objRegistrationTest.setDtransactiondate(getString(arg0,"dtransactiondate",arg1));
		objRegistrationTest.setSmanuflotno(getString(arg0,"smanuflotno",arg1));
		objRegistrationTest.setStestname(getString(arg0,"stestname",arg1));
		objRegistrationTest.setSactiondisplaystatus(getString(arg0,"sactiondisplaystatus",arg1));
		objRegistrationTest.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objRegistrationTest.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationTest.setNrepeatcountno(getShort(arg0,"nrepeatcountno",arg1));
		objRegistrationTest.setNcost(getFloat(arg0,"ncost",arg1));
		objRegistrationTest.setNtesthistorycode(getInteger(arg0,"ntesthistorycode",arg1));
		objRegistrationTest.setSamplearno(getString(arg0,"samplearno",arg1));
		objRegistrationTest.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objRegistrationTest.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objRegistrationTest.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objRegistrationTest.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objRegistrationTest.setNtestgrouprulesenginecode(getInteger(arg0,"ntestgrouprulesenginecode",arg1));
		objRegistrationTest.setNparenttestgrouptestcode(getInteger(arg0,"nparenttestgrouptestcode",arg1));
		objRegistrationTest.setSparametersynonym(getString(arg0,"sparametersynonym",arg1));
		objRegistrationTest.setSresult(getString(arg0,"sresult",arg1));
		objRegistrationTest.setSgradename(getString(arg0,"sgradename",arg1));
		objRegistrationTest.setRepretest(getString(arg0,"repretest",arg1));
		objRegistrationTest.setLimsprimarycode(getInteger(arg0,"limsprimarycode",arg1));
		objRegistrationTest.setSbatcharno(getString(arg0,"sbatcharno",arg1));
		objRegistrationTest.setSampleID(getString(arg0,"sampleID",arg1));
		objRegistrationTest.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objRegistrationTest.setNexternalordertestcode(getInteger(arg0,"nexternalordertestcode",arg1));
		objRegistrationTest.setSremarks(getString(arg0,"sremarks",arg1));
		objRegistrationTest.setSsampleid(getString(arg0,"ssampleid",arg1));
		objRegistrationTest.setSshipmenttracking(getString(arg0,"sshipmenttracking",arg1));
		objRegistrationTest.setDoutsourcedate(getDate(arg0,"doutsourcedate",arg1));
		
		return objRegistrationTest;
	}

}
