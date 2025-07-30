package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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

@Entity
@Table(name = "transactionfiltertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionFilterType extends CustomizedResultsetRowMapper<TransactionFilterType> implements Serializable, RowMapper<TransactionFilterType> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntransfiltertypecode")
	private int ntransfiltertypecode;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;

	@Column(name="dmodifieddate")private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String stransfiltertypename;
	
	@Override
	public TransactionFilterType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TransactionFilterType objTransactionType = new TransactionFilterType();
		
		objTransactionType.setNtransfiltertypecode(getShort(arg0, "ntransfiltertypecode", arg1));
		objTransactionType.setStransfiltertypename(getString(arg0, "stransfiltertypename", arg1));
		objTransactionType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objTransactionType.setNstatus(getShort(arg0, "nstatus", arg1));
		objTransactionType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		
		return objTransactionType;
	}

	

}
