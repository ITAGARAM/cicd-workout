package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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

/**
 * This class is used to map the fields of 'resultparametercomments' table of the Database.
 */
@Entity
@Table(name = "resultparametercomments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultParameterComments extends CustomizedResultsetRowMapper<ResultParameterComments> implements Serializable, RowMapper<ResultParameterComments> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntransactionresultcode")
	private int ntransactionresultcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;

	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;

	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Override
	public ResultParameterComments mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ResultParameterComments objResultParameterComments = new ResultParameterComments();

		objResultParameterComments.setNtransactionresultcode(getInteger(arg0, "ntransactionresultcode", arg1));
		objResultParameterComments.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objResultParameterComments.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objResultParameterComments.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objResultParameterComments.setNtestparametercode(getInteger(arg0, "ntestparametercode", arg1));
		objResultParameterComments.setNstatus(getShort(arg0, "nstatus", arg1));
		objResultParameterComments.setNsitecode(getShort(arg0,"nsitecode",arg1));
	
		return objResultParameterComments;
	}

}
