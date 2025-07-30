package com.agaramtech.qualis.submitter.model;

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
@Table(name = "region")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Region extends CustomizedResultsetRowMapper<Region> implements Serializable, RowMapper<Region> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nregioncode")
	private int nregioncode;

	@Column(name = "sregionname", length = 100, nullable = false)
	private String sregionname;

	@Column(name = "sregioncode", length = 5, nullable = false)
	private String sregioncode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Transient
	private transient String smodifieddate;

	@Override
	public Region mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Region objRegion = new Region();
		
		objRegion.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objRegion.setSregionname(StringEscapeUtils.unescapeJava(getString(arg0, "sregionname", arg1)));
		objRegion.setSregioncode(StringEscapeUtils.unescapeJava(getString(arg0, "sregioncode", arg1)));
		objRegion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objRegion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objRegion.setNstatus(getShort(arg0, "nstatus", arg1));
		objRegion.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		
		return objRegion;
	}

}
