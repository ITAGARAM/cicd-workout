package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "querybuilder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QueryBuilder extends CustomizedResultsetRowMapper<QueryBuilder> implements Serializable, RowMapper<QueryBuilder> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nquerybuildercode")
	private short nquerybuildercode;

	@Column(name = "squerybuildername")
	private String squerybuildername = "";

	@Column(name = "nquerytype")
	private short nquerytype = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "sviewname")
	private String sviewname = "";

	@Column(name = "squerywithparam")
	private String squerywithparam = "";

	@Column(name = "squerywithvalue")
	private String squerywithvalue = "";

	@Lob
	@Column(name = "sdefaultvalue", columnDefinition = "jsonb")
	private Map<String, Object> sdefaultvalue;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public QueryBuilder mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.setNquerybuildercode(getShort(arg0, "nquerybuildercode", arg1));
		queryBuilder.setSquerybuildername(StringEscapeUtils.unescapeJava(getString(arg0, "squerybuildername", arg1)));
		queryBuilder.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		queryBuilder.setSdefaultvalue(unescapeString(getJsonObject(arg0, "sdefaultvalue", arg1)));
		queryBuilder.setNquerytype(getShort(arg0, "nquerytype", arg1));
		queryBuilder.setSviewname(StringEscapeUtils.unescapeJava(getString(arg0, "sviewname", arg1)));
		queryBuilder.setSquerywithparam(StringEscapeUtils.unescapeJava(getString(arg0, "squerywithparam", arg1)));
		queryBuilder.setSquerywithvalue(StringEscapeUtils.unescapeJava(getString(arg0, "squerywithvalue", arg1)));
		queryBuilder.setNstatus(getShort(arg0, "nstatus", arg1));

		return queryBuilder;
	}

}
