package com.agaramtech.qualis.stability.model;

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
@Table(name = "stbtimepoint")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StbTimePoint extends CustomizedResultsetRowMapper<StbTimePoint> implements Serializable,RowMapper<StbTimePoint>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstbtimepointcode")
	private long nstbtimepointcode;

	@Column(name = "nstbstudyplancode", nullable = false)
	private long nstbstudyplancode;

	@Column(name = "ncomponentcode", nullable = false)
	private int ncomponentcode;

	@Column(name = "nspecsampletypecode", nullable = false)
	private int nspecsampletypecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	public transient int slno;
	@Transient
	public transient int nallottedspeccode;
	@Transient
	public transient String scomponentname;
	
	public StbTimePoint mapRow(ResultSet arg0, int arg1) throws SQLException {
		StbTimePoint objStbTimePoint = new StbTimePoint();
		objStbTimePoint.setNstbtimepointcode(getLong(arg0, "nstbtimepointcode", arg1));
		objStbTimePoint.setNstbstudyplancode(getLong(arg0, "nstbstudyplancode", arg1));
		objStbTimePoint.setNcomponentcode(getInteger(arg0, "ncomponentcode", arg1));
		objStbTimePoint.setNspecsampletypecode(getInteger(arg0, "nspecsampletypecode", arg1));
		objStbTimePoint.setNstatus(getShort(arg0, "nstatus", arg1));
		objStbTimePoint.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objStbTimePoint.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objStbTimePoint.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objStbTimePoint.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objStbTimePoint.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objStbTimePoint.setSlno(getInteger(arg0, "slno", arg1));
		objStbTimePoint.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objStbTimePoint.setScomponentname(getString(arg0, "scomponentname", arg1));

		return objStbTimePoint;
	}

}
