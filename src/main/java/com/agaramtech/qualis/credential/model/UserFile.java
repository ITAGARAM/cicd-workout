package com.agaramtech.qualis.credential.model;
/**
 * This class is used to map fields of 'userrolepolicy' table of database
*/

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
@Table(name = "userfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class UserFile extends CustomizedResultsetRowMapper<UserFile> implements Serializable, RowMapper<UserFile> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nuserfilecode")	
	private int nuserfilecode;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@Column(name = "ssignimgname", length= 100)
	private String ssignimgname;
	
	@Column(name = "ssignimgftp", length = 100)	
	private String ssignimgftp;
	
	@Column(name = "suserimgname", length = 100)
	private String suserimgname;
	
	@Column(name = "suserimgftp", length = 100)
	private String suserimgftp;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")	
	@Column(name = "nsitecode")	
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")	
	@Column(name = "nstatus", nullable = false)
	private short nstatus =  (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	


	@Override
	public UserFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UserFile userFile = new UserFile();
		userFile.setNstatus(getShort(arg0,"nstatus",arg1));
		userFile.setNusercode(getInteger(arg0,"nusercode",arg1));
		userFile.setNuserfilecode(getInteger(arg0,"nuserfilecode",arg1));
		userFile.setSsignimgftp(StringEscapeUtils.unescapeJava(getString(arg0,"ssignimgftp",arg1)));
		userFile.setSsignimgname(StringEscapeUtils.unescapeJava(getString(arg0,"ssignimgname",arg1)));
		userFile.setSuserimgftp(StringEscapeUtils.unescapeJava(getString(arg0,"suserimgftp",arg1)));
		userFile.setSuserimgname(StringEscapeUtils.unescapeJava(getString(arg0,"suserimgname",arg1)));
		userFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		userFile.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return userFile;
	}

}
