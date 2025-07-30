package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
@Table(name = "holidayplanner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CommonHolidays extends CustomizedResultsetRowMapper<CommonHolidays>
		implements Serializable, RowMapper<CommonHolidays> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncommonholidaycode")
	private int ncommonholidaycode = -1;

	@Column(name = "nholidayyearversion", nullable = false)
	private int nholidayyearversion = -1;

	@Column(name = "nyearcode", nullable = false)
	private int nyearcode = -1;

	@Column(name = "nmonday", nullable = false)
	private int nmonday = 4;

	@Column(name = "ntuesday", nullable = false)
	private int ntuesday = 4;

	@Column(name = "nwednesday", nullable = false)
	private int nwednesday = 4;

	@Column(name = "nthursday", nullable = false)
	private int nthursday = 4;

	@Column(name = "nfriday", nullable = false)
	private int nfriday = 4;

	@Column(name = "nsaturday", nullable = false)
	private int nsaturday = 4;

	@Column(name = "nsunday", nullable = false)
	private int nsunday = 4;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public CommonHolidays mapRow(ResultSet arg0, int arg1) throws SQLException {
		final CommonHolidays commonHolidays = new CommonHolidays();
		commonHolidays.setNcommonholidaycode(getInteger(arg0, "ncommonholidaycode", arg1));
		commonHolidays.setNholidayyearversion(getInteger(arg0, "nholidayyearversion", arg1));
		commonHolidays.setNyearcode(getInteger(arg0, "nyearcode", arg1));
		commonHolidays.setNmonday(getInteger(arg0, "nmonday", arg1));
		commonHolidays.setNtuesday(getInteger(arg0, "ntuesday", arg1));
		commonHolidays.setNwednesday(getInteger(arg0, "nwednesday", arg1));
		commonHolidays.setNthursday(getInteger(arg0, "nthursday", arg1));
		commonHolidays.setNfriday(getInteger(arg0, "nfriday", arg1));
		commonHolidays.setNsaturday(getInteger(arg0, "nsaturday", arg1));
		commonHolidays.setNsunday(getInteger(arg0, "nsunday", arg1));
		commonHolidays.setNstatus(getShort(arg0, "nstatus", arg1));
		return commonHolidays;
	}
}
