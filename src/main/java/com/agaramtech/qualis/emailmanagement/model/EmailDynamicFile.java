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

@Entity
@Table(name = "emaildynamicfile")
//@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmailDynamicFile extends CustomizedResultsetRowMapper<EmailDynamicFile> implements Serializable,RowMapper<EmailDynamicFile> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nemaildynamicfilecode")
	private int nemaildynamicfilecode;
	
	@Column(name = "ncontrolcode" , nullable = false )
	private int ncontrolcode;
	
	@Column(name = "nformcode" , nullable = false )
	private short nformcode;

	@Column(name = "stablename", length = 50, nullable = false)
	private String stablename;

	@Column(name = "stableprimarycolumnname", length = 50, nullable = false)
	private String stableprimarycolumnname;

	@Column(name = "sprimarykeycolumnname", length = 50, nullable = false)
	private String sprimarykeycolumnname;

	@Column(name = "sfilecolumnname", length = 50, nullable = false)
	private String sfilecolumnname;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name= "isqueryneed", nullable= false)
	@ColumnDefault("4")
	private short  isqueryneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name="squery", columnDefinition="text" )
	private String squery;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

@Override
public EmailDynamicFile mapRow(ResultSet arg0, int arg1) throws SQLException {
	EmailDynamicFile objEmailDynamicFile = new EmailDynamicFile();
	objEmailDynamicFile.setStablename(StringEscapeUtils.unescapeJava(getString(arg0,"stablename",arg1)));
	objEmailDynamicFile.setSfilecolumnname(StringEscapeUtils.unescapeJava(getString(arg0,"sfilecolumnname",arg1)));
	objEmailDynamicFile.setStableprimarycolumnname(StringEscapeUtils.unescapeJava(getString(arg0,"stableprimarycolumnname",arg1)));
	objEmailDynamicFile.setSprimarykeycolumnname(StringEscapeUtils.unescapeJava(getString(arg0,"sprimarykeycolumnname",arg1)));
	objEmailDynamicFile.setNcontrolcode(getInteger(arg0,"ncontrolcode",arg1));
	objEmailDynamicFile.setNformcode(getShort(arg0,"nformcode",arg1));
	objEmailDynamicFile.setNemaildynamicfilecode(getInteger(arg0,"nemaildynamicfile",arg1));
	objEmailDynamicFile.setNstatus(getShort(arg0,"nstatus",arg1));
	objEmailDynamicFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
	objEmailDynamicFile.setIsqueryneed(getShort(arg0,"isqueryneed",arg1));	
	objEmailDynamicFile.setSquery(StringEscapeUtils.unescapeJava(getString(arg0,"squery",arg1)));
		return objEmailDynamicFile;
}

}

