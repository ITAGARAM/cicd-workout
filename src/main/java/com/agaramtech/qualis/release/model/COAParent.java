package com.agaramtech.qualis.release.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coaparent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class COAParent extends CustomizedResultsetRowMapper<COAParent> implements Serializable, RowMapper<COAParent> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncoaparentcode")
	private int ncoaparentcode;

	@Column(name = "ncoareporttypecode", nullable = false)
	private short ncoareporttypecode;

	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;

	@Column(name = "nregsubtypecode", nullable = false)
	private short nregsubtypecode;

	@Column(name = "napproveconfversioncode", nullable = false)
	private int napproveconfversioncode;

	@Column(name = "sreportno", length = 30)
	private String sreportno;

	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nenteredby", nullable = false)
	private int nenteredby = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nenteredrole", nullable = false)
	private int nenteredrole = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndeputyenteredby", nullable = false)
	private int ndeputyenteredby = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ndeputyenteredrole", nullable = false)
	private int ndeputyenteredrole = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nreporttemplatecode", nullable = false)
	private short nreporttemplatecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String spreregno;

	@Transient
	private transient String stransactiontestcode;

	@Transient
	private transient String stransactionsamplecode;

	@Transient
	private transient int npreregno;

	@Transient
	private transient int ncoachildcode;

	@Transient
	private transient String susername;

	@Transient
	private transient String sgenerateddate;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient int nversionno;

	@Transient
	private transient String suserrolename;

	@Transient
	private transient String sreporttypename;

	@Transient
	private transient int ncoareporthistorycode;

	@Transient
	private transient int npreliminaryreporthistorycode;

	@Transient
	private transient int nregeneratereporthistorycode;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient String sversionno;

	@Transient
	private transient String sreporttemplatename;

	@Override
	public COAParent mapRow(ResultSet arg0, int arg1) throws SQLException {

		COAParent objCOAParent = new COAParent();

		objCOAParent.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objCOAParent.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objCOAParent.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		objCOAParent.setSreportno(getString(arg0, "sreportno", arg1));
		objCOAParent.setNenteredby(getInteger(arg0, "nenteredby", arg1));
		objCOAParent.setNenteredrole(getInteger(arg0, "nenteredrole", arg1));
		objCOAParent.setNdeputyenteredby(getInteger(arg0, "ndeputyenteredby", arg1));
		objCOAParent.setNdeputyenteredrole(getInteger(arg0, "ndeputyenteredrole", arg1));
		objCOAParent.setNstatus(getShort(arg0, "nstatus", arg1));
		objCOAParent.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCOAParent.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objCOAParent.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCOAParent.setNcoachildcode(getInteger(arg0, "ncoachildcode", arg1));
		objCOAParent.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objCOAParent.setNcoareporttypecode(getShort(arg0, "ncoareporttypecode", arg1));
		objCOAParent.setSusername(getString(arg0, "susername", arg1));
		objCOAParent.setSgenerateddate(getString(arg0, "sgenerateddate", arg1));
		objCOAParent.setNapproveconfversioncode(getInteger(arg0, "napproveconfversioncode", arg1));
		objCOAParent.setSpreregno(getString(arg0, "spreregno", arg1));
		objCOAParent.setStransactiontestcode(getString(arg0, "stransactiontestcode", arg1));
		objCOAParent.setStransactionsamplecode(getString(arg0, "stransactionsamplecode", arg1));
		objCOAParent.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objCOAParent.setNversionno(getInteger(arg0, "nversionno", arg1));
		objCOAParent.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objCOAParent.setSreporttypename(getString(arg0, "sreporttypename", arg1));
		objCOAParent.setNcoareporthistorycode(getInteger(arg0, "ncoareporthistorycode", arg1));
		objCOAParent.setNpreliminaryreporthistorycode(getInteger(arg0, "npreliminaryreporthistorycode", arg1));
		objCOAParent.setNregeneratereporthistorycode(getInteger(arg0, "nregeneratereporthistorycode", arg1));
		objCOAParent.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objCOAParent.setSversionno(getString(arg0, "sversionno", arg1));
		objCOAParent.setNreporttemplatecode(getShort(arg0, "nreporttemplatecode", arg1));
		objCOAParent.setSreporttemplatename(getString(arg0, "sreporttemplatename", arg1));

		return objCOAParent;
	}

}
