package com.agaramtech.qualis.release.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "releasehistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseHistory extends CustomizedResultsetRowMapper<ReleaseHistory> implements Serializable, RowMapper<ReleaseHistory> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nreleasehistorycode")
	@ColumnDefault("-1")
	private int nreleasehistorycode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="ncoaparentcode", nullable=false)
	@ColumnDefault("-1")
	private int ncoaparentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "sreportno",  length = 50, nullable=false)
	private String sreportno;
	
	@Column(name="nversionno", nullable=false)
	@ColumnDefault("-1")
	private int nversionno = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name ="nusercode", nullable=false)
	@ColumnDefault("-1")
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name ="nuserrolecode", nullable=false)
	@ColumnDefault("-1")
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "sreportcomments",  length = 255)
	private String sreportcomments;
	
	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("-1")
	private int ntransactionstatus = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;
	
	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode", nullable = false)
	private int ntransdatetimezonecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name ="nstatus", nullable=false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String susername;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String ssitename;
	@Transient
	private transient String sgenerateddate;

	public ReleaseHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		ReleaseHistory objReleaseHistory = new ReleaseHistory();
		objReleaseHistory.setNreleasehistorycode(getInteger(arg0,"nreleasehistorycode",arg1));
		objReleaseHistory.setNcoaparentcode(getInteger(arg0,"ncoaparentcode",arg1));
		objReleaseHistory.setSreportno(StringEscapeUtils.unescapeJava(getString(arg0,"sreportno",arg1)));
		objReleaseHistory.setNversionno(getInteger(arg0,"nversionno",arg1));
		objReleaseHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objReleaseHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objReleaseHistory.setSreportcomments(StringEscapeUtils.unescapeJava(getString(arg0,"sreportcomments",arg1)));
		objReleaseHistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objReleaseHistory.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objReleaseHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objReleaseHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objReleaseHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objReleaseHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objReleaseHistory.setSusername(getString(arg0,"susername",arg1));
		objReleaseHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objReleaseHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objReleaseHistory.setSsitename(getString(arg0,"ssitename",arg1));
		objReleaseHistory.setSgenerateddate(getString(arg0,"sgenerateddate",arg1));

		return objReleaseHistory;
	}
}
