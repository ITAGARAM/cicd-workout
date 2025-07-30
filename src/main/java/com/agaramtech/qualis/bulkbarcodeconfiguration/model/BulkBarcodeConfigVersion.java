package com.agaramtech.qualis.bulkbarcodeconfiguration.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = false)  
@Entity
@Data
@Table(name = "bulkbarcodeconfigversion")
public class BulkBarcodeConfigVersion extends CustomizedResultsetRowMapper<BulkBarcodeConfigVersion>
		implements Serializable, RowMapper<BulkBarcodeConfigVersion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbulkbarcodeconfigversioncode")
	private int nbulkbarcodeconfigversioncode;

	@Column(name = "nbulkbarcodeconfigcode")
	private int nbulkbarcodeconfigcode;

	@Column(name = "sversionno", length = 20, nullable = false)
	private String sversionno;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "dmodifieddate", nullable= false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public BulkBarcodeConfigVersion mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BulkBarcodeConfigVersion objBulkBarcodeConfig = new BulkBarcodeConfigVersion();

		objBulkBarcodeConfig.setNbulkbarcodeconfigcode(getInteger(arg0, "nbulkbarcodeconfigcode", arg1));
		objBulkBarcodeConfig.setSversionno(StringEscapeUtils.unescapeJava(getString(arg0, "sversionno", arg1)));
		objBulkBarcodeConfig.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objBulkBarcodeConfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBulkBarcodeConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		objBulkBarcodeConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objBulkBarcodeConfig.setNbulkbarcodeconfigversioncode(getInteger(arg0, "nbulkbarcodeconfigversioncode", arg1));
		return objBulkBarcodeConfig;
	}
}
