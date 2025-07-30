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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subreportdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubReportDetails extends CustomizedResultsetRowMapper<SubReportDetails>
		implements Serializable, RowMapper<SubReportDetails> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsubreportdetailcode")
	private int nsubreportdetailcode;
	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode;
	@Column(name = "nreportcode", nullable = false)
	private int nreportcode;
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;
	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename;
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	
	@Override
	public SubReportDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SubReportDetails reportDetailsObj = new SubReportDetails();
		reportDetailsObj.setNsubreportdetailcode(getInteger(arg0, "nsubreportdetailcode", arg1));
		reportDetailsObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		reportDetailsObj.setNreportcode(getInteger(arg0, "nreportcode", arg1));
		reportDetailsObj.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		reportDetailsObj.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		reportDetailsObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		reportDetailsObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportDetailsObj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return reportDetailsObj;
	}

}
