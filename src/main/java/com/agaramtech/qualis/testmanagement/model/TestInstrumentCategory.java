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
 * This class is used to map the fields of 'testinstrumentcategory' table of the
 * Database. 
 */
@Entity
@Table(name = "testinstrumentcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class TestInstrumentCategory extends CustomizedResultsetRowMapper<TestInstrumentCategory> implements Serializable, RowMapper<TestInstrumentCategory> {
 
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestinstrumentcatcode")
	private int ntestinstrumentcatcode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode;
	
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
	private short nsitecode= (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sinstrumentcatname;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient int nsectioncode;
	@Transient
	private transient int ncalibrationreq;

	@Override
	public TestInstrumentCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestInstrumentCategory objTestInstrumentCategory = new TestInstrumentCategory();
		objTestInstrumentCategory.setNtestinstrumentcatcode(getInteger(arg0,"ntestinstrumentcatcode",arg1));
		objTestInstrumentCategory.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestInstrumentCategory.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		objTestInstrumentCategory.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestInstrumentCategory.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestInstrumentCategory.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objTestInstrumentCategory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestInstrumentCategory.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objTestInstrumentCategory.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objTestInstrumentCategory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestInstrumentCategory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestInstrumentCategory.setNcalibrationreq(getInteger(arg0,"ncalibrationreq",arg1));
		return objTestInstrumentCategory;
	}
	
	

}
