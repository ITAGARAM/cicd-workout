package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
 * This class is used to map the fields of 'unit' table of the Database.
 */
@Entity
@Table(name = "unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Unit extends CustomizedResultsetRowMapper<Unit> implements Serializable,RowMapper<Unit> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nunitcode")
	private int nunitcode;

	@Column(name = "sunitname", length=100, nullable = false)
	private String sunitname;
	
	@Column(name = "sunitsynonym", length=100, nullable = false)
	private String sunitsynonym;

	@Column(name = "sdescription",length=255)
	private String sdescription="";

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String sunitname1;
	
	@Transient
	private transient String smodifieddate;	
	
	@Transient
	private transient int nsourceunitcode;
	
	@Transient
	private transient int ndestinationunitcode;	
	
	@Override
	public Unit mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Unit objUnit = new Unit();
		
		objUnit.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objUnit.setSunitname(StringEscapeUtils.unescapeJava(getString(arg0,"sunitname",arg1)));
		objUnit.setSunitname1(StringEscapeUtils.unescapeJava(getString(arg0,"sunitname1",arg1)));
		objUnit.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objUnit.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objUnit.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objUnit.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objUnit.setNstatus(getShort(arg0,"nstatus",arg1));
		objUnit.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objUnit.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		objUnit.setSunitsynonym(getString(arg0,"sunitsynonym",arg1));
		objUnit.setNsourceunitcode(getInteger(arg0,"nsourceunitcode",arg1));
		objUnit.setNdestinationunitcode(getInteger(arg0,"ndestinationunitcode",arg1));
		
		return objUnit;
	}

}