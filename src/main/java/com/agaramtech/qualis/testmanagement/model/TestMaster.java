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
 * This class is used to map the fields of 'testmaster' table of the
 * Database. 
 */

@Entity
@Data
@Table(name = "testmaster")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)


public class TestMaster extends CustomizedResultsetRowMapper<TestMaster> implements Serializable, RowMapper<TestMaster> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestcode")
	private int ntestcode;
	
	@Column(name = "ntestcategorycode", nullable = false)
	private int ntestcategorycode;
	
	@ColumnDefault("-1")
	@Column(name = "nchecklistversioncode", nullable = false)
	private int nchecklistversioncode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("71")
	@Column(name = "naccredited", nullable = false)
	private short naccredited = (short)Enumeration.TransactionStatus.ACCREDIT.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "stestname", length = 100, nullable = false)	
	private String stestname;
	
	@Column(name = "stestsynonym", length = 100)
	private String stestsynonym="";
	
	@Column(name = "sshortname", length = 20)
	private String sshortname="";
	
	@Column(name = "sdescription", length = 2000)
	private String sdescription="";
	
	@Column(name = "ncost", nullable = false)
	private Double ncost;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "stestplatform", length = 100, nullable = false)	
	private String stestplatform;
	
	@Column(name = "ntat", nullable = false)
	private int ntat;
	
	@Column(name = "ntatperiodcode", nullable = false)
	private int ntatperiodcode;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("4")
	@Column(name = "ntrainingneed", nullable = false)
	private short ntrainingneed = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ninterfacetypecode", nullable = false)
	private short ninterfacetypecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient	
	private transient String schecklistname;
	@Transient
	private transient String stransactionstatus;
	@Transient
	private transient String saccredited;
	@Transient
	private transient String stestcategoryname;
	@Transient
	private transient String sparametername;
	@Transient
	private transient String statperiodname;
	@Transient
	private transient String sdeltaunitname;
	@Transient
	private transient String sinterfacetypename;
	@Transient
	private transient String strainingneed;
	@Transient
	private transient String smodifieddate;
	@Transient
	private transient int ncomponentcode;
	@Transient
	private transient int nproductcatcode;
	@Transient
	private transient int nproductcode;
	@Transient
	private transient int ncontainertypecode;
	@Transient
	private transient String scontainertype;
	@Transient
	private transient int nallottedspeccode;
	@Transient
	private transient int ntestgrouptestcode;
	@Transient
	private transient int ntemplatemanipulationcode;
	@Transient
	private transient int ntestpackagetestcode;
	@Transient
	private transient String stestpackagename;
	@Transient
	private transient int ntestpackagecode;
	@Transient
	private transient int nclinicaltyperequired;
	@Transient
	private transient int nresultaccuracycode;

	@Override
	
	public TestMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestMaster objTestMaster = new TestMaster();
		objTestMaster.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestMaster.setNtestcategorycode(getInteger(arg0,"ntestcategorycode",arg1));
		objTestMaster.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objTestMaster.setNaccredited(getShort(arg0,"naccredited",arg1));
		objTestMaster.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objTestMaster.setStestname(StringEscapeUtils.unescapeJava(getString(arg0,"stestname",arg1)));
		objTestMaster.setStestsynonym(StringEscapeUtils.unescapeJava(getString(arg0,"stestsynonym",arg1)));
		objTestMaster.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTestMaster.setNcost(getDouble(arg0,"ncost",arg1));
		objTestMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestMaster.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestMaster.setSaccredited(getString(arg0,"saccredited",arg1));
		objTestMaster.setSchecklistname(getString(arg0,"schecklistname",arg1));
		objTestMaster.setSshortname(StringEscapeUtils.unescapeJava(getString(arg0,"sshortname",arg1)));
		objTestMaster.setStransactionstatus(getString(arg0,"stransactionstatus",arg1));
		objTestMaster.setStestcategoryname(getString(arg0,"stestcategoryname",arg1));
		objTestMaster.setSparametername(getString(arg0,"sparametername",arg1));
		objTestMaster.setStestplatform(StringEscapeUtils.unescapeJava(getString(arg0,"stestplatform",arg1)));
		objTestMaster.setNtat(getInteger(arg0,"ntat",arg1));
		objTestMaster.setNtatperiodcode(getInteger(arg0,"ntatperiodcode",arg1));
		objTestMaster.setStatperiodname(getString(arg0,"statperiodname",arg1));
		objTestMaster.setSdeltaunitname(getString(arg0,"sdeltaunitname",arg1));
		objTestMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestMaster.setNtrainingneed(getShort(arg0,"ntrainingneed",arg1));
		objTestMaster.setNinterfacetypecode(getShort(arg0,"ninterfacetypecode",arg1));
		objTestMaster.setSinterfacetypename(getString(arg0,"sinterfacetypename",arg1));
		objTestMaster.setStrainingneed(getString(arg0,"strainingneed",arg1));
		objTestMaster.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objTestMaster.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objTestMaster.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objTestMaster.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objTestMaster.setNcontainertypecode(getInteger(arg0,"ncontainertypecode",arg1));
		objTestMaster.setScontainertype(getString(arg0,"scontainertype",arg1));
		objTestMaster.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestMaster.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objTestMaster.setNtemplatemanipulationcode(getInteger(arg0,"ntemplatemanipulationcode",arg1));
		objTestMaster.setNtestpackagetestcode(getInteger(arg0,"ntestpackagetestcode",arg1));
		objTestMaster.setStestpackagename(getString(arg0,"stestpackagename",arg1));
		objTestMaster.setNtestpackagecode(getInteger(arg0,"ntestpackagecode",arg1));
		objTestMaster.setNclinicaltyperequired(getInteger(arg0,"nclinicaltyperequired",arg1));
		objTestMaster.setNresultaccuracycode(getInteger(arg0,"nresultaccuracycode",arg1));

		return objTestMaster;
	}
	

}
