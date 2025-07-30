package com.agaramtech.qualis.configuration.model;

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
@Table(name = "sampletype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleType extends CustomizedResultsetRowMapper<SampleType> implements Serializable, RowMapper<SampleType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampletypecode")
	private short nsampletypecode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@ColumnDefault("4")
	@Column(name = "napprovalconfigview", nullable = false)
	private short napprovalconfigview =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "ncategorybasedflowrequired", nullable = false)
	private short ncategorybasedflowrequired;

	@Column(name = "nclinicaltyperequired", nullable = false)
	private short nclinicaltyperequired;

	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter =(short)Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.ALL.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("4")
	@Column(name = "nprojectspecrequired", nullable = false)
	private short nprojectspecrequired=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "ncomponentrequired", nullable = false)
	private short ncomponentrequired=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nportalrequired", nullable = false)
	private short nportalrequired=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "nrulesenginerequired", nullable = false)
	private short nrulesenginerequired=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("4")
	@Column(name = "noutsourcerequired", nullable = false)
	private short noutsourcerequired=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "ntransfiltertypecode", nullable = false)
	private int ntransfiltertypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String ssampletypename;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String scategorybasedflowrequired;
	@Transient
	private transient String sclinicaltyperequired;
	@Transient
	private transient String sportalrequired;
	@Transient
	private transient String sprojectspecrequired;
	@Transient
	private transient String scomponentrequired;
	@Transient
	private transient String srulesenginerequired;
	@Transient
	private transient String soutsourcerequired;
	@Transient
	private transient String stransfiltertypename;

	@Override
	public SampleType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final SampleType objSampleType = new SampleType();
		
		objSampleType.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objSampleType.setNformcode(getShort(arg0, "nformcode", arg1));
		objSampleType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSampleType.setSsampletypename(getString(arg0, "ssampletypename", arg1));
		objSampleType.setNsorter(getShort(arg0, "nsorter", arg1));
		objSampleType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleType.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objSampleType.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objSampleType.setNcategorybasedflowrequired(getShort(arg0, "ncategorybasedflowrequired", arg1));
		objSampleType.setNclinicaltyperequired(getShort(arg0, "nclinicaltyperequired", arg1));
		objSampleType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleType.setNprojectspecrequired(getShort(arg0, "nprojectspecrequired", arg1));
		objSampleType.setNcomponentrequired(getShort(arg0, "ncomponentrequired", arg1));
		objSampleType.setNportalrequired(getShort(arg0, "nportalrequired", arg1));
		objSampleType.setNrulesenginerequired(getShort(arg0, "nrulesenginerequired", arg1));
		objSampleType.setNoutsourcerequired(getShort(arg0, "noutsourcerequired", arg1));
		objSampleType.setScategorybasedflowrequired(getString(arg0, "scategorybasedflowrequired", arg1));
		objSampleType.setSclinicaltyperequired(getString(arg0, "sclinicaltyperequired", arg1));
		objSampleType.setSportalrequired(getString(arg0, "sportalrequired", arg1));
		objSampleType.setSprojectspecrequired(getString(arg0, "sprojectspecrequired", arg1));
		objSampleType.setScomponentrequired(getString(arg0, "scomponentrequired", arg1));
		objSampleType.setSrulesenginerequired(getString(arg0, "srulesenginerequired", arg1));
		objSampleType.setSoutsourcerequired(getString(arg0, "soutsourcerequired", arg1));
		objSampleType.setStransfiltertypename(getString(arg0, "stransfiltertypename", arg1));
		objSampleType.setNtransfiltertypecode(getInteger(arg0, "ntransfiltertypecode", arg1));

		return objSampleType;

	}
}
