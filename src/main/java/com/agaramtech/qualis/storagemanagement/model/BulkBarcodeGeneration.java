package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

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
@Table(name = "bulkbarcodegeneration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BulkBarcodeGeneration extends CustomizedResultsetRowMapper<BulkBarcodeGeneration>
		implements Serializable, RowMapper<BulkBarcodeGeneration> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbulkbarcodegenerationcode ")
	private int nbulkbarcodegenerationcode;

	@Column(name = "nprojecttypecode", nullable = false)
	private int nprojecttypecode;

	@Column(name = "nbulkbarcodeconfigcode", nullable = false)
	private int nbulkbarcodeconfigcode;

	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "ssystemfilename", length = 255)
	private String ssystemfilename = "";

	@Column(name = "dcreateddate", nullable = false)
	private Instant dcreateddate;

	@Column(name = "ntzcreateddate", nullable = false)
	private short ntzcreateddate;

	@Column(name = "noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private List<?> jsondata;

	@Transient
	private transient String screateddate;

	@Override
	public BulkBarcodeGeneration mapRow(ResultSet arg0, int arg1) throws SQLException {
		final BulkBarcodeGeneration objBulkBarcodeGeneration = new BulkBarcodeGeneration();
		objBulkBarcodeGeneration.setNbulkbarcodegenerationcode(getInteger(arg0, "nbulkbarcodegenerationcode", arg1));
		objBulkBarcodeGeneration.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBulkBarcodeGeneration.setNstatus(getShort(arg0, "nstatus", arg1));
		objBulkBarcodeGeneration.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		objBulkBarcodeGeneration.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objBulkBarcodeGeneration.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objBulkBarcodeGeneration.setNoffsetdcreateddate(getInteger(arg0, "noffsetdcreateddate", arg1));
		objBulkBarcodeGeneration.setNtzcreateddate(getShort(arg0, "ntzcreateddate", arg1));
		objBulkBarcodeGeneration.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objBulkBarcodeGeneration.setScreateddate(getString(arg0, "screateddate", arg1));
		objBulkBarcodeGeneration.setSsystemfilename(StringEscapeUtils.unescapeJava(getString(arg0, "ssystemfilename", arg1)));
		objBulkBarcodeGeneration.setNbulkbarcodeconfigcode(getInteger(arg0, "nbulkbarcodeconfigcode", arg1));
		objBulkBarcodeGeneration.setJsondata(getJsonObjecttoList(arg0, "jsondata", arg1));
		return objBulkBarcodeGeneration;
	}

}
