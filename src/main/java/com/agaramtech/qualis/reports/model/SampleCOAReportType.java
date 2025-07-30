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
@Table(name = "samplecoareporttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleCOAReportType extends CustomizedResultsetRowMapper<SampleCOAReportType>
		implements Serializable, RowMapper<SampleCOAReportType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplecoareporttypecode")
	private short nsamplecoareporttypecode;

	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;

	@Column(name = "ncoareporttypecode", nullable = false)
	private short ncoareporttypecode;

	@Column(name = "nsorter", nullable = false)
	@ColumnDefault("1")
	private short nsorter = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleCOAReportType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleCOAReportType sampleCoaReportTypeObj = new SampleCOAReportType();
		sampleCoaReportTypeObj.setNsamplecoareporttypecode(getShort(arg0, "nsamplecoareporttypecode", arg1));
		sampleCoaReportTypeObj.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		sampleCoaReportTypeObj.setNcoareporttypecode(getShort(arg0, "ncoareporttypecode", arg1));
		sampleCoaReportTypeObj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleCoaReportTypeObj.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleCoaReportTypeObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleCoaReportTypeObj.setNsorter(getShort(arg0, "nsorter", arg1));
		return sampleCoaReportTypeObj;
	}

}
