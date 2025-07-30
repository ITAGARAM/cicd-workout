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
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'storagesampleprocessing' table of the Database.
 */
@Entity
@Table(name = "storagesampleprocessing")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleProcessing extends CustomizedResultsetRowMapper<SampleProcessing> implements Serializable, RowMapper<SampleProcessing> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsampleprocessingcode ")
	private int nsampleprocessingcode;
	
	@Column(name = "nprojecttypecode", nullable = false)
	private int nprojecttypecode;
	
	@Column(name = "nsamplecollectiontypecode", nullable = false)
	private int nsamplecollectiontypecode;
	
	@Column(name = "ncollectiontubetypecode", nullable = false)
	private int ncollectiontubetypecode;

	@Column(name = "nsampleprocesstypecode", nullable = false)
	private int nsampleprocesstypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "sbarcodeid", length = 20, nullable = false)
	private String sbarcodeid;

	@Column(name = "dprocessstartdate", nullable = false)
	private Instant dprocessstartdate;

	@Column(name = "ntzprocessstartdate", nullable = false)
	private int ntzprocessstartdate;

	@Column(name = "noffsetdprocessstartdate", nullable = false)
	private int noffsetdprocessstartdate;

	@Column(name = "dprocessenddate", nullable = false)
	private Instant dprocessenddate;

	@Column(name = "ntzprocessenddate", nullable = false)
	private int ntzprocessenddate;

	@Column(name = "noffsetdprocessenddate", nullable = false)
	private int noffsetdprocessenddate;

	@Column(name = "scomments", length = 255)
	private String scomments="";

	@Column(name = "sdeviationcomments", length = 255)
	private String sdeviationcomments="";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus= (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sprocessstartdate;
	@Transient
	private transient String sprocessenddate;
	@Transient
	private transient String stubename;
	@Transient
	private transient String sprocesstypename;
	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient String sproductname;
	@Transient
	private transient String sprocessduration;
	@Transient
	private transient String sgraceduration;
	@Transient
	private transient int nprocesstime;
	@Transient
	private transient int ngracetime;

	@Override
	public SampleProcessing mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final SampleProcessing objSampleProcessing = new SampleProcessing();
		objSampleProcessing.setNsampleprocessingcode(getInteger(arg0, "nsampleprocessingcode", arg1));
		objSampleProcessing.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objSampleProcessing.setNsamplecollectiontypecode(getInteger(arg0, "nsamplecollectiontypecode", arg1));
		objSampleProcessing.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSampleProcessing.setSbarcodeid(StringEscapeUtils.unescapeJava(getString(arg0, "sbarcodeid", arg1)));
		objSampleProcessing.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objSampleProcessing.setDprocessstartdate(getInstant(arg0, " dprocessstartdate", arg1));
		objSampleProcessing.setNtzprocessstartdate(getInteger(arg0, "ntzprocessstartdate", arg1));
		objSampleProcessing.setNoffsetdprocessstartdate(getInteger(arg0, "noffsetdprocessstartdate", arg1));
		objSampleProcessing.setDprocessenddate(getInstant(arg0, " dprocessenddate", arg1));
		objSampleProcessing.setNtzprocessenddate(getInteger(arg0, "ntzprocessenddate", arg1));
		objSampleProcessing.setNoffsetdprocessenddate(getInteger(arg0, "noffsetdprocessenddate", arg1));
		objSampleProcessing.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleProcessing.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleProcessing.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleProcessing.setSprocessstartdate(getString(arg0, "sprocessstartdate", arg1));
		objSampleProcessing.setSprocessenddate(getString(arg0, "sprocessenddate", arg1));
		objSampleProcessing.setStubename(getString(arg0, "stubename", arg1));
		objSampleProcessing.setSdeviationcomments(StringEscapeUtils.unescapeJava(getString(arg0, "sdeviationcomments", arg1)));
		objSampleProcessing.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		objSampleProcessing.setSprocesstypename(getString(arg0, "sprocesstypename", arg1));
		objSampleProcessing.setSproductname(getString(arg0, "sproductname", arg1));
		objSampleProcessing.setSprocessduration(getString(arg0, "sprocessduration", arg1));
		objSampleProcessing.setNprocesstime(getInteger(arg0, "nprocesstime", arg1));
		objSampleProcessing.setNgracetime(getInteger(arg0, "ngracetime", arg1));
		objSampleProcessing.setNsampleprocesstypecode(getInteger(arg0, "nsampleprocesstypecode", arg1));
		objSampleProcessing.setSgraceduration(getString(arg0, "sgraceduration", arg1));
		return objSampleProcessing;
	}
}
