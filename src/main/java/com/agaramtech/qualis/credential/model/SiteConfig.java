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

@Entity
@Table(name = "siteconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

/**
 * This class is used to map the fields of 'siteconfig' table of the Database.
 */
public class SiteConfig extends CustomizedResultsetRowMapper<SiteConfig> implements Serializable, RowMapper<SiteConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitecode")
	private short nsitecode;

	@Column(name = "ssitedatetime", length = 30)
	private String ssitedatetime;

	@Column(name = "ssitereportdatetime", length = 30)
	private String ssitereportdatetime;

	@Column(name = "spgdatetime", length = 30)
	private String spgdatetime;

	@Column(name = "spgreportdatetime", length = 30)
	private String spgreportdatetime;

	@Column(name = "ssitereportdate", length = 30)
	private String ssitereportdate;

	@Column(name = "ssitedate", length = 30)
	private String ssitedate;

	@ColumnDefault("4")
	@Column(name = "nisstandaloneserver", nullable = false)
	private short nisstandaloneserver = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("4")
	@Column(name = "nissyncserver", nullable = false)
	private short nissyncserver = (short) Enumeration.TransactionStatus.NO.gettransactionstatus(); 
	
	@ColumnDefault("-1")
	@Column(name = "nsitetypecode", nullable = false)
	private int nsitetypecode = Enumeration.TransactionStatus.NA.gettransactionstatus(); ; 
	

	@Transient
	private transient String needutcconversation;

	@Transient
	private transient String ssitecode;
	


	@Override
	public SiteConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SiteConfig objSiteConfig = new SiteConfig();
		objSiteConfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSiteConfig.setSsitedatetime(StringEscapeUtils.unescapeJava(getString(arg0, "ssitedatetime", arg1)));
		objSiteConfig.setSsitereportdatetime(StringEscapeUtils.unescapeJava(getString(arg0, "ssitereportdatetime", arg1)));
		objSiteConfig.setSsitedate(StringEscapeUtils.unescapeJava(getString(arg0, "ssitedate", arg1)));
		objSiteConfig.setSsitereportdate(StringEscapeUtils.unescapeJava(getString(arg0, "ssitereportdate", arg1)));
		objSiteConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		objSiteConfig.setSpgdatetime(StringEscapeUtils.unescapeJava(getString(arg0, "spgdatetime", arg1)));
		objSiteConfig.setSpgreportdatetime(StringEscapeUtils.unescapeJava(getString(arg0, "spgreportdatetime", arg1)));
		objSiteConfig.setNisstandaloneserver(getShort(arg0, "nisstandaloneserver", arg1));
		objSiteConfig.setNeedutcconversation(getString(arg0, "needutcconversation", arg1));
		objSiteConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSiteConfig.setNissyncserver(getShort(arg0, "nissyncserver", arg1));
		objSiteConfig.setSsitecode(getString(arg0, "ssitecode", arg1));
		objSiteConfig.setNsitetypecode(getInteger(arg0, "nsitetypecode", arg1));

		return objSiteConfig;

	}

}
