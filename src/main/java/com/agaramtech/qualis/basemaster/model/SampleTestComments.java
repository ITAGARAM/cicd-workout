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


@Entity
@Table(name = "sampletestcomments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleTestComments extends CustomizedResultsetRowMapper<SampleTestComments>
		implements Serializable, RowMapper<SampleTestComments> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampletestcommentscode")
	private int nsampletestcommentscode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ncommenttypecode")
	private short ncommenttypecode;

	@Column(name = "ncommentsubtypecode")
	private short ncommentsubtypecode;

	@Column(name = "spredefinedname", length = 100)
	private String spredefinedname = "";

	@Column(name = "sdescription", length = 2000)
	private String sdescription = "";

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String scommenttype;

	@Transient
	private transient String scommentsubtype;

	@Transient
	private transient String spredefinedenable;

	public SampleTestComments mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleTestComments sampleTestComments = new SampleTestComments();
		sampleTestComments.setNsampletestcommentscode(getInteger(arg0, "nsampletestcommentscode", arg1));
		sampleTestComments.setNcommenttypecode(getShort(arg0, "ncommenttypecode", arg1));
		sampleTestComments.setNcommentsubtypecode(getShort(arg0, "ncommentsubtypecode", arg1));
		sampleTestComments.setSpredefinedname(StringEscapeUtils.unescapeJava(getString(arg0, "spredefinedname", arg1)));
		sampleTestComments.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		sampleTestComments.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		sampleTestComments.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleTestComments.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleTestComments.setScommenttype(getString(arg0, "scommenttype", arg1));
		sampleTestComments.setScommentsubtype(getString(arg0, "scommentsubtype", arg1));
		sampleTestComments.setSpredefinedenable(getString(arg0, "spredefinedenable", arg1));
		sampleTestComments.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return sampleTestComments;
	}
}
