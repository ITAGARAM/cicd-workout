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
@Table(name = "synchistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SyncHistory extends CustomizedResultsetRowMapper<SyncHistory>
implements Serializable, RowMapper<SyncHistory> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsynchistorycode")
	private int nsynchistorycode;

	@Column(name = "nsyncbatchcode")
	private int nsyncbatchcode;

	@ColumnDefault("-1")
	@Column(name = "sdestinationsitecode", nullable = false)
	private String sdestinationsitecode;

	@ColumnDefault("-1")
	@Column(name = "ssourcesitecode", nullable = false)
	private String ssourcesitecode;

	@Column(name = "sbatchtransferid")
	private String sbatchtransferid;

	@Column(name = "stransferid")
	private String stransferid = "";

	@Column(name = "ntransferstatus")
	private short ntransferstatus;

	@Column(name = "ntransfertype")
	private short ntransfertype;

	@Lob
	@Column(name = "jerrormsg", columnDefinition = "jsonb")
	private Map<String, Object> jerrormsg;

	@Column(name = "dtransactiondatetime")
	private Instant dtransactiondatetime;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String stransferstatus;
	@Transient
	private transient String stransfertype;
	@Transient
	private transient String stablename;
	@Transient
	private transient String stransactiondatetime;
	@Transient
	private transient String serrormsg;

	@Override
	public SyncHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SyncHistory syncHistory = new SyncHistory();

		syncHistory.setNsynchistorycode(getInteger(arg0, "nsynchistorycode", arg1));
		syncHistory.setNsyncbatchcode(getInteger(arg0, "nsyncbatchcode", arg1));
		syncHistory.setSbatchtransferid(getString(arg0, "sbatchtransferid", arg1));
		syncHistory.setStransferid(getString(arg0, "stransferid", arg1));
		syncHistory.setNtransferstatus(getShort(arg0, "ntransferstatus", arg1));
		syncHistory.setNtransfertype(getShort(arg0, "ntransfertype", arg1));
		syncHistory.setJerrormsg(getJsonObject(arg0, "jerrormsg", arg1));
		syncHistory.setDtransactiondatetime(getInstant(arg0, "dtransactiondatetime", arg1));
		syncHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		syncHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		syncHistory.setStransferstatus(getString(arg0, "stransferstatus", arg1));
		syncHistory.setStransfertype(getString(arg0, "stransfertype", arg1));
		syncHistory.setStablename(getString(arg0, "stablename", arg1));
		syncHistory.setStransactiondatetime(getString(arg0, "stransactiondatetime", arg1));
		syncHistory.setSerrormsg(getString(arg0, "serrormsg", arg1));
		syncHistory.setSdestinationsitecode(getString(arg0, "sdestinationsitecode", arg1));
		syncHistory.setSsourcesitecode(getString(arg0, "ssourcesitecode", arg1));

		return syncHistory;
	}

}
