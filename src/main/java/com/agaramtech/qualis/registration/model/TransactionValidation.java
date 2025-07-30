package com.agaramtech.qualis.registration.model;

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

/**
 * This class is used to map the fields of 'transactionvalidation' table of the Database.
 */
@Entity
@Table(name = "transactionvalidation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionValidation extends CustomizedResultsetRowMapper<TransactionValidation> implements Serializable, RowMapper<TransactionValidation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntransactionvalidationcode")
	private short ntransactionvalidationcode;
	
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	
	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;
	
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;
	
	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;
	
	@Column(name = "nregsubtypecode", nullable = false)
	private short nregsubtypecode;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String stransdisplaystatus;

	@Override
	public TransactionValidation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TransactionValidation objTransactionValidation = new TransactionValidation();
		
		objTransactionValidation.setNtransactionvalidationcode(getShort(arg0,"ntransactionvalidationcode",arg1));
		objTransactionValidation.setNformcode(getShort(arg0,"nformcode",arg1));
		objTransactionValidation.setNcontrolcode(getShort(arg0,"ncontrolcode",arg1));
		objTransactionValidation.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objTransactionValidation.setNstatus(getShort(arg0,"nstatus",arg1));
		objTransactionValidation.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTransactionValidation.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTransactionValidation.setNregtypecode(getShort(arg0,"nregtypecode",arg1));
		objTransactionValidation.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objTransactionValidation.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		
		return objTransactionValidation;
	}

}