package com.agaramtech.qualis.audittrail.model;

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
 * This entity class is used to map the fields with dynamicaudittable of database.
 */
@Entity 
@Table(name="dynamicaudittable")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DynamicAuditTable extends CustomizedResultsetRowMapper<DynamicAuditTable> implements Serializable,RowMapper<DynamicAuditTable> {
	
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "ndynamicaudittablecode")
	private short ndynamicaudittablecode;
	
	@Column(name = "nformcode", nullable=false)	 
	private short  nformcode;
	
    @Column(name = "stablename", length=50, nullable=false)   
    private String  stablename;
    
    @Column(name = "nissubsampletable", nullable=false) 
    private short  nissubsampletable;  
    
    @Column(name = "dauditdate")	
	private Instant dauditdate;
	
	@Column(name = "stableprimarykey", length=100)   
    private String  stableprimarykey;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")	
	private Map<String,Object> jsondata;	
	
	@ColumnDefault("1")
    @Column(name = "nstatus", nullable=false) 
    private short  nstatus= (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name="nsitecode") 
	private short nsitecode  = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

      
	@Transient
	private transient Map<String, Object> sjsondata;
	
	@Transient
	private transient short index;
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String sdefaultname;
	
	@Transient
	private transient String sfieldname;

	@Override
	public DynamicAuditTable mapRow(ResultSet arg0, int arg1)throws SQLException {
		
		 final DynamicAuditTable objDynamicAuditTable = new DynamicAuditTable();
		
		 objDynamicAuditTable.setNstatus(getShort(arg0,"nstatus",arg1));
		 objDynamicAuditTable.setNdynamicaudittablecode(getShort(arg0,"ndynamicaudittablecode",arg1));
		 objDynamicAuditTable.setStablename(getString(arg0,"stablename",arg1));
		 objDynamicAuditTable.setNformcode(getShort(arg0,"nformcode",arg1));
		 objDynamicAuditTable.setNissubsampletable(getShort(arg0,"nissubsampletable",arg1));
		 objDynamicAuditTable.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		 objDynamicAuditTable.setNsitecode(getShort(arg0,"nsitecode",arg1));
		 objDynamicAuditTable.setDauditdate(getInstant(arg0,"dauditdate",arg1));
		 objDynamicAuditTable.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		 objDynamicAuditTable.setSjsondata(unescapeString(getJsonObject(arg0, "sjsondata", arg1)));
		 objDynamicAuditTable.setIndex(getShort(arg0, "index", arg1));
		 objDynamicAuditTable.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		 objDynamicAuditTable.setStableprimarykey(getString(arg0,"stableprimarykey",arg1));
		 objDynamicAuditTable.setSfieldname(getString(arg0,"sfieldname",arg1));

		return objDynamicAuditTable;
	}	
}
