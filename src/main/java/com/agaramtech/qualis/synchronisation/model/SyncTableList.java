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
@Table(name = "synctablelist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SyncTableList extends CustomizedResultsetRowMapper<SyncTableList>
implements Serializable, RowMapper<SyncTableList> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsynctablecode")
	private int nsynctablecode;

	@Column(name = "stablename")
	private String stablename = "";

	@Column(name = "stableprimarykey")
	private String stableprimarykey = "";

	@Column(name = "ninttypeprimarykey")
	private short ninttypeprimarykey = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();

	@Column(name = "nupdatedatereqd")
	private short nupdatedatereqd = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "sseqnotablename")
	private String sseqnotablename = "";

	@Column(name = "squery")
	private String squery = "";

	@Column(name = "srecordquery")
	private String srecordquery = "";

	@Column(name = "sreleaseqry")
	private String sreleaseqry = "";

	@Column(name = "sdatefieldname")
	private String sdatefieldname = "";

	@Lob
	@Column(name = "jupdatefield", columnDefinition = "jsonb")
	private Map<String, Object> jupdatefield;

	@Column(name = "ntransfertype")
	private short ntransfertype;

	@Column(name = "nrecordcount")
	private short nrecordcount;

	@ColumnDefault("-1")
	@Column(name = "nsorter")
	private short nsorter = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nsitecodereqd")
	private short nsitecodereqd = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Override
	public SyncTableList mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SyncTableList syncTableList = new SyncTableList();

		syncTableList.setNsynctablecode(getInteger(arg0, "nsynctablecode", arg1));
		syncTableList.setSseqnotablename(getString(arg0, "sseqnotablename", arg1));
		syncTableList.setStablename(getString(arg0, "stablename", arg1));
		syncTableList.setStableprimarykey(getString(arg0, "stableprimarykey", arg1));
		syncTableList.setNinttypeprimarykey(getShort(arg0, "ninttypeprimarykey", arg1));
		syncTableList.setNupdatedatereqd(getShort(arg0, "nupdatedatereqd", arg1));
		syncTableList.setNtransfertype(getShort(arg0, "ntransfertype", arg1));
		syncTableList.setNrecordcount(getShort(arg0, "nrecordcount", arg1));
		syncTableList.setNsorter(getShort(arg0, "nsorter", arg1));
		syncTableList.setSrecordquery(getString(arg0, "srecordquery", arg1));
		syncTableList.setSquery(getString(arg0, "squery", arg1));
		syncTableList.setSdatefieldname(getString(arg0, "sdatefieldname", arg1));
		syncTableList.setJupdatefield(getJsonObject(arg0, "jupdatefield", arg1));
		syncTableList.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		syncTableList.setNsitecode(getShort(arg0, "nsitecode", arg1));
		syncTableList.setNstatus(getShort(arg0, "nstatus", arg1));
		syncTableList.setNsitecodereqd(getShort(arg0, "nsitecodereqd", arg1));
		syncTableList.setSreleaseqry(getString(arg0, "sreleaseqry", arg1));

		return syncTableList;
	}

}
