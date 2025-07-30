package com.agaramtech.qualis.scheduler.model;

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
 * This class is used to map the fields of 'schedulemasterweekly' table of the Database.
 */
@Entity 
@Table(name="schedulemasterweekly")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScheduleMasterWeekly extends CustomizedResultsetRowMapper<ScheduleMasterWeekly> implements Serializable,RowMapper<ScheduleMasterWeekly> {

	private static final long serialVersionUID = 1L;
	
	@Id
    @Column(name = "nschedulecode")
	private int nschedulecode;
	
	@Column(name = "nsunday", nullable=false)
	private int nsunday;
	
	@Column(name = "nmonday", nullable=false)
	private int nmonday;
	
	@Column(name = "ntuesday", nullable=false)
	private int ntuesday;
	
	@Column(name = "nwednesday", nullable=false)
	private int nwednesday;
	
	@Column(name = "nthursday", nullable=false)
	private int nthursday;
	
	@Column(name = "nfriday", nullable=false)
	private int nfriday;
	
	@Column(name = "nsaturday", nullable=false)
	private int nsaturday;
	
	@Column(name = "nmonthyweek", nullable=false)
	private int nmonthyweek;
	
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
    private transient String soccurence;

	public ScheduleMasterWeekly mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ScheduleMasterWeekly objSch = new ScheduleMasterWeekly();
		
		objSch.setNschedulecode(getInteger(arg0,"nschedulecode",arg1));
		
		objSch.setNsunday(getInteger(arg0,"nsunday",arg1));
		objSch.setNmonday(getInteger(arg0,"nmonday",arg1));
		objSch.setNtuesday(getInteger(arg0,"ntuesday",arg1));
		objSch.setNwednesday(getInteger(arg0,"nwednesday",arg1));
		objSch.setNthursday(getInteger(arg0,"nthursday",arg1));
		objSch.setNfriday(getInteger(arg0,"nfriday",arg1));
		objSch.setNsaturday(getInteger(arg0,"nsaturday",arg1));
		objSch.setNstatus(getShort(arg0,"nstatus",arg1));
		objSch.setNmonthyweek(getInteger(arg0,"nmonthyweek",arg1));
		objSch.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSch.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSch.setSoccurence(getString(arg0,"soccurence",arg1));  

		return objSch;
	}


}
