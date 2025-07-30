package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "reportmodule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportModule extends CustomizedResultsetRowMapper<ReportModule> implements Serializable, RowMapper<ReportModule> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreportmodulecode")
	private short nreportmodulecode;
	@Column(name = "nsorter", nullable = false)
	private short nsorter = (short) Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	@Column(name = "sreportmodulename", length = 100, nullable = false)
	private String sreportmodulename;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private int nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String sdisplayname;

	@Override
	public ReportModule mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ReportModule reportModuleObj = new ReportModule();
		reportModuleObj.setNreportmodulecode(getShort(arg0, "nreportmodulecode", arg1));
		reportModuleObj.setNsorter(getShort(arg0, "nsorter", arg1));
		reportModuleObj.setSreportmodulename(StringEscapeUtils.unescapeJava(getString(arg0, "sreportmodulename", arg1)));
		reportModuleObj.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		reportModuleObj.setNstatus(getShort(arg0, "nstatus", arg1));
		reportModuleObj.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		reportModuleObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportModuleObj.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		return reportModuleObj;

	}

}
