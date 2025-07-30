package com.agaramtech.qualis.testmanagement.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'reportinfotest' table of the
 * Database. 
 */

@Entity
@Table(name = "reportinfotest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class ReportInfoTest extends CustomizedResultsetRowMapper<ReportInfoTest> implements Serializable, RowMapper<ReportInfoTest> {

   private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name ="ntestcode",nullable=false)
	private int ntestcode;
	
	@Column(name ="sconfirmstatement",length =500)
	private String sconfirmstatement="";
	
	@Column(name ="sdecisionrule",length =500)
	private String sdecisionrule="";
	
	@Column(name ="ssopdescription",length =500)
	private String ssopdescription="";
	
	@Column(name ="stestcondition",length =500)
	private String stestcondition="";
	
	@Column(name ="sdeviationcomments",length =500)
	private String sdeviationcomments="";
	
	@Column(name ="smethodstandard",length =255)
	private String smethodstandard="";
	
	@Column(name="sreference",length =255)
	private String sreference="";
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode",nullable=false)
	@ColumnDefault("-1")
	private short nsitecode= (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus",nullable=false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
 
	@Override
	public ReportInfoTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ReportInfoTest  objReportInfoTest =new ReportInfoTest();		
		objReportInfoTest.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objReportInfoTest.setSconfirmstatement(StringEscapeUtils.unescapeJava(getString(arg0,"sconfirmstatement",arg1)));
		objReportInfoTest.setSdecisionrule(StringEscapeUtils.unescapeJava(getString(arg0,"sdecisionrule",arg1)));
		objReportInfoTest.setSsopdescription(StringEscapeUtils.unescapeJava(getString(arg0,"ssopdescription",arg1)));
		objReportInfoTest.setStestcondition(StringEscapeUtils.unescapeJava(getString(arg0,"stestcondition",arg1)));
		objReportInfoTest.setSdeviationcomments(StringEscapeUtils.unescapeJava(getString(arg0,"sdeviationcomments",arg1)));
		objReportInfoTest.setSmethodstandard(StringEscapeUtils.unescapeJava(getString(arg0,"smethodstandard",arg1)));
		objReportInfoTest.setSreference(StringEscapeUtils.unescapeJava(getString(arg0,"sreference",arg1)));
		objReportInfoTest.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objReportInfoTest.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objReportInfoTest.setNstatus(getShort(arg0,"nstatus",arg1));
		return objReportInfoTest;
	}
}
