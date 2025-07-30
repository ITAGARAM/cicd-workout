package com.agaramtech.qualis.configuration.model;
/**
 * This class is used to map fields of 'reportsettings' table of database
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reportsettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportSettings extends CustomizedResultsetRowMapper<ReportSettings> implements Serializable,RowMapper<ReportSettings> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nreportsettingcode")
	private short nreportsettingcode;

	@Column(name = "ssettingname", length = 50, nullable = false)
	private String ssettingname;

	@Column(name = "ssettingvalue", length = 255, nullable = false)	
	private String ssettingvalue;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nisvisible", nullable = false)	
	private short nisvisible = (short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient int nsitecode;

@Override
public ReportSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ReportSettings reportSettings = new ReportSettings();
		reportSettings.setSsettingname(StringEscapeUtils.unescapeJava(getString(arg0,"ssettingname",arg1)));
		reportSettings.setSsettingvalue(StringEscapeUtils.unescapeJava(getString(arg0,"ssettingvalue",arg1)));
		reportSettings.setNreportsettingcode(getShort(arg0,"nreportsettingcode",arg1));
		reportSettings.setNstatus(getShort(arg0,"nstatus",arg1));
		reportSettings.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		reportSettings.setNsitecode(getInteger(arg0,"nsitecode",arg1));
		reportSettings.setNisvisible(getShort(arg0,"nisvisible",arg1));
		return reportSettings;
}

}
