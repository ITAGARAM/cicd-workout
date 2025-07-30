package com.agaramtech.qualis.digitalsignature.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userdigitalsign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DigitalSignature extends CustomizedResultsetRowMapper<DigitalSignature>
		implements Serializable, RowMapper<DigitalSignature> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "sdigisignname", length = 100, nullable = false)
	private String sdigisignname;

	@Column(name = "sdigisignftp", length = 100, nullable = false)
	private String sdigisignftp;

	@Column(name = "ssecuritykey", length = 50)
	private String ssecuritykey;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public DigitalSignature mapRow(ResultSet arg0, int arg1) throws SQLException {
		final DigitalSignature digitalSignature = new DigitalSignature();
		digitalSignature.setNusercode(getInteger(arg0, "nusercode", arg1));
		digitalSignature.setSdigisignftp(StringEscapeUtils.unescapeJava(getString(arg0, "sdigisignftp", arg1)));
		digitalSignature.setSdigisignname(StringEscapeUtils.unescapeJava(getString(arg0, "sdigisignname", arg1)));
		digitalSignature.setSsecuritykey(StringEscapeUtils.unescapeJava(getString(arg0, "ssecuritykey", arg1)));
		digitalSignature.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		digitalSignature.setNsitecode(getShort(arg0, "nsitecode", arg1));
		digitalSignature.setNstatus(getShort(arg0, "nstatus", arg1));
		return digitalSignature;
	}
}