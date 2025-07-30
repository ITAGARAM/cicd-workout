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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sqlquery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SQLQuery extends CustomizedResultsetRowMapper<SQLQuery> implements Serializable, RowMapper<SQLQuery> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsqlquerycode")
	private int nsqlquerycode;
	@Column(name = "nquerytypecode", nullable = false)
	private short nquerytypecode;
	@Column(name = "ssqlqueryname", length = 100)
	private String ssqlqueryname="";
	@Column(name = "ssqlquery", columnDefinition = "text")
	private String ssqlquery="";
	@Column(name = "sscreenrecordquery", columnDefinition = "text")
	private String sscreenrecordquery="";
	@Column(name = "sscreenheader", length = 100)
	private String sscreenheader="";
	@ColumnDefault("-1")
	@Column(name = "ncharttypecode", nullable = false)
	private short ncharttypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "svaluemember", length = 30)
	private String svaluemember="";
	@Column(name = "sdisplaymember", length = 30)
	private String sdisplaymember="";
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String squerytypename;
	@Transient
	private transient String schartname;
	@Transient
	private transient int nalertrightscode;

	@Override
	public SQLQuery mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SQLQuery sqlQuery = new SQLQuery();

		sqlQuery.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		sqlQuery.setNquerytypecode(getShort(arg0, "nquerytypecode", arg1));
		sqlQuery.setSsqlqueryname(StringEscapeUtils.unescapeJava(getString(arg0, "ssqlqueryname", arg1)));
		sqlQuery.setSsqlquery(StringEscapeUtils.unescapeJava(getString(arg0, "ssqlquery", arg1)));
		sqlQuery.setSscreenrecordquery(StringEscapeUtils.unescapeJava(getString(arg0, "sscreenrecordquery", arg1)));
		sqlQuery.setSscreenheader(StringEscapeUtils.unescapeJava(getString(arg0, "sscreenheader", arg1)));
		sqlQuery.setNcharttypecode(getShort(arg0, "ncharttypecode", arg1));
		sqlQuery.setSquerytypename(getString(arg0, "squerytypename", arg1));
		sqlQuery.setSchartname(getString(arg0, "schartname", arg1));
		sqlQuery.setSvaluemember(StringEscapeUtils.unescapeJava(getString(arg0, "svaluemember", arg1)));
		sqlQuery.setSdisplaymember(StringEscapeUtils.unescapeJava(getString(arg0, "sdisplaymember", arg1)));
		sqlQuery.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sqlQuery.setNstatus(getShort(arg0, "nstatus", arg1));
		sqlQuery.setNalertrightscode(getInteger(arg0, "nalertrightscode", arg1));
		sqlQuery.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return sqlQuery;
	}

}
