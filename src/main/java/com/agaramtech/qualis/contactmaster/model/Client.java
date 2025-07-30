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
 * This class is used to map the fields of 'client' table of the Database.
 * 
 * @author ATE181
 * @version 9.0.0.1
 * @since 30- Jun- 2020
 */
@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Client extends CustomizedResultsetRowMapper<Client> implements Serializable, RowMapper<Client> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nclientcode")
	private int nclientcode;

	@Column(name = "nclientcatcode", nullable = false)
	private int nclientcatcode;

	@Column(name = "ncountrycode", nullable = false)
	private int ncountrycode;

	@Column(name = "sclientname", length = 100, nullable = false)
	private String sclientname;

	@Column(name = "sclientid", length = 50, nullable = false)
	private String sclientid;

	@ColumnDefault("1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String scountryname;

	@Transient
	private transient String sclientcatname;

	@Transient
	private transient String sclientsitename;

	@Transient
	private transient String scontactname;

	@Transient
	private transient String saddress1;

	@Transient
	private transient String saddress2;

	@Transient
	private transient String saddress3;

	@Override
	public Client mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Client client = new Client();
		client.setNclientcode(getInteger(arg0, "nclientcode", arg1));
		client.setNclientcatcode(getInteger(arg0, "nclientcatcode", arg1));
		client.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		client.setSclientname(StringEscapeUtils.unescapeJava(getString(arg0, "sclientname", arg1)));
		client.setSclientcatname(getString(arg0, "sclientcatname", arg1));
		client.setSaddress1(getString(arg0, "saddress1", arg1));
		client.setSaddress2(getString(arg0, "saddress2", arg1));
		client.setSaddress3(getString(arg0, "saddress3", arg1));
		client.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		client.setNsitecode(getShort(arg0, "nsitecode", arg1));
		client.setNstatus(getShort(arg0, "nstatus", arg1));
		client.setScountryname(getString(arg0, "scountryname", arg1));
		client.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		client.setSclientid(StringEscapeUtils.unescapeJava(getString(arg0, "sclientid", arg1)));
		client.setSclientsitename(getString(arg0, "sclientsitename", arg1));
		client.setScontactname(getString(arg0, "scontactname", arg1));
		client.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return client;
	}

}
