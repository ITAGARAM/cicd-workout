package com.agaramtech.qualis.testmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
 * This class is used to map the fields of 'dynamicformulafields' table of the
 * Database. 
 */
@Entity
@Table(name = "dynamicformulafields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class DynamicFormulaFields extends CustomizedResultsetRowMapper<DynamicFormulaFields> implements Serializable, RowMapper<DynamicFormulaFields> {

private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ndynamicformulafieldcode")
	private int ndynamicformulafieldcode;
	@Column(name="sdescription",  length=255, nullable=false)
	private String sdescription;
	@Column(name="sdatatype",  length=100, nullable=false)
	private String sdatatype;
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false)	
	private int nstatus;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;

	
	@Transient
	 private transient String svalue;
	@Transient
	 private transient int ntestparametercode;
	@Transient
	 private transient int ntransactiontestcode;
	@Transient
	 private transient int ntransactionresultcode;
	@Transient
	 private transient int nisaverageneed;
	@Transient
	 private transient int ntransactionsamplecode;
	@Transient
	 private transient String sdefaultname;
	@Transient
	 private transient String sdisplayname;
	@Transient
	 private transient String sdynamicfieldname;


	@Override
	public DynamicFormulaFields mapRow(ResultSet arg0, int arg1) throws SQLException {
		final DynamicFormulaFields objDynamicFormulaFields = new DynamicFormulaFields();
		objDynamicFormulaFields.setNdynamicformulafieldcode(getInteger(arg0, "ndynamicformulafieldcode", arg1));
		objDynamicFormulaFields.setSdynamicfieldname(getString(arg0, "sdynamicfieldname", arg1));
		objDynamicFormulaFields.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objDynamicFormulaFields.setSdatatype(StringEscapeUtils.unescapeJava(getString(arg0, "sdatatype", arg1)));
		objDynamicFormulaFields.setNstatus(getInteger(arg0, "nstatus", arg1));
		objDynamicFormulaFields.setSvalue(getString(arg0, "svalue", arg1));
		objDynamicFormulaFields.setNtestparametercode(getInteger(arg0, "ntestparametercode", arg1));
		objDynamicFormulaFields.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objDynamicFormulaFields.setNtransactionresultcode(getInteger(arg0, "ntransactionresultcode", arg1));
		objDynamicFormulaFields.setNisaverageneed(getInteger(arg0, "nisaverageneed", arg1));
		objDynamicFormulaFields.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objDynamicFormulaFields.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objDynamicFormulaFields.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objDynamicFormulaFields.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objDynamicFormulaFields.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		return objDynamicFormulaFields;
	}

}
