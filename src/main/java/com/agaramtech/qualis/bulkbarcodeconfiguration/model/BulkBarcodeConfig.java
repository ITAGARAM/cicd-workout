package com.agaramtech.qualis.bulkbarcodeconfiguration.model;

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
import lombok.Data;
import lombok.AllArgsConstructor; 
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor; 
import org.apache.commons.text.StringEscapeUtils;

@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = false)  
@Entity
@Data
@Table(name = "bulkbarcodeconfig")
public class BulkBarcodeConfig extends CustomizedResultsetRowMapper<BulkBarcodeConfig>
		implements Serializable, RowMapper<BulkBarcodeConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbulkbarcodeconfigcode")
	private int nbulkbarcodeconfigcode;

	@Column(name = "nprojecttypecode", nullable = false)
	@ColumnDefault("-1")
	private int nprojecttypecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "sconfigname", length = 100, nullable = false)
	private String sconfigname;

	@Column(name = "nbarcodelength", nullable = false)
	private short nbarcodelength;

	@Column(name = "sdescription", length = 255)
	private String sdescription= "";

	@Column(name = "dmodifieddate", nullable= false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient String sversionno;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient short ntransactionstatus;
	@Transient
	private transient int nbulkbarcodeconfigversioncode;

	@Override
	public BulkBarcodeConfig mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BulkBarcodeConfig objBulkBarcodeConfig = new BulkBarcodeConfig();

		objBulkBarcodeConfig.setNbulkbarcodeconfigcode(getInteger(arg0, "nbulkbarcodeconfigcode", arg1));
		objBulkBarcodeConfig.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objBulkBarcodeConfig.setSconfigname(StringEscapeUtils.unescapeJava(getString(arg0, "sconfigname", arg1)));
		objBulkBarcodeConfig.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objBulkBarcodeConfig.setNbarcodelength(getShort(arg0, "nbarcodelength", arg1));
		objBulkBarcodeConfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBulkBarcodeConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		objBulkBarcodeConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objBulkBarcodeConfig.setSprojecttypename(StringEscapeUtils.unescapeJava(getString(arg0, "sprojecttypename", arg1)));
		objBulkBarcodeConfig.setSversionno(StringEscapeUtils.unescapeJava(getString(arg0, "sversionno", arg1)));
		objBulkBarcodeConfig.setStransdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0, "stransdisplaystatus", arg1)));
		objBulkBarcodeConfig.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBulkBarcodeConfig.setNbulkbarcodeconfigversioncode(getInteger(arg0, "nbulkbarcodeconfigversioncode", arg1));
		return objBulkBarcodeConfig;
	}
}
