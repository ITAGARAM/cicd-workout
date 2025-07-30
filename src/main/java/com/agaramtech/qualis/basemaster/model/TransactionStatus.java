package com.agaramtech.qualis.basemaster.model;

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

@Entity
@Table(name = "transactionstatus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionStatus extends CustomizedResultsetRowMapper<TransactionStatus>
		implements Serializable, RowMapper<TransactionStatus> {


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntranscode")
	private short ntranscode;

	@Column(name = "stransstatus", length = 50, nullable = false)
	private String stransstatus;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String stransdisplaystatus;

	@Transient
	private transient String sactiondisplaystatus;

	@Transient
	private transient String salertdisplaystatus;

	@Transient
	private transient String svalidationtypename;
	
	@Transient
	private transient int napprovalconfigcode;
	
	@Transient
	private transient String scolorname;
	
	@Transient
	private transient int ntransactiontype;
	
	@Transient
	private transient int nactiontype;
	
	@Transient
	private transient int ntranscode1;
	
	@Transient
	private transient int nsorter;
	
	@Transient
	private transient short ntransactionstatus;
	
	@Transient
	private transient String sfilterstatus;
	
	@Transient
	private transient String sdefaultname;
	
	@Transient
	private transient short napprovalstatuscode;
	
	@Transient
	private transient short napproveconfversioncode;
	
	@Transient
	private transient String scolorhexcode;


	@Override
	public TransactionStatus mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TransactionStatus transactionstatus = new TransactionStatus();
		transactionstatus.setNstatus(getShort(arg0, "nstatus", arg1));
		transactionstatus.setNtranscode(getShort(arg0, "ntranscode", arg1));
		transactionstatus.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		transactionstatus.setStransstatus(StringEscapeUtils.unescapeJava(getString(arg0, "stransstatus", arg1)));
		transactionstatus.setSvalidationtypename(getString(arg0, "svalidationtypename", arg1));
		transactionstatus.setNapprovalconfigcode(getInteger(arg0, "napprovalconfigcode", arg1));
		transactionstatus.setScolorname(getString(arg0, "scolorname", arg1));
		transactionstatus.setNtransactiontype(getInteger(arg0, "ntransactiontype", arg1));
		transactionstatus.setNactiontype(getInteger(arg0, "nactiontype", arg1));
		transactionstatus.setNtranscode1(getInteger(arg0, "ntranscode1", arg1));
		transactionstatus.setSactiondisplaystatus(getString(arg0, "sactiondisplaystatus", arg1));
		transactionstatus.setSalertdisplaystatus(getString(arg0, "salertdisplaystatus", arg1));
		transactionstatus.setNsorter(getInteger(arg0, "nsorter", arg1));
		transactionstatus.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		transactionstatus.setSfilterstatus(getString(arg0, "sfilterstatus", arg1));
		transactionstatus.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		transactionstatus.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		transactionstatus.setNapprovalstatuscode(getShort(arg0, "napprovalstatuscode", arg1));
		transactionstatus.setNapproveconfversioncode(getShort(arg0, "napproveconfversioncode", arg1));
		transactionstatus.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		transactionstatus.setScolorhexcode(getString(arg0, "scolorhexcode", arg1));
		return transactionstatus;
	}
}
