package com.agaramtech.qualis.configuration.model;

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
@Table(name = "adssettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ADSSettings extends CustomizedResultsetRowMapper<ADSSettings> implements Serializable, RowMapper<ADSSettings> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nadssettingscode")
	private int nadssettingscode;
	
	@Column(name = "sservername", length = 50, nullable = false)
	private String sservername;
	
	@Column(name = "sdomainname", length = 50, nullable = false)
	private String sdomainname;
	
	@Column(name = "sorganisationunit", length = 50)
	private String sorganisationunit;
	
	@Column(name = "sgroupname", length = 50)
	private String sgroupname;

	@Column(name = "sldaplink", length = 150, nullable = false)
	private String sldaplink;
	
	@Column(name = "suserid", length = 50, nullable = false)
	private String suserid;

	@Column(name = "spassword", length = 50)
	private String spassword;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nldapstatus", nullable = false)
	private short nldapstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String sloginid;
	
	@Override
	public ADSSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ADSSettings objADSsettings = new ADSSettings();
		objADSsettings.setNadssettingscode(getInteger(arg0, "nadssettingscode", arg1));
		objADSsettings.setSservername(StringEscapeUtils.unescapeJava(getString(arg0, "sservername", arg1)));
		objADSsettings.setSdomainname(StringEscapeUtils.unescapeJava(getString(arg0, "sdomainname", arg1)));
		objADSsettings.setSorganisationunit(StringEscapeUtils.unescapeJava(getString(arg0, "sorganisationunit", arg1)));
		objADSsettings.setSgroupname(StringEscapeUtils.unescapeJava(getString(arg0, "sgroupname", arg1)));
		objADSsettings.setSldaplink(StringEscapeUtils.unescapeJava(getString(arg0, "sldaplink", arg1)));
		objADSsettings.setSuserid(StringEscapeUtils.unescapeJava(getString(arg0, "suserid", arg1)));
		objADSsettings.setSpassword(StringEscapeUtils.unescapeJava(getString(arg0, "spassword", arg1)));
		objADSsettings.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objADSsettings.setNldapstatus(getShort(arg0, "nldapstatus", arg1));
		objADSsettings.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objADSsettings.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objADSsettings.setNstatus(getShort(arg0, "nstatus", arg1));
		objADSsettings.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objADSsettings.setSloginid(getString(arg0, "sloginid", arg1));
		
		return objADSsettings;
	}

}
