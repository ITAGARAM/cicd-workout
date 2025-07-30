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
@Table(name = "regeneratereporthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegenerateReportHistory extends CustomizedResultsetRowMapper<RegenerateReportHistory>
		implements Serializable, RowMapper<RegenerateReportHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregeneratereporthistorycode")
	private int nregeneratereporthistorycode;

	@ColumnDefault("-1")
	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

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

	public RegenerateReportHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		RegenerateReportHistory objRegenerateReportHistory = new RegenerateReportHistory();

		objRegenerateReportHistory
				.setNregeneratereporthistorycode(getInteger(arg0, "nregeneratereporthistorycode", arg1));
		objRegenerateReportHistory.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objRegenerateReportHistory.setNversionno(getInteger(arg0, "nversionno", arg1));
		objRegenerateReportHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objRegenerateReportHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objRegenerateReportHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objRegenerateReportHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objRegenerateReportHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegenerateReportHistory.setSreportcomments(StringEscapeUtils.unescapeJava(getString(arg0, "sreportcomments", arg1)));
		objRegenerateReportHistory.setNreportypecode(getShort(arg0, "nreportypecode", arg1));
		objRegenerateReportHistory.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		objRegenerateReportHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objRegenerateReportHistory.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objRegenerateReportHistory.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objRegenerateReportHistory.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objRegenerateReportHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objRegenerateReportHistory;
	}
}
