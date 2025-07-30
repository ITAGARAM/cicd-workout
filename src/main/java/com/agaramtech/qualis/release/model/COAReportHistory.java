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
@Table(name = "coareporthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class COAReportHistory extends CustomizedResultsetRowMapper<COAReportHistory>
		implements Serializable, RowMapper<COAReportHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncoareporthistorycode", nullable = false)
	private int ncoareporthistorycode;

	@ColumnDefault("-1")
	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nversionno", nullable = false)
	private int nversionno = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename;

	@ColumnDefault("-1")
	@Column(name = "nusercode", nullable = false)
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dgenerateddate", nullable = false)
	private Instant dgenerateddate;

	@ColumnDefault("-1")
	@Column(name = "ntzgenerateddate", nullable = false)
	private short ntzgenerateddate = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdgenerateddate", nullable = false)
	private int noffsetdgenerateddate = Enumeration.TransactionStatus.NON_EMPTY .gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "ntzmodifieddate", nullable = false)
	private short ntzmodifieddate = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetdmodifieddate", nullable = false)
	private int noffsetdmodifieddate = Enumeration.TransactionStatus.NON_EMPTY .gettransactionstatus();

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
	@Transient
	private transient String sreportno;
	@Transient
	private transient String sarno;
	@Transient
	private transient String sreleasedate;
	@Transient
	private transient int nexternalordercode;
	@Transient
	private transient int npreregno;
	@Transient
	private transient String sexternalorderid;

	public COAReportHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final COAReportHistory objCOAReportHistory = new COAReportHistory();

		objCOAReportHistory.setNcoareporthistorycode(getInteger(arg0, "ncoareporthistorycode", arg1));
		objCOAReportHistory.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objCOAReportHistory.setNversionno(getInteger(arg0, "nversionno", arg1));
		objCOAReportHistory.setSsystemfilename(StringEscapeUtils.unescapeJava(getString(arg0, "ssystemfilename", arg1)));
		objCOAReportHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objCOAReportHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objCOAReportHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objCOAReportHistory.setSreportcomments(StringEscapeUtils.unescapeJava(getString(arg0, "sreportcomments", arg1)));
		objCOAReportHistory.setNreportypecode(getShort(arg0, "nreportypecode", arg1));
		objCOAReportHistory.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		objCOAReportHistory.setNoffsetdmodifieddate(getInteger(arg0, "noffsetdmodifieddate", arg1));
		objCOAReportHistory.setNtzmodifieddate(getShort(arg0, "ntzmodifieddate", arg1));
		objCOAReportHistory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCOAReportHistory.setNoffsetdgenerateddate(getInteger(arg0, "noffsetdgenerateddate", arg1));
		objCOAReportHistory.setNtzgenerateddate(getShort(arg0, "ntzgenerateddate", arg1));
		objCOAReportHistory.setDgenerateddate(getInstant(arg0, "dgenerateddate", arg1));
		objCOAReportHistory.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objCOAReportHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCOAReportHistory.setSreportno(getString(arg0, "sreportno", arg1));
		objCOAReportHistory.setSarno(getString(arg0, "sarno", arg1));
		objCOAReportHistory.setSreleasedate(getString(arg0, "sreleasedate", arg1));
		objCOAReportHistory.setSexternalorderid(getString(arg0, "sexternalorderid", arg1));
		objCOAReportHistory.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objCOAReportHistory.setNexternalordercode(getInteger(arg0, "nexternalordercode", arg1));

		return objCOAReportHistory;
	}
}
