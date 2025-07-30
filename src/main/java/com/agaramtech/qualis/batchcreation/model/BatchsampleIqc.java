package com.agaramtech.qualis.batchcreation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * This class is used to map the fields of 'batchsampleiqc' table of the
 * Database. 
 */

@Entity
@Data
@Table(name = "batchsampleiqc")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BatchsampleIqc extends CustomizedResultsetRowMapper<BatchsampleIqc> implements Serializable, RowMapper<BatchsampleIqc> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nbatchsampleiqccode")
	private int nbatchsampleiqccode;
	
	@Column(name = "nbatchmastercode")
	private int nbatchmastercode;
	
	@Column(name = "npreregno")
	private int npreregno;
	
	@Column(name = "ntransactionsamplecode")
	private int ntransactionsamplecode;
	
	@Column(name = "ntransactiontestcode")
	private int ntransactiontestcode;
	
	@Column(name = "nmaterialtypecode ")
	private int nmaterialtypecode ;
	
	@Column(name = "nmaterialcatcode")
	private int nmaterialcatcode;
	
	@Column(name = "nmaterialcode")
	private int nmaterialcode;
	
	@Column(name = "nmaterialinventtranscode")
	private int nmaterialinventtranscode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus") 
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private  String smaterialtype; 
	
	@Transient
	private  String smaterialname;
	
	@Transient
	private  String savailablequatity; 
	
	@Transient
	private  String sunitname; 
	
	@Transient
	private  String sinventoryid; 
	
	@Transient
	private  String sremarks; 
	
	@Transient
	private  float nqtyused; 
	
	@Transient
	private  int nallottedspeccode;
	
	@Transient
	private  int nspecsampletypecode;
	
	@Transient
	private  int nmaterialinventorycode;
	
	@Transient
	private  int nsectioncode;
	
	@Transient
	private  String sarno;
	
	
	@Override
	public BatchsampleIqc mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		BatchsampleIqc objBatchSampleIQC = new BatchsampleIqc();
		
		objBatchSampleIQC.setNbatchsampleiqccode(getInteger(arg0,"nbatchsampleiqccode",arg1));
		objBatchSampleIQC.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		objBatchSampleIQC.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objBatchSampleIQC.setNmaterialtypecode(getInteger(arg0,"nmaterialtypecode",arg1));
		objBatchSampleIQC.setNmaterialcatcode(getInteger(arg0,"nmaterialcatcode",arg1));
		objBatchSampleIQC.setNmaterialcode(getInteger(arg0,"nmaterialcode",arg1));
		objBatchSampleIQC.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBatchSampleIQC.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objBatchSampleIQC.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objBatchSampleIQC.setNstatus(getShort(arg0,"nstatus",arg1));
		objBatchSampleIQC.setSmaterialtype(getString(arg0,"smaterialtype",arg1));
		objBatchSampleIQC.setSmaterialname(getString(arg0,"smaterialname",arg1));
		objBatchSampleIQC.setSavailablequatity(getString(arg0,"savailablequatity",arg1));
		objBatchSampleIQC.setSunitname(getString(arg0,"sunitname",arg1));
		objBatchSampleIQC.setSinventoryid(getString(arg0,"sinventoryid",arg1));
		objBatchSampleIQC.setSremarks(getString(arg0,"sremarks",arg1));
		objBatchSampleIQC.setNqtyused(getFloat(arg0,"nqtyused",arg1));
		objBatchSampleIQC.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objBatchSampleIQC.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objBatchSampleIQC.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objBatchSampleIQC.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objBatchSampleIQC.setNmaterialinventorycode(getInteger(arg0,"nmaterialinventorycode",arg1));
		objBatchSampleIQC.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objBatchSampleIQC.setNmaterialinventtranscode(getInteger(arg0,"nmaterialinventtranscode",arg1));
		objBatchSampleIQC.setSarno(getString(arg0,"sarno",arg1));
		return objBatchSampleIQC;
	}


}
