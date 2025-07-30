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
@Table(name = "chartproptransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChartPropTransaction extends CustomizedResultsetRowMapper<ChartPropTransaction>
		implements Serializable, RowMapper<ChartPropTransaction> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nchartproptranscode")
	private int nchartproptranscode;

	@Column(name = "ndashboardtypecode", nullable = false)
	private int ndashboardtypecode;

	@Column(name = "nchartpropertycode", nullable = false)
	private short nchartpropertycode;

	@Column(name = "nseries", nullable = false)
	private short nseries;

	@Column(name = "schartpropvalue", length = 255, nullable = false)
	private String schartpropvalue;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String schartpropertyname;

	@Override
	public ChartPropTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ChartPropTransaction chartPropTransaction = new ChartPropTransaction();

		chartPropTransaction.setNchartpropertycode(getShort(arg0, "nchartpropertycode", arg1));
		chartPropTransaction.setNchartproptranscode(getInteger(arg0, "nchartproptranscode", arg1));
		chartPropTransaction.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		chartPropTransaction.setNseries(getShort(arg0, "nseries", arg1));
		chartPropTransaction.setSchartpropvalue(StringEscapeUtils.unescapeJava(getString(arg0, "schartpropvalue", arg1)));
		chartPropTransaction.setNstatus(getShort(arg0, "nstatus", arg1));
		chartPropTransaction.setSchartpropertyname(getString(arg0, "schartpropertyname", arg1));
		chartPropTransaction.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		chartPropTransaction.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return chartPropTransaction;
	}

}
