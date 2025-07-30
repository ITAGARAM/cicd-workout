package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
@Table(name = "reportparameteraction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportParameterAction extends CustomizedResultsetRowMapper<ReportParameterAction>
		implements Serializable, RowMapper<ReportParameterAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreportparameteractioncode")
	private int nreportparameteractioncode;
	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode;
	@Column(name = "nparentreportdesigncode", nullable = false)
	private int nparentreportdesigncode;
	@Column(name = "nreportdesigncode", nullable = false)
	private int nreportdesigncode;
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sparentparametername;
	@Transient
	private transient String schildparametername;
	@Transient
	private transient String schilddisplayname;
	@Transient
	private transient String ssqlquery;

	@Override
	public ReportParameterAction mapRow(ResultSet arg0, int arg1) throws SQLException {

		ReportParameterAction mappingObj = new ReportParameterAction();
		mappingObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		mappingObj.setNreportdesigncode(getInteger(arg0, "nreportdesigncode", arg1));
		mappingObj.setNparentreportdesigncode(getInteger(arg0, "nparentreportdesigncode", arg1));
		mappingObj.setNreportparameteractioncode(getInteger(arg0, "nreportparameteractioncode", arg1));
		mappingObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		mappingObj.setSchildparametername(getString(arg0, "schildparametername", arg1));
		mappingObj.setSparentparametername(getString(arg0, "sparentparametername", arg1));
		mappingObj.setSchilddisplayname(getString(arg0, "schilddisplayname", arg1));
		mappingObj.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		mappingObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		mappingObj.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return mappingObj;
	}

}
