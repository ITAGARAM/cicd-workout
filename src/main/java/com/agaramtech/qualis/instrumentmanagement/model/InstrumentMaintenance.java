package com.agaramtech.qualis.instrumentmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
 * This class is used to map the fields of 'instrumentmaintenance' table of the Database.
 */
@Entity
@Table(name = "instrumentmaintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentMaintenance extends CustomizedResultsetRowMapper<InstrumentMaintenance> implements Serializable,RowMapper<InstrumentMaintenance>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentmaintenancecode ")
	private int ninstrumentmaintenancecode;
	
	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "dduedate ")
	private Instant dduedate;
	
	@Column(name = "dopendate ")
	private Instant dopendate;
	
	@Column(name = "dlastmaintenancedate ")
	private Instant dlastmaintenancedate;
	
	@ColumnDefault("-1")
	@Column(name = "nopenusercode", nullable = false)
	private int nopenusercode;
	
	@Column(name = "sopenreason",length = 255)
	private String sopenreason;
	
	@ColumnDefault("-1")
	@Column(name = "ncloseusercode ", nullable = false)
	private int ncloseusercode;
	
	@Column(name = "sclosereason",length = 255)
	private String sclosereason;
	
	@Column(name = "dclosedate ")
	private Instant dclosedate;
	
	@Column(name = "nmaintenancestatus ")
	private short nmaintenancestatus;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	
	@Column(name = "ntzopendate", nullable = false)
	private short ntzopendate;
	
	@Column(name = "ntzclosedate", nullable = false)
	private short ntzclosedate;
	
	@Column(name = "ntzlastmaintenancedate", nullable = false)
	private short ntzlastmaintenancedate;
	
	@Column(name = "ntzduedate", nullable = false)
	private short ntzduedate;
	
	@Column(name = "noffsetdduedate", nullable = false)
	private int noffsetdduedate;
	
	@Column(name = "noffsetdopendate", nullable = false)
	private int noffsetdopendate;
	
	@Column(name = "noffsetdlastmaintenancedate", nullable = false)
	private int noffsetdlastmaintenancedate;
	
	@Column(name = "noffsetdclosedate", nullable = false)
	private int noffsetdclosedate;
	
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	
	@Transient
	private transient String slastmaintenancedate;
	
	@Transient
	private transient String sduedate;
	
	@Transient
	private transient String stzlastmaintenancedate;
	
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
	//added by sujatha ALPDJ21-21 
	@Transient
	private transient String snextmaintenanceperiod;
	
	@Override
	public InstrumentMaintenance mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InstrumentMaintenance objInstMaintainence = new InstrumentMaintenance();
		objInstMaintainence.setNinstrumentmaintenancecode(getInteger(arg0,"ninstrumentmaintenancecode",arg1));
		objInstMaintainence.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objInstMaintainence.setDduedate(getInstant(arg0,"dduedate",arg1));
		objInstMaintainence.setDopendate(getInstant(arg0,"dopendate",arg1));
		objInstMaintainence.setDlastmaintenancedate(getInstant(arg0,"dlastmaintenancedate",arg1));
		objInstMaintainence.setNopenusercode(getInteger(arg0,"nopenusercode",arg1));
		objInstMaintainence.setSopenreason(StringEscapeUtils.unescapeJava(getString(arg0,"sopenreason",arg1)));
		objInstMaintainence.setNcloseusercode(getInteger(arg0,"ncloseusercode",arg1));
		objInstMaintainence.setSclosereason(StringEscapeUtils.unescapeJava(getString(arg0,"sclosereason",arg1)));
		objInstMaintainence.setDclosedate(getInstant(arg0,"dclosedate",arg1));
		objInstMaintainence.setNmaintenancestatus(getShort(arg0,"nmaintenancestatus",arg1));
		objInstMaintainence.setNstatus(getShort(arg0,"nstatus",arg1));
		objInstMaintainence.setSlastmaintenancedate(getString(arg0,"slastmaintenancedate",arg1));
		objInstMaintainence.setSduedate(getString(arg0,"sduedate",arg1));
		objInstMaintainence.setSinstrumentid(getString(arg0,"sinstrumentid",arg1));
		objInstMaintainence.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objInstMaintainence.setSopendate(getString(arg0,"sopendate",arg1));
		objInstMaintainence.setSclosedate(getString(arg0,"sclosedate",arg1));
		objInstMaintainence.setStzopendate(getString(arg0,"stzopendate",arg1));
		objInstMaintainence.setStzclosedate(getString(arg0,"stzclosedate",arg1));
		objInstMaintainence.setNtzopendate(getShort(arg0,"ntzopendate",arg1));
		objInstMaintainence.setNtzclosedate(getShort(arg0,"ntzclosedate",arg1));
		objInstMaintainence.setNtzlastmaintenancedate(getShort(arg0,"ntzlastmaintenancedate",arg1));
		objInstMaintainence.setNtzduedate(getShort(arg0,"ntzduedate",arg1));
		objInstMaintainence.setStzduedate(getString(arg0,"stzduedate",arg1));
		objInstMaintainence.setStzlastmaintenancedate(getString(arg0,"stzlastmaintenancedate",arg1));
		objInstMaintainence.setSopenusername(getString(arg0,"sopenusername",arg1));
		objInstMaintainence.setScloseusername(getString(arg0,"scloseusername",arg1));
		objInstMaintainence.setSinstrumentname(getString(arg0,"sinstrumentname",arg1));
		objInstMaintainence.setNtimezonecode(getShort(arg0,"ntimezonecode",arg1));
		objInstMaintainence.setStimezoneid(getString(arg0,"stimezoneid",arg1));
		objInstMaintainence.setNtranscode(getShort(arg0,"ntranscode",arg1));
		objInstMaintainence.setNusercode(getShort(arg0,"nusercode",arg1));
		objInstMaintainence.setIsreadonly(getBoolean(arg0,"isreadonly", arg1));
		objInstMaintainence.setNoffsetdduedate(getInteger(arg0,"noffsetdduedate", arg1));
		objInstMaintainence.setNoffsetdlastmaintenancedate(getInteger(arg0,"noffsetdlastmaintenancedate", arg1));
		objInstMaintainence.setNoffsetdopendate(getInteger(arg0,"noffsetdopendate", arg1));
		objInstMaintainence.setNoffsetdclosedate(getInteger(arg0,"noffsetdclosedate", arg1));
		objInstMaintainence.setSheadername(getString(arg0,"sheadername",arg1));
		objInstMaintainence.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objInstMaintainence.setNsitecode(getShort(arg0,"nsitecode",arg1));
	   //added by sujatha ALPDJ21-21 
		objInstMaintainence.setSnextmaintenanceperiod(getString(arg0,"snextmaintenanceperiod",arg1));

		return objInstMaintainence;
	}

}
