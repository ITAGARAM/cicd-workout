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
@Table(name = "usermultirole")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class UserMultiRole extends CustomizedResultsetRowMapper<UserMultiRole> implements Serializable, RowMapper<UserMultiRole> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusermultirolecode")
	private int nusermultirolecode;

	@Column(name = "nusersitecode", nullable = false)
	private int nusersitecode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@ColumnDefault("4")
	@Column(name = "ndefaultrole", nullable = false)
	private short ndefaultrole = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "spassword", length = 100)
	private String spassword;

	@Column(name = "dpasswordvalidatedate")
	private Date dpasswordvalidatedate;

	@ColumnDefault("-1")
	@Column(name = "nnooffailedattempt", nullable = false)
	private short nnooffailedattempt = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String suserrolename;

	@Transient
	private transient String sloginid;

	@Transient
	private transient String sactivestatus;

	@Transient
	private transient String sdefaultstatus;

	@Transient
	private transient String susername;

	@Transient
	private transient int nusercode;

	@Transient
	private transient int nlockmode;

	@Transient
	private transient String sfirstname;

	@Transient
	private transient String slastname;

	@Override
	public UserMultiRole mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UserMultiRole userMultiRole = new UserMultiRole();

		userMultiRole.setSuserrolename(getString(arg0, "suserrolename", arg1));
		userMultiRole.setSpassword(StringEscapeUtils.unescapeJava(getString(arg0, "spassword", arg1)));
		userMultiRole.setSdefaultstatus(getString(arg0, "sdefaultstatus", arg1));
		userMultiRole.setNusermultirolecode(getInteger(arg0, "nusermultirolecode", arg1));
		userMultiRole.setNdefaultrole(getShort(arg0, "ndefaultrole", arg1));
		userMultiRole.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		userMultiRole.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		userMultiRole.setDpasswordvalidatedate(getDate(arg0, "dpasswordvalidatedate", arg1));
		userMultiRole.setNstatus(getShort(arg0, "nstatus", arg1));
		userMultiRole.setSactivestatus(getString(arg0, "sactivestatus", arg1));
		userMultiRole.setNusersitecode(getInteger(arg0, "nusersitecode", arg1));
		userMultiRole.setSloginid(getString(arg0, "sloginid", arg1));
		userMultiRole.setNnooffailedattempt(getShort(arg0, "nnooffailedattempt", arg1));
		userMultiRole.setSusername(getString(arg0, "susername", arg1));
		userMultiRole.setNsitecode(getShort(arg0, "nsitecode", arg1));
		userMultiRole.setNusercode(getInteger(arg0, "nusercode", arg1));
		userMultiRole.setNlockmode(getInteger(arg0, "nlockmode", arg1));
		userMultiRole.setSfirstname(getString(arg0, "sfirstname", arg1));
		userMultiRole.setSlastname(getString(arg0, "slastname", arg1));

		return userMultiRole;
	}

}
