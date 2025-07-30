package com.agaramtech.qualis.emailmanagement.model;

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
 * This class is used to map the fields of 'emailuserquery' table of the Database.
 */
@Entity
@Table(name = "emailuserquery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmailUserQuery extends CustomizedResultsetRowMapper<EmailUserQuery> implements Serializable, RowMapper<EmailUserQuery> {
	

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nemailuserquerycode")
	private int nemailuserquerycode;
	
	@Column(name="squery", columnDefinition="text", nullable=false )
	private String squery;
	
	@Column(name="nformcode", nullable=false)
	private int nformcode;
	
	@Column(name ="sdisplayname" ,length=20, nullable=false) 
	private String sdisplayname;
	
	@Column(name = "dmodifieddate", nullable=false) 
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Override
	public EmailUserQuery mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final EmailUserQuery objEmailUserQuery = new EmailUserQuery();
		
		objEmailUserQuery.setNemailuserquerycode(getInteger(arg0,"nemailuserquerycode",arg1));
		objEmailUserQuery.setSquery(StringEscapeUtils.unescapeJava(getString(arg0,"squery",arg1)));
		objEmailUserQuery.setNformcode(getInteger(arg0,"nformcode",arg1));
		objEmailUserQuery.setNstatus(getShort(arg0,"nstatus",arg1));
		objEmailUserQuery.setSdisplayname(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplayname",arg1)));
		return objEmailUserQuery;
	}
	
}
