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
 * This class is used to map the fields of 'testpackage' table of the Database.
 */
@Entity
@Data
@Table(name = "testpackage")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestPackage extends CustomizedResultsetRowMapper<TestPackage> implements Serializable,RowMapper<TestPackage>
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestpackagecode")
	private int ntestpackagecode;
	
	@Column(name = "stestpackagename",  length = 100, nullable=false)
	private String stestpackagename;

	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@Column(name = "ntestpackageprice", nullable=false)
	private float ntestpackageprice;
	
	@Column(name = "ntestpackagetatdays", nullable=false)
	private int ntestpackagetatdays;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name="sopenmrsrefcode", length=20 )  
	private String sopenmrsrefcode="";
	
	@Column(name="spreventtbrefcode", length=20 ) 
	private String spreventtbrefcode="";
	
	@Column(name="sportalrefcode", length=20 )  
	private String sportalrefcode="";
	
	@Transient
	private transient String smodifieddate;
	
	@Override
	public TestPackage mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestPackage testpackage = new TestPackage();	
		
		testpackage.setNtestpackagecode(getInteger(arg0,"ntestpackagecode",arg1));
		testpackage.setStestpackagename(StringEscapeUtils.unescapeJava(getString(arg0,"stestpackagename",arg1)));
		testpackage.setNtestpackageprice(getFloat(arg0,"ntestpackageprice",arg1));
		testpackage.setNtestpackagetatdays(getInteger(arg0,"ntestpackagetatdays",arg1));
		testpackage.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		testpackage.setNsitecode(getShort(arg0,"nsitecode",arg1));
		testpackage.setNstatus(getShort(arg0,"nstatus",arg1));
		testpackage.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		testpackage.setSopenmrsrefcode(StringEscapeUtils.unescapeJava(getString(arg0,"sopenmrsrefcode",arg1)));
		testpackage.setSpreventtbrefcode(StringEscapeUtils.unescapeJava(getString(arg0,"spreventtbrefcode",arg1)));
		testpackage.setSportalrefcode(StringEscapeUtils.unescapeJava(getString(arg0,"sportalrefcode",arg1)));	
		testpackage.setSmodifieddate(getString(arg0,"smodifieddate",arg1));	

		return testpackage;
	}

}
