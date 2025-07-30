package com.agaramtech.qualis.credential.model;

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
@Table(name = "userroleconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRoleConfig extends CustomizedResultsetRowMapper<UserRoleConfig> implements Serializable, RowMapper<UserRoleConfig> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nuserrolecode")	private int nuserrolecode;
	
	@ColumnDefault("4")
	@Column(name = "nneedapprovalflow", nullable = false)
	private short nneedapprovalflow = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nneedresultflow", nullable = false)
	private short nneedresultflow = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nneedprojectflow", nullable = false)
	private short nneedprojectflow = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate")private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;

	@Transient
	private transient String suserrolename;

	

	@Override
	public UserRoleConfig mapRow(ResultSet arg0,int arg1) throws SQLException {
		final UserRoleConfig objUserRoleConfig = new UserRoleConfig();
		objUserRoleConfig.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objUserRoleConfig.setNneedapprovalflow(getShort(arg0,"nneedapprovalflow",arg1));
		objUserRoleConfig.setNneedresultflow(getShort(arg0,"nneedresultflow",arg1));
		objUserRoleConfig.setNneedprojectflow(getShort(arg0,"nneedprojectflow",arg1));
		objUserRoleConfig.setNstatus(getShort(arg0,"nstatus",arg1));
		objUserRoleConfig.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objUserRoleConfig.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objUserRoleConfig.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objUserRoleConfig;
	}

}
