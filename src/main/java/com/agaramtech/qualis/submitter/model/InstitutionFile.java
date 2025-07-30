package com.agaramtech.qualis.submitter.model;

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
@Table(name = "institutionfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstitutionFile extends CustomizedResultsetRowMapper<InstitutionFile> implements Serializable, RowMapper<InstitutionFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstitutionfilecode")
	private int ninstitutionfilecode;

	@Column(name = "ninstitutioncode")
	private int ninstitutioncode;

	@Column(name = "nfilesize")
	private int nfilesize;

	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;

	@Column(name = "sfiledesc", length = 255, nullable = false)
	private String sfiledesc;

	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename;

	@Column(name = "dcreateddate")
	private Instant dcreateddate;

	@Column(name = "ntzcreateddate")
	private short ntzcreateddate = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "noffsetdcreateddate")
	private int noffsetdcreateddate;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "ntzmodifieddate")
	private short ntzmodifieddate = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "noffsetdmodifieddate")
	private int noffsetdmodifieddate;

	@Column(name = "nlinkcode", nullable = false)
	@ColumnDefault("-1")
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	@ColumnDefault("-1")
	private short nattachmenttypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sinstitutionname;

	@Transient
	private transient String screateddate;

	@Transient
	private transient String slinkname;

	@Transient
	private transient String sattachmenttype;

	

	@Override
	public InstitutionFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InstitutionFile objInstitututionFile = new InstitutionFile();
		objInstitututionFile.setNinstitutionfilecode(getInteger(arg0, "ninstitutionfilecode", arg1));
		objInstitututionFile.setNinstitutioncode(getInteger(arg0, "ninstitutioncode", arg1));
		objInstitututionFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objInstitututionFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objInstitututionFile.setSfiledesc(StringEscapeUtils.unescapeJava(getString(arg0, "sfiledesc", arg1)));
		objInstitututionFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objInstitututionFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objInstitututionFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objInstitututionFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objInstitututionFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInstitututionFile.setNtzmodifieddate(getShort(arg0, "ntzmodifieddate", arg1));
		objInstitututionFile.setNoffsetdmodifieddate(getInteger(arg0, "noffsetdmodifieddate", arg1));
		objInstitututionFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objInstitututionFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objInstitututionFile.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInstitututionFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objInstitututionFile.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objInstitututionFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objInstitututionFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objInstitututionFile.setSlinkname(getString(arg0, "slinkname", arg1));
		return objInstitututionFile;
	}
}
