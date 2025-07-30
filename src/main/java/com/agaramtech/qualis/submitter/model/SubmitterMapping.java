package com.agaramtech.qualis.submitter.model;

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
@Table(name = "submittermapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubmitterMapping extends CustomizedResultsetRowMapper<SubmitterMapping>
		implements Serializable, RowMapper<SubmitterMapping> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsubmittermappingcode")
	private int nsubmittermappingcode;

	@Column(name = "ssubmittercode", length = 50, nullable = false)
	private String ssubmittercode;

	@Column(name = "ninstitutioncatcode")
	private int ninstitutioncatcode;

	@Column(name = "ninstitutioncode")
	private int ninstitutioncode;

	@Column(name = "ninstitutionsitecode")
	private int ninstitutionsitecode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sinstitutioncatname;

	@Transient
	private transient String sinstitutionname;

	@Transient
	private transient String sinstitutionsitename;

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient String ssubmitterid;

	@Override
	public SubmitterMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SubmitterMapping objSubmitterMapping = new SubmitterMapping();
		objSubmitterMapping.setNsubmittermappingcode(getInteger(arg0, "nsubmittermappingcode", arg1));
		objSubmitterMapping.setSsubmittercode(StringEscapeUtils.unescapeJava(getString(arg0, "ssubmittercode", arg1)));
		objSubmitterMapping.setSsubmitterid(getString(arg0, "ssubmitterid", arg1));
		objSubmitterMapping.setNinstitutioncatcode(getInteger(arg0, "ninstitutioncatcode", arg1));
		objSubmitterMapping.setNinstitutioncode(getInteger(arg0, "ninstitutioncode", arg1));
		objSubmitterMapping.setNinstitutionsitecode(getInteger(arg0, "ninstitutionsitecode", arg1));
		objSubmitterMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSubmitterMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSubmitterMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		objSubmitterMapping.setSinstitutioncatname(getString(arg0, "sinstitutioncatname", arg1));
		objSubmitterMapping.setSinstitutionname(getString(arg0, "sinstitutionname", arg1));
		objSubmitterMapping.setSinstitutionsitename(getString(arg0, "sinstitutionsitename", arg1));
		objSubmitterMapping.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objSubmitterMapping;
	}
}