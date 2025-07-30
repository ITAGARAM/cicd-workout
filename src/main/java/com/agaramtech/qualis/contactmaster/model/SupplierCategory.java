package com.agaramtech.qualis.contactmaster.model;

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
 * This class is used to map the fields of 'suppliercategory' table of the
 * Database.
 * 
 * @author ATE184
 * @version 9.0.0.1
 * @since 30- Jun- 2020
 */

@Entity
@Table(name = "suppliercategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupplierCategory extends CustomizedResultsetRowMapper<SupplierCategory> implements Serializable, RowMapper<SupplierCategory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsuppliercatcode")
	private int nsuppliercatcode;

	@Column(name = "ssuppliercatname", length = 100, nullable = false)
	private String ssuppliercatname;

	@Column(name = "sdescription", length = 255)
	private String sdescription="";

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient int nsuppliercode;
	@Transient
	private transient int nsuppliermatrixcode;

	@Override
	public SupplierCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SupplierCategory suppliercategory = new SupplierCategory();

		suppliercategory.setNstatus(getShort(arg0, "nstatus", arg1));
		suppliercategory.setNsuppliercatcode(getInteger(arg0, "nsuppliercatcode", arg1));
		suppliercategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		suppliercategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		suppliercategory.setSsuppliercatname(StringEscapeUtils.unescapeJava(getString(arg0, "ssuppliercatname", arg1)));
		suppliercategory.setNsuppliercode(getInteger(arg0, "nsuppliercode", arg1));
		suppliercategory.setNsuppliermatrixcode(getInteger(arg0, "nsuppliermatrixcode", arg1));
		suppliercategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return suppliercategory;
	}

	
}
