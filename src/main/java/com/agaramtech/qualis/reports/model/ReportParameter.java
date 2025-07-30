package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "reportparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportParameter extends CustomizedResultsetRowMapper<ReportParameter> implements Serializable, RowMapper<ReportParameter> {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreportparametercode")
	private int nreportparametercode;
	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode;
	@Column(name = "sreportparametername", length = 50, nullable = false)
	private String sreportparametername;
	@Column(name = "sdatatype", length = 50, nullable = false)
	private String sdatatype;
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String sdisplaydatatype;

	@Override
	public ReportParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReportParameter reportParameterObj = new ReportParameter();
		reportParameterObj.setNreportparametercode(getInteger(arg0, "nreportparametercode", arg1));
		reportParameterObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		reportParameterObj.setSreportparametername(StringEscapeUtils.unescapeJava(getString(arg0, "sreportparametername", arg1)));
		reportParameterObj.setSdatatype(StringEscapeUtils.unescapeJava(getString(arg0, "sdatatype", arg1)));
		reportParameterObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		reportParameterObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportParameterObj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		reportParameterObj.setSdisplaydatatype(getString(arg0, "sdisplaydatatype", arg1));
		return reportParameterObj;
	}

}
