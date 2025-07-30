package com.agaramtech.qualis.organization.model;

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
@Table(name = "sectionusers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

/**
 * This class is used to map the fields of 'sectionusers' table of the Database.
 * 
 * @author ATE153
 * @version 9.0.0.1
 * @since 22- Nov- 2020
 */
public class SectionUsers extends CustomizedResultsetRowMapper<SectionUsers> implements Serializable, RowMapper<SectionUsers> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsectionusercode")
	private int nsectionusercode;

	@Column(name = "nlabsectioncode", nullable = false)
	private int nlabsectioncode;

	@Column(name = "nusercode", nullable = false)
	private int nusercode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient  String sfirstname;
	@Transient
	private transient  String sdesignationname;
	@Transient
	private transient  String susername;
	@Transient
	private transient  String ssectionname;
	@Transient
	private transient  String slabname;
	@Transient
	private transient  String sdeptname;
	@Transient
	private transient  String ssitename;
	@Transient
	private transient  String sempid;

	@Override
	public SectionUsers mapRow(ResultSet arg0, int arg1) throws SQLException {
		SectionUsers sectionUsers = new SectionUsers();
		sectionUsers.setNusercode(getInteger(arg0, "nusercode", arg1));
		sectionUsers.setNsectionusercode(getInteger(arg0, "nsectionusercode", arg1));
		sectionUsers.setSusername(getString(arg0, "susername", arg1));
		sectionUsers.setNstatus(getShort(arg0, "nstatus", arg1));
		sectionUsers.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sectionUsers.setNlabsectioncode(getInteger(arg0, "nlabsectioncode", arg1));
		sectionUsers.setSfirstname(getString(arg0, "sfirstname", arg1));
		sectionUsers.setSdesignationname(getString(arg0, "sdesignationname", arg1));
		sectionUsers.setSsectionname(getString(arg0, "ssectionname", arg1));
		sectionUsers.setSlabname(getString(arg0, "slabname", arg1));
		sectionUsers.setSdeptname(getString(arg0, "sdeptname", arg1));
		sectionUsers.setSsitename(getString(arg0, "ssitename", arg1));
		sectionUsers.setSempid(getString(arg0, "sempid", arg1));
		sectionUsers.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return sectionUsers;
	}
}
