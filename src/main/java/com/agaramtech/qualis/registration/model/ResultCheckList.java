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
 * This class is used to map the fields of 'resultchecklist' table of the Database.
 */
@Entity
@Table(name = "resultchecklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultCheckList extends CustomizedResultsetRowMapper<ResultCheckList> implements Serializable, RowMapper<ResultCheckList>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nresultchecklistcode")
	private int nresultchecklistcode;
	
	@Column(name = "ntransactionresultcode", nullable = false)
	private int ntransactionresultcode;
	
	@Column(name = "npreregno", nullable = false)
	private int npreregno;
	
	@Column(name = "nchecklistversioncode", nullable = false)
	private int nchecklistversioncode;	
	
	@Lob@Column(name ="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		
	@Transient
	private transient int nchecklistqbcode;
	
	@Transient
	private transient String sdefaultvalue;
	
	@Transient
	private transient int nchecklistversionqbcode;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Override
	public ResultCheckList mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ResultCheckList objResultCheckList = new ResultCheckList();
		
		objResultCheckList.setNresultchecklistcode(getInteger(arg0, "resultchecklistcode", arg1));
		objResultCheckList.setNtransactionresultcode(getInteger(arg0, "ntransactionresultcode", arg1));
		objResultCheckList.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objResultCheckList.setNchecklistversioncode(getInteger(arg0, "nchecklistversioncode", arg1));
		objResultCheckList.setNchecklistversionqbcode(getInteger(arg0, "nchecklistversionqbcode", arg1));
		objResultCheckList.setNchecklistqbcode(getInteger(arg0, "nchecklistqbcode", arg1));
		objResultCheckList.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objResultCheckList.setSarno(getString(arg0, "sarno", arg1));
		objResultCheckList.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objResultCheckList.setNstatus(getShort(arg0, "nstatus", arg1));
		objResultCheckList.setSdefaultvalue(getString(arg0, "sdefaultvalue", arg1));
		objResultCheckList.setNsitecode(getShort(arg0, "nsitecode", arg1));
	
		return objResultCheckList;
	}
	
}