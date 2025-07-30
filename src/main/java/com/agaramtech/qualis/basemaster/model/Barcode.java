package com.agaramtech.qualis.basemaster.model;

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

/**
 * This class is used to map the fields of 'barcode' table of the Database.
 */
@Entity
@Table(name = "barcode")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Barcode extends CustomizedResultsetRowMapper<Barcode> implements Serializable, RowMapper<Barcode> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nbarcode")
	private int nbarcode = -1;
	@Column(name = "nquerycode", nullable = false)
	private int nquerycode = -1;
	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;
	@Column(name = "sbarcodename", length = 100, nullable = false)
	private String sbarcodename = "";
	@Column(name = "sdescription", length = 255, nullable = false)
	private String sdescription = "";
	@Column(name = "sfilename", length = 100, nullable = false)
	private String sfilename = "";
	@Column(name = "ssystemfilename", length = 100, nullable = false)
	private String ssystemfilename = "";
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Transient
	private String ssqlqueryname;
	@Transient
	private String scontrolname;
	@Transient
	private int nsqlquerycode;
	@Transient
	private String scontrolids;	

	@Override
	public Barcode mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Barcode barcode = new Barcode();
		barcode.setNbarcode(getInteger(arg0, "nbarcode", arg1));
		barcode.setNquerycode(getInteger(arg0, "nquerycode", arg1));
		barcode.setNcontrolcode(getShort(arg0, "ncontrolcode", arg1));
		barcode.setSbarcodename(StringEscapeUtils.unescapeJava(getString(arg0, "sbarcodename", arg1)));
		barcode.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		barcode.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0, "sfilename", arg1)));
		barcode.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		barcode.setNsitecode(getShort(arg0, "nsitecode", arg1));
		barcode.setNstatus(getShort(arg0, "nstatus", arg1));
		barcode.setSsqlqueryname(getString(arg0, "ssqlqueryname", arg1));
		barcode.setScontrolname(getString(arg0, "scontrolname", arg1));
		barcode.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		barcode.setScontrolids(getString(arg0, "scontrolids", arg1));
		barcode.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return barcode;
	}
}
