package com.agaramtech.qualis.login.model;

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
 * This class is used to map the fields of 'usermfa' table of the Database.
 */
@Entity
@Table(name = "usermfa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserMFA extends CustomizedResultsetRowMapper<UserMFA> implements Serializable,RowMapper<UserMFA> {

	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@Column(name = "nmfatypecode", nullable = false)
	private int nmfatypecode;
	
	@ColumnDefault("3")
	@Column(name = "nactivestatus", nullable = false)
	private short nactivestatus=(short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String smfatypename;
	
	@Transient
	private transient String sreceivermailid;
	
	@Override
	public UserMFA mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final UserMFA objUserMfa= new UserMFA();
		
		objUserMfa.setNusercode(getInteger(arg0,"nusercode",arg1));
		objUserMfa.setNmfatypecode(getInteger(arg0,"nmfatypecode",arg1));
		objUserMfa.setNactivestatus(getShort(arg0,"nactivestatus",arg1));
		objUserMfa.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objUserMfa.setNstatus(getShort(arg0,"nstatus",arg1));
		objUserMfa.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objUserMfa.setSmfatypename(getString(arg0,"smfatypename",arg1));
		objUserMfa.setSreceivermailid(getString(arg0,"sreceivermailid",arg1));
		
		return objUserMfa;
	}

}