package com.agaramtech.qualis.configuration.model;

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

/**
 * This class is used to map the fields of 'approvalconfigautoapproval' table of the Database.
 */
@Entity
@Table(name = "approvalconfigautoapproval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class  ApprovalConfigAutoapproval extends CustomizedResultsetRowMapper<ApprovalConfigAutoapproval> implements Serializable,RowMapper<ApprovalConfigAutoapproval>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nautoapprovalcode" )  
	private int nautoapprovalcode;
	
	@Column(name="napprovalconfigcode",nullable=false )  
	private short napprovalconfigcode;
	
	@Column(name="napprovalconfigversioncode",nullable=false )  
	private int napprovalconfigversioncode;
	
	@Column(name="sversionname",length=100,nullable=false) 
	private String sversionname;
	
	@ColumnDefault("4")
	@Column(name="nneedautocomplete",nullable=false )  
	private short nneedautocomplete=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name="nneedautoapproval",nullable=false )  
	private short nneedautoapproval=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("50")
	@Column(name="nautodescisionstatus",nullable=false )  
	private short nautodescisionstatus=(short)Enumeration.TransactionStatus.PASS.gettransactionstatus();
	
	@Column(name="nautoapprovalstatus",nullable=false )  
	private short nautoapprovalstatus;
	
	@ColumnDefault("4")
	@Column(name="nautoallot",nullable=false )  
	private short nautoallot=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	
	
	@ColumnDefault("4")
	@Column(name="nneedjoballocation",nullable=false )  
	private short nneedjoballocation=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	

	@ColumnDefault("4")
	@Column(name="nneedautoinnerband",nullable=false )  
	private short nneedautoinnerband=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name="nneedautoouterband",nullable=false )  
	private short nneedautoouterband=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Transient
	private transient String sneedautoapproval;
	
	@Transient
	private transient String sautodescisionstatus;
	
	@Transient
	private transient String sautoapprovalstatus;
	
	@Transient
	private transient String sneedjoballocation;
	
	@Transient
	private transient String sneedautocomplete;
	
	@Transient
	private transient String sautoallot;
	
	@Transient
	private transient short ntransactionstatus;
	
	@Transient
	private transient String sversionstatus;
	
	@Transient
	private transient String sapproveconfversiondesc;
	
	@Transient
	private transient int napproveconfversioncode;
	
	@Transient
	private transient int npreregno;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String stranstablename;

	@Override
	public ApprovalConfigAutoapproval mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		 final ApprovalConfigAutoapproval objApprovalConfigAutoapproval = new ApprovalConfigAutoapproval();
		 
		 objApprovalConfigAutoapproval.setNautodescisionstatus(getShort(arg0, "nautodescisionstatus", arg1));
		 objApprovalConfigAutoapproval.setNautoapprovalstatus(getShort(arg0, "nautoapprovalstatus", arg1));
		 objApprovalConfigAutoapproval.setNstatus(getShort(arg0, "nstatus", arg1));
		 objApprovalConfigAutoapproval.setNautoapprovalcode(getInteger(arg0, "nautoapprovalcode", arg1));
		 objApprovalConfigAutoapproval.setNapprovalconfigversioncode(getInteger(arg0, "napprovalconfigversioncode", arg1));
		 objApprovalConfigAutoapproval.setNneedautocomplete(getShort(arg0, "nneedautocomplete", arg1));
		 objApprovalConfigAutoapproval.setNneedautoapproval(getShort(arg0, "nneedautoapproval", arg1));
		 objApprovalConfigAutoapproval.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		 objApprovalConfigAutoapproval.setNneedjoballocation(getShort(arg0, "nneedjoballocation", arg1));
		 objApprovalConfigAutoapproval.setNautoallot(getShort(arg0, "nautoallot", arg1));
		 objApprovalConfigAutoapproval.setStranstablename(getString(arg0, "stranstablename", arg1));
		 objApprovalConfigAutoapproval.setSversionname(StringEscapeUtils.unescapeJava(getString(arg0, "sversionname", arg1)));
		 objApprovalConfigAutoapproval.setSversionstatus(getString(arg0, "sversionstatus", arg1));
		 objApprovalConfigAutoapproval.setSapproveconfversiondesc(getString(arg0, "sapproveconfversiondesc", arg1));
		 objApprovalConfigAutoapproval.setSneedautocomplete(getString(arg0, "sneedautocomplete", arg1));
		 objApprovalConfigAutoapproval.setSneedautoapproval(getString(arg0, "sneedautoapproval", arg1));
		 objApprovalConfigAutoapproval.setSneedjoballocation(getString(arg0, "sneedjoballocation", arg1));
		 objApprovalConfigAutoapproval.setSautoallot(getString(arg0, "sautoallot", arg1));
		 objApprovalConfigAutoapproval.setSautoapprovalstatus(getString(arg0, "sautoapprovalstatus", arg1));
		 objApprovalConfigAutoapproval.setSautodescisionstatus(getString(arg0, "sautodescisionstatus", arg1));
		 objApprovalConfigAutoapproval.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		 objApprovalConfigAutoapproval.setNapproveconfversioncode(getInteger(arg0, "napproveconfversioncode", arg1));
		 objApprovalConfigAutoapproval.setNpreregno(getInteger(arg0, "npreregno", arg1));
		 objApprovalConfigAutoapproval.setSarno(getString(arg0, "sarno", arg1));
		 objApprovalConfigAutoapproval.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		 objApprovalConfigAutoapproval.setNsitecode(getShort(arg0, "nsitecode", arg1));
		 objApprovalConfigAutoapproval.setNneedautoouterband(getShort(arg0, "nneedautoouterband", arg1));
		 objApprovalConfigAutoapproval.setNneedautoinnerband(getShort(arg0, "nneedautoinnerband", arg1));

		return objApprovalConfigAutoapproval;
	}

}


