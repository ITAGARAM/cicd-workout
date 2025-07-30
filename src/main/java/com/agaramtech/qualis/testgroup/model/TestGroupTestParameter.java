package com.agaramtech.qualis.testgroup.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

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
 * This class is used to map the fields of 'testgrouptestparameter' table of the Database.
 */
@Entity
@Table(name = "testgrouptestparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestParameter extends CustomizedResultsetRowMapper<TestGroupTestParameter> implements Serializable,RowMapper<TestGroupTestParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestparametercode")
	private int ntestgrouptestparametercode;
	
	@Column(name = "ntestgrouptestcode", nullable = false)
	private int ntestgrouptestcode;
	
	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;
	
	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;
	
	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;
	
	@Column(name = "sparametersynonym", length = 100, nullable = false)
	private String sparametersynonym;
	
	@Column(name = "nroundingdigits", nullable = false)
	private short nroundingdigits;
	
	@ColumnDefault("3")
	@Column(name = "nresultmandatory", nullable = false)
	private short nresultmandatory=(short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nreportmandatory", nullable = false)
	private short nreportmandatory=(short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "ngradingmandatory", nullable = false)
	private short ngradingmandatory=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "nchecklistversioncode", nullable = false)
	private int nchecklistversioncode;
	
	@Column(name = "sspecdesc", length = 255)
	private String sspecdesc="";
	
	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter = (short)Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nisadhocparameter", nullable = false)
	private short nisadhocparameter=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nisvisible", nullable = false)
	private short nisvisible=(short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nresultaccuracycode", nullable = false)
	private int nresultaccuracycode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient int nparenttestgrouptestcode; 

	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String sreportmandatory;
	
	@Transient
	private transient String sresultmandatory;
	
	@Transient
	private transient String schecklistversionname;
	
	@Transient
	private transient String schecklistname;
	
	@Transient
	private transient String sunitname;
	
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
	private transient int ntestgrouptestformulacode;
	
	@Transient
	private transient String sformulacalculationdetail;
	
	@Transient
	private transient int ntestformulacode;
	
	@Transient
	private transient String sformulacalculationcode;
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient String stestparametersynonym;
	
	@Transient
	private transient String sisadhocparameter;
	
	@Transient
	private transient int ntestcode;
	
	@Transient
	private transient String sresult;
	
	@Transient
	private transient int ngendercode;
	
	@Transient
	private transient int nfromage;
	
	@Transient
	private transient int ntoage;
	
	@Transient
	private transient Map<String, Object> jsondata;
	
	@Transient
	private transient int nprojectmastercode;
	
	@Transient
	private transient int ncomponentcode;
	
	@Transient
	private transient String sresultaccuracyname;
	
	@Transient
	private transient String sformulaname;
	
	//ALPD-5178 L. Subashini - added
	@Transient
	private transient String sparametername;

	
	@Override
	public TestGroupTestParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final TestGroupTestParameter objTestGroupTestParameter = new TestGroupTestParameter();
		objTestGroupTestParameter.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objTestGroupTestParameter.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		objTestGroupTestParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestGroupTestParameter.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objTestGroupTestParameter.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objTestGroupTestParameter.setSparametersynonym(StringEscapeUtils.unescapeJava(getString(arg0,"sparametersynonym",arg1)));
		objTestGroupTestParameter.setNroundingdigits(getShort(arg0,"nroundingdigits",arg1));
		objTestGroupTestParameter.setNresultmandatory(getShort(arg0,"nresultmandatory",arg1));
		objTestGroupTestParameter.setNreportmandatory(getShort(arg0,"nreportmandatory",arg1));
		objTestGroupTestParameter.setNgradingmandatory(getShort(arg0,"ngradingmandatory",arg1));
		objTestGroupTestParameter.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objTestGroupTestParameter.setSspecdesc(StringEscapeUtils.unescapeJava(getString(arg0,"sspecdesc",arg1)));
		objTestGroupTestParameter.setNsorter(getShort(arg0,"nsorter",arg1));
		objTestGroupTestParameter.setNisadhocparameter(getShort(arg0,"nisadhocparameter",arg1));
		objTestGroupTestParameter.setNisvisible(getShort(arg0,"nisvisible",arg1));
		objTestGroupTestParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupTestParameter.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objTestGroupTestParameter.setSreportmandatory(getString(arg0,"sreportmandatory",arg1));
		objTestGroupTestParameter.setSresultmandatory(getString(arg0,"sresultmandatory",arg1));
		objTestGroupTestParameter.setSchecklistversionname(getString(arg0,"schecklistversionname",arg1));
		objTestGroupTestParameter.setSunitname(getString(arg0,"sunitname",arg1));
		objTestGroupTestParameter.setSchecklistname(getString(arg0,"schecklistname",arg1));
		objTestGroupTestParameter.setSmina(getString(arg0,"smina",arg1));
		objTestGroupTestParameter.setSminb(getString(arg0,"sminb",arg1));
		objTestGroupTestParameter.setSmaxb(getString(arg0,"smaxb",arg1));
		objTestGroupTestParameter.setSmaxa(getString(arg0,"smaxa",arg1));
		objTestGroupTestParameter.setSminlod(getString(arg0,"sminlod",arg1));
		objTestGroupTestParameter.setSmaxlod(getString(arg0,"smaxlod",arg1));
		objTestGroupTestParameter.setSminloq(getString(arg0,"sminloq",arg1));
		objTestGroupTestParameter.setSmaxloq(getString(arg0,"smaxloq",arg1));
		objTestGroupTestParameter.setSdisregard(getString(arg0,"sdisregard",arg1));
		objTestGroupTestParameter.setSresultvalue(getString(arg0,"sresultvalue",arg1));
		objTestGroupTestParameter.setNtestgrouptestformulacode(getInteger(arg0,"ntestgrouptestformulacode",arg1));
		objTestGroupTestParameter.setSformulacalculationdetail(getString(arg0,"sformulacalculationdetail",arg1));
		objTestGroupTestParameter.setNtestformulacode(getInteger(arg0,"ntestformulacode",arg1));
		objTestGroupTestParameter.setSformulacalculationcode(getString(arg0,"sformulacalculationcode",arg1));
		objTestGroupTestParameter.setStestname(getString(arg0,"stestname",arg1));
		objTestGroupTestParameter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupTestParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestGroupTestParameter.setStestparametersynonym(getString(arg0,"stestparametersynonym",arg1));
		objTestGroupTestParameter.setNgendercode(getInteger(arg0,"ngendercode",arg1));
		objTestGroupTestParameter.setNfromage(getInteger(arg0,"nfromage",arg1));
		objTestGroupTestParameter.setNtoage(getInteger(arg0,"ntoage",arg1));
		objTestGroupTestParameter.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objTestGroupTestParameter.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objTestGroupTestParameter.setSisadhocparameter(getString(arg0,"sisadhocparameter",arg1));
		objTestGroupTestParameter.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestGroupTestParameter.setSresult(getString(arg0,"sresult", arg1));
		objTestGroupTestParameter.setNparenttestgrouptestcode(getInteger(arg0,"nparenttestgrouptestcode",arg1));
		objTestGroupTestParameter.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objTestGroupTestParameter.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objTestGroupTestParameter.setNresultaccuracycode(getInteger(arg0,"nresultaccuracycode",arg1));
		objTestGroupTestParameter.setSresultaccuracyname(getString(arg0,"sresultaccuracyname",arg1));
		objTestGroupTestParameter.setSformulaname(getString(arg0,"sformulaname",arg1));
		//ALPD-5178 L. Subashini - added
		objTestGroupTestParameter.setSparametername(getString(arg0,"sparametername",arg1));
				

		return objTestGroupTestParameter;
	}

}
