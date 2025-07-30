package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
@Table(name = "seqnoreportmanagement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoReportManagement extends CustomizedResultsetRowMapper<SeqNoReportManagement>
		implements Serializable, RowMapper<SeqNoReportManagement> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoReportManagement mapRow(ResultSet arg0, int arg1) throws SQLException {
		SeqNoReportManagement seqNoReportManagement = new SeqNoReportManagement();
		seqNoReportManagement.setStablename(getString(arg0, "stablename", arg1));
		seqNoReportManagement.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		seqNoReportManagement.setNstatus(getInteger(arg0, "nstatus", arg1));
		return seqNoReportManagement;
	}
}
