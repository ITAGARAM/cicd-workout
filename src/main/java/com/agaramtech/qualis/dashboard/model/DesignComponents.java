package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

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

@Entity
@Table(name = "designcomponents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DesignComponents extends CustomizedResultsetRowMapper<DesignComponents>
		implements Serializable, RowMapper<DesignComponents> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndesigncomponentcode")
	private short ndesigncomponentcode;
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	@Column(name = "nsitecode")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdesigncomponentname;

	@Override
	public DesignComponents mapRow(ResultSet arg0, int arg1) throws SQLException {
		final DesignComponents designComponents = new DesignComponents();
		designComponents.setNdesigncomponentcode(getShort(arg0, "ndesigncomponentcode", arg1));
		designComponents.setSdesigncomponentname(getString(arg0, "sdesigncomponentname", arg1));
		designComponents.setNstatus(getShort(arg0, "nstatus", arg1));
		designComponents.setNsitecode(getShort(arg0, "nsitecode", arg1));
		designComponents.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		designComponents.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		designComponents.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		designComponents.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		return designComponents;
	}

}
