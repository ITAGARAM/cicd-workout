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
 * This class is used to map the fields of 'registrationsamplehistory' table of the Database.
 */
@Entity
@Table(name = "registrationsamplehistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSampleHistory extends CustomizedResultsetRowMapper<RegistrationSampleHistory> implements Serializable,RowMapper<RegistrationSampleHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplehistorycode")
	private int nsamplehistorycode;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

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
	
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;

	@Column(name = "scomments", length = 255)
	private String scomments="";
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient boolean isneedsample;
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sarno;

	@Override
	public RegistrationSampleHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final RegistrationSampleHistory objRegistrationSampleHistory = new RegistrationSampleHistory();
	
		objRegistrationSampleHistory.setNsamplehistorycode(getInteger(arg0,"nsamplehistorycode",arg1));
		objRegistrationSampleHistory.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objRegistrationSampleHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationSampleHistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistrationSampleHistory.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objRegistrationSampleHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objRegistrationSampleHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objRegistrationSampleHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objRegistrationSampleHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objRegistrationSampleHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objRegistrationSampleHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationSampleHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objRegistrationSampleHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objRegistrationSampleHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objRegistrationSampleHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationSampleHistory.setIsneedsample(getBoolean(arg0,"isneedsample",arg1));
		objRegistrationSampleHistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objRegistrationSampleHistory.setSusername(getString(arg0,"susername",arg1));
		objRegistrationSampleHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objRegistrationSampleHistory.setSarno(getString(arg0,"sarno",arg1));
		
		return objRegistrationSampleHistory;
	}

}
