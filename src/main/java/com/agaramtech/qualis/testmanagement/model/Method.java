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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'method' table of the Database.
 * 
 * @author ATE180
 * @version 9.0.0.1
 * @since 1-July-2020
 */

@Entity
@Table(name = "method")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Method extends CustomizedResultsetRowMapper<Method> implements Serializable,RowMapper<Method>{

	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmethodcode")
	private int nmethodcode;

	@Column(name = "nmethodcatcode", nullable=false)
	private int nmethodcatcode;

	@Column(name = "smethodname",  length = 100, nullable=false)
	private String smethodname;

	@Column(name = "sdescription", length = 255)
	private String sdescription="";

	@ColumnDefault("4")
	@Column(name = "nneedvalidity", nullable=false)
	private short nneedvalidity=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable=false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate",nullable=false)
	private Instant dmodifieddate;

	@Transient
	private transient String smethodcatname;
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String smethodvalidityenable;

	@Override
	public Method mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Method method = new Method();

		method.setNstatus(getShort(arg0,"nstatus",arg1));
		method.setNmethodcode(getInteger(arg0,"nmethodcode",arg1));
		method.setNmethodcatcode(getInteger(arg0,"nmethodcatcode",arg1));
		method.setNsitecode(getShort(arg0,"nsitecode",arg1));
		method.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		method.setSmethodname(StringEscapeUtils.unescapeJava(getString(arg0,"smethodname",arg1)));
		method.setNneedvalidity(getShort(arg0,"nneedvalidity",arg1));
		method.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		method.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		method.setSmethodcatname(getString(arg0,"smethodcatname",arg1));
		method.setSmethodvalidityenable(getString(arg0,"smethodvalidityenable",arg1));
		method.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return method;
	}

}
