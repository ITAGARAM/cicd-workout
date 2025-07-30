package com.agaramtech.qualis.registration.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'registrationsection' table of the Database.
 */
@Entity
@Table(name = "registrationsection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSection extends CustomizedResultsetRowMapper<RegistrationSection> implements RowMapper<RegistrationSection>{

	@Id
	@Column(name = "nregistrationsectioncode")
	private int nregistrationsectioncode;
	
	@Column(name = "npreregno")
	private int npreregno;
	
	@Column(name = "nsectioncode")
	private int nsectioncode;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public RegistrationSection mapRow(ResultSet arg0, int arg1) throws SQLException {
		RegistrationSection  objRegistrationSection = new RegistrationSection();
		objRegistrationSection.setNregistrationsectioncode(getInteger(arg0,"nregistrationsectioncode",arg1));
		objRegistrationSection.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationSection.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objRegistrationSection.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationSection.setNstatus(getShort(arg0,"nstatus",arg1));
		return objRegistrationSection;
	}

}
