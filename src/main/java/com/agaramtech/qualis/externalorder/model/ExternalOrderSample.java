package com.agaramtech.qualis.externalorder.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

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
 * This class is used to map the fields of 'externalordersample' table of the Database.
 */
@Entity
@Table(name = "externalordersample")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalOrderSample extends CustomizedResultsetRowMapper<ExternalOrderSample> implements Serializable,RowMapper<ExternalOrderSample> {


private static final long serialVersionUID = 1L;
 
	@Id
	@Column(name = "nexternalordersamplecode")
	private int nexternalordersamplecode;
	
	@Column(name = "nexternalordercode", nullable=false)
	private int nexternalordercode;
	
	@ColumnDefault("-1")
	@Column(name = "ncomponentcode")
	private int ncomponentcode=-1;
	
	@Column(name = "nsampleqty")
	private float nsampleqty;
	
	@ColumnDefault("-1")
	@Column(name = "nunitcode")
	private int nunitcode=-1;
	
	@Column(name="sexternalsampleid", length=50 ) 
	private String sexternalsampleid;
	
	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable=false)
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "dmodifieddate")	
	private Instant dmodifieddate;
	
	@Column(name = "nparentsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nparentsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
    @Column(name = "npreregno", nullable = false)
	@ColumnDefault("-1")
	private Integer npreregno=-1;
    
    @Column(name = "ntransactionsamplecode", nullable = false)
	@ColumnDefault("-1")
	private Integer ntransactionsamplecode=-1;
	
    @Column(name = "dsamplecollectiondatetime")
	private Instant dsamplecollectiondatetime;
	
    @Column(name = "nsampleappearancecode", nullable = false)
	@ColumnDefault("-1")
	private Integer nsampleappearancecode=-1;   
    
    @ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
    
	@Transient
	private transient String ssampletype;
	
	@Transient
	private transient String sdisplaystatus;
		
	@Transient
	private transient String ssubmittername;
	
	@Transient
	private transient String sdiagnosticcasename;
	
	@Transient
	private transient String sinstitutiondeptname;
	
	@Transient
	private transient String sinstitutiondeptcode; 
	
	@Transient
	private transient String sinstitutionname;
	
	@Transient
	private transient String sinstitutionsitename; 
	
	@Transient
	private transient String sfathername;
	
	@Transient
	private transient Date ddob;
	
	@Transient
	private transient  String sage;
	
	@Transient
	private transient String scountryname;
	
	@Transient
	private transient  String scityname;
	
	@Transient
	private transient String sdistrictname;
	
	@Transient
	private transient String spostalcode;
	
	@Transient
	private transient String sregionname;
	
	@Transient
	private transient String sphoneno;
 	
	@Transient
	private transient  String smobileno;
	
	@Transient
	private transient String semail;
	
	@Transient
	private transient String sexternalid;
	
	@Transient
	private transient String spassportno;
	
	@Transient
	private transient String shouseno;
	
	@Transient
	private transient String sproductcatname;
	
	@Transient
	private transient String sproductname;
	
	@Transient
	private transient String spatientname;
	
	@Transient
	private transient String sgendername ;
	
	@Transient
	private transient String sunitname;
	
	@Transient
	private transient int norderstatus;
	
	@Transient
	private transient String dcollectiondate; 
	
	@Transient
	private transient List<ExternalOrderTest> externalordertest;
	
	@Transient
	private transient String sexternalordertypename;
	
	@Transient
	private transient String sexternalorderid;
	
	@Transient
	private transient short nexternalordertypecode;
	
	@Transient
	private transient String sorderseqno;
	
	@Transient
	private transient String ssamplecollectiondatetime;
		
	@Override
	public ExternalOrderSample mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ExternalOrderSample objExternalOrderSample = new ExternalOrderSample();
		
		objExternalOrderSample.setNexternalordersamplecode(getInteger(arg0,"nexternalordersamplecode",arg1)); 
		objExternalOrderSample.setNexternalordercode(getInteger(arg0,"nexternalordercode",arg1)); 
		objExternalOrderSample.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objExternalOrderSample.setNsampleqty(getFloat(arg0,"nsampleqty",arg1));
		objExternalOrderSample.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objExternalOrderSample.setSexternalsampleid(StringEscapeUtils.unescapeJava(getString(arg0,"sexternalsampleid",arg1))); 
		objExternalOrderSample.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objExternalOrderSample.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objExternalOrderSample.setNparentsitecode(getShort(arg0,"nparentsitecode",arg1));
		objExternalOrderSample.setNstatus(getShort(arg0,"nstatus",arg1));
		objExternalOrderSample.setSsubmittername(getString(arg0,"ssubmittername",arg1));
		objExternalOrderSample.setSdiagnosticcasename(getString(arg0,"sdiagnosticcasename",arg1));
		objExternalOrderSample.setSinstitutiondeptname(getString(arg0,"sinstitutiondeptname",arg1));
		objExternalOrderSample.setSinstitutiondeptcode(getString(arg0,"sinstitutiondeptcode",arg1));
		objExternalOrderSample.setSinstitutionname(getString(arg0,"sinstitutionname",arg1));
		objExternalOrderSample.setSinstitutionsitename(getString(arg0,"sinstitutionsitename",arg1));
		objExternalOrderSample.setSfathername(getString(arg0, "sfathername", arg1));
		objExternalOrderSample.setDdob(getDate(arg0, "ddob", arg1));
        objExternalOrderSample.setSage(getString(arg0, "sage", arg1));
        objExternalOrderSample.setScountryname(getString(arg0,"scountryname",arg1));
        objExternalOrderSample.setScityname(getString(arg0,"scityname",arg1));
        objExternalOrderSample.setSdistrictname(getString(arg0,"sdistrictname",arg1));
        objExternalOrderSample.setSregionname(getString(arg0,"sregionname",arg1));
        objExternalOrderSample.setSphoneno(getString(arg0, "sphoneno", arg1));
        objExternalOrderSample.setSmobileno(getString(arg0, "smobileno", arg1));
        objExternalOrderSample.setSemail(getString(arg0, "semail", arg1));
        objExternalOrderSample.setSexternalid(getString(arg0, "sexternalid", arg1));
        objExternalOrderSample.setShouseno(getString(arg0, "shouseno", arg1));
        objExternalOrderSample.setSproductcatname(getString(arg0,"sproductcatname",arg1));
        objExternalOrderSample.setSproductname(getString(arg0,"sproductname",arg1)); 
        objExternalOrderSample.setSpatientname(getString(arg0,"spatientname",arg1));
        objExternalOrderSample.setSpassportno(getString(arg0, "spassportno", arg1)); 
        objExternalOrderSample.setSgendername(getString(arg0,"sgendername",arg1));
        objExternalOrderSample.setSunitname(getString(arg0,"sunitname",arg1));
        objExternalOrderSample.setSpostalcode(getString(arg0, "spostalcode", arg1));
        objExternalOrderSample.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
        objExternalOrderSample.setNorderstatus(getShort(arg0,"norderstatus",arg1));
        objExternalOrderSample.setNpreregno(getInteger(arg0,"npreregno",arg1));
        objExternalOrderSample.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));		
        objExternalOrderSample.setDcollectiondate(getString(arg0,"dcollectiondate",arg1));
        objExternalOrderSample.setSexternalordertypename(getString(arg0,"sexternalordertypename",arg1));
        objExternalOrderSample.setSexternalorderid(getString(arg0,"sexternalorderid",arg1));
        objExternalOrderSample.setNexternalordertypecode(getShort(arg0,"nexternalordertypecode",arg1));
        objExternalOrderSample.setSorderseqno(getString(arg0,"sorderseqno",arg1));
        objExternalOrderSample.setDsamplecollectiondatetime(getInstant(arg0,"dsamplecollectiondatetime",arg1));
        objExternalOrderSample.setNsampleappearancecode(getInteger(arg0,"nsampleappearancecode",arg1));		
        objExternalOrderSample.setSsamplecollectiondatetime(getString(arg0,"ssamplecollectiondatetime",arg1));
		return objExternalOrderSample;
	}
}

