
package com.agaramtech.qualis.release.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "releasetestcomment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseTestComment extends CustomizedResultsetRowMapper<ReleaseTestComment>
		implements Serializable, RowMapper<ReleaseTestComment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreleasetestcommentcode", nullable = false)
	private int nreleasetestcommentcode;

	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "nsamplecommentscode", nullable = false)
	private int nsamplecommentscode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@ColumnDefault("-1")
	@Column(name = "ncommentsubtypecode", nullable = false)
	private int ncommentsubtypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsampletestcommentscode", nullable = false)
	private int nsampletestcommentscode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> jsondata;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode")
	private short ntransdatetimezonecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int nneedreport;

	@Transient
	private transient String sarno;

	@Transient
	private transient String ssamplearno;

	@Transient
	private transient String sformname;

	@Transient
	private transient String susername;

	@Transient
	private transient String suserrolename;

	@Transient
	private transient String sneedreport;

	@Transient
	private transient String stestsynonym;

	@Transient
	private transient String scomments;

	@Transient
	private transient int ntransactionstatus;

	@Transient
	private transient String scommentsubtype;

	@Transient
	private transient String spredefinedname;

	

	@Override
	public ReleaseTestComment mapRow(ResultSet arg0, int arg1) throws SQLException {

		ReleaseTestComment objReleaseTestComment = new ReleaseTestComment();

		objReleaseTestComment.setNreleasetestcommentcode(getInteger(arg0, "nreleasetestcommentcode", arg1));
		objReleaseTestComment.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objReleaseTestComment.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objReleaseTestComment.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objReleaseTestComment.setNsamplecommentscode(getInteger(arg0, "nsamplecommentscode", arg1));
		objReleaseTestComment.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objReleaseTestComment.setNformcode(getShort(arg0, "nformcode", arg1));
		objReleaseTestComment.setNusercode(getInteger(arg0, "nusercode", arg1));
		objReleaseTestComment.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objReleaseTestComment.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objReleaseTestComment.setNtransdatetimezonecode(getShort(arg0, "ntransdatetimezonecode", arg1));
		objReleaseTestComment.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objReleaseTestComment.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objReleaseTestComment.setNstatus(getShort(arg0, "nstatus", arg1));
		objReleaseTestComment.setNneedreport(getInteger(arg0, "nneedreport", arg1));
		objReleaseTestComment.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objReleaseTestComment.setSarno(getString(arg0, "sarno", arg1));
		objReleaseTestComment.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objReleaseTestComment.setSformname(getString(arg0, "sformname", arg1));
		objReleaseTestComment.setSusername(getString(arg0, "susername", arg1));
		objReleaseTestComment.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objReleaseTestComment.setSneedreport(getString(arg0, "sneedreport", arg1));
		objReleaseTestComment.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objReleaseTestComment.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objReleaseTestComment.setScommentsubtype(getString(arg0, "scommentsubtype", arg1));
		objReleaseTestComment.setSpredefinedname(getString(arg0, "spredefinedname", arg1));
		objReleaseTestComment.setNcommentsubtypecode(getInteger(arg0, "ncommentsubtypecode", arg1));
		objReleaseTestComment.setNsampletestcommentscode(getInteger(arg0, "nsampletestcommentscode", arg1));
		objReleaseTestComment.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));


		return objReleaseTestComment;
	}

}
