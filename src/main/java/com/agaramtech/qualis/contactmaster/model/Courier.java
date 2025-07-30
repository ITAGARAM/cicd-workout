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
@Table(name = "courier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Courier extends CustomizedResultsetRowMapper<Courier> implements Serializable,RowMapper<Courier>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ncouriercode")
    private int ncouriercode;
	
	@Column(name = "scouriername", length =100, nullable=false)
	private String scouriername;
	
	@Column(name = "scontactperson", length =100 )
	private String scontactperson="";	
	
	@Column(name = "saddress1", length = 255)
	private String saddress1="";	
	
	@Column(name = "saddress2", length = 255)
	private String saddress2="";
	
	@Column(name = "saddress3", length =255 )
	private String saddress3="";	
	
	@Column(name = "ncountrycode", nullable=false)
	private int ncountrycode;
	
	@Column(name = "sphoneno", length = 50)
	private String sphoneno="";	
	
	@Column(name = "smobileno", length =50)
	private String smobileno="";
	
	@Column(name = "sfaxno", length =50)
	private String sfaxno="";	
	
	@Column(name = "semail", length =50)
	private String semail="";	
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String scountryname;

	@Override
	public Courier mapRow(ResultSet arg0, int arg1) throws SQLException {		
		final Courier courier = new Courier();	
		courier.setNcouriercode(getInteger(arg0,"ncouriercode",arg1));
		courier.setScouriername(StringEscapeUtils.unescapeJava(getString(arg0,"scouriername",arg1)));
		courier.setScontactperson(StringEscapeUtils.unescapeJava(getString(arg0,"scontactperson",arg1)));        
		courier.setSaddress1(StringEscapeUtils.unescapeJava(getString(arg0,"saddress1",arg1)));
		courier.setSaddress2(StringEscapeUtils.unescapeJava(getString(arg0,"saddress2",arg1)));
		courier.setSaddress3(StringEscapeUtils.unescapeJava(getString(arg0,"saddress3",arg1)));
		courier.setNcountrycode(getInteger(arg0,"ncountrycode",arg1));
		courier.setSphoneno(StringEscapeUtils.unescapeJava(getString(arg0,"sphoneno",arg1)));
		courier.setSmobileno(StringEscapeUtils.unescapeJava(getString(arg0,"smobileno",arg1)));        
		courier.setSfaxno(StringEscapeUtils.unescapeJava(getString(arg0,"sfaxno",arg1)));
		courier.setSemail(StringEscapeUtils.unescapeJava(getString(arg0,"semail",arg1)));
		courier.setNsitecode(getShort(arg0,"nsitecode",arg1));
		courier.setNstatus(getShort(arg0,"nstatus",arg1));
		courier.setScountryname(getString(arg0,"scountryname",arg1));
		courier.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return courier;
	}
}
