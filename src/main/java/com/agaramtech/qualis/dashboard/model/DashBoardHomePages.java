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
 * This class is used to map the fields of 'DashBoardHomePages' table of the
 * Database.
 * 
 * @author ATE090
 * @version 9.0.0.1
 * @since 25- Jan- 2021
 */
@Entity
@Table(name = "dashboardhomepages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardHomePages extends CustomizedResultsetRowMapper<DashBoardHomePages>
		implements Serializable, RowMapper<DashBoardHomePages> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardhomepagecode")
	private int ndashboardhomepagecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	private transient String sdashboardhomepagename;

	@Override
	public DashBoardHomePages mapRow(ResultSet arg0, int arg1) throws SQLException {
		DashBoardHomePages dashBoardHomePages = new DashBoardHomePages();

		dashBoardHomePages.setNdashboardhomepagecode(getInteger(arg0, "ndashboardhomepagecode", arg1));
		dashBoardHomePages.setSdashboardhomepagename(getString(arg0, "sdashboardhomepagename", arg1));
		dashBoardHomePages.setNstatus(getShort(arg0, "nstatus", arg1));
		dashBoardHomePages.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		dashBoardHomePages.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		dashBoardHomePages.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return dashBoardHomePages;
	}


}
