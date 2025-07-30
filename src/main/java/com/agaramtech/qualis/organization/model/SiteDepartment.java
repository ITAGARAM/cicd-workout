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
@Table(name = "sitedepartment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SiteDepartment extends CustomizedResultsetRowMapper<SiteDepartment> implements Serializable, RowMapper<SiteDepartment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitedeptcode")
	private int nsitedeptcode;

	@Column(name = "nsitecode", nullable = false)
	private short nsitecode;

	@Column(name = "ndeptcode", nullable = false)
	private int ndeptcode;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdeptname;
	@Transient
	private transient String ssitename;

	@Override
	public SiteDepartment mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SiteDepartment siteDepartment = new SiteDepartment();
		siteDepartment.setNdeptcode(getInteger(arg0, "ndeptcode", arg1));
		siteDepartment.setNsitecode(getShort(arg0, "nsitecode", arg1));
		siteDepartment.setNstatus(getShort(arg0, "nstatus", arg1));
		siteDepartment.setSdeptname(getString(arg0, "sdeptname", arg1));
		siteDepartment.setSsitename(getString(arg0, "sdeptname", arg1));
		siteDepartment.setNsitedeptcode(getInteger(arg0, "nsitedeptcode", arg1));
		siteDepartment.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return siteDepartment;
	}

}
