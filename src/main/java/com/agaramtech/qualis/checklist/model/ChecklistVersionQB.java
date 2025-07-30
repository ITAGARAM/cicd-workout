package com.agaramtech.qualis.checklist.model;

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
@Table(name="checklistversionqb")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChecklistVersionQB extends CustomizedResultsetRowMapper<ChecklistVersionQB> implements RowMapper<ChecklistVersionQB>,Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="nchecklistversionqbcode" )  
	private int nchecklistversionqbcode;
	
	@Column(name="nchecklistversioncode", nullable=false ) 
	private int nchecklistversioncode;
	
	@Column(name="nchecklistqbcategorycode", nullable=false ) 
	private int nchecklistqbcategorycode=Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="nchecklistqbcode", nullable=false )
	private int nchecklistqbcode=Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="nmandatoryfield", nullable=false ) 
	private short nmandatoryfield;
	
	@Column(name = "dmodifieddate", nullable=false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false) 
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false )  
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Transient
	private transient String schecklistversionname;
	@Transient
	private transient String schecklistqbcategoryname;
	@Transient
	private transient String squestion;
	@Transient
	private transient String scomponentname;
	@Transient
	private transient String smandatory;
	
	@Override
	public ChecklistVersionQB mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistVersionQB objChecklistVersionQB=new ChecklistVersionQB();
		objChecklistVersionQB.setNchecklistversionqbcode(getInteger(arg0,"nchecklistversionqbcode",arg1));
		objChecklistVersionQB.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objChecklistVersionQB.setSchecklistversionname(getString(arg0,"schecklistversionname",arg1));
		objChecklistVersionQB.setNchecklistqbcategorycode(getInteger(arg0,"nchecklistqbcategorycode",arg1));
		objChecklistVersionQB.setSchecklistqbcategoryname(getString(arg0,"schecklistqbcategoryname",arg1));
		objChecklistVersionQB.setNchecklistqbcode(getInteger(arg0,"nchecklistqbcode",arg1));
		objChecklistVersionQB.setSquestion(getString(arg0,"squestion",arg1));
		objChecklistVersionQB.setScomponentname(getString(arg0,"scomponentname",arg1));
		objChecklistVersionQB.setNmandatoryfield(getShort(arg0,"nmandatoryfield",arg1));
		objChecklistVersionQB.setSmandatory(getString(arg0,"smandatory",arg1));
		objChecklistVersionQB.setNstatus(getShort(arg0,"nstatus",arg1));
		objChecklistVersionQB.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objChecklistVersionQB.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objChecklistVersionQB;
	}
	
	
}
