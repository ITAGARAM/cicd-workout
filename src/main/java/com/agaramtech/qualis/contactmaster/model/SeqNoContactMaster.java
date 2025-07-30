package com.agaramtech.qualis.contactmaster.model;

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
@Table(name = "seqnocontactmaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class SeqNoContactMaster extends CustomizedResultsetRowMapper<SeqNoContactMaster>
		implements Serializable, RowMapper<SeqNoContactMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SeqNoContactMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqNoContactMaster objSeqNoContactMaster = new SeqNoContactMaster();
		objSeqNoContactMaster.setStablename(getString(arg0, "stablename", arg1));
		objSeqNoContactMaster.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objSeqNoContactMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		return objSeqNoContactMaster;
	}

}
