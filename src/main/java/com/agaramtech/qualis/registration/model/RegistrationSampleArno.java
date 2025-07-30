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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'registrationsamplearno' table of the Database.
 */
@Entity
@Table(name = "registrationsamplearno")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSampleArno extends CustomizedResultsetRowMapper<RegistrationSampleArno> implements Serializable,RowMapper<RegistrationSampleArno> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransactionsamplecode")
	private int ntransactionsamplecode;

	@Column(name = "ssamplearno", length = 30)
	private String ssamplearno="";

	@Column(name = "npreregno", nullable = false)
	private int npreregno;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Override
	public RegistrationSampleArno mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationSampleArno objRegistrationSampleArno = new RegistrationSampleArno();
		
		objRegistrationSampleArno.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationSampleArno.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objRegistrationSampleArno.setSsamplearno(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplearno", arg1)));
		objRegistrationSampleArno.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationSampleArno.setNsitecode(getShort(arg0,"nsitecode",arg1));
				
		return objRegistrationSampleArno;
	}
}
