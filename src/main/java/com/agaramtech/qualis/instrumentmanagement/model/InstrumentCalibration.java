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


/**
 * This class is used to map the fields of 'instrumentcalibration' table of the
 * Database.
 */

@Entity
@Table(name = "instrumentcalibration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentCalibration extends CustomizedResultsetRowMapper<InstrumentCalibration> implements Serializable,RowMapper<InstrumentCalibration> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentcalibrationcode")
	private int ninstrumentcalibrationcode;
	
	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "nopenusercode", nullable = false)
	private int nopenusercode;
	
	@ColumnDefault("-1")	
	@Column(name = "ncloseusercode", nullable = false)
	private int ncloseusercode=-1;
	
	@ColumnDefault("-1")
	@Column(name = "ncalibrationstatus", nullable = false)
	private short ncalibrationstatus=(short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "npreregno", nullable = false)
	private int npreregno = -1;
	
	@Column(name = "dduedate")
	private Instant dduedate;
	
	@Column(name = "dopendate")
	private Instant dopendate;
	
	@Column(name = "dlastcalibrationdate")
	private Instant dlastcalibrationdate;
	
	@Column(name = "sopenreason",length = 255)
	private String sopenreason;
	
	@Column(name = "sclosereason",length = 255)
	private String sclosereason;
	
	@Column(name = "dclosedate")
	private Instant dclosedate;
	
	@ColumnDefault("-1")
	@Column(name = "sarno", nullable = true)
	private String sarno = "-1";

	@Column(name = "ntzopendate", nullable = false)
	private short ntzopendate;
	
	@Column(name = "ntzclosedate", nullable = false)
	private short ntzclosedate;
	
	@Column(name = "ntzlastcalibrationdate", nullable = false)
	private short ntzlastcalibrationdate;
	
	@Column(name = "ntzduedate", nullable = false)
	private short ntzduedate;	
	
	@Column(name = "noffsetdduedate", nullable = false)
	private int noffsetdduedate;
	
	@Column(name = "noffsetdopendate", nullable = false)
	private int noffsetdopendate;
	
	@Column(name = "noffsetdlastcalibrationdate", nullable = false)
	private int noffsetdlastcalibrationdate;
	
	@Column(name = "noffsetdclosedate", nullable = false)
	private int noffsetdclosedate;	

	@Column(name="dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String slastcalibrationdate;
	
	@Transient
	private transient String sduedate;
	
	@Transient
	private transient String stzlastcalibrationdate;
	
	@Transient
	private transient String stzduedate;
	
	@Transient
	private transient String sopendate;
	
	@Transient
	private transient String sclosedate;
	
	@Transient
	private transient String sinstrumentid;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String stzopendate;
	
	@Transient
	private transient String stzclosedate;
	
	@Transient
	private transient String scloseusername;
	
	@Transient
	private transient String sopenusername;
	
	@Transient
	private transient String sinstrumentname;
	
	@Transient
	private transient short ntimezonecode;
	
	@Transient
	private transient String stimezoneid;
	
	@Transient
	private transient short ntranscode;
	
	@Transient
	private transient int nusercode;
	
	@Transient
	private transient boolean isreadonly;
	
	@Transient
	private transient String sheadername;
	
	@Transient
	private transient String snextcalibrationperiod;

	
	@Override
	public InstrumentCalibration mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final InstrumentCalibration objInstCallibration = new InstrumentCalibration();
		
		objInstCallibration.setNinstrumentcalibrationcode(getInteger(arg0,"ninstrumentcalibrationcode",arg1));
		objInstCallibration.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objInstCallibration.setNopenusercode(getInteger(arg0,"nopenusercode",arg1));
		objInstCallibration.setNcloseusercode(getInteger(arg0,"ncloseusercode",arg1));
		objInstCallibration.setNcalibrationstatus(getShort(arg0,"ncalibrationstatus",arg1));
		objInstCallibration.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objInstCallibration.setDduedate(getInstant(arg0,"dduedate",arg1));
		objInstCallibration.setDopendate(getInstant(arg0,"dopendate",arg1));
		objInstCallibration.setDlastcalibrationdate(getInstant(arg0,"dlastcalibrationdate",arg1));
		objInstCallibration.setNstatus(getShort(arg0,"nstatus",arg1));
		objInstCallibration.setSopenreason(StringEscapeUtils.unescapeJava(getString(arg0,"sopenreason",arg1)));
		objInstCallibration.setSclosereason(StringEscapeUtils.unescapeJava(getString(arg0,"sclosereason",arg1)));
		objInstCallibration.setDclosedate(getInstant(arg0,"dclosedate",arg1));
		objInstCallibration.setSarno(StringEscapeUtils.unescapeJava(getString(arg0,"sarno",arg1)));
		objInstCallibration.setSlastcalibrationdate(getString(arg0,"slastcalibrationdate",arg1));
		objInstCallibration.setSduedate(getString(arg0,"sduedate",arg1));
		objInstCallibration.setSinstrumentid(getString(arg0,"sinstrumentid",arg1));
		objInstCallibration.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objInstCallibration.setSopendate(getString(arg0,"sopendate",arg1));
		objInstCallibration.setSclosedate(getString(arg0,"sclosedate",arg1));
		objInstCallibration.setStzopendate(getString(arg0,"stzopendate",arg1));
		objInstCallibration.setStzclosedate(getString(arg0,"stzclosedate",arg1));
		objInstCallibration.setNtzopendate(getShort(arg0,"ntzopendate",arg1));
		objInstCallibration.setNtzclosedate(getShort(arg0,"ntzclosedate",arg1));
		objInstCallibration.setNtzlastcalibrationdate(getShort(arg0,"ntzlastcalibrationdate",arg1));
		objInstCallibration.setNtzduedate(getShort(arg0,"ntzduedate",arg1));
		objInstCallibration.setStzduedate(getString(arg0,"stzduedate",arg1));
		objInstCallibration.setStzlastcalibrationdate(getString(arg0,"stzlastcalibrationdate",arg1));
		objInstCallibration.setSopenusername(getString(arg0,"sopenusername",arg1));
		objInstCallibration.setScloseusername(getString(arg0,"scloseusername",arg1));
		objInstCallibration.setSinstrumentname(getString(arg0,"sinstrumentname",arg1));
		objInstCallibration.setNtimezonecode(getShort(arg0,"ntimezonecode",arg1));
		objInstCallibration.setStimezoneid(getString(arg0,"stimezoneid",arg1));
		objInstCallibration.setNtranscode(getShort(arg0,"ntranscode",arg1));
		objInstCallibration.setNusercode(getShort(arg0,"nusercode",arg1));
		objInstCallibration.setIsreadonly(getBoolean(arg0,"isreadonly", arg1));
		objInstCallibration.setNoffsetdduedate(getInteger(arg0,"noffsetdduedate", arg1));
		objInstCallibration.setNoffsetdlastcalibrationdate(getInteger(arg0,"noffsetdlastcalibrationdate", arg1));
		objInstCallibration.setNoffsetdopendate(getInteger(arg0,"noffsetdopendate", arg1));
		objInstCallibration.setNoffsetdclosedate(getInteger(arg0,"noffsetdclosedate", arg1));
		objInstCallibration.setSheadername(getString(arg0,"sheadername",arg1));
		objInstCallibration.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objInstCallibration.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objInstCallibration.setSnextcalibrationperiod(getString(arg0,"snextcalibrationperiod",arg1));
		
		return objInstCallibration;
	}

}
