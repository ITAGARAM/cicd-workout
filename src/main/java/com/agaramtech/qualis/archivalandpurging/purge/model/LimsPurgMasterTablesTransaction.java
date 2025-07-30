package com.agaramtech.qualis.archivalandpurging.purge.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'limspurgmasterTablesTransaction' table of the Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 25- June- 2024
 */
@Entity
@Table(name = "lims_purg_master_Tables_Transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LimsPurgMasterTablesTransaction extends CustomizedResultsetRowMapper<LimsPurgMasterTablesTransaction> implements Serializable,RowMapper<LimsPurgMasterTablesTransaction> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "npurgmastertabletransactioncode")
	private int npurgmastertabletransactioncode;
	
	@Column(name = "npurgmastercode")
	private int npurgmastercode;
	
	@Column(name = "npurgmastertablecode")
	private int npurgmastertablecode;
	
	@Column(name = "nsequenceno")
	private int nsequenceno;
	
	@Column(name = "nsequencenocomposite")
	private int nsequencenocomposite;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

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
	public LimsPurgMasterTablesTransaction mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		LimsPurgMasterTablesTransaction objLimsPurgMastertblTrans = new LimsPurgMasterTablesTransaction();
		objLimsPurgMastertblTrans.setNpurgmastertabletransactioncode(getInteger(arg0,"npurgmastertabletransactioncode",arg1));
		objLimsPurgMastertblTrans.setNpurgmastercode(getInteger(arg0,"npurgmastercode",arg1));
		objLimsPurgMastertblTrans.setNpurgmastertablecode(getInteger(arg0,"npurgmastertablecode",arg1));
		objLimsPurgMastertblTrans.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objLimsPurgMastertblTrans.setNsequencenocomposite(getInteger(arg0,"nsequencenocomposite",arg1));
		objLimsPurgMastertblTrans.setJsondata(getJsonObject(arg0,"jsondata",arg1));
					
		objLimsPurgMastertblTrans.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objLimsPurgMastertblTrans.setNstatus(getShort(arg0,"nstatus",arg1));
		objLimsPurgMastertblTrans.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objLimsPurgMastertblTrans.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		
		return objLimsPurgMastertblTrans;
	}
	
}