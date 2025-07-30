package com.agaramtech.qualis.release.model;

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
@Table(name = "seqnoformatgeneratorrelease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoFormatGeneratorRelease extends CustomizedResultsetRowMapper<SeqNoFormatGeneratorRelease>
		implements Serializable, RowMapper<SeqNoFormatGeneratorRelease> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;
	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "dseqresetdate", nullable = false)
	private Instant dseqresetdate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoFormatGeneratorRelease mapRow(ResultSet arg0, int arg1) throws SQLException {

		SeqNoFormatGeneratorRelease objSeqNoFormatGeneratorRelease = new SeqNoFormatGeneratorRelease();

		objSeqNoFormatGeneratorRelease.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objSeqNoFormatGeneratorRelease.setDseqresetdate(getInstant(arg0, "dseqresetdate", arg1));
		objSeqNoFormatGeneratorRelease.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSeqNoFormatGeneratorRelease.setNstatus(getShort(arg0, "nstatus", arg1));
		objSeqNoFormatGeneratorRelease.setStablename(getString(arg0, "stablename", arg1));

		return objSeqNoFormatGeneratorRelease;
	}

}
