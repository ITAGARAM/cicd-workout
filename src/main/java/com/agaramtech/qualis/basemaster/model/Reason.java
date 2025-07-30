package com.agaramtech.qualis.basemaster.model;

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

/**
 * This class is used to map the fields of 'reason' table of the Database.
 */
@Entity
@Table(name = "reason")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Reason extends CustomizedResultsetRowMapper<Reason> implements Serializable, RowMapper<Reason> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreasoncode")
	private int nreasoncode;

	@Column(name = "sreason", length = 100, nullable = false)
	private String sreason;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	public Reason mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Reason reason = new Reason();
		reason.setNstatus(getShort(arg0, "nstatus", arg1));
		reason.setNreasoncode(getShort(arg0, "nreasoncode", arg1));
		reason.setNsitecode(getShort(arg0, "nsitecode", arg1));
		reason.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		reason.setSreason(StringEscapeUtils.unescapeJava(getString(arg0, "sreason", arg1)));
		reason.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return reason;
	}

}
