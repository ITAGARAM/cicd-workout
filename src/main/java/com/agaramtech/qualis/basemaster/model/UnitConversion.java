package com.agaramtech.qualis.basemaster.model;

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
 * This class is used to map the fields of 'unit' table of the Database.
 */
@Entity
@Table(name = "unitconversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnitConversion extends CustomizedResultsetRowMapper<UnitConversion> implements Serializable, RowMapper<UnitConversion> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nunitconversioncode")
	private int nunitconversioncode;

	@Column(name = "nsourceunitcode")
	private int nsourceunitcode;

	@Column(name = "ndestinationunitcode")
	private int ndestinationunitcode;

	@Column(name = "noperatorcode", nullable = false)
	private short noperatorcode;

	@Column(name = "nconversionfactor", length = 10, nullable = false)
	private String nconversionfactor;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int nunitcode;
	@Transient
	private transient String sunitname;
	@Transient
	private transient String sunitname1;
	@Transient
	private transient String smodifieddate;
	@Transient
	private transient String soperator;

	@Override
	public UnitConversion mapRow(ResultSet arg0, int arg1) throws SQLException {

		final UnitConversion objUnitConversion = new UnitConversion();

		objUnitConversion.setNunitconversioncode(getInteger(arg0, "nunitconversioncode", arg1));
		objUnitConversion.setNsourceunitcode(getInteger(arg0, "nsourceunitcode", arg1));
		objUnitConversion.setNdestinationunitcode(getInteger(arg0, "ndestinationunitcode", arg1));
		objUnitConversion.setNoperatorcode(getShort(arg0, "noperatorcode", arg1));
		objUnitConversion.setSoperator(getString(arg0, "soperator", arg1));
		objUnitConversion.setNconversionfactor(getString(arg0, "nconversionfactor", arg1));
		objUnitConversion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objUnitConversion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objUnitConversion.setNstatus(getShort(arg0, "nstatus", arg1));
		objUnitConversion.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objUnitConversion.setSunitname(getString(arg0, "sunitname", arg1));
		objUnitConversion.setSunitname1(getString(arg0, "sunitname1", arg1));
		objUnitConversion.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objUnitConversion;
	}

}
