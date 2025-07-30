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

/**
 * This class is used to map the fields of 'approvalconfigrole' table of the Database.
 */
@Entity
@Table(name = "approvalconfigrole")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalConfigRole extends CustomizedResultsetRowMapper<ApprovalConfigRole> implements Serializable,RowMapper<ApprovalConfigRole>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "napprovalconfigrolecode")
	private int napprovalconfigrolecode;
	
	@Column(name = "napprovalconfigcode",nullable=false)
	private short  napprovalconfigcode;
	
	@Column(name = "ntreeversiontempcode",nullable=false)
	private int ntreeversiontempcode;
	
	@Column(name = "nuserrolecode",nullable=false)
	private int nuserrolecode;
	
	@ColumnDefault("-1")
	@Column(name = "nchecklistversioncode",nullable=false)
	private int nchecklistversioncode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "npartialapprovalneed",nullable=false)
	private short npartialapprovalneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nsectionwiseapprovalneed",nullable=false)
	private short nsectionwiseapprovalneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nrecomretestneed",nullable=false)
	private short  nrecomretestneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nrecomrecalcneed",nullable=false)
	private short nrecomrecalcneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nretestneed",nullable=false)
	private short  nretestneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nrecalcneed",nullable=false)
	private short  nrecalcneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "nlevelno",nullable=false)
	private short nlevelno;
	
	@ColumnDefault("4")
	@Column(name = "napprovalstatuscode",nullable=false)
	private short  napprovalstatuscode=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "napproveconfversioncode",nullable=false)
	private int napproveconfversioncode;
	
	@ColumnDefault("4")
	@Column(name = "nautoapproval",nullable=false)
	private short  nautoapproval=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "nautoapprovalstatuscode",nullable=false)
	private short nautoapprovalstatuscode;
	
	@Column(name = "nautodescisionstatuscode",nullable=false)
	private short nautodescisionstatuscode;
	
	@Column(name = "ncorrectionneed")
	private short ncorrectionneed;
	
	@Column(name = "nesignneed")
	private short nesignneed;
	
	@Column(name = "ntransactionstatus")
	private short  ntransactionstatus;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
		
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		
	@Transient
	private transient String stransdisplaystatus; 
	
	@Transient
	private transient String sesignneed;
	
	@Transient
	private transient String scorrectionneed;
	
	@Transient
	private transient short nsampletypecode;
	
	@Transient
	private transient int nmaterialinventoryid;
		
	@Transient
	private transient int ntoplevelautoapprove;
		
	@Transient
	private transient int nrolewiseautoapprove;
		
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String schecklistname;
		
	@Transient
	private transient String schecklistversionname;
		
	@Transient
	private transient String ssectionwiseapprovalneed;
		
	@Transient
	private transient String spartialapprovalneed;	
		
	@Transient
	private transient String srecomretestneed;
		
	@Transient
	private transient String srecalcneed;
			
	@Transient
	private transient String srecomrecalcneed;
	
	@Transient
	private transient String sretestneed;
	
	@Transient
	private transient String sapprovalstatus;
	
	@Transient
	private transient String sneedautoapproval;
	
	@Transient
	private transient String sautoapprovalstatus;
		
	@Transient
	private transient String sautodescisionstatuscode;
		
	@Transient
	private transient int nautoapprovalstatus;
		
	@Transient
	private transient int ntopleveldescisionstatuscode;
		
	@Transient
	private transient int nneedautocomplete;
		
	@Transient
	private transient String suserrolenamelevel;
	
	@Transient
	private transient short nneedautoinnerband;
		
	@Transient
	private transient short nneedautoouterband;

	@Override
	public ApprovalConfigRole mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		 
		final ApprovalConfigRole objApprovalConfigRole = new ApprovalConfigRole();
		 objApprovalConfigRole.setNautoapproval(getShort(arg0,"nautoapproval",arg1));
		 objApprovalConfigRole.setNretestneed(getShort(arg0,"nretestneed",arg1));
		 objApprovalConfigRole.setNtreeversiontempcode(getInteger(arg0,"ntreeversiontempcode",arg1));
		 objApprovalConfigRole.setNautodescisionstatuscode(getShort(arg0,"nautodescisionstatuscode",arg1));
		 objApprovalConfigRole.setNapprovalstatuscode(getShort(arg0,"napprovalstatuscode",arg1));
		 objApprovalConfigRole.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		 objApprovalConfigRole.setNstatus(getShort(arg0,"nstatus",arg1));
		 objApprovalConfigRole.setNapproveconfversioncode(getShort(arg0,"napproveconfversioncode",arg1));
		 objApprovalConfigRole.setNrecomrecalcneed(getShort(arg0,"nrecomrecalcneed",arg1));
		 objApprovalConfigRole.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		 objApprovalConfigRole.setNlevelno(getShort(arg0,"nlevelno",arg1));
		 objApprovalConfigRole.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		 objApprovalConfigRole.setNautoapprovalstatuscode(getShort(arg0,"nautoapprovalstatuscode",arg1));
		 objApprovalConfigRole.setNpartialapprovalneed(getShort(arg0,"npartialapprovalneed",arg1));
		 objApprovalConfigRole.setNsectionwiseapprovalneed(getShort(arg0,"nsectionwiseapprovalneed",arg1));
		 objApprovalConfigRole.setSsectionwiseapprovalneed(getString(arg0,"ssectionwiseapprovalneed",arg1));
		 objApprovalConfigRole.setNrecalcneed(getShort(arg0,"nrecalcneed",arg1));
		 objApprovalConfigRole.setSapprovalstatus(getString(arg0,"sapprovalstatus",arg1));
		 objApprovalConfigRole.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		 objApprovalConfigRole.setNapprovalconfigrolecode(getInteger(arg0,"napprovalconfigrolecode",arg1));
		 objApprovalConfigRole.setNrecomretestneed(getShort(arg0,"nrecomretestneed",arg1));
		 objApprovalConfigRole.setNcorrectionneed(getShort(arg0,"ncorrectionneed",arg1));
		 objApprovalConfigRole.setSuserrolename(getString(arg0,"suserrolename",arg1));
		 objApprovalConfigRole.setNsampletypecode(getShort(arg0,"nsampletypecode",arg1));
		 objApprovalConfigRole.setNmaterialinventoryid(getShort(arg0,"nmaterialinventoryid",arg1));
		 objApprovalConfigRole.setNesignneed(getShort(arg0,"nesignneed",arg1));
		 objApprovalConfigRole.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		 objApprovalConfigRole.setSchecklistname(getString(arg0,"schecklistname",arg1));
		 objApprovalConfigRole.setSchecklistversionname(getString(arg0,"schecklistversionname",arg1));
		 objApprovalConfigRole.setNsitecode(getShort(arg0,"nsitecode",arg1));
		 objApprovalConfigRole.setSneedautoapproval(getString(arg0,"sneedautoapproval",arg1));
		 objApprovalConfigRole.setSesignneed(getString(arg0,"sesignneed",arg1));
		 objApprovalConfigRole.setScorrectionneed(getString(arg0,"scorrectionneed",arg1));
		 objApprovalConfigRole.setSpartialapprovalneed(getString(arg0,"spartialapprovalneed",arg1));
		 objApprovalConfigRole.setSautoapprovalstatus(getString(arg0,"sautoapprovalstatus",arg1));
		 objApprovalConfigRole.setSautodescisionstatuscode(getString(arg0,"sautodescisionstatuscode",arg1));
		 objApprovalConfigRole.setNtoplevelautoapprove(getInteger(arg0,"ntoplevelautoapprove",arg1));
		 objApprovalConfigRole.setNrolewiseautoapprove(getInteger(arg0,"nrolewiseautoapprove",arg1));
		 objApprovalConfigRole.setNautoapprovalstatus(getInteger(arg0,"nautoapprovalstatus",arg1));
		 objApprovalConfigRole.setNtopleveldescisionstatuscode(getInteger(arg0,"ntopleveldescisionstatuscode",arg1));
		 objApprovalConfigRole.setNneedautocomplete(getInteger(arg0,"nneedautocomplete",arg1));
		 objApprovalConfigRole.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		 objApprovalConfigRole.setSuserrolenamelevel(getString(arg0,"suserrolenamelevel",arg1));
		 objApprovalConfigRole.setNneedautoouterband(getShort(arg0, "nneedautoouterband", arg1));
		 objApprovalConfigRole.setNneedautoinnerband(getShort(arg0, "nneedautoinnerband", arg1));	
		 
		return objApprovalConfigRole;
	}	 
	
}
