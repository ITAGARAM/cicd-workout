package com.agaramtech.qualis.barcode.model;

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
import org.apache.commons.text.StringEscapeUtils;
/**
 * This class is used to map the fields of 'StudyIdentity' table of the Database.
 */
@Entity
@Table(name = "studyidentity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StudyIdentity extends CustomizedResultsetRowMapper<StudyIdentity>
		implements Serializable, RowMapper<StudyIdentity> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstudyidentitycode ")
	private int nstudyidentitycode;

	@Column(name = "nprojecttypecode ")
	private int nprojecttypecode;

	@Column(name = "sidentificationname ", length = 100, nullable = false)
	private String sidentificationname;

	@Column(name = "scode ", length = 1, nullable = false)
	private String scode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sprojecttypename;

	@Transient
	private transient boolean isStudyidentyNameExists;

	@Transient
	private transient boolean isCodeExists;

	@Override
	public StudyIdentity mapRow(ResultSet arg0, int arg1) throws SQLException {
		StudyIdentity studyIdentity = new StudyIdentity();
		studyIdentity.setNstudyidentitycode(getInteger(arg0, "nstudyidentitycode", arg1));
		studyIdentity.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		studyIdentity
				.setSidentificationname(StringEscapeUtils.unescapeJava(getString(arg0, "sidentificationname", arg1)));
		studyIdentity.setScode(StringEscapeUtils.unescapeJava(getString(arg0, "scode", arg1)));
		studyIdentity.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		studyIdentity.setNsitecode(getShort(arg0, "nsitecode", arg1));
		studyIdentity.setNstatus(getShort(arg0, "nstatus", arg1));
		studyIdentity.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		studyIdentity.setStudyidentyNameExists(getBoolean(arg0, "isStudyidentyNameExists", arg1));
		studyIdentity.setCodeExists(getBoolean(arg0, "isCodeExists", arg1));
		return studyIdentity;
	}
}