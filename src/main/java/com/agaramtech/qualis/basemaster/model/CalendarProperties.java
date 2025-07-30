package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Table(name = "calendersettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class CalendarProperties extends CustomizedResultsetRowMapper<CalendarProperties>
		implements Serializable, RowMapper<CalendarProperties> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncalendersettingcode")
	private int ncalendersettingcode;

	@Column(name = "scalendersettingname", length = 100, nullable = false)
	private String scalendersettingname;

	@Column(name = "scalendersettingvalue", length = 100, nullable = false)
	private String scalendersettingvalue;

	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public CalendarProperties mapRow(ResultSet arg0, int arg1) throws SQLException {
		final CalendarProperties calendarProperties = new CalendarProperties();
		calendarProperties.setNcalendersettingcode(getInteger(arg0, "ncalendersettingcode", arg1));
		calendarProperties.setScalendersettingname(getString(arg0, "scalendersettingname", arg1));
		calendarProperties.setScalendersettingvalue(getString(arg0, "scalendersettingvalue", arg1));
		calendarProperties.setNstatus(getShort(arg0, "nstatus", arg1));
		return calendarProperties;
	}
}
