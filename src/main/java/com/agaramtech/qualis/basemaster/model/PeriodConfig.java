package com.agaramtech.qualis.basemaster.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'periodconfig' table of the Database.
 * 
 * @author ATE175
 * @version 9.0.0.1
 * @since 12- April- 2021
 */
@Entity
@Table(name = "periodconfig")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PeriodConfig extends CustomizedResultsetRowMapper<PeriodConfig>
		implements Serializable, RowMapper<PeriodConfig> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nperiodconfigcode")
	private short nperiodconfigcode;

	@Column(name = "nformcode", nullable = false)
	private short nformcode;

	@Column(name = "ncontrolcode", nullable = false)
	private short ncontrolcode;

	@Column(name = "nperiodcode", nullable = false)
	private short nperiodcode;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public PeriodConfig mapRow(ResultSet arg0, int arg1) throws SQLException {
		final PeriodConfig periodConfig = new PeriodConfig();
		periodConfig.setNperiodconfigcode(getShort(arg0, "nperiodconfigcode", arg1));
		periodConfig.setNperiodcode(getShort(arg0, "nperiodcode", arg1));
		periodConfig.setNcontrolcode(getShort(arg0, "ncontrolcode", arg1));
		periodConfig.setNformcode(getShort(arg0, "nformcode", arg1));
		periodConfig.setNstatus(getShort(arg0, "nstatus", arg1));
		periodConfig.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		periodConfig.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return periodConfig;
	}

}