package com.agaramtech.qualis.credential.model;

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
 * This class is used to map fields of 'site' table of database
 * 
 * @author ATE142
 * @version 9.0.0.1
 * @since 25-06-2020
 */

@Entity
@Table(name = "site")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Site extends CustomizedResultsetRowMapper<Site> implements Serializable, RowMapper<Site> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitecode")
	private short nsitecode;

	@Column(name = "ntimezonecode", nullable = false)
	private short ntimezonecode;

	@Column(name = "ndateformatcode", nullable = false)
	private short ndateformatcode;

	@Column(name = "ssitename", length = 100, nullable = false)
	private String ssitename;

	@Column(name = "ssitecode", length = 5, nullable = false)
	private String ssitecode;

	@Column(name = "ssiteaddress", length = 255)
	private String ssiteaddress;

	@Column(name = "scontactperson", length = 100)
	private String scontactperson;

	@Column(name = "sphoneno", length = 50)
	private String sphoneno;

	@Column(name = "sfaxno", length = 50)
	private String sfaxno;

	@Column(name = "semail", length = 50)
	private String semail;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();	

	@ColumnDefault("4")
	@Column(name = "nismultisite", nullable = false)
	private short nismultisite = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();	

	@Column(name = "nmastersitecode", nullable = false)
	private short nmastersitecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Column(name = "nregioncode")
	private int nregioncode;

	@Column(name = "ndistrictcode")
	private int ndistrictcode;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient int nusercode;
	
	@Transient
	private transient String sdatetimeformat;
	
	@Transient
	private transient String stimezoneid;
	
	@Transient
	private transient String sgmtoffset;
	
	@Transient
	private transient String spgdatetimeformat;
	
	@Transient
	private transient String sdateformat;
	
	@Transient
	private transient String sdistributedstatus;
	
	@Transient
	private transient short nisstandaloneserver;
	
	@Transient
	private transient String sregionname;
	
	@Transient
	private transient String sdistrictname;
	
	@Transient
	private transient String utcconversationstatus;
	
	@Transient
	private transient Short nutcconversation;
	
	@Transient
	private transient String needutcconversation;
	
	@Transient
	private transient String sprimarysereverstatus;
	
	@Transient
	private transient Short nissyncserver;
	
	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient String ssitetypename;
	
	@Transient
	private transient String ssiteconfigname;

	@Transient
	private transient int nsitetypecode;
	
	@Transient
	private transient short nhierarchicalorderno;

	@Override
	public Site mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Site objSite = new Site();
		
		objSite.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSite.setNtimezonecode(getShort(arg0, "ntimezonecode", arg1));
		objSite.setSsitename(StringEscapeUtils.unescapeJava(getString(arg0, "ssitename", arg1)));
		objSite.setSsiteaddress(StringEscapeUtils.unescapeJava(getString(arg0, "ssiteaddress", arg1)));
		objSite.setScontactperson(StringEscapeUtils.unescapeJava(getString(arg0, "scontactperson", arg1)));
		objSite.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0, "sphoneno", arg1)));
		objSite.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0, "sfaxno", arg1)));
		objSite.setSemail(StringEscapeUtils.unescapeJava(getString(arg0, "semail", arg1)));
		objSite.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objSite.setNismultisite(getShort(arg0, "nismultisite", arg1));
		objSite.setNstatus(getShort(arg0, "nstatus", arg1));
		objSite.setNusercode(getInteger(arg0, "nusercode", arg1));
		objSite.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objSite.setSdatetimeformat(getString(arg0, "sdatetimeformat", arg1));
		objSite.setSpgdatetimeformat(getString(arg0, "spgdatetimeformat", arg1));
		objSite.setStimezoneid(getString(arg0, "stimezoneid", arg1));
		objSite.setSgmtoffset(getString(arg0, "sgmtoffset", arg1));
		objSite.setNmastersitecode(getShort(arg0, "nmastersitecode", arg1));
		objSite.setSsitecode(StringEscapeUtils.unescapeJava(getString(arg0, "ssitecode", arg1)));
		objSite.setNdateformatcode(getShort(arg0, "ndateformatcode", arg1));
		objSite.setSdateformat(getString(arg0, "sdateformat", arg1));
		objSite.setSdistributedstatus(getString(arg0, "sdistributedstatus", arg1));
		objSite.setNisstandaloneserver(getShort(arg0, "nisstandaloneserver", arg1));
		objSite.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSite.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objSite.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objSite.setSregionname(getString(arg0, "sregionname", arg1));
		objSite.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objSite.setNutcconversation(getShort(arg0, "nutcconversation", arg1));
		objSite.setUtcconversationstatus(getString(arg0, "utcconversationstatus", arg1));
		objSite.setNeedutcconversation(getString(arg0, "needutcconversation", arg1));
		objSite.setSprimarysereverstatus(getString(arg0, "sprimarysereverstatus", arg1));
		objSite.setNissyncserver(getShort(arg0, "nissyncserver", arg1));
		objSite.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objSite.setNsitetypecode(getInteger(arg0, "nsitetypecode", arg1));
		objSite.setSsitetypename(getString(arg0, "ssitetypename", arg1));
		objSite.setSsiteconfigname(getString(arg0, "ssiteconfigname", arg1));
		objSite.setNhierarchicalorderno(getShort(arg0, "nhierarchicalorderno", arg1));

		return objSite;
	}	

}
