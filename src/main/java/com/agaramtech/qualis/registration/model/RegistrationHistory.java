package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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

/**
 * This class is used to map the fields of 'registrationhistory' table of the Database.
 */
@Entity
@Table(name = "registrationhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationHistory extends CustomizedResultsetRowMapper<RegistrationHistory> implements Serializable, RowMapper<RegistrationHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreghistorycode")
	private int nreghistorycode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "scomments", length = 255)
	private String scomments="";
	
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String username;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String stransactiondate;

	@Override
	public RegistrationHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationHistory objRegistrationHistory = new RegistrationHistory();
		
		objRegistrationHistory.setNreghistorycode(getInteger(arg0,"nreghistorycode",arg1));
		objRegistrationHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationHistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistrationHistory.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objRegistrationHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objRegistrationHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objRegistrationHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objRegistrationHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objRegistrationHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objRegistrationHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationHistory.setSarno(getString(arg0,"sarno",arg1));
		objRegistrationHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objRegistrationHistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objRegistrationHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objRegistrationHistory.setUsername(getString(arg0,"username",arg1));
		objRegistrationHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objRegistrationHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objRegistrationHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objRegistrationHistory;
	}
}
