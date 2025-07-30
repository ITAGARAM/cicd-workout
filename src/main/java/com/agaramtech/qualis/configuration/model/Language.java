package com.agaramtech.qualis.configuration
.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields with the language table of database.
 */
@Entity
@Table(name = "language")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Language extends CustomizedResultsetRowMapper<Language> implements Serializable, RowMapper<Language> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nlanguagecode")
	private short nlanguagecode;

	@Column(name = "slanguagename", length = 50, nullable = false)
	private String slanguagename;

	@Column(name = "sfilename", length = 50, nullable = false)
	private String sfilename;

	@Column(name = "slanguagetypecode", length = 50, nullable = false)
	private String slanguagetypecode;

	@Column(name = "sreportlanguagecode", length = 50, nullable = false)
	private String sreportlanguagecode;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "sreportingtoolfilename", length = 50, nullable = false)
	private String sreportingtoolfilename;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public Language mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Language language = new Language();
		language.setNlanguagecode(getShort(arg0, "nlanguagecode", arg1));
		language.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		language.setSlanguagename(StringEscapeUtils.unescapeJava(getString(arg0, "slanguagename", arg1)));
		language.setNstatus(getShort(arg0, "nstatus", arg1));
		language.setSlanguagetypecode(StringEscapeUtils.unescapeJava(getString(arg0, "slanguagetypecode", arg1)));
		language.setSreportlanguagecode(StringEscapeUtils.unescapeJava(getString(arg0, "sreportlanguagecode", arg1)));
		language.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		language.setSreportingtoolfilename(
				StringEscapeUtils.unescapeJava(getString(arg0, "sreportingtoolfilename", arg1)));
		language.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		language.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return language;
	}
}
