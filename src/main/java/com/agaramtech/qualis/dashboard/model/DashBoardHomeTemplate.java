package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'DashBoardHomeTemplate' table of the
 * Database.
 * 
 * @author ATE090
 * @version 9.0.0.1
 * @since 25- Jan- 2021
 */
@Entity
@Table(name = "dashboardhometemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardHomeTemplate extends CustomizedResultsetRowMapper<DashBoardHomeTemplate>
		implements Serializable, RowMapper<DashBoardHomeTemplate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardhometemplatecode")
	private int ndashboardhometemplatecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "ndashboardhomeorientation", nullable = false)
	private int ndashboardhomeorientation;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	private transient String sdashboardhometemplatename;

	@Override
	public DashBoardHomeTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {

		DashBoardHomeTemplate dashBoardHomeTemplate = new DashBoardHomeTemplate();

		dashBoardHomeTemplate.setNdashboardhometemplatecode(getInteger(arg0, "ndashboardhometemplatecode", arg1));
		dashBoardHomeTemplate.setSdashboardhometemplatename(getString(arg0, "sdashboardhometemplatename", arg1));
		dashBoardHomeTemplate.setNdashboardhomeorientation(getInteger(arg0, "ndashboardhomeorientation", arg1));
		dashBoardHomeTemplate.setNstatus(getShort(arg0, "nstatus", arg1));
		dashBoardHomeTemplate.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		dashBoardHomeTemplate.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return dashBoardHomeTemplate;
	}

}
