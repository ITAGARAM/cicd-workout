package com.agaramtech.qualis.login.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TblADSSettings extends CustomizedResultsetRowMapper<TblADSSettings> implements Serializable, RowMapper<TblADSSettings> {
	
	private static final long serialVersionUID = 1L;

	@Transient
	private transient String ldAPlocation;
	@Transient
	private transient String ldapserverdomainName;
	@Transient
	private transient String sloginid;
	@Transient
	private transient String spassword; 
	

	@Override
	public TblADSSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TblADSSettings objAdsConnectConfig = new TblADSSettings();
		objAdsConnectConfig.setLdAPlocation(getString(arg0,"ldAPlocation",arg1));
		objAdsConnectConfig.setLdapserverdomainName(getString(arg0,"ldapserverdomainName",arg1));
		objAdsConnectConfig.setSloginid(getString(arg0,"sloginid",arg1));
		objAdsConnectConfig.setSpassword(getString(arg0,"spassword",arg1));

		return objAdsConnectConfig;
	}

}
