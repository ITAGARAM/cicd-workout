package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//@Entity
//@Table(name = "certificatetype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CertificateType extends CustomizedResultsetRowMapper<CertificateType> implements Serializable, RowMapper<CertificateType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncertificatetypecode")
	private int ncertificatetypecode;
	@Column(name = "scertificatetype", length = 20, nullable = false)
	private String scertificatetype;
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@Column(name = "nedqm", nullable = false)
	private int nedqm = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
	@Column(name = "naccredited", nullable = false)
	private int naccredited = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();
	@Column(name = "ncertificatereporttypecode", nullable = false)
	private int ncertificatereporttypecode;
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sedqmCertificate;
	@Transient
	private transient String saccreditedCertificate;
	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String sreportname;
	@Transient
	private transient String spreviewreportname;
	@Transient
	private transient String sbatchdisplayname;
	@Transient
	private transient int ncertificatetypeversionno;
	@Transient
	private transient int nreportdetailcode;
	@Transient
	private transient int npreviewreportdetailcode;

	@Override
	public CertificateType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final CertificateType certificatetype = new CertificateType();
		certificatetype.setNcertificatetypecode(getInteger(arg0, "ncertificatetypecode", arg1));
		certificatetype.setScertificatetype(StringEscapeUtils.unescapeJava(getString(arg0, "scertificatetype", arg1)));
		certificatetype.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		certificatetype.setNedqm(getInteger(arg0, "nedqm", arg1));
		certificatetype.setNaccredited(getInteger(arg0, "naccredited", arg1));
		certificatetype.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		certificatetype.setNcertificatereporttypecode(getInteger(arg0, "ncertificatereporttypecode", arg1));
		certificatetype.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		certificatetype.setNstatus(getInteger(arg0, "nstatus", arg1));
		certificatetype.setSedqmCertificate(getString(arg0, "sedqmCertificate", arg1));
		certificatetype.setSaccreditedCertificate(getString(arg0, "saccreditedCertificate", arg1));
		certificatetype.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		certificatetype.setSreportname(getString(arg0, "sreportname", arg1));
		certificatetype.setSpreviewreportname(getString(arg0, "spreviewreportname", arg1));
		certificatetype.setSbatchdisplayname(getString(arg0, "sbatchdisplayname", arg1));
		certificatetype.setNcertificatetypeversionno(getInteger(arg0, "ncertificatetypeversionno", arg1));
		certificatetype.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		certificatetype.setNpreviewreportdetailcode(getInteger(arg0, "npreviewreportdetailcode", arg1));
		return certificatetype;
	}
}
