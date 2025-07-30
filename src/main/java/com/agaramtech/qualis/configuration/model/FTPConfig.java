package com.agaramtech.qualis.configuration.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'ftpconfig' table of the Database.
 */
@Entity
@Table(name = "ftpconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FTPConfig extends CustomizedResultsetRowMapper<FTPConfig> implements Serializable, RowMapper<FTPConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nftpno")
	private int nftpno;

	@Column(name = "shost", length = 20, nullable = false)
	private String shost;

	@Column(name = "spassword", length = 100)
	private String spassword;

	@Column(name = "susername", length = 50, nullable = false)
	private String susername;

	@Column(name = "nportno", nullable = false)
	private short nportno;

	@Column(name = "sphysicalpath", length = 100, nullable = false)
	private String sphysicalpath;

	@ColumnDefault("1")
	@Column(name = "nftptypecode", nullable = false)
	private short nftptypecode = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nsslrequired", nullable = false)
	private short nsslrequired = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nchecksumrequired", nullable = false)
	private short nchecksumrequired = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String ssslrequired;

	@Transient
	private transient String schecksumrequired;

	@Transient
	private transient String sdefaultstatus;

	@Transient
	private transient String ssitename;

	@Transient
	private transient String sftptypename;

	@Transient
	private transient String sdisplayname;

	@Override
	public FTPConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final FTPConfig objFTPconfig = new FTPConfig();
		objFTPconfig.setNftpno(getInteger(arg0, "nftpno", arg1));
		objFTPconfig.setShost(getString(arg0, "shost", arg1));
		objFTPconfig.setSpassword(getString(arg0, "spassword", arg1));
		objFTPconfig.setSusername(getString(arg0, "susername", arg1));
		objFTPconfig.setSsitename(getString(arg0, "ssitename", arg1));
		objFTPconfig.setNportno(getShort(arg0, "nportno", arg1));
		objFTPconfig.setSphysicalpath(getString(arg0, "sphysicalpath", arg1));
		objFTPconfig.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objFTPconfig.setSdefaultstatus(getString(arg0, "sdefaultstatus", arg1));
		objFTPconfig.setSsslrequired(getString(arg0, "ssslrequired", arg1));
		objFTPconfig.setSchecksumrequired(getString(arg0, "schecksumrequired", arg1));
		objFTPconfig.setNsslrequired(getShort(arg0, "nsslrequired", arg1));
		objFTPconfig.setNchecksumrequired(getShort(arg0, "nchecksumrequired", arg1));
		objFTPconfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objFTPconfig.setNstatus(getShort(arg0, "nstatus", arg1));
		objFTPconfig.setNftptypecode(getShort(arg0, "nftptypecode", arg1));
		objFTPconfig.setSftptypename(getString(arg0, "sftptypename", arg1));
		objFTPconfig.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objFTPconfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objFTPconfig;
	}

}
