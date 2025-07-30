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

/**
 * This class is used to map the fields of 'schedulartransaction' table of the Database.
 * @author ATE113
 * @version 9.0.0.1
 * @since   18- Apr- 2022
 */

@Entity 
@Table(name="schedulertransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerTransaction extends CustomizedResultsetRowMapper<SchedulerTransaction> implements Serializable,RowMapper<SchedulerTransaction> {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "nschedulertransactioncode")
	private int nschedulertransactioncode;
	
//	ALPD-4941 Added nschedulercode in schedulertransaction table
	@Column(name = "nschedulecode")
	private int nschedulecode;
	
	@Column(name = "nschedulersamplecode")
	private int nschedulersamplecode;
	 
	@Column(name = "nsampletypecode")
	private short nsampletypecode;
	
	@Column(name = "npreregno")
	private int npreregno;
		
//	ALPD-4941 Added dscheduleoccurrencedate in schedulertransaction table
	@Column(name = "dscheduleoccurrencedate", nullable=false)
	private Instant dscheduleoccurrencedate;
	
	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;
	
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private short ntransdatetimezonecode;
	
//	ALPD-4941 Added ntransactionstatus in schedulertransaction table
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus;	
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sscheduleoccurencedate;
	@Transient
	private transient String stzscheduleoccurencedate;
	@Transient
	private transient String sproduct;
	@Transient
	private transient String ssamplingpoint;
	

	@Override
	public SchedulerTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SchedulerTransaction objSch = new SchedulerTransaction();
		
		objSch.setNschedulertransactioncode(getInteger(arg0,"nschedulertransactioncode",arg1));
		objSch.setNschedulecode(getInteger(arg0,"nschedulecode",arg1));
		objSch.setNschedulersamplecode(getInteger(arg0,"nschedulersamplecode",arg1));
		objSch.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objSch.setDscheduleoccurrencedate(getInstant(arg0,"dscheduleoccurrencedate",arg1));				
		objSch.setSscheduleoccurencedate(getString(arg0,"sscheduleoccurencedate",arg1));
		objSch.setStzscheduleoccurencedate(getString(arg0,"stzscheduleoccurencedate",arg1));
		objSch.setSproduct(getString(arg0,"sproduct",arg1));
		objSch.setSsamplingpoint(getString(arg0,"ssamplingpoint",arg1));
		objSch.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSch.setNstatus(getShort(arg0,"nstatus",arg1));
		objSch.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objSch.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objSch.setNtransdatetimezonecode(getShort(arg0,"ntransdatetimezonecode",arg1));
		objSch.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objSch.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objSch.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objSch.setNsampletypecode(getShort(arg0,"nsampletypecode",arg1));

		return objSch;
	}

}

