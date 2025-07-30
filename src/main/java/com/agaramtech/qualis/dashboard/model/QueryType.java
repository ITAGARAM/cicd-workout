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
@Table(name = "querytype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QueryType extends CustomizedResultsetRowMapper<QueryType> implements Serializable, RowMapper<QueryType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nquerytypecode")
	private short nquerytypecode;

	@Column(name = "squerytypename", length = 100, nullable = false)
	private String squerytypename;

	@ColumnDefault("1")
	@Column(name = "nsorter", nullable = false)
	private short nsorter = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Override
	public QueryType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryType queryType = new QueryType();
		queryType.setNquerytypecode(getShort(arg0, "nquerytypecode", arg1));
		queryType.setSquerytypename(StringEscapeUtils.unescapeJava(getString(arg0, "squerytypename", arg1)));
		queryType.setNstatus(getShort(arg0, "nstatus", arg1));
		queryType.setNsorter(getShort(arg0, "nsorter", arg1));
		queryType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		queryType.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return queryType;
	}

}
