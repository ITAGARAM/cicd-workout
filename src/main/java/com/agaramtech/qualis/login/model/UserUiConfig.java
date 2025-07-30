package com.agaramtech.qualis.login.model;

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
@Table(name = "useruiconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserUiConfig extends CustomizedResultsetRowMapper<UserUiConfig> implements Serializable, RowMapper<UserUiConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nusercode")
	private int nusercode;

	@ColumnDefault("-1")
	@Column(name = "nthemecolorcode", nullable = false)
	private short nthemecolorcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "sthemecolorhexcode", length = 100, nullable = false)
	private String sthemecolorhexcode;

	@ColumnDefault("3")
	@Column(name = "nfontsize", nullable = false)
	private short nfontsize = (short) Enumeration.TransactionStatus.YES.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sthemecolorname;

	@Override
	public UserUiConfig mapRow(final ResultSet arg0, final int arg1) throws SQLException {
		final UserUiConfig useruiconfig = new UserUiConfig();
		useruiconfig.setNusercode(getInteger(arg0, "nusercode", arg1));
		useruiconfig.setNthemecolorcode(getShort(arg0, "nthemecolorcode", arg1));
		useruiconfig.setSthemecolorhexcode(StringEscapeUtils.unescapeJava(getString(arg0, "sthemecolorhexcode", arg1)));
		useruiconfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		useruiconfig.setNfontsize(getShort(arg0, "nfontsize", arg1));
		useruiconfig.setNsitecode(getShort(arg0, "nsitecode", arg1));
		useruiconfig.setNstatus(getShort(arg0, "nstatus", arg1));
		useruiconfig.setSthemecolorname(getString(arg0, "sthemecolorname", arg1));
		return useruiconfig;
	}
}