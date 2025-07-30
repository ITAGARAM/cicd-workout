package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
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

@Entity
@Table(name = "storagesamplecollection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StorageSampleCollection extends CustomizedResultsetRowMapper<StorageSampleCollection>
		implements Serializable, RowMapper<StorageSampleCollection> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplecollectioncode ")
	private int nsamplecollectioncode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;

	@Column(name = "nprojecttypecode", nullable = false)
	private int nprojecttypecode;

	@Column(name = "sbarcodeid", length = 20, nullable = false)
	private String sbarcodeid;

	@Column(name = "scomments", length = 255)
	private String scomments="";

	@Column(name = "nsampleqty")
	private Double nsampleqty;

	@Column(name = "dcollectiondate", nullable = false)
	private Instant dcollectiondate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "ntzcollectiondatetime", nullable = false)
	private int ntzcollectiondatetime;

	@Column(name = "noffsetdcollectiondatetime", nullable = false)
	private int noffsetdcollectiondatetime;

	@Transient
	private transient String scollectiondate;

	@Transient
	private transient String sunitname;

	@Transient
	private transient String sprojecttypename;

	@Override
	public StorageSampleCollection mapRow(ResultSet arg0, int arg1) throws SQLException {
		final StorageSampleCollection objSampleCollection = new StorageSampleCollection();
		objSampleCollection.setNsamplecollectioncode(getInteger(arg0, "nsamplecollectioncode", arg1));
		objSampleCollection.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleCollection.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleCollection.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSampleCollection.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objSampleCollection.setSbarcodeid(StringEscapeUtils.unescapeJava(getString(arg0, "sbarcodeid", arg1)));
		objSampleCollection.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objSampleCollection.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleCollection.setDcollectiondate(getInstant(arg0, " dcollectiondate", arg1));
		objSampleCollection.setNsampleqty(getDouble(arg0, "nsampleqty", arg1));
		objSampleCollection.setNtzcollectiondatetime(getInteger(arg0, "ntzcollectiondatetime", arg1));
		objSampleCollection.setNoffsetdcollectiondatetime(getInteger(arg0, "noffsetdcollectiondatetime", arg1));
		objSampleCollection.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objSampleCollection.setScollectiondate(getString(arg0, "scollectiondate", arg1));
		objSampleCollection.setSunitname(getString(arg0, "sunitname", arg1));
		objSampleCollection.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		return objSampleCollection;
	}

}
