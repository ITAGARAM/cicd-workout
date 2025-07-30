package com.agaramtech.qualis.credential.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
@Table(name = "limselnusermapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LimsElnUserMapping extends CustomizedResultsetRowMapper<LimsElnUserMapping>
		implements Serializable, RowMapper<LimsElnUserMapping> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nelnusermappingcode")
	private int nelnusermappingcode = -1;
	@Column(name = "nelncode")
	private int nelncode = -1;
	@Column(name = "nlimsusercode")
	private int nlimsusercode = -1;
	@Column(name = "nelnusergroupcode")
	private int nelnusergroupcode = -1;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "selnusername", length = 510, nullable = false)
	private String selnusername;
	@Column(name = "selnuserid", length = 510, nullable = false)
	private String selnuserid;
	@Transient
	private String sloginid;
	@Transient
	private String slimsusername;
	@Transient
	private String username;
	@Transient
	private int usercode = -1;
	@Transient
	private String userfullname;

	@Override
	public LimsElnUserMapping mapRow(final ResultSet arg0,final int arg1) throws SQLException {
		final LimsElnUserMapping reason = new LimsElnUserMapping();
		reason.setNelnusermappingcode(getShort(arg0, "nelnusermappingcode", arg1));
		reason.setNelncode(getShort(arg0, "nelncode", arg1));
		reason.setNlimsusercode(getShort(arg0, "nlimsusercode", arg1));
		reason.setSelnusername(StringEscapeUtils.unescapeJava(getString(arg0, "selnusername", arg1)));
		reason.setNsitecode(getShort(arg0, "nsitecode", arg1));
		reason.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		reason.setNstatus(getShort(arg0, "nstatus", arg1));
		reason.setSloginid(getString(arg0, "sloginid", arg1));
		reason.setSlimsusername(getString(arg0, "slimsusername", arg1));
		reason.setUsername(getString(arg0, "username", arg1));
		reason.setUsercode(getInteger(arg0, "usercode", arg1));
		reason.setUserfullname(getString(arg0, "userfullname", arg1));
		reason.setSelnuserid(StringEscapeUtils.unescapeJava(getString(arg0, "selnuserid", arg1)));
		reason.setNelnusergroupcode(getInteger(arg0, "nelnusergroupcode", arg1));
		return reason;
	}

}
