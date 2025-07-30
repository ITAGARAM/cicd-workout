package com.agaramtech.qualis.testgroup.model;

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
 * This class is used to map the fields of 'testgroupspecification' table of the Database.
 */
@Entity
@Table(name = "testgroupspecification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupSpecification extends CustomizedResultsetRowMapper<TestGroupSpecification> implements Serializable, RowMapper<TestGroupSpecification> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nallottedspeccode")
	private int nallottedspeccode;
	
	@Column(name = "ntemplatemanipulationcode", nullable = false)
	private int ntemplatemanipulationcode;
	
	@Column(name = "napproveconfversioncode", nullable = false)	
	private int napproveconfversioncode;
	
	@ColumnDefault("4")
	@Column(name = "ncomponentrequired", nullable = false)
	private short ncomponentrequired=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "sspecname", length = 100, nullable = false)
	private String sspecname;
	
	@Column(name = "sversion", length = 20)
	private String sversion;
	
	@ColumnDefault("8")
	@Column(name = "napprovalstatus", nullable = false)
	private short napprovalstatus =(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	
	@Column(name = "dexpirydate")
	private Instant dexpirydate;
	
	@Column(name = "noffsetdexpirydate")
	private int noffsetdexpirydate;
	
	@ColumnDefault("-1")
	@Column(name = "ntzexpirydate", nullable = false)
	private short ntzexpirydate =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;	
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sapprovalstatus;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sexpirydate;
	
	@Transient
	private transient int ndate;
	
	@Transient
	private transient String stimezoneid;
	
	@Transient
	private transient String scomponentrequired;
	
	@Transient
	private transient String stzexpirydate;
	
	@Transient
	private transient String scolorhexcode;
	
	@Transient
	private transient String sactiondisplaystatus;
	
	@Transient
	private transient int nspecsampletypecode;
	
	@Transient
	private transient String sclinicalrequired;
	
	@Transient
	private transient String sprojectcode;
	
	@Transient
	private transient int nclinicaltyperequired;

	@Override
	public TestGroupSpecification mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final TestGroupSpecification objTestGroupSpecification = new TestGroupSpecification();
		
		objTestGroupSpecification.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestGroupSpecification.setNtemplatemanipulationcode(getInteger(arg0,"ntemplatemanipulationcode",arg1));
		objTestGroupSpecification.setNapproveconfversioncode(getInteger(arg0,"napproveconfversioncode",arg1));
		objTestGroupSpecification.setNcomponentrequired(getShort(arg0,"ncomponentrequired",arg1));
		objTestGroupSpecification.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objTestGroupSpecification.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objTestGroupSpecification.setSspecname(StringEscapeUtils.unescapeJava(getString(arg0,"sspecname",arg1)));
		objTestGroupSpecification.setSversion(StringEscapeUtils.unescapeJava(getString(arg0,"sversion",arg1)));
		objTestGroupSpecification.setDexpirydate(getInstant(arg0,"dexpirydate",arg1));
		objTestGroupSpecification.setNtzexpirydate(getShort(arg0,"ntzexpirydate",arg1));
		objTestGroupSpecification.setNapprovalstatus(getShort(arg0,"napprovalstatus",arg1));
		objTestGroupSpecification.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupSpecification.setSapprovalstatus(getString(arg0,"sapprovalstatus",arg1));
		objTestGroupSpecification.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestGroupSpecification.setSexpirydate(getString(arg0,"sexpirydate",arg1));
		objTestGroupSpecification.setNdate(getInteger(arg0,"ndate",arg1));
		objTestGroupSpecification.setStimezoneid(getString(arg0,"stimezoneid",arg1));
		objTestGroupSpecification.setScomponentrequired(getString(arg0,"scomponentrequired",arg1));
		objTestGroupSpecification.setStzexpirydate(getString(arg0,"stzexpirydate",arg1));
		objTestGroupSpecification.setScolorhexcode(getString(arg0,"scolorhexcode",arg1));
		objTestGroupSpecification.setSactiondisplaystatus(getString(arg0,"sactiondisplaystatus",arg1));
		objTestGroupSpecification.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		objTestGroupSpecification.setNoffsetdexpirydate(getInteger(arg0,"noffsetdexpirydate",arg1));
		objTestGroupSpecification.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupSpecification.setSprojectcode(getString(arg0,"sprojectcode",arg1));
		objTestGroupSpecification.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestGroupSpecification.setNclinicaltyperequired(getInteger(arg0,"nclinicaltyperequired",arg1));

		return objTestGroupSpecification;
	}
	

}
