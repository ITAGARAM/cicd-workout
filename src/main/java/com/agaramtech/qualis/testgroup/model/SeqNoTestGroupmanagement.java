package com.agaramtech.qualis.testgroup.model;

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
@Table(name = "seqnotestgroupmanagement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoTestGroupmanagement extends CustomizedResultsetRowMapper<SeqNoTestGroupmanagement>  implements Serializable, RowMapper<SeqNoTestGroupmanagement> {

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
	public SeqNoTestGroupmanagement mapRow(ResultSet arg0, int arg1) throws SQLException {
		SeqNoTestGroupmanagement objseq = new SeqNoTestGroupmanagement();
		objseq.setStablename(getString(arg0,"stablename",arg1));
		objseq.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objseq.setNstatus(getShort(arg0,"nstatus",arg1));
		return objseq;
	}
}
