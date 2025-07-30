package com.agaramtech.qualis.testmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

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

/**
 * This class is used to map the fields of 'seqnotestmanagement' table of the
 * Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 29- Jun- 2020
 */
@Entity
@Table(name = "seqnotestmanagement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoTestManagement extends CustomizedResultsetRowMapper<SeqNoTestManagement>
		implements Serializable, RowMapper<SeqNoTestManagement> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoTestManagement mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqNoTestManagement objseq = new SeqNoTestManagement();
		objseq.setStablename(getString(arg0, "stablename", arg1));
		objseq.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objseq.setNstatus(getShort(arg0, "nstatus", arg1));
		return objseq;
	}
}
