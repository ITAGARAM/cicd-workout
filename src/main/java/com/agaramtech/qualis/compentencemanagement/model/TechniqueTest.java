package com.agaramtech.qualis.compentencemanagement.model;

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
 * This class is used to map the fields of 'techniquetest' table of the Database.
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "techniquetest")
@Data
public class TechniqueTest extends CustomizedResultsetRowMapper<TechniqueTest> implements Serializable, RowMapper<TechniqueTest> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntechniquetestcode")
	private int ntechniquetestcode;

	@Column(name = "ntechniquecode", nullable = false)
	private int ntechniquecode;

	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient  String sdisplaystatus;
	@Transient
	private transient  String stestname;

	@Override
	public TechniqueTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TechniqueTest objTechniqueTest = new TechniqueTest();
		objTechniqueTest.setNtechniquetestcode(getInteger(arg0, "ntechniquetestcode", arg1));
		objTechniqueTest.setNtechniquecode(getInteger(arg0, "ntechniquecode", arg1));
		objTechniqueTest.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objTechniqueTest.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTechniqueTest.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTechniqueTest.setNstatus(getShort(arg0, "nstatus", arg1));
		objTechniqueTest.setStestname(getString(arg0, "stestname", arg1));
		return objTechniqueTest;
	}
}
