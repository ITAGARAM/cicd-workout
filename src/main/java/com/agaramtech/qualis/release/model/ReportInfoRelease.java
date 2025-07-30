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
@Table(name = "reportinforelease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportInfoRelease extends CustomizedResultsetRowMapper<ReportInfoRelease>
		implements Serializable, RowMapper<ReportInfoRelease> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportinforeleasecode", nullable = false)
	private int nreportinforeleasecode;

	@Column(name = "ncoaparentcode", nullable = false)
	private int ncoaparentcode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "stablename ", length = 50, nullable = false)
	private String stablename;

	@Column(name = "sprimarykeyname ", length = 50, nullable = false)
	private String sprimarykeyname;

	@Column(name = "nparentmastercode", nullable = false)
	private int nparentmastercode;

	@Column(name = "sreportfieldname ", length = 50, nullable = false)
	private String sreportfieldname;

	@Column(name = "sreportfieldvalue ", length = 500, nullable = false)
	private String sreportfieldvalue;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String testname;

	@Override
	public ReportInfoRelease mapRow(ResultSet arg0, int arg1) throws SQLException {

		ReportInfoRelease objCOAChild = new ReportInfoRelease();

		objCOAChild.setNreportinforeleasecode(getInteger(arg0, "nreportinforeleasecode", arg1));
		objCOAChild.setNcoaparentcode(getInteger(arg0, "ncoaparentcode", arg1));
		objCOAChild.setNformcode(getShort(arg0, "nformcode", arg1));
		objCOAChild.setStablename(StringEscapeUtils.unescapeJava(getString(arg0, "stablename", arg1)));
		objCOAChild.setSprimarykeyname(StringEscapeUtils.unescapeJava(getString(arg0, "sprimarykeyname", arg1)));
		objCOAChild.setNparentmastercode(getShort(arg0, "nparentmastercode", arg1));
		objCOAChild.setSreportfieldname(StringEscapeUtils.unescapeJava(getString(arg0, "sreportfieldname", arg1)));
		objCOAChild.setSreportfieldvalue(StringEscapeUtils.unescapeJava(getString(arg0, "sreportfieldvalue", arg1)));
		objCOAChild.setNstatus(getShort(arg0, "nstatus", arg1));
		objCOAChild.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCOAChild.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCOAChild.setTestname(getString(arg0, "testname", arg1));

		return objCOAChild;
	}

}
