package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
@Table(name = "quotationtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QuotationType extends CustomizedResultsetRowMapper implements Serializable,RowMapper<QuotationType> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nquotationtypecode")
	private int nquotationtypecode;
	
	@Column(name = "squotationname", length = 100, nullable = false)
	private String squotationname;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private String sdisplaystatus;

	public QuotationType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		QuotationType objQuotation = new QuotationType();
		
		objQuotation.setNquotationtypecode(getInteger(arg0,"nquotationtypecode",arg1));
		objQuotation.setSquotationname(getString(arg0,"squotationname",arg1));
		objQuotation.setSquotationname(getString(arg0,"squotationname",arg1));
		objQuotation.setDmodifieddate(getDate(arg0,"dmodifieddate",arg1));
		objQuotation.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objQuotation.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objQuotation;
		
	}
	

}