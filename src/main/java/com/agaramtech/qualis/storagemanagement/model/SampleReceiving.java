package com.agaramtech.qualis.storagemanagement.model;

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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "storagesamplereceiving")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleReceiving extends CustomizedResultsetRowMapper<SampleReceiving> implements Serializable, RowMapper<SampleReceiving> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstoragesamplereceivingcode ")
	private int nstoragesamplereceivingcode;
	
	@Column(name = "sbarcodeid", length = 20, nullable = false)
	private String sbarcodeid;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Column(name = "dcollectiondate", nullable = false)
	private Instant dcollectiondate;
	
	@Column(name = "scomments", length = 255)
	private String scomments;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "ntzcollectiondatetime", nullable = false)
	private int ntzcollectiondatetime;
	
	@Column(name = "noffsetdcollectiondatetime", nullable = false)
	private int noffsetdcollectiondatetime;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String scollectiondate;
	@Transient
	private transient String sunitname;
	@Transient
	private transient int nprojecttypecode;
	@Transient
	private transient String sprojecttypename;
	@Transient
	private transient double nsampleqty;
	@Transient
	private transient boolean isprocessing;
	@Transient
	private transient boolean istemporarystorage;

	public SampleReceiving mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		SampleReceiving objSampleReceiving = new SampleReceiving();
		objSampleReceiving.setNstoragesamplereceivingcode(getInteger(arg0, "nstoragesamplereceivingcode", arg1));
		objSampleReceiving.setSbarcodeid(StringEscapeUtils.unescapeJava(getString(arg0, "sbarcodeid", arg1)));
		objSampleReceiving.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objSampleReceiving.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objSampleReceiving.setDcollectiondate(getInstant(arg0, " dcollectiondate", arg1));
		objSampleReceiving.setNtzcollectiondatetime(getInteger(arg0, "ntzcollectiondatetime", arg1));
		objSampleReceiving.setNoffsetdcollectiondatetime(getInteger(arg0, "noffsetdcollectiondatetime", arg1));
		objSampleReceiving.setScomments(StringEscapeUtils.unescapeJava(getString(arg0, "scomments", arg1)));
		objSampleReceiving.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSampleReceiving.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSampleReceiving.setNstatus(getShort(arg0, "nstatus", arg1));
		objSampleReceiving.setScollectiondate(StringEscapeUtils.unescapeJava(getString(arg0, "scollectiondate", arg1)));
		objSampleReceiving.setSunitname(StringEscapeUtils.unescapeJava(getString(arg0, "sunitname", arg1)));
		objSampleReceiving.setSprojecttypename(StringEscapeUtils.unescapeJava(getString(arg0, "sprojecttypename", arg1)));
		objSampleReceiving.setNsampleqty(getDouble(arg0, "nsampleqty", arg1));
		objSampleReceiving.setIsprocessing(getBoolean(arg0, "isprocessing", arg1));
		objSampleReceiving.setIstemporarystorage(getBoolean(arg0, "istemporarystorage", arg1));
		return objSampleReceiving;
	}

}
