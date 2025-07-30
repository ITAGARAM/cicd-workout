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
@Table(name = "schedulertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerType extends CustomizedResultsetRowMapper<SchedulerType> implements Serializable, RowMapper<SchedulerType> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nschedulertypecode")
	private short nschedulertypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("3")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String sschedulertypename;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient short nflag;
	@Transient
	private transient String sdefaultname;

	
	
	@Override
	public SchedulerType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		SchedulerType objSchedulerType = new SchedulerType();
		objSchedulerType.setNschedulertypecode(getShort(arg0, "nschedulertypecode", arg1));
		objSchedulerType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSchedulerType.setSschedulertypename(getString(arg0, "sschedulertypename", arg1));
		objSchedulerType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objSchedulerType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSchedulerType.setNstatus(getShort(arg0, "nstatus", arg1));
		objSchedulerType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objSchedulerType.setNflag(getShort(arg0,"nflag",arg1));
		objSchedulerType.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objSchedulerType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return objSchedulerType;
		
	}
	
}
