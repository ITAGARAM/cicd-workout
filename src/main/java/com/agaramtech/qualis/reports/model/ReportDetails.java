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
@Table(name = "reportdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportDetails extends CustomizedResultsetRowMapper<ReportDetails> implements Serializable, RowMapper<ReportDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportdetailcode")
	private int nreportdetailcode;

	@Column(name = "nreportcode")
	private int nreportcode;

	@Column(name = "sfilename", length = 150)
	private String sfilename="";

	@Column(name = "ssystemfilename", length = 150)
	private String ssystemfilename="";

	@Column(name = "nversionno")
	private int nversionno = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntransactionstatus")
	private int ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@Column(name = "nisplsqlquery")
	private int nisplsqlquery = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nstatus")
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "sreportformatdetail", nullable = false)
	private String sreportformatdetail;

	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String splsqlquery;
	@Transient
	private transient String sreportname;
	@Transient
	private transient String sversionno;
	@Transient
	private transient String scolorhexcode;
	@Transient
	private transient String sfilterreporttypecode;

	

	@Override
	public ReportDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReportDetails reportDetailsObj = new ReportDetails();
		reportDetailsObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		reportDetailsObj.setNreportcode(getInteger(arg0, "nreportcode", arg1));
		reportDetailsObj.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		reportDetailsObj.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		reportDetailsObj.setNversionno(getInteger(arg0, "nversionno", arg1));
		reportDetailsObj.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		reportDetailsObj.setNisplsqlquery(getInteger(arg0, "nisplsqlquery", arg1));
		reportDetailsObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		reportDetailsObj.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		reportDetailsObj.setSplsqlquery(getString(arg0, "splsqlquery", arg1));
		reportDetailsObj.setSreportname(getString(arg0, "sreportname", arg1));
		reportDetailsObj.setSversionno(getString(arg0, "sversionno", arg1));
		reportDetailsObj.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		reportDetailsObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportDetailsObj.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		reportDetailsObj.setSfilterreporttypecode(getString(arg0, "sfilterreporttypecode", arg1));
		reportDetailsObj.setSreportformatdetail(StringEscapeUtils.unescapeJava(getString(arg0, "sreportformatdetail", arg1)));

		return reportDetailsObj;
	}

}
