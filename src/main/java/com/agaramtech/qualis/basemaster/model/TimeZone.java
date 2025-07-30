package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.annotations.ColumnDefault;
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
 * This class is usedd to map the fields with timezone table of database
 */
@Entity
@Table(name = "timezone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TimeZone extends CustomizedResultsetRowMapper<TimeZone> implements Serializable,RowMapper<TimeZone>{

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ntimezonecode")
	private short ntimezonecode;

	@Column(name = "stimezoneid", length = 50, nullable = false)
	private String stimezoneid;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@Column(name = "sdatetimeformat", length = 50, nullable = false)
	private String sdatetimeformat;

	@Column(name = "sgmtoffset", length = 30, nullable = false)
	private String sgmtoffset;

	@Column(name = "nsqldateformat", nullable = false)
	private short nsqldateformat;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	
	@Override
	public TimeZone mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		final TimeZone objTimeZone = new TimeZone();
		
		objTimeZone.setNtimezonecode(getShort(arg0,"ntimezonecode",arg1));
		objTimeZone.setStimezoneid(StringEscapeUtils.unescapeJava(getString(arg0,"stimezoneid",arg1)));
		objTimeZone.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTimeZone.setSdatetimeformat(StringEscapeUtils.unescapeJava(getString(arg0,"sdatetimeformat",arg1)));
		objTimeZone.setNsqldateformat(getShort(arg0,"nsqldateformat",arg1));
		objTimeZone.setSgmtoffset(StringEscapeUtils.unescapeJava(getString(arg0,"sgmtoffset",arg1)));
		objTimeZone.setNstatus(getShort(arg0,"nstatus",arg1));
		objTimeZone.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		
		return objTimeZone;
	}

}
