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
 * This class is used to map the fields of 'alertrights' table of the Database.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 17-04- 2025
 */

@Entity
@Table(name = "alertrights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AlertRights extends CustomizedResultsetRowMapper<AlertRights> implements Serializable, RowMapper<AlertRights> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nalertrightscode")
	private int nalertrightscode;

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
	public AlertRights mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final AlertRights AlertRights = new AlertRights();

		AlertRights.setNstatus(getShort(arg0, "nstatus", arg1));
		AlertRights.setNsitecode(getShort(arg0, "nsitecode", arg1));
		AlertRights.setNalertrightscode(getInteger(arg0, "nalertrightscode", arg1));
		AlertRights.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		AlertRights.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		AlertRights.setSscreenheader(getString(arg0, "sscreenheader", arg1));
		AlertRights.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		AlertRights.setNcount(getInteger(arg0, "ncount", arg1));
		AlertRights.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return AlertRights;

	}

}
