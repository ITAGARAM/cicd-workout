package com.agaramtech.qualis.BarcodeConfiguration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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

@Entity
@Table(name = "barcodeconfiguration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BarcodeConfiguration extends CustomizedResultsetRowMapper<BarcodeConfiguration>
		implements Serializable, RowMapper<BarcodeConfiguration> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbarcodeconfigurationcode ")
	private int nbarcodeconfigurationcode;

	@Column(name = "nbarcodetemplatecode ")
	private int nbarcodetemplatecode;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "ncontrolcode", nullable = false)
	private int ncontrolcode;

	@Column(name = "nbarcode", nullable = false)
	private int nbarcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "ndesigntemplatemappingcode", nullable = false)
	private int ndesigntemplatemappingcode;

	@Transient
	private transient String sformname;
	@Transient
	private transient List<Object> jnumericcolumns;
	@Transient
	private transient String jsonstring;
	@Transient
	private transient String sbarcodename;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient short nisdynamic;
	@Transient
	private transient short nissubsample;
	@Transient
	private transient int nquerybuildertablecode;
	@Transient
	private Map<String, Object> jsondatabt;
	@Transient
	private transient String sneedconfiguration;
	@Transient
	private transient String sbarcodeprint;
	@Transient
	private transient String ssqlqueryneed;
	@Transient
	private transient String ssqlqueryname;
	@Transient
	private transient String ssystemfilename;
	@Transient
	private transient int nfiltersqlquerycode;

	@Override
	public BarcodeConfiguration mapRow(ResultSet arg0, int arg1) throws SQLException {
		BarcodeConfiguration objBarcodeConfiguration = new BarcodeConfiguration();
		objBarcodeConfiguration.setNbarcodetemplatecode(getInteger(arg0, "nbarcodetemplatecode", arg1));
		objBarcodeConfiguration.setSformname(getString(arg0, "sformname", arg1));
		objBarcodeConfiguration.setNformcode(getInteger(arg0, "nformcode", arg1));
		objBarcodeConfiguration.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objBarcodeConfiguration.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBarcodeConfiguration.setNstatus(getShort(arg0, "nstatus", arg1));
		objBarcodeConfiguration.setJnumericcolumns(getJSONArray(arg0, "jnumericcolumns", arg1));
		objBarcodeConfiguration.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		objBarcodeConfiguration.setJsonstring(getString(arg0, "jsonstring", arg1));
		objBarcodeConfiguration.setNbarcode(getInteger(arg0, "nbarcode", arg1));
		objBarcodeConfiguration.setNbarcodeconfigurationcode(getInteger(arg0, "nbarcodeconfigurationcode", arg1));
		objBarcodeConfiguration.setSbarcodename(getString(arg0, "sbarcodename", arg1));
		objBarcodeConfiguration.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBarcodeConfiguration.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objBarcodeConfiguration.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objBarcodeConfiguration.setNisdynamic(getShort(arg0, "nisdynamic", arg1));
		objBarcodeConfiguration.setNquerybuildertablecode(getInteger(arg0, "nquerybuildertablecode", arg1));
		objBarcodeConfiguration.setNissubsample(getShort(arg0, "nissubsample", arg1));
		objBarcodeConfiguration.setJsondatabt(getJsonObject(arg0, "jsondatabt", arg1));
		objBarcodeConfiguration.setSsqlqueryname(getString(arg0, "ssqlqueryname", arg1));
		objBarcodeConfiguration.setSneedconfiguration(getString(arg0, "sneedconfiguration", arg1));
		objBarcodeConfiguration.setSbarcodeprint(getString(arg0, "sbarcodeprint", arg1));
		objBarcodeConfiguration.setSsqlqueryneed(getString(arg0, "ssqlqueryneed", arg1));
		objBarcodeConfiguration.setNfiltersqlquerycode(getInteger(arg0, "nfiltersqlquerycode", arg1));
		objBarcodeConfiguration.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		return objBarcodeConfiguration;
	}

}
