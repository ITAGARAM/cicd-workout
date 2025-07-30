package com.agaramtech.qualis.archivalandpurging.restoremaster.model;

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
 * This class is used to map the fields of 'restoremaster' table of the Database.
 * 
*/
@Entity
@Table(name = "restoremaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RestoreMaster extends CustomizedResultsetRowMapper<RestoreMaster> implements Serializable,RowMapper<RestoreMaster> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nrestoremastercode")
	private int nrestoremastercode;
	
	@Column(name = "nrestoresitecode")
	@ColumnDefault("-1")
	private int nrestoresitecode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
	@Column(name = "npurgemastercode")
	private int npurgemastercode;

	@Column(name = "dfromdate")
	private Instant dfromdate;
	
	@Column(name = "dtodate")
	private Instant dtodate;
	
	@ColumnDefault("0")
	@Column(name = "noffsetdfromdate", nullable=false)
	private int noffsetdfromdate = 0;
	
	@Column(name = "nfromdatetimezonecode", nullable=false)
	private short nfromdatetimezonecode = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("0")
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
	
	@ColumnDefault("0")
	@Column(name = "noffsetdmodifieddate", nullable=false)
	private int noffsetdmodifieddate = 0;
	
	@Column(name = "nmodifieddatetimezonecode", nullable=false)
	private short nmodifieddatetimezonecode = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nmodifiedby")
	private int nmodifiedby;
	
	@Transient
	private String stransactionstatus;
	@Transient
	private String smodifieddate;
	@Transient
	private String sfromdate;
	@Transient
	private String stzfromdate;
	@Transient
	private String stodate;
	@Transient
	private String stztodate;
	@Transient
	private String stzmodifieddate;
	@Transient
	private String ssitename;
	@Transient
	private String smodifiedby;
	
	
	@Override
	public RestoreMaster mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		RestoreMaster objLimsRestoreMaster = new RestoreMaster();
		objLimsRestoreMaster.setNrestoremastercode(getInteger(arg0,"nrestoremastercode",arg1));
		objLimsRestoreMaster.setNpurgemastercode(getInteger(arg0,"npurgemastercode",arg1));
		objLimsRestoreMaster.setNrestoresitecode(getInteger(arg0,"nrestoresitecode",arg1));
		objLimsRestoreMaster.setDfromdate(getInstant(arg0,"dfromdate",arg1));
		objLimsRestoreMaster.setDtodate(getInstant(arg0,"dtodate",arg1));
		objLimsRestoreMaster.setNretrycount(getInteger(arg0,"nretrycount",arg1));
		objLimsRestoreMaster.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objLimsRestoreMaster.setSdescription(getString(arg0,"sdescription",arg1));
		objLimsRestoreMaster.setNmodifiedby(getInteger(arg0,"nmodifiedby",arg1));
		objLimsRestoreMaster.setStransactionstatus(getString(arg0,"stransactionstatus",arg1));
		objLimsRestoreMaster.setSsitename(getString(arg0,"ssitename",arg1));
		objLimsRestoreMaster.setSmodifiedby(getString(arg0,"smodifiedby",arg1));
		objLimsRestoreMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objLimsRestoreMaster.setNstatus(getShort(arg0,"nstatus",arg1));
		objLimsRestoreMaster.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objLimsRestoreMaster.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objLimsRestoreMaster.setSfromdate(getString(arg0,"sfromdate",arg1));
		objLimsRestoreMaster.setStodate(getString(arg0,"stodate",arg1));
		objLimsRestoreMaster.setNoffsetdfromdate(getInteger(arg0,"noffsetdfromdate",arg1));
		objLimsRestoreMaster.setNfromdatetimezonecode(getShort(arg0,"nfromdatetimezonecode",arg1));
		objLimsRestoreMaster.setNoffsetdtodate(getInteger(arg0,"noffsetdtodate",arg1));
		objLimsRestoreMaster.setNtodatetimezonecode(getShort(arg0,"ntodatetimezonecode",arg1));
		objLimsRestoreMaster.setNoffsetdmodifieddate(getInteger(arg0,"noffsetdmodifieddate",arg1));
		objLimsRestoreMaster.setNmodifieddatetimezonecode(getShort(arg0,"nmodifieddatetimezonecode",arg1));
		return objLimsRestoreMaster;
	}
	
}