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
@Table(name = "mfasetting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MfaSettings extends CustomizedResultsetRowMapper<MfaSettings> implements Serializable, RowMapper<MfaSettings> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nmfasettingcode")
	private short nmfasettingcode;

	@Column(name = "smfasettingname", length = 100, nullable = false)
	private String smfasettingname;

	@Column(name = "smfasettingvalue", length = 200, nullable = false)
	private String smfasettingvalue;

//	@Column(name = "nisvisible", nullable = false)
//	private short nisvisible;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String smodifieddate;

	@Transient
	private transient int nsitecode;

	@Override
	public MfaSettings mapRow(ResultSet arg0, int arg1) throws SQLException {
		MfaSettings objMfaSetting = new MfaSettings();
		objMfaSetting.setNmfasettingcode(getShort(arg0, "nmfasettingcode", arg1));
		objMfaSetting.setSmfasettingname(getString(arg0, "smfasettingname", arg1));
		objMfaSetting.setSmfasettingvalue(getString(arg0, "smfasettingvalue", arg1));
		objMfaSetting.setNstatus(getShort(arg0, "nstatus", arg1));
		//objMfaSetting.setNisvisible(getShort(arg0, "nisvisible", arg1));
		objMfaSetting.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objMfaSetting.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objMfaSetting.setNsitecode(getInteger(arg0, "nsitecode", arg1));
		return objMfaSetting;
	}
}
