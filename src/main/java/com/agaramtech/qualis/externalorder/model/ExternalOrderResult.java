package com.agaramtech.qualis.externalorder.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * This class is used to map the fields of 'externalorderresult' table of the Database.
 */
@Entity
@Table(name = "externalorderresult")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalOrderResult extends CustomizedResultsetRowMapper<ExternalOrderResult> implements Serializable, RowMapper<ExternalOrderResult>{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nexternalorderresultcode") 
	private int nexternalorderresultcode;
	
	@Column(name = "nexternalordertestcode", nullable=false)  
	private int nexternalordertestcode;
	
	@Column(name = "nexternalordersamplecode", nullable=false) 
	private int nexternalordersamplecode;	
	
	@Column(name = "nexternalordercode",  nullable=false) 
	private int nexternalordercode;	
	
	@Column(name = "npreregno",  nullable=false) 
	private int npreregno;	
	
	@Column(name = "ntransactionsamplecode",  nullable=false) 
	private int ntransactionsamplecode;	
	
	@Column(name = "ntransactiontestcode",  nullable=false) 
	private int ntransactiontestcode;
	
	@Column(name="ntransactionstatus")
	@ColumnDefault("8")
	private int ntransactionstatus=(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name="ntestcode") 
	private int ntestcode;
	
	@Column(name="nsentstatus")
	@ColumnDefault("0")
	private int nsentstatus=(short)Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient int  nrepeatno;
	
	@Transient
	private transient int nretestno;
	
	@Transient
	private transient String externalsampleid;
	
	@Transient
	private transient String jsondata;
	
	@Transient
	private transient int nparametertypecode;
	
	@Transient
	private transient int  ntransactionresultcode;
	
	@Transient
	private transient int serialnumber;
	
	

	@Override
	public ExternalOrderResult mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ExternalOrderResult objExternalOrderResult =new ExternalOrderResult();
		

		objExternalOrderResult.setNexternalorderresultcode(getInteger(arg0,"nexternalorderresultcode",arg1));
		objExternalOrderResult.setNexternalordertestcode(getInteger(arg0,"nexternalordertestcode",arg1));
		objExternalOrderResult.setNexternalordersamplecode(getInteger(arg0,"nexternalordersamplecode",arg1));
		objExternalOrderResult.setNexternalordercode(getInteger(arg0,"nexternalordercode",arg1));
		objExternalOrderResult.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objExternalOrderResult.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objExternalOrderResult.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objExternalOrderResult.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objExternalOrderResult.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objExternalOrderResult.setNsentstatus(getInteger(arg0,"nsentstatus",arg1));
		objExternalOrderResult.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objExternalOrderResult.setNstatus(getShort(arg0,"nstatus",arg1));
		objExternalOrderResult.setStestname(getString(arg0,"stestname",arg1));
		objExternalOrderResult.setNrepeatno(getInteger(arg0,"nrepeatno",arg1));
		objExternalOrderResult.setNretestno(getInteger(arg0,"nretestno",arg1));
		objExternalOrderResult.setExternalsampleid(getString(arg0,"externalsampleid",arg1));
		objExternalOrderResult.setJsondata(getString(arg0,"jsondata",arg1));
		objExternalOrderResult.setNparametertypecode(getInteger(arg0,"nparametertypecode",arg1));
		objExternalOrderResult.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));
		objExternalOrderResult.setSerialnumber(getInteger(arg0,"serialnumber",arg1));
		return objExternalOrderResult;
	
	}

//	@Override
//	public String toString() {
//		return "ExternalOrderResult [nexternalorderresultcode=" + nexternalorderresultcode + ", nexternalordertestcode="
//				+ nexternalordertestcode + ", nexternalordersamplecode=" + nexternalordersamplecode
//				+ ", nexternalordercode=" + nexternalordercode + ", npreregno=" + npreregno
//				+ ", ntransactionsamplecode=" + ntransactionsamplecode + ", ntransactiontestcode="
//				+ ntransactiontestcode + ", ntransactionstatus=" + ntransactionstatus + ", ntestcode=" + ntestcode
//				+ ", nsentstatus=" + nsentstatus + ", nsitecode=" + nsitecode + ", nstatus=" + nstatus + ", nparametertypecode=" + nparametertypecode +", ntransactionresultcode=" + ntransactionresultcode +", serialnumber=" + serialnumber +"]";
//	}

	

}
