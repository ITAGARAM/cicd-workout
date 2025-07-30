package com.agaramtech.qualis.dashboard.model;

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
@Table(name = "querytabletype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QueryTableType extends CustomizedResultsetRowMapper<QueryTableType> implements Serializable, RowMapper<QueryTableType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntabletypecode")
	private short ntabletypecode;

	@Column(name = "stabletype", length = 30, nullable = false)
	private String stabletype;

	@Column(name = "sidstablename", length = 30, nullable = false)
	private String sidstablename;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Override
	public QueryTableType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryTableType queryTableType = new QueryTableType();

		queryTableType.setNtabletypecode(getShort(arg0, "ntabletypecode", arg1));
		queryTableType.setStabletype(StringEscapeUtils.unescapeJava(getString(arg0, "stabletype", arg1)));
		queryTableType.setSidstablename(StringEscapeUtils.unescapeJava(getString(arg0, "sidstablename", arg1)));
		queryTableType.setNstatus(getShort(arg0, "nstatus", arg1));
		queryTableType.setDmodifieddate(getInstant(arg0, "modifieddate", arg1));
		queryTableType.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return queryTableType;
	}

}
