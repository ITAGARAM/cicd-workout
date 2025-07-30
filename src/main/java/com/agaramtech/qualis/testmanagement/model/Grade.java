package com.agaramtech.qualis.testmanagement.model;

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
@Table(name = "grade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Grade extends CustomizedResultsetRowMapper<Grade> implements Serializable, RowMapper<Grade> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ngradecode")
	private short ngradecode;

	@Column(name = "sgradename", length = 25, nullable = false)
	private String sgradename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("-1")
	@Column(name = "ncolorcode", nullable = false)
	private short ncolorcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String scolorname;

	@Transient
	private transient String scolorhexcode;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient short nflag;

	@Transient
	private transient String sdefaultname;

	@Transient
	private transient String sdisplaystatus;

	@Override
	public Grade mapRow(ResultSet arg0, int arg1) throws SQLException {
		Grade objGrade = new Grade();
		objGrade.setNgradecode(getShort(arg0, "ngradecode", arg1));
		objGrade.setSgradename(StringEscapeUtils.unescapeJava(getString(arg0, "sgradename", arg1)));
		objGrade.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objGrade.setNcolorcode(getShort(arg0, "ncolorcode", arg1));
		objGrade.setNstatus(getShort(arg0, "nstatus", arg1));
		objGrade.setScolorname(getString(arg0, "scolorname", arg1));
		objGrade.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		objGrade.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objGrade.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objGrade.setNflag(getShort(arg0, "nflag", arg1));
		objGrade.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objGrade.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objGrade.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objGrade.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objGrade;
	}
}
