package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
@Table(name = "discountband")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DiscountBand extends CustomizedResultsetRowMapper<DiscountBand> implements Serializable, RowMapper<DiscountBand> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ndiscountbandcode")
	private int ndiscountbandcode;

	@Column(name = "sdiscountbandname", length = 100, nullable = false)
	private String sdiscountbandname;

	@Column(name = "namount")
	private float namount;

	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	public DiscountBand mapRow(ResultSet arg0, int arg1) throws SQLException {
		DiscountBand discountBand = new DiscountBand();
		discountBand.setNdiscountbandcode(getInteger(arg0, "ndiscountbandcode", arg1));
		discountBand.setSdiscountbandname(StringEscapeUtils.unescapeJava(getString(arg0, "sdiscountbandname", arg1)));
		discountBand.setNamount(getFloat(arg0, "namount", arg1));
		discountBand.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		discountBand.setDmodifieddate(getDate(arg0, "dmodifieddate", arg1));
		discountBand.setNsitecode(getShort(arg0, "nsitecode", arg1));
		discountBand.setNstatus(getShort(arg0, "nstatus", arg1));
		return discountBand;
	}
}