package com.agaramtech.qualis.scheduler.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//ALPD-4941 Created SchedulerTestParameterDetail pojo for Scheduler configuration screen
@Entity
@Table(name = "schedulertestparameterdetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerTestParameterDetail extends CustomizedResultsetRowMapper<SchedulerTestParameterDetail>
implements Serializable, RowMapper<SchedulerTestParameterDetail> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nschedulertestparametercode")
	private int nschedulertestparametercode;

	@Column(name = "nschedulertestcode")
	private int nschedulertestcode;

	@Column(name = "nschedulersamplecode")
	private int nschedulersamplecode;

	@Column(name = "ntestgrouptestparametercode")
	private int ntestgrouptestparametercode;

	@Column(name = "ntestparametercode")
	private int ntestparametercode;

	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;

	@Column(name = "ntestgrouptestformulacode")
	private int ntestgrouptestformulacode;

	@Column(name = "nreportmandatory", nullable = false)
	private short nreportmandatory;

	@Column(name = "nresultmandatory", nullable = false)
	private short nresultmandatory;

	@Column(name = "nunitcode")
	private int nunitcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;

	@Column(name = "ntransdatetimezonecode")
	private short ntransdatetimezonecode;

	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SchedulerTestParameterDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SchedulerTestParameterDetail objSchedulerTestParameterDetail = new SchedulerTestParameterDetail();
		objSchedulerTestParameterDetail
		.setNschedulertestparametercode(getInteger(arg0, "nschedulertestparametercode", arg1));
		objSchedulerTestParameterDetail.setNschedulertestcode(getInteger(arg0, "nschedulertestcode", arg1));
		objSchedulerTestParameterDetail.setNschedulersamplecode(getInteger(arg0, "nschedulersamplecode", arg1));
		objSchedulerTestParameterDetail
		.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objSchedulerTestParameterDetail.setNtestparametercode(getInteger(arg0, "ntestparametercode", arg1));
		objSchedulerTestParameterDetail.setNparametertypecode(getShort(arg0, "nparametertypecode", arg1));
		objSchedulerTestParameterDetail
		.setNtestgrouptestformulacode(getInteger(arg0, "ntestgrouptestformulacode", arg1));
		objSchedulerTestParameterDetail.setNreportmandatory(getShort(arg0, "nreportmandatory", arg1));
		objSchedulerTestParameterDetail.setNresultmandatory(getShort(arg0, "nresultmandatory", arg1));
		objSchedulerTestParameterDetail.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objSchedulerTestParameterDetail.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objSchedulerTestParameterDetail.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSchedulerTestParameterDetail.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objSchedulerTestParameterDetail.setNtransdatetimezonecode(getShort(arg0, "ntransdatetimezonecode", arg1));
		objSchedulerTestParameterDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSchedulerTestParameterDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		return objSchedulerTestParameterDetail;
	}

}
