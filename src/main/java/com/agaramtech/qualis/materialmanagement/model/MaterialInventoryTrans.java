package com.agaramtech.qualis.materialmanagement.model;


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

@Entity
@Table(name = "materialinventorytransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialInventoryTrans extends CustomizedResultsetRowMapper<MaterialInventoryTrans>
		implements Serializable, RowMapper<MaterialInventoryTrans> {


	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmaterialinventtranscode")
	private int nmaterialinventtranscode;
	
	@Column(name = "ntransactiontype")
	private short ntransactiontype;
	
	@Column(name = "ninventorytranscode")
	private short ninventorytranscode;
	
	@Column(name = "nsectioncode")
	private int nsectioncode;
	
	@Column(name = "nmaterialinventorycode")
	private int nmaterialinventorycode;
	
	@Column(name = "nresultusedmaterialcode")
	private int nresultusedmaterialcode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Column(name="dtransactiondate")
	private Instant dtransactiondate;
	
	@Column(name="noffsetdtransactiondate") 
	private int noffsetdtransactiondate;	
	
	@Column(name="ntztransactiondate")  
	private short ntztransactiondate;
	
	
	@Transient
	private transient String sinventorytypename;
	
	@Transient
	private transient String sqtyreceived;
	
	@Transient
	private transient String sqtyissued;
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String scomments;
	
	@Transient
	private transient String suraniumissued;
	
	@Transient
	private transient String suraniumreceived;
	
	@Transient
	private transient String sproductname;
	
	@Override
	public MaterialInventoryTrans mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MaterialInventoryTrans objMaterialInventoryTrans = new MaterialInventoryTrans();

		objMaterialInventoryTrans.setNmaterialinventtranscode(getInteger(arg0, "nmaterialinventtranscode", arg1));
		objMaterialInventoryTrans.setNinventorytranscode(getShort(arg0, "ninventorytranscode", arg1));
		objMaterialInventoryTrans.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objMaterialInventoryTrans.setNtransactiontype(getShort(arg0, "ntransactiontype", arg1));
		objMaterialInventoryTrans.setSqtyissued(getString(arg0, "sqtyissued", arg1));
		objMaterialInventoryTrans.setSqtyreceived(getString(arg0, "sqtyreceived", arg1));
		objMaterialInventoryTrans.setNmaterialinventorycode(getInteger(arg0, "nmaterialinventorycode", arg1));
		objMaterialInventoryTrans.setNresultusedmaterialcode(getInteger(arg0, "nresultusedmaterialcode", arg1));
		objMaterialInventoryTrans.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objMaterialInventoryTrans.setNstatus(getShort(arg0, "nstatus", arg1));
		objMaterialInventoryTrans.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objMaterialInventoryTrans.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objMaterialInventoryTrans.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objMaterialInventoryTrans.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objMaterialInventoryTrans.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objMaterialInventoryTrans.setSinventorytypename(getString(arg0, "sinventorytypename", arg1));
		objMaterialInventoryTrans.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objMaterialInventoryTrans.setScomments(getString(arg0, "scomments", arg1));
		objMaterialInventoryTrans.setSuraniumissued(getString(arg0, "suraniumissued", arg1));
		objMaterialInventoryTrans.setSuraniumreceived(getString(arg0, "suraniumreceived", arg1));
		objMaterialInventoryTrans.setSproductname(getString(arg0, "sproductname", arg1));
		
		return objMaterialInventoryTrans;
		
	}

}
