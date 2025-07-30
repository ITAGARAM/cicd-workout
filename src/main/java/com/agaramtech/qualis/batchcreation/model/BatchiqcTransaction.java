package com.agaramtech.qualis.batchcreation.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'batchiqctransaction' table of the
 * Database. 
 */

@Entity
@Data
@Table(name = "batchiqctransaction")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BatchiqcTransaction extends CustomizedResultsetRowMapper<BatchiqcTransaction> implements Serializable, RowMapper<BatchiqcTransaction> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntransactiontestcode")
	private int ntransactiontestcode;
	
	@Column(name = "npreregno")
	private int npreregno;
	
	@Column(name = "ntransactionsamplecode")  
	private int ntransactionsamplecode;
	
	@Column(name = "nbatchmastercode")
	private int nbatchmastercode;
	
	@Column(name = "ntestcode" )  
	private int ntestcode;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode") 
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus") 
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Override
	public BatchiqcTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {
		BatchiqcTransaction objBatchIqcTransaction = new BatchiqcTransaction();
		
		objBatchIqcTransaction.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objBatchIqcTransaction.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objBatchIqcTransaction.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objBatchIqcTransaction.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		objBatchIqcTransaction.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objBatchIqcTransaction.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objBatchIqcTransaction.setNstatus(getShort(arg0,"nstatus",arg1));
		objBatchIqcTransaction.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		return objBatchIqcTransaction;
	}
}
