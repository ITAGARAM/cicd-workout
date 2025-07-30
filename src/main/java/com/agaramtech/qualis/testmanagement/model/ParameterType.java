package com.agaramtech.qualis.testmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
 * This class is used to map the fields of 'parametertype' table of the Database.
 */

@Entity
@Table(name = "parametertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ParameterType extends CustomizedResultsetRowMapper<ParameterType> implements Serializable,RowMapper<ParameterType>{
	
private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nparametertypecode")
	private short nparametertypecode;
	
	@Column(name="nunitrequired", nullable=false)
	private short nunitrequired;
	
	@Column(name="npredefinedrequired", nullable=false)
	private short npredefinedrequired;
	
	@Column(name="ngraderequired", nullable=false)
	private short ngraderequired;	
	
	@Column(name="sparametertypename",  length=25, nullable=false)
	private String sparametertypename;
	
	@Column(name="sdisplaystatus", length=25, nullable=false)
	private String sdisplaystatus;
	
	@Column(name="nroundingrequired", nullable=false)
	private short nroundingrequired;
	
	@ColumnDefault("4")
	@Column(name="ndefaultstatus", nullable=false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Override
	public ParameterType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ParameterType objParameterType = new ParameterType();
		
		objParameterType.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objParameterType.setNunitrequired(getShort(arg0,"nunitrequired",arg1));
		objParameterType.setNpredefinedrequired(getShort(arg0,"npredefinedrequired",arg1));
		objParameterType.setNgraderequired(getShort(arg0,"ngraderequired",arg1));
		objParameterType.setSparametertypename(getString(arg0,"sparametertypename",arg1));
		objParameterType.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objParameterType.setNroundingrequired(getShort(arg0,"nroundingrequired",arg1));
		objParameterType.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objParameterType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objParameterType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objParameterType.setNstatus(getShort(arg0,"nstatus",arg1));

		return objParameterType;
	}

}
