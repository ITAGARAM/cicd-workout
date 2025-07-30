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
@Table(name = "syncsendrecord")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SyncSendRecord extends CustomizedResultsetRowMapper<SyncSendRecord> implements Serializable, RowMapper<SyncSendRecord> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsyncsendrecordcode")
	private int nsyncsendrecordcode;

	@Column(name = "nsyncbatchcode")
	private int nsyncbatchcode;

	@Column(name = "sbatchtransferid")
	private String sbatchtransferid = "";

	@Column(name = "stablename")
	private String stablename = "";

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "stransferid")
	private String stransferid = "";

	@ColumnDefault("4")
	@Column(name = "nfetchstatus")
	private short nfetchstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nacknowledgestatus")
	private short nacknowledgestatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nretrycount")
	private short nretrycount = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	// @ColumnDefault("-1")
	// @Column(name = "nsourcesitecode", nullable=false)
	// private short nsourcesitecode;

	// @ColumnDefault("-1")
	// @Column(name = "ndestinationsitecode", nullable=false)
	// private short ndestinationsitecode;

	@ColumnDefault("-1")
	@Column(name = "sdestinationsitecode", nullable = false)
	private String sdestinationsitecode;

	@ColumnDefault("-1")
	@Column(name = "ssourcesitecode", nullable = false)
	private String ssourcesitecode;

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
	public SyncSendRecord mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SyncSendRecord syncSendRecord = new SyncSendRecord();

		syncSendRecord.setNsyncsendrecordcode(getInteger(arg0, "nsyncsendrecordcode", arg1));
		syncSendRecord.setNsyncbatchcode(getInteger(arg0, "nsyncbatchcode", arg1));
		syncSendRecord.setSbatchtransferid(getString(arg0, "sbatchtransferid", arg1));
		syncSendRecord.setStablename(getString(arg0, "stablename", arg1));
		syncSendRecord.setStransferid(getString(arg0, "stransferid", arg1));
		syncSendRecord.setNfetchstatus(getShort(arg0, "nfetchstatus", arg1));
		syncSendRecord.setNacknowledgestatus(getShort(arg0, "nacknowledgestatus", arg1));
		syncSendRecord.setNretrycount(getShort(arg0, "nretrycount", arg1));
		syncSendRecord.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		syncSendRecord.setNsorter(getShort(arg0, "nsorter", arg1));
		syncSendRecord.setDtransactiondatetime(getInstant(arg0, "dtransactiondatetime", arg1));
		syncSendRecord.setNsitecode(getShort(arg0, "nsitecode", arg1));
		syncSendRecord.setNstatus(getShort(arg0, "nstatus", arg1));
		syncSendRecord.setSdestinationsitecode(getString(arg0, "sdestinationsitecode", arg1));
		syncSendRecord.setSsourcesitecode(getString(arg0, "ssourcesitecode", arg1));

		return syncSendRecord;
	}

}
