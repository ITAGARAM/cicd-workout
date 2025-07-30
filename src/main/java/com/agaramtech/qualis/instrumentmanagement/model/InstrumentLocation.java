package com.agaramtech.qualis.instrumentmanagement.model;

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

/**
 * This class is used to map the fields of 'Instrument location' table of the
 * Database.
 */
@Entity
@Table(name = "instrumentlocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentLocation extends CustomizedResultsetRowMapper<InstrumentLocation>
		implements Serializable, RowMapper<InstrumentLocation> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentlocationcode")
	private int ninstrumentlocationcode = -1;

	@Column(name = "sinstrumentlocationname", length = 100, nullable = false)
	private String sinstrumentlocationname = "";

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sdisplaystatus = "";

	@Transient
	private transient String smodifieddate = "";

	@Override
	public InstrumentLocation mapRow(ResultSet arg0, int arg1) throws SQLException {
		final InstrumentLocation instrumentlocation = new InstrumentLocation();
		instrumentlocation.setNinstrumentlocationcode(getInteger(arg0, "ninstrumentlocationcode", arg1));
		instrumentlocation.setSinstrumentlocationname(StringEscapeUtils.unescapeJava(getString(arg0, "sinstrumentlocationname", arg1)));
		instrumentlocation.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		instrumentlocation.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		instrumentlocation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		instrumentlocation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		instrumentlocation.setNstatus(getShort(arg0, "nstatus", arg1));
		instrumentlocation.setSdisplaystatus(getString(arg0, "sdisplaystatus", arg1));
		return instrumentlocation;
	}

}
