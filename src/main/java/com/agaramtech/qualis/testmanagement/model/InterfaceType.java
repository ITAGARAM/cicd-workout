package com.agaramtech.qualis.testmanagement.model;

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

/**
 * This class is used to map the fields of 'interfacetype' table of the
 * Database. 
 */
@Entity
@Table(name = "interfacetype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)

public class InterfaceType extends CustomizedResultsetRowMapper<InterfaceType> implements Serializable, RowMapper<InterfaceType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninterfacetypecode")	
	private short ninterfacetypecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")	
	private Map<String, Object> jsondata;
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus =  (short)Enumeration.TransactionStatus.NO.gettransactionstatus();;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus =  (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")	
	private Instant dmodifieddate;
	
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sdescription;
	@Transient
	private transient String sinterfacetypename;
	@Transient
	private transient String spreregno;
	
	@Override
	public InterfaceType mapRow(ResultSet arg0, int arg1) throws SQLException {
		InterfaceType objInterfaceType = new InterfaceType();
		objInterfaceType.setNinterfacetypecode(getShort(arg0, "ninterfacetypecode", arg1));
		objInterfaceType.setSdescription(getString(arg0, "sdescription", arg1));
		objInterfaceType.setSinterfacetypename(getString(arg0, "sinterfacetypename", arg1));
		objInterfaceType.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objInterfaceType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInterfaceType.setNstatus(getShort(arg0, "nstatus", arg1));
		objInterfaceType.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objInterfaceType.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objInterfaceType.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objInterfaceType.setDmodifieddate(getInstant(arg0,"dmodifieddate", arg1));
		objInterfaceType.setSpreregno(getString(arg0, "spreregno", arg1));

		return objInterfaceType;
	}

}
