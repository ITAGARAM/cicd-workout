package com.agaramtech.qualis.credential.model;

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

/**
 * This class is used to map fields of 'sitecontrolmaster' table of database
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 04-04-2025
 */

@Entity
@Table(name = "sitecontrolmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SiteControlMaster extends CustomizedResultsetRowMapper<SiteControlMaster>
		implements Serializable, RowMapper<SiteControlMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitecontrolcode")
	private short nsitecontrolcode;

	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;

	@Column(name = "nneedesign", nullable = false)
	private short nneedesign = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nisbarcodecontrol", nullable = false)
	private short nisbarcodecontrol = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nisreportcontrol", nullable = false)
	private short nisreportcontrol = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nisemailrequired", nullable = false)
	private short nisemailrequired = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient String scontrolids;

	@Transient
	private transient String scontrolname;

	@Override
	public SiteControlMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SiteControlMaster objSiteControlMaster = new SiteControlMaster();
		objSiteControlMaster.setNsitecontrolcode(getShort(arg0, "nsitecontrolcode", arg1));
		objSiteControlMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSiteControlMaster.setNformcode(getShort(arg0, "nformcode", arg1));
		objSiteControlMaster.setNcontrolcode(getShort(arg0, "ncontrolcode", arg1));
		objSiteControlMaster.setNneedesign(getShort(arg0, "nneedesign", arg1));
		objSiteControlMaster.setNisbarcodecontrol(getShort(arg0, "nisbarcodecontrol", arg1));
		objSiteControlMaster.setNisreportcontrol(getShort(arg0, "nisreportcontrol", arg1));
		objSiteControlMaster.setNisemailrequired(getShort(arg0, "nisemailrequired", arg1));
		objSiteControlMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objSiteControlMaster.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objSiteControlMaster.setScontrolids(getString(arg0, "scontrolids", arg1));
		objSiteControlMaster.setScontrolname(getString(arg0, "scontrolname", arg1));
		objSiteControlMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objSiteControlMaster;
	}

}
