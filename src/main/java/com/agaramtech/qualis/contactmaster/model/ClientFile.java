package com.agaramtech.qualis.contactmaster.model;

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

@Entity
@Table(name = "clientfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientFile extends CustomizedResultsetRowMapper<ClientFile> implements Serializable, RowMapper<ClientFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nclientfilecode")
	private int nclientfilecode;

	@Column(name = "nclientcode", nullable = false)
	private int nclientcode;

	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;

	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;

	@Column(name = "dcreateddate")
	private Instant dcreateddate;

	@Column(name = "ntzcreateddate")
	private short ntzcreateddate;

	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;

	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename = "";

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String slinkname;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String sattachmenttype;

	@Transient
	private transient String screateddate;

	@Transient
	private transient String sfilesize;

	@Override
	public ClientFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ClientFile objClientFile = new ClientFile();
		objClientFile.setNclientfilecode(getInteger(arg0, "nclientfilecode", arg1));
		objClientFile.setNclientcode(getInteger(arg0, "nclientcode", arg1));
		objClientFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objClientFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objClientFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objClientFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objClientFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objClientFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objClientFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objClientFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objClientFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objClientFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objClientFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objClientFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objClientFile.setSfilename(getString(arg0, "sfilename", arg1));
		objClientFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objClientFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objClientFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objClientFile.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objClientFile;
	}
}