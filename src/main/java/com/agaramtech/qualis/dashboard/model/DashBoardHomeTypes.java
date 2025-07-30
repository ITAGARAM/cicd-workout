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
 * This class is used to map the fields of 'DashBoardHomeTypes' table of the
 * Database.
 * 
 * @author ATE090
 * @version 9.0.0.1
 * @since 25- Jan- 2021
 */
@Entity
@Table(name = "dashboardhometypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardHomeTypes extends CustomizedResultsetRowMapper<DashBoardHomeTypes>
		implements Serializable, RowMapper<DashBoardHomeTypes> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardhometypecode")
	private int ndashboardhometypecode;

	@Column(name = "ndashboardhomeprioritycode", nullable = false)
	private int ndashboardhomeprioritycode;

	@Column(name = "ndashboardtypecode", nullable = false)
	private int ndashboardtypecode;

	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = -1;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	private transient String sdashboardtypename;

	@Override
	public DashBoardHomeTypes mapRow(ResultSet arg0, int arg1) throws SQLException {

		DashBoardHomeTypes dashBoardHomeTypes = new DashBoardHomeTypes();
		dashBoardHomeTypes.setNdashboardhometypecode(getInteger(arg0, "ndashboardhometypecode", arg1));
		dashBoardHomeTypes.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		dashBoardHomeTypes.setNdashboardhomeprioritycode(getInteger(arg0, "ndashboardhomeprioritycode", arg1));
		dashBoardHomeTypes.setNsorter(getShort(arg0, "nsorter", arg1));
		dashBoardHomeTypes.setSdashboardtypename(getString(arg0, "sdashboardtypename", arg1));
		dashBoardHomeTypes.setNstatus(getShort(arg0, "nstatus", arg1));
		dashBoardHomeTypes.setNsitecode(getShort(arg0, "nsitecode", arg1));
		dashBoardHomeTypes.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return dashBoardHomeTypes;
	}

}
