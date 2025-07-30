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
@Table(name = "holidayyearversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HolidayYearVersion extends CustomizedResultsetRowMapper<HolidayYearVersion>
		implements Serializable, RowMapper<HolidayYearVersion> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nholidayyearversion")
	private int nholidayyearversion = -1;
	
	@Column(name = "nversioncode", nullable = false)
	private int nversioncode = 0;
	
	@Column(name = "nyearcode", nullable = false)
	private int nyearcode = -1;
	
	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus = Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sversionno;
	
	@Transient
	private transient String stransdisplaystatus;

	@Override
	public HolidayYearVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		final HolidayYearVersion holidayYear = new HolidayYearVersion();
		holidayYear.setNholidayyearversion(getInteger(arg0, "nholidayyearversion", arg1));
		holidayYear.setNyearcode(getInteger(arg0, "nyearcode", arg1));
		holidayYear.setNversioncode(getInteger(arg0, "nversioncode", arg1));
		holidayYear.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		holidayYear.setNstatus(getShort(arg0, "nstatus", arg1));
		holidayYear.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		holidayYear.setSversionno(getString(arg0, "sversionno", arg1));
		return holidayYear;
	}
}
