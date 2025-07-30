package com.agaramtech.qualis.externalorder.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
 * This class is used to map the fields of 'externalordertest' table of the Database.
 */
@Entity
@Table(name = "externalordertest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalOrderTest extends CustomizedResultsetRowMapper<ExternalOrderTest> implements Serializable,RowMapper<ExternalOrderTest>{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nexternalordertestcode", nullable=false)  
	private int nexternalordertestcode;
	
	@Column(name = "nexternalordersamplecode", nullable=false) 
	private int nexternalordersamplecode;	
	
	@Column(name = "nexternalordercode",  nullable=false) 
	private int nexternalordercode;	
	
	@ColumnDefault("-1")
	@Column(name = "ntestpackagecode", nullable=false) 
	private int ntestpackagecode;
	
	@ColumnDefault("-1")	
	@Column(name = "ncontainertypecode") 	
	private int ncontainertypecode;
	
	@Column(name = "ntestcode", nullable=false) 
	private int ntestcode;
	
	@Column(name = "dmodifieddate")	 
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false) 
	private short nsitecode;  
    
    @Column(name = "nparentsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nparentsitecode;
    
    @Column(name = "npreregno", nullable = false)
	@ColumnDefault("-1")
	private Integer npreregno;
    
    @Column(name = "ntransactionsamplecode", nullable = false)
	@ColumnDefault("-1")
	private Integer ntransactionsamplecode;
    
    @Column(name = "ntransactiontestcode", nullable = false)
	@ColumnDefault("-1")
	private Integer ntransactiontestcode;
    
    @Column(name = "nstatus", nullable=false) 
    private short nstatus;
    
    @Transient
	private transient String stestname;
    @Transient
	private transient String stestcategoryname;
    @Transient
	private transient Integer nexternalorderstatuscode;
    @Transient
	private transient String sexternalorderid;
    @Transient
	private transient Integer ntransactionstatus;
	
	
	@Override
	public ExternalOrderTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ExternalOrderTest objExternalOrderTest = new ExternalOrderTest();
				
		objExternalOrderTest.setNexternalordertestcode(getInteger(arg0,"nexternalordertestcode",arg1));
		objExternalOrderTest.setNexternalordersamplecode(getInteger(arg0,"nexternalordersamplecode",arg1));
		objExternalOrderTest.setNexternalordercode(getInteger(arg0,"nexternalordercode",arg1));
		objExternalOrderTest.setNtestpackagecode(getInteger(arg0,"ntestpackagecode",arg1));
		objExternalOrderTest.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objExternalOrderTest.setNstatus(getShort(arg0,"nstatus",arg1));
		objExternalOrderTest.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objExternalOrderTest.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objExternalOrderTest.setNparentsitecode(getShort(arg0,"nparentsitecode",arg1));
		objExternalOrderTest.setStestname(getString(arg0,"stestname",arg1));
		objExternalOrderTest.setStestcategoryname(getString(arg0,"stestcategoryname",arg1));
		objExternalOrderTest.setNcontainertypecode(getInteger(arg0,"ncontainertypecode",arg1));
		objExternalOrderTest.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objExternalOrderTest.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objExternalOrderTest.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objExternalOrderTest.setNexternalorderstatuscode(getInteger(arg0,"nexternalorderstatuscode",arg1));
		objExternalOrderTest.setSexternalorderid(getString(arg0,"sexternalorderid",arg1));		
		objExternalOrderTest.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		return objExternalOrderTest;
	}


	@Override
	public String toString() {
		return "ExternalOrderTest [nexternalordertestcode=" + nexternalordertestcode + ", nexternalordersamplecode="
				+ nexternalordersamplecode + ", nexternalordercode=" + nexternalordercode + ", ntestpackagecode="
				+ ntestpackagecode + ", ncontainertypecode=" + ncontainertypecode + ", ntestcode=" + ntestcode
				+ ", dmodifieddate=" + dmodifieddate + ", nsitecode=" + nsitecode + ", nparentsitecode="
				+ nparentsitecode + ", npreregno=" + npreregno + ", ntransactionsamplecode=" + ntransactionsamplecode
				+ ", ntransactiontestcode=" + ntransactiontestcode + ", nstatus=" + nstatus + ", stestname=" + stestname
				+ ", stestcategoryname=" + stestcategoryname 
				+ ", sexternalorderid=" + sexternalorderid
				+ ", nexternalorderstatuscode=" + nexternalorderstatuscode
				+ ", ntransactionstatus=" + ntransactionstatus
				+ "]";
	}




}
