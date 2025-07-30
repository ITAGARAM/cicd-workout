package com.agaramtech.qualis.emailmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "emailscreen")
public class EmailScreen extends CustomizedResultsetRowMapper<EmailScreen>
		implements Serializable, RowMapper<EmailScreen> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nemailscreencode")
	private int nemailscreencode;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "stablename", length = 100)
	private String stablename = "";

	@Column(name = "scolumnname", length = 100)
	private String scolumnname = "";

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sformname;

	@Transient
	private transient String sscreenname;

	@Override
	public EmailScreen mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailScreen emailScreen = new EmailScreen();
		emailScreen.setNemailscreencode(getInteger(arg0, "nemailscreencode", arg1));
		emailScreen.setNformcode(getInteger(arg0, "nformcode", arg1));
		emailScreen.setSscreenname(getString(arg0, "sscreenname", arg1));
		emailScreen.setStablename(getString(arg0, "stablename", arg1));
		emailScreen.setScolumnname(getString(arg0, "scolumnname", arg1));
		emailScreen.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailScreen.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		emailScreen.setSformname(getString(arg0, "sformname", arg1));
		return emailScreen;
	}
}
