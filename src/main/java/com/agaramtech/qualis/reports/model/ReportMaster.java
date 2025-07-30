package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

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
@Table(name = "reportmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportMaster extends CustomizedResultsetRowMapper<ReportMaster> implements Serializable, RowMapper<ReportMaster> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreportcode")
	private int nreportcode;
	@Column(name = "nreporttypecode", nullable = false)
	private int nreporttypecode;
	@Column(name = "nsampletypecode", nullable = false)
	private int nsampletypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nregtypecode", nullable = false)
	private int nregtypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nregsubtypecode", nullable = false)
	private int nregsubtypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "napproveconfversioncode", nullable = false)
	private int napproveconfversioncode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nreportmodulecode", nullable = false)
	private int nreportmodulecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "sreportname", length = 100, nullable = false)
	private String sreportname;
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@Column(name = "ncoareporttypecode", nullable = false)
	private int ncoareporttypecode;
	@Column(name = "nreportdecisiontypecode", nullable = false)
	private int nreportdecisiontypecode;
	@Column(name = "ncertificatetypecode", nullable = false)
	private int ncertificatetypecode;
	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "ncontrolcode", nullable = false)
	private int ncontrolcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nreporttemplatecode", nullable = false)
	private int nreporttemplatecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sactivestatus;
	@Transient
	private transient String sreporttypename;
	@Transient
	private transient String sregtypename;
	@Transient
	private transient String sregsubtypename;
	@Transient
	private transient String smenuname;
	@Transient
	private transient String sreportmodulename;
	@Transient
	private transient String sreportdisplayname;
	@Transient
	private transient String smoduledisplayname;
	@Transient
	private transient String sbatchdisplayname;
	@Transient
	private transient int isneedregtype;
	@Transient
	private transient String sdecisiontypename;
	@Transient
	private transient String scoareporttypename;
	@Transient
	private transient String scertificatetype;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String ssectionname;
	@Transient
	private transient String scontrolids;
	@Transient
	private transient int nformcode;
	@Transient
	private transient String scolorhexcode;
	@Transient
	private transient String sapproveversionname;
	@Transient
	private transient Map<String, Object> jsondata;
	@Transient
	private transient String ssampletypename;
	@Transient
	private transient String sfilterreporttypecode;
	@Transient
	private transient String sreporttemplatename;
	@Transient
	private transient short isneedsection;
	@Transient
	private transient String sreportformatdetail;

	

	@Override
	public ReportMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReportMaster reportMasterObj = new ReportMaster();
		reportMasterObj.setNreportcode(getInteger(arg0, "nreportcode", arg1));
		reportMasterObj.setNreporttypecode(getInteger(arg0, "nreporttypecode", arg1));
		reportMasterObj.setNregtypecode(getInteger(arg0, "nregtypecode", arg1));
		reportMasterObj.setNregsubtypecode(getInteger(arg0, "nregsubtypecode", arg1));
		reportMasterObj.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		reportMasterObj.setNreportmodulecode(getInteger(arg0, "nreportmodulecode", arg1));
		reportMasterObj.setSreportname(StringEscapeUtils.unescapeJava(getString(arg0, "sreportname", arg1)));
		reportMasterObj.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		reportMasterObj.setNcoareporttypecode(getInteger(arg0, "ncoareporttypecode", arg1));
		reportMasterObj.setNreportdecisiontypecode(getInteger(arg0, "nreportdecisiontypecode", arg1));
		reportMasterObj.setNcertificatetypecode(getInteger(arg0, "ncertificatetypecode", arg1));
		reportMasterObj.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		reportMasterObj.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		reportMasterObj.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		reportMasterObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		reportMasterObj.setSactivestatus(getString(arg0, "sactivestatus", arg1));
		reportMasterObj.setSreporttypename(getString(arg0, "sreporttypename", arg1));
		reportMasterObj.setSregtypename(getString(arg0, "sregtypename", arg1));
		reportMasterObj.setSregsubtypename(getString(arg0, "sregsubtypename", arg1));
		reportMasterObj.setSmenuname(getString(arg0, "smenuname", arg1));
		reportMasterObj.setSreportmodulename(getString(arg0, "sreportmodulename", arg1));
		reportMasterObj.setSreportdisplayname(getString(arg0, "sreportdisplayname", arg1));
		reportMasterObj.setSmoduledisplayname(getString(arg0, "smoduledisplayname", arg1));
		reportMasterObj.setSbatchdisplayname(getString(arg0, "sbatchdisplayname", arg1));
		reportMasterObj.setIsneedregtype(getInteger(arg0, "isneedregtype", arg1));
		reportMasterObj.setSdecisiontypename(getString(arg0, "sdecisiontypename", arg1));
		reportMasterObj.setScoareporttypename(getString(arg0, "scoareporttypename", arg1));
		reportMasterObj.setScertificatetype(getString(arg0, "scertificatetype", arg1));
		reportMasterObj.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		reportMasterObj.setSsectionname(getString(arg0, "ssectionname", arg1));
		reportMasterObj.setScontrolids(getString(arg0, "scontrolids", arg1));
		reportMasterObj.setNformcode(getInteger(arg0, "nformcode", arg1));
		reportMasterObj.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		reportMasterObj.setNapproveconfversioncode(getInteger(arg0, "napproveconfversioncode", arg1));
		reportMasterObj.setSapproveversionname(getString(arg0, "sapproveversionname", arg1));
		reportMasterObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportMasterObj.setNsampletypecode(getInteger(arg0, "nsampletypecode", arg1));
		reportMasterObj.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		reportMasterObj.setSsampletypename(getString(arg0, "ssampletypename", arg1));
		reportMasterObj.setSfilterreporttypecode(getString(arg0, "sfilterreporttypecode", arg1));
		reportMasterObj.setNreporttemplatecode(getInteger(arg0, "nreporttemplatecode", arg1));
		reportMasterObj.setSreporttemplatename(getString(arg0, "sreporttemplatename", arg1));
		reportMasterObj.setIsneedsection(getShort(arg0, "isneedsection", arg1));
		reportMasterObj.setSreportformatdetail(getString(arg0, "sreportformatdetail", arg1));

		return reportMasterObj;
	}

	
}
