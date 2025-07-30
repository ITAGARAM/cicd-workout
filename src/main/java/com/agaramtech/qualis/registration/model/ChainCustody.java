package com.agaramtech.qualis.registration.model;

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
 * This class is used to map the fields of 'chaincustody' table of the Database.
 */
@Entity
@Table(name = "chaincustody")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChainCustody extends CustomizedResultsetRowMapper<ChainCustody> implements Serializable,RowMapper<ChainCustody>{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column (name="nchaincustodycode")
	private long nchaincustodycode;	
	
	@Column (name="nformcode") 
	private short nformcode;
	
	@Column (name="ntablepkno") 
	private int ntablepkno;
	
	@Column (name="stablepkcolumnname",length =50,nullable=false) 
	private String stablepkcolumnname;
	
	@Column (name="stablename",length =50,nullable=false) 
	private String stablename;
	
	@Column (name="sitemno",length =50,nullable=false) 
	private String sitemno;
	
	@Column	(name="ntransactionstatus")
	private int ntransactionstatus;
	
	@Column	(name="nusercode")
	private int nusercode;
	
	@Column (name="nuserrolecode")
	private int nuserrolecode;
	
	@Column(name="dtransactiondate")
	private Instant dtransactiondate;
	
	@Column	(name="ntztransactiondate")
	private short  ntztransactiondate; 
	
	@Column	(name="noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column	(name ="sremarks", length = 255, nullable = false)
	private String sremarks; 
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String username;
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String sformname;


	@Override
	public ChainCustody mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChainCustody objChainCustody =new ChainCustody();
		
		objChainCustody.setNchaincustodycode(getInteger(arg0,"nchaincustodycode",arg1));
		objChainCustody.setNformcode(getShort(arg0,"nformcode",arg1));
		objChainCustody.setNtablepkno(getInteger(arg0,"ntablepkno",arg1));
		objChainCustody.setStablepkcolumnname(StringEscapeUtils.unescapeJava(getString(arg0,"stablepkcolumnname",arg1)));
		objChainCustody.setStablename(StringEscapeUtils.unescapeJava(getString(arg0,"stablename",arg1)));
		objChainCustody.setSitemno(StringEscapeUtils.unescapeJava(getString(arg0,"sitemno",arg1)));	
		objChainCustody.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objChainCustody.setNusercode(getInteger(arg0,"nusercode",arg1));
		objChainCustody.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objChainCustody.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objChainCustody.setNtztransactiondate(getShort(arg0,"ntztransactiondate",arg1));
		objChainCustody.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objChainCustody.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0,"sremarks",arg1)));
		objChainCustody.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objChainCustody.setNstatus(getShort(arg0,"nstatus",arg1));
		objChainCustody.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objChainCustody.setUsername(getString(arg0,"username",arg1));
		objChainCustody.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objChainCustody.setSformname(getString(arg0,"sformname",arg1));	
	
		return objChainCustody;	
	}

}
