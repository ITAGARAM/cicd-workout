package com.agaramtech.qualis.misrights.model;

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

/**
 * This class is used to map the fields of 'dashboardrights' table of the
 * Database.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 17-04- 2025
 */

@Entity
@Table(name = "dashboardhomerights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardHomeRights extends CustomizedResultsetRowMapper<DashBoardHomeRights>
		implements Serializable, RowMapper<DashBoardHomeRights> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardhomerightscode")
	private int ndashboardhomerightscode;

	@Column(name = "ndashboardhomeprioritycode", nullable = false)
	private int ndashboardhomeprioritycode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sdashboardhomepagename;

	@Override
	public DashBoardHomeRights mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final DashBoardHomeRights DashBoardHomeRights = new DashBoardHomeRights();
		DashBoardHomeRights.setNstatus(getShort(arg0, "nstatus", arg1));
		DashBoardHomeRights.setNsitecode(getShort(arg0, "nsitecode", arg1));
		DashBoardHomeRights.setNdashboardhomerightscode(getInteger(arg0, "ndashboardhomerightscode", arg1));
		DashBoardHomeRights.setNdashboardhomeprioritycode(getInteger(arg0, "ndashboardhomeprioritycode", arg1));
		DashBoardHomeRights.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		DashBoardHomeRights.setSdashboardhomepagename(getString(arg0, "sdashboardhomepagename", arg1));
		DashBoardHomeRights.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return DashBoardHomeRights;

	}

}
