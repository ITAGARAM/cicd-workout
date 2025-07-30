package com.agaramtech.qualis.instrumentmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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

@Entity
@Table(name = "instrument")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Instrument extends CustomizedResultsetRowMapper<Instrument> implements Serializable, RowMapper<Instrument> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentcode")
	private int ninstrumentcode;
	
	@ColumnDefault("-1")
	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ninstrumentlocationcode", nullable = false)
	private int ninstrumentlocationcode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();;
	
	@ColumnDefault("-1")
	@Column(name = "nsuppliercode", nullable = false)
	private int nsuppliercode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();;
	
	@ColumnDefault("-1")
	@Column(name = "nservicecode")
	private int nservicecode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();;
	
	@ColumnDefault("-1")
	@Column(name = "nmanufcode", nullable = false)
	private int nmanufcode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();;
	
	@Column(name = "sinstrumentid", length = 100, nullable = false)
	private String sinstrumentid;
	
	@Column(name = "sinstrumentname", length = 100, nullable = false)
	private String sinstrumentname;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@Column(name = "smodelnumber", length = 100)
	private String smodelnumber;
	
	@Column(name = "spono", length = 100)
	private String spono;
	
	@Column(name = "dmanufacdate")
	private Instant dmanufacdate;
	
	@Column(name = "dpodate")
	private Instant dpodate;
	
	@Column(name = "dreceiveddate")
	private Instant dreceiveddate;
	
	@Column(name = "dinstallationdate")
	private Instant dinstallationdate;
	
	@Column(name = "dexpirydate")
	private Instant dexpirydate;
	
	@Column(name = "dservicedate")
	private Instant dservicedate;
	
	@Column(name = "npurchasecost")
	private Double npurchasecost;
	
	@ColumnDefault("1")
	@Column(name = "ninstrumentstatus", nullable = false)
	private short ninstrumentstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nwindowsperiodminus", nullable = false)
	private short nwindowsperiodminus;
	
	@Column(name = "nwindowsperiodplus", nullable = false)
	private short nwindowsperiodplus;
	
	@Column(name = "nwindowsperiodminusunit", nullable = false)
	private short nwindowsperiodminusunit;
	
	@Column(name = "nwindowsperiodplusunit", nullable = false)
	private short nwindowsperiodplusunit;
	
	@Column(name = "sserialno", length = 255)
	private String sserialno;
	
	@ColumnDefault("-1")
	@Column(name = "nusercode")
	private int nusercode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nregionalsitecode", nullable = false)
	private short nregionalsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nnextcalibration", nullable = false)
	private short nnextcalibration;
	
	@Column(name = "nnextcalibrationperiod", nullable = false)
	private short nnextcalibrationperiod;
	//added by sujatha ALPDJ21-21 
	@Column(name = "nnextmaintenance", nullable = false)
	private short nnextmaintenance;
	
	@Column(name = "nnextmaintenanceperiod", nullable = false)
	private short nnextmaintenanceperiod;
	
	@Column(name = "sremarks", length = 255)
	private String sremarks;
	
	@Column(name = "ssoftwareinformation", length = 255)
	private String ssoftwareinformation;
	
	@Column(name = "sperformancecapabilities", length = 255)
	private String sperformancecapabilities;
	
	@Column(name = "sacceptancecriteria", length = 255)
	private String sacceptancecriteria;
	
	@Column(name = "sassociateddocument", length = 1000)
	private String sassociateddocument;
	
	@Column(name = "smovement", length = 50)
	private String smovement;
	
	@ColumnDefault("-1")
	@Column(name = "ntzmanufdate", nullable = false)
	private short ntzmanufdate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntzpodate", nullable = false)
	private short ntzpodate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntzreceivedate", nullable = false)
	private short ntzreceivedate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntzinstallationdate", nullable = false)
	private short ntzinstallationdate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntzexpirydate", nullable = false)
	private short ntzexpirydate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntzservicedate", nullable = false)
	private short ntzservicedate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "noffsetdmanufacdate", nullable = false)
	private int noffsetdmanufacdate;
	
	@Column(name = "noffsetdpodate", nullable = false)
	private int noffsetdpodate;
	
	@Column(name = "noffsetdreceiveddate", nullable = false)
	private int noffsetdreceiveddate;
	
	@Column(name = "noffsetdinstallationdate", nullable = false)
	private int noffsetdinstallationdate;
	
	@Column(name = "noffsetdexpirydate", nullable = false)
	private int noffsetdexpirydate;
	
	@Column(name = "noffsetdservicedate", nullable = false)
	private int noffsetdservicedate;

	@Column(name="dmodifieddate" ,nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "ninstrumentnamecode")
	private int ninstrumentnamecode;
	
	//Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
	@ColumnDefault("4")
	@Column(name = "nautocalibration", nullable = false)
	private short nautocalibration=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();;
	
	
	@Transient
	private transient  String sactivestatus;
	@Transient
	private transient  short ntranscode;
	@Transient
	private transient  String sinstrumentcatname;
	@Transient
	private transient  String smanufname;
	@Transient
	private transient  String ssuppliername;
	@Transient
	private  transient  int nsectioncode;
	@Transient
	private transient  String ssectionname;
	@Transient
	private transient  String sreceiveddate;
	@Transient
	private transient  String sexpirydate;
	@Transient
	private transient  String smanufacdate;
	@Transient
	private transient  String sinstallationdate;
	@Transient
	private transient  String spodate;
	@Transient
	private transient  int nproductcode;
	@Transient
	private transient  int nproductcatcode;
	@Transient
	private transient   String sproductname;
	@Transient
	private transient  String stzmanufdate;
	@Transient
	private transient  String stzpodate;
	@Transient
	private  transient String stzreceivedate;
	@Transient
	private transient  String stzinstallationdate;
	@Transient
	private transient  String stzexpirydate;
	@Transient
	private transient  String swindowsperiodplusunit;
	@Transient
	private transient  String swindowsperiodminusunit;
	@Transient
	private transient  String snextcalibrationperiod;
	//added by sujatha ALPDJ21-21 
	@Transient
	private transient  String snextmaintenanceperiod;
	@Transient
	private transient  String susername;
	@Transient
	private transient  int nusercode1;
	@Transient
	private transient  String sdefaultstatus;
	@Transient
	private transient  String sserviceby;
	@Transient
	private transient  String speriodname;
	@Transient
	private transient  Instant dcreateddate;
	@Transient
	private transient  int noffsetdcreateddate;
	@Transient
	private transient  short ntzcreateddate;
	@Transient
	private transient  int nfilesize;
	@Transient
	private transient  int ncalibrationreq;
	@Transient
	private  transient String sregionalsitename;
	@Transient
	private transient  String sservicedate;
	@Transient
	private transient  String stzservicedate;
	@Transient
	private transient  String sinstrumentlocationname;
	@Transient
	private transient  Boolean issinstrumentname;
	@Transient
	private transient Boolean isninstrumentid;
	@Transient
	private transient  String sautocalibration;  

	
	@Override
	public Instrument mapRow(ResultSet arg0, int arg1) throws SQLException {

		final Instrument objInstrument = new Instrument();

		objInstrument.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objInstrument.setNmanufcode(getInteger(arg0,"nmanufcode",arg1));
		objInstrument.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		objInstrument.setSinstrumentid(StringEscapeUtils.unescapeJava(getString(arg0,"sinstrumentid",arg1)));
		objInstrument.setSinstrumentname(StringEscapeUtils.unescapeJava(getString(arg0,"sinstrumentname",arg1)));
		objInstrument.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objInstrument.setNsuppliercode(getInteger(arg0,"nsuppliercode",arg1));
		objInstrument.setSmodelnumber(StringEscapeUtils.unescapeJava(getString(arg0,"smodelnumber",arg1)));
		objInstrument.setDmanufacdate(getInstant(arg0,"dmanufacdate",arg1));
		objInstrument.setDpodate(getInstant(arg0,"dpodate",arg1));
		objInstrument.setDreceiveddate(getInstant(arg0,"dreceiveddate",arg1));
		objInstrument.setDinstallationdate(getInstant(arg0,"dinstallationdate",arg1));
		objInstrument.setDexpirydate(getInstant(arg0,"dexpirydate",arg1));
		objInstrument.setNinstrumentstatus(getShort(arg0,"ninstrumentstatus",arg1));
		objInstrument.setNwindowsperiodminus(getShort(arg0,"nwindowsperiodminus",arg1));
		objInstrument.setNwindowsperiodplus(getShort(arg0,"nwindowsperiodplus",arg1));
		objInstrument.setNwindowsperiodminusunit(getShort(arg0,"nwindowsperiodminusunit",arg1));
		objInstrument.setNwindowsperiodplusunit(getShort(arg0,"nwindowsperiodplusunit",arg1));
		objInstrument.setSassociateddocument(StringEscapeUtils.unescapeJava(getString(arg0,"sassociateddocument",arg1)));
		objInstrument.setSmovement(StringEscapeUtils.unescapeJava(getString(arg0,"smovement",arg1)));
		objInstrument.setNnextcalibration(getShort(arg0,"nnextcalibration",arg1));
	   //added by sujatha ALPDJ21-21 
		objInstrument.setNnextmaintenance(getShort(arg0,"nnextmaintenance",arg1));
		objInstrument.setNnextcalibrationperiod(getShort(arg0,"nnextcalibrationperiod",arg1));
		objInstrument.setSnextcalibrationperiod(getString(arg0,"snextcalibrationperiod",arg1));
		objInstrument.setNnextmaintenanceperiod(getShort(arg0,"nnextmaintenanceperiod",arg1));
		objInstrument.setSnextmaintenanceperiod(getString(arg0,"snextmaintenanceperiod",arg1));
		objInstrument.setNusercode(getInteger(arg0,"nusercode",arg1));
		objInstrument.setSserialno(StringEscapeUtils.unescapeJava(getString(arg0,"sserialno",arg1)));
		objInstrument.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0,"sremarks",arg1)));
		objInstrument.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objInstrument.setNservicecode(getInteger(arg0,"nservicecode",arg1));
		objInstrument.setSpono(StringEscapeUtils.unescapeJava(getString(arg0,"spono",arg1)));
		objInstrument.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objInstrument.setNstatus(getShort(arg0,"nstatus",arg1));
		objInstrument.setSactivestatus(getString(arg0,"sactivestatus",arg1));
		objInstrument.setNtranscode(getShort(arg0,"ntranscode",arg1));
		objInstrument.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objInstrument.setSmanufname(getString(arg0,"smanufname",arg1));
		objInstrument.setSsuppliername(getString(arg0,"ssuppliername",arg1));
		objInstrument.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objInstrument.setSsectionname(getString(arg0,"ssectionname",arg1));
		objInstrument.setSreceiveddate(getString(arg0,"sreceiveddate",arg1));
		objInstrument.setSexpirydate(getString(arg0,"sexpirydate",arg1));
		objInstrument.setSmanufacdate(getString(arg0,"smanufacdate",arg1));
		objInstrument.setSinstallationdate(getString(arg0,"sinstallationdate",arg1));
		objInstrument.setSpodate(getString(arg0,"spodate",arg1));
		objInstrument.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objInstrument.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objInstrument.setSproductname(getString(arg0,"sproductname",arg1));
		objInstrument.setNtzmanufdate(getShort(arg0,"ntzmanufdate",arg1));
		objInstrument.setNtzpodate(getShort(arg0,"ntzpodate",arg1));
		objInstrument.setNtzreceivedate(getShort(arg0,"ntzreceivedate",arg1));
		objInstrument.setNtzinstallationdate(getShort(arg0,"ntzinstallationdate",arg1));
		objInstrument.setNtzexpirydate(getShort(arg0,"ntzexpirydate",arg1));
		objInstrument.setStzmanufdate(getString(arg0,"stzmanufdate",arg1));
		objInstrument.setStzpodate(getString(arg0,"stzpodate",arg1));
		objInstrument.setStzreceivedate(getString(arg0,"stzreceivedate",arg1));
		objInstrument.setStzinstallationdate(getString(arg0,"stzinstallationdate",arg1));
		objInstrument.setStzexpirydate(getString(arg0,"stzexpirydate",arg1));
		objInstrument.setSwindowsperiodplusunit(getString(arg0,"swindowsperiodplusunit",arg1));
		objInstrument.setSwindowsperiodminusunit(getString(arg0,"swindowsperiodminusunit",arg1));
		objInstrument.setSusername(getString(arg0,"susername",arg1));
		objInstrument.setNusercode1(getInteger(arg0,"nusercode1",arg1));
		objInstrument.setSdefaultstatus(getString(arg0,"sdefaultstatus",arg1));
		objInstrument.setSserviceby(getString(arg0,"sserviceby",arg1));
		objInstrument.setSperiodname(getString(arg0,"speriodname",arg1));
		objInstrument.setNoffsetdmanufacdate(getInteger(arg0,"noffsetdmanufacdate",arg1));
		objInstrument.setNoffsetdpodate(getInteger(arg0,"noffsetdpodate",arg1));
		objInstrument.setNoffsetdinstallationdate(getInteger(arg0,"noffsetdinstallationdate",arg1));
		objInstrument.setNoffsetdreceiveddate(getInteger(arg0,"noffsetdreceiveddate",arg1));
		objInstrument.setNoffsetdexpirydate(getInteger(arg0,"noffsetdexpirydate",arg1));
		objInstrument.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objInstrument.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objInstrument.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objInstrument.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objInstrument.setNcalibrationreq(getInteger(arg0,"ncalibrationreq",arg1));
		objInstrument.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objInstrument.setNregionalsitecode(getShort(arg0,"nregionalsitecode",arg1));
		objInstrument.setSregionalsitename(getString(arg0,"sregionalsitename",arg1));
		objInstrument.setNinstrumentnamecode(getInteger(arg0,"ninstrumentnamecode",arg1));
		objInstrument.setNinstrumentlocationcode(getInteger(arg0,"ninstrumentlocationcode",arg1));
		objInstrument.setDservicedate(getInstant(arg0,"ddateservice",arg1));
		objInstrument.setSinstrumentlocationname(getString(arg0,"sinstrumentlocationname",arg1));
		objInstrument.setNtzservicedate(getShort(arg0,"ntzservicedate",arg1));
		objInstrument.setNoffsetdservicedate(getInteger(arg0,"noffsetdservicedate",arg1));
		objInstrument.setSacceptancecriteria(StringEscapeUtils.unescapeJava(getString(arg0,"sacceptancecriteria",arg1)));
		objInstrument.setSperformancecapabilities(StringEscapeUtils.unescapeJava(getString(arg0,"sperformancecapabilities",arg1)));
		objInstrument.setSsoftwareinformation(StringEscapeUtils.unescapeJava(getString(arg0,"ssoftwareinformation",arg1)));
		objInstrument.setNpurchasecost(getDouble(arg0,"npurchasecost",arg1));
		objInstrument.setSservicedate(getString(arg0,"sservicedate",arg1));
		objInstrument.setStzservicedate(getString(arg0,"stzservicedate",arg1));
		objInstrument.setIssinstrumentname(getBoolean(arg0,"issinstrumentname",arg1));
		objInstrument.setIsninstrumentid(getBoolean(arg0,"isninstrumentid",arg1));
		objInstrument.setNautocalibration(getShort(arg0,"nautocalibration",arg1));  //Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
		objInstrument.setSautocalibration(getString(arg0,"sautocalibration",arg1));  //Added by sonia on 27th Sept 2024 for Jira idL:ALPD-4939
		
		return objInstrument;
	}

	
}
