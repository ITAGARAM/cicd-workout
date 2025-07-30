package com.agaramtech.qualis.batchcreation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
 * This class is used to map the fields of 'batchhistory' table of the
 * Database. 
 */

@Entity
@Data
@Table(name = "batchhistory")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BatchHistory extends CustomizedResultsetRowMapper<BatchHistory> implements Serializable, RowMapper<BatchHistory> {
private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nbatchhistorycode")
	private int nbatchhistorycode;
	
	@Column(name = "nbatchmastercode")
	private int nbatchmastercode;
	
	@Column(name = "sbatcharno",length=50)  
	private String sbatcharno;
	
	@Column(name = "ndeputyusercode")
	private int ndeputyusercode;
	
	@Column(name = "ndeputyuserrolecode")
	private int ndeputyuserrolecode;
	
	@Column(name = "ntransactionstatus")
	private int ntransactionstatus;
	
	@Column(name = "nusercode")
	private int nusercode;
	
	@Column(name = "nuserrolecode")
	private int nuserrolecode;
	
	@Column(name = "dtransactiondate")
	private Date dtransactiondate;
	
	@Column(name = "noffsetdtransactiondate") 
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode; 
	
	@Column(name = "scomments" ,length=255) 
	private String scomments;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode") 
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus")  
	private int nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String stransactiondate;
	
	@Override
	public BatchHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		BatchHistory objBatchhistory = new BatchHistory();
		
		objBatchhistory.setNbatchhistorycode(getInteger(arg0,"nbatchhistorycode",arg1));
		objBatchhistory.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		objBatchhistory.setSbatcharno(StringEscapeUtils.unescapeJava(getString(arg0,"sbatcharno",arg1)));
		objBatchhistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objBatchhistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objBatchhistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objBatchhistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objBatchhistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objBatchhistory.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objBatchhistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objBatchhistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objBatchhistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objBatchhistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objBatchhistory.setNstatus(getInteger(arg0,"nstatus",arg1));
		objBatchhistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		return objBatchhistory;
	}
}
