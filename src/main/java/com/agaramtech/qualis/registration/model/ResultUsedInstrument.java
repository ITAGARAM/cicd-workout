package com.agaramtech.qualis.registration.model;

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
 * This class is used to map the fields of 'resultusedinstrument' table of the Database.
 */
@Entity
@Table(name = "resultusedinstrument")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultUsedInstrument extends CustomizedResultsetRowMapper<ResultUsedInstrument> implements Serializable,RowMapper<ResultUsedInstrument> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nresultusedinstrumentcode")
	private int nresultusedinstrumentcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode;

	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "ninstrumentnamecode", nullable = false)
	private int ninstrumentnamecode;

	@Column(name = "dfromdate")
	private Instant dfromdate;

	@Column(name = "dtodate")
	private Instant dtodate;

	@Column(name = "ntzfromdate", nullable = false)
	@ColumnDefault("-1")
	private short ntzfromdate = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ntztodate", nullable = false)
	@ColumnDefault("-1")
	private short ntztodate = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "noffsetdfromdate")
	@ColumnDefault("0")
	private int noffsetdfromdate = (short)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@Column(name = "noffsetdtodate")
	@ColumnDefault("0")
	private int noffsetdtodate = (short)Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@Lob
	@Column(name ="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;

	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String sinstrumentcatname;
	
	@Transient
	private transient String sinstrumentid;
	
	@Transient
	private transient String sinstrumentname;
	
	@Transient
	private transient String sfromdate;
	
	@Transient
	private transient String stodate;
	
	@Transient
	private transient String stzfromdate;
	
	@Transient
	private transient String stztodate;
	
	@Transient
	private transient int ncalibrationreq;
	

	@Override
	public ResultUsedInstrument mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ResultUsedInstrument objResultUsedInstrument = new ResultUsedInstrument();
		
		objResultUsedInstrument.setNresultusedinstrumentcode(getInteger(arg0, "nresultusedinstrumentcode", arg1));
		objResultUsedInstrument.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objResultUsedInstrument.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objResultUsedInstrument.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objResultUsedInstrument.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
		objResultUsedInstrument.setNinstrumentnamecode(getInteger(arg0, "ninstrumentnamecode", arg1));
		objResultUsedInstrument.setDfromdate(getInstant(arg0, "dfromdate", arg1));
		objResultUsedInstrument.setDtodate(getInstant(arg0, "dtodate", arg1));
		objResultUsedInstrument.setNtzfromdate(getShort(arg0, "ntzfromdate", arg1));
		objResultUsedInstrument.setNtztodate(getShort(arg0, "ntztodate", arg1));
		objResultUsedInstrument.setNstatus(getShort(arg0, "nstatus", arg1));
		objResultUsedInstrument.setSarno(getString(arg0, "sarno", arg1));
		objResultUsedInstrument.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objResultUsedInstrument.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objResultUsedInstrument.setSinstrumentcatname(getString(arg0, "sinstrumentcatname", arg1));
		objResultUsedInstrument.setSinstrumentid(getString(arg0, "sinstrumentid", arg1));
		objResultUsedInstrument.setSinstrumentname(getString(arg0, "sinstrumentname", arg1));
		objResultUsedInstrument.setSfromdate(getString(arg0, "sfromdate", arg1));
		objResultUsedInstrument.setStodate(getString(arg0, "stodate", arg1));
		objResultUsedInstrument.setStzfromdate(getString(arg0, "stzfromdate", arg1));
		objResultUsedInstrument.setStztodate(getString(arg0, "stztodate", arg1));
		objResultUsedInstrument.setNcalibrationreq(getInteger(arg0, "ncalibrationreq", arg1));
		objResultUsedInstrument.setNoffsetdfromdate(getInteger(arg0,"noffsetdfromdate",arg1));
		objResultUsedInstrument.setNoffsetdtodate(getInteger(arg0,"noffsetdtodate",arg1));
		objResultUsedInstrument.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objResultUsedInstrument.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));

		return objResultUsedInstrument;
	}

}
