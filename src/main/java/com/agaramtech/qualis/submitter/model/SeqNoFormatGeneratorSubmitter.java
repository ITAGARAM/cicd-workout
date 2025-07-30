package com.agaramtech.qualis.submitter.model;

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
@Table(name = "seqnoformatgeneratorsubmitter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoFormatGeneratorSubmitter extends CustomizedResultsetRowMapper<SeqNoFormatGeneratorSubmitter>
		implements Serializable, RowMapper<SeqNoFormatGeneratorSubmitter> {

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
	public SeqNoFormatGeneratorSubmitter mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqNoFormatGeneratorSubmitter objseq = new SeqNoFormatGeneratorSubmitter();
		objseq.setStablename(StringEscapeUtils.unescapeJava(getString(arg0, "stablename", arg1)));
		objseq.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objseq.setDseqresetdate(getInstant(arg0, "dseqresetdate", arg1));
		objseq.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objseq.setNstatus(getShort(arg0, "nstatus", arg1));
		return objseq;
	}

}
