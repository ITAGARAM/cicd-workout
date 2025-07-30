package com.agaramtech.qualis.contactmaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patientcasetype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PatientCaseType extends CustomizedResultsetRowMapper<PatientCaseType> implements Serializable,
RowMapper<PatientCaseType> {
	

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "npatientcasetypecode")
	private short npatientcasetypecode;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String spatientcasetypename;
	@Transient
	private transient String displayname;

	@Override
	public PatientCaseType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final PatientCaseType objPatientCaseType = new PatientCaseType();
		objPatientCaseType.setNpatientcasetypecode(getShort(arg0, "npatientcasetypecode", arg1));
		objPatientCaseType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objPatientCaseType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objPatientCaseType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objPatientCaseType.setNstatus(getShort(arg0, "nstatus", arg1));
		objPatientCaseType.setSpatientcasetypename(getString(arg0, "spatientcasetypename", arg1));
		objPatientCaseType.setDisplayname(getString(arg0, "displayname", arg1));
		return objPatientCaseType;
	}
}
