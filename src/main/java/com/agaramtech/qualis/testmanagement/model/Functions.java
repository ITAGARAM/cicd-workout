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
 * This class is used to map the fields of 'functions' table of the
 * Database. 
 */
@Entity
@Table(name = "functions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class Functions extends CustomizedResultsetRowMapper<Functions> implements Serializable, RowMapper<Functions> {

private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nfunctioncode")
	private short nfunctioncode;
	
	@Column(name="sfunctionsyntax",  length=255)
	private String sfunctionsyntax="";
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false)
	private short nstatus;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Transient
	private transient String sfunctionname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;


	@Override
	public Functions mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Functions objFunctions = new Functions();
		objFunctions.setNfunctioncode(getShort(arg0,"nfunctioncode",arg1));
		objFunctions.setSfunctionname(getString(arg0,"sfunctionname",arg1));
		objFunctions.setSfunctionsyntax(StringEscapeUtils.unescapeJava(getString(arg0,"sfunctionsyntax",arg1)));
		objFunctions.setNstatus(getShort(arg0,"nstatus",arg1));
		objFunctions.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objFunctions.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objFunctions.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objFunctions.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objFunctions.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		return objFunctions;
	}
}
