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
@Table(name="materialinventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialInventory extends CustomizedResultsetRowMapper<MaterialInventory> implements Serializable, RowMapper<MaterialInventory> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmaterialinventorycode")
	private int nmaterialinventorycode;
	
	@Column(name = "nmaterialcode")
	private int nmaterialcode;
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus;
	
	@Column(name = "nsectioncode")
	private int nsectioncode;
	
	@Transient
	private transient String sinventoryid;
	
	@Transient
	private transient String savailablequatity;
	
	@Transient
	private transient String sunitname;
	
	@Transient
	private transient int nunitcode;
	
	@Transient
	private transient int nmaterialinventtranscode;


	@Override
	public MaterialInventory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MaterialInventory objMaterialInventory = new MaterialInventory();
		
		objMaterialInventory.setNmaterialinventorycode(getInteger(arg0,"nmaterialinventorycode",arg1));
		objMaterialInventory.setNmaterialcode(getInteger(arg0,"nmaterialcode",arg1));
		objMaterialInventory.setNstatus(getShort(arg0,"nstatus",arg1));
		objMaterialInventory.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objMaterialInventory.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objMaterialInventory.setSinventoryid(getString(arg0, "sinventoryid", arg1));
		objMaterialInventory.setSavailablequatity(getString(arg0, "savailablequatity", arg1));
		objMaterialInventory.setSunitname(getString(arg0, "sunitname", arg1));
		objMaterialInventory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objMaterialInventory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objMaterialInventory.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objMaterialInventory.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objMaterialInventory.setNmaterialinventtranscode(getInteger(arg0, "nmaterialinventtranscode", arg1));
		objMaterialInventory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));

		return objMaterialInventory;
	}

}
