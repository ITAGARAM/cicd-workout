package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "dashboarddesignconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DashBoardDesignConfig extends CustomizedResultsetRowMapper<DashBoardDesignConfig>
		implements Serializable, RowMapper<DashBoardDesignConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndashboarddesigncode")
	private int ndashboarddesigncode;

	@Column(name = "ndashboardtypecode", nullable = false)
	private int ndashboardtypecode;

	@Column(name = "ndesigncomponentcode", nullable = false)
	private short ndesigncomponentcode = -1;

	@Column(name = "nsqlquerycode", nullable = false)
	private int nsqlquerycode = -1;

	@Column(name = "nmandatory", nullable = false)
	private short nmandatory = 3;

	@Column(name = "sfieldname", length = 100, nullable = false)
	private String sfieldname;

	@Column(name = "sdisplayname", length = 100, nullable = false)
	private String sdisplayname;

	@Column(name = "sdefaultvalue", length = 255)
	private String sdefaultvalue = "";

	@ColumnDefault("0")
	@Column(name = "ndays", nullable = false)
	private short ndays = (short) Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdesigncomponentname;
	@Transient
	private transient String ssqlqueryname;
	@Transient
	private transient String smandatory;
	@Transient
	private transient String ssqlquery;
	@Transient
	private transient String svaluemember;
	@Transient
	private transient String sdisplaymember;
	@Transient
	private transient List<?> dataList = new ArrayList<>();

	@Override
	public DashBoardDesignConfig mapRow(ResultSet arg0, int arg1) throws SQLException {

		final DashBoardDesignConfig dashBoardDesignConfig = new DashBoardDesignConfig();

		dashBoardDesignConfig.setNdashboarddesigncode(getInteger(arg0, "ndashboarddesigncode", arg1));
		dashBoardDesignConfig.setNdashboardtypecode(getInteger(arg0, "ndashboardtypecode", arg1));
		dashBoardDesignConfig.setNdesigncomponentcode(getShort(arg0, "ndesigncomponentcode", arg1));
		dashBoardDesignConfig.setNsqlquerycode(getInteger(arg0, "nsqlquerycode", arg1));
		dashBoardDesignConfig.setNmandatory(getShort(arg0, "nmandatory", arg1));
		dashBoardDesignConfig.setSfieldname(StringEscapeUtils.unescapeJava(getString(arg0, "sfieldname", arg1)));
		dashBoardDesignConfig.setSdisplayname(StringEscapeUtils.unescapeJava(getString(arg0, "sdisplayname", arg1)));
		dashBoardDesignConfig.setNdays(getShort(arg0, "ndays", arg1));
		dashBoardDesignConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		dashBoardDesignConfig.setSdesigncomponentname(getString(arg0, "sdesigncomponentname", arg1));
		dashBoardDesignConfig.setSsqlqueryname(getString(arg0, "ssqlqueryname", arg1));
		dashBoardDesignConfig.setSsqlquery(getString(arg0, "ssqlquery", arg1));
		dashBoardDesignConfig.setSmandatory(getString(arg0, "smandatory", arg1));
		dashBoardDesignConfig.setSvaluemember(getString(arg0, "svaluemember", arg1));
		dashBoardDesignConfig.setSdisplaymember(getString(arg0, "sdisplaymember", arg1));
		dashBoardDesignConfig.setSdefaultvalue(StringEscapeUtils.unescapeJava(getString(arg0, "sdefaultvalue", arg1)));
		dashBoardDesignConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		dashBoardDesignConfig.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return dashBoardDesignConfig;
	}

}
