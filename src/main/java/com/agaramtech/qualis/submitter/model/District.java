package com.agaramtech.qualis.submitter.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
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

/**
 * This class is used to map the fields of 'district' table of the Database.
 */
@Entity
@Table(name = "district")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class District extends CustomizedResultsetRowMapper<District> implements Serializable, RowMapper<District> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndistrictcode")
	private int ndistrictcode;

	@Column(name = "sdistrictname", length = 100, nullable = false)
	private String sdistrictname;

	@Column(name = "sdistrictcode", length = 5, nullable = false)
	private String sdistrictcode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nregioncode")
	private int nregioncode;

	@Transient
	private transient String sregionname;

	@Transient
	private transient String smodifieddate;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;

	@Override
	public District mapRow(ResultSet arg0, int arg1) throws SQLException {

		final District objDistrict = new District();

		objDistrict.setNdistrictcode(getInteger(arg0, "ndistrictcode", arg1));
		objDistrict.setSdistrictname(StringEscapeUtils.unescapeJava(getString(arg0, "sdistrictname", arg1)));
		objDistrict.setSdistrictcode(StringEscapeUtils.unescapeJava(getString(arg0, "sdistrictcode", arg1)));
		objDistrict.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objDistrict.setNstatus(getShort(arg0, "nstatus", arg1));
		objDistrict.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objDistrict.setNregioncode(getInteger(arg0, "nregioncode", arg1));
		objDistrict.setSregionname(getString(arg0, "sregionname", arg1));
		objDistrict.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		objDistrict.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));

		return objDistrict;
	}

}