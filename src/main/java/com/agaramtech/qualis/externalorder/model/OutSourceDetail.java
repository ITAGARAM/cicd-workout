package com.agaramtech.qualis.externalorder.model;

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
 * This class is used to map the fields of 'outsourcedetail' table of the Database.
 */
@Entity
@Table(name = "outsourcedetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OutSourceDetail  extends CustomizedResultsetRowMapper<OutSourceDetail> implements Serializable,RowMapper<OutSourceDetail>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "noutsourcedetailcode", nullable=false)  
	private int noutsourcedetailcode;
	
	@Column(name = "npreregno", nullable=false) 
	private int npreregno;	
	
	@Column(name = "ntransactionsamplecode",  nullable=false) 
	private int ntransactionsamplecode;	
	
	@Column(name = "ntransactiontestcode", nullable=false) 
	private int ntransactiontestcode;
	
	@Column(name = "ssampleid", length = 255, nullable = false) 
	private String ssampleid;
	
	@Column(name = "sremarks", length = 255) 
	private String sremarks;
	
	@Column(name = "sshipmenttracking", length = 255) 
	private String sshipmenttracking;
	
	@Column(name = "bflag") 
	private boolean bflag;
	
	@Column(name = "doutsourcedate") 
	private Instant doutsourcedate;
	
	@Column(name = "dmodifieddate")	 
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Override
	public OutSourceDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final OutSourceDetail objOutSourceDetail=new OutSourceDetail();
		objOutSourceDetail.setNoutsourcedetailcode(getInteger(arg0,"noutsourcedetailcode",arg1));
		objOutSourceDetail.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objOutSourceDetail.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objOutSourceDetail.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objOutSourceDetail.setSsampleid(StringEscapeUtils.unescapeJava(getString(arg0,"ssampleid",arg1)));
		objOutSourceDetail.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0,"sremarks",arg1)));
		objOutSourceDetail.setBflag(getBoolean(arg0,"bflag",arg1));
		objOutSourceDetail.setSshipmenttracking(StringEscapeUtils.unescapeJava(getString(arg0,"sshipmenttracking",arg1)));
		objOutSourceDetail.setDoutsourcedate(getInstant(arg0,"doutsourcedate",arg1));		
		objOutSourceDetail.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objOutSourceDetail.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objOutSourceDetail.setNstatus(getShort(arg0,"nstatus",arg1));
	
		return objOutSourceDetail;
	}
	
	

}
