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
 * This class is used to map the fields of 'testparameternumeric' table of the
 * Database. 
 */

@Entity
@Table(name = "testparameternumeric")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestParameterNumeric extends CustomizedResultsetRowMapper<TestParameterNumeric> implements Serializable, RowMapper<TestParameterNumeric> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestparamnumericcode")
	private int ntestparamnumericcode;
	
	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;
	
	@Column(name = "sminlod", length = 20)
	private String sminlod="";
	
	@Column(name = "smaxlod", length = 20)
	private String smaxlod="";
	
	@Column(name = "sminb", length = 20)
	private String sminb="";
	
	@Column(name = "smina", length = 20)
	private String smina="";
	
	@Column(name = "smaxa", length = 20)
	private String smaxa="";
	
	@Column(name = "smaxb", length = 20)
	private String smaxb="";
	
	@Column(name = "sminloq", length = 20)
	private String sminloq="";
	
	@Column(name = "smaxloq", length = 20)
	private String smaxloq="";
	
	@Column(name = "sdisregard", length = 20)
	private String sdisregard="";
	
	@Column(name = "sresultvalue", length = 20)
	private String sresultvalue="";
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ngradecode", nullable = false)
	private int ngradecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String sgradename;
	
	@Override
	public TestParameterNumeric mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestParameterNumeric objTestParameterNumeric = new TestParameterNumeric();
		objTestParameterNumeric.setNtestparamnumericcode(getInteger(arg0,"ntestparamnumericcode",arg1));
		objTestParameterNumeric.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestParameterNumeric.setSminlod(StringEscapeUtils.unescapeJava(getString(arg0,"sminlod",arg1)));
		objTestParameterNumeric.setSmaxlod(StringEscapeUtils.unescapeJava(getString(arg0,"smaxlod",arg1)));
		objTestParameterNumeric.setSmaxa(StringEscapeUtils.unescapeJava(getString(arg0,"smaxa",arg1)));
		objTestParameterNumeric.setSmaxb(StringEscapeUtils.unescapeJava(getString(arg0,"smaxb",arg1)));
		objTestParameterNumeric.setSmina(StringEscapeUtils.unescapeJava(getString(arg0,"smina",arg1)));
		objTestParameterNumeric.setSminb(StringEscapeUtils.unescapeJava(getString(arg0,"sminb",arg1)));
		objTestParameterNumeric.setSminloq(StringEscapeUtils.unescapeJava(getString(arg0,"sminloq",arg1)));
		objTestParameterNumeric.setSmaxloq(StringEscapeUtils.unescapeJava(getString(arg0,"smaxloq",arg1)));
		objTestParameterNumeric.setSdisregard(StringEscapeUtils.unescapeJava(getString(arg0,"sdisregard",arg1)));
		objTestParameterNumeric.setSresultvalue(StringEscapeUtils.unescapeJava(getString(arg0,"sresultvalue",arg1)));
		objTestParameterNumeric.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestParameterNumeric.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestParameterNumeric.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestParameterNumeric.setSgradename(getString(arg0,"sgradename",arg1));
		objTestParameterNumeric.setNgradecode(getInteger(arg0,"ngradecode",arg1));

		return objTestParameterNumeric;
	}
}
