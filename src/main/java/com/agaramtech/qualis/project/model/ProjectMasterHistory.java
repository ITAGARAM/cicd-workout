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

@Entity
@Table(name = "projectmasterhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectMasterHistory extends CustomizedResultsetRowMapper<ProjectMasterHistory>
		implements Serializable, RowMapper<ProjectMasterHistory> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprojectmasterhistorycode")
	private int nprojectmasterhistorycode;

	@Column(name = "nprojectmastercode", nullable = false)
	private int nprojectmastercode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "ndeputyusercode", nullable = false)
	private int ndeputyusercode;

	@Column(name = "ndeputyuserrolecode", nullable = false)
	private int ndeputyuserrolecode;

	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus;

	@Column(name = "sremarks", length = 500)
	private String sremarks="";

	@Column(name = "dtransactiondate", nullable = false)
	private Date dtransactiondate;

	@Column(name = "noffsetdtransactiondate")
	private int noffsetdtransactiondate;

	@Column(name = "ntransdatetimezonecode")
	private int ntransdatetimezonecode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdtransactiondate;

	@Transient
	private transient String susername;

	@Transient
	private transient String suserrolename;

	@Transient
	private transient String stransdisplaystatus;

	public ProjectMasterHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		ProjectMasterHistory objProjectMasterHistory = new ProjectMasterHistory();
		objProjectMasterHistory.setNprojectmasterhistorycode(getInteger(arg0, "nprojectmasterhistorycode", arg1));
		objProjectMasterHistory.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objProjectMasterHistory.setNusercode(getInteger(arg0, "nusercode", arg1));
		objProjectMasterHistory.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objProjectMasterHistory.setNdeputyusercode(getInteger(arg0, "ndeputyusercode", arg1));
		objProjectMasterHistory.setNdeputyuserrolecode(getInteger(arg0, "ndeputyuserrolecode", arg1));
		objProjectMasterHistory.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
		objProjectMasterHistory.setDtransactiondate(getDate(arg0, "dtransactiondate", arg1));
		objProjectMasterHistory.setNstatus(getShort(arg0, "nstatus", arg1));
		objProjectMasterHistory.setNoffsetdtransactiondate(getInteger(arg0, "noffsetdtransactiondate", arg1));
		objProjectMasterHistory.setNtransdatetimezonecode(getInteger(arg0, "ntransdatetimezonecode", arg1));
		objProjectMasterHistory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objProjectMasterHistory.setSdtransactiondate(getString(arg0, "sdtransactiondate", arg1));
		objProjectMasterHistory.setSusername(getString(arg0, "susername", arg1));
		objProjectMasterHistory.setSuserrolename(getString(arg0, "suserrolename", arg1));
		objProjectMasterHistory.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objProjectMasterHistory.setSremarks(StringEscapeUtils.unescapeJava(getString(arg0, "sremarks", arg1)));

		return objProjectMasterHistory;
	}

}
