package com.agaramtech.qualis.scheduler.model;

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
@Table(name = "schedulertyperecurring")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerTypeRecurring extends CustomizedResultsetRowMapper<SchedulerTypeRecurring> implements Serializable, RowMapper<SchedulerTypeRecurring> {

private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntyperecurringcode")
	private short ntyperecurringcode;
	
	@Column(name = "nschedulertypecode")
	private short nschedulertypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("3")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String srecurringmodename;
	
	@Override
	public SchedulerTypeRecurring mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		SchedulerTypeRecurring objSchTypeRecurring = new SchedulerTypeRecurring();
		objSchTypeRecurring.setNtyperecurringcode(getShort(arg0, "ntyperecurringcode", arg1));
		objSchTypeRecurring.setNschedulertypecode(getShort(arg0, "nschedulertypecode", arg1));
		objSchTypeRecurring.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSchTypeRecurring.setSrecurringmodename(getString(arg0, "srecurringmodename", arg1));
		objSchTypeRecurring.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objSchTypeRecurring.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSchTypeRecurring.setNstatus(getShort(arg0, "nstatus", arg1));
		objSchTypeRecurring.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objSchTypeRecurring;
		
	}

}
