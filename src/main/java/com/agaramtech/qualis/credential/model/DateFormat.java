package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dateformat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class DateFormat extends CustomizedResultsetRowMapper<DateFormat> implements Serializable, RowMapper<DateFormat> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndateformatcode")
	private short ndateformatcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode")
	private short nsitecode;

	@Transient
	private transient String sdateformat;

	@Override
	public DateFormat mapRow(ResultSet arg0, int arg1) throws SQLException {
		final DateFormat objDateFormat = new DateFormat();
		objDateFormat.setNdateformatcode(getShort(arg0, "ndateformatcode", arg1));
		objDateFormat.setSdateformat(StringEscapeUtils.unescapeJava(getString(arg0, "sdateformat", arg1)));
		objDateFormat.setNstatus(getShort(arg0, "nstatus", arg1));
		objDateFormat.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objDateFormat.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objDateFormat.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objDateFormat;
	}

}
