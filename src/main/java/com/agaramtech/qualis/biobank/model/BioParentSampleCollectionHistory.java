package com.agaramtech.qualis.biobank.model;

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

/**
 * This class is used to map the fields of 'bioparentsamplecollectionhistory' table of the Database.
 */
@Entity
@Table(name = "bioparentsamplecollectionhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BioParentSampleCollectionHistory extends CustomizedResultsetRowMapper<BioParentSampleCollectionHistory> implements Serializable, RowMapper<BioParentSampleCollectionHistory>  {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioparentsamplecolhistorycode")
	private int nbioparentsamplecolhistorycode;

	@Column(name = "nbioparentsamplecollectioncode", nullable = false)
	private int nbioparentsamplecollectioncode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;
	
	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Column(name = "scomments", length = 255)
	private String scomments="";
	
	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;
	
	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;
	
	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	
	@Transient
	private transient String username;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String stransactiondate;

	@Override
	public BioParentSampleCollectionHistory mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioParentSampleCollectionHistory objBioParentSampleCollectionHistory = new BioParentSampleCollectionHistory();
		
		objBioParentSampleCollectionHistory.setNbioparentsamplecolhistorycode(getInteger(arg0,"nbioparentsamplecolhistorycode",arg1));
		objBioParentSampleCollectionHistory.setNbioparentsamplecollectioncode(getInteger(arg0, "nbioparentsamplecollectioncode", arg1));
		objBioParentSampleCollectionHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objBioParentSampleCollectionHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objBioParentSampleCollectionHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objBioParentSampleCollectionHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objBioParentSampleCollectionHistory.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objBioParentSampleCollectionHistory.setScomments(getString(arg0, "scomments", arg1));
		objBioParentSampleCollectionHistory.setDtransactiondate(getDate(arg0, "dtransactiondate", arg1));
		objBioParentSampleCollectionHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objBioParentSampleCollectionHistory.setNtransdatetimezonecode(getInteger(arg0, "ntransdatetimezonecode", arg1));
		objBioParentSampleCollectionHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioParentSampleCollectionHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioParentSampleCollectionHistory.setUsername(getString(arg0, "username", arg1));
		objBioParentSampleCollectionHistory.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objBioParentSampleCollectionHistory.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBioParentSampleCollectionHistory.setStransactiondate(getString(arg0, "stransactiondate", arg1));

		
		return objBioParentSampleCollectionHistory;
	}
}
