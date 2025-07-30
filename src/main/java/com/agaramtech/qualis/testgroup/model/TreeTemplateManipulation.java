package com.agaramtech.qualis.testgroup.model;

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
@Table(name = "treetemplatemanipulation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreeTemplateManipulation extends CustomizedResultsetRowMapper<TreeTemplateManipulation>
		implements Serializable, RowMapper<TreeTemplateManipulation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntemplatemanipulationcode")
	private int ntemplatemanipulationcode;

	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "ntreeversiontempcode", nullable = false)
	private int ntreeversiontempcode;

	@Column(name = "ntemptranstestgroupcode", nullable = false)
	private int ntemptranstestgroupcode;

	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode;

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

	@ColumnDefault("-1")
	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "slevelcode", length = 100)
	private String slevelcode="";

	@Column(name = "sleveldescription", length = 100)
	private String sleveldescription="";

	@Column(name = "nparentnode", nullable = false)
	private int nparentnode;

	@Column(name = "schildnode", length = 100)
	private String schildnode="";

	@Column(name = "nnextchildcode", nullable = false)
	private short nnextchildcode;

	@Column(name = "npositioncode", nullable = false)
	private short npositioncode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String slabelname;

	@Transient
	private transient boolean isreadonly;

	@Transient
	private transient String slevelformat;

	@Transient
	private transient int nallottedspeccode;

	@Transient
	private transient String sspecname;

	@Transient
	private transient int napprovalstatus;

	@Override
	public TreeTemplateManipulation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TreeTemplateManipulation objTreeTemplateManipulation = new TreeTemplateManipulation();
		objTreeTemplateManipulation.setNtemplatemanipulationcode(getInteger(arg0, "ntemplatemanipulationcode", arg1));
		objTreeTemplateManipulation.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objTreeTemplateManipulation.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objTreeTemplateManipulation.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objTreeTemplateManipulation.setNformcode(getShort(arg0, "nformcode", arg1));
		objTreeTemplateManipulation.setNtreeversiontempcode(getInteger(arg0, "ntreeversiontempcode", arg1));
		objTreeTemplateManipulation.setNtemptranstestgroupcode(getInteger(arg0, "ntemptranstestgroupcode", arg1));
		objTreeTemplateManipulation.setSlevelcode(StringEscapeUtils.unescapeJava(getString(arg0, "slevelcode", arg1)));
		objTreeTemplateManipulation.setSleveldescription(StringEscapeUtils.unescapeJava(getString(arg0, "sleveldescription", arg1)));
		objTreeTemplateManipulation.setNparentnode(getInteger(arg0, "nparentnode", arg1));
		objTreeTemplateManipulation.setSchildnode(StringEscapeUtils.unescapeJava(getString(arg0, "schildnode", arg1)));
		objTreeTemplateManipulation.setNnextchildcode(getShort(arg0, "nnextchildcode", arg1));
		objTreeTemplateManipulation.setNpositioncode(getShort(arg0, "npositioncode", arg1));
		objTreeTemplateManipulation.setNstatus(getShort(arg0, "nstatus", arg1));
		objTreeTemplateManipulation.setSlabelname(getString(arg0, "slabelname", arg1));
		objTreeTemplateManipulation.setIsreadonly(getBoolean(arg0, "isreadonly", arg1));
		objTreeTemplateManipulation.setSlevelformat(getString(arg0, "slevelformat", arg1));
		objTreeTemplateManipulation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTreeTemplateManipulation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objTreeTemplateManipulation.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objTreeTemplateManipulation.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objTreeTemplateManipulation.setSspecname(getString(arg0, "sspecname", arg1));
		objTreeTemplateManipulation.setNapprovalstatus(getInteger(arg0, "napprovalstatus", arg1));
		return objTreeTemplateManipulation;
	}
}
