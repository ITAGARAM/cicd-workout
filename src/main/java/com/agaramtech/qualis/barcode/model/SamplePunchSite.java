package com.agaramtech.qualis.barcode.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'SamplePunchSite' table of the
 * Database.
 */
@Entity
@Table(name = "samplepunchsite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SamplePunchSite extends CustomizedResultsetRowMapper<SamplePunchSite>
		implements Serializable, RowMapper<SamplePunchSite> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsamplepunchsitecode")
	private int nsamplepunchsitecode;
	
	@Column(name = "nsamplecollectiontypecode")
	private int nsamplecollectiontypecode;
	
	@Column(name = "spunchdescription", length = 100, nullable = false)
	private String spunchdescription;
	
	@Column(name = "ncode", nullable = false)
	private int ncode;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = 1;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;
	
	@Transient
	private transient String sprojecttypename;
	
	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient String sproductname;
	
	@Transient
	private transient boolean ispunchdescription;
	
	@Transient
	private transient boolean iscode;
	
	@Transient
	private transient int nproductcode;

	@Override
	public SamplePunchSite mapRow(ResultSet arg0, int arg1) throws SQLException {
		SamplePunchSite samplePunchSite = new SamplePunchSite();
		samplePunchSite.setNsamplepunchsitecode(getInteger(arg0, "nsamplepunchsitecode", arg1));
		samplePunchSite.setNsamplecollectiontypecode(getInteger(arg0, "nsamplecollectiontypecode", arg1));
		samplePunchSite.setSpunchdescription(StringEscapeUtils.unescapeJava(getString(arg0, "spunchdescription", arg1)));
		samplePunchSite.setNcode(getInteger(arg0, "ncode", arg1));
		samplePunchSite.setNsitecode(getShort(arg0, "nsitecode", arg1));
		samplePunchSite.setNstatus(getShort(arg0, "nstatus", arg1));
		samplePunchSite.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		samplePunchSite.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		samplePunchSite.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		samplePunchSite.setSproductname(getString(arg0, "sproductname", arg1));
		samplePunchSite.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		samplePunchSite.setIspunchdescription(getBoolean(arg0, "ispunchdescription", arg1));
		samplePunchSite.setIscode(getBoolean(arg0, "iscode", arg1));
		samplePunchSite.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		return samplePunchSite;
	}
}
