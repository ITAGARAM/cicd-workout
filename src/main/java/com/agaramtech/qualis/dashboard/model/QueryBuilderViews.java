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
@Table(name = "querybuilderviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QueryBuilderViews extends CustomizedResultsetRowMapper<QueryBuilderViews>
		implements Serializable, RowMapper<QueryBuilderViews> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nquerybuilderviewscode")
	private short nquerybuilderviewscode;

	@Column(name = "sviewname")
	private String sviewname;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;

	@Override
	public QueryBuilderViews mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryBuilderViews queryBuilderViews = new QueryBuilderViews();

		queryBuilderViews.setNquerybuilderviewscode(getShort(arg0, "nquerybuilderviewscode", arg1));
		queryBuilderViews.setSviewname(StringEscapeUtils.unescapeJava(getString(arg0, "sviewname", arg1)));
		queryBuilderViews.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		queryBuilderViews.setNstatus(getShort(arg0, "nstatus", arg1));
		queryBuilderViews.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		queryBuilderViews.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		queryBuilderViews.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		queryBuilderViews.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return queryBuilderViews;
	}
}
