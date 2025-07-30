package com.agaramtech.qualis.configuration.model;
/**
 * This class is used to map fields of 'licenseconfiguration' table of database
*/
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
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

@Entity
@Table(name = "licenseconfiguration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LicenseConfiguration extends CustomizedResultsetRowMapper<LicenseConfiguration> implements Serializable, RowMapper<LicenseConfiguration> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nlicenseconfigcode")
	private int nlicenseconfigcode;
	
	@Column(name = "nlicensetypecode", nullable = false)	
	private int nlicensetypecode;
	
	//ALPD-4169
	@Column(name = "snooflicense", nullable = false)	
	private String snooflicense;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)	
	private int nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable = false)	
	private short ntransactionstatus = (short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Transient
	private transient String slicensetypename;
	@Transient
	private transient String sversionstatus;
	
	
	@Override
	public LicenseConfiguration mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final LicenseConfiguration objLicenseConfiguration = new LicenseConfiguration();
		
		objLicenseConfiguration.setNlicenseconfigcode(getInteger(arg0, "nlicenseconfigcode", arg1));
		objLicenseConfiguration.setNlicensetypecode(getInteger(arg0, "nlicensetypecode", arg1));
		objLicenseConfiguration.setSnooflicense(StringEscapeUtils.unescapeJava(getString(arg0, "snooflicense", arg1)));
		objLicenseConfiguration.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		objLicenseConfiguration.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));		
		objLicenseConfiguration.setNstatus(getShort(arg0, "nstatus", arg1));
		objLicenseConfiguration.setSlicensetypename(getString(arg0,"slicensetypename",arg1));
		objLicenseConfiguration.setSversionstatus(getString(arg0,"sversionstatus",arg1));
		objLicenseConfiguration.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objLicenseConfiguration.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));

		return objLicenseConfiguration;
		
	}
}
