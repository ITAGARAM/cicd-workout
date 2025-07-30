package com.agaramtech.qualis.configuration.model;

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

@Entity
@Table(name = "adsusers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ADSUsers extends CustomizedResultsetRowMapper<ADSUsers> implements Serializable, RowMapper<ADSUsers> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nadsusercode")
	private int nadsusercode;

	@Column(name = "sloginid", length = 20, nullable = false)
	private String sloginid;

	@Column(name = "necno")
	private int necno;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String suserid;

	@Transient
	private transient String spassword;

	@Override
	public ADSUsers mapRow(ResultSet arg0, int arg1) throws SQLException {

		ADSUsers objADSUsers = new ADSUsers();
		objADSUsers.setNadsusercode(getInteger(arg0, "nadsusercode", arg1));
		objADSUsers.setSloginid(getString(arg0, "sloginid", arg1));
		objADSUsers.setSpassword(getString(arg0, "spassword", arg1));
		objADSUsers.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objADSUsers.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objADSUsers.setNstatus(getShort(arg0, "nstatus", arg1));
		objADSUsers.setSuserid(getString(arg0, "suserid", arg1));
		objADSUsers.setNecno(getInteger(arg0, "necno", arg1));

		return objADSUsers;
	}

}
