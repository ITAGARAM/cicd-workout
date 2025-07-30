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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'unit' table of the Database.
 */
@Entity
@Table(name="checklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Checklist extends CustomizedResultsetRowMapper<Checklist> implements RowMapper<Checklist>,Serializable{


	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="nchecklistcode")  
	private int nchecklistcode;
	
	@Column(name="schecklistname", length =100, nullable=false) 
	private String schecklistname;
	
	@Column(name="sdescription", length = 255)
	private String sdescription;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false) 
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false) 
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	
	@Override
	public Checklist mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Checklist objChecklist=new Checklist();
		objChecklist.setNchecklistcode(getInteger(arg0,"nchecklistcode",arg1));
		objChecklist.setSchecklistname(StringEscapeUtils.unescapeJava(getString(arg0,"schecklistname",arg1)));
		objChecklist.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objChecklist.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objChecklist.setNstatus(getShort(arg0,"nstatus",arg1));
		objChecklist.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		
		return objChecklist;
	}

}
