package com.agaramtech.qualis.testgroup.model;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "testgrouptestmaterial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestMaterial extends CustomizedResultsetRowMapper<TestGroupTestMaterial> implements Serializable,RowMapper<TestGroupTestMaterial> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestmaterialcode")
	private int ntestgrouptestmaterialcode;
	
	@Column(name = "ntestgrouptestcode", nullable = false)
	private int ntestgrouptestcode;
	
	@Column(name = "nmaterialtypecode", nullable = false)
	private short nmaterialtypecode;
	
	@Column(name = "nmaterialcatcode", nullable = false)
	private int nmaterialcatcode;
	
	@Column(name = "nmaterialcode", nullable = false)
	private int nmaterialcode;
	
	@Column(name = "sremarks", length = 255)
	private String sremarks;
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String smaterialcatname;
	@Transient
	private transient String smaterialname;
	@Transient
	private transient String smaterialtypename;
	@Transient
	private transient Map<String, Object> jsondata;


	@Override
	public TestGroupTestMaterial mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestMaterial objTestGroupTestMaterial = new TestGroupTestMaterial();
		objTestGroupTestMaterial.setNtestgrouptestmaterialcode(getInteger(arg0, "ntestgrouptestmaterialcode", arg1));
		objTestGroupTestMaterial.setNtestgrouptestcode(getInteger(arg0, "ntestgrouptestcode", arg1));
		objTestGroupTestMaterial.setNmaterialcatcode(getInteger(arg0, "nmaterialcatcode", arg1));
		objTestGroupTestMaterial.setNmaterialcode(getInteger(arg0, "nmaterialcode", arg1));
		objTestGroupTestMaterial.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0, "sremarks", arg1)));
		objTestGroupTestMaterial.setNmaterialtypecode(getShort(arg0, "nmaterialtypecode", arg1));
		objTestGroupTestMaterial.setNstatus(getShort(arg0, "nstatus", arg1));
		objTestGroupTestMaterial.setSmaterialcatname(getString(arg0, "smaterialcatname", arg1));
		objTestGroupTestMaterial.setSmaterialname(getString(arg0, "smaterialname", arg1));
		objTestGroupTestMaterial.setSmaterialtypename(getString(arg0, "smaterialtypename", arg1));
		objTestGroupTestMaterial.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objTestGroupTestMaterial.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupTestMaterial.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objTestGroupTestMaterial;
	}
	
}
