package com.agaramtech.qualis.configuration.model;

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
@Data
@Table(name = "treetemplatetransactionrole")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreeTemplateTransactionRole extends CustomizedResultsetRowMapper<TreeTemplateTransactionRole> implements Serializable, RowMapper<TreeTemplateTransactionRole> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntemptransrolecode")
	private int ntemptransrolecode;
	
	@Column(name = "napprovalconfigcode")
	private short napprovalconfigcode;
	
	@Column(name = "nuserrolecode")
	private int nuserrolecode;
	
	@Column(name = "ntreeversiontempcode", nullable = false)
	private int ntreeversiontempcode;
	
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;
	
	@Column(name = "nlevelno", nullable = false)
	private short nlevelno;
	
	@Column(name = "nparentnode", nullable = false)
	private int nparentnode;
	
	@Column(name = "ntemplatecode", nullable = false)
	private int ntemplatecode;
	
	@Column(name = "schildnode", length = 100)
	private String schildnode;
	
	@Column(name = "slevelformat", length = 100)
	private String slevelformat;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sresultentrystatus;
	
	@Transient
	private transient String sapprovalentrystatus;
	
	@Transient
	private transient String sversiondescription;
	
	@Transient
	private transient String suserrolenamelevel;

	@Override
	public TreeTemplateTransactionRole mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TreeTemplateTransactionRole objTreeTemplateTransactionUserrol = new TreeTemplateTransactionRole();

		objTreeTemplateTransactionUserrol.setNtemptransrolecode(getInteger(arg0,"ntemptransrolecode",arg1));
		objTreeTemplateTransactionUserrol.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		objTreeTemplateTransactionUserrol.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objTreeTemplateTransactionUserrol.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objTreeTemplateTransactionUserrol.setNlevelno(getShort(arg0,"nlevelno",arg1));
		objTreeTemplateTransactionUserrol.setNparentnode(getInteger(arg0,"nparentnode",arg1));
		objTreeTemplateTransactionUserrol.setNtemplatecode(getInteger(arg0,"ntemplatecode",arg1));
		objTreeTemplateTransactionUserrol.setSchildnode(StringEscapeUtils.unescapeJava(getString(arg0,"schildnode",arg1)));
		objTreeTemplateTransactionUserrol.setSlevelformat(StringEscapeUtils.unescapeJava(getString(arg0,"slevelformat",arg1)));
		objTreeTemplateTransactionUserrol.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTreeTemplateTransactionUserrol.setNstatus(getShort(arg0,"nstatus",arg1));
		objTreeTemplateTransactionUserrol.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objTreeTemplateTransactionUserrol.setSapprovalentrystatus(getString(arg0,"sapprovalentrystatus",arg1));
		objTreeTemplateTransactionUserrol.setSresultentrystatus(getString(arg0,"sresultentrystatus",arg1));
		objTreeTemplateTransactionUserrol.setSversiondescription(getString(arg0,"sversiondescription",arg1));
		objTreeTemplateTransactionUserrol.setNtreeversiontempcode(getInteger(arg0,"ntreeversiontempcode",arg1));
		objTreeTemplateTransactionUserrol.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTreeTemplateTransactionUserrol.setSuserrolenamelevel(getString(arg0,"suserrolenamelevel",arg1));
		return objTreeTemplateTransactionUserrol;
	}
	
}
