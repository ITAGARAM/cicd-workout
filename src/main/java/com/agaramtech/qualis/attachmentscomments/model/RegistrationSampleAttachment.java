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
 * This class is used to map the fields of 'registrationsampleattachment' table of the Database.
 */
@Entity
@Table(name = "registrationsampleattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSampleAttachment extends CustomizedResultsetRowMapper<RegistrationSampleAttachment> implements Serializable, RowMapper<RegistrationSampleAttachment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsampleattachmentcode")
	private int nsampleattachmentcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nlinkcode", nullable = false)
	@ColumnDefault("-1")
	private int nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();;

	@Column(name = "nattachmenttypecode", nullable = false)
	private int nattachmenttypecode;

	@Lob@Column(name = "jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String stestsynonym;
	
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
	private transient String groupingField1;
	
	@Transient
	private transient String groupingField2;

	@Override
	public RegistrationSampleAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegistrationSampleAttachment objRegistrationSampleAttachment = new RegistrationSampleAttachment();
		objRegistrationSampleAttachment.setNsampleattachmentcode(getInteger(arg0 ,"nsampleattachmentcode", arg1));
		objRegistrationSampleAttachment.setNpreregno(getInteger(arg0 ,"npreregno", arg1));
		objRegistrationSampleAttachment.setNtransactionsamplecode(getInteger(arg0 ,"ntransactionsamplecode", arg1));
		objRegistrationSampleAttachment.setNformcode(getInteger(arg0 ,"nformcode", arg1));
		objRegistrationSampleAttachment.setNusercode(getInteger(arg0 ,"nusercode", arg1));
		objRegistrationSampleAttachment.setNuserrolecode(getInteger(arg0 ,"nuserrolecode", arg1));
		objRegistrationSampleAttachment.setNlinkcode(getInteger(arg0 ,"nlinkcode", arg1));
		objRegistrationSampleAttachment.setNattachmenttypecode(getInteger(arg0 ,"nattachmenttypecode", arg1));
		objRegistrationSampleAttachment.setNstatus(getShort(arg0 ,"nstatus", arg1));
		objRegistrationSampleAttachment.setSarno(getString(arg0 ,"sarno", arg1));
		objRegistrationSampleAttachment.setSsamplearno(getString(arg0 ,"ssamplearno", arg1));
		objRegistrationSampleAttachment.setStestsynonym(getString(arg0 ,"stestsynonym", arg1));
		objRegistrationSampleAttachment.setSdisplayname(getString(arg0 ,"sdisplayname", arg1));
		objRegistrationSampleAttachment.setSusername(getString(arg0 ,"susername", arg1));
		objRegistrationSampleAttachment.setSuserrolename(getString(arg0 ,"suserrolename", arg1));
		objRegistrationSampleAttachment.setSlinkname(getString(arg0 ,"slinkname", arg1));
		objRegistrationSampleAttachment.setStypename(getString(arg0 ,"stypename", arg1));
		objRegistrationSampleAttachment.setScreateddate(getString(arg0 ,"screateddate", arg1));
		objRegistrationSampleAttachment.setGroupingField1(getString(arg0 ,"groupingField1", arg1));
		objRegistrationSampleAttachment.setGroupingField2(getString(arg0 ,"groupingField2", arg1));
		objRegistrationSampleAttachment.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationSampleAttachment.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objRegistrationSampleAttachment;

	}

}

