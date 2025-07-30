package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "QuotationVersion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QuotationVersion extends CustomizedResultsetRowMapper implements Serializable,RowMapper<QuotationVersion> {

	
	
private static final long serialVersionUID = 1L;
@Id
@Column(name = "nquotationversioncode")
private int nquotationversioncode;

@Column(name = "nquotationcode")
private int nquotationcode;

@Column(name = "squotationversionname", length = 100, nullable = false)
private String squotationversionname;


@Column(name = "nsitecode", nullable = false)
@ColumnDefault("-1")
private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

@Column(name = "nstatus", nullable = false)
@ColumnDefault("1")
private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();



   public QuotationVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		QuotationVersion objQuotationVersion = new QuotationVersion();
			
		objQuotationVersion.setNquotationversioncode(getInteger(arg0,"nquotationversioncode",arg1));
		objQuotationVersion.setNquotationcode(getInteger(arg0,"nquotationcode",arg1));
        objQuotationVersion.setSquotationversionname(getString(arg0,"squotationversionname",arg1));
		objQuotationVersion.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objQuotationVersion.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objQuotationVersion;
		
	}

	
	

}