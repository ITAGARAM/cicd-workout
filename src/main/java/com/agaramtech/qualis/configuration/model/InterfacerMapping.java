package com.agaramtech.qualis.configuration.model;

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

/**
 * @author Janakumar R
 *
 */

@Entity
@Table(name = "interfacermapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InterfacerMapping extends CustomizedResultsetRowMapper<InterfacerMapping>
		implements Serializable, RowMapper<InterfacerMapping> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninterfacemappingcode")
	private int ninterfacemappingcode;

	@Column(name = "stestname")
	private String stestname = "";

	@Column(name = "ntestcode")
	private int ntestcode;

	@Column(name = "ninterfacetypecode")
	private int ninterfacetypecode;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Transient
	private transient String sinterfacetypename;

	@Transient
	private transient String sinterfacertestname;

	@Transient
	private transient String stestsynonym;

	@Transient
	private transient String sinterfacetype;

	@Transient
	private transient boolean isinterfacername;

	@Transient
	private transient boolean istestcode;

	@Override
	public InterfacerMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InterfacerMapping interfacermapping = new InterfacerMapping();
		interfacermapping.setNinterfacemappingcode(getInteger(arg0, "ninterfacemappingcode", arg1));
		interfacermapping.setStestname(getString(arg0, "stestname", arg1));
		interfacermapping.setNtestcode(getInteger(arg0, "ntestcode", arg1));
		interfacermapping.setNinterfacetypecode(getInteger(arg0, "ninterfacetypecode", arg1));
		interfacermapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		interfacermapping.setNsitecode(getShort(arg0, "nsitecode", arg1));
		interfacermapping.setNstatus(getShort(arg0, "nstatus", arg1));
		interfacermapping.setSinterfacetypename(getString(arg0, "sinterfacetypename", arg1));
		interfacermapping.setSinterfacertestname(getString(arg0, "sinterfacertestname", arg1));
		interfacermapping.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		interfacermapping.setSinterfacetype(getString(arg0, "sinterfacetype", arg1));
		interfacermapping.setIsinterfacername(getBoolean(arg0, "isinterfacername", arg1));
		interfacermapping.setIstestcode(getBoolean(arg0, "istestcode", arg1));
		return interfacermapping;
	}
}
