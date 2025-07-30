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
 * This class is used to map the fields of 'methodvalidity' table of the Database.
 * @author ATE113
 * @version 9.0.0.1
 * @since   22- 07- 2022
 */
@Entity
@Table(name = "methodvalidity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MethodValidity extends CustomizedResultsetRowMapper<MethodValidity> implements Serializable,RowMapper<MethodValidity> {

	
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "nmethodvaliditycode")
	private int nmethodvaliditycode;
	
	@Column(name = "nmethodcode", nullable=false)
	private int nmethodcode;
	
	@Column(name = "dvaliditystartdate", nullable=false)
	private Instant dvaliditystartdate;
	
	@Column(name = "dvalidityenddate", nullable=false)
	private Instant dvalidityenddate;
	
	@Column(name = "noffsetdvaliditystartdate", nullable=false)
	private int noffsetdvaliditystartdate =(int)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ntzvaliditystartdatetimezone", nullable=false)
	private short ntzvaliditystartdatetimezone =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "noffsetdvalidityenddate", nullable=false)
	private int noffsetdvalidityenddate =(int)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "ntzvalidityenddatetimezone", nullable=false)
	private short ntzvalidityenddatetimezone =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("8")
	@Column(name = "ntransactionstatus", nullable=false)
	private short ntransactionstatus =(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable=false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String svaliditystartdate;
	
	@Transient
	private transient String stempvaliditystartdate;
	
	@Transient
	private transient String stzvaliditystartdatetimezone;
	
	@Transient
	private transient String svalidityenddate;
	
	@Transient
	private transient String stempvalidityenddate;
	
	@Transient
	private transient String stzvalidityenddatetimezone;
	
	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient String smethodname;
	
	public MethodValidity mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final MethodValidity objMvalid = new MethodValidity();
		
		objMvalid.setNmethodvaliditycode(getInteger(arg0,"nmethodvaliditycode",arg1));
		objMvalid.setNmethodcode(getInteger(arg0,"nmethodcode",arg1));
		objMvalid.setDvaliditystartdate(getInstant(arg0,"dvaliditystartdate",arg1));
		objMvalid.setDvalidityenddate(getInstant(arg0,"dvalidityenddate",arg1));
		objMvalid.setNoffsetdvaliditystartdate(getInteger(arg0,"noffsetdvaliditystartdate",arg1));
		objMvalid.setNoffsetdvalidityenddate(getInteger(arg0,"noffsetdvalidityenddate",arg1));
		objMvalid.setNtzvaliditystartdatetimezone(getShort(arg0,"ntzvaliditystartdatetimezone",arg1));
		objMvalid.setNtzvalidityenddatetimezone(getShort(arg0,"ntzvalidityenddatetimezone",arg1));
		objMvalid.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objMvalid.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		objMvalid.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objMvalid.setNstatus(getShort(arg0,"nstatus",arg1));
		objMvalid.setSvaliditystartdate(getString(arg0,"svaliditystartdate",arg1));
		objMvalid.setStzvaliditystartdatetimezone(getString(arg0,"stzvaliditystartdatetimezone",arg1));
		objMvalid.setSvalidityenddate(getString(arg0,"svalidityenddate",arg1));
		objMvalid.setStzvalidityenddatetimezone(getString(arg0,"stzvalidityenddatetimezone",arg1));
		objMvalid.setSmethodname(getString(arg0,"smethodname",arg1));
		objMvalid.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objMvalid.setStempvaliditystartdate(getString(arg0,"stempvaliditystartdate",arg1));
		objMvalid.setStempvalidityenddate(getString(arg0,"stempvalidityenddate",arg1));
				
		return objMvalid;
	}
}
