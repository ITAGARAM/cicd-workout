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
 * This class is used to map the fields of 'suppliermatrix' table of the
 * Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 10- Jun- 2020
 */

@Entity
@Table(name = "suppliermatrix")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupplierMatrix extends CustomizedResultsetRowMapper<SupplierMatrix> implements Serializable, RowMapper<SupplierMatrix> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsuppliermatrixcode")
	private int nsuppliermatrixcode;

	@Column(name = "nsuppliercode", nullable = false)
	private int nsuppliercode;

	@Column(name = "ncategorycode", nullable = false)
	private int ncategorycode;

	@Column(name = "ntypecode", nullable = false)
	private short ntypecode;

	@Column(name = "sremarks", length = 255)
	private String sremarks="";

	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("1")
	private short ntransactionstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String ssuppliercatname;
	@Transient
	private transient String smaterialcatname;
	@Transient
	private transient int nsuppliercatcode;
	@Transient
	private transient int nmaterialcatcode;
	@Transient
	private transient int napprovalstatus;

	


	@Override
	public SupplierMatrix mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SupplierMatrix supplierMatrix = new SupplierMatrix();

		supplierMatrix.setNstatus(getShort(arg0,"nstatus",arg1));
		supplierMatrix.setNsuppliermatrixcode(getInteger(arg0,"nsuppliermatrixcode",arg1));
		supplierMatrix.setNsuppliercode(getInteger(arg0,"nsuppliercode",arg1));
		supplierMatrix.setNcategorycode(getInteger(arg0,"ncategorycode",arg1));
		supplierMatrix.setNtypecode(getShort(arg0,"ntypecode",arg1));
		supplierMatrix.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0,"sremarks",arg1)));
		supplierMatrix.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		supplierMatrix.setSsuppliercatname(getString(arg0,"ssuppliercatname",arg1));
		supplierMatrix.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		supplierMatrix.setSmaterialcatname(getString(arg0,"smaterialcatname",arg1));
		supplierMatrix.setNsuppliercatcode(getInteger(arg0,"nsuppliercatcode",arg1));
		supplierMatrix.setNmaterialcatcode(getInteger(arg0,"nmaterialcatcode",arg1));
		supplierMatrix.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		supplierMatrix.setNsitecode(getShort(arg0,"nsitecode",arg1));
		supplierMatrix.setNapprovalstatus(getInteger(arg0,"napprovalstatus",arg1));
		return supplierMatrix;

	}


}
