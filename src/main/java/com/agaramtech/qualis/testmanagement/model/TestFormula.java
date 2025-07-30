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
 * This class is used to map the fields of 'testformula' table of the
 * Database. 
 */
@Entity
@Table(name = "testformula")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestFormula extends CustomizedResultsetRowMapper<TestFormula> implements Serializable,RowMapper<TestFormula> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestformulacode")
	private int ntestformulacode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;
	
	@Column(name = "sformulaname", length = 100)
	private String sformulaname;
	
	@Column(name = "sformulacalculationcode", length = 512)
	private String sformulacalculationcode;
	
	@Column(name = "sformulacalculationdetail", length = 512)
	private String sformulacalculationdetail;
	
	@ColumnDefault("4")
	@Column(name = "nispredefinedformula", nullable = false)
	private short nispredefinedformula = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient short npredefinedformulacode;
	@Transient
	private transient String spredefinedformula;
	@Transient
	private transient String sdescription;

	@Override
	public TestFormula mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final TestFormula objTestFormula = new TestFormula();
		objTestFormula.setNtestformulacode(getInteger(arg0,"ntestformulacode",arg1));
		objTestFormula.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestFormula.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestFormula.setSformulaname(StringEscapeUtils.unescapeJava(getString(arg0,"sformulaname",arg1)));
		objTestFormula.setSformulacalculationcode(StringEscapeUtils.unescapeJava(getString(arg0,"sformulacalculationcode",arg1)));
		objTestFormula.setSformulacalculationdetail(StringEscapeUtils.unescapeJava(getString(arg0,"sformulacalculationdetail",arg1)));
		objTestFormula.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestFormula.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestFormula.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestFormula.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestFormula.setNispredefinedformula(getShort(arg0,"nispredefinedformula",arg1));
		objTestFormula.setNpredefinedformulacode(getShort(arg0,"npredefinedformulacode",arg1));
		objTestFormula.setSpredefinedformula(getString(arg0,"spredefinedformula",arg1));
		objTestFormula.setSdescription(getString(arg0,"sdescription",arg1));

		return objTestFormula;
	}
}
