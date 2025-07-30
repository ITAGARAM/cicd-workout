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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'operators' table of the Database.
 */
@Entity
@Table(name = "operators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Operators extends CustomizedResultsetRowMapper<Operators> implements Serializable, RowMapper<Operators> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "noperatorcode")
	private short noperatorcode;

	@Column(name = "soperator", length = 150, nullable = false)
	private String soperator;

	@Column(name = "soperatordesc", length = 150, nullable = false)
	private String soperatordesc;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nisunitconversion", nullable = false)
	private short nisunitconversion = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public Operators mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Operators objOperators = new Operators();
		objOperators.setNoperatorcode(getShort(arg0, "noperatorcode", arg1));
		objOperators.setSoperator(StringEscapeUtils.unescapeJava(getString(arg0, "soperator", arg1)));
		objOperators.setSoperatordesc(StringEscapeUtils.unescapeJava(getString(arg0, "soperatordesc", arg1)));
		objOperators.setNstatus(getShort(arg0, "nstatus", arg1));
		objOperators.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objOperators.setNisunitconversion(getShort(arg0, "nisunitconversion", arg1));
		objOperators.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objOperators;
	}
}
