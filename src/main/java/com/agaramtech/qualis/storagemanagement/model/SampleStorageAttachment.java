package com.agaramtech.qualis.storagemanagement.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "samplestorageattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SampleStorageAttachment extends CustomizedResultsetRowMapper<SampleStorageAttachment>
		implements Serializable, RowMapper<SampleStorageAttachment> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstorageattachmentcode")
	private short nstorageattachmentcode;

	@Column(name = "nmaintenancecode")
	private short nmaintenancecode;

	@Column(name = "nattachmenttypecode")
	private short nattachmenttypecode;

	@Column(name = "nlinkcode")
	private short nlinkcode;

	@Column(name = "sdescription")
	private String sdescription = "";

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "nuserrolecode")
	private int nuserrolecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleStorageAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageAttachment sampleStorageAttachment = new SampleStorageAttachment();
		sampleStorageAttachment.setNstorageattachmentcode(getShort(arg0, "nstorageattachmentcode", arg1));
		sampleStorageAttachment.setNmaintenancecode(getShort(arg0, "nmaintenancecode", arg1));
		sampleStorageAttachment.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		sampleStorageAttachment.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		sampleStorageAttachment.setSdescription(getString(arg0, "sdescription", arg1));
		sampleStorageAttachment.setNusercode(getInteger(arg0, "nusercode", arg1));
		sampleStorageAttachment.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		sampleStorageAttachment.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		sampleStorageAttachment.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleStorageAttachment.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleStorageAttachment.setNstatus(getShort(arg0, "nstatus", arg1));
		return sampleStorageAttachment;
	}
}
