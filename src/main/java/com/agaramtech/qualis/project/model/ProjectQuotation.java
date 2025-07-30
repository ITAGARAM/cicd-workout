package com.agaramtech.qualis.project.model;

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
@Data
@Table(name = "projectquotation")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectQuotation extends CustomizedResultsetRowMapper<ProjectQuotation>
		implements Serializable, RowMapper<ProjectQuotation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprojectquotationcode")
	private int nprojectquotationcode;

	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode;

	@Column(name = "nquotationcode", nullable = false)
	private int nquotationcode;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "sremarks", length = 255)
	private String sremarks="";

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sprojectname;

	@Transient
	private transient String squotationno;

	@Transient
	private transient String stransdisplaystatus;

	@Override
	public ProjectQuotation mapRow(ResultSet arg0, int arg1) throws SQLException {
		ProjectQuotation objProjectQuotation = new ProjectQuotation();
		objProjectQuotation.setNprojectquotationcode(getInteger(arg0, "nprojectquotationcode", arg1));
		objProjectQuotation.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objProjectQuotation.setNquotationcode(getInteger(arg0, "nquotationcode", arg1));
		objProjectQuotation.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objProjectQuotation.setNstatus(getShort(arg0, "nstatus", arg1));
		objProjectQuotation.setSquotationno(getString(arg0, "squotationno", arg1));
		objProjectQuotation.setSprojectname(getString(arg0, "sprojectname", arg1));
		objProjectQuotation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProjectQuotation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProjectQuotation.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0, "sremarks", arg1)));
		objProjectQuotation.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));

		return objProjectQuotation;
	}

}
