package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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


@Entity
@Table(name = "approvalstatusconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApprovalStatusConfig extends CustomizedResultsetRowMapper<ApprovalStatusConfig> implements Serializable, RowMapper<ApprovalStatusConfig> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstatusconfigcode")
	private short nstatusconfigcode;

	@Column(name = "nstatusfunctioncode", nullable = false)
	private short nstatusfunctioncode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "napprovalsubtypecode", nullable = false)
	private short napprovalsubtypecode;

	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;

	@Column(name = "nregsubtypecode", nullable = false)
	private short nregsubtypecode;

	@Column(name = "ntranscode", nullable = false)
	private short ntranscode;

	@Column(name = "nsorter", nullable = false)
	private short nsorter;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus;
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String sstatusfunction;
	@Transient
	private transient String sformname;
	@Transient
	private transient String sdisplayapprovalsubtype;
	@Transient
	private transient String sdisplayregtype;
	@Transient
	private transient String sdisplayregsubtype;
	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String ssampletypename;
	@Transient
	private transient String sapprovalconfig;
	@Transient
	private transient String label;
	@Transient
	private transient String value;
	@Transient
	private transient int nsampletypecode;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sregtypename;
	@Transient
	private transient String sapprovalstatusfunctions;

	@Override
	public ApprovalStatusConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final ApprovalStatusConfig objApproveconfVersion = new ApprovalStatusConfig();

		objApproveconfVersion.setNstatusconfigcode(getShort(arg0, "nstatusconfigcode", arg1));
		objApproveconfVersion.setNstatusfunctioncode(getShort(arg0, "nstatusfunctioncode", arg1));
		objApproveconfVersion.setNformcode(getShort(arg0, "nformcode", arg1));
		objApproveconfVersion.setNapprovalsubtypecode(getShort(arg0, "napprovalsubtypecode", arg1));
		objApproveconfVersion.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objApproveconfVersion.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		objApproveconfVersion.setNtranscode(getShort(arg0, "ntranscode", arg1));
		objApproveconfVersion.setNsorter(getShort(arg0, "nsorter", arg1));
		objApproveconfVersion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objApproveconfVersion.setNstatus(getShort(arg0, "nstatus", arg1));
		objApproveconfVersion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objApproveconfVersion.setSdisplayapprovalsubtype(getString(arg0, "sdisplayapprovalsubtype", arg1));
		objApproveconfVersion.setSdisplayregtype(getString(arg0, "sdisplayregtype", arg1));
		objApproveconfVersion.setSdisplayregsubtype(getString(arg0, "sdisplayregsubtype", arg1));
		objApproveconfVersion.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		objApproveconfVersion.setSstatusfunction(getString(arg0, "sstatusfunction", arg1));
		objApproveconfVersion.setSformname(getString(arg0, "sformname", arg1));
		objApproveconfVersion.setSsampletypename(getString(arg0, "ssampletypename", arg1));
		objApproveconfVersion.setSapprovalconfig(getString(arg0, "sapprovalconfig", arg1));
		objApproveconfVersion.setLabel(getString(arg0, "label", arg1));
		objApproveconfVersion.setValue(getString(arg0, "value", arg1));
		objApproveconfVersion.setNsampletypecode(getInteger(arg0, "nsampletypecode", arg1));
		objApproveconfVersion.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objApproveconfVersion.setSregtypename(getString(arg0, "sregtypename", arg1));
		objApproveconfVersion.setSapprovalstatusfunctions(getString(arg0, "sapprovalstatusfunctions", arg1));


		return objApproveconfVersion;
	}

//	 
}
