package com.agaramtech.qualis.checklist.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name="checklistqb")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)


public class ChecklistQB extends CustomizedResultsetRowMapper<Object> implements Serializable,RowMapper<ChecklistQB> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="nchecklistqbcode" ) 
	private int nchecklistqbcode;
	
	@Column(name="nchecklistqbcategorycode", nullable=false ) 
	private int nchecklistqbcategorycode=(int)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Column(name="nchecklistcomponentcode", nullable=false ) 
	private short nchecklistcomponentcode;
	
	@Column(name="nmandatory", nullable=false )
	private short nmandatory=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	
	
	@Column(name="squestion", length = 255 ) 
	private String squestion;
	
	@Column(name="squestiondata", length = 255 )  
	private String squestiondata;
	
	@Column(name = "dmodifieddate", nullable=false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false )
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String scomponentname;
	@Transient
	private transient String smandatory;
	@Transient
	private transient String sdefaultvalue;
	@Transient
	private transient String schecklistqbcategoryname;
	@Transient
	private transient int nchecklistversionqbcode;
	@Transient
	private transient int nchecklistversioncode;
	@Transient
	private transient int nmandatoryfield;
	
	@Override
	public ChecklistQB mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistQB objChecklistQB=new ChecklistQB();
		objChecklistQB.setNchecklistqbcode(getInteger(arg0,"nchecklistqbcode",arg1));
		objChecklistQB.setNchecklistqbcategorycode(getInteger(arg0,"nchecklistqbcategorycode",arg1));
		objChecklistQB.setSchecklistqbcategoryname(getString(arg0,"schecklistqbcategoryname",arg1));
		objChecklistQB.setNchecklistcomponentcode(getShort(arg0,"nchecklistcomponentcode",arg1));
		objChecklistQB.setScomponentname(getString(arg0,"scomponentname",arg1));
		objChecklistQB.setNmandatory(getShort(arg0,"nmandatory",arg1));
		objChecklistQB.setSmandatory(getString(arg0,"smandatory",arg1));
		objChecklistQB.setSquestion(StringEscapeUtils.unescapeJava(getString(arg0,"squestion",arg1)));
		objChecklistQB.setSquestiondata(StringEscapeUtils.unescapeJava(getString(arg0,"squestiondata",arg1)));
		objChecklistQB.setSdefaultvalue(getString(arg0,"sdefaultvalue",arg1));
		objChecklistQB.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objChecklistQB.setNstatus(getShort(arg0,"nstatus",arg1));
		objChecklistQB.setNchecklistversionqbcode(getInteger(arg0,"nchecklistversionqbcode",arg1));
		objChecklistQB.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objChecklistQB.setNmandatoryfield(getInteger(arg0,"nmandatoryfield",arg1));
		objChecklistQB.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objChecklistQB;
	}
	


}
