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
@Table(name = "stbtimepointtest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StbTimePointTest extends CustomizedResultsetRowMapper<StbTimePointTest> implements Serializable,RowMapper<StbTimePointTest>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstbtimepointtestcode")
	private long nstbtimepointtestcode;

	@Column(name = "nstbtimepointcode", nullable = false)
	private long nstbtimepointcode;

	@Column(name = "nstbstudyplancode", nullable = false)
	private long nstbstudyplancode;

	@Column(name = "ntestgrouptestcode", nullable = false)
	private int ntestgrouptestcode;

	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;

	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode;

	@Column(name = "nmethodcode")
	private int nmethodcode;

	@Column(name = "ninstrumentcatcode")
	private int ninstrumentcatcode;

	@Column(name = "ntestrepeatno", nullable = false)
	private short ntestrepeatno;

	@Column(name = "ntestretestno", nullable = false)
	private short ntestretestno;

	@Column(name = "nchecklistversioncode", nullable = false)
	@ColumnDefault("-1")
	private int nchecklistversioncode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus")
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient float ncost;
	@Transient
	private transient short nrepeatcountno;
	@Transient
	private transient String scomponentname;
	@Transient
	private transient String ssectionname;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient String smethodname;
	
	
	public StbTimePointTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		StbTimePointTest objStbTimePointTest = new StbTimePointTest();
		objStbTimePointTest.setNstbtimepointtestcode(getLong(arg0, "nstbtimepointtestcode", arg1));
		objStbTimePointTest.setNstbtimepointcode(getLong(arg0, "nstbtimepointcode", arg1));
		objStbTimePointTest.setNstbstudyplancode(getLong(arg0, "nstbstudyplancode", arg1));
		objStbTimePointTest.setNtestgrouptestcode(getInteger(arg0, "ntestgrouptestcode", arg1));
		objStbTimePointTest.setNchecklistversioncode(getInteger(arg0, "nchecklistversioncode", arg1));
		objStbTimePointTest.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		objStbTimePointTest.setNtestrepeatno(getShort(arg0, "ntestrepeatno", arg1));
		objStbTimePointTest.setNtestretestno(getShort(arg0, "ntestretestno", arg1));
		objStbTimePointTest.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objStbTimePointTest.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objStbTimePointTest.setNmethodcode(getInteger(arg0, "nmethodcode", arg1));
		objStbTimePointTest.setNstatus(getShort(arg0, "nstatus", arg1));
		objStbTimePointTest.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objStbTimePointTest.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objStbTimePointTest.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objStbTimePointTest.setNcost(getFloat(arg0, "ncost", arg1));
		objStbTimePointTest.setNrepeatcountno(getShort(arg0, "nrepeatcountno", arg1));
		objStbTimePointTest.setSsectionname(getString(arg0, "ssectionname", arg1));
		objStbTimePointTest.setScomponentname(getString(arg0, "scomponentname", arg1));
		objStbTimePointTest.setSmethodname(getString(arg0, "smethodname", arg1));
		objStbTimePointTest.setStestsynonym(getString(arg0, "stestsynonym", arg1));

		return objStbTimePointTest;
	}

}
