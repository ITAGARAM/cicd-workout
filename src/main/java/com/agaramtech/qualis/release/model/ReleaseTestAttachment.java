
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
@Table(name = "releasetestattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseTestAttachment extends CustomizedResultsetRowMapper<ReleaseTestAttachment>
		implements Serializable, RowMapper<ReleaseTestAttachment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreleasetestattachmentcode", nullable = false)
	private int nreleasetestattachmentcode;

	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;

	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

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
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode", nullable = false)
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
	private transient String sdescription;

	@Transient
	private transient String ssystemfilename;

	@Transient
	private transient String sfilename;

	@Transient
	private transient String screateddate;

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
	private transient int nfilesize;

	@Transient
	private transient String sneedreport;

	@Transient
	private transient int ntransactionstatus;

	@Transient
	private transient String stestsynonym;

	@Transient
	private transient String sheader;

	@Transient
	private transient int nsortorder;

	@Override
	public ReleaseTestAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {

		ReleaseTestAttachment objReleaseTestAttachment = new ReleaseTestAttachment();

		objReleaseTestAttachment.setNreleasetestattachmentcode(getInteger(arg0, "nreleasetestattachmentcode", arg1));
		objReleaseTestAttachment.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objReleaseTestAttachment.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objReleaseTestAttachment.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objReleaseTestAttachment.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objReleaseTestAttachment.setNformcode(getShort(arg0, "nformcode", arg1));
		objReleaseTestAttachment.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objReleaseTestAttachment.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objReleaseTestAttachment.setNusercode(getInteger(arg0, "nusercode", arg1));
		objReleaseTestAttachment.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objReleaseTestAttachment.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objReleaseTestAttachment.setNtransdatetimezonecode(getShort(arg0, "ntransdatetimezonecode", arg1));
		objReleaseTestAttachment.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objReleaseTestAttachment.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objReleaseTestAttachment.setNstatus(getShort(arg0, "nstatus", arg1));
		objReleaseTestAttachment.setNneedreport(getInteger(arg0, "nneedreport", arg1));
		objReleaseTestAttachment.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objReleaseTestAttachment.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objReleaseTestAttachment.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objReleaseTestAttachment.setScreateddate(getString(arg0, "screateddate", arg1));
		objReleaseTestAttachment.setSarno(getString(arg0, "sarno", arg1));
		objReleaseTestAttachment.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objReleaseTestAttachment.setSformname(getString(arg0, "sformname", arg1));
		objReleaseTestAttachment.setSusername(getString(arg0, "susername", arg1));
		objReleaseTestAttachment.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objReleaseTestAttachment.setNfilesize(getInteger(arg0, "nfilesize", arg1));
		objReleaseTestAttachment.setSneedreport(getString(arg0, "sneedreport", arg1));
		objReleaseTestAttachment.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objReleaseTestAttachment.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objReleaseTestAttachment.setSheader(getString(arg0, "sheader", arg1));
		objReleaseTestAttachment.setNsortorder(getInteger(arg0, "nsortorder", arg1));
		objReleaseTestAttachment.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		return objReleaseTestAttachment;
	}

}
