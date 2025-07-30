package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

/**
 * This class is used to map the fields of 'staticdashboardmaster' table of the
 * Database.
 */
@Entity
@Table(name = "staticdashboardmaster")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaticDashBoardMaster extends CustomizedResultsetRowMapper<StaticDashBoardMaster>
		implements Serializable, RowMapper<StaticDashBoardMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstaticdashboardmastercode")
	private int nstaticdashboardmastercode;

	@Column(name = "sstaticdashboardmastername", length = 50, nullable = false)
	private String sstaticdashboardmastername;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient List<?> staticDBDetailsList;

	@Override
	public StaticDashBoardMaster mapRow(ResultSet rs, int rowNum) throws SQLException {
		final StaticDashBoardMaster staticDashBoardMaster = new StaticDashBoardMaster();
		staticDashBoardMaster.setNstaticdashboardmastercode(getInteger(rs, "nstaticdashboardmastercode", rowNum));
		staticDashBoardMaster.setSstaticdashboardmastername(getString(rs, "sstaticdashboardmastername", rowNum));
		staticDashBoardMaster.setSdescription(getString(rs, "sdescription", rowNum));
		staticDashBoardMaster.setNsitecode(getShort(rs, "nsitecode", rowNum));
		staticDashBoardMaster.setNstatus(getShort(rs, "nstatus", rowNum));
		staticDashBoardMaster.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
		return staticDashBoardMaster;
	}
}
