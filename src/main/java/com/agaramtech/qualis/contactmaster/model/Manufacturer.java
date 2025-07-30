package com.agaramtech.qualis.contactmaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Comparator;

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
@Table(name = "manufacturer")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Manufacturer extends CustomizedResultsetRowMapper<Manufacturer>
		implements Serializable, RowMapper<Manufacturer>, Comparator<Manufacturer> {


	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nmanufcode")
	private int nmanufcode;
	
	@Column(name = "smanufname", length = 100, nullable = false)
	private String smanufname = "";
	
	@Column(name = "sdescription", length = 255)
	private String sdescription;
	
	@ColumnDefault("1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Transient
	private transient String smanufsitename = "";
	
	@Transient
	private transient int nmanufsitecode = -1;
	
	@Transient
	private transient String sofficialmanufname = "";
	
	@Transient
	private transient String stransdisplaystatus = "";
	
	@Transient
	private transient String sproductgroupname = "";
	
	@Transient
	private transient int nproductmanufcode = -1;
	
	@Transient
	private transient String scontactname = "";
	
	@Transient
	private transient int ncountrycode = -1;
	
	@Transient
	private transient String seprotocolname = "";

	@Override
	public Manufacturer mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Manufacturer manufacturer = new Manufacturer();
		manufacturer.setNmanufcode(getInteger(arg0, "nmanufcode", arg1));
		manufacturer.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		manufacturer.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		manufacturer.setSmanufname(StringEscapeUtils.unescapeJava(getString(arg0, "smanufname", arg1)));
		manufacturer.setNsitecode(getShort(arg0, "nsitecode", arg1));
		manufacturer.setNstatus(getShort(arg0, "nstatus", arg1));
		manufacturer.setSmanufname(getString(arg0, "smanufname", arg1));
		manufacturer.setSofficialmanufname(getString(arg0, "sofficialmanufname", arg1));
		manufacturer.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		manufacturer.setSmanufsitename(getString(arg0, "smanufsitename", arg1));
		manufacturer.setNmanufsitecode(getInteger(arg0, "nmanufsitecode", arg1));
		manufacturer.setNproductmanufcode(getInteger(arg0, "nproductmanufcode", arg1));
		manufacturer.setSproductgroupname(getString(arg0, "sproductgroupname", arg1));
		manufacturer.setSeprotocolname(getString(arg0, "seprotocolname", arg1));
		manufacturer.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		manufacturer.setScontactname(getString(arg0, "scontactname", arg1));
		manufacturer.setNcountrycode(getInteger(arg0, "ncountrycode", arg1));
		return manufacturer;
	}

	@Override
	public int compare(Manufacturer arg0, Manufacturer arg1) {
		return Integer.valueOf(arg0.nmanufcode).compareTo(arg1.nmanufcode);
	}

}
