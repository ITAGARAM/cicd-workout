package com.agaramtech.qualis.credential.model;
/**
 * This class is used to map fields of 'logintype' table of database
*/
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "logintype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginType extends CustomizedResultsetRowMapper<LoginType> implements Serializable, RowMapper<LoginType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nlogintypecode")
	private short nlogintypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
	private Map<String,Object> jsondata;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")	
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode") 
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String slogintype;
	@Transient
	private transient String sdisplayname;

	@Override
	public LoginType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final LoginType objLoginType = new LoginType();
		objLoginType.setNlogintypecode(getShort(arg0,"nlogintypecode",arg1));
		objLoginType.setSlogintype(getString(arg0,"slogintype",arg1));
		objLoginType.setNstatus(getShort(arg0,"nstatus",arg1));
		objLoginType.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objLoginType.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objLoginType.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objLoginType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objLoginType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		return objLoginType;
	}

}
