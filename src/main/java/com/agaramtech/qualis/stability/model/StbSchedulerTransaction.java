package com.agaramtech.qualis.stability.model;

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

@Entity
@Table(name = "stbschedulertransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StbSchedulerTransaction extends CustomizedResultsetRowMapper<StbSchedulerTransaction> implements Serializable,RowMapper<StbSchedulerTransaction>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstbschedulertransactioncode")private long nstbschedulertransactioncode;

	@Column(name = "nstbregistrationcode", nullable = false)private long nstbregistrationcode;

	@Column(name = "nstbregistrationsamplecode", nullable = false)private long nstbregistrationsamplecode;

	@Column(name = "npreregno", nullable = false)private int npreregno;
		
	@Column(name = "dscheduleoccurrencedate", nullable = false)private Instant dscheduleoccurrencedate;
	
	@Column(name = "dtransactiondate", nullable = false)private Instant dtransactiondate;
	
	@Column(name="noffsetdtransactiondate") private int noffsetdtransactiondate;
	
	@Column(name="ntransdatetimezonecode")  private short ntransdatetimezonecode;
	
	@Column(name = "ntransactionstatus", nullable = false)private short ntransactionstatus;
	
	@Column(name = "nactivestatus", nullable = false)private short nactivestatus;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	public StbSchedulerTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		StbSchedulerTransaction objStbSchedulerTransaction = new StbSchedulerTransaction();
		
		objStbSchedulerTransaction.setNstbschedulertransactioncode(getLong(arg0,"nstbschedulertransactioncode",arg1));
		objStbSchedulerTransaction.setNstbregistrationcode(getLong(arg0,"nstbregistrationcode",arg1));
		objStbSchedulerTransaction.setNstbregistrationsamplecode(getLong(arg0,"nstbregistrationsamplecode",arg1));
		objStbSchedulerTransaction.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objStbSchedulerTransaction.setDscheduleoccurrencedate(getInstant(arg0, "dscheduleoccurrencedate", arg1));
		objStbSchedulerTransaction.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objStbSchedulerTransaction.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objStbSchedulerTransaction.setNtransdatetimezonecode(getShort(arg0,"ntransdatetimezonecode",arg1));
		objStbSchedulerTransaction.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objStbSchedulerTransaction.setNactivestatus(getShort(arg0,"nactivestatus",arg1));
		objStbSchedulerTransaction.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objStbSchedulerTransaction.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objStbSchedulerTransaction;

	}

}
