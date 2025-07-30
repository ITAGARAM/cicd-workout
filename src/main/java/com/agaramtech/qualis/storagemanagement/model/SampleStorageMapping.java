package com.agaramtech.qualis.storagemanagement.model;

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
@Table(name = "samplestoragemapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageMapping extends CustomizedResultsetRowMapper<SampleStorageMapping>
		implements Serializable, RowMapper<SampleStorageMapping> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplestoragemappingcode")
	private int nsamplestoragemappingcode;

	@Column(name = "nsamplestoragecontainerpathcode")
	private int nsamplestoragecontainerpathcode;

	@Column(name = "ncontainertypecode")
	private int ncontainertypecode;

	@Column(name = "ncontainerstructurecode")
	private int ncontainerstructurecode;

	@Column(name = "ndirectionmastercode")
	private short ndirectionmastercode;

	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "nrow")
	private int nrow;

	@Column(name = "ncolumn")
	private int ncolumn;

	@Column(name = "sboxid")
	private String sboxid;

	@Column(name = "nneedposition")
	private int nneedposition;

	@Column(name = "nquantity")
	private Double nquantity;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "nnoofcontainer")
	private int nnoofcontainer;

	@Column(name = "nunitcode")
	private int nunitcode;

	@Transient
	private transient String sproductname;

	@Transient
	private transient String scontainertype;

	@Transient
	private transient String squantity;

	@Transient
	private transient String sunitname;

	@Override
	public SampleStorageMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageMapping objSampleStorageMapping = new SampleStorageMapping();
		objSampleStorageMapping.setNsamplestoragemappingcode(getInteger(arg0, "nsamplestoragemappingcode", arg1));
		objSampleStorageMapping
				.setNsamplestoragecontainerpathcode(getInteger(arg0, "nsamplestoragecontainerpathcode", arg1));
		objSampleStorageMapping.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		objSampleStorageMapping.setNneedposition(getInteger(arg0, "nneedposition", arg1));
		objSampleStorageMapping.setNquantity(getDouble(arg0, "nquantity", arg1));
		objSampleStorageMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleStorageMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleStorageMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleStorageMapping.setSproductname(getString(arg0, "sproductname", arg1));
		objSampleStorageMapping.setScontainertype(getString(arg0, "scontainertype", arg1));
		objSampleStorageMapping.setNnoofcontainer(getInteger(arg0, "nnoofcontainer", arg1));
		objSampleStorageMapping.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		objSampleStorageMapping.setSquantity(getString(arg0, "squantity", arg1));
		objSampleStorageMapping.setSunitname(getString(arg0, "sunitname", arg1));
		objSampleStorageMapping.setNcontainerstructurecode(getInteger(arg0, "ncontainerstructurecode", arg1));
		objSampleStorageMapping.setNdirectionmastercode(getShort(arg0, "ndirectionmastercode", arg1));
		objSampleStorageMapping.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objSampleStorageMapping.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objSampleStorageMapping.setNrow(getInteger(arg0, "nrow", arg1));
		objSampleStorageMapping.setNcolumn(getInteger(arg0, "ncolumn", arg1));
		objSampleStorageMapping.setSboxid(StringEscapeUtils.unescapeJava(getString(arg0, "sboxid", arg1)));
		return objSampleStorageMapping;
	}

}