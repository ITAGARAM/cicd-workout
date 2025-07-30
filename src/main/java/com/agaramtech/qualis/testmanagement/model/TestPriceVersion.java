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

@Entity
@Table(name = "testpriceversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestPriceVersion extends CustomizedResultsetRowMapper<TestPriceVersion>
		implements Serializable, RowMapper<TestPriceVersion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npriceversioncode")
	private int npriceversioncode;

	@Column(name = "sversionname", length = 100, nullable = false)
	private String sversionname;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "nversionno", nullable = false)
	private short nversionno;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sversionno;

	@Transient
	private transient String sversionstatus;

	@Override
	public TestPriceVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestPriceVersion testPriceVersion = new TestPriceVersion();
		testPriceVersion.setNpriceversioncode(getInteger(arg0, "npriceversioncode", arg1));
		testPriceVersion.setSversionname(StringEscapeUtils.unescapeJava(getString(arg0, "sversionname", arg1)));
		testPriceVersion.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		testPriceVersion.setNversionno(getShort(arg0, "nversionno", arg1));
		testPriceVersion.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		testPriceVersion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		testPriceVersion.setNstatus(getShort(arg0, "nstatus", arg1));
		testPriceVersion.setSversionno(getString(arg0, "sversionno", arg1));
		testPriceVersion.setSversionstatus(getString(arg0, "sversionstatus", arg1));
		testPriceVersion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return testPriceVersion;
	}
}
