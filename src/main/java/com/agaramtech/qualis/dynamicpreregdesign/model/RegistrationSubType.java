package com.agaramtech.qualis.dynamicpreregdesign.model;

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
@Table(name = "registrationsubtype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSubType extends CustomizedResultsetRowMapper<RegistrationSubType> implements Serializable,RowMapper<RegistrationSubType> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nregsubtypecode")
	private short nregsubtypecode;
	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String,Object> jsondata;	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter=(short)Enumeration.TransactionStatus.ALL.gettransactionstatus();
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sregsubtypename="";
	@Transient
	private transient String sdescription="";	
	@Transient
	private transient String sregtypename;
	@Transient
	private transient short napprovalconfigcode;
	@Transient
	private transient boolean nneedsubsample; 
	@Transient
	private transient int nregsubtypeversioncode;
	@Transient
	private transient boolean nneedjoballocation;
	@Transient
	private transient boolean nneedmyjob;
	@Transient
	private transient boolean nneedtestinitiate;
	@Transient
	private transient boolean nneedtemplatebasedflow; 
	@Transient
	private transient String sampletypename;
	@Transient
	private transient boolean nneedbatch;
	@Transient
	private transient boolean nneedworklist;
	@Transient
	private transient int ntransfiltertypecode;
	@Transient
	private transient int nsampletypecode;
	@Transient
	private transient boolean ntestgroupspecrequired;
	
	@Override
	public RegistrationSubType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final RegistrationSubType objRegistrationSubType = new RegistrationSubType();
		objRegistrationSubType.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		objRegistrationSubType.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationSubType.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objRegistrationSubType.setSregsubtypename(getString(arg0, "sregsubtypename", arg1));
		objRegistrationSubType.setSdescription(getString(arg0, "sdescription", arg1));
		objRegistrationSubType.setNsorter(getShort(arg0, "nsorter", arg1));
		objRegistrationSubType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objRegistrationSubType.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegistrationSubType.setSregtypename(getString(arg0, "sregtypename", arg1));
		objRegistrationSubType.setNapprovalconfigcode(getShort(arg0, "napprovalconfigcode", arg1));
		objRegistrationSubType.setNneedsubsample(getBoolean(arg0, "nneedsubsample", arg1));
		objRegistrationSubType.setNregsubtypeversioncode(getShort(arg0, "nregsubtypeversioncode", arg1));
		objRegistrationSubType.setNneedsubsample(getBoolean(arg0, "nneedsubsample", arg1));
		objRegistrationSubType.setNneedjoballocation(getBoolean(arg0, "nneedjoballocation", arg1));
		objRegistrationSubType.setNneedmyjob(getBoolean(arg0, "nneedmyjob", arg1));
		objRegistrationSubType.setNneedtestinitiate(getBoolean(arg0, "nneedtestinitiate", arg1));
		objRegistrationSubType.setNneedtemplatebasedflow(getBoolean(arg0, "nneedtemplatebasedflow", arg1));
		objRegistrationSubType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objRegistrationSubType.setSampletypename(getString(arg0, "sampletypename", arg1));
		objRegistrationSubType.setNneedbatch(getBoolean(arg0, "nneedbatch", arg1));
		objRegistrationSubType.setNneedworklist(getBoolean(arg0, "nneedworklist", arg1));
		objRegistrationSubType.setNtransfiltertypecode(getShort(arg0, "ntransfiltertypecode", arg1));
		objRegistrationSubType.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objRegistrationSubType.setNtestgroupspecrequired(getBoolean(arg0, "ntestgroupspecrequired", arg1));
		
		return objRegistrationSubType;
	}

}
