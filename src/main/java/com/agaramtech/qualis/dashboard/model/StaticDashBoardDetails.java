package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

/**
 * This class is used to map the fields of 'staticdashboarddetails' table of the
 * Database.
 */
@Entity
@Table(name = "staticdashboarddetails")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaticDashBoardDetails extends CustomizedResultsetRowMapper<StaticDashBoardDetails>
		implements Serializable, RowMapper<StaticDashBoardDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstaticdashboardcode")
	private int nstaticdashboardcode;

	@Column(name = "nstaticdashboardmastercode", nullable = false)
	private int nstaticdashboardmastercode;

	@Column(name = "sstaticdashboardname", length = 50, nullable = false)
	private String sstaticdashboardname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nisshowprimaryserver", nullable = false)
	@ColumnDefault("4")
	private short nisshowprimaryserver = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Override
	public StaticDashBoardDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		final StaticDashBoardDetails staticDashBoardDetails = new StaticDashBoardDetails();
		staticDashBoardDetails.setNstaticdashboardcode(getInteger(rs, "nstaticdashboardcode", rowNum));
		staticDashBoardDetails.setNstaticdashboardmastercode(getInteger(rs, "nstaticdashboardmastercode", rowNum));
		staticDashBoardDetails.setSstaticdashboardname(getString(rs, "sstaticdashboardname", rowNum));
		staticDashBoardDetails.setSdescription(getString(rs, "sdescription", rowNum));
		staticDashBoardDetails.setNstatus(getShort(rs, "nstatus", rowNum));
		staticDashBoardDetails.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
		staticDashBoardDetails.setNsitecode(getShort(rs, "nsitecode", rowNum));
		staticDashBoardDetails.setNisshowprimaryserver(getShort(rs, "nisshowprimaryserver", rowNum));
		return staticDashBoardDetails;
	}
}
