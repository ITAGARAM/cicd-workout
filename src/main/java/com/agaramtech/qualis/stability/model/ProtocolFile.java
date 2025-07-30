package com.agaramtech.qualis.stability.model;

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
@Table(name = "protocolfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProtocolFile extends CustomizedResultsetRowMapper<ProtocolFile> implements Serializable,RowMapper<ProtocolFile>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nprotocolfilecode")private int nprotocolfilecode;
	@Column(name="nprotocolversioncode",nullable =false)private int nprotocolversioncode;
	@Column(name = "nprotocolcode", nullable = false)private int nprotocolcode;
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)private short nlinkcode;
	@Column(name = "nattachmenttypecode", nullable = false)private short nattachmenttypecode;
	@Column(name = "sfilename", length = 100 , nullable = false)private String sfilename;
	@Column(name = "sdescription", length = 255 )private String sdescription;
	@Column(name="nfilesize", nullable = false)private int nfilesize;
	@Column(name="dcreateddate", nullable = false)private Instant dcreateddate;
	@Column(name="noffsetdcreateddate")private int noffsetdcreateddate;
	@Column(name="ntzcreateddate" )  private short ntzcreateddate;
	@Column(name = "ssystemfilename", length = 100 )private String ssystemfilename;
	@Column(name="dmodifieddate", nullable = false)private Instant dmodifieddate;
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String slinkname;
	@Transient
	private transient String sattachmenttype;
	@Transient
	private transient String screateddate;
	@Transient
	private transient String stypename;
	@Transient
	private transient String sfilesize;
	

	
	public ProtocolFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		ProtocolFile objProtocolFile = new ProtocolFile();
		objProtocolFile.setNprotocolfilecode(getInteger(arg0,"nprotocolfilecode",arg1));
		objProtocolFile.setNprotocolversioncode(getInteger(arg0,"nprotocolversioncode",arg1));
		objProtocolFile.setNprotocolcode(getInteger(arg0,"nprotocolcode",arg1));
		objProtocolFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objProtocolFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objProtocolFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objProtocolFile.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objProtocolFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objProtocolFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objProtocolFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objProtocolFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objProtocolFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objProtocolFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objProtocolFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objProtocolFile.setStypename(getString(arg0,"stypename",arg1));
		objProtocolFile.setSfilesize(getString(arg0,"sfilesize",arg1));
		objProtocolFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objProtocolFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objProtocolFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objProtocolFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objProtocolFile;
	}

}
