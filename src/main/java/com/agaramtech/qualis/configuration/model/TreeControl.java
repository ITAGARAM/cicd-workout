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

@Entity
@Table(name = "treecontrol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TreeControl extends CustomizedResultsetRowMapper<TreeControl> implements Serializable, RowMapper<TreeControl> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntreecontrolcode")
	private int ntreecontrolcode;

	@Column(name = "slabelname", length = 50)
	private String slabelname;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public TreeControl mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TreeControl objTreeControl = new TreeControl();
		objTreeControl.setNtreecontrolcode(getInteger(arg0, "ntreecontrolcode", arg1));
		objTreeControl.setSlabelname(StringEscapeUtils.unescapeJava(getString(arg0, "slabelname", arg1)));
		objTreeControl.setNstatus(getShort(arg0, "nstatus", arg1));
		objTreeControl.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objTreeControl.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objTreeControl;
	}
}
