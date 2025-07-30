package com.agaramtech.qualis.dashboard.model;

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
@Table(name = "querybuilderviewscolumns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QueryBuilderViewsColumns extends CustomizedResultsetRowMapper<QueryBuilderViewsColumns>
		implements Serializable, RowMapper<QueryBuilderViewsColumns> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "sviewname")
	private String sviewname;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nstatus")
	@ColumnDefault("-1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient Map<String, Object> sjsondata;
	@Transient
	private transient String keys;
	@Transient
	private transient int index;

	@Override
	public QueryBuilderViewsColumns mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryBuilderViewsColumns queryBuilderViewsColumns = new QueryBuilderViewsColumns();

		queryBuilderViewsColumns.setSviewname(StringEscapeUtils.unescapeJava(getString(arg0, "sviewname", arg1)));
		queryBuilderViewsColumns.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		queryBuilderViewsColumns.setNstatus(getShort(arg0, "nstatus", arg1));
		queryBuilderViewsColumns.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		queryBuilderViewsColumns.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		queryBuilderViewsColumns.setSjsondata(getJsonObject(arg0, "sjsondata", arg1));
		queryBuilderViewsColumns.setKeys(getString(arg0, "keys", arg1));
		queryBuilderViewsColumns.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		queryBuilderViewsColumns.setNsitecode(getShort(arg0, "nsitecode", arg1));
		queryBuilderViewsColumns.setIndex(getInteger(arg0, "index", arg1));
		return queryBuilderViewsColumns;
	}
}
