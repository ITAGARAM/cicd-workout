package com.agaramtech.qualis.emailmanagement.model;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "emailalerttransaction")
public class EmailAlertTransaction extends CustomizedResultsetRowMapper<EmailAlertTransaction>
		implements Serializable, RowMapper<EmailAlertTransaction> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nrunningno")
	private int nrunningno;

	@Column(name = "ntransactionno", nullable = false)
	private int ntransactionno;

	@Column(name = "nemailconfigcode", nullable = false)
	private int nemailconfigcode;

	@Column(name = "dcreateddate", nullable = false)
	private Instant dcreateddate;

	@Column(name = "semail", length = 200, nullable = false)
	private String semail;

	@Column(name = "sreason", length = 50)
	private String sreason = "";

	@Column(name = "ssubject", length = 100)
	private String ssubject = "";

	@Column(name = "stemplatebody", length = 5000)
	private String stemplatebody = "";

	@Column(name = "ssystemid", length = 50, nullable = false)
	private String ssystemid;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "ncontrolcode")
	private int ncontrolcode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String susername;

	@Transient
	private transient String scontrolname;

	@Transient
	private transient String screateddate;

	@Override
	public EmailAlertTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailAlertTransaction emailAlertTransaction = new EmailAlertTransaction();
		emailAlertTransaction.setNrunningno(getInteger(arg0, "nrunningno", arg1));
		emailAlertTransaction.setNtransactionno(getInteger(arg0, "ntransactionno", arg1));
		emailAlertTransaction.setNemailconfigcode(getInteger(arg0, "nemailconfigcode", arg1));
		emailAlertTransaction.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		emailAlertTransaction.setScreateddate(getString(arg0, "screateddate", arg1));
		emailAlertTransaction.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		emailAlertTransaction.setSreason(StringEscapeUtils.unescapeJava(getString(arg0, "sreason", arg1)));
		emailAlertTransaction.setSsubject(StringEscapeUtils.unescapeJava(getString(arg0, "ssubject", arg1)));
		emailAlertTransaction.setStemplatebody(StringEscapeUtils.unescapeJava(getString(arg0, "stemplatebody", arg1)));
		emailAlertTransaction.setSsystemid(StringEscapeUtils.unescapeJava(getString(arg0, "ssystemid", arg1)));
		emailAlertTransaction.setSusername(getString(arg0, "susername", arg1));
		emailAlertTransaction.setScontrolname(getString(arg0, "scontrolname", arg1));
		emailAlertTransaction.setNusercode(getInteger(arg0, "nusercode", arg1));
		emailAlertTransaction.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		emailAlertTransaction.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailAlertTransaction.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return emailAlertTransaction;
	}
}
