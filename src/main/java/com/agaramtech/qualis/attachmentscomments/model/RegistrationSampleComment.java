package com.agaramtech.qualis.attachmentscomments.model;

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
 * This class is used to map the fields of 'registrationsamplecomment' table of the Database.
 */
@Entity
@Table(name = "registrationsamplecomment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationSampleComment extends CustomizedResultsetRowMapper<RegistrationSampleComment>
		implements RowMapper<RegistrationSampleComment> {

	@Id
	@Column(name = "nsamplecommentcode")
	private int nsamplecommentcode;

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

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> jsondata;

	@Column(name = "nsamplecommentscode", nullable = false)
	private int nsamplecommentscode;
	
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
	private transient String scomponentname;
	
	@Transient
	private transient String groupingField;
	
	@Transient
	private transient String groupingField2;
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String ssampletestcommentname;
	
	@Transient
	private transient String scomments;


	@Override
	public RegistrationSampleComment mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegistrationSampleComment objRegistrationSampleComment = new RegistrationSampleComment();

		objRegistrationSampleComment.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objRegistrationSampleComment.setNformcode(getInteger(arg0, "nformcode", arg1));
		objRegistrationSampleComment.setNusercode(getInteger(arg0, "nusercode", arg1));
		objRegistrationSampleComment.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objRegistrationSampleComment.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationSampleComment.setNsamplecommentscode(getInteger(arg0, "nsamplecommentscode", arg1));
		objRegistrationSampleComment.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegistrationSampleComment.setSarno(getString(arg0, "sarno", arg1));
		objRegistrationSampleComment.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objRegistrationSampleComment.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objRegistrationSampleComment.setSusername(getString(arg0, "susername", arg1));
		objRegistrationSampleComment.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objRegistrationSampleComment.setSsampletestcommentname(getString(arg0, "ssampletestcommentname", arg1));
		objRegistrationSampleComment.setScomponentname(getString(arg0, "scomponentname", arg1));
		objRegistrationSampleComment.setGroupingField(getString(arg0, "groupingField", arg1));
		objRegistrationSampleComment.setGroupingField2(getString(arg0, "groupingField2", arg1));
		objRegistrationSampleComment.setNsamplecommentcode(getInteger(arg0, "nsamplecommentcode", arg1));
		objRegistrationSampleComment.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objRegistrationSampleComment.setScomments(getString(arg0, "scomments", arg1));
		objRegistrationSampleComment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objRegistrationSampleComment;

	}

}
