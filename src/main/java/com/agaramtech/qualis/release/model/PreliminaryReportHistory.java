
package com.agaramtech.qualis.release.model;

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
@Table(name = "preliminaryreporthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PreliminaryReportHistory extends CustomizedResultsetRowMapper<PreliminaryReportHistory>
		implements Serializable, RowMapper<PreliminaryReportHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npreliminaryreporthistorycode", nullable = false)
	private int npreliminaryreporthistorycode;

	@ColumnDefault("-1")
	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode;

	@ColumnDefault("-1")
	@Column(name = "nversionno", nullable = false)
	private int nversionno = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nusercode", nullable = false)
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "sreportcomments", length = 255)
	private String sreportcomments;

	@ColumnDefault("-1")
	@Column(name = "nreportypecode", nullable = false)
	private short nreportypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient int ntransactionstatus;

	public PreliminaryReportHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		PreliminaryReportHistory objPreliminaryReportHistory = new PreliminaryReportHistory();

		objPreliminaryReportHistory
				.setNpreliminaryreporthistorycode(getInteger(arg0, "npreliminaryreporthistorycode", arg1));
		objPreliminaryReportHistory.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objPreliminaryReportHistory.setNversionno(getInteger(arg0, "nversionno", arg1));
		objPreliminaryReportHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objPreliminaryReportHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objPreliminaryReportHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objPreliminaryReportHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objPreliminaryReportHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objPreliminaryReportHistory.setSreportcomments(StringEscapeUtils.unescapeJava(getString(arg0, "sreportcomments", arg1)));
		objPreliminaryReportHistory.setNreportypecode(getShort(arg0, "nreportypecode", arg1));
		objPreliminaryReportHistory.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		objPreliminaryReportHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objPreliminaryReportHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objPreliminaryReportHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objPreliminaryReportHistory.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objPreliminaryReportHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objPreliminaryReportHistory.setSsystemfilename(StringEscapeUtils.unescapeJava(getString(arg0, "ssystemfilename", arg1)));

		return objPreliminaryReportHistory;
	}
}
