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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "emailtag")
public class EmailTag extends CustomizedResultsetRowMapper<EmailTag> implements Serializable, RowMapper<EmailTag> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nemailtagcode")
	private int nemailtagcode;

	@Column(name = "stagname", length = 255, nullable = false)
	private String stagname;

	@Column(name = "squery", columnDefinition = "text", nullable = false)
	private String squery;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public EmailTag mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailTag emailTag = new EmailTag();
		emailTag.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		emailTag.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailTag.setStagname(getString(arg0, "stagname", arg1));
		emailTag.setSquery(getString(arg0, "squery", arg1));
		emailTag.setNemailtagcode(getInteger(arg0, "nemailtagcode", arg1));
		return emailTag;
	}
}
