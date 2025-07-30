package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'DashBoardHomePriority' table of the
 * Database.
 * 
 * @author ATE090
 * @version 9.0.0.1
 * @since 25- Jan- 2021
 */
@Entity
@Table(name = "dashboardhomepriority")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardHomePriority extends CustomizedResultsetRowMapper<DashBoardHomePriority>
		implements Serializable, RowMapper<DashBoardHomePriority> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardhomeprioritycode")
	private int ndashboardhomeprioritycode;

	@Column(name = "ndashboardhometemplatecode", nullable = false)
	private int ndashboardhometemplatecode;

	@Column(name = "ndashboardhomepagecode", nullable = false)
	private int ndashboardhomepagecode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = -1;

	private transient String sdashboardhomepagename;
	private transient String sdashboardhometemplatename;
	private transient String suserrolename;
	private transient int ndashboardtypecode;
	private transient int npagedashboardindex;

	@Override
	public DashBoardHomePriority mapRow(ResultSet arg0, int arg1) throws SQLException {

		final DashBoardHomePriority dashBoardHomePriority = new DashBoardHomePriority();

		dashBoardHomePriority.setNdashboardhomepagecode(getInteger(arg0, "ndashboardhomepagecode", arg1));
		dashBoardHomePriority.setNdashboardhometemplatecode(getInteger(arg0, "ndashboardhometemplatecode", arg1));
		dashBoardHomePriority.setNdashboardhomeprioritycode(getInteger(arg0, "ndashboardhomeprioritycode", arg1));
		dashBoardHomePriority.setNstatus(getShort(arg0, "nstatus", arg1));
		dashBoardHomePriority.setSdashboardhomepagename(getString(arg0, "sdashboardhomepagename", arg1));
		dashBoardHomePriority.setSdashboardhometemplatename(getString(arg0, "sdashboardhometemplatename", arg1));
		dashBoardHomePriority.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		dashBoardHomePriority.setSdashboardhometemplatename(getString(arg0, "sdashboardhometemplatename", arg1));
		dashBoardHomePriority.setSuserrolename(getString(arg0, "suserrolename", arg1));
		dashBoardHomePriority.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		dashBoardHomePriority.setNpagedashboardindex(getInteger(arg0, "npagedashboardindex", arg1));
		dashBoardHomePriority.setNsitecode(getShort(arg0, "nsitecode", arg1));
		dashBoardHomePriority.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return dashBoardHomePriority;
	}

}
