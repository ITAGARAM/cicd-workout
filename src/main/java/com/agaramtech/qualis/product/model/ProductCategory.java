package com.agaramtech.qualis.product.model;

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

@Entity
@Table(name = "productcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductCategory extends CustomizedResultsetRowMapper<ProductCategory> implements Serializable, RowMapper<ProductCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nproductcatcode")
	private int nproductcatcode;

	@Column(name = "sproductcatname", length = 100, nullable = false)
	private String sproductcatname;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@ColumnDefault("4")
	@Column(name = "ncategorybasedflow", nullable = false)
	private short ncategorybasedflow = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "ncategorybasedprotocol", nullable = false)
	private short ncategorybasedprotocol = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nschedulerrequired", nullable = false)
	private short nschedulerrequired = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	@Transient
	private transient String scategorybasedflow;

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient int nallottedspeccode;

	@Transient
	private transient String scategorybasedprotocol;

	@Transient
	private transient String sschedulerrequired;

	@Override
	public ProductCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ProductCategory objProductCategory = new ProductCategory();
		objProductCategory.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objProductCategory.setSproductcatname(StringEscapeUtils.unescapeJava(getString(arg0, "sproductcatname", arg1)));
		objProductCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objProductCategory.setNcategorybasedflow(getShort(arg0, "ncategorybasedflow", arg1));
		objProductCategory.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objProductCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProductCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objProductCategory.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objProductCategory.setScategorybasedflow(getString(arg0, "scategorybasedflow", arg1));
		objProductCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProductCategory.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objProductCategory.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objProductCategory.setNcategorybasedprotocol(getShort(arg0, "ncategorybasedprotocol", arg1));
		objProductCategory.setScategorybasedprotocol(getString(arg0, "scategorybasedprotocol", arg1));
		objProductCategory.setNschedulerrequired(getShort(arg0, "nschedulerrequired", arg1));
		objProductCategory.setSschedulerrequired(getString(arg0, "sschedulerrequired", arg1));
		return objProductCategory;
	}

}
