package com.agaramtech.qualis.submitter.model;

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
@Table(name = "city")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class City extends CustomizedResultsetRowMapper<City> implements Serializable, RowMapper<City> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncitycode")
	private int ncitycode;

	@Column(name = "scityname", length = 100, nullable = false)
	private String scityname;

	@Column(name = "scitycode", length = 5, nullable = false)
	private String scitycode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "ndistrictcode")
	private int ndistrictcode;

	@Transient
	private transient String sdistrictname;

	@Transient
	private transient String smodifieddate;

	@Override
	public City mapRow(ResultSet arg0, int arg1) throws SQLException {
		final City objCity = new City();
		objCity.setNcitycode(getInteger(arg0, "ncitycode", arg1));
		objCity.setScityname(StringEscapeUtils.unescapeJava(getString(arg0, "scityname", arg1)));
		objCity.setScitycode(StringEscapeUtils.unescapeJava(getString(arg0, "scitycode", arg1)));
		objCity.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCity.setNstatus(getShort(arg0, "nstatus", arg1));
		objCity.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCity.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objCity.setSdistrictname(getString(arg0, "sdistrictname", arg1));
		objCity.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objCity;
	}

}