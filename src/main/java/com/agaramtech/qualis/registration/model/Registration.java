package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * This class is used to map the fields of 'registration' table of the Database.
 */
@Entity
@Table(name = "registration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Registration extends CustomizedResultsetRowMapper<Registration> implements Serializable,RowMapper<Registration> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npreregno")
	private int npreregno;

	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;

	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;

	@Column(name = "nregsubtypecode", nullable = false)
	private short nregsubtypecode;

	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode;

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;
	
	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode;

	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "nmaterialcatcode", nullable = false)
	private int nmaterialcatcode;

	@Column(name = "nmaterialcode", nullable = false)
	private int nmaterialcode;
	
	@Column(name = "nmaterialinventorycode", nullable = false)
	private int nmaterialinventorycode;
	
	@Column(name = "nisiqcmaterial", nullable = false)
	@ColumnDefault("4")
	private short nisiqcmaterial = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndesigntemplatemappingcode", nullable = false)
	private int ndesigntemplatemappingcode;

	
	@Column(name = "nregsubtypeversioncode", nullable = false)
	private int nregsubtypeversioncode;
	
	@Column(name = "ntemplatemanipulationcode", nullable = false)
	private int ntemplatemanipulationcode;

	@Column(name = "nallottedspeccode", nullable = false)
	private int nallottedspeccode;

	
//	ALPD-4941 Added nschedulertransactioncode column in registration table
	@Column(name = "nschedulertransactioncode", nullable = false)
	@ColumnDefault("-1")
	private int nschedulertransactioncode = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@Column(name = "nprotocolcode", nullable = false)
	private int nprotocolcode;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	
	@Transient
	private transient int ntransactionsamplecode;
	
	@Transient
	private transient int ntransactiontestcode;
	
	@Transient
	private transient String stestsynonym;
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient int nspecsampletypecode;
	
	@Transient
	private transient int ncategorybasedflow;
	
	@Transient
	private transient int ntransactionstatus;
	
	@Transient
	private transient String dtransactiondate;
	
	@Transient
	private transient String dregdate;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient int noffset;
	
	@Transient
	private transient int ntransdatetimezonecode;
	
	@Transient
	private transient int noffsetdtransactiondate;
	
	@Transient
	private transient int noffsetdregdate;
	
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient int napprovalversioncode;
	
	@Transient
	private transient String stranscolor;
	
	@Transient
	private transient boolean bflag;
	
	@Transient
	private transient String ssectionname;
	
	@Transient
	private transient String ssystemfilename;
	
	@Transient
	private transient String sfilename;
	
	@Transient
	private transient int slno;
	
	@Transient
	private transient int nexternalordertypecode;
	
	@Transient
	private transient int ncomponentcode;
	
	@Transient
	private transient int ntestcode;
	
	@Transient
	private transient String ssamplename;
	
	@Transient
	private transient short nnextcalibration;
	
	@Transient
	private transient short nnextcalibrationperiod;	
	
	@Transient
	private transient int ngendercode;
	
	//start-->ALPD-5332--added by vignesh R(04-02-2025)
	
	@Transient
	private transient String sinstrumentid;

	@Transient
	private transient short isnextvalidation;
	
	@Transient
	private transient String sinventoryid;
	
	@Transient
	private transient int nnextvalidationperiod;
	
	@Transient
	private transient int nnextvalidation;
	
	@Transient
	private transient int ndecisionstatus;

	//end-->ALPD-5332
	
	@Override
	public Registration mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Registration objRegistration = new Registration();
		objRegistration.setNgendercode(getInteger(arg0,"ngendercode",arg1));

		objRegistration.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistration.setNsampletypecode(getShort(arg0,"nsampletypecode",arg1));
		objRegistration.setNregtypecode(getShort(arg0,"nregtypecode",arg1));
		objRegistration.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objRegistration.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objRegistration.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objRegistration.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		objRegistration.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objRegistration.setNmaterialcatcode(getInteger(arg0,"nmaterialcatcode",arg1));
		objRegistration.setNmaterialcode(getInteger(arg0,"nmaterialcode",arg1));
		objRegistration.setNtemplatemanipulationcode(getInteger(arg0,"ntemplatemanipulationcode",arg1));
		objRegistration.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objRegistration.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistration.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistration.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objRegistration.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objRegistration.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objRegistration.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objRegistration.setStestname(getString(arg0,"stestname",arg1));
		objRegistration.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objRegistration.setNcategorybasedflow(getInteger(arg0,"ncategorybasedflow",arg1));
		objRegistration.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistration.setSarno(getString(arg0, "sarno", arg1));
		objRegistration.setDtransactiondate(getString(arg0, "dtransactiondate", arg1));
		objRegistration.setDregdate(getString(arg0, "dregdate", arg1));
		objRegistration.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objRegistration.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objRegistration.setNoffset(getInteger(arg0,"noffset",arg1));
		objRegistration.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objRegistration.setNoffsetdregdate(getInteger(arg0,"noffsetdregdate",arg1));
		objRegistration.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objRegistration.setNapprovalversioncode(getInteger(arg0, "napprovalversioncode", arg1));
		objRegistration.setStranscolor(getString(arg0, "stranscolor", arg1));
		objRegistration.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objRegistration.setBflag(getBoolean(arg0, "bflag", arg1));
		objRegistration.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objRegistration.setNregsubtypeversioncode(getInteger(arg0, "nregsubtypeversioncode", arg1));
		objRegistration.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objRegistration.setNisiqcmaterial(getShort(arg0, "nisiqcmaterial", arg1));
		objRegistration.setSsectionname(getString(arg0, "ssectionname", arg1));
		objRegistration.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objRegistration.setSfilename(getString(arg0, "sfilename", arg1));
		objRegistration.setSlno(getInteger(arg0, "slno", arg1));
		objRegistration.setNexternalordertypecode(getInteger(arg0, "nexternalordertypecode", arg1));
		objRegistration.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objRegistration.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objRegistration.setSsamplename(getString(arg0, "ssamplename", arg1));
		objRegistration.setNschedulertransactioncode(getInteger(arg0,"nschedulertransactiontestcode",arg1));
		objRegistration.setNprotocolcode(getInteger(arg0,"nprotocolcode",arg1));
		//start-->ALPD-5332--added by Vignesh R(04-02-2025)-- Material Scheduler
		objRegistration.setNnextcalibration(getShort(arg0,"nnextcalibration",arg1));
		objRegistration.setNnextcalibrationperiod(getShort(arg0,"nnextcalibrationperiod",arg1));
		objRegistration.setSinstrumentid(getString(arg0,"sinstrumentid",arg1));
		objRegistration.setIsnextvalidation(getShort(arg0,"isnextvalidation", arg1));
		objRegistration.setSinventoryid(getString(arg0,"sinventoryid", arg1));
		objRegistration.setNmaterialinventorycode(getInteger(arg0,"nmaterialinventorycode", arg1));
		objRegistration.setNnextvalidationperiod(getInteger(arg0,"nnextvalidationperiod", arg1));
		objRegistration.setNnextvalidation(getInteger(arg0,"nnextvalidation", arg1));
		objRegistration.setNdecisionstatus (getInteger(arg0,"ndecisionstatus", arg1));

		//end-->ALPD-5332

		return objRegistration;
	}

}
