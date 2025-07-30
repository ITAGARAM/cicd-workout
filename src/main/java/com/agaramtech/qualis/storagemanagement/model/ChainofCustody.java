package com.agaramtech.qualis.storagemanagement.model;

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
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChainofCustody extends CustomizedResultsetRowMapper<ChainofCustody> implements Serializable, RowMapper<ChainofCustody> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncustodycode")
	private int ncustodycode;

	@Column(name = "npreregno")
	private int npreregno;

	@Column(name = "ntransactionsamplecode")
	private int ntransactionsamplecode;

	@Column(name = "ntransactionstatus")
	private int ntransactionstatus;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

	@Column(name = "sarno", length = 40, nullable = false)
	private String sarno;

	@Column(name = "ssamplearno", length = 40, nullable = false)
	private String ssamplearno;

	@Column(name = "sbarcode", length = 40, nullable = false)
	private String sbarcode;

	@Column(name = "sremarks", length = 40, nullable = false)
	private String sremarks;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private String sdisplaystatus;

	@Transient
	private String username;

	@Transient
	private String stransactiondate;

	@Override
	public ChainofCustody mapRow(final ResultSet arg0, final int arg1) throws SQLException {
		final ChainofCustody objChainofCustody = new ChainofCustody();
		objChainofCustody.setNcustodycode(getInteger(arg0, "ncustodycode", arg1));
		objChainofCustody.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objChainofCustody.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objChainofCustody.setNusercode(getInteger(arg0, "nusercode", arg1));
		objChainofCustody.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objChainofCustody.setSarno(StringEscapeUtils.unescapeJava(getString(arg0, "sarno", arg1)));
		objChainofCustody.setSsamplearno(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplearno", arg1)));
		objChainofCustody.setSbarcode(StringEscapeUtils.unescapeJava(getString(arg0, "sbarcode", arg1)));
		objChainofCustody.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0, "sremarks", arg1)));
		objChainofCustody.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objChainofCustody.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objChainofCustody.setNstatus(getShort(arg0, "nstatus", arg1));
		objChainofCustody.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objChainofCustody.setUsername(getString(arg0, "username", arg1));
		objChainofCustody.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objChainofCustody.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		return objChainofCustody;
	}
}