package com.agaramtech.qualis.testmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * This class is used to map the fields of 'testmethod' table of the
 * Database. 
 */
@Entity
@Table(name = "testmethod")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestMethod extends CustomizedResultsetRowMapper<TestMethod> implements Serializable, RowMapper<TestMethod> {
  
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestmethodcode")
	private int ntestmethodcode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "nmethodcode", nullable = false)
	private int nmethodcode;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus= (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String smethodname;

	@Override
	public TestMethod mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestMethod objTestMethod = new TestMethod();
		objTestMethod.setNtestmethodcode(getInteger(arg0,"ntestmethodcode",arg1));
		objTestMethod.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestMethod.setNmethodcode(getInteger(arg0,"nmethodcode",arg1));
		objTestMethod.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestMethod.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestMethod.setSmethodname(getString(arg0,"smethodname",arg1));
		objTestMethod.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		return objTestMethod;
	}
}
