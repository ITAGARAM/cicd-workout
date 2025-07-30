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
 * This class is used to map the fields of 'resultusedtasks' table of the Database.
 */
@Entity
@Table(name = "resultusedtasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultUsedTasks extends CustomizedResultsetRowMapper<ResultUsedTasks> implements Serializable, RowMapper<ResultUsedTasks> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nresultusedtaskcode")
	private int nresultusedtaskcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Lob@Column(name ="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =  (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sarno;
	@Transient
	private transient String ssamplearno;
	
	@Override
	public ResultUsedTasks mapRow(ResultSet arg0, int arg1) throws SQLException {
		ResultUsedTasks objResultUsedTask = new ResultUsedTasks();
		objResultUsedTask.setNresultusedtaskcode(getInteger(arg0, "nresultusedtaskcode", arg1));
		objResultUsedTask.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objResultUsedTask.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objResultUsedTask.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objResultUsedTask.setNstatus(getShort(arg0, "nstatus", arg1));
		objResultUsedTask.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objResultUsedTask;
	}

}

