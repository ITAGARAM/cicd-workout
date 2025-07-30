package com.agaramtech.qualis.externalorder.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
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

/**
 * This class is used to map the fields of 'externalordersampleattachment' table of the
 * Database.
 */

@Entity
@Table(name = "externalordersampleattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalOrderSampleAttachment extends CustomizedResultsetRowMapper<ExternalOrderSampleAttachment> implements Serializable, RowMapper<ExternalOrderSampleAttachment>{


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nexternalordersampleattachmentcode")
	private int nexternalordersampleattachmentcode;

	@Column(name = "nexternalordercode", nullable = false)
	private int nexternalordercode;
	
	@Column(name = "sexternalorderid", length = 50, nullable = false)
	private String sexternalorderid;
	
	@Column(name = "sorderseqno", length = 50, nullable = false)
	private String sorderseqno;

	@Column(name = "nlinkcode", nullable = false)
	@ColumnDefault("-1")
	private short nlinkcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;
		
	@Lob@Column(name = "jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String jsonValue;


	
	@Override
	public ExternalOrderSampleAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ExternalOrderSampleAttachment objExternalOrderSampleAttachment= new ExternalOrderSampleAttachment();
	
		objExternalOrderSampleAttachment.setNexternalordersampleattachmentcode(getInteger(arg0, "nexternalordersampleattachmentcode", arg1));
		objExternalOrderSampleAttachment.setNexternalordercode(getInteger(arg0, "nexternalordercode", arg1));
		objExternalOrderSampleAttachment.setSexternalorderid(StringEscapeUtils.unescapeJava(getString(arg0, "sexternalorderid", arg1)));
		objExternalOrderSampleAttachment.setSorderseqno(StringEscapeUtils.unescapeJava(getString(arg0, "sorderseqno", arg1)));
		objExternalOrderSampleAttachment.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objExternalOrderSampleAttachment.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objExternalOrderSampleAttachment.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objExternalOrderSampleAttachment.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objExternalOrderSampleAttachment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objExternalOrderSampleAttachment.setNstatus(getShort(arg0, "nstatus", arg1));
		objExternalOrderSampleAttachment.setJsonValue(getString(arg0, "jsonValue", arg1));
		return objExternalOrderSampleAttachment;
	}
	

}
