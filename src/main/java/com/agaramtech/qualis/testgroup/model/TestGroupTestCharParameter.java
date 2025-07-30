package com.agaramtech.qualis.testgroup.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "testgrouptestcharparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestCharParameter extends CustomizedResultsetRowMapper<TestGroupTestCharParameter> implements Serializable,RowMapper<TestGroupTestCharParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestcharcode")
	private int ntestgrouptestcharcode;
	
	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "scharname", length = 255)
	private String scharname="";
	
	@Column(name="dmodifieddate",nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public TestGroupTestCharParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestCharParameter objTGTCP = new TestGroupTestCharParameter();
		objTGTCP.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objTGTCP.setNtestgrouptestcharcode(getInteger(arg0,"ntestgrouptestcharcode",arg1));
		objTGTCP.setScharname(StringEscapeUtils.unescapeJava(getString(arg0,"scharname",arg1)));
		objTGTCP.setNstatus(getShort(arg0,"nstatus",arg1));
		objTGTCP.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTGTCP.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objTGTCP;
	}
	
}
