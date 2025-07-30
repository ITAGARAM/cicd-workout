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
 * This class is used to map the fields of 'testparameter' table of the
 * Database. 
 */

@Entity
@Table(name = "testparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestParameter extends CustomizedResultsetRowMapper<TestParameter> implements Serializable, RowMapper<TestParameter> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestparametercode")
	private int ntestparametercode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;
	
	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;
	
	@Column(name = "sparametername", length = 100, nullable = false)
	private String sparametername;
	
	@Column(name = "sparametersynonym", length = 100, nullable = false)
	private String sparametersynonym;
	
	@Column(name = "nroundingdigits", nullable = false)	
	private short nroundingdigits;
	
	@ColumnDefault("4")
	@Column(name = "nisadhocparameter", nullable = false)
	private short nisadhocparameter= (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nisvisible", nullable = false)
	private short nisvisible = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ndeltachecklimitcode", nullable = false)
	private float ndeltachecklimitcode;
	
	@Column(name = "ndeltacheckframe", nullable = false)
	private int ndeltacheckframe;
	
	@Column(name = "ndeltaunitcode", nullable = false)
	private int ndeltaunitcode;
	
	@Column(name = "ndeltacheck", nullable = false)
	private short ndeltacheck;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndestinationunitcode", nullable = false)
	private int ndestinationunitcode;

	@Column(name = "nconversionfactor",length = 10, nullable = false)
	private String nconversionfactor;
	
	@Column(name = "noperatorcode", nullable = false)
	private short noperatorcode;
	
	@ColumnDefault("-1")
	@Column(name = "nresultaccuracycode", nullable = false)
	private int nresultaccuracycode= (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String sunitname;
	
	@Transient
	private transient TestPredefinedParameter objPredefinedParameter;
	
	@Transient
	private transient  String stestname;
	
	@Transient
	private transient String sformulacalculationcode;
	
	@Transient
	private transient int isformula;
	
	@Transient
	private transient String sdeltaunitname;
	
	@Transient
	private transient String stestparametersynonym;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sunitname1;
	
	@Transient
	private transient String soperator;
	
	@Transient
	private transient String sresultaccuracyname;


	@Override
	public TestParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestParameter objTestParameter = new TestParameter();
		objTestParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestParameter.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestParameter.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objTestParameter.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objTestParameter.setSparametername(StringEscapeUtils.unescapeJava(getString(arg0,"sparametername",arg1)));
		objTestParameter.setSparametersynonym(StringEscapeUtils.unescapeJava(getString(arg0,"sparametersynonym",arg1)));
		objTestParameter.setNroundingdigits(getShort(arg0,"nroundingdigits",arg1));
		objTestParameter.setNisadhocparameter(getShort(arg0,"nisadhocparameter",arg1));
		objTestParameter.setNisvisible(getShort(arg0,"nisvisible",arg1));
		objTestParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestParameter.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objTestParameter.setSunitname(getString(arg0,"sunitname",arg1));
		objTestParameter.setStestname(getString(arg0,"stestname",arg1));
		objTestParameter.setSformulacalculationcode(getString(arg0,"sformulacalculationcode",arg1));
		objTestParameter.setIsformula(getInteger(arg0,"isformula",arg1));
		objTestParameter.setNdeltachecklimitcode(getFloat(arg0,"ndeltachecklimitcode",arg1));
		objTestParameter.setNdeltacheckframe(getInteger(arg0,"ndeltacheckframe",arg1));
		objTestParameter.setNdeltaunitcode(getInteger(arg0,"ndeltaunitcode",arg1));
		objTestParameter.setNdeltacheck(getShort(arg0,"ndeltacheck",arg1));
		objTestParameter.setSdeltaunitname(getString(arg0,"sdeltaunitname",arg1));
		objTestParameter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestParameter.setStestparametersynonym(getString(arg0,"stestparametersynonym",arg1));
		objTestParameter.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestParameter.setNdestinationunitcode(getInteger(arg0,"ndestinationunitcode",arg1));
		objTestParameter.setNconversionfactor(StringEscapeUtils.unescapeJava(getString(arg0,"nconversionfactor",arg1)));
		objTestParameter.setSunitname1(getString(arg0,"sunitname1",arg1));
		objTestParameter.setNoperatorcode(getShort(arg0,"noperatorcode",arg1));
		objTestParameter.setSoperator(getString(arg0,"soperator",arg1));
		objTestParameter.setNresultaccuracycode(getInteger(arg0,"nresultaccuracycode",arg1));
		objTestParameter.setSresultaccuracyname(getString(arg0,"sresultaccuracyname",arg1));

		
		return objTestParameter;
	}
}
