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

/**
 * This class is used to map the fields of 'methodcategory' table of the
 * Database. 
 */
@Entity
@Table(name = "methodcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class MethodCategory extends CustomizedResultsetRowMapper<MethodCategory> implements Serializable, RowMapper<MethodCategory> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmethodcatcode")
	private int nmethodcatcode;
	
	@Column(name = "smethodcatname", length = 100, nullable=false)
	private String smethodcatname;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Override
	public MethodCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MethodCategory methodcategory = new MethodCategory();
		
		
		methodcategory.setNmethodcatcode(getInteger(arg0,"nmethodcatcode",arg1));
		methodcategory.setSmethodcatname(StringEscapeUtils.unescapeJava(getString(arg0,"smethodcatname",arg1)));
		methodcategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		methodcategory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		methodcategory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		methodcategory.setNstatus(getShort(arg0,"nstatus",arg1));

		return methodcategory;
	}


}
