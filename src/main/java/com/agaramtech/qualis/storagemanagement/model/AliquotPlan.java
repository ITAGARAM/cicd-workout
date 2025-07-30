package com.agaramtech.qualis.storagemanagement.model;

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
@Table(name = "aliquotplan")
//@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AliquotPlan extends CustomizedResultsetRowMapper<AliquotPlan> implements Serializable, RowMapper<AliquotPlan> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "naliquotplancode")
	private int naliquotplancode;
	
	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;
	
	@Column(name = "nsamplecollectiontypecode")
	private int nsamplecollectiontypecode;

	@Column(name = "ncollectiontubetypecode")
	private int ncollectiontubetypecode;
	
	@Column(name = "npatientcatcode")
	private int npatientcatcode;
	
	@Column(name = "nvisitnumbercode")
	private int nvisitnumbercode;
	
	@Column(name = "saliquotno", length = 5)
	private String saliquotno;
	
	@Column(name = "squantity", length = 10)
	private String squantity; 	
	
	@Column(name = "nunitcode")
	private int nunitcode;
	
	@Column(name = "nsampledonorcode")
	private int nsampledonorcode;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false) 
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false) 
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private String sprojecttypename;
	@Transient
	private int nprojectcode;
	@Transient
	private int  nproductsamplecode;
	@Transient
	private String sproductname;
	@Transient
	private int ncollectiontubecode;
	@Transient
	private String stubename;
	@Transient
	private int npatientcode ;
	@Transient
	private String spatientcatname;
	@Transient
	private String svisitnumber;
	@Transient
	private String sunitname;
	@Transient
	private int nunitbasiccode;
	@Transient
	private int nvisitcode;
	@Transient
	private String ssampledonor; 
	
	
	@Override   
	public AliquotPlan mapRow(ResultSet arg0, int arg1) throws SQLException {
	
		final AliquotPlan objAliquotPlan=new AliquotPlan();
		
		objAliquotPlan.setNaliquotplancode(getInteger(arg0, "naliquotplancode", arg1));
		objAliquotPlan.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objAliquotPlan.setNsamplecollectiontypecode(getInteger(arg0, "nsamplecollectiontypecode", arg1));
		objAliquotPlan.setNcollectiontubetypecode(getInteger(arg0, "ncollectiontubetypecode", arg1));
		objAliquotPlan.setNpatientcatcode(getInteger(arg0,"npatientcatcode",arg1));
		objAliquotPlan.setNvisitnumbercode(getInteger(arg0, "nvisitnumbercode", arg1)); 
		objAliquotPlan.setSaliquotno(StringEscapeUtils.unescapeJava(getString(arg0, "saliquotno", arg1)));
		objAliquotPlan.setSquantity(StringEscapeUtils.unescapeJava(getString(arg0, "squantity", arg1)));  
		objAliquotPlan.setNunitcode(getInteger(arg0, "nunitcode", arg1)); 
		objAliquotPlan.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objAliquotPlan.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objAliquotPlan.setNstatus(getShort(arg0,"nstatus",arg1));
		objAliquotPlan.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objAliquotPlan.setSprojecttypename(StringEscapeUtils.unescapeJava(getString(arg0, "sprojecttypename", arg1)));
		objAliquotPlan.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objAliquotPlan.setStubename(StringEscapeUtils.unescapeJava(getString(arg0, "stubename", arg1)));
		objAliquotPlan.setNprojectcode(getInteger(arg0, "nprojectcode", arg1));
		objAliquotPlan.setNproductsamplecode(getInteger(arg0, "nproductsamplecode", arg1));
		objAliquotPlan.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductname", arg1)));
		objAliquotPlan.setNcollectiontubecode(getInteger(arg0, "ncollectiontubecode", arg1));
		objAliquotPlan.setNpatientcode(getInteger(arg0, "npatientcode", arg1));
		objAliquotPlan.setSpatientcatname(StringEscapeUtils.unescapeJava(getString(arg0, "spatientcatname", arg1)));
		objAliquotPlan.setSvisitnumber(StringEscapeUtils.unescapeJava(getString(arg0, "svisitnumber", arg1)));
		objAliquotPlan.setSunitname(StringEscapeUtils.unescapeJava(getString(arg0, "sunitname", arg1)));
		objAliquotPlan.setNunitbasiccode(getInteger(arg0, "nunitbasiccode", arg1));
		objAliquotPlan.setNvisitcode(getInteger(arg0, "nvisitcode", arg1));
		objAliquotPlan.setNsampledonorcode(getInteger(arg0, "nsampledonorcode", arg1));
		objAliquotPlan.setSsampledonor(StringEscapeUtils.unescapeJava(getString(arg0, "ssampledonor", arg1)));
		
		return objAliquotPlan;
	}

}
