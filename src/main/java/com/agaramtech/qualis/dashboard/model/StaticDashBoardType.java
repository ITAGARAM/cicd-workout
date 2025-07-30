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
@Table(name = "staticdashboardtype")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaticDashBoardType extends CustomizedResultsetRowMapper<StaticDashBoardType>
		implements Serializable, RowMapper<StaticDashBoardType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstaticdashboardtypecode")
	private int nstaticdashboardtypecode;

	@Column(name = "nstaticdashboardcode", nullable = false)
	private int nstaticdashboardcode;

	@Column(name = "ncharttypecode", nullable = false)
	private int ncharttypecode;

	@Column(name = "squery", columnDefinition = "text", nullable = false)
	private String squery;

	@Column(name = "nsorter", nullable = false)
	@ColumnDefault("0")
	private int nsorter = Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public StaticDashBoardType mapRow(ResultSet rs, int rowNum) throws SQLException {
		final StaticDashBoardType staticDashBoardType = new StaticDashBoardType();
		staticDashBoardType.setNstaticdashboardtypecode(getInteger(rs, "nstaticdashboardtypecode", rowNum));
		staticDashBoardType.setNstaticdashboardcode(getInteger(rs, "nstaticdashboardcode", rowNum));
		staticDashBoardType.setNcharttypecode(getInteger(rs, "ncharttypecode", rowNum));
		staticDashBoardType.setSquery(getString(rs, "squery", rowNum));
		staticDashBoardType.setNsorter(getInteger(rs, "nsorter", rowNum));
		staticDashBoardType.setNstatus(getShort(rs, "nstatus", rowNum));
		staticDashBoardType.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
		staticDashBoardType.setNsitecode(getShort(rs, "nsitecode", rowNum));
		return staticDashBoardType;
	}
}
