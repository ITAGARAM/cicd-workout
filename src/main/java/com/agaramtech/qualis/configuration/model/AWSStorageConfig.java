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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "awsstorageconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AWSStorageConfig extends CustomizedResultsetRowMapper<AWSStorageConfig> implements Serializable, RowMapper<AWSStorageConfig> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nawsstorageconfigcode")
	private int nawsstorageconfigcode;
	
	@Column(name = "saccesskeyid", length = 50, nullable = false)
	private String saccesskeyid;
	
	@Column(name = "ssecretpasskey", length = 50, nullable = false)
	private String ssecretpasskey;
	
	@Column(name = "sbucketname")
	private String sbucketname;
	
	@Column(name = "sregion")
	private String sregion;
	
	@Column(name = "ndefaultstatus", nullable = false)
	private int ndefaultstatus;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Override
	public AWSStorageConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final AWSStorageConfig objAWSStorageConfig = new AWSStorageConfig();
		objAWSStorageConfig.setNawsstorageconfigcode(getInteger(arg0, "nawsstorageconfigcode", arg1));
		objAWSStorageConfig.setSaccesskeyid(getString(arg0, "saccesskeyid", arg1));
		objAWSStorageConfig.setSsecretpasskey(getString(arg0, "ssecretpasskey", arg1));
		objAWSStorageConfig.setSbucketname(getString(arg0, "sbucketname", arg1));
		objAWSStorageConfig.setSregion(getString(arg0, "sregion", arg1));
		objAWSStorageConfig.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objAWSStorageConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objAWSStorageConfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objAWSStorageConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		return objAWSStorageConfig;
	}
}
