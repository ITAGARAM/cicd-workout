package com.agaramtech.qualis.biobank.model;

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
 * This class is used to map the fields of 'bioparentsamplereceiving' table of
 * the Database.
 */

@Entity
@Table(name = "bioparentsamplereceiving")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BioParentSampleReceiving extends CustomizedResultsetRowMapper<BioParentSampleReceiving>
		implements Serializable, RowMapper<BioParentSampleReceiving> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioparentsamplecode")
	private int nbioparentsamplecode;
	
	@Column(name = "sparentsamplecode", length = 30, nullable = false)
	private String sparentsamplecode;
	
	@Column(name = "ssubjectid", length = 50, nullable = false)
	private String ssubjectid;

	@Column(name = "scasetype", length = 100, nullable = false)
	private String scasetype;
	
	@Column(name = "ndiseasecode", nullable = false)
	private int ndiseasecode;

	@Column(name = "nbioprojectcode", nullable = false)
	private int nbioprojectcode;

	@Column(name = "ncohortno", nullable = false)
	private short ncohortno;

	@Column(name = "ncollectionsitecode", nullable = false)
	private short ncollectionsitecode;

	@Column(name = "ncollectedhospitalcode", nullable = false)
	private int ncollectedhospitalcode;

	@Column(name = "nsamplestoragelocationcode")
	private short nsamplestoragelocationcode;

	@Column(name = "darrivaldate", nullable = false)
	private Instant darrivaldate;

	@ColumnDefault("-1")
	@Column(name = "ntzarrivaldate", nullable = false)
	private short ntzarrivaldate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetarrivaldate", nullable = false)
	private int noffsetarrivaldate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsettransactiondate", nullable = false)
	private int noffsettransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name ="nusercode", nullable=false)
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name ="nuserrolecode", nullable=false)
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Transient
	private transient String sdiseasename;
	
	@Transient
	private transient String sprojecttitle;
	
	@Transient
	private transient String sprojectcode;
	
	@Transient
	private transient String sprojectincharge;
	
	@Transient
	private transient String scollectionsitename;
	
	@Transient
	private transient String shospitalname;
	
	
	@Transient
	private transient String ssamplestoragelocationname;

	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sarrivaldate;
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String stzarrivaldate;
	
	@Transient
	private transient String stztransactiondate;
	
	@Transient
	private transient int nstoragelocationtemp;
	

	@Override
	public BioParentSampleReceiving mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioParentSampleReceiving objBioParentSampleReceiving = new BioParentSampleReceiving();

		objBioParentSampleReceiving.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioParentSampleReceiving.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioParentSampleReceiving.setSsubjectid(getString(arg0, "ssubjectid", arg1));
		objBioParentSampleReceiving.setScasetype(getString(arg0, "scasetype", arg1));
		objBioParentSampleReceiving.setNdiseasecode(getInteger(arg0, "ndiseasecode", arg1));
		objBioParentSampleReceiving.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objBioParentSampleReceiving.setNcohortno(getShort(arg0, "ncohortno", arg1));
		objBioParentSampleReceiving.setNcollectionsitecode(getShort(arg0, "ncollectionsitecode", arg1));
		objBioParentSampleReceiving.setNcollectedhospitalcode(getShort(arg0, "ncollectedhospitalcode", arg1));
		objBioParentSampleReceiving.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		objBioParentSampleReceiving.setNoffsetarrivaldate(getInteger(arg0, "noffsetarrivaldate", arg1));
		objBioParentSampleReceiving.setNtzarrivaldate(getShort(arg0, "ntzarrivaldate", arg1));
		objBioParentSampleReceiving.setDarrivaldate(getInstant(arg0, "darrivaldate", arg1));
		objBioParentSampleReceiving.setNoffsettransactiondate(getInteger(arg0, "noffsettransactiondate", arg1));
		objBioParentSampleReceiving.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioParentSampleReceiving.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioParentSampleReceiving.setNusercode(getInteger(arg0, "nusercode", arg1));
		objBioParentSampleReceiving.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objBioParentSampleReceiving.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioParentSampleReceiving.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioParentSampleReceiving.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioParentSampleReceiving.setScollectionsitename(getString(arg0, "scollectionsitename", arg1));
		objBioParentSampleReceiving.setShospitalname(getString(arg0, "shospitalname", arg1));
		objBioParentSampleReceiving.setSsamplestoragelocationname(getString(arg0, "ssamplestoragelocationname", arg1));
		objBioParentSampleReceiving.setSdiseasename(getString(arg0, "sdiseasename", arg1));
		objBioParentSampleReceiving.setSusername(getString(arg0, "susername", arg1));
		objBioParentSampleReceiving.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objBioParentSampleReceiving.setSprojectcode(getString(arg0, "sprojectcode", arg1));
		objBioParentSampleReceiving.setSprojectincharge(getString(arg0, "sprojectincharge", arg1));
		objBioParentSampleReceiving.setSarrivaldate(getString(arg0, "sarrivaldate", arg1));
		objBioParentSampleReceiving.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objBioParentSampleReceiving.setStzarrivaldate(getString(arg0, "stzarrivaldate", arg1));
		objBioParentSampleReceiving.setStztransactiondate(getString(arg0, "stztransactiondate", arg1));
		objBioParentSampleReceiving.setNstoragelocationtemp(getInteger(arg0, "nstoragelocationtemp", arg1));


		return objBioParentSampleReceiving;
	}
}
