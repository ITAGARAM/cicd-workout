package com.agaramtech.qualis.stability.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stbtimepointparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StbTimePointParameter extends CustomizedResultsetRowMapper<StbTimePointParameter> implements Serializable,RowMapper<StbTimePointParameter>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstbtimepointparametercode")
	private long nstbtimepointparametercode;

	@Column(name = "nstbstudyplancode", nullable = false)
	private long nstbstudyplancode;

	@Column(name = "nstbtimepointtestcode", nullable = false)
	private long nstbtimepointtestcode;

	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;

	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;

	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;

	@Column(name = "ntestgrouptestformulacode", nullable = false)
	private int ntestgrouptestformulacode;

	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;

	@Column(name = "nresultmandatory", nullable = false)
	@ColumnDefault("4")
	private short nresultmandatory=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nreportmandatory", nullable = false)
	@ColumnDefault("4")
	private short nreportmandatory=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	public StbTimePointParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		StbTimePointParameter objStbTimePointParameter = new StbTimePointParameter();

		objStbTimePointParameter.setNstbtimepointparametercode(getLong(arg0, "nstbtimepointparametercode", arg1));
		objStbTimePointParameter.setNstbstudyplancode(getLong(arg0, "nstbstudyplancode", arg1));
		objStbTimePointParameter.setNstbtimepointtestcode(getLong(arg0, "nstbtimepointtestcode", arg1));
		objStbTimePointParameter.setNtestgrouptestparametercode(getInteger(arg0, "ntestgrouptestparametercode", arg1));
		objStbTimePointParameter.setNtestparametercode(getInteger(arg0, "ntestparametercode", arg1));
		objStbTimePointParameter.setNparametertypecode(getShort(arg0, "nparametertypecode", arg1));
		objStbTimePointParameter.setNresultmandatory(getShort(arg0, "nresultmandatory", arg1));
		objStbTimePointParameter.setNreportmandatory(getShort(arg0, "nreportmandatory", arg1));
		objStbTimePointParameter.setNtestgrouptestformulacode(getInteger(arg0, "ntestgrouptestformulacode", arg1));
		objStbTimePointParameter.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objStbTimePointParameter.setNstatus(getShort(arg0, "nstatus", arg1));
		objStbTimePointParameter.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objStbTimePointParameter.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));

		return objStbTimePointParameter;
	}

}
