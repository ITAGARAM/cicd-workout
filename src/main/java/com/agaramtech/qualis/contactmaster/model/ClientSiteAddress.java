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

@Entity
@Table(name = "clientsiteaddress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientSiteAddress extends CustomizedResultsetRowMapper<ClientSiteAddress>
		implements Serializable, RowMapper<ClientSiteAddress> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nclientsitecode")
	private int nclientsitecode;

	@Column(name = "nclientcode", nullable = false)
	private int nclientcode;

	@ColumnDefault("1")
	@Column(name = "ncountrycode", nullable = false)
	private int ncountrycode = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "sclientsitename", length = 100, nullable = false)
	private String sclientsitename;

	@Column(name = "saddress1", length = 255, nullable = false)
	private String saddress1;

	@Column(name = "saddress2", length = 255)
	private String saddress2 = "";

	@Column(name = "saddress3", length = 255)
	private String saddress3 = "";

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String scountryname;

	@Transient
	private transient String transactionstatus;

	@Transient
	private transient String defaultstatus;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient boolean isreadonly;

	@Transient
	private transient String scontactname;

	@Transient
	private transient int nclientcatcode;

	@Override
	public ClientSiteAddress mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ClientSiteAddress objClientSiteAddress = new ClientSiteAddress();
		objClientSiteAddress.setScountryname(getString(arg0, "scountryname", arg1));
		objClientSiteAddress.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		objClientSiteAddress.setNstatus(getShort(arg0, "nstatus", arg1));
		objClientSiteAddress.setSaddress2(StringEscapeUtils.unescapeJava(getString(arg0, "saddress2", arg1)));
		objClientSiteAddress.setTransactionstatus(getString(arg0, "transactionstatus", arg1));
		objClientSiteAddress.setSaddress1(StringEscapeUtils.unescapeJava(getString(arg0, "saddress1", arg1)));
		objClientSiteAddress.setSaddress3(StringEscapeUtils.unescapeJava(getString(arg0, "saddress3", arg1)));
		objClientSiteAddress.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objClientSiteAddress.setSclientsitename(StringEscapeUtils.unescapeJava(getString(arg0, "sclientsitename", arg1)));
		objClientSiteAddress.setDefaultstatus(getString(arg0, "defaultstatus", arg1));
		objClientSiteAddress.setNclientcode(getInteger(arg0, "nclientcode", arg1));
		objClientSiteAddress.setNclientsitecode(getInteger(arg0, "nclientsitecode", arg1));
		objClientSiteAddress.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objClientSiteAddress.setIsreadonly(getBoolean(arg0, "isreadonly", arg1));
		objClientSiteAddress.setScontactname(getString(arg0, "scontactname", arg1));
		objClientSiteAddress.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objClientSiteAddress.setNclientcatcode(getInteger(arg0, "nclientcatcode", arg1));
		objClientSiteAddress.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objClientSiteAddress;

	}
}
