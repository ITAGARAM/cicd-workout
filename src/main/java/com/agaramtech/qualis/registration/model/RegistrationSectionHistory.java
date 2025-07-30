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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'registrationsectionhistory' table of the Database.
 */
@Entity
@Table(name = "registrationsectionhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSectionHistory extends CustomizedResultsetRowMapper<RegistrationSectionHistory> implements Serializable,RowMapper<RegistrationSectionHistory>{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column (name ="nsectionhistorycode")
	private int nsectionhistorycode;
	
	@Column (name ="npreregno")
	private int npreregno;
	
	@Column (name="nsectioncode")
	private int nsectioncode;
	
	@Column (name="ntransactionstatus")
	private int ntransactionstatus;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Override
	public RegistrationSectionHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final RegistrationSectionHistory objRegistrationSectionHistory =new RegistrationSectionHistory();
		objRegistrationSectionHistory.setNsectionhistorycode(getInteger(arg0,"nsectionhistorycode",arg1));
		objRegistrationSectionHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objRegistrationSectionHistory.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objRegistrationSectionHistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objRegistrationSectionHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objRegistrationSectionHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
			
		return objRegistrationSectionHistory;
		
	}

}
