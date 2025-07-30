package com.agaramtech.qualis.basemaster.model;

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
@Table(name = "seqnobasemaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoBasemaster extends CustomizedResultsetRowMapper implements Serializable, RowMapper<SeqNoBasemaster> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "nstatus", nullable = false)

	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;

	public String getStablename() {
		return stablename;
	}

	public void setStablename(String stablename) {
		this.stablename = stablename;
	}

	public int getNsequenceno() {
		return nsequenceno;
	}

	public void setNsequenceno(int nsequenceno) {
		this.nsequenceno = nsequenceno;
	}

	public short getNstatus() {
		return nstatus;
	}

	public void setNstatus(short nstatus) {
		this.nstatus = nstatus;
	}

	@Override
	public SeqNoBasemaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		SeqNoBasemaster objseq = new SeqNoBasemaster();
		objseq.setStablename(getString(arg0, "stablename", arg1));
		objseq.setNsequenceno(getInteger(arg0, "nsequenceno", arg1));
		objseq.setNstatus(getShort(arg0, "nstatus", arg1));
		return objseq;
	}
}
