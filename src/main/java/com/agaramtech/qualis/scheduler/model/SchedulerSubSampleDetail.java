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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//ALPD-4941 Created SchedulerSubSampleDetail pojo for Scheduler configuration screen
@Entity
@Table(name = "schedulersubsampledetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerSubSampleDetail extends CustomizedResultsetRowMapper<SchedulerSubSampleDetail>
implements Serializable, RowMapper<SchedulerSubSampleDetail> {


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nschedulersubsamplecode")
	private int nschedulersubsamplecode;

	@Column(name = "nschedulersamplecode", nullable = false)
	private int nschedulersamplecode;

	@Column(name = "nspecsampletypecode", nullable = false)
	private int nspecsampletypecode;

	@Column(name = "ncomponentcode", nullable = false)
	private int ncomponentcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Column(name = "dtransactiondate")
	private Instant dtransactiondate;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;

	@Column(name = "ntransdatetimezonecode")
	private short ntransdatetimezonecode;

	@Column(name = "nsitecode")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	transient public int slno;
	@Transient
	transient public int nallottedspeccode;
	@Transient
	private transient String scomponentname;

	@Override
	public SchedulerSubSampleDetail mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SchedulerSubSampleDetail objSchedulerSubSampleDetail = new SchedulerSubSampleDetail();

		objSchedulerSubSampleDetail.setNschedulersubsamplecode(getInteger(arg0, "nschedulersubsamplecode", arg1));
		objSchedulerSubSampleDetail.setNschedulersamplecode(getInteger(arg0, "nschedulersamplecode", arg1));
		objSchedulerSubSampleDetail.setNspecsampletypecode(getInteger(arg0, "nspecsampletypecode", arg1));
		objSchedulerSubSampleDetail.setNcomponentcode(getInteger(arg0, "ncomponentcode", arg1));
		objSchedulerSubSampleDetail.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objSchedulerSubSampleDetail.setJsonuidata(getJsonObject(arg0, "jsonuidata", arg1));
		objSchedulerSubSampleDetail.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSchedulerSubSampleDetail.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objSchedulerSubSampleDetail.setNtransdatetimezonecode(getShort(arg0, "ntransdatetimezonecode", arg1));
		objSchedulerSubSampleDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSchedulerSubSampleDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		objSchedulerSubSampleDetail.setSlno(getInteger(arg0, "slno", arg1));
		objSchedulerSubSampleDetail.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objSchedulerSubSampleDetail.setScomponentname(getString(arg0, "scomponentname", arg1));

		return objSchedulerSubSampleDetail;
	}

}
