package com.agaramtech.qualis.quotation.model;

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
@Table(name = "sampletestcomments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VATBand extends CustomizedResultsetRowMapper<VATBand> implements Serializable, RowMapper<VATBand> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nvatbandcode")
	private int nvatbandcode;

	@Column(name = "svatbandname", length = 100, nullable = false)
	private String svatbandname;

	@Column(name = "namount")
	private float namount;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	public VATBand mapRow(ResultSet arg0, int arg1) throws SQLException {
		VATBand vatBand = new VATBand();
		vatBand.setNvatbandcode(getInteger(arg0, "nvatbandcode", arg1));
		vatBand.setSvatbandname(StringEscapeUtils.unescapeJava(getString(arg0, "svatbandname", arg1)));
		vatBand.setNamount(getFloat(arg0, "namount", arg1));
		vatBand.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		vatBand.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		vatBand.setNsitecode(getShort(arg0, "nsitecode", arg1));
		vatBand.setNstatus(getShort(arg0, "nstatus", arg1));
		return vatBand;
	}
}