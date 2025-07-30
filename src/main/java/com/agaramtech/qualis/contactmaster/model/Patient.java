package com.agaramtech.qualis.contactmaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

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
@Table(name = "patientmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Patient extends CustomizedResultsetRowMapper<Patient> implements Serializable, RowMapper<Patient> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "spatientid", length = 50, nullable = false)
	private String spatientid;

	@Column(name = "sfirstname", length = 100, nullable = false)
	private String sfirstname;

	@Column(name = "slastname", length = 100, nullable = false)
	private String slastname;

	@Column(name = "sfathername", length = 100)
	private String sfathername;

	@Column(name = "ngendercode", nullable = false)
	private short ngendercode;
	
	@Column(name = "ncitycode", nullable = false)
	@ColumnDefault("-1")
	private int ncitycode = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nneedmigrant", nullable = false)
	@ColumnDefault("4")
	private short nneedmigrant = 4;
	
	@Column(name = "ncountrycode", nullable = false)
	@ColumnDefault("-1")
	private int ncountrycode = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nregioncode", nullable = false)
	@ColumnDefault("-1")
	private int nregioncode = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndistrictcode", nullable = false)
	@ColumnDefault("-1")
	private int ndistrictcode = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ddob", nullable = false)
	private Date ddob;
	
	@Column(name = "sage", length = 50, nullable = false)
	private String sage;
	
	@Column(name = "spostalcode", length = 20)
	private String spostalcode;
	
	@Column(name = "sstreet", length = 100, nullable = false)
	private String sstreet;
	
	@Column(name = "shouseno", length = 20, nullable = false)
	private String shouseno;
	
	@Column(name = "sflatno", length = 20, nullable = false)
	private String sflatno;
	
	@Column(name = "nneedcurrentaddress", nullable = false)
	@ColumnDefault("4")
	private short nneedcurrentaddress = 4;
	
	@Column(name = "nregioncodetemp")
	@ColumnDefault("-1")
	private int nregioncodetemp = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndistrictcodetemp")
	@ColumnDefault("-1")
	private int ndistrictcodetemp = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ncitycodetemp")
	@ColumnDefault("-1")
	private int ncitycodetemp = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "spostalcodetemp", length = 20)
	private String spostalcodetemp;
	
	@Column(name = "sstreettemp", length = 100, nullable = false)
	private String sstreettemp;
	
	@Column(name = "shousenotemp", length = 20, nullable = false)
	private String shousenotemp;
	
	@Column(name = "sflatnotemp", length = 20)
	private String sflatnotemp;
	
	@Column(name = "sphoneno", length = 20)
	private String sphoneno;
	
	@Column(name = "smobileno", length = 20)
	private String smobileno;
	
	@Column(name = "semail", length = 100)
	private String semail;
	
	@Column(name = "srefid", length = 255)
	private String srefid;
	
	@Column(name = "spassportno", length = 50)
	private String spassportno;
	
	@Column(name = "sexternalid", length = 255)
	private String sexternalid;
	
	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "scityname", length = 50, nullable = false)
	private String scityname;
	
	@Column(name = "scitynametemp", length = 50, nullable = false)
	private String scitynametemp;

	@Transient
	private transient String sdob;

	@Transient
	private transient String spatientname;

	@Transient
	private transient String sgendername;

	@Transient
	private transient String scountryname;

	@Transient
	private transient String sdistrictname;

	@Transient
	private transient String sregionname;

	@Transient
	private transient String sregionnametemp;

	@Transient
	private transient String sdistrictnametemp;

	@Transient
	private transient String sdisplaystatus;

	@Transient
	private transient String scurrentaddress;

	@Transient
	private transient String currentdate;

	@Transient
	private transient int npatientfiltercode;

	@Transient
	private transient String spatientfiltername;

	@Transient
	private transient Map<String, Object> jsondata;

	@Transient
	private transient String tree;

	@Transient
	private transient Map<String, Object> config;

	@Transient
	private transient String filterquery;

	@Transient
	private transient int nfilterstatus;

	@Transient
	private transient String sfilterstatus;

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient String sarno;

	@Transient
	private transient String sregdate;

	@Transient
	private transient String scompletedate;

	@Transient
	private transient String sreportno;

	@Transient
	private transient String stransactiondate;

	@Transient
	private transient int npreregno;

	@Transient
	private transient String ssamplearno;

	@Transient
	private transient int noffsetdtransactiondate;

	@Transient
	private transient String stestsynonym;

	@Transient
	private transient String dcollectiondate;

	@Transient
	private transient String ssubmitterid;

	@Transient
	private transient String ssubmitterfirstname;

	@Transient
	private transient String ssubmitterlastname;

	@Transient
	private transient String ssubmitteremail;

	@Transient
	private transient String sshortname;

	@Transient
	private transient String ssubmittercode;

	@Transient
	private transient String stelephone;

	@Transient
	private transient String sinstitutionsitename;

	@Transient
	private transient String sinstitutioncityname;

	@Transient
	private transient String sinstitutiondistrictname;
 
	@Transient
	private transient String sinstitutiondistrictcode;

	@Transient
	private transient String sinstitutionname;

	@Transient
	private transient String sinstitutioncatname;

	@Transient
	private transient String sinstitutioncode;

	@Transient
	private transient String sdiagnosticcasename;

	@Transient
	private transient String ssubmittershortname;

	@Transient
	private transient String ssubmittersemail;

	@Transient
	private transient String ssubmittertelephone;

	@Transient
	private transient String sinscityname;

	@Transient
	private transient String sinsdistrictname;

	@Transient
	private transient String sinsdistrictcode;

	@Transient
	private transient String ssitecode;

	@Transient
	private transient String sinstitutiondepartment;

	@Transient
	private transient String sinstitutionregion;

	@Transient
	private transient String sinstitutioncountry;

	@Transient
	private transient String ssubmittermobileno;

	@Override
	public Patient mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Patient objPatient = new Patient();
		objPatient.setSpatientid(StringEscapeUtils.unescapeJava(getString(arg0, "spatientid", arg1)));
		objPatient.setSfirstname(StringEscapeUtils.unescapeJava(getString(arg0, "sfirstname", arg1)));
		objPatient.setSlastname(StringEscapeUtils.unescapeJava(getString(arg0, "slastname", arg1)));
		objPatient.setSfathername(StringEscapeUtils.unescapeJava(getString(arg0, "sfathername", arg1)));
		objPatient.setNgendercode(getShort(arg0, "ngendercode", arg1));
		objPatient.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objPatient.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		objPatient.setDdob(getDate(arg0, "ddob", arg1));
		objPatient.setSage(StringEscapeUtils.unescapeJava(getString(arg0, "sage", arg1)));
		objPatient.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objPatient.setSpostalcode(StringEscapeUtils.unescapeJava(getString(arg0, "spostalcode", arg1)));
		objPatient.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0, "sphoneno", arg1)));
		objPatient.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0, "smobileno", arg1)));
		objPatient.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objPatient.setSrefid(StringEscapeUtils.unescapeJava(getString(arg0, "srefid", arg1)));
		objPatient.setSpassportno(StringEscapeUtils.unescapeJava(getString(arg0, "spassportno", arg1)));
		objPatient.setSexternalid(StringEscapeUtils.unescapeJava(getString(arg0, "sexternalid", arg1)));
		objPatient.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objPatient.setNstatus(getShort(arg0, "nstatus", arg1));
		objPatient.setSpatientname(getString(arg0, "spatientname", arg1));
		objPatient.setSgendername(getString(arg0, "sgendername", arg1));
		objPatient.setScountryname(getString(arg0, "scountryname", arg1));
		objPatient.setScityname(StringEscapeUtils.unescapeJava(getString(arg0, "scityname", arg1)));
		objPatient.setSdob(getString(arg0, "sdob", arg1));
		objPatient.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objPatient.setSstreet(StringEscapeUtils.unescapeJava(getString(arg0, "sstreet", arg1)));
		objPatient.setSstreettemp(StringEscapeUtils.unescapeJava(getString(arg0, "sstreettemp", arg1)));
		objPatient.setShousenotemp(StringEscapeUtils.unescapeJava(getString(arg0, "shousenotemp", arg1)));
		objPatient.setShouseno(StringEscapeUtils.unescapeJava(getString(arg0, "shouseno", arg1)));
		objPatient.setSflatno(StringEscapeUtils.unescapeJava(getString(arg0, "sflatno", arg1)));
		objPatient.setSflatnotemp(StringEscapeUtils.unescapeJava(getString(arg0, "sflatnotemp", arg1)));
		objPatient.setNcitycodetemp(getInteger(arg0, "ncitycodetemp", arg1));
		objPatient.setNregioncodetemp(getInteger(arg0, "nregioncodetemp", arg1));
		objPatient.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objPatient.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objPatient.setNdistrictcodetemp(getInteger(arg0, "ndistrictcodetemp", arg1));
		objPatient.setNneedcurrentaddress(getShort(arg0, "nneedcurrentaddress", arg1));
		objPatient.setNneedmigrant(getShort(arg0, "nneedmigrant", arg1));
		objPatient.setSpostalcodetemp(StringEscapeUtils.unescapeJava(getString(arg0, "spostalcodetemp", arg1)));
		objPatient.setSregionnametemp(getString(arg0, "sregionnametemp", arg1));
		objPatient.setScitynametemp(StringEscapeUtils.unescapeJava(getString(arg0, "scitynametemp", arg1)));
		objPatient.setSdistrictnametemp(getString(arg0, "sdistrictnametemp", arg1));
		objPatient.setSregionname(getString(arg0, "sregionname", arg1));
		objPatient.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objPatient.setScurrentaddress(getString(arg0, "scurrentaddress", arg1));
		objPatient.setCurrentdate(getString(arg0, "currentdate", arg1));
		objPatient.setSpatientfiltername(getString(arg0, "spatientfiltername", arg1));
		objPatient.setNpatientfiltercode(getInteger(arg0, "npatientfiltercode", arg1));
		objPatient.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objPatient.setTree(getString(arg0, "tree", arg1));
		objPatient.setConfig(getJsonObject(arg0, "config", arg1));
		objPatient.setFilterquery(getString(arg0, "filterquery", arg1));
		objPatient.setNfilterstatus(getInteger(arg0, "nfilterstatus", arg1));
		objPatient.setSfilterstatus(getString(arg0, "sfilterstatus", arg1));
		objPatient.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objPatient.setSarno(getString(arg0, "sarno", arg1));
		objPatient.setSregdate(getString(arg0, "sregdate", arg1));
		objPatient.setScompletedate(getString(arg0, "scompletedate", arg1));
		objPatient.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objPatient.setSreportno(getString(arg0, "sreportno", arg1));
		objPatient.setDcollectiondate(getString(arg0, "dcollectiondate", arg1));
		objPatient.setSsubmitterfirstname(getString(arg0, "ssubmitterfirstname", arg1));
		objPatient.setSsubmitterlastname(getString(arg0, "ssubmitterlastname", arg1));
		objPatient.setSsubmittershortname(getString(arg0, "ssubmittershortname", arg1));
		objPatient.setSsubmittercode(getString(arg0, "ssubmittercode", arg1));
		objPatient.setSsubmittersemail(getString(arg0, "ssubmittersemail", arg1));
		objPatient.setSsubmitterid(getString(arg0, "ssubmitterid", arg1));
		objPatient.setSsubmittertelephone(getString(arg0, "ssubmittertelephone", arg1));
		objPatient.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objPatient.setSinstitutioncatname(getString(arg0, "sinstitutioncatname", arg1));
		objPatient.setSinstitutioncode(getString(arg0, "sinstitutioncode", arg1));
		objPatient.setSinstitutionsitename(getString(arg0, "sinstitutionsitename", arg1));
		objPatient.setSinscityname(getString(arg0, "sinscityname", arg1));
		objPatient.setSinsdistrictname(getString(arg0, "sinsdistrictname", arg1));
		objPatient.setSinsdistrictcode(getString(arg0, "sinsdistrictcode", arg1));
		objPatient.setSsitecode(getString(arg0, "ssitecode", arg1));
		objPatient.setSinstitutiondepartment(getString(arg0, "sinstitutiondepartment", arg1));
		objPatient.setSinstitutionregion(getString(arg0, "sinstitutionregion", arg1));
		objPatient.setSinstitutioncountry(getString(arg0, "sinstitutioncountry", arg1));
		objPatient.setSsubmittermobileno(getString(arg0, "ssubmittermobileno", arg1));

		return objPatient;
	}

}