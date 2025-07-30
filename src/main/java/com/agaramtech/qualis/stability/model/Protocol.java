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
@Table(name = "protocol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Protocol extends CustomizedResultsetRowMapper<Protocol> implements Serializable,RowMapper<Protocol>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nprotocolcode",nullable =false)private int nprotocolcode;
	@Column(name="nproductcatcode",nullable=false)private int nproductcatcode;
	@Column(name="nproductcode",nullable=false)private int nproductcode;
	@Column(name="sprotocolid",nullable =false,length=30)private String sprotocolid;
	@Column(name="sprotocolname",nullable =false,length=100)private String sprotocolname;			
	@Column(name = "dmodifieddate",nullable=false)	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode",nullable=false)private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus",nullable=false)private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sproductcatname;
	@Transient
	private transient String sproductname;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String smodifieddate;
	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient int nprotocolversioncode;
	@Transient
	private transient int napproveconfversioncode;
	@Transient
	private transient String susername;
	@Transient
	private transient String suserrolename;


	public Protocol mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		Protocol objProtocol =new Protocol();
		objProtocol.setNprotocolcode(getInteger(arg0,"nprotocolcode",arg1));
		objProtocol.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objProtocol.setNproductcode(getInteger(arg0,"nproductcode",arg1));
		objProtocol.setSprotocolid(getString(arg0,"sprotocolid",arg1));
		objProtocol.setSprotocolname(StringEscapeUtils.unescapeJava(getString(arg0,"sprotocolname",arg1)));
		objProtocol.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objProtocol.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objProtocol.setNstatus(getShort(arg0,"nstatus",arg1));
		objProtocol.setSproductcatname(getString(arg0,"sproductcatname",arg1));
		objProtocol.setSproductname(getString(arg0,"sproductname",arg1));		
		objProtocol.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objProtocol.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objProtocol.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objProtocol.setNprotocolversioncode(getShort(arg0,"nprotocolversioncode",arg1));
		objProtocol.setNapproveconfversioncode(getInteger(arg0,"napproveconfversioncode",arg1));
		objProtocol.setSusername(getString(arg0,"susername",arg1));
		objProtocol.setSuserrolename(getString(arg0,"suserrolename",arg1));

		return objProtocol;
	}
}
