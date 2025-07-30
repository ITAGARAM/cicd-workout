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

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "emailhost")
@Data
public class EmailHost extends CustomizedResultsetRowMapper<EmailHost> implements Serializable, RowMapper<EmailHost> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nemailhostcode")
	private int nemailhostcode;

	@Column(name = "nportno", nullable = false)
	private int nportno;

	@Column(name = "shostname", length = 100, nullable = false)
	private String shostname;

	@Column(name = "sauthenticationname", length = 50, nullable = false)
	private String sauthenticationname;

	@Column(name = "sprofilename", length = 100, nullable = false)
	private String sprofilename;

	@Column(name = "semail", length = 100, nullable = false)
	private String semail;

	@Column(name = "spassword", length = 100)
	private String spassword = "";

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public EmailHost mapRow(ResultSet arg0, int arg1) throws SQLException {
		final EmailHost emailhost = new EmailHost();
		emailhost.setNstatus(getInteger(arg0, "nstatus", arg1));
		emailhost.setNemailhostcode(getInteger(arg0, "nemailhostcode", arg1));
		emailhost.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		emailhost.setNportno(getInteger(arg0, "nportno", arg1));
		emailhost.setSemail(getString(arg0, "semail", arg1));
		emailhost.setSpassword(getString(arg0, "spassword", arg1));
		emailhost.setSprofilename(getString(arg0, "sprofilename", arg1));
		emailhost.setShostname(getString(arg0, "shostname", arg1));
		emailhost.setSauthenticationname(getString(arg0, "sauthenticationname", arg1));
		return emailhost;
	}
}
