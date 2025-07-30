package com.agaramtech.qualis.testmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
 * This class is used to map the fields of 'testpackagetest' table of the Database.
 */
@Entity
@Table(name = "testpackagetest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class TestPackageTest extends CustomizedResultsetRowMapper<TestPackageTest> implements Serializable ,RowMapper<TestPackageTest>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestpackagetestcode")
	private int ntestpackagetestcode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "ntestpackagecode", nullable = false)
	private int ntestpackagecode;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;	

	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String stestpackagename;

	@Override
	public TestPackageTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestPackageTest objTestPakage = new TestPackageTest();
		objTestPakage.setNtestpackagetestcode(getInteger(arg0,"ntestpackagetestcode",arg1));
		objTestPakage.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestPakage.setNtestpackagecode(getInteger(arg0,"ntestpackagecode",arg1));
		objTestPakage.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestPakage.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestPakage.setStestpackagename(getString(arg0,"stestpackagename",arg1));
		objTestPakage.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestPakage.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objTestPakage;
	}


}
