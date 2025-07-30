package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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
@Table(name = "querybuildertablecolumns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class QueryBuilderTableColumns extends CustomizedResultsetRowMapper<QueryBuilderTableColumns>
		implements Serializable, RowMapper<QueryBuilderTableColumns> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nquerybuildertablecode")
	private short nquerybuildertablecode;

	@Column(name = "stablename")
	private String stablename;

	@Column(name = "sprimarykeyname")
	private String sprimarykeyname;

	@Lob
	@Column(name = "jstaticcolumns", columnDefinition = "jsonb")
	private List<Object> jstaticcolumns;

	@Lob
	@Column(name = "jdynamiccolumns", columnDefinition = "jsonb")
	private List<Object> jdynamiccolumns;

	@Lob
	@Column(name = "jmultilingualcolumn", columnDefinition = "jsonb")
	private List<Object> jmultilingualcolumn;

	@Lob
	@Column(name = "jnumericcolumns", columnDefinition = "jsonb")
	private List<Object> jnumericcolumns;

	@Lob
	@Column(name = "jsqlquerycolumns", columnDefinition = "jsonb")
	private List<Object> jsqlquerycolumns;

	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Lob
	@Column(name = "jsonimportdata", columnDefinition = "jsonb")
	private Map<String, Object> jsonimportdata;

	@Transient
	private transient String scolumnname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient Map<String, Object> sjsondata;
	@Transient
	private transient short index;
	@Transient
	private transient int ndesigntemplatemappingcode;

	@Override
	public QueryBuilderTableColumns mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryBuilderTableColumns objQueryBuilderTableColumns = new QueryBuilderTableColumns();

		objQueryBuilderTableColumns.setStablename(StringEscapeUtils.unescapeJava(getString(arg0, "stablename", arg1)));
		objQueryBuilderTableColumns.setSprimarykeyname(StringEscapeUtils.unescapeJava(getString(arg0, "sprimarykeyname", arg1)));
		objQueryBuilderTableColumns.setJstaticcolumns(getJSONArray(arg0, "jstaticcolumns", arg1));
		objQueryBuilderTableColumns.setJdynamiccolumns(getJSONArray(arg0, "jdynamiccolumns", arg1));
		objQueryBuilderTableColumns.setJnumericcolumns(getJSONArray(arg0, "jnumericcolumns", arg1));
		objQueryBuilderTableColumns.setJmultilingualcolumn(getJSONArray(arg0, "jmultilingualcolumn", arg1));
		objQueryBuilderTableColumns.setNstatus(getShort(arg0, "nstatus", arg1));
		objQueryBuilderTableColumns.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objQueryBuilderTableColumns.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objQueryBuilderTableColumns.setScolumnname(getString(arg0, "scolumnname", arg1));
		objQueryBuilderTableColumns.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objQueryBuilderTableColumns.setSjsondata(getJsonObject(arg0, "sjsondata", arg1));
		objQueryBuilderTableColumns.setIndex(getShort(arg0, "index", arg1));
		objQueryBuilderTableColumns.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objQueryBuilderTableColumns.setJsqlquerycolumns(getJSONArray(arg0, "jsqlquerycolumns", arg1));
		objQueryBuilderTableColumns.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objQueryBuilderTableColumns.setJsonimportdata(unescapeString(getJsonObject(arg0, "jsonimportdata", arg1)));

		return objQueryBuilderTableColumns;
	}

}
