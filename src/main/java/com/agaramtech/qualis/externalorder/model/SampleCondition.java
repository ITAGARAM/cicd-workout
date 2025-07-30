package com.agaramtech.qualis.externalorder.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'samplecondition' table of the
 * Database.
 */
@Entity
@Table(name = "samplecondition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleCondition extends CustomizedResultsetRowMapper<SampleCondition> implements Serializable, RowMapper<SampleCondition> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsampleconditioncode")
	private int nsampleconditioncode;

	@Column(name = "ssampleconditionname", length = 100, nullable = false)
	private String ssampleconditionname;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleCondition mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleCondition sampleCondition = new SampleCondition();
		sampleCondition.setNsampleconditioncode(getInteger(arg0, "nsampleconditioncode", arg1));
		sampleCondition.setSsampleconditionname(StringEscapeUtils.unescapeJava(getString(arg0, "ssampleconditionname", arg1)));
		sampleCondition.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		sampleCondition.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleCondition.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleCondition.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return sampleCondition;
	}
}
