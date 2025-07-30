package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * This class is used to map the fields of 'registrationarno' table of the Database.
 */
@Entity
@Table(name = "registrationarno")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationArno extends CustomizedResultsetRowMapper<RegistrationArno> implements Serializable,RowMapper<RegistrationArno> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "npreregno")
	private int npreregno;

	@Column(name = "sarno", length = 30)
	private String sarno="";

	@Column(name = "napprovalversioncode", nullable = false)
	@ColumnDefault("-1")
	private int napprovalversioncode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient int nneedautoapproval;
	
	@Override
	public RegistrationArno mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationArno objRegistrationArno = new RegistrationArno();
		
		objRegistrationArno.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationArno.setSarno(StringEscapeUtils.unescapeJava(getString(arg0, "sarno", arg1)));
		objRegistrationArno.setNapprovalversioncode(getInteger(arg0,"napprovalversioncode",arg1));
		objRegistrationArno.setNneedautoapproval(getInteger(arg0,"nneedautoapproval",arg1));
		
		objRegistrationArno.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationArno.setNsitecode(getShort(arg0,"nsitecode",arg1));		
		
		return objRegistrationArno;
	}
}
