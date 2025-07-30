package com.agaramtech.qualis.submitter.model;

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
@Table(name = "submitter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Submitter extends CustomizedResultsetRowMapper<Submitter> implements Serializable, RowMapper<Submitter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ssubmittercode", length = 50, nullable = false)
	private String ssubmittercode;

	@Column(name = "dcreateddate")
	private Instant dcreateddate;

	@Column(name = "ninstitutioncatcode")
	private int ninstitutioncatcode;

	@Column(name = "ninstitutioncode")
	private int ninstitutioncode;

	@Column(name = "ninstitutionsitecode")
	private int ninstitutionsitecode;

	@Column(name = "ninstitutiondeptcode")
	private int ninstitutiondeptcode;

	@Column(name = "sfirstname", length = 100, nullable = false)
	private String sfirstname;

	@Column(name = "slastname", length = 100, nullable = false)
	private String slastname;

	@Column(name = "ssubmitterid", length = 10, nullable = false)
	private String ssubmitterid;

	@Column(name = "sshortname", length = 20, nullable = false)
	private String sshortname;

	@Column(name = "swardname", length = 100, nullable = false)
	private String swardname;

	@Column(name = "stelephone", length = 20, nullable = false)
	private String stelephone;

	@Column(name = "smobileno", length = 20, nullable = false)
	private String smobileno;

	@Column(name = "sfaxno", length = 20, nullable = false)
	private String sfaxno;

	@Column(name = "semail", length = 100, nullable = false)
	private String semail;

	@Column(name = "sspecialization", length = 100, nullable = false)
	private String sspecialization;

	@Column(name = "sreportrequirement", length = 255, nullable = false)
	private String sreportrequirement;

	@Column(name = "ssampletransport", length = 255, nullable = false)
	private String ssampletransport;

	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@Column(name = "ntzcreateddate")
	private short ntzcreateddate = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "noffsetdcreateddate")
	private int noffsetdcreateddate;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sinstitutioncatname;

	@Transient
	private transient String sinstitutionname;

	@Transient
	private transient String sinstitutionsitename;

	@Transient
	private transient String sinstitutiondeptname;

	@Transient
	private transient String sinstitutiondeptcode;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String ssubmittername;

	@Transient
	private transient String smodifieddate;

	@Override
	public Submitter mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Submitter objSubmitter = new Submitter();
		objSubmitter.setSsubmittercode(StringEscapeUtils.unescapeJava(getString(arg0, "ssubmittercode", arg1)));
		objSubmitter.setNinstitutioncatcode(getInteger(arg0, "ninstitutioncatcode", arg1));
		objSubmitter.setNinstitutioncode(getInteger(arg0, "ninstitutioncode", arg1));
		objSubmitter.setNinstitutionsitecode(getInteger(arg0, "ninstitutionsitecode", arg1));
		objSubmitter.setNinstitutiondeptcode(getInteger(arg0, "ninstitutiondeptcode", arg1));
		objSubmitter.setSsubmittername(getString(arg0, "ssubmittername", arg1));
		objSubmitter.setSsubmitterid(StringEscapeUtils.unescapeJava(getString(arg0, "ssubmitterid", arg1)));
		objSubmitter.setSshortname(StringEscapeUtils.unescapeJava(getString(arg0, "sshortname", arg1)));
		objSubmitter.setSwardname(StringEscapeUtils.unescapeJava(getString(arg0, "swardname", arg1)));
		objSubmitter.setStelephone(StringEscapeUtils.unescapeJava(getString(arg0, "stelephone", arg1)));
		objSubmitter.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0, "smobileno", arg1)));
		objSubmitter.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0, "sfaxno", arg1)));
		objSubmitter.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objSubmitter.setSspecialization(StringEscapeUtils.unescapeJava(getString(arg0, "sspecialization", arg1)));
		objSubmitter.setSreportrequirement(StringEscapeUtils.unescapeJava(getString(arg0, "sreportrequirement", arg1)));
		objSubmitter.setSsampletransport(StringEscapeUtils.unescapeJava(getString(arg0, "ssampletransport", arg1)));
		objSubmitter.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objSubmitter.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objSubmitter.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objSubmitter.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objSubmitter.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSubmitter.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSubmitter.setNstatus(getShort(arg0, "nstatus", arg1));
		objSubmitter.setSinstitutioncatname(getString(arg0, "sinstitutioncatname", arg1));
		objSubmitter.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objSubmitter.setSinstitutionsitename(getString(arg0, "sinstitutionsitename", arg1));
		objSubmitter.setSinstitutiondeptname(getString(arg0, "sinstitutiondeptname", arg1));
		objSubmitter.setSinstitutiondeptcode(getString(arg0, "sinstitutiondeptcode", arg1));
		objSubmitter.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSubmitter.setSfirstname(StringEscapeUtils.unescapeJava(getString(arg0, "sfirstname", arg1)));
		objSubmitter.setSlastname(StringEscapeUtils.unescapeJava(getString(arg0, "slastname", arg1)));
		objSubmitter.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objSubmitter;
	}

}
