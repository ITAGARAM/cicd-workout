package com.agaramtech.qualis.registration.model;

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

/**
 * This class is used to map the fields of 'registrationdecisionhistory' table of the Database.
 */
@Entity
@Table(name = "registrationdecisionhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationDecisionHistory extends CustomizedResultsetRowMapper<RegistrationDecisionHistory> implements Serializable, RowMapper<RegistrationDecisionHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregdecisionhistorycode")
	private int nregdecisionhistorycode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ndecisionstatus", nullable = false)
	private int ndecisionstatus;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

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

	@Column(name = "scomments", length = 255, nullable = false)
	private String scomments;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
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
	public RegistrationDecisionHistory mapRow(ResultSet arg0,int arg1) throws SQLException {
		
		final RegistrationDecisionHistory objRegistrationDecisionHistory = new RegistrationDecisionHistory();
		
		objRegistrationDecisionHistory.setNregdecisionhistorycode(getInteger(arg0,"nregdecisionhistorycode",arg1));
		objRegistrationDecisionHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationDecisionHistory.setNdecisionstatus(getInteger(arg0,"ndecisionstatus",arg1));
		objRegistrationDecisionHistory.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objRegistrationDecisionHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objRegistrationDecisionHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objRegistrationDecisionHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objRegistrationDecisionHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objRegistrationDecisionHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objRegistrationDecisionHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationDecisionHistory.setSarno(getString(arg0,"sarno",arg1));
		objRegistrationDecisionHistory.setUsername(getString(arg0,"username",arg1));
		objRegistrationDecisionHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objRegistrationDecisionHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objRegistrationDecisionHistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objRegistrationDecisionHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objRegistrationDecisionHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objRegistrationDecisionHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objRegistrationDecisionHistory;
	}

}
