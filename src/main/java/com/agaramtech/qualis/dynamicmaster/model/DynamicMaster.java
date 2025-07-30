package com.agaramtech.qualis.dynamicmaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

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
@Data
@Table(name = "dynamicmaster")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DynamicMaster extends CustomizedResultsetRowMapper<DynamicMaster> implements Serializable, RowMapper<DynamicMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndynamicmastercode")
	private int ndynamicmastercode;

	@Column(name = "nformcode")
	private short nformcode;

	@Column(name = "ndesigntemplatemappingcode")
	private int ndesigntemplatemappingcode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Column(name = "nstatus")
	private short nstatus;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode")
	private short nsitecode;

	@Transient
	private transient String jsonstring;
	@Transient
	private transient String jsonuistring;
	@Transient
	private transient String sregtemplatename;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient Map<String, Object> screendesign;
	@Transient
	private transient List<Object> slideoutdesign;
	@Transient
	private transient String ssystemfilename;
	@Transient
	private transient String sfieldname;
	@Transient
	private transient String sfilename;

	@Override
	public DynamicMaster mapRow(ResultSet arg0, int arg1) throws SQLException {

		final DynamicMaster objReactRegistrationTemplate = new DynamicMaster();

		objReactRegistrationTemplate.setNdynamicmastercode(getInteger(arg0, "ndynamicmastercode", arg1));
		objReactRegistrationTemplate.setNformcode(getShort(arg0, "nformcode", arg1));
		objReactRegistrationTemplate.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objReactRegistrationTemplate.setSregtemplatename(getString(arg0, "sregtemplatename", arg1));
		objReactRegistrationTemplate.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objReactRegistrationTemplate.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objReactRegistrationTemplate.setJsonstring(getString(arg0,"jsonstring", arg1));
		objReactRegistrationTemplate.setJsonuistring(getString(arg0,"jsonuistring", arg1));
		objReactRegistrationTemplate.setNstatus(getShort(arg0, "nstatus", arg1));
		objReactRegistrationTemplate.setScreendesign(getJsonObject(arg0, "screendesign", arg1));
		objReactRegistrationTemplate.setSlideoutdesign(getJSONArray(arg0, "slideoutdesign", arg1));
		objReactRegistrationTemplate.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objReactRegistrationTemplate.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objReactRegistrationTemplate.setSsystemfilename(getString(arg0, "ssystemfilename", arg1));
		objReactRegistrationTemplate.setSfieldname(getString(arg0, "sfieldname", arg1));
		objReactRegistrationTemplate.setSfilename(getString(arg0, "sfilename", arg1));

		return objReactRegistrationTemplate;
	}

}
