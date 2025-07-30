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

@Entity
@Table(name = "patientcategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PatientCategory extends CustomizedResultsetRowMapper<PatientCategory> implements Serializable, RowMapper<PatientCategory> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "npatientcatcode ")
	private int npatientcatcode;

	@Column(name = "nprojecttypecode ")
	private int nprojecttypecode;

	@Column(name = "spatientcatname ", length = 100, nullable = false)
	private String spatientcatname;

	@Column(name = "ncode", nullable = false)
	private short ncode;
	
	@Column(name = "ncodelength", nullable = false)
	private int ncodelength;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient boolean iscodeexists;
	@Transient
	private transient boolean ispatientcatnameexists;
	@Transient
	private transient String sprojecttypename = "";

	@Override
	public PatientCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		PatientCategory objPatientCategory = new PatientCategory();
		objPatientCategory.setNpatientcatcode(getInteger(arg0, "npatientcatcode", arg1));
		objPatientCategory.setSpatientcatname(StringEscapeUtils.unescapeJava(getString(arg0, "spatientcatname", arg1)));
		objPatientCategory.setNcode(getShort(arg0, "ncode", arg1));
		objPatientCategory.setNcodelength(getInteger(arg0, "ncodelength", arg1));
		objPatientCategory.setIscodeexists(getBoolean(arg0, "iscodeexists", arg1));
		objPatientCategory.setIspatientcatnameexists(getBoolean(arg0, "ispatientcatnameexists", arg1));
		objPatientCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objPatientCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objPatientCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		objPatientCategory.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objPatientCategory.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));

		return objPatientCategory;
	}

}