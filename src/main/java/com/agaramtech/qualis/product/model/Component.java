package com.agaramtech.qualis.product.model;

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
 * This class is used to map the fields of 'component' table of the Database.
 */
@Entity
@Table(name = "component")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Component extends CustomizedResultsetRowMapper<Component> implements Serializable,RowMapper<Component>{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncomponentcode")
	private int ncomponentcode;
	
	@Column(name = "scomponentname", length=100, nullable=false)
	private String scomponentname;
	
	@Column(name = "sdescription", length=255)
	private String sdescription="";
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable=false)
	private short ndefaultstatus = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;
	
	@Transient
	private transient int nproducttypemand;
	
	@Transient
	private transient int nspecsampletypecode;
	
	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient int ntzdreceivedate;
	
	@Transient
	private transient int ntimezonecode;
	
	@Transient
	private transient String stimezonecode;
	
	@Transient
	private transient Instant dreceiveddate;
	
	@Transient
	private transient String sreceiveddate;
	
	@Transient
	private transient String stzdreceivedate;
	
	@Transient
	private transient int nproductcatcode;
	
	
	@Override
	public Component mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final Component component = new Component();
		
		component.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		component.setScomponentname(StringEscapeUtils.unescapeJava(getString(arg0,"scomponentname",arg1)));
		component.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		component.setSdisplaystatus(getString(arg0,"sdisplaystatus",arg1));
		component.setNproducttypemand(getInteger(arg0,"nproducttypemand",arg1));
		component.setNsitecode(getShort(arg0,"nsitecode",arg1));		
		component.setNstatus(getShort(arg0,"nstatus",arg1));
		component.setDreceiveddate(getInstant(arg0,"dreceiveddate",arg1));
		component.setNspecsampletypecode(getInteger(arg0,"nspecsampletypecode",arg1));
		component.setNtzdreceivedate(getInteger(arg0,"ntzdreceivedate",arg1));
		component.setNtimezonecode(getInteger(arg0,"ntimezonecode",arg1));
		component.setStimezonecode(getString(arg0,"stimezonecode",arg1));
		component.setSreceiveddate(getString(arg0,"sreceiveddate",arg1));
		component.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		component.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		component.setSmodifieddate(getString(arg0,"smodifieddate",arg1));
		component.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));

		return component;
	}
}
