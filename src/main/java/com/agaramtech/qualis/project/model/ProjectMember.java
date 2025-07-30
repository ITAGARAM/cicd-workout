package com.agaramtech.qualis.project.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
@Table(name = "projectmember")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectMember extends CustomizedResultsetRowMapper<ProjectMember> implements Serializable, RowMapper<ProjectMember> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nprojectmembercode")
	private int nprojectmembercode;

	@Column(name = "nprojectmastercode")
	private int nprojectmastercode;

	@Column(name = "nusercode")
	private int nusercode;

	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sloginid;

	@Transient
	private transient String steammembername;

	public ProjectMember mapRow(ResultSet arg0, int arg1) throws SQLException {

		ProjectMember objProjectMember = new ProjectMember();

		objProjectMember.setNprojectmembercode(getInteger(arg0, "nprojectmembercode", arg1));
		objProjectMember.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objProjectMember.setNusercode(getInteger(arg0, "nusercode", arg1));
		objProjectMember.setDmodifieddate(getDate(arg0, "dmodifieddate", arg1));
		objProjectMember.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProjectMember.setNstatus(getShort(arg0, "nstatus", arg1));
		objProjectMember.setSloginid(getString(arg0, "sloginid", arg1));
		objProjectMember.setSteammembername(getString(arg0, "steammembername", arg1));

		return objProjectMember;

	}

}