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
@Table(name = "materialinventorytype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialInventoryType extends CustomizedResultsetRowMapper<MaterialInventoryType>
		implements Serializable, RowMapper<MaterialInventoryType> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ninventorytypecode")
	private short ninventorytypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ndefaultstatus")
	private short ndefaultstatus;
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
    //ALPD-4129 For MAterial Accounting
	@Transient
	private transient String sinventorytypename;
	
	@Transient
	private transient double navailableqty;
	
	@Transient
	private transient double navailableuraniumqty;  
	
	@Transient
	private transient double suraniumconversionfactor;
	
	
	@Override
	public MaterialInventoryType mapRow(ResultSet arg0, int arg1) throws SQLException {
	
		final MaterialInventoryType objMaterialInventoryType = new MaterialInventoryType();
		
		objMaterialInventoryType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objMaterialInventoryType.setNinventorytypecode(getShort(arg0, "ninventorytypecode", arg1));
		objMaterialInventoryType.setNstatus(getShort(arg0, "nstatus", arg1));
		objMaterialInventoryType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objMaterialInventoryType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objMaterialInventoryType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objMaterialInventoryType.setSinventorytypename(getString(arg0, "sinventorytypename", arg1));
		objMaterialInventoryType.setNavailableqty(getDouble(arg0,"navailableqty",arg1));
		objMaterialInventoryType.setNavailableuraniumqty(getDouble(arg0,"navailableuraniumqty",arg1));
		objMaterialInventoryType.setSuraniumconversionfactor(getDouble(arg0,"suraniumconversionfactor",arg1));
		
		return objMaterialInventoryType;
	}

}
