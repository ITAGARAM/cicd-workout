package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.storagemanagement.model.StorageCategory;

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
@Table(name = "usersrolescreenhide")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UsersRoleScreenHide extends CustomizedResultsetRowMapper<UsersRoleScreenHide> implements Serializable,RowMapper<UsersRoleScreenHide>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="nusersrolehidescreencode")private int nusersrolehidescreencode;
	@Column(name="nformcode", nullable=false)private short nformcode;
	@Column(name="nusercode", nullable=false)private int nusercode;
	@Column(name="nuserrolecode", nullable=false)private int nuserrolecode;
	@Column(name="needrights", nullable=false)private short needrights;
	@Column(name = "dmodifieddate", nullable = false)private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sdisplayname; 
	@Transient
	private transient String label;
	@Transient
	private transient String value;
	@Transient
	private transient Integer nmenucode;
	
	
	public UsersRoleScreenHide mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UsersRoleScreenHide objUsersRoleScreenHide = new UsersRoleScreenHide();
		objUsersRoleScreenHide.setNusersrolehidescreencode(getInteger(arg0,"nusersrolehidescreencode",arg1));
		objUsersRoleScreenHide.setNformcode(getShort(arg0,"nformcode",arg1));
		objUsersRoleScreenHide.setNusercode(getInteger(arg0,"nusercode",arg1));
		objUsersRoleScreenHide.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objUsersRoleScreenHide.setNeedrights(getShort(arg0,"needrights",arg1));
		objUsersRoleScreenHide.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objUsersRoleScreenHide.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objUsersRoleScreenHide.setNstatus(getShort(arg0,"nstatus",arg1));
		objUsersRoleScreenHide.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objUsersRoleScreenHide.setLabel(getString(arg0,"label",arg1));
		objUsersRoleScreenHide.setValue(getString(arg0,"value",arg1));
		objUsersRoleScreenHide.setNmenucode(getInteger(arg0,"nmenucode",arg1));
		return objUsersRoleScreenHide;
	}
}
