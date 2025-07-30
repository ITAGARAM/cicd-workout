package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Table(name = "holidaysyear")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HolidaysYear extends CustomizedResultsetRowMapper<HolidaysYear>
		implements Serializable, RowMapper<HolidaysYear> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nyearcode")
	private int nyearcode = -1;

	@Column(name = "syear", length = 4, nullable = false)
	private String syear = "";

	@Column(name = "sdescription", length = 250)
	private String sdescription = "";

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public HolidaysYear mapRow(ResultSet arg0, int arg1) throws SQLException {
		final HolidaysYear holidaysYear = new HolidaysYear();
		holidaysYear.setNyearcode(getInteger(arg0, "nyearcode", arg1));
		holidaysYear.setSyear(getString(arg0, "syear", arg1));
		holidaysYear.setSdescription(getString(arg0, "sdescription", arg1));
		holidaysYear.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		holidaysYear.setNstatus(getInteger(arg0, "nstatus", arg1));
		return holidaysYear;
	}
}
