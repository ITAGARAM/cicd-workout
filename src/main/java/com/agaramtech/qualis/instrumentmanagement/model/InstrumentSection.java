package com.agaramtech.qualis.instrumentmanagement.model;

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
@Table(name = "instrumentsection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class InstrumentSection extends CustomizedResultsetRowMapper<InstrumentSection> implements  RowMapper<InstrumentSection> {

	@Id
	@Column(name = "ninstrumentsectioncode")
	private int ninstrumentsectioncode;
	
	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "nsectioncode", nullable = false)
	private int nsectioncode;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name="dmodifieddate",nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String ssectionname;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String susername;

	@Override
	public InstrumentSection mapRow(ResultSet arg0, int arg1) throws SQLException {

		final InstrumentSection objInstrumentSec = new InstrumentSection();

		objInstrumentSec.setNinstrumentsectioncode(getInteger(arg0,"ninstrumentsectioncode",arg1));
		objInstrumentSec.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objInstrumentSec.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objInstrumentSec.setNusercode(getInteger(arg0,"nusercode",arg1));
		objInstrumentSec.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objInstrumentSec.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objInstrumentSec.setNstatus(getShort(arg0,"nstatus",arg1));
		objInstrumentSec.setSsectionname(getString(arg0,"ssectionname",arg1));
		objInstrumentSec.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objInstrumentSec.setSusername(getString(arg0,"susername",arg1));
		objInstrumentSec.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return objInstrumentSec;
	}
	

}
