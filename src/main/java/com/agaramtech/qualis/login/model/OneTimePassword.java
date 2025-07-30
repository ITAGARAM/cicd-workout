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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'unit' table of the Database.
 */
@Entity
@Table(name = "onetimepassword")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OneTimePassword extends CustomizedResultsetRowMapper<OneTimePassword> implements Serializable,RowMapper<OneTimePassword> {

	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "sonetimepasswordcode", length=100, nullable = false)
	private String sonetimepasswordcode;
	
	@Column(name = "sonetimepassword", length=10, nullable = false)
	private String sonetimepassword;
	
	@Column(name = "dcreateddate", nullable = false)
	private Instant dcreateddate;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	private transient String screateddate;
	
	@Override
	public OneTimePassword mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final OneTimePassword objOneTimePassword = new OneTimePassword();
		
		objOneTimePassword.setSonetimepasswordcode(getString(arg0,"sonetimepasswordcode",arg1));
		objOneTimePassword.setSonetimepassword(getString(arg0,"sonetimepassword",arg1));
		objOneTimePassword.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objOneTimePassword.setNstatus(getShort(arg0,"nstatus",arg1));
		objOneTimePassword.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objOneTimePassword.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objOneTimePassword.setScreateddate(getString(arg0,"screateddate",arg1));
		
		return objOneTimePassword;
	}

}