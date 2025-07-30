package com.agaramtech.qualis.registration.model;

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

/**
 * This class is used to map the fields of 'seqnoarnogenerator' table of the Database.
 */
@Entity
@Table(name = "seqnoarnogenerator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoArnoGenerator extends CustomizedResultsetRowMapper<SeqNoArnoGenerator> implements Serializable, RowMapper<SeqNoArnoGenerator> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nseqnoarnogencode")
	private int nseqnoarnogencode;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;

	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	 
	@Transient
    private transient String ssampleformat;
	
	@Transient
	private transient Instant dseqresetdate;
	
	@Transient
	private transient short nperiodcode = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Transient
	private transient int nsubsamplesequenceno;
	
	@Transient
	private transient Map<String,Object> jsondata;
	
	@Transient
	private transient int nregsubtypeversioncode;

	@Override
	public SeqNoArnoGenerator mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final SeqNoArnoGenerator objseq = new SeqNoArnoGenerator();
		
		objseq.setNseqnoarnogencode(getInteger(arg0,"nseqnoarnogencode",arg1));
		objseq.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objseq.setNstatus(getShort(arg0,"nstatus",arg1));
		objseq.setSsampleformat(getString(arg0,"ssampleformat",arg1));
		objseq.setNperiodcode(getShort(arg0,"nperiodcode",arg1));
		objseq.setDseqresetdate(getInstant(arg0,"dseqresetdate",arg1));
		objseq.setNsubsamplesequenceno(getInteger(arg0,"nsubsamplesequenceno",arg1));
		objseq.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objseq.setNregsubtypeversioncode(getInteger(arg0,"nregsubtypeversioncode",arg1));
		
		return objseq;
	}
}
