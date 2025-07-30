package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
* This class is used to map the fields of 'userrole' table of the
* Database.
*/

@Entity
@Table(name = "userrole")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRole extends CustomizedResultsetRowMapper<UserRole> implements Serializable,RowMapper<UserRole> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nuserrolecode")	
	private int nuserrolecode;
	
	@Column(name = "suserrolename", length = 100, nullable = false)	
	private String suserrolename;
	
	@Column(name = "sdescription", length = 255)
	private String sdescription="";
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int nlevelno;
	@Transient
	private transient int nparentrolecode;
	@Transient
	private transient int schildnode;
	@Transient
	private transient String sleveluserrole;

@Override
public UserRole mapRow(ResultSet arg0, int arg1) throws SQLException {
	
		final UserRole userRole = new UserRole();
		
		userRole.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		userRole.setSuserrolename(StringEscapeUtils.unescapeJava(getString(arg0,"suserrolename",arg1)));
		userRole.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		userRole.setNlevelno(getInteger(arg0,"nlevelno",arg1));
		userRole.setNparentrolecode(getInteger(arg0,"nparentrolecode",arg1));
		userRole.setSchildnode(getInteger(arg0,"schildnode",arg1));
		userRole.setSleveluserrole(getString(arg0,"sleveluserrole",arg1));
		userRole.setNsitecode(getShort(arg0,"nsitecode",arg1));
		userRole.setNstatus(getShort(arg0,"nstatus",arg1));
		userRole.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

	return userRole;
}

}
