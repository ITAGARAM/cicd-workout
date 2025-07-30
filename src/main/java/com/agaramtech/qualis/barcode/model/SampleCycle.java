package com.agaramtech.qualis.barcode.model;

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
 * This class is used to map the fields of 'Sample Cycle' table of the Database.
 */
@Entity
@Table(name = "samplecycle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleCycle extends CustomizedResultsetRowMapper<SampleCycle>
		implements Serializable, RowMapper<SampleCycle> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nsamplecyclecode")
	private int nsamplecyclecode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ssamplecyclename", length = 30, nullable = false)
	private String ssamplecyclename;

	@Column(name = "ncode", nullable = false)
	private short ncode;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nprojecttypecode")
	private int nprojecttypecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String sprojecttypename = "";
	
	@Transient
	private transient String smodifieddate = "";
	
	@Transient
	private transient Boolean issamplecycle;
	
	@Transient
	private transient Boolean iscode;

	@Override
	public SampleCycle mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleCycle sampleCycle = new SampleCycle();
		sampleCycle.setNsamplecyclecode(getInteger(arg0, "nsamplecyclecode", arg1));
		sampleCycle.setSsamplecyclename(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplecyclename", arg1)));
		sampleCycle.setNcode(getShort(arg0, "ncode", arg1));
		sampleCycle.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleCycle.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleCycle.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleCycle.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		sampleCycle.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		sampleCycle.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		sampleCycle.setIssamplecycle(getBoolean(arg0, "issamplecycle", arg1));
		sampleCycle.setIscode(getBoolean(arg0, "iscode", arg1));
		return sampleCycle;
	}
}
