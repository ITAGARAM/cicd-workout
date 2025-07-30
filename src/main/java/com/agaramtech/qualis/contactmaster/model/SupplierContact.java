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

@Entity
@Table(name = "suppliercontact")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupplierContact extends CustomizedResultsetRowMapper<SupplierContact> implements Serializable,RowMapper<SupplierContact>
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsuppliercontactcode")
	private int nsuppliercontactcode;

	@Column(name = "nsuppliercode")
	private int nsuppliercode;

	@Column(name = "ssuppliercontactname", length = 100, nullable = false)
	private String ssuppliercontactname;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@Column(name = "sdesignation", length = 255)
	private String sdesignation="";

	@Column(name = "stelephoneno", length = 50)
	private String stelephoneno="";

	@Column(name = "smobileno", length = 50)
	private String smobileno="";

	@Column(name = "semail", length = 50)
	private String semail="";

	@Column(name = "ndefaultstatus", nullable = false)
	@ColumnDefault("4")
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
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
	private transient boolean isreadonly;
	@Transient
	private transient String sdefaultstatus;
	@Transient
	private transient int napprovalstatus;

	@Override
	public SupplierContact mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SupplierContact objSupplierContact = new SupplierContact();

		objSupplierContact.setNsuppliercontactcode(getInteger(arg0,"nsuppliercontactcode",arg1));
		objSupplierContact.setNsuppliercode(getInteger(arg0,"nsuppliercode",arg1));
		objSupplierContact.setSsuppliercontactname(StringEscapeUtils.unescapeJava(getString(arg0,"ssuppliercontactname",arg1)));
		objSupplierContact.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objSupplierContact.setSdesignation(StringEscapeUtils.unescapeJava(getString(arg0,"sdesignation",arg1)));
		objSupplierContact.setStelephoneno(StringEscapeUtils.unescapeJava(getString(arg0,"stelephoneno",arg1)));
		objSupplierContact.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0,"smobileno",arg1)));
		objSupplierContact.setSemail(StringEscapeUtils.unescapeJava(getString(arg0,"semail",arg1)));
		objSupplierContact.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objSupplierContact.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objSupplierContact.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSupplierContact.setNstatus(getShort(arg0,"nstatus",arg1));
		objSupplierContact.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSupplierContact.setIsreadonly(getBoolean(arg0,"isreadonly",arg1));
		objSupplierContact.setSdefaultstatus(getString(arg0,"sdefaultstatus",arg1));
		objSupplierContact.setNapprovalstatus(getInteger(arg0,"napprovalstatus",arg1));

		return objSupplierContact;
	}

	
}
