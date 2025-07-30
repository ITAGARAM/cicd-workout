package com.agaramtech.qualis.testgroup.model;

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
 * This class is used to map the fields of 'testgroupspecsampletype' table of the Database.
 */
@Entity
@Table(name = "testgroupspecsampletype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupSpecSampleType extends CustomizedResultsetRowMapper<TestGroupSpecSampleType> implements Serializable, RowMapper<TestGroupSpecSampleType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nspecsampletypecode")
	private int nspecsampletypecode;
	
	@Column(name = "nallottedspeccode", nullable = false)
	private int nallottedspeccode;
	
	@Column(name = "ncomponentcode", nullable = false)
	private int ncomponentcode;
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String scomponentname;

	@Override
	public TestGroupSpecSampleType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final TestGroupSpecSampleType objTestGroupSpecSampleType = new TestGroupSpecSampleType();
		
		objTestGroupSpecSampleType.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objTestGroupSpecSampleType.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestGroupSpecSampleType.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objTestGroupSpecSampleType.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupSpecSampleType.setScomponentname(getString(arg0,"scomponentname",arg1));
		objTestGroupSpecSampleType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupSpecSampleType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objTestGroupSpecSampleType;
	}
	
}
