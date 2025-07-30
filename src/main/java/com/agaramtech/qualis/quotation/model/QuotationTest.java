package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;

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


@Entity
@Table(name = "QuotationTest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QuotationTest extends CustomizedResultsetRowMapper implements Serializable,RowMapper<QuotationTest> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nquotationtestcode")
	private int nquotationtestcode;
	
	@Column(name = "nquotationcode")
	private int nquotationcode;
	
	@Column(name = "ntestcode")
	private int ntestcode;
	
	@Column(name = "ndiscountbandcode")
	@ColumnDefault("-1")
	private int ndiscountbandcode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "squotationleadtime", length = 5, nullable = false)
	private String squotationleadtime;
	
	@Column(name = "ncost", nullable = false)
	@ColumnDefault("0")
	private double ncost=0;
	
	@ColumnDefault("1")
	@Column(name = "nnoofsamples")
	private int nnoofsamples=(int)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ntotalamount", nullable = false)
	private double ntotalamount;
	
	@Column(name = "snotes", length = 500, nullable = false)
	private String snotes;
	
	@Column(name = "stestplatform", length = 500, nullable = false)
	private String stestplatform;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String stestsynonym;
	@Transient
	private float ntotalgrosstamount;
	@Transient
	private int nclientcode;
	@Transient
	private int nprojectmastercode;
	@Transient
	private String sdiscountbandname;
	@Transient
	private float namount;
	@Transient
	private String smethodname;
	@Transient
	private int nmethodcode;
	@Transient
	private int nclientcatcode;
	@Transient
	private int nclientsitecode;
	@Transient
	private int nclientcontactcode;


	public QuotationTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		QuotationTest objQuotationTest = new QuotationTest();
			
		objQuotationTest.setNquotationtestcode(getInteger(arg0,"nquotationtestcode",arg1));
		objQuotationTest.setNquotationcode(getShort(arg0,"nquotationcode",arg1));
		objQuotationTest.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objQuotationTest.setNdiscountbandcode(getInteger(arg0,"ndiscountbandcode",arg1));
		objQuotationTest.setNcost(getDouble(arg0,"ncost",arg1));
		objQuotationTest.setNnoofsamples(getInteger(arg0,"nnoofsamples",arg1));		
		objQuotationTest.setDmodifieddate(getDate(arg0,"dmodifieddate",arg1));
		objQuotationTest.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objQuotationTest.setNstatus(getShort(arg0,"nstatus",arg1));
        objQuotationTest.setStestsynonym(getString(arg0,"stestsynonym",arg1));
        objQuotationTest.setNtotalamount(getDouble(arg0,"ntotalamount",arg1));
        objQuotationTest.setNtotalgrosstamount(getFloat(arg0,"ntotalgrosstamount",arg1));
		objQuotationTest.setNclientcode(getInteger(arg0,"nclientcode",arg1));
		objQuotationTest.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objQuotationTest.setNamount(getFloat(arg0,"namount",arg1));
		objQuotationTest.setSdiscountbandname(getString(arg0,"sdiscountbandname",arg1));

		objQuotationTest.setSnotes(getString(arg0,"snotes",arg1));
		objQuotationTest.setStestplatform(getString(arg0,"stestplatform",arg1));
		objQuotationTest.setSmethodname(getString(arg0,"smethodname",arg1));
		objQuotationTest.setNmethodcode(getInteger(arg0,"nmethodcode",arg1));
		objQuotationTest.setSquotationleadtime(getString(arg0,"squotationleadtime",arg1));
		
		objQuotationTest.setNclientcatcode(getInteger(arg0,"nclientcatcode",arg1));
		objQuotationTest.setNclientsitecode(getInteger(arg0,"nclientsitecode",arg1));
		objQuotationTest.setNclientcontactcode(getInteger(arg0,"nclientcontactcode",arg1));
		return objQuotationTest;
		
	}

	
	

}