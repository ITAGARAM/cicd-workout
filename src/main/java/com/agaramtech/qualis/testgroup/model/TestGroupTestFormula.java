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
@Table(name="testgrouptestformula")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestFormula extends CustomizedResultsetRowMapper<TestGroupTestFormula> implements Serializable,RowMapper<TestGroupTestFormula> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestgrouptestformulacode")
	private int ntestgrouptestformulacode;
	
	@Column(name = "ntestgrouptestcode", nullable = false)
	private int ntestgrouptestcode;
	
	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "ntestformulacode", nullable = false)
	private int ntestformulacode;
	
	@Column(name = "sformulacalculationcode", length = 512 )
	private String sformulacalculationcode="";
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "sformulacalculationdetail", length = 512 )
	private String sformulacalculationdetail="";
	
	@ColumnDefault("4")
	@Column(name = "ntransactionstatus", nullable = false)	
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String sformulaname;


	@Override
	public TestGroupTestFormula mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestFormula objTestGroupTestFormula = new TestGroupTestFormula();
		objTestGroupTestFormula.setNtestgrouptestformulacode(getInteger(arg0, "ntestgrouptestformulacode", arg1));
		objTestGroupTestFormula.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objTestGroupTestFormula.setNtestgrouptestcode(getInteger(arg0, "ntestgrouptestcode", arg1));
		objTestGroupTestFormula.setNtestformulacode(getInteger(arg0, "ntestformulacode", arg1));
		objTestGroupTestFormula.setSformulacalculationcode(StringEscapeUtils.unescapeJava(getString(arg0, "sformulacalculationcode", arg1)));
		objTestGroupTestFormula.setSformulacalculationdetail(StringEscapeUtils.unescapeJava(getString(arg0, "sformulacalculationdetail", arg1)));
		objTestGroupTestFormula.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objTestGroupTestFormula.setNstatus(getShort(arg0, "nstatus", arg1));
		objTestGroupTestFormula.setSformulaname(getString(arg0, "sformulaname", arg1));
		objTestGroupTestFormula.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupTestFormula.setNsitecode(getShort(arg0, "nsitecode", arg1));
		
		return objTestGroupTestFormula;
	}
	
}
