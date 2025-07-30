package com.agaramtech.qualis.global;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seqformatgenerator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqFormatGenerator extends CustomizedResultsetRowMapper
		implements Serializable, RowMapper<SeqFormatGenerator> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nseqformatno")
	private short nseqformatno;

	@Column(name = "nperiodcode", nullable = false)
	private short nperiodcode;

	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "sformattype", length = 30, nullable = false)
	private String sformattype;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqFormatGenerator mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqFormatGenerator objseq = new SeqFormatGenerator();
		objseq.setNseqformatno(getShort(arg0, "nseqformatno", arg1));
		objseq.setNperiodcode(getShort(arg0, "nperiodcode", arg1));
		objseq.setStablename(getString(arg0, "stablename", arg1));
		objseq.setSformattype(getString(arg0, "sformattype", arg1));
		objseq.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objseq.setNstatus(getShort(arg0, "nstatus", arg1));
		return objseq;
	}

}
