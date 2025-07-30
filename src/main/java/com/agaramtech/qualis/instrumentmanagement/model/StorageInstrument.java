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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "storageinstrument")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class StorageInstrument extends CustomizedResultsetRowMapper<StorageInstrument> implements  RowMapper<StorageInstrument> {

	@Id
	@Column(name = "nstorageinstrumentcode")
	private int nstorageinstrumentcode;
	
	@Column(name = "nsamplestoragelocationcode", nullable = false)
	private int nsamplestoragelocationcode;
	
	@Column(name = "nsamplestorageversioncode", nullable = false)
	private int nsamplestorageversioncode;
	
	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "nregionalsitecode", nullable = false)
	private int nregionalsitecode;
	
	@Column(name="dmodifieddate",nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	

	@Override
	public StorageInstrument mapRow(ResultSet arg0, int arg1) throws SQLException {

		final StorageInstrument objStorageInstrument = new StorageInstrument();

		objStorageInstrument.setNstorageinstrumentcode(getInteger(arg0,"nstorageinstrumentcode",arg1));
		objStorageInstrument.setNsamplestoragelocationcode(getInteger(arg0,"nsamplestoragelocationcode",arg1));
		objStorageInstrument.setNsamplestorageversioncode(getInteger(arg0,"nsamplestorageversioncode",arg1));
		objStorageInstrument.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objStorageInstrument.setNregionalsitecode(getInteger(arg0,"nregionalsitecode",arg1));
		objStorageInstrument.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objStorageInstrument.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objStorageInstrument.setNstatus(getShort(arg0,"nstatus",arg1));

		return objStorageInstrument;
	}
	

}
