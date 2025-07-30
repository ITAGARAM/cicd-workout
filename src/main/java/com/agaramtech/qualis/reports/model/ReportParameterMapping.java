package com.agaramtech.qualis.reports.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
@Table(name = "reportparametermapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportParameterMapping extends CustomizedResultsetRowMapper<ReportParameterMapping>
		implements Serializable, RowMapper<ReportParameterMapping> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nreportparametermappingcode")
	private int nreportparametermappingcode;
	@Column(name = "nreportdetailcode", nullable = false)
	private int nreportdetailcode;
	@Column(name = "nparentreportdesigncode", nullable = false)
	private int nparentreportdesigncode;
	@Column(name = "nchildreportdesigncode", nullable = false)
	private int nchildreportdesigncode;
	@Column(name = "sfieldname", length = 100, nullable = false)
	private String sfieldname;
	@Column(name = "nisactionparent", nullable = false)
	private int nisactionparent = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	@Column(name = "nstatus", nullable = false)
	private int nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sparentparametername;
	@Transient
	private transient String schildparametername;
	@Transient
	private transient String sisactionparent;
	@Transient
	private transient String ssqlquery;

	@Override
	public ReportParameterMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReportParameterMapping mappingObj = new ReportParameterMapping();
		mappingObj.setNreportparametermappingcode(getInteger(arg0, "nreportparametermappingcode", arg1));
		mappingObj.setNreportdetailcode(getInteger(arg0, "nreportdetailcode", arg1));
		mappingObj.setNparentreportdesigncode(getInteger(arg0, "nparentreportdesigncode", arg1));
		mappingObj.setNchildreportdesigncode(getInteger(arg0, "nchildreportdesigncode", arg1));
		mappingObj.setSfieldname(StringEscapeUtils.unescapeJava(getString(arg0, "sfieldname", arg1)));
		mappingObj.setNisactionparent(getInteger(arg0, "nisactionparent", arg1));
		mappingObj.setNstatus(getInteger(arg0, "nstatus", arg1));
		mappingObj.setSparentparametername(getString(arg0, "sparentparametername", arg1));
		mappingObj.setSchildparametername(getString(arg0, "schildparametername", arg1));
		mappingObj.setSisactionparent(getString(arg0, "sisactionparent", arg1));
		mappingObj.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		mappingObj.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		mappingObj.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return mappingObj;
	}

}
