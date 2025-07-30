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
@Table(name = "supplierfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SupplierFile extends CustomizedResultsetRowMapper<SupplierFile> implements Serializable,RowMapper<SupplierFile> 
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsupplierfilecode")
	private int nsupplierfilecode;
	
	@Column(name = "nsuppliercode", nullable = false)
	private int nsuppliercode;
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;
	
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;
	
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	
	@Column(name="ntzcreateddate" )  
	private short ntzcreateddate;
	
	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;
	
	@Column(name = "ssystemfilename", length = 100)
	private String ssystemfilename="";

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String slinkname;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sattachmenttype;
	@Transient
	private transient String screateddate;
	@Transient
	private transient String sfilesize;
	@Transient
	private transient int napprovalstatus;
	
	
	
	@Override
	public SupplierFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SupplierFile objSupplierFile = new SupplierFile();
		objSupplierFile.setNsupplierfilecode(getInteger(arg0,"nsupplierfilecode",arg1));
		objSupplierFile.setNsuppliercode(getInteger(arg0,"nsuppliercode",arg1));
		objSupplierFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objSupplierFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objSupplierFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objSupplierFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objSupplierFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objSupplierFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objSupplierFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objSupplierFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objSupplierFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objSupplierFile.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objSupplierFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objSupplierFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objSupplierFile.setSfilename(getString(arg0,"sfilename",arg1));
		objSupplierFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objSupplierFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objSupplierFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSupplierFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSupplierFile.setNapprovalstatus(getInteger(arg0,"napprovalstatus",arg1));
		return objSupplierFile;
	}
	
	
}
