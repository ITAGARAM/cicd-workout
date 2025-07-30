package com.agaramtech.qualis.archivalandpurging.purge.model;

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
 * This class is used to map the fields of 'limspurgmasterTables' table of the Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 25- June- 2024
 */
@Entity
@Table(name = "lims_purg_master_Tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LimsPurgMasterTables extends CustomizedResultsetRowMapper<LimsPurgMasterTables> implements Serializable,RowMapper<LimsPurgMasterTables> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "npurgmastertablecode")
	private int npurgmastertablecode;

	@Column(name = "stablename", length = 100)
	private String stablename;
	
	@Column(name = "stableprimarykey", length = 100)
	private String stableprimarykey;
	
	@Column(name = "iscompositeprimarykeyneed")
	private Boolean iscompositeprimarykeyneed;
	
	@Column(name = "stablecompositeprimarykey", length = 100)
	private String stablecompositeprimarykey;
	
	@Column(name = "stabledatecolumn", length = 100)
	private String stabledatecolumn;
	
	@Column(name = "nparentcode", nullable = false)
	private short nparentcode;
	
	@Column(name = "nsortordercode", nullable = false)
	private short nsortordercode;
	
	@Column(name = "isqueryneed")
	private Boolean isqueryneed;
	
	@Column(name = "isquerymappingneed")
	private Boolean isquerymappingneed;
	
	@Column(name = "squerymappingcolumn", length = 100)
	private String squerymappingcolumn;
	
	@Column(name = "squery", columnDefinition = "text")	
	private String squery;
	
	@Column(name = "srestorequery", columnDefinition = "text")	
	private String srestorequery;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private String smodifieddate;
	
	
	@Override
	public LimsPurgMasterTables mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		LimsPurgMasterTables objLimsPurgMastertbl = new LimsPurgMasterTables();
		objLimsPurgMastertbl.setNpurgmastertablecode(getInteger(arg0,"npurgmastertablecode",arg1));
		objLimsPurgMastertbl.setStablename(getString(arg0,"stablename",arg1));
		objLimsPurgMastertbl.setStableprimarykey(getString(arg0,"stableprimarykey",arg1));
		objLimsPurgMastertbl.setIscompositeprimarykeyneed(getBoolean(arg0,"iscompositeprimarykeyneed", arg1));
		objLimsPurgMastertbl.setStablecompositeprimarykey(getString(arg0,"stablecompositeprimarykey",arg1));
		objLimsPurgMastertbl.setStabledatecolumn(getString(arg0,"stabledatecolumn",arg1));
		objLimsPurgMastertbl.setNparentcode(getShort(arg0,"nparentcode",arg1));
		objLimsPurgMastertbl.setNsortordercode(getShort(arg0,"nsortordercode",arg1));
		objLimsPurgMastertbl.setIsqueryneed(getBoolean(arg0,"isqueryneed", arg1));
		objLimsPurgMastertbl.setIsquerymappingneed(getBoolean(arg0,"isquerymappingneed", arg1));
		objLimsPurgMastertbl.setSquerymappingcolumn(getString(arg0,"squerymappingcolumn",arg1));
		objLimsPurgMastertbl.setSquery(getString(arg0,"squery",arg1));
		objLimsPurgMastertbl.setSrestorequery(getString(arg0,"srestorequery",arg1));
			
		objLimsPurgMastertbl.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objLimsPurgMastertbl.setNstatus(getShort(arg0,"nstatus",arg1));
		objLimsPurgMastertbl.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objLimsPurgMastertbl.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		
		return objLimsPurgMastertbl;
	}
	
}
