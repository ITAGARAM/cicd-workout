package com.agaramtech.qualis.attachmentscomments.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * This class is used to map the fields of 'registrationattachment' table of the Database.
 */
@Entity
@Table(name = "registrationattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationAttachment extends CustomizedResultsetRowMapper<RegistrationAttachment> implements Serializable, RowMapper<RegistrationAttachment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregattachmentcode")
	private int nregattachmentcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nlinkcode", nullable = false)
	@ColumnDefault("-1")
	private short nlinkcode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;	
	
	@Lob@Column(name = "jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String ssystemfilename;
	
	@Transient
	private transient String sfilename;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String stypename;
	
	@Transient
	private transient String slinkname;
	
	@Transient
	private transient String screateddate;

	@Transient
	private transient String groupingField;
	

	@Override
	public RegistrationAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegistrationAttachment objRegistrationAttachment = new RegistrationAttachment();

		objRegistrationAttachment.setNregattachmentcode(getInteger(arg0, "nregattachmentcode", arg1));
		objRegistrationAttachment.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objRegistrationAttachment.setNformcode(getShort(arg0, "nformcode", arg1));
		objRegistrationAttachment.setNusercode(getInteger(arg0, "nusercode", arg1));
		objRegistrationAttachment.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objRegistrationAttachment.setNlinkcode(getShort(arg0, "nlinkcode", arg1));
		objRegistrationAttachment.setNattachmenttypecode(getShort(arg0, "nattachmenttypecode", arg1));
		objRegistrationAttachment.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationAttachment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationAttachment.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegistrationAttachment.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objRegistrationAttachment.setSfilename(getString(arg0, "sfilename", arg1));
		objRegistrationAttachment.setSarno(getString(arg0, "sarno", arg1));
		objRegistrationAttachment.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objRegistrationAttachment.setSusername(getString(arg0, "susername", arg1));
		objRegistrationAttachment.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objRegistrationAttachment.setSlinkname(getString(arg0, "slinkname", arg1));
		objRegistrationAttachment.setStypename(getString(arg0, "stypename", arg1));
		objRegistrationAttachment.setScreateddate(getString(arg0, "screateddate", arg1));
		objRegistrationAttachment.setGroupingField(getString(arg0, "groupingField", arg1));
		
		return objRegistrationAttachment;

	}
	

}

