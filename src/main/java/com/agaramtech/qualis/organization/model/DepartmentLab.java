package com.agaramtech.qualis.organization.model;

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

@Entity
@Table(name = "departmentlab")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class DepartmentLab extends CustomizedResultsetRowMapper<DepartmentLab>
		implements Serializable, RowMapper<DepartmentLab> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ndeptlabcode")
	private int ndeptlabcode;

	@Column(name = "nsitedeptcode", nullable = false)
	private int nsitedeptcode;

	@Column(name = "nlabcode", nullable = false)
	private int nlabcode;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Transient
	private transient String slabname;

	@Transient
	private transient String sdeptname;

	@Override
	public DepartmentLab mapRow(ResultSet arg0, int arg1) throws SQLException {
		final DepartmentLab departmentLab = new DepartmentLab();
		departmentLab.setNdeptlabcode(getInteger(arg0, "ndeptlabcode", arg1));
		departmentLab.setNlabcode(getInteger(arg0, "nlabcode", arg1));
		departmentLab.setNsitedeptcode(getInteger(arg0, "nsitedeptcode", arg1));
		departmentLab.setSlabname(getString(arg0, "slabname", arg1));
		departmentLab.setSdeptname(getString(arg0, "sdeptname", arg1));
		departmentLab.setNstatus(getShort(arg0, "nstatus", arg1));
		departmentLab.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		departmentLab.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return departmentLab;
	}

}
