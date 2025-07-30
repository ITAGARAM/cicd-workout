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
@Table(name="product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Product  extends CustomizedResultsetRowMapper<Product> implements Serializable,RowMapper<Product>
{

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="nproductcode" )  
	private int nproductcode ;
	
	@Column(name="nproductcatcode", nullable=false) 
	private int nproductcatcode;
	
	@Column(name="sproductname", length=100, nullable=false )  
	private String sproductname;
	
	@Column(name="sdescription", length=255)  
	private String sdescription="";
	
	@ColumnDefault("4")
	@Column(name="ndefaultstatus", nullable=false) 
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name ="nsitecode", nullable=false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name="nstatus" , nullable=false)  
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String sproductcatname;
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient int ntransactionsamplecode;
	
	@Transient
	private transient int ncomponentcode;
	
	@Transient
	private transient int nallottedspeccode;
	
	@Override
	public Product mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		
		final Product objProduct = new Product();
		
		objProduct.setSproductname(StringEscapeUtils.unescapeJava(getString(arg0,"sproductname",arg1)));
		objProduct.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objProduct.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objProduct.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objProduct.setSproductcatname(getString(arg0,"sproductcatname",arg1));
		objProduct.setNstatus(getShort(arg0,"nstatus",arg1));
		objProduct.setNsitecode(getShort(arg0,"nsitecode",arg1)); 
		objProduct.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1)); 
		objProduct.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objProduct.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objProduct.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objProduct.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objProduct.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objProduct.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		
		return objProduct;
	}
	
		
}
