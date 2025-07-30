package com.agaramtech.qualis.storagemanagement.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'storagecategory' table of the Database.
 */

@Entity
@Table(name = "storagecategory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StorageCategory extends CustomizedResultsetRowMapper<StorageCategory> implements Serializable,RowMapper<StorageCategory> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstoragecategorycode")
	private int nstoragecategorycode;

	@Column(name = "sstoragecategoryname", length=100, nullable = false)
	private String sstoragecategoryname;
	
	@Column(name = "sdescription",length=255)
	private String sdescription="";	

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@ColumnDefault("-1")
	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode=Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	
	@Override
	public StorageCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		
		final StorageCategory objStorageCategory = new StorageCategory();	
		
		objStorageCategory.setNstoragecategorycode(getInteger(arg0,"nstoragecategorycode",arg1));
		objStorageCategory.setSstoragecategoryname(StringEscapeUtils.unescapeJava(getString(arg0,"sstoragecategoryname",arg1)));
		objStorageCategory.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objStorageCategory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objStorageCategory.setNsitecode(getShort(arg0,"nsitecode",arg1));		
		objStorageCategory.setNstatus(getShort(arg0,"nstatus",arg1));	
		objStorageCategory.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		
		return objStorageCategory;
	}

}
