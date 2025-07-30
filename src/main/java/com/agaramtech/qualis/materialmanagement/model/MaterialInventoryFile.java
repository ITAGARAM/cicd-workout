package com.agaramtech.qualis.materialmanagement.model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name="materialinventoryfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialInventoryFile extends CustomizedResultsetRowMapper<MaterialInventoryFile> implements Serializable,RowMapper<MaterialInventoryFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaterialinventoryfilecode")
	private int nmaterialinventoryfilecode;

	@Column(name = "nmaterialinventorycode", nullable = false)
	private int nmaterialinventorycode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Column(name="dtransactiondate")
	private Instant dtransactiondate;
	
	@Column(name="noffsetdtransactiondate") 
	private int noffsetdtransactiondate;
	
	@Column(name="ntztransactiondate")  
	private short ntztransactiondate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@Transient
	private transient String slinkname;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String sattachmenttype;
	
	@Transient
	private transient String screateddate;
	
	@Transient
	private transient String sfilesize;
	
	
	@Override
	public MaterialInventoryFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MaterialInventoryFile objTestFile = new MaterialInventoryFile();
		
		objTestFile.setNmaterialinventoryfilecode(getInteger(arg0,"nmaterialinventoryfilecode",arg1));
		objTestFile.setNmaterialinventorycode(getInteger(arg0,"nmaterialinventorycode",arg1));
		objTestFile.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objTestFile.setNstatus(getShort(arg0,"sfilename",arg1));
		objTestFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objTestFile.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestFile.setSattachmenttype(getString(arg0,"sattachmenttype",arg1));
		objTestFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objTestFile.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestFile.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objTestFile.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objTestFile.setNtztransactiondate(getShort(arg0,"ntztransactiondate",arg1));

		return objTestFile;
	}
	

}
