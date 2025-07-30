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

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "emailalerttranshistory")
public class EmailAlertTransHistory extends CustomizedResultsetRowMapper<EmailAlertTransHistory>
		implements Serializable, RowMapper<EmailAlertTransHistory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nemailhistorycode")
	private int nemailhistorycode;

	@Column(name = "nrunningno", nullable = false)
	private int nrunningno;

	@Column(name = "ntransstatus", nullable = false)
	private int ntransstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@Column(name = "dtransdate", nullable = false)
	private Instant dtransdate;

	@Column(name = "nmailitemid", nullable = false)
	private int nmailitemid = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "smailsentby", length = 10)
	private String smailsentby = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode", nullable = false)
	private int ntransdatetimezonecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "noffsetdtransdate", nullable = false)
	private int noffsetdtransdate;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient String ssubject;

	@Transient
	private transient String srecipientusers;

	@Transient
	private transient String stemplatebody;

	@Transient
	private transient String ssentstatus;

	@Transient
	private transient String sscreenname;

	@Transient
	private transient String dsentdate;

	@Transient
	private transient String semail;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String stransdate;

	@Transient
	private transient String smailstatus;

	@Transient
	private transient String screateddate;

	@Transient
	private transient String sreason;

	@Transient
	private transient String susername;

	@Transient
	private transient String scontrolname;

	@Transient
	private transient String ssystemid;

	@Override
	public EmailAlertTransHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailAlertTransHistory emailAlertTransactionHistory = new EmailAlertTransHistory();
		emailAlertTransactionHistory.setNemailhistorycode(getInteger(arg0, "nemailhistorycode", arg1));
		emailAlertTransactionHistory.setNrunningno(getInteger(arg0, "nrunningno", arg1));
		emailAlertTransactionHistory.setNtransstatus(getInteger(arg0, "ntransstatus", arg1));
		emailAlertTransactionHistory.setDtransdate(getInstant(arg0, "dtransdate", arg1));
		emailAlertTransactionHistory.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailAlertTransactionHistory.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		emailAlertTransactionHistory.setSsubject(getString(arg0, "ssubject", arg1));
		emailAlertTransactionHistory.setSrecipientusers(getString(arg0, "srecipientusers", arg1));
		emailAlertTransactionHistory.setStemplatebody(getString(arg0, "stemplatebody", arg1));
		emailAlertTransactionHistory.setSsentstatus(getString(arg0, "ssentstatus", arg1));
		emailAlertTransactionHistory.setDsentdate(getString(arg0, "dsentdate", arg1));
		emailAlertTransactionHistory
				.setSmailsentby(StringEscapeUtils.unescapeJava(getString(arg0, "smailsentby", arg1)));
		emailAlertTransactionHistory.setSscreenname(getString(arg0, "sscreenname", arg1));
		emailAlertTransactionHistory.setSemail(getString(arg0, "semail", arg1));
		emailAlertTransactionHistory.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		emailAlertTransactionHistory.setStransdate(getString(arg0, "stransdate", arg1));
		emailAlertTransactionHistory.setSmailstatus(getString(arg0, "smailstatus", arg1));
		emailAlertTransactionHistory.setScreateddate(getString(arg0, "screateddate", arg1));
		emailAlertTransactionHistory.setSreason(getString(arg0, "sreason", arg1));
		emailAlertTransactionHistory.setSusername(getString(arg0, "susername", arg1));
		emailAlertTransactionHistory.setScontrolname(getString(arg0, "scontrolname", arg1));
		emailAlertTransactionHistory.setSsystemid(getString(arg0, "ssystemid", arg1));
		emailAlertTransactionHistory.setNmailitemid(getInteger(arg0, "nmailitemid", arg1));
		emailAlertTransactionHistory.setNtransdatetimezonecode(getInteger(arg0, "ntransdatetimezonecode", arg1));
		emailAlertTransactionHistory.setNoffsetdtransdate(getInteger(arg0, "noffsetdtransdate", arg1));
		emailAlertTransactionHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return emailAlertTransactionHistory;

	}
}
