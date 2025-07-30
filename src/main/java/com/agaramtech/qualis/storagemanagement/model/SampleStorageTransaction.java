/**
 * 
 */
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
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Janakumar R
 *
 */
@Table(name = "samplestoragetransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageTransaction extends CustomizedResultsetRowMapper<SampleStorageTransaction>
		implements Serializable, RowMapper<SampleStorageTransaction> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplestoragetransactioncode", nullable = false)
	private int nsamplestoragetransactioncode;

	@Column(name = "nsamplestoragelocationcode", nullable = false)
	private short nsamplestoragelocationcode;

	@Column(name = "nsamplestoragemappingcode", nullable = false)
	private int nsamplestoragemappingcode;

	@Column(name = "nprojecttypecode", nullable = false)
	private int nprojecttypecode;

	@Column(name = "sposition", length = 100, nullable = false)
	private String sposition;

	@Column(name = "spositionvalue", length = 100, nullable = false)
	private String spositionvalue;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "npositionfilled", nullable = false)
	private short npositionfilled;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String ssamplestoragelocationname;

	@Transient
	private transient String ssamplestoragepathname;

	@Transient
	private transient String stosamplestoragelocationname;

	@Transient
	private transient String stosamplestoragepathname;

	@Transient
	private transient String sboxid;

	@Transient
	private transient String stoboxid;

	@Override
	public SampleStorageTransaction mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageTransaction samplestoragetransaction = new SampleStorageTransaction();
		samplestoragetransaction
				.setNsamplestoragetransactioncode(getInteger(arg0, "nsamplestoragetransactioncode", arg1));
		samplestoragetransaction.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		samplestoragetransaction.setNsamplestoragemappingcode(getInteger(arg0, "nsamplestoragemappingcode", arg1));
		samplestoragetransaction.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		samplestoragetransaction.setSposition(StringEscapeUtils.unescapeJava(getString(arg0, "sposition", arg1)));
		samplestoragetransaction
				.setSpositionvalue(StringEscapeUtils.unescapeJava(getString(arg0, "spositionvalue", arg1)));
		samplestoragetransaction.setNpositionfilled(getShort(arg0, "npositionfilled", arg1));
		samplestoragetransaction.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		samplestoragetransaction.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		samplestoragetransaction.setNsitecode(getShort(arg0, "nsitecode", arg1));
		samplestoragetransaction.setNstatus(getShort(arg0, "nstatus", arg1));
		samplestoragetransaction.setSsamplestoragelocationname(getString(arg0, "ssamplestoragelocationname", arg1));
		samplestoragetransaction.setSsamplestoragepathname(getString(arg0, "ssamplestoragepathname", arg1));
		samplestoragetransaction.setStosamplestoragelocationname(getString(arg0, "stosamplestoragelocationname", arg1));
		samplestoragetransaction.setStosamplestoragepathname(getString(arg0, "stosamplestoragepathname", arg1));
		samplestoragetransaction.setSboxid(getString(arg0, "sboxid", arg1));
		samplestoragetransaction.setStoboxid(getString(arg0, "stoboxid", arg1));
		return samplestoragetransaction;
	}

}
