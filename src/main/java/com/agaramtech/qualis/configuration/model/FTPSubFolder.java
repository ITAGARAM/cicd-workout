package com.agaramtech.qualis.configuration.model;

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
 * This class is used to map the fields of 'ftpsubfolder' table of the Database.
 */
@Entity
@Table(name = "ftpsubfolder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FTPSubFolder extends CustomizedResultsetRowMapper<FTPSubFolder> implements Serializable, RowMapper<FTPSubFolder> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nftpsubfoldercode", nullable = false)
	private short nftpsubfoldercode;
	
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	
	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;
	
	@Column(name = "ssubfoldername", length = 100)
	private String ssubfoldername;
	
	@Column(name = "sdescription", length = 100)
	private String sdescription;

	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	

	@Override
	public FTPSubFolder mapRow(ResultSet arg0, int arg1) throws SQLException {
		final FTPSubFolder objFTPSubFolder = new FTPSubFolder();
		
		objFTPSubFolder.setNftpsubfoldercode(getShort(arg0, "nftpsubfoldercode", arg1));
		objFTPSubFolder.setNformcode(getShort(arg0, "nformcode", arg1));
		objFTPSubFolder.setNcontrolcode(getShort(arg0, "ncontrolcode", arg1));
		objFTPSubFolder.setSsubfoldername(StringEscapeUtils.unescapeJava(getString(arg0, "ssubfoldername", arg1)));
		objFTPSubFolder.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objFTPSubFolder.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objFTPSubFolder.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objFTPSubFolder.setNstatus(getShort(arg0, "nstatus", arg1));		
		return objFTPSubFolder;
	}

}
