package com.agaramtech.qualis.submitter.model;

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
 * This class is used to map the fields of 'institutiondepartment' table of the
 * Database.
 */
@Entity
@Table(name = "institutiondepartment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstitutionDepartment extends CustomizedResultsetRowMapper<InstitutionDepartment>
		implements Serializable, RowMapper<InstitutionDepartment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstitutiondeptcode")
	private int ninstitutiondeptcode;

	@Column(name = "sinstitutiondeptname", length = 100, nullable = false)
	private String sinstitutiondeptname;

	@Column(name = "sinstitutiondeptcode", length = 5, nullable = false)
	private String sinstitutiondeptcode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	@Transient
	private transient String smodifieddate;

	@Override
	public InstitutionDepartment mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InstitutionDepartment institutionDeptartment = new InstitutionDepartment();
		institutionDeptartment.setNstatus(getShort(arg0, "nstatus", arg1));
		institutionDeptartment.setNinstitutiondeptcode(getInteger(arg0, "ninstitutiondeptcode", arg1));
		institutionDeptartment.setNsitecode(getShort(arg0, "nsitecode", arg1));
		institutionDeptartment.setSinstitutiondeptcode(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutiondeptcode", arg1)));
		institutionDeptartment.setSinstitutiondeptname(StringEscapeUtils.unescapeJava(getString(arg0, "sinstitutiondeptname", arg1)));
		institutionDeptartment.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		institutionDeptartment.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		institutionDeptartment.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return institutionDeptartment;
	}

}
