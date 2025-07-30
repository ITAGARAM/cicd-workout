package com.agaramtech.qualis.exception.model;


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
@Table(name = "jsonexceptionlogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JsonExceptionLogs extends CustomizedResultsetRowMapper<JsonExceptionLogs> implements Serializable, RowMapper<JsonExceptionLogs> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "njsonexceptioncode")
	private int njsonexceptioncode;
	
	@Column(name = "sstacktrace", columnDefinition = "text")
	private String sstacktrace="";
	
	@Column(name = "smessage", columnDefinition = "text")
	private String smessage="";
	
	@Column(name = "nmodulecode", nullable=false)
	private short nmodulecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nformcode", nullable=false)
	private short nformcode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nusercode", nullable=false)
	private int nusercode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nuserrolecode", nullable=false)
	private int nuserrolecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndeputyusercode", nullable=false)
	private int ndeputyusercode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "ndeputyuserrolecode", nullable=false)
	private int ndeputyuserrolecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dexceptiondate", nullable=false)
	private Instant dexceptiondate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;	

	@Transient
	private transient String smodulename;
	
	@Transient
	private transient String sformname;
	
	@Transient
	private transient String sfirstname;
	
	@Transient
	private transient String slastname;
	
	@Transient
	private transient String ssitename;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sexceptiondate;
	
	@Transient
	private transient String sdeputyusername;
	
	@Transient
	private transient String sdeputyuserrolename;
	
	@Override
	public JsonExceptionLogs mapRow(ResultSet arg0, int arg1) throws SQLException {

		final JsonExceptionLogs objJsonExceptionLogs = new JsonExceptionLogs();
		
		objJsonExceptionLogs.setNjsonexceptioncode(getInteger(arg0,"njsonexceptioncode", arg1));
		objJsonExceptionLogs.setSstacktrace(StringEscapeUtils.unescapeJava(getString(arg0, "sstacktrace", arg1)));
		objJsonExceptionLogs.setSmessage(StringEscapeUtils.unescapeJava(getString(arg0, "smessage", arg1)));
		objJsonExceptionLogs.setNmodulecode(getShort(arg0, "nmodulecode", arg1));
		objJsonExceptionLogs.setNformcode(getShort(arg0, "nformcode", arg1));
		objJsonExceptionLogs.setNusercode(getInteger(arg0, "nusercode", arg1));
		objJsonExceptionLogs.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objJsonExceptionLogs.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objJsonExceptionLogs.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objJsonExceptionLogs.setDexceptiondate(getInstant(arg0, "dexceptiondate", arg1));
		objJsonExceptionLogs.setNstatus(getShort(arg0, "nstatus", arg1));
		objJsonExceptionLogs.setSmodulename(getString(arg0, "smodulename", arg1));
		objJsonExceptionLogs.setSformname(getString(arg0, "sformname", arg1));
		objJsonExceptionLogs.setSfirstname(getString(arg0, "sfirstname", arg1));
		objJsonExceptionLogs.setSlastname(getString(arg0, "slastname", arg1));
		objJsonExceptionLogs.setSsitename(getString(arg0, "ssitename", arg1));
		objJsonExceptionLogs.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objJsonExceptionLogs.setSusername(getString(arg0,"susername", arg1));
		objJsonExceptionLogs.setSuserrolename(getString(arg0,"suserrolename", arg1));
		objJsonExceptionLogs.setSexceptiondate(getString(arg0,"sexceptiondate", arg1));
		objJsonExceptionLogs.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objJsonExceptionLogs.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objJsonExceptionLogs;

	}

}
