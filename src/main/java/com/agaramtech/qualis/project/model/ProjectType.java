package com.agaramtech.qualis.project.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.text.StringEscapeUtils;
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
 * This class is used to map the fields of 'Project Type' table of the Database.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "projecttype")
@Data
public class ProjectType extends CustomizedResultsetRowMapper<ProjectType> implements Serializable, RowMapper<ProjectType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Column(name = "sprojecttypename", length = 50, nullable = false)
	private String sprojecttypename;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	public ProjectType mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ProjectType projectType = new ProjectType();
		projectType.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		projectType.setSprojecttypename(StringEscapeUtils.unescapeJava(getString(arg0, "sprojecttypename", arg1)));
		projectType.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		projectType.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		projectType.setDmodifieddate(getDate(arg0, "dmodifieddate", arg1));
		projectType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		projectType.setNstatus(getShort(arg0, "nstatus", arg1));
		return projectType;
	}
}