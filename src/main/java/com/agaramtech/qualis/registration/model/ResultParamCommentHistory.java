package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

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

/**
 * This class is used to map the fields of 'resultparamcommenthistory' table of the Database.
 */
@Entity
@Table(name = "resultparamcommenthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultParamCommentHistory extends CustomizedResultsetRowMapper<ResultParamCommentHistory> implements Serializable, RowMapper<ResultParamCommentHistory> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nresultparamcommenthistorycode")
	private int nresultparamcommenthistorycode;

	@Column(name = "ntransactionresultcode", nullable = false)
	private int ntransactionresultcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;

	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Transient
	private transient String sresultcomment;
	
	@Transient
	private transient String senforcestatuscomment;
	
	@Transient
	private transient String senforceresultcomment;

	@Override
	public ResultParamCommentHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ResultParamCommentHistory objResultParameterComments = new ResultParamCommentHistory();

		objResultParameterComments.setNresultparamcommenthistorycode(getInteger(arg0,"nresultparamcommenthistorycode",arg1));
		objResultParameterComments.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));
		objResultParameterComments.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objResultParameterComments.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objResultParameterComments.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objResultParameterComments.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objResultParameterComments.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objResultParameterComments.setSresultcomment(getString(arg0,"sresultcomment",arg1));
		objResultParameterComments.setSenforcestatuscomment(getString(arg0,"senforcestatuscomment",arg1));
		objResultParameterComments.setSenforceresultcomment(getString(arg0,"senforceresultcomment",arg1));
		objResultParameterComments.setNstatus(getShort(arg0,"nstatus",arg1));
		objResultParameterComments.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objResultParameterComments;
	}

}
