package com.agaramtech.qualis.testmanagement.model;


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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "predefinedformula")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class PreDefinedFormula extends CustomizedResultsetRowMapper<PreDefinedFormula> implements Serializable,RowMapper<PreDefinedFormula> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npredefinedformulacode") 
	private short npredefinedformulacode;
	
	@Column(name = "spredefinedformula", length = 100, nullable = false)
	private String spredefinedformula;
	
	@Column(name = "sdescription", length = 255) 
	private String sdescription="";
	
	@Column(name = "nstatus", nullable = false) 
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@ColumnDefault("-1")
	@Column(name ="nsitecode", nullable=false) 
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Column(name="dmodifieddate") 
	private Instant dmodifieddate;

	@Override
	public PreDefinedFormula mapRow(ResultSet arg0, int arg1) throws SQLException {
		final PreDefinedFormula objPreDefinedFormula = new PreDefinedFormula();
		objPreDefinedFormula.setNpredefinedformulacode(getShort(arg0,"npredefinedformulacode",arg1));
		objPreDefinedFormula.setSpredefinedformula(StringEscapeUtils.unescapeJava(getString(arg0,"spredefinedformula",arg1)));
		objPreDefinedFormula.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objPreDefinedFormula.setNstatus(getShort(arg0,"nstatus",arg1));
		objPreDefinedFormula.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objPreDefinedFormula.setNsitecode(getShort(arg0,"nsitecode",arg1)); 

	return objPreDefinedFormula;
	}
	
	
      
}
