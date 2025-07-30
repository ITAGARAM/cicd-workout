package com.agaramtech.qualis.dashboard.model;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'chartproptransaction' table of the
 * Database.
 */
@Entity
@Data
@Table(name = "staticchartproptransaction")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaticChartPropTransaction extends CustomizedResultsetRowMapper<StaticChartPropTransaction>
		implements Serializable, RowMapper<StaticChartPropTransaction> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstaticchartproptranscode")
	private int nstaticchartproptranscode;

	@Column(name = "nstaticdashboardtypecode")
	private int nstaticdashboardtypecode;

	@Column(name = "nchartpropertycode")
	private int nchartpropertycode;

	@Column(name = "nseries")
	private int nseries;

	@Column(name = "schartpropvalue", columnDefinition = "nvarchar", length = 255)
	private String schartpropvalue;

	@ColumnDefault("1")
	@Column(name = "nstatus",nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode=(short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String schartpropertyname;

	@Override
	public StaticChartPropTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
		final StaticChartPropTransaction staticChartPropTransaction = new StaticChartPropTransaction();
		staticChartPropTransaction.setNstaticchartproptranscode(getInteger(rs, "nstaticchartproptranscode", rowNum));
		staticChartPropTransaction.setNstaticdashboardtypecode(getInteger(rs, "nstaticdashboardtypecode", rowNum));
		staticChartPropTransaction.setNseries(getInteger(rs, "nseries", rowNum));
		staticChartPropTransaction.setSchartpropvalue(getString(rs, "schartpropvalue", rowNum));
		staticChartPropTransaction.setNstatus(getShort(rs, "nstatus", rowNum));
		staticChartPropTransaction.setSchartpropertyname(getString(rs, "schartpropertyname", rowNum));
		staticChartPropTransaction.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
		staticChartPropTransaction.setNsitecode(getShort(rs, "nsitecode", rowNum));
		return staticChartPropTransaction;
	}
}
