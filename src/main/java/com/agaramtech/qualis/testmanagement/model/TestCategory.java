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
 * This class is used to map the fields of 'testcategory' table of the Database.
 */
@Entity
@Table(name = "testcategory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestCategory extends CustomizedResultsetRowMapper<TestCategory> implements Serializable, RowMapper<TestCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestcategorycode")
	private int ntestcategorycode;
	
	@Column(name = "stestcategoryname", length = 100, nullable = false)
	private String stestcategoryname;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus")
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =  (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nclinicaltyperequired")
	private short nclinicaltyperequired = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient String sclinicalstatus;

	@Override
	public TestCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestCategory objTestCategory = new TestCategory();
		objTestCategory.setNtestcategorycode(getInteger(arg0, "ntestcategorycode", arg1));
		objTestCategory.setStestcategoryname(StringEscapeUtils.unescapeJava(getString(arg0, "stestcategoryname", arg1)));
		objTestCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objTestCategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objTestCategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objTestCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTestCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objTestCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTestCategory.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objTestCategory.setNclinicaltyperequired(getShort(arg0, "nclinicaltyperequired", arg1));
		objTestCategory.setSclinicalstatus(getString(arg0, "sclinicalstatus", arg1));

		return objTestCategory;
	}

}
