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

@Entity
@Table(name = "holidayplanner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserBasedHolidays extends CustomizedResultsetRowMapper<UserBasedHolidays>
		implements Serializable, RowMapper<UserBasedHolidays> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nuserbasedholidaycode")
	private int nuserbasedholidaycode = -1;
	
	@Column(name = "nholidayyearversion", nullable = false)
	private int nholidayyearversion = -1;
	
	@Column(name = "nyearcode", nullable = false)
	private int nyearcode = -1;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode = -1;
	
	@Column(name = "ddate", nullable = false)
	private Instant ddate;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	
	@ColumnDefault("-1")
	@Column(name = "ntzddate", nullable = false)
	private int ntzddate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	public transient String sversiondate = "";
	
	@Transient
	public transient String sdate = "";
	
	@Transient
	public transient String stzddate = "";
	
	@Transient
	public transient String susername = "";
	
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
	public UserBasedHolidays mapRow(ResultSet arg0, int arg1) throws SQLException {
		UserBasedHolidays userHolidays = new UserBasedHolidays();
		userHolidays.setNuserbasedholidaycode(getInteger(arg0, "nuserbasedholidaycode", arg1));
		userHolidays.setNholidayyearversion(getInteger(arg0, "nholidayyearversion", arg1));
		userHolidays.setNyearcode(getInteger(arg0, "nyearcode", arg1));
		userHolidays.setDdate(getInstant(arg0, "ddate", arg1));
		userHolidays.setSdescription(getString(arg0, "sdescription", arg1));
		userHolidays.setNstatus(getInteger(arg0, "nstatus", arg1));
		userHolidays.setSversiondate(getString(arg0, "sversiondate", arg1));
		userHolidays.setSdate(getString(arg0, "sdate", arg1));
		userHolidays.setNtzddate(getInteger(arg0, "ntzddate", arg1));
		userHolidays.setStzddate(getString(arg0, "stzddate", arg1));
		userHolidays.setDate(getInteger(arg0, "date", arg1));
		userHolidays.setDay(getInteger(arg0, "day", arg1));
		userHolidays.setDayno(getInteger(arg0, "dayno", arg1));
		userHolidays.setSno(getInteger(arg0, "sno", arg1));
		userHolidays.setFdate(getInstant(arg0, "fdate", arg1));
		userHolidays.setFday(getString(arg0, "fday", arg1));
		userHolidays.setNusercode(getInteger(arg0, "nusercode", arg1));
		userHolidays.setSusername(getString(arg0, "susername", arg1));
		return userHolidays;
	}
}
