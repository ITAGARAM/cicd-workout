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
 * This class is used to map the fields of 'registrationtestattachment' table of the Database.
 */
@Entity
@Table(name = "registrationtestattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationTestAttachment extends CustomizedResultsetRowMapper<RegistrationTestAttachment> implements Serializable, RowMapper<RegistrationTestAttachment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestattachmentcode")
	private int ntestattachmentcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nlinkcode", nullable = false)
	@ColumnDefault("-1")
	private short nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode;

	@Lob
	@Column(name = "jsondata",columnDefinition = "jsonb")
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
	
	@Transient
	private transient int noffsetdcreateddate;
	
	@Transient
	private transient int nneedreport;
	
	@Transient
	private transient String sneedreport;
	
	
	/**
	 *
	 */
	@Override
	public RegistrationTestAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegistrationTestAttachment objRegistrationTestAttachment = new RegistrationTestAttachment();

		objRegistrationTestAttachment.setNtestattachmentcode(getInteger(arg0 ,"ntestattachmentcode", arg1));
		objRegistrationTestAttachment.setNpreregno(getInteger(arg0 ,"npreregno", arg1));
		objRegistrationTestAttachment.setNtransactionsamplecode(getInteger(arg0 ,"ntransactionsamplecode", arg1));
		objRegistrationTestAttachment.setNtransactiontestcode(getInteger(arg0 ,"ntransactiontestcode", arg1));
		objRegistrationTestAttachment.setNformcode(getShort(arg0 ,"nformcode", arg1));
		objRegistrationTestAttachment.setNusercode(getInteger(arg0 ,"nusercode", arg1));
		objRegistrationTestAttachment.setNuserrolecode(getInteger(arg0 ,"nuserrolecode", arg1));
		objRegistrationTestAttachment.setNlinkcode(getShort(arg0 ,"nlinkcode", arg1));
		objRegistrationTestAttachment.setNattachmenttypecode(getShort(arg0 ,"nattachmenttypecode", arg1));
		objRegistrationTestAttachment.setNstatus(getShort(arg0 ,"nstatus", arg1));
		objRegistrationTestAttachment.setSarno(getString(arg0 ,"sarno", arg1));
		objRegistrationTestAttachment.setSsamplearno(getString(arg0 ,"ssamplearno", arg1));
		objRegistrationTestAttachment.setStestsynonym(getString(arg0 ,"stestsynonym", arg1));
		objRegistrationTestAttachment.setSdisplayname(getString(arg0 ,"sdisplayname", arg1));
		objRegistrationTestAttachment.setSusername(getString(arg0 ,"susername", arg1));
		objRegistrationTestAttachment.setSuserrolename(getString(arg0 ,"suserrolename", arg1));
		objRegistrationTestAttachment.setSlinkname(getString(arg0 ,"slinkname", arg1));
		objRegistrationTestAttachment.setStypename(getString(arg0 ,"stypename", arg1));
		objRegistrationTestAttachment.setScreateddate(getString(arg0 ,"screateddate", arg1));
		objRegistrationTestAttachment.setGroupingField1(getString(arg0 ,"groupingField1", arg1));
		objRegistrationTestAttachment.setGroupingField2(getString(arg0 ,"groupingField2", arg1));
		objRegistrationTestAttachment.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationTestAttachment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationTestAttachment.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objRegistrationTestAttachment.setNneedreport(getInteger(arg0,"nneedreport",arg1));
		objRegistrationTestAttachment.setSneedreport(getString(arg0 ,"sneedreport", arg1));
		

		return objRegistrationTestAttachment;

	}

}
