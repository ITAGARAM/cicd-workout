package com.agaramtech.qualis.misrights.model;


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
 * This class is used to map the fields of 'reportrights' table of the
 * Database.
 * @author ATE234
 * @version 9.0.0.1
 * @since 17-04- 2025
 */

@Entity
@Table(name = "reportrights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReportRights extends CustomizedResultsetRowMapper<ReportRights> implements Serializable, RowMapper<ReportRights> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nreportrightscode")
	private int nreportrightscode;

	@Column(name = "nreportcode", nullable = false)
	private int nreportcode;

	@Column(name = "nuserrolecode", nullable = false)
	private int nuserrolecode;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sreportname;

	@Override
	public ReportRights mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ReportRights reportRights = new ReportRights();

		reportRights.setNstatus(getShort(arg0, "nstatus", arg1));
		reportRights.setNsitecode(getShort(arg0, "nsitecode", arg1));
		reportRights.setNreportrightscode(getInteger(arg0, "nreportrightscode", arg1));
		reportRights.setNreportcode(getInteger(arg0, "nreportcode", arg1));
		reportRights.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		reportRights.setSreportname(getString(arg0, "sreportname", arg1));
		reportRights.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		return reportRights;

	}

}
