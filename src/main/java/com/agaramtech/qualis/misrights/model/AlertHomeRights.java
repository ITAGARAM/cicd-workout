package com.agaramtech.qualis.misrights.model;

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
 * This class is used to map the fields of 'alerthomerights' table of the
 * Database.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 17-04- 2025
 */

@Entity
@Table(name = "alerthomerights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AlertHomeRights extends CustomizedResultsetRowMapper<AlertHomeRights> implements Serializable, RowMapper<AlertHomeRights> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id

	@Column(name = "nalerthomerightscode")
	private int nalerthomerightscode;

	@Column(name = "nsqlquerycode", nullable = false)
	private int nsqlquerycode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sscreenheader;

	@Transient
	private transient String ssqlquery;

	@Transient
	private transient int ncount;

	@Override
	public AlertHomeRights mapRow(ResultSet arg0, int arg1) throws SQLException {

		final AlertHomeRights AlertHomeRights = new AlertHomeRights();

		AlertHomeRights.setNstatus(getShort(arg0, "nstatus", arg1));
		AlertHomeRights.setNsitecode(getShort(arg0, "nsitecode", arg1));
		AlertHomeRights.setNalerthomerightscode(getInteger(arg0, "nalerthomerightscode", arg1));
		AlertHomeRights.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		AlertHomeRights.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		AlertHomeRights.setSscreenheader(getString(arg0, "sscreenheader", arg1));
		AlertHomeRights.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		AlertHomeRights.setNcount(getInteger(arg0, "ncount", arg1));
		AlertHomeRights.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return AlertHomeRights;

	}

}
