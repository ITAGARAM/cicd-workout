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
@Entity
@Table(name = "emailtemplate")
@Data
public class EmailTemplate extends CustomizedResultsetRowMapper<EmailTemplate>
		implements Serializable, RowMapper<EmailTemplate> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nemailtemplatecode")
	private int nemailtemplatecode;

	@Column(name = "stemplatename", length = 255, nullable = false)
	private String stemplatename;

	@Column(name = "stemplatebody", length = 3500, nullable = false)
	private String stemplatebody;

	@Column(name = "ssubject", length = 100, nullable = false)
	private String ssubject;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nemailtagcode", nullable = false)
	private int nemailtagcode;

	@Column(name = "nregtypecode", nullable = false)
	private int nregtypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nregsubtypecode", nullable = false)
	private int nregsubtypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String stagname;

	@Override
	public EmailTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailTemplate emailTemplate = new EmailTemplate();
		emailTemplate.setNemailtemplatecode(getInteger(arg0, "nemailtemplatecode", arg1));
		emailTemplate.setStemplatename(getString(arg0, "stemplatename", arg1));
		emailTemplate.setStemplatebody(getString(arg0, "stemplatebody", arg1));
		emailTemplate.setSsubject(getString(arg0, "ssubject", arg1));
		emailTemplate.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailTemplate.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		emailTemplate.setNemailtagcode(getInteger(arg0, "nemailtagcode", arg1));
		emailTemplate.setNregsubtypecode(getInteger(arg0, "nregsubtypecode", arg1));
		emailTemplate.setNregtypecode(getInteger(arg0, "nregtypecode", arg1));
		emailTemplate.setStagname(getString(arg0, "stagname", arg1));
		return emailTemplate;
	}
}
