package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Table(name = "QuotationTotalAmount")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QuotationTotalAmount extends CustomizedResultsetRowMapper implements Serializable,RowMapper<QuotationTotalAmount> {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nquotationtotalamountcode")
	private int nquotationtotalamountcode;
	
	@Column(name = "nquotationcode")
	private int nquotationcode;
	
	@Column(name = "ndiscountbandcode")
	private int ndiscountbandcode;
	
	@Column(name = "nvatbandcode", nullable = false)
	private int nvatbandcode;
	
	@Column(name = "ndiscountamount", nullable = false)
	private double ndiscountamount;
	
	@Column(name = "nvatamount", nullable = false)
	private double nvatamount;
	
	@Column(name = "ntotalgrossamount", nullable = false)
	private double ntotalgrossamount;
	
	@Column(name = "ntotalnetamount", nullable = false)
	private double ntotalnetamount;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sdiscountbandname;
	@Transient
	private String svatbandname;
	@Transient
	private float nvatpercentage;
	@Transient
	private float ndiscountpercentage;
	@Transient
	private int nclientcode;
	@Transient
	private int nprojectmastercode;
	@Transient
	private int nclientcatcode;
	@Transient
	private int nclientsitecode;
	@Transient
	private int nclientcontactcode;

	public QuotationTotalAmount mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		QuotationTotalAmount objQuotationTotalAmount = new QuotationTotalAmount();
			
		objQuotationTotalAmount.setNquotationtotalamountcode(getInteger(arg0,"nquotationtotalamountcode",arg1));
		objQuotationTotalAmount.setNquotationcode(getShort(arg0,"nquotationcode",arg1));
		objQuotationTotalAmount.setDmodifieddate(getDate(arg0,"dmodifieddate",arg1));
		objQuotationTotalAmount.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objQuotationTotalAmount.setNstatus(getShort(arg0,"nstatus",arg1));
        objQuotationTotalAmount.setNtotalnetamount(getDouble(arg0,"ntotalnetamount",arg1));
        objQuotationTotalAmount.setNtotalgrossamount(getDouble(arg0,"ntotalgrossamount",arg1));
        objQuotationTotalAmount.setNvatbandcode(getInteger(arg0,"nvatbandcode",arg1));
        objQuotationTotalAmount.setNdiscountbandcode(getInteger(arg0,"ndiscountbandcode",arg1));

        objQuotationTotalAmount.setNdiscountamount(getDouble(arg0,"ndiscountamount",arg1));
        objQuotationTotalAmount.setNvatamount(getDouble(arg0,"nvatamount",arg1));
        objQuotationTotalAmount.setSdiscountbandname(getString(arg0,"sdiscountbandname",arg1));
        objQuotationTotalAmount.setSvatbandname(getString(arg0,"svatbandname",arg1));
        objQuotationTotalAmount.setNdiscountpercentage(getFloat(arg0,"ndiscountpercentage",arg1));
        objQuotationTotalAmount.setNvatpercentage(getFloat(arg0,"nvatpercentage",arg1));
        objQuotationTotalAmount.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
        objQuotationTotalAmount.setNclientcode(getInteger(arg0,"nclientcode",arg1));
        
        objQuotationTotalAmount.setNclientcatcode(getInteger(arg0,"nclientcatcode",arg1));
        objQuotationTotalAmount.setNclientsitecode(getInteger(arg0,"nclientsitecode",arg1));
        objQuotationTotalAmount.setNclientcontactcode(getInteger(arg0,"nclientcontactcode",arg1));
		return objQuotationTotalAmount;
		
	}

	
	

}