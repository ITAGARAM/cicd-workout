package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

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
@Table(name = "usermultideputy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class UserMultiDeputy extends CustomizedResultsetRowMapper<UserMultiDeputy> implements Serializable, RowMapper<UserMultiDeputy> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusermultideputycode")
	private int nusermultideputycode;

	@Column(name = "nusersitecode", nullable = false)
	private int nusersitecode;

	@Column(name = "ndeputyusersitecode", nullable = false)
	private int ndeputyusersitecode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient List<UserMultiDeputy> lstUserMultiDeputy;

	@Transient
	private transient String sdeputyid;

	@Transient
	private transient String sdeputyname;

	@Transient
	private transient String suserrolename;

	@Transient
	private transient String sdisplaystatus;

	@Transient
	private transient int nusercode;

	@Override
	public UserMultiDeputy mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UserMultiDeputy objMultiDeputy = new UserMultiDeputy();
		objMultiDeputy.setNdeputyusersitecode(getInteger(arg0, "ndeputyusersitecode", arg1));
		objMultiDeputy.setNstatus(getShort(arg0, "nstatus", arg1));
		objMultiDeputy.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objMultiDeputy.setNusermultideputycode(getInteger(arg0, "nusermultideputycode", arg1));
		objMultiDeputy.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objMultiDeputy.setNusersitecode(getInteger(arg0, "nusersitecode", arg1));
		objMultiDeputy.setSdeputyid(getString(arg0, "sdeputyid", arg1));
		objMultiDeputy.setSdeputyname(getString(arg0, "sdeputyname", arg1));
		objMultiDeputy.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objMultiDeputy.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objMultiDeputy.setNusercode(getInteger(arg0, "nusercode", arg1));
		objMultiDeputy.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objMultiDeputy.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objMultiDeputy;
	}
}
