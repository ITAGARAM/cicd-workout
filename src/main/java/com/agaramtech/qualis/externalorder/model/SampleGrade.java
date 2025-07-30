package com.agaramtech.qualis.externalorder.model;

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

@Entity
@Table(name = "samplegrade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleGrade extends CustomizedResultsetRowMapper<SampleGrade> implements Serializable, RowMapper<SampleGrade> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsamplegradecode")
	private int nsamplegradecode;
	@Column(name = "ssamplegradename", length = 100, nullable = false)
	private String ssamplegradename = "";
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleGrade mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleGrade sampleGrade = new SampleGrade();
		sampleGrade.setNsamplegradecode(getInteger(arg0, "nsamplegradecode", arg1));
		sampleGrade.setSsamplegradename(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplegradename", arg1)));
		sampleGrade.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		sampleGrade.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleGrade.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleGrade.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return sampleGrade;
	}

}
