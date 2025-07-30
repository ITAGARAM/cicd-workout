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
@Table(name = "testgrouptestclinicalspec")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestClinicalSpec extends CustomizedResultsetRowMapper<TestGroupTestClinicalSpec> implements Serializable, RowMapper<TestGroupTestClinicalSpec> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestclinicspeccode")
	private int ntestgrouptestclinicspeccode;
	
	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "ngendercode")
	private int ngendercode;
	
	@Column(name = "nfromage")
	private int nfromage;
	
	@Column(name = "ntoage")
	private int ntoage;

	@Column(name = "shigha", length = 20, nullable = false)
	private String shigha;
	
	@Column(name = "shighb", length = 20, nullable = false)
	private String shighb;
	
	@Column(name = "slowa", length = 20, nullable = false)
	private String slowa;
	
	@Column(name = "slowb", length = 20, nullable = false)
	private String slowb;
	
	@Column(name = "sminlod", length = 20, nullable = false)
	private String sminlod;
	
	@Column(name = "smaxlod", length = 20, nullable = false)
	private String smaxlod;
	
	@Column(name = "sminloq", length = 20, nullable = false)
	private String sminloq;
	
	@Column(name = "smaxloq", length = 20, nullable = false)
	private String smaxloq;
	
	@Column(name = "sdisregard", length = 20, nullable = false)
	private String sdisregard;
	
	@Column(name = "sresultvalue", length = 20, nullable = false)
	private String sresultvalue;
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ntzmodifieddate", nullable=false)
	private short ntzmodifieddate;
	
	@Column(name = "noffsetdmodifieddate", nullable=false)
	private int noffsetdmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ngradecode", nullable = false)
	private int ngradecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nfromageperiod", nullable = false)
	private short nfromageperiod;
	
	@Column(name = "ntoageperiod", nullable = false)
	private short ntoageperiod;
	
	@Transient
	private transient String sgendername;
	@Transient
	private transient String sgradename;
	@Transient
	private transient String sfromageperiod;
	@Transient
	private transient String stoageperiod;


	@Override
	public TestGroupTestClinicalSpec mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestClinicalSpec objTGTPP = new TestGroupTestClinicalSpec();
		objTGTPP.setNtestgrouptestclinicspeccode(getInteger(arg0, "ntestgrouptestclinicspeccode", arg1));
		objTGTPP.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objTGTPP.setNfromage(getInteger(arg0, "nfromage", arg1));
		objTGTPP.setNgendercode(getInteger(arg0, "ngendercode", arg1));
		objTGTPP.setNtoage(getInteger(arg0, "ntoage", arg1));
		objTGTPP.setShigha(StringEscapeUtils.unescapeJava(getString(arg0,"shigha",arg1)));
		objTGTPP.setShighb(StringEscapeUtils.unescapeJava(getString(arg0,"shighb",arg1)));
		objTGTPP.setSlowa(StringEscapeUtils.unescapeJava(getString(arg0,"slowa",arg1)));
		objTGTPP.setSlowb(StringEscapeUtils.unescapeJava(getString(arg0,"slowb",arg1)));
		objTGTPP.setSminlod(StringEscapeUtils.unescapeJava(getString(arg0,"sminlod",arg1)));
		objTGTPP.setSmaxlod(StringEscapeUtils.unescapeJava(getString(arg0,"smaxlod",arg1)));
		objTGTPP.setSminloq(StringEscapeUtils.unescapeJava(getString(arg0,"sminloq",arg1)));
		objTGTPP.setSmaxloq(StringEscapeUtils.unescapeJava(getString(arg0,"smaxloq",arg1)));
		objTGTPP.setSdisregard(StringEscapeUtils.unescapeJava(getString(arg0,"sdisregard",arg1)));
		objTGTPP.setNstatus(getShort(arg0, "nstatus", arg1));
		objTGTPP.setNoffsetdmodifieddate(getInteger(arg0,"noffsetdmodifieddate",arg1));
		objTGTPP.setNtzmodifieddate(getShort(arg0,"ntzmodifieddate",arg1));
		objTGTPP.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTGTPP.setSgendername(getString(arg0,"sgendername",arg1));
		objTGTPP.setSresultvalue(StringEscapeUtils.unescapeJava(getString(arg0,"sresultvalue",arg1)));
		objTGTPP.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTGTPP.setNgradecode(getInteger(arg0, "ngradecode", arg1));
		objTGTPP.setSgradename(getString(arg0,"sgradename",arg1));
		objTGTPP.setNfromageperiod(getShort(arg0,"nfromageperiod",arg1));
		objTGTPP.setNtoageperiod(getShort(arg0, "ntoageperiod", arg1));
		objTGTPP.setSfromageperiod(getString(arg0,"sfromageperiod",arg1));
		objTGTPP.setStoageperiod(getString(arg0,"stoageperiod",arg1));
		
		return objTGTPP;
	}

}
