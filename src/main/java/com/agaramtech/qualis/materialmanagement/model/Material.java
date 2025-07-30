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
@Table(name = "material")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Material extends CustomizedResultsetRowMapper<Material> implements Serializable, RowMapper<Material> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaterialcode")
	private int nmaterialcode;

	@Id
	@Column(name = "nmaterialcatcode")
	private int nmaterialcatcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String smaterialname;

	@Transient
	private transient String sunitname;

	@Transient
	private transient short needsection;

	@Transient
	private transient int nproductcode;

	@Transient
	private transient int nproductcatcode;

	@Transient
	private transient String sproductname;

	@Transient
	private transient String sinventoryid;

	@Transient
	private transient int nmaterialinventorycode;

	@Transient
	private transient String smaterialcatname;

	@Transient
	private transient int nunitcode;

	@Transient
	private transient int needsectionwise;

	@Transient
	private transient int ncount;

	@Transient
	private transient int nmaterialinventtranscode;

	@Transient
	private transient short isnextvalidationperiod;

	@Transient
	private transient int nnextvalidationperiod;

	@Transient
	private transient int nnextvalidation;

	@Override
	public Material mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Material objMaterial = new Material();
		objMaterial.setNmaterialcode(getInteger(arg0, "nmaterialcode", arg1));
		objMaterial.setNmaterialcatcode(getInteger(arg0, "nmaterialcatcode", arg1));
		objMaterial.setNstatus(getShort(arg0, "nstatus", arg1));
		objMaterial.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objMaterial.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objMaterial.setSmaterialname(getString(arg0, "smaterialname", arg1));
		objMaterial.setNeedsection(getShort(arg0, "needsection", arg1));
		objMaterial.setSunitname(getString(arg0, "sunitname", arg1));
		objMaterial.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objMaterial.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objMaterial.setSproductname(getString(arg0, "sproductname", arg1));
		objMaterial.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objMaterial.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objMaterial.setSinventoryid(getString(arg0, "sinventoryid", arg1));
		objMaterial.setNmaterialinventorycode(getInteger(arg0, "nmaterialinventorycode", arg1));
		objMaterial.setSmaterialcatname(getString(arg0, "smaterialcatname", arg1));
		objMaterial.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objMaterial.setNeedsectionwise(getInteger(arg0, "needsectionwise", arg1));
		objMaterial.setNcount(getInteger(arg0, "ncount", arg1));
		objMaterial.setNmaterialinventtranscode(getInteger(arg0, "nmaterialinventtranscode", arg1));
		objMaterial.setIsnextvalidationperiod(getShort(arg0, "isnextvalidationperiod", arg1));
		objMaterial.setNnextvalidationperiod(getInteger(arg0, "nnextvalidationperiod", arg1));
		objMaterial.setNnextvalidation(getInteger(arg0, "nnextvalidation", arg1));
		return objMaterial;
	}
}