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
@Table(name = "treetemptranstestgroup")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreetempTranstestGroup extends CustomizedResultsetRowMapper<TreetempTranstestGroup>
		implements Serializable, RowMapper<TreetempTranstestGroup> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntemptranstestgroupcode")
	private int ntemptranstestgroupcode;

	@Column(name = "ntreeversiontempcode", nullable = false)
	private int ntreeversiontempcode;

	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;

	@Column(name = "ntemplatecode", nullable = false)
	private int ntemplatecode;

	@Column(name = "ntreecontrolcode", nullable = false)
	private int ntreecontrolcode;

	@Column(name = "nparentnode", nullable = false)
	private int nparentnode;

	@Column(name = "schildnode", length = 100)
	private String schildnode;

	@Column(name = "nlevelno", nullable = false)
	private short nlevelno;

	@Column(name = "slevelformat", length = 100)
	private String slevelformat;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

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
	private transient String sversiondescription;

	@Transient
	private transient boolean isreadonly;

	@Transient
	private transient String sleveldescription;

	@Transient
	private transient int ntemplatemanipulationcode;

	@Transient
	private transient String stransdisplaystatus;

	@Override
	public TreetempTranstestGroup mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TreetempTranstestGroup objTreetempTranstestGroup = new TreetempTranstestGroup();

		objTreetempTranstestGroup.setNtemptranstestgroupcode(getInteger(arg0, "ntemptranstestgroupcode", arg1));
		objTreetempTranstestGroup.setNtemplatecode(getInteger(arg0, "ntemplatecode", arg1));
		objTreetempTranstestGroup.setNtreeversiontempcode(getInteger(arg0, "ntreeversiontempcode", arg1));
		objTreetempTranstestGroup.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objTreetempTranstestGroup.setNtreecontrolcode(getInteger(arg0, "ntreecontrolcode", arg1));
		objTreetempTranstestGroup.setNparentnode(getInteger(arg0, "nparentnode", arg1));
		objTreetempTranstestGroup.setSchildnode(StringEscapeUtils.unescapeJava(getString(arg0, "schildnode", arg1)));
		objTreetempTranstestGroup.setNlevelno(getShort(arg0, "nlevelno", arg1));
		objTreetempTranstestGroup.setSlevelformat(StringEscapeUtils.unescapeJava(getString(arg0, "slevelformat", arg1)));
		objTreetempTranstestGroup.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objTreetempTranstestGroup.setSlabelname(getString(arg0, "slabelname", arg1));
		objTreetempTranstestGroup.setSversiondescription(getString(arg0, "Sversiondescription", arg1));
		objTreetempTranstestGroup.setNtreeversiontempcode(getInteger(arg0, "ntreeversiontempcode", arg1));
		objTreetempTranstestGroup.setNstatus(getShort(arg0, "nstatus", arg1));
		objTreetempTranstestGroup.setIsreadonly(getBoolean(arg0, "isreadonly", arg1));
		objTreetempTranstestGroup.setSleveldescription(getString(arg0, "sleveldescription", arg1));
		objTreetempTranstestGroup.setNtemplatemanipulationcode(getInteger(arg0, "ntemplatemanipulationcode", arg1));
		objTreetempTranstestGroup.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objTreetempTranstestGroup.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTreetempTranstestGroup.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objTreetempTranstestGroup;

	}

}
