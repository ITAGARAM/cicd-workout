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

/**
 * This class is used to map the fields of 'emailconfig' table of the Database.
 */
@Entity
@Table(name = "emailconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmailConfig extends CustomizedResultsetRowMapper<EmailConfig>
		implements Serializable, RowMapper<EmailConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nemailconfigcode")
	private int nemailconfigcode;

	@Column(name = "nemailhostcode", nullable = false)
	private int nemailhostcode;

	@Column(name = "nemailtemplatecode", nullable = false)
	private int nemailtemplatecode;

	@Column(name = "nemailscreencode", nullable = false)
	private int nemailscreencode;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "ncontrolcode", nullable = false)
	private int ncontrolcode;

	@Column(name = "nenableemail", nullable = false)
	private int nenableemail;

	@Column(name = "nneedattachment", nullable = false)
	private int nneedattachment;

	@Column(name = "nemailuserquerycode", nullable = false)
	private int nemailuserquerycode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String scontrolids;

	@Transient
	private transient String senablemailstatus;

	@Transient
	private transient String stablename;

	@Transient
	private transient String shostname;

	@Transient
	private transient String stemplatename;

	@Transient
	private transient String sscreenname;

	@Transient
	private transient String sactiontype;

	@Transient
	private transient String senablestatus;

	@Transient
	private transient String scolumnname;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String sformname;

	@Transient
	private transient String squery;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient String semail;

	@Transient
	private transient int nportno;

	@Transient
	private transient String stemplatebody;

	@Transient
	private transient String ssubject;

	@Transient
	private transient String stagquery;

	@Override
	public EmailConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final EmailConfig emailConfiguration = new EmailConfig();
		emailConfiguration.setNemailconfigcode(getInteger(arg0, "nemailconfigcode", arg1));
		emailConfiguration.setNemailhostcode(getInteger(arg0, "nemailhostcode", arg1));
		emailConfiguration.setNemailscreencode(getInteger(arg0, "nemailscreencode", arg1));
		emailConfiguration.setNemailtemplatecode(getInteger(arg0, "nemailtemplatecode", arg1));
		emailConfiguration.setNsitecode(getShort(arg0, "nsitecode", arg1));
		emailConfiguration.setNstatus(getShort(arg0, "nstatus", arg1));
		emailConfiguration.setSactiontype(getString(arg0, "sactiontype", arg1));
		emailConfiguration.setSenablestatus(getString(arg0, "senablestatus", arg1));
		emailConfiguration.setShostname(getString(arg0, "shostname", arg1));
		emailConfiguration.setSscreenname(getString(arg0, "sscreenname", arg1));
		emailConfiguration.setStemplatename(getString(arg0, "stemplatename", arg1));
		emailConfiguration.setNneedattachment(getInteger(arg0, "nneedattachment", arg1));
		emailConfiguration.setNenableemail(getInteger(arg0, "nenableemail", arg1));
		emailConfiguration.setScolumnname(getString(arg0, "scolumnname", arg1));
		emailConfiguration.setSenablemailstatus(getString(arg0, "senablemailstatus", arg1));
		emailConfiguration.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		emailConfiguration.setScontrolids(getString(arg0, "scontrolids", arg1));
		emailConfiguration.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		emailConfiguration.setNformcode(getInteger(arg0, "nformcode", arg1));
		emailConfiguration.setSformname(getString(arg0, "sformname", arg1));
		emailConfiguration.setStablename(getString(arg0, "stablename", arg1));
		emailConfiguration.setNemailuserquerycode(getInteger(arg0, "nemailuserquerycode", arg1));
		emailConfiguration.setSquery(getString(arg0, "squery", arg1));
		emailConfiguration.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		emailConfiguration.setSemail(getString(arg0, "semail", arg1));
		emailConfiguration.setNportno(getInteger(arg0, "nportno", arg1));
		emailConfiguration.setStemplatebody(getString(arg0, "stemplatebody", arg1));
		emailConfiguration.setSsubject(getString(arg0, "ssubject", arg1));
		emailConfiguration.setStagquery(getString(arg0, "stagquery", arg1));
		return emailConfiguration;
	}

}
