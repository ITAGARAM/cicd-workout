package com.agaramtech.qualis.credential.model;

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

@Entity
@Table(name = "userssite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UsersSite extends CustomizedResultsetRowMapper<UsersSite> implements Serializable, RowMapper<UsersSite> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusersitecode")
	private int nusersitecode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "ndefaultsite", nullable = false)
	private short ndefaultsite;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String ssitename;
 
	@Transient
	private transient String sfirstname;

	@Transient
	private transient String slastname;

	@Transient
	private transient String sloginid;

	@Override
	public UsersSite mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UsersSite objUsersSite = new UsersSite();
		objUsersSite.setNdefaultsite(getShort(arg0, "ndefaultsite", arg1));
		objUsersSite.setNusercode(getInteger(arg0, "nusercode", arg1));
		objUsersSite.setNusersitecode(getInteger(arg0, "nusersitecode", arg1));
		objUsersSite.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objUsersSite.setNstatus(getShort(arg0, "nstatus", arg1));
		objUsersSite.setSsitename(getString(arg0, "ssitename", arg1));
		objUsersSite.setSfirstname(getString(arg0, "sfirstname", arg1));
		objUsersSite.setSlastname(getString(arg0, "slastname", arg1));
		objUsersSite.setSloginid(getString(arg0, "sloginid", arg1));
		objUsersSite.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objUsersSite;
	}

}
