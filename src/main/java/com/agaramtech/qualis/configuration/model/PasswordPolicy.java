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

@Entity
@Table(name = "passwordpolicy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PasswordPolicy extends CustomizedResultsetRowMapper<PasswordPolicy> implements Serializable, RowMapper<PasswordPolicy> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npolicycode")
	private int npolicycode;

	@Column(name = "spolicyname", length = 100, nullable = false)
	private String spolicyname;

	@Column(name = "nminnoofnumberchar", nullable = false)
	private short nminnoofnumberchar;

	@Column(name = "nminnooflowerchar", nullable = false)
	private short nminnooflowerchar;

	@Column(name = "nminnoofupperchar", nullable = false)
	private short nminnoofupperchar;

	@Column(name = "nminnoofspecialchar", nullable = false)
	private short nminnoofspecialchar;

	@Column(name = "nminpasslength", nullable = false)
	private short nminpasslength;

	@Column(name = "nmaxpasslength", nullable = false)
	private short nmaxpasslength;

	@Column(name = "nnooffailedattempt", nullable = false)
	private short nnooffailedattempt;

	@ColumnDefault("4")
	@Column(name = "nexpirypolicyrequired", nullable = false)
	private short nexpirypolicyrequired = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "nexpirypolicy", nullable = false)
	private short nexpirypolicy = (short) Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "nremainderdays", nullable = false)
	private short nremainderdays = (short) Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@Column(name = "scomments", length = 255)
	private String scomments;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient int nuserrolepolicycode;

	@Transient
	private transient String stransstatus;

	@Transient
	private transient short ntransactionstatus;

	@Transient
	private transient short numrnnooffailedattempt;

	@Transient
	private transient int nuserrolecode;

	@Transient
	private transient String sexpirystatus;

	@Transient
	private transient String suserrolename;

	@Override
	public PasswordPolicy mapRow(ResultSet arg0, int arg1) throws SQLException {
		 final PasswordPolicy objPasswordPolicy = new PasswordPolicy();
		objPasswordPolicy.setNnooffailedattempt(getShort(arg0, "nnooffailedattempt", arg1));
		objPasswordPolicy.setNmaxpasslength(getShort(arg0, "nmaxpasslength", arg1));
		objPasswordPolicy.setNuserrolepolicycode(getShort(arg0, "nuserrolepolicycode", arg1));
		objPasswordPolicy.setNminnoofnumberchar(getShort(arg0, "nminnoofnumberchar", arg1));
		objPasswordPolicy.setNminnoofupperchar(getShort(arg0, "nminnoofupperchar", arg1));
		objPasswordPolicy.setNstatus(getShort(arg0, "nstatus", arg1));
		objPasswordPolicy.setNminnoofspecialchar(getShort(arg0, "nminnoofspecialchar", arg1));
		objPasswordPolicy.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objPasswordPolicy.setNexpirypolicy(getShort(arg0, "nexpirypolicy", arg1));
		objPasswordPolicy.setStransstatus(getString(arg0, "stransstatus", arg1));
		objPasswordPolicy.setSpolicyname(StringEscapeUtils.unescapeJava(getString(arg0, "spolicyname", arg1)));
		objPasswordPolicy.setNminpasslength(getShort(arg0, "nminpasslength", arg1));
		objPasswordPolicy.setNremainderdays(getShort(arg0, "nremainderdays", arg1));
		objPasswordPolicy.setNpolicycode(getInteger(arg0, "npolicycode", arg1));
		objPasswordPolicy.setNminnooflowerchar(getShort(arg0, "nminnooflowerchar", arg1));
		objPasswordPolicy.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objPasswordPolicy.setNexpirypolicyrequired(getShort(arg0, "nexpirypolicyrequired", arg1));
		objPasswordPolicy.setNumrnnooffailedattempt(getShort(arg0, "numrnnooffailedattempt", arg1));
		objPasswordPolicy.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objPasswordPolicy.setSexpirystatus(getString(arg0, "sexpirystatus", arg1));
		objPasswordPolicy.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objPasswordPolicy.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objPasswordPolicy.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objPasswordPolicy;
	}

}
