package com.agaramtech.qualis.testgroup.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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
@Table(name = "testgrouprulesengine")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupRulesEngine extends CustomizedResultsetRowMapper<TestGroupRulesEngine> implements Serializable, RowMapper<TestGroupRulesEngine> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestgrouprulesenginecode")
	private int ntestgrouprulesenginecode;
	
	@Column(name = "ntestgrouptestcode")
	private int ntestgrouptestcode;
	
	@Column(name = "srulename", length = 100)
	private String srulename;
	
	@Column(name = "nruleexecorder")
	private int nruleexecorder;
	
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private List<?> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("8")
	private short ntransactionstatus= (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
    private transient String sproductcatname;
	@Transient
    private transient String sleveldescription;
	@Transient
    private transient String sspecname;
	@Transient
    private transient String scomponentname;
	@Transient
    private transient String stestsynonym;
	@Transient
    private transient String sproductname;
	@Transient
    private transient String stransdisplaystatus;

	@Override
	public TestGroupRulesEngine mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupRulesEngine objRulesEngine = new TestGroupRulesEngine();
		objRulesEngine.setNtestgrouprulesenginecode(getInteger(arg0, "ntestgrouprulesenginecode", arg1));
		objRulesEngine.setNtestgrouptestcode(getInteger(arg0, "ntestgrouptestcode", arg1)); 
		objRulesEngine.setSrulename(StringEscapeUtils.unescapeJava(getString(arg0, "srulename", arg1)));
		objRulesEngine.setJsondata(getJsonObjecttoList(arg0, "jsondata", arg1));
		objRulesEngine.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objRulesEngine.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objRulesEngine.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objRulesEngine.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objRulesEngine.setNstatus(getShort(arg0, "nstatus", arg1));
		objRulesEngine.setNruleexecorder(getInteger(arg0, "nruleexecorder", arg1));
		objRulesEngine.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objRulesEngine.setSleveldescription(getString(arg0, "sleveldescription", arg1));
		objRulesEngine.setScomponentname(getString(arg0, "scomponentname", arg1));
		objRulesEngine.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objRulesEngine.setSproductname(getString(arg0, "sproductname", arg1));
		objRulesEngine.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		return objRulesEngine;
	}
}