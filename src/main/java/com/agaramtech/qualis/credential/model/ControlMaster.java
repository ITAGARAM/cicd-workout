package com.agaramtech.qualis.credential.model;

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
@Table(name = "controlmaster")
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ControlMaster extends CustomizedResultsetRowMapper<ControlMaster> implements Serializable, RowMapper<ControlMaster> {

	@Id
	@Column(name = "ncontrolcode")
	private short ncontrolcode;
	
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	
	@Column(name = "scontrolname", length = 50, nullable = false)
	private String scontrolname;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@ColumnDefault("4")
	@Column(name = "nisesigncontrol", nullable = false)
	private short nisesigncontrol = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nisdistributedsite", nullable = false)
	private short nisdistributedsite = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("3")
	@Column(name = "nisprimarysyncsite", nullable = false)
	private short nisprimarysyncsite = (short)Enumeration.TransactionStatus.YES.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String scontrolids;
	
	@Transient
	private transient int nuserrolecontrolcode;
	
	@Transient
	private transient int npropertiescode;
	
	@Transient
	private transient int npropertytypecode;
	
	@Transient
	private transient String spropertyname;
	
	@Transient
	private transient String spropertiesvalue;
	
	@Transient
	private transient String screenname;
	
	@Transient
	private transient int nauditpropertiescode;
	
	@Transient
	private transient String sauditpropertyname;
	
	@Transient
	private transient String sauditpropertyvalue;
	
	@Transient
	private transient String sformname;
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient int nsitecontrolcode;
	
	@Transient
	private transient short nsitecode;
	
	@Transient
	private transient int nuserrolecode;
	
	@Transient
	private transient int nuserrolescreencode;
	
	@Transient
	private transient short nneedesign;
	
	@Transient
	private transient short nisbarcodecontrol;
	
	@Transient
	private transient short nneedrights;
	
	@Transient
	private transient String ssubfoldername;
	
	@Transient
	private transient int needesignsparent;
	
	@Transient
	private transient String sdefaultname;

	@Override
	public ControlMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ControlMaster objControlMaster = new ControlMaster();
		
		objControlMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		objControlMaster.setNcontrolcode(getShort(arg0, "ncontrolcode", arg1));
		objControlMaster.setScontrolname(getString(arg0, "scontrolname", arg1));
		objControlMaster.setNformcode(getShort(arg0, "nformcode", arg1));
		objControlMaster.setNuserrolecontrolcode(getInteger(arg0, "nuserrolecontrolcode", arg1));
		objControlMaster.setNpropertiescode(getInteger(arg0, "npropertiescode", arg1));
		objControlMaster.setNpropertytypecode(getInteger(arg0, "npropertytypecode", arg1));
		objControlMaster.setSpropertyname(getString(arg0, "spropertyname", arg1));
		objControlMaster.setSpropertiesvalue(getString(arg0, "spropertiesvalue", arg1));
		objControlMaster.setSauditpropertyname(getString(arg0, "sauditpropertyname", arg1));
		objControlMaster.setSauditpropertyvalue(getString(arg0, "sauditpropertyvalue", arg1));
		objControlMaster.setNauditpropertiescode(getInteger(arg0, "nauditpropertiescode", arg1));
		objControlMaster.setSformname(getString(arg0, "sformname", arg1));
		objControlMaster.setNisesigncontrol(getShort(arg0, "nisesigncontrol", arg1));
		objControlMaster.setNisbarcodecontrol(getShort(arg0, "nisbarcodecontrol", arg1));
		objControlMaster.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objControlMaster.setScontrolids(getString(arg0, "scontrolids", arg1));
		objControlMaster.setNsitecontrolcode(getInteger(arg0, "nsitecontrolcode", arg1));
		objControlMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objControlMaster.setNneedesign(getShort(arg0, "nneedesign", arg1));
		objControlMaster.setNneedrights(getShort(arg0, "nneedrights", arg1));
		objControlMaster.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objControlMaster.setNisdistributedsite(getShort(arg0, "nisdistributedsite", arg1));
		objControlMaster.setNuserrolescreencode(getInteger(arg0, "nuserrolescreencode", arg1));
		objControlMaster.setScreenname(getString(arg0, "screenname", arg1));
		objControlMaster.setSsubfoldername(getString(arg0, "ssubfoldername", arg1));
		objControlMaster.setNeedesignsparent(getInteger(arg0, "needesignsparent", arg1));
		objControlMaster.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objControlMaster.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objControlMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objControlMaster.setNisprimarysyncsite(getShort(arg0, "nisprimarysyncsite", arg1));

		return objControlMaster;
	}
	
}
