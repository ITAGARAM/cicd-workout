package com.agaramtech.qualis.dynamicpreregdesign.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "seqnoregsubtypeversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SeqNoRegSubTypeVersion extends CustomizedResultsetRowMapper<SeqNoRegSubTypeVersion> implements Serializable,RowMapper<SeqNoRegSubTypeVersion> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "napprovalconfigcode") private short napprovalconfigcode;
	@Column(name = "nsequenceno")private int nsequenceno;
	@Column(name = "nstatus")private short nstatus;
	
	@Override
	public SeqNoRegSubTypeVersion mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SeqNoRegSubTypeVersion objSeqNoRegSubTypeVersion = new SeqNoRegSubTypeVersion();
		objSeqNoRegSubTypeVersion.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		objSeqNoRegSubTypeVersion.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeqNoRegSubTypeVersion.setNstatus(getShort(arg0,"nstatus",arg1));
		return objSeqNoRegSubTypeVersion;
	}

}
