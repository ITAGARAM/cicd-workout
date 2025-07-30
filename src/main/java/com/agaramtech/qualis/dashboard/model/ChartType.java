package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "charttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChartType extends CustomizedResultsetRowMapper<ChartType> implements Serializable, RowMapper<ChartType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncharttypecode")
	private short ncharttypecode;

	@Column(name = "nseries", nullable = false)
	private short nseries = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Transient
	private transient String schartname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sdisplayname;

	@Override
	public ChartType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ChartType chartType = new ChartType();

		chartType.setNcharttypecode(getShort(arg0, "ncharttypecode", arg1));
		chartType.setSchartname(getString(arg0, "schartname", arg1));
		chartType.setNseries(getShort(arg0, "nseries", arg1));
		chartType.setNstatus(getShort(arg0, "nstatus", arg1));
		chartType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		chartType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		chartType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		chartType.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		chartType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		return chartType;
	}

}
