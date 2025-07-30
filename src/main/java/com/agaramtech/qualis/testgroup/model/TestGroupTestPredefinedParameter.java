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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "testgrouptestpredefparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestPredefinedParameter extends CustomizedResultsetRowMapper<TestGroupTestPredefinedParameter> implements Serializable, RowMapper<TestGroupTestPredefinedParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestgrouptestpredefcode")
	private int ntestgrouptestpredefcode;
	
	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "ngradecode", nullable = false)
	private short ngradecode;
	
	@Column(name = "spredefinedname", length = 100, nullable = false)
	private String spredefinedname;
	
	@Column(name = "spredefinedsynonym", length = 100, nullable = false)
	private String spredefinedsynonym;
	
	@Column(name = "spredefinedcomments", length = 255, nullable = false)
	private String spredefinedcomments;
	
	@Column(name = "salertmessage", length = 255, nullable = false)
	private String salertmessage;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =  (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nneedresultentryalert", nullable = false)
	private short nneedresultentryalert =  (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nneedsubcodedresult", nullable = false)
	private short nneedsubcodedresult = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String sresultpredefinedname;
	@Transient
	private transient int ntransactiontestcode;
	@Transient
	private transient int ntransactionresultcode;
	

	@Override
	public TestGroupTestPredefinedParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestPredefinedParameter objTGTPP = new TestGroupTestPredefinedParameter();
		objTGTPP.setNtestgrouptestpredefcode(getInteger(arg0, "ntestgrouptestpredefcode", arg1));
		objTGTPP.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objTGTPP.setNgradecode(getShort(arg0, "ngradecode", arg1));
		objTGTPP.setSpredefinedname(StringEscapeUtils.unescapeJava(getString(arg0, "spredefinedname", arg1)));
		objTGTPP.setSpredefinedsynonym(StringEscapeUtils.unescapeJava(getString(arg0, "spredefinedsynonym", arg1)));
		objTGTPP.setSpredefinedcomments(StringEscapeUtils.unescapeJava(getString(arg0, "spredefinedcomments", arg1)));
		objTGTPP.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objTGTPP.setNstatus(getShort(arg0, "nstatus", arg1));
		objTGTPP.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objTGTPP.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objTGTPP.setNtransactionresultcode(getInteger(arg0, "ntransactionresultcode", arg1));
		objTGTPP.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTGTPP.setNneedresultentryalert(getShort(arg0,"nneedresultentryalert",arg1));
		objTGTPP.setNneedsubcodedresult(getShort(arg0,"nneedsubcodedresult",arg1));
		objTGTPP.setSalertmessage(StringEscapeUtils.unescapeJava(getString(arg0, "salertmessage", arg1)));
		objTGTPP.setSresultpredefinedname(getString(arg0, "sresultpredefinedname", arg1));

		

		return objTGTPP;
	}

}
