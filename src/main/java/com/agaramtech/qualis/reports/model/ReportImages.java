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
@Table(name = "reportimages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportImages extends CustomizedResultsetRowMapper<ReportImages> implements Serializable, RowMapper<ReportImages> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportimagecode")
	private int nreportimagecode;
	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode;
	@Column(name = "nreportcode", nullable = false)
	private int nreportcode;
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;
	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename;
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	

	public ReportImages mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ReportImages reportImagesObj = new ReportImages();
		reportImagesObj.setNreportimagecode(getInteger(arg0, "nreportimagecode", arg1));
		reportImagesObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		reportImagesObj.setNreportcode(getInteger(arg0, "nreportcode", arg1));
		reportImagesObj.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		reportImagesObj.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		reportImagesObj.setNstatus(getShort(arg0, "nstatus", arg1));
		reportImagesObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportImagesObj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return reportImagesObj;
	}

}
