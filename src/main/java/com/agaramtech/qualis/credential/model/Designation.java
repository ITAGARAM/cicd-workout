package com.agaramtech.qualis.credential.model;

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
@Table(name = "designation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Designation extends CustomizedResultsetRowMapper<Designation> implements Serializable, RowMapper<Designation> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ndesignationcode")
	private int ndesignationcode;
	@Column(name = "sdesignationname", length = 100)
	private String sdesignationname = "";
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();
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

	@Override
	public Designation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Designation designation = new Designation();
		designation.setNstatus(getShort(arg0, "nstatus", arg1));
		designation.setNdesignationcode(getInteger(arg0, "ndesignationcode", arg1));
		designation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		designation.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		designation.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		designation.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		designation.setSdesignationname(StringEscapeUtils.unescapeJava(getString(arg0, "sdesignationname", arg1)));
		designation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return designation;
	}

}
