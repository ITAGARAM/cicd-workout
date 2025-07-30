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
@Table(name = "barcodetemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BarcodeTemplate extends CustomizedResultsetRowMapper<BarcodeTemplate>
		implements Serializable, RowMapper<BarcodeTemplate> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nbarcodetemplatecode ")
	private int nbarcodetemplatecode;

	@Column(name = "stableprimarykeyname ", length = 50, nullable = false)
	private String stableprimarykeyname;

	@Column(name = "stablename ", nullable = false)
	private String stablename;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "ncontrolcode", nullable = false)
	private int ncontrolcode;

	@Column(name = "nquerybuildertablecode", nullable = false)
	private int nquerybuildertablecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.DRAFT.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sformname;
	@Transient
	private transient List<Object> jnumericcolumns;
	@Transient
	private transient String jsonstring;
	@Transient
	private transient boolean nneedconfiguration;
	@Transient
	private transient boolean nneedscreenfilter;
	@Transient
	private transient boolean nbarcodeprint;
	@Transient
	private transient String scontrolname;
	@Transient
	private transient short nisdynamic;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient short nissubsample;
	@Transient
	private transient Map<String, Object> jsondatabt;
	@Transient
	private transient String ssqlqueryname;
	@Transient
	private transient int nsqlquerycode;
	@Transient
	private transient boolean nsqlqueryneed;
	@Transient
	private transient boolean nfiltersqlqueryneed;
	@Transient
	private transient String sneedconfiguration;
	@Transient
	private transient String sbarcodeprint;
	@Transient
	private transient String ssqlqueryneed;
	@Transient
	private transient String sfiltersqlqueryneed;

	@Override
	public BarcodeTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {
		BarcodeTemplate objBarcodeTemplate = new BarcodeTemplate();
		objBarcodeTemplate.setNbarcodetemplatecode(getInteger(arg0, "nbarcodetemplatecode", arg1));
		objBarcodeTemplate.setSformname(getString(arg0, "sformname", arg1));
		objBarcodeTemplate.setNformcode(getInteger(arg0, "nformcode", arg1));
		objBarcodeTemplate.setStablename(getString(arg0, "stablename", arg1));
		objBarcodeTemplate.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objBarcodeTemplate.setStableprimarykeyname(getString(arg0, "stableprimarykeyname", arg1));
		objBarcodeTemplate.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBarcodeTemplate.setNstatus(getShort(arg0, "nstatus", arg1));
		objBarcodeTemplate.setJnumericcolumns(getJSONArray(arg0, "jnumericcolumns", arg1));
		objBarcodeTemplate.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		objBarcodeTemplate.setJsonstring(getString(arg0, "jsonstring", arg1));
		objBarcodeTemplate.setNcontrolcode(getInteger(arg0, "ncontrolcode", arg1));
		objBarcodeTemplate.setNneedconfiguration(getBoolean(arg0, "nneedconfiguration", arg1));
		objBarcodeTemplate.setNneedscreenfilter(getBoolean(arg0, "nneedscreenfilter", arg1));
		objBarcodeTemplate.setNbarcodeprint(getBoolean(arg0, "nbarcodeprint", arg1));
		objBarcodeTemplate.setScontrolname(getString(arg0, "scontrolname", arg1));
		objBarcodeTemplate.setNquerybuildertablecode(getInteger(arg0, "nquerybuildertablecode", arg1));
		objBarcodeTemplate.setNisdynamic(getShort(arg0, "nisdynamic", arg1));
		objBarcodeTemplate.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objBarcodeTemplate.setNissubsample(getShort(arg0, "nissubsample", arg1));
		objBarcodeTemplate.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBarcodeTemplate.setJsondatabt(getJsonObject(arg0, "jsondatabt", arg1));
		objBarcodeTemplate.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		objBarcodeTemplate.setSsqlqueryname(getString(arg0, "ssqlqueryname", arg1));
		objBarcodeTemplate.setNsqlqueryneed(getBoolean(arg0, "nsqlqueryneed", arg1));
		objBarcodeTemplate.setSneedconfiguration(getString(arg0, "sneedconfiguration", arg1));
		objBarcodeTemplate.setSbarcodeprint(getString(arg0, "sbarcodeprint", arg1));
		objBarcodeTemplate.setSsqlqueryneed(getString(arg0, "ssqlqueryneed", arg1));
		objBarcodeTemplate.setNfiltersqlqueryneed(getBoolean(arg0, "nfiltersqlqueryneed", arg1));
		objBarcodeTemplate.setSfiltersqlqueryneed(getString(arg0, "sfiltersqlqueryneed", arg1));
		return objBarcodeTemplate;
	}

}
