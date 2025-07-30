package com.agaramtech.qualis.storagemanagement.model;

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

@Entity
@Table(name = "samplestoragemaintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageMaintenance extends CustomizedResultsetRowMapper<SampleStorageMaintenance>
		implements Serializable, RowMapper<SampleStorageMaintenance> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nmaintenancecode")
	private short nmaintenancecode;

	@Column(name = "scontainertraycode")
	private String scontainertraycode = "";

	@Column(name = "damcperiodfrom")
	private Instant damcperiodfrom;

	@Column(name = "damcperiodto")
	private Instant damcperiodto;

	@Column(name = "dopendate")
	private Instant dopendate;

	@Column(name = "dclosedate")
	private Instant dclosedate;

	@Column(name = "nmaintenancestatus")
	private short nmaintenancestatus;

	@Column(name = "sreason")
	private String sreason = "";

	@Column(name = "sclosereason")
	private String sclosereason = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SampleStorageMaintenance mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageMaintenance sampleStorageMaintenance = new SampleStorageMaintenance();
		sampleStorageMaintenance.setNmaintenancecode(getShort(arg0, "nmaintenancecode", arg1));
		sampleStorageMaintenance.setScontainertraycode(getString(arg0, "scontainertraycode", arg1));
		sampleStorageMaintenance.setDamcperiodfrom(getInstant(arg0, "damcperiodfrom", arg1));
		sampleStorageMaintenance.setDamcperiodto(getInstant(arg0, "damcperiodto", arg1));
		sampleStorageMaintenance.setDopendate(getInstant(arg0, "dopendate", arg1));
		sampleStorageMaintenance.setDclosedate(getInstant(arg0, "dclosedate", arg1));
		sampleStorageMaintenance.setNmaintenancestatus(getShort(arg0, "nmaintenancestatus", arg1));
		sampleStorageMaintenance.setSreason(getString(arg0, "sreason", arg1));
		sampleStorageMaintenance.setSclosereason(getString(arg0, "sclosereason", arg1));
		sampleStorageMaintenance.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleStorageMaintenance.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleStorageMaintenance.setNstatus(getShort(arg0, "nstatus", arg1));
		return sampleStorageMaintenance;
	}

}
