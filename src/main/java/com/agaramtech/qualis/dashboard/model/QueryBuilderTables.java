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
@Table(name = "querybuildertables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QueryBuilderTables extends CustomizedResultsetRowMapper<QueryBuilderTables>
		implements Serializable, RowMapper<QueryBuilderTables> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nquerybuildertablecode")
	private short nquerybuildertablecode;

	@Column(name = "nformcode")
	private short nformcode;

	@Column(name = "stablename")
	private String stablename;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nismastertable")
	@ColumnDefault("4")
	private short nismastertable=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sformname;
	@Transient
	private transient String methodUrl;
	@Transient
	private transient boolean isMasterAdd;
	@Transient
	private transient String classUrl;
	@Transient
	private transient String component;
	@Transient
	private transient int addControlCode;
	@Transient
	private transient int editControlCode;

	@Override
	public QueryBuilderTables mapRow(ResultSet arg0, int arg1) throws SQLException {

		final QueryBuilderTables objQueryBuilderTables = new QueryBuilderTables();

		objQueryBuilderTables.setNquerybuildertablecode(getShort(arg0, "nquerybuildertablecode", arg1));
		objQueryBuilderTables.setNformcode(getShort(arg0, "nformcode", arg1));
		objQueryBuilderTables.setStablename(StringEscapeUtils.unescapeJava(getString(arg0, "stablename", arg1)));
		objQueryBuilderTables.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objQueryBuilderTables.setNismastertable(getShort(arg0, "nismastertable", arg1));
		objQueryBuilderTables.setNstatus(getShort(arg0, "nstatus", arg1));
		objQueryBuilderTables.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objQueryBuilderTables.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objQueryBuilderTables.setSformname(getString(arg0, "sformname", arg1));
		objQueryBuilderTables.setMethodUrl(getString(arg0, "methodUrl", arg1));
		objQueryBuilderTables.setMasterAdd(getBoolean(arg0, "ismasterAdd", arg1));
		objQueryBuilderTables.setClassUrl(getString(arg0, "classUrl", arg1));
		objQueryBuilderTables.setComponent(getString(arg0, "component", arg1));
		objQueryBuilderTables.setAddControlCode(getInteger(arg0, "addControlCode", arg1));
		objQueryBuilderTables.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objQueryBuilderTables.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objQueryBuilderTables.setEditControlCode(getInteger(arg0, "editControlCode", arg1));

		return objQueryBuilderTables;
	}

}
