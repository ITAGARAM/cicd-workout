package com.agaramtech.qualis.materialmanagement.model;

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
 * This class is used to map the fields of 'materialcategory' table of the
 * Database.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 04- 04- 2025
 */
@Entity
@Table(name = "materialcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialCategory extends CustomizedResultsetRowMapper<MaterialCategory>
		implements Serializable, RowMapper<MaterialCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaterialcatcode")
	private int nmaterialcatcode;

	@Column(name = "nmaterialtypecode", nullable = false)
	private short nmaterialtypecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@Column(name = "needsectionwise")
	private short needSectionwise;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@ColumnDefault("-1")
	@Column(name = "nbarcode", nullable = false)
	private int nbarcode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@ColumnDefault("4")
	@Column(name = "ncategorybasedflow", nullable = false)
	private short ncategorybasedflow =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	

	@ColumnDefault("1")
	@Column(name = "nactivestatus", nullable = false)
	private short nactivestatus;

	@Column(name = "smaterialcatname", length = 100, nullable = false)
	private String smaterialcatname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String sneedSectionwise;
	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String sbarcodename;
	@Transient
	private transient int nsuppliercode;
	@Transient
	private transient int nsuppliermatrixcode;
	@Transient
	private transient String scategorybasedflow;
	@Transient
	private transient String smaterialtypename;
	@Transient
	private transient boolean needSectionwisedisabled;
	@Transient
	private transient int nproductcatcode;
	@Transient
	private transient String sproductcatname;


	@Override
	public MaterialCategory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final MaterialCategory materialcategory = new MaterialCategory();
		materialcategory.setNeedSectionwisedisabled(getBoolean(arg0, "needSectionwisedisabled", arg1));
		materialcategory.setNmaterialcatcode(getInteger(arg0, "nmaterialcatcode", arg1));
		materialcategory.setNmaterialtypecode(getShort(arg0, "nmaterialtypecode", arg1));
		materialcategory.setNeedSectionwise(getShort(arg0, "needSectionwise", arg1));
		materialcategory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		materialcategory.setNbarcode(getInteger(arg0, "nbarcode", arg1));
		materialcategory.setNcategorybasedflow(getShort(arg0, "ncategorybasedflow", arg1));
		materialcategory.setNactivestatus(getShort(arg0, "nactivestatus", arg1));
		materialcategory.setSmaterialcatname(StringEscapeUtils.unescapeJava(getString(arg0, "smaterialcatname", arg1)));
		materialcategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		materialcategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		materialcategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		materialcategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		materialcategory.setNstatus(getShort(arg0, "nstatus", arg1));
		materialcategory.setNsuppliercode(getInteger(arg0, "nsuppliercode", arg1));
		materialcategory.setNsuppliermatrixcode(getInteger(arg0, "nsuppliermatrixcode", arg1));
		materialcategory.setScategorybasedflow(getString(arg0, "scategorybasedflow", arg1));
		materialcategory.setSmaterialtypename(getString(arg0, "smaterialtypename", arg1));
		materialcategory.setSneedSectionwise(getString(arg0, "sneedSectionwise", arg1));
		materialcategory.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		materialcategory.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		materialcategory.setSbarcodename(getString(arg0, "sbarcodename", arg1));
		materialcategory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return materialcategory;

	}

}
