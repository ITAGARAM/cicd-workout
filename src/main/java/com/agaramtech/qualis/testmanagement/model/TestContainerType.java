package com.agaramtech.qualis.testmanagement.model;

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
 * This class is used to map the fields of 'testcontainertype' table of the
 * Database. 
 */

@Entity
@Table(name = "testcontainertype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class TestContainerType extends CustomizedResultsetRowMapper<TestContainerType> implements Serializable,RowMapper<TestContainerType> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="ntestcontainertypecode")
	private int ntestcontainertypecode;
	
	@Column(name="ntestcode")
	private int ntestcode;
	
	@Column(name="ncontainertypecode")
	private int ncontainertypecode;
	
	@Column(name="nquantity")
	private float nquantity;
	
	@ColumnDefault("4")
	@Column(name="ndefaultstatus")
	private short ndefaultstatus =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	

	@ColumnDefault("-1")
	@Column(name = "nunitcode", nullable = false)
	private int nunitcode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
    private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	

	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String scontainertype;
	
	@Transient
	private transient String sdescription;
	

	@Transient
	private transient String sunitname;


	@Override
	public TestContainerType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final TestContainerType objTestContainerType = new TestContainerType();
		objTestContainerType.setNtestcontainertypecode(getInteger(arg0,"ntestcontainertypecode",arg1));
		objTestContainerType.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestContainerType.setNcontainertypecode(getInteger(arg0,"ncontainertypecode",arg1));
		objTestContainerType.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objTestContainerType.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestContainerType.setScontainertype(getString(arg0,"scontainertype",arg1));
		objTestContainerType.setNquantity(getFloat(arg0,"nquantity",arg1));
		objTestContainerType.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objTestContainerType.setSdescription(getString(arg0,"sdescription",arg1));
		objTestContainerType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestContainerType.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objTestContainerType.setSunitname(getString(arg0,"sunitname",arg1));
		objTestContainerType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objTestContainerType;
	}
}
