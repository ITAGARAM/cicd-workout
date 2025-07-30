package com.agaramtech.qualis.project.model;

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
@Data
@Table(name = "projectmasterfile")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectMasterFile extends CustomizedResultsetRowMapper<ProjectMasterFile>
		implements Serializable, RowMapper<ProjectMasterFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprojectmasterfilecode")
	private int nprojectmasterfilecode;

	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode;

	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;

	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;

	@Column(name = "sdescription", length = 255)
	private String sdescription="";

	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;

	@Column(name = "dcreateddate")
	private Instant dcreateddate;

	@Column(name = "ntzcreateddate")
	private short ntzcreateddate;

	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;

	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename="";

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

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

	@Transient
	private transient short ntransactionstatus;

	@Transient
	private transient int nprojecttypecode;

	@Override
	public ProjectMasterFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		ProjectMasterFile objProjectMasterFile = new ProjectMasterFile();
		objProjectMasterFile.setNprojectmasterfilecode(getInteger(arg0, "nprojectmasterfilecode", arg1));
		objProjectMasterFile.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objProjectMasterFile.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objProjectMasterFile.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objProjectMasterFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objProjectMasterFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objProjectMasterFile.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objProjectMasterFile.setDcreateddate(getInstant(arg0, "dcreateddate", arg1));
		objProjectMasterFile.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objProjectMasterFile.setNstatus(getShort(arg0, "nstatus", arg1));
		objProjectMasterFile.setSlinkname(getString(arg0, "slinkname", arg1));
		objProjectMasterFile.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objProjectMasterFile.setSattachmenttype(getString(arg0, "sattachmenttype", arg1));
		objProjectMasterFile.setScreateddate(getString(arg0, "screateddate", arg1));
		objProjectMasterFile.setSfilename(getString(arg0, "sfilename", arg1));
		objProjectMasterFile.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objProjectMasterFile.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objProjectMasterFile.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProjectMasterFile.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProjectMasterFile.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objProjectMasterFile.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));

		return objProjectMasterFile;
	}

}
