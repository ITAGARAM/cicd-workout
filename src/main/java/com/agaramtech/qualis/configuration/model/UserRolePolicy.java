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
@Table(name = "userrolepolicy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRolePolicy extends CustomizedResultsetRowMapper<UserRolePolicy> implements Serializable, RowMapper<UserRolePolicy> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nuserrolepolicycode")
	private int nuserrolepolicycode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "npolicycode", nullable = false)
	private int npolicycode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Transient
	private transient String suserrolename;

	@Override
	public UserRolePolicy mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UserRolePolicy objUserRolePolicy = new UserRolePolicy();
		objUserRolePolicy.setNuserrolepolicycode(getInteger(arg0, "nuserrolepolicycode", arg1));
		objUserRolePolicy.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objUserRolePolicy.setNpolicycode(getInteger(arg0, "npolicycode", arg1));
		objUserRolePolicy.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objUserRolePolicy.setNstatus(getShort(arg0, "nstatus", arg1));
		objUserRolePolicy.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objUserRolePolicy.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objUserRolePolicy.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objUserRolePolicy;
	}

}
