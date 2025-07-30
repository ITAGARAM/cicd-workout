package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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

//import lombok.Data;


@Entity
@Table(name = "quotation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Quotation extends CustomizedResultsetRowMapper implements Serializable,RowMapper<Quotation> {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nquotationcode")
	private int nquotationcode;
	
	@Column(name = "squotationno", length = 100, nullable = false)
	private String squotationno;
	
	@Column(name = "nclientcatcode")
	private int nclientcatcode;
	
	@Column(name = "nclientcode")
	private int nclientcode;
	
	@Column(name = "nclientsitecode")
	private int nclientsitecode;
	
	@Column(name = "nclientcontactcode")
	private int nclientcontactcode;
	
	@Column(name = "sclientsiteaddress", length = 255, nullable = false)
	private String sclientsiteaddress;
	
	@Column(name = "sinvoiceaddress", length = 500, nullable = false)
	private String sinvoiceaddress;
	
	@Column(name = "noemcode")
	private int noemcode;
	
	@Column(name = "nquotationtypecode")
	private int nquotationtypecode;
	
	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;
	
	@Column(name = "sprojecttitle", length = 255, nullable = false)
	private String sprojecttitle;
	
	@Column(name = "nprojectmastercode")
	private int nprojectmastercode;
	
	@Column(name = "drfwdate", nullable = false)
	private Instant drfwdate;
	
	@Column(name = "nproductcatcode")
	private int nproductcatcode;
	
	@Column(name = "nproductcode")
	private int nproductcode;
	
	@Column(name = "dquotationdate", nullable = false)
	private Instant dquotationdate;
	
	@Column(name = "sdescription", length = 500, nullable = false)
	private String sdescription;
	
	@Column(name = "sdeviationremarks", length = 1000, nullable = false)
	private String sdeviationremarks;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name="noffsetdrfwdate") 
	private int noffsetdrfwdate;
	
	@Column(name="ntzrfwdate" )  
	private short ntzrfwdate;
	
	@Column(name="noffsetdquotationdate") 
	private int noffsetdquotationdate;
	
	@Column(name="ntzquotationdate" )  
	private short ntzquotationdate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sversionstatus;
	@Transient
	private String squotationdate;
	@Transient
	private String sclientcatname;
	@Transient
	private String sclientname;
	@Transient
	private String sdiscountbandname;
	@Transient
	private String svatbandname;
	@Transient
	private float nvatpercentage;
	@Transient
	private float ndiscountpercentage;
	@Transient
	private double ntotalgrossamount;
	@Transient
	private double ntotalnetamount;
	@Transient
	private double ndiscountamount;
	@Transient
	private double nvatamount;
	@Transient
	private String squotationname;
	@Transient
	private int ndiscountbandcode;
	@Transient
	private int nvatbandcode;
	@Transient
	private int ntransactionstatus;
	@Transient
	private String sproductname;
	@Transient
	private String scontactname;
	@Transient
	private String sphoneno;
	@Transient
	private String semail;
	@Transient
	private String sproductcatname;
	@Transient
	private String soemname;
	@Transient
	private String sclientsitename;
	@Transient
	private String sretireremarks;
	@Transient
	private int ntransdatetimezonecode;
	@Transient
	private int nquotationversioncode;
	@Transient
	private Instant dtransactiondate;
	@Transient
	private String soemnameview;



	public Quotation mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		Quotation objQuotation = new Quotation();
		
		
		objQuotation.setNquotationcode(getInteger(arg0,"nquotationcode",arg1));
		objQuotation.setNclientcatcode(getInteger(arg0,"nclientcatcode",arg1));
		objQuotation.setNclientcode(getInteger(arg0,"nclientcode",arg1));
		objQuotation.setSclientsiteaddress(getString(arg0,"sclientsiteaddress",arg1));
		objQuotation.setSinvoiceaddress(getString(arg0,"sinvoiceaddress",arg1));
	    objQuotation.setNprojecttypecode(getShort(arg0,"nprojecttypecode",arg1));
		objQuotation.setSprojecttitle(getString(arg0,"sprojecttitle",arg1));
		objQuotation.setDrfwdate(getInstant(arg0,"drfwdate",arg1));
		objQuotation.setDquotationdate(getInstant(arg0,"dquotationdate",arg1));
		objQuotation.setSdescription(getString(arg0,"sdescription",arg1));
		objQuotation.setSdeviationremarks(getString(arg0,"sdeviationremarks",arg1));
		objQuotation.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objQuotation.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objQuotation.setNstatus(getShort(arg0,"nstatus",arg1));
		objQuotation.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objQuotation.setSversionstatus(getString(arg0,"sversionstatus",arg1));
		objQuotation.setSclientcatname(getString(arg0,"sclientcatname",arg1));
		objQuotation.setSclientname(getString(arg0,"sclientname",arg1));
		objQuotation.setSquotationdate(getString(arg0,"squotationdate",arg1));
		objQuotation.setNquotationtypecode(getShort(arg0,"nquotationtypecode",arg1));
		objQuotation.setSdiscountbandname(getString(arg0,"sdiscountbandname",arg1));
	    objQuotation.setSvatbandname(getString(arg0,"svatbandname",arg1));
	    objQuotation.setNdiscountpercentage(getFloat(arg0,"ndiscountpercentage",arg1));
	    objQuotation.setNvatpercentage(getFloat(arg0,"nvatpercentage",arg1));
	    objQuotation.setNtotalgrossamount(getDouble(arg0,"ntotalgrossamount",arg1));
        objQuotation.setNtotalnetamount(getDouble(arg0,"ntotalnetamount",arg1));
        objQuotation.setNdiscountamount(getDouble(arg0,"ndiscountamount",arg1));
		objQuotation.setNvatamount(getDouble(arg0,"nvatamount",arg1));
		objQuotation.setSquotationname(getString(arg0,"squotationname",arg1));
		objQuotation.setSquotationno(getString(arg0,"squotationno",arg1));

		objQuotation.setNtzrfwdate(getShort(arg0,"ntzrfwdate",arg1));
		objQuotation.setNtzquotationdate(getShort(arg0,"ntzquotationdate",arg1));
		objQuotation.setNoffsetdrfwdate(getInteger(arg0,"noffsetdrfwdate",arg1));
		objQuotation.setNoffsetdquotationdate(getInteger(arg0,"noffsetdquotationdate",arg1));
		objQuotation.setNdiscountbandcode(getInteger(arg0,"ndiscountbandcode",arg1));
		objQuotation.setNvatbandcode(getInteger(arg0,"nvatbandcode",arg1));
		objQuotation.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		
		objQuotation.setNproductcode(getInteger(arg0,"nproductcode",arg1));
	    objQuotation.setSproductname(getString(arg0,"sproductname",arg1));
	    
	    objQuotation.setScontactname(getString(arg0,"scontactname",arg1));
	    objQuotation.setSphoneno(getString(arg0,"sphoneno",arg1));
	    objQuotation.setSemail(getString(arg0,"semail",arg1));
	    objQuotation.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1)); 
	    objQuotation.setSproductcatname(getString(arg0,"sproductcatname",arg1));
	    
	    objQuotation.setNclientsitecode(getInteger(arg0,"nclientsitecode",arg1)); 
	    objQuotation.setNclientcontactcode(getInteger(arg0,"nclientcontactcode",arg1)); 
	    objQuotation.setNoemcode(getInteger(arg0,"noemcode",arg1));
	    objQuotation.setSoemname(getString(arg0,"soemname",arg1));
	    objQuotation.setSclientsitename(getString(arg0,"sclientsitename",arg1));
	    objQuotation.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
	    objQuotation.setSretireremarks(getString(arg0,"sretireremarks",arg1));
	    objQuotation.setNtransdatetimezonecode(getShort(arg0,"ntransadatetimezonecode",arg1));
		objQuotation.setNquotationversioncode(getInteger(arg0,"nquotationversioncode",arg1));
		objQuotation.setSoemnameview(getString(arg0,"soemnameview",arg1));
		
		
		return objQuotation;
		
	}
	

}