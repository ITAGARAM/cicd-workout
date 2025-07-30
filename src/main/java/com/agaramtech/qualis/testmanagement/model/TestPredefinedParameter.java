package com.agaramtech.qualis.testmanagement.model;

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

/**
 * This class is used to map the fields of 'testpredefinedparameter' table of the
 * Database. 
 */
@Entity
@Table(name = "testpredefinedparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestPredefinedParameter extends CustomizedResultsetRowMapper<TestPredefinedParameter> implements Serializable,RowMapper<TestPredefinedParameter> {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntestpredefinedcode")
	private int ntestpredefinedcode;
	
	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;
	
	@Column(name = "ngradecode", nullable = false)
	private short ngradecode;
	
	@Column(name = "spredefinedname", length = 100)
	private String spredefinedname="";
	
	@ColumnDefault("1")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "spredefinedsynonym", length = 100, nullable = false)
	private String spredefinedsynonym;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();


	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String sdisplaystatus;

	
	@Override
	public TestPredefinedParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final TestPredefinedParameter objTestPredefinedParameter = new TestPredefinedParameter();
		objTestPredefinedParameter.setNtestpredefinedcode(getInteger(arg0,"ntestpredefinedcode",arg1));
		objTestPredefinedParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestPredefinedParameter.setSpredefinedname(StringEscapeUtils.unescapeJava(getString(arg0,"spredefinedname",arg1)));
		objTestPredefinedParameter.setNgradecode(getShort(arg0,"ngradecode",arg1));
		objTestPredefinedParameter.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestPredefinedParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestPredefinedParameter.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objTestPredefinedParameter.setSpredefinedsynonym(StringEscapeUtils.unescapeJava(getString(arg0,"spredefinedsynonym",arg1)));
		objTestPredefinedParameter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestPredefinedParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objTestPredefinedParameter;
	}
}
