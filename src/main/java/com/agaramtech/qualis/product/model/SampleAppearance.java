package com.agaramtech.qualis.product.model;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "Sample Appearance" table.
 * 
 * @jira ALPD-3583
 */
@Entity
@Table(name = "sampleappearance")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleAppearance extends CustomizedResultsetRowMapper<SampleAppearance>
		implements Serializable, RowMapper<SampleAppearance> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsampleappearancecode")
	private int nsampleappearancecode;
	@Column(name = "ssampleappearance", length = 100, nullable = false)
	private String ssampleappearance;
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient	
	private transient String smodifieddate;
	@Transient	
	private transient String sdisplaystatus;

	@Override
	public SampleAppearance mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleAppearance sampleappearance = new SampleAppearance();
		sampleappearance.setNsampleappearancecode(getInteger(arg0, "nsampleappearancecode", arg1));
		sampleappearance.setSsampleappearance(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleappearance", arg1)));
		sampleappearance.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		sampleappearance.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		sampleappearance.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleappearance.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleappearance.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleappearance.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		sampleappearance.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		return sampleappearance;
	}

}
