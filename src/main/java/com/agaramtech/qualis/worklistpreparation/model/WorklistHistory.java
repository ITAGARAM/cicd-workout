package com.agaramtech.qualis.worklistpreparation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
@Table(name = "worklisthistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WorklistHistory extends CustomizedResultsetRowMapper<WorklistHistory> implements Serializable,RowMapper<WorklistHistory>{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nworkhistorycode")
	private int nworkhistorycode;

	@Column(name = "nworklistcode", nullable = false)
	private int nworklistcode;
	
	@Column(name = "sworklistno", length = 30)
	private String sworklistno;
	
	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;	

	@Column(name = "scomments", length = 255)
	private String scomments;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private int nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String sarno;
	@Transient
	private transient String username;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String stransactiondate;
	@Transient
	private transient String stransdisplaystatus;
	
	
	@Override
	public WorklistHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		final WorklistHistory objWorklistHistory = new WorklistHistory();
		
		objWorklistHistory.setNworkhistorycode(getInteger(arg0,"nworkhistorycode",arg1));
		objWorklistHistory.setNworklistcode(getInteger(arg0,"nworklistcode",arg1));
		objWorklistHistory.setSworklistno(StringEscapeUtils.unescapeJava(getString(arg0,"sworklistno",arg1)));
		objWorklistHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objWorklistHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objWorklistHistory.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objWorklistHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objWorklistHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));		
		objWorklistHistory.setDtransactiondate(getDate(arg0,"dtransactiondate",arg1));
		objWorklistHistory.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objWorklistHistory.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objWorklistHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objWorklistHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objWorklistHistory.setNstatus(getInteger(arg0,"nstatus",arg1));
		objWorklistHistory.setSarno(getString(arg0,"sarno",arg1));
		objWorklistHistory.setUsername(getString(arg0,"username",arg1));
		objWorklistHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objWorklistHistory.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objWorklistHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		return objWorklistHistory;
	}

}
