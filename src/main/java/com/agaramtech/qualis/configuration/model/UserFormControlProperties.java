package com.agaramtech.qualis.configuration.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "userformcontrolproperties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserFormControlProperties extends CustomizedResultsetRowMapper<UserFormControlProperties> implements Serializable, RowMapper<UserFormControlProperties> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nuserformcontrolpropertiescode")
	private short nuserformcontrolpropertiescode;
	
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	
	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;
	
	@Column(name = "nperiodcode", nullable = false)
	private short nperiodcode;
	
	@Column(name = "nwindowperiod", nullable = false)
	private short nwindowperiod;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String scontrolname;
	
	@Transient
	private transient int ndata;

	@Override
	public UserFormControlProperties mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final UserFormControlProperties objUFCP = new UserFormControlProperties();
		objUFCP.setNuserformcontrolpropertiescode(getShort(arg0,"nuserformcontrolpropertiescode",arg1));
		objUFCP.setNcontrolcode(getShort(arg0,"ncontrolcode",arg1));
		objUFCP.setNformcode(getShort(arg0,"nformcode",arg1));
		objUFCP.setNperiodcode(getShort(arg0,"nperiodcode",arg1));
		objUFCP.setNwindowperiod(getShort(arg0,"nwindowperiod",arg1));
		objUFCP.setNstatus(getShort(arg0,"nstatus",arg1));
		objUFCP.setScontrolname(getString(arg0,"scontrolname",arg1));
		objUFCP.setNdata(getInteger(arg0,"ndata",arg1));
		objUFCP.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objUFCP.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objUFCP;
	}
}
