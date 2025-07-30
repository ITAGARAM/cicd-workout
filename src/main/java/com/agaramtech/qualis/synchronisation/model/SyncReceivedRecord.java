package com.agaramtech.qualis.synchronisation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
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

@Entity
@Table(name = "syncreceivedrecord")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SyncReceivedRecord extends CustomizedResultsetRowMapper<SyncReceivedRecord>
implements Serializable, RowMapper<SyncReceivedRecord> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsyncreceivedrecordcode")
	private int nsyncreceivedrecordcode;

	@ColumnDefault("-1")
	@Column(name = "ssourcesitecode", nullable = false)
	private String ssourcesitecode;

	@Column(name = "sbatchtransferid")
	private String sbatchtransferid = "";

	@Column(name = "stransferid")
	private String stransferid = "";

	@Column(name = "stablename")
	private String stablename = "";

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nsorter")
	private short nsorter;

	@Column(name = "dtransactiondatetime")
	private Instant dtransactiondatetime;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SyncReceivedRecord mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SyncReceivedRecord receivedRecord = new SyncReceivedRecord();

		receivedRecord.setNsyncreceivedrecordcode(getInteger(arg0, "nsyncreceivedrecordcode", arg1));
		receivedRecord.setSbatchtransferid(getString(arg0, "sbatchtransferid", arg1));
		receivedRecord.setStablename(getString(arg0, "stablename", arg1));
		receivedRecord.setStransferid(getString(arg0, "stransferid", arg1));
		// receivedRecord.setNsourcesitecode(getShort(arg0, "nsourcesitecode", arg1));
		receivedRecord.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		receivedRecord.setDtransactiondatetime(getInstant(arg0, "dtransactiondatetime", arg1));
		receivedRecord.setNsitecode(getShort(arg0, "nsitecode", arg1));
		receivedRecord.setNsorter(getShort(arg0, "nsorter", arg1));
		receivedRecord.setNstatus(getShort(arg0, "nstatus", arg1));
		receivedRecord.setSsourcesitecode(getString(arg0, "ssourcesitecode", arg1));

		return receivedRecord;
	}

}