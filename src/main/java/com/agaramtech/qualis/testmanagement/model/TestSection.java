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
 * This class is used to map the fields of 'testsection' table of the
 * Database. 
 */

@Entity
@Table(name = "testsection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestSection extends CustomizedResultsetRowMapper<TestSection> implements Serializable, RowMapper<TestSection> {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestsectioncode")
	private int ntestsectioncode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode;
	
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
	private transient String stransdisplaystatus;
	@Transient
	private transient String ssectionname;

	@Override
	public TestSection mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final TestSection objTestSection = new TestSection();
		objTestSection.setNtestsectioncode(getInteger(arg0,"ntestsectioncode",arg1));
		objTestSection.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestSection.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objTestSection.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestSection.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestSection.setSsectionname(getString(arg0,"ssectionname",arg1));
		objTestSection.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestSection.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestSection.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objTestSection;
	}
	
}
