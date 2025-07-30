package com.agaramtech.qualis.scheduler.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedulersampledetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulerSampleDetail extends CustomizedResultsetRowMapper<SchedulerSampleDetail>
		implements Serializable, RowMapper<SchedulerSampleDetail> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nschedulersamplecode")
	private int nschedulersamplecode;

	@Column(name = "nsampleschedulerconfigtypecode", nullable = false)
	private short nsampleschedulerconfigtypecode;

	@Column(name = "nschedulecode", nullable = false)
	private int nschedulecode;

	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;

	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;

	@Column(name = "nregsubtypecode", nullable = false)
	private short nregsubtypecode;

	@Column(name = "ndesigntemplatemappingcode", nullable = false)
	private int ndesigntemplatemappingcode;

	@Column(name = "nregsubtypeversioncode", nullable = false)
	private int nregsubtypeversioncode;

	@Column(name = "ntemplatemanipulationcode", nullable = false)
	private int ntemplatemanipulationcode;

	@Column(name = "nallottedspeccode", nullable = false)
	private int nallottedspeccode;

	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode;

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode;

	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;

	@Column(name = "nmaterialcatcode", nullable = false)
	private int nmaterialcatcode;

	@Column(name = "nmaterialcode", nullable = false)
	private int nmaterialcode;

	@Column(name = "nmaterialinventorycode", nullable = false)
	@ColumnDefault("-1")
	private int nmaterialinventorycode;

	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode = -1;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "nactivestatus", nullable = false)
	private short nactivestatus;

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

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = -1;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;

	@Override
	public SchedulerSampleDetail mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SchedulerSampleDetail objSchedulerSampleDetail = new SchedulerSampleDetail();

		objSchedulerSampleDetail.setNschedulersamplecode(getInteger(arg0, "nschedulersamplecode", arg1));
		objSchedulerSampleDetail
				.setNsampleschedulerconfigtypecode(getShort(arg0, "nsampleschedulerconfigtypecode", arg1));
		objSchedulerSampleDetail.setNschedulecode(getInteger(arg0, "nschedulecode", arg1));
		objSchedulerSampleDetail.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objSchedulerSampleDetail.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objSchedulerSampleDetail.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		objSchedulerSampleDetail.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objSchedulerSampleDetail.setNregsubtypeversioncode(getInteger(arg0, "nregsubtypeversioncode", arg1));
		objSchedulerSampleDetail.setNtemplatemanipulationcode(getInteger(arg0, "ntemplatemanipulationcode", arg1));
		objSchedulerSampleDetail.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objSchedulerSampleDetail.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objSchedulerSampleDetail.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objSchedulerSampleDetail.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objSchedulerSampleDetail.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
		objSchedulerSampleDetail.setNmaterialcatcode(getInteger(arg0, "nmaterialcatcode", arg1));
		objSchedulerSampleDetail.setNmaterialcode(getInteger(arg0, "nmaterialcode", arg1));
		objSchedulerSampleDetail.setNmaterialinventorycode(getInteger(arg0, "nmaterialinventorycode", arg1));
		objSchedulerSampleDetail.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objSchedulerSampleDetail.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objSchedulerSampleDetail.setNactivestatus(getShort(arg0, "nactivestatus", arg1));
		objSchedulerSampleDetail.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSchedulerSampleDetail.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objSchedulerSampleDetail.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objSchedulerSampleDetail.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objSchedulerSampleDetail.setNtransdatetimezonecode(getShort(arg0, "ntransdatetimezonecode", arg1));
		objSchedulerSampleDetail.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSchedulerSampleDetail.setNstatus(getShort(arg0, "nstatus", arg1));
		return objSchedulerSampleDetail;
	}

}
