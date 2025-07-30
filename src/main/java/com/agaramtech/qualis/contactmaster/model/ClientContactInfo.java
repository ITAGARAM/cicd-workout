package com.agaramtech.qualis.contactmaster.model;

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

/**
 * This class is used to map the fields of 'manufacturercontactinfo' table of
 * the Database.
 * 
 * @author ATE090
 * @version 9.0.0.1
 * @since 07- Aug- 2020
 */
@Entity
@Table(name = "clientcontactinfo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientContactInfo extends CustomizedResultsetRowMapper<ClientContactInfo>
		implements Serializable, RowMapper<ClientContactInfo> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nclientcontactcode")
	private int nclientcontactcode;

	@Column(name = "nclientsitecode", nullable = false)
	private int nclientsitecode;

	@Column(name = "nclientcode", nullable = false)
	private int nclientcode;

	@Column(name = "scontactname", length = 100, nullable = false)
	private String scontactname;

	@Column(name = "sphoneno", length = 50)
	private String sphoneno = "";

	@Column(name = "smobileno", length = 50)
	private String smobileno = "";

	@Column(name = "semail", length = 50)
	private String semail = "";

	@Column(name = "sfaxno", length = 50)
	private String sfaxno = "";

	@Column(name = "scomments", length = 255)
	private String scomments = "";

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();;

	@Transient
	private transient String sdefaultContact;

	@Transient
	private transient int ncountrycode;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient boolean isreadonly;

	@Transient
	private transient int nclientcatcode;

	@Override
	public ClientContactInfo mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ClientContactInfo clientContactInfo = new ClientContactInfo();
		clientContactInfo.setNclientcontactcode(getInteger(arg0, "nclientcontactcode", arg1));
		clientContactInfo.setNclientsitecode(getInteger(arg0, "nclientsitecode", arg1));
		clientContactInfo.setNclientcode(getInteger(arg0, "nclientcode", arg1));
		clientContactInfo.setScontactname(StringEscapeUtils.unescapeJava(getString(arg0, "scontactname", arg1)));
		clientContactInfo.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0, "sphoneno", arg1)));
		clientContactInfo.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0, "smobileno", arg1)));
		clientContactInfo.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		clientContactInfo.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0, "sfaxno", arg1)));
		clientContactInfo.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		clientContactInfo.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		clientContactInfo.setNstatus(getShort(arg0, "nstatus", arg1));
		clientContactInfo.setSdefaultContact(getString(arg0, "sdefaultContact", arg1));
		clientContactInfo.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		clientContactInfo.setIsreadonly(getBoolean(arg0, "isreadonly", arg1));
		clientContactInfo.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		clientContactInfo.setNclientcatcode(getInteger(arg0, "nclientcatcode", arg1));
		clientContactInfo.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return clientContactInfo;
	}

}
