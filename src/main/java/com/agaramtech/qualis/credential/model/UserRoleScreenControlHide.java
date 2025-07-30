package com.agaramtech.qualis.credential.model;

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

@Entity
@Table(name = "userrolescreencontrolhide")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRoleScreenControlHide extends CustomizedResultsetRowMapper<UserRoleScreenControlHide> implements Serializable,RowMapper<UserRoleScreenControlHide>{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nuserrolehidecontrolecode" )  private int nuserrolehidecontrolecode;
	
	@Column(name="ncontrolcode", nullable=false)  private short ncontrolcode;
	
	@Column(name="nusercode", nullable=false)  private int nusercode;
	
	@Column(name="nformcode", nullable=false) private short nformcode;
	
	@Column(name="nuserrolecode", nullable=false) private int nuserrolecode;
	
	@Column(name="needrights", nullable=false )  private short needrights;
	
	@Column(name="nneedesign", nullable=false )  private short nneedesign;
	
	@Column(name = "dmodifieddate")private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false )  private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	

	@Transient
	private transient String  scontrolids;
	@Transient
	private transient int nisesigncontrol=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();	
	@Transient
	private transient int nauditpropertiescode;
	@Transient
	private transient String  sauditpropertyname;
	@Transient
	private transient String  sauditpropertyvalue;
	@Transient
	private transient String sformname;	
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient int nuserrolescreencode;
	@Transient
	private transient int nsitecontrolcode;
	@Transient
	private transient String screenname;
	@Transient
	private transient String ssubfoldername;
	@Transient
	private transient int nisbarcodecontrol;

	public UserRoleScreenControlHide mapRow(ResultSet arg0, int arg1) throws SQLException {
		final UserRoleScreenControlHide objScreenControlHide = new UserRoleScreenControlHide();
		objScreenControlHide.setNuserrolehidecontrolecode(getInteger(arg0,"nuserrolehidecontrolecode",arg1));
		objScreenControlHide.setNcontrolcode(getShort(arg0,"ncontrolcode",arg1));
		objScreenControlHide.setNusercode(getInteger(arg0,"nusercode",arg1));
		objScreenControlHide.setNformcode(getShort(arg0,"nformcode",arg1));
		objScreenControlHide.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objScreenControlHide.setNeedrights(getShort(arg0,"needrights",arg1));
		objScreenControlHide.setNneedesign(getShort(arg0,"nneedesign",arg1));
		objScreenControlHide.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objScreenControlHide.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objScreenControlHide.setNstatus(getShort(arg0,"nstatus",arg1));
		objScreenControlHide.setScontrolids(getString(arg0,"scontrolids",arg1));
		objScreenControlHide.setNisesigncontrol(getInteger(arg0,"nisesigncontrol",arg1));
		objScreenControlHide.setNauditpropertiescode(getInteger(arg0,"nauditpropertiescode",arg1));		
		objScreenControlHide.setSauditpropertyname(getString(arg0,"sauditpropertyname",arg1));
		objScreenControlHide.setSauditpropertyvalue(getString(arg0,"sauditpropertyvalue",arg1));
		objScreenControlHide.setSformname(getString(arg0,"sformname",arg1));
		objScreenControlHide.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objScreenControlHide.setNuserrolescreencode(getInteger(arg0,"nuserrolescreencode",arg1));
		objScreenControlHide.setNsitecontrolcode(getInteger(arg0,"nsitecontrolcode",arg1));
		objScreenControlHide.setScreenname(getString(arg0,"screenname",arg1));
		objScreenControlHide.setSsubfoldername(getString(arg0,"ssubfoldername",arg1));
		objScreenControlHide.setNisbarcodecontrol(getInteger(arg0,"nisbarcodecontrol",arg1));
		return objScreenControlHide;
	}

}
