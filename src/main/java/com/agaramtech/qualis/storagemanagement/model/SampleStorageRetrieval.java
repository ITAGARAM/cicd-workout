package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Table(name = "samplestorageretrieval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageRetrieval extends CustomizedResultsetRowMapper<SampleStorageRetrieval>
		implements Serializable, RowMapper<SampleStorageRetrieval> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplestorageretrievalcode", nullable = false)
	private int nsamplestorageretrievalcode;

	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode;

	@Column(name = "nsamplestoragelocationcode", nullable = false)
	private short nsamplestoragelocationcode;

	@Column(name = "nsamplestoragelistcode", nullable = false)
	private int nsamplestoragelistcode;

	@Column(name = "nsamplestoragemappingcode", nullable = false)
	private int nsamplestoragemappingcode;

	@Column(name = "nprojecttypecode", nullable = false)
	private int nprojecttypecode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "sposition", length = 100, nullable = false)
	private String sposition;

	@Column(name = "spositionvalue", length = 100, nullable = false)
	private String spositionvalue;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;

	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@Transient
	private transient String scomments;

	@Transient
	private transient String stransdisplaystatus;

	@Override
	public SampleStorageRetrieval mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageRetrieval samplestorageretrieval = new SampleStorageRetrieval();
		samplestorageretrieval.setNsamplestorageretrievalcode(getInteger(arg0, "nsamplestorageretrievalcode", arg1));
		samplestorageretrieval.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		samplestorageretrieval.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		samplestorageretrieval.setNsamplestoragelistcode(getInteger(arg0, "nsamplestoragelistcode", arg1));
		samplestorageretrieval.setNsamplestoragemappingcode(getInteger(arg0, "nsamplestoragemappingcode", arg1));
		samplestorageretrieval.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		samplestorageretrieval.setNusercode(getInteger(arg0, "nusercode", arg1));
		samplestorageretrieval.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		samplestorageretrieval.setSposition(StringEscapeUtils.unescapeJava(getString(arg0, "sposition", arg1)));
		samplestorageretrieval.setSpositionvalue(StringEscapeUtils.unescapeJava(getString(arg0, "spositionvalue", arg1)));
		samplestorageretrieval.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		samplestorageretrieval.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		samplestorageretrieval.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		samplestorageretrieval.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		samplestorageretrieval.setNtransdatetimezonecode(getInteger(arg0, "ntransdatetimezonecode", arg1));
		samplestorageretrieval.setNsitecode(getShort(arg0, "nsitecode", arg1));
		samplestorageretrieval.setNstatus(getShort(arg0, "nstatus", arg1));
		return samplestorageretrieval;
	}

}
