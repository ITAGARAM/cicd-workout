package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
 * This class is used to map the fields of 'approvalparameter' table of the Database.
 */
@Entity
@Table(name = "approvalparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalParameter extends CustomizedResultsetRowMapper<ApprovalParameter> implements Serializable, RowMapper<ApprovalParameter> {

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
	private short ngradecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ntransactionstatus",nullable=false)	
	private short ntransactionstatus;
	
	@ColumnDefault("4")
	@Column(name = "nenforcestatus",nullable=false)
	private short nenforcestatus =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nenforceresult",nullable=false)
	private short nenforceresult =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "ncalculatedresult",nullable=false)
	private short ncalculatedresult =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nenteredby")
	private int nenteredby =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nenteredrole",nullable=false)
	private int nenteredrole =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ndeputyenteredby",nullable=false)
	private int ndeputyenteredby =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ndeputyenteredrole",nullable=false)
	private int ndeputyenteredrole =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode",nullable=false)
	private short nlinkcode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nattachmenttypecode",nullable=false)
	private short nattachmenttypecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Lob@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> jsondata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient int nfilesize;	
	
	@Transient
	private transient String ssystemfilename;
	
	@Transient
	private transient String sfinal;
	
	@Transient
	private transient String sresult;
	
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
	private transient short nroundingdigits;
	
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
	private transient String sregdate;
	
	@Transient
	private transient String scompletedate;
	
	@Transient
	private transient String sreportno;
	
	@Transient
	private transient int  nexternalordercode;
	
	@Transient
	private transient int  nexternalordersamplecode;
	
	@Transient
	private transient int nexternalordertestcode;
	
	@Transient
	private transient int  ntestcode;

	@Override
	public ApprovalParameter mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ApprovalParameter objApprovalParameter = new ApprovalParameter();

		objApprovalParameter.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));
		objApprovalParameter.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objApprovalParameter.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objApprovalParameter.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objApprovalParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objApprovalParameter.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objApprovalParameter.setNroundingdigits(getShort(arg0,"nroundingdigits",arg1));
		objApprovalParameter.setNresultmandatory(getShort(arg0,"nresultmandatory",arg1));
		objApprovalParameter.setNreportmandatory(getShort(arg0,"nreportmandatory",arg1));
		objApprovalParameter.setNtestgrouptestformulacode(getInteger(arg0,"ntestgrouptestformulacode",arg1));
		objApprovalParameter.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objApprovalParameter.setNgradecode(getShort(arg0,"ngradecode",arg1));
		objApprovalParameter.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objApprovalParameter.setSfinal(getString(arg0,"sfinal",arg1));
		objApprovalParameter.setSresult(getString(arg0,"sresult",arg1));
		objApprovalParameter.setNenforcestatus(getShort(arg0,"nenforcestatus",arg1));
		objApprovalParameter.setNenforceresult(getShort(arg0,"nenforceresult",arg1));
		objApprovalParameter.setSmina(getString(arg0,"smina",arg1));
		objApprovalParameter.setSminb(getString(arg0,"sminb",arg1));
		objApprovalParameter.setSmaxb(getString(arg0,"smaxb",arg1));
		objApprovalParameter.setSmaxa(getString(arg0,"smaxa",arg1));
		objApprovalParameter.setSminlod(getString(arg0,"sminlod",arg1));
		objApprovalParameter.setSmaxlod(getString(arg0,"smaxlod",arg1));
		objApprovalParameter.setSminloq(getString(arg0,"sminloq",arg1));
		objApprovalParameter.setSmaxloq(getString(arg0,"smaxloq",arg1));
		objApprovalParameter.setSdisregard(getString(arg0,"sdisregard",arg1));
		objApprovalParameter.setSresultvalue(getString(arg0,"sresultvalue",arg1));
		objApprovalParameter.setDentereddate(getInstant(arg0,"dentereddate",arg1));
		objApprovalParameter.setNenteredby(getInteger(arg0,"nenteredby",arg1));
		objApprovalParameter.setNenteredrole(getInteger(arg0,"nenteredrole",arg1));
		objApprovalParameter.setNdeputyenteredby(getInteger(arg0,"ndeputyenteredby",arg1));
		objApprovalParameter.setNdeputyenteredrole(getInteger(arg0,"ndeputyenteredrole",arg1));
		objApprovalParameter.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objApprovalParameter.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objApprovalParameter.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objApprovalParameter.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objApprovalParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objApprovalParameter.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objApprovalParameter.setNgradecodenew(getInteger(arg0,"ngradecodenew",arg1));
		objApprovalParameter.setNgradecodenew(getInteger(arg0,"nchecklistversioncode",arg1));
		objApprovalParameter.setSarno(getString(arg0,"sarno",arg1));
		objApprovalParameter.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		objApprovalParameter.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objApprovalParameter.setSparametersynonym(getString(arg0,"sparametersynonym",arg1));
		objApprovalParameter.setSunitname(getString(arg0,"sunitname",arg1));
		objApprovalParameter.setSgradename(getString(arg0,"sgradename",arg1));
		objApprovalParameter.setSenforceresult(getString(arg0,"senforceresult",arg1));
		objApprovalParameter.setSenforcestatus(getString(arg0,"senforcestatus",arg1));
		objApprovalParameter.setEnteredby(getString(arg0,"enteredby",arg1));
		objApprovalParameter.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objApprovalParameter.setSchecklistversionname(getString(arg0,"schecklistversionname",arg1));
		objApprovalParameter.setSentereddate(getString(arg0,"sentereddate",arg1));
		objApprovalParameter.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objApprovalParameter.setSchecklistname(getString(arg0,"schecklistname",arg1));
		objApprovalParameter.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objApprovalParameter.setScolorhexcode(getString(arg0,"scolorhexcode",arg1));
		objApprovalParameter.setSresultcomment(getString(arg0,"sresultcomment",arg1));
		objApprovalParameter.setSenforceresultcomment(getString(arg0,"senforceresultcomment",arg1));
		objApprovalParameter.setSenforcestatuscomment(getString(arg0,"senforcestatuscomment",arg1));
		objApprovalParameter.setSusername(getString(arg0,"susername",arg1));
		objApprovalParameter.setSparametertypename(getString(arg0,"sparametertypename",arg1));
		objApprovalParameter.setNcalculatedresult(getShort(arg0,"ncalculatedresult",arg1));
		objApprovalParameter.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objApprovalParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		objApprovalParameter.setSage(getString(arg0,"sage",arg1));
		objApprovalParameter.setSgendername(getString(arg0,"sgendername",arg1));
		objApprovalParameter.setSpredefinedcomments(getString(arg0,"spredefinedcomments", arg1));
		objApprovalParameter.setSregdate(getString(arg0,"sregdate", arg1));
		objApprovalParameter.setScompletedate(getString(arg0,"scompletedate", arg1));
		objApprovalParameter.setSreportno(getString(arg0,"sreportno",arg1));
		objApprovalParameter.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		
		
		objApprovalParameter.setNexternalordercode(getInteger(arg0,"nexternalordercode",arg1));
		objApprovalParameter.setNexternalordersamplecode(getInteger(arg0,"nexternalordersamplecode",arg1));
		objApprovalParameter.setNexternalordertestcode(getInteger(arg0,"nexternalordertestcode",arg1));
		objApprovalParameter.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		
		return objApprovalParameter;
	}

}
