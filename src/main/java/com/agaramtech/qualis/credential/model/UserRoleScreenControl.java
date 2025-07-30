/**
 * @author ATE234
 * @Date 04-04-2025
 * @time 4:20:21 PM
 */
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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
* This class is used to map the fields of 'userrolescreencontrol' table of the
* Database.
*/

@Entity
@Table(name = "userrolescreencontrol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRoleScreenControl extends CustomizedResultsetRowMapper<UserRoleScreenControl> implements Serializable,RowMapper<UserRoleScreenControl>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nuserrolecontrolcode")
	private int nuserrolecontrolcode;
	
	@Column(name = "nformcode", nullable = false)
	private short nformcode;
	
	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;
	
	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;
	
	@Column(name = "nneedrights", nullable = false)
	private short nneedrights;
	
	@Column(name = "nneedesign", nullable = false)	
	private short nneedesign=4;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")	
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String scontrolname;
	@Transient
	private transient String scontrolids;
	@Transient
	private transient int npropertiescode;
	@Transient
	private transient int npropertytypecode;
	@Transient
	private transient String spropertyname;
	@Transient
	private transient String spropertiesvalue;
	@Transient
	private transient int nisesigncontrol = 4;
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
	private transient int nuserrolescreencode;
	@Transient
	private transient int nsitecontrolcode;
	@Transient
	private transient int nsitecode;
	@Transient
	private transient String screenname;
	@Transient
	private transient String ssubfoldername;
	@Transient
	private transient int nisbarcodecontrol;
	@Transient
	private transient int needesignsparent;
	@Transient
	private transient Map<String,Object> jsondata;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient Short nisdistributedsite;
	@Transient
	private transient Short nisprimarysyncsite;


	@Override
	public UserRoleScreenControl mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UserRoleScreenControl objUserRoleScreenControl = new UserRoleScreenControl();
		objUserRoleScreenControl.setNstatus(getShort(arg0, "nstatus",arg1));
		objUserRoleScreenControl.setNcontrolcode(getShort(arg0, "ncontrolcode",arg1));
		objUserRoleScreenControl.setScontrolname(getString(arg0, "scontrolname",arg1));
		objUserRoleScreenControl.setNformcode(getShort(arg0, "nformcode",arg1));
		objUserRoleScreenControl.setSauditpropertyname(getString(arg0, "sauditpropertyname",arg1));
		objUserRoleScreenControl.setSauditpropertyvalue(getString(arg0, "sauditpropertyvalue",arg1));
		objUserRoleScreenControl.setNauditpropertiescode(getInteger(arg0, "nauditpropertiescode",arg1));
		objUserRoleScreenControl.setSformname(getString(arg0, "sformname",arg1));
		objUserRoleScreenControl.setNisesigncontrol(getInteger(arg0, "nisesigncontrol",arg1));
		objUserRoleScreenControl.setNisbarcodecontrol(getInteger(arg0, "nisbarcodecontrol",arg1));
		objUserRoleScreenControl.setSdisplayname(getString(arg0, "sdisplayname",arg1));
		objUserRoleScreenControl.setScontrolids(getString(arg0, "scontrolids",arg1));
		objUserRoleScreenControl.setNsitecontrolcode(getInteger(arg0, "nsitecontrolcode",arg1));
		objUserRoleScreenControl.setNsitecode(getShort(arg0, "nsitecode",arg1));
		objUserRoleScreenControl.setNneedesign(getShort(arg0, "nneedesign",arg1));
		objUserRoleScreenControl.setNneedrights(getShort(arg0, "nneedrights",arg1));
		objUserRoleScreenControl.setNuserrolecode(getInteger(arg0, "nuserrolecode",arg1));
		objUserRoleScreenControl.setNuserrolescreencode(getInteger(arg0, "nuserrolescreencode",arg1));
		objUserRoleScreenControl.setNuserrolecontrolcode(getInteger(arg0, "nuserrolecontrolcode",arg1));
		objUserRoleScreenControl.setScreenname(getString(arg0, "screenname",arg1));
		objUserRoleScreenControl.setSsubfoldername(getString(arg0, "ssubfoldername",arg1));
		objUserRoleScreenControl.setNeedesignsparent(getInteger(arg0, "needesignsparent",arg1));
		objUserRoleScreenControl.setJsondata(getJsonObject(arg0, "jsondata",arg1));
		objUserRoleScreenControl.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		objUserRoleScreenControl.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objUserRoleScreenControl.setNisdistributedsite(getShort(arg0,"nisdistributedsite",arg1));
		objUserRoleScreenControl.setNisprimarysyncsite(getShort(arg0,"nisprimarysyncsite",arg1));

		return objUserRoleScreenControl;
	}

}
