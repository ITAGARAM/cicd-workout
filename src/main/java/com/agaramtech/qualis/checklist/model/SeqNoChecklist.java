package com.agaramtech.qualis.checklist.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "seqnochecklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoChecklist extends CustomizedResultsetRowMapper<SeqNoChecklist> implements Serializable,RowMapper<SeqNoChecklist> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "stablename", length = 30, nullable = false)
	private String stablename;

	@Column(name = "nsequenceno", nullable = false)
	private int nsequenceno;

	@Column(name = "nstatus", nullable = false)
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Override
	public SeqNoChecklist mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqNoChecklist objSeq = new SeqNoChecklist();
		objSeq.setStablename(StringEscapeUtils.unescapeJava(getString(arg0,"stablename",arg1)));
		objSeq.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeq.setNstatus(getShort(arg0,"nstatus",arg1));
		return objSeq;
	}
	

}
