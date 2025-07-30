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
@Table(name = "coareporttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class COAReportType extends CustomizedResultsetRowMapper<COAReportType> implements Serializable, RowMapper<COAReportType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ncoareporttypecode")
	private short ncoareporttypecode;
	@Column(name = "nreporttypecode", nullable = false)
	private short nreporttypecode = -1;
	@Column(name = "scoareporttypename", length = 50, nullable = false)
	private String scoareporttypename;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "isneedsection", nullable = false)
	private short isneedsection;
	@Column(name = "nisarnowiserelease", nullable = false)
	private short nisarnowiserelease;
	@Column(name = "nismultipleproject", nullable = false)
	private short nismultipleproject;
	@Column(name = "nneedreleaseformattoggle", nullable = false)
	private short nneedreleaseformattoggle;

	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient short nsorter;

	@Override
	public COAReportType mapRow(ResultSet arg0, int arg1) throws SQLException {
		COAReportType reportTypeObj = new COAReportType();
		reportTypeObj.setNcoareporttypecode(getShort(arg0, "ncoareporttypecode", arg1));
		reportTypeObj.setNreporttypecode(getShort(arg0, "nreporttypecode", arg1));
		reportTypeObj.setScoareporttypename(StringEscapeUtils.unescapeJava(getString(arg0, "scoareporttypename", arg1)));
		reportTypeObj.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		reportTypeObj.setNstatus(getShort(arg0, "nstatus", arg1));
		reportTypeObj.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		reportTypeObj.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		reportTypeObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportTypeObj.setIsneedsection(getShort(arg0, "isneedsection", arg1));
		reportTypeObj.setNisarnowiserelease(getShort(arg0, "nisarnowiserelease", arg1));
		reportTypeObj.setNismultipleproject(getShort(arg0, "nismultipleproject", arg1));
		reportTypeObj.setNneedreleaseformattoggle(getShort(arg0, "nneedreleaseformattoggle", arg1));
		reportTypeObj.setNsorter(getShort(arg0, "nsorter", arg1));

		return reportTypeObj;
	}

}
