package com.agaramtech.qualis.biobank.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;	
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'bioparentsamplecollection' table of
 * the Database.
 */

@Entity
@Table(name = "bioparentsamplecollection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BioParentSampleCollection extends CustomizedResultsetRowMapper<BioParentSampleCollection>
implements Serializable, RowMapper<BioParentSampleCollection>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbioparentsamplecollectioncode")
	private int nbioparentsamplecollectioncode;
	
	@Column(name = "nbioparentsamplecode", nullable = false)
	private int nbioparentsamplecode;
	
	@Column(name = "nreceivingsitecode", nullable = false)
	private short nreceivingsitecode;
	
	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode;
	
	@Column(name = "nnoofsamples", nullable = false)
	private short nnoofsamples;
	
	@Column(name = "ntemperature", nullable = false)
	private short ntemperature;

	@Column(name = "scollectorname", length = 150)
	private String scollectorname;
	
	@Column(name = "stemporarystoragename", length = 150)
	private String stemporarystoragename;

	@Column(name = "ssendername", length = 150)
	private String ssendername;
	
	@Column(name = "nrecipientusercode", nullable = false)
	private int nrecipientusercode;

	@Column(name = "sinformation", length = 300)
	private String sinformation;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@Column(name = "dsamplecollectiondate", nullable = false)
	private Instant dsamplecollectiondate;

	@ColumnDefault("-1")
	@Column(name = "ntzsamplecollectiondate", nullable = false)
	private short ntzsamplecollectiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetsamplecollectiondate", nullable = false)
	private int noffsetsamplecollectiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "dbiobankarrivaldate", nullable = false)
	private Instant dbiobankarrivaldate;

	@ColumnDefault("-1")
	@Column(name = "ntzbiobankarrivaldate", nullable = false)
	private short ntzbiobankarrivaldate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsetbiobankarrivaldate", nullable = false)
	private int noffsetbiobankarrivaldate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "dtemporarystoragedate", nullable = false)
	private Instant dtemporarystoragedate;

	@ColumnDefault("-1")
	@Column(name = "ntztemporarystoragedate", nullable = false)
	private short ntztemporarystoragedate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsettemporarystoragedate", nullable = false)
	private int noffsettemporarystoragedate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@ColumnDefault("-1")
	@Column(name = "ntztransactiondate", nullable = false)
	private short ntztransactiondate = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("0")
	@Column(name = "noffsettransactiondate", nullable = false)
	private int noffsettransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("112")
	@Column(name ="ntransactionstatus", nullable=false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.NOTYETPROCESSED.gettransactionstatus();
	
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
	private transient String sparentsamplecode;
	
	@Transient
	private transient int ncohortno;
	
	@Transient
	private transient String sprojectincharge;
	
	@Transient
	private transient String sprojectcode;
	
	@Transient
	private transient String sreceivingsitename;
	
	@Transient
	private transient String srecipientusername;
	
	@Transient
	private transient String sprojecttitle;
	
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
	private transient String sproductcatname;
	
	@Transient
	private transient String stransactionstatus;
	
	@Transient
	private transient String ssamplecollectiondate;
	
	@Transient
	private transient String sbiobankarrivaldate;
	
	@Transient
	private transient String stemporarystoragedate;
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String stzsamplecollectiondate;
	
	@Transient
	private transient String stzbiobankarrivaldate;
	
	@Transient
	private transient String stztemporarystoragedate;
	
	@Transient
	private transient String stztransactiondate;

	@Override
	public BioParentSampleCollection mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BioParentSampleCollection objBioParentSampleCollection = new BioParentSampleCollection();

		objBioParentSampleCollection.setNbioparentsamplecollectioncode(getInteger(arg0, "nbioparentsamplecollectioncode", arg1));
		objBioParentSampleCollection.setNbioparentsamplecode(getInteger(arg0, "nbioparentsamplecode", arg1));
		objBioParentSampleCollection.setNreceivingsitecode(getShort(arg0, "nreceivingsitecode", arg1));
		objBioParentSampleCollection.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objBioParentSampleCollection.setNnoofsamples(getShort(arg0, "nnoofsamples", arg1));
		objBioParentSampleCollection.setNtemperature(getShort(arg0, "ntemperature", arg1));
		objBioParentSampleCollection.setScollectorname(getString(arg0, "scollectorname", arg1));
		objBioParentSampleCollection.setStemporarystoragename(getString(arg0, "stemporarystoragename", arg1));
		objBioParentSampleCollection.setSsendername(getString(arg0, "ssendername", arg1));
		objBioParentSampleCollection.setNrecipientusercode(getInteger(arg0, "nrecipientusercode", arg1));
		objBioParentSampleCollection.setSinformation(getString(arg0, "sinformation", arg1));
		objBioParentSampleCollection.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objBioParentSampleCollection.setDsamplecollectiondate(getInstant(arg0, "dsamplecollectiondate", arg1));
		objBioParentSampleCollection.setNtzsamplecollectiondate(getShort(arg0, "ntzsamplecollectiondate", arg1));
		objBioParentSampleCollection.setNoffsetsamplecollectiondate(getInteger(arg0, "noffsetsamplecollectiondate", arg1));
		objBioParentSampleCollection.setDbiobankarrivaldate(getInstant(arg0, "dbiobankarrivaldate", arg1));
		objBioParentSampleCollection.setNtzbiobankarrivaldate(getShort(arg0, "ntzbiobankarrivaldate", arg1));
		objBioParentSampleCollection.setNoffsetbiobankarrivaldate(getInteger(arg0, "noffsetbiobankarrivaldate", arg1));
		objBioParentSampleCollection.setDtemporarystoragedate(getInstant(arg0, "dtemporarystoragedate", arg1));
		objBioParentSampleCollection.setNtztemporarystoragedate(getShort(arg0, "ntztemporarystoragedate", arg1));
		objBioParentSampleCollection.setNoffsettemporarystoragedate(getInteger(arg0, "noffsettemporarystoragedate", arg1));
		objBioParentSampleCollection.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objBioParentSampleCollection.setNtztransactiondate(getShort(arg0, "ntztransactiondate", arg1));
		objBioParentSampleCollection.setNoffsettransactiondate(getInteger(arg0, "noffsettransactiondate", arg1));
		objBioParentSampleCollection.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBioParentSampleCollection.setNusercode(getInteger(arg0, "nusercode", arg1));
		objBioParentSampleCollection.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objBioParentSampleCollection.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBioParentSampleCollection.setNstatus(getShort(arg0, "nstatus", arg1));
		objBioParentSampleCollection.setSdiseasename(getString(arg0, "sdiseasename", arg1));
		objBioParentSampleCollection.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objBioParentSampleCollection.setScollectionsitename(getString(arg0, "scollectionsitename", arg1));
		objBioParentSampleCollection.setShospitalname(getString(arg0, "shospitalname", arg1));
		objBioParentSampleCollection.setSsamplestoragelocationname(getString(arg0, "ssamplestoragelocationname", arg1));
		objBioParentSampleCollection.setSusername(getString(arg0, "susername", arg1));
		objBioParentSampleCollection.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objBioParentSampleCollection.setSparentsamplecode(getString(arg0, "sparentsamplecode", arg1));
		objBioParentSampleCollection.setSprojectincharge(getString(arg0, "sprojectincharge", arg1));
		objBioParentSampleCollection.setSprojectcode(getString(arg0, "sprojectcode", arg1));
		objBioParentSampleCollection.setSreceivingsitename(getString(arg0, "sreceivingsitename", arg1));
		objBioParentSampleCollection.setNcohortno(getInteger(arg0, "ncohortno", arg1));
		objBioParentSampleCollection.setSproductcatname(getString(arg0, "sproductcatname", arg1));
		objBioParentSampleCollection.setSrecipientusername(getString(arg0, "srecipientusername", arg1));
		objBioParentSampleCollection.setStransactionstatus(getString(arg0, "stransactionstatus", arg1));
		objBioParentSampleCollection.setSsamplecollectiondate(getString(arg0, "ssamplecollectiondate", arg1));
		objBioParentSampleCollection.setSbiobankarrivaldate(getString(arg0, "sbiobankarrivaldate", arg1));
		objBioParentSampleCollection.setStemporarystoragedate(getString(arg0, "stemporarystoragedate", arg1));
		objBioParentSampleCollection.setStransactiondate(getString(arg0, "stransactiondate", arg1));
		objBioParentSampleCollection.setStzsamplecollectiondate(getString(arg0, "stzsamplecollectiondate", arg1));
		objBioParentSampleCollection.setStzbiobankarrivaldate(getString(arg0, "stzbiobankarrivaldate", arg1));
		objBioParentSampleCollection.setStztemporarystoragedate(getString(arg0, "stztemporarystoragedate", arg1));
		objBioParentSampleCollection.setStztransactiondate(getString(arg0, "stztransactiondate", arg1));

		return objBioParentSampleCollection;
	}
}
