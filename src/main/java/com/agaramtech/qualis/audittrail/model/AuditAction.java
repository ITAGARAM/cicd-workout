package com.agaramtech.qualis.audittrail.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
 * This entity class is used to map the fields with auditaction table of database.
 */
@Entity
@Table(name = "auditaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditAction extends CustomizedResultsetRowMapper<AuditAction> implements Serializable,  RowMapper<AuditAction> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nauditcode")
	private long nauditcode;

	@Column(name = "dauditdate", nullable = false)
	private Instant dauditdate;

	@Column(name = "sactiontype", length = 10, nullable = false)
	private String sactiontype;

	@Column(name = "sauditaction", length = 255, nullable = false)
	private String sauditaction;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyrolecode", nullable = false)
	private int ndeputyrolecode;

	@ColumnDefault("-1")
	@Column(name = "nreasoncode", nullable=false)
	private int nreasoncode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@Column(name = "sreason", length = 255)
	private String sreason="";

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nmodulecode", nullable = false)
	private short nmodulecode;

	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "sday", length = 10, nullable = false)
	private String sday = " ";

	@Column(name = "smonth", length = 10, nullable = false)
	private String smonth = " ";

	@Column(name = "syear", length = 10, nullable = false)
	private String syear = " ";

	@Column(name = "shour", length = 15, nullable = false)
	private String shour = " ";

	@Column(name = "sauditdate", length = 30, nullable = false)
	private String sauditdate = " ";
	
	@Column(name = "stransactionno", length = 50)
	private String stransactionno = "";	

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String sformname;
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String scomments;
	
	@Transient
	private transient String jsoncomments;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String smodulename;
	
	@Transient
	private transient String saudittraildate;
	
	@Transient
	private transient String viewPeriod;
	
	@Transient
	private transient Instant daudittraildate;
	
	@Transient
	private transient int nday;

	

	@Override
	public AuditAction mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final AuditAction objAuditAction = new AuditAction();
		
		objAuditAction.setNformcode(getShort(arg0, "nformcode", arg1));
		objAuditAction.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objAuditAction.setNdeputyrolecode(getInteger(arg0, "ndeputyrolecode", arg1));
		objAuditAction.setNauditcode(getLong(arg0, "nauditcode", arg1));
		objAuditAction.setNmodulecode(getShort(arg0, "nmodulecode", arg1));
		objAuditAction.setSactiontype(getString(arg0, "sactiontype", arg1));
		objAuditAction.setSauditaction(getString(arg0, "sauditaction", arg1));
		objAuditAction.setSreason(getString(arg0, "sreason", arg1));
		objAuditAction.setNusercode(getInteger(arg0, "nusercode", arg1));
		objAuditAction.setNreasoncode(getInteger(arg0, "nreasoncode", arg1));
		objAuditAction.setDauditdate(getInstant(arg0, "dauditdate", arg1));
		objAuditAction.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objAuditAction.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objAuditAction.setSformname(getString(arg0, "sformname", arg1));
		objAuditAction.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objAuditAction.setNstatus(getShort(arg0, "nstatus", arg1));
		objAuditAction.setScomments(getString(arg0, "scomments", arg1));
		objAuditAction.setSusername(getString(arg0, "susername", arg1));
		objAuditAction.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objAuditAction.setSmodulename(getString(arg0, "smodulename", arg1));
		objAuditAction.setSauditdate(getString(arg0, "sauditdate", arg1));
		objAuditAction.setSaudittraildate(getString(arg0, "saudittraildate", arg1));
		objAuditAction.setDaudittraildate(getInstant(arg0, "daudittraildate", arg1));
		objAuditAction.setViewPeriod(getString(arg0, "viewPeriod", arg1));
		objAuditAction.setSday(getString(arg0, "sday", arg1));
		objAuditAction.setSmonth(getString(arg0, "smonth", arg1));
		objAuditAction.setSyear(getString(arg0, "syear", arg1));
		objAuditAction.setShour(getString(arg0, "shour", arg1));
		objAuditAction.setNday(getInteger(arg0, "nday", arg1));
		objAuditAction.setJsoncomments(getString(arg0, "jsoncomments", arg1));
		objAuditAction.setStransactionno(getString(arg0, "stransactiono", arg1));
		return objAuditAction;
	}

}
