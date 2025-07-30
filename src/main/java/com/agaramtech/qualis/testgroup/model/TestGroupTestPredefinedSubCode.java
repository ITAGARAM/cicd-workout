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
@Table(name = "testgrouptestpredefsubresult")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupTestPredefinedSubCode extends CustomizedResultsetRowMapper<TestGroupTestPredefinedSubCode> implements Serializable,RowMapper<TestGroupTestPredefinedSubCode> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntestgrouptestpredefsubcode")
	private int ntestgrouptestpredefsubcode;
	
	@Column(name = "ntestgrouptestpredefcode", nullable = false)
	private int ntestgrouptestpredefcode;
	
	@Column(name = "ssubcodedresult", length = 100)
	private String ssubcodedresult;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus =  (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
    @Column(name="dmodifieddate")
    private Instant dmodifieddate;
    
    
	@Override
	public TestGroupTestPredefinedSubCode mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupTestPredefinedSubCode objTestPredefinedParameter = new TestGroupTestPredefinedSubCode();
		objTestPredefinedParameter.setNtestgrouptestpredefsubcode(getInteger(arg0,"ntestgrouptestpredefsubcode",arg1));
		objTestPredefinedParameter.setNtestgrouptestpredefcode(getInteger(arg0,"ntestgrouptestpredefcode",arg1));
		objTestPredefinedParameter.setSsubcodedresult(StringEscapeUtils.unescapeJava(getString(arg0,"ssubcodedresult",arg1)));
        objTestPredefinedParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestPredefinedParameter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestPredefinedParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objTestPredefinedParameter;
	}
}

