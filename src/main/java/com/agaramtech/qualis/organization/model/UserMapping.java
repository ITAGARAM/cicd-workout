package com.agaramtech.qualis.organization.model;

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
 * This Class is used to map the columns in usermapping table
 * 
 * @author ATE169
 * @version 9.0.0.1
 */
@Entity
@Table(name = "usermapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class UserMapping extends CustomizedResultsetRowMapper<UserMapping> implements Serializable, RowMapper<UserMapping> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusermappingcode")
	private int nusermappingcode;

	@Column(name = "nversioncode", nullable = false)
	private int nversioncode;

	@Column(name = "napprovalconfigcode", nullable = false)
	private short napprovalconfigcode;

	@Column(name = "nparentrolecode", nullable = false)
	private int nparentrolecode;

	@Column(name = "nparentusercode", nullable = false)
	private int nparentusercode;

	@Column(name = "nparentusersitecode", nullable = false)
	private int nparentusersitecode;

	@Column(name = "nchildrolecode", nullable = false)
	private int nchildrolecode;

	@Column(name = "nchildusercode", nullable = false)
	private int nchildusercode;

	@Column(name = "nchildusersitecode", nullable = false)
	private int nchildusersitecode;

	@Column(name = "nparusermappingcode", nullable = false)
	private int nparusermappingcode;

	@Column(name = "nlevel", nullable = false)
	private short nlevel;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sparentuserrolename;
	
	@Transient
	private transient String sparentusername;
	
	@Transient
	private transient String schilduserrolename;
	
	@Transient
	private transient String schildusername;
	
	@Transient
	private transient int nregtypecode;
	
	@Transient
	private transient int nregsubtypecode;

	public UserMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final UserMapping objUserMapping = new UserMapping();
		
		objUserMapping.setNusermappingcode(getInteger(arg0, "nusermappingcode", arg1));
		objUserMapping.setNparusermappingcode(getInteger(arg0, "nparusermappingcode", arg1));
		objUserMapping.setNversioncode(getInteger(arg0, "nversioncode", arg1));
		objUserMapping.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		objUserMapping.setNparentrolecode(getInteger(arg0, "nparentrolecode", arg1));
		objUserMapping.setSparentuserrolename(getString(arg0, "sparentuserrolename", arg1));
		objUserMapping.setNparentusercode(getInteger(arg0, "nparentusercode", arg1));
		objUserMapping.setSparentusername(getString(arg0, "sparentusername", arg1));
		objUserMapping.setNparentusersitecode(getInteger(arg0, "nparentusersitecode", arg1));
		objUserMapping.setNchildrolecode(getInteger(arg0, "nchildrolecode", arg1));
		objUserMapping.setSchilduserrolename(getString(arg0, "schilduserrolename", arg1));
		objUserMapping.setNchildusercode(getInteger(arg0, "nchildusercode", arg1));
		objUserMapping.setSchildusername(getString(arg0, "schildusername", arg1));
		objUserMapping.setNchildusersitecode(getInteger(arg0, "nchildusersitecode", arg1));
		objUserMapping.setNparusermappingcode(getInteger(arg0, "nparusermappingcode", arg1));
		objUserMapping.setNlevel(getShort(arg0, "nlevel", arg1));
		objUserMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objUserMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		objUserMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objUserMapping.setNregtypecode(getInteger(arg0, "nregtypecode", arg1));
		objUserMapping.setNregsubtypecode(getInteger(arg0, "nregsubtypecode", arg1));

		return objUserMapping;
	}

}
