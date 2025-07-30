package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import com.fasterxml.jackson.annotation.JsonFormat;

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
 * This class is used to map the fields of 'users' table of the Database.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Users extends CustomizedResultsetRowMapper<Users> implements Serializable, RowMapper<Users> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusercode")
	private int nusercode;

	@ColumnDefault("-1")
	@Column(name = "ndeptcode", nullable = false)
	private int ndeptcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ncountrycode", nullable = false)
	private int ncountrycode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndesignationcode", nullable = false)
	private int ndesignationcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("6")
	@Column(name = "nlockmode", nullable = false)
	private short nlockmode = (short) Enumeration.TransactionStatus.UNLOCK.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nlogintypecode", nullable = false)
	private short nlogintypecode = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "sloginid", length = 20, nullable = false)
	private String sloginid;

	@Column(name = "sfirstname", length = 50, nullable = false)
	private String sfirstname;

	@Column(name = "slastname", length = 50, nullable = false)
	private String slastname;

	@Column(name = "saddress1", length = 255, nullable = false)
	private String saddress1;

	@Column(name = "semail", length = 50)
	private String semail;

	@Column(name = "sphoneno", length = 50, nullable = false)
	private String sphoneno;

	@Column(name = "saddress2", length = 255)
	private String saddress2;

	@Column(name = "saddress3", length = 255)
	private String saddress3;

	@Column(name = "sbloodgroup", length = 10)
	private String sbloodgroup;

	@Column(name = "squalification", length = 10)
	private String squalification;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Column(name = "ddateofjoin")
	private Date ddateofjoin;

	@Column(name = "sjobdescription", length = 255)
	private String sjobdescription;

	@Column(name = "smobileno", length = 50)
	private String smobileno;

	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "sempid", length = 20)
	private String sempid;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient int ntranssitecode;
	@Transient
	private transient String sdeptname;
	@Transient
	private transient String sdesignationname;
	@Transient
	private transient String scountryname;
	@Transient
	private transient String slockstatus;
	@Transient
	private transient String stransstatus;
	@Transient
	private transient String logintype;
	@Transient
	private transient String susername;
	@Transient
	private transient String sactivestatus;
	@Transient
	private transient String ssignimgname;
	@Transient
	private transient String ssignimgftp;
	@Transient
	private transient String suserimgname;
	@Transient
	private transient String suserimgftp;
	@Transient
	private transient String sdateofjoin;
	@Transient
	private transient int ncontactusercode;
	@Transient
	private transient String scontactusername;
	@Transient
	private transient int nusersitecode;

	@Override
	public Users mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Users objUsers = new Users();
		objUsers.setSbloodgroup(StringEscapeUtils.unescapeJava(getString(arg0, "sbloodgroup", arg1)));
		objUsers.setSjobdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sjobdescription", arg1)));
		objUsers.setNusercode(getInteger(arg0, "nusercode", arg1));
		objUsers.setDdateofjoin(getDate(arg0, "ddateofjoin", arg1));
		objUsers.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objUsers.setSfirstname(StringEscapeUtils.unescapeJava(getString(arg0, "sfirstname", arg1)));
		objUsers.setSlastname(StringEscapeUtils.unescapeJava(getString(arg0, "slastname", arg1)));
		objUsers.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objUsers.setNdesignationcode(getInteger(arg0, "ndesignationcode", arg1));
		objUsers.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0, "sphoneno", arg1)));
		objUsers.setSaddress3(StringEscapeUtils.unescapeJava(getString(arg0, "saddress3", arg1)));
		objUsers.setNdeptcode(getInteger(arg0, "ndeptcode", arg1));
		objUsers.setSaddress2(StringEscapeUtils.unescapeJava(getString(arg0, "saddress2", arg1)));
		objUsers.setSaddress1(StringEscapeUtils.unescapeJava(getString(arg0, "saddress1", arg1)));
		objUsers.setSqualification(StringEscapeUtils.unescapeJava(getString(arg0, "squalification", arg1)));
		objUsers.setSloginid(StringEscapeUtils.unescapeJava(getString(arg0, "sloginid", arg1)));
		objUsers.setNstatus(getShort(arg0, "nstatus", arg1));
		objUsers.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		objUsers.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0, "smobileno", arg1)));
		objUsers.setNlockmode(getShort(arg0, "nlockmode", arg1));
		objUsers.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objUsers.setNlogintypecode(getShort(arg0, "nlogintypecode", arg1));
		objUsers.setNtranssitecode(getInteger(arg0, "ntranssitecode", arg1));
		objUsers.setSdeptname(getString(arg0, "sdeptname", arg1));
		objUsers.setScountryname(getString(arg0, "scountryname", arg1));
		objUsers.setSdesignationname(getString(arg0, "sdesignationname", arg1));
		objUsers.setLogintype(getString(arg0, "logintype", arg1));
		objUsers.setSlockstatus(StringEscapeUtils.unescapeJava(getString(arg0, "slockstatus", arg1)));
		objUsers.setStransstatus(getString(arg0, "stransstatus", arg1));
		objUsers.setSusername(StringEscapeUtils.unescapeJava(getString(arg0, "susername", arg1)));
		objUsers.setSactivestatus(getString(arg0, "sactivestatus", arg1));
		objUsers.setSsignimgname(getString(arg0, "ssignimgname", arg1));
		objUsers.setSsignimgftp(getString(arg0, "ssignimgftp", arg1));
		objUsers.setSuserimgname(StringEscapeUtils.unescapeJava(getString(arg0, "suserimgname", arg1)));
		objUsers.setSuserimgftp(StringEscapeUtils.unescapeJava(getString(arg0, "suserimgftp", arg1)));
		objUsers.setScontactusername(StringEscapeUtils.unescapeJava(getString(arg0, "scontactusername", arg1)));
		objUsers.setNcontactusercode(getInteger(arg0, "ncontactusercode", arg1));
		objUsers.setSdateofjoin(getString(arg0, "sdateofjoin", arg1));
		objUsers.setNusersitecode(getInteger(arg0, "nusersitecode", arg1));
		objUsers.setSempid(StringEscapeUtils.unescapeJava(getString(arg0, "sempid", arg1)));
		objUsers.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objUsers;
	}
	
	
}
