package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'resultchangehistory' table of the Database.
 */
@Entity
@Table(name = "resultchangehistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultChangeHistory extends CustomizedResultsetRowMapper<ResultChangeHistory> implements Serializable, RowMapper<ResultChangeHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nresultchangehistorycode")
	private int nresultchangehistorycode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "ntransactionresultcode", nullable = false)
	private int ntransactionresultcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ngradecode", nullable = false)
	@ColumnDefault("-1")
	private int ngradecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;

	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nenforceresult", nullable = false)
	@ColumnDefault("4")
	private short nenforceresult =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nenforcestatus", nullable = false)
	@ColumnDefault("4")
	private short nenforcestatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nenteredby", nullable = false)
	private int nenteredby;

	@Column(name = "nenteredrole", nullable = false)
	private int nenteredrole;

	@Column(name = "ndeputyenteredby", nullable = false)
	@ColumnDefault("-1")
	private int ndeputyenteredby =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ndeputyenteredrole", nullable = false)
	@ColumnDefault("-1")
	private int ndeputyenteredrole =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	

	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sparametersynonym;
	
	@Transient
	private transient String username;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sentereddate;
	
	@Transient
	private transient String sminb;
	
	@Transient
	private transient String smaxb;
	
	@Transient
	private transient String sformname;
	
	@Transient
	private transient String sresult;
	
	@Transient
	private transient String sfinal;
	
	@Transient
	private transient String sresultaccuracyname;

	
	@Override
	public ResultChangeHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ResultChangeHistory objResultChangeHistory = new ResultChangeHistory();

		objResultChangeHistory.setNresultchangehistorycode(getInteger(arg0, "nresultchangehistorycode", arg1));
		objResultChangeHistory.setNtransactionresultcode(getInteger(arg0, "ntransactionresultcode", arg1));
		objResultChangeHistory.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objResultChangeHistory.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objResultChangeHistory.setNgradecode(getInteger(arg0, "nchecklistqbcode", arg1));
		objResultChangeHistory.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objResultChangeHistory.setNparametertypecode(getShort(arg0, "nparametertypecode", arg1));
		objResultChangeHistory.setSresult(getString(arg0, "sresult", arg1));
		objResultChangeHistory.setSfinal(getString(arg0, "sfinal", arg1));
		objResultChangeHistory.setNenforceresult(getShort(arg0, "nenforceresult", arg1));
		objResultChangeHistory.setNenforcestatus(getShort(arg0, "nenforcestatus", arg1));
		objResultChangeHistory.setNenteredby(getInteger(arg0, "nenteredby", arg1));
		objResultChangeHistory.setNenteredrole(getInteger(arg0, "nenteredrole", arg1));
		objResultChangeHistory.setNdeputyenteredby(getInteger(arg0, "ndeputyenteredby", arg1));
		objResultChangeHistory.setNdeputyenteredrole(getInteger(arg0, "ndeputyenteredrole", arg1));
		objResultChangeHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objResultChangeHistory.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objResultChangeHistory.setSparametersynonym(getString(arg0, "sparametersynonym", arg1));
		objResultChangeHistory.setSarno(getString(arg0, "sarno", arg1));
		objResultChangeHistory.setUsername(getString(arg0, "username", arg1));
		objResultChangeHistory.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objResultChangeHistory.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objResultChangeHistory.setSentereddate(getString(arg0, "sentereddate", arg1));
		objResultChangeHistory.setSminb(getString(arg0, "sminb", arg1));
		objResultChangeHistory.setSmaxb(getString(arg0, "smaxb", arg1));
		objResultChangeHistory.setNformcode(getShort(arg0, "nformcode", arg1));
		objResultChangeHistory.setSformname(getString(arg0, "sformname", arg1));
		objResultChangeHistory.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objResultChangeHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objResultChangeHistory.setSresultaccuracyname(getString(arg0, "sresultaccuracyname", arg1));
		
		return objResultChangeHistory;
	}

}
