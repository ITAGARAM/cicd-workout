package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
@Table(name = "quotationversionhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QuotationVersionHistory extends CustomizedResultsetRowMapper implements Serializable,RowMapper<QuotationVersionHistory> {
		
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nquotationhistorycode")
	private int nquotationhistorycode;

	@Column(name = "nquotationcode", nullable = false)
	private int nquotationcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;
	
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "sretireremarks", length = 255, nullable = false)
	private String sretireremarks;
	
	@Column(name = "nquotationversioncode")
	private int nquotationversioncode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sdtransactiondate;
	@Transient
	private String susername;
	@Transient
	private String suserrolename;
	@Transient
	private String stransdisplaystatus;
	
	
	public QuotationVersionHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		QuotationVersionHistory objQuotationVersionHistory = new QuotationVersionHistory();
		objQuotationVersionHistory.setNquotationhistorycode(getInteger(arg0,"nquotationhistorycode",arg1));
		objQuotationVersionHistory.setNquotationcode(getInteger(arg0,"nquotationcode",arg1));
		
		objQuotationVersionHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objQuotationVersionHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objQuotationVersionHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objQuotationVersionHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objQuotationVersionHistory.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objQuotationVersionHistory.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objQuotationVersionHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		
		objQuotationVersionHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objQuotationVersionHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objQuotationVersionHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		objQuotationVersionHistory.setSdtransactiondate(getString(arg0,"sdtransactiondate",arg1));
		objQuotationVersionHistory.setSusername(getString(arg0,"susername",arg1));
		objQuotationVersionHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objQuotationVersionHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objQuotationVersionHistory.setSretireremarks(getString(arg0,"sretireremarks",arg1));
		objQuotationVersionHistory.setNquotationversioncode(getInteger(arg0,"nquotationversioncode",arg1));
		objQuotationVersionHistory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objQuotationVersionHistory;
	}


	
}
