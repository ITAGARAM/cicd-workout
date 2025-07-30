package com.agaramtech.qualis.testmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
@Table(name = "testpricedetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestPriceDetail extends CustomizedResultsetRowMapper<TestPriceDetail> implements Serializable, RowMapper<TestPriceDetail> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestpricedetailcode")
	private int ntestpricedetailcode;

	@Column(name = "npriceversioncode", nullable = false)
	private int npriceversioncode;

	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;

	@Column(name = "ncost", nullable = false)
	private Double ncost;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String stestname;

	@Transient
	private transient String sversionname;

	@Override
	public TestPriceDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestPriceDetail testPrice = new TestPriceDetail();
		testPrice.setNtestpricedetailcode(getInteger(arg0, "ntestpricedetailcode", arg1));
		testPrice.setNpriceversioncode(getInteger(arg0, "npriceversioncode", arg1));
		testPrice.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		testPrice.setNcost(getDouble(arg0, "ncost", arg1));
		testPrice.setNstatus(getShort(arg0, "nstatus", arg1));
		testPrice.setNsitecode(getShort(arg0, "nsitecode", arg1));
		testPrice.setStestname(getString(arg0, "stestname", arg1));
		testPrice.setSversionname(getString(arg0, "sversionname", arg1));
		testPrice.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return testPrice;
	}
}
