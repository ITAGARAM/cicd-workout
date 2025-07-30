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
@Table(name = "country")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Country extends CustomizedResultsetRowMapper<Country> implements Serializable, RowMapper<Country>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncountrycode")
	private int ncountrycode;
	
	@Column(name = "scountryname", length =100, nullable=false)
	private String scountryname;
	
	@Column(name = "scountryshortname", length =10, nullable=false )
	private String scountryshortname;
	
	@Column(name = "stwocharcountry", length =2 )
	private String stwocharcountry="";	
	
	@Column(name = "sthreecharcountry", length =3 )
	private String sthreecharcountry="";	
	
	@Column(name = "dmodifieddate", nullable=false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)	
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Transient
	private transient int slno;
	@Transient
	private transient String smodifieddate;
	

	@Override
	public Country mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Country objCountry = new Country();
		
		objCountry.setNcountrycode(getInteger(arg0,"ncountrycode",arg1));
		objCountry.setScountryname(StringEscapeUtils.unescapeJava(getString(arg0,"scountryname",arg1)));
		objCountry.setScountryshortname(StringEscapeUtils.unescapeJava(getString(arg0,"scountryshortname",arg1)));        
		objCountry.setStwocharcountry(StringEscapeUtils.unescapeJava(getString(arg0,"stwocharcountry",arg1)));
		objCountry.setSthreecharcountry(StringEscapeUtils.unescapeJava(getString(arg0,"sthreecharcountry",arg1)));
		objCountry.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objCountry.setNstatus(getShort(arg0,"nstatus",arg1));
		objCountry.setSlno(getInteger(arg0,"slno",arg1));
		objCountry.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objCountry.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		return objCountry;
	}

}
