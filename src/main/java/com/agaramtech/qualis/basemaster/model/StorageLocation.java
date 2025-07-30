package com.agaramtech.qualis.basemaster.model;

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
 * This class is used to map the fields of 'storagelocation' table of the
 * Database. 
 */
@Entity
@Table(name = "storagelocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class StorageLocation extends CustomizedResultsetRowMapper<StorageLocation> implements Serializable, RowMapper<StorageLocation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstoragelocationcode")
	private int nstoragelocationcode;

	@Column(name = "sstoragelocationname", length = 100, nullable = false)
	private String sstoragelocationname;

	@Column(name = "sdescription", length = 255)
	private String sdescription="";	
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public StorageLocation mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final StorageLocation storagelocation = new StorageLocation();
		
		storagelocation.setNstatus(getShort(arg0,"nstatus",arg1));
		storagelocation.setNstoragelocationcode(getInteger(arg0,"nstoragelocationcode",arg1));
		storagelocation.setNsitecode(getShort(arg0,"nsitecode",arg1));
		storagelocation.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		storagelocation.setSstoragelocationname(StringEscapeUtils.unescapeJava(getString(arg0,"sstoragelocationname",arg1)));
		storagelocation.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return storagelocation;
	}

}
