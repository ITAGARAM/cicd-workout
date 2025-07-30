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
@Table(name = "testgrouptestnumericparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestNumericParameter extends CustomizedResultsetRowMapper<TestGroupTestNumericParameter> implements Serializable,RowMapper<TestGroupTestNumericParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestnumericcode")
	private int ntestgrouptestnumericcode;
	
	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "sminlod", length = 20, nullable = false)
	private String sminlod;
	
	@Column(name = "smaxlod", length = 20, nullable = false)
	private String smaxlod;
	
	@Column(name = "sminb", length = 20, nullable = false)
	private String sminb;
	
	@Column(name = "smina", length = 20, nullable = false)
	private String smina;
	
	@Column(name = "smaxa", length = 20, nullable = false)
	private String smaxa;
	
	@Column(name = "smaxb", length = 20, nullable = false)
	private String smaxb;
	
	@Column(name = "sminloq", length = 20, nullable = false)
	private String sminloq;
	
	@Column(name = "smaxloq", length = 20, nullable = false)
	private String smaxloq;
	
	@Column(name = "sdisregard", length = 20, nullable = false)
	private String sdisregard;
	
	@Column(name = "sresultvalue", length = 20, nullable = false)
	private String sresultvalue;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate",nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "ngradecode", nullable = false)
	private int ngradecode  = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String sgradename;

	@Override
	public TestGroupTestNumericParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
	
		TestGroupTestNumericParameter objTestGroupTestNP = new TestGroupTestNumericParameter();
		
		objTestGroupTestNP.setNtestgrouptestnumericcode(getInteger(arg0,"ntestgrouptestnumericcode",arg1));
		objTestGroupTestNP.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objTestGroupTestNP.setSminlod(StringEscapeUtils.unescapeJava(getString(arg0,"sminlod",arg1)));
		objTestGroupTestNP.setSmaxloq(StringEscapeUtils.unescapeJava(getString(arg0,"smaxloq",arg1)));
		objTestGroupTestNP.setSmina(StringEscapeUtils.unescapeJava(getString(arg0,"smina",arg1)));
		objTestGroupTestNP.setSminb(StringEscapeUtils.unescapeJava(getString(arg0,"sminb",arg1)));
		objTestGroupTestNP.setSmaxb(StringEscapeUtils.unescapeJava(getString(arg0,"smaxb",arg1)));
		objTestGroupTestNP.setSmaxa(StringEscapeUtils.unescapeJava(getString(arg0,"smaxa",arg1)));
		objTestGroupTestNP.setSminloq(StringEscapeUtils.unescapeJava(getString(arg0,"sminloq",arg1)));
		objTestGroupTestNP.setSmaxlod(StringEscapeUtils.unescapeJava(getString(arg0,"smaxlod",arg1)));
		objTestGroupTestNP.setSdisregard(StringEscapeUtils.unescapeJava(getString(arg0,"sdisregard",arg1)));
		objTestGroupTestNP.setSresultvalue(StringEscapeUtils.unescapeJava(getString(arg0,"sresultvalue",arg1)));
		objTestGroupTestNP.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupTestNP.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupTestNP.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestGroupTestNP.setNgradecode(getInteger(arg0,"ngradecode",arg1));
		objTestGroupTestNP.setSgradename(getString(arg0,"sgradename",arg1));
		return objTestGroupTestNP;
	}

}
