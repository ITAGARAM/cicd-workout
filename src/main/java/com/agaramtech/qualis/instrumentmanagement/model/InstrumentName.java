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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instrumentname")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentName extends CustomizedResultsetRowMapper<InstrumentName> implements Serializable, RowMapper<InstrumentName> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentnamecode")
	private int ninstrumentnamecode;

	@Column(name = "sinstrumentname", length = 100, nullable = false)
	private String sinstrumentname;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public InstrumentName mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final InstrumentName objInstrumentName = new InstrumentName();

		objInstrumentName.setNinstrumentnamecode(getInteger(arg0, "ninstrumentnamecode", arg1));
		objInstrumentName.setSinstrumentname(StringEscapeUtils.unescapeJava(getString(arg0, "sinstrumentname", arg1)));
		objInstrumentName.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objInstrumentName.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objInstrumentName.setNstatus(getShort(arg0, "nstatus", arg1));
		return objInstrumentName;
	}

}
