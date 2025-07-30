package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
 * This class is used to map the fields of 'releaseparameter' table of the Database.
 */
@Entity
@Table(name = "releaseparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseParameter extends CustomizedResultsetRowMapper<ReleaseParameter> implements Serializable, RowMapper<ReleaseParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransactionresultcode")
	private int ntransactionresultcode;
	
	@Column(name = "npreregno",nullable=false)	
	private int npreregno;
	
	@Column(name = "ntransactiontestcode",nullable=false)
	private int ntransactiontestcode;
	
	@Column(name = "ntestgrouptestparametercode",nullable=false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "ntestparametercode",nullable=false)
	private int ntestparametercode;
	
	@Column(name = "nparametertypecode",nullable=false)
	private short nparametertypecode;

	private transient short nroundingdigits;
	
	@Column(name = "nresultmandatory",nullable=false)
	private short nresultmandatory;
	
	@Column(name = "nreportmandatory",nullable=false)
	private short nreportmandatory;
	
	@Column(name = "ntestgrouptestformulacode",nullable=false)
	private int ntestgrouptestformulacode;
	
	@Column(name = "nunitcode",nullable=false)
	private int nunitcode;
	
	@ColumnDefault("-1")
	@Column(name = "ngradecode",nullable=false)
	private short ngradecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ntransactionstatus",nullable=false)
	private short ntransactionstatus;
	
	@ColumnDefault("4")
	@Column(name = "nenforcestatus",nullable=false)
	private short nenforcestatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nenforceresult",nullable=false)
	private short nenforceresult=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")	
	@Column(name = "ncalculatedresult",nullable=false)
	private short ncalculatedresult=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nenteredby")
	private int nenteredby=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nenteredrole",nullable=false)
	private int nenteredrole=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ndeputyenteredby",nullable=false)
	private int ndeputyenteredby=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ndeputyenteredrole",nullable=false)
	private int ndeputyenteredrole=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode",nullable=false)
	private short nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nattachmenttypecode",nullable=false)
	private short nattachmenttypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
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
	private transient int nfilesize;
	
	@Transient
	private transient String ssystemfilename;	
	
	@Transient
	private transient String smina;
	
	@Transient
	private transient String sminb;
	
	@Transient
	private transient String smaxb;
	
	@Transient
	private transient String smaxa;
	
	@Transient
	private transient String sminlod;
	
	@Transient
	private transient String smaxlod;
	
	@Transient
	private transient String sminloq;
	
	@Transient
	private transient String smaxloq;
	
	@Transient
	private transient String sdisregard;
	
	@Transient
	private transient String sresultvalue;
	
	@Transient
	private transient Instant dentereddate;
	
	@Transient
	private transient int ntestgrouptestcode;
	
	@Transient
	private transient int ngradecodenew;
	
	@Transient
	private transient int nchecklistversioncode;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sparametersynonym;
	
	@Transient	
	private transient String sdisplaystatus;
	
	@Transient
	private transient String sunitname;
	
	@Transient
	private transient String sgradename;
	
	@Transient
	private transient String senforceresult;
	
	@Transient
	private transient String senforcestatus;
	
	@Transient
	private transient String sentereddate;
	
	@Transient
	private transient String enteredby;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String schecklistversionname;
	
	@Transient
	private transient String schecklistname;
	
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient String senforcestatuscomment;
	
	@Transient
	private transient String senforceresultcomment;
	
	@Transient
	private transient String sresultcomment;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String sparametertypename;
	
	@Transient
	private transient int ntransactionsamplecode;
	
	@Transient
	private transient String sage;
	
	@Transient
	private transient String sgendername;
	
	@Transient
	private transient String spredefinedcomments;
	
	@Transient
	private transient int ncoaparentcode;
	
	@Transient
	private transient String jsonstring;
	
	@Transient
	private transient String sresultaccuracyname;
	
	@Transient
	private transient int dentereddatetimezonecode;
	
	@Transient
	private transient int noffsetdentereddate;
	
	@Transient
	private transient Date entereddate;
	
	@Transient
	private transient int nresultaccuracycode;
	
	
	@Override
	public ReleaseParameter mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ReleaseParameter objReleaseParameter = new ReleaseParameter();

		objReleaseParameter.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));
		objReleaseParameter.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objReleaseParameter.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objReleaseParameter.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objReleaseParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objReleaseParameter.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objReleaseParameter.setNroundingdigits(getShort(arg0,"nroundingdigits",arg1));
		objReleaseParameter.setNresultmandatory(getShort(arg0,"nresultmandatory",arg1));
		objReleaseParameter.setNreportmandatory(getShort(arg0,"nreportmandatory",arg1));
		objReleaseParameter.setNtestgrouptestformulacode(getInteger(arg0,"ntestgrouptestformulacode",arg1));
		objReleaseParameter.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objReleaseParameter.setNgradecode(getShort(arg0,"ngradecode",arg1));
		objReleaseParameter.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objReleaseParameter.setSfinal(getString(arg0,"sfinal",arg1));
		objReleaseParameter.setSresult(getString(arg0,"sresult",arg1));
		objReleaseParameter.setNenforcestatus(getShort(arg0,"nenforcestatus",arg1));
		objReleaseParameter.setNenforceresult(getShort(arg0,"nenforceresult",arg1));
		objReleaseParameter.setSmina(getString(arg0,"smina",arg1));
		objReleaseParameter.setSminb(getString(arg0,"sminb",arg1));
		objReleaseParameter.setSmaxb(getString(arg0,"smaxb",arg1));
		objReleaseParameter.setSmaxa(getString(arg0,"smaxa",arg1));
		objReleaseParameter.setSminlod(getString(arg0,"sminlod",arg1));
		objReleaseParameter.setSmaxlod(getString(arg0,"smaxlod",arg1));
		objReleaseParameter.setSminloq(getString(arg0,"sminloq",arg1));
		objReleaseParameter.setSmaxloq(getString(arg0,"smaxloq",arg1));
		objReleaseParameter.setSdisregard(getString(arg0,"sdisregard",arg1));
		objReleaseParameter.setSresultvalue(getString(arg0,"sresultvalue",arg1));
		objReleaseParameter.setDentereddate(getInstant(arg0,"dentereddate",arg1));
		objReleaseParameter.setNenteredby(getInteger(arg0,"nenteredby",arg1));
		objReleaseParameter.setNenteredrole(getInteger(arg0,"nenteredrole",arg1));
		objReleaseParameter.setNdeputyenteredby(getInteger(arg0,"ndeputyenteredby",arg1));
		objReleaseParameter.setNdeputyenteredrole(getInteger(arg0,"ndeputyenteredrole",arg1));
		objReleaseParameter.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objReleaseParameter.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objReleaseParameter.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objReleaseParameter.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objReleaseParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objReleaseParameter.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objReleaseParameter.setNgradecodenew(getInteger(arg0,"ngradecodenew",arg1));
		objReleaseParameter.setNgradecodenew(getInteger(arg0,"nchecklistversioncode",arg1));
		objReleaseParameter.setSarno(getString(arg0,"sarno",arg1));
		objReleaseParameter.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		objReleaseParameter.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objReleaseParameter.setSparametersynonym(getString(arg0,"sparametersynonym",arg1));
		objReleaseParameter.setSunitname(getString(arg0,"sunitname",arg1));
		objReleaseParameter.setSgradename(getString(arg0,"sgradename",arg1));
		objReleaseParameter.setSenforceresult(getString(arg0,"senforceresult",arg1));
		objReleaseParameter.setSenforcestatus(getString(arg0,"senforcestatus",arg1));
		objReleaseParameter.setEnteredby(getString(arg0,"enteredby",arg1));
		objReleaseParameter.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objReleaseParameter.setSchecklistversionname(getString(arg0,"schecklistversionname",arg1));
		objReleaseParameter.setSentereddate(getString(arg0,"sentereddate",arg1));
		objReleaseParameter.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objReleaseParameter.setSchecklistname(getString(arg0,"schecklistname",arg1));
		objReleaseParameter.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objReleaseParameter.setScolorhexcode(getString(arg0,"scolorhexcode",arg1));
		objReleaseParameter.setSresultcomment(getString(arg0,"sresultcomment",arg1));
		objReleaseParameter.setSenforceresultcomment(getString(arg0,"senforceresultcomment",arg1));
		objReleaseParameter.setSenforcestatuscomment(getString(arg0,"senforcestatuscomment",arg1));
		objReleaseParameter.setSusername(getString(arg0,"susername",arg1));
		objReleaseParameter.setSparametertypename(getString(arg0,"sparametertypename",arg1));
		objReleaseParameter.setNcalculatedresult(getShort(arg0,"ncalculatedresult",arg1));
		objReleaseParameter.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objReleaseParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		objReleaseParameter.setSage(getString(arg0,"sage",arg1));
		objReleaseParameter.setSgendername(getString(arg0,"sgendername",arg1));
		objReleaseParameter.setSpredefinedcomments(getString(arg0,"spredefinedcomments", arg1));
		objReleaseParameter.setNcoaparentcode(getInteger(arg0,"ncoaparentcode",arg1));
		objReleaseParameter.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objReleaseParameter.setJsonstring(getString(arg0,"jsonstring", arg1));
		objReleaseParameter.setSresultaccuracyname(getString(arg0,"sresultaccuracyname", arg1));
		objReleaseParameter.setDentereddatetimezonecode(getInteger(arg0,"dentereddatetimezonecode",arg1));
		objReleaseParameter.setNoffsetdentereddate(getInteger(arg0,"noffsetdentereddate",arg1));
		objReleaseParameter.setEntereddate(getDate(arg0,"entereddate",arg1));
		objReleaseParameter.setNresultaccuracycode(getInteger(arg0,"nresultaccuracycode",arg1));
		
		return objReleaseParameter;
	}

}
