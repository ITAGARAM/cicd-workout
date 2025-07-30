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
 * This class is used to map the fields of 'registrationtesthistory' table of the Database.
 */
@Entity
@Table(name = "registrationtesthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationTestHistory extends CustomizedResultsetRowMapper<RegistrationTestHistory> implements Serializable,RowMapper<RegistrationTestHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntesthistorycode")
	private int ntesthistorycode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

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

	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;

	@Column(name = "nsampleapprovalhistorycode", nullable = false)
	@ColumnDefault("-1")
	private int nsampleapprovalhistorycode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
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
	private transient String stransactiondate;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sfinal;
	
	@Transient
	private transient int nsectioncode;
	
	@Transient
	private transient int ntestgrouptestcode;
	
	@Transient
	private transient Date dreceiveddate;
	
	@Transient
	private transient Date dinitiatedate;
	
	@Transient
	private transient Date dcompletedate;
	
	@Transient
	private transient String scolorname;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	/** transients added for audit trail purpose*/
	
	@Transient
	private transient String username;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String analysername;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String ssectionname;


	@Override
	public RegistrationTestHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationTestHistory objRegistrationTestHistory = new RegistrationTestHistory();
	
		objRegistrationTestHistory.setNtesthistorycode(getInteger(arg0,"ntesthistorycode",arg1));
		objRegistrationTestHistory.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objRegistrationTestHistory.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objRegistrationTestHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationTestHistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistrationTestHistory.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objRegistrationTestHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objRegistrationTestHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objRegistrationTestHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objRegistrationTestHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objRegistrationTestHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objRegistrationTestHistory.setNformcode(getInteger(arg0,"nformcode",arg1));
		objRegistrationTestHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationTestHistory.setNsampleapprovalhistorycode(getInteger(arg0,"nsampleapprovalhistorycode",arg1));
		objRegistrationTestHistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objRegistrationTestHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objRegistrationTestHistory.setSfinal(getString(arg0,"sfinal",arg1));
		objRegistrationTestHistory.setDreceiveddate(getDate(arg0,"dreceiveddate",arg1));
		objRegistrationTestHistory.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objRegistrationTestHistory.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objRegistrationTestHistory.setScolorname(getString(arg0,"scolorname",arg1));
		objRegistrationTestHistory.setDinitiatedate(getDate(arg0,"dinitiatedate",arg1));
		objRegistrationTestHistory.setDcompletedate(getDate(arg0,"dcompletedate",arg1));
		objRegistrationTestHistory.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objRegistrationTestHistory.setSarno(getString(arg0,"sarno",arg1));
		objRegistrationTestHistory.setUsername(getString(arg0,"username",arg1));
		objRegistrationTestHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objRegistrationTestHistory.setSusername(getString(arg0,"susername",arg1));
		objRegistrationTestHistory.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		objRegistrationTestHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objRegistrationTestHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objRegistrationTestHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationTestHistory.setAnalysername(getString(arg0,"analysername",arg1));
		objRegistrationTestHistory.setSsectionname(getString(arg0, "ssectionname", arg1));
		
		return objRegistrationTestHistory;
	}

}
