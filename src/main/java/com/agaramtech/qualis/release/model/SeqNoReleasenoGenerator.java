package com.agaramtech.qualis.release.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

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
@Table(name = "seqnoreleasenogenerator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoReleasenoGenerator extends CustomizedResultsetRowMapper<SeqNoReleasenoGenerator>
		implements Serializable, RowMapper<SeqNoReleasenoGenerator> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nseqnoreleasenogencode", nullable = false)
	private int nseqnoreleasenogencode;

	@ColumnDefault("0")
	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String sreleaseformat;

	@Transient
	private transient Instant dseqresetdate;

	@Transient
	private transient short nperiodcode = 4;

	@Transient
	private transient int nsubsamplesequenceno;

	@Transient
	private transient Map<String, Object> jsondata;

	@Transient
	private transient int nregsubtypeversionreleasecode;

	@Override
	public SeqNoReleasenoGenerator mapRow(ResultSet arg0, int arg1) throws SQLException {

		SeqNoReleasenoGenerator objSeqNoReleasenoGenerator = new SeqNoReleasenoGenerator();
		objSeqNoReleasenoGenerator.setNseqnoreleasenogencode(getInteger(arg0, "nseqnoreleasenogencode", arg1));
		objSeqNoReleasenoGenerator.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objSeqNoReleasenoGenerator.setNstatus(getShort(arg0, "nstatus", arg1));
		objSeqNoReleasenoGenerator.setSreleaseformat(getString(arg0, "sreleaseformat", arg1));
		objSeqNoReleasenoGenerator.setNperiodcode(getShort(arg0, "nperiodcode", arg1));
		objSeqNoReleasenoGenerator.setDseqresetdate(getInstant(arg0, "dseqresetdate", arg1));
		objSeqNoReleasenoGenerator.setNsubsamplesequenceno(getInteger(arg0, "nsubsamplesequenceno", arg1));
		objSeqNoReleasenoGenerator.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objSeqNoReleasenoGenerator
				.setNregsubtypeversionreleasecode(getInteger(arg0, "nregsubtypeversionreleasecode", arg1));

		return objSeqNoReleasenoGenerator;
	}
}
