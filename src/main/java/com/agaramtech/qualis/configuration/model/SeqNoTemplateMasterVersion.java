package com.agaramtech.qualis.configuration.model;

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
@Table(name = "seqnotemplatemasterversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SeqNoTemplateMasterVersion extends CustomizedResultsetRowMapper<SeqNoTemplateMasterVersion>
		implements Serializable, RowMapper<SeqNoTemplateMasterVersion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoTemplateMasterVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqNoTemplateMasterVersion objSeqNoTemplateMasterVersion = new SeqNoTemplateMasterVersion();
		objSeqNoTemplateMasterVersion.setStablename(getString(arg0, "stablename", arg1));
		objSeqNoTemplateMasterVersion.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objSeqNoTemplateMasterVersion.setNstatus(getShort(arg0, "nstatus", arg1));
		objSeqNoTemplateMasterVersion.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSeqNoTemplateMasterVersion.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objSeqNoTemplateMasterVersion;
	}
}
