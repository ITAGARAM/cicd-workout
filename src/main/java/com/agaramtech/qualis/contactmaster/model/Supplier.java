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
 * This class is used to map the fields of 'supplier' table of the Database.
 * 
 * @author ATE090
 * @version 9.0.0.1
 * @since 30- Jun- 2020
 */

@Entity
@Table(name = "supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Supplier extends CustomizedResultsetRowMapper<Supplier> implements Serializable,RowMapper<Supplier> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsuppliercode")
	private int nsuppliercode;

	@Column(name = "ncountrycode", nullable = false)
	private int ncountrycode;

	@Column(name = "ssuppliername", length = 100, nullable = false)
	private String ssuppliername;

	@Column(name = "saddress1", length = 255, nullable = false)
	private String saddress1;

	@Column(name = "saddress2", length = 255)
	private String saddress2="";

	@Column(name = "saddress3", length = 255)
	private String saddress3="";

	@Column(name = "sphoneno", length = 50)
	private String sphoneno="";

	@Column(name = "smobileno", length = 50)
	private String smobileno="";

	@Column(name = "sfaxno", length = 50)
	private String sfaxno="";

	@Column(name = "semail", length = 50)
	private String semail="";

	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("1")
	private short ntransactionstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "napprovalstatus", nullable = false)
	@ColumnDefault("8")
	private short napprovalstatus = (short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String scountryname;
	@Transient
	private transient String sapprovalstatus;

	@Override
	public Supplier mapRow(ResultSet arg0, int arg1) throws SQLException {

		final Supplier supplier = new Supplier();

		supplier.setNsuppliercode(getInteger(arg0,"nsuppliercode",arg1));
		supplier.setNcountrycode(getInteger(arg0,"ncountrycode",arg1));
		supplier.setSsuppliername(StringEscapeUtils.unescapeJava(getString(arg0,"ssuppliername",arg1)));
		supplier.setSaddress1(StringEscapeUtils.unescapeJava(getString(arg0,"saddress1",arg1)));
		supplier.setSaddress2(StringEscapeUtils.unescapeJava(getString(arg0,"saddress2",arg1)));
		supplier.setSaddress3(StringEscapeUtils.unescapeJava(getString(arg0,"saddress3",arg1)));
		supplier.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0,"sphoneno",arg1)));
		supplier.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0,"smobileno",arg1)));
		supplier.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0,"sfaxno",arg1)));
		supplier.setSemail(StringEscapeUtils.unescapeJava(getString(arg0,"semail",arg1)));
		supplier.setScountryname(getString(arg0,"scountryname",arg1));
		supplier.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		supplier.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		supplier.setNsitecode(getShort(arg0,"nsitecode",arg1));
		supplier.setNstatus(getShort(arg0,"nstatus",arg1));
		supplier.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		supplier.setNapprovalstatus(getShort(arg0,"napprovalstatus",arg1));
		supplier.setSapprovalstatus(getString(arg0,"sapprovalstatus",arg1));
		return supplier;
	}

	
}
