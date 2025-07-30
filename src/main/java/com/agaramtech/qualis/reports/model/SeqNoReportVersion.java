package com.agaramtech.qualis.reports.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seqnoreportversion")
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SeqNoReportVersion extends CustomizedResultsetRowMapper<SeqNoReportVersion>
		implements Serializable, RowMapper<SeqNoReportVersion> {
	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;
	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoReportVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		SeqNoReportVersion objseq = new SeqNoReportVersion();
		objseq.setStablename(getString(arg0, "stablename", arg1));
		objseq.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objseq.setNstatus(getInteger(arg0, "nstatus", arg1));
		objseq.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objseq.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objseq;
	}

}
