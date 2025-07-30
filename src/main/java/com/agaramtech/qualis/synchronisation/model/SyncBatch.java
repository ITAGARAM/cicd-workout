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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "syncbatch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SyncBatch extends CustomizedResultsetRowMapper<SyncBatch> implements Serializable, RowMapper<SyncBatch> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsyncbatchcode")
	private int nsyncbatchcode;

	@Column(name = "nsynchronizationcode", nullable = false)
	private int nsynchronizationcode = -1;

	@Column(name = "sbatchtransferid")
	private String sbatchtransferid = "";

	@Column(name = "nbatchtransferstatus")
	private short nbatchtransferstatus;

	@Column(name = "nbatchfinalstatus")
	private short nbatchfinalstatus;

	@Column(name = "ntransfertype")
	private short ntransfertype;

	@Column(name = "dbatchstartdatetime")
	private Instant dbatchstartdatetime;

	@Column(name = "dbatchenddatetime")
	private Instant dbatchenddatetime;

	@ColumnDefault("-1")
	@Column(name = "ndestinationsitecode", nullable = false)
	private short ndestinationsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "sdestinationsitecode", nullable = false)
	private String sdestinationsitecode;

	@ColumnDefault("-1")
	@Column(name = "ssourcesitecode", nullable = false)
	private String ssourcesitecode;

	@Lob
	@Column(name = "jerrormsg", columnDefinition = "jsonb")
	private Map<String, Object> jerrormsg;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nbatchcount", nullable = false)
	private int nbatchcount = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sdestinationsitename;
	@Transient
	private transient String sbatchtransferstatus;
	@Transient
	private transient String sbatchfinalstatus;
	@Transient
	private transient String stransertype;
	@Transient
	private transient String sbatchstartdatetime;
	@Transient
	private transient String sbatchenddatetime;
	@Transient
	private transient String serrormsg;
	@Transient
	private transient String slastsyncdatetime;
	@Transient
	private transient String ssyncdatetime;
	@Transient
	private transient String susername;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String ssynctype;

	@Override
	public SyncBatch mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SyncBatch syncBatchHistory = new SyncBatch();

		syncBatchHistory.setNsyncbatchcode(getInteger(arg0, "nsyncbatchcode", arg1));
		syncBatchHistory.setNsynchronizationcode(getInteger(arg0, "nsynchronizationcode", arg1));
		syncBatchHistory.setSbatchtransferid(getString(arg0, "sbatchtransferid", arg1));
		syncBatchHistory.setNbatchtransferstatus(getShort(arg0, "nbatchtransferstatus", arg1));
		syncBatchHistory.setNbatchfinalstatus(getShort(arg0, "nbatchfinalstatus", arg1));
		syncBatchHistory.setNtransfertype(getShort(arg0, "ntransfertype", arg1));
		syncBatchHistory.setDbatchstartdatetime(getInstant(arg0, "dbatchstartdatetime", arg1));
		syncBatchHistory.setDbatchenddatetime(getInstant(arg0, "dbatchenddatetime", arg1));
		syncBatchHistory.setNdestinationsitecode(getShort(arg0, "ndestinationsitecode", arg1));
		syncBatchHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		syncBatchHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		syncBatchHistory.setJerrormsg(getJsonObject(arg0, "jerrormsg", arg1));
		syncBatchHistory.setSerrormsg(getString(arg0, "serrormsg", arg1));
		syncBatchHistory.setSdestinationsitename(getString(arg0, "sdestinationsitename", arg1));
		syncBatchHistory.setSbatchtransferstatus(getString(arg0, "sbatchtransferstatus", arg1));
		syncBatchHistory.setSbatchfinalstatus(getString(arg0, "sbatchfinalstatus", arg1));
		syncBatchHistory.setStransertype(getString(arg0, "stransertype", arg1));
		syncBatchHistory.setSbatchstartdatetime(getString(arg0, "sbatchstartdatetime", arg1));
		syncBatchHistory.setSbatchenddatetime(getString(arg0, "sbatchenddatetime", arg1));
		syncBatchHistory.setSlastsyncdatetime(getString(arg0, "slastsyncdatetime", arg1));
		syncBatchHistory.setSsyncdatetime(getString(arg0, "ssyncdatetime", arg1));
		syncBatchHistory.setSusername(getString(arg0, "susername", arg1));
		syncBatchHistory.setSuserrolename(getString(arg0, "suserrolename", arg1));
		syncBatchHistory.setSdestinationsitecode(getString(arg0, "sdestinationsitecode", arg1));
		syncBatchHistory.setSsourcesitecode(getString(arg0, "ssourcesitecode", arg1));
		syncBatchHistory.setSsynctype(getString(arg0, "ssynctype", arg1));
		syncBatchHistory.setNbatchcount(getInteger(arg0, "nbatchcount", arg1));

		return syncBatchHistory;
	}

}
