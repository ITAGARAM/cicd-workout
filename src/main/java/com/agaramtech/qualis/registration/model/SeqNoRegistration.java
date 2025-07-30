package com.agaramtech.qualis.registration.model;

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
 * This class is used to map the fields of 'seqnoregistration' table of the Database.
 */
@Entity
@Table(name = "seqnoregistration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoRegistration extends CustomizedResultsetRowMapper<SeqNoRegistration> implements Serializable, RowMapper<SeqNoRegistration> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


	@Override
	public SeqNoRegistration mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final SeqNoRegistration objSeqNoRegistration = new SeqNoRegistration();
		
		objSeqNoRegistration.setStablename(getString(arg0,"stablename",arg1));
		objSeqNoRegistration.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeqNoRegistration.setNstatus(getShort(arg0,"nstatus",arg1));
		return objSeqNoRegistration;
	}

}
