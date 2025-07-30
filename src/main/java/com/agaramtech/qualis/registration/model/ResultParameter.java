package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
 * This class is used to map the fields of 'resultparameter' table of the Database.
 */
@Entity
@Table(name = "resultparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultParameter extends CustomizedResultsetRowMapper<ResultParameter> implements Serializable, RowMapper<ResultParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransactionresultcode")
	private int ntransactionresultcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;

	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;

	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;

	@Column(name = "nresultmandatory", nullable = false)
	private short nresultmandatory;

	@Column(name = "nreportmandatory", nullable = false)
	private short nreportmandatory;

	@Column(name = "ntestgrouptestformulacode", nullable = false)
	private int ntestgrouptestformulacode;

	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;

	@Column(name = "ngradecode", nullable = false)
	@ColumnDefault("-1")
	private short ngradecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "ncalculatedresult", nullable = false)
	@ColumnDefault("4")
	private short ncalculatedresult = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nenteredby", nullable = false)
	@ColumnDefault("-1")
	private int nenteredby = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nenteredrole", nullable = false)
	@ColumnDefault("-1")
	private int nenteredrole = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ndeputyenteredby", nullable = false)
	@ColumnDefault("-1")
	private int ndeputyenteredby = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ndeputyenteredrole", nullable = false)
	@ColumnDefault("-1")
	private int ndeputyenteredrole = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Column(name = "nenforcestatus", nullable = false)
	@ColumnDefault("4")
	private short nenforcestatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "nenforceresult", nullable = false)
	@ColumnDefault("4")
	private short nenforceresult = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "nlinkcode", nullable = false)
	@ColumnDefault("-1")
	private short nlinkcode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	@ColumnDefault("-1")
	private short nattachmenttypecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
	@Lob@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> jsondata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		
	@Transient
	private transient String sfinal;
	
	@Transient
	private transient String sresult;
	
	@Transient
	private transient String jsonstring;
	
	@Transient
	private transient int nroundingdigits;
	
	@Transient
	private transient int ntransactionsamplecode;
	
	@Transient
	private transient int nisaverageneed;
	
	@Transient
	private transient String spredefinedcomments;
	
	@Transient
	private transient String senforceresultcomment;
	
	@Transient
	private transient String sparametername;
	
	@Transient
	private transient int nroundingdigit;
	
	@Transient
	private transient int nispredefinedformula;
	
	@Transient
	private transient int npredefinedformulacode;
	
	@Transient
	private transient int ntestgrouptestcode;
	
	@Transient
	private transient int ngradecodenew;
	
	@Transient
	private transient int ntestrepeatno;
	
	@Transient
	private transient int ntestretestno;
	
	@Transient
	private transient int nchecklistversioncode;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sparametersynonym;
	
	@Transient
	private transient String sparametertypename;
	
	@Transient
	private transient String sgradename;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String sretestrepeatcount;
	
	@Transient
	private transient String sunitname;
	
	@Transient
	private transient String schecklistname;
	
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient String sspecdesc;
	
	@Transient
	private transient String senforcestatus;
	
	@Transient
	private transient String sresultmandatory;
	
	@Transient
	private transient String senteredby;
	
	@Transient
	private transient String sresultcomment;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sentereddate;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sresultvalue;
	
	@Transient
	private transient String sminb;
	
	@Transient
	private transient String smaxb;
	
	@Transient
	private transient String smina;
	
	@Transient
	private transient String smaxa;
	
	@Transient
	private transient Date dentereddate;
	
	@Transient
	private transient int dentereddatetimezonecode;
	
	@Transient
	private transient int noffsetdentereddate;
	
	@Transient
	private transient String sresultaccuracyname;
	
	@Transient
	private transient int nresultaccuracycode;
	
	@Transient
	private transient String ssystemfilename;  //Added by sonia on 2nd Sept 2024 for JIRA ID:ALPD-4785
	
	@Transient
	private transient int nfilesize;  //Added by sonia on 2nd Sept 2024 for JIRA ID:ALPD-4785
	

	@Override
	public ResultParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ResultParameter resultParameter = new ResultParameter();
		
		resultParameter.setNtransactionresultcode(getInteger(arg0, "ntransactionresultcode", arg1));
		resultParameter.setNpreregno(getInteger(arg0, "npreregno", arg1));
		resultParameter.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		resultParameter.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode", arg1));
		resultParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode", arg1));
		resultParameter.setNparametertypecode(getShort(arg0,"nparametertypecode", arg1));
		resultParameter.setNresultmandatory(getShort(arg0,"nresultmandatory", arg1));
		resultParameter.setNreportmandatory(getShort(arg0,"nreportmandatory", arg1));
		resultParameter.setNunitcode(getInteger(arg0,"nunitcode", arg1));
		resultParameter.setNgradecode(getShort(arg0,"ngradecode", arg1));
		resultParameter.setNtransactionstatus(getShort(arg0,"ntransactionstatus", arg1));
		resultParameter.setNenteredby(getInteger(arg0,"nenteredby", arg1));
		resultParameter.setNenteredrole(getInteger(arg0,"nenteredrole", arg1));
		resultParameter.setNdeputyenteredby(getInteger(arg0,"ndeputyenteredby", arg1));
		resultParameter.setNdeputyenteredrole(getInteger(arg0,"ndeputyenteredrole", arg1));
		resultParameter.setNstatus(getShort(arg0,"nstatus", arg1));
		resultParameter.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode", arg1));
		resultParameter.setNgradecodenew(getInteger(arg0,"ngradecodenew", arg1));
		resultParameter.setNtestrepeatno(getInteger(arg0,"ntestrepeatno", arg1));
		resultParameter.setNtestretestno(getInteger(arg0,"ntestretestno", arg1));
		resultParameter.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode", arg1));
		resultParameter.setStestsynonym(getString(arg0,"stestsynonym", arg1));
		resultParameter.setSparametersynonym(getString(arg0,"sparametersynonym", arg1));
		resultParameter.setSparametertypename(getString(arg0,"sparametertypename", arg1));
		resultParameter.setSgradename(getString(arg0,"sgradename", arg1));
		resultParameter.setSarno(getString(arg0,"sarno", arg1));
		resultParameter.setSsamplearno(getString(arg0,"ssamplearno", arg1));
		resultParameter.setSretestrepeatcount(getString(arg0,"sretestrepeatcount", arg1));
		resultParameter.setNtestgrouptestformulacode(getInteger(arg0,"ntestgrouptestformulacode", arg1));
		resultParameter.setSunitname(getString(arg0,"sunitname", arg1));
		resultParameter.setStransdisplaystatus(getString(arg0,"stransdisplaystatus", arg1));
		resultParameter.setSchecklistname(getString(arg0,"schecklistname", arg1));
		resultParameter.setScolorhexcode(getString(arg0,"scolorhexcode", arg1));
		resultParameter.setSspecdesc(getString(arg0,"sspecdesc", arg1));
		resultParameter.setSenforcestatus(getString(arg0,"senforcestatus", arg1));
		resultParameter.setSresultmandatory(getString(arg0,"sresultmandatory", arg1));
		resultParameter.setSenteredby(getString(arg0,"senteredby", arg1));
		resultParameter.setSresultcomment(getString(arg0,"sresultcomment", arg1));
		resultParameter.setSuserrolename(getString(arg0,"suserrolename", arg1));
		resultParameter.setSentereddate(getString(arg0,"sentereddate", arg1));
		resultParameter.setNcalculatedresult(getShort(arg0,"calculatedresult", arg1));
		resultParameter.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		resultParameter.setSresult(getString(arg0,"sresult", arg1));
		resultParameter.setSfinal(getString(arg0,"sfinal", arg1));
		resultParameter.setSresultvalue(getString(arg0,"sresultvalue", arg1));
		resultParameter.setJsonstring(getString(arg0,"jsonstring", arg1));
		resultParameter.setNroundingdigits(getInteger(arg0,"nroundingdigits", arg1));
		resultParameter.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode", arg1));
		resultParameter.setSmaxb(getString(arg0,"smaxb", arg1));
		resultParameter.setSminb(getString(arg0,"sminb", arg1));
		resultParameter.setSmaxa(getString(arg0,"smaxa", arg1));
		resultParameter.setSmina(getString(arg0,"smina", arg1));
		resultParameter.setNisaverageneed(getInteger(arg0, "nisaverageneed", arg1));
		resultParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		resultParameter.setSsystemfilename(getString(arg0,"ssystemfilename", arg1));
		resultParameter.setNfilesize(getInteger(arg0,"nfilesize", arg1));
		resultParameter.setSpredefinedcomments(getString(arg0,"spredefinedcomments", arg1));
		resultParameter.setSenforceresultcomment(getString(arg0,"senforceresultcomment", arg1));
		resultParameter.setSparametername(getString(arg0,"sparametername", arg1));
		resultParameter.setNroundingdigits(getInteger(arg0,"nroundingdigit", arg1));
		resultParameter.setDentereddate(getDate(arg0,"dentereddate",arg1));
		resultParameter.setDentereddatetimezonecode(getInteger(arg0,"dentereddatetimezonecode",arg1));
		resultParameter.setNoffsetdentereddate(getInteger(arg0,"noffsetdentereddate",arg1));
		resultParameter.setSresultaccuracyname(getString(arg0,"sresultaccuracyname", arg1));
		resultParameter.setNenforcestatus(getShort(arg0,"nenforcestatus", arg1));
		resultParameter.setNenforceresult(getShort(arg0,"nenforceresult", arg1));
		resultParameter.setNispredefinedformula(getInteger(arg0,"nispredefinedformula", arg1));
		resultParameter.setNpredefinedformulacode(getInteger(arg0,"npredefinedformulacode", arg1));
		resultParameter.setNresultaccuracycode(getInteger(arg0,"nresultaccuracycode", arg1));
		
		return resultParameter;
		
	}

}
