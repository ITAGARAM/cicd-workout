package com.agaramtech.qualis.login.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map fields of 'adsconnectconfig' table of database
 * @since 08-04-2025
 * @version 11.0.0.1
*/

//@Entity
//@Table(name="adsconnectconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AdsConnectConfig extends CustomizedResultsetRowMapper<AdsConnectConfig> implements Serializable, RowMapper<AdsConnectConfig> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nadsconnectconfigcode")
	private int nadsconnectconfigcode;
	
	@Column(name="nactivecode")
	private int nactivecode;
	
	@Column(name="ndeactivecode")
	private int ndeactivecode;
	
	@Column(name="sgroupid",length=50)
	private String sgroupid;
	
	@Column(name="nport")
	private int nport;
	
	@Column(name="sdomainname",length=50)
	private String sdomainname;
	
	@Column(name="ssearchbase",length=50)
	private String ssearchbase;
	
	@Column(name="surl",length=50)
	private String surl;
	
	@Column(name="nstatus")
	private int nstatus;

	@Transient
	private transient String ldAPlocation;
	@Transient
	private transient String ldapserverdomainName;
	@Transient
	private transient String sloginid;
	@Transient
	private transient String spassword; 
	

	
	@Override
	public AdsConnectConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final AdsConnectConfig objAdsConnectConfig = new AdsConnectConfig();
		
		objAdsConnectConfig.setNadsconnectconfigcode(getInteger(arg0,"nadsconnectconfigcode",arg1));
		objAdsConnectConfig.setNactivecode(getInteger(arg0,"nactivecode",arg1));
		objAdsConnectConfig.setNdeactivecode(getInteger(arg0,"ndeactivecode",arg1));
		objAdsConnectConfig.setSgroupid(StringEscapeUtils.unescapeJava(getString(arg0,"sgroupid",arg1)));
		objAdsConnectConfig.setNport(getInteger(arg0,"nport",arg1));
		objAdsConnectConfig.setSdomainname(StringEscapeUtils.unescapeJava(getString(arg0,"sdomainname",arg1)));
		objAdsConnectConfig.setSsearchbase(StringEscapeUtils.unescapeJava(getString(arg0,"ssearchbase",arg1)));
		objAdsConnectConfig.setSurl(StringEscapeUtils.unescapeJava(getString(arg0,"surl",arg1)));
		objAdsConnectConfig.setNstatus(getInteger(arg0,"nstatus",arg1));
		objAdsConnectConfig.setLdAPlocation(getString(arg0,"ldAPlocation",arg1));
		objAdsConnectConfig.setLdapserverdomainName(getString(arg0,"ldapserverdomainName",arg1));
		objAdsConnectConfig.setSloginid(getString(arg0,"sloginid",arg1));
		objAdsConnectConfig.setSpassword(getString(arg0,"spassword",arg1));
		return objAdsConnectConfig;
	}

}
