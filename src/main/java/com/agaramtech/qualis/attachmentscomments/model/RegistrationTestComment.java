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
 * This class is used to map the fields of 'registrationtestcomments' table of the Database.
 */
@Entity
@Table(name = "registrationtestcomments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegistrationTestComment extends CustomizedResultsetRowMapper<RegistrationTestComment> implements Serializable, RowMapper<RegistrationTestComment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestcommentcode")
	private int ntestcommentcode;

	@Column(name = "npreregno", nullable = false)
	private int npreregno;

	@Column(name = "ntransactionsamplecode", nullable = false)
	private int ntransactionsamplecode;

	@Column(name = "ntransactiontestcode", nullable = false)
	private int ntransactiontestcode;

	@Column(name = "nformcode", nullable = false)
	private int nformcode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Lob@Column(name = "jsondata", columnDefinition = "jsonb", nullable = false)
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
	private transient String scomponentname;
	
	@Transient
	private transient String ssamplearno;
	
	@Transient
	private transient String stestsynonym;
	
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
	private transient String stestname;
	
	@Transient
	private transient String scommentsubtype;
	
	@Transient
	private transient String spredefinedname;
	
	@Transient
	private transient String sdescription; 
	
	@Transient
	private transient int nneedreport;
	
	@Transient
	private transient String sneedreport;
	
	@Transient
	private transient String scomments;
	
	

	@Override
	public RegistrationTestComment mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegistrationTestComment objRegistrationTestComment = new RegistrationTestComment();

		objRegistrationTestComment.setNtestcommentcode(getInteger(arg0, "ntestcommentcode", arg1));
		objRegistrationTestComment.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objRegistrationTestComment.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objRegistrationTestComment.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objRegistrationTestComment.setNformcode(getInteger(arg0, "nformcode", arg1));
		objRegistrationTestComment.setNusercode(getInteger(arg0, "nusercode", arg1));
		objRegistrationTestComment.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objRegistrationTestComment.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objRegistrationTestComment.setNsamplecommentscode(getInteger(arg0, "nsamplecommentscode", arg1));
		objRegistrationTestComment.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegistrationTestComment.setSarno(getString(arg0, "sarno", arg1));
		objRegistrationTestComment.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objRegistrationTestComment.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objRegistrationTestComment.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objRegistrationTestComment.setSusername(getString(arg0, "susername", arg1));
		objRegistrationTestComment.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objRegistrationTestComment.setSsampletestcommentname(getString(arg0, "ssampletestcommentname", arg1));
		objRegistrationTestComment.setScomponentname(getString(arg0, "scomponentname", arg1));
		objRegistrationTestComment.setStestname(getString(arg0, "stestname", arg1));
		objRegistrationTestComment.setGroupingField(getString(arg0, "groupingField", arg1));
		objRegistrationTestComment.setGroupingField2(getString(arg0, "groupingField2", arg1));
		objRegistrationTestComment.setScomments(getString(arg0, "scomments", arg1));
		objRegistrationTestComment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objRegistrationTestComment.setScommentsubtype(getString(arg0, "scommentsubtype", arg1));
		objRegistrationTestComment.setSdescription(getString(arg0, "sdescription", arg1));
		objRegistrationTestComment.setSpredefinedname(getString(arg0, "spredefinedname", arg1));
		objRegistrationTestComment.setNneedreport(getInteger(arg0,"nneedreport",arg1));
		objRegistrationTestComment.setSneedreport(getString(arg0 ,"sneedreport", arg1));

		return objRegistrationTestComment;

	}

}
