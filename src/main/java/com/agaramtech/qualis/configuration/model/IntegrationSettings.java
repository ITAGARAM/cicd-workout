package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "integrationsettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class IntegrationSettings extends CustomizedResultsetRowMapper<IntegrationSettings> implements Serializable, RowMapper<IntegrationSettings>{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nintegrationcode")
	private short nintegrationcode;
	
	@Column(name = "sdescription", length = 255, nullable = false)
	private String sdescription;
	
	@Column(name = "slinkname", length = 200, nullable = false)	
	private String slinkname;
	
	@Column(name = "sclassurlname", length = 100, nullable = false)	
	private String sclassurlname;
	
	@Column(name = "ninterfacetypecode")
	private short ninterfacetypecode;
	
	@Column(name = "smethodname", length = 100, nullable = false)	
	private String smethodname;
	
	@Column(name = "nstatus", nullable = false)	
	private short nstatus= (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	

	@Override
	public IntegrationSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		final IntegrationSettings objSettings = new IntegrationSettings();
		objSettings.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objSettings.setSlinkname(StringEscapeUtils.unescapeJava(getString(arg0,"slinkname",arg1)));
		objSettings.setNintegrationcode(getShort(arg0,"nintegrationcode",arg1));
		objSettings.setSmethodname(StringEscapeUtils.unescapeJava(getString(arg0,"smethodname",arg1)));
		objSettings.setSclassurlname(StringEscapeUtils.unescapeJava(getString(arg0,"sclassurlname",arg1)));
		objSettings.setNstatus(getShort(arg0,"nstatus",arg1));
		objSettings.setNinterfacetypecode(getShort(arg0,"ninterfacetypecode",arg1));

		return objSettings;
	}

}
