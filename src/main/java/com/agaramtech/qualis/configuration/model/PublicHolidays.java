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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'unit' table of the Database.
 */
@Entity
@Table(name = "holidayplanner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PublicHolidays extends CustomizedResultsetRowMapper<PublicHolidays>
		implements Serializable, RowMapper<PublicHolidays> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npublicholidaycode")
	private int npublicholidaycode = -1;

	@Column(name = "nholidayyearversion", nullable = false)
	private int nholidayyearversion = -1;

	@Column(name = "nyearcode", nullable = false)
	private int nyearcode = -1;

	@Column(name = "ddate", nullable = false)
	private Instant ddate;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@ColumnDefault("-1")
	@Column(name = "ntzddate", nullable = false)
	private int ntzddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	public transient String sversiondate = "";

	@Transient
	public transient String sdate = "";

	@Transient
	public transient String stzddate = "";

	@Transient
	private transient Instant fdate;

	@Transient
	private transient int sno = -1;

	@Transient
	private transient String fday = "";

	@Transient
	private transient int dayno = -1;

	@Transient
	private transient int year = -1;

	@Transient
	private transient int day = -1;

	@Transient
	private transient int date = -1;

	@Override
	public PublicHolidays mapRow(ResultSet arg0, int arg1) throws SQLException {		
		final PublicHolidays publicHolidays = new PublicHolidays();
		publicHolidays.setNpublicholidaycode(getInteger(arg0, "npublicholidaycode", arg1));
		publicHolidays.setNholidayyearversion(getInteger(arg0, "nholidayyearversion", arg1));
		publicHolidays.setNyearcode(getInteger(arg0, "nyearcode", arg1));
		publicHolidays.setDdate(getInstant(arg0, "ddate", arg1));
		publicHolidays.setSdescription(getString(arg0, "sdescription", arg1));
		publicHolidays.setNstatus(getShort(arg0, "nstatus", arg1));
		publicHolidays.setSversiondate(getString(arg0, "sversiondate", arg1));
		publicHolidays.setSdate(getString(arg0, "sdate", arg1));
		publicHolidays.setNtzddate(getInteger(arg0, "ntzddate", arg1));
		publicHolidays.setStzddate(getString(arg0, "stzddate", arg1));
		publicHolidays.setDate(getInteger(arg0, "date", arg1));
		publicHolidays.setDay(getInteger(arg0, "day", arg1));
		publicHolidays.setDayno(getInteger(arg0, "dayno", arg1));
		publicHolidays.setSno(getInteger(arg0, "sno", arg1));
		publicHolidays.setFdate(getInstant(arg0, "fdate", arg1));
		publicHolidays.setFday(getString(arg0, "fday", arg1));
		return publicHolidays;
	}
}
