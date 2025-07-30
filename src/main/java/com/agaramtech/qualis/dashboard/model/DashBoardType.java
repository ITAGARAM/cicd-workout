package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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

@Entity
@Table(name = "dashboardtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardType extends CustomizedResultsetRowMapper<DashBoardType> implements Serializable, RowMapper<DashBoardType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboardtypecode")
	private int ndashboardtypecode;

	@Column(name = "sdashboardtypename", length = 255, nullable = false)
	private String sdashboardtypename;

	@Column(name = "nquerycode", nullable = false)
	private int nquerycode;

	@Column(name = "ncharttypecode", nullable = false)
	private short ncharttypecode;

	@Column(name = "nseriesno", nullable = false)
	private short nseriesno;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String schartname;
	@Transient
	private transient String ssqlqueryname;
	@Transient
	private transient int nsqlquerycode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public DashBoardType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final DashBoardType dashBoardType = new DashBoardType();

		dashBoardType.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		dashBoardType.setSdashboardtypename(StringEscapeUtils.unescapeJava(getString(arg0, "sdashboardtypename", arg1)));
		dashBoardType.setNquerycode(getInteger(arg0, "nquerycode", arg1));
		dashBoardType.setNcharttypecode(getShort(arg0, "ncharttypecode", arg1));
		dashBoardType.setNseriesno(getShort(arg0, "nseriesno", arg1));
		dashBoardType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		dashBoardType.setSchartname(getString(arg0, "schartname", arg1));
		dashBoardType.setNstatus(getShort(arg0, "nstatus", arg1));
		dashBoardType.setSsqlqueryname(getString(arg0, "ssqlqueryname", arg1));
		dashBoardType.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		dashBoardType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return dashBoardType;
	}

}