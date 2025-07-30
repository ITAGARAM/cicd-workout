package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Table(name = "reportdecisiontype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportDecisionType extends CustomizedResultsetRowMapper<ReportDecisionType>
		implements Serializable, RowMapper<ReportDecisionType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreportdecisiontypecode")
	private short nreportdecisiontypecode;
	@Column(name = "sdecisiontypename", length = 30, nullable = false)
	private String sdecisiontypename;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplayname;

	@Override
	public ReportDecisionType mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReportDecisionType reportTypeObj = new ReportDecisionType();
		reportTypeObj.setNreportdecisiontypecode(getShort(arg0, "nreportdecisiontypecode", arg1));
		reportTypeObj.setSdecisiontypename(StringEscapeUtils.unescapeJava(getString(arg0, "sdecisiontypename", arg1)));
		reportTypeObj.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		reportTypeObj.setNstatus(getShort(arg0, "nstatus", arg1));
		reportTypeObj.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		return reportTypeObj;
	}

}
