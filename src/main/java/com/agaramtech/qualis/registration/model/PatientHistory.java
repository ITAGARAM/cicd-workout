package com.agaramtech.qualis.registration.model;

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
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'patienthistory' table of the Database.
 */
@Entity
@Table(name = "patienthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PatientHistory extends CustomizedResultsetRowMapper<PatientHistory> implements Serializable, RowMapper<PatientHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npatienthistorycode")
	private int npatienthistorycode;
	
	@Column(name = "ncoaparentcode")	
	private int ncoaparentcode;
	
	@Column(name = "ncoachildcode")
	private int ncoachildcode;
	
	@Column(name = "npreregno",nullable=false)
	private int npreregno;
	
	@Column(name = "ntransactionsamplecode",nullable=false)
	private int ntransactionsamplecode;
	
	@Column(name = "ntransactiontestcode",nullable=false)
	private int ntransactiontestcode;
	
	@Column(name = "spatientid", length = 50, nullable = false)
	private String spatientid;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name="dtransactiondate")
	private Instant dtransactiondate;
	
	@Column(name="noffsetdtransactiondate") 
	private int noffsetdtransactiondate;	
	
	@Column(name="ntransdatetimezonecode")  
	private int ntransdatetimezonecode;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =  (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="nversionno")
	private int nversionno;

	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sresult;
	
	@Transient
	private transient String sparametersynonym;
	
	@Transient
	private transient String sgradename;
	
	@Transient
	private transient String sfinal;
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String sreportno;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient int ntransactionresultcode;
	
	@Override
	public PatientHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final PatientHistory objPatientHistory = new PatientHistory();

		objPatientHistory.setNpatienthistorycode(getInteger(arg0,"npatienthistorycode",arg1));
		objPatientHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objPatientHistory.setNcoaparentcode(getInteger(arg0,"ncoaparentcode",arg1));
		objPatientHistory.setNcoachildcode(getInteger(arg0,"ncoachildcode",arg1));
		objPatientHistory.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objPatientHistory.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objPatientHistory.setSpatientid(StringEscapeUtils.unescapeJava(getString(arg0,"spatientid",arg1)));
		objPatientHistory.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objPatientHistory.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objPatientHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objPatientHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objPatientHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objPatientHistory.setNstatus(getShort(arg0,"nstatus",arg1));		
		objPatientHistory.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		objPatientHistory.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objPatientHistory.setSresult(getString(arg0,"sresult",arg1));
		objPatientHistory.setSparametersynonym(getString(arg0,"sparametersynonym",arg1));
		objPatientHistory.setSgradename(getString(arg0,"sgradename",arg1));
		objPatientHistory.setSfinal(getString(arg0,"sfinal",arg1));
		objPatientHistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objPatientHistory.setSreportno(getString(arg0,"sreportno",arg1));
		objPatientHistory.setSarno(getString(arg0,"sarno",arg1));
		objPatientHistory.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));		
		objPatientHistory.setNversionno(getInteger(arg0,"nversionno",arg1));		

		return objPatientHistory;
	}

}
