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
@Table(name = "institution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Institution extends CustomizedResultsetRowMapper<Institution> implements Serializable, RowMapper<Institution> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstitutioncode")
	private int ninstitutioncode;

	@Column(name = "ninstitutioncatcode")
	private int ninstitutioncatcode;

	@Column(name = "sinstitutionname", length = 100, nullable = false)
	private String sinstitutionname;

	@Column(name = "sinstitutioncode", length = 5, nullable = false)
	private String sinstitutioncode;

	@Column(name = "sdescription", length = 255, nullable = false)
	private String sdescription;

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
	private transient String smodifieddate;

	@Override
	public Institution mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Institution objInstitution = new Institution();
		objInstitution.setNinstitutioncode(getInteger(arg0, "ninstitutioncode", arg1));
		objInstitution.setNinstitutioncatcode(getInteger(arg0, "ninstitutioncatcode", arg1));
		objInstitution.setSinstitutionname(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutionname", arg1)));
		objInstitution.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objInstitution.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInstitution.setSinstitutioncatname(getString(arg0, "sinstitutioncatname", arg1));
		objInstitution.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInstitution.setNstatus(getShort(arg0, "nstatus", arg1));
		objInstitution.setSinstitutioncode(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutioncode", arg1)));
		objInstitution.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return objInstitution;
	}

}
