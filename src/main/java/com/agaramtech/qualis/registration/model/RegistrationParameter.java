package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * This class is used to map the fields of 'registrationparameter' table of the Database.
 */
@Entity
@Table(name = "registrationparameter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationParameter extends CustomizedResultsetRowMapper<RegistrationParameter> implements Serializable, RowMapper<RegistrationParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntransactionresultcode")
	private int ntransactionresultcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "ntestgrouptestparametercode", nullable = false)
	private int ntestgrouptestparametercode;

	@Column(name = "ntestparametercode", nullable = false)
	private int ntestparametercode;

	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;

	@Column(name = "ntestgrouptestformulacode", nullable = false)
	private int ntestgrouptestformulacode;

	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;

	@Column(name = "nresultmandatory", nullable = false)
	@ColumnDefault("4")
	private short nresultmandatory =  (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nreportmandatory", nullable = false)
	@ColumnDefault("4")
	private short nreportmandatory = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String senforceresultcomment;

	@Override
	public RegistrationParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final RegistrationParameter objRegistrationParemter = new RegistrationParameter();

		objRegistrationParemter.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));
		objRegistrationParemter.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationParemter.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objRegistrationParemter.setNtestgrouptestparametercode(getInteger(arg0,"ntestgrouptestparametercode",arg1));
		objRegistrationParemter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objRegistrationParemter.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objRegistrationParemter.setNresultmandatory(getShort(arg0,"nresultmandatory",arg1));
		objRegistrationParemter.setNreportmandatory(getShort(arg0,"nreportmandatory",arg1));
		objRegistrationParemter.setNtestgrouptestformulacode(getInteger(arg0,"ntestgrouptestformulacode",arg1));
		objRegistrationParemter.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objRegistrationParemter.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationParemter.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationParemter.setSenforceresultcomment(getString(arg0,"senforceresultcomment", arg1));

		return objRegistrationParemter;
	}

}
