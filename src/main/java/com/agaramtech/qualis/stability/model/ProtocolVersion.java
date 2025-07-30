package com.agaramtech.qualis.stability.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
@Table(name = "protocolversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProtocolVersion extends CustomizedResultsetRowMapper<ProtocolVersion> implements Serializable,RowMapper<ProtocolVersion>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nprotocolversioncode",nullable =false)private int nprotocolversioncode;
	@Column(name="nprotocolcode",nullable =false)private int nprotocolcode;
	@Column(name="ndesigntemplatemappingcode",nullable=false)private int ndesigntemplatemappingcode;
	@Column(name="napproveconfversioncode",nullable=false)private int napproveconfversioncode;
	@Column(name="sversion",nullable =false,length=20)private String sversion;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")private Map<String, Object> jsondata;
			
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")private Map<String, Object> jsonuidata;
			
	@Column(name="ntransactionstatus",nullable=false)private short ntransactionstatus;
	@Column(name = "dmodifieddate",nullable=false)	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode",nullable=false)private short nsitecode;
	@ColumnDefault("1")
	@Column(name = "nstatus",nullable=false)private short nstatus;	
		
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sproductcatname;
	@Transient
	private transient String sproductname;
	@Transient
	private transient String sprotocolname;
	@Transient
	private transient int nproductcatcode;
	@Transient
	private transient int nproductcode; 
	@Transient
	private transient String sfilename;
	@Transient
	private transient String ssystemfilename;
	
	public ProtocolVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		ProtocolVersion objProtocolVersion =new ProtocolVersion();
		objProtocolVersion.setNprotocolversioncode(getInteger(arg0,"nprotocolversioncode",arg1));
		objProtocolVersion.setNprotocolcode(getInteger(arg0,"nprotocolcode",arg1));
		objProtocolVersion.setNdesigntemplatemappingcode(getInteger(arg0,"ndesigntemplatemappingcode",arg1));
		objProtocolVersion.setNapproveconfversioncode(getInteger(arg0,"napproveconfversioncode",arg1));
		objProtocolVersion.setSversion(StringEscapeUtils.unescapeJava(getString(arg0,"sversion",arg1)));
		objProtocolVersion.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objProtocolVersion.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objProtocolVersion.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objProtocolVersion.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objProtocolVersion.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objProtocolVersion.setNstatus(getShort(arg0,"nstatus",arg1));		
		objProtocolVersion.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));	
		objProtocolVersion.setSproductcatname(getString(arg0,"sproductcatname",arg1));
		objProtocolVersion.setSproductname(getString(arg0,"sproductname",arg1));
		objProtocolVersion.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objProtocolVersion.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objProtocolVersion.setSprotocolname(getString(arg0,"sprotocolname",arg1));
		objProtocolVersion.setSfilename(getString(arg0, "sfilename", arg1));
		objProtocolVersion.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));

		
		return objProtocolVersion;
	}

}
