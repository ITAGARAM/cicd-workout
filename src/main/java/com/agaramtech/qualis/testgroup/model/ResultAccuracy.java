package com.agaramtech.qualis.testgroup.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
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
@Table(name = "resultaccuracy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultAccuracy extends  CustomizedResultsetRowMapper<ResultAccuracy> implements Serializable,RowMapper<ResultAccuracy> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nresultaccuracycode")
	private int nresultaccuracycode;
	
	@Column(name = "sresultaccuracyname", length = 10, nullable = false)
	private String sresultaccuracyname;
	
	@Column(name = "sdescription", length = 255, nullable = false)
	private String sdescription;
	
	@Column(name="dmodifieddate",nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


	@Override
	public ResultAccuracy mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		ResultAccuracy objResultAccuracy = new ResultAccuracy();
		
		objResultAccuracy.setNresultaccuracycode(getInteger(arg0,"nresultaccuracycode",arg1));
		objResultAccuracy.setSresultaccuracyname(StringEscapeUtils.unescapeJava(getString(arg0,"sresultaccuracyname",arg1)));
		objResultAccuracy.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objResultAccuracy.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objResultAccuracy.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objResultAccuracy.setNstatus(getShort(arg0,"nstatus",arg1));
		return objResultAccuracy;
	}

}
