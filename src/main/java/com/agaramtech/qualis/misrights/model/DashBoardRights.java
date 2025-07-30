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
@Table(name = "dashboardrights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardRights extends CustomizedResultsetRowMapper<DashBoardRights> implements Serializable, RowMapper<DashBoardRights> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardtranscode")
	private int ndashboardtranscode;

	@Column(name = "ndashboardtypecode", nullable = false)
	private int ndashboardtypecode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter = (short) Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nhomechartview", nullable = false)
	private short nhomechartview = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdashboardtypename;

	@Override
	public DashBoardRights mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final DashBoardRights DashBoardRights = new DashBoardRights();

		DashBoardRights.setNstatus(getShort(arg0, "nstatus", arg1));
		DashBoardRights.setNsitecode(getShort(arg0, "nsitecode", arg1));
		DashBoardRights.setNdashboardtranscode(getInteger(arg0, "ndashboardtranscode", arg1));
		DashBoardRights.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		DashBoardRights.setNhomechartview(getShort(arg0, "nhomechartview", arg1));
		DashBoardRights.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		DashBoardRights.setNsorter(getShort(arg0, "nsorter", arg1));
		DashBoardRights.setSdashboardtypename(getString(arg0, "sdashboardtypename", arg1));
		DashBoardRights.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return DashBoardRights;

	}

}
