package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "reportdesignconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportDesignConfig extends CustomizedResultsetRowMapper<ReportDesignConfig>
		implements Serializable, RowMapper<ReportDesignConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportdesigncode")
	private int nreportdesigncode;

	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode;

	@Column(name = "nreportparametercode", nullable = false)
	private int nreportparametercode;

	@Column(name = "ndesigncomponentcode", nullable = false)
	private int ndesigncomponentcode;

	@Column(name = "nsqlquerycode", nullable = false)
	private int nsqlquerycode;

	@Column(name = "nmandatory", nullable = false)
	private int nmandatory = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();

	@Column(name = "sdisplayname", length = 100, nullable = false)
	private String sdisplayname;

	@Column(name = "ndays", nullable = false)
	private int ndays;

	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sreportparametername;
	@Transient
	private transient String sdesigncomponentname;
	@Transient
	private transient String ssqlqueryname;
	@Transient
	private transient String smandatory;
	@Transient
	private transient String ssqlquery;
	@Transient
	private transient String svaluemember;
	@Transient
	private transient String sdisplaymember;
	@Transient
	private transient List<?> dataList;

	@Override
	public ReportDesignConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ReportDesignConfig reportDesignObj = new ReportDesignConfig();
		reportDesignObj.setNreportdesigncode(getInteger(arg0, "nreportdesigncode", arg1));
		reportDesignObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		reportDesignObj.setNreportparametercode(getInteger(arg0, "nreportparametercode", arg1));
		reportDesignObj.setNdesigncomponentcode(getInteger(arg0, "ndesigncomponentcode", arg1));
		reportDesignObj.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		reportDesignObj.setNmandatory(getInteger(arg0, "nmandatory", arg1));
		reportDesignObj.setSdisplayname(StringEscapeUtils.unescapeJava(getString(arg0, "sdisplayname", arg1)));
		reportDesignObj.setNdays(getInteger(arg0, "ndays", arg1));
		reportDesignObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		reportDesignObj.setSreportparametername(getString(arg0, "sreportparametername", arg1));
		reportDesignObj.setSdesigncomponentname(getString(arg0, "sdesigncomponentname", arg1));
		reportDesignObj.setSsqlqueryname(getString(arg0, "ssqlqueryname", arg1));
		reportDesignObj.setSmandatory(getString(arg0, "smandatory", arg1));
		reportDesignObj.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		reportDesignObj.setSvaluemember(getString(arg0, "svaluemember", arg1));
		reportDesignObj.setSdisplaymember(getString(arg0, "sdisplaymember", arg1));
		reportDesignObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reportDesignObj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return reportDesignObj;
	}

}
