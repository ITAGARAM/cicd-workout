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
@Table(name="checklistversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ChecklistVersion extends CustomizedResultsetRowMapper<ChecklistVersion>  implements RowMapper<ChecklistVersion>,Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="nchecklistversioncode" ) 
	private int nchecklistversioncode;
	
	@Column(name="nchecklistcode", nullable=false ) 
	private int nchecklistcode;
	
	@Column(name="schecklistversionname",  length = 100 )
	private String schecklistversionname;
	
	@ColumnDefault("'-'")
	@Column(name="schecklistversiondesc",  length = 100 )
	private String schecklistversiondesc="-";
	
	@ColumnDefault("8")
	@Column(name="ntransactionstatus", nullable=false )  
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false ) 
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false)  
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Column(name="dmodifieddate", nullable=false)
	private Instant dmodifieddate;

	
	
	@Transient
	private transient String schecklistname;
	@Transient
	private transient String stransdisplaystatus;
	
	
	@Override
	public ChecklistVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistVersion objChecklistVersion=new ChecklistVersion();
		objChecklistVersion.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objChecklistVersion.setNchecklistcode(getInteger(arg0,"nchecklistcode",arg1));
		objChecklistVersion.setSchecklistversionname(StringEscapeUtils.unescapeJava(getString(arg0,"schecklistversionname",arg1)));
		objChecklistVersion.setSchecklistversiondesc(StringEscapeUtils.unescapeJava(getString(arg0,"schecklistversiondesc",arg1)));
		objChecklistVersion.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objChecklistVersion.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objChecklistVersion.setNstatus(getShort(arg0,"nstatus",arg1));
		objChecklistVersion.setSchecklistname(getString(arg0,"schecklistname",arg1));
		objChecklistVersion.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objChecklistVersion.setDmodifieddate((getInstant(arg0,"dmodifieddate",arg1)));
		
		return objChecklistVersion;
	}
	

}
