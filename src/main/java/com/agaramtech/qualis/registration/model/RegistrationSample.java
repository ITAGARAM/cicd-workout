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
 * This class is used to map the fields of 'registrationsample' table of the Database.
 */
@Entity
@Table(name = "registrationsample")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSample extends CustomizedResultsetRowMapper<RegistrationSample> implements Serializable,RowMapper<RegistrationSample> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransactionsamplecode")
	private int ntransactionsamplecode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;


	@Column(name = "ncomponentcode", nullable = false)
	private int ncomponentcode;

	@Column(name = "nspecsampletypecode", nullable = false)
	private int nspecsampletypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int slno;
	
	@Transient
	private transient int nallottedspeccode;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient int ntransactionstatus;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String stranscolor;
	
	@Transient
	private transient String scomponentname;
	
	@Transient
	private transient String sexternalsampleid;
	
	@Transient
	private transient String formData;
	
	@Transient
	private transient String ssystemfilename;
	
	@Transient
	private transient String sfilename;
	
	@Transient
	private transient String dsamplecollectiondatetime;
	
	@Transient
	private transient String ssampleappearance;
	
	@Transient
	private transient int nsampleappearancecode;
	@Transient
	private transient boolean isneedsample;


	@Override
	public RegistrationSample mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationSample objRegistrationSample = new RegistrationSample();
		
		objRegistrationSample.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objRegistrationSample.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationSample.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objRegistrationSample.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objRegistrationSample.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationSample.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objRegistrationSample.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objRegistrationSample.setSlno(getInteger(arg0,"slno",arg1));
		objRegistrationSample.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objRegistrationSample.setSarno(getString(arg0, "sarno", arg1));
		objRegistrationSample.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objRegistrationSample.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistrationSample.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objRegistrationSample.setStranscolor(getString(arg0,"stranscolor",arg1));
		objRegistrationSample.setScomponentname(getString(arg0,"scomponentname",arg1));
		objRegistrationSample.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationSample.setSexternalsampleid(getString(arg0,"sexternalsampleid",arg1));
		objRegistrationSample.setFormData(getString(arg0,"formData",arg1));
		objRegistrationSample.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objRegistrationSample.setSfilename(getString(arg0, "sfilename", arg1));
		objRegistrationSample.setDsamplecollectiondatetime(getString(arg0, "dsamplecollectiondatetime", arg1));
		objRegistrationSample.setSsampleappearance(getString(arg0, "ssampleappearance", arg1));
		objRegistrationSample.setNsampleappearancecode(getInteger(arg0,"nsampleappearancecode",arg1));
		objRegistrationSample.setIsneedsample(getBoolean(arg0,"isneedsample",arg1));

		return objRegistrationSample;
	}
}
