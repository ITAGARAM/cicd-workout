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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "emailuserconfig")
public class EmailUserConfig extends CustomizedResultsetRowMapper<EmailUserConfig>
		implements Serializable, RowMapper<EmailUserConfig> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nemailuserconfigcode")
	private int nemailuserconfigcode;

	@Column(name = "nemailconfigcode", nullable = false)
	private int nemailconfigcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String semail;

	@Transient
	private transient String susername;

	@Override
	public EmailUserConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		EmailUserConfig emailUserConfiguration = new EmailUserConfig();
		emailUserConfiguration.setNemailuserconfigcode(getInteger(arg0, "nemailuserconfigcode", arg1));
		emailUserConfiguration.setNemailconfigcode(getInteger(arg0, "nemailconfigcode", arg1));
		emailUserConfiguration.setNusercode(getInteger(arg0, "nusercode", arg1));
		emailUserConfiguration.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailUserConfiguration.setSemail(getString(arg0, "semail", arg1));
		emailUserConfiguration.setSusername(getString(arg0, "susername", arg1));
		return emailUserConfiguration;
	}
}
