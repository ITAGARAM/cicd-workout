package com.agaramtech.qualis.contactmaster.model;

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
@Data
@Table(name = "manufacturersiteaddress")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ManufacturerSiteAddress extends CustomizedResultsetRowMapper<ManufacturerSiteAddress>
		implements Serializable, RowMapper<ManufacturerSiteAddress> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmanufsitecode")
	private int nmanufsitecode;
	
	@Column(name = "nmanufcode", nullable = false)
	private int nmanufcode;
	
	@Column(name = "ncountrycode", nullable = false)
	private int ncountrycode;
	
	@Column(name = "smanufsitename", length = 100, nullable = false)
	private String smanufsitename = "";
	
	@Column(name = "saddress1", length = 255, nullable = false)
	private String saddress1 = "";
	
	@Column(name = "saddress2", length = 255)
	private String saddress2 = "";
	
	@Column(name = "saddress3", length = 255)
	private String saddress3 = "";
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus =(short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String scountryname = "";
	
	@Transient
	private transient String transactionstatus = "";
	
	@Transient
	private transient String defaultstatus = "";
	
	@Transient
	private transient String stransdisplaystatus = "";
	
	@Transient
	private transient boolean isreadonly;
	
	@Transient
	private transient String scontactname = "";

	@Override
	public ManufacturerSiteAddress mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ManufacturerSiteAddress objManufacturerSiteAddress = new ManufacturerSiteAddress();
		objManufacturerSiteAddress.setScountryname(getString(arg0, "scountryname", arg1));
		objManufacturerSiteAddress.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		objManufacturerSiteAddress.setNstatus(getShort(arg0, "nstatus", arg1));
		objManufacturerSiteAddress.setSaddress2(StringEscapeUtils.unescapeJava(getString(arg0, "saddress2", arg1)));
		objManufacturerSiteAddress.setTransactionstatus(getString(arg0, "transactionstatus", arg1));
		objManufacturerSiteAddress.setSaddress1(StringEscapeUtils.unescapeJava(getString(arg0, "saddress1", arg1)));
		objManufacturerSiteAddress.setSaddress3(StringEscapeUtils.unescapeJava(getString(arg0, "saddress3", arg1)));
		objManufacturerSiteAddress.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objManufacturerSiteAddress.setSmanufsitename(StringEscapeUtils.unescapeJava(getString(arg0, "smanufsitename", arg1)));
		objManufacturerSiteAddress.setDefaultstatus(getString(arg0, "defaultstatus", arg1));
		objManufacturerSiteAddress.setNmanufcode(getInteger(arg0, "nmanufcode", arg1));
		objManufacturerSiteAddress.setNmanufsitecode(getInteger(arg0, "nmanufsitecode", arg1));
		objManufacturerSiteAddress.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objManufacturerSiteAddress.setIsreadonly(getBoolean(arg0, "isreadonly", arg1));
		objManufacturerSiteAddress.setScontactname(getString(arg0, "scontactname", arg1));
		objManufacturerSiteAddress.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objManufacturerSiteAddress.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objManufacturerSiteAddress;
	}
}
