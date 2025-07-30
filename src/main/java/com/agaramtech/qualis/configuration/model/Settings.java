package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Settings extends CustomizedResultsetRowMapper<Settings> implements Serializable, RowMapper<Settings> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsettingcode")
	private short nsettingcode;

	@Column(name = "ssettingname", length = 100, nullable = false)
	private String ssettingname;

	@Column(name = "ssettingvalue", length = 200, nullable = false)
	private String ssettingvalue;

	@Column(name = "nisvisible", nullable = false)
	private short nisvisible;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient int nsitecode;

	@Override
	public Settings mapRow(ResultSet arg0, int arg1) throws SQLException {
		Settings settings = new Settings();
		settings.setNsettingcode(getShort(arg0, "nsettingcode", arg1));
		settings.setSsettingname(getString(arg0, "ssettingname", arg1));
		settings.setSsettingvalue(getString(arg0, "ssettingvalue", arg1));
		settings.setNstatus(getShort(arg0, "nstatus", arg1));
		settings.setNisvisible(getShort(arg0, "nisvisible", arg1));
		settings.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		settings.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		settings.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		return settings;
	}
}
