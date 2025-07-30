package com.agaramtech.qualis.configuration.model;
/**
 * This class is used to map fields of 'sdmselnsettings' table of database
*/
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sdmselnsettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SDMSELNSettings extends CustomizedResultsetRowMapper<SDMSELNSettings>  implements Serializable, RowMapper<SDMSELNSettings> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsettingcode")
	private int nsettingcode;
	
	@Column(name = "ssettingname", length = 100, nullable = false)
	private String ssettingname;
	
	@Column(name = "ssettingvalue", length = 100, nullable = false)
	private String ssettingvalue;
	
	@Column(name = "nstatus", nullable = false)	
	private int nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Override
	public SDMSELNSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SDMSELNSettings objSettings = new SDMSELNSettings();
		
		objSettings.setSsettingname(StringEscapeUtils.unescapeJava(getString(arg0,"ssettingname",arg1)));
		objSettings.setSsettingvalue(StringEscapeUtils.unescapeJava(getString(arg0,"ssettingvalue",arg1)));
		objSettings.setNsettingcode(getInteger(arg0,"nsettingcode",arg1));
		objSettings.setNstatus(getInteger(arg0,"nstatus",arg1));
		objSettings.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSettings.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objSettings;
	}

}