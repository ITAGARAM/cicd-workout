package com.agaramtech.qualis.registration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * This class is used to map the fields of 'resultusedmaterial' table of the Database.
 */
@Entity
@Table(name = "resultusedmaterial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultUsedMaterial extends CustomizedResultsetRowMapper<ResultUsedMaterial> implements Serializable, RowMapper<ResultUsedMaterial> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nresultusedmaterialcode")
	private int nresultusedmaterialcode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "nmaterialtypecode", nullable = false)
	private short nmaterialtypecode;

	@Column(name = "nmaterialcategorycode", nullable = false)
	private int nmaterialcategorycode;

	@Column(name = "nmaterialcode", nullable = false)
	private int nmaterialcode;

	@Column(name = "ninventorycode", nullable = false)
	private int ninventorycode;	
	
	@Lob
	@Column(name ="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
		
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient int nsectioncode;
	
	@Override
	public ResultUsedMaterial mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ResultUsedMaterial objResultUsedMaterial = new ResultUsedMaterial();
		
		objResultUsedMaterial.setNresultusedmaterialcode(getInteger(arg0, "nresultusedmaterialcode", arg1));
		objResultUsedMaterial.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objResultUsedMaterial.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objResultUsedMaterial.setNmaterialtypecode(getShort(arg0, "nmaterialtypecode", arg1));
		objResultUsedMaterial.setNmaterialcategorycode(getInteger(arg0, "nmaterialcategorycode", arg1));
		objResultUsedMaterial.setNmaterialcode(getInteger(arg0, "nmaterialcode", arg1));
		objResultUsedMaterial.setNinventorycode(getInteger(arg0, "ninventorycode", arg1));
		objResultUsedMaterial.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objResultUsedMaterial.setSarno(getString(arg0, "sarno", arg1));
		objResultUsedMaterial.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objResultUsedMaterial.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objResultUsedMaterial.setNstatus(getShort(arg0, "nstatus", arg1));

		return objResultUsedMaterial;
	}

}
