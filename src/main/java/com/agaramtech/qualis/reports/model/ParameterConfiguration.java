package com.agaramtech.qualis.reports.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parameterconfiguration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ParameterConfiguration extends CustomizedResultsetRowMapper<ParameterConfiguration>
		implements Serializable, RowMapper<ParameterConfiguration> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportparameterconfigurationcode")
	private int nreportparameterconfigurationcode;

	@Column(name = "nreportmastercode")
	private int nreportmastercode;

	@Column(name = "nreportdetailcode")
	private int nreportdetailcode;

	@Column(name = "smappedcolumnname")
	private String smappedcolumnname;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String parameterlist;
	@Transient
	private transient String sreportparametername;
	@Transient
	private transient String stablecolumn;
	@Transient
	private transient String stablename;
	@Transient
	private transient String scontrolBasedparameter;
	@Transient
	private transient int ncontrolBasedparameter;
	@Transient
	private transient int nquerybuildertablecode;
	@Transient
	private transient String squerybuildercolumnname;
	@Transient
	private transient String sreportgenerated;
	@Transient
	private transient String sformname;

	public ParameterConfiguration mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ParameterConfiguration parameterconfiguration = new ParameterConfiguration();

		parameterconfiguration.setNreportparameterconfigurationcode(getInteger(arg0, "nreportparameterconfigurationcode", arg1));
		parameterconfiguration.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		parameterconfiguration.setNreportmastercode(getInteger(arg0, "nreportmastercode", arg1));
		parameterconfiguration.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		parameterconfiguration.setNsitecode(getShort(arg0, "nsitecode", arg1));
		parameterconfiguration.setNstatus(getShort(arg0, "nstatus", arg1));
		parameterconfiguration.setSmappedcolumnname(StringEscapeUtils.unescapeJava(getString(arg0, "smappedcolumnname", arg1)));
		parameterconfiguration.setParameterlist(getString(arg0, "parameterlist", arg1));
		parameterconfiguration.setNquerybuildertablecode(getInteger(arg0, "nquerybuildertablecode", arg1));
		parameterconfiguration.setSquerybuildercolumnname(getString(arg0, "squerybuildercolumnname", arg1));
		parameterconfiguration.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		parameterconfiguration.setNsitecode(getShort(arg0, "nsitecode", arg1));
		parameterconfiguration.setNstatus(getShort(arg0, "nstatus", arg1));
		parameterconfiguration.setStablecolumn(getString(arg0, "stablecolumn", arg1));
		parameterconfiguration.setStablename(getString(arg0, "stablename", arg1));
		parameterconfiguration.setScontrolBasedparameter(getString(arg0, "scontrolBasedparameter", arg1));
		parameterconfiguration.setNcontrolBasedparameter(getInteger(arg0, "ncontrolBasedparameter", arg1));
		parameterconfiguration.setSreportgenerated(getString(arg0, "sreportgenerated", arg1));
		parameterconfiguration.setSformname(getString(arg0, "sformname", arg1));

		return parameterconfiguration;
	}

}
