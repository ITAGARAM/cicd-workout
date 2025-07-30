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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'department' table of the Database.
 * 
 */

@Entity
@Table(name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Department extends CustomizedResultsetRowMapper<Department> implements Serializable, RowMapper<Department> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndeptcode")
	private int ndeptcode;

	@Column(name = "sdeptname", length = 100, nullable = false)
	private String sdeptname;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplaystatus;

	@Override
	public Department mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Department department = new Department();
		department.setNdeptcode(getInteger(arg0, "ndeptcode", arg1));
		department.setSdeptname(StringEscapeUtils.unescapeJava(getString(arg0, "sdeptname", arg1)));
		department.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		department.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		department.setNsitecode(getShort(arg0, "nsitecode", arg1));
		department.setNstatus(getShort(arg0, "nstatus", arg1));
		department.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		department.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return department;
	}
}
