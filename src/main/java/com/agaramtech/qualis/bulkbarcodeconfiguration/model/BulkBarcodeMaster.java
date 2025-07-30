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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@Table(name = "barcodemaster")
public class BulkBarcodeMaster extends CustomizedResultsetRowMapper<BulkBarcodeMaster>
		implements Serializable, RowMapper<BulkBarcodeMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbarcodemastercode")
	private int nbarcodemastercode;
	@Column(name = "nformcode")
	private int nformcode;
	@Column(name = "nquerybuildertablecode")
	private short nquerybuildertablecode;
	@Column(name = "stablename", length = 50, nullable = false)
	private String stablename;
	@Column(name = "sconditionalfieldname", length = 50, nullable = false)
	private String sconditionalfieldname;
	@Column(name = "stablecolumnname", length = 50, nullable = false)
	private String stablecolumnname;
	@Column(name = "stableprimarykey", length = 50, nullable = false)
	private String stableprimarykey;
	@Column(name = "squery", length = 1000, nullable = false)
	private String squery;
	// @Column(name = "nfieldlength")
	// private short nfieldlength;
	// @Lob
	// @Column(name = "sdisplayname", columnDefinition = "jsonb")
	// private Map<String, Object> sdisplayname;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sformname;

	@Override
	public BulkBarcodeMaster mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BulkBarcodeMaster objBulkBarcodeMaster = new BulkBarcodeMaster();

		objBulkBarcodeMaster.setNbarcodemastercode(getInteger(arg0, "nbarcodemastercode", arg1));
		objBulkBarcodeMaster.setStablename(StringEscapeUtils.unescapeJava(getString(arg0, "stablename", arg1)));
		objBulkBarcodeMaster.setSconditionalfieldname(
				StringEscapeUtils.unescapeJava(getString(arg0, "sconditionalfieldname", arg1)));
		objBulkBarcodeMaster
				.setStablecolumnname(StringEscapeUtils.unescapeJava(getString(arg0, "stablecolumnname", arg1)));
		objBulkBarcodeMaster
				.setStableprimarykey(StringEscapeUtils.unescapeJava(getString(arg0, "stableprimarykey", arg1)));
		objBulkBarcodeMaster.setSquery(StringEscapeUtils.unescapeJava(getString(arg0, "squery", arg1)));
		// objBulkBarcodeMaster.setNfieldlength(getShort(arg0, "nfieldlength", arg1));
		objBulkBarcodeMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objBulkBarcodeMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objBulkBarcodeMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		// objBulkBarcodeMaster.setSdisplayname(getJsonObject(arg0, "sdisplayname",
		// arg1));
		objBulkBarcodeMaster.setSformname(StringEscapeUtils.unescapeJava(getString(arg0, "sformname", arg1)));
		objBulkBarcodeMaster.setNformcode(getInteger(arg0, "nformcode", arg1));
		objBulkBarcodeMaster.setNquerybuildertablecode(getShort(arg0, "nquerybuildertablecode", arg1));
		return objBulkBarcodeMaster;
	}
}
