package com.agaramtech.qualis.reports.model;

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

@Entity
@Table(name = "controlbasedreportvalidation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ControlbasedReportvalidation extends CustomizedResultsetRowMapper<ControlbasedReportvalidation>
		implements Serializable, RowMapper<ControlbasedReportvalidation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportvalidationcode")
	private int nreportvalidationcode;

	@Column(name = "nreportdetailcode")
	private int nreportdetailcode;

	@Column(name = "nformcode")
	private int nformcode;

	@Column(name = "ncontrolcode")
	private int ncontrolcode;

	@Column(name = "ntranscode")
	private int ntranscode;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String stransactionstatus;
	@Transient
	private transient String sreportgenerated;
	@Transient
	private transient String sformname;

	@Override
	public ControlbasedReportvalidation mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ControlbasedReportvalidation controlbasedreportvalidation = new ControlbasedReportvalidation();

		controlbasedreportvalidation.setNreportvalidationcode(getInteger(arg0, "nreportvalidationcode", arg1));
		controlbasedreportvalidation.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		controlbasedreportvalidation.setNformcode(getInteger(arg0, "nformcode", arg1));
		controlbasedreportvalidation.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		controlbasedreportvalidation.setNtranscode(getInteger(arg0, "ntranscode", arg1));
		controlbasedreportvalidation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		controlbasedreportvalidation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		controlbasedreportvalidation.setNstatus(getShort(arg0, "nstatus", arg1));
		controlbasedreportvalidation.setStransactionstatus(getString(arg0, "stransactionstatus", arg1));
		controlbasedreportvalidation.setSreportgenerated(getString(arg0, "sreportgenerated", arg1));
		controlbasedreportvalidation.setSformname(getString(arg0, "sformname", arg1));
		return controlbasedreportvalidation;
	}

}
