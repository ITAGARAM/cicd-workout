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
@Data
@Table(name = "manufacturercontactinfo")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ManufacturerContactInfo extends CustomizedResultsetRowMapper<ManufacturerContactInfo>
		implements Serializable, RowMapper<ManufacturerContactInfo> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmanufcontactcode")
	private int nmanufcontactcode;

	@Column(name = "nmanufsitecode", nullable = false)
	private int nmanufsitecode;

	@Column(name = "nmanufcode", nullable = false)
	private int nmanufcode ;

	@Column(name = "scontactname", length = 100, nullable = false)
	private String scontactname = "";

	@Column(name = "sphoneno", length = 50)
	private String sphoneno = "";

	@Column(name = "smobileno", length = 50)
	private String smobileno = "";

	@Column(name = "semail", length = 50)
	private String semail = "";

	@Column(name = "sfaxno", length = 50)
	private String sfaxno = "";

	@Column(name = "scomments", length = 255)
	private String scomments = "";

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sdefaultContact = "";
	
	@Transient
	private transient boolean isreadonly;

	@Override
	public ManufacturerContactInfo mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ManufacturerContactInfo manufacturerContactInfo = new ManufacturerContactInfo();
		manufacturerContactInfo.setNmanufcontactcode(getInteger(arg0, "nmanufcontactcode", arg1));
		manufacturerContactInfo.setNmanufsitecode(getInteger(arg0, "nmanufsitecode", arg1));
		manufacturerContactInfo.setNmanufcode(getInteger(arg0, "nmanufcode", arg1));
		manufacturerContactInfo.setScontactname(StringEscapeUtils.unescapeJava(getString(arg0, "scontactname", arg1)));
		manufacturerContactInfo.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0, "sphoneno", arg1)));
		manufacturerContactInfo.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0, "smobileno", arg1)));
		manufacturerContactInfo.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		manufacturerContactInfo.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0, "sfaxno", arg1)));
		manufacturerContactInfo.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		manufacturerContactInfo.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		manufacturerContactInfo.setNstatus(getShort(arg0, "nstatus", arg1));
		manufacturerContactInfo.setSdefaultContact(getString(arg0, "sdefaultContact", arg1));
		manufacturerContactInfo.setIsreadonly(getBoolean(arg0, "isreadonly", arg1));
		manufacturerContactInfo.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		manufacturerContactInfo.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return manufacturerContactInfo;
	}
}
