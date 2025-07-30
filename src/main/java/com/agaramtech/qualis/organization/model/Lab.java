package com.agaramtech.qualis.organization.model;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is used to map the fields of Lab table of the Database.
 */
@Entity
@Table(name = "lab")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
public class Lab extends CustomizedResultsetRowMapper<Lab> implements Serializable, RowMapper<Lab> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nlabcode")
	private int nlabcode;
	@Column(name = "slabname", length = 100, nullable = false)
	private String slabname = "";
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public Lab mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Lab lab = new Lab();
		lab.setNstatus(getShort(arg0, "nstatus", arg1));
		lab.setNlabcode(getInteger(arg0, "nlabcode", arg1));
		lab.setNsitecode(getShort(arg0, "nsitecode", arg1));
		lab.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		lab.setSlabname(StringEscapeUtils.unescapeJava(getString(arg0, "slabname", arg1)));
		lab.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return lab;
	}
}
