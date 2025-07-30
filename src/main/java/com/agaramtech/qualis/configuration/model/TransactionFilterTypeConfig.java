package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
@Table(name = "transactionfiltertypeconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionFilterTypeConfig extends CustomizedResultsetRowMapper<TransactionFilterTypeConfig> implements Serializable,RowMapper<TransactionFilterTypeConfig> {
	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "ntransfiltertypeconfigcode")
	private int ntransfiltertypeconfigcode;
	
	@ColumnDefault("-1")
	@Column(name = "ntransfiltertypecode", nullable = false)
	private int ntransfiltertypecode = (int)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nsampletypecode", nullable = false) private int nsampletypecode;
	@Column(name = "nregtypecode", nullable = false) private int nregtypecode;
	@Column(name = "nregsubtypecode", nullable = false) private int nregsubtypecode;
	
	@ColumnDefault("-1")
	@Column(name = "nmappingfieldcode", nullable = false)
	private int nmappingfieldcode = (int)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nneedalluser", nullable = false)
	private short nneedalluser = (short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String susername;
	@Transient
	private transient String sdeptname;
	@Transient
	private transient int ntransusercode;
	

	@Override
	public TransactionFilterTypeConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TransactionFilterTypeConfig objTransFilterTypeconfig = new TransactionFilterTypeConfig();
		objTransFilterTypeconfig.setNtransfiltertypeconfigcode(getInteger(arg0, "ntransfiltertypeconfigcode", arg1));
		objTransFilterTypeconfig.setNtransfiltertypecode(getInteger(arg0, "ntransfiltertypecode", arg1));
		objTransFilterTypeconfig.setNsampletypecode(getInteger(arg0, "nsampletypecode", arg1));
		objTransFilterTypeconfig.setNregtypecode(getInteger(arg0, "nregtypecode", arg1));
		objTransFilterTypeconfig.setNregsubtypecode(getInteger(arg0, "nregsubtypecode", arg1));
		objTransFilterTypeconfig.setNmappingfieldcode(getInteger(arg0, "nmappingfieldcode", arg1));
		objTransFilterTypeconfig.setNneedalluser(getShort(arg0,"nneedalluser",arg1));
		objTransFilterTypeconfig.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTransFilterTypeconfig.setNstatus(getShort(arg0,"nstatus",arg1));
		objTransFilterTypeconfig.setSusername(getString(arg0,"susername",arg1));
		objTransFilterTypeconfig.setSdeptname(getString(arg0,"sdeptname",arg1));
		objTransFilterTypeconfig.setNtransusercode(getInteger(arg0, "ntransusercode", arg1));
		return objTransFilterTypeconfig;
	}

}
