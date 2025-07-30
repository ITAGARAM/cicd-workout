package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

/**
 * This class is used to map the fields of 'staticdashboardparameter' table of
 * the Database.
 */
@Entity
@Table(name = "staticdashboardparameter")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StaticDashBoardParameter extends CustomizedResultsetRowMapper<StaticDashBoardParameter>
		implements Serializable, RowMapper<StaticDashBoardParameter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nstaticdashboardparametercode")
	private int nstaticdashboardparametercode;

	@Column(name = "nstaticdashboardtypecode", nullable = false)
	private int nstaticdashboardtypecode;

	@Column(name = "ndesigncomponentcode", nullable = false)
	private int ndesigncomponentcode;

	@Column(name = "sfieldname", length = 100, nullable = false)
	private String sfieldname;

	@Column(name = "sdisplayname", length = 100, nullable = false)
	private String sdisplayname;

	@Column(name = "ndays", nullable = false)
	private int ndays;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Override
	public StaticDashBoardParameter mapRow(ResultSet rs, int rowNum) throws SQLException {
		final StaticDashBoardParameter staticDashBoardParameter = new StaticDashBoardParameter();
		staticDashBoardParameter
				.setNstaticdashboardparametercode(getInteger(rs, "nstaticdashboardparametercode", rowNum));
		staticDashBoardParameter.setNstaticdashboardtypecode(getInteger(rs, "nstaticdashboardtypecode", rowNum));
		staticDashBoardParameter.setNdesigncomponentcode(getInteger(rs, "ndesigncomponentcode", rowNum));
		staticDashBoardParameter.setSfieldname(getString(rs, "sfieldname", rowNum));
		staticDashBoardParameter.setSdisplayname(getString(rs, "sdisplayname", rowNum));
		staticDashBoardParameter.setNdays(getInteger(rs, "ndays", rowNum));
		staticDashBoardParameter.setNstatus(getShort(rs, "nstatus", rowNum));
		staticDashBoardParameter.setDmodifieddate(getInstant(rs, "dmodifieddate", rowNum));
		staticDashBoardParameter.setNsitecode(getShort(rs, "nsitecode", rowNum));
		return staticDashBoardParameter;
	}
}
