package com.agaramtech.qualis.testgroup.model;

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
@Table(name = "testgroupspecificationhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TestGroupSpecificationHistory extends CustomizedResultsetRowMapper<TestGroupSpecificationHistory> implements Serializable,RowMapper<TestGroupSpecificationHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nspecificationhistorycode")
	private int nspecificationhistorycode;
	
	@Column(name = "nallottedspeccode", nullable = false)
	private int nallottedspeccode;
	
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	
	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;
	
	@Column(name = "noffsetdtransactiondate", nullable = false)
	private int noffsetdtransactiondate;
	
	@Column(name="ntztransactiondate" )  
	private short ntztransactiondate;
	
	@Column(name = "scomments", length = 255)
	private String scomments="";
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sdtransactiondate;
	@Transient
	private transient String susername;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String stransdisplaystatus;

	@Override
	public TestGroupSpecificationHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		TestGroupSpecificationHistory objTestGroupSpecHistory = new TestGroupSpecificationHistory();
		objTestGroupSpecHistory.setNspecificationhistorycode(getInteger(arg0,"nspecificationhistorycode",arg1));
		objTestGroupSpecHistory.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestGroupSpecHistory.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objTestGroupSpecHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objTestGroupSpecHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objTestGroupSpecHistory.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objTestGroupSpecHistory.setScomments(StringEscapeUtils.unescapeJava(getString(arg0,"scomments",arg1)));
		objTestGroupSpecHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestGroupSpecHistory.setSdtransactiondate(getString(arg0,"sdtransactiondate",arg1));
		objTestGroupSpecHistory.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestGroupSpecHistory.setSusername(getString(arg0,"susername",arg1));
		objTestGroupSpecHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objTestGroupSpecHistory.setNoffsetdtransactiondate(getInteger(arg0,"Noffsetdtransactiondate",arg1));
		objTestGroupSpecHistory.setNtztransactiondate(getShort(arg0,"ntztransactiondate",arg1));
		objTestGroupSpecHistory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestGroupSpecHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objTestGroupSpecHistory;
	}
}
