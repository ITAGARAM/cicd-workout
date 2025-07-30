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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "treeversiontemplate")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreeVersionTemplate extends CustomizedResultsetRowMapper<TreeVersionTemplate>
		implements Serializable, RowMapper<TreeVersionTemplate> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntreeversiontempcode")
	private int ntreeversiontempcode;

	@Column(name = "napprovalconfigcode", nullable = false)
	private short napprovalconfigcode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "ntemplatecode", nullable = false)
	private int ntemplatecode;

	@Column(name = "nversionno", nullable = false)
	private int nversionno;

	@Column(name = "sversiondescription", length = 100)
	private String sversiondescription;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String sversionstatus;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient int ntemptransrolecode;
	@Transient
	private transient String sleveluserrole;
	@Transient
	private transient String nparentnode;
	@Transient
	private transient int nuserrolecode;
	@Transient
	private transient int nlevelno;
	@Transient
	private transient String slabelname;
	@Transient
	private transient int ntreecontrolcode;
	@Transient
	private transient int ntemptranstestgroupcode;

	@Override
	public TreeVersionTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TreeVersionTemplate objTreeVersionTemplate = new TreeVersionTemplate();
		objTreeVersionTemplate.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objTreeVersionTemplate.setSversiondescription(StringEscapeUtils.unescapeJava(getString(arg0, "sversiondescription", arg1)));
		objTreeVersionTemplate.setNversionno(getInteger(arg0, "nversionno", arg1));
		objTreeVersionTemplate.setSversionstatus(getString(arg0, "sversionstatus", arg1));
		objTreeVersionTemplate.setNtemplatecode(getInteger(arg0, "ntemplatecode", arg1));
		objTreeVersionTemplate.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTreeVersionTemplate.setNtreeversiontempcode(getInteger(arg0, "ntreeversiontempcode", arg1));
		objTreeVersionTemplate.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objTreeVersionTemplate.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		objTreeVersionTemplate.setNtemptransrolecode(getInteger(arg0, "ntemptransrolecode", arg1));
		objTreeVersionTemplate.setSleveluserrole(getString(arg0, "sleveluserrole", arg1));
		objTreeVersionTemplate.setNparentnode(getString(arg0, "nparentnode", arg1));
		objTreeVersionTemplate.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objTreeVersionTemplate.setNlevelno(getInteger(arg0, "nlevelno", arg1));
		objTreeVersionTemplate.setSlabelname(getString(arg0, "slabelname", arg1));
		objTreeVersionTemplate.setNtreecontrolcode(getInteger(arg0, "ntreecontrolcode", arg1));
		objTreeVersionTemplate.setNtemptranstestgroupcode(getInteger(arg0, "ntemptranstestgroupcode", arg1));
		objTreeVersionTemplate.setNstatus(getShort(arg0, "nstatus", arg1));
		objTreeVersionTemplate.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objTreeVersionTemplate.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return objTreeVersionTemplate;
	}

}
