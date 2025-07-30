/**
 * @author ATE234
 * @Date 04-04-2025
 * @time 4:20:21 PM
 */
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


/**
* This class is used to map the fields of 'usersrolescreen' table of the
* Database.
*/

@Entity
@Table(name = "usersrolescreen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UsersRoleScreen extends CustomizedResultsetRowMapper<UsersRoleScreen> implements Serializable, RowMapper<UsersRoleScreen> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nuserrolescreencode")
	private int nuserrolescreencode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String label;
	@Transient
	private transient String value;
	@Transient
	private transient Integer nsitecode;
	@Transient
	private transient int nusercode;



	@Override
	public UsersRoleScreen mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UsersRoleScreen objUsersRoleScreen = new UsersRoleScreen();
		objUsersRoleScreen.setNuserrolescreencode(getInteger(arg0,"nuserrolescreencode",arg1));
		objUsersRoleScreen.setNformcode(getShort(arg0,"nformcode",arg1));
		objUsersRoleScreen.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objUsersRoleScreen.setNstatus(getShort(arg0,"nstatus",arg1));
		objUsersRoleScreen.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objUsersRoleScreen.setLabel(getString(arg0,"label",arg1));
		objUsersRoleScreen.setValue(getString(arg0,"value",arg1));
		objUsersRoleScreen.setNsitecode(getInteger(arg0,"nsitecode",arg1));
		objUsersRoleScreen.setNusercode(getInteger(arg0,"nusercode",arg1));
		objUsersRoleScreen.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return objUsersRoleScreen;
	}

}
