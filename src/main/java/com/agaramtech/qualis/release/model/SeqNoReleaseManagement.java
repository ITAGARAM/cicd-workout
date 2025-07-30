package com.agaramtech.qualis.release.model;

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

@Entity
@Table(name = "seqnoreleasemanagement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoReleaseManagement extends CustomizedResultsetRowMapper<SeqNoReleaseManagement>
		implements Serializable, RowMapper<SeqNoReleaseManagement> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private int nstatus = Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoReleaseManagement mapRow(ResultSet arg0, int arg1) throws SQLException {

		SeqNoReleaseManagement objSeqNoReleaseManagement = new SeqNoReleaseManagement();

		objSeqNoReleaseManagement.setStablename(getString(arg0, "stablename", arg1));
		objSeqNoReleaseManagement.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objSeqNoReleaseManagement.setNstatus(getInteger(arg0, "nstatus", arg1));

		return objSeqNoReleaseManagement;
	}
}
