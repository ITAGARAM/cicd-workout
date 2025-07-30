package com.agaramtech.qualis.archivalandpurging.purge.model;

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
 * This class is used to map the fields of 'purgemaster' table of the Database.
 * 
 */
@Entity
@Table(name = "purgemaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PurgeMaster extends CustomizedResultsetRowMapper<PurgeMaster> implements Serializable,RowMapper<PurgeMaster> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "npurgemastercode")
	private int npurgemastercode;
	
	@Column(name = "npurgesitecode")
	@ColumnDefault("-1")
	private int npurgesitecode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dfromdate")
	private Instant dfromdate;
	
	@Column(name = "dtodate")
	private Instant dtodate;
	
	@Column(name = "noffsetdfromdate", nullable=false)
	private int noffsetdfromdate = 0;
	
	@Column(name = "nfromdatetimezonecode", nullable=false)
	private short nfromdatetimezonecode = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "noffsetdtodate", nullable=false)
	private int noffsetdtodate = 0;
	
	@Column(name = "ntodatetimezonecode", nullable=false)
	private short ntodatetimezonecode = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nretrycount")
	private int nretrycount;
	
	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("8")
	private short ntransactionstatus = (short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "noffsetdmodifieddate", nullable=false)
	private int noffsetdmodifieddate = 0;
	
	@Column(name = "nmodifieddatetimezonecode", nullable=false)
	private short nmodifieddatetimezonecode = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nmodifiedby")
	private int nmodifiedby;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String stransactionstatus;
	@Transient
	private String smodifieddate;
	@Transient
	private String sfromdate;
	@Transient
	private String stodate;
	@Transient
	private String stzfromdate;
	@Transient
	private String stztodate;
	@Transient
	private String stzmodifieddate;
	@Transient
	private String ssitename;
	@Transient
	private String smodifiedby;
	
	
	@Override
	public PurgeMaster mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		PurgeMaster objPurgeMaster = new PurgeMaster();
		objPurgeMaster.setNpurgemastercode(getInteger(arg0,"npurgemastercode",arg1));
		objPurgeMaster.setNpurgesitecode(getInteger(arg0,"npurgesitecode",arg1));
		objPurgeMaster.setDfromdate(getInstant(arg0,"dfromdate",arg1));
		objPurgeMaster.setDtodate(getInstant(arg0,"dtodate",arg1));
		objPurgeMaster.setNretrycount(getInteger(arg0,"nretrycount",arg1));
		objPurgeMaster.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objPurgeMaster.setSdescription(getString(arg0,"sdescription",arg1));
		objPurgeMaster.setNmodifiedby(getInteger(arg0,"nmodifiedby",arg1));
		objPurgeMaster.setSmodifiedby(getString(arg0,"smodifiedby",arg1));
		objPurgeMaster.setStransactionstatus(getString(arg0,"stransactionstatus",arg1));
		objPurgeMaster.setSsitename(getString(arg0,"ssitename",arg1));
		objPurgeMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objPurgeMaster.setNstatus(getShort(arg0,"nstatus",arg1));
		objPurgeMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objPurgeMaster.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objPurgeMaster.setSfromdate(getString(arg0,"sfromdate",arg1));
		objPurgeMaster.setStodate(getString(arg0,"stodate",arg1));
		objPurgeMaster.setNoffsetdfromdate(getInteger(arg0,"noffsetdfromdate",arg1));
		objPurgeMaster.setNfromdatetimezonecode(getShort(arg0,"nfromdatetimezonecode",arg1));
		objPurgeMaster.setNoffsetdtodate(getInteger(arg0,"noffsetdtodate",arg1));
		objPurgeMaster.setNtodatetimezonecode(getShort(arg0,"ntodatetimezonecode",arg1));
		objPurgeMaster.setNoffsetdmodifieddate(getInteger(arg0,"noffsetdmodifieddate",arg1));
		objPurgeMaster.setNmodifieddatetimezonecode(getShort(arg0,"nmodifieddatetimezonecode",arg1));
		
		return objPurgeMaster;
	}
	
}