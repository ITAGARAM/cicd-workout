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
@Data
@Table(name = "productfile")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProductFile extends CustomizedResultsetRowMapper<ProductFile> implements Serializable,RowMapper<ProductFile> 
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nproductfilecode")
	private int nproductfilecode;
	
	@ColumnDefault("-1")
	@Column(name = "nproductcode", nullable = false)
	private int nproductcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();;
	
	@ColumnDefault("-1")	
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
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
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")	
	@Column(name = "nsitecode", nullable = false)	
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();;


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
	
	
	
	
	@Override
	public ProductFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ProductFile objProductFile = new ProductFile();
		objProductFile.setNproductfilecode(getInteger(arg0,"nproductfilecode",arg1));
		objProductFile.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objProductFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objProductFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objProductFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objProductFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objProductFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objProductFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objProductFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objProductFile.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objProductFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objProductFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objProductFile.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objProductFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objProductFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objProductFile.setSfilename(getString(arg0,"sfilename",arg1));
		objProductFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objProductFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objProductFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objProductFile.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objProductFile;
	}

	
}

