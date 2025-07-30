package com.agaramtech.qualis.submitter.model;

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
@Table(name = "institutionsite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstitutionSite extends CustomizedResultsetRowMapper<InstitutionSite> implements Serializable, RowMapper<InstitutionSite> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstitutionsitecode")
	private int ninstitutionsitecode;

	@Column(name = "ninstitutioncode")
	private int ninstitutioncode;

	@Column(name = "nregionalsitecode")
	private int nregionalsitecode;

	@Column(name = "ncitycode")
	private int ncitycode;

	@Column(name = "nregioncode")
	private int nregioncode;

	@Column(name = "ndistrictcode")
	private int ndistrictcode;

	@Column(name = "ncountrycode")
	private int ncountrycode;

	@Column(name = "sinstitutionsitename", length = 100, nullable = false)
	private String sinstitutionsitename;

	@Column(name = "sinstitutionsiteaddress", length = 255, nullable = false)
	private String sinstitutionsiteaddress;

	@Column(name = "szipcode", length = 20, nullable = false)
	private String szipcode;

	@Column(name = "sstate", length = 100, nullable = false)
	private String sstate;

	@Column(name = "stelephone", length = 20, nullable = false)
	private String stelephone;

	@Column(name = "sfaxno", length = 20, nullable = false)
	private String sfaxno;

	@Column(name = "semail", length = 100, nullable = false)
	private String semail;

	@Column(name = "swebsite", length = 100, nullable = false)
	private String swebsite;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "scityname", length = 50, nullable = false)
	private String scityname;

	@Transient
	private transient String sinstitutionname;

	@Transient
	private transient String ssitename;

	@Transient
	private transient String ssitecode;

	@Transient
	private transient String scitycode;

	@Transient
	private transient String scountryname;

	@Transient
	private transient String sregionname;

	@Transient
	private transient String sdistrictname;

	@Transient
	private transient String sregioncode;

	@Transient
	private transient String sdistrictcode;

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient int ninstitutioncatcode;

	@Override
	public InstitutionSite mapRow(ResultSet arg0, int arg1) throws SQLException {
		InstitutionSite objInstitutionSite = new InstitutionSite();
		objInstitutionSite.setNinstitutionsitecode(getInteger(arg0, "ninstitutionsitecode", arg1));
		objInstitutionSite.setNinstitutioncode(getInteger(arg0, "ninstitutioncode", arg1));
		objInstitutionSite.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objInstitutionSite.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		objInstitutionSite.setSinstitutionsitename(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutionsitename", arg1)));
		objInstitutionSite.setSinstitutionsiteaddress(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutionsiteaddress", arg1)));
		objInstitutionSite.setSzipcode(StringEscapeUtils.unescapeJava(getString(arg0, "szipcode", arg1)));
		objInstitutionSite.setSstate(StringEscapeUtils.unescapeJava(getString(arg0, "sstate", arg1)));
		objInstitutionSite.setStelephone(StringEscapeUtils.unescapeJava(getString(arg0, "stelephone", arg1)));
		objInstitutionSite.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0, "sfaxno", arg1)));
		objInstitutionSite.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objInstitutionSite.setSwebsite(StringEscapeUtils.unescapeJava(getString(arg0, "swebsite", arg1)));
		objInstitutionSite.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInstitutionSite.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInstitutionSite.setNstatus(getShort(arg0, "nstatus", arg1));
		objInstitutionSite.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objInstitutionSite.setSsitename(getString(arg0, "ssitename", arg1));
		objInstitutionSite.setSsitecode(getString(arg0, "ssitecode", arg1));
		objInstitutionSite.setScityname(StringEscapeUtils.unescapeJava(getString(arg0, "scityname", arg1)));
		objInstitutionSite.setScitycode(getString(arg0, "scitycode", arg1));
		objInstitutionSite.setScountryname(getString(arg0, "scountryname", arg1));
		objInstitutionSite.setNregionalsitecode(getInteger(arg0, "nregionalsitecode", arg1));
		objInstitutionSite.setSregionname(getString(arg0, "sregionname", arg1));
		objInstitutionSite.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objInstitutionSite.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objInstitutionSite.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objInstitutionSite.setSregioncode(getString(arg0, "sregioncode", arg1));
		objInstitutionSite.setSdistrictcode(getString(arg0, "sdistrictcode", arg1));
		objInstitutionSite.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objInstitutionSite.setNinstitutioncatcode(getInteger(arg0, "ninstitutioncatcode", arg1));
		return objInstitutionSite;
	}
}
