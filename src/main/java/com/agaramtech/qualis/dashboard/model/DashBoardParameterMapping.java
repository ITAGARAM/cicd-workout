package com.agaramtech.qualis.dashboard.model;

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
@Table(name = "dashboardparametermapping")
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardParameterMapping extends CustomizedResultsetRowMapper<DashBoardParameterMapping>
		implements Serializable, RowMapper<DashBoardParameterMapping> {

	@Id
	@Column(name = "ndashboardparametermappingcode")
	private int ndashboardparametermappingcode;

	@Column(name = "ndashboardtypecode", nullable = false)
	private int ndashboardtypecode;

	@Column(name = "nparentdashboarddesigncode", nullable = false)
	private int nparentdashboarddesigncode;

	@Column(name = "sfieldname", length = 100, nullable = false)
	private String sfieldname;

	@Column(name = "nchilddashboarddesigncode", nullable = false)
	private int nchilddashboarddesigncode;

	@ColumnDefault("4")
	@Column(name = "nisactionparent", nullable = false)
	private short nisactionparent = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sparentparametername;
	@Transient
	private transient String schildparametername;
	@Transient
	private transient String sisactionparent;
	@Transient
	private transient String ssqlquery;

	@Override
	public DashBoardParameterMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		final DashBoardParameterMapping objParamMapping = new DashBoardParameterMapping();

		objParamMapping.setNdashboardparametermappingcode(getInteger(arg0, "ndashboardparametermappingcode", arg1));
		objParamMapping.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		objParamMapping.setNparentdashboarddesigncode(getInteger(arg0, "nparentdashboarddesigncode", arg1));
		objParamMapping.setNchilddashboarddesigncode(getShort(arg0, "nchilddashboarddesigncode", arg1));
		objParamMapping.setNstatus(getShort(arg0, "nstatus", arg1));
		objParamMapping.setSfieldname(StringEscapeUtils.unescapeJava(getString(arg0, "sfieldname", arg1)));
		objParamMapping.setSchildparametername(getString(arg0, "schildparametername", arg1));
		objParamMapping.setSparentparametername(getString(arg0, "sparentparametername", arg1));
		objParamMapping.setNisactionparent(getShort(arg0, "nisactionparent", arg1));
		objParamMapping.setSisactionparent(getString(arg0, "sisactionparent", arg1));
		objParamMapping.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		objParamMapping.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objParamMapping.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return objParamMapping;
	}

}
